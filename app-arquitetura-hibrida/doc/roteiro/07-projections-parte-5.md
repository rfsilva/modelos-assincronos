# 📊 PROJECTION HANDLERS - PARTE 5: MONITORAMENTO E TROUBLESHOOTING
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar técnicas de monitoramento, debugging e troubleshooting do sistema de projeções, garantindo operação confiável em produção.

---

## 📊 **MONITORAMENTO ABRANGENTE**

### **📈 Métricas Essenciais**

**Categorias de Métricas:**
```
Projection Metrics
├── Performance (Latência, throughput)
├── Health (Status, lag, error rate)
├── Business (Dados processados, consistência)
└── Infrastructure (Recursos, conectividade)
```

### **🎯 Implementação de Métricas**

**Localização**: `com.seguradora.hibrida.projection.metrics.ProjectionMetrics`

```java
@Component
public class ProjectionMetrics implements MeterBinder {
    
    private final ProjectionTrackerRepository trackerRepository;
    private final EventStoreRepository eventStoreRepository;
    private final MeterRegistry meterRegistry;
    
    // Gauges para métricas em tempo real
    private Gauge totalProjectionsGauge;
    private Gauge activeProjectionsGauge;
    private Gauge errorProjectionsGauge;
    private Gauge overallLagGauge;
    private Gauge healthScoreGauge;
    
    // Timers para performance
    private Timer projectionProcessingTimer;
    private Timer consistencyCheckTimer;
    private Timer rebuildTimer;
    
    // Counters para eventos
    private Counter eventsProcessedCounter;
    private Counter eventsFailedCounter;
    private Counter rebuildsExecutedCounter;
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        
        // === MÉTRICAS DE SAÚDE ===
        
        totalProjectionsGauge = Gauge.builder("projection.total")
            .description("Total number of registered projections")
            .register(registry, this, ProjectionMetrics::getTotalProjections);
        
        activeProjectionsGauge = Gauge.builder("projection.active")
            .description("Number of active projections")
            .register(registry, this, ProjectionMetrics::getActiveProjections);
        
        errorProjectionsGauge = Gauge.builder("projection.error")
            .description("Number of projections in error state")
            .register(registry, this, ProjectionMetrics::getErrorProjections);
        
        Gauge.builder("projection.stale")
            .description("Number of stale projections")
            .register(registry, this, ProjectionMetrics::getStaleProjections);
        
        // === MÉTRICAS DE PERFORMANCE ===
        
        overallLagGauge = Gauge.builder("projection.lag.overall")
            .description("Overall lag across all projections")
            .register(registry, this, ProjectionMetrics::getOverallLag);
        
        Gauge.builder("projection.throughput")
            .description("Overall projection throughput (events/sec)")
            .register(registry, this, ProjectionMetrics::getProjectionsThroughput);
        
        Gauge.builder("projection.error_rate")
            .description("Overall projection error rate")
            .register(registry, this, ProjectionMetrics::getProjectionsErrorRate);
        
        // === MÉTRICAS DE QUALIDADE ===
        
        healthScoreGauge = Gauge.builder("projection.health_score")
            .description("Overall health score (0-1)")
            .register(registry, this, ProjectionMetrics::getHealthScore);
        
        Gauge.builder("projection.lag.estimated_seconds")
            .description("Estimated lag in seconds")
            .register(registry, this, ProjectionMetrics::getEstimatedLagSeconds);
        
        // === TIMERS ===
        
        projectionProcessingTimer = Timer.builder("projection.processing.time")
            .description("Time taken to process projection events")
            .register(registry);
        
        consistencyCheckTimer = Timer.builder("projection.consistency.check.time")
            .description("Time taken for consistency checks")
            .register(registry);
        
        rebuildTimer = Timer.builder("projection.rebuild.time")
            .description("Time taken for projection rebuilds")
            .register(registry);
        
        // === COUNTERS ===
        
        eventsProcessedCounter = Counter.builder("projection.events.processed")
            .description("Total events processed by projections")
            .register(registry);
        
        eventsFailedCounter = Counter.builder("projection.events.failed")
            .description("Total events failed in projections")
            .register(registry);
        
        rebuildsExecutedCounter = Counter.builder("projection.rebuilds.executed")
            .description("Total projection rebuilds executed")
            .register(registry);
        
        // === MÉTRICAS POR PROJEÇÃO ===
        registerPerProjectionMetrics(registry);
    }
    
    private void registerPerProjectionMetrics(MeterRegistry registry) {
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        Long maxEventId = eventStoreRepository.findMaxEventId();
        
        for (ProjectionTracker tracker : trackers) {
            String projectionName = tracker.getProjectionName();
            
            // Posição da projeção
            Gauge.builder("projection.position")
                .tag("projection", projectionName)
                .description("Current position of projection")
                .register(registry, tracker, t -> t.getLastProcessedEventId());
            
            // Lag específico da projeção
            Gauge.builder("projection.lag")
                .tag("projection", projectionName)
                .description("Current lag of projection")
                .register(registry, tracker, t -> calculateProjectionLag(t, maxEventId));
            
            // Taxa de erro específica
            Gauge.builder("projection.error_rate")
                .tag("projection", projectionName)
                .description("Error rate of specific projection")
                .register(registry, tracker, ProjectionTracker::getErrorRate);
            
            // Throughput específico
            Gauge.builder("projection.throughput")
                .tag("projection", projectionName)
                .description("Throughput of specific projection")
                .register(registry, tracker, t -> calculateProjectionThroughput(t));
            
            // Status da projeção
            Gauge.builder("projection.status")
                .tag("projection", projectionName)
                .tag("status", tracker.getStatus().name())
                .description("Status of projection (1=active, 0=inactive)")
                .register(registry, tracker, t -> t.getStatus() == ProjectionStatus.ACTIVE ? 1 : 0);
        }
    }
    
    // === MÉTODOS DE CÁLCULO ===
    
    public double getTotalProjections() {
        return trackerRepository.count();
    }
    
    public double getActiveProjections() {
        return trackerRepository.countByStatus(ProjectionStatus.ACTIVE);
    }
    
    public double getErrorProjections() {
        return trackerRepository.countByStatus(ProjectionStatus.ERROR);
    }
    
    public double getStaleProjections() {
        Instant threshold = Instant.now().minus(1, ChronoUnit.HOURS);
        return trackerRepository.findStaleProjections(threshold).size();
    }
    
    public double getOverallLag() {
        Long maxEventId = eventStoreRepository.findMaxEventId();
        if (maxEventId == null) return 0;
        
        List<ProjectionTracker> activeProjections = 
            trackerRepository.findByStatus(ProjectionStatus.ACTIVE);
        
        return activeProjections.stream()
            .mapToLong(tracker -> tracker.calculateLag(maxEventId))
            .average()
            .orElse(0.0);
    }
    
    public double getProjectionsThroughput() {
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        return trackers.stream()
            .mapToDouble(this::calculateProjectionThroughput)
            .sum();
    }
    
    public double getProjectionsErrorRate() {
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        
        double totalEvents = trackers.stream()
            .mapToLong(t -> t.getEventsProcessed() + t.getEventsFailed())
            .sum();
        
        double totalErrors = trackers.stream()
            .mapToLong(ProjectionTracker::getEventsFailed)
            .sum();
        
        return totalEvents > 0 ? totalErrors / totalEvents : 0.0;
    }
    
    public double getHealthScore() {
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        if (trackers.isEmpty()) return 1.0;
        
        long healthyProjections = trackers.stream()
            .mapToLong(tracker -> tracker.isHealthy() ? 1 : 0)
            .sum();
        
        return (double) healthyProjections / trackers.size();
    }
    
    public double getEstimatedLagSeconds() {
        // Estimar lag em segundos baseado na taxa de eventos
        double avgLag = getOverallLag();
        double avgThroughput = getProjectionsThroughput();
        
        return avgThroughput > 0 ? avgLag / avgThroughput : 0.0;
    }
    
    private double calculateProjectionLag(ProjectionTracker tracker, Long maxEventId) {
        return maxEventId != null ? tracker.calculateLag(maxEventId) : 0;
    }
    
    private double calculateProjectionThroughput(ProjectionTracker tracker) {
        if (tracker.getCreatedAt() == null) return 0.0;
        
        long durationSeconds = Duration.between(tracker.getCreatedAt(), Instant.now()).getSeconds();
        return durationSeconds > 0 ? (double) tracker.getEventsProcessed() / durationSeconds : 0.0;
    }
    
    // === MÉTODOS PARA INSTRUMENTAÇÃO ===
    
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopProcessingTimer(Timer.Sample sample, String projectionName) {
        sample.stop(Timer.builder("projection.processing.time")
            .tag("projection", projectionName)
            .register(meterRegistry));
    }
    
    public void recordEventProcessed(String projectionName) {
        Counter.builder("projection.events.processed")
            .tag("projection", projectionName)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordEventFailed(String projectionName, String errorType) {
        Counter.builder("projection.events.failed")
            .tag("projection", projectionName)
            .tag("error_type", errorType)
            .register(meterRegistry)
            .increment();
    }
    
    public void recordRebuildExecuted(String projectionName, String rebuildType, boolean success) {
        Counter.builder("projection.rebuilds.executed")
            .tag("projection", projectionName)
            .tag("type", rebuildType)
            .tag("success", String.valueOf(success))
            .register(meterRegistry)
            .increment();
    }
    
    /**
     * Força atualização de todas as métricas.
     */
    public void forceUpdate() {
        // Re-registrar métricas por projeção para capturar mudanças
        registerPerProjectionMetrics(meterRegistry);
    }
    
    /**
     * Atualização periódica das métricas.
     */
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void updateMetrics() {
        try {
            forceUpdate();
        } catch (Exception e) {
            log.error("Erro ao atualizar métricas de projeção", e);
        }
    }
}
```

