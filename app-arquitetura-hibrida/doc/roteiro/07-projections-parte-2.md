# 📊 PROJECTION HANDLERS - PARTE 2: IMPLEMENTAÇÃO E CONFIGURAÇÃO
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar a implementação prática de Projection Handlers, configurações avançadas e integração com o sistema de tracking.

---

## 🏗️ **IMPLEMENTAÇÃO DETALHADA**

### **📋 AbstractProjectionHandler**

**Localização**: `com.seguradora.hibrida.projection.AbstractProjectionHandler`

**Funcionalidades Fornecidas:**
```java
public abstract class AbstractProjectionHandler<T extends DomainEvent> 
    implements ProjectionHandler<T> {
    
    private static final Logger log = LoggerFactory.getLogger(AbstractProjectionHandler.class);
    
    @Override
    public final void handle(T event) {
        long startTime = System.currentTimeMillis();
        
        try {
            // Verificar se evento é mais recente
            if (isEventNewer(event, getLastProcessedTimestamp())) {
                doHandle(event);
                recordSuccess(event, System.currentTimeMillis() - startTime);
            } else {
                log.debug("Evento ignorado (não é mais recente): {}", event.getEventType());
            }
            
        } catch (Exception e) {
            recordError(event, System.currentTimeMillis() - startTime, e);
            throw e;
        }
    }
    
    /**
     * Implementação específica do processamento do evento.
     */
    protected abstract void doHandle(T event);
    
    /**
     * Registra sucesso no processamento.
     */
    protected void recordSuccess(T event, long processingTimeMs) {
        log.info("Evento processado com sucesso: {} em {}ms [{}]",
            event.getEventType(), processingTimeMs, getProjectionName());
    }
    
    /**
     * Registra erro no processamento.
     */
    protected void recordError(T event, long processingTimeMs, Exception error) {
        log.error("Erro ao processar evento: {} em {}ms [{}] - {}",
            event.getEventType(), processingTimeMs, getProjectionName(), 
            error.getMessage(), error);
    }
}
```

### **🎯 Implementação Concreta - Exemplo Completo**

