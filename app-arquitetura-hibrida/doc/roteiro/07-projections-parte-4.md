# 📊 PROJECTION HANDLERS - PARTE 4: REBUILD E CONSISTÊNCIA
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Compreender os mecanismos de rebuild de projeções, verificação de consistência e recuperação de falhas no sistema de projeções.

---

## 🔄 **CONCEITOS DE REBUILD**

### **📋 Por que Rebuild é Necessário?**

**Cenários Comuns:**
- ✅ **Nova projeção**: Processar todo o histórico de eventos
- ✅ **Mudança de schema**: Atualizar estrutura da projeção
- ✅ **Correção de bugs**: Reprocessar eventos com lógica corrigida
- ✅ **Inconsistências**: Resolver divergências entre write e read side
- ✅ **Recuperação de falhas**: Restaurar projeções corrompidas

**Tipos de Rebuild:**
```
Rebuild Types
├── Full Rebuild (Completo)
│   ├── Apaga toda a projeção
│   └── Reprocessa todos os eventos
└── Incremental Rebuild (Incremental)
    ├── Mantém dados existentes
    └── Processa apenas eventos faltantes
```

---

## 🏗️ **ARQUITETURA DO SISTEMA DE REBUILD**

### **📐 Componentes Principais**

```
Rebuild System
├── ProjectionRebuilder (Coordenador principal)
├── ProjectionConsistencyChecker (Verificador de consistência)
├── ProjectionMaintenanceScheduler (Agendador automático)
└── RebuildResult (Resultado e métricas)
```

### **🎯 ProjectionRebuilder - Implementação**

**Localização**: `com.seguradora.hibrida.projection.rebuild.ProjectionRebuilder`

