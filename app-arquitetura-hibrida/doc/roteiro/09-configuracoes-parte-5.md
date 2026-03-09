# ⚙️ CONFIGURAÇÕES E DATASOURCES - PARTE 5
## Configurações Avançadas e Troubleshooting

### 🎯 **OBJETIVOS DESTA PARTE**
- Configurações avançadas de performance
- Estratégias de troubleshooting
- Configurações de ambiente específicas
- Boas práticas e otimizações

---

## 🚀 **CONFIGURAÇÕES AVANÇADAS DE PERFORMANCE**

### **⚡ PerformanceConfiguration.java**

```java
@Configuration
@EnableConfigurationProperties(PerformanceProperties.class)
@Slf4j
public class PerformanceConfiguration {
    
    private final PerformanceProperties properties;
    
    public PerformanceConfiguration(PerformanceProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @Primary
    public TaskExecutor applicationTaskExecutor() {
        log.info("Configurando Task Executor customizado");
        
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // === CONFIGURAÇÕES BÁSICAS ===
        executor.setCorePoolSize(properties.getThreadPool().getCoreSize());
        executor.setMaxPoolSize(properties.getThreadPool().getMaxSize());
        executor.setQueueCapacity(properties.getThreadPool().getQueueCapacity());
        executor.setKeepAliveSeconds(properties.getThreadPool().getKeepAliveSeconds());
        
        // === CONFIGURAÇÕES AVANÇADAS ===
        executor.setThreadNamePrefix("hibrida-async-");
        executor.setAllowCoreThreadTimeOut(true);
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        
        // === POLÍTICA DE REJEIÇÃO ===
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // === CONFIGURAÇÕES DE MONITORAMENTO ===
        executor.setTaskDecorator(runnable -> {
            String correlationId = MDC.get("correlationId");
            return () -> {
                try {
                    if (correlationId != null) {
                        MDC.put("correlationId", correlationId);
                    }
                    runnable.run();
                } finally {
                    MDC.clear();
                }
            };
        });
        
        executor.initialize();
        
        return executor;
    }
    
    @Bean
    public TaskScheduler applicationTaskScheduler() {
        log.info("Configurando Task Scheduler customizado");
        
        ThreadPoolTaskScheduler scheduler = new ThreadPoolTaskScheduler();
        
        scheduler.setPoolSize(properties.getScheduler().getPoolSize());
        scheduler.setThreadNamePrefix("hibrida-scheduler-");
        scheduler.setWaitForTasksToCompleteOnShutdown(true);
        scheduler.setAwaitTerminationSeconds(30);
        
        scheduler.initialize();
        
        return scheduler;
    }
    
    @Bean
    public AsyncConfigurer asyncConfigurer() {
        return new AsyncConfigurer() {
            @Override
            public Executor getAsyncExecutor() {
                return applicationTaskExecutor();
            }
            
            @Override
            public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
                return (ex, method, params) -> {
                    log.error("Erro em execução assíncrona - Método: {}, Parâmetros: {}", 
                        method.getName(), Arrays.toString(params), ex);
                };
            }
        };
    }
    
    @Bean
    public ConnectionPoolMetrics connectionPoolMetrics(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            MeterRegistry meterRegistry) {
        
        return new ConnectionPoolMetrics(writeDataSource, readDataSource, meterRegistry);
    }
    
    @PostConstruct
    public void logConfiguration() {
        log.info("=== PERFORMANCE CONFIGURADA ===");
        log.info("Thread Pool Core: {}", properties.getThreadPool().getCoreSize());
        log.info("Thread Pool Max: {}", properties.getThreadPool().getMaxSize());
        log.info("Queue Capacity: {}", properties.getThreadPool().getQueueCapacity());
        log.info("Scheduler Pool: {}", properties.getScheduler().getPoolSize());
        log.info("===============================");
    }
}
```

### **🔧 PerformanceProperties.java**

