# 📖 CAPÍTULO 02: EVENT SOURCING - PARTE 4
## Monitoramento e Observabilidade

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar monitoramento completo do Event Store
- Configurar métricas e alertas essenciais
- Entender health checks e diagnósticos
- Explorar ferramentas de troubleshooting

---

## 📊 **MÉTRICAS DO EVENT STORE**

### **🎯 Métricas Essenciais**

```java
// Localização: eventstore/config/EventStoreMetrics.java
@Component
public class EventStoreMetrics implements MeterBinder {
    
    private final EventStore eventStore;
    private final MeterRegistry meterRegistry;
    
    // Contadores
    private Counter eventsWritten;
    private Counter eventsRead;
    private Counter concurrencyErrors;
    private Counter serializationErrors;
    
    // Timers
    private Timer writeTimer;
    private Timer readTimer;
    private Timer serializationTimer;
    
    // Gauges
    private Gauge totalEvents;
    private Gauge totalAggregates;
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        
        // Contadores de operações
        eventsWritten = Counter.builder("eventstore.events.written")
            .description("Total de eventos escritos")
            .register(registry);
            
        eventsRead = Counter.builder("eventstore.events.read")
            .description("Total de eventos lidos")
            .register(registry);
            
        concurrencyErrors = Counter.builder("eventstore.errors.concurrency")
            .description("Erros de concorrência")
            .register(registry);
            
        serializationErrors = Counter.builder("eventstore.errors.serialization")
            .description("Erros de serialização")
            .register(registry);
        
        // Timers de performance
        writeTimer = Timer.builder("eventstore.operation.write")
            .description("Tempo de escrita de eventos")
            .register(registry);
            
        readTimer = Timer.builder("eventstore.operation.read")
            .description("Tempo de leitura de eventos")
            .register(registry);
            
        serializationTimer = Timer.builder("eventstore.serialization")
            .description("Tempo de serialização")
            .register(registry);
        
        // Gauges de estado
        totalEvents = Gauge.builder("eventstore.events.total")
            .description("Total de eventos no store")
            .register(registry, this, EventStoreMetrics::getTotalEvents);
            
        totalAggregates = Gauge.builder("eventstore.aggregates.total")
            .description("Total de aggregates")
            .register(registry, this, EventStoreMetrics::getTotalAggregates);
    }
    
    // Métodos para coleta de métricas
    public Timer.Sample startWriteTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopWriteTimer(Timer.Sample sample) {
        sample.stop(writeTimer);
    }
    
    public void incrementEventsWritten(int count) {
        eventsWritten.increment(count);
    }
    
    public void incrementConcurrencyErrors() {
        concurrencyErrors.increment();
    }
    
    private double getTotalEvents() {
        // Implementação para contar eventos totais
        return eventStoreRepository.count();
    }
    
    private double getTotalAggregates() {
        // Implementação para contar aggregates únicos
        return eventStoreRepository.countDistinctAggregateIds();
    }
}
```

### **📈 Métricas Customizadas por Tipo de Evento**

```java
@Component
public class EventTypeMetrics {
    
    private final Map<String, Counter> eventCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> eventTimers = new ConcurrentHashMap<>();
    private final MeterRegistry meterRegistry;
    
    public void recordEventProcessed(String eventType, long processingTimeMs) {
        
        // Contador por tipo
        Counter counter = eventCounters.computeIfAbsent(eventType, type ->
            Counter.builder("eventstore.events.by_type")
                .tag("event_type", type)
                .description("Eventos processados por tipo")
                .register(meterRegistry)
        );
        counter.increment();
        
        // Timer por tipo
        Timer timer = eventTimers.computeIfAbsent(eventType, type ->
            Timer.builder("eventstore.processing.by_type")
                .tag("event_type", type)
                .description("Tempo de processamento por tipo")
                .register(meterRegistry)
        );
        timer.record(processingTimeMs, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Métricas de distribuição de eventos por hora
     */
    @Scheduled(fixedRate = 300000) // A cada 5 minutos
    public void updateHourlyDistribution() {
        
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        
        Map<String, Long> eventsByType = eventStoreRepository
            .getEventStatistics(oneHourAgo, Instant.now())
            .stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],  // event_type
                row -> (Long) row[1]     // count
            ));
        
        eventsByType.forEach((eventType, count) -> {
            Gauge.builder("eventstore.events.hourly")
                .tag("event_type", eventType)
                .description("Eventos na última hora por tipo")
                .register(meterRegistry, count, Number::doubleValue);
        });
    }
}
```

