# 📊 ETAPA 09: MONITORAMENTO E MÉTRICAS
## Observabilidade Completa - Métricas, Health Checks e Alertas

### 🎯 **OBJETIVO DA ETAPA**

Implementar observabilidade completa do domínio, configurando métricas customizadas, health checks, dashboards e alertas para garantir visibilidade operacional e detecção proativa de problemas.

**⏱️ Duração Estimada:** 2-3 horas  
**👥 Participantes:** Desenvolvedor + SRE/DevOps  
**📋 Pré-requisitos:** Etapas 01-08 concluídas

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **📈 1. MÉTRICAS CUSTOMIZADAS**

#### **🎯 Métricas de Domínio:**
```java
@Component
public class [Dominio]Metrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter comandosProcessados;
    private final Counter eventosPublicados;
    private final Timer tempoProcessamento;
    private final Gauge agregadosAtivos;
    private final DistributionSummary tamanhoEvento;
    
    // ========== CONSTRUTOR ==========
    public [Dominio]Metrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Contadores
        this.comandosProcessados = Counter.builder("dominio.comandos.processados")
            .tag("dominio", "[dominio]")
            .description("Total de comandos processados no domínio")
            .register(meterRegistry);
            
        this.eventosPublicados = Counter.builder("dominio.eventos.publicados")
            .tag("dominio", "[dominio]")
            .description("Total de eventos publicados no domínio")
            .register(meterRegistry);
        
        // Timers
        this.tempoProcessamento = Timer.builder("dominio.processamento.tempo")
            .tag("dominio", "[dominio]")
            .description("Tempo de processamento de comandos")
            .publishPercentiles(0.5, 0.95, 0.99)
            .minimumExpectedValue(Duration.ofMillis(10))
            .maximumExpectedValue(Duration.ofSeconds(10))
            .register(meterRegistry);
        
        // Gauges
        this.agregadosAtivos = Gauge.builder("dominio.agregados.ativos")
            .tag("dominio", "[dominio]")
            .description("Número de agregados ativos em memória")
            .register(meterRegistry, this, [Dominio]Metrics::contarAgregadosAtivos);
        
        // Distribution Summary
        this.tamanhoEvento = DistributionSummary.builder("dominio.evento.tamanho")
            .tag("dominio", "[dominio]")
            .description("Tamanho dos eventos em bytes")
            .baseUnit("bytes")
            .publishPercentiles(0.5, 0.95, 0.99)
            .register(meterRegistry);
    }
    
    // ========== REGISTRO DE MÉTRICAS ==========
    
    public void registrarComandoProcessado(String tipoComando, boolean sucesso) {
        comandosProcessados.increment(
            Tags.of(
                "tipo", tipoComando,
                "status", sucesso ? "sucesso" : "falha"
            )
        );
    }
    
    public void registrarEventoPublicado(String tipoEvento, int tamanhoBytes) {
        eventosPublicados.increment(Tags.of("tipo", tipoEvento));
        tamanhoEvento.record(tamanhoBytes);
    }
    
    public Timer.Sample iniciarTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void pararTimer(Timer.Sample sample, String operacao) {
        sample.stop(tempoProcessamento.withTags("operacao", operacao));
    }
    
    public void registrarErro(String tipoErro, String contexto) {
        meterRegistry.counter("dominio.erros",
            "dominio", "[dominio]",
            "tipo", tipoErro,
            "contexto", contexto)
            .increment();
    }
    
    public void registrarViolacaoRegra(String nomeRegra) {
        meterRegistry.counter("dominio.regras.violacoes",
            "dominio", "[dominio]",
            "regra", nomeRegra)
            .increment();
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    
    private double contarAgregadosAtivos() {
        // Implementar lógica para contar agregados em memória/cache
        // Pode usar cache do Aggregate Repository
        return 0.0;
    }
}
```