```java
@ConfigurationProperties(prefix = "app.performance")
@Data
@Validated
public class PerformanceProperties {
    
    @Valid
    private ThreadPool threadPool = new ThreadPool();
    
    @Valid
    private Scheduler scheduler = new Scheduler();
    
    @Valid
    private Database database = new Database();
    
    @Valid
    private Cache cache = new Cache();
    
    @Data
    public static class ThreadPool {
        @Min(1)
        @Max(100)
        private int coreSize = 10;
        
        @Min(1)
        @Max(200)
        private int maxSize = 50;
        
        @Min(0)
        @Max(10000)
        private int queueCapacity = 1000;
        
        @Min(1)
        @Max(3600)
        private int keepAliveSeconds = 60;
    }
    
    @Data
    public static class Scheduler {
        @Min(1)
        @Max(20)
        private int poolSize = 5;
    }
    
    @Data
    public static class Database {
        @Min(1)
        @Max(300)
        private int queryTimeout = 30;
        
        @Min(1)
        @Max(10000)
        private int fetchSize = 1000;
        
        @Min(1)
        @Max(1000)
        private int batchSize = 50;
        
        private boolean enableQueryCache = true;
        private boolean enableStatistics = true;
    }
    
    @Data
    public static class Cache {
        @Min(60)
        @Max(86400)
        private int defaultTtl = 900;
        
        @Min(1)
        @Max(100)
        private int maxSize = 10000;
        
        private boolean enableCompression = true;
        private boolean enableStatistics = true;
    }
}
```

---

## 🔍 **TROUBLESHOOTING E DIAGNÓSTICO**

### **🛠️ DiagnosticController.java**

