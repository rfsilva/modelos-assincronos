# 📖 CAPÍTULO 03: CQRS - PARTE 5
## Padrões Avançados e Otimizações

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar padrões avançados de CQRS
- Implementar otimizações de performance
- Resolver problemas comuns (troubleshooting)
- Aplicar boas práticas e lições aprendidas

---

## 🚀 **PADRÕES AVANÇADOS**

### **🔄 CQRS com Sagas**

```java
// Localização: cqrs/saga/SinistroProcessingSaga.java
@Component
public class SinistroProcessingSaga {
    
    private final CommandBus commandBus;
    private final SagaRepository sagaRepository;
    
    /**
     * Saga para processamento completo de sinistro
     */
    @EventHandler
    public void handle(SinistroCriadoEvent event) {
        
        // Iniciar saga
        SinistroSaga saga = new SinistroSaga(event.getAggregateId());
        saga.setStatus(SagaStatus.STARTED);
        saga.setCurrentStep(SagaStep.VALIDAR_SEGURADO);
        
        sagaRepository.save(saga);
        
        // Primeiro comando da saga
        ValidarSeguradoCommand command = new ValidarSeguradoCommand(
            event.getAggregateId(),
            event.getCpfSegurado()
        );
        
        commandBus.sendAsync(command)
            .thenAccept(result -> handleValidacaoSegurado(saga, result))
            .exceptionally(ex -> handleSagaError(saga, ex));
    }
    
    private void handleValidacaoSegurado(SinistroSaga saga, CommandResult result) {
        
        if (result.isSuccess()) {
            // Próximo passo: validar veículo
            saga.setCurrentStep(SagaStep.VALIDAR_VEICULO);
            sagaRepository.save(saga);
            
            ValidarVeiculoCommand command = new ValidarVeiculoCommand(
                saga.getSinistroId(),
                saga.getPlacaVeiculo()
            );
            
            commandBus.sendAsync(command)
                .thenAccept(res -> handleValidacaoVeiculo(saga, res))
                .exceptionally(ex -> handleSagaError(saga, ex));
                
        } else {
            // Falha na validação - compensar
            compensateSaga(saga, "Falha na validação do segurado: " + result.getErrorMessage());
        }
    }
    
    private void handleValidacaoVeiculo(SinistroSaga saga, CommandResult result) {
        
        if (result.isSuccess()) {
            // Próximo passo: consultar DETRAN
            saga.setCurrentStep(SagaStep.CONSULTAR_DETRAN);
            sagaRepository.save(saga);
            
            ConsultarDetranCommand command = new ConsultarDetranCommand(
                saga.getSinistroId(),
                saga.getPlacaVeiculo()
            );
            
            commandBus.sendAsync(command)
                .thenAccept(res -> handleConsultaDetran(saga, res))
                .exceptionally(ex -> handleSagaError(saga, ex));
                
        } else {
            compensateSaga(saga, "Falha na validação do veículo: " + result.getErrorMessage());
        }
    }
    
    private void handleConsultaDetran(SinistroSaga saga, CommandResult result) {
        
        // DETRAN é opcional - continuar mesmo se falhar
        saga.setCurrentStep(SagaStep.FINALIZAR);
        
        FinalizarCriacaoSinistroCommand command = new FinalizarCriacaoSinistroCommand(
            saga.getSinistroId(),
            result.isSuccess() // Indica se DETRAN foi consultado com sucesso
        );
        
        commandBus.sendAsync(command)
            .thenAccept(res -> completeSaga(saga))
            .exceptionally(ex -> handleSagaError(saga, ex));
    }
    
    private void completeSaga(SinistroSaga saga) {
        saga.setStatus(SagaStatus.COMPLETED);
        saga.setCompletedAt(Instant.now());
        sagaRepository.save(saga);
        
        log.info("Saga concluída com sucesso para sinistro: {}", saga.getSinistroId());
    }
    
    private void compensateSaga(SinistroSaga saga, String reason) {
        
        saga.setStatus(SagaStatus.COMPENSATING);
        saga.setErrorMessage(reason);
        sagaRepository.save(saga);
        
        // Executar ações de compensação
        CancelarSinistroCommand compensationCommand = new CancelarSinistroCommand(
            saga.getSinistroId(),
            "Cancelado automaticamente: " + reason
        );
        
        commandBus.sendAsync(compensationCommand)
            .thenAccept(result -> {
                saga.setStatus(SagaStatus.COMPENSATED);
                sagaRepository.save(saga);
            });
    }
    
    private Void handleSagaError(SinistroSaga saga, Throwable ex) {
        log.error("Erro na saga para sinistro {}: {}", saga.getSinistroId(), ex.getMessage());
        compensateSaga(saga, "Erro técnico: " + ex.getMessage());
        return null;
    }
}
```

