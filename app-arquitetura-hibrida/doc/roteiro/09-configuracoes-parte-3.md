# ⚙️ CONFIGURAÇÕES E DATASOURCES - PARTE 3
## Configuração de Cache e Message Broker

### 🎯 **OBJETIVOS DESTA PARTE**
- Configurar Redis para cache de consultas
- Implementar configuração de Kafka para Event Bus
- Configurar cache multi-level
- Otimizar message broker para alta performance

---

## 🚀 **CONFIGURAÇÃO DE CACHE (REDIS)**

### **📊 Estratégia de Cache**

O cache na arquitetura híbrida é usado para:

#### **Casos de Uso:**
- ✅ **Query Cache**: Resultados de consultas frequentes
- ✅ **Session Cache**: Dados de sessão de usuário
- ✅ **Metadata Cache**: Configurações e metadados
- ✅ **Projection Cache**: Projeções computadas
- ✅ **Rate Limiting**: Controle de taxa de requisições

### **🔧 RedisConfiguration.java**

```java
@Configuration
@EnableCaching
@EnableConfigurationProperties(RedisProperties.class)
@Slf4j
public class RedisConfiguration {
    
    private final RedisProperties properties;
    
    public RedisConfiguration(RedisProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    @Primary
    public LettuceConnectionFactory redisConnectionFactory() {
        log.info("Configurando Redis Connection Factory");
        log.info("Host: {}:{}", properties.getHost(), properties.getPort());
        
        // === CONFIGURAÇÃO BÁSICA ===
        RedisStandaloneConfiguration config = new RedisStandaloneConfiguration();
        config.setHostName(properties.getHost());
        config.setPort(properties.getPort());
        config.setDatabase(properties.getDatabase());
        
        if (properties.getPassword() != null && !properties.getPassword().isEmpty()) {
            config.setPassword(properties.getPassword());
        }
        
        // === CONFIGURAÇÃO DO POOL ===
        GenericObjectPoolConfig<StatefulRedisConnection<String, String>> poolConfig = 
            new GenericObjectPoolConfig<>();
        
        poolConfig.setMaxTotal(properties.getLettuce().getPool().getMaxActive());
        poolConfig.setMaxIdle(properties.getLettuce().getPool().getMaxIdle());
        poolConfig.setMinIdle(properties.getLettuce().getPool().getMinIdle());
        poolConfig.setMaxWait(properties.getLettuce().getPool().getMaxWait());
        
        // Configurações de validação
        poolConfig.setTestOnBorrow(true);
        poolConfig.setTestOnReturn(true);
        poolConfig.setTestWhileIdle(true);
        
        // === CONFIGURAÇÃO DO LETTUCE ===
        LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
            .commandTimeout(properties.getTimeout())
            .shutdownTimeout(Duration.ofSeconds(10))
            .poolConfig(poolConfig)
            .build();
        
        LettuceConnectionFactory factory = new LettuceConnectionFactory(config, clientConfig);
        factory.setValidateConnection(true);
        factory.setShareNativeConnection(false);
        
        return factory;
    }
    
    @Bean
    public RedisTemplate<String, Object> redisTemplate(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        
        log.info("Configurando RedisTemplate");
        
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(connectionFactory);
        
        // === CONFIGURAÇÃO DE SERIALIZERS ===
        // Key Serializer
        StringRedisSerializer stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        
        // Value Serializer - JSON
        GenericJackson2JsonRedisSerializer jsonSerializer = 
            new GenericJackson2JsonRedisSerializer(createObjectMapper());
        template.setValueSerializer(jsonSerializer);
        template.setHashValueSerializer(jsonSerializer);
        
        // Default Serializer
        template.setDefaultSerializer(jsonSerializer);
        
        template.afterPropertiesSet();
        
        return template;
    }
    
    @Bean
    public StringRedisTemplate stringRedisTemplate(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        
        StringRedisTemplate template = new StringRedisTemplate();
        template.setConnectionFactory(connectionFactory);
        
        return template;
    }
    
    @Bean
    public CacheManager cacheManager(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        
        log.info("Configurando Cache Manager");
        
        // === CONFIGURAÇÃO PADRÃO ===
        RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
            .entryTtl(Duration.ofSeconds(properties.getCache().getDefaultTtl()))
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer(createObjectMapper())))
            .computePrefixWith(cacheName -> properties.getCache().getKeyPrefix() + cacheName + ":")
            .disableCachingNullValues();
        
        // === CONFIGURAÇÕES ESPECÍFICAS POR CACHE ===
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache de consultas de sinistros (TTL curto)
        cacheConfigurations.put("sinistros", defaultConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("sinistros:"));
        
        // Cache de dados de segurados (TTL médio)
        cacheConfigurations.put("segurados", defaultConfig
            .entryTtl(Duration.ofMinutes(15))
            .prefixCacheNameWith("segurados:"));
        
        // Cache de configurações (TTL longo)
        cacheConfigurations.put("configuracoes", defaultConfig
            .entryTtl(Duration.ofHours(1))
            .prefixCacheNameWith("config:"));
        
        // Cache de metadados (TTL muito longo)
        cacheConfigurations.put("metadados", defaultConfig
            .entryTtl(Duration.ofHours(6))
            .prefixCacheNameWith("meta:"));
        
        // Cache de sessões (TTL customizado)
        cacheConfigurations.put("sessoes", defaultConfig
            .entryTtl(Duration.ofMinutes(30))
            .prefixCacheNameWith("session:"));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(defaultConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
    
    @Bean
    public CacheErrorHandler cacheErrorHandler() {
        return new CacheErrorHandler() {
            @Override
            public void handleCacheGetError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Erro ao buscar no cache '{}' com key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Não propagar erro - aplicação deve funcionar sem cache
            }
            
            @Override
            public void handleCachePutError(RuntimeException exception, Cache cache, Object key, Object value) {
                log.warn("Erro ao salvar no cache '{}' com key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Não propagar erro
            }
            
            @Override
            public void handleCacheEvictError(RuntimeException exception, Cache cache, Object key) {
                log.warn("Erro ao remover do cache '{}' com key '{}': {}", 
                    cache.getName(), key, exception.getMessage());
                // Não propagar erro
            }
            
            @Override
            public void handleCacheClearError(RuntimeException exception, Cache cache) {
                log.warn("Erro ao limpar cache '{}': {}", cache.getName(), exception.getMessage());
                // Não propagar erro
            }
        };
    }
    
    @Bean
    public HealthIndicator redisHealthIndicator(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new RedisHealthIndicator(connectionFactory);
    }
    
    @Bean
    public MeterBinder redisMetrics(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        return new LettuceMetrics(connectionFactory);
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
    
    @PostConstruct
    public void logConfiguration() {
        log.info("=== REDIS CONFIGURADO ===");
        log.info("Host: {}:{}", properties.getHost(), properties.getPort());
        log.info("Database: {}", properties.getDatabase());
        log.info("Pool Max Active: {}", properties.getLettuce().getPool().getMaxActive());
        log.info("Default TTL: {}s", properties.getCache().getDefaultTtl());
        log.info("Key Prefix: {}", properties.getCache().getKeyPrefix());
        log.info("==========================");
    }
}
```