```java
@Component
@Slf4j
public class SinistroProjectionHandler extends AbstractProjectionHandler<SinistroEvent> {
    
    private final SinistroQueryRepository repository;
    private final Counter eventsProcessedCounter;
    private final Timer processingTimer;
    
    public SinistroProjectionHandler(SinistroQueryRepository repository,
                                   MeterRegistry meterRegistry) {
        this.repository = repository;
        this.eventsProcessedCounter = Counter.builder("projection.events.processed")
            .tag("projection", "sinistro")
            .register(meterRegistry);
        this.processingTimer = Timer.builder("projection.processing.time")
            .tag("projection", "sinistro")
            .register(meterRegistry);
    }
    
    @Override
    protected void doHandle(SinistroEvent event) {
        Timer.Sample sample = Timer.start();
        
        try {
            Map<String, Object> eventData = extractEventData(event);
            
            switch (event.getEventType()) {
                case "SinistroCriado" -> handleSinistroCriado(eventData);
                case "SinistroAtualizado" -> handleSinistroAtualizado(eventData);
                case "SinistroFinalizado" -> handleSinistroFinalizado(eventData);
                case "ConsultaDetranConcluida" -> handleConsultaDetranConcluida(eventData);
                default -> log.warn("Tipo de evento não tratado: {}", event.getEventType());
            }
            
            eventsProcessedCounter.increment();
            
        } finally {
            sample.stop(processingTimer);
        }
    }
    
    private void handleSinistroCriado(Map<String, Object> eventData) {
        SinistroQueryModel sinistro = new SinistroQueryModel();
        
        // Dados básicos
        sinistro.setId(UUID.fromString((String) eventData.get("aggregateId")));
        sinistro.setProtocolo((String) eventData.get("numeroSinistro"));
        sinistro.setDescricao((String) eventData.get("descricao"));
        sinistro.setStatus("ABERTO");
        sinistro.setTipoSinistro((String) eventData.get("tipoSinistro"));
        
        // Dados temporais
        sinistro.setDataAbertura(Instant.parse((String) eventData.get("timestamp")));
        sinistro.setDataOcorrencia(Instant.parse((String) eventData.get("dataOcorrencia")));
        
        // Dados do segurado
        sinistro.setCpfSegurado((String) eventData.get("cpfSegurado"));
        sinistro.setNomeSegurado((String) eventData.get("nomeSegurado"));
        sinistro.setEmailSegurado((String) eventData.get("emailSegurado"));
        sinistro.setTelefoneSegurado((String) eventData.get("telefoneSegurado"));
        
        // Dados do veículo
        sinistro.setPlaca((String) eventData.get("placa"));
        sinistro.setMarca((String) eventData.get("marca"));
        sinistro.setModelo((String) eventData.get("modelo"));
        sinistro.setCor((String) eventData.get("cor"));
        
        // Dados da apólice
        sinistro.setApoliceNumero((String) eventData.get("apoliceNumero"));
        
        // Valores
        if (eventData.get("valorEstimado") != null) {
            sinistro.setValorEstimado(new BigDecimal(eventData.get("valorEstimado").toString()));
        }
        
        // Localização
        sinistro.setEnderecoOcorrencia((String) eventData.get("enderecoOcorrencia"));
        sinistro.setCidadeOcorrencia((String) eventData.get("cidadeOcorrencia"));
        sinistro.setEstadoOcorrencia((String) eventData.get("estadoOcorrencia"));
        sinistro.setCepOcorrencia((String) eventData.get("cepOcorrencia"));
        
        // Controle
        sinistro.setLastEventId((Long) eventData.get("eventId"));
        sinistro.setCreatedAt(Instant.now());
        sinistro.setUpdatedAt(Instant.now());
        
        repository.save(sinistro);
        
        log.info("Sinistro criado na projeção: {} [{}]", 
            sinistro.getProtocolo(), sinistro.getId());
    }
    
    private void handleSinistroAtualizado(Map<String, Object> eventData) {
        String aggregateId = (String) eventData.get("aggregateId");
        Long eventId = (Long) eventData.get("eventId");
        
        SinistroQueryModel sinistro = repository.findById(UUID.fromString(aggregateId))
            .orElseThrow(() -> new ProjectionException(
                getProjectionName(), 
                "SinistroAtualizado", 
                "Sinistro não encontrado: " + aggregateId));
        
        // Verificar se evento é mais recente
        if (sinistro.getLastEventId() >= eventId) {
            log.debug("Evento SinistroAtualizado ignorado (não é mais recente): {}", aggregateId);
            return;
        }
        
        // Atualizar campos modificáveis
        if (eventData.containsKey("status")) {
            sinistro.setStatus((String) eventData.get("status"));
        }
        
        if (eventData.containsKey("descricao")) {
            sinistro.setDescricao((String) eventData.get("descricao"));
        }
        
        if (eventData.containsKey("valorEstimado")) {
            sinistro.setValorEstimado(new BigDecimal(eventData.get("valorEstimado").toString()));
        }
        
        if (eventData.containsKey("operadorResponsavel")) {
            sinistro.setOperadorResponsavel((String) eventData.get("operadorResponsavel"));
        }
        
        if (eventData.containsKey("prioridade")) {
            sinistro.setPrioridade((String) eventData.get("prioridade"));
        }
        
        // Controle
        sinistro.setLastEventId(eventId);
        sinistro.setUpdatedAt(Instant.now());
        
        repository.save(sinistro);
        
        log.info("Sinistro atualizado na projeção: {} [{}]", 
            sinistro.getProtocolo(), sinistro.getId());
    }
    
    private void handleConsultaDetranConcluida(Map<String, Object> eventData) {
        String aggregateId = (String) eventData.get("aggregateId");
        
        SinistroQueryModel sinistro = repository.findById(UUID.fromString(aggregateId))
            .orElseThrow(() -> new ProjectionException(
                getProjectionName(), 
                "ConsultaDetranConcluida", 
                "Sinistro não encontrado: " + aggregateId));
        
        // Atualizar dados DETRAN
        sinistro.setConsultaDetranRealizada(true);
        sinistro.setConsultaDetranStatus((String) eventData.get("status"));
        sinistro.setConsultaDetranTimestamp(Instant.parse((String) eventData.get("timestamp")));
        
        // Dados retornados pelo DETRAN (se sucesso)
        if ("SUCCESS".equals(eventData.get("status"))) {
            @SuppressWarnings("unchecked")
            Map<String, Object> dadosDetran = (Map<String, Object>) eventData.get("dadosDetran");
            sinistro.setDadosDetran(dadosDetran);
        }
        
        sinistro.setLastEventId((Long) eventData.get("eventId"));
        sinistro.setUpdatedAt(Instant.now());
        
        repository.save(sinistro);
        
        log.info("Consulta DETRAN processada na projeção: {} [{}]", 
            sinistro.getProtocolo(), sinistro.getId());
    }
    
    @Override
    public Class<SinistroEvent> getEventType() {
        return SinistroEvent.class;
    }
    
    @Override
    public String getProjectionName() {
        return "SinistroProjection";
    }
    
    @Override
    public int getOrder() {
        return 10; // Alta prioridade
    }
    
    @Override
    public boolean isAsync() {
        return true;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    @Override
    public int getMaxRetries() {
        return 3;
    }
    
    /**
     * Extrai dados do evento de forma segura.
     */
    private Map<String, Object> extractEventData(DomainEvent event) {
        try {
            // Usar reflexão ou serialização para extrair dados
            ObjectMapper mapper = new ObjectMapper();
            return mapper.convertValue(event, Map.class);
        } catch (Exception e) {
            throw new ProjectionException(getProjectionName(), 
                event.getEventType(), 
                "Erro ao extrair dados do evento: " + e.getMessage());
        }
    }
}
```

