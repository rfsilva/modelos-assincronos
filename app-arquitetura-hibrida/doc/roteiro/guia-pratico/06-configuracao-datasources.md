# 💾 ETAPA 06: CONFIGURAÇÃO DE DATASOURCES
## DataSources & Persistence - Otimização e Monitoramento

### 🎯 **OBJETIVO DA ETAPA**

Configurar e otimizar os DataSources para write e read, implementar connection pooling eficiente, configurar health checks e estabelecer monitoramento de performance de banco de dados.

**⏱️ Duração Estimada:** 2-3 horas  
**👥 Participantes:** Desenvolvedor + DBA + Tech Lead  
**📋 Pré-requisitos:** Etapa 05 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **🔧 1. CONFIGURAÇÃO DE DATASOURCES**

#### **📊 Configuração Write DataSource:**
```yaml
# application.yml - Write DataSource
spring:
  datasource:
    write:
      enabled: true
      url: jdbc:postgresql://localhost:5432/seguradora_write
      username: ${DB_WRITE_USER:seguradora_write}
      password: ${DB_WRITE_PASSWORD:password}
      driver-class-name: org.postgresql.Driver
      
      # HikariCP Configuration
      hikari:
        pool-name: WritePool
        maximum-pool-size: 20
        minimum-idle: 5
        connection-timeout: 30000
        idle-timeout: 600000
        max-lifetime: 1800000
        validation-timeout: 5000
        connection-test-query: SELECT 1
        
      # JPA Configuration
      jpa:
        hibernate:
          ddl-auto: validate
        properties:
          hibernate:
            dialect: org.hibernate.dialect.PostgreSQLDialect
            format_sql: false
            show_sql: false
            order_inserts: true
            order_updates: true
            batch_size: 25
            
      # Flyway Configuration
      flyway:
        enabled: true
        locations: classpath:db/migration
        table: flyway_schema_history_write
        validate-on-migrate: true
```

#### **📈 Configuração Read DataSource:**
```yaml
# application.yml - Read DataSource
spring:
  datasource:
    read:
      enabled: true
      url: jdbc:postgresql://localhost:5432/seguradora_read
      username: ${DB_READ_USER:seguradora_read}
      password: ${DB_READ_PASSWORD:password}
      driver-class-name: org.postgresql.Driver
      read-only: true
      
      # HikariCP Configuration (otimizado para leitura)
      hikari:
        pool-name: ReadPool
        maximum-pool-size: 50  # Mais conexões para leitura
        minimum-idle: 10
        connection-timeout: 20000
        idle-timeout: 300000
        max-lifetime: 1200000
        validation-timeout: 3000
        connection-test-query: SELECT 1
        
      # JPA Configuration (otimizado para consultas)
      jpa:
        hibernate:
          ddl-auto: validate
        properties:
          hibernate:
            dialect: org.hibernate.dialect.PostgreSQLDialect
            format_sql: false
            show_sql: false
            use_query_cache: true
            use_second_level_cache: true
            fetch_size: 50
            
      # Flyway Configuration
      flyway:
        enabled: true
        locations: classpath:db/migration-projections
        table: flyway_schema_history_read
        validate-on-migrate: true
        
      # Fallback Configuration
      fallback:
        enabled: true
        fallback-url: jdbc:postgresql://localhost:5432/seguradora_write
        max-retries: 3
        retry-interval: 5000
        failure-detection-timeout: 30000
```

#### **✅ Checklist de Configuração:**
- [ ] **Write DataSource** configurado adequadamente
- [ ] **Read DataSource** otimizado para consultas
- [ ] **Connection pooling** configurado por tipo de uso
- [ ] **Fallback** configurado para read DataSource
- [ ] **Flyway** configurado para ambos os bancos

---

### **🏗️ 2. IMPLEMENTAÇÃO DE CONFIGURAÇÃO JAVA**