### **⚙️ RedisProperties.java**

```java
@ConfigurationProperties(prefix = "spring.redis")
@Data
@Validated
public class RedisProperties {
    
    @NotBlank
    private String host = "localhost";
    
    @Min(1)
    @Max(65535)
    private int port = 6379;
    
    private String password;
    
    @Min(0)
    @Max(15)
    private int database = 0;
    
    private Duration timeout = Duration.ofSeconds(2);
    
    @Valid
    private Lettuce lettuce = new Lettuce();
    
    @Valid
    private Cache cache = new Cache();
    
    @Data
    public static class Lettuce {
        @Valid
        private Pool pool = new Pool();
        
        @Data
        public static class Pool {
            @Min(1)
            @Max(100)
            private int maxActive = 20;
            
            @Min(0)
            @Max(50)
            private int maxIdle = 10;
            
            @Min(0)
            @Max(20)
            private int minIdle = 5;
            
            private Duration maxWait = Duration.ofSeconds(2);
        }
    }
    
    @Data
    public static class Cache {
        @Min(60)
        @Max(86400)
        private int defaultTtl = 900; // 15 minutos
        
        @NotBlank
        private String keyPrefix = "hibrida:";
        
        private boolean enableStatistics = true;
        private boolean enableNullValues = false;
    }
}
```