```java
@Component
@Slf4j
public class ProjectionRebuilder {
    
    private final EventStore eventStore;
    private final ProjectionRegistry projectionRegistry;
    private final ProjectionTrackerRepository trackerRepository;
    private final ProjectionEventProcessor eventProcessor;
    private final ProjectionRebuildProperties properties;
    private final TaskExecutor rebuildTaskExecutor;
    
    private final Map<String, CompletableFuture<RebuildResult>> activeRebuilds = new ConcurrentHashMap<>();
    
    /**
     * Executa rebuild completo de uma projeção.
     */
    public CompletableFuture<RebuildResult> rebuildProjection(String projectionName) {
        return rebuildProjection(projectionName, false);
    }
    
    /**
     * Executa rebuild incremental de uma projeção.
     */
    public CompletableFuture<RebuildResult> rebuildProjectionIncremental(String projectionName) {
        return rebuildProjection(projectionName, true);
    }
    
    private CompletableFuture<RebuildResult> rebuildProjection(String projectionName, boolean incremental) {
        // Verificar se já existe rebuild em andamento
        if (activeRebuilds.containsKey(projectionName)) {
            log.warn("Rebuild já em andamento para projeção: {}", projectionName);
            return activeRebuilds.get(projectionName);
        }
        
        // Buscar handler da projeção
        ProjectionHandler<? extends DomainEvent> handler = 
            projectionRegistry.getHandler(projectionName);
        
        if (handler == null) {
            return CompletableFuture.completedFuture(
                RebuildResult.failure(projectionName, 
                    incremental ? RebuildType.INCREMENTAL : RebuildType.FULL,
                    0, 0, 0, "Handler não encontrado"));
        }
        
        // Executar rebuild assíncrono
        CompletableFuture<RebuildResult> rebuildFuture = CompletableFuture
            .supplyAsync(() -> executeRebuild(handler, incremental), rebuildTaskExecutor)
            .whenComplete((result, throwable) -> {
                activeRebuilds.remove(projectionName);
                if (throwable != null) {
                    log.error("Erro durante rebuild da projeção: {}", projectionName, throwable);
                }
            });
        
        activeRebuilds.put(projectionName, rebuildFuture);
        return rebuildFuture;
    }
    
    private RebuildResult executeRebuild(ProjectionHandler<? extends DomainEvent> handler, 
                                       boolean incremental) {
        
        String projectionName = handler.getProjectionName();
        RebuildType type = incremental ? RebuildType.INCREMENTAL : RebuildType.FULL;
        long startTime = System.currentTimeMillis();
        
        log.info("Iniciando {} rebuild da projeção: {}", 
            incremental ? "incremental" : "full", projectionName);
        
        try {
            ProjectionTracker tracker = getOrCreateTracker(projectionName);
            
            // Pausar projeção durante rebuild
            tracker.pause();
            trackerRepository.save(tracker);
            
            long fromEventId = incremental ? tracker.getLastProcessedEventId() + 1 : 1L;
            long processed = 0;
            long failed = 0;
            
            // Buscar eventos para reprocessamento
            List<DomainEvent> events = loadEventsForRebuild(handler.getEventType(), fromEventId);
            
            log.info("Encontrados {} eventos para reprocessar [{}]", events.size(), projectionName);
            
            // Processar eventos em lotes
            for (int i = 0; i < events.size(); i += properties.getBatchSize()) {
                int endIndex = Math.min(i + properties.getBatchSize(), events.size());
                List<DomainEvent> batch = events.subList(i, endIndex);
                
                for (DomainEvent event : batch) {
                    try {
                        // Processar evento sem tracking (modo rebuild)
                        processWithoutTracking(event, handler);
                        processed++;
                        
                        // Atualizar posição do tracker
                        tracker.updatePosition(event.getEventId());
                        
                    } catch (Exception e) {
                        failed++;
                        log.error("Erro ao processar evento durante rebuild: {} [{}]", 
                            event.getEventType(), projectionName, e);
                        
                        if (failed > properties.getMaxErrorsBeforeStop()) {
                            throw new ProjectionRebuildException(projectionName, type,
                                "Muitos erros durante rebuild: " + failed);
                        }
                    }
                }
                
                // Salvar progresso do lote
                trackerRepository.save(tracker);
                
                log.debug("Processado lote {}/{} - {} eventos [{}]", 
                    (i / properties.getBatchSize()) + 1, 
                    (events.size() / properties.getBatchSize()) + 1,
                    batch.size(), projectionName);
            }
            
            // Reativar projeção
            tracker.resume();
            trackerRepository.save(tracker);
            
            long duration = System.currentTimeMillis() - startTime;
            
            log.info("Rebuild concluído: projection={}, type={}, processed={}, failed={}, duration={}ms",
                projectionName, type, processed, failed, duration);
            
            return RebuildResult.success(projectionName, type, processed, failed, duration);
            
        } catch (Exception e) {
            long duration = System.currentTimeMillis() - startTime;
            log.error("Falha no rebuild da projeção: {} [{}]", projectionName, type, e);
            
            // Tentar reativar projeção mesmo em caso de erro
            try {
                ProjectionTracker tracker = getOrCreateTracker(projectionName);
                tracker.resume();
                trackerRepository.save(tracker);
            } catch (Exception resumeError) {
                log.error("Erro ao reativar projeção após falha no rebuild: {}", 
                    projectionName, resumeError);
            }
            
            return RebuildResult.failure(projectionName, type, 0, 0, duration, e.getMessage());
        }
    }
    
    /**
     * Busca projeções que precisam de rebuild automático.
     */
    public List<ProjectionTracker> findProjectionsNeedingRebuild() {
        Long maxEventId = eventStore.getCurrentMaxEventId();
        if (maxEventId == null) {
            return Collections.emptyList();
        }
        
        return trackerRepository.findProjectionsNeedingRebuild(
            maxEventId,
            properties.getLagThresholdForRebuild(),
            (long) (properties.getErrorThresholdForRebuild() * 100)
        );
    }
    
    /**
     * Executa rebuild automático de projeções que precisam.
     */
    public CompletableFuture<List<RebuildResult>> rebuildProjectionsNeedingRebuild() {
        List<ProjectionTracker> projectionsNeedingRebuild = findProjectionsNeedingRebuild();
        
        if (projectionsNeedingRebuild.isEmpty()) {
            log.info("Nenhuma projeção precisa de rebuild automático");
            return CompletableFuture.completedFuture(Collections.emptyList());
        }
        
        log.info("Encontradas {} projeções que precisam de rebuild", 
            projectionsNeedingRebuild.size());
        
        List<CompletableFuture<RebuildResult>> rebuildFutures = projectionsNeedingRebuild.stream()
            .limit(properties.getMaxConcurrentRebuilds())
            .map(tracker -> {
                boolean useIncremental = shouldUseIncrementalRebuild(tracker);
                return useIncremental ? 
                    rebuildProjectionIncremental(tracker.getProjectionName()) :
                    rebuildProjection(tracker.getProjectionName());
            })
            .collect(Collectors.toList());
        
        return CompletableFuture.allOf(rebuildFutures.toArray(new CompletableFuture[0]))
            .thenApply(v -> rebuildFutures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList()));
    }
    
    private boolean shouldUseIncrementalRebuild(ProjectionTracker tracker) {
        // Usar incremental se:
        // 1. Lag não é muito alto
        // 2. Taxa de erro não é muito alta
        // 3. Não há muito tempo sem processamento
        
        Long maxEventId = eventStore.getCurrentMaxEventId();
        if (maxEventId == null) return false;
        
        long lag = tracker.calculateLag(maxEventId);
        double errorRate = tracker.getErrorRate();
        
        return lag < properties.getLagThresholdForFullRebuild() &&
               errorRate < properties.getErrorRateThresholdForFullRebuild();
    }
    
    private List<DomainEvent> loadEventsForRebuild(Class<? extends DomainEvent> eventType, 
                                                  long fromEventId) {
        // Implementar carregamento otimizado de eventos
        // Pode usar paginação para eventos muito grandes
        return eventStore.loadEventsByTypeFromId(eventType, fromEventId);
    }
    
    private void processWithoutTracking(DomainEvent event, 
                                      ProjectionHandler<DomainEvent> handler) {
        // Processar evento diretamente, sem atualizar tracker
        handler.handle(event);
    }
    
    private ProjectionTracker getOrCreateTracker(String projectionName) {
        return trackerRepository.findByProjectionName(projectionName)
            .orElseGet(() -> {
                ProjectionTracker newTracker = new ProjectionTracker();
                newTracker.setProjectionName(projectionName);
                newTracker.setStatus(ProjectionStatus.ACTIVE);
                newTracker.setLastProcessedEventId(0L);
                return trackerRepository.save(newTracker);
            });
    }
}
```

