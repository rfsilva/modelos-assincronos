# 🏗️ AGREGADOS - PARTE 3: SNAPSHOTS E OTIMIZAÇÃO
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Compreender e implementar estratégias de snapshot, otimizações de performance e técnicas avançadas para agregados com muitos eventos.

---

## 📸 **CONCEITOS DE SNAPSHOT**

### **📋 Por que Usar Snapshots?**

**Problema sem Snapshot:**
```
Agregado com 10.000 eventos
├── Carregar = Replay de 10.000 eventos
├── Tempo = ~5-10 segundos
├── Memória = Alta utilização
└── Performance = Inaceitável
```

**Solução com Snapshot:**
```
Snapshot na versão 9.500 + 500 eventos incrementais
├── Carregar = Snapshot + 500 eventos
├── Tempo = ~100-200ms
├── Memória = Baixa utilização
└── Performance = Excelente
```

### **🎯 Estratégias de Snapshot**

**Tipos de Snapshot:**
1. **Periódico**: A cada N eventos (ex: 100)
2. **Temporal**: A cada período (ex: diário)
3. **Manual**: Sob demanda
4. **Inteligente**: Baseado em métricas

---

## 🗃️ **SNAPSHOT STORE**

### **📋 Interface SnapshotStore**

**Localização**: `com.seguradora.hibrida.snapshot.SnapshotStore`

```java
public interface SnapshotStore {
    
    /**
     * Salva um snapshot do agregado.
     */
    void saveSnapshot(String aggregateId, long version, Object snapshotData);
    
    /**
     * Carrega o snapshot mais recente de um agregado.
     */
    Optional<Object> loadSnapshot(String aggregateId);
    
    /**
     * Carrega snapshot de uma versão específica.
     */
    Optional<Object> loadSnapshot(String aggregateId, long version);
    
    /**
     * Remove snapshot de um agregado.
     */
    boolean deleteSnapshot(String aggregateId);
    
    /**
     * Remove snapshots antigos baseado em política de retenção.
     */
    int cleanupOldSnapshots(Duration retentionPeriod);
    
    /**
     * Obtém informações sobre snapshots de um agregado.
     */
    List<SnapshotMetadata> getSnapshotMetadata(String aggregateId);
    
    /**
     * Verifica se existe snapshot para um agregado.
     */
    boolean hasSnapshot(String aggregateId);
    
    /**
     * Obtém estatísticas do snapshot store.
     */
    SnapshotStatistics getStatistics();
}
```

### **🎯 Implementação PostgreSQL**