---

## 📨 **CONFIGURAÇÃO DE MESSAGE BROKER**

### **🔧 Kafka Configuration**

#### **KafkaConfiguration.java:**
```java
@Configuration
@EnableKafka
@EnableConfigurationProperties(KafkaProperties.class)
@ConditionalOnProperty(name = "app.event-bus.type", havingValue = "kafka")
@Slf4j
public class KafkaConfiguration {
    
    private final KafkaProperties properties;
    
    public KafkaConfiguration(KafkaProperties properties) {
        this.properties = properties;
    }
    
    @Bean
    public ProducerFactory<String, String> producerFactory() {
        log.info("Configurando Kafka Producer Factory");
        
        Map<String, Object> configProps = new HashMap<>();
        
        // === CONFIGURAÇÕES BÁSICAS ===
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            properties.getBootstrapServers());
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, 
            StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, 
            StringSerializer.class);
        
        // === CONFIGURAÇÕES DE CONFIABILIDADE ===
        configProps.put(ProducerConfig.ACKS_CONFIG, properties.getProducer().getAcks());
        configProps.put(ProducerConfig.RETRIES_CONFIG, properties.getProducer().getRetries());
        configProps.put(ProducerConfig.ENABLE_IDEMPOTENCE_CONFIG, true);
        
        // === CONFIGURAÇÕES DE PERFORMANCE ===
        configProps.put(ProducerConfig.BATCH_SIZE_CONFIG, 
            properties.getProducer().getBatchSize());
        configProps.put(ProducerConfig.LINGER_MS_CONFIG, 
            properties.getProducer().getLingerMs());
        configProps.put(ProducerConfig.BUFFER_MEMORY_CONFIG, 
            properties.getProducer().getBufferMemory());
        configProps.put(ProducerConfig.COMPRESSION_TYPE_CONFIG, "snappy");
        
        // === CONFIGURAÇÕES DE TIMEOUT ===
        configProps.put(ProducerConfig.REQUEST_TIMEOUT_MS_CONFIG, 30000);
        configProps.put(ProducerConfig.DELIVERY_TIMEOUT_MS_CONFIG, 120000);
        
        // === CONFIGURAÇÕES DE MONITORAMENTO ===
        configProps.put(ProducerConfig.CLIENT_ID_CONFIG, "hibrida-producer");
        configProps.put(ProducerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 30000);
        
        return new DefaultKafkaProducerFactory<>(configProps);
    }
    
    @Bean
    public KafkaTemplate<String, String> kafkaTemplate(
            ProducerFactory<String, String> producerFactory) {
        
        KafkaTemplate<String, String> template = new KafkaTemplate<>(producerFactory);
        
        // === CONFIGURAÇÕES DO TEMPLATE ===
        template.setDefaultTopic(properties.getDefaultTopic());
        template.setProducerInterceptors(Arrays.asList(new ProducerInterceptor<String, String>() {
            @Override
            public ProducerRecord<String, String> onSend(ProducerRecord<String, String> record) {
                // Adicionar headers de rastreamento
                record.headers().add("timestamp", Instant.now().toString().getBytes());
                record.headers().add("source", "hibrida-app".getBytes());
                return record;
            }
            
            @Override
            public void onAcknowledgement(RecordMetadata metadata, Exception exception) {
                if (exception != null) {
                    log.error("Erro ao enviar mensagem para tópico {}: {}", 
                        metadata.topic(), exception.getMessage());
                }
            }
            
            @Override
            public void close() {
                // Cleanup se necessário
            }
            
            @Override
            public void configure(Map<String, ?> configs) {
                // Configuração adicional se necessário
            }
        }));
        
        return template;
    }
    
    @Bean
    public ConsumerFactory<String, String> consumerFactory() {
        log.info("Configurando Kafka Consumer Factory");
        
        Map<String, Object> configProps = new HashMap<>();
        
        // === CONFIGURAÇÕES BÁSICAS ===
        configProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, 
            properties.getBootstrapServers());
        configProps.put(ConsumerConfig.GROUP_ID_CONFIG, 
            properties.getConsumer().getGroupId());
        configProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, 
            StringDeserializer.class);
        configProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, 
            StringDeserializer.class);
        
        // === CONFIGURAÇÕES DE OFFSET ===
        configProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, 
            properties.getConsumer().getAutoOffsetReset());
        configProps.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, 
            properties.getConsumer().isEnableAutoCommit());
        
        if (!properties.getConsumer().isEnableAutoCommit()) {
            configProps.put(ConsumerConfig.AUTO_COMMIT_INTERVAL_MS_CONFIG, 5000);
        }
        
        // === CONFIGURAÇÕES DE PERFORMANCE ===
        configProps.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 
            properties.getConsumer().getMaxPollRecords());
        configProps.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 1024);
        configProps.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500);
        
        // === CONFIGURAÇÕES DE SESSION ===
        configProps.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 
            properties.getConsumer().getSessionTimeoutMs());
        configProps.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000);
        
        // === CONFIGURAÇÕES DE MONITORAMENTO ===
        configProps.put(ConsumerConfig.CLIENT_ID_CONFIG, "hibrida-consumer");
        configProps.put(ConsumerConfig.METRICS_SAMPLE_WINDOW_MS_CONFIG, 30000);
        
        return new DefaultKafkaConsumerFactory<>(configProps);
    }
    
    @Bean
    public KafkaListenerContainerFactory<ConcurrentMessageListenerContainer<String, String>>
            kafkaListenerContainerFactory(ConsumerFactory<String, String> consumerFactory) {
        
        ConcurrentKafkaListenerContainerFactory<String, String> factory = 
            new ConcurrentKafkaListenerContainerFactory<>();
        
        factory.setConsumerFactory(consumerFactory);
        
        // === CONFIGURAÇÕES DE CONCORRÊNCIA ===
        factory.setConcurrency(properties.getConsumer().getConcurrency());
        factory.getContainerProperties().setPollTimeout(3000);
        
        // === CONFIGURAÇÕES DE ERROR HANDLING ===
        factory.setCommonErrorHandler(new DefaultErrorHandler(
            new FixedBackOff(1000L, 3L))); // 3 tentativas com 1s de intervalo
        
        // === CONFIGURAÇÕES DE ACK ===
        factory.getContainerProperties().setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);
        
        // === CONFIGURAÇÕES DE MONITORAMENTO ===
        factory.getContainerProperties().setMonitorInterval(Duration.ofSeconds(30));
        
        return factory;
    }
    
    @Bean
    public KafkaAdmin kafkaAdmin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, properties.getBootstrapServers());
        configs.put(AdminClientConfig.CLIENT_ID_CONFIG, "hibrida-admin");
        
        return new KafkaAdmin(configs);
    }
    
    @Bean
    public NewTopic domainEventsTopic() {
        return TopicBuilder.name(properties.getTopics().getDomainEvents())
            .partitions(properties.getPartitions())
            .replicas(properties.getReplicationFactor())
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
            .config(TopicConfig.RETENTION_MS_CONFIG, "604800000") // 7 dias
            .build();
    }
    
    @Bean
    public NewTopic integrationEventsTopic() {
        return TopicBuilder.name(properties.getTopics().getIntegrationEvents())
            .partitions(properties.getPartitions())
            .replicas(properties.getReplicationFactor())
            .config(TopicConfig.COMPRESSION_TYPE_CONFIG, "snappy")
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 dias
            .build();
    }
    
    @Bean
    public NewTopic deadLetterTopic() {
        return TopicBuilder.name(properties.getTopics().getDeadLetter())
            .partitions(1) // DLQ não precisa de múltiplas partições
            .replicas(properties.getReplicationFactor())
            .config(TopicConfig.RETENTION_MS_CONFIG, "2592000000") // 30 dias
            .build();
    }
    
    private ObjectMapper createObjectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        mapper.setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE);
        mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        return mapper;
    }
    
    @PostConstruct
    public void logConfiguration() {
        log.info("=== KAFKA CONFIGURADO ===");
        log.info("Bootstrap Servers: {}", properties.getBootstrapServers());
        log.info("Default Topic: {}", properties.getDefaultTopic());
        log.info("Consumer Group: {}", properties.getConsumer().getGroupId());
        log.info("Partitions: {}", properties.getPartitions());
        log.info("Replication Factor: {}", properties.getReplicationFactor());
        log.info("==========================");
    }
}
```