---

## 🏥 **HEALTH CHECKS**

### **💊 Health Indicator Principal**

```java
// Localização: eventstore/config/EventStoreHealthIndicator.java
@Component
public class EventStoreHealthIndicator implements HealthIndicator {
    
    private final EventStore eventStore;
    private final PartitionManager partitionManager;
    private final EventArchiver eventArchiver;
    
    @Override
    public Health health() {
        
        Map<String, Object> details = checkHealth();
        
        boolean isHealthy = isHealthy(details);
        
        return isHealthy ? 
            Health.up().withDetails(details).build() :
            Health.down().withDetails(details).build();
    }
    
    public Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // 1. Verificar conectividade básica
            details.put("database_connectivity", checkDatabaseConnectivity());
            
            // 2. Verificar partições
            details.put("partitions", checkPartitionHealth());
            
            // 3. Verificar arquivamento
            details.put("archiving", checkArchivingHealth());
            
            // 4. Verificar performance
            details.put("performance", checkPerformanceHealth());
            
            // 5. Verificar espaço em disco
            details.put("storage", checkStorageHealth());
            
            // Status geral
            details.put("status", determineOverallStatus(details));
            details.put("timestamp", Instant.now());
            
        } catch (Exception e) {
            details.put("error", e.getMessage());
            details.put("status", "DOWN");
        }
        
        return details;
    }
    
    private Map<String, Object> checkDatabaseConnectivity() {
        Map<String, Object> connectivity = new HashMap<>();
        
        try {
            // Teste simples de conectividade
            long eventCount = eventStoreRepository.count();
            connectivity.put("status", "UP");
            connectivity.put("total_events", eventCount);
            connectivity.put("response_time_ms", measureDatabaseResponseTime());
            
        } catch (Exception e) {
            connectivity.put("status", "DOWN");
            connectivity.put("error", e.getMessage());
        }
        
        return connectivity;
    }
    
    private Map<String, Object> checkPartitionHealth() {
        Map<String, Object> partitions = new HashMap<>();
        
        try {
            List<PartitionStatistics> stats = partitionManager.getPartitionStatistics();
            
            partitions.put("total_partitions", stats.size());
            partitions.put("healthy_partitions", 
                stats.stream().mapToInt(s -> s.isEmpty() ? 0 : 1).sum());
            
            // Verificar se há partições muito grandes
            List<PartitionStatistics> largePartitions = stats.stream()
                .filter(PartitionStatistics::isLarge)
                .collect(Collectors.toList());
                
            if (!largePartitions.isEmpty()) {
                partitions.put("warning", "Partições grandes detectadas");
                partitions.put("large_partitions", largePartitions.size());
            }
            
            partitions.put("status", largePartitions.isEmpty() ? "UP" : "WARNING");
            
        } catch (Exception e) {
            partitions.put("status", "DOWN");
            partitions.put("error", e.getMessage());
        }
        
        return partitions;
    }
    
    private Map<String, Object> checkPerformanceHealth() {
        Map<String, Object> performance = new HashMap<>();
        
        try {
            // Medir tempo de operações básicas
            long writeTime = measureWritePerformance();
            long readTime = measureReadPerformance();
            
            performance.put("avg_write_time_ms", writeTime);
            performance.put("avg_read_time_ms", readTime);
            
            // Alertas de performance
            boolean writeOk = writeTime < 100; // < 100ms
            boolean readOk = readTime < 50;    // < 50ms
            
            performance.put("write_performance", writeOk ? "OK" : "SLOW");
            performance.put("read_performance", readOk ? "OK" : "SLOW");
            performance.put("status", (writeOk && readOk) ? "UP" : "WARNING");
            
        } catch (Exception e) {
            performance.put("status", "DOWN");
            performance.put("error", e.getMessage());
        }
        
        return performance;
    }
    
    private long measureDatabaseResponseTime() {
        long start = System.currentTimeMillis();
        eventStoreRepository.count();
        return System.currentTimeMillis() - start;
    }
}
```

