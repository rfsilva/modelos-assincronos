# 📖 CAPÍTULO 05: COMMAND BUS - PARTE 3
## Validação Avançada e Políticas de Resilência

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar validação avançada de comandos
- Configurar políticas de retry e circuit breaker
- Desenvolver monitoramento detalhado
- Otimizar performance do Command Bus

---

## ✅ **VALIDAÇÃO AVANÇADA**

### **🔍 Sistema de Validação em Camadas**

```java
// Localização: command/validation/LayeredCommandValidation.java
@Component
public class LayeredCommandValidation {
    
    private final Map<ValidationLayer, List<CommandValidator<? extends Command>>> validatorsByLayer;
    
    public LayeredCommandValidation(List<CommandValidator<? extends Command>> validators) {
        this.validatorsByLayer = validators.stream()
            .collect(Collectors.groupingBy(this::determineLayer));
    }
    
    /**
     * Executa validação em camadas ordenadas
     */
    public ValidationResult validateInLayers(Command command) {
        
        ValidationResult result = ValidationResult.valid();
        
        // Camada 1: Validação Sintática (formato, tipos, etc.)
        result = result.combine(validateLayer(command, ValidationLayer.SYNTACTIC));
        if (result.isInvalid()) {
            return result; // Parar se sintaxe inválida
        }
        
        // Camada 2: Validação Semântica (regras de negócio básicas)
        result = result.combine(validateLayer(command, ValidationLayer.SEMANTIC));
        if (result.isInvalid()) {
            return result;
        }
        
        // Camada 3: Validação de Integridade (referências, consistência)
        result = result.combine(validateLayer(command, ValidationLayer.INTEGRITY));
        if (result.isInvalid()) {
            return result;
        }
        
        // Camada 4: Validação de Autorização (permissões, políticas)
        result = result.combine(validateLayer(command, ValidationLayer.AUTHORIZATION));
        
        return result;
    }
    
    private ValidationResult validateLayer(Command command, ValidationLayer layer) {
        
        List<CommandValidator<? extends Command>> layerValidators = 
            validatorsByLayer.getOrDefault(layer, Collections.emptyList());
        
        ValidationResult layerResult = ValidationResult.valid();
        
        for (CommandValidator<? extends Command> validator : layerValidators) {
            
            if (validator.supports(command.getClass())) {
                
                try {
                    @SuppressWarnings("unchecked")
                    CommandValidator<Command> typedValidator = (CommandValidator<Command>) validator;
                    
                    ValidationResult validatorResult = typedValidator.validate(command);
                    layerResult = layerResult.combine(validatorResult);
                    
                } catch (Exception e) {
                    log.error("Erro no validador {} para comando {}: {}", 
                             validator.getClass().getSimpleName(), 
                             command.getCommandType(), 
                             e.getMessage());
                    
                    layerResult = layerResult.addErrorMessage(
                        "Erro interno na validação: " + validator.getClass().getSimpleName()
                    );
                }
            }
        }
        
        return layerResult;
    }
    
    private ValidationLayer determineLayer(CommandValidator<? extends Command> validator) {
        
        // Usar anotação se presente
        if (validator.getClass().isAnnotationPresent(ValidationLayer.class)) {
            return validator.getClass().getAnnotation(ValidationLayer.class).value();
        }
        
        // Determinar por convenção de nome
        String className = validator.getClass().getSimpleName().toLowerCase();
        
        if (className.contains("syntax") || className.contains("format")) {
            return ValidationLayer.SYNTACTIC;
        } else if (className.contains("business") || className.contains("rule")) {
            return ValidationLayer.SEMANTIC;
        } else if (className.contains("integrity") || className.contains("reference")) {
            return ValidationLayer.INTEGRITY;
        } else if (className.contains("auth") || className.contains("permission")) {
            return ValidationLayer.AUTHORIZATION;
        }
        
        return ValidationLayer.SEMANTIC; // Padrão
    }
}

// Enum para camadas de validação
public enum ValidationLayer {
    SYNTACTIC(1, "Validação Sintática"),
    SEMANTIC(2, "Validação Semântica"), 
    INTEGRITY(3, "Validação de Integridade"),
    AUTHORIZATION(4, "Validação de Autorização");
    
    private final int order;
    private final String description;
    
    ValidationLayer(int order, String description) {
        this.order = order;
        this.description = description;
    }
    
    public int getOrder() { return order; }
    public String getDescription() { return description; }
}

// Anotação para marcar camada do validador
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidationLayer {
    ValidationLayer value();
}
```