### **📋 Configuração YAML para Cache e Kafka**

#### **application.yml - Cache e Messaging:**
```yaml
# === CONFIGURAÇÕES DE CACHE (REDIS) ===
spring:
  redis:
    host: ${REDIS_HOST:localhost}
    port: ${REDIS_PORT:6379}
    password: ${REDIS_PASSWORD:}
    database: ${REDIS_DATABASE:0}
    timeout: ${REDIS_TIMEOUT:2000ms}
    
    lettuce:
      pool:
        max-active: ${REDIS_POOL_MAX_ACTIVE:20}
        max-idle: ${REDIS_POOL_MAX_IDLE:10}
        min-idle: ${REDIS_POOL_MIN_IDLE:5}
        max-wait: ${REDIS_POOL_MAX_WAIT:2000ms}
      
      shutdown-timeout: ${REDIS_SHUTDOWN_TIMEOUT:100ms}
    
    cache:
      default-ttl: ${REDIS_CACHE_TTL:900}  # 15 minutos
      key-prefix: ${REDIS_KEY_PREFIX:hibrida:}
      enable-statistics: ${REDIS_CACHE_STATS:true}

  # === CONFIGURAÇÕES DE CACHE SPRING ===
  cache:
    type: redis
    redis:
      time-to-live: ${CACHE_TTL:900000}  # 15 minutos em ms
      key-prefix: ${CACHE_KEY_PREFIX:hibrida:}
      cache-null-values: false
      use-key-prefix: true

# === CONFIGURAÇÕES DE KAFKA ===
spring:
  kafka:
    bootstrap-servers: ${KAFKA_BOOTSTRAP_SERVERS:localhost:9092}
    
    # === PRODUCER ===
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer
      acks: ${KAFKA_PRODUCER_ACKS:all}
      retries: ${KAFKA_PRODUCER_RETRIES:3}
      batch-size: ${KAFKA_PRODUCER_BATCH_SIZE:16384}
      linger-ms: ${KAFKA_PRODUCER_LINGER:5}
      buffer-memory: ${KAFKA_PRODUCER_BUFFER:33554432}
      
      properties:
        enable.idempotence: true
        max.in.flight.requests.per.connection: 5
        compression.type: snappy
        request.timeout.ms: 30000
        delivery.timeout.ms: 120000
    
    # === CONSUMER ===
    consumer:
      group-id: ${KAFKA_CONSUMER_GROUP:hibrida-consumers}
      auto-offset-reset: ${KAFKA_CONSUMER_OFFSET_RESET:earliest}
      enable-auto-commit: ${KAFKA_CONSUMER_AUTO_COMMIT:false}
      max-poll-records: ${KAFKA_CONSUMER_MAX_POLL:500}
      session-timeout-ms: ${KAFKA_CONSUMER_SESSION_TIMEOUT:30000}
      concurrency: ${KAFKA_CONSUMER_CONCURRENCY:3}
      
      properties:
        fetch.min.bytes: 1024
        fetch.max.wait.ms: 500
        heartbeat.interval.ms: 3000
        max.partition.fetch.bytes: 1048576
    
    # === ADMIN ===
    admin:
      properties:
        request.timeout.ms: 30000

# === CONFIGURAÇÕES ESPECÍFICAS DA APLICAÇÃO ===
app:
  event-bus:
    type: ${EVENT_BUS_TYPE:kafka}  # kafka ou simple
    enabled: ${EVENT_BUS_ENABLED:true}
    
    # === CONFIGURAÇÕES KAFKA ===
    kafka:
      default-topic: ${KAFKA_DEFAULT_TOPIC:domain-events}
      partitions: ${KAFKA_PARTITIONS:3}
      replication-factor: ${KAFKA_REPLICATION_FACTOR:1}
      
      topics:
        domain-events: ${KAFKA_TOPIC_DOMAIN:domain-events}
        integration-events: ${KAFKA_TOPIC_INTEGRATION:integration-events}
        dead-letter: ${KAFKA_TOPIC_DLQ:dead-letter-queue}
      
      # Configurações de retry
      retry:
        max-attempts: ${KAFKA_RETRY_MAX:3}
        initial-delay-ms: ${KAFKA_RETRY_INITIAL:1000}
        max-delay-ms: ${KAFKA_RETRY_MAX_DELAY:30000}
        backoff-multiplier: ${KAFKA_RETRY_BACKOFF:2.0}
        jitter-percent: ${KAFKA_RETRY_JITTER:0.1}
      
      # Configurações de timeout
      timeout:
        producer: ${KAFKA_PRODUCER_TIMEOUT:30s}
        consumer: ${KAFKA_CONSUMER_TIMEOUT:30s}
        admin: ${KAFKA_ADMIN_TIMEOUT:30s}
    
    # === CONFIGURAÇÕES SIMPLE EVENT BUS ===
    simple:
      thread-pool:
        core-size: ${SIMPLE_EVENTBUS_CORE:5}
        max-size: ${SIMPLE_EVENTBUS_MAX:20}
        queue-capacity: ${SIMPLE_EVENTBUS_QUEUE:500}
        keep-alive-seconds: ${SIMPLE_EVENTBUS_KEEP_ALIVE:60}
        thread-name-prefix: "simple-eventbus-"
      
      retry:
        max-attempts: ${SIMPLE_EVENTBUS_RETRY_MAX:3}
        initial-delay-ms: ${SIMPLE_EVENTBUS_RETRY_INITIAL:1000}
        max-delay-ms: ${SIMPLE_EVENTBUS_RETRY_MAX_DELAY:10000}
        backoff-multiplier: ${SIMPLE_EVENTBUS_RETRY_BACKOFF:2.0}
    
    # === CONFIGURAÇÕES DE MONITORAMENTO ===
    monitoring:
      enabled: ${EVENT_BUS_MONITORING:true}
      metrics-interval-seconds: ${EVENT_BUS_METRICS_INTERVAL:30}
      detailed-logging: ${EVENT_BUS_DETAILED_LOGGING:false}
      error-rate-threshold: ${EVENT_BUS_ERROR_THRESHOLD:0.05}
```