---

## ⚙️ **CONFIGURAÇÃO DO SISTEMA**

### **📋 ProjectionConfiguration**

**Localização**: `com.seguradora.hibrida.projection.config.ProjectionConfiguration`

```java
@Configuration
@EnableConfigurationProperties(ProjectionProperties.class)
@ConditionalOnProperty(name = "projection.enabled", havingValue = "true", matchIfMissing = true)
public class ProjectionConfiguration {
    
    @Bean
    public ProjectionRegistry projectionRegistry() {
        return new ProjectionRegistry();
    }
    
    @Bean
    public ProjectionEventProcessor projectionEventProcessor(
            ProjectionRegistry projectionRegistry,
            ProjectionTrackerRepository trackerRepository) {
        return new ProjectionEventProcessor(projectionRegistry, trackerRepository);
    }
    
    @Bean
    @ConditionalOnProperty(name = "projection.parallel", havingValue = "true")
    public TaskExecutor projectionTaskExecutor(ProjectionProperties properties) {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(properties.getThreadPool().getCoreSize());
        executor.setMaxPoolSize(properties.getThreadPool().getMaxSize());
        executor.setQueueCapacity(properties.getThreadPool().getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getThreadPool().getKeepAliveSeconds());
        executor.setThreadNamePrefix(properties.getThreadNamePrefix());
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    @Bean
    public ProjectionHandlerRegistrar projectionHandlerRegistrar(
            ProjectionRegistry registry,
            List<ProjectionHandler<? extends DomainEvent>> handlers) {
        
        return new ProjectionHandlerRegistrar(registry, handlers);
    }
    
    @EventListener
    public void onApplicationReady(ApplicationReadyEvent event) {
        ProjectionRegistry registry = event.getApplicationContext()
            .getBean(ProjectionRegistry.class);
        logRegistryStatistics(registry);
    }
    
    private void logRegistryStatistics(ProjectionRegistry registry) {
        log.info("Projection Registry Statistics:");
        log.info("  - Registered projections: {}", registry.getRegisteredProjectionNames().size());
        log.info("  - Event types covered: {}", registry.getRegisteredEventTypes().size());
        log.info("  - Projections: {}", registry.getRegisteredProjectionNames());
    }
}
```

### **🔧 Propriedades de Configuração**

**Arquivo**: `projection.yml`

```yaml
projection:
  enabled: true
  parallel: true
  batch-size: 100
  detailed-logging: true
  thread-name-prefix: "projection-"
  
  thread-pool:
    core-size: 5
    max-size: 20
    queue-capacity: 1000
    keep-alive-seconds: 60
  
  retry:
    max-attempts: 3
    initial-delay-ms: 1000
    max-delay-ms: 30000
    backoff-multiplier: 2.0
    jitter-percent: 0.1
  
  monitoring:
    enabled: true
    metrics-interval-seconds: 60
    lag-threshold: 1000
    error-rate-threshold: 0.05
```