### **🚨 Alertas Automáticos**

```java
@Component
public class EventStoreAlerting {
    
    private final EventStoreHealthIndicator healthIndicator;
    private final NotificationService notificationService;
    
    @Scheduled(fixedRate = 60000) // A cada minuto
    public void checkHealthAndAlert() {
        
        Map<String, Object> health = healthIndicator.checkHealth();
        String status = (String) health.get("status");
        
        if ("DOWN".equals(status)) {
            sendCriticalAlert(health);
        } else if ("WARNING".equals(status)) {
            sendWarningAlert(health);
        }
    }
    
    private void sendCriticalAlert(Map<String, Object> health) {
        Alert alert = Alert.builder()
            .severity(AlertSeverity.CRITICAL)
            .title("Event Store DOWN")
            .message("Event Store está indisponível")
            .details(health)
            .timestamp(Instant.now())
            .build();
            
        notificationService.sendAlert(alert);
    }
    
    private void sendWarningAlert(Map<String, Object> health) {
        // Verificar se já foi enviado recentemente para evitar spam
        if (shouldSendWarning()) {
            Alert alert = Alert.builder()
                .severity(AlertSeverity.WARNING)
                .title("Event Store Performance Issues")
                .message("Detectados problemas de performance no Event Store")
                .details(health)
                .timestamp(Instant.now())
                .build();
                
            notificationService.sendAlert(alert);
        }
    }
}
```

---

## 🔧 **FERRAMENTAS DE ADMINISTRAÇÃO**

### **🎛️ Controller de Manutenção**

