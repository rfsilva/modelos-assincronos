# 📘 COMMAND BUS - PARTE 5
## Métricas, Monitoramento e Otimização

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar métricas e monitoramento do Command Bus
- Otimizar performance de comandos
- Configurar alertas e dashboards
- Dominar testes avançados de handlers

---

## 📊 **SISTEMA DE MÉTRICAS**

### **🎯 Métricas Fundamentais**

O projeto implementa um sistema completo de métricas para monitoramento do Command Bus, permitindo observabilidade total do sistema.

### **📈 CommandBusMetrics - Implementação**

Localização: `com.seguradora.hibrida.command.config.CommandBusMetrics`

```java
@Component
public class CommandBusMetrics implements MeterBinder {
    
    private final MeterRegistry meterRegistry;
    private final CommandBus commandBus;
    
    // Contadores
    private Counter commandsProcessed;
    private Counter commandsFailed;
    private Counter commandsRejected;
    private Counter commandsTimeout;
    
    // Timers
    private Timer executionTimer;
    private Timer validationTimer;
    
    // Gauges
    private Gauge registeredHandlers;
    private Gauge activeCommands;
    
    @Override
    public void bindTo(MeterRegistry registry) {
        // Contador de comandos processados
        commandsProcessed = Counter.builder("command.bus.processed")
            .description("Total number of commands processed")
            .tag("component", "command-bus")
            .register(registry);
            
        // Contador de falhas
        commandsFailed = Counter.builder("command.bus.failed")
            .description("Total number of failed commands")
            .tag("component", "command-bus")
            .register(registry);
            
        // Timer de execução
        executionTimer = Timer.builder("command.bus.execution.time")
            .description("Command execution time")
            .register(registry);
            
        // Gauge de handlers registrados
        registeredHandlers = Gauge.builder("command.bus.handlers.registered")
            .description("Number of registered command handlers")
            .register(registry, this, CommandBusMetrics::getRegisteredHandlersCount);
    }
    
    // Métodos para coleta de métricas
    public void incrementCommandsProcessed(String commandType) {
        commandsProcessed.increment(Tags.of("command.type", commandType));
    }
    
    public void recordExecutionTime(String commandType, long executionTimeMs) {
        executionTimer.record(executionTimeMs, TimeUnit.MILLISECONDS,
            Tags.of("command.type", commandType));
    }
}
```

### **🔍 Métricas por Tipo de Comando**

```java
@Component
public class CommandTypeMetrics {
    
    private final Map<String, CommandTypeStatistics> statisticsByType = 
        new ConcurrentHashMap<>();
    
    public void recordCommandExecution(String commandType, 
                                     boolean success, 
                                     long executionTimeMs) {
        CommandTypeStatistics stats = statisticsByType
            .computeIfAbsent(commandType, k -> new CommandTypeStatistics());
            
        stats.recordExecution(success, executionTimeMs);
    }
    
    public CommandTypeStatistics getStatistics(String commandType) {
        return statisticsByType.getOrDefault(commandType, 
                                           new CommandTypeStatistics());
    }
    
    public Map<String, CommandTypeStatistics> getAllStatistics() {
        return new HashMap<>(statisticsByType);
    }
}

// Classe para estatísticas por tipo
public class CommandTypeStatistics {
    private final AtomicLong totalExecutions = new AtomicLong(0);
    private final AtomicLong successfulExecutions = new AtomicLong(0);
    private final AtomicLong failedExecutions = new AtomicLong(0);
    private final AtomicLong totalExecutionTime = new AtomicLong(0);
    
    public void recordExecution(boolean success, long executionTimeMs) {
        totalExecutions.incrementAndGet();
        totalExecutionTime.addAndGet(executionTimeMs);
        
        if (success) {
            successfulExecutions.incrementAndGet();
        } else {
            failedExecutions.incrementAndGet();
        }
    }
    
    public double getSuccessRate() {
        long total = totalExecutions.get();
        return total > 0 ? (double) successfulExecutions.get() / total : 0.0;
    }
    
    public double getAverageExecutionTime() {
        long total = totalExecutions.get();
        return total > 0 ? (double) totalExecutionTime.get() / total : 0.0;
    }
}
```

---

## 🏥 **HEALTH CHECKS**

### **🎯 CommandBusHealthIndicator**