### **📊 Read Models Hierárquicos**

```java
// Localização: query/model/HierarchicalSinistroView.java
@Entity
@Table(name = "sinistro_hierarchical_view", schema = "projections")
public class HierarchicalSinistroView {
    
    @Id
    private UUID id;
    
    // Dados do sinistro (nível raiz)
    private String protocolo;
    private String status;
    private Instant dataAbertura;
    
    // Segurado (nível 1)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "id", column = @Column(name = "segurado_id")),
        @AttributeOverride(name = "nome", column = @Column(name = "segurado_nome")),
        @AttributeOverride(name = "cpf", column = @Column(name = "segurado_cpf"))
    })
    private SeguradoEmbedded segurado;
    
    // Veículo (nível 1)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "placa", column = @Column(name = "veiculo_placa")),
        @AttributeOverride(name = "marca", column = @Column(name = "veiculo_marca")),
        @AttributeOverride(name = "modelo", column = @Column(name = "veiculo_modelo"))
    })
    private VeiculoEmbedded veiculo;
    
    // Apólice (nível 1)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "numero", column = @Column(name = "apolice_numero")),
        @AttributeOverride(name = "vigenciaInicio", column = @Column(name = "apolice_vigencia_inicio")),
        @AttributeOverride(name = "vigenciaFim", column = @Column(name = "apolice_vigencia_fim"))
    })
    private ApoliceEmbedded apolice;
    
    // Ocorrência (nível 1)
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "dataOcorrencia", column = @Column(name = "ocorrencia_data")),
        @AttributeOverride(name = "endereco", column = @Column(name = "ocorrencia_endereco")),
        @AttributeOverride(name = "descricao", column = @Column(name = "ocorrencia_descricao"))
    })
    private OcorrenciaEmbedded ocorrencia;
    
    // Histórico de status (nível 2)
    @OneToMany(mappedBy = "sinistroId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SinistroStatusHistory> historicoStatus;
    
    // Documentos anexados (nível 2)
    @OneToMany(mappedBy = "sinistroId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SinistroDocumento> documentos;
    
    // Interações (nível 2)
    @OneToMany(mappedBy = "sinistroId", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<SinistroInteracao> interacoes;
    
    // Métodos de conveniência para navegação hierárquica
    public SinistroSummaryView toSummaryView() {
        return SinistroSummaryView.builder()
            .id(id)
            .protocolo(protocolo)
            .status(status)
            .seguradoNome(segurado.getNome())
            .veiculoPlaca(veiculo.getPlaca())
            .dataAbertura(dataAbertura)
            .build();
    }
    
    public SinistroDetailView toDetailView() {
        return SinistroDetailView.builder()
            .id(id)
            .protocolo(protocolo)
            .status(status)
            .segurado(segurado.toView())
            .veiculo(veiculo.toView())
            .apolice(apolice.toView())
            .ocorrencia(ocorrencia.toView())
            .historicoStatus(historicoStatus.stream()
                .map(SinistroStatusHistory::toView)
                .collect(Collectors.toList()))
            .build();
    }
}
```

---

## ⚡ **OTIMIZAÇÕES DE PERFORMANCE**

### **🔄 Batch Processing Otimizado**