#### **⚙️ Write DataSource Configuration:**
```java
@Configuration
@EnableJpaRepositories(
    basePackages = "com.seguradora.hibrida.*.aggregate.repository",
    entityManagerFactoryRef = "writeEntityManagerFactory",
    transactionManagerRef = "writeTransactionManager"
)
@EnableConfigurationProperties(WriteDataSourceProperties.class)
public class WriteDataSourceConfiguration {
    
    private final WriteDataSourceProperties properties;
    
    public WriteDataSourceConfiguration(WriteDataSourceProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @Primary
    @ConfigurationProperties("spring.datasource.write.hikari")
    public DataSource writeDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configurações básicas
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        
        // Configurações de pool otimizadas para escrita
        config.setPoolName(properties.getHikari().getPoolName());
        config.setMaximumPoolSize(properties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(properties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(properties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(properties.getHikari().getIdleTimeout());
        config.setMaxLifetime(properties.getHikari().getMaxLifetime());
        config.setValidationTimeout(properties.getHikari().getValidationTimeout());
        config.setConnectionTestQuery(properties.getConnectionTestQuery());
        
        // Configurações específicas para escrita
        config.setAutoCommit(false); // Controle manual de transações
        config.setReadOnly(false);
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        
        // Configurações de performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        
        return new HikariDataSource(config);
    }
    
    @Bean
    @Primary
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory(
            @Qualifier("writeDataSource") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
            "com.seguradora.hibrida.eventstore.entity",
            "com.seguradora.hibrida.snapshot.entity",
            "com.seguradora.hibrida.*.aggregate"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(properties.getJpa().isShowSql());
        factory.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", properties.getJpa().getDialect());
        jpaProperties.put("hibernate.hbm2ddl.auto", properties.getJpa().getDdlAuto());
        jpaProperties.put("hibernate.format_sql", properties.getJpa().isFormatSql());
        jpaProperties.put("hibernate.order_inserts", properties.getJpa().isOrderInserts());
        jpaProperties.put("hibernate.order_updates", properties.getJpa().isOrderUpdates());
        jpaProperties.put("hibernate.jdbc.batch_size", properties.getJpa().getBatchSize());
        
        factory.setJpaProperties(jpaProperties);
        return factory;
    }
    
    @Bean
    @Primary
    public PlatformTransactionManager writeTransactionManager(
            @Qualifier("writeEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDefaultTimeout(30); // 30 segundos
        return transactionManager;
    }
    
    @Bean
    public HealthIndicator writeDataSourceHealthIndicator(
            @Qualifier("writeDataSource") DataSource dataSource) {
        return new SimpleDataSourceHealthIndicator(dataSource, "Write DataSource");
    }
}
```

#### **📊 Read DataSource Configuration:**
```java
@Configuration
@EnableJpaRepositories(
    basePackages = {
        "com.seguradora.hibrida.query.repository",
        "com.seguradora.hibrida.projection.tracking"
    },
    entityManagerFactoryRef = "readEntityManagerFactory",
    transactionManagerRef = "readTransactionManager"
)
@EnableConfigurationProperties(ReadDataSourceProperties.class)
public class ReadDataSourceConfiguration {
    
    private final ReadDataSourceProperties properties;
    
    public ReadDataSourceConfiguration(ReadDataSourceProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @ConfigurationProperties("spring.datasource.read.hikari")
    public DataSource readDataSource() {
        if (!properties.isEnabled()) {
            return writeDataSource(); // Fallback para write se read estiver desabilitado
        }
        
        HikariConfig config = new HikariConfig();
        
        // Configurações básicas
        config.setJdbcUrl(properties.getUrl());
        config.setUsername(properties.getUsername());
        config.setPassword(properties.getPassword());
        config.setDriverClassName(properties.getDriverClassName());
        
        // Configurações de pool otimizadas para leitura
        config.setPoolName(properties.getHikari().getPoolName());
        config.setMaximumPoolSize(properties.getHikari().getMaximumPoolSize());
        config.setMinimumIdle(properties.getHikari().getMinimumIdle());
        config.setConnectionTimeout(properties.getHikari().getConnectionTimeout());
        config.setIdleTimeout(properties.getHikari().getIdleTimeout());
        config.setMaxLifetime(properties.getHikari().getMaxLifetime());
        config.setValidationTimeout(properties.getHikari().getValidationTimeout());
        config.setConnectionTestQuery(properties.getConnectionTestQuery());
        
        // Configurações específicas para leitura
        config.setAutoCommit(true); // Auto-commit para consultas
        config.setReadOnly(properties.isReadOnly());
        config.setTransactionIsolation("TRANSACTION_READ_COMMITTED");
        
        // Configurações de performance para leitura
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500"); // Cache maior para leitura
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("defaultRowFetchSize", String.valueOf(properties.getFetchSize()));
        
        return new HikariDataSource(config);
    }
    
    @Bean
    public DataSource readDataSourceWithFallback(@Qualifier("readDataSource") DataSource readDataSource,
                                                 @Qualifier("writeDataSource") DataSource writeDataSource) {
        
        if (!properties.getFallback().isEnabled()) {
            return readDataSource;
        }
        
        return new FallbackDataSource(readDataSource, writeDataSource, properties.getFallback());
    }
    
    @Bean
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory(
            @Qualifier("readDataSourceWithFallback") DataSource dataSource) {
        
        LocalContainerEntityManagerFactoryBean factory = new LocalContainerEntityManagerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setPackagesToScan(
            "com.seguradora.hibrida.query.model",
            "com.seguradora.hibrida.projection.tracking"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        vendorAdapter.setGenerateDdl(false);
        vendorAdapter.setShowSql(properties.getJpa().isShowSql());
        factory.setJpaVendorAdapter(vendorAdapter);
        
        Properties jpaProperties = new Properties();
        jpaProperties.put("hibernate.dialect", properties.getJpa().getDialect());
        jpaProperties.put("hibernate.hbm2ddl.auto", properties.getJpa().getDdlAuto());
        jpaProperties.put("hibernate.format_sql", properties.getJpa().isFormatSql());
        jpaProperties.put("hibernate.use_query_cache", properties.getJpa().isUseQueryCache());
        jpaProperties.put("hibernate.use_second_level_cache", properties.getJpa().isUseSecondLevelCache());
        jpaProperties.put("hibernate.jdbc.fetch_size", properties.getJpa().getFetchSize());
        
        factory.setJpaProperties(jpaProperties);
        return factory;
    }
    
    @Bean
    public PlatformTransactionManager readTransactionManager(
            @Qualifier("readEntityManagerFactory") EntityManagerFactory entityManagerFactory) {
        
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(entityManagerFactory);
        transactionManager.setDefaultTimeout(60); // Timeout maior para consultas
        return transactionManager;
    }
    
    @Bean
    public HealthIndicator readDataSourceHealthIndicator(
            @Qualifier("readDataSourceWithFallback") DataSource dataSource) {
        return new SimpleDataSourceHealthIndicator(dataSource, "Read DataSource");
    }
}
```

