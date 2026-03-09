# ⚙️ CONFIGURAÇÕES E DATASOURCES - PARTE 4
## Health Checks e Monitoramento

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar health checks customizados
- Configurar métricas e monitoramento
- Configurar Actuator endpoints
- Implementar alertas e observabilidade

---

## 🏥 **HEALTH CHECKS CUSTOMIZADOS**

### **📊 HibridaHealthIndicator.java**

```java
@Component
@Slf4j
public class HibridaHealthIndicator implements HealthIndicator {
    
    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    private final EventStore eventStore;
    private final ProjectionTrackerRepository projectionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EventBus eventBus;
    
    public HibridaHealthIndicator(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            EventStore eventStore,
            ProjectionTrackerRepository projectionRepository,
            RedisTemplate<String, Object> redisTemplate,
            EventBus eventBus) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        this.eventStore = eventStore;
        this.projectionRepository = projectionRepository;
        this.redisTemplate = redisTemplate;
        this.eventBus = eventBus;
    }
    
    @Override
    public Health health() {
        Map<String, Object> details = new HashMap<>();
        boolean isHealthy = true;
        
        try {
            // === VERIFICAR COMPONENTES CRÍTICOS ===
            Health writeDbHealth = checkWriteDataSource();
            details.put("writeDatabase", writeDbHealth.getDetails());
            isHealthy &= writeDbHealth.getStatus() == Status.UP;
            
            Health readDbHealth = checkReadDataSource();
            details.put("readDatabase", readDbHealth.getDetails());
            isHealthy &= readDbHealth.getStatus() == Status.UP;
            
            Health eventStoreHealth = checkEventStore();
            details.put("eventStore", eventStoreHealth.getDetails());
            isHealthy &= eventStoreHealth.getStatus() == Status.UP;
            
            Health projectionsHealth = checkProjections();
            details.put("projections", projectionsHealth.getDetails());
            isHealthy &= projectionsHealth.getStatus() == Status.UP;
            
            Health cacheHealth = checkCache();
            details.put("cache", cacheHealth.getDetails());
            // Cache não é crítico - não afeta saúde geral
            
            Health eventBusHealth = checkEventBus();
            details.put("eventBus", eventBusHealth.getDetails());
            isHealthy &= eventBusHealth.getStatus() == Status.UP;
            
            // === INFORMAÇÕES GERAIS ===
            details.put("timestamp", Instant.now());
            details.put("uptime", getUptime());
            details.put("version", getClass().getPackage().getImplementationVersion());
            details.put("environment", System.getProperty("spring.profiles.active", "unknown"));
            
            return isHealthy ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            log.error("Erro ao verificar saúde da aplicação", e);
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("timestamp", Instant.now())
                .build();
        }
    }
    
    private Health checkWriteDataSource() {
        try (Connection connection = writeDataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            
            Map<String, Object> details = new HashMap<>();
            details.put("database", connection.getMetaData().getDatabaseProductName());
            details.put("version", connection.getMetaData().getDatabaseProductVersion());
            details.put("url", maskUrl(connection.getMetaData().getURL()));
            details.put("readOnly", connection.isReadOnly());
            details.put("autoCommit", connection.getAutoCommit());
            details.put("validationQuery", "SELECT 1");
            details.put("responseTime", measureResponseTime(() -> {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                    return stmt.executeQuery().next();
                }
            }));
            
            return isValid ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("type", "write-database")
                .build();
        }
    }
    
    private Health checkReadDataSource() {
        try (Connection connection = readDataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            
            Map<String, Object> details = new HashMap<>();
            details.put("database", connection.getMetaData().getDatabaseProductName());
            details.put("version", connection.getMetaData().getDatabaseProductVersion());
            details.put("url", maskUrl(connection.getMetaData().getURL()));
            details.put("readOnly", connection.isReadOnly());
            details.put("validationQuery", "SELECT 1");
            details.put("responseTime", measureResponseTime(() -> {
                try (PreparedStatement stmt = connection.prepareStatement("SELECT COUNT(*) FROM projections.sinistro_view LIMIT 1")) {
                    return stmt.executeQuery().next();
                }
            }));
            
            return isValid ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("type", "read-database")
                .build();
        }
    }
    
    private Health checkEventStore() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Verificar se consegue acessar o Event Store
            boolean exists = eventStore.aggregateExists("health-check");
            long responseTime = System.currentTimeMillis() - startTime;
            
            Map<String, Object> details = new HashMap<>();
            details.put("accessible", true);
            details.put("responseTime", responseTime + "ms");
            details.put("testQuery", "aggregateExists");
            
            return Health.up().withDetails(details).build();
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("accessible", false)
                .withDetail("type", "event-store")
                .build();
        }
    }
    
    private Health checkProjections() {
        try {
            long totalProjections = projectionRepository.count();
            long activeProjections = projectionRepository.countByStatus(ProjectionStatus.ACTIVE);
            long errorProjections = projectionRepository.countByStatus(ProjectionStatus.ERROR);
            long pausedProjections = projectionRepository.countByStatus(ProjectionStatus.PAUSED);
            
            double healthScore = totalProjections > 0 ? 
                (double) activeProjections / totalProjections : 1.0;
            
            Map<String, Object> details = new HashMap<>();
            details.put("total", totalProjections);
            details.put("active", activeProjections);
            details.put("errors", errorProjections);
            details.put("paused", pausedProjections);
            details.put("healthScore", Math.round(healthScore * 100) / 100.0);
            details.put("status", healthScore >= 0.9 ? "HEALTHY" : 
                                 healthScore >= 0.7 ? "DEGRADED" : "UNHEALTHY");
            
            boolean isHealthy = errorProjections == 0 || healthScore >= 0.9;
            
            return isHealthy ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("type", "projections")
                .build();
        }
    }
    
    private Health checkCache() {
        try {
            long startTime = System.currentTimeMillis();
            
            // Teste de escrita e leitura no cache
            String testKey = "health-check:" + System.currentTimeMillis();
            String testValue = "test-value";
            
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            redisTemplate.delete(testKey);
            
            long responseTime = System.currentTimeMillis() - startTime;
            boolean isWorking = testValue.equals(retrievedValue);
            
            Map<String, Object> details = new HashMap<>();
            details.put("accessible", true);
            details.put("working", isWorking);
            details.put("responseTime", responseTime + "ms");
            details.put("testOperation", "set/get/delete");
            
            return isWorking ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("accessible", false)
                .withDetail("type", "cache")
                .build();
        }
    }
    
    private Health checkEventBus() {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            boolean isHealthy = eventBus.isHealthy();
            
            Map<String, Object> details = new HashMap<>();
            details.put("healthy", isHealthy);
            details.put("totalEvents", stats.getTotalEvents());
            details.put("successRate", Math.round(stats.getSuccessRate() * 100) / 100.0);
            details.put("errorRate", Math.round(stats.getErrorRate() * 100) / 100.0);
            details.put("throughput", Math.round(stats.getThroughputPerSecond() * 100) / 100.0);
            
            return isHealthy ? 
                Health.up().withDetails(details).build() :
                Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .withDetail("type", "event-bus")
                .build();
        }
    }
    
    private String getUptime() {
        long uptimeMs = ManagementFactory.getRuntimeMXBean().getUptime();
        Duration uptime = Duration.ofMillis(uptimeMs);
        
        return String.format("%dd %dh %dm %ds",
            uptime.toDays(),
            uptime.toHours() % 24,
            uptime.toMinutes() % 60,
            uptime.getSeconds() % 60);
    }
    
    private String maskUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
    }
    
    private long measureResponseTime(Supplier<Boolean> operation) {
        long startTime = System.currentTimeMillis();
        try {
            operation.get();
            return System.currentTimeMillis() - startTime;
        } catch (Exception e) {
            return -1;
        }
    }
}
```