Localização: `com.seguradora.hibrida.command.config.CommandBusHealthIndicator`

```java
@Component
public class CommandBusHealthIndicator implements HealthIndicator {
    
    private final CommandBus commandBus;
    private final CommandBusMetrics metrics;
    
    @Override
    public Health health() {
        try {
            Map<String, Object> details = checkHealth();
            String status = determineOverallStatus(details);
            
            return "UP".equals(status) 
                ? Health.up().withDetails(details).build()
                : Health.down().withDetails(details).build();
                
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        // Verifica se Command Bus está operacional
        boolean isOperational = isOperational();
        details.put("operational", isOperational);
        
        // Estatísticas básicas
        CommandBusStatistics stats = commandBus.getStatistics();
        details.put("totalCommands", stats.getTotalCommands());
        details.put("successRate", stats.getSuccessRate());
        details.put("errorRate", stats.getErrorRate());
        
        // Handlers registrados
        details.put("registeredHandlers", stats.getRegisteredHandlers());
        
        // Performance
        details.put("averageExecutionTime", stats.getAverageExecutionTime());
        details.put("throughputPerSecond", stats.getThroughputPerSecond());
        
        // Alertas
        List<String> alerts = checkForAlerts(stats);
        if (!alerts.isEmpty()) {
            details.put("alerts", alerts);
        }
        
        return details;
    }
    
    private List<String> checkForAlerts(CommandBusStatistics stats) {
        List<String> alerts = new ArrayList<>();
        
        // Taxa de erro alta
        if (stats.getErrorRate() > 0.05) { // 5%
            alerts.add("High error rate: " + 
                      String.format("%.2f%%", stats.getErrorRate() * 100));
        }
        
        // Tempo de execução alto
        if (stats.getAverageExecutionTime() > 5000) { // 5 segundos
            alerts.add("High average execution time: " + 
                      stats.getAverageExecutionTime() + "ms");
        }
        
        // Throughput baixo
        if (stats.getThroughputPerSecond() < 1.0) {
            alerts.add("Low throughput: " + 
                      String.format("%.2f commands/sec", stats.getThroughputPerSecond()));
        }
        
        return alerts;
    }
}
```

---

## ⚡ **OTIMIZAÇÃO DE PERFORMANCE**

### **🚀 Pool de Threads Otimizado**

```java
@Configuration
public class CommandBusOptimizationConfig {
    
    @Bean
    @ConfigurationProperties(prefix = "command-bus.thread-pool")
    public ThreadPoolTaskExecutor commandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        
        // Configuração otimizada baseada no hardware
        int processors = Runtime.getRuntime().availableProcessors();
        
        executor.setCorePoolSize(processors * 2);
        executor.setMaxPoolSize(processors * 4);
        executor.setQueueCapacity(1000);
        executor.setKeepAliveSeconds(60);
        executor.setThreadNamePrefix("command-exec-");
        
        // Política de rejeição customizada
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        
        // Permite que threads core sejam removidas quando ociosas
        executor.setAllowCoreThreadTimeOut(true);
        
        executor.initialize();
        return executor;
    }
    
    @Bean
    public CommandBusOptimizer commandBusOptimizer(
            CommandBus commandBus,
            CommandBusMetrics metrics) {
        return new CommandBusOptimizer(commandBus, metrics);
    }
}
```

### **🎯 Cache de Handlers**

```java
@Component
public class OptimizedCommandHandlerRegistry implements CommandHandlerRegistry {
    
    // Cache para lookup rápido de handlers
    private final Map<Class<? extends Command>, CommandHandler<?>> handlerCache = 
        new ConcurrentHashMap<>();
    
    // Cache para verificação de suporte
    private final Map<String, Boolean> supportCache = 
        new ConcurrentHashMap<>();
    
    @Override
    public <T extends Command> void registerHandler(CommandHandler<T> handler) {
        Class<T> commandType = handler.getCommandType();
        
        // Registra no cache principal
        handlerCache.put(commandType, handler);
        
        // Limpa cache de suporte para forçar recálculo
        supportCache.clear();
        
        log.info("Registered handler for command type: {}", 
                commandType.getSimpleName());
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public <T extends Command> CommandHandler<T> getHandler(Class<T> commandType) {
        // Lookup direto no cache (O(1))
        CommandHandler<T> handler = (CommandHandler<T>) handlerCache.get(commandType);
        
        if (handler == null) {
            throw new CommandHandlerNotFoundException(commandType);
        }
        
        return handler;
    }
    
    @Override
    public boolean hasHandler(Class<? extends Command> commandType) {
        String cacheKey = commandType.getName();
        
        return supportCache.computeIfAbsent(cacheKey, 
            k -> handlerCache.containsKey(commandType));
    }
}
```