```java
@RestController
@RequestMapping("/actuator/diagnostic")
@Slf4j
public class DiagnosticController {
    
    private final DataSource writeDataSource;
    private final DataSource readDataSource;
    private final EventStore eventStore;
    private final ProjectionTrackerRepository projectionRepository;
    private final RedisTemplate<String, Object> redisTemplate;
    private final EventBus eventBus;
    private final CommandBus commandBus;
    
    public DiagnosticController(
            @Qualifier("writeDataSource") DataSource writeDataSource,
            @Qualifier("readDataSource") DataSource readDataSource,
            EventStore eventStore,
            ProjectionTrackerRepository projectionRepository,
            RedisTemplate<String, Object> redisTemplate,
            EventBus eventBus,
            CommandBus commandBus) {
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        this.eventStore = eventStore;
        this.projectionRepository = projectionRepository;
        this.redisTemplate = redisTemplate;
        this.eventBus = eventBus;
        this.commandBus = commandBus;
    }
    
    @GetMapping("/system-info")
    public ResponseEntity<Map<String, Object>> getSystemInfo() {
        Map<String, Object> info = new HashMap<>();
        
        // === INFORMAÇÕES DA JVM ===
        Runtime runtime = Runtime.getRuntime();
        Map<String, Object> jvm = new HashMap<>();
        jvm.put("maxMemory", formatBytes(runtime.maxMemory()));
        jvm.put("totalMemory", formatBytes(runtime.totalMemory()));
        jvm.put("freeMemory", formatBytes(runtime.freeMemory()));
        jvm.put("usedMemory", formatBytes(runtime.totalMemory() - runtime.freeMemory()));
        jvm.put("availableProcessors", runtime.availableProcessors());
        jvm.put("version", System.getProperty("java.version"));
        jvm.put("vendor", System.getProperty("java.vendor"));
        
        info.put("jvm", jvm);
        
        // === INFORMAÇÕES DO SISTEMA ===
        Map<String, Object> system = new HashMap<>();
        system.put("osName", System.getProperty("os.name"));
        system.put("osVersion", System.getProperty("os.version"));
        system.put("osArch", System.getProperty("os.arch"));
        system.put("userName", System.getProperty("user.name"));
        system.put("userTimezone", System.getProperty("user.timezone"));
        system.put("fileEncoding", System.getProperty("file.encoding"));
        
        info.put("system", system);
        
        // === INFORMAÇÕES DA APLICAÇÃO ===
        Map<String, Object> application = new HashMap<>();
        application.put("uptime", getUptime());
        application.put("startTime", getStartTime());
        application.put("activeProfiles", getActiveProfiles());
        application.put("springBootVersion", getSpringBootVersion());
        
        info.put("application", application);
        
        return ResponseEntity.ok(info);
    }
    
    @GetMapping("/connection-pools")
    public ResponseEntity<Map<String, Object>> getConnectionPoolInfo() {
        Map<String, Object> pools = new HashMap<>();
        
        try {
            // === WRITE DATASOURCE ===
            if (writeDataSource instanceof HikariDataSource) {
                HikariDataSource hikari = (HikariDataSource) writeDataSource;
                HikariPoolMXBean poolBean = hikari.getHikariPoolMXBean();
                
                Map<String, Object> writePool = new HashMap<>();
                writePool.put("activeConnections", poolBean.getActiveConnections());
                writePool.put("idleConnections", poolBean.getIdleConnections());
                writePool.put("totalConnections", poolBean.getTotalConnections());
                writePool.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                writePool.put("maximumPoolSize", hikari.getMaximumPoolSize());
                writePool.put("minimumIdle", hikari.getMinimumIdle());
                
                pools.put("writeDataSource", writePool);
            }
            
            // === READ DATASOURCE ===
            if (readDataSource instanceof HikariDataSource) {
                HikariDataSource hikari = (HikariDataSource) readDataSource;
                HikariPoolMXBean poolBean = hikari.getHikariPoolMXBean();
                
                Map<String, Object> readPool = new HashMap<>();
                readPool.put("activeConnections", poolBean.getActiveConnections());
                readPool.put("idleConnections", poolBean.getIdleConnections());
                readPool.put("totalConnections", poolBean.getTotalConnections());
                readPool.put("threadsAwaitingConnection", poolBean.getThreadsAwaitingConnection());
                readPool.put("maximumPoolSize", hikari.getMaximumPoolSize());
                readPool.put("minimumIdle", hikari.getMinimumIdle());
                
                pools.put("readDataSource", readPool);
            }
            
        } catch (Exception e) {
            log.error("Erro ao obter informações dos pools de conexão", e);
            pools.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(pools);
    }
    
    @GetMapping("/performance-metrics")
    public ResponseEntity<Map<String, Object>> getPerformanceMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        try {
            // === MÉTRICAS DE EVENT BUS ===
            EventBusStatistics eventBusStats = eventBus.getStatistics();
            Map<String, Object> eventBusMetrics = new HashMap<>();
            eventBusMetrics.put("totalEvents", eventBusStats.getTotalEvents());
            eventBusMetrics.put("successRate", eventBusStats.getSuccessRate());
            eventBusMetrics.put("errorRate", eventBusStats.getErrorRate());
            eventBusMetrics.put("throughput", eventBusStats.getThroughputPerSecond());
            
            metrics.put("eventBus", eventBusMetrics);
            
            // === MÉTRICAS DE COMMAND BUS ===
            CommandBusStatistics commandBusStats = commandBus.getStatistics();
            Map<String, Object> commandBusMetrics = new HashMap<>();
            commandBusMetrics.put("totalCommands", commandBusStats.getTotalCommands());
            commandBusMetrics.put("successRate", commandBusStats.getSuccessRate());
            commandBusMetrics.put("errorRate", commandBusStats.getErrorRate());
            commandBusMetrics.put("throughput", commandBusStats.getThroughputPerSecond());
            
            metrics.put("commandBus", commandBusMetrics);
            
            // === MÉTRICAS DE PROJEÇÕES ===
            long totalProjections = projectionRepository.count();
            long activeProjections = projectionRepository.countByStatus(ProjectionStatus.ACTIVE);
            long errorProjections = projectionRepository.countByStatus(ProjectionStatus.ERROR);
            
            Map<String, Object> projectionMetrics = new HashMap<>();
            projectionMetrics.put("total", totalProjections);
            projectionMetrics.put("active", activeProjections);
            projectionMetrics.put("errors", errorProjections);
            projectionMetrics.put("healthScore", totalProjections > 0 ? 
                (double) activeProjections / totalProjections : 1.0);
            
            metrics.put("projections", projectionMetrics);
            
        } catch (Exception e) {
            log.error("Erro ao obter métricas de performance", e);
            metrics.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(metrics);
    }
    
    @PostMapping("/test-connections")
    public ResponseEntity<Map<String, Object>> testConnections() {
        Map<String, Object> results = new HashMap<>();
        
        // === TESTE WRITE DATASOURCE ===
        Map<String, Object> writeTest = testDataSource(writeDataSource, "write");
        results.put("writeDataSource", writeTest);
        
        // === TESTE READ DATASOURCE ===
        Map<String, Object> readTest = testDataSource(readDataSource, "read");
        results.put("readDataSource", readTest);
        
        // === TESTE REDIS ===
        Map<String, Object> redisTest = testRedis();
        results.put("redis", redisTest);
        
        return ResponseEntity.ok(results);
    }
    
    @PostMapping("/clear-caches")
    public ResponseEntity<Map<String, Object>> clearCaches() {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // Limpar caches Redis
            Set<String> keys = redisTemplate.keys("hibrida:*");
            if (keys != null && !keys.isEmpty()) {
                redisTemplate.delete(keys);
                result.put("clearedKeys", keys.size());
            } else {
                result.put("clearedKeys", 0);
            }
            
            result.put("success", true);
            result.put("message", "Caches limpos com sucesso");
            
        } catch (Exception e) {
            log.error("Erro ao limpar caches", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }
        
        return ResponseEntity.ok(result);
    }
    
    // === MÉTODOS AUXILIARES ===
    
    private Map<String, Object> testDataSource(DataSource dataSource, String type) {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try (Connection connection = dataSource.getConnection()) {
            boolean isValid = connection.isValid(5);
            long responseTime = System.currentTimeMillis() - startTime;
            
            result.put("success", isValid);
            result.put("responseTime", responseTime + "ms");
            result.put("url", maskUrl(connection.getMetaData().getURL()));
            result.put("readOnly", connection.isReadOnly());
            
            if (isValid) {
                // Teste de query simples
                try (PreparedStatement stmt = connection.prepareStatement("SELECT 1")) {
                    stmt.executeQuery();
                    result.put("queryTest", "SUCCESS");
                }
            }
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTime", (System.currentTimeMillis() - startTime) + "ms");
        }
        
        return result;
    }
    
    private Map<String, Object> testRedis() {
        Map<String, Object> result = new HashMap<>();
        long startTime = System.currentTimeMillis();
        
        try {
            String testKey = "diagnostic:test:" + System.currentTimeMillis();
            String testValue = "test-value";
            
            // Teste de escrita
            redisTemplate.opsForValue().set(testKey, testValue, Duration.ofSeconds(10));
            
            // Teste de leitura
            String retrievedValue = (String) redisTemplate.opsForValue().get(testKey);
            
            // Limpeza
            redisTemplate.delete(testKey);
            
            long responseTime = System.currentTimeMillis() - startTime;
            boolean success = testValue.equals(retrievedValue);
            
            result.put("success", success);
            result.put("responseTime", responseTime + "ms");
            result.put("operations", "set/get/delete");
            
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            result.put("responseTime", (System.currentTimeMillis() - startTime) + "ms");
        }
        
        return result;
    }
    
    private String formatBytes(long bytes) {
        if (bytes < 1024) return bytes + " B";
        if (bytes < 1024 * 1024) return String.format("%.1f KB", bytes / 1024.0);
        if (bytes < 1024 * 1024 * 1024) return String.format("%.1f MB", bytes / (1024.0 * 1024));
        return String.format("%.1f GB", bytes / (1024.0 * 1024 * 1024));
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
    
    private String getStartTime() {
        long startTime = ManagementFactory.getRuntimeMXBean().getStartTime();
        return Instant.ofEpochMilli(startTime).toString();
    }
    
    private String[] getActiveProfiles() {
        return System.getProperty("spring.profiles.active", "default").split(",");
    }
    
    private String getSpringBootVersion() {
        return SpringBootVersion.getVersion();
    }
    
    private String maskUrl(String url) {
        if (url == null) return null;
        return url.replaceAll("://[^:]+:[^@]+@", "://***:***@");
    }
}
```

