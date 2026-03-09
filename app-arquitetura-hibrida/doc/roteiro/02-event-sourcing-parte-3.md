# 📖 CAPÍTULO 02: EVENT SOURCING - PARTE 3
## Snapshots e Otimizações de Performance

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender o conceito e implementação de Snapshots
- Entender estratégias de otimização de performance
- Explorar o sistema de arquivamento de eventos
- Conhecer técnicas de replay e reconstrução de estado

---

## 📸 **SNAPSHOTS: OTIMIZAÇÃO ESSENCIAL**

### **🤔 Por que Snapshots?**

**Problema**: Aggregate com muitos eventos demora para reconstruir
```java
// Sinistro com 1000 eventos = 1000 operações para reconstruir estado
List<DomainEvent> eventos = eventStore.loadEvents("sinistro-antigo-123");
// 1000 eventos × 10ms cada = 10 segundos para carregar!

SinistroAggregate sinistro = new SinistroAggregate();
for (DomainEvent evento : eventos) {
    sinistro.applyEvent(evento); // Muito lento!
}
```

**Solução**: Snapshot = "foto" do estado em um ponto específico
```java
// Com snapshot na versão 900:
Snapshot snapshot = snapshotStore.loadSnapshot("sinistro-antigo-123");
List<DomainEvent> eventosIncremento = eventStore.loadEvents("sinistro-antigo-123", 900);

// Apenas 100 eventos para processar = 1 segundo!
SinistroAggregate sinistro = new SinistroAggregate();
sinistro.restoreFromSnapshot(snapshot.getData());
for (DomainEvent evento : eventosIncremento) {
    sinistro.applyEvent(evento);
}
```

### **🏗️ Arquitetura de Snapshots**

```
┌─────────────────────────────────────────────────────────┐
│                   SNAPSHOT SYSTEM                       │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │  SnapshotStore  │    │  SnapshotEntry  │            │
│  │   Interface     │◄───│    Entity       │            │
│  └─────────────────┘    └─────────────────┘            │
│           ▲                       │                     │
│           │                       ▼                     │
│  ┌─────────────────┐    ┌─────────────────┐            │
│  │   PostgreSQL    │    │   Compression   │            │
│  │ SnapshotStore   │    │    Strategy     │            │
│  └─────────────────┘    └─────────────────┘            │
│           │                       │                     │
│           ▼                       ▼                     │
│  ┌─────────────────────────────────────────────────────┤
│  │         PostgreSQL snapshots table                 │
│  └─────────────────────────────────────────────────────┘
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### **🗃️ Estrutura da Tabela de Snapshots**

```sql
-- Localização: db/migration/V2__Create_Snapshots_Table.sql
CREATE TABLE eventstore.snapshots (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    aggregate_id VARCHAR(255) NOT NULL,
    aggregate_type VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL,
    snapshot_data JSONB NOT NULL,
    metadata JSONB,
    timestamp TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    data_size INTEGER,
    compressed BOOLEAN DEFAULT FALSE,
    compression_ratio DECIMAL(5,2),
    
    -- Constraint: apenas um snapshot por aggregate/version
    CONSTRAINT snapshots_aggregate_version_unique 
        UNIQUE (aggregate_id, version)
);

-- Índices para performance
CREATE INDEX idx_snapshots_aggregate_id ON eventstore.snapshots (aggregate_id);
CREATE INDEX idx_snapshots_timestamp ON eventstore.snapshots (timestamp);
CREATE INDEX idx_snapshots_version ON eventstore.snapshots (aggregate_id, version DESC);
```

### **📊 Interface do Snapshot Store**

```java
// Localização: snapshot/SnapshotStore.java
public interface SnapshotStore {
    
    /**
     * Salva snapshot de um aggregate
     */
    void saveSnapshot(String aggregateId, 
                     long version, 
                     Object snapshotData);
    
    /**
     * Carrega o snapshot mais recente
     */
    Optional<Snapshot> loadSnapshot(String aggregateId);
    
    /**
     * Carrega snapshot de uma versão específica
     */
    Optional<Snapshot> loadSnapshot(String aggregateId, long version);
    
    /**
     * Remove snapshots antigos (limpeza)
     */
    int deleteSnapshotsOlderThan(String aggregateId, long keepVersion);
    
    /**
     * Verifica se existe snapshot
     */
    boolean hasSnapshot(String aggregateId);
    
    /**
     * Obtém estatísticas de snapshots
     */
    SnapshotStatistics getStatistics();
}
```

### **🔧 Implementação PostgreSQL**

```java
// Localização: snapshot/impl/PostgreSQLSnapshotStore.java
@Repository
public class PostgreSQLSnapshotStore implements SnapshotStore {
    