### **⚡ Execução Assíncrona Otimizada**

```java
@Component
public class OptimizedCommandBus implements CommandBus {
    
    private final ThreadPoolTaskExecutor executor;
    private final CommandBusMetrics metrics;
    
    @Override
    public CompletableFuture<CommandResult> sendAsync(Command command) {
        // Métricas de início
        Timer.Sample sample = metrics.startExecutionTimer();
        
        return CompletableFuture
            .supplyAsync(() -> {
                try {
                    return processCommand(command);
                } finally {
                    // Métricas de fim
                    metrics.stopExecutionTimer(sample, command.getCommandType());
                }
            }, executor)
            .exceptionally(throwable -> {
                // Tratamento de erro assíncrono
                log.error("Async command execution failed: {}", 
                         command.getCommandId(), throwable);
                         
                metrics.incrementCommandsFailed(command.getCommandType());
                
                return CommandResult.failure(throwable.getMessage());
            });
    }
    
    private CommandResult processCommand(Command command) {
        // Implementação otimizada do processamento
        long startTime = System.currentTimeMillis();
        
        try {
            // Validação rápida
            ValidationResult validation = validateCommand(command);
            if (validation.isInvalid()) {
                return CommandResult.failure(validation.getFirstErrorMessage());
            }
            
            // Execução do handler
            CommandHandler<Command> handler = getHandler(command);
            CommandResult result = handler.handle(command);
            
            // Métricas de sucesso
            long executionTime = System.currentTimeMillis() - startTime;
            metrics.recordExecutionTime(command.getCommandType(), executionTime);
            metrics.incrementCommandsProcessed(command.getCommandType());
            
            return result;
            
        } catch (Exception e) {
            // Métricas de erro
            long executionTime = System.currentTimeMillis() - startTime;
            metrics.recordExecutionTime(command.getCommandType(), executionTime);
            metrics.incrementCommandsFailed(command.getCommandType());
            
            throw e;
        }
    }
}
```

---

## 📊 **DASHBOARDS E ALERTAS**

### **🎯 Endpoint de Métricas**

Localização: `com.seguradora.hibrida.command.controller.CommandBusController`

```java
@RestController
@RequestMapping("/api/command-bus")
public class CommandBusController {
    
    private final CommandBus commandBus;
    private final CommandBusMetrics metrics;
    
    @GetMapping("/metrics")
    public ResponseEntity<Map<String, Object>> getMetrics() {
        Map<String, Object> metricsData = new HashMap<>();
        
        CommandBusStatistics stats = commandBus.getStatistics();
        
        // Métricas gerais
        metricsData.put("totalCommands", stats.getTotalCommands());
        metricsData.put("successRate", stats.getSuccessRate());
        metricsData.put("errorRate", stats.getErrorRate());
        metricsData.put("averageExecutionTime", stats.getAverageExecutionTime());
        metricsData.put("throughput", stats.getThroughputPerSecond());
        
        // Métricas por tipo de comando
        Map<String, CommandTypeStatistics> typeStats = stats.getCommandTypeStatistics();
        metricsData.put("commandTypes", typeStats);
        
        // Top comandos mais executados
        List<Map<String, Object>> topCommands = typeStats.entrySet().stream()
            .sorted(Map.Entry.<String, CommandTypeStatistics>comparingByValue(
                (a, b) -> Long.compare(b.getTotalExecutions(), a.getTotalExecutions())))
            .limit(10)
            .map(entry -> Map.of(
                "commandType", entry.getKey(),
                "executions", entry.getValue().getTotalExecutions(),
                "successRate", entry.getValue().getSuccessRate(),
                "avgTime", entry.getValue().getAverageExecutionTime()
            ))
            .collect(Collectors.toList());
        metricsData.put("topCommands", topCommands);
        
        return ResponseEntity.ok(metricsData);
    }
    
    @GetMapping("/health")
    public ResponseEntity<Map<String, Object>> getHealth() {
        CommandBusHealthIndicator healthIndicator = 
            new CommandBusHealthIndicator(commandBus, metrics);
        
        Health health = healthIndicator.health();
        
        Map<String, Object> response = new HashMap<>();
        response.put("status", health.getStatus().getCode());
        response.put("details", health.getDetails());
        
        return ResponseEntity.ok(response);
    }
}
```