---

## 🎯 **CONFIGURAÇÕES POR AMBIENTE**

### **📋 application-prod.yml - Produção Otimizada**

```yaml
# === CONFIGURAÇÕES DE PRODUÇÃO ===
spring:
  config:
    activate:
      on-profile: prod

# === DATASOURCES OTIMIZADAS ===
spring:
  datasource:
    write:
      url: ${WRITE_DB_URL}
      username: ${WRITE_DB_USERNAME}
      password: ${WRITE_DB_PASSWORD}
      
      hikari:
        maximum-pool-size: ${WRITE_DB_POOL_MAX:30}
        minimum-idle: ${WRITE_DB_POOL_MIN:10}
        connection-timeout: 20000
        idle-timeout: 300000
        max-lifetime: 1200000
        validation-timeout: 3000
        leak-detection-threshold: 60000
        
        # Configurações de produção
        register-mbeans: true
        allow-pool-suspension: false
        
        # Configurações de conexão
        data-source-properties:
          cachePrepStmts: true
          prepStmtCacheSize: 500
          prepStmtCacheSqlLimit: 4096
          useServerPrepStmts: true
          rewriteBatchedStatements: true
          cacheResultSetMetadata: true
          cacheServerConfiguration: true
          elideSetAutoCommits: true
          maintainTimeStats: false
    
    read:
      url: ${READ_DB_URL}
      username: ${READ_DB_USERNAME}
      password: ${READ_DB_PASSWORD}
      
      hikari:
        maximum-pool-size: ${READ_DB_POOL_MAX:50}
        minimum-idle: ${READ_DB_POOL_MIN:20}
        connection-timeout: 20000
        idle-timeout: 180000
        max-lifetime: 1200000
        validation-timeout: 3000
        
        # Otimizações para read
        data-source-properties:
          cachePrepStmts: true
          prepStmtCacheSize: 1000
          prepStmtCacheSqlLimit: 8192
          useServerPrepStmts: true
          defaultFetchSize: 2000
          cacheResultSetMetadata: true
          cacheServerConfiguration: true

# === REDIS OTIMIZADO ===
spring:
  redis:
    host: ${REDIS_HOST}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD}
    database: ${REDIS_DATABASE:0}
    timeout: 1000ms
    
    lettuce:
      pool:
        max-active: 50
        max-idle: 20
        min-idle: 10
        max-wait: 1000ms
      
      cluster:
        refresh:
          adaptive: true
          period: 30s

# === KAFKA OTIMIZADO ===
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS}
    
    producer:
      acks: all
      retries: 5
      batch-size: 32768
      linger-ms: 10
      buffer-memory: 67108864
      compression-type: snappy
      
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        request.timeout.ms: 30000
        delivery.timeout.ms: 120000
        retry.backoff.ms: 1000
    
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP}
      auto-offset-reset: earliest
      enable-auto-commit: false
      max-poll-records: 1000
      session-timeout-ms: 30000
      concurrency: 5
      
      properties:
        fetch.min.bytes: 2048
        fetch.max.wait.ms: 1000
        heartbeat.interval.ms: 3000
        max.partition.fetch.bytes: 2097152

# === PERFORMANCE OTIMIZADA ===
app:
  performance:
    thread-pool:
      core-size: 20
      max-size: 100
      queue-capacity: 2000
      keep-alive-seconds: 120
    
    scheduler:
      pool-size: 10
    
    database:
      query-timeout: 60
      fetch-size: 2000
      batch-size: 100
    
    cache:
      default-ttl: 1800  # 30 minutos
      max-size: 50000
      enable-compression: true

# === LOGGING OTIMIZADO ===
logging:
  level:
    root: WARN
    com.seguradora.hibrida: INFO
    org.springframework.transaction: ERROR
    org.hibernate.SQL: ERROR
  
  pattern:
    file: "%d{yyyy-MM-dd HH:mm:ss.SSS} [%thread] %-5level [%X{correlationId:-},%X{traceId:-}] %logger{50} - %msg%n"
  
  file:
    name: ${LOG_FILE:/var/log/hibrida/application.log}
    max-size: 200MB
    max-history: 60
    total-size-cap: 10GB

# === MONITORAMENTO RESTRITO ===
management:
  endpoints:
    web:
      exposure:
        include: health,info,metrics,prometheus
  
  endpoint:
    health:
      show-details: never
      show-components: false
  
  metrics:
    export:
      prometheus:
        enabled: true
        step: 15s

# === SEGURANÇA REFORÇADA ===
spring:
  security:
    user:
      name: ${ACTUATOR_USER}
      password: ${ACTUATOR_PASSWORD}
      roles: ACTUATOR

server:
  port: 8080
  servlet:
    context-path: /api
  
  tomcat:
    threads:
      max: 300
      min-spare: 20
    max-connections: 10000
    connection-timeout: 20000
    keep-alive-timeout: 60000
    max-http-post-size: 10MB
  
  compression:
    enabled: true
    min-response-size: 2048
```