```java
// Localização: cqrs/optimization/BatchProjectionProcessor.java
@Component
public class BatchProjectionProcessor {
    
    private final ProjectionRegistry projectionRegistry;
    private final EventStoreRepository eventStoreRepository;
    
    /**
     * Processamento em lote otimizado para múltiplas projeções
     */
    public BatchProcessingResult processBatch(BatchProcessingRequest request) {
        
        BatchProcessingResult result = new BatchProcessingResult();
        result.setStartTime(Instant.now());
        
        try {
            // Buscar eventos em lote
            List<EventStoreEntry> events = loadEventsBatch(request);
            
            if (events.isEmpty()) {
                result.setStatus(BatchProcessingStatus.NO_EVENTS);
                return result;
            }
            
            // Agrupar eventos por aggregate para manter ordem
            Map<String, List<EventStoreEntry>> eventsByAggregate = events.stream()
                .collect(Collectors.groupingBy(
                    EventStoreEntry::getAggregateId,
                    LinkedHashMap::new, // Manter ordem
                    Collectors.toList()
                ));
            
            // Processar cada aggregate sequencialmente
            for (Map.Entry<String, List<EventStoreEntry>> entry : eventsByAggregate.entrySet()) {
                
                String aggregateId = entry.getKey();
                List<EventStoreEntry> aggregateEvents = entry.getValue();
                
                BatchAggregateResult aggregateResult = processAggregateEvents(
                    aggregateId, 
                    aggregateEvents, 
                    request.getProjectionNames()
                );
                
                result.addAggregateResult(aggregateResult);
            }
            
            result.setStatus(BatchProcessingStatus.SUCCESS);
            result.setTotalEvents(events.size());
            result.setTotalAggregates(eventsByAggregate.size());
            
        } catch (Exception e) {
            result.setStatus(BatchProcessingStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    private BatchAggregateResult processAggregateEvents(String aggregateId, 
                                                       List<EventStoreEntry> events,
                                                       Set<String> projectionNames) {
        
        BatchAggregateResult result = new BatchAggregateResult(aggregateId);
        
        // Converter eventos
        List<DomainEvent> domainEvents = events.stream()
            .map(this::convertToDomainEvent)
            .collect(Collectors.toList());
        
        // Processar em cada projeção especificada
        for (String projectionName : projectionNames) {
            
            try {
                ProjectionHandler<DomainEvent> handler = projectionRegistry.getHandler(projectionName);
                
                if (handler != null) {
                    // Processar todos os eventos do aggregate em sequência
                    for (DomainEvent event : domainEvents) {
                        if (handler.supports(event)) {
                            handler.handle(event);
                            result.incrementProcessed(projectionName);
                        }
                    }
                } else {
                    result.addError(projectionName, "Handler não encontrado");
                }
                
            } catch (Exception e) {
                result.addError(projectionName, e.getMessage());
            }
        }
        
        return result;
    }
    
    /**
     * Otimização: Pré-carregamento de dados relacionados
     */
    @Transactional
    public void preloadRelatedData(List<String> aggregateIds) {
        
        // Pré-carregar dados do segurado
        Set<String> cpfs = extractCpfsFromAggregates(aggregateIds);
        seguradoService.preloadByCpfs(cpfs);
        
        // Pré-carregar dados do veículo
        Set<String> placas = extractPlacasFromAggregates(aggregateIds);
        veiculoService.preloadByPlacas(placas);
        
        // Pré-carregar apólices
        apoliceService.preloadByAggregateIds(aggregateIds);
    }
}
```

### **💾 Cache Distribuído Inteligente**

