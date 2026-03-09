# 📖 CAPÍTULO 05: COMMAND BUS - PARTE 2
## Implementação e Registro de Handlers

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar o Command Bus completo
- Configurar sistema de registro de handlers
- Desenvolver middleware e interceptadores
- Configurar inicialização automática

---

## 🚌 **IMPLEMENTAÇÃO DO COMMAND BUS**

### **🔧 SimpleCommandBus - Implementação Principal**

```java
// Localização: command/impl/SimpleCommandBus.java
@Component
public class SimpleCommandBus implements CommandBus {
    
    private final CommandHandlerRegistry handlerRegistry;
    private final CommandBusMetrics metrics;
    private final List<CommandMiddleware> middlewares;
    private final TaskExecutor asyncExecutor;
    private final CommandBusProperties properties;
    
    public SimpleCommandBus(CommandHandlerRegistry handlerRegistry,
                           CommandBusMetrics metrics,
                           List<CommandMiddleware> middlewares,
                           @Qualifier("commandExecutor") TaskExecutor asyncExecutor,
                           CommandBusProperties properties) {
        this.handlerRegistry = handlerRegistry;
        this.metrics = metrics;
        this.middlewares = middlewares != null ? middlewares : Collections.emptyList();
        this.asyncExecutor = asyncExecutor;
        this.properties = properties;
    }
    
    @Override
    public CommandResult send(Command command) {
        
        Timer.Sample sample = metrics.startExecutionTimer();
        String commandType = command.getCommandType();
        
        try {
            // 1. Validar comando básico
            validateCommand(command);
            
            // 2. Encontrar handler
            CommandHandler<Command> handler = getHandlerForCommand(command);
            
            // 3. Aplicar middlewares (pré-processamento)
            CommandContext context = new CommandContext(command, handler);
            applyPreProcessingMiddlewares(context);
            
            // 4. Executar handler com timeout
            CommandResult result = executeWithTimeout(command, handler);
            
            // 5. Aplicar middlewares (pós-processamento)
            context.setResult(result);
            applyPostProcessingMiddlewares(context);
            
            // 6. Atualizar métricas de sucesso
            updateSuccessMetrics(command, sample.stop(metrics.getExecutionTimer()));
            
            return result.withExecutionTime(sample.stop(metrics.getExecutionTimer()))
                        .withCorrelationId(command.getCorrelationId());
            
        } catch (CommandHandlerNotFoundException e) {
            updateRejectedMetrics(command, sample.stop(metrics.getExecutionTimer()));
            return CommandResult.failure("Handler não encontrado para: " + commandType, "HANDLER_NOT_FOUND");
            
        } catch (CommandTimeoutException e) {
            updateTimeoutMetrics(command, sample.stop(metrics.getExecutionTimer()));
            return CommandResult.failure("Timeout ao processar comando", "COMMAND_TIMEOUT");
            
        } catch (CommandValidationException e) {
            updateFailureMetrics(command, sample.stop(metrics.getExecutionTimer()));
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR");
            
        } catch (Exception e) {
            log.error("Erro inesperado ao processar comando {}: {}", command.getCommandId(), e.getMessage(), e);
            updateFailureMetrics(command, sample.stop(metrics.getExecutionTimer()));
            return CommandResult.failure("Erro interno do sistema", "INTERNAL_ERROR");
        }
    }
    
    @Override
    public CompletableFuture<CommandResult> sendAsync(Command command) {
        
        return CompletableFuture.supplyAsync(() -> send(command), asyncExecutor)
            .exceptionally(throwable -> {
                log.error("Erro na execução assíncrona do comando {}: {}", 
                         command.getCommandId(), throwable.getMessage(), throwable);
                return CommandResult.failure("Erro na execução assíncrona", "ASYNC_ERROR");
            });
    }
    
    private void validateCommand(Command command) {
        
        if (command == null) {
            throw new CommandValidationException("Comando não pode ser nulo");
        }
        
        if (command.getCommandId() == null) {
            throw new CommandValidationException("Command ID é obrigatório");
        }
        
        if (command.getTimestamp() == null) {
            throw new CommandValidationException("Timestamp é obrigatório");
        }
        
        if (StringUtils.isBlank(command.getUserId())) {
            throw new CommandValidationException("User ID é obrigatório");
        }
        
        // Validar se comando não é muito antigo
        Duration age = Duration.between(command.getTimestamp(), Instant.now());
        if (age.toMinutes() > properties.getMaxCommandAgeMinutes()) {
            throw new CommandValidationException("Comando muito antigo: " + age.toMinutes() + " minutos");
        }
    }
    
    @SuppressWarnings("unchecked")
    private CommandHandler<Command> getHandlerForCommand(Command command) {
        
        CommandHandler<? extends Command> handler = handlerRegistry.getHandler(command.getClass());
        
        if (handler == null) {
            throw new CommandHandlerNotFoundException(command.getClass());
        }
        
        return (CommandHandler<Command>) handler;
    }
    
    private CommandResult executeWithTimeout(Command command, CommandHandler<Command> handler) {
        
        int timeoutSeconds = handler.getTimeoutSeconds();
        
        try {
            CompletableFuture<CommandResult> future = CompletableFuture
                .supplyAsync(() -> handler.handle(command), asyncExecutor);
            
            return future.get(timeoutSeconds, TimeUnit.SECONDS);
            
        } catch (TimeoutException e) {
            throw new CommandTimeoutException(
                command.getCommandId().toString(),
                command.getClass(),
                timeoutSeconds,
                timeoutSeconds * 1000L
            );
            
        } catch (ExecutionException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw (RuntimeException) cause;
            } else {
                throw new CommandExecutionException(
                    command.getCommandId().toString(),
                    command.getClass(),
                    cause
                );
            }
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new CommandExecutionException(
                command.getCommandId().toString(),
                command.getClass(),
                e
            );
        }
    }
    
    private void applyPreProcessingMiddlewares(CommandContext context) {
        
        for (CommandMiddleware middleware : middlewares) {
            try {
                middleware.preProcess(context);
            } catch (Exception e) {
                log.error("Erro no middleware {} durante pré-processamento: {}", 
                         middleware.getClass().getSimpleName(), e.getMessage(), e);
                // Continuar com outros middlewares
            }
        }
    }
    
    private void applyPostProcessingMiddlewares(CommandContext context) {
        
        // Aplicar em ordem reversa para pós-processamento
        List<CommandMiddleware> reversedMiddlewares = new ArrayList<>(middlewares);
        Collections.reverse(reversedMiddlewares);
        
        for (CommandMiddleware middleware : reversedMiddlewares) {
            try {
                middleware.postProcess(context);
            } catch (Exception e) {
                log.error("Erro no middleware {} durante pós-processamento: {}", 
                         middleware.getClass().getSimpleName(), e.getMessage(), e);
                // Continuar com outros middlewares
            }
        }
    }
    
    // Métodos de atualização de métricas
    private void updateSuccessMetrics(Command command, long executionTime) {
        metrics.incrementCommandsProcessed();
        metrics.incrementCommandsProcessed(command.getCommandType());
        metrics.recordExecutionTime(command.getCommandType(), executionTime);
    }
    
    private void updateFailureMetrics(Command command, long executionTime) {
        metrics.incrementCommandsFailed();
        metrics.incrementCommandsFailed(command.getCommandType(), "EXECUTION_ERROR");
        metrics.recordExecutionTime(command.getCommandType(), executionTime);
    }
    
    private void updateRejectedMetrics(Command command, long executionTime) {
        metrics.incrementCommandsRejected();
        metrics.incrementCommandsRejected(command.getCommandType());
    }
    
    private void updateTimeoutMetrics(Command command, long executionTime) {
        metrics.incrementCommandsTimeout();
        metrics.incrementCommandsTimeout(command.getCommandType());
    }
    
    // Implementação dos outros métodos da interface
    @Override
    public <T extends Command> void registerHandler(CommandHandler<T> handler) {
        handlerRegistry.registerHandler(handler);
    }
    
    @Override
    public void unregisterHandler(Class<? extends Command> commandType) {
        handlerRegistry.unregisterHandler(commandType);
    }
    
    @Override
    public boolean hasHandler(Class<? extends Command> commandType) {
        return handlerRegistry.hasHandler(commandType);
    }
    
    @Override
    public CommandBusStatistics getStatistics() {
        return metrics.getStatistics();
    }
    
    /**
     * Shutdown gracioso do Command Bus
     */
    @PreDestroy
    public void shutdown() {
        
        log.info("Iniciando shutdown do Command Bus...");
        
        try {
            // Aguardar conclusão de comandos em execução
            if (asyncExecutor instanceof ThreadPoolTaskExecutor) {
                ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) asyncExecutor;
                executor.shutdown();
                
                boolean terminated = executor.getThreadPoolExecutor()
                    .awaitTermination(30, TimeUnit.SECONDS);
                
                if (!terminated) {
                    log.warn("Timeout no shutdown - forçando encerramento");
                    executor.getThreadPoolExecutor().shutdownNow();
                }
            }
            
            log.info("Command Bus finalizado com sucesso");
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            log.error("Shutdown interrompido", e);
        }
    }
}
```