```java
@Repository
public class PostgreSQLSnapshotStore implements SnapshotStore {
    
    private static final Logger log = LoggerFactory.getLogger(PostgreSQLSnapshotStore.class);
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final SnapshotMetrics metrics;
    
    public PostgreSQLSnapshotStore(JdbcTemplate jdbcTemplate,
                                 ObjectMapper objectMapper,
                                 SnapshotMetrics metrics) {
        this.jdbcTemplate = jdbcTemplate;
        this.objectMapper = objectMapper;
        this.metrics = metrics;
    }
    
    @Override
    public void saveSnapshot(String aggregateId, long version, Object snapshotData) {
        Timer.Sample sample = metrics.startCreationTimer();
        
        try {
            String serializedData = serializeSnapshot(snapshotData);
            byte[] compressedData = compressData(serializedData);
            
            String sql = """
                INSERT INTO snapshots (aggregate_id, version, snapshot_data, 
                                     data_size, compressed_size, created_at)
                VALUES (?, ?, ?, ?, ?, ?)
                ON CONFLICT (aggregate_id) 
                DO UPDATE SET 
                    version = EXCLUDED.version,
                    snapshot_data = EXCLUDED.snapshot_data,
                    data_size = EXCLUDED.data_size,
                    compressed_size = EXCLUDED.compressed_size,
                    created_at = EXCLUDED.created_at
                """;
            
            int originalSize = serializedData.getBytes(StandardCharsets.UTF_8).length;
            
            jdbcTemplate.update(sql,
                aggregateId,
                version,
                compressedData,
                originalSize,
                compressedData.length,
                Instant.now()
            );
            
            // Atualizar métricas
            metrics.updateStorageMetrics(compressedData.length, originalSize - compressedData.length);
            
            log.info("Snapshot salvo: {} versão {} (compressão: {:.1f}%)", 
                aggregateId, version, 
                (1.0 - (double) compressedData.length / originalSize) * 100);
                
        } catch (Exception e) {
            metrics.incrementFailures();
            log.error("Erro ao salvar snapshot: {} versão {}", aggregateId, version, e);
            throw new SnapshotException("Erro ao salvar snapshot", e);
            
        } finally {
            sample.stop(metrics.getCreationTimer());
        }
    }
    
    @Override
    public Optional<Object> loadSnapshot(String aggregateId) {
        Timer.Sample sample = metrics.startLoadTimer();
        
        try {
            String sql = """
                SELECT snapshot_data, version, created_at
                FROM snapshots 
                WHERE aggregate_id = ?
                ORDER BY version DESC 
                LIMIT 1
                """;
            
            List<Map<String, Object>> results = jdbcTemplate.queryForList(sql, aggregateId);
            
            if (results.isEmpty()) {
                log.debug("Nenhum snapshot encontrado para agregado: {}", aggregateId);
                return Optional.empty();
            }
            
            Map<String, Object> row = results.get(0);
            byte[] compressedData = (byte[]) row.get("snapshot_data");
            long version = ((Number) row.get("version")).longValue();
            
            String decompressedData = decompressData(compressedData);
            Object snapshotData = deserializeSnapshot(decompressedData);
            
            log.debug("Snapshot carregado: {} versão {} (tamanho: {} bytes)", 
                aggregateId, version, compressedData.length);
            
            return Optional.of(snapshotData);
            
        } catch (Exception e) {
            log.error("Erro ao carregar snapshot: {}", aggregateId, e);
            return Optional.empty();
            
        } finally {
            sample.stop(metrics.getLoadTimer());
        }
    }
    
    @Override
    public Optional<Object> loadSnapshot(String aggregateId, long version) {
        try {
            String sql = """
                SELECT snapshot_data
                FROM snapshots 
                WHERE aggregate_id = ? AND version = ?
                """;
            
            List<byte[]> results = jdbcTemplate.queryForList(sql, byte[].class, aggregateId, version);
            
            if (results.isEmpty()) {
                return Optional.empty();
            }
            
            byte[] compressedData = results.get(0);
            String decompressedData = decompressData(compressedData);
            Object snapshotData = deserializeSnapshot(decompressedData);
            
            return Optional.of(snapshotData);
            
        } catch (Exception e) {
            log.error("Erro ao carregar snapshot: {} versão {}", aggregateId, version, e);
            return Optional.empty();
        }
    }
    
    @Override
    public boolean deleteSnapshot(String aggregateId) {
        try {
            String sql = "DELETE FROM snapshots WHERE aggregate_id = ?";
            int deleted = jdbcTemplate.update(sql, aggregateId);
            
            if (deleted > 0) {
                log.info("Snapshot deletado: {}", aggregateId);
                metrics.incrementDeleted(deleted);
            }
            
            return deleted > 0;
            
        } catch (Exception e) {
            log.error("Erro ao deletar snapshot: {}", aggregateId, e);
            return false;
        }
    }
    
    @Override
    public int cleanupOldSnapshots(Duration retentionPeriod) {
        try {
            Instant cutoffTime = Instant.now().minus(retentionPeriod);
            
            String sql = "DELETE FROM snapshots WHERE created_at < ?";
            int deleted = jdbcTemplate.update(sql, cutoffTime);
            
            if (deleted > 0) {
                log.info("Limpeza de snapshots: {} snapshots antigos removidos", deleted);
                metrics.incrementDeleted(deleted);
            }
            
            return deleted;
            
        } catch (Exception e) {
            log.error("Erro na limpeza de snapshots antigos", e);
            return 0;
        }
    }
    
    @Override
    public List<SnapshotMetadata> getSnapshotMetadata(String aggregateId) {
        try {
            String sql = """
                SELECT aggregate_id, version, data_size, compressed_size, created_at
                FROM snapshots 
                WHERE aggregate_id = ?
                ORDER BY version DESC
                """;
            
            return jdbcTemplate.query(sql, (rs, rowNum) -> {
                SnapshotMetadata metadata = new SnapshotMetadata();
                metadata.setAggregateId(rs.getString("aggregate_id"));
                metadata.setVersion(rs.getLong("version"));
                metadata.setDataSize(rs.getInt("data_size"));
                metadata.setCompressedSize(rs.getInt("compressed_size"));
                metadata.setCreatedAt(rs.getTimestamp("created_at").toInstant());
                return metadata;
            }, aggregateId);
            
        } catch (Exception e) {
            log.error("Erro ao obter metadados de snapshot: {}", aggregateId, e);
            return Collections.emptyList();
        }
    }
    
    @Override
    public boolean hasSnapshot(String aggregateId) {
        try {
            String sql = "SELECT COUNT(*) FROM snapshots WHERE aggregate_id = ?";
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, aggregateId);
            return count != null && count > 0;
            
        } catch (Exception e) {
            log.error("Erro ao verificar existência de snapshot: {}", aggregateId, e);
            return false;
        }
    }
    
    @Override
    public SnapshotStatistics getStatistics() {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_snapshots,
                    COUNT(DISTINCT aggregate_id) as total_aggregates,
                    AVG(data_size) as avg_size,
                    AVG(compressed_size) as avg_compressed_size,
                    SUM(data_size) as total_storage,
                    SUM(compressed_size) as total_compressed_storage
                FROM snapshots
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> {
                SnapshotStatistics stats = new SnapshotStatistics();
                stats.setTotalSnapshots(rs.getLong("total_snapshots"));
                stats.setTotalAggregates(rs.getLong("total_aggregates"));
                stats.setAverageSize(rs.getDouble("avg_size"));
                stats.setAverageCompressedSize(rs.getDouble("avg_compressed_size"));
                stats.setTotalStorageUsed(rs.getLong("total_storage"));
                stats.setTotalCompressedStorage(rs.getLong("total_compressed_storage"));
                return stats;
            });
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas de snapshot", e);
            return new SnapshotStatistics();
        }
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private String serializeSnapshot(Object snapshotData) throws Exception {
        return objectMapper.writeValueAsString(snapshotData);
    }
    
    private Object deserializeSnapshot(String serializedData) throws Exception {
        return objectMapper.readValue(serializedData, Map.class);
    }
    
    private byte[] compressData(String data) throws Exception {
        Timer.Sample sample = metrics.startCompressionTimer();
        
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            
            try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
                gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
            }
            
            return baos.toByteArray();
            
        } finally {
            sample.stop(metrics.getCompressionTimer());
        }
    }
    
    private String decompressData(byte[] compressedData) throws Exception {
        ByteArrayInputStream bais = new ByteArrayInputStream(compressedData);
        
        try (GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            
            return baos.toString(StandardCharsets.UTF_8);
        }
    }
}
```