#### **📊 Métricas de Infraestrutura:**
```java
@Component
public class [Dominio]InfrastructureMetrics {
    
    private final MeterRegistry meterRegistry;
    private final EventStore eventStore;
    private final SnapshotStore snapshotStore;
    private final EventBus eventBus;
    
    public [Dominio]InfrastructureMetrics(
            MeterRegistry meterRegistry,
            EventStore eventStore,
            SnapshotStore snapshotStore,
            EventBus eventBus) {
        
        this.meterRegistry = meterRegistry;
        this.eventStore = eventStore;
        this.snapshotStore = snapshotStore;
        this.eventBus = eventBus;
        
        registrarGauges();
    }
    
    private void registrarGauges() {
        // Event Store
        Gauge.builder("eventstore.eventos.total")
            .tag("dominio", "[dominio]")
            .register(meterRegistry, eventStore, 
                     store -> store.countEvents("[dominio]"));
        
        Gauge.builder("eventstore.tamanho.mb")
            .tag("dominio", "[dominio]")
            .register(meterRegistry, eventStore,
                     store -> store.getStorageSize("[dominio]") / 1024.0 / 1024.0);
        
        // Snapshot Store
        Gauge.builder("snapshotstore.snapshots.total")
            .tag("dominio", "[dominio]")
            .register(meterRegistry, snapshotStore,
                     store -> store.countSnapshots("[dominio]"));
        
        // Event Bus
        Gauge.builder("eventbus.fila.tamanho")
            .tag("dominio", "[dominio]")
            .register(meterRegistry, eventBus,
                     bus -> bus.getQueueSize("[dominio]"));
        
        Gauge.builder("eventbus.dlq.tamanho")
            .tag("dominio", "[dominio]")
            .register(meterRegistry, eventBus,
                     bus -> bus.getDeadLetterQueueSize("[dominio]"));
    }
    
    public void registrarEventoArmazenado(long tempoMs, int tamanhoBytes) {
        meterRegistry.timer("eventstore.gravacao.tempo",
            "dominio", "[dominio]")
            .record(tempoMs, TimeUnit.MILLISECONDS);
            
        meterRegistry.summary("eventstore.gravacao.tamanho",
            "dominio", "[dominio]")
            .record(tamanhoBytes);
    }
    
    public void registrarSnapshotCriado(long tempoMs, long numeroEventos) {
        meterRegistry.timer("snapshotstore.criacao.tempo",
            "dominio", "[dominio]")
            .record(tempoMs, TimeUnit.MILLISECONDS);
            
        meterRegistry.summary("snapshotstore.eventos.economizados",
            "dominio", "[dominio]")
            .record(numeroEventos);
    }
}
```

#### **✅ Checklist de Métricas:**
- [ ] **Contadores** para comandos, eventos e erros
- [ ] **Timers** para operações críticas
- [ ] **Gauges** para estado atual do sistema
- [ ] **Distribution Summaries** para tamanhos e quantidades
- [ ] **Tags apropriadas** para filtros e agregações

---

### **🏥 2. HEALTH CHECKS**

#### **📋 Health Check de Domínio:**
```java
@Component
public class [Dominio]HealthIndicator implements HealthIndicator {
    
    private final AggregateRepository<[Dominio]Aggregate> repository;
    private final CommandBus commandBus;
    private final EventBus eventBus;
    private final EventStore eventStore;
    private final [Dominio]Metrics metrics;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Verificar componentes críticos
            boolean repositoryHealthy = checkRepository();
            boolean commandBusHealthy = checkCommandBus();
            boolean eventBusHealthy = checkEventBus();
            boolean eventStoreHealthy = checkEventStore();
            boolean metricsHealthy = checkMetrics();
            
            // Determinar status geral
            boolean allHealthy = repositoryHealthy && commandBusHealthy && 
                               eventBusHealthy && eventStoreHealthy && metricsHealthy;
            
            if (allHealthy) {
                builder.up();
            } else {
                builder.down();
            }
            
            // Adicionar detalhes
            builder
                .withDetail("repository", repositoryHealthy ? "UP" : "DOWN")
                .withDetail("commandBus", commandBusHealthy ? "UP" : "DOWN")
                .withDetail("eventBus", eventBusHealthy ? "UP" : "DOWN")
                .withDetail("eventStore", eventStoreHealthy ? "UP" : "DOWN")
                .withDetail("metrics", metricsHealthy ? "UP" : "DOWN")
                .withDetail("lastCheck", Instant.now());
            
            // Adicionar estatísticas
            adicionarEstatisticas(builder);
            
            return builder.build();
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("exception", e.getClass().getSimpleName())
                .build();
        }
    }
    
    private boolean checkRepository() {
        try {
            repository.getStatistics();
            return true;
        } catch (Exception e) {
            log.error("Repository health check failed", e);
            return false;
        }
    }
    
    private boolean checkCommandBus() {
        try {
            return commandBus.isHealthy() && 
                   commandBus.hasHandler([Criar][Dominio]Command.class);
        } catch (Exception e) {
            log.error("Command Bus health check failed", e);
            return false;
        }
    }
    
    private boolean checkEventBus() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            double errorRate = stats.getErrorRate();
            return errorRate < 0.1; // Menos de 10% de erros
        } catch (Exception e) {
            log.error("Event Bus health check failed", e);
            return false;
        }
    }
    
    private boolean checkEventStore() {
        try {
            return eventStore.isHealthy();
        } catch (Exception e) {
            log.error("Event Store health check failed", e);
            return false;
        }
    }
    
    private boolean checkMetrics() {
        try {
            // Verificar se métricas estão sendo coletadas
            return metrics != null;
        } catch (Exception e) {
            log.error("Metrics health check failed", e);
            return false;
        }
    }
    
    private void adicionarEstatisticas(Health.Builder builder) {
        try {
            builder
                .withDetail("comandosProcessados", getComandosProcessados())
                .withDetail("eventosPublicados", getEventosPublicados())
                .withDetail("errosUltima1h", getErrosUltima1h())
                .withDetail("agregadosAtivos", getAgregadosAtivos());
        } catch (Exception e) {
            log.warn("Erro ao adicionar estatísticas ao health check", e);
        }
    }
    
    private long getComandosProcessados() {
        // Obter do metrics registry
        return 0L;
    }
    
    private long getEventosPublicados() {
        // Obter do metrics registry
        return 0L;
    }
    
    private long getErrosUltima1h() {
        // Obter do metrics registry
        return 0L;
    }
    
    private long getAgregadosAtivos() {
        // Obter do cache/repository
        return 0L;
    }
}
```