---

## 🛠️ **TROUBLESHOOTING GUIDE**

### **🔍 Problemas Comuns e Soluções**

#### **1. Problemas de Conexão com Banco**
```yaml
# Diagnóstico
management:
  endpoint:
    health:
      show-details: always

# Verificar logs
logging:
  level:
    com.zaxxer.hikari: DEBUG
    org.springframework.jdbc: DEBUG

# Soluções
spring:
  datasource:
    write:
      hikari:
        leak-detection-threshold: 30000  # Detectar vazamentos
        connection-test-query: "SELECT 1"
        validation-timeout: 5000
```

#### **2. Problemas de Performance**
```yaml
# Monitoramento detalhado
management:
  metrics:
    distribution:
      percentiles-histogram:
        "[http.server.requests]": true
        "[spring.data.repository.invocations]": true

# Otimizações
spring:
  jpa:
    properties:
      hibernate:
        jdbc.batch_size: 50
        order_inserts: true
        order_updates: true
        jdbc.batch_versioned_data: true
```

#### **3. Problemas de Cache**
```yaml
# Diagnóstico Redis
spring:
  redis:
    timeout: 2000ms
    lettuce:
      pool:
        max-active: 20
        max-wait: 2000ms

# Fallback sem cache
spring:
  cache:
    type: none  # Desabilitar temporariamente
```