---

## 🔍 **VERIFICAÇÃO DE CONSISTÊNCIA**

### **📊 ProjectionConsistencyChecker**

```java
@Component
@Slf4j
public class ProjectionConsistencyChecker {
    
    private final ProjectionTrackerRepository trackerRepository;
    private final EventStoreRepository eventStoreRepository;
    private final ProjectionConsistencyProperties properties;
    
    /**
     * Verifica consistência de todas as projeções.
     */
    public ConsistencyReport checkAllProjections() {
        log.info("Iniciando verificação de consistência de todas as projeções");
        
        List<ProjectionTracker> allProjections = trackerRepository.findAll();
        Long maxEventId = eventStoreRepository.findMaxEventId();
        
        List<ConsistencyIssue> allIssues = new ArrayList<>();
        
        // Verificar cada projeção individualmente
        for (ProjectionTracker tracker : allProjections) {
            List<ConsistencyIssue> projectionIssues = 
                checkProjectionConsistency(tracker, maxEventId);
            allIssues.addAll(projectionIssues);
        }
        
        // Verificar projeções órfãs (sem handler)
        List<ConsistencyIssue> orphanIssues = checkOrphanedProjections(allProjections);
        allIssues.addAll(orphanIssues);
        
        ConsistencyReport report = new ConsistencyReport(allIssues, allProjections.size());
        
        log.info("Verificação de consistência concluída: {} issues encontradas", 
            allIssues.size());
        
        // Tratar issues críticas automaticamente se configurado
        if (properties.isAutoRebuildOnPersistentError()) {
            handleCriticalIssues(report.getCriticalIssues());
        }
        
        return report;
    }
    
    /**
     * Verifica consistência de uma projeção específica.
     */
    public List<ConsistencyIssue> checkProjectionConsistency(String projectionName) {
        ProjectionTracker tracker = trackerRepository.findByProjectionName(projectionName)
            .orElse(null);
        
        if (tracker == null) {
            return List.of(ConsistencyIssue.persistentError(projectionName, 
                "Tracker não encontrado", 0));
        }
        
        Long maxEventId = eventStoreRepository.findMaxEventId();
        return checkProjectionConsistency(tracker, maxEventId);
    }
    
    private List<ConsistencyIssue> checkProjectionConsistency(ProjectionTracker tracker, 
                                                             Long maxEventId) {
        List<ConsistencyIssue> issues = new ArrayList<>();
        String projectionName = tracker.getProjectionName();
        
        // 1. Verificar lag alto
        if (maxEventId != null) {
            long lag = tracker.calculateLag(maxEventId);
            if (lag > properties.getMaxAllowedLag()) {
                issues.add(ConsistencyIssue.highLag(projectionName, lag, properties.getMaxAllowedLag()));
            }
            
            // Lag crítico
            if (lag > properties.getCriticalLagThreshold()) {
                issues.add(ConsistencyIssue.highLag(projectionName, lag, properties.getCriticalLagThreshold())
                    .withSeverity(IssueSeverity.CRITICAL));
            }
        }
        
        // 2. Verificar taxa de erro alta
        double errorRate = tracker.getErrorRate();
        if (errorRate > properties.getMaxAllowedErrorRate()) {
            issues.add(ConsistencyIssue.highErrorRate(projectionName, errorRate, 
                properties.getMaxAllowedErrorRate()));
        }
        
        // Taxa de erro crítica
        if (errorRate > properties.getCriticalErrorRate()) {
            issues.add(ConsistencyIssue.highErrorRate(projectionName, errorRate, 
                properties.getCriticalErrorRate()).withSeverity(IssueSeverity.CRITICAL));
        }
        
        // 3. Verificar projeção parada há muito tempo
        if (tracker.getLastProcessedAt() != null) {
            long minutesStale = Duration.between(tracker.getLastProcessedAt(), Instant.now())
                .toMinutes();
            
            if (minutesStale > properties.getStaleThresholdMinutes()) {
                issues.add(ConsistencyIssue.staleProjection(projectionName, minutesStale));
            }
        }
        
        // 4. Verificar erro persistente
        if (tracker.getStatus() == ProjectionStatus.ERROR && 
            tracker.getLastErrorAt() != null) {
            
            long minutesInError = Duration.between(tracker.getLastErrorAt(), Instant.now())
                .toMinutes();
            
            if (minutesInError > properties.getMaxErrorDurationMinutes()) {
                issues.add(ConsistencyIssue.persistentError(projectionName, 
                    tracker.getLastErrorMessage(), minutesInError));
            }
        }
        
        return issues;
    }
    
    private List<ConsistencyIssue> checkOrphanedProjections(List<ProjectionTracker> allProjections) {
        // Verificar se existem trackers para projeções que não têm mais handlers
        Set<String> activeProjectionNames = projectionRegistry.getRegisteredProjectionNames();
        
        return allProjections.stream()
            .filter(tracker -> !activeProjectionNames.contains(tracker.getProjectionName()))
            .filter(tracker -> {
                // Considerar órfã apenas se não foi atualizada há muito tempo
                if (tracker.getUpdatedAt() == null) return true;
                
                long hoursStale = Duration.between(tracker.getUpdatedAt(), Instant.now())
                    .toHours();
                return hoursStale > properties.getOrphanThresholdHours();
            })
            .map(tracker -> ConsistencyIssue.orphanedProjection(tracker.getProjectionName()))
            .collect(Collectors.toList());
    }
    
    private void handleCriticalIssues(List<ConsistencyIssue> criticalIssues) {
        for (ConsistencyIssue issue : criticalIssues) {
            switch (issue.getType()) {
                case HIGH_LAG -> handleHighLagIssue(issue);
                case HIGH_ERROR_RATE -> handleHighErrorRateIssue(issue);
                case PERSISTENT_ERROR -> handlePersistentErrorIssue(issue);
                case STALE_PROJECTION -> handleStaleProjectionIssue(issue);
            }
        }
    }
    
    private void handleHighLagIssue(ConsistencyIssue issue) {
        if (properties.isAutoRestartOnHighLag()) {
            log.warn("Reiniciando projeção com lag alto: {}", issue.getProjectionName());
            // Implementar restart da projeção
        }
    }
    
    private void handleHighErrorRateIssue(ConsistencyIssue issue) {
        if (properties.isAutoPauseOnHighErrorRate()) {
            log.warn("Pausando projeção com alta taxa de erro: {}", issue.getProjectionName());
            pauseProjection(issue.getProjectionName());
        }
    }
    
    private void handlePersistentErrorIssue(ConsistencyIssue issue) {
        if (properties.isAutoRebuildOnPersistentError()) {
            log.warn("Iniciando rebuild para projeção com erro persistente: {}", 
                issue.getProjectionName());
            // Agendar rebuild
            scheduleRebuild(issue.getProjectionName());
        }
    }
    
    private void handleStaleProjectionIssue(ConsistencyIssue issue) {
        if (properties.isAutoRestartOnStale()) {
            log.warn("Reiniciando projeção parada: {}", issue.getProjectionName());
            // Implementar restart
        }
    }
    
    /**
     * Verificação agendada de consistência.
     */
    @Scheduled(fixedRateString = "${projection.consistency.check-interval-seconds:300}000")
    public void scheduledConsistencyCheck() {
        if (!properties.isEnabled()) {
            return;
        }
        
        try {
            ConsistencyReport report = checkAllProjections();
            
            if (report.hasCriticalIssues()) {
                log.error("Encontradas {} issues críticas de consistência", 
                    report.getCriticalIssuesCount());
            }
            
            if (properties.isDetailedLogging()) {
                log.info("Relatório de consistência: {}", report.getSummary());
            }
            
        } catch (Exception e) {
            log.error("Erro durante verificação agendada de consistência", e);
        }
    }
}
```