#### **✅ Checklist de Configuração Java:**
- [ ] **Write DataSource** configurado com otimizações para escrita
- [ ] **Read DataSource** configurado com otimizações para leitura
- [ ] **Fallback mechanism** implementado
- [ ] **Transaction managers** configurados adequadamente
- [ ] **Health indicators** implementados

---

### **🔄 3. FALLBACK DATASOURCE**

#### **🛡️ Implementação de Fallback:**
```java
public class FallbackDataSource implements DataSource {
    
    private final DataSource primaryDataSource;
    private final DataSource fallbackDataSource;
    private final FallbackConfig config;
    private final AtomicBoolean usingFallback = new AtomicBoolean(false);
    private final AtomicLong lastFailureTime = new AtomicLong(0);
    
    public FallbackDataSource(DataSource primaryDataSource, 
                             DataSource fallbackDataSource, 
                             FallbackConfig config) {
        this.primaryDataSource = primaryDataSource;
        this.fallbackDataSource = fallbackDataSource;
        this.config = config;
    }
    
    @Override
    public Connection getConnection() throws SQLException {
        return getConnection(null, null);
    }
    
    @Override
    public Connection getConnection(String username, String password) throws SQLException {
        // Tentar usar primary primeiro
        if (!usingFallback.get() || shouldRetryPrimary()) {
            try {
                Connection connection = username != null 
                    ? primaryDataSource.getConnection(username, password)
                    : primaryDataSource.getConnection();
                    
                // Se conseguiu conectar, resetar fallback
                if (usingFallback.get()) {
                    log.info("Reconectado ao DataSource primário");
                    usingFallback.set(false);
                }
                
                return connection;
                
            } catch (SQLException e) {
                log.warn("Falha ao conectar no DataSource primário: {}", e.getMessage());
                markPrimaryAsDown();
                
                // Continuar para tentar fallback
            }
        }
        
        // Usar fallback
        try {
            Connection connection = username != null 
                ? fallbackDataSource.getConnection(username, password)
                : fallbackDataSource.getConnection();
                
            if (!usingFallback.get()) {
                log.warn("Usando DataSource de fallback");
                usingFallback.set(true);
            }
            
            return connection;
            
        } catch (SQLException e) {
            log.error("Falha também no DataSource de fallback: {}", e.getMessage());
            throw e;
        }
    }
    
    private boolean shouldRetryPrimary() {
        long timeSinceLastFailure = System.currentTimeMillis() - lastFailureTime.get();
        return timeSinceLastFailure > config.getRetryInterval();
    }
    
    private void markPrimaryAsDown() {
        lastFailureTime.set(System.currentTimeMillis());
        usingFallback.set(true);
    }
    
    public boolean isUsingFallback() {
        return usingFallback.get();
    }
    
    // Implementar outros métodos da interface DataSource...
}
```