### **🔒 Validadores Específicos por Camada**

```java
// Validador sintático
@Component
@ValidationLayer(ValidationLayer.SYNTACTIC)
public class CriarSinistroSyntacticValidator implements CommandValidator<CriarSinistroCommand> {
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        
        List<String> errors = new ArrayList<>();
        
        // Validar formato de CPF
        if (!CpfValidator.hasValidFormat(command.getCpfSegurado())) {
            errors.add("Formato de CPF inválido");
        }
        
        // Validar formato de placa
        if (!PlacaValidator.hasValidFormat(command.getPlacaVeiculo())) {
            errors.add("Formato de placa inválido");
        }
        
        // Validar tamanhos de string
        if (command.getDescricaoOcorrencia().length() > 2000) {
            errors.add("Descrição muito longa (máximo 2000 caracteres)");
        }
        
        if (command.getEnderecoOcorrencia().length() > 500) {
            errors.add("Endereço muito longo (máximo 500 caracteres)");
        }
        
        return errors.isEmpty() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid(errors);
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getPriority() {
        return 1; // Primeira validação
    }
}

// Validador de integridade referencial
@Component
@ValidationLayer(ValidationLayer.INTEGRITY)
public class CriarSinistroIntegrityValidator implements CommandValidator<CriarSinistroCommand> {
    
    private final SeguradoService seguradoService;
    private final VeiculoService veiculoService;
    private final ApoliceService apoliceService;
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        
        List<String> errors = new ArrayList<>();
        
        try {
            // Verificar se segurado existe
            Segurado segurado = seguradoService.buscarPorCpf(command.getCpfSegurado());
            if (segurado == null) {
                errors.add("Segurado não encontrado");
            } else if (!segurado.isAtivo()) {
                errors.add("Segurado não está ativo");
            } else {
                
                // Verificar veículo
                Veiculo veiculo = veiculoService.buscarPorPlaca(command.getPlacaVeiculo());
                if (veiculo == null) {
                    errors.add("Veículo não encontrado");
                } else if (!veiculo.pertenceAoSegurado(segurado.getId())) {
                    errors.add("Veículo não pertence ao segurado");
                } else {
                    
                    // Verificar apólice vigente
                    boolean temApoliceVigente = apoliceService.existeApoliceVigente(
                        segurado.getId(),
                        veiculo.getId(),
                        command.getDataOcorrencia().toLocalDate()
                    );
                    
                    if (!temApoliceVigente) {
                        errors.add("Não há apólice vigente para o veículo na data da ocorrência");
                    }
                }
            }
            
        } catch (Exception e) {
            log.error("Erro na validação de integridade: {}", e.getMessage());
            errors.add("Erro interno na validação de dados");
        }
        
        return errors.isEmpty() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid(errors);
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getPriority() {
        return 30; // Após validações básicas
    }
}

// Validador de autorização
@Component
@ValidationLayer(ValidationLayer.AUTHORIZATION)
public class CriarSinistroAuthorizationValidator implements CommandValidator<CriarSinistroCommand> {
    
    private final AuthorizationService authService;
    private final UserService userService;
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        
        try {
            // Verificar se usuário existe e está ativo
            User user = userService.findById(command.getUserId());
            if (user == null || !user.isActive()) {
                return ValidationResult.invalid("Usuário não autorizado");
            }
            
            // Verificar permissão para criar sinistros
            if (!authService.hasPermission(user, "SINISTRO_CREATE")) {
                return ValidationResult.invalid("Usuário não tem permissão para criar sinistros");
            }
            
            // Verificar limite de valor para o usuário
            if (command.getValorEstimado() != null) {
                BigDecimal limiteUsuario = authService.getValueLimit(user, "SINISTRO_CREATE");
                
                if (command.getValorEstimado().compareTo(limiteUsuario) > 0) {
                    return ValidationResult.invalid(
                        String.format("Valor estimado (R$ %.2f) excede limite do usuário (R$ %.2f)",
                                    command.getValorEstimado(), limiteUsuario)
                    );
                }
            }
            
            // Verificar horário de trabalho para operações críticas
            if (command.getValorEstimado() != null && 
                command.getValorEstimado().compareTo(new BigDecimal("100000")) > 0) {
                
                LocalTime agora = LocalTime.now();
                if (agora.isBefore(LocalTime.of(8, 0)) || agora.isAfter(LocalTime.of(18, 0))) {
                    return ValidationResult.invalid(
                        "Sinistros de alto valor só podem ser criados em horário comercial (8h-18h)"
                    );
                }
            }
            
            return ValidationResult.valid();
            
        } catch (Exception e) {
            log.error("Erro na validação de autorização: {}", e.getMessage());
            return ValidationResult.invalid("Erro interno na validação de autorização");
        }
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getPriority() {
        return 40; // Última validação
    }
}
```