    private final SnapshotRepository repository;
    private final EventSerializer serializer;
    private final SnapshotMetrics metrics;
    
    @Override
    public void saveSnapshot(String aggregateId, 
                           long version, 
                           Object snapshotData) {
        
        Timer.Sample sample = metrics.startCreationTimer();
        
        try {
            // 1. Serializar dados do snapshot
            String serializedData = serializer.serialize(snapshotData);
            
            // 2. Comprimir se necessário (threshold: 1KB)
            SerializationResult result = serializer.serializeWithCompression(
                snapshotData, 1024
            );
            
            // 3. Criar entrada
            SnapshotEntry entry = new SnapshotEntry();
            entry.setAggregateId(aggregateId);
            entry.setAggregateType(snapshotData.getClass().getSimpleName());
            entry.setVersion(version);
            entry.setSnapshotData(result.getData());
            entry.setCompressed(result.isCompressed());
            entry.setDataSize(result.getOriginalSize());
            entry.setCompressionRatio(result.getCompressionRatio());
            
            // 4. Salvar
            repository.save(entry);
            
            // 5. Limpar snapshots antigos (manter apenas os 3 mais recentes)
            deleteSnapshotsOlderThan(aggregateId, version - 3);
            
            metrics.incrementSnapshotsCreated();
            
        } finally {
            metrics.stopCreationTimer(sample);
        }
    }
    
    @Override
    public Optional<Snapshot> loadSnapshot(String aggregateId) {
        Timer.Sample sample = metrics.startLoadTimer();
        
        try {
            Optional<SnapshotEntry> entry = repository
                .findTopByAggregateIdOrderByVersionDesc(aggregateId);
            
            if (entry.isEmpty()) {
                return Optional.empty();
            }
            
            // Deserializar dados
            Object snapshotData = deserializeSnapshotData(entry.get());
            
            Snapshot snapshot = new Snapshot(
                entry.get().getAggregateId(),
                entry.get().getVersion(),
                snapshotData,
                entry.get().getTimestamp()
            );
            
            metrics.incrementSnapshotsLoaded();
            return Optional.of(snapshot);
            
        } finally {
            metrics.stopLoadTimer(sample);
        }
    }
    
    private Object deserializeSnapshotData(SnapshotEntry entry) {
        if (entry.getCompressed()) {
            return serializer.deserializeCompressed(
                entry.getSnapshotData(),
                entry.getAggregateType(),
                true
            );
        } else {
            return serializer.deserialize(
                entry.getSnapshotData(),
                entry.getAggregateType()
            );
        }
    }
}
```

---

## ⚡ **ESTRATÉGIAS DE CRIAÇÃO DE SNAPSHOTS**

### **📏 Estratégia por Número de Eventos**

```java
// Localização: aggregate/repository/EventSourcingAggregateRepository.java
@Override
public void save(T aggregate) {
    // 1. Salvar eventos
    List<DomainEvent> uncommittedEvents = aggregate.getUncommittedEvents();
    eventStore.saveEvents(
        aggregate.getId(),
        uncommittedEvents,
        aggregate.getVersion() - uncommittedEvents.size()
    );
    
    // 2. Verificar se precisa criar snapshot
    createSnapshotIfNeeded(aggregate);
    
    // 3. Marcar eventos como commitados
    aggregate.markEventsAsCommitted();
}

private void createSnapshotIfNeeded(T aggregate) {
    // Criar snapshot a cada 50 eventos
    if (aggregate.getVersion() % 50 == 0) {
        boolean success = createSnapshotForAggregate(aggregate);
        
        if (success) {
            log.info("Snapshot criado para aggregate {} na versão {}", 
                    aggregate.getId(), aggregate.getVersion());
        }
    }
}
```

### **⏰ Estratégia por Tempo**

```java
// Snapshot diário para aggregates ativos
@Scheduled(cron = "0 2 0 * * ?") // Todo dia às 02:00
public void createDailySnapshots() {
    
    // Buscar aggregates modificados nas últimas 24h
    Instant yesterday = Instant.now().minus(24, ChronoUnit.HOURS);
    
    List<String> activeAggregates = eventStoreRepository
        .findDistinctAggregateIdsModifiedSince(yesterday);
    
    for (String aggregateId : activeAggregates) {
        try {
            // Verificar se já tem snapshot recente
            Optional<Snapshot> lastSnapshot = snapshotStore.loadSnapshot(aggregateId);
            
            if (shouldCreateSnapshot(lastSnapshot)) {
                createSnapshotForAggregate(aggregateId);
            }
            
        } catch (Exception e) {
            log.error("Erro ao criar snapshot para {}: {}", aggregateId, e.getMessage());
        }
    }
}