```java
// Localização: cqrs/cache/DistributedCacheManager.java
@Component
public class DistributedCacheManager {
    
    private final RedisTemplate<String, Object> redisTemplate;
    private final CacheMetrics cacheMetrics;
    
    /**
     * Cache com invalidação inteligente baseada em eventos
     */
    @EventHandler
    public void handleSinistroEvent(SinistroEvent event) {
        
        // Invalidar caches relacionados ao sinistro
        String sinistroId = event.getAggregateId();
        
        // Cache direto do sinistro
        invalidateCache("sinistros", sinistroId);
        invalidateCache("sinistros", event.getProtocolo());
        
        // Caches de listas que podem incluir este sinistro
        invalidateCachePattern("sinistros-lista:cpf:" + event.getCpfSegurado() + ":*");
        invalidateCachePattern("sinistros-lista:placa:" + event.getPlaca() + ":*");
        invalidateCachePattern("sinistros-lista:status:" + event.getStatus() + ":*");
        
        // Cache de dashboard (sempre invalidar)
        invalidateCache("dashboard", "sinistros");
        
        // Cache de estatísticas
        invalidateCachePattern("estatisticas:*");
        
        cacheMetrics.recordCacheInvalidation(event.getEventType());
    }
    
    /**
     * Cache com TTL adaptativo baseado na frequência de acesso
     */
    public <T> void cacheWithAdaptiveTtl(String cacheName, String key, T value) {
        
        // Calcular TTL baseado na frequência de acesso
        AccessFrequency frequency = calculateAccessFrequency(cacheName, key);
        Duration ttl = calculateAdaptiveTtl(frequency);
        
        // Armazenar com TTL calculado
        redisTemplate.opsForValue().set(
            buildCacheKey(cacheName, key), 
            value, 
            ttl
        );
        
        cacheMetrics.recordCacheWrite(cacheName, ttl.toSeconds());
    }
    
    private Duration calculateAdaptiveTtl(AccessFrequency frequency) {
        
        switch (frequency) {
            case VERY_HIGH: // > 100 acessos/hora
                return Duration.ofMinutes(60);
                
            case HIGH: // 50-100 acessos/hora
                return Duration.ofMinutes(30);
                
            case MEDIUM: // 10-50 acessos/hora
                return Duration.ofMinutes(15);
                
            case LOW: // 1-10 acessos/hora
                return Duration.ofMinutes(5);
                
            case VERY_LOW: // < 1 acesso/hora
                return Duration.ofMinutes(2);
                
            default:
                return Duration.ofMinutes(10);
        }
    }
    
    /**
     * Cache warming inteligente baseado em padrões de acesso
     */
    @Scheduled(fixedRate = 600000) // A cada 10 minutos
    public void intelligentCacheWarming() {
        
        try {
            // Identificar dados mais acessados
            List<CacheAccessPattern> patterns = cacheMetrics.getTopAccessPatterns(100);
            
            for (CacheAccessPattern pattern : patterns) {
                
                if (pattern.getAccessCount() > 10) { // Apenas dados frequentemente acessados
                    
                    try {
                        warmCacheForPattern(pattern);
                    } catch (Exception e) {
                        log.warn("Erro no cache warming para padrão {}: {}", 
                                pattern.getKey(), e.getMessage());
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erro no cache warming inteligente: {}", e.getMessage());
        }
    }
    
    private void warmCacheForPattern(CacheAccessPattern pattern) {
        
        String[] keyParts = pattern.getKey().split(":");
        
        if (keyParts.length >= 2) {
            String cacheName = keyParts[0];
            String entityId = keyParts[1];
            
            switch (cacheName) {
                case "sinistros":
                    warmSinistroCache(entityId);
                    break;
                    
                case "dashboard":
                    warmDashboardCache();
                    break;
                    
                case "estatisticas":
                    warmEstatisticasCache(entityId);
                    break;
            }
        }
    }
}
```

---

## 🔧 **TROUBLESHOOTING COMUM**

### **🚨 Problemas Frequentes e Soluções**