---

## 📊 **CONFIGURAÇÃO DE MÉTRICAS**

### **⚡ ApplicationMetrics.java**

```java
@Component
@Slf4j
public class ApplicationMetrics implements MeterBinder {
    
    private final EventStore eventStore;
    private final ProjectionTrackerRepository projectionRepository;
    private final EventBus eventBus;
    private final CommandBus commandBus;
    
    // Métricas customizadas
    private Gauge totalEventsGauge;
    private Gauge activeProjectionsGauge;
    private Gauge projectionLagGauge;
    private Counter businessOperationsCounter;
    private Timer businessOperationTimer;
    
    public ApplicationMetrics(
            EventStore eventStore,
            ProjectionTrackerRepository projectionRepository,
            EventBus eventBus,
            CommandBus commandBus) {
        this.eventStore = eventStore;
        this.projectionRepository = projectionRepository;
        this.eventBus = eventBus;
        this.commandBus = commandBus;
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        log.info("Registrando métricas customizadas da aplicação");
        
        // === MÉTRICAS DE EVENT STORE ===
        totalEventsGauge = Gauge.builder("eventstore.events.total")
            .description("Total de eventos no Event Store")
            .tag("component", "eventstore")
            .register(registry, this, ApplicationMetrics::getTotalEvents);
        
        Gauge.builder("eventstore.aggregates.total")
            .description("Total de agregados no Event Store")
            .tag("component", "eventstore")
            .register(registry, this, ApplicationMetrics::getTotalAggregates);
        
        // === MÉTRICAS DE PROJEÇÕES ===
        activeProjectionsGauge = Gauge.builder("projections.active.count")
            .description("Número de projeções ativas")
            .tag("component", "projections")
            .register(registry, this, ApplicationMetrics::getActiveProjections);
        
        Gauge.builder("projections.error.count")
            .description("Número de projeções com erro")
            .tag("component", "projections")
            .register(registry, this, ApplicationMetrics::getErrorProjections);
        
        projectionLagGauge = Gauge.builder("projections.lag.max")
            .description("Maior lag entre as projeções (em eventos)")
            .tag("component", "projections")
            .register(registry, this, ApplicationMetrics::getMaxProjectionLag);
        
        // === MÉTRICAS DE NEGÓCIO ===
        businessOperationsCounter = Counter.builder("business.operations.total")
            .description("Total de operações de negócio executadas")
            .tag("component", "business")
            .register(registry);
        
        businessOperationTimer = Timer.builder("business.operations.duration")
            .description("Tempo de execução de operações de negócio")
            .tag("component", "business")
            .register(registry);
        
        // === MÉTRICAS DE SISTEMA ===
        Gauge.builder("system.memory.used")
            .description("Memória utilizada pela JVM")
            .tag("component", "system")
            .register(registry, this, ApplicationMetrics::getUsedMemory);
        
        Gauge.builder("system.memory.max")
            .description("Memória máxima da JVM")
            .tag("component", "system")
            .register(registry, this, ApplicationMetrics::getMaxMemory);
        
        // === MÉTRICAS DE PERFORMANCE ===
        Gauge.builder("performance.throughput.events")
            .description("Throughput de eventos por segundo")
            .tag("component", "performance")
            .register(registry, this, ApplicationMetrics::getEventThroughput);
        
        Gauge.builder("performance.throughput.commands")
            .description("Throughput de comandos por segundo")
            .tag("component", "performance")
            .register(registry, this, ApplicationMetrics::getCommandThroughput);
    }
    
    // === MÉTODOS DE COLETA DE MÉTRICAS ===
    
    private double getTotalEvents(ApplicationMetrics metrics) {
        try {
            // Implementar contagem total de eventos
            return 0; // Placeholder - implementar conforme EventStore
        } catch (Exception e) {
            log.warn("Erro ao obter total de eventos", e);
            return -1;
        }
    }
    
    private double getTotalAggregates(ApplicationMetrics metrics) {
        try {
            // Implementar contagem total de agregados
            return 0; // Placeholder - implementar conforme EventStore
        } catch (Exception e) {
            log.warn("Erro ao obter total de agregados", e);
            return -1;
        }
    }
    
    private double getActiveProjections(ApplicationMetrics metrics) {
        try {
            return projectionRepository.countByStatus(ProjectionStatus.ACTIVE);
        } catch (Exception e) {
            log.warn("Erro ao obter projeções ativas", e);
            return -1;
        }
    }
    
    private double getErrorProjections(ApplicationMetrics metrics) {
        try {
            return projectionRepository.countByStatus(ProjectionStatus.ERROR);
        } catch (Exception e) {
            log.warn("Erro ao obter projeções com erro", e);
            return -1;
        }
    }
    
    private double getMaxProjectionLag(ApplicationMetrics metrics) {
        try {
            // Implementar cálculo de lag máximo
            return 0; // Placeholder - implementar conforme necessário
        } catch (Exception e) {
            log.warn("Erro ao calcular lag das projeções", e);
            return -1;
        }
    }
    
    private double getUsedMemory(ApplicationMetrics metrics) {
        Runtime runtime = Runtime.getRuntime();
        return runtime.totalMemory() - runtime.freeMemory();
    }
    
    private double getMaxMemory(ApplicationMetrics metrics) {
        return Runtime.getRuntime().maxMemory();
    }
    
    private double getEventThroughput(ApplicationMetrics metrics) {
        try {
            EventBusStatistics stats = eventBus.getStatistics();
            return stats.getThroughputPerSecond();
        } catch (Exception e) {
            log.warn("Erro ao obter throughput de eventos", e);
            return -1;
        }
    }
    
    private double getCommandThroughput(ApplicationMetrics metrics) {
        try {
            CommandBusStatistics stats = commandBus.getStatistics();
            return stats.getThroughputPerSecond();
        } catch (Exception e) {
            log.warn("Erro ao obter throughput de comandos", e);
            return -1;
        }
    }
    
    // === MÉTODOS PÚBLICOS PARA INSTRUMENTAÇÃO ===
    
    public void incrementBusinessOperation(String operationType) {
        businessOperationsCounter.increment(Tags.of("type", operationType));
    }
    
    public Timer.Sample startBusinessOperation() {
        return Timer.start();
    }
    
    public void stopBusinessOperation(Timer.Sample sample, String operationType) {
        sample.stop(businessOperationTimer.tag("type", operationType));
    }
}
```

