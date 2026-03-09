# 📊 MONITORAMENTO E OBSERVABILIDADE - PARTE 1
## Fundamentos de Observabilidade na Arquitetura Híbrida

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os pilares da observabilidade
- Entender as necessidades específicas da arquitetura híbrida
- Conhecer as ferramentas implementadas no projeto
- Configurar monitoramento básico

---

## 🔍 **CONCEITOS FUNDAMENTAIS**

### **📋 Os Três Pilares da Observabilidade**

A observabilidade em sistemas distribuídos se baseia em três pilares fundamentais:

#### **1. 📈 Métricas (Metrics)**
- **Definição**: Dados numéricos agregados ao longo do tempo
- **Características**: Eficientes para armazenar e consultar
- **Exemplos**: CPU, memória, throughput, latência, taxa de erro

#### **2. 📝 Logs**
- **Definição**: Registros de eventos discretos no sistema
- **Características**: Contexto detalhado de eventos específicos
- **Exemplos**: Logs de aplicação, logs de erro, logs de auditoria

#### **3. 🔗 Traces**
- **Definição**: Representação de uma requisição através de múltiplos serviços
- **Características**: Visibilidade de fluxos end-to-end
- **Exemplos**: Rastreamento de comandos, eventos, consultas

### **🏗️ Desafios na Arquitetura Híbrida**

A arquitetura híbrida com Event Sourcing e CQRS apresenta desafios únicos:

#### **Complexidades Específicas:**
- ✅ **Separação Command/Query**: Monitorar ambos os lados
- ✅ **Processamento Assíncrono**: Rastrear eventos e projeções
- ✅ **Consistência Eventual**: Monitorar lag entre write/read
- ✅ **Múltiplos DataSources**: Observar diferentes bancos
- ✅ **Event Bus**: Monitorar publicação e consumo de eventos

---

## 🛠️ **STACK DE OBSERVABILIDADE DO PROJETO**

### **📦 Ferramentas Implementadas**

| **Categoria** | **Ferramenta** | **Propósito** | **Configuração** |
|---------------|----------------|---------------|------------------|
| **Métricas** | Micrometer + Prometheus | Coleta e armazenamento | Spring Boot Actuator |
| **Logs** | Logback + ELK Stack | Agregação e análise | Structured Logging |
| **Traces** | Spring Cloud Sleuth | Rastreamento distribuído | Auto-instrumentação |
| **Dashboards** | Grafana | Visualização | Dashboards pré-configurados |
| **Alertas** | Prometheus AlertManager | Notificações | Regras customizadas |

### **🔧 Configuração Base do Projeto**

#### **application.yml - Configurações de Observabilidade:**

```yaml
# Configurações de Observabilidade
management:
  endpoints:
    web:
      exposure:
        include: "*"
  endpoint:
    health:
      show-details: always
      show-components: always
    metrics:
      enabled: true
    prometheus:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
    distribution:
      percentiles-histogram:
        http.server.requests: true
        spring.data.repository.invocations: true
      percentiles:
        http.server.requests: 0.5, 0.95, 0.99
        spring.data.repository.invocations: 0.5, 0.95, 0.99

# Configurações de Logging
logging:
  level:
    com.seguradora.hibrida: INFO
    org.springframework.data: DEBUG
    org.hibernate.SQL: DEBUG
  pattern:
    console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
    file: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
  file:
    name: logs/app-arquitetura-hibrida.log

# Configurações de Tracing
spring:
  sleuth:
    sampler:
      probability: 1.0 # 100% para desenvolvimento, reduzir em produção
    zipkin:
      base-url: http://localhost:9411
```

---

## 📊 **MÉTRICAS IMPLEMENTADAS**

### **🎯 Métricas de Negócio**

O projeto implementa métricas específicas para monitorar aspectos de negócio:

