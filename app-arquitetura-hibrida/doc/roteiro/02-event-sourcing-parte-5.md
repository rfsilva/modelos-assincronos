# 📖 CAPÍTULO 02: EVENT SOURCING - PARTE 5
## Troubleshooting e Operações Avançadas

### 🎯 **OBJETIVOS DESTA PARTE**
- Dominar técnicas de troubleshooting do Event Store
- Implementar estratégias de backup e disaster recovery
- Entender migração e evolução de eventos
- Conhecer otimizações avançadas de performance

---

## 🔍 **TROUBLESHOOTING COMUM**

### **🚨 Problemas Frequentes e Soluções**

#### **1. Performance Degradada**

**Sintomas:**
```bash
# Tempo de resposta alto
curl -w "@curl-format.txt" http://localhost:8083/api/v1/actuator/eventstore/health
# time_total: 5.234s (deveria ser < 1s)

# Métricas mostrando lentidão
curl http://localhost:8083/api/v1/actuator/metrics/eventstore.operation.write
# Valor médio > 100ms
```

**Diagnóstico:**
```java
@Component
public class EventStorePerformanceDiagnostic {
    
    public PerformanceDiagnosticReport diagnosePerformance() {
        PerformanceDiagnosticReport report = new PerformanceDiagnosticReport();
        
        // 1. Verificar conexões de banco
        report.addCheck("Database Connections", checkDatabaseConnections());
        
        // 2. Verificar tamanho das partições
        report.addCheck("Partition Sizes", checkPartitionSizes());
        
        // 3. Verificar índices
        report.addCheck("Index Performance", checkIndexPerformance());
        
        // 4. Verificar serialização
        report.addCheck("Serialization Performance", checkSerializationPerformance());
        
        // 5. Verificar I/O do disco
        report.addCheck("Disk I/O", checkDiskIO());
        
        return report;
    }
    
    private DiagnosticResult checkPartitionSizes() {
        List<PartitionStatistics> stats = partitionManager.getPartitionStatistics();
        
        List<PartitionStatistics> largePartitions = stats.stream()
            .filter(p -> p.getSizeInBytes() > 10_000_000_000L) // > 10GB
            .collect(Collectors.toList());
        
        if (!largePartitions.isEmpty()) {
            return DiagnosticResult.warning(
                "Partições grandes detectadas",
                Map.of(
                    "large_partitions", largePartitions.size(),
                    "recommendation", "Considerar arquivamento ou re-particionamento"
                )
            );
        }
        
        return DiagnosticResult.ok("Tamanhos de partição normais");
    }
    
    private DiagnosticResult checkIndexPerformance() {
        // Query para verificar uso de índices
        String sql = """
            SELECT 
                schemaname,
                tablename,
                indexname,
                idx_scan,
                idx_tup_read,
                idx_tup_fetch
            FROM pg_stat_user_indexes 
            WHERE schemaname = 'eventstore'
            AND idx_scan < 100;  -- Índices pouco usados
        """;
        
        List<Map<String, Object>> underusedIndexes = jdbcTemplate.queryForList(sql);
        
        if (!underusedIndexes.isEmpty()) {
            return DiagnosticResult.warning(
                "Índices subutilizados detectados",
                Map.of("unused_indexes", underusedIndexes)
            );
        }
        
        return DiagnosticResult.ok("Índices sendo utilizados adequadamente");
    }
}
```

**Soluções:**
```java
@Component
public class EventStoreOptimizer {
    
    /**
     * Otimização automática baseada em diagnóstico
     */
    @Scheduled(cron = "0 2 0 * * SUN") // Domingo às 02:00
    public void performWeeklyOptimization() {
        
        PerformanceDiagnosticReport report = diagnostic.diagnosePerformance();
        
        for (DiagnosticResult result : report.getResults()) {
            if (result.getSeverity() == DiagnosticSeverity.WARNING) {
                applyOptimization(result);
            }
        }
    }
    
    private void applyOptimization(DiagnosticResult result) {
        switch (result.getCheckName()) {
            case "Partition Sizes":
                optimizePartitions();
                break;
            case "Index Performance":
                optimizeIndexes();
                break;
            case "Serialization Performance":
                optimizeSerialization();
                break;
        }
    }
    
    private void optimizePartitions() {
        // Arquivar partições antigas automaticamente
        List<String> eligiblePartitions = eventArchiver.findPartitionsForArchiving();
        
        for (String partition : eligiblePartitions) {
            try {
                eventArchiver.archivePartition(partition);
                log.info("Partição {} arquivada automaticamente", partition);
            } catch (Exception e) {
                log.error("Erro ao arquivar partição {}: {}", partition, e.getMessage());
            }
        }
    }
    
    private void optimizeIndexes() {
        // Recriar estatísticas do PostgreSQL
        String sql = "ANALYZE eventstore.events;";
        jdbcTemplate.execute(sql);
        
        // Reindexar se necessário
        sql = "REINDEX TABLE eventstore.events;";
        jdbcTemplate.execute(sql);
    }
}
```