---

## 🔍 **DASHBOARD E OBSERVABILIDADE**

### **📊 Controller de Monitoramento**

```java
@RestController
@RequestMapping("/api/projections")
@Tag(name = "Projection Monitoring", description = "APIs para monitoramento de projeções")
public class ProjectionController {
    
    private final ProjectionTrackerRepository trackerRepository;
    private final ProjectionConsistencyChecker consistencyChecker;
    private final ProjectionRebuilder rebuilder;
    private final ProjectionMetrics metrics;
    
    /**
     * Dashboard principal com visão geral.
     */
    @GetMapping("/dashboard")
    @Operation(summary = "Obtém dashboard de projeções")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estatísticas gerais
        dashboard.put("totalProjections", metrics.getTotalProjections());
        dashboard.put("activeProjections", metrics.getActiveProjections());
        dashboard.put("errorProjections", metrics.getErrorProjections());
        dashboard.put("staleProjections", metrics.getStaleProjections());
        
        // Métricas de performance
        dashboard.put("overallLag", metrics.getOverallLag());
        dashboard.put("healthScore", metrics.getHealthScore());
        dashboard.put("errorRate", metrics.getProjectionsErrorRate());
        dashboard.put("throughput", metrics.getProjectionsThroughput());
        dashboard.put("estimatedLagSeconds", metrics.getEstimatedLagSeconds());
        
        // Status por projeção
        List<ProjectionTracker> trackers = trackerRepository.findAll();
        List<Map<String, Object>> projectionStatus = trackers.stream()
            .map(this::mapProjectionStatus)
            .collect(Collectors.toList());
        dashboard.put("projections", projectionStatus);
        
        // Últimas verificações
        dashboard.put("lastUpdate", Instant.now());
        
        return ResponseEntity.ok(dashboard);
    }
    
    /**
     * Lista todas as projeções com paginação.
     */
    @GetMapping
    @Operation(summary = "Lista projeções com paginação")
    public ResponseEntity<Page<ProjectionTracker>> listProjections(
            @Parameter(description = "Configuração de paginação")
            @PageableDefault(size = 20, sort = "projectionName") Pageable pageable) {
        
        Page<ProjectionTracker> projections = trackerRepository.findAll(pageable);
        return ResponseEntity.ok(projections);
    }
    
    /**
     * Detalhes de uma projeção específica.
     */
    @GetMapping("/{projectionName}")
    @Operation(summary = "Obtém detalhes de uma projeção")
    public ResponseEntity<ProjectionTracker> getProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        return trackerRepository.findByProjectionName(projectionName)
            .map(ResponseEntity::ok)
            .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Verifica consistência de todas as projeções.
     */
    @GetMapping("/consistency")
    @Operation(summary = "Verifica consistência das projeções")
    public ResponseEntity<ConsistencyReport> checkConsistency() {
        ConsistencyReport report = consistencyChecker.checkAllProjections();
        return ResponseEntity.ok(report);
    }
    
    /**
     * Verifica consistência de uma projeção específica.
     */
    @GetMapping("/{projectionName}/consistency")
    @Operation(summary = "Verifica consistência de uma projeção específica")
    public ResponseEntity<Map<String, Object>> checkProjectionConsistency(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        List<ConsistencyIssue> issues = consistencyChecker.checkProjectionConsistency(projectionName);
        
        Map<String, Object> result = new HashMap<>();
        result.put("projectionName", projectionName);
        result.put("isHealthy", issues.isEmpty());
        result.put("issues", issues);
        result.put("issueCount", issues.size());
        result.put("criticalIssues", issues.stream().filter(ConsistencyIssue::isCritical).count());
        result.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(result);
    }
    
    /**
     * Executa rebuild de uma projeção.
     */
    @PostMapping("/{projectionName}/rebuild")
    @Operation(summary = "Executa rebuild de uma projeção")
    public ResponseEntity<Map<String, Object>> rebuildProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            CompletableFuture<RebuildResult> rebuildFuture = rebuilder.rebuildProjection(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("status", "STARTED");
            response.put("message", "Rebuild iniciado com sucesso");
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("status", "ERROR");
            error.put("message", "Erro ao iniciar rebuild: " + e.getMessage());
            error.put("timestamp", Instant.now());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Executa rebuild incremental de uma projeção.
     */
    @PostMapping("/{projectionName}/rebuild/incremental")
    @Operation(summary = "Executa rebuild incremental de uma projeção")
    public ResponseEntity<Map<String, Object>> rebuildProjectionIncremental(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        try {
            CompletableFuture<RebuildResult> rebuildFuture = 
                rebuilder.rebuildProjectionIncremental(projectionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("projectionName", projectionName);
            response.put("type", "INCREMENTAL");
            response.put("status", "STARTED");
            response.put("message", "Rebuild incremental iniciado com sucesso");
            response.put("timestamp", Instant.now());
            
            return ResponseEntity.accepted().body(response);
            
        } catch (Exception e) {
            Map<String, Object> error = new HashMap<>();
            error.put("projectionName", projectionName);
            error.put("type", "INCREMENTAL");
            error.put("status", "ERROR");
            error.put("message", "Erro ao iniciar rebuild incremental: " + e.getMessage());
            error.put("timestamp", Instant.now());
            
            return ResponseEntity.badRequest().body(error);
        }
    }
    
    /**
     * Pausa uma projeção.
     */
    @PostMapping("/{projectionName}/pause")
    @Operation(summary = "Pausa uma projeção")
    public ResponseEntity<Map<String, Object>> pauseProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        Optional<ProjectionTracker> trackerOpt = trackerRepository.findByProjectionName(projectionName);
        
        if (trackerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ProjectionTracker tracker = trackerOpt.get();
        tracker.pause();
        trackerRepository.save(tracker);
        
        Map<String, Object> response = new HashMap<>();
        response.put("projectionName", projectionName);
        response.put("status", "PAUSED");
        response.put("message", "Projeção pausada com sucesso");
        response.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Resume uma projeção pausada.
     */
    @PostMapping("/{projectionName}/resume")
    @Operation(summary = "Resume uma projeção pausada")
    public ResponseEntity<Map<String, Object>> resumeProjection(
            @Parameter(description = "Nome da projeção", example = "SinistroProjection")
            @PathVariable String projectionName) {
        
        Optional<ProjectionTracker> trackerOpt = trackerRepository.findByProjectionName(projectionName);
        
        if (trackerOpt.isEmpty()) {
            return ResponseEntity.notFound().build();
        }
        
        ProjectionTracker tracker = trackerOpt.get();
        tracker.resume();
        trackerRepository.save(tracker);
        
        Map<String, Object> response = new HashMap<>();
        response.put("projectionName", projectionName);
        response.put("status", "ACTIVE");
        response.put("message", "Projeção reativada com sucesso");
        response.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Health check das projeções.
     */
    @GetMapping("/health")
    @Operation(summary = "Verifica saúde das projeções")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        Map<String, Object> health = new HashMap<>();
        
        double healthScore = metrics.getHealthScore();
        boolean isHealthy = healthScore > 0.8; // 80% threshold
        
        health.put("status", isHealthy ? "UP" : "DOWN");
        health.put("healthScore", healthScore);
        health.put("totalProjections", metrics.getTotalProjections());
        health.put("activeProjections", metrics.getActiveProjections());
        health.put("errorProjections", metrics.getErrorProjections());
        health.put("overallLag", metrics.getOverallLag());
        health.put("timestamp", Instant.now());
        
        HttpStatus status = isHealthy ? HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
        return ResponseEntity.status(status).body(health);
    }
    
    /**
     * Estatísticas detalhadas.
     */
    @GetMapping("/statistics")
    @Operation(summary = "Obtém estatísticas detalhadas")
    public ResponseEntity<Map<String, Object>> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        
        // Métricas gerais
        stats.put("totalProjections", metrics.getTotalProjections());
        stats.put("activeProjections", metrics.getActiveProjections());
        stats.put("errorProjections", metrics.getErrorProjections());
        stats.put("staleProjections", metrics.getStaleProjections());
        
        // Performance
        stats.put("overallLag", metrics.getOverallLag());
        stats.put("healthScore", metrics.getHealthScore());
        stats.put("errorRate", metrics.getProjectionsErrorRate());
        stats.put("throughput", metrics.getProjectionsThroughput());
        stats.put("estimatedLagSeconds", metrics.getEstimatedLagSeconds());
        
        // Distribuição por status
        Map<String, Long> statusDistribution = Arrays.stream(ProjectionStatus.values())
            .collect(Collectors.toMap(
                Enum::name,
                status -> trackerRepository.countByStatus(status)
            ));
        stats.put("statusDistribution", statusDistribution);
        
        // Top projeções com lag
        List<ProjectionTracker> topLagProjections = getTopLagProjections(5);
        stats.put("topLagProjections", topLagProjections.stream()
            .map(this::mapProjectionStatus)
            .collect(Collectors.toList()));
        
        stats.put("timestamp", Instant.now());
        
        return ResponseEntity.ok(stats);
    }
    
    private Map<String, Object> mapProjectionStatus(ProjectionTracker tracker) {
        Map<String, Object> status = new HashMap<>();
        status.put("name", tracker.getProjectionName());
        status.put("status", tracker.getStatus());
        status.put("lastProcessedEventId", tracker.getLastProcessedEventId());
        status.put("lastProcessedAt", tracker.getLastProcessedAt());
        status.put("eventsProcessed", tracker.getEventsProcessed());
        status.put("eventsFailed", tracker.getEventsFailed());
        status.put("errorRate", tracker.getErrorRate());
        status.put("isHealthy", tracker.isHealthy());
        
        // Calcular lag se possível
        Long maxEventId = eventStoreRepository.findMaxEventId();
        if (maxEventId != null) {
            status.put("lag", tracker.calculateLag(maxEventId));
        }
        
        return status;
    }
    
    private List<ProjectionTracker> getTopLagProjections(int limit) {
        Long maxEventId = eventStoreRepository.findMaxEventId();
        if (maxEventId == null) {
            return Collections.emptyList();
        }
        
        return trackerRepository.findProjectionsOrderedByLag(maxEventId)
            .stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
}
```