#### **Métricas de Command Side:**
```java
// Exemplo de métricas no CommandBus
@Component
public class CommandBusMetrics implements MeterBinder {
    
    private final Counter commandsProcessed;
    private final Counter commandsFailed;
    private final Timer commandExecutionTime;
    private final Gauge activeCommands;
    
    public CommandBusMetrics(MeterRegistry meterRegistry) {
        this.commandsProcessed = Counter.builder("commands.processed")
            .description("Total number of commands processed")
            .register(meterRegistry);
            
        this.commandsFailed = Counter.builder("commands.failed")
            .description("Total number of failed commands")
            .register(meterRegistry);
            
        this.commandExecutionTime = Timer.builder("commands.execution.time")
            .description("Command execution time")
            .register(meterRegistry);
            
        this.activeCommands = Gauge.builder("commands.active")
            .description("Number of currently active commands")
            .register(meterRegistry, this, CommandBusMetrics::getActiveCommandsCount);
    }
    
    public void incrementCommandsProcessed(String commandType) {
        commandsProcessed.increment(Tags.of("type", commandType));
    }
    
    public void incrementCommandsFailed(String commandType, String errorType) {
        commandsFailed.increment(Tags.of("type", commandType, "error", errorType));
    }
    
    public Timer.Sample startExecutionTimer() {
        return Timer.start(commandExecutionTime);
    }
    
    private double getActiveCommandsCount() {
        // Implementação para contar comandos ativos
        return 0.0; // Placeholder
    }
}
```

#### **Métricas de Query Side:**
```java
// Exemplo de métricas no Query Service
@Component
public class QueryMetrics {
    
    private final Counter queriesExecuted;
    private final Timer queryExecutionTime;
    private final Counter cacheHits;
    private final Counter cacheMisses;
    
    public QueryMetrics(MeterRegistry meterRegistry) {
        this.queriesExecuted = Counter.builder("queries.executed")
            .description("Total number of queries executed")
            .register(meterRegistry);
            
        this.queryExecutionTime = Timer.builder("queries.execution.time")
            .description("Query execution time")
            .register(meterRegistry);
            
        this.cacheHits = Counter.builder("cache.hits")
            .description("Number of cache hits")
            .register(meterRegistry);
            
        this.cacheMisses = Counter.builder("cache.misses")
            .description("Number of cache misses")
            .register(meterRegistry);
    }
    
    public void recordQueryExecution(String queryType, Duration duration) {
        queriesExecuted.increment(Tags.of("type", queryType));
        queryExecutionTime.record(duration, Tags.of("type", queryType));
    }
    
    public void recordCacheHit(String cacheType) {
        cacheHits.increment(Tags.of("cache", cacheType));
    }
    
    public void recordCacheMiss(String cacheType) {
        cacheMisses.increment(Tags.of("cache", cacheType));
    }
}
```

### **📈 Métricas de CQRS**

Métricas específicas para monitorar a separação Command/Query:

```java
@Component
public class CQRSMetrics implements MeterBinder {
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    public CQRSMetrics(EventStoreRepository eventStoreRepository,
                       ProjectionTrackerRepository projectionTrackerRepository) {
        this.eventStoreRepository = eventStoreRepository;
        this.projectionTrackerRepository = projectionTrackerRepository;
    }
    
    @Override
    public void bindTo(MeterRegistry registry) {
        // Métrica de lag entre command e query side
        Gauge.builder("cqrs.lag.seconds")
            .description("Lag between command and query side in seconds")
            .register(registry, this, CQRSMetrics::calculateLag);
            
        // Número total de eventos no event store
        Gauge.builder("eventstore.events.total")
            .description("Total number of events in event store")
            .register(registry, this, CQRSMetrics::getTotalEvents);
            
        // Número de projeções ativas
        Gauge.builder("projections.active.count")
            .description("Number of active projections")
            .register(registry, this, CQRSMetrics::getActiveProjections);
            
        // Taxa de erro das projeções
        Gauge.builder("projections.error.rate")
            .description("Error rate of projections")
            .register(registry, this, CQRSMetrics::getProjectionErrorRate);
    }
    
    private double calculateLag() {
        // Implementação para calcular lag entre command e query side
        Long maxEventId = eventStoreRepository.findMaxEventId();
        Long minProcessedEventId = projectionTrackerRepository.findMinProcessedEventId()
            .orElse(0L);
        
        return maxEventId != null ? maxEventId - minProcessedEventId : 0.0;
    }
    
    private double getTotalEvents() {
        return eventStoreRepository.count();
    }
    
    private double getActiveProjections() {
        return projectionTrackerRepository.countByStatus(ProjectionStatus.ACTIVE);
    }
    
    private double getProjectionErrorRate() {
        long totalProjections = projectionTrackerRepository.count();
        long errorProjections = projectionTrackerRepository.countByStatus(ProjectionStatus.ERROR);
        
        return totalProjections > 0 ? (double) errorProjections / totalProjections : 0.0;
    }
}
```