```java
// Localização: eventstore/controller/EventStoreMaintenanceController.java
@RestController
@RequestMapping("/api/v1/actuator/eventstore/maintenance")
@Tag(name = "Event Store Maintenance", description = "Ferramentas de manutenção do Event Store")
public class EventStoreMaintenanceController {
    
    private final EventArchiver eventArchiver;
    private final PartitionManager partitionManager;
    private final EventStoreHealthIndicator healthIndicator;
    
    @GetMapping("/dashboard")
    @Operation(summary = "Dashboard de manutenção")
    public ResponseEntity<Map<String, Object>> getDashboard() {
        
        Map<String, Object> dashboard = new HashMap<>();
        
        // Estatísticas gerais
        dashboard.put("health", healthIndicator.checkHealth());
        dashboard.put("partitions", partitionManager.getPartitionStatistics());
        dashboard.put("archive", eventArchiver.getArchiveStatistics());
        
        // Ações recomendadas
        List<String> recommendations = generateRecommendations();
        dashboard.put("recommendations", recommendations);
        
        return ResponseEntity.ok(dashboard);
    }
    
    @PostMapping("/partitions/create")
    @Operation(summary = "Criar nova partição")
    public ResponseEntity<Map<String, Object>> createPartition(
            @RequestParam String date) {
        
        try {
            LocalDate partitionDate = LocalDate.parse(date);
            boolean created = partitionManager.createMonthlyPartition("events", partitionDate);
            
            Map<String, Object> result = new HashMap<>();
            result.put("success", created);
            result.put("partition_date", date);
            result.put("message", created ? "Partição criada com sucesso" : "Partição já existe");
            
            return ResponseEntity.ok(result);
            
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @PostMapping("/archive")
    @Operation(summary = "Executar arquivamento manual")
    public ResponseEntity<ArchiveSummary> executeArchiving() {
        
        try {
            ArchiveSummary summary = eventArchiver.executeAutoArchiving();
            return ResponseEntity.ok(summary);
            
        } catch (Exception e) {
            ArchiveSummary errorSummary = new ArchiveSummary();
            errorSummary.setErrorMessage(e.getMessage());
            return ResponseEntity.status(500).body(errorSummary);
        }
    }
    
    @PostMapping("/partitions/{partitionName}/archive")
    @Operation(summary = "Arquivar partição específica")
    public ResponseEntity<Map<String, Object>> archivePartition(
            @PathVariable String partitionName) {
        
        try {
            ArchiveResult result = eventArchiver.archivePartition(partitionName);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", result.isSuccess());
            response.put("partition_name", partitionName);
            response.put("event_count", result.getEventCount());
            response.put("compressed_size", result.getCompressedSize());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    @GetMapping("/partitions/health")
    @Operation(summary = "Verificar saúde das partições")
    public ResponseEntity<Map<String, Object>> checkPartitionHealth() {
        
        Map<String, Object> health = new HashMap<>();
        
        try {
            boolean healthy = partitionManager.arePartitionsHealthy();
            List<PartitionStatistics> stats = partitionManager.getPartitionStatistics();
            
            health.put("healthy", healthy);
            health.put("total_partitions", stats.size());
            health.put("statistics", stats);
            
            // Identificar problemas
            List<String> issues = new ArrayList<>();
            
            for (PartitionStatistics stat : stats) {
                if (stat.isLarge()) {
                    issues.add("Partição " + stat.getPartitionName() + " está muito grande");
                }
                if (stat.isEmpty()) {
                    issues.add("Partição " + stat.getPartitionName() + " está vazia");
                }
            }
            
            health.put("issues", issues);
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            return ResponseEntity.status(500)
                .body(Map.of("error", e.getMessage()));
        }
    }
    
    private List<String> generateRecommendations() {
        List<String> recommendations = new ArrayList<>();
        
        try {
            // Verificar partições elegíveis para arquivamento
            List<String> eligiblePartitions = eventArchiver.findPartitionsForArchiving();
            if (!eligiblePartitions.isEmpty()) {
                recommendations.add(
                    String.format("Arquivar %d partições antigas", eligiblePartitions.size())
                );
            }
            
            // Verificar partições grandes
            List<PartitionStatistics> largePartitions = partitionManager
                .getPartitionStatistics()
                .stream()
                .filter(PartitionStatistics::isLarge)
                .collect(Collectors.toList());
                
            if (!largePartitions.isEmpty()) {
                recommendations.add(
                    String.format("Considerar particionamento adicional para %d partições grandes", 
                                largePartitions.size())
                );
            }
            
            // Verificar necessidade de manutenção
            if (partitionManager.schedulePartitionMaintenance()) {
                recommendations.add("Executar manutenção de partições");
            }
            
        } catch (Exception e) {
            recommendations.add("Erro ao gerar recomendações: " + e.getMessage());
        }
        
        return recommendations;
    }
}
```

### **📊 Dashboard de Monitoramento**

