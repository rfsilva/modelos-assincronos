# 📖 CAPÍTULO 03: CQRS - PARTE 4
## Consistência Eventual e Sincronização

### 🎯 **OBJETIVOS DESTA PARTE**
- Entender consistência eventual no CQRS
- Implementar monitoramento de lag
- Configurar estratégias de sincronização
- Tratar falhas na projeção de dados

---

## ⏱️ **CONSISTÊNCIA EVENTUAL**

### **🔄 Conceito e Implementação**

```java
// Localização: cqrs/consistency/EventualConsistencyMonitor.java
@Component
public class EventualConsistencyMonitor {
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    
    /**
     * Calcula lag entre Command Side e Query Side
     */
    public ConsistencyReport checkConsistency() {
        
        ConsistencyReport report = new ConsistencyReport();
        
        // Obter último evento no Event Store
        Long maxEventId = eventStoreRepository.findMaxEventId().orElse(0L);
        
        // Verificar posição de cada projeção
        List<ProjectionTracker> projections = projectionTrackerRepository.findAll();
        
        for (ProjectionTracker projection : projections) {
            
            ConsistencyStatus status = calculateConsistencyStatus(projection, maxEventId);
            report.addProjectionStatus(projection.getProjectionName(), status);
            
            // Alertar se lag for muito alto
            if (status.getLag() > 1000) { // > 1000 eventos atrás
                report.addWarning(String.format(
                    "Projeção %s com lag alto: %d eventos", 
                    projection.getProjectionName(), 
                    status.getLag()
                ));
            }
        }
        
        return report;
    }
    
    private ConsistencyStatus calculateConsistencyStatus(ProjectionTracker projection, Long maxEventId) {
        
        Long lastProcessedEventId = projection.getLastProcessedEventId();
        if (lastProcessedEventId == null) {
            lastProcessedEventId = 0L;
        }
        
        long lag = maxEventId - lastProcessedEventId;
        
        // Calcular tempo estimado para sincronização
        double throughput = calculateProjectionThroughput(projection);
        long estimatedSyncTimeSeconds = throughput > 0 ? (long) (lag / throughput) : -1;
        
        return ConsistencyStatus.builder()
            .projectionName(projection.getProjectionName())
            .lastProcessedEventId(lastProcessedEventId)
            .maxAvailableEventId(maxEventId)
            .lag(lag)
            .estimatedSyncTimeSeconds(estimatedSyncTimeSeconds)
            .lastProcessedAt(projection.getLastProcessedAt())
            .isHealthy(lag < 100) // Saudável se lag < 100 eventos
            .build();
    }
    
    private double calculateProjectionThroughput(ProjectionTracker projection) {
        
        // Calcular eventos processados por segundo nas últimas 24h
        Instant oneDayAgo = Instant.now().minus(24, ChronoUnit.HOURS);
        
        if (projection.getLastProcessedAt() == null || 
            projection.getLastProcessedAt().isBefore(oneDayAgo)) {
            return 0.0;
        }
        
        long eventsProcessed = projection.getEventsProcessed() != null ? 
            projection.getEventsProcessed() : 0L;
        
        long secondsSinceCreation = ChronoUnit.SECONDS.between(
            projection.getCreatedAt(), 
            Instant.now()
        );
        
        return secondsSinceCreation > 0 ? (double) eventsProcessed / secondsSinceCreation : 0.0;
    }
}
```

### **📊 Métricas de Consistência**