---

## 🔄 **POLÍTICAS DE RESILÊNCIA**

### **🔁 Retry Policy**

```java
// Localização: command/resilience/CommandRetryPolicy.java
@Component
public class CommandRetryPolicy {
    
    private final CommandBusProperties properties;
    
    /**
     * Executa comando com retry automático
     */
    public <T extends Command> CommandResult executeWithRetry(T command, 
                                                            CommandHandler<T> handler) {
        
        int maxAttempts = properties.getRetry().getMaxAttempts();
        long initialDelay = properties.getRetry().getInitialDelayMs();
        double backoffMultiplier = properties.getRetry().getBackoffMultiplier();
        
        CommandResult lastResult = null;
        
        for (int attempt = 1; attempt <= maxAttempts; attempt++) {
            
            try {
                log.debug("Tentativa {} de {} para comando {}", 
                         attempt, maxAttempts, command.getCommandId());
                
                CommandResult result = handler.handle(command);
                
                if (result.isSuccess() || !isRetryableError(result)) {
                    return result;
                }
                
                lastResult = result;
                
                // Se não é a última tentativa, aguardar antes de retry
                if (attempt < maxAttempts) {
                    long delay = calculateRetryDelay(attempt, initialDelay, backoffMultiplier);
                    Thread.sleep(delay);
                }
                
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return CommandResult.failure("Retry interrompido", "RETRY_INTERRUPTED");
                
            } catch (Exception e) {
                
                if (!isRetryableException(e)) {
                    return CommandResult.failure(e.getMessage(), "NON_RETRYABLE_ERROR");
                }
                
                lastResult = CommandResult.failure(e.getMessage(), "RETRYABLE_ERROR");
                
                if (attempt < maxAttempts) {
                    try {
                        long delay = calculateRetryDelay(attempt, initialDelay, backoffMultiplier);
                        Thread.sleep(delay);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return CommandResult.failure("Retry interrompido", "RETRY_INTERRUPTED");
                    }
                }
            }
        }
        
        // Todas as tentativas falharam
        return lastResult != null ? 
            lastResult.withMetadata("retry_attempts", maxAttempts) :
            CommandResult.failure("Máximo de tentativas excedido", "MAX_RETRIES_EXCEEDED");
    }
    
    private boolean isRetryableError(CommandResult result) {
        
        if (result.isSuccess()) {
            return false;
        }
        
        String errorCode = result.getErrorCode();
        
        // Erros que NÃO devem ser retentados
        Set<String> nonRetryableErrors = Set.of(
            "VALIDATION_ERROR",
            "BUSINESS_RULE_VIOLATION", 
            "AUTHORIZATION_ERROR",
            "COMMAND_NOT_FOUND",
            "DUPLICATE_COMMAND"
        );
        
        return !nonRetryableErrors.contains(errorCode);
    }
    
    private boolean isRetryableException(Exception e) {
        
        // Exceções que NÃO devem ser retentadas
        if (e instanceof ValidationException ||
            e instanceof BusinessRuleException ||
            e instanceof AuthorizationException ||
            e instanceof IllegalArgumentException) {
            return false;
        }
        
        // Exceções que DEVEM ser retentadas
        if (e instanceof DataAccessException ||
            e instanceof TransientDataAccessException ||
            e instanceof CannotAcquireLockException ||
            e instanceof OptimisticLockingFailureException) {
            return true;
        }
        
        // Por padrão, tentar retry para exceções desconhecidas
        return true;
    }
    
    private long calculateRetryDelay(int attempt, long initialDelay, double backoffMultiplier) {
        
        // Backoff exponencial com jitter
        long baseDelay = (long) (initialDelay * Math.pow(backoffMultiplier, attempt - 1));
        
        // Adicionar jitter (±10%)
        double jitterPercent = properties.getRetry().getJitterPercent();
        double jitter = 1.0 + (Math.random() - 0.5) * 2 * jitterPercent;
        
        long delayWithJitter = (long) (baseDelay * jitter);
        
        // Limitar delay máximo
        long maxDelay = properties.getRetry().getMaxDelayMs();
        return Math.min(delayWithJitter, maxDelay);
    }
}
```