---

## 🔧 **CONFIGURAÇÕES AVANÇADAS**

### **📊 Cache Customizado para Consultas**

#### **QueryCacheConfiguration.java:**
```java
@Configuration
@EnableCaching
@Slf4j
public class QueryCacheConfiguration {
    
    @Bean
    @Primary
    public CacheManager queryCacheManager(
            @Qualifier("redisConnectionFactory") LettuceConnectionFactory connectionFactory) {
        
        log.info("Configurando Query Cache Manager");
        
        // === CONFIGURAÇÃO BASE ===
        RedisCacheConfiguration baseConfig = RedisCacheConfiguration.defaultCacheConfig()
            .serializeKeysWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(RedisSerializationContext.SerializationPair
                .fromSerializer(new GenericJackson2JsonRedisSerializer()))
            .disableCachingNullValues()
            .computePrefixWith(cacheName -> "query:" + cacheName + ":");
        
        // === CONFIGURAÇÕES ESPECÍFICAS POR TIPO DE CONSULTA ===
        Map<String, RedisCacheConfiguration> cacheConfigurations = new HashMap<>();
        
        // Cache para consultas de sinistros por ID (TTL curto - dados mudam frequentemente)
        cacheConfigurations.put("sinistro-by-id", baseConfig
            .entryTtl(Duration.ofMinutes(2))
            .prefixCacheNameWith("sinistro:id:"));
        
        // Cache para consultas de sinistros por CPF (TTL médio)
        cacheConfigurations.put("sinistros-by-cpf", baseConfig
            .entryTtl(Duration.ofMinutes(5))
            .prefixCacheNameWith("sinistro:cpf:"));
        
        // Cache para consultas de dashboard (TTL longo - dados agregados)
        cacheConfigurations.put("dashboard", baseConfig
            .entryTtl(Duration.ofMinutes(15))
            .prefixCacheNameWith("dashboard:"));
        
        // Cache para consultas de relatórios (TTL muito longo)
        cacheConfigurations.put("relatorios", baseConfig
            .entryTtl(Duration.ofHours(1))
            .prefixCacheNameWith("report:"));
        
        // Cache para metadados (TTL longo - raramente mudam)
        cacheConfigurations.put("metadados", baseConfig
            .entryTtl(Duration.ofHours(6))
            .prefixCacheNameWith("meta:"));
        
        return RedisCacheManager.builder(connectionFactory)
            .cacheDefaults(baseConfig)
            .withInitialCacheConfigurations(cacheConfigurations)
            .transactionAware()
            .build();
    }
    
    @Bean
    public CacheResolver customCacheResolver(CacheManager cacheManager) {
        return new SimpleCacheResolver(cacheManager);
    }
    
    @Bean
    public KeyGenerator customKeyGenerator() {
        return new KeyGenerator() {
            @Override
            public Object generate(Object target, Method method, Object... params) {
                StringBuilder key = new StringBuilder();
                key.append(target.getClass().getSimpleName());
                key.append(":");
                key.append(method.getName());
                
                for (Object param : params) {
                    key.append(":");
                    if (param != null) {
                        key.append(param.toString());
                    } else {
                        key.append("null");
                    }
                }
                
                return key.toString();
            }
        };
    }
    
    @EventListener
    public void handleCacheEvent(CacheEvent event) {
        log.debug("Cache event: {} - Cache: {} - Key: {}", 
            event.getType(), event.getCacheName(), event.getKey());
    }
}
```