### **📈 Configuração do Prometheus**

```yaml
# prometheus.yml
global:
  scrape_interval: 15s

scrape_configs:
  - job_name: 'command-bus'
    static_configs:
      - targets: ['localhost:8080']
    metrics_path: '/actuator/prometheus'
    scrape_interval: 5s
```

### **🚨 Alertas no Grafana**

```json
{
  "alert": {
    "name": "Command Bus High Error Rate",
    "message": "Command Bus error rate is above 5%",
    "frequency": "10s",
    "conditions": [
      {
        "query": {
          "queryType": "",
          "refId": "A",
          "expr": "rate(command_bus_failed_total[5m]) / rate(command_bus_processed_total[5m]) * 100"
        },
        "reducer": {
          "type": "last",
          "params": []
        },
        "evaluator": {
          "params": [5],
          "type": "gt"
        }
      }
    ]
  }
}
```

---

## 🧪 **TESTES AVANÇADOS**

### **🎯 Testes de Performance**

```java
@SpringBootTest
@TestMethodOrder(OrderAnnotation.class)
class CommandBusPerformanceTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Test
    @Order(1)
    void testThroughputUnderLoad() {
        // Teste de throughput com múltiplos comandos
        int numberOfCommands = 1000;
        List<Command> commands = generateTestCommands(numberOfCommands);
        
        long startTime = System.currentTimeMillis();
        
        List<CompletableFuture<CommandResult>> futures = commands.stream()
            .map(commandBus::sendAsync)
            .collect(Collectors.toList());
        
        // Aguarda todos os comandos completarem
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]))
            .join();
        
        long endTime = System.currentTimeMillis();
        long duration = endTime - startTime;
        
        double throughput = (double) numberOfCommands / (duration / 1000.0);
        
        System.out.printf("Processed %d commands in %d ms (%.2f commands/sec)%n",
                         numberOfCommands, duration, throughput);
        
        // Verifica se throughput está dentro do esperado
        assertThat(throughput).isGreaterThan(100.0); // Mínimo 100 comandos/sec
    }
    
    @Test
    @Order(2)
    void testMemoryUsageUnderLoad() {
        // Monitora uso de memória durante execução
        Runtime runtime = Runtime.getRuntime();
        long initialMemory = runtime.totalMemory() - runtime.freeMemory();
        
        // Executa comandos
        for (int i = 0; i < 10000; i++) {
            TestCommand command = new TestCommand("test-" + i);
            commandBus.send(command);
        }
        
        // Força garbage collection
        System.gc();
        Thread.sleep(1000);
        
        long finalMemory = runtime.totalMemory() - runtime.freeMemory();
        long memoryIncrease = finalMemory - initialMemory;
        
        System.out.printf("Memory increase: %d bytes (%.2f MB)%n",
                         memoryIncrease, memoryIncrease / (1024.0 * 1024.0));
        
        // Verifica se não há vazamento significativo de memória
        assertThat(memoryIncrease).isLessThan(50 * 1024 * 1024); // Máximo 50MB
    }
}
```

### **🔧 Testes de Resiliência**