### **⚡ Circuit Breaker**

```java
// Localização: command/resilience/CommandCircuitBreaker.java
@Component
public class CommandCircuitBreaker {
    
    private final Map<String, CircuitBreakerState> circuitBreakers = new ConcurrentHashMap<>();
    private final CommandBusProperties properties;
    
    /**
     * Executa comando com circuit breaker
     */
    public <T extends Command> CommandResult executeWithCircuitBreaker(T command, 
                                                                      CommandHandler<T> handler) {
        
        String handlerKey = handler.getClass().getSimpleName();
        CircuitBreakerState circuitBreaker = getOrCreateCircuitBreaker(handlerKey);
        
        // Verificar estado do circuit breaker
        if (circuitBreaker.isOpen()) {
            
            // Verificar se pode tentar half-open
            if (circuitBreaker.canAttemptReset()) {
                circuitBreaker.attemptReset();
            } else {
                return CommandResult.failure(
                    "Circuit breaker aberto para handler: " + handlerKey,
                    "CIRCUIT_BREAKER_OPEN"
                );
            }
        }
        
        try {
            // Executar comando
            CommandResult result = handler.handle(command);
            
            // Registrar resultado
            if (result.isSuccess()) {
                circuitBreaker.recordSuccess();
            } else {
                circuitBreaker.recordFailure();
            }
            
            return result;
            
        } catch (Exception e) {
            circuitBreaker.recordFailure();
            throw e;
        }
    }
    
    private CircuitBreakerState getOrCreateCircuitBreaker(String handlerKey) {
        
        return circuitBreakers.computeIfAbsent(handlerKey, key -> {
            
            CircuitBreakerConfig config = CircuitBreakerConfig.builder()
                .failureThreshold(properties.getCircuitBreaker().getFailureThreshold())
                .recoveryTimeout(properties.getCircuitBreaker().getRecoveryTimeoutSeconds())
                .minimumThroughput(properties.getCircuitBreaker().getMinimumThroughput())
                .build();
            
            return new CircuitBreakerState(key, config);
        });
    }
}

// Estado do Circuit Breaker
public class CircuitBreakerState {
    
    private final String name;
    private final CircuitBreakerConfig config;
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private volatile CircuitState state = CircuitState.CLOSED;
    private volatile Instant lastFailureTime;
    private volatile Instant stateChangedTime = Instant.now();
    
    public CircuitBreakerState(String name, CircuitBreakerConfig config) {
        this.name = name;
        this.config = config;
    }
    
    public boolean isOpen() {
        return state == CircuitState.OPEN;
    }
    
    public boolean isHalfOpen() {
        return state == CircuitState.HALF_OPEN;
    }
    
    public boolean isClosed() {
        return state == CircuitState.CLOSED;
    }
    
    public void recordSuccess() {
        
        successCount.incrementAndGet();
        
        if (state == CircuitState.HALF_OPEN) {
            // Sucesso em half-open -> fechar circuit
            changeState(CircuitState.CLOSED);
            resetCounters();
        }
    }
    
    public void recordFailure() {
        
        failureCount.incrementAndGet();
        lastFailureTime = Instant.now();
        
        if (state == CircuitState.HALF_OPEN) {
            // Falha em half-open -> abrir circuit novamente
            changeState(CircuitState.OPEN);
        } else if (state == CircuitState.CLOSED) {
            // Verificar se deve abrir circuit
            if (shouldOpenCircuit()) {
                changeState(CircuitState.OPEN);
            }
        }
    }
    
    public boolean canAttemptReset() {
        
        if (state != CircuitState.OPEN) {
            return false;
        }
        
        Duration timeSinceStateChange = Duration.between(stateChangedTime, Instant.now());
        return timeSinceStateChange.getSeconds() >= config.getRecoveryTimeoutSeconds();
    }
    
    public void attemptReset() {
        if (canAttemptReset()) {
            changeState(CircuitState.HALF_OPEN);
        }
    }
    
    private boolean shouldOpenCircuit() {
        
        long totalRequests = successCount.get() + failureCount.get();
        
        // Verificar throughput mínimo
        if (totalRequests < config.getMinimumThroughput()) {
            return false;
        }
        
        // Verificar taxa de falha
        double failureRate = (double) failureCount.get() / totalRequests;
        return failureRate >= config.getFailureThreshold();
    }
    
    private void changeState(CircuitState newState) {
        
        CircuitState oldState = this.state;
        this.state = newState;
        this.stateChangedTime = Instant.now();
        
        log.info("Circuit breaker {} mudou de estado: {} -> {}", name, oldState, newState);
        
        // Resetar contadores quando fechar
        if (newState == CircuitState.CLOSED) {
            resetCounters();
        }
    }
    
    private void resetCounters() {
        successCount.set(0);
        failureCount.set(0);
    }
    
    // Getters para métricas
    public long getSuccessCount() { return successCount.get(); }
    public long getFailureCount() { return failureCount.get(); }
    public CircuitState getState() { return state; }
    public Instant getLastFailureTime() { return lastFailureTime; }
    public Instant getStateChangedTime() { return stateChangedTime; }
}

public enum CircuitState {
    CLOSED,   // Funcionando normalmente
    OPEN,     // Bloqueando chamadas
    HALF_OPEN // Testando se pode voltar ao normal
}
```