---

## 📊 **TRACKING E MONITORAMENTO**

### **🎯 ProjectionEventProcessor**

**Funcionalidades Principais:**
```java
@Component
public class ProjectionEventProcessor {
    
    private final ProjectionRegistry projectionRegistry;
    private final ProjectionTrackerRepository trackerRepository;
    private final TaskExecutor taskExecutor;
    
    public void processEvent(DomainEvent event, Long eventId) {
        List<ProjectionHandler<? extends DomainEvent>> handlers = 
            projectionRegistry.getHandlers(event.getClass());
        
        for (ProjectionHandler<DomainEvent> handler : handlers) {
            if (handler.isAsync()) {
                processEventAsync(event, eventId, handler);
            } else {
                processWithHandler(event, eventId, handler, 0);
            }
        }
    }
    
    private CompletableFuture<Void> processEventAsync(DomainEvent event, 
                                                    Long eventId,
                                                    ProjectionHandler<DomainEvent> handler) {
        return CompletableFuture.runAsync(() -> {
            processWithHandler(event, eventId, handler, 0);
        }, taskExecutor);
    }
    
    private void processWithHandler(DomainEvent event, 
                                  Long eventId, 
                                  ProjectionHandler<DomainEvent> handler, 
                                  int attemptNumber) {
        
        ProjectionTracker tracker = getOrCreateTracker(handler.getProjectionName());
        
        try {
            // Processar com timeout
            processWithTimeout(handler, event);
            
            // Atualizar posição
            tracker.updatePosition(eventId);
            trackerRepository.save(tracker);
            
        } catch (Exception e) {
            handleProcessingError(event, eventId, handler, tracker, attemptNumber, e);
        }
    }
    
    private void handleProcessingError(DomainEvent event, 
                                     Long eventId,
                                     ProjectionHandler<DomainEvent> handler,
                                     ProjectionTracker tracker, 
                                     int attemptNumber, 
                                     Exception error) {
        
        tracker.recordFailure(error.getMessage());
        trackerRepository.save(tracker);
        
        if (attemptNumber < handler.getMaxRetries() && handler.isRetryable()) {
            scheduleRetry(event, eventId, handler, attemptNumber + 1);
        } else {
            sendToDeadLetterQueue(event, eventId, handler, error, attemptNumber + 1);
        }
    }
}
```

### **📈 Métricas de Projeção**

```java
@Component
public class ProjectionMetrics {
    
    private final MeterRegistry meterRegistry;
    private final ProjectionTrackerRepository trackerRepository;
    
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void updateProjectionMetrics() {
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        
        for (ProjectionTracker tracker : trackers) {
            String projectionName = tracker.getProjectionName();
            
            // Métricas de posição
            Gauge.builder("projection.position")
                .tag("projection", projectionName)
                .register(meterRegistry, tracker, t -> t.getLastProcessedEventId());
            
            // Métricas de performance
            Gauge.builder("projection.events.processed")
                .tag("projection", projectionName)
                .register(meterRegistry, tracker, t -> t.getEventsProcessed());
            
            Gauge.builder("projection.events.failed")
                .tag("projection", projectionName)
                .register(meterRegistry, tracker, t -> t.getEventsFailed());
            
            // Taxa de erro
            double errorRate = tracker.getErrorRate();
            Gauge.builder("projection.error.rate")
                .tag("projection", projectionName)
                .register(meterRegistry, tracker, t -> errorRate);
        }
    }
}
```

---

## 🔄 **REGISTRO AUTOMÁTICO DE HANDLERS**

### **📋 ProjectionHandlerRegistrar**