---

## 🚨 **ALERTAS E NOTIFICAÇÕES**

### **📢 Sistema de Alertas**

```java
@Component
@ConditionalOnProperty(name = "projection.consistency.alerts-enabled", havingValue = "true")
public class ProjectionAlertService {
    
    private final ProjectionConsistencyProperties properties;
    private final NotificationService notificationService;
    
    @EventListener
    public void handleConsistencyIssues(ConsistencyReportEvent event) {
        ConsistencyReport report = event.getReport();
        
        if (report.hasCriticalIssues()) {
            sendCriticalAlert(report);
        }
        
        if (report.getHealthScore() < properties.getHealthScoreAlertThreshold()) {
            sendHealthScoreAlert(report);
        }
    }
    
    private void sendCriticalAlert(ConsistencyReport report) {
        List<ConsistencyIssue> criticalIssues = report.getCriticalIssues();
        
        String message = String.format(
            "🚨 ALERTA CRÍTICO - Projeções com problemas graves:\n" +
            "- Issues críticas: %d\n" +
            "- Health Score: %.1f%%\n" +
            "- Projeções afetadas: %s",
            criticalIssues.size(),
            report.getHealthScore() * 100,
            String.join(", ", report.getProjectionsWithIssues())
        );
        
        notificationService.sendAlert(AlertLevel.CRITICAL, "Projection Critical Issues", message);
    }
    
    private void sendHealthScoreAlert(ConsistencyReport report) {
        String message = String.format(
            "⚠️ ALERTA - Health Score das projeções baixo:\n" +
            "- Health Score atual: %.1f%%\n" +
            "- Threshold: %.1f%%\n" +
            "- Projeções saudáveis: %d/%d",
            report.getHealthScore() * 100,
            properties.getHealthScoreAlertThreshold() * 100,
            report.getHealthyProjectionsCount(),
            report.getTotalProjections()
        );
        
        notificationService.sendAlert(AlertLevel.WARNING, "Projection Health Score Low", message);
    }
}
```