#### **🔍 Health Check Detalhado:**
```java
@Component
public class [Dominio]DetailedHealthIndicator implements HealthIndicator {
    
    private final [Dominio]HealthIndicator basicHealth;
    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    private final RedisTemplate<String, Object> redisTemplate;
    
    @Override
    public Health health() {
        Health.Builder builder = new Health.Builder();
        
        try {
            // Health check básico
            Health basicHealthResult = basicHealth.health();
            builder.status(basicHealthResult.getStatus());
            basicHealthResult.getDetails().forEach(builder::withDetail);
            
            // Verificações adicionais
            checkDatabases(builder);
            checkCache(builder);
            checkPerformance(builder);
            
            return builder.build();
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private void checkDatabases(Health.Builder builder) {
        // Write Database
        try {
            checkDataSource(writeDataSource);
            builder.withDetail("writeDatabase", "UP");
        } catch (Exception e) {
            builder.down().withDetail("writeDatabase", "DOWN: " + e.getMessage());
        }
        
        // Read Database
        try {
            checkDataSource(readDataSource);
            builder.withDetail("readDatabase", "UP");
        } catch (Exception e) {
            builder.down().withDetail("readDatabase", "DOWN: " + e.getMessage());
        }
    }
    
    private void checkDataSource(DataSource dataSource) throws SQLException {
        try (Connection conn = dataSource.getConnection();
             PreparedStatement stmt = conn.prepareStatement("SELECT 1");
             ResultSet rs = stmt.executeQuery()) {
            
            if (!rs.next()) {
                throw new SQLException("Query não retornou resultado");
            }
        }
    }
    
    private void checkCache(Health.Builder builder) {
        try {
            redisTemplate.opsForValue().set("health:check", "ok", Duration.ofSeconds(5));
            String value = (String) redisTemplate.opsForValue().get("health:check");
            
            if ("ok".equals(value)) {
                builder.withDetail("cache", "UP");
            } else {
                builder.down().withDetail("cache", "DOWN: valor incorreto");
            }
        } catch (Exception e) {
            builder.down().withDetail("cache", "DOWN: " + e.getMessage());
        }
    }
    
    private void checkPerformance(Health.Builder builder) {
        // Verificar se performance está dentro dos SLAs
        // Exemplo: tempo médio de processamento de comandos
        
        builder
            .withDetail("performanceStatus", "NORMAL")
            .withDetail("avgCommandTime", "45ms")
            .withDetail("avgEventTime", "12ms");
    }
}
```

#### **✅ Checklist de Health Checks:**
- [ ] **Health indicator** do domínio implementado
- [ ] **Verificações** de todos os componentes críticos
- [ ] **Estatísticas** incluídas nos detalhes
- [ ] **Health checks** detalhados configurados
- [ ] **Endpoints** de health expostos