### **🎯 Event Bus Configuration Selector**

#### **EventBusConfigurationSelector.java:**
```java
@Configuration
@Slf4j
public class EventBusConfigurationSelector {
    
    @Bean
    @ConditionalOnProperty(name = "app.event-bus.type", havingValue = "simple", matchIfMissing = true)
    public EventBus simpleEventBus(EventHandlerRegistry handlerRegistry) {
        log.info("Configurando Simple Event Bus");
        return new SimpleEventBus(handlerRegistry);
    }
    
    @Bean
    @ConditionalOnProperty(name = "app.event-bus.type", havingValue = "kafka")
    public EventBus kafkaEventBus(
            EventHandlerRegistry handlerRegistry,
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper) {
        
        log.info("Configurando Kafka Event Bus");
        return new KafkaEventBus(handlerRegistry, kafkaTemplate, objectMapper);
    }
    
    @Bean
    public EventBusHealthIndicator eventBusHealthIndicator(EventBus eventBus) {
        return new EventBusHealthIndicator(eventBus);
    }
    
    @Bean
    public EventBusMetrics eventBusMetrics(EventBus eventBus, MeterRegistry meterRegistry) {
        return new EventBusMetrics(eventBus, meterRegistry);
    }
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Spring Data Redis](https://docs.spring.io/spring-data/redis/docs/current/reference/html/)
- [Spring for Apache Kafka](https://docs.spring.io/spring-kafka/docs/current/reference/html/)
- [Redis Best Practices](https://redis.io/docs/manual/clients-guide/)
- [Kafka Configuration](https://kafka.apache.org/documentation/#configuration)

### **📖 Próximas Partes:**
- **Parte 4**: Health Checks e Monitoramento
- **Parte 5**: Configurações Avançadas e Troubleshooting

---

**📝 Parte 3 de 5 - Cache e Message Broker**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 4 - Health Checks e Monitoramento](./09-configuracoes-parte-4.md)