---

## 📊 **MODELOS DE RESULTADO**

### **🎯 RebuildResult**

```java
public class RebuildResult {
    
    private final String projectionName;
    private final RebuildType type;
    private final boolean success;
    private final long eventsProcessed;
    private final long eventsFailed;
    private final long durationMs;
    private final String errorMessage;
    private final Instant timestamp;
    
    public static RebuildResult success(String projectionName, RebuildType type, 
                                      long processed, long failed, long duration) {
        return new RebuildResult(projectionName, type, true, processed, failed, 
            duration, null, Instant.now());
    }
    
    public static RebuildResult failure(String projectionName, RebuildType type, 
                                      long processed, long failed, long duration, 
                                      String error) {
        return new RebuildResult(projectionName, type, false, processed, failed, 
            duration, error, Instant.now());
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public boolean hasFailed() {
        return !success;
    }
    
    public double getSuccessRate() {
        long total = eventsProcessed + eventsFailed;
        return total > 0 ? (double) eventsProcessed / total : 0.0;
    }
    
    public double getThroughput() {
        return durationMs > 0 ? (double) eventsProcessed / (durationMs / 1000.0) : 0.0;
    }
}

public enum RebuildType {
    FULL("Rebuild Completo"),
    INCREMENTAL("Rebuild Incremental");
    
    private final String displayName;
    
    RebuildType(String displayName) {
        this.displayName = displayName;
    }
    
    public String getDisplayName() {
        return displayName;
    }
}
```