```java
// Localização: cqrs/troubleshooting/CQRSTroubleshooter.java
@Component
public class CQRSTroubleshooter {
    
    /**
     * Diagnóstico completo do sistema CQRS
     */
    public TroubleshootingReport diagnoseSystem() {
        
        TroubleshootingReport report = new TroubleshootingReport();
        
        // 1. Verificar conectividade entre Command e Query
        report.addCheck("Command-Query Connectivity", checkCommandQueryConnectivity());
        
        // 2. Verificar lag das projeções
        report.addCheck("Projection Lag", checkProjectionLag());
        
        // 3. Verificar performance de consultas
        report.addCheck("Query Performance", checkQueryPerformance());
        
        // 4. Verificar integridade dos dados
        report.addCheck("Data Integrity", checkDataIntegrity());
        
        // 5. Verificar cache
        report.addCheck("Cache Health", checkCacheHealth());
        
        return report;
    }
    
    private DiagnosticResult checkCommandQueryConnectivity() {
        
        try {
            // Testar Command Side
            CommandBusStatistics commandStats = commandBus.getStatistics();
            
            // Testar Query Side
            long queryCount = sinistroQueryRepository.count();
            
            // Testar Event Bus
            boolean eventBusHealthy = eventBus.isHealthy();
            
            if (!eventBusHealthy) {
                return DiagnosticResult.error(
                    "Event Bus não está saudável",
                    Map.of("recommendation", "Verificar conectividade com Kafka")
                );
            }
            
            if (commandStats.getErrorRate() > 0.1) { // > 10% erro
                return DiagnosticResult.warning(
                    "Alta taxa de erro no Command Side",
                    Map.of(
                        "error_rate", commandStats.getErrorRate(),
                        "recommendation", "Verificar logs de erro dos Command Handlers"
                    )
                );
            }
            
            return DiagnosticResult.ok("Conectividade Command-Query normal");
            
        } catch (Exception e) {
            return DiagnosticResult.error(
                "Erro na verificação de conectividade: " + e.getMessage(),
                Map.of("exception", e.getClass().getSimpleName())
            );
        }
    }
    
    private DiagnosticResult checkProjectionLag() {
        
        try {
            ConsistencyReport consistency = consistencyMonitor.checkConsistency();
            
            List<ConsistencyStatus> laggedProjections = consistency.getProjectionStatuses()
                .stream()
                .filter(status -> status.getLag() > 100)
                .collect(Collectors.toList());
            
            if (laggedProjections.isEmpty()) {
                return DiagnosticResult.ok("Todas as projeções estão sincronizadas");
            }
            
            if (laggedProjections.stream().anyMatch(status -> status.getLag() > 1000)) {
                return DiagnosticResult.error(
                    "Projeções com lag crítico detectadas",
                    Map.of(
                        "lagged_projections", laggedProjections.size(),
                        "max_lag", laggedProjections.stream()
                            .mapToLong(ConsistencyStatus::getLag)
                            .max().orElse(0),
                        "recommendation", "Executar sincronização manual ou rebuild"
                    )
                );
            }
            
            return DiagnosticResult.warning(
                "Algumas projeções com lag moderado",
                Map.of(
                    "lagged_projections", laggedProjections.size(),
                    "recommendation", "Monitorar evolução do lag"
                )
            );
            
        } catch (Exception e) {
            return DiagnosticResult.error(
                "Erro na verificação de lag: " + e.getMessage()
            );
        }
    }
    
    private DiagnosticResult checkQueryPerformance() {
        
        try {
            // Testar consultas comuns
            long startTime = System.currentTimeMillis();
            
            // Consulta simples por ID
            sinistroQueryService.buscarPorId(UUID.randomUUID());
            long simpleQueryTime = System.currentTimeMillis() - startTime;
            
            // Consulta complexa com filtros
            startTime = System.currentTimeMillis();
            SinistroFilter filter = new SinistroFilter();
            filter.setStatus("ABERTO");
            sinistroQueryService.listar(filter, PageRequest.of(0, 10));
            long complexQueryTime = System.currentTimeMillis() - startTime;
            
            // Consulta de dashboard
            startTime = System.currentTimeMillis();
            sinistroQueryService.obterDashboard();
            long dashboardQueryTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> metrics = Map.of(
                "simple_query_ms", simpleQueryTime,
                "complex_query_ms", complexQueryTime,
                "dashboard_query_ms", dashboardQueryTime
            );
            
            if (complexQueryTime > 1000 || dashboardQueryTime > 2000) {
                return DiagnosticResult.warning(
                    "Performance de consultas degradada",
                    Map.of(
                        "metrics", metrics,
                        "recommendation", "Verificar índices e otimizar consultas"
                    )
                );
            }
            
            return DiagnosticResult.ok("Performance de consultas normal", metrics);
            
        } catch (Exception e) {
            return DiagnosticResult.error(
                "Erro na verificação de performance: " + e.getMessage()
            );
        }
    }
    
    /**
     * Auto-correção de problemas comuns
     */
    public AutoFixResult attemptAutoFix(TroubleshootingReport report) {
        
        AutoFixResult result = new AutoFixResult();
        
        for (DiagnosticResult diagnostic : report.getResults()) {
            
            if (diagnostic.getSeverity() == DiagnosticSeverity.ERROR) {
                
                switch (diagnostic.getCheckName()) {
                    case "Projection Lag":
                        result.addAction(fixProjectionLag());
                        break;
                        
                    case "Cache Health":
                        result.addAction(fixCacheIssues());
                        break;
                        
                    case "Data Integrity":
                        result.addAction(fixDataIntegrityIssues());
                        break;
                }
            }
        }
        
        return result;
    }
    
    private AutoFixAction fixProjectionLag() {
        
        try {
            // Tentar sincronização automática
            projectionSynchronizer.synchronizeProjections();
            
            return AutoFixAction.success(
                "Projection Lag", 
                "Sincronização automática executada"
            );
            
        } catch (Exception e) {
            return AutoFixAction.failed(
                "Projection Lag", 
                "Falha na sincronização: " + e.getMessage()
            );
        }
    }
}
```

---

## 📚 **BOAS PRÁTICAS E LIÇÕES APRENDIDAS**