---

## 🔧 **TROUBLESHOOTING GUIDE**

### **❌ Problemas Comuns e Soluções**

#### **1. Projeção com Lag Alto**

**Sintomas:**
- Lag > 1000 eventos
- Consultas retornando dados desatualizados
- Dashboard mostrando métricas antigas

**Diagnóstico:**
```bash
# Verificar lag específico
curl -X GET "http://localhost:8080/api/projections/SinistroProjection" | jq '.lag'

# Verificar últimos eventos processados
curl -X GET "http://localhost:8080/api/projections/SinistroProjection/consistency"
```

**Soluções:**
1. **Rebuild incremental**: `POST /api/projections/{name}/rebuild/incremental`
2. **Verificar performance**: Analisar logs de processamento
3. **Otimizar queries**: Revisar consultas do handler
4. **Aumentar recursos**: Ajustar thread pool

#### **2. Taxa de Erro Alta**

**Sintomas:**
- Error rate > 5%
- Muitos eventos falhando
- Projeção em status ERROR

**Diagnóstico:**
```java
// Verificar logs de erro
@GetMapping("/projections/{name}/errors")
public ResponseEntity<List<String>> getRecentErrors(@PathVariable String name) {
    ProjectionTracker tracker = trackerRepository.findByProjectionName(name).orElse(null);
    if (tracker == null) return ResponseEntity.notFound().build();
    
    // Buscar logs de erro recentes
    List<String> recentErrors = getRecentErrorLogs(name);
    return ResponseEntity.ok(recentErrors);
}
```