---

## ⚙️ **ESTRATÉGIAS DE OTIMIZAÇÃO**

### **📊 Políticas de Snapshot**

```java
@Component
public class SnapshotPolicy {
    
    private final SnapshotProperties properties;
    
    /**
     * Determina se deve criar snapshot baseado em configuração.
     */
    public boolean shouldCreateSnapshot(AggregateRoot aggregate) {
        long version = aggregate.getVersion();
        
        // Política por frequência de eventos
        if (properties.getEventFrequency() > 0 && 
            version % properties.getEventFrequency() == 0) {
            return true;
        }
        
        // Política temporal
        if (properties.getTimeInterval() != null) {
            return shouldCreateByTime(aggregate);
        }
        
        // Política por tamanho
        if (properties.getMaxEventsWithoutSnapshot() > 0) {
            return shouldCreateByEventCount(aggregate);
        }
        
        return false;
    }
    
    private boolean shouldCreateByTime(AggregateRoot aggregate) {
        // Verificar se passou tempo suficiente desde último snapshot
        // Implementação específica baseada em timestamp
        return true; // Placeholder
    }
    
    private boolean shouldCreateByEventCount(AggregateRoot aggregate) {
        // Verificar quantos eventos foram adicionados desde último snapshot
        // Implementação específica baseada em contagem
        return true; // Placeholder
    }
    
    /**
     * Determina se deve limpar snapshots antigos.
     */
    public boolean shouldCleanupSnapshots() {
        return properties.isAutoCleanup() && 
               properties.getRetentionPeriod() != null;
    }
}
```