#### **4. Problemas de Memória**
```yaml
# JVM Tuning
JAVA_OPTS: >
  -Xms2g
  -Xmx4g
  -XX:+UseG1GC
  -XX:MaxGCPauseMillis=200
  -XX:+HeapDumpOnOutOfMemoryError
  -XX:HeapDumpPath=/var/log/hibrida/

# Monitoramento
management:
  metrics:
    tags:
      jvm: enabled
```

---

## 📚 **BOAS PRÁTICAS E OTIMIZAÇÕES**

### **✅ Checklist de Configuração**

#### **Segurança:**
- [ ] Senhas externalizadas via variáveis de ambiente
- [ ] URLs de banco mascaradas nos logs
- [ ] Actuator com autenticação
- [ ] Endpoints sensíveis protegidos

#### **Performance:**
- [ ] Pools de conexão dimensionados adequadamente
- [ ] Cache configurado com TTL apropriado
- [ ] Timeouts configurados
- [ ] Compressão habilitada

#### **Monitoramento:**
- [ ] Health checks customizados implementados
- [ ] Métricas de negócio configuradas
- [ ] Alertas configurados
- [ ] Logs estruturados

#### **Manutenibilidade:**
- [ ] Configurações por ambiente separadas
- [ ] Propriedades documentadas
- [ ] Valores padrão sensatos
- [ ] Validação de configuração

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Boot Configuration Properties](https://docs.spring.io/spring-boot/docs/current/reference/html/application-properties.html)
- [HikariCP Configuration](https://github.com/brettwooldridge/HikariCP#configuration-knobs-baby)
- [JVM Tuning Guide](https://docs.oracle.com/javase/8/docs/technotes/guides/vm/gctuning/)
- [Production Deployment](https://docs.spring.io/spring-boot/docs/current/reference/html/deployment.html)

### **📖 Documentação Complementar:**
- **Configuração**: `com.seguradora.hibrida.config`
- **Properties**: Classes `*Properties.java`
- **Health**: `*HealthIndicator.java`
- **Metrics**: `*Metrics.java`

---

**📝 Parte 5 de 5 - Configurações Avançadas e Troubleshooting**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Conclusão**: Configuração completa da arquitetura híbrida

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Configurações avançadas e troubleshooting  
**⏱️ Tempo total do módulo:** 4 horas  
**🔧 Hands-on:** Configuração completa de ambiente produtivo