```java
@SpringBootTest
class CommandBusResilienceTest {
    
    @Test
    void testTimeoutHandling() {
        // Comando que demora mais que o timeout
        SlowCommand slowCommand = new SlowCommand(10000); // 10 segundos
        
        assertThatThrownBy(() -> commandBus.send(slowCommand))
            .isInstanceOf(CommandTimeoutException.class)
            .hasMessageContaining("timeout");
    }
    
    @Test
    void testConcurrentExecution() throws InterruptedException {
        int numberOfThreads = 50;
        int commandsPerThread = 100;
        CountDownLatch latch = new CountDownLatch(numberOfThreads);
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger errorCount = new AtomicInteger(0);
        
        ExecutorService executor = Executors.newFixedThreadPool(numberOfThreads);
        
        for (int i = 0; i < numberOfThreads; i++) {
            final int threadId = i;
            executor.submit(() -> {
                try {
                    for (int j = 0; j < commandsPerThread; j++) {
                        TestCommand command = new TestCommand("thread-" + threadId + "-cmd-" + j);
                        CommandResult result = commandBus.send(command);
                        
                        if (result.isSuccess()) {
                            successCount.incrementAndGet();
                        } else {
                            errorCount.incrementAndGet();
                        }
                    }
                } finally {
                    latch.countDown();
                }
            });
        }
        
        latch.await(30, TimeUnit.SECONDS);
        executor.shutdown();
        
        int totalCommands = numberOfThreads * commandsPerThread;
        System.out.printf("Concurrent execution: %d success, %d errors out of %d total%n",
                         successCount.get(), errorCount.get(), totalCommands);
        
        assertThat(successCount.get()).isEqualTo(totalCommands);
        assertThat(errorCount.get()).isZero();
    }
}
```

---

## 🎯 **CONFIGURAÇÕES DE PRODUÇÃO**

### **⚙️ application.yml Otimizado**

```yaml
command-bus:
  enabled: true
  thread-pool:
    core-size: 8
    max-size: 32
    queue-capacity: 1000
    keep-alive-seconds: 60
    thread-name-prefix: "cmd-exec-"
  
  timeout:
    default-seconds: 30
    validation-seconds: 5
  
  metrics:
    enabled: true
    detailed-logging: false
  
  monitoring:
    health-check-enabled: true
    error-rate-threshold: 0.05
    lag-threshold: 1000

# Actuator para métricas
management:
  endpoints:
    web:
      exposure:
        include: health,metrics,prometheus
  endpoint:
    health:
      show-details: always
    metrics:
      enabled: true
  metrics:
    export:
      prometheus:
        enabled: true
```

---

## 🎯 **EXERCÍCIOS PRÁTICOS**

### **📝 Exercício 1: Dashboard Customizado**
Crie um endpoint que retorne:
- Métricas em tempo real do Command Bus
- Top 5 comandos mais lentos
- Histórico de erros das últimas 24 horas

### **📝 Exercício 2: Alerta Customizado**
Implemente um sistema de alertas que:
- Monitore tempo de resposta por tipo de comando
- Envie notificação quando erro rate > 3%
- Detecte comandos "presos" (executando há muito tempo)

### **📝 Exercício 3: Otimização de Performance**
Otimize um handler específico:
- Implemente cache para consultas frequentes
- Adicione processamento em lote
- Meça o ganho de performance

---

## 🎉 **CONCLUSÃO DO COMMAND BUS**

Parabéns! Você completou o estudo completo do **Command Bus**. Agora você domina:

### **✅ Conhecimentos Adquiridos:**
- **Arquitetura** do Command Bus e seus componentes
- **Implementação** de handlers e validadores
- **Tratamento** robusto de erros e exceções
- **Monitoramento** e métricas em produção
- **Otimização** de performance e escalabilidade

### **🚀 Próximos Passos:**
Na próxima seção, estudaremos o **Event Bus**, que trabalha em conjunto com o Command Bus para implementar a arquitetura orientada a eventos.

---

## 🔗 **PRÓXIMO CAPÍTULO**

Continue sua jornada com:
**[06 - Event Bus - Parte 1](./06-event-bus-parte-1.md)** - Fundamentos do Event Bus

---

## 📚 **REFERÊNCIAS**

### **📖 Documentação Técnica**
- [Micrometer Metrics](https://micrometer.io/docs)
- [Spring Boot Actuator](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html)
- [Prometheus Monitoring](https://prometheus.io/docs/)
- [Grafana Dashboards](https://grafana.com/docs/)

### **🔧 Código de Referência**
- `CommandBusMetrics.java` - Sistema de métricas
- `CommandBusHealthIndicator.java` - Health checks
- `CommandBusController.java` - Endpoints de monitoramento
- `OptimizedCommandBus.java` - Implementação otimizada

---

**📘 Capítulo:** 05 - Command Bus - Parte 5 (Final)  
**⏱️ Tempo Estimado:** 60 minutos  
**🎯 Próximo:** [06 - Event Bus - Parte 1](./06-event-bus-parte-1.md)  
**📋 Checklist:** Métricas ✅ | Monitoramento ✅ | Otimização ✅ | Testes ✅