**Soluções:**
1. **Analisar logs**: Identificar causa raiz dos erros
2. **Corrigir handler**: Ajustar lógica de processamento
3. **Rebuild completo**: Reprocessar com lógica corrigida
4. **Pausar temporariamente**: Evitar propagação de erros

#### **3. Projeção Travada (Stale)**

**Sintomas:**
- Não processa eventos há > 1 hora
- lastProcessedAt muito antigo
- Status ACTIVE mas sem atividade

**Diagnóstico:**
```sql
-- Verificar última atividade
SELECT projection_name, last_processed_at, 
       EXTRACT(EPOCH FROM (NOW() - last_processed_at))/60 as minutes_stale
FROM projection_tracker 
WHERE last_processed_at < NOW() - INTERVAL '1 hour';
```

**Soluções:**
1. **Restart da projeção**: Pausar e reativar
2. **Verificar conectividade**: Event Bus funcionando?
3. **Verificar recursos**: Memória/CPU suficientes?
4. **Rebuild se necessário**: Último recurso

---

## 📊 **DASHBOARDS GRAFANA**

### **📈 Queries Prometheus Essenciais**

```yaml
# Dashboard: Projection Overview
panels:
  - title: "Health Score"
    query: "projection_health_score"
    type: "stat"
    
  - title: "Active Projections"
    query: "projection_active"
    type: "stat"
    
  - title: "Overall Lag"
    query: "projection_lag_overall"
    type: "graph"
    
  - title: "Error Rate"
    query: "rate(projection_events_failed[5m]) / rate(projection_events_processed[5m])"
    type: "graph"
    
  - title: "Throughput"
    query: "rate(projection_events_processed[1m])"
    type: "graph"
    
  - title: "Projection Status"
    query: "projection_status"
    type: "table"

# Alertas Grafana
alerts:
  - name: "Projection High Lag"
    condition: "projection_lag_overall > 1000"
    for: "5m"
    
  - name: "Projection High Error Rate"
    condition: "projection_error_rate > 0.05"
    for: "2m"
    
  - name: "Projection Health Score Low"
    condition: "projection_health_score < 0.8"
    for: "10m"
```