#### **2. Conflitos de Concorrência Excessivos**

**Sintomas:**
```java
// Muitas ConcurrencyException nos logs
2024-01-15 10:30:15 ERROR ConcurrencyException: Conflito para aggregate sinistro-123
2024-01-15 10:30:16 ERROR ConcurrencyException: Conflito para aggregate sinistro-456
```

**Diagnóstico e Solução:**
```java
@Component
public class ConcurrencyAnalyzer {
    
    /**
     * Analisa padrões de conflito de concorrência
     */
    public ConcurrencyReport analyzeConcurrencyPatterns() {
        
        // Buscar conflitos nas últimas 24h
        Instant since = Instant.now().minus(24, ChronoUnit.HOURS);
        
        List<ConcurrencyConflict> conflicts = logAnalyzer.findConcurrencyConflicts(since);
        
        // Agrupar por aggregate
        Map<String, List<ConcurrencyConflict>> conflictsByAggregate = conflicts.stream()
            .collect(Collectors.groupingBy(ConcurrencyConflict::getAggregateId));
        
        // Identificar hot spots
        List<String> hotSpots = conflictsByAggregate.entrySet().stream()
            .filter(entry -> entry.getValue().size() > 10) // > 10 conflitos
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        ConcurrencyReport report = new ConcurrencyReport();
        report.setTotalConflicts(conflicts.size());
        report.setHotSpots(hotSpots);
        report.setRecommendations(generateConcurrencyRecommendations(conflictsByAggregate));
        
        return report;
    }
    
    private List<String> generateConcurrencyRecommendations(
            Map<String, List<ConcurrencyConflict>> conflictsByAggregate) {
        
        List<String> recommendations = new ArrayList<>();
        
        // Recomendar otimizações baseadas nos padrões
        for (Map.Entry<String, List<ConcurrencyConflict>> entry : conflictsByAggregate.entrySet()) {
            
            if (entry.getValue().size() > 20) {
                recommendations.add(
                    String.format("Aggregate %s: Considerar redesign para reduzir contenção", 
                                entry.getKey())
                );
            } else if (entry.getValue().size() > 10) {
                recommendations.add(
                    String.format("Aggregate %s: Implementar retry com backoff exponencial", 
                                entry.getKey())
                );
            }
        }
        
        return recommendations;
    }
}

// Implementação de retry inteligente
@Component
public class IntelligentRetryStrategy {
    
    private final Map<String, AtomicInteger> conflictCounters = new ConcurrentHashMap<>();
    
    public <T> T executeWithIntelligentRetry(String aggregateId, Supplier<T> operation) {
        
        int maxRetries = calculateMaxRetries(aggregateId);
        
        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                T result = operation.get();
                
                // Reset contador em caso de sucesso
                conflictCounters.remove(aggregateId);
                
                return result;
                
            } catch (ConcurrencyException e) {
                
                // Incrementar contador de conflitos
                int conflicts = conflictCounters
                    .computeIfAbsent(aggregateId, k -> new AtomicInteger(0))
                    .incrementAndGet();
                
                if (attempt == maxRetries) {
                    throw new MaxRetriesExceededException(aggregateId, maxRetries, conflicts);
                }
                
                // Backoff adaptativo baseado no histórico de conflitos
                long delay = calculateAdaptiveDelay(attempt, conflicts);
                
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Retry interrompido", ie);
                }
            }
        }
        
        throw new IllegalStateException("Não deveria chegar aqui");
    }
    
    private int calculateMaxRetries(String aggregateId) {
        int conflicts = conflictCounters
            .getOrDefault(aggregateId, new AtomicInteger(0))
            .get();
        
        // Mais conflitos = mais tentativas
        return Math.min(10, 3 + (conflicts / 5));
    }
    
    private long calculateAdaptiveDelay(int attempt, int totalConflicts) {
        // Base: backoff exponencial
        long baseDelay = 100L * (1L << (attempt - 1));
        
        // Ajuste baseado no histórico: mais conflitos = delay maior
        double multiplier = 1.0 + (totalConflicts * 0.1);
        
        // Jitter para evitar thundering herd
        double jitter = 0.8 + (Math.random() * 0.4); // 80% - 120%
        
        return (long) (baseDelay * multiplier * jitter);
    }
}
```