```java
@RestController
@RequestMapping("/api/v1/actuator/eventstore")
public class EventStoreController {
    
    @GetMapping("/statistics")
    @Operation(summary = "Estatísticas detalhadas do Event Store")
    public ResponseEntity<Map<String, Object>> getStatistics(
            @RequestParam(defaultValue = "24") int hours) {
        
        Map<String, Object> stats = new HashMap<>();
        
        Instant since = Instant.now().minus(hours, ChronoUnit.HOURS);
        
        // Estatísticas gerais
        stats.put("total_events", eventStoreRepository.count());
        stats.put("total_aggregates", eventStoreRepository.countDistinctAggregateIds());
        
        // Estatísticas por período
        List<Object[]> eventsByType = eventStoreRepository.getEventStatistics(since, Instant.now());
        Map<String, Object> typeStats = eventsByType.stream()
            .collect(Collectors.toMap(
                row -> (String) row[0],
                row -> Map.of(
                    "count", row[1],
                    "avg_size", row[2]
                )
            ));
        stats.put("events_by_type", typeStats);
        
        // Performance
        stats.put("avg_write_time_ms", getAverageWriteTime());
        stats.put("avg_read_time_ms", getAverageReadTime());
        
        // Armazenamento
        stats.put("storage_usage", getStorageUsage());
        
        return ResponseEntity.ok(stats);
    }
    
    @GetMapping("/health")
    @Operation(summary = "Health check detalhado")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        
        Map<String, Object> health = healthIndicator.checkHealth();
        
        HttpStatus status = "UP".equals(health.get("status")) ? 
            HttpStatus.OK : HttpStatus.SERVICE_UNAVAILABLE;
            
        return ResponseEntity.status(status).body(health);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Configurar monitoramento completo

#### **Passo 1: Explorar Métricas**
```bash
# Acessar métricas do Actuator
curl http://localhost:8083/api/v1/actuator/metrics | jq

# Métricas específicas do Event Store
curl http://localhost:8083/api/v1/actuator/metrics/eventstore.events.written
curl http://localhost:8083/api/v1/actuator/metrics/eventstore.operation.write

# Prometheus endpoint (se configurado)
curl http://localhost:8083/api/v1/actuator/prometheus | grep eventstore
```

#### **Passo 2: Testar Health Checks**
```bash
# Health check geral
curl http://localhost:8083/api/v1/actuator/health | jq

# Health check específico do Event Store
curl http://localhost:8083/api/v1/actuator/eventstore/health | jq

# Dashboard de manutenção
curl http://localhost:8083/api/v1/actuator/eventstore/maintenance/dashboard | jq
```

#### **Passo 3: Simular Problemas**
```java
@Test
public void simularProblemasPerformance() {
    // Simular muitas escritas simultâneas
    ExecutorService executor = Executors.newFixedThreadPool(10);
    
    for (int i = 0; i < 100; i++) {
        final int eventNum = i;
        executor.submit(() -> {
            try {
                SinistroEvent evento = SinistroEvent.sinistroCriado(
                    "sinistro-stress-" + eventNum,
                    "SIN-STRESS-" + eventNum,
                    "Teste de stress",
                    1000.0
                );
                
                eventStore.saveEvents(
                    "sinistro-stress-" + eventNum,
                    Arrays.asList(evento),
                    0
                );
                
            } catch (Exception e) {
                System.err.println("Erro no evento " + eventNum + ": " + e.getMessage());
            }
        });
    }
    
    executor.shutdown();
    
    // Verificar métricas após stress test
    Map<String, Object> health = healthIndicator.checkHealth();
    System.out.println("Health após stress test: " + health);
}
```

#### **Passo 4: Configurar Alertas**
```yaml
# application.yml - Configuração de alertas
eventstore:
  monitoring:
    alerts:
      enabled: true
      thresholds:
        write_time_ms: 100
        read_time_ms: 50
        error_rate: 0.05
        partition_size_gb: 10
    notifications:
      email:
        enabled: true
        recipients: ["admin@seguradora.com"]
      slack:
        enabled: false
        webhook_url: "https://hooks.slack.com/..."
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Configurar** métricas customizadas para Event Store
2. **Implementar** health checks abrangentes
3. **Criar** dashboards de monitoramento
4. **Configurar** alertas automáticos
5. **Usar** ferramentas de troubleshooting

### **❓ Perguntas para Reflexão:**

1. Quais métricas são mais importantes para monitorar?
2. Como detectar problemas antes que afetem usuários?
3. Qual a diferença entre métricas e logs?
4. Como balancear observabilidade vs performance?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 5**, vamos finalizar com:
- Troubleshooting de problemas comuns
- Otimizações avançadas de performance
- Backup e disaster recovery
- Migração e evolução do Event Store

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 50 minutos  
**📋 Pré-requisitos:** Event Sourcing Partes 1-3