---

## 🔧 **CONFIGURAÇÃO DO ACTUATOR**

### **📋 application.yml - Actuator Configuration**

```yaml
# === CONFIGURAÇÕES DE MONITORAMENTO ===
management:
  endpoints:
    web:
      exposure:
        include: ${ACTUATOR_ENDPOINTS:health,info,metrics,prometheus,cqrs,projections,eventbus,commandbus}
      base-path: ${ACTUATOR_BASE_PATH:/actuator}
      path-mapping:
        prometheus: metrics
        health: health
        info: info
      cors:
        allowed-origins: ${ACTUATOR_CORS_ORIGINS:*}
        allowed-methods: GET,POST
        allowed-headers: "*"
  
  endpoint:
    # === HEALTH ENDPOINT ===
    health:
      enabled: true
      show-details: ${HEALTH_SHOW_DETAILS:when-authorized}
      show-components: always
      probes:
        enabled: ${HEALTH_PROBES_ENABLED:true}
      group:
        readiness:
          include: readinessState,db,redis,eventbus
          show-details: always
        liveness:
          include: livenessState,diskSpace
          show-details: always
      
      # Configurações de timeout
      cache:
        time-to-live: ${HEALTH_CACHE_TTL:10s}
    
    # === INFO ENDPOINT ===
    info:
      enabled: true
      cache:
        time-to-live: ${INFO_CACHE_TTL:10s}
    
    # === METRICS ENDPOINT ===
    metrics:
      enabled: true
      cache:
        time-to-live: ${METRICS_CACHE_TTL:5s}
    
    # === PROMETHEUS ENDPOINT ===
    prometheus:
      enabled: ${PROMETHEUS_ENABLED:true}
      cache:
        time-to-live: ${PROMETHEUS_CACHE_TTL:5s}
    
    # === ENDPOINTS CUSTOMIZADOS ===
    cqrs:
      enabled: true
      cache:
        time-to-live: 30s
    
    projections:
      enabled: true
      cache:
        time-to-live: 30s
    
    eventbus:
      enabled: true
      cache:
        time-to-live: 10s
    
    commandbus:
      enabled: true
      cache:
        time-to-live: 10s
  
  # === CONFIGURAÇÕES DE MÉTRICAS ===
  metrics:
    tags:
      application: ${spring.application.name}
      environment: ${ENVIRONMENT:unknown}
      version: ${spring.application.version:unknown}
      instance: ${HOSTNAME:${random.uuid}}
    
    export:
      prometheus:
        enabled: ${PROMETHEUS_ENABLED:true}
        step: ${PROMETHEUS_STEP:30s}
        descriptions: true
        histogram-flavor: prometheus
    
    distribution:
      percentiles-histogram:
        "[http.server.requests]": true
        "[spring.data.repository.invocations]": true
        "[business.operations.duration]": true
      
      percentiles:
        "[http.server.requests]": 0.5, 0.95, 0.99
        "[spring.data.repository.invocations]": 0.5, 0.95, 0.99
        "[business.operations.duration]": 0.5, 0.95, 0.99
      
      slo:
        "[http.server.requests]": 10ms, 50ms, 100ms, 200ms, 500ms, 1s, 2s, 5s
        "[business.operations.duration]": 100ms, 500ms, 1s, 2s, 5s, 10s
      
      minimum-expected-value:
        "[http.server.requests]": 1ms
        "[business.operations.duration]": 1ms
      
      maximum-expected-value:
        "[http.server.requests]": 30s
        "[business.operations.duration]": 60s
  
  # === INFORMAÇÕES DA APLICAÇÃO ===
  info:
    app:
      name: ${spring.application.name}
      version: ${spring.application.version:unknown}
      description: "Sistema de Sinistros com Arquitetura Híbrida"
      contact:
        team: "Arquitetura"
        email: "arquitetura@seguradora.com"
    
    build:
      artifact: "@project.artifactId@"
      name: "@project.name@"
      time: "@maven.build.timestamp@"
      version: "@project.version@"
    
    git:
      mode: full
    
    java:
      source: "@java.version@"
      target: "@java.version@"
      vendor: "${java.vendor}"
      runtime: "${java.runtime.name} ${java.runtime.version}"
    
    system:
      timezone: "${user.timezone}"
      encoding: "${file.encoding}"
    
    # Informações customizadas
    architecture:
      patterns:
        - "Event Sourcing"
        - "CQRS"
        - "Domain Driven Design"
        - "Command Bus"
        - "Event Bus"
      
      components:
        eventstore: "PostgreSQL"
        projections: "PostgreSQL"
        cache: "Redis"
        messaging: "${app.event-bus.type}"
      
      features:
        snapshots: "${event-sourcing.snapshot.enabled}"
        archive: "${event-sourcing.archive.enabled}"
        monitoring: "${management.metrics.export.prometheus.enabled}"

# === CONFIGURAÇÕES DE SEGURANÇA PARA ACTUATOR ===
spring:
  security:
    user:
      name: ${ACTUATOR_USER:admin}
      password: ${ACTUATOR_PASSWORD:admin123}
      roles: ${ACTUATOR_ROLES:ADMIN,ACTUATOR}

# === CONFIGURAÇÕES DE ALERTAS ===
app:
  monitoring:
    alerts:
      enabled: ${ALERTS_ENABLED:true}
      
      # Thresholds para alertas
      thresholds:
        memory-usage: ${ALERT_MEMORY_THRESHOLD:0.85}
        error-rate: ${ALERT_ERROR_RATE_THRESHOLD:0.05}
        response-time: ${ALERT_RESPONSE_TIME_THRESHOLD:2000}
        projection-lag: ${ALERT_PROJECTION_LAG_THRESHOLD:1000}
        disk-usage: ${ALERT_DISK_USAGE_THRESHOLD:0.80}
      
      # Configurações de notificação
      notifications:
        email:
          enabled: ${ALERT_EMAIL_ENABLED:false}
          recipients: ${ALERT_EMAIL_RECIPIENTS:}
        
        slack:
          enabled: ${ALERT_SLACK_ENABLED:false}
          webhook-url: ${ALERT_SLACK_WEBHOOK:}
        
        webhook:
          enabled: ${ALERT_WEBHOOK_ENABLED:false}
          url: ${ALERT_WEBHOOK_URL:}
```