### **📋 ConsistencyReport**

```java
public class ConsistencyReport {
    
    private final List<ConsistencyIssue> issues;
    private final int totalProjections;
    private final Instant timestamp;
    
    public ConsistencyReport(List<ConsistencyIssue> issues, int totalProjections) {
        this.issues = new ArrayList<>(issues);
        this.totalProjections = totalProjections;
        this.timestamp = Instant.now();
    }
    
    public boolean isHealthy() {
        return issues.isEmpty();
    }
    
    public boolean hasCriticalIssues() {
        return issues.stream().anyMatch(ConsistencyIssue::isCritical);
    }
    
    public List<ConsistencyIssue> getCriticalIssues() {
        return issues.stream()
            .filter(ConsistencyIssue::isCritical)
            .collect(Collectors.toList());
    }
    
    public long getCriticalIssuesCount() {
        return getCriticalIssues().size();
    }
    
    public long getHighPriorityIssuesCount() {
        return issues.stream()
            .filter(ConsistencyIssue::isHighPriority)
            .count();
    }
    
    public int getTotalIssues() {
        return issues.size();
    }
    
    public int getHealthyProjectionsCount() {
        return totalProjections - getProjectionsWithIssues().size();
    }
    
    public List<String> getProjectionsWithIssues() {
        return issues.stream()
            .map(ConsistencyIssue::getProjectionName)
            .distinct()
            .collect(Collectors.toList());
    }
    
    public double getHealthScore() {
        if (totalProjections == 0) return 1.0;
        
        int healthyProjections = getHealthyProjectionsCount();
        return (double) healthyProjections / totalProjections;
    }
    
    public Map<IssueSeverity, List<ConsistencyIssue>> getIssuesBySeverity() {
        return issues.stream()
            .collect(Collectors.groupingBy(ConsistencyIssue::getSeverity));
    }
    
    public Map<IssueType, List<ConsistencyIssue>> getIssuesByType() {
        return issues.stream()
            .collect(Collectors.groupingBy(ConsistencyIssue::getType));
    }
    
    public String getSummary() {
        return String.format(
            "Consistency Report: %d/%d projections healthy (%.1f%%), %d issues (%d critical)",
            getHealthyProjectionsCount(), totalProjections, getHealthScore() * 100,
            getTotalIssues(), getCriticalIssuesCount()
        );
    }
}
```