---

## 📝 **LOGGING ESTRUTURADO**

### **🔧 Configuração de Logs Estruturados**

#### **logback-spring.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<configuration>
    <include resource="org/springframework/boot/logging/logback/defaults.xml"/>
    
    <!-- Console Appender -->
    <appender name="CONSOLE" class="ch.qos.logback.core.ConsoleAppender">
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <arguments/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <!-- File Appender -->
    <appender name="FILE" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <file>logs/app-arquitetura-hibrida.log</file>
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <fileNamePattern>logs/app-arquitetura-hibrida.%d{yyyy-MM-dd}.%i.gz</fileNamePattern>
            <maxFileSize>100MB</maxFileSize>
            <maxHistory>30</maxHistory>
            <totalSizeCap>3GB</totalSizeCap>
        </rollingPolicy>
        <encoder class="net.logstash.logback.encoder.LoggingEventCompositeJsonEncoder">
            <providers>
                <timestamp/>
                <logLevel/>
                <loggerName/>
                <mdc/>
                <arguments/>
                <message/>
                <stackTrace/>
            </providers>
        </encoder>
    </appender>
    
    <!-- Async Appender for better performance -->
    <appender name="ASYNC_FILE" class="ch.qos.logback.classic.AsyncAppender">
        <appender-ref ref="FILE"/>
        <queueSize>1024</queueSize>
        <discardingThreshold>0</discardingThreshold>
    </appender>
    
    <!-- Logger configurations -->
    <logger name="com.seguradora.hibrida" level="INFO"/>
    <logger name="com.seguradora.hibrida.command" level="DEBUG"/>
    <logger name="com.seguradora.hibrida.eventbus" level="DEBUG"/>
    <logger name="com.seguradora.hibrida.projection" level="DEBUG"/>
    
    <root level="INFO">
        <appender-ref ref="CONSOLE"/>
        <appender-ref ref="ASYNC_FILE"/>
    </root>
</configuration>
```

### **📋 Padrões de Logging no Projeto**

#### **Logging em Command Handlers:**
```java
@Component
@Slf4j
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {
    
    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        MDC.put("commandType", command.getCommandType());
        MDC.put("commandId", command.getCommandId().toString());
        MDC.put("aggregateId", command.getAggregateId());
        
        try {
            log.info("Iniciando processamento do comando: {}", command.getCommandType());
            
            // Lógica do comando...
            
            log.info("Comando processado com sucesso");
            return CommandResult.success();
            
        } catch (Exception e) {
            log.error("Erro ao processar comando: {}", e.getMessage(), e);
            return CommandResult.failure(e);
        } finally {
            MDC.clear();
        }
    }
}
```

#### **Logging em Event Handlers:**
```java
@Component
@Slf4j
public class SinistroEventHandler implements EventHandler<SinistroEvent> {
    
    @Override
    public void handle(SinistroEvent event) {
        MDC.put("eventType", event.getEventType());
        MDC.put("eventId", event.getEventId().toString());
        MDC.put("aggregateId", event.getAggregateId());
        MDC.put("correlationId", event.getCorrelationId().toString());
        
        try {
            log.info("Processando evento: {}", event.getEventType());
            
            // Lógica do evento...
            
            log.info("Evento processado com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao processar evento: {}", e.getMessage(), e);
            throw new EventHandlingException("Falha no processamento do evento", e);
        } finally {
            MDC.clear();
        }
    }
}
```

---

## 🔗 **DISTRIBUTED TRACING**

### **🔧 Configuração do Sleuth**

O projeto usa Spring Cloud Sleuth para rastreamento distribuído:

#### **Configuração Automática:**
```java
@Configuration
@EnableZipkinServer // Para desenvolvimento local
public class TracingConfiguration {
    
    @Bean
    public Sampler alwaysSampler() {
        return Sampler.create(1.0f); // 100% para desenvolvimento
    }
    