---

## 📊 **MONITORAMENTO DETALHADO**

### **📈 Métricas Avançadas**

```java
// Localização: command/metrics/DetailedCommandMetrics.java
@Component
public class DetailedCommandMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Map<String, CommandTypeMetrics> commandMetrics = new ConcurrentHashMap<>();
    
    /**
     * Registra execução de comando com detalhes
     */
    public void recordCommandExecution(Command command, 
                                     CommandResult result, 
                                     Duration executionTime) {
        
        String commandType = command.getCommandType();
        CommandTypeMetrics metrics = getOrCreateMetrics(commandType);
        
        // Atualizar contadores
        metrics.incrementTotal();
        
        if (result.isSuccess()) {
            metrics.incrementSuccess();
        } else {
            metrics.incrementFailure(result.getErrorCode());
        }
        
        // Atualizar tempos
        metrics.recordExecutionTime(executionTime.toMillis());
        
        // Registrar no Micrometer
        Counter.builder("commandbus.commands.total")
            .tag("command_type", commandType)
            .tag("result", result.isSuccess() ? "success" : "failure")
            .register(meterRegistry)
            .increment();
        
        Timer.builder("commandbus.execution.time")
            .tag("command_type", commandType)
            .register(meterRegistry)
            .record(executionTime);
        
        // Métricas de erro por tipo
        if (result.isFailure()) {
            Counter.builder("commandbus.errors.by_type")
                .tag("command_type", commandType)
                .tag("error_code", result.getErrorCode())
                .register(meterRegistry)
                .increment();
        }
    }
    
    /**
     * Registra uso de middleware
     */
    public void recordMiddlewareExecution(String middlewareName, 
                                        String phase, 
                                        Duration executionTime) {
        
        Timer.builder("commandbus.middleware.time")
            .tag("middleware", middlewareName)
            .tag("phase", phase) // "pre" ou "post"
            .register(meterRegistry)
            .record(executionTime);
    }
    
    /**
     * Registra circuit breaker events
     */
    public void recordCircuitBreakerEvent(String handlerName, String event) {
        
        Counter.builder("commandbus.circuit_breaker.events")
            .tag("handler", handlerName)
            .tag("event", event) // "opened", "closed", "half_opened"
            .register(meterRegistry)
            .increment();
    }
    
    private CommandTypeMetrics getOrCreateMetrics(String commandType) {
        return commandMetrics.computeIfAbsent(commandType, CommandTypeMetrics::new);
    }
    
    /**
     * Obtém relatório de performance por tipo de comando
     */
    public Map<String, CommandPerformanceReport> getPerformanceReport() {
        
        Map<String, CommandPerformanceReport> report = new HashMap<>();
        
        for (Map.Entry<String, CommandTypeMetrics> entry : commandMetrics.entrySet()) {
            
            CommandTypeMetrics metrics = entry.getValue();
            
            CommandPerformanceReport commandReport = CommandPerformanceReport.builder()
                .commandType(entry.getKey())
                .totalExecutions(metrics.getTotalCount())
                .successCount(metrics.getSuccessCount())
                .failureCount(metrics.getFailureCount())
                .successRate(metrics.getSuccessRate())
                .averageExecutionTime(metrics.getAverageExecutionTime())
                .minExecutionTime(metrics.getMinExecutionTime())
                .maxExecutionTime(metrics.getMaxExecutionTime())
                .p95ExecutionTime(metrics.getP95ExecutionTime())
                .errorsByType(metrics.getErrorsByType())
                .build();
            
            report.put(entry.getKey(), commandReport);
        }
        
        return report;
    }
}

// Métricas por tipo de comando
public class CommandTypeMetrics {
    
    private final String commandType;
    private final AtomicLong totalCount = new AtomicLong(0);
    private final AtomicLong successCount = new AtomicLong(0);
    private final AtomicLong failureCount = new AtomicLong(0);
    private final Map<String, AtomicLong> errorsByType = new ConcurrentHashMap<>();
    
    // Estatísticas de tempo
    private final LongAdder totalExecutionTime = new LongAdder();
    private volatile long minExecutionTime = Long.MAX_VALUE;
    private volatile long maxExecutionTime = Long.MIN_VALUE;
    private final List<Long> recentExecutionTimes = Collections.synchronizedList(new ArrayList<>());
    
    public CommandTypeMetrics(String commandType) {
        this.commandType = commandType;
    }
    
    public void incrementTotal() {
        totalCount.incrementAndGet();
    }
    
    public void incrementSuccess() {
        successCount.incrementAndGet();
    }
    
    public void incrementFailure(String errorCode) {
        failureCount.incrementAndGet();
        
        if (errorCode != null) {
            errorsByType.computeIfAbsent(errorCode, k -> new AtomicLong(0))
                       .incrementAndGet();
        }
    }
    
    public void recordExecutionTime(long timeMs) {
        
        totalExecutionTime.add(timeMs);
        
        // Atualizar min/max
        updateMinTime(timeMs);
        updateMaxTime(timeMs);
        
        // Manter últimos 1000 tempos para percentis
        synchronized (recentExecutionTimes) {
            recentExecutionTimes.add(timeMs);
            
            if (recentExecutionTimes.size() > 1000) {
                recentExecutionTimes.remove(0);
            }
        }
    }
    
    private void updateMinTime(long timeMs) {
        long currentMin = minExecutionTime;
        while (timeMs < currentMin) {
            if (compareAndSetMinTime(currentMin, timeMs)) {
                break;
            }
            currentMin = minExecutionTime;
        }
    }
    
    private void updateMaxTime(long timeMs) {
        long currentMax = maxExecutionTime;
        while (timeMs > currentMax) {
            if (compareAndSetMaxTime(currentMax, timeMs)) {
                break;
            }
            currentMax = maxExecutionTime;
        }
    }
    
    private boolean compareAndSetMinTime(long expect, long update) {
        // Implementação thread-safe para atualizar mínimo
        synchronized (this) {
            if (minExecutionTime == expect) {
                minExecutionTime = update;
                return true;
            }
            return false;
        }
    }
    
    private boolean compareAndSetMaxTime(long expect, long update) {
        // Implementação thread-safe para atualizar máximo
        synchronized (this) {
            if (maxExecutionTime == expect) {
                maxExecutionTime = update;
                return true;
            }
            return false;
        }
    }
    
    // Getters para relatórios
    public long getTotalCount() { return totalCount.get(); }
    public long getSuccessCount() { return successCount.get(); }
    public long getFailureCount() { return failureCount.get(); }
    
    public double getSuccessRate() {
        long total = totalCount.get();
        return total > 0 ? (double) successCount.get() / total : 0.0;
    }
    
    public double getAverageExecutionTime() {
        long total = totalCount.get();
        return total > 0 ? (double) totalExecutionTime.sum() / total : 0.0;
    }
    
    public long getMinExecutionTime() {
        return minExecutionTime == Long.MAX_VALUE ? 0 : minExecutionTime;
    }
    
    public long getMaxExecutionTime() {
        return maxExecutionTime == Long.MIN_VALUE ? 0 : maxExecutionTime;
    }
    
    public long getP95ExecutionTime() {
        
        synchronized (recentExecutionTimes) {
            if (recentExecutionTimes.isEmpty()) {
                return 0;
            }
            
            List<Long> sortedTimes = new ArrayList<>(recentExecutionTimes);
            sortedTimes.sort(Long::compareTo);
            
            int p95Index = (int) Math.ceil(sortedTimes.size() * 0.95) - 1;
            return sortedTimes.get(Math.max(0, p95Index));
        }
    }
    
    public Map<String, Long> getErrorsByType() {
        
        Map<String, Long> errors = new HashMap<>();
        
        for (Map.Entry<String, AtomicLong> entry : errorsByType.entrySet()) {
            errors.put(entry.getKey(), entry.getValue().get());
        }
        
        return errors;
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar resilência completa

#### **Passo 1: Configurar Retry Policy**
```yaml
# application.yml
command-bus:
  retry:
    enabled: true
    max-attempts: 3
    initial-delay-ms: 100
    max-delay-ms: 5000
    backoff-multiplier: 2.0
    jitter-percent: 0.1
  
  circuit-breaker:
    enabled: true
    failure-threshold: 0.5  # 50% de falhas
    recovery-timeout-seconds: 60
    minimum-throughput: 10