### **🚀 Otimizações de Performance**

```java
@Component
public class AggregateOptimizer {
    
    private final SnapshotStore snapshotStore;
    private final EventStore eventStore;
    private final SnapshotPolicy snapshotPolicy;
    
    /**
     * Otimiza carregamento de agregado usando estratégias inteligentes.
     */
    public <T extends AggregateRoot> Optional<T> optimizedLoad(String aggregateId, 
                                                             Class<T> aggregateType) {
        
        // 1. Tentar carregar via snapshot
        Optional<T> fromSnapshot = loadFromSnapshot(aggregateId, aggregateType);
        if (fromSnapshot.isPresent()) {
            return fromSnapshot;
        }
        
        // 2. Verificar se vale a pena criar snapshot durante carregamento
        List<DomainEvent> events = eventStore.loadEvents(aggregateId);
        
        if (events.size() > 100) { // Threshold configurável
            // Carregar, criar snapshot e retornar
            T aggregate = reconstructFromEvents(events, aggregateType);
            createSnapshotAsync(aggregate);
            return Optional.of(aggregate);
        }
        
        // 3. Carregamento normal para poucos eventos
        return Optional.of(reconstructFromEvents(events, aggregateType));
    }
    
    /**
     * Cria snapshot de forma assíncrona.
     */
    @Async
    public void createSnapshotAsync(AggregateRoot aggregate) {
        try {
            if (snapshotPolicy.shouldCreateSnapshot(aggregate)) {
                Object snapshotData = aggregate.createSnapshot();
                snapshotStore.saveSnapshot(
                    aggregate.getAggregateId(),
                    aggregate.getVersion(),
                    snapshotData
                );
            }
        } catch (Exception e) {
            log.warn("Erro ao criar snapshot assíncrono: {}", 
                aggregate.getAggregateId(), e);
        }
    }
    
    /**
     * Otimiza agregados com muitos eventos criando snapshots.
     */
    @Scheduled(fixedRate = 3600000) // A cada hora
    public void optimizeHeavyAggregates() {
        try {
            // Encontrar agregados com muitos eventos sem snapshot recente
            List<String> heavyAggregates = findHeavyAggregates();
            
            for (String aggregateId : heavyAggregates) {
                optimizeAggregate(aggregateId);
            }
            
        } catch (Exception e) {
            log.error("Erro na otimização automática de agregados", e);
        }
    }
    
    private List<String> findHeavyAggregates() {
        // Implementar lógica para encontrar agregados pesados
        // Por exemplo: agregados com > 1000 eventos e sem snapshot recente
        return Collections.emptyList(); // Placeholder
    }
    
    private void optimizeAggregate(String aggregateId) {
        // Implementar otimização específica do agregado
        log.info("Otimizando agregado pesado: {}", aggregateId);
    }
}
```

---

## 📊 **MÉTRICAS E MONITORAMENTO**

### **📈 SnapshotMetrics**