---

### **🎨 3. DASHBOARDS E VISUALIZAÇÃO**

#### **📊 Dashboard de Domínio (Grafana):**
```yaml
# dashboard-[dominio].json (estrutura simplificada)
dashboard:
  title: "[Domínio] - Monitoramento"
  panels:
    - title: "Comandos Processados"
      type: graph
      targets:
        - expr: rate(dominio_comandos_processados_total{dominio="[dominio]"}[5m])
      
    - title: "Eventos Publicados"
      type: graph
      targets:
        - expr: rate(dominio_eventos_publicados_total{dominio="[dominio]"}[5m])
    
    - title: "Tempo de Processamento (p95)"
      type: graph
      targets:
        - expr: histogram_quantile(0.95, rate(dominio_processamento_tempo_bucket{dominio="[dominio]"}[5m]))
    
    - title: "Taxa de Erros"
      type: graph
      targets:
        - expr: rate(dominio_erros_total{dominio="[dominio]"}[5m])
    
    - title: "Event Store - Tamanho"
      type: gauge
      targets:
        - expr: eventstore_tamanho_mb{dominio="[dominio]"}
    
    - title: "Agregados Ativos"
      type: stat
      targets:
        - expr: dominio_agregados_ativos{dominio="[dominio]"}
    
    - title: "Dead Letter Queue"
      type: stat
      targets:
        - expr: eventbus_dlq_tamanho{dominio="[dominio]"}
    
    - title: "Health Status"
      type: stat
      targets:
        - expr: up{job="[dominio]-service"}
```

#### **📈 Queries Prometheus Úteis:**
```yaml
# Comandos por segundo
rate(dominio_comandos_processados_total[1m])

# Eventos por segundo
rate(dominio_eventos_publicados_total[1m])

# Percentil 95 de tempo de processamento
histogram_quantile(0.95, rate(dominio_processamento_tempo_bucket[5m]))

# Taxa de erro (%)
(rate(dominio_comandos_processados_total{status="falha"}[5m]) / 
 rate(dominio_comandos_processados_total[5m])) * 100

# Tamanho médio de eventos
avg(dominio_evento_tamanho)

# Agregados ativos
dominio_agregados_ativos

# Event Store - crescimento por hora
rate(eventstore_eventos_total[1h])

# Dead Letter Queue - alertar se > 10
eventbus_dlq_tamanho > 10
```

#### **✅ Checklist de Dashboards:**
- [ ] **Dashboard principal** do domínio criado
- [ ] **Panels** para métricas chave configurados
- [ ] **Alertas visuais** para thresholds
- [ ] **Links** para dashboards relacionados
- [ ] **Documentação** do dashboard

---

### **🚨 4. ALERTAS E NOTIFICAÇÕES**

#### **⚠️ Regras de Alerta (Prometheus):**
```yaml
# alerts-[dominio].yml
groups:
  - name: [dominio]_alerts
    interval: 30s
    rules:
      # Taxa de erro alta
      - alert: [Dominio]HighErrorRate
        expr: |
          (rate(dominio_comandos_processados_total{status="falha"}[5m]) / 
           rate(dominio_comandos_processados_total[5m])) > 0.05
        for: 2m
        labels:
          severity: warning
          dominio: [dominio]
        annotations:
          summary: "Alta taxa de erro no domínio [Dominio]"
          description: "Taxa de erro: {{ $value | humanizePercentage }}"
      
      # Tempo de processamento alto
      - alert: [Dominio]SlowProcessing
        expr: |
          histogram_quantile(0.95, 
            rate(dominio_processamento_tempo_bucket[5m])) > 5
        for: 3m
        labels:
          severity: warning
          dominio: [dominio]
        annotations:
          summary: "Processamento lento no domínio [Dominio]"
          description: "P95: {{ $value }}s (threshold: 5s)"
      
      # Dead Letter Queue crescendo
      - alert: [Dominio]DLQGrowing
        expr: eventbus_dlq_tamanho{dominio="[dominio]"} > 50
        for: 5m
        labels:
          severity: critical
          dominio: [dominio]
        annotations:
          summary: "Dead Letter Queue crescendo no domínio [Dominio]"
          description: "DLQ size: {{ $value }} eventos"
      
      # Event Store crescendo muito rápido
      - alert: [Dominio]EventStoreGrowthHigh
        expr: |
          rate(eventstore_eventos_total{dominio="[dominio]"}[1h]) > 10000
        for: 10m
        labels:
          severity: warning
          dominio: [dominio]
        annotations:
          summary: "Event Store crescendo rapidamente"
          description: "Taxa: {{ $value }} eventos/hora"
      
      # Service Down
      - alert: [Dominio]ServiceDown
        expr: up{job="[dominio]-service"} == 0
        for: 1m
        labels:
          severity: critical
          dominio: [dominio]
        annotations:
          summary: "Serviço [Dominio] está DOWN"
          description: "O serviço não está respondendo há mais de 1 minuto"
      
      # Muitas violações de regras de negócio
      - alert: [Dominio]HighBusinessRuleViolations
        expr: |
          rate(dominio_regras_violacoes_total{dominio="[dominio]"}[5m]) > 10
        for: 3m
        labels:
          severity: warning
          dominio: [dominio]
        annotations:
          summary: "Muitas violações de regras de negócio"
          description: "Taxa: {{ $value }} violações/s"
```