private boolean shouldCreateSnapshot(Optional<Snapshot> lastSnapshot) {
    if (lastSnapshot.isEmpty()) {
        return true; // Nunca teve snapshot
    }
    
    // Criar se último snapshot tem mais de 7 dias
    return lastSnapshot.get().getTimestamp()
        .isBefore(Instant.now().minus(7, ChronoUnit.DAYS));
}
```

### **🎯 Estratégia Inteligente**

```java
// Combina múltiplos critérios
public class SmartSnapshotStrategy {
    
    public boolean shouldCreateSnapshot(String aggregateId, long currentVersion) {
        
        // 1. Verificar número de eventos desde último snapshot
        Optional<Snapshot> lastSnapshot = snapshotStore.loadSnapshot(aggregateId);
        long eventsSinceSnapshot = lastSnapshot
            .map(s -> currentVersion - s.getVersion())
            .orElse(currentVersion);
        
        // 2. Verificar tempo desde último snapshot
        boolean timeThresholdMet = lastSnapshot
            .map(s -> s.getTimestamp().isBefore(
                Instant.now().minus(24, ChronoUnit.HOURS)
            ))
            .orElse(true);
        
        // 3. Verificar frequência de acesso
        AccessPattern pattern = accessPatternAnalyzer.getPattern(aggregateId);
        
        // Critérios combinados:
        return (eventsSinceSnapshot >= 50) ||  // Muitos eventos
               (eventsSinceSnapshot >= 20 && timeThresholdMet) ||  // Eventos + tempo
               (pattern.isHighFrequency() && eventsSinceSnapshot >= 10);  // Alto acesso
    }
}
```

---

## 🗄️ **ARQUIVAMENTO DE EVENTOS**

### **📦 Sistema de Archive**

```java
// Localização: eventstore/archive/EventArchiver.java
@Component
public class EventArchiver {
    
    private final ArchiveStorageService storageService;
    private final PartitionManager partitionManager;
    
    /**
     * Arquiva partições antigas automaticamente
     */
    @Scheduled(cron = "0 3 0 * * SUN") // Domingo às 03:00
    public ArchiveSummary executeAutoArchiving() {
        
        List<String> eligiblePartitions = findPartitionsForArchiving();
        ArchiveSummary summary = new ArchiveSummary();
        
        for (String partitionName : eligiblePartitions) {
            try {
                ArchiveResult result = archivePartition(partitionName);
                summary.addResult(result);
                
                if (result.isSuccess()) {
                    log.info("Partição {} arquivada com sucesso. {} eventos, {} comprimido",
                            partitionName, result.getEventCount(), 
                            formatBytes(result.getCompressedSize()));
                }
                
            } catch (Exception e) {
                log.error("Erro ao arquivar partição {}: {}", partitionName, e.getMessage());
                summary.addResult(ArchiveResult.error(partitionName, e.getMessage()));
            }
        }
        
        return summary;
    }
    
    public ArchiveResult archivePartition(String partitionName) {
        
        // 1. Exportar dados da partição
        byte[] partitionData = exportPartitionData(partitionName);
        
        // 2. Comprimir dados
        byte[] compressedData = compressData(partitionData);
        
        // 3. Armazenar no storage frio (S3, filesystem, etc.)
        String archiveKey = generateArchiveKey(partitionName);
        boolean stored = storageService.store(archiveKey, compressedData);
        
        if (!stored) {
            return ArchiveResult.error(partitionName, "Falha no armazenamento");
        }
        
        // 4. Registrar metadados do arquivo
        registerArchive(partitionName, archiveKey, 
                       getPartitionEventCount(partitionName), 
                       compressedData.length);
        
        // 5. Remover partição original (após confirmação)
        dropPartition(partitionName);
        
        return ArchiveResult.success(partitionName, 
                                   getPartitionEventCount(partitionName),
                                   compressedData.length);
    }
    
    /**
     * Restaura partição arquivada quando necessário
     */
    public boolean restorePartition(String partitionName) {
        
        ArchiveMetadata metadata = getArchiveMetadata(partitionName);
        if (metadata == null) {
            return false;
        }
        
        // 1. Recuperar dados do storage
        byte[] compressedData = storageService.retrieve(metadata.getArchiveKey());
        
        // 2. Descomprimir
        byte[] partitionData = decompressData(compressedData);
        
        // 3. Restaurar partição
        return restorePartitionData(partitionName, partitionData);
    }
}
```

### **☁️ Storage Strategies**

```java
// Implementação para filesystem local
@Component
@ConditionalOnProperty(name = "eventstore.archive.storage", havingValue = "filesystem")
public class FileSystemArchiveStorage implements ArchiveStorageService {
    