#### **3. Corrupção de Dados**

**Detecção:**
```java
@Component
public class EventStoreIntegrityChecker {
    
    /**
     * Verificação de integridade completa
     */
    @Scheduled(cron = "0 3 0 * * ?") // Todo dia às 03:00
    public IntegrityReport performIntegrityCheck() {
        
        IntegrityReport report = new IntegrityReport();
        
        // 1. Verificar sequência de versões
        report.addCheck("Version Sequence", checkVersionSequence());
        
        // 2. Verificar integridade de serialização
        report.addCheck("Serialization Integrity", checkSerializationIntegrity());
        
        // 3. Verificar referências órfãs
        report.addCheck("Orphaned References", checkOrphanedReferences());
        
        // 4. Verificar checksums (se implementado)
        report.addCheck("Data Checksums", checkDataChecksums());
        
        return report;
    }
    
    private IntegrityResult checkVersionSequence() {
        
        String sql = """
            SELECT aggregate_id, array_agg(version ORDER BY version) as versions
            FROM eventstore.events 
            GROUP BY aggregate_id
            HAVING array_length(array_agg(version), 1) != max(version)
        """;
        
        List<Map<String, Object>> brokenSequences = jdbcTemplate.queryForList(sql);
        
        if (!brokenSequences.isEmpty()) {
            return IntegrityResult.error(
                "Sequências de versão quebradas detectadas",
                Map.of("broken_aggregates", brokenSequences)
            );
        }
        
        return IntegrityResult.ok("Sequências de versão íntegras");
    }
    
    private IntegrityResult checkSerializationIntegrity() {
        
        List<EventStoreEntry> recentEvents = eventStoreRepository
            .findByTimestampGreaterThan(Instant.now().minus(1, ChronoUnit.HOURS));
        
        List<String> corruptedEvents = new ArrayList<>();
        
        for (EventStoreEntry entry : recentEvents) {
            try {
                // Tentar deserializar
                eventSerializer.deserialize(entry.getEventData(), entry.getEventType());
            } catch (Exception e) {
                corruptedEvents.add(entry.getId().toString());
            }
        }
        
        if (!corruptedEvents.isEmpty()) {
            return IntegrityResult.error(
                "Eventos corrompidos detectados",
                Map.of("corrupted_events", corruptedEvents)
            );
        }
        
        return IntegrityResult.ok("Serialização íntegra");
    }
}
```

---

## 💾 **BACKUP E DISASTER RECOVERY**

### **🔄 Estratégia de Backup**