    @Bean
    public SpanCustomizer spanCustomizer() {
        return span -> {
            span.tag("service.name", "app-arquitetura-hibrida");
            span.tag("service.version", getClass().getPackage().getImplementationVersion());
        };
    }
}
```

#### **Instrumentação Manual:**
```java
@Component
@Slf4j
public class SinistroService {
    
    private final Tracer tracer;
    
    public SinistroService(Tracer tracer) {
        this.tracer = tracer;
    }
    
    public void processarSinistro(String sinistroId) {
        Span span = tracer.nextSpan()
            .name("processar-sinistro")
            .tag("sinistro.id", sinistroId)
            .start();
        
        try (Tracer.SpanInScope ws = tracer.withSpanInScope(span)) {
            log.info("Processando sinistro: {}", sinistroId);
            
            // Lógica de processamento...
            
            span.tag("sinistro.status", "processado");
            
        } catch (Exception e) {
            span.tag("error", e.getMessage());
            throw e;
        } finally {
            span.end();
        }
    }
}
```

---

## 🏥 **HEALTH CHECKS**

### **📋 Health Indicators Customizados**

O projeto implementa health checks específicos para cada componente:

#### **Event Store Health Check:**
```java
@Component
public class EventStoreHealthIndicator implements HealthIndicator {
    
    private final EventStore eventStore;
    
    public EventStoreHealthIndicator(EventStore eventStore) {
        this.eventStore = eventStore;
    }
    
    @Override
    public Health health() {
        try {
            // Testa conectividade básica
            boolean canConnect = testConnection();
            
            if (canConnect) {
                Map<String, Object> details = new HashMap<>();
                details.put("status", "UP");
                details.put("totalEvents", getTotalEvents());
                details.put("lastEventTime", getLastEventTime());
                
                return Health.up()
                    .withDetails(details)
                    .build();
            } else {
                return Health.down()
                    .withDetail("error", "Cannot connect to Event Store")
                    .build();
            }
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private boolean testConnection() {
        // Implementação do teste de conexão
        return true;
    }
    
    private long getTotalEvents() {
        // Implementação para contar eventos
        return 0L;
    }
    
    private Instant getLastEventTime() {
        // Implementação para obter último evento
        return Instant.now();
    }
}
```

#### **CQRS Health Check:**
```java
@Component
public class CQRSHealthIndicator implements HealthIndicator {
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = checkHealth();
            String status = determineOverallStatus(details);
            
            if ("UP".equals(status)) {
                return Health.up().withDetails(details).build();
            } else {
                return Health.down().withDetails(details).build();
            }
            
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        // Verifica command side
        details.put("commandSide", checkCommandSideHealth());
        
        // Verifica query side
        details.put("querySide", checkQuerySideHealth());
        
        // Verifica lag
        details.put("lag", checkCommandQueryLag());
        
        return details;
    }
    
    private Map<String, Object> checkCommandSideHealth() {
        // Implementação da verificação do command side
        return Map.of("status", "UP", "eventsCount", 1000L);
    }
    
    private Map<String, Object> checkQuerySideHealth() {
        // Implementação da verificação do query side
        return Map.of("status", "UP", "projectionsCount", 5L);
    }
    
    private Map<String, Object> checkCommandQueryLag() {
        // Implementação da verificação de lag
        return Map.of("lagSeconds", 2.5, "threshold", 10.0);
    }
    
    private String determineOverallStatus(Map<String, Object> details) {
        // Lógica para determinar status geral
        return "UP";
    }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Actuator](https://spring.io/guides/gs/actuator-service/)
- [Micrometer Documentation](https://micrometer.io/docs)
- [Spring Cloud Sleuth](https://spring.io/projects/spring-cloud-sleuth)
- [Prometheus Best Practices](https://prometheus.io/docs/practices/naming/)

### **📖 Próximas Partes:**
- **Parte 2**: Configuração de Prometheus e Grafana
- **Parte 3**: Dashboards e Visualizações
- **Parte 4**: Alertas e Notificações
- **Parte 5**: Troubleshooting e Debugging

---

**📝 Parte 1 de 5 - Fundamentos de Observabilidade**  
**⏱️ Tempo estimado**: 45 minutos  
**🎯 Próximo**: [Parte 2 - Prometheus e Grafana](./11-monitoramento-parte-2.md)