```java
// Localização: cqrs/metrics/CQRSMetrics.java
@Component
public class CQRSMetrics implements MeterBinder {
    
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository projectionTrackerRepository;
    private MeterRegistry meterRegistry;
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        
        // Gauge para lag geral
        Gauge.builder("cqrs.lag.overall")
            .description("Lag geral entre Command e Query Side")
            .register(registry, this, CQRSMetrics::getOverallLag);
        
        // Gauge para número de projeções ativas
        Gauge.builder("cqrs.projections.active")
            .description("Número de projeções ativas")
            .register(registry, this, CQRSMetrics::getActiveProjections);
        
        // Gauge para taxa de erro das projeções
        Gauge.builder("cqrs.projections.error_rate")
            .description("Taxa de erro das projeções")
            .register(registry, this, CQRSMetrics::getProjectionsErrorRate);
    }
    
    public double getOverallLag() {
        try {
            Long maxEventId = eventStoreRepository.findMaxEventId().orElse(0L);
            Optional<Long> minProcessedEventId = projectionTrackerRepository.findMinProcessedEventId();
            
            if (minProcessedEventId.isPresent()) {
                return maxEventId - minProcessedEventId.get();
            }
            
            return 0.0;
        } catch (Exception e) {
            return -1.0; // Indica erro
        }
    }
    
    public double getActiveProjections() {
        return projectionTrackerRepository.countByStatus(ProjectionStatus.ACTIVE);
    }
    
    public double getProjectionsErrorRate() {
        
        List<ProjectionTracker> allProjections = projectionTrackerRepository.findAll();
        
        if (allProjections.isEmpty()) {
            return 0.0;
        }
        
        long projectionsWithErrors = allProjections.stream()
            .mapToLong(p -> {
                Long processed = p.getEventsProcessed() != null ? p.getEventsProcessed() : 0L;
                Long failed = p.getEventsFailed() != null ? p.getEventsFailed() : 0L;
                long total = processed + failed;
                
                return total > 0 && (double) failed / total > 0.05 ? 1 : 0;
            })
            .sum();
        
        return (double) projectionsWithErrors / allProjections.size();
    }
    
    /**
     * Atualiza métricas periodicamente
     */
    @Scheduled(fixedRate = 30000) // A cada 30 segundos
    public void updateMetrics() {
        
        try {
            // Atualizar métricas por projeção
            List<ProjectionTracker> projections = projectionTrackerRepository.findAll();
            Long maxEventId = eventStoreRepository.findMaxEventId().orElse(0L);
            
            for (ProjectionTracker projection : projections) {
                
                String projectionName = projection.getProjectionName();
                Long lastProcessedEventId = projection.getLastProcessedEventId();
                
                if (lastProcessedEventId != null) {
                    long lag = maxEventId - lastProcessedEventId;
                    
                    // Registrar lag por projeção
                    Gauge.builder("cqrs.projection.lag")
                        .tag("projection", projectionName)
                        .description("Lag por projeção")
                        .register(meterRegistry, lag, Number::doubleValue);
                    
                    // Registrar throughput por projeção
                    double throughput = calculateProjectionThroughput(projection);
                    Gauge.builder("cqrs.projection.throughput")
                        .tag("projection", projectionName)
                        .description("Throughput por projeção (eventos/segundo)")
                        .register(meterRegistry, throughput, Number::doubleValue);
                }
            }
            
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas CQRS: {}", e.getMessage());
        }
    }
}
```

---

## 🔄 **ESTRATÉGIAS DE SINCRONIZAÇÃO**

### **⚡ Sincronização Automática**