---

## 📋 **SISTEMA DE REGISTRO DE HANDLERS**

### **🗂️ CommandHandlerRegistry**

```java
// Localização: command/CommandHandlerRegistry.java
@Component
public class CommandHandlerRegistry {
    
    private final Map<Class<? extends Command>, CommandHandler<? extends Command>> handlers;
    private final ReadWriteLock lock;
    private final CommandHandlerValidator validator;
    
    public CommandHandlerRegistry(CommandHandlerValidator validator) {
        this.handlers = new ConcurrentHashMap<>();
        this.lock = new ReentrantReadWriteLock();
        this.validator = validator;
    }
    
    /**
     * Registra um handler para um tipo de comando
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> void registerHandler(CommandHandler<T> handler) {
        
        Objects.requireNonNull(handler, "Handler não pode ser nulo");
        
        Class<T> commandType = handler.getCommandType();
        Objects.requireNonNull(commandType, "Tipo de comando não pode ser nulo");
        
        // Validar handler
        List<String> validationErrors = validator.validate(handler);
        if (!validationErrors.isEmpty()) {
            throw new InvalidCommandHandlerException(
                "Handler inválido para " + commandType.getSimpleName() + ": " + 
                String.join(", ", validationErrors)
            );
        }
        
        lock.writeLock().lock();
        try {
            // Verificar se já existe handler para o tipo
            if (handlers.containsKey(commandType)) {
                CommandHandler<? extends Command> existingHandler = handlers.get(commandType);
                
                // Permitir substituição apenas se nova prioridade for maior
                if (handler.getPriority() <= existingHandler.getPriority()) {
                    throw new DuplicateCommandHandlerException(
                        "Já existe handler para " + commandType.getSimpleName() + 
                        " com prioridade maior ou igual"
                    );
                }
                
                log.warn("Substituindo handler para {} - prioridade {} -> {}", 
                        commandType.getSimpleName(), 
                        existingHandler.getPriority(), 
                        handler.getPriority());
            }
            
            handlers.put(commandType, handler);
            
            log.info("Handler registrado: {} -> {} (prioridade: {})", 
                    commandType.getSimpleName(), 
                    handler.getClass().getSimpleName(),
                    handler.getPriority());
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtém handler para um tipo de comando
     */
    @SuppressWarnings("unchecked")
    public <T extends Command> CommandHandler<T> getHandler(Class<T> commandType) {
        
        lock.readLock().lock();
        try {
            return (CommandHandler<T>) handlers.get(commandType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Remove handler para um tipo de comando
     */
    public CommandHandler<? extends Command> unregisterHandler(Class<? extends Command> commandType) {
        
        lock.writeLock().lock();
        try {
            CommandHandler<? extends Command> removed = handlers.remove(commandType);
            
            if (removed != null) {
                log.info("Handler removido: {} -> {}", 
                        commandType.getSimpleName(), 
                        removed.getClass().getSimpleName());
            }
            
            return removed;
            
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Verifica se existe handler para um tipo de comando
     */
    public boolean hasHandler(Class<? extends Command> commandType) {
        
        lock.readLock().lock();
        try {
            return handlers.containsKey(commandType);
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Obtém todos os tipos de comando registrados
     */
    public Set<Class<? extends Command>> getRegisteredCommandTypes() {
        
        lock.readLock().lock();
        try {
            return new HashSet<>(handlers.keySet());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Obtém número total de handlers registrados
     */
    public int getHandlerCount() {
        
        lock.readLock().lock();
        try {
            return handlers.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Limpa todos os handlers (usado em testes)
     */
    public void clear() {
        
        lock.writeLock().lock();
        try {
            int count = handlers.size();
            handlers.clear();
            log.info("Registry limpo - {} handlers removidos", count);
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Obtém informações de debug do registry
     */
    public Map<String, String> getDebugInfo() {
        
        lock.readLock().lock();
        try {
            Map<String, String> info = new HashMap<>();
            
            for (Map.Entry<Class<? extends Command>, CommandHandler<? extends Command>> entry : handlers.entrySet()) {
                info.put(
                    entry.getKey().getSimpleName(),
                    entry.getValue().getClass().getSimpleName() + " (prioridade: " + entry.getValue().getPriority() + ")"
                );
            }
            
            return info;
            
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * Valida configuração atual do registry
     */
    public List<String> validateConfiguration() {
        
        List<String> issues = new ArrayList<>();
        
        lock.readLock().lock();
        try {
            // Verificar handlers duplicados por prioridade
            Map<Integer, List<String>> handlersByPriority = new HashMap<>();
            
            for (Map.Entry<Class<? extends Command>, CommandHandler<? extends Command>> entry : handlers.entrySet()) {
                int priority = entry.getValue().getPriority();
                handlersByPriority.computeIfAbsent(priority, k -> new ArrayList<>())
                    .add(entry.getKey().getSimpleName());
            }
            
            for (Map.Entry<Integer, List<String>> entry : handlersByPriority.entrySet()) {
                if (entry.getValue().size() > 1) {
                    issues.add("Múltiplos handlers com prioridade " + entry.getKey() + ": " + 
                              String.join(", ", entry.getValue()));
                }
            }
            
            // Verificar handlers sem timeout configurado
            for (Map.Entry<Class<? extends Command>, CommandHandler<? extends Command>> entry : handlers.entrySet()) {
                if (entry.getValue().getTimeoutSeconds() <= 0) {
                    issues.add("Handler " + entry.getKey().getSimpleName() + " tem timeout inválido: " + 
                              entry.getValue().getTimeoutSeconds());
                }
            }
            
        } finally {
            lock.readLock().unlock();
        }
        
        return issues;
    }
}
```