---

## 🎯 **LOGS ESTRUTURADOS**

### **📝 Configuração de Logging**

```yaml
logging:
  level:
    com.seguradora.hibrida.projection: INFO
    com.seguradora.hibrida.projection.rebuild: DEBUG
  pattern:
    console: "%d{HH:mm:ss.SSS} [%thread] %-5level [%X{projectionName}] %logger{36} - %msg%n"
```

**Exemplo de Log Estruturado:**
```java
@Override
protected void recordSuccess(T event, long processingTimeMs) {
    try (MDCCloseable mdc = MDCCloseable.put("projectionName", getProjectionName())) {
        log.info("Evento processado: eventType={}, aggregateId={}, processingTime={}ms, " +
                 "eventId={}, projectionPosition={}", 
            event.getEventType(), event.getAggregateId(), processingTimeMs,
            event.getEventId(), getCurrentPosition());
    }
}

@Override
protected void recordError(T event, long processingTimeMs, Exception error) {
    try (MDCCloseable mdc = MDCCloseable.put("projectionName", getProjectionName())) {
        log.error("Erro no processamento: eventType={}, aggregateId={}, processingTime={}ms, " +
                  "eventId={}, error={}", 
            event.getEventType(), event.getAggregateId(), processingTimeMs,
            event.getEventId(), error.getMessage(), error);
    }
}
```