#### **📧 Configuração de Notificações:**
```yaml
# alertmanager.yml
route:
  group_by: ['dominio', 'severity']
  group_wait: 30s
  group_interval: 5m
  repeat_interval: 4h
  receiver: 'default-receiver'
  
  routes:
    - match:
        severity: critical
        dominio: [dominio]
      receiver: 'pagerduty-critical'
      continue: true
    
    - match:
        severity: warning
        dominio: [dominio]
      receiver: 'slack-warnings'

receivers:
  - name: 'default-receiver'
    email_configs:
      - to: 'team@empresa.com'
        subject: '[{{ .GroupLabels.severity | toUpper }}] {{ .GroupLabels.alertname }}'
  
  - name: 'pagerduty-critical'
    pagerduty_configs:
      - service_key: '<pagerduty-key>'
        description: '{{ .CommonAnnotations.summary }}'
  
  - name: 'slack-warnings'
    slack_configs:
      - api_url: '<slack-webhook-url>'
        channel: '#alerts-[dominio]'
        title: '{{ .CommonAnnotations.summary }}'
        text: '{{ .CommonAnnotations.description }}'
```

#### **✅ Checklist de Alertas:**
- [ ] **Regras de alerta** definidas e configuradas
- [ ] **Thresholds** apropriados estabelecidos
- [ ] **Severidades** corretamente classificadas
- [ ] **Notificações** para canais apropriados
- [ ] **Runbooks** documentados para cada alerta

---

### **📝 5. LOGGING E RASTREAMENTO**

#### **🔍 Structured Logging:**
```java
@Component
@Slf4j
public class [Dominio]LoggingAspect {
    
    private final [Dominio]Metrics metrics;
    
    @Around("@annotation(LogDomainOperation)")
    public Object logOperation(ProceedingJoinPoint joinPoint) throws Throwable {
        String operacao = joinPoint.getSignature().getName();
        String classe = joinPoint.getTarget().getClass().getSimpleName();
        
        MDC.put("operacao", operacao);
        MDC.put("classe", classe);
        MDC.put("dominio", "[dominio]");
        
        Timer.Sample sample = metrics.iniciarTimer();
        
        try {
            log.info("Iniciando operação: {} em {}", operacao, classe);
            
            Object resultado = joinPoint.proceed();
            
            log.info("Operação concluída com sucesso: {} em {}", operacao, classe);
            metrics.registrarComandoProcessado(operacao, true);
            
            return resultado;
            
        } catch (Exception e) {
            log.error("Erro na operação: {} em {}: {}", operacao, classe, e.getMessage(), e);
            metrics.registrarComandoProcessado(operacao, false);
            metrics.registrarErro(e.getClass().getSimpleName(), operacao);
            throw e;
            
        } finally {
            metrics.pararTimer(sample, operacao);
            MDC.clear();
        }
    }
}
```

#### **🔗 Distributed Tracing:**
```java
@Component
public class [Dominio]TracingConfiguration {
    
    @Bean
    public Sampler defaultSampler() {
        // Amostragem de 10% em produção, 100% em dev
        return Sampler.traceIdRatioBased(
            "production".equals(profile) ? 0.1 : 1.0
        );
    }
    
    @Bean
    public SpanCustomizer [dominio]SpanCustomizer() {
        return span -> {
            span.tag("dominio", "[dominio]");
            span.tag("versao", versao);
        };
    }
}
```