```java
// Localização: cqrs/sync/ProjectionSynchronizer.java
@Component
public class ProjectionSynchronizer {
    
    private final ProjectionEventProcessor eventProcessor;
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionTrackerRepository trackerRepository;
    
    /**
     * Sincronização automática de projeções atrasadas
     */
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void synchronizeProjections() {
        
        List<ProjectionTracker> laggedProjections = findLaggedProjections();
        
        for (ProjectionTracker projection : laggedProjections) {
            try {
                synchronizeProjection(projection);
            } catch (Exception e) {
                log.error("Erro ao sincronizar projeção {}: {}", 
                         projection.getProjectionName(), e.getMessage());
            }
        }
    }
    
    private List<ProjectionTracker> findLaggedProjections() {
        
        Long maxEventId = eventStoreRepository.findMaxEventId().orElse(0L);
        long lagThreshold = 100; // Considerar atrasada se > 100 eventos
        
        return trackerRepository.findProjectionsWithHighLag(maxEventId, lagThreshold);
    }
    
    private void synchronizeProjection(ProjectionTracker projection) {
        
        log.info("Iniciando sincronização da projeção: {}", projection.getProjectionName());
        
        Long lastProcessedEventId = projection.getLastProcessedEventId();
        if (lastProcessedEventId == null) {
            lastProcessedEventId = 0L;
        }
        
        // Buscar eventos não processados
        List<EventStoreEntry> pendingEvents = eventStoreRepository
            .findByLastEventIdGreaterThanOrderByLastEventIdAsc(lastProcessedEventId);
        
        // Processar em lotes
        int batchSize = 50;
        for (int i = 0; i < pendingEvents.size(); i += batchSize) {
            
            List<EventStoreEntry> batch = pendingEvents.subList(
                i, Math.min(i + batchSize, pendingEvents.size())
            );
            
            processBatch(batch, projection);
            
            // Pequena pausa para não sobrecarregar
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }
        
        log.info("Sincronização concluída para projeção: {}", projection.getProjectionName());
    }
    
    private void processBatch(List<EventStoreEntry> batch, ProjectionTracker projection) {
        
        for (EventStoreEntry entry : batch) {
            try {
                // Converter para DomainEvent
                DomainEvent event = convertToDomainEvent(entry);
                
                // Processar evento
                eventProcessor.processEvent(event, entry.getId());
                
            } catch (Exception e) {
                log.error("Erro ao processar evento {} na projeção {}: {}", 
                         entry.getId(), projection.getProjectionName(), e.getMessage());
                
                // Registrar falha
                projection.recordFailure(e.getMessage());
                trackerRepository.save(projection);
            }
        }
    }
}
```

### **🚨 Alertas de Inconsistência**

```java
// Localização: cqrs/alerts/ConsistencyAlerting.java
@Component
public class ConsistencyAlerting {
    
    private final EventualConsistencyMonitor consistencyMonitor;
    private final NotificationService notificationService;
    
    @Scheduled(fixedRate = 300000) // A cada 5 minutos
    public void checkConsistencyAndAlert() {
        
        ConsistencyReport report = consistencyMonitor.checkConsistency();
        
        // Verificar se há problemas críticos
        List<ConsistencyStatus> criticalIssues = report.getProjectionStatuses()
            .stream()
            .filter(status -> !status.isHealthy())
            .filter(status -> status.getLag() > 1000) // Lag crítico
            .collect(Collectors.toList());
        
        if (!criticalIssues.isEmpty()) {
            sendCriticalAlert(criticalIssues);
        }
        
        // Verificar warnings
        if (!report.getWarnings().isEmpty()) {
            sendWarningAlert(report.getWarnings());
        }
    }
    
    private void sendCriticalAlert(List<ConsistencyStatus> criticalIssues) {
        
        StringBuilder message = new StringBuilder();
        message.append("🚨 ALERTA CRÍTICO - Inconsistência CQRS\n\n");
        
        for (ConsistencyStatus issue : criticalIssues) {
            message.append(String.format(
                "Projeção: %s\n" +
                "Lag: %d eventos\n" +
                "Tempo estimado para sincronização: %d segundos\n" +
                "Última atualização: %s\n\n",
                issue.getProjectionName(),
                issue.getLag(),
                issue.getEstimatedSyncTimeSeconds(),
                issue.getLastProcessedAt()
            ));
        }
        
        Alert alert = Alert.builder()
            .severity(AlertSeverity.CRITICAL)
            .title("Inconsistência Crítica CQRS")
            .message(message.toString())
            .timestamp(Instant.now())
            .build();
        
        notificationService.sendAlert(alert);
    }
    
    private void sendWarningAlert(List<String> warnings) {
        
        Alert alert = Alert.builder()
            .severity(AlertSeverity.WARNING)
            .title("Avisos de Consistência CQRS")
            .message(String.join("\n", warnings))
            .timestamp(Instant.now())
            .build();
        
        notificationService.sendAlert(alert);
    }
}
```