    private final Path archiveDirectory;
    
    @Override
    public boolean store(String key, byte[] data) {
        try {
            Path filePath = getFilePath(key);
            Files.createDirectories(filePath.getParent());
            Files.write(filePath, data);
            return true;
        } catch (IOException e) {
            log.error("Erro ao armazenar arquivo {}: {}", key, e.getMessage());
            return false;
        }
    }
    
    @Override
    public byte[] retrieve(String key) {
        try {
            Path filePath = getFilePath(key);
            return Files.readAllBytes(filePath);
        } catch (IOException e) {
            log.error("Erro ao recuperar arquivo {}: {}", key, e.getMessage());
            return null;
        }
    }
    
    private Path getFilePath(String key) {
        return archiveDirectory.resolve(key + ".gz");
    }
}

// Implementação para AWS S3 (exemplo)
@Component
@ConditionalOnProperty(name = "eventstore.archive.storage", havingValue = "s3")
public class S3ArchiveStorage implements ArchiveStorageService {
    
    private final AmazonS3 s3Client;
    private final String bucketName;
    
    @Override
    public boolean store(String key, byte[] data) {
        try {
            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(data.length);
            metadata.setContentType("application/gzip");
            
            s3Client.putObject(bucketName, key, 
                             new ByteArrayInputStream(data), metadata);
            return true;
        } catch (Exception e) {
            log.error("Erro ao armazenar no S3 {}: {}", key, e.getMessage());
            return false;
        }
    }
}
```

---

## 🔄 **REPLAY DE EVENTOS**

### **⏪ Event Replay System**

```java
// Localização: eventstore/replay/EventReplayer.java
@Component
public class EventReplayer {
    