#### **📊 Health Check com Fallback:**
```java
@Component
public class DataSourceHealthIndicator implements HealthIndicator {
    
    private final DataSource writeDataSource;
    private final FallbackDataSource readDataSource;
    
    @Override
    public Health health() {
        Health.Builder builder = Health.up();
        
        // Verificar write DataSource
        try {
            checkDataSource(writeDataSource, "SELECT 1");
            builder.withDetail("writeDataSource", "healthy");
        } catch (Exception e) {
            builder.down().withDetail("writeDataSource", "unhealthy: " + e.getMessage());
        }
        
        // Verificar read DataSource
        try {
            checkDataSource(readDataSource, "SELECT 1");
            builder.withDetail("readDataSource", "healthy");
            builder.withDetail("readDataSourceUsingFallback", readDataSource.isUsingFallback());
        } catch (Exception e) {
            builder.down().withDetail("readDataSource", "unhealthy: " + e.getMessage());
        }
        
        return builder.build();
    }
    
    private void checkDataSource(DataSource dataSource, String query) throws SQLException {
        try (Connection connection = dataSource.getConnection();
             PreparedStatement statement = connection.prepareStatement(query);
             ResultSet resultSet = statement.executeQuery()) {
            
            if (!resultSet.next()) {
                throw new SQLException("Query não retornou resultados");
            }
        }
    }
}
```

#### **✅ Checklist de Fallback:**
- [ ] **Fallback DataSource** implementado
- [ ] **Retry mechanism** configurado
- [ ] **Health checks** incluindo status de fallback
- [ ] **Logs** adequados para troubleshooting
- [ ] **Métricas** de uso de fallback

---

### **📊 4. MONITORAMENTO E MÉTRICAS**

#### **📈 Métricas de DataSource:**
```java
@Component
public class DataSourceMetrics {
    
    private final MeterRegistry meterRegistry;
    private final DataSource writeDataSource;
    private final FallbackDataSource readDataSource;
    
    public DataSourceMetrics(MeterRegistry meterRegistry,
                           @Qualifier("writeDataSource") DataSource writeDataSource,
                           @Qualifier("readDataSourceWithFallback") FallbackDataSource readDataSource) {
        this.meterRegistry = meterRegistry;
        this.writeDataSource = writeDataSource;
        this.readDataSource = readDataSource;
        
        initializeGauges();
    }
    
    private void initializeGauges() {
        // Métricas do pool de conexões write
        if (writeDataSource instanceof HikariDataSource) {
            HikariDataSource hikariWrite = (HikariDataSource) writeDataSource;
            
            Gauge.builder("datasource.connections.active")
                .tag("pool", "write")
                .register(meterRegistry, hikariWrite.getHikariPoolMXBean(), 
                         pool -> pool.getActiveConnections());
                         
            Gauge.builder("datasource.connections.idle")
                .tag("pool", "write")
                .register(meterRegistry, hikariWrite.getHikariPoolMXBean(), 
                         pool -> pool.getIdleConnections());
                         
            Gauge.builder("datasource.connections.total")
                .tag("pool", "write")
                .register(meterRegistry, hikariWrite.getHikariPoolMXBean(), 
                         pool -> pool.getTotalConnections());
                         
            Gauge.builder("datasource.connections.waiting")
                .tag("pool", "write")
                .register(meterRegistry, hikariWrite.getHikariPoolMXBean(), 
                         pool -> pool.getThreadsAwaitingConnection());
        }
        
        // Métricas de fallback
        Gauge.builder("datasource.fallback.active")
            .tag("pool", "read")
            .register(meterRegistry, readDataSource, 
                     ds -> ds.isUsingFallback() ? 1.0 : 0.0);
    }
    
    public void recordConnectionAcquisition(String pool, long timeMs) {
        meterRegistry.timer("datasource.connection.acquisition.time",
            "pool", pool)
            .record(timeMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordQueryExecution(String pool, String operation, long timeMs) {
        meterRegistry.timer("datasource.query.execution.time",
            "pool", pool,
            "operation", operation)
            .record(timeMs, TimeUnit.MILLISECONDS);
    }
    
    public void recordConnectionFailure(String pool, String reason) {
        meterRegistry.counter("datasource.connection.failures",
            "pool", pool,
            "reason", reason)
            .increment();
    }
}
```