---

## 🎓 **EXERCÍCIO FINAL**

### **📝 Implementação Completa de Monitoramento**

Implemente um sistema completo de monitoramento que:

1. **Colete métricas customizadas** específicas do domínio
2. **Detecte anomalias** automaticamente
3. **Envie alertas** para diferentes canais
4. **Gere relatórios** automáticos
5. **Sugira ações corretivas**

**Template:**
```java
@Component
public class CustomProjectionMonitor {
    
    public void detectAnomalies() {
        // Sua implementação de detecção de anomalias
    }
    
    public void generateHealthReport() {
        // Sua implementação de relatório
    }
    
    public void suggestCorrectiveActions(List<ConsistencyIssue> issues) {
        // Sua implementação de sugestões
    }
}
```

---

## 📚 **RESUMO DO MÓDULO PROJECTIONS**

Após completar as 5 partes do módulo Projections, você deve ser capaz de:

✅ **Compreender** os fundamentos e arquitetura das projeções  
✅ **Implementar** handlers robustos e configurações avançadas  
✅ **Projetar** query models otimizados e repositórios eficientes  
✅ **Gerenciar** rebuild e verificação de consistência  
✅ **Monitorar** e troubleshootar projeções em produção  

---

**📍 Próximo Módulo**: [Agregados - Parte 1](./08-agregados-parte-1.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Monitoramento e troubleshooting completo  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Sistema de monitoramento customizado