```java
@Component
public class EventStoreBackupService {
    
    private final BackupStorageService backupStorage;
    private final EncryptionService encryptionService;
    
    /**
     * Backup incremental diário
     */
    @Scheduled(cron = "0 1 0 * * ?") // Todo dia à 01:00
    public BackupResult performIncrementalBackup() {
        
        BackupResult result = new BackupResult();
        result.setType(BackupType.INCREMENTAL);
        result.setStartTime(Instant.now());
        
        try {
            // 1. Identificar dados novos desde último backup
            Instant lastBackupTime = getLastBackupTime();
            
            List<EventStoreEntry> newEvents = eventStoreRepository
                .findByTimestampGreaterThan(lastBackupTime);
            
            if (newEvents.isEmpty()) {
                result.setStatus(BackupStatus.SKIPPED);
                result.setMessage("Nenhum evento novo para backup");
                return result;
            }
            
            // 2. Serializar eventos
            BackupData backupData = createBackupData(newEvents);
            
            // 3. Comprimir
            byte[] compressedData = compressBackupData(backupData);
            
            // 4. Criptografar
            byte[] encryptedData = encryptionService.encrypt(compressedData);
            
            // 5. Armazenar
            String backupKey = generateBackupKey(BackupType.INCREMENTAL);
            boolean stored = backupStorage.store(backupKey, encryptedData);
            
            if (stored) {
                // 6. Registrar metadados do backup
                registerBackup(backupKey, BackupType.INCREMENTAL, 
                             newEvents.size(), encryptedData.length);
                
                result.setStatus(BackupStatus.SUCCESS);
                result.setEventCount(newEvents.size());
                result.setBackupSize(encryptedData.length);
            } else {
                result.setStatus(BackupStatus.FAILED);
                result.setErrorMessage("Falha no armazenamento");
            }
            
        } catch (Exception e) {
            result.setStatus(BackupStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    /**
     * Backup completo semanal
     */
    @Scheduled(cron = "0 0 2 * * SUN") // Domingo às 02:00
    public BackupResult performFullBackup() {
        
        BackupResult result = new BackupResult();
        result.setType(BackupType.FULL);
        result.setStartTime(Instant.now());
        
        try {
            // 1. Backup de todas as tabelas
            List<String> tables = Arrays.asList("events", "snapshots", "archive_metadata");
            
            for (String table : tables) {
                BackupResult tableResult = backupTable(table);
                result.addTableResult(table, tableResult);
            }
            
            // 2. Backup de configurações
            BackupResult configResult = backupConfigurations();
            result.addTableResult("configurations", configResult);
            
            result.setStatus(BackupStatus.SUCCESS);
            
        } catch (Exception e) {
            result.setStatus(BackupStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    private BackupResult backupTable(String tableName) {
        // Implementação específica para cada tabela
        String sql = String.format("SELECT * FROM eventstore.%s", tableName);
        
        List<Map<String, Object>> tableData = jdbcTemplate.queryForList(sql);
        
        BackupData backupData = new BackupData();
        backupData.setTableName(tableName);
        backupData.setData(tableData);
        backupData.setTimestamp(Instant.now());
        
        // Processar e armazenar...
        return processTableBackup(backupData);
    }
}
```

### **🚑 Disaster Recovery**

```java
@Component
public class EventStoreRecoveryService {
    
    /**
     * Recuperação completa do Event Store
     */
    public RecoveryResult performFullRecovery(RecoveryOptions options) {
        
        RecoveryResult result = new RecoveryResult();
        result.setStartTime(Instant.now());
        
        try {
            // 1. Validar pré-condições
            validateRecoveryPreconditions(options);
            
            // 2. Preparar ambiente
            prepareRecoveryEnvironment();
            
            // 3. Recuperar backup mais recente
            BackupMetadata latestBackup = findLatestFullBackup(options.getRecoveryPointTime());
            
            if (latestBackup == null) {
                throw new RecoveryException("Nenhum backup válido encontrado");
            }
            
            // 4. Restaurar backup completo
            restoreFullBackup(latestBackup);
            
            // 5. Aplicar backups incrementais
            List<BackupMetadata> incrementalBackups = findIncrementalBackups(
                latestBackup.getTimestamp(), 
                options.getRecoveryPointTime()
            );
            
            for (BackupMetadata backup : incrementalBackups) {
                restoreIncrementalBackup(backup);
            }
            
            // 6. Verificar integridade
            IntegrityReport integrity = integrityChecker.performIntegrityCheck();
            
            if (!integrity.isHealthy()) {
                throw new RecoveryException("Falha na verificação de integridade: " + 
                                          integrity.getErrors());
            }
            
            // 7. Reconstruir índices
            rebuildIndexes();
            
            // 8. Atualizar estatísticas
            updateDatabaseStatistics();
            
            result.setStatus(RecoveryStatus.SUCCESS);
            result.setRecoveredEvents(countRecoveredEvents());
            
        } catch (Exception e) {
            result.setStatus(RecoveryStatus.FAILED);
            result.setErrorMessage(e.getMessage());
            
            // Tentar rollback se possível
            attemptRecoveryRollback();
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    /**
     * Recuperação pontual (Point-in-Time Recovery)
     */
    public RecoveryResult performPointInTimeRecovery(Instant targetTime) {
        
        // 1. Encontrar backup base
        BackupMetadata baseBackup = findLatestFullBackupBefore(targetTime);
        
        // 2. Restaurar backup base
        restoreFullBackup(baseBackup);
        
        // 3. Aplicar eventos até o ponto desejado
        List<EventStoreEntry> events = loadEventsUntil(baseBackup.getTimestamp(), targetTime);
        
        for (EventStoreEntry event : events) {
            replayEvent(event);
        }
        
        // 4. Verificar consistência
        return verifyRecoveryConsistency(targetTime);
    }
}
```