### **✅ Checklist de Implementação CQRS**

```markdown
## Command Side
- [ ] Commands são imutáveis e expressam intenção
- [ ] Command Handlers são idempotentes
- [ ] Validação robusta antes de processar comandos
- [ ] Timeout apropriado para cada tipo de comando
- [ ] Métricas de performance e erro
- [ ] Tratamento de concorrência otimista

## Query Side
- [ ] Read Models desnormalizados para performance
- [ ] Índices otimizados para consultas frequentes
- [ ] Cache inteligente com invalidação automática
- [ ] Múltiplas projeções para diferentes necessidades
- [ ] Consultas paginadas para grandes volumes
- [ ] Full-text search quando necessário

## Sincronização
- [ ] Monitoramento de lag entre Command e Query
- [ ] Alertas para inconsistências críticas
- [ ] Recovery automático de projeções com falha
- [ ] Estratégias de retry com backoff exponencial
- [ ] Snapshots para projeções complexas

## Performance
- [ ] Processamento em lote para alta throughput
- [ ] Cache distribuído com TTL adaptativo
- [ ] Pré-carregamento de dados relacionados
- [ ] Otimização de consultas com EXPLAIN ANALYZE
- [ ] Particionamento de dados quando necessário

## Monitoramento
- [ ] Métricas de lag, throughput e erro
- [ ] Health checks para todos os componentes
- [ ] Dashboards de observabilidade
- [ ] Alertas proativos
- [ ] Logs estruturados para troubleshooting
```

### **🎯 Quando Usar CQRS**

```java
/**
 * CQRS é apropriado quando:
 * 
 * ✅ Diferentes modelos de leitura e escrita
 * ✅ Necessidade de alta performance em consultas
 * ✅ Múltiplas representações dos mesmos dados
 * ✅ Escalabilidade independente de leitura/escrita
 * ✅ Auditoria completa necessária
 * ✅ Domínio complexo com regras de negócio ricas
 * 
 * ❌ CQRS NÃO é apropriado quando:
 * 
 * ❌ Aplicação CRUD simples
 * ❌ Equipe pequena sem experiência
 * ❌ Consistência imediata é crítica
 * ❌ Overhead de complexidade não se justifica
 * ❌ Poucos usuários/baixo volume
 */
```

---

## 🧪 **EXERCÍCIO FINAL**

### **🎯 Objetivo**: Implementar CQRS completo para nova entidade

#### **Passo 1: Criar Command Side**
```java
// Implementar comando, handler e aggregate para "Perito"
public class AtribuirPeritoCommand implements Command { /* ... */ }

@Component
public class AtribuirPeritoCommandHandler implements CommandHandler<AtribuirPeritoCommand> {
    // Implementar lógica completa
}
```

#### **Passo 2: Criar Query Side**
```java
// Implementar query model e repository
@Entity
public class PeritoQueryModel { /* ... */ }

public interface PeritoQueryRepository extends JpaRepository<PeritoQueryModel, UUID> {
    // Consultas otimizadas
}
```

#### **Passo 3: Implementar Projeção**
```java
@Component
public class PeritoProjectionHandler extends AbstractProjectionHandler<PeritoEvent> {
    // Sincronização Command -> Query
}
```

#### **Passo 4: Testar Integração Completa**
```java
@Test
public void testarFluxoCompletoPerito() {
    // 1. Enviar comando
    // 2. Verificar eventos gerados
    // 3. Aguardar projeção
    // 4. Consultar dados
    // 5. Verificar consistência
}
```

---

## 🎓 **CONCLUSÃO DO CAPÍTULO CQRS**

### **✅ Competências Adquiridas:**

1. **Fundamentos** - Separação clara entre Command e Query
2. **Implementação** - Command Handlers e Query Services robustos
3. **Otimização** - Cache, índices e consultas eficientes
4. **Consistência** - Monitoramento e correção de inconsistências
5. **Troubleshooting** - Diagnóstico e correção de problemas

### **🚀 Próximo Capítulo**

No **Capítulo 04 - Domain-Driven Design**, vamos explorar:
- Modelagem de domínio rica
- Bounded Contexts e Aggregates
- Domain Services e Value Objects
- Linguagem ubíqua e padrões táticos

---

**📖 Capítulo elaborado por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Total:** 4 horas (5 partes × 48 minutos)  
**📋 Pré-requisitos:** Capítulos 01-02 completos