---

## 🛠️ **TRATAMENTO DE FALHAS**

### **🔄 Recovery de Projeções**

```java
// Localização: cqrs/recovery/ProjectionRecoveryService.java
@Component
public class ProjectionRecoveryService {
    
    /**
     * Recupera projeção com falha
     */
    public RecoveryResult recoverProjection(String projectionName, RecoveryStrategy strategy) {
        
        RecoveryResult result = new RecoveryResult();
        result.setProjectionName(projectionName);
        result.setStrategy(strategy);
        result.setStartTime(Instant.now());
        
        try {
            ProjectionTracker tracker = trackerRepository
                .findByProjectionName(projectionName)
                .orElseThrow(() -> new ProjectionNotFoundException(projectionName));
            
            switch (strategy) {
                case RESTART_FROM_LAST_POSITION:
                    result = restartFromLastPosition(tracker);
                    break;
                    
                case REBUILD_FROM_BEGINNING:
                    result = rebuildFromBeginning(tracker);
                    break;
                    
                case REBUILD_FROM_SNAPSHOT:
                    result = rebuildFromSnapshot(tracker);
                    break;
                    
                default:
                    throw new IllegalArgumentException("Estratégia não suportada: " + strategy);
            }
            
        } catch (Exception e) {
            result.setStatus(RecoveryStatus.FAILED);
            result.setErrorMessage(e.getMessage());
        }
        
        result.setEndTime(Instant.now());
        return result;
    }
    
    private RecoveryResult restartFromLastPosition(ProjectionTracker tracker) {
        
        // Simplesmente reativar a projeção
        tracker.resume();
        tracker.setLastErrorMessage(null);
        tracker.setLastErrorAt(null);
        trackerRepository.save(tracker);
        
        return RecoveryResult.success(tracker.getProjectionName(), RecoveryStrategy.RESTART_FROM_LAST_POSITION);
    }
    
    private RecoveryResult rebuildFromBeginning(ProjectionTracker tracker) {
        
        // Resetar posição para o início
        tracker.setLastProcessedEventId(0L);
        tracker.setEventsProcessed(0L);
        tracker.setEventsFailed(0L);
        tracker.setStatus(ProjectionStatus.ACTIVE);
        tracker.setLastErrorMessage(null);
        tracker.setLastErrorAt(null);
        
        trackerRepository.save(tracker);
        
        // Limpar dados da projeção (se necessário)
        clearProjectionData(tracker.getProjectionName());
        
        return RecoveryResult.success(tracker.getProjectionName(), RecoveryStrategy.REBUILD_FROM_BEGINNING);
    }
    
    private RecoveryResult rebuildFromSnapshot(ProjectionTracker tracker) {
        
        // Buscar snapshot mais recente
        Optional<ProjectionSnapshot> snapshot = snapshotRepository
            .findLatestSnapshot(tracker.getProjectionName());
        
        if (snapshot.isEmpty()) {
            // Fallback para rebuild completo
            return rebuildFromBeginning(tracker);
        }
        
        // Restaurar a partir do snapshot
        ProjectionSnapshot snap = snapshot.get();
        tracker.setLastProcessedEventId(snap.getLastEventId());
        tracker.setStatus(ProjectionStatus.ACTIVE);
        tracker.setLastErrorMessage(null);
        tracker.setLastErrorAt(null);
        
        trackerRepository.save(tracker);
        
        // Restaurar dados da projeção
        restoreProjectionFromSnapshot(tracker.getProjectionName(), snap);
        
        return RecoveryResult.success(tracker.getProjectionName(), RecoveryStrategy.REBUILD_FROM_SNAPSHOT);
    }
    
    /**
     * Recovery automático para projeções com falha persistente
     */
    @Scheduled(fixedRate = 1800000) // A cada 30 minutos
    public void autoRecoveryCheck() {
        
        // Buscar projeções com erro há mais de 1 hora
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        
        List<ProjectionTracker> failedProjections = trackerRepository
            .findByStatusAndLastErrorAtIsNotNull(ProjectionStatus.ERROR)
            .stream()
            .filter(p -> p.getLastErrorAt() != null && p.getLastErrorAt().isBefore(oneHourAgo))
            .collect(Collectors.toList());
        
        for (ProjectionTracker projection : failedProjections) {
            
            log.info("Tentando recovery automático para projeção: {}", projection.getProjectionName());
            
            // Tentar restart simples primeiro
            RecoveryResult result = recoverProjection(
                projection.getProjectionName(), 
                RecoveryStrategy.RESTART_FROM_LAST_POSITION
            );
            
            if (result.getStatus() == RecoveryStatus.SUCCESS) {
                log.info("Recovery automático bem-sucedido para: {}", projection.getProjectionName());
            } else {
                log.warn("Recovery automático falhou para: {}. Intervenção manual necessária.", 
                        projection.getProjectionName());
            }
        }
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Monitorar e corrigir inconsistências

#### **Passo 1: Verificar Consistência**
```bash
# Verificar métricas de lag
curl http://localhost:8083/api/v1/actuator/metrics/cqrs.lag.overall
curl http://localhost:8083/api/v1/actuator/metrics/cqrs.projections.active