```

#### **Passo 2: Testar Retry**
```java
@Test
public void testarRetryPolicy() {
    
    // Mock handler que falha 2 vezes e depois sucede
    CommandHandler<TestCommand> mockHandler = Mockito.mock(CommandHandler.class);
    
    when(mockHandler.handle(any()))
        .thenThrow(new DataAccessException("Erro temporário"))
        .thenThrow(new DataAccessException("Erro temporário"))
        .thenReturn(CommandResult.success());
    
    // Executar com retry
    TestCommand command = new TestCommand("user123", "Teste retry");
    CommandResult result = retryPolicy.executeWithRetry(command, mockHandler);
    
    // Deve ter sucesso após 3 tentativas
    assertThat(result.isSuccess()).isTrue();
    verify(mockHandler, times(3)).handle(command);
}
```

#### **Passo 3: Testar Circuit Breaker**
```java
@Test
public void testarCircuitBreaker() {
    
    // Handler que sempre falha
    CommandHandler<TestCommand> failingHandler = command -> 
        CommandResult.failure("Sempre falha", "TEST_ERROR");
    
    TestCommand command = new TestCommand("user123", "Teste circuit breaker");
    
    // Executar até abrir circuit breaker
    for (int i = 0; i < 20; i++) {
        CommandResult result = circuitBreaker.executeWithCircuitBreaker(command, failingHandler);
        
        if (i < 10) {
            // Primeiras execuções devem falhar normalmente
            assertThat(result.getErrorCode()).isEqualTo("TEST_ERROR");
        } else {
            // Após threshold, deve retornar circuit breaker aberto
            assertThat(result.getErrorCode()).isEqualTo("CIRCUIT_BREAKER_OPEN");
        }
    }
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Implementar** validação em camadas ordenadas
2. **Configurar** políticas de retry com backoff exponencial
3. **Usar** circuit breaker para proteção de handlers
4. **Coletar** métricas detalhadas de performance
5. **Monitorar** saúde do Command Bus

### **❓ Perguntas para Reflexão:**

1. Como balancear número de retries vs latência?
2. Quando usar circuit breaker vs timeout simples?
3. Qual a importância da ordem de validação?
4. Como detectar degradação de performance?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 4**, vamos explorar:
- Configuração avançada do Command Bus
- Otimizações de performance
- Padrões de deployment
- Troubleshooting comum

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** Command Bus Partes 1-2