#### **✅ Checklist de Logging:**
- [ ] **Structured logging** implementado
- [ ] **MDC** configurado com informações de contexto
- [ ] **Correlation IDs** propagados
- [ ] **Distributed tracing** configurado
- [ ] **Log levels** apropriados

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **📈 Métricas:**
- [ ] **Métricas customizadas** implementadas e coletadas
- [ ] **Tags apropriadas** para filtros e agregações
- [ ] **Percentis** configurados para timers
- [ ] **Gauges** refletindo estado atual
- [ ] **Métricas de infraestrutura** funcionando

#### **🏥 Health Checks:**
- [ ] **Health indicators** implementados
- [ ] **Verificações** de componentes críticos
- [ ] **Estatísticas** incluídas
- [ ] **Endpoints** expostos e acessíveis
- [ ] **Status** refletindo realidade do sistema

#### **📊 Visualização:**
- [ ] **Dashboards** criados e funcionais
- [ ] **Queries** otimizadas
- [ ] **Panels** informativos
- [ ] **Links** entre dashboards
- [ ] **Documentação** disponível

#### **🚨 Alertas:**
- [ ] **Regras de alerta** configuradas
- [ ] **Thresholds** apropriados
- [ ] **Notificações** funcionando
- [ ] **Runbooks** documentados
- [ ] **Testes** de alertas realizados

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Muitas Métricas:**
```java
// ❌ EVITAR: Métrica para cada operação específica
meterRegistry.counter("usuario.joao.login"); // Tag seria melhor
meterRegistry.counter("usuario.maria.login");

// ✅ PREFERIR: Métrica com tags
meterRegistry.counter("usuario.login", "usuario", usuarioId);
```

#### **🚫 Health Checks Caros:**
```java
// ❌ EVITAR: Health check que faz queries pesadas
public Health health() {
    List<Aggregate> all = repository.findAll(); // Caro!
    return Health.up().build();
}

// ✅ PREFERIR: Health check leve
public Health health() {
    boolean healthy = repository.isHealthy(); // Apenas verifica conexão
    return healthy ? Health.up().build() : Health.down().build();
}
```

#### **🚫 Alertas Muito Sensíveis:**
```yaml
# ❌ EVITAR: Alerta que dispara constantemente
- alert: AnyError
  expr: errors_total > 0
  for: 10s  # Muito curto

# ✅ PREFERIR: Alerta com threshold apropriado
- alert: HighErrorRate
  expr: rate(errors_total[5m]) > 0.05
  for: 3m  # Tempo para confirmar o problema
```

### **✅ Boas Práticas:**

#### **🎯 Métricas:**
- **Sempre** usar tags em vez de criar múltiplas métricas
- **Sempre** configurar percentis para timers
- **Sempre** usar unidades base consistentes
- **Sempre** incluir descrições nas métricas

#### **🏥 Health Checks:**
- **Sempre** fazer verificações leves e rápidas
- **Sempre** incluir detalhes úteis
- **Sempre** cachear resultados quando apropriado
- **Sempre** retornar status correto (UP/DOWN)

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 10 - Documentação & Deploy](./10-documentacao-deploy.md)**
2. Documentar API e arquitetura
3. Criar runbooks operacionais
4. Configurar pipeline de deploy

### **📋 Preparação para Próxima Etapa:**
- [ ] **Documentação técnica** iniciada
- [ ] **Swagger/OpenAPI** revisado
- [ ] **Deploy patterns** estudados
- [ ] **Testes de monitoramento** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Monitoramento](../11-monitoramento-README.md)**: Guia completo de monitoramento
- **Micrometer**: Documentação de métricas
- **Prometheus**: Query language e best practices
- **Grafana**: Dashboard design

### **🛠️ Ferramentas:**
- **Prometheus**: Coleta de métricas
- **Grafana**: Visualização e dashboards
- **AlertManager**: Gerenciamento de alertas
- **Jaeger/Zipkin**: Distributed tracing
- **ELK Stack**: Logging centralizado

### **🎨 Templates:**
- **Dashboard Template**: Template padrão de dashboard
- **Alert Rules Template**: Template de regras de alerta
- **Runbook Template**: Template de runbook operacional

---

**📋 Checklist Total:** 50+ itens de validação  
**⏱️ Tempo Médio:** 2-3 horas  
**🎯 Resultado:** Observabilidade completa implementada  
**✅ Próxima Etapa:** Documentação e Deploy