---

## ⚙️ **CONFIGURAÇÃO E PROPRIEDADES**

### **📋 Propriedades de Rebuild**

```yaml
projection:
  rebuild:
    enabled: true
    batch-size: 100
    max-concurrent-rebuilds: 2
    max-errors-before-stop: 10
    timeout-seconds: 3600
    auto-check-interval-seconds: 3600
    
    # Thresholds para rebuild automático
    lag-threshold-for-rebuild: 1000
    lag-threshold-for-full-rebuild: 10000
    error-threshold-for-rebuild: 0.05
    error-rate-threshold-for-full-rebuild: 0.20
    
    # Configurações de retry
    auto-retry-after-failure: true
    retry-delay-seconds: 300
    auto-pause-on-errors: true
    detailed-logging: true

  consistency:
    enabled: true
    check-interval-seconds: 300
    max-allowed-lag: 500
    critical-lag-threshold: 5000
    max-allowed-error-rate: 0.02
    critical-error-rate: 0.10
    stale-threshold-minutes: 60
    max-error-duration-minutes: 30
    orphan-threshold-hours: 24
    
    # Ações automáticas
    auto-restart-on-high-lag: true
    auto-pause-on-high-error-rate: true
    auto-rebuild-on-persistent-error: true
    auto-restart-on-stale: true
    alerts-enabled: true
    detailed-logging: false
    health-score-alert-threshold: 0.8
```

---

## 🎯 **AGENDAMENTO AUTOMÁTICO**

### **📅 ProjectionMaintenanceScheduler**