#### **🔍 Interceptor para Métricas:**
```java
@Component
public class DataSourceMetricsInterceptor implements MethodInterceptor {
    
    private final DataSourceMetrics metrics;
    
    public DataSourceMetricsInterceptor(DataSourceMetrics metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public Object invoke(MethodInvocation invocation) throws Throwable {
        String methodName = invocation.getMethod().getName();
        
        if ("getConnection".equals(methodName)) {
            return interceptGetConnection(invocation);
        }
        
        return invocation.proceed();
    }
    
    private Object interceptGetConnection(MethodInvocation invocation) throws Throwable {
        long startTime = System.currentTimeMillis();
        String pool = determinePool(invocation.getThis());
        
        try {
            Object result = invocation.proceed();
            
            long duration = System.currentTimeMillis() - startTime;
            metrics.recordConnectionAcquisition(pool, duration);
            
            return result;
            
        } catch (SQLException e) {
            metrics.recordConnectionFailure(pool, e.getClass().getSimpleName());
            throw e;
        }
    }
    
    private String determinePool(Object dataSource) {
        if (dataSource instanceof HikariDataSource) {
            HikariDataSource hikari = (HikariDataSource) dataSource;
            return hikari.getPoolName().toLowerCase();
        }
        return "unknown";
    }
}
```

#### **✅ Checklist de Monitoramento:**
- [ ] **Métricas de connection pool** implementadas
- [ ] **Métricas de performance** de queries
- [ ] **Métricas de fallback** configuradas
- [ ] **Alertas** para problemas de conexão
- [ ] **Dashboards** para monitoramento

---

### **🔧 5. OTIMIZAÇÕES DE PERFORMANCE**

#### **⚡ Configurações de Performance:**
```yaml
# Configurações específicas por ambiente
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:local}

---
# Perfil de desenvolvimento
spring:
  config:
    activate:
      on-profile: local
  datasource:
    write:
      hikari:
        maximum-pool-size: 5
        minimum-idle: 2
    read:
      hikari:
        maximum-pool-size: 10
        minimum-idle: 3

---
# Perfil de produção
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    write:
      hikari:
        maximum-pool-size: 50
        minimum-idle: 10
        connection-timeout: 20000
        validation-timeout: 3000
    read:
      hikari:
        maximum-pool-size: 100
        minimum-idle: 20
        connection-timeout: 15000
        validation-timeout: 2000
      jpa:
        properties:
          hibernate:
            jdbc:
              fetch_size: 100
              batch_size: 50
```

#### **🎯 Connection Pool Tuning:**
```java
@Component
@ConditionalOnProperty(name = "datasource.tuning.enabled", havingValue = "true")
public class DataSourceTuner {
    
    private final HikariDataSource writeDataSource;
    private final HikariDataSource readDataSource;
    private final MeterRegistry meterRegistry;
    
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void tuneConnectionPools() {
        tuneWritePool();
        tuneReadPool();
    }
    
    private void tuneWritePool() {
        HikariPoolMXBean poolBean = writeDataSource.getHikariPoolMXBean();
        
        int activeConnections = poolBean.getActiveConnections();
        int totalConnections = poolBean.getTotalConnections();
        int waitingThreads = poolBean.getThreadsAwaitingConnection();
        
        // Se há muitas threads esperando, aumentar pool
        if (waitingThreads > 5 && totalConnections < 50) {
            int newSize = Math.min(totalConnections + 5, 50);
            writeDataSource.setMaximumPoolSize(newSize);
            log.info("Aumentando pool de escrita para: {}", newSize);
        }
        
        // Se pool está subutilizado, diminuir
        if (activeConnections < totalConnections * 0.3 && totalConnections > 10) {
            int newSize = Math.max(totalConnections - 2, 10);
            writeDataSource.setMaximumPoolSize(newSize);
            log.info("Diminuindo pool de escrita para: {}", newSize);
        }
    }
    
    private void tuneReadPool() {
        // Lógica similar para read pool
        // Considerando que leitura pode ter mais variação de carga
    }
}
```