---

## 🚨 **CONFIGURAÇÃO DE ALERTAS**

### **📧 AlertingConfiguration.java**

```java
@Configuration
@ConditionalOnProperty(name = "app.monitoring.alerts.enabled", havingValue = "true")
@EnableConfigurationProperties(AlertingProperties.class)
@Slf4j
public class AlertingConfiguration {
    
    private final AlertingProperties properties;
    
    public AlertingConfiguration(AlertingProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    public AlertManager alertManager() {
        return new AlertManager(properties);
    }
    
    @Bean
    public HealthStatusChangeListener healthStatusChangeListener(AlertManager alertManager) {
        return new HealthStatusChangeListener(alertManager);
    }
    
    @Bean
    public MetricThresholdMonitor metricThresholdMonitor(
            MeterRegistry meterRegistry, 
            AlertManager alertManager) {
        return new MetricThresholdMonitor(meterRegistry, alertManager, properties);
    }
    
    @EventListener
    public void handleHealthChange(HealthChangedEvent event) {
        if (event.getStatus() == Status.DOWN) {
            log.warn("Sistema com problemas de saúde: {}", event.getDetails());
            // Implementar lógica de alerta
        }
    }
    
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void checkSystemHealth() {
        // Implementar verificações periódicas
        log.debug("Verificando saúde do sistema...");
    }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Micrometer Metrics](https://micrometer.io/docs)
- [Prometheus Configuration](https://prometheus.io/docs/prometheus/latest/configuration/configuration/)
- [Health Indicators](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.endpoints.health)

### **📖 Próxima Parte:**
- **Parte 5**: Configurações Avançadas e Troubleshooting

---

**📝 Parte 4 de 5 - Health Checks e Monitoramento**  
**⏱️ Tempo estimado**: 50 minutos  
**🎯 Próximo**: [Parte 5 - Configurações Avançadas e Troubleshooting](./09-configuracoes-parte-5.md)