```java
@Component
@ConditionalOnProperty(name = "projection.rebuild.enabled", havingValue = "true")
public class ProjectionMaintenanceScheduler {
    
    private final ProjectionRebuilder rebuilder;
    private final ProjectionConsistencyChecker consistencyChecker;
    private final ProjectionRebuildProperties properties;
    
    private ConsistencyReport lastConsistencyReport;
    private Instant lastConsistencyCheck;
    private Instant lastMaintenanceRun;
    
    /**
     * Verificação automática de projeções que precisam de rebuild.
     */
    @Scheduled(fixedRateString = "${projection.rebuild.auto-check-interval-seconds:3600}000")
    public void autoRebuildProjections() {
        if (!properties.isEnabled()) {
            return;
        }
        
        try {
            log.info("Iniciando verificação automática de rebuild");
            
            CompletableFuture<List<RebuildResult>> rebuildFuture = 
                rebuilder.rebuildProjectionsNeedingRebuild();
            
            List<RebuildResult> results = rebuildFuture.get(
                properties.getTimeoutSeconds(), TimeUnit.SECONDS);
            
            if (!results.isEmpty()) {
                log.info("Rebuild automático concluído: {} projeções processadas", 
                    results.size());
                
                long successful = results.stream()
                    .mapToLong(r -> r.isSuccess() ? 1 : 0)
                    .sum();
                
                log.info("Resultados do rebuild automático: {}/{} sucessos", 
                    successful, results.size());
            }
            
        } catch (Exception e) {
            log.error("Erro durante rebuild automático", e);
        }
    }
    
    /**
     * Verificação periódica de consistência.
     */
    @Scheduled(fixedRateString = "${projection.consistency.check-interval-seconds:300}000")
    public void periodicConsistencyCheck() {
        try {
            lastConsistencyReport = consistencyChecker.checkAllProjections();
            lastConsistencyCheck = Instant.now();
            
            if (lastConsistencyReport.hasCriticalIssues()) {
                log.warn("Encontradas {} issues críticas de consistência", 
                    lastConsistencyReport.getCriticalIssuesCount());
            }
            
        } catch (Exception e) {
            log.error("Erro durante verificação de consistência", e);
        }
    }
    
    /**
     * Manutenção diária geral.
     */
    @Scheduled(cron = "0 0 2 * * *") // 2:00 AM todos os dias
    public void dailyMaintenance() {
        try {
            log.info("Iniciando manutenção diária das projeções");
            
            // Limpeza de dados antigos
            cleanupOldData();
            
            // Otimização de performance
            performanceOptimization();
            
            // Relatório de saúde
            generateHealthReport(lastConsistencyReport);
            
            lastMaintenanceRun = Instant.now();
            
            log.info("Manutenção diária concluída");
            
        } catch (Exception e) {
            log.error("Erro durante manutenção diária", e);
        }
    }
    
    private void cleanupOldData() {
        // Implementar limpeza de dados antigos
        log.info("Executando limpeza de dados antigos");
    }
    
    private void performanceOptimization() {
        // Implementar otimizações de performance
        log.info("Executando otimizações de performance");
    }
    
    private void generateHealthReport(ConsistencyReport report) {
        if (report == null) return;
        
        log.info("=== RELATÓRIO DE SAÚDE DAS PROJEÇÕES ===");
        log.info("Health Score: {:.1f}%", report.getHealthScore() * 100);
        log.info("Projeções saudáveis: {}/{}", 
            report.getHealthyProjectionsCount(), report.getTotalProjections());
        log.info("Issues encontradas: {} (críticas: {})", 
            report.getTotalIssues(), report.getCriticalIssuesCount());
        log.info("==========================================");
    }
    
    // Getters para monitoramento
    public ConsistencyReport getLastConsistencyReport() {
        return lastConsistencyReport;
    }
    
    public Instant getLastConsistencyCheck() {
        return lastConsistencyCheck;
    }
    
    public Instant getLastMaintenanceRun() {
        return lastMaintenanceRun;
    }
    
    public boolean isSystemHealthy() {
        return lastConsistencyReport != null && 
               !lastConsistencyReport.hasCriticalIssues();
    }
    
    public double getCurrentHealthScore() {
        return lastConsistencyReport != null ? 
               lastConsistencyReport.getHealthScore() : 0.0;
    }
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Verificação Customizada**

Crie uma verificação de consistência customizada que:

1. **Verifique integridade referencial** entre projeções
2. **Detecte dados órfãos** ou inconsistentes
3. **Sugira ações corretivas** automaticamente
4. **Gere relatório detalhado** com métricas

**Template:**
```java
@Component
public class CustomConsistencyChecker {
    
    public List<ConsistencyIssue> checkReferentialIntegrity() {
        // Sua implementação aqui
        return new ArrayList<>();
    }
    
    public List<ConsistencyIssue> checkDataOrphans() {
        // Sua implementação aqui
        return new ArrayList<>();
    }
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.projection.rebuild`
- **Configuração**: `projection-rebuild.yml`
- **Consistência**: `ProjectionConsistencyChecker`
- **Agendamento**: `ProjectionMaintenanceScheduler`

---

**📍 Próxima Parte**: [Projections - Parte 5: Monitoramento e Troubleshooting](./07-projections-parte-5.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Rebuild e verificação de consistência  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Implementação de verificação customizada