---

## 🔄 **MIGRAÇÃO E EVOLUÇÃO**

### **📈 Evolução de Schema de Eventos**

```java
@Component
public class EventSchemaEvolution {
    
    /**
     * Migração de eventos para nova versão
     */
    public MigrationResult migrateEventsToNewSchema(String eventType, int targetVersion) {
        
        MigrationResult result = new MigrationResult();
        result.setEventType(eventType);
        result.setTargetVersion(targetVersion);
        result.setStartTime(Instant.now());
        
        try {
            // 1. Buscar eventos da versão antiga
            List<EventStoreEntry> oldEvents = eventStoreRepository
                .findByEventTypeAndVersionLessThan(eventType, targetVersion);
            
            result.setTotalEvents(oldEvents.size());
            
            // 2. Migrar em lotes
            int batchSize = 100;
            int migratedCount = 0;
            
            for (int i = 0; i < oldEvents.size(); i += batchSize) {
                List<EventStoreEntry> batch = oldEvents.subList(
                    i, Math.min(i + batchSize, oldEvents.size())
                );
                
                int batchMigrated = migrateBatch(batch, targetVersion);
                migratedCount += batchMigrated;
                
                // Checkpoint para permitir pausa/resume
                saveMigrationCheckpoint(eventType, i + batch.size());
            }
            
            result.setMigratedEvents(migratedCount);
            result.setStatus(MigrationStatus.SUCCESS);
            
        } catch (Exception e) {
            result.setStatus(MigrationStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    private int migrateBatch(List<EventStoreEntry> batch, int targetVersion) {
        
        int migratedCount = 0;
        
        for (EventStoreEntry entry : batch) {
            try {
                // 1. Deserializar evento antigo
                DomainEvent oldEvent = eventSerializer.deserialize(
                    entry.getEventData(), 
                    entry.getEventType()
                );
                
                // 2. Aplicar transformação
                DomainEvent newEvent = applySchemaTransformation(oldEvent, targetVersion);
                
                // 3. Serializar novo evento
                String newEventData = eventSerializer.serialize(newEvent);
                
                // 4. Atualizar entrada (em transação)
                updateEventEntry(entry.getId(), newEventData, targetVersion);
                
                migratedCount++;
                
            } catch (Exception e) {
                log.error("Erro ao migrar evento {}: {}", entry.getId(), e.getMessage());
                // Continuar com próximo evento
            }
        }
        
        return migratedCount;
    }
    
    private DomainEvent applySchemaTransformation(DomainEvent oldEvent, int targetVersion) {
        
        // Factory para transformações baseado no tipo e versão
        EventTransformer transformer = transformerFactory.getTransformer(
            oldEvent.getEventType(), 
            targetVersion
        );
        
        return transformer.transform(oldEvent);
    }
}

// Exemplo de transformer específico
@Component
public class SinistroEventV1ToV2Transformer implements EventTransformer {
    
    @Override
    public DomainEvent transform(DomainEvent oldEvent) {
        
        if (!(oldEvent instanceof SinistroEventV1)) {
            throw new IllegalArgumentException("Evento não é SinistroEventV1");
        }
        
        SinistroEventV1 v1 = (SinistroEventV1) oldEvent;
        
        // Criar evento V2 com novos campos
        SinistroEventV2 v2 = new SinistroEventV2();
        
        // Copiar campos existentes
        v2.setAggregateId(v1.getAggregateId());
        v2.setVersion(v1.getVersion());
        v2.setTimestamp(v1.getTimestamp());
        
        // Transformar dados específicos
        v2.setValorEstimado(v1.getValor()); // Renomear campo
        v2.setMoeda("BRL"); // Novo campo com valor padrão
        
        // Migrar estrutura de endereço
        if (v1.getEndereco() != null) {
            EnderecoV2 enderecoV2 = new EnderecoV2();
            enderecoV2.setLogradouro(v1.getEndereco());
            enderecoV2.setCep(extrairCep(v1.getEndereco())); // Extrair CEP
            v2.setEnderecoOcorrencia(enderecoV2);
        }
        
        return v2;
    }
    
    @Override
    public boolean supports(String eventType, int targetVersion) {
        return "SinistroEvent".equals(eventType) && targetVersion == 2;
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar troubleshooting completo

#### **Passo 1: Simular Problema de Performance**
```java
@Test
public void simularProblemaPerformance() {
    // Criar muitos eventos em uma partição
    String aggregateId = "sinistro-performance-problem";
    
    for (int i = 0; i < 10000; i++) {
        SinistroEvent evento = SinistroEvent.sinistroCriado(
            aggregateId + "-" + i,
            "SIN-PERF-" + i,
            "Evento para teste de performance",
            1000.0
        );
        
        eventStore.saveEvents(aggregateId + "-" + i, Arrays.asList(evento), 0);
    }
    
    // Medir performance após sobrecarga
    long inicio = System.currentTimeMillis();
    eventStore.loadEvents(aggregateId + "-5000");
    long tempo = System.currentTimeMillis() - inicio;
    
    System.out.println("Tempo de carregamento após sobrecarga: " + tempo + "ms");
    
    // Executar diagnóstico
    PerformanceDiagnosticReport report = diagnostic.diagnosePerformance();
    System.out.println("Relatório de diagnóstico: " + report);
}
```

#### **Passo 2: Testar Backup e Recovery**
```bash
# Executar backup manual
curl -X POST http://localhost:8083/api/v1/actuator/eventstore/backup/incremental