    /**
     * Replay de eventos para reconstruir projeções
     */
    public ReplayResult replayEvents(ReplayFilter filter) {
        
        ReplayResult result = new ReplayResult();
        result.setStartTime(Instant.now());
        
        try {
            // 1. Buscar eventos baseado no filtro
            List<DomainEvent> events = loadEventsForReplay(filter);
            result.setTotalEvents(events.size());
            
            // 2. Processar eventos em lotes
            int batchSize = filter.getBatchSize();
            
            for (int i = 0; i < events.size(); i += batchSize) {
                List<DomainEvent> batch = events.subList(
                    i, Math.min(i + batchSize, events.size())
                );
                
                ReplayProgress progress = processBatch(batch, filter);
                result.addProgress(progress);
                
                // 3. Checkpoint para permitir pausa/resume
                if (filter.isCheckpointEnabled()) {
                    saveReplayCheckpoint(filter.getReplayId(), i + batch.size());
                }
                
                // 4. Verificar se deve pausar
                if (shouldPauseReplay(filter.getReplayId())) {
                    result.setStatus(ReplayStatus.PAUSED);
                    break;
                }
            }
            
            if (result.getStatus() != ReplayStatus.PAUSED) {
                result.setStatus(ReplayStatus.COMPLETED);
            }
            
        } catch (Exception e) {
            result.setStatus(ReplayStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    private ReplayProgress processBatch(List<DomainEvent> batch, ReplayFilter filter) {
        
        ReplayProgress progress = new ReplayProgress();
        progress.setBatchSize(batch.size());
        progress.setStartTime(Instant.now());
        
        for (DomainEvent event : batch) {
            try {
                // Republicar evento para processamento
                if (filter.shouldReplayEvent(event)) {
                    eventBus.publish(event);
                    progress.incrementProcessed();
                } else {
                    progress.incrementSkipped();
                }
                
            } catch (Exception e) {
                progress.incrementFailed();
                progress.addError(new ReplayError(event, e));
            }
        }
        
        progress.setEndTime(Instant.now());
        return progress;
    }
}
```

### **🎯 Filtros de Replay**

```java
public class ReplayFilter {
    
    private String replayId;
    private Instant fromTimestamp;
    private Instant toTimestamp;
    private Set<String> eventTypes;
    private Set<String> aggregateIds;
    private String aggregateType;
    private int batchSize = 100;
    private boolean checkpointEnabled = true;
    
    public boolean shouldReplayEvent(DomainEvent event) {
        
        // Filtro por timestamp
        if (fromTimestamp != null && event.getTimestamp().isBefore(fromTimestamp)) {
            return false;
        }
        
        if (toTimestamp != null && event.getTimestamp().isAfter(toTimestamp)) {
            return false;
        }
        
        // Filtro por tipo de evento
        if (eventTypes != null && !eventTypes.isEmpty()) {
            return eventTypes.contains(event.getEventType());
        }
        
        // Filtro por aggregate
        if (aggregateIds != null && !aggregateIds.isEmpty()) {
            return aggregateIds.contains(event.getAggregateId());
        }
        
        return true;
    }
    
    // Factory methods para filtros comuns
    public static ReplayFilter forProjectionRebuild(String projectionName) {
        ReplayFilter filter = new ReplayFilter();
        filter.setReplayId("projection-rebuild-" + projectionName);
        filter.setBatchSize(50); // Menor para projeções
        return filter;
    }
    
    public static ReplayFilter forDateRange(LocalDate from, LocalDate to) {
        ReplayFilter filter = new ReplayFilter();
        filter.setFromTimestamp(from.atStartOfDay().toInstant(ZoneOffset.UTC));
        filter.setToTimestamp(to.plusDays(1).atStartOfDay().toInstant(ZoneOffset.UTC));
        return filter;
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar e testar Snapshots

#### **Passo 1: Criar Snapshot Manualmente**
```java
@Test
public void testarCriacaoSnapshot() {
    // 1. Criar aggregate com vários eventos
    String aggregateId = "sinistro-snapshot-test";
    
    SinistroAggregate sinistro = new SinistroAggregate();
    sinistro.create("Teste snapshot", "Descrição teste");
    
    // Simular 60 eventos para forçar snapshot
    for (int i = 0; i < 60; i++) {
        sinistro.adicionarObservacao("Observação " + i);
    }
    
    // 2. Salvar (deve criar snapshot automaticamente)
    repository.save(sinistro);
    
    // 3. Verificar se snapshot foi criado
    Optional<Snapshot> snapshot = snapshotStore.loadSnapshot(aggregateId);
    assertThat(snapshot).isPresent();
    assertThat(snapshot.get().getVersion()).isEqualTo(60);
}
```

#### **Passo 2: Testar Performance com Snapshot**
```java
@Test
public void compararPerformanceComSnapshot() {
    String aggregateId = "sinistro-performance-test";
    
    // Criar aggregate com muitos eventos
    criarAggregateComMuitosEventos(aggregateId, 500);
    
    // Teste 1: Carregar sem snapshot
    long inicio1 = System.currentTimeMillis();
    SinistroAggregate sinistro1 = repository.findById(aggregateId).get();
    long tempo1 = System.currentTimeMillis() - inicio1;
    
    // Criar snapshot
    snapshotStore.saveSnapshot(aggregateId, 500, sinistro1.createSnapshot());
    
    // Teste 2: Carregar com snapshot
    long inicio2 = System.currentTimeMillis();
    SinistroAggregate sinistro2 = repository.findById(aggregateId).get();
    long tempo2 = System.currentTimeMillis() - inicio2;
    
    System.out.println("Sem snapshot: " + tempo1 + "ms");
    System.out.println("Com snapshot: " + tempo2 + "ms");
    System.out.println("Melhoria: " + ((tempo1 - tempo2) * 100 / tempo1) + "%");
    
    // Snapshot deve ser significativamente mais rápido
    assertThat(tempo2).isLessThan(tempo1 / 2);
}
```

#### **Passo 3: Explorar Arquivamento**
```bash
# Ver partições elegíveis para arquivamento
curl http://localhost:8083/api/v1/actuator/eventstore/maintenance/eligible-partitions

# Executar arquivamento manual
curl -X POST http://localhost:8083/api/v1/actuator/eventstore/maintenance/archive

# Ver estatísticas de arquivo
curl http://localhost:8083/api/v1/actuator/eventstore/archive/statistics
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** por que snapshots são necessários
2. **Implementar** estratégias de criação de snapshots
3. **Configurar** arquivamento automático de eventos
4. **Executar** replay de eventos para reconstrução
5. **Monitorar** performance do Event Store

### **❓ Perguntas para Reflexão:**

1. Quando criar snapshots: por eventos ou por tempo?
2. Como garantir consistência durante arquivamento?
3. Qual o impacto de snapshots na auditoria?
4. Como testar replay sem afetar produção?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 4**, vamos explorar:
- Monitoramento e métricas do Event Store
- Health checks e alertas
- Troubleshooting de problemas comuns
- Ferramentas de administração

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 55 minutos  
**📋 Pré-requisitos:** Event Sourcing Partes 1-2