```java
@Component
public class SnapshotMetrics implements MeterBinder {
    
    private final MeterRegistry meterRegistry;
    private final SnapshotStore snapshotStore;
    
    // Contadores
    private Counter snapshotsCreated;
    private Counter snapshotsLoaded;
    private Counter snapshotsFailed;
    private Counter snapshotsDeleted;
    
    // Timers
    private Timer creationTimer;
    private Timer loadTimer;
    private Timer compressionTimer;
    
    // Gauges
    private Gauge totalSnapshots;
    private Gauge totalAggregates;
    private Gauge storageUsed;
    private Gauge compressionRatio;
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        
        // Contadores
        snapshotsCreated = Counter.builder("snapshot.created")
            .description("Total snapshots created")
            .register(registry);
            
        snapshotsLoaded = Counter.builder("snapshot.loaded")
            .description("Total snapshots loaded")
            .register(registry);
            
        snapshotsFailed = Counter.builder("snapshot.failed")
            .description("Total snapshot operations failed")
            .register(registry);
            
        snapshotsDeleted = Counter.builder("snapshot.deleted")
            .description("Total snapshots deleted")
            .register(registry);
        
        // Timers
        creationTimer = Timer.builder("snapshot.creation.time")
            .description("Time to create snapshot")
            .register(registry);
            
        loadTimer = Timer.builder("snapshot.load.time")
            .description("Time to load snapshot")
            .register(registry);
            
        compressionTimer = Timer.builder("snapshot.compression.time")
            .description("Time to compress snapshot data")
            .register(registry);
        
        // Gauges
        totalSnapshots = Gauge.builder("snapshot.total")
            .description("Total number of snapshots")
            .register(registry, this, SnapshotMetrics::getTotalSnapshots);
            
        totalAggregates = Gauge.builder("snapshot.aggregates")
            .description("Total aggregates with snapshots")
            .register(registry, this, SnapshotMetrics::getTotalAggregates);
            
        storageUsed = Gauge.builder("snapshot.storage.used")
            .description("Total storage used by snapshots (bytes)")
            .register(registry, this, SnapshotMetrics::getStorageUsed);
            
        compressionRatio = Gauge.builder("snapshot.compression.ratio")
            .description("Average compression ratio")
            .register(registry, this, SnapshotMetrics::getCompressionRatio);
    }
    
    // Métodos para obter valores das métricas
    public double getTotalSnapshots() {
        try {
            return snapshotStore.getStatistics().getTotalSnapshots();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public double getTotalAggregates() {
        try {
            return snapshotStore.getStatistics().getTotalAggregates();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public double getStorageUsed() {
        try {
            return snapshotStore.getStatistics().getTotalStorageUsed();
        } catch (Exception e) {
            return 0;
        }
    }
    
    public double getCompressionRatio() {
        try {
            SnapshotStatistics stats = snapshotStore.getStatistics();
            if (stats.getTotalStorageUsed() > 0) {
                return (double) stats.getTotalCompressedStorage() / stats.getTotalStorageUsed();
            }
            return 0;
        } catch (Exception e) {
            return 0;
        }
    }
    
    // Métodos para instrumentação
    public Timer.Sample startCreationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopCreationTimer(Timer.Sample sample) {
        sample.stop(creationTimer);
    }
    
    public Timer.Sample startLoadTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopLoadTimer(Timer.Sample sample) {
        sample.stop(loadTimer);
    }
    
    public Timer.Sample startCompressionTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopCompressionTimer(Timer.Sample sample) {
        sample.stop(compressionTimer);
    }
    
    public void incrementCreated() {
        snapshotsCreated.increment();
    }
    
    public void incrementLoaded() {
        snapshotsLoaded.increment();
    }
    
    public void incrementFailures() {
        snapshotsFailed.increment();
    }
    
    public void incrementDeleted(int count) {
        snapshotsDeleted.increment(count);
    }
    
    public void updateStorageMetrics(long storageUsed, long spaceSaved) {
        // Atualizar métricas de storage
        // Implementação específica para tracking de storage
    }
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Estratégia de Snapshot Inteligente**

Crie uma estratégia de snapshot que:

1. **Analise padrões de uso** do agregado
2. **Otimize frequência** baseada em acesso
3. **Implemente limpeza automática** de snapshots antigos
4. **Monitore eficiência** da compressão

**Template:**
```java
@Component
public class IntelligentSnapshotStrategy {
    
    public boolean shouldCreateSnapshot(AggregateRoot aggregate, 
                                      AggregateUsageStats usage) {
        // Sua implementação de análise inteligente
        return false;
    }
    
    public void optimizeSnapshotFrequency(String aggregateId, 
                                        List<AccessPattern> patterns) {
        // Sua implementação de otimização
    }
    
    public int cleanupBasedOnUsage(Duration retentionPeriod, 
                                 UsageThreshold threshold) {
        // Sua implementação de limpeza inteligente
        return 0;
    }
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.snapshot`
- **Store**: `PostgreSQLSnapshotStore`
- **Métricas**: `SnapshotMetrics`
- **Otimização**: `AggregateOptimizer`

---

**📍 Próxima Parte**: [Agregados - Parte 4: Regras de Negócio](./08-agregados-parte-4.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Snapshots e otimização de performance  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Estratégia de snapshot inteligente