# Relatório de consistência
curl http://localhost:8083/api/v1/actuator/cqrs/consistency | jq
```

#### **Passo 2: Simular Inconsistência**
```java
@Test
public void simularInconsistencia() {
    // Pausar uma projeção
    projectionController.pauseProjection("SinistroProjectionHandler");
    
    // Gerar eventos no Command Side
    for (int i = 0; i < 100; i++) {
        commandBus.send(new CriarSinistroCommand(...));
    }
    
    // Verificar lag
    ConsistencyReport report = consistencyMonitor.checkConsistency();
    assertThat(report.hasInconsistencies()).isTrue();
    
    // Reativar projeção
    projectionController.resumeProjection("SinistroProjectionHandler");
    
    // Aguardar sincronização
    Thread.sleep(5000);
    
    // Verificar se sincronizou
    ConsistencyReport newReport = consistencyMonitor.checkConsistency();
    assertThat(newReport.hasInconsistencies()).isFalse();
}
```

#### **Passo 3: Testar Recovery**
```bash
# Forçar erro em projeção
curl -X POST http://localhost:8083/api/v1/actuator/projections/SinistroProjectionHandler/force-error

# Verificar status
curl http://localhost:8083/api/v1/actuator/projections/SinistroProjectionHandler | jq

# Executar recovery
curl -X POST http://localhost:8083/api/v1/actuator/projections/SinistroProjectionHandler/recover \
  -H "Content-Type: application/json" \
  -d '{"strategy": "RESTART_FROM_LAST_POSITION"}'
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Monitorar** consistência eventual entre Command e Query
2. **Calcular** lag e throughput de projeções
3. **Implementar** sincronização automática
4. **Configurar** alertas de inconsistência
5. **Executar** recovery de projeções com falha

### **❓ Perguntas para Reflexão:**

1. Como balancear consistência vs performance?
2. Quando aceitar inconsistência eventual?
3. Como detectar problemas antes que afetem usuários?
4. Qual estratégia de recovery usar em cada situação?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 5**, vamos finalizar com:
- Padrões avançados de CQRS
- Otimizações de performance
- Troubleshooting de problemas comuns
- Boas práticas e lições aprendidas

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 48 minutos  
**📋 Pré-requisitos:** CQRS Partes 1-3