# Verificar status do backup
curl http://localhost:8083/api/v1/actuator/eventstore/backup/status

# Simular recovery (em ambiente de teste)
curl -X POST http://localhost:8083/api/v1/actuator/eventstore/recovery/test \
  -H "Content-Type: application/json" \
  -d '{"recoveryPointTime": "2024-01-15T10:00:00Z"}'
```

#### **Passo 3: Testar Migração de Schema**
```java
@Test
public void testarMigracaoSchema() {
    // 1. Criar eventos com schema antigo
    criarEventosSchemaAntigo();
    
    // 2. Executar migração
    MigrationResult result = schemaEvolution.migrateEventsToNewSchema("SinistroEvent", 2);
    
    // 3. Verificar resultado
    assertThat(result.getStatus()).isEqualTo(MigrationStatus.SUCCESS);
    assertThat(result.getMigratedEvents()).isGreaterThan(0);
    
    // 4. Verificar integridade após migração
    IntegrityReport integrity = integrityChecker.performIntegrityCheck();
    assertThat(integrity.isHealthy()).isTrue();
}
```

---

## 📚 **CHECKPOINT FINAL - EVENT SOURCING**

### **✅ Você deve dominar:**

1. **Conceitos fundamentais** do Event Sourcing
2. **Implementação prática** do Event Store
3. **Otimizações** com Snapshots e Arquivamento
4. **Monitoramento** e observabilidade completa
5. **Troubleshooting** e operações avançadas

### **🎯 Competências Adquiridas:**

- ✅ Modelar eventos de domínio corretamente
- ✅ Implementar Event Store com PostgreSQL
- ✅ Configurar snapshots para performance
- ✅ Monitorar métricas e health checks
- ✅ Resolver problemas de performance e concorrência
- ✅ Implementar backup e disaster recovery
- ✅ Migrar schemas de eventos

### **❓ Questões de Revisão:**

1. **Quando usar Event Sourcing vs CRUD tradicional?**
2. **Como garantir performance com muitos eventos?**
3. **Qual a estratégia ideal para snapshots?**
4. **Como detectar e resolver problemas de concorrência?**
5. **Qual a importância do backup em Event Sourcing?**

---

## 🎓 **CONCLUSÃO DO CAPÍTULO**

Parabéns! Você completou o estudo completo de **Event Sourcing**. Este conhecimento é fundamental para entender como nossa arquitetura híbrida mantém um histórico completo e auditável de todas as mudanças no sistema.

### **🔗 Próximo Capítulo**

No **Capítulo 03 - CQRS**, vamos explorar:
- Como separar responsabilidades de comando e consulta
- Implementação do Command Side
- Implementação do Query Side
- Sincronização entre os lados
- Padrões de consistência eventual

---

**📖 Capítulo elaborado por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Total:** 4 horas (5 partes × 48 minutos)  
**📋 Pré-requisitos:** Capítulo 01 completo