#### **✅ Checklist de Otimizações:**
- [ ] **Connection pool sizing** otimizado por ambiente
- [ ] **Fetch sizes** configurados adequadamente
- [ ] **Batch sizes** otimizados para operações
- [ ] **Timeouts** configurados por tipo de operação
- [ ] **Auto-tuning** implementado (opcional)

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **🔧 Configuração:**
- [ ] **Write DataSource** configurado e funcionando
- [ ] **Read DataSource** otimizado para consultas
- [ ] **Connection pooling** adequado para cada uso
- [ ] **Fallback mechanism** operacional
- [ ] **Migrations** executando corretamente

#### **📊 Performance:**
- [ ] **Connection acquisition** dentro dos SLAs
- [ ] **Query performance** adequada
- [ ] **Pool utilization** otimizada
- [ ] **Fallback** funcionando quando necessário
- [ ] **Métricas** sendo coletadas

#### **🏥 Saúde:**
- [ ] **Health checks** funcionais
- [ ] **Monitoring** operacional
- [ ] **Alertas** configurados
- [ ] **Logs** estruturados
- [ ] **Dashboards** informativos

#### **🧪 Testes:**
- [ ] **Testes de conexão** passando
- [ ] **Testes de fallback** funcionais
- [ ] **Testes de performance** adequados
- [ ] **Testes de health check** validados
- [ ] **Testes de carga** executados

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Pool Sizing Inadequado:**
```yaml
# ❌ EVITAR: Pool muito pequeno para produção
hikari:
  maximum-pool-size: 5  # Muito pequeno
  minimum-idle: 1

# ✅ PREFERIR: Pool dimensionado adequadamente
hikari:
  maximum-pool-size: 50  # Baseado na carga esperada
  minimum-idle: 10       # Conexões sempre disponíveis
```

#### **🚫 Timeouts Inadequados:**
```yaml
# ❌ EVITAR: Timeouts muito baixos
hikari:
  connection-timeout: 5000    # Muito baixo
  validation-timeout: 1000    # Muito baixo

# ✅ PREFERIR: Timeouts adequados
hikari:
  connection-timeout: 30000   # 30 segundos
  validation-timeout: 5000    # 5 segundos
```

#### **🚫 Falta de Monitoramento:**
```java
// ❌ EVITAR: DataSource sem monitoramento
@Bean
public DataSource dataSource() {
    return new HikariDataSource(config); // Sem métricas
}

// ✅ PREFERIR: DataSource com monitoramento
@Bean
public DataSource dataSource() {
    HikariDataSource ds = new HikariDataSource(config);
    ds.setMetricRegistry(meterRegistry); // Com métricas
    return ds;
}
```

### **✅ Boas Práticas:**

#### **🎯 Configuração:**
- **Sempre** dimensionar pools baseado na carga real
- **Sempre** configurar timeouts adequados
- **Sempre** implementar fallback para read
- **Sempre** usar profiles por ambiente

#### **📊 Monitoramento:**
- **Sempre** monitorar métricas de connection pool
- **Sempre** configurar alertas para problemas
- **Sempre** implementar health checks
- **Sempre** usar logs estruturados

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 07 - Implementação de APIs](./07-implementacao-apis.md)**
2. Criar controllers REST
3. Implementar DTOs e validações
4. Configurar documentação Swagger

### **📋 Preparação para Próxima Etapa:**
- [ ] **REST API patterns** estudados
- [ ] **DTO design** compreendido
- [ ] **Swagger/OpenAPI** revisado
- [ ] **Testes de DataSource** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Configurações](../09-configuracoes-README.md)**: Guia completo de configuração
- **HikariCP Documentation**: Otimizações de connection pool
- **PostgreSQL Tuning**: Otimizações de banco

### **🛠️ Ferramentas de Monitoramento:**
- **pgAdmin**: Monitoramento PostgreSQL
- **HikariCP Metrics**: Métricas de connection pool
- **Grafana**: Dashboards de monitoramento
- **Prometheus**: Coleta de métricas

### **🧪 Ferramentas de Teste:**
- **JMeter**: Testes de carga de banco
- **pgbench**: Benchmark PostgreSQL
- **Connection Pool Tester**: Testes de pool

---

**📋 Checklist Total:** 40+ itens de validação  
**⏱️ Tempo Médio:** 2-3 horas  
**🎯 Resultado:** DataSources otimizados e monitorados  
**✅ Próxima Etapa:** Implementação de APIs