### **✅ Validador de Handlers**

```java
// Localização: command/validation/CommandHandlerValidator.java
@Component
public class CommandHandlerValidator {
    
    /**
     * Valida um Command Handler
     */
    public List<String> validate(CommandHandler<? extends Command> handler) {
        
        List<String> errors = new ArrayList<>();
        
        // Validar tipo de comando
        if (handler.getCommandType() == null) {
            errors.add("Tipo de comando não pode ser nulo");
        } else {
            validateCommandType(handler.getCommandType(), errors);
        }
        
        // Validar timeout
        if (handler.getTimeoutSeconds() <= 0) {
            errors.add("Timeout deve ser positivo");
        } else if (handler.getTimeoutSeconds() > 300) { // 5 minutos
            errors.add("Timeout muito alto: " + handler.getTimeoutSeconds() + "s (máximo: 300s)");
        }
        
        // Validar prioridade
        if (handler.getPriority() < 0) {
            errors.add("Prioridade deve ser não-negativa");
        }
        
        // Validar se handler implementa método handle corretamente
        validateHandleMethod(handler, errors);
        
        // Validar anotações Spring se presente
        validateSpringAnnotations(handler, errors);
        
        return errors;
    }
    
    private void validateCommandType(Class<? extends Command> commandType, List<String> errors) {
        
        // Verificar se é interface ou classe abstrata
        if (commandType.isInterface()) {
            errors.add("Tipo de comando não pode ser interface: " + commandType.getSimpleName());
        }
        
        if (Modifier.isAbstract(commandType.getModifiers())) {
            errors.add("Tipo de comando não pode ser abstrato: " + commandType.getSimpleName());
        }
        
        // Verificar se implementa Command
        if (!Command.class.isAssignableFrom(commandType)) {
            errors.add("Tipo deve implementar Command: " + commandType.getSimpleName());
        }
        
        // Verificar se tem construtor público
        try {
            Constructor<?>[] constructors = commandType.getConstructors();
            if (constructors.length == 0) {
                errors.add("Comando deve ter pelo menos um construtor público: " + commandType.getSimpleName());
            }
        } catch (SecurityException e) {
            errors.add("Não foi possível verificar construtores de: " + commandType.getSimpleName());
        }
    }
    
    private void validateHandleMethod(CommandHandler<? extends Command> handler, List<String> errors) {
        
        try {
            Method handleMethod = handler.getClass().getMethod("handle", handler.getCommandType());
            
            // Verificar se método é público
            if (!Modifier.isPublic(handleMethod.getModifiers())) {
                errors.add("Método handle deve ser público");
            }
            
            // Verificar tipo de retorno
            if (!CommandResult.class.equals(handleMethod.getReturnType())) {
                errors.add("Método handle deve retornar CommandResult");
            }
            
        } catch (NoSuchMethodException e) {
            errors.add("Método handle não encontrado ou com assinatura incorreta");
        } catch (SecurityException e) {
            errors.add("Não foi possível verificar método handle");
        }
    }
    
    private void validateSpringAnnotations(CommandHandler<? extends Command> handler, List<String> errors) {
        
        Class<?> handlerClass = handler.getClass();
        
        // Verificar se tem anotação @Component ou similar
        if (!handlerClass.isAnnotationPresent(Component.class) &&
            !handlerClass.isAnnotationPresent(Service.class) &&
            !handlerClass.isAnnotationPresent(Repository.class)) {
            
            errors.add("Handler deve ter anotação @Component, @Service ou @Repository para ser gerenciado pelo Spring");
        }
        
        // Verificar se não tem anotações conflitantes
        if (handlerClass.isAnnotationPresent(Scope.class)) {
            Scope scope = handlerClass.getAnnotation(Scope.class);
            if (!ConfigurableBeanFactory.SCOPE_SINGLETON.equals(scope.value())) {
                errors.add("Handler deve ser singleton (scope atual: " + scope.value() + ")");
            }
        }
    }
}
```

---

## 🔧 **MIDDLEWARE SYSTEM**

### **🎭 Interface de Middleware**

```java
// Localização: command/middleware/CommandMiddleware.java
public interface CommandMiddleware {
    
    /**
     * Executado antes do processamento do comando
     */
    void preProcess(CommandContext context);
    
    /**
     * Executado após o processamento do comando
     */
    void postProcess(CommandContext context);
    
    /**
     * Ordem de execução (menor = executa primeiro)
     */
    default int getOrder() {
        return 100;
    }
    
    /**
     * Nome do middleware para logs
     */
    default String getName() {
        return getClass().getSimpleName();
    }
    
    /**
     * Indica se middleware está habilitado
     */
    default boolean isEnabled() {
        return true;
    }
}

// Contexto compartilhado entre middlewares
public class CommandContext {
    
    private final Command command;
    private final CommandHandler<? extends Command> handler;
    private CommandResult result;
    private final Map<String, Object> attributes;
    private final Instant startTime;
    
    public CommandContext(Command command, CommandHandler<? extends Command> handler) {
        this.command = command;
        this.handler = handler;
        this.attributes = new HashMap<>();
        this.startTime = Instant.now();
    }
    
    // Getters e setters
    public Command getCommand() { return command; }
    public CommandHandler<? extends Command> getHandler() { return handler; }
    public CommandResult getResult() { return result; }
    public void setResult(CommandResult result) { this.result = result; }
    public Instant getStartTime() { return startTime; }
    
    // Atributos customizados
    public void setAttribute(String key, Object value) {
        attributes.put(key, value);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getAttribute(String key, Class<T> type) {
        Object value = attributes.get(key);
        return type.isInstance(value) ? (T) value : null;
    }
    
    public boolean hasAttribute(String key) {
        return attributes.containsKey(key);
    }
    
    // Métodos de conveniência
    public Duration getExecutionTime() {
        return Duration.between(startTime, Instant.now());
    }
    
    public String getCommandType() {
        return command.getCommandType();
    }
    
    public String getHandlerName() {
        return handler.getClass().getSimpleName();
    }
}
```

### **📊 Middlewares Específicos**

```java
// Middleware de logging
@Component
public class LoggingCommandMiddleware implements CommandMiddleware {
    
    private final Logger log = LoggerFactory.getLogger(LoggingCommandMiddleware.class);
    
    @Override
    public void preProcess(CommandContext context) {
        
        log.info("Iniciando processamento do comando: {} [ID: {}, User: {}]",
                context.getCommandType(),
                context.getCommand().getCommandId(),
                context.getCommand().getUserId());
        
        if (log.isDebugEnabled()) {
            log.debug("Detalhes do comando: {}", context.getCommand());
            log.debug("Handler: {}", context.getHandlerName());
        }
    }
    
    @Override
    public void postProcess(CommandContext context) {
        
        CommandResult result = context.getResult();
        Duration executionTime = context.getExecutionTime();
        
        if (result.isSuccess()) {
            log.info("Comando processado com sucesso: {} [ID: {}, Tempo: {}ms]",
                    context.getCommandType(),
                    context.getCommand().getCommandId(),
                    executionTime.toMillis());
        } else {
            log.warn("Falha no processamento do comando: {} [ID: {}, Erro: {}, Tempo: {}ms]",
                    context.getCommandType(),
                    context.getCommand().getCommandId(),
                    result.getErrorMessage(),
                    executionTime.toMillis());
        }
    }
    
    @Override
    public int getOrder() {
        return 10; // Executar cedo
    }
}

// Middleware de validação
@Component
public class ValidationCommandMiddleware implements CommandMiddleware {
    
    private final List<CommandValidator<? extends Command>> validators;
    
    public ValidationCommandMiddleware(List<CommandValidator<? extends Command>> validators) {
        this.validators = validators != null ? validators : Collections.emptyList();
    }
    
    @Override
    public void preProcess(CommandContext context) {
        
        Command command = context.getCommand();
        
        // Encontrar validadores aplicáveis
        List<CommandValidator<Command>> applicableValidators = findApplicableValidators(command);
        
        if (applicableValidators.isEmpty()) {
            return;
        }
        
        // Executar validações
        List<String> errors = new ArrayList<>();
        
        for (CommandValidator<Command> validator : applicableValidators) {
            try {
                ValidationResult result = validator.validate(command);
                
                if (result.isInvalid()) {
                    errors.addAll(result.getErrorMessages());
                }
                
            } catch (Exception e) {
                log.error("Erro no validador {}: {}", validator.getClass().getSimpleName(), e.getMessage());
                errors.add("Erro interno na validação");
            }
        }
        
        // Se há erros, interromper processamento
        if (!errors.isEmpty()) {
            throw new CommandValidationException("Comando inválido: " + String.join(", ", errors));
        }
    }
    
    @Override
    public void postProcess(CommandContext context) {
        // Nada a fazer no pós-processamento
    }
    
    @SuppressWarnings("unchecked")
    private List<CommandValidator<Command>> findApplicableValidators(Command command) {
        
        return validators.stream()
            .filter(validator -> validator.supports(command.getClass()))
            .map(validator -> (CommandValidator<Command>) validator)
            .collect(Collectors.toList());
    }
    
    @Override
    public int getOrder() {
        return 20; // Executar após logging, antes de métricas
    }
}

// Middleware de métricas
@Component
public class MetricsCommandMiddleware implements CommandMiddleware {
    
    private final CommandBusMetrics metrics;
    
    public MetricsCommandMiddleware(CommandBusMetrics metrics) {
        this.metrics = metrics;
    }
    
    @Override
    public void preProcess(CommandContext context) {
        
        // Registrar início do comando
        context.setAttribute("metrics.timer", metrics.startExecutionTimer());
        
        // Incrementar contador de comandos ativos
        metrics.incrementActiveCommands();
    }
    
    @Override
    public void postProcess(CommandContext context) {
        
        // Parar timer
        Timer.Sample timer = context.getAttribute("metrics.timer", Timer.Sample.class);
        if (timer != null) {
            long executionTime = timer.stop(metrics.getExecutionTimer());
            
            // Registrar tempo de execução por tipo
            metrics.recordExecutionTime(context.getCommandType(), executionTime);
        }
        
        // Decrementar contador de comandos ativos
        metrics.decrementActiveCommands();
        
        // Registrar resultado
        CommandResult result = context.getResult();
        if (result != null) {
            if (result.isSuccess()) {
                metrics.incrementCommandsProcessed(context.getCommandType());
            } else {
                metrics.incrementCommandsFailed(context.getCommandType(), result.getErrorCode());
            }
        }
    }
    
    @Override
    public int getOrder() {
        return 30; // Executar após validação
    }
}

// Middleware de auditoria
@Component
public class AuditCommandMiddleware implements CommandMiddleware {
    
    private final AuditService auditService;
    
    public AuditCommandMiddleware(AuditService auditService) {
        this.auditService = auditService;
    }
    
    @Override
    public void preProcess(CommandContext context) {
        
        Command command = context.getCommand();
        
        // Registrar início da operação
        AuditEntry entry = AuditEntry.builder()
            .operationType("COMMAND_EXECUTION")
            .entityType(command.getCommandType())
            .entityId(command.getCommandId().toString())
            .userId(command.getUserId())
            .correlationId(command.getCorrelationId().toString())
            .timestamp(Instant.now())
            .status("STARTED")
            .details(Map.of(
                "command_type", command.getCommandType(),
                "handler", context.getHandlerName()
            ))
            .build();
        
        auditService.record(entry);
        context.setAttribute("audit.entry_id", entry.getId());
    }
    
    @Override
    public void postProcess(CommandContext context) {
        
        String entryId = context.getAttribute("audit.entry_id", String.class);
        if (entryId == null) {
            return;
        }
        
        Command command = context.getCommand();
        CommandResult result = context.getResult();
        
        // Atualizar entrada de auditoria
        Map<String, Object> details = new HashMap<>();
        details.put("execution_time_ms", context.getExecutionTime().toMillis());
        details.put("success", result.isSuccess());
        
        if (result.isFailure()) {
            details.put("error_message", result.getErrorMessage());
            details.put("error_code", result.getErrorCode());
        }
        
        auditService.updateEntry(entryId, result.isSuccess() ? "COMPLETED" : "FAILED", details);
    }
    
    @Override
    public int getOrder() {
        return 40; // Executar por último
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar middleware customizado

#### **Passo 1: Criar Middleware de Rate Limiting**
```java
@Component
public class RateLimitingCommandMiddleware implements CommandMiddleware {
    
    private final Map<String, RateLimiter> rateLimiters = new ConcurrentHashMap<>();
    
    @Override
    public void preProcess(CommandContext context) {
        
        String userId = context.getCommand().getUserId();
        String commandType = context.getCommandType();
        String key = userId + ":" + commandType;
        
        // Criar rate limiter se não existir (10 comandos por minuto)
        RateLimiter limiter = rateLimiters.computeIfAbsent(key, 
            k -> RateLimiter.create(10.0 / 60.0)); // 10 por minuto
        
        // Verificar se pode executar
        if (!limiter.tryAcquire()) {
            throw new CommandRateLimitException(
                "Rate limit excedido para usuário " + userId + 
                " e comando " + commandType
            );
        }
    }
    
    @Override
    public void postProcess(CommandContext context) {
        // Nada a fazer
    }
    
    @Override
    public int getOrder() {
        return 15; // Entre logging e validação
    }
}
```

#### **Passo 2: Testar Middleware**
```java
@Test
public void testarMiddlewareRateLimit() {
    
    // Criar comando
    TestCommand comando = new TestCommand("user123", "Teste rate limit");
    
    // Executar múltiplas vezes rapidamente
    for (int i = 0; i < 15; i++) {
        try {
            CommandResult result = commandBus.send(comando);
            
            if (i < 10) {
                assertThat(result.isSuccess()).isTrue();
            } else {
                assertThat(result.isFailure()).isTrue();
                assertThat(result.getErrorCode()).isEqualTo("RATE_LIMIT_EXCEEDED");
            }
            
        } catch (Exception e) {
            // Esperado após 10 comandos
            assertThat(i).isGreaterThanOrEqualTo(10);
        }
    }
}
```

#### **Passo 3: Configurar Ordem de Middlewares**
```java
@Configuration
public class CommandMiddlewareConfiguration {
    
    @Bean
    @Order(10)
    public LoggingCommandMiddleware loggingMiddleware() {
        return new LoggingCommandMiddleware();
    }
    
    @Bean
    @Order(15)
    public RateLimitingCommandMiddleware rateLimitingMiddleware() {
        return new RateLimitingCommandMiddleware();
    }
    
    @Bean
    @Order(20)
    public ValidationCommandMiddleware validationMiddleware(
            List<CommandValidator<? extends Command>> validators) {
        return new ValidationCommandMiddleware(validators);
    }
    
    @Bean
    @Order(30)
    public MetricsCommandMiddleware metricsMiddleware(CommandBusMetrics metrics) {
        return new MetricsCommandMiddleware(metrics);
    }
    
    @Bean
    @Order(40)
    public AuditCommandMiddleware auditMiddleware(AuditService auditService) {
        return new AuditCommandMiddleware(auditService);
    }
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Implementar** Command Bus completo com timeout e error handling
2. **Configurar** sistema de registro automático de handlers
3. **Criar** middlewares para cross-cutting concerns
4. **Validar** handlers e configurações
5. **Organizar** ordem de execução de middlewares

### **❓ Perguntas para Reflexão:**

1. Como garantir thread-safety no registry de handlers?
2. Qual a importância da ordem de execução dos middlewares?
3. Como implementar retry automático para comandos falhados?
4. Quando usar execução síncrona vs assíncrona?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 3**, vamos explorar:
- Validação avançada de comandos
- Políticas de retry e circuit breaker
- Monitoramento e métricas detalhadas
- Configurações de performance

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** Command Bus Parte 1