```java
@Component
public class ProjectionHandlerRegistrar {
    
    private static final Logger log = LoggerFactory.getLogger(ProjectionHandlerRegistrar.class);
    
    private final ProjectionRegistry registry;
    private final List<ProjectionHandler<? extends DomainEvent>> handlers;
    
    @PostConstruct
    public void registerHandlers() {
        log.info("Registrando {} projection handlers...", handlers.size());
        
        for (ProjectionHandler<? extends DomainEvent> handler : handlers) {
            try {
                registerHandlerSafely(registry, handler);
                log.info("Handler registrado: {} para eventos {}", 
                    handler.getProjectionName(), 
                    handler.getEventType().getSimpleName());
            } catch (Exception e) {
                log.error("Erro ao registrar handler: {}", 
                    handler.getClass().getSimpleName(), e);
            }
        }
        
        log.info("Registro de handlers concluído. Total: {}", registry.getRegisteredProjectionNames().size());
    }
    
    @SuppressWarnings("unchecked")
    private void registerHandlerSafely(ProjectionRegistry registry,
                                     ProjectionHandler<? extends DomainEvent> handler) {
        
        Class<? extends DomainEvent> eventType = extractEventType(handler);
        
        if (eventType != null) {
            registry.registerHandler((ProjectionHandler<DomainEvent>) handler);
        } else {
            throw new IllegalArgumentException(
                "Não foi possível determinar o tipo de evento para: " + 
                handler.getClass().getSimpleName());
        }
    }
    
    private Class<? extends DomainEvent> extractEventType(
            ProjectionHandler<? extends DomainEvent> handler) {
        
        // Tentar obter via método getEventType()
        try {
            return handler.getEventType();
        } catch (Exception e) {
            log.warn("Erro ao obter tipo de evento via getEventType(): {}", e.getMessage());
        }
        
        // Tentar extrair via generics
        return extractEventTypeFromGenericInterface(handler);
    }
}
```

---

## 🎯 **BOAS PRÁTICAS DE IMPLEMENTAÇÃO**

### **✅ Padrões Recomendados**

**1. Estrutura Consistente:**
```java
@Component
public class [Entidade]ProjectionHandler extends AbstractProjectionHandler<[Entidade]Event> {
    
    // Dependências injetadas
    private final [Entidade]QueryRepository repository;
    private final MeterRegistry meterRegistry;
    
    // Construtor com injeção
    public [Entidade]ProjectionHandler(...) { ... }
    
    // Implementação do processamento
    @Override
    protected void doHandle([Entidade]Event event) { ... }
    
    // Métodos auxiliares privados
    private void handle[TipoEvento](Map<String, Object> eventData) { ... }
    
    // Configurações
    @Override
    public String getProjectionName() { return "[Entidade]Projection"; }
}
```

**2. Tratamento de Dados:**
```java
// ✅ Validação de dados
private void handleEventoAtualizado(Map<String, Object> eventData) {
    String aggregateId = (String) eventData.get("aggregateId");
    if (aggregateId == null) {
        throw new ProjectionException(getProjectionName(), 
            "EventoAtualizado", "AggregateId não pode ser null");
    }
    
    // Buscar entidade existente
    Optional<QueryModel> existing = repository.findById(UUID.fromString(aggregateId));
    if (existing.isEmpty()) {
        log.warn("Entidade não encontrada para atualização: {}", aggregateId);
        return; // Ou criar nova se fizer sentido
    }
    
    // Atualizar apenas campos presentes
    QueryModel entity = existing.get();
    if (eventData.containsKey("campo")) {
        entity.setCampo((String) eventData.get("campo"));
    }
}
```

**3. Logging Estruturado:**
```java
@Override
protected void recordSuccess(T event, long processingTimeMs) {
    log.info("Evento processado com sucesso: projection={}, eventType={}, " +
             "aggregateId={}, processingTime={}ms", 
        getProjectionName(), event.getEventType(), 
        event.getAggregateId(), processingTimeMs);
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Handler Completo**

Implemente um `SeguradoProjectionHandler` completo que:

1. **Processe todos os tipos de eventos de segurado**
2. **Mantenha projeção otimizada para consultas**
3. **Implemente todas as boas práticas**
4. **Inclua métricas e logging**

**Eventos a tratar:**
- `SeguradoCriado`
- `SeguradoAtualizado`
- `ContatoAtualizado`
- `EnderecoAtualizado`
- `SeguradoDesativado`

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.projection`
- **Configuração**: `projection.yml`
- **Exemplos**: `SinistroProjectionHandler`, `SeguradoProjectionHandler`
- **Testes**: `ProjectionHandlerIntegrationTest`

---

**📍 Próxima Parte**: [Projections - Parte 3: Query Models e Repositórios](./07-projections-parte-3.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Implementação prática e configuração  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Handler completo com todas as funcionalidades