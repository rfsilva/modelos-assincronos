# 📖 CAPÍTULO 03: CQRS - PARTE 2
## Command Side - Implementação Detalhada

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar Command Side completo
- Configurar validação e tratamento de erros
- Implementar métricas e monitoramento
- Otimizar performance de comandos

---

## 🎛️ **CONFIGURAÇÃO DO COMMAND BUS**

### **⚙️ Configuração Spring**

```java
// Localização: command/config/CommandBusConfiguration.java
@Configuration
@EnableConfigurationProperties(CommandBusProperties.class)
public class CommandBusConfiguration {
    
    @Bean
    public CommandHandlerRegistry commandHandlerRegistry() {
        return new CommandHandlerRegistry();
    }
    
    @Bean
    public CommandBus commandBus(CommandHandlerRegistry handlerRegistry) {
        return new SimpleCommandBus(handlerRegistry);
    }
    
    @Bean
    public CommandBusMetrics commandBusMetrics(MeterRegistry meterRegistry) {
        return new CommandBusMetrics(meterRegistry);
    }
    
    @Bean
    public CommandBusHealthIndicator commandBusHealthIndicator(CommandBus commandBus) {
        return new CommandBusHealthIndicator(commandBus);
    }
    
    @Bean
    @ConfigurationProperties(prefix = "command-bus.executor")
    public TaskExecutor commandExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(20);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("command-");
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.CallerRunsPolicy());
        executor.initialize();
        return executor;
    }
    
    /**
     * Registra automaticamente todos os Command Handlers
     */
    @EventListener
    public void registerHandlers(ContextRefreshedEvent event) {
        
        ApplicationContext context = event.getApplicationContext();
        CommandHandlerRegistry registry = context.getBean(CommandHandlerRegistry.class);
        
        // Buscar todos os beans que implementam CommandHandler
        Map<String, CommandHandler> handlers = context.getBeansOfType(CommandHandler.class);
        
        for (CommandHandler<? extends Command> handler : handlers.values()) {
            try {
                registerHandlerSafely(registry, handler);
            } catch (Exception e) {
                log.error("Erro ao registrar handler {}: {}", 
                         handler.getClass().getSimpleName(), e.getMessage());
            }
        }
        
        log.info("Registrados {} command handlers", handlers.size());
    }
    
    @SuppressWarnings("unchecked")
    private void registerHandlerSafely(CommandHandlerRegistry registry, 
                                     CommandHandler<? extends Command> handler) {
        
        Class<? extends Command> commandType = extractCommandType(handler);
        
        if (commandType != null) {
            registry.registerHandler((CommandHandler<Command>) handler);
            log.debug("Handler registrado: {} -> {}", 
                     commandType.getSimpleName(), 
                     handler.getClass().getSimpleName());
        } else {
            log.warn("Não foi possível determinar tipo de comando para handler: {}", 
                    handler.getClass().getSimpleName());
        }
    }
    
    private Class<? extends Command> extractCommandType(CommandHandler<? extends Command> handler) {
        
        // Tentar obter via método getCommandType()
        try {
            return handler.getCommandType();
        } catch (Exception e) {
            log.debug("Erro ao obter command type via método: {}", e.getMessage());
        }
        
        // Fallback: extrair via reflection dos generics
        return extractCommandTypeFromGenericInterface(handler);
    }
    
    @SuppressWarnings("unchecked")
    private Class<? extends Command> extractCommandTypeFromGenericInterface(
            CommandHandler<? extends Command> handler) {
        
        Type[] interfaces = handler.getClass().getGenericInterfaces();
        
        for (Type interfaceType : interfaces) {
            if (interfaceType instanceof ParameterizedType) {
                ParameterizedType paramType = (ParameterizedType) interfaceType;
                
                if (CommandHandler.class.equals(paramType.getRawType())) {
                    Type[] typeArgs = paramType.getActualTypeArguments();
                    if (typeArgs.length > 0 && typeArgs[0] instanceof Class) {
                        return (Class<? extends Command>) typeArgs[0];
                    }
                }
            }
        }
        
        return null;
    }
}
```

### **📋 Propriedades de Configuração**

```yaml
# Localização: command-bus.yml
command-bus:
  enabled: true
  
  # Configurações de timeout
  timeout:
    default-seconds: 30
    max-seconds: 300
  
  # Pool de threads para execução assíncrona
  executor:
    core-pool-size: 5
    max-pool-size: 20
    queue-capacity: 100
    keep-alive-seconds: 60
    thread-name-prefix: "command-"
  
  # Configurações de retry
  retry:
    enabled: true
    max-attempts: 3
    initial-delay-ms: 100
    max-delay-ms: 5000
    backoff-multiplier: 2.0
    jitter-percent: 0.1
  
  # Monitoramento
  monitoring:
    metrics-enabled: true
    detailed-logging: false
    health-check-enabled: true
    error-rate-threshold: 0.05
  
  # Validação
  validation:
    enabled: true
    fail-fast: true
    detailed-errors: true
```

---

## ✅ **VALIDAÇÃO DE COMANDOS**

### **🔍 Sistema de Validação**

```java
// Localização: command/validation/CommandValidator.java
public interface CommandValidator<T extends Command> {
    
    /**
     * Valida o comando e retorna resultado
     */
    ValidationResult validate(T command);
    
    /**
     * Tipo de comando validado
     */
    Class<T> getCommandType();
    
    /**
     * Prioridade do validador (menor = maior prioridade)
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Indica se suporta o tipo de comando
     */
    default boolean supports(Class<? extends Command> commandType) {
        return getCommandType().equals(commandType);
    }
}

// Implementação para validação de sinistro
@Component
public class CriarSinistroCommandValidator implements CommandValidator<CriarSinistroCommand> {
    
    private final SeguradoService seguradoService;
    private final VeiculoService veiculoService;
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        
        ValidationResult result = ValidationResult.valid();
        
        // Validações básicas
        result = result.combine(validateBasicFields(command));
        
        // Validações de negócio
        result = result.combine(validateBusinessRules(command));
        
        // Validações de integridade referencial
        result = result.combine(validateReferentialIntegrity(command));
        
        return result;
    }
    
    private ValidationResult validateBasicFields(CriarSinistroCommand command) {
        
        List<String> errors = new ArrayList<>();
        
        // CPF obrigatório e válido
        if (StringUtils.isBlank(command.getCpfSegurado())) {
            errors.add("CPF do segurado é obrigatório");
        } else if (!CpfValidator.isValid(command.getCpfSegurado())) {
            errors.add("CPF do segurado é inválido");
        }
        
        // Placa obrigatória e válida
        if (StringUtils.isBlank(command.getPlacaVeiculo())) {
            errors.add("Placa do veículo é obrigatória");
        } else if (!PlacaValidator.isValid(command.getPlacaVeiculo())) {
            errors.add("Placa do veículo é inválida");
        }
        
        // Descrição obrigatória
        if (StringUtils.isBlank(command.getDescricaoOcorrencia())) {
            errors.add("Descrição da ocorrência é obrigatória");
        } else if (command.getDescricaoOcorrencia().length() < 10) {
            errors.add("Descrição da ocorrência deve ter pelo menos 10 caracteres");
        } else if (command.getDescricaoOcorrencia().length() > 2000) {
            errors.add("Descrição da ocorrência não pode exceder 2000 caracteres");
        }
        
        // Data da ocorrência
        if (command.getDataOcorrencia() == null) {
            errors.add("Data da ocorrência é obrigatória");
        } else {
            LocalDateTime agora = LocalDateTime.now();
            LocalDateTime limite = agora.minusYears(1); // Máximo 1 ano atrás
            
            if (command.getDataOcorrencia().isAfter(agora)) {
                errors.add("Data da ocorrência não pode ser futura");
            } else if (command.getDataOcorrencia().isBefore(limite)) {
                errors.add("Data da ocorrência não pode ser anterior a 1 ano");
            }
        }
        
        // Valor estimado
        if (command.getValorEstimado() != null) {
            if (command.getValorEstimado().compareTo(BigDecimal.ZERO) <= 0) {
                errors.add("Valor estimado deve ser positivo");
            } else if (command.getValorEstimado().compareTo(new BigDecimal("1000000")) > 0) {
                errors.add("Valor estimado não pode exceder R$ 1.000.000,00");
            }
        }
        
        return errors.isEmpty() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid(errors);
    }
    
    private ValidationResult validateBusinessRules(CriarSinistroCommand command) {
        
        List<String> errors = new ArrayList<>();
        
        // Regra: Não pode haver mais de 3 sinistros abertos para o mesmo CPF
        long sinistrosAbertos = sinistroService.contarSinistrosAbertos(command.getCpfSegurado());
        if (sinistrosAbertos >= 3) {
            errors.add("Segurado já possui 3 sinistros abertos. Limite máximo atingido.");
        }
        
        // Regra: Não pode haver sinistro para o mesmo veículo nas últimas 24h
        boolean sinistroRecente = sinistroService.existeSinistroRecente(
            command.getPlacaVeiculo(), 
            Duration.ofHours(24)
        );
        if (sinistroRecente) {
            errors.add("Já existe sinistro para este veículo nas últimas 24 horas");
        }
        
        // Regra: Horário comercial para sinistros de alta complexidade
        if (command.getValorEstimado() != null && 
            command.getValorEstimado().compareTo(new BigDecimal("50000")) > 0) {
            
            LocalTime agora = LocalTime.now();
            if (agora.isBefore(LocalTime.of(8, 0)) || agora.isAfter(LocalTime.of(18, 0))) {
                errors.add("Sinistros de alto valor só podem ser abertos em horário comercial (8h-18h)");
            }
        }
        
        return errors.isEmpty() ? 
            ValidationResult.valid() : 
            ValidationResult.invalid(errors);
    }
    
    private ValidationResult validateReferentialIntegrity(CriarSinistroCommand command) {
        
        List<String> errors = new ArrayList<>();
        
        try {
            // Verificar se segurado existe
            Segurado segurado = seguradoService.buscarPorCpf(command.getCpfSegurado());
            if (segurado == null) {
                errors.add("Segurado não encontrado");
            } else if (!segurado.isAtivo()) {
                errors.add("Segurado não está ativo");
            }
            
            // Verificar se veículo existe e pertence ao segurado
            Veiculo veiculo = veiculoService.buscarPorPlaca(command.getPlacaVeiculo());
            if (veiculo == null) {
                errors.add("Veículo não encontrado");
            } else if (segurado != null && !veiculo.pertenceAoSegurado(segurado.getId())) {
                errors.add("Veículo não pertence ao segurado informado");
            }
            
            // Verificar se há apólice vigente
            if (segurado != null && veiculo != null) {
                boolean apoliceVigente = apoliceService.existeApoliceVigente(
                    segurado.getId(), 
                    veiculo.getId(), 
                    command.getDataOcorrencia().toLocalDate()
                );
                
                if (!apoliceVigente) {
                    errors.add("Não há apólice vigente para o veículo na data da ocorrência");
                }
            }
            
        } catch (Exception e) {
            log.error("Erro na validação de integridade referencial: {}", e.getMessage());
            errors.add("Erro interno na validação. Tente novamente.");
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
        return 10; // Alta prioridade
    }
}
```

### **📊 Resultado de Validação**

```java
// Localização: command/validation/ValidationResult.java
public class ValidationResult {
    
    private final boolean valid;
    private final List<String> errorMessages;
    private final String errorCode;
    private final Map<String, Object> metadata;
    private final String validatorName;
    
    private ValidationResult(boolean valid, 
                           List<String> errorMessages, 
                           String errorCode,
                           Map<String, Object> metadata,
                           String validatorName) {
        this.valid = valid;
        this.errorMessages = errorMessages != null ? 
            new ArrayList<>(errorMessages) : new ArrayList<>();
        this.errorCode = errorCode;
        this.metadata = metadata != null ? 
            new HashMap<>(metadata) : new HashMap<>();
        this.validatorName = validatorName;
    }
    
    // Factory methods
    public static ValidationResult valid() {
        return new ValidationResult(true, null, null, null, null);
    }
    
    public static ValidationResult valid(Map<String, Object> metadata) {
        return new ValidationResult(true, null, null, metadata, null);
    }
    
    public static ValidationResult invalid(String errorMessage) {
        return new ValidationResult(false, Arrays.asList(errorMessage), null, null, null);
    }
    
    public static ValidationResult invalid(List<String> errorMessages) {
        return new ValidationResult(false, errorMessages, null, null, null);
    }
    
    public static ValidationResult invalid(String errorMessage, String errorCode) {
        return new ValidationResult(false, Arrays.asList(errorMessage), errorCode, null, null);
    }
    
    // Métodos de combinação
    public ValidationResult combine(ValidationResult other) {
        if (this.valid && other.valid) {
            Map<String, Object> combinedMetadata = new HashMap<>(this.metadata);
            combinedMetadata.putAll(other.metadata);
            return new ValidationResult(true, null, null, combinedMetadata, null);
        }
        
        List<String> combinedErrors = new ArrayList<>(this.errorMessages);
        combinedErrors.addAll(other.errorMessages);
        
        return new ValidationResult(false, combinedErrors, null, null, null);
    }
    
    public ValidationResult addErrorMessage(String message) {
        if (this.valid) {
            return ValidationResult.invalid(message);
        }
        
        List<String> newErrors = new ArrayList<>(this.errorMessages);
        newErrors.add(message);
        return new ValidationResult(false, newErrors, this.errorCode, this.metadata, this.validatorName);
    }
    
    public ValidationResult withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return new ValidationResult(this.valid, this.errorMessages, this.errorCode, newMetadata, this.validatorName);
    }
    
    public ValidationResult withValidatorName(String validatorName) {
        return new ValidationResult(this.valid, this.errorMessages, this.errorCode, this.metadata, validatorName);
    }
    
    // Getters
    public boolean isValid() {
        return valid;
    }
    
    public boolean isInvalid() {
        return !valid;
    }
    
    public List<String> getErrorMessages() {
        return new ArrayList<>(errorMessages);
    }
    
    public boolean hasErrorMessages() {
        return !errorMessages.isEmpty();
    }
    
    public String getFirstErrorMessage() {
        return errorMessages.isEmpty() ? null : errorMessages.get(0);
    }
    
    // Outros getters...
}
```

---

## 📊 **MÉTRICAS E MONITORAMENTO**

### **📈 Command Bus Metrics**

```java
// Localização: command/config/CommandBusMetrics.java
@Component
public class CommandBusMetrics implements MeterBinder {
    
    private final MeterRegistry meterRegistry;
    
    // Contadores
    private Counter commandsProcessed;
    private Counter commandsFailed;
    private Counter commandsRejected;
    private Counter commandsTimeout;
    
    // Timers
    private Timer executionTimer;
    private Timer validationTimer;
    
    // Gauges
    private AtomicLong activeCommands = new AtomicLong(0);
    private AtomicLong registeredHandlers = new AtomicLong(0);
    
    // Métricas por tipo de comando
    private final Map<String, Counter> commandCounters = new ConcurrentHashMap<>();
    private final Map<String, Timer> commandTimers = new ConcurrentHashMap<>();
    
    @Override
    public void bindTo(MeterRegistry registry) {
        this.meterRegistry = registry;
        initializeMetrics();
    }
    
    private void initializeMetrics() {
        
        // Contadores gerais
        commandsProcessed = Counter.builder("commandbus.commands.processed")
            .description("Total de comandos processados com sucesso")
            .register(meterRegistry);
            
        commandsFailed = Counter.builder("commandbus.commands.failed")
            .description("Total de comandos que falharam")
            .register(meterRegistry);
            
        commandsRejected = Counter.builder("commandbus.commands.rejected")
            .description("Total de comandos rejeitados")
            .register(meterRegistry);
            
        commandsTimeout = Counter.builder("commandbus.commands.timeout")
            .description("Total de comandos que excederam timeout")
            .register(meterRegistry);
        
        // Timers
        executionTimer = Timer.builder("commandbus.execution.time")
            .description("Tempo de execução de comandos")
            .register(meterRegistry);
            
        validationTimer = Timer.builder("commandbus.validation.time")
            .description("Tempo de validação de comandos")
            .register(meterRegistry);
        
        // Gauges
        Gauge.builder("commandbus.commands.active")
            .description("Comandos atualmente em execução")
            .register(meterRegistry, activeCommands, AtomicLong::get);
            
        Gauge.builder("commandbus.handlers.registered")
            .description("Número de handlers registrados")
            .register(meterRegistry, registeredHandlers, AtomicLong::get);
    }
    
    // Métodos para registrar métricas
    public Timer.Sample startExecutionTimer() {
        activeCommands.incrementAndGet();
        return Timer.start(meterRegistry);
    }
    
    public void stopExecutionTimer(Timer.Sample sample) {
        sample.stop(executionTimer);
        activeCommands.decrementAndGet();
    }
    
    public void stopExecutionTimer(Timer.Sample sample, String commandType) {
        sample.stop(getCommandTimer(commandType));
        activeCommands.decrementAndGet();
    }
    
    public Timer.Sample startValidationTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopValidationTimer(Timer.Sample sample) {
        sample.stop(validationTimer);
    }
    
    public void incrementCommandsProcessed() {
        commandsProcessed.increment();
    }
    
    public void incrementCommandsProcessed(String commandType) {
        commandsProcessed.increment();
        getCommandCounter(commandType, "processed").increment();
    }
    
    public void incrementCommandsFailed() {
        commandsFailed.increment();
    }
    
    public void incrementCommandsFailed(String commandType, String errorType) {
        commandsFailed.increment();
        getCommandCounter(commandType, "failed")
            .tag("error_type", errorType)
            .increment();
    }
    
    public void incrementCommandsRejected() {
        commandsRejected.increment();
    }
    
    public void incrementCommandsRejected(String commandType) {
        commandsRejected.increment();
        getCommandCounter(commandType, "rejected").increment();
    }
    
    public void incrementCommandsTimeout() {
        commandsTimeout.increment();
    }
    
    public void incrementCommandsTimeout(String commandType) {
        commandsTimeout.increment();
        getCommandCounter(commandType, "timeout").increment();
    }
    
    public void recordExecutionTime(String commandType, long executionTimeMs) {
        getCommandTimer(commandType).record(executionTimeMs, TimeUnit.MILLISECONDS);
    }
    
    public void updateRegisteredHandlers(int count) {
        registeredHandlers.set(count);
    }
    
    // Métodos auxiliares
    private Counter getCommandCounter(String commandType, String operation) {
        String key = commandType + "." + operation;
        return commandCounters.computeIfAbsent(key, k ->
            Counter.builder("commandbus.commands.by_type")
                .tag("command_type", commandType)
                .tag("operation", operation)
                .description("Comandos por tipo e operação")
                .register(meterRegistry)
        );
    }
    
    private Timer getCommandTimer(String commandType) {
        return commandTimers.computeIfAbsent(commandType, type ->
            Timer.builder("commandbus.execution.by_type")
                .tag("command_type", type)
                .description("Tempo de execução por tipo de comando")
                .register(meterRegistry)
        );
    }
    
    // Getters para health checks
    public double getActiveCommandsCount() {
        return activeCommands.get();
    }
    
    public double getRegisteredHandlersCount() {
        return registeredHandlers.get();
    }
}
```

### **🏥 Health Indicator**

```java
// Localização: command/config/CommandBusHealthIndicator.java
@Component
public class CommandBusHealthIndicator implements HealthIndicator {
    
    private final CommandBus commandBus;
    private final CommandBusMetrics metrics;
    private final CommandBusProperties properties;
    
    @Override
    public Health health() {
        
        Map<String, Object> details = checkHealth();
        
        boolean isOperational = isOperational();
        
        return isOperational ? 
            Health.up().withDetails(details).build() :
            Health.down().withDetails(details).build();
    }
    
    public Map<String, Object> checkHealth() {
        Map<String, Object> details = new HashMap<>();
        
        try {
            // Verificar se Command Bus está operacional
            details.put("operational", isOperational());
            
            // Estatísticas básicas
            CommandBusStatistics stats = commandBus.getStatistics();
            details.put("total_commands", stats.getTotalCommands());
            details.put("success_rate", stats.getSuccessRate());
            details.put("error_rate", stats.getErrorRate());
            details.put("avg_execution_time_ms", stats.getAverageExecutionTime());
            
            // Comandos ativos
            details.put("active_commands", metrics.getActiveCommandsCount());
            details.put("registered_handlers", metrics.getRegisteredHandlersCount());
            
            // Verificar thresholds
            double errorRate = stats.getErrorRate();
            double errorThreshold = properties.getMonitoring().getErrorRateThreshold();
            
            if (errorRate > errorThreshold) {
                details.put("warning", String.format(
                    "Taxa de erro (%.2f%%) acima do threshold (%.2f%%)", 
                    errorRate * 100, errorThreshold * 100
                ));
            }
            
            // Tempo de resposta
            double avgTime = stats.getAverageExecutionTime();
            if (avgTime > 5000) { // > 5 segundos
                details.put("warning", String.format(
                    "Tempo médio de execução alto: %.2fms", avgTime
                ));
            }
            
            // Última execução
            details.put("last_command_time", stats.getLastCommandTime());
            details.put("uptime", formatDuration(stats.getUptime()));
            
        } catch (Exception e) {
            details.put("error", e.getMessage());
            details.put("operational", false);
        }
        
        return details;
    }
    
    public boolean isOperational() {
        try {
            // Teste simples: verificar se consegue obter estatísticas
            CommandBusStatistics stats = commandBus.getStatistics();
            
            // Verificar se não há muitos comandos falhando
            double errorRate = stats.getErrorRate();
            double threshold = properties.getMonitoring().getErrorRateThreshold();
            
            return errorRate <= threshold;
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde do Command Bus: {}", e.getMessage());
            return false;
        }
    }
    
    private String formatDuration(Duration duration) {
        long hours = duration.toHours();
        long minutes = duration.toMinutesPart();
        long seconds = duration.toSecondsPart();
        
        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar Command Handler completo

#### **Passo 1: Criar Comando Customizado**
```java
// Criar comando para atualizar sinistro
public class AtualizarSinistroCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    private final String sinistroId;
    private final String novaDescricao;
    private final BigDecimal novoValorEstimado;
    private final String observacoes;
    
    // Construtor e getters...
}
```

#### **Passo 2: Implementar Handler**
```java
@Component
public class AtualizarSinistroCommandHandler implements CommandHandler<AtualizarSinistroCommand> {
    
    @Override
    public CommandResult handle(AtualizarSinistroCommand command) {
        // Implementar lógica de atualização
        // 1. Validar comando
        // 2. Carregar aggregate
        // 3. Aplicar mudanças
        // 4. Salvar
        return CommandResult.success();
    }
    
    @Override
    public Class<AtualizarSinistroCommand> getCommandType() {
        return AtualizarSinistroCommand.class;
    }
}
```

#### **Passo 3: Testar Validação**
```java
@Test
public void testarValidacaoComando() {
    CriarSinistroCommand comando = new CriarSinistroCommand(
        "", // CPF vazio - deve falhar
        "ABC1234",
        "Descrição teste",
        LocalDateTime.now(),
        "Rua Teste, 123",
        new BigDecimal("1000.00"),
        "user123"
    );
    
    ValidationResult result = validator.validate(comando);
    
    assertThat(result.isInvalid()).isTrue();
    assertThat(result.getErrorMessages()).contains("CPF do segurado é obrigatório");
}
```

#### **Passo 4: Verificar Métricas**
```bash
# Métricas do Command Bus
curl http://localhost:8083/api/v1/actuator/metrics/commandbus.commands.processed
curl http://localhost:8083/api/v1/actuator/metrics/commandbus.execution.time

# Health check
curl http://localhost:8083/api/v1/actuator/commandbus/health | jq
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Configurar** Command Bus com Spring Boot
2. **Implementar** validação robusta de comandos
3. **Criar** Command Handlers com tratamento de erros
4. **Configurar** métricas e monitoramento
5. **Testar** componentes do Command Side

### **❓ Perguntas para Reflexão:**

1. Como garantir que validações sejam executadas?
2. Qual a diferença entre validação técnica e de negócio?
3. Como monitorar performance de comandos?
4. Quando usar execução síncrona vs assíncrona?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 3**, vamos explorar:
- Implementação detalhada do Query Side
- Otimização de consultas e índices
- Cache e estratégias de performance
- Padrões de projeção de dados

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 48 minutos  
**📋 Pré-requisitos:** CQRS Parte 1