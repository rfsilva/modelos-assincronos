# 📘 COMMAND BUS - PARTE 4
## Validação de Comandos e Tratamento de Erros

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender o sistema de validação de comandos
- Implementar validadores customizados
- Dominar o tratamento de erros e exceções
- Aplicar padrões de validação no projeto

---

## 🔍 **SISTEMA DE VALIDAÇÃO DE COMANDOS**

### **📋 Conceitos Fundamentais**

A validação de comandos é **crucial** para garantir a integridade dos dados e a consistência do sistema. No projeto, utilizamos um sistema robusto de validação em múltiplas camadas.

### **🏗️ Arquitetura de Validação**

```
Command → Validator → Command Handler → Domain Validation → Event Store
    ↓         ↓              ↓                ↓              ↓
 Sintática  Semântica    Negócio         Invariantes    Persistência
```

---

## 🛠️ **IMPLEMENTAÇÃO DE VALIDADORES**

### **📝 Interface CommandValidator**

Localização: `com.seguradora.hibrida.command.validation.CommandValidator`

```java
public interface CommandValidator<T extends Command> {
    
    /**
     * Valida o comando e retorna o resultado.
     */
    ValidationResult validate(T command);
    
    /**
     * Tipo de comando que este validador processa.
     */
    Class<T> getCommandType();
    
    /**
     * Prioridade do validador (menor valor = maior prioridade).
     */
    default int getPriority() {
        return 100;
    }
}
```

### **🎯 Exemplo Prático: Validador de Sinistro**

```java
@Component
public class CriarSinistroCommandValidator 
    implements CommandValidator<CriarSinistroCommand> {
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        ValidationResult result = ValidationResult.valid();
        
        // Validação de campos obrigatórios
        if (StringUtils.isBlank(command.getCpfSegurado())) {
            result = result.addErrorMessage("CPF do segurado é obrigatório");
        }
        
        // Validação de formato de CPF
        if (!CpfValidator.isValid(command.getCpfSegurado())) {
            result = result.addErrorMessage("CPF inválido");
        }
        
        // Validação de placa
        if (!PlacaValidator.isValid(command.getPlaca())) {
            result = result.addErrorMessage("Placa do veículo inválida");
        }
        
        // Validação de valor estimado
        if (command.getValorEstimado() != null && 
            command.getValorEstimado().compareTo(BigDecimal.ZERO) <= 0) {
            result = result.addErrorMessage("Valor estimado deve ser positivo");
        }
        
        return result;
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getPriority() {
        return 10; // Alta prioridade para validações básicas
    }
}
```

---

## 🔧 **VALIDAÇÃO EM CAMADAS**

### **📊 Níveis de Validação**

#### **1. Validação Sintática (Formato)**
```java
// Exemplo: Validação de CPF
public class CpfValidator {
    
    public static boolean isValid(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        
        // Algoritmo de validação de CPF
        return calculateDigit(cpf, 10) == Character.getNumericValue(cpf.charAt(9)) &&
               calculateDigit(cpf, 11) == Character.getNumericValue(cpf.charAt(10));
    }
    
    private static int calculateDigit(String cpf, int weight) {
        int sum = 0;
        for (int i = 0; i < weight - 1; i++) {
            sum += Character.getNumericValue(cpf.charAt(i)) * (weight - i);
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }
}
```

#### **2. Validação Semântica (Contexto)**
```java
@Component
public class SinistroBusinessValidator 
    implements CommandValidator<CriarSinistroCommand> {
    
    @Autowired
    private ApoliceService apoliceService;
    
    @Override
    public ValidationResult validate(CriarSinistroCommand command) {
        ValidationResult result = ValidationResult.valid();
        
        // Verifica se apólice existe e está vigente
        Optional<Apolice> apolice = apoliceService
            .findByCpf(command.getCpfSegurado());
            
        if (apolice.isEmpty()) {
            return ValidationResult.invalid(
                "Não existe apólice ativa para o CPF informado"
            );
        }
        
        if (!apolice.get().isVigente(command.getDataOcorrencia())) {
            return ValidationResult.invalid(
                "Apólice não estava vigente na data da ocorrência"
            );
        }
        
        return result;
    }
    
    @Override
    public int getPriority() {
        return 50; // Prioridade média para validações de negócio
    }
}
```

#### **3. Validação de Domínio (Invariantes)**
```java
// No Aggregate Root
public class SinistroAggregate extends AggregateRoot {
    
    @Override
    protected void registerBusinessRules() {
        // Regra: Não pode criar sinistro com data futura
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                SinistroAggregate sinistro = (SinistroAggregate) aggregate;
                return sinistro.dataOcorrencia.isBefore(Instant.now());
            }
            
            @Override
            public String getErrorMessage() {
                return "Data de ocorrência não pode ser futura";
            }
        });
        
        // Regra: Valor estimado não pode exceder valor da apólice
        registerBusinessRule(new ValorMaximoRule());
    }
}
```

---

## ⚠️ **TRATAMENTO DE ERROS**

### **🎯 Hierarquia de Exceções**

```java
// Exceção base para comandos
public class CommandException extends RuntimeException {
    private final Class<? extends Command> commandType;
    
    public CommandException(String message, Class<? extends Command> commandType) {
        super(message);
        this.commandType = commandType;
    }
}

// Exceção específica para validação
public class CommandValidationException extends CommandException {
    private final Set<String> violations;
    
    public CommandValidationException(
        Class<? extends Command> commandType, 
        Set<String> violations) {
        super(buildViolationMessage(commandType, violations), commandType);
        this.violations = violations;
    }
}

// Exceção para handler não encontrado
public class CommandHandlerNotFoundException extends CommandException {
    public CommandHandlerNotFoundException(Class<? extends Command> commandType) {
        super("No handler found for command: " + commandType.getSimpleName(), 
              commandType);
    }
}
```

### **🔄 Fluxo de Tratamento de Erros**

```java
@Component
public class SimpleCommandBus implements CommandBus {
    
    @Override
    public CommandResult send(Command command) {
        try {
            // 1. Validação do comando
            validateCommand(command);
            
            // 2. Busca handler
            CommandHandler<Command> handler = getHandlerForCommand(command);
            
            // 3. Execução com timeout
            return executeWithTimeout(command, handler);
            
        } catch (CommandValidationException e) {
            // Erro de validação - retorna resultado com detalhes
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR")
                .withMetadata("violations", e.getViolations());
                
        } catch (CommandHandlerNotFoundException e) {
            // Handler não encontrado
            return CommandResult.failure(e.getMessage(), "HANDLER_NOT_FOUND");
            
        } catch (CommandTimeoutException e) {
            // Timeout na execução
            return CommandResult.failure(e.getMessage(), "TIMEOUT")
                .withMetadata("timeoutSeconds", e.getTimeoutSeconds());
                
        } catch (Exception e) {
            // Erro genérico
            log.error("Unexpected error processing command: {}", 
                     command.getCommandType(), e);
            return CommandResult.failure("Internal server error", "INTERNAL_ERROR");
        }
    }
}
```

---

## 📊 **VALIDAÇÃO CUSTOMIZADA**

### **🎯 Validador Composto**

```java
@Component
public class CompositeCommandValidator<T extends Command> 
    implements CommandValidator<T> {
    
    private final List<CommandValidator<T>> validators;
    
    public CompositeCommandValidator(List<CommandValidator<T>> validators) {
        this.validators = validators.stream()
            .sorted(Comparator.comparing(CommandValidator::getPriority))
            .collect(Collectors.toList());
    }
    
    @Override
    public ValidationResult validate(T command) {
        ValidationResult result = ValidationResult.valid();
        
        for (CommandValidator<T> validator : validators) {
            if (validator.supports(command.getClass())) {
                ValidationResult validationResult = validator.validate(command);
                result = result.combine(validationResult);
                
                // Para na primeira validação que falhar (fail-fast)
                if (validationResult.isInvalid()) {
                    break;
                }
            }
        }
        
        return result;
    }
}
```

### **🔧 Validação Condicional**

```java
@Component
public class ConditionalSinistroValidator 
    implements CommandValidator<AtualizarSinistroCommand> {
    
    @Override
    public ValidationResult validate(AtualizarSinistroCommand command) {
        ValidationResult result = ValidationResult.valid();
        
        // Validação condicional baseada no status
        if ("FECHADO".equals(command.getNovoStatus())) {
            // Se está fechando, valor final é obrigatório
            if (command.getValorFinal() == null) {
                result = result.addErrorMessage(
                    "Valor final é obrigatório para fechar sinistro"
                );
            }
            
            // Valor final não pode ser zero
            if (command.getValorFinal() != null && 
                command.getValorFinal().compareTo(BigDecimal.ZERO) <= 0) {
                result = result.addErrorMessage(
                    "Valor final deve ser maior que zero"
                );
            }
        }
        
        // Validação de transição de status
        if (!isValidStatusTransition(command.getStatusAtual(), 
                                   command.getNovoStatus())) {
            result = result.addErrorMessage(
                String.format("Transição de status inválida: %s → %s",
                             command.getStatusAtual(), command.getNovoStatus())
            );
        }
        
        return result;
    }
    
    private boolean isValidStatusTransition(String from, String to) {
        // Matriz de transições válidas
        Map<String, Set<String>> validTransitions = Map.of(
            "ABERTO", Set.of("EM_ANALISE", "CANCELADO"),
            "EM_ANALISE", Set.of("FECHADO", "CANCELADO", "ABERTO"),
            "FECHADO", Set.of(), // Estado final
            "CANCELADO", Set.of() // Estado final
        );
        
        return validTransitions.getOrDefault(from, Set.of()).contains(to);
    }
}
```

---

## 🎯 **BOAS PRÁTICAS DE VALIDAÇÃO**

### **✅ Diretrizes Importantes**

#### **1. Separação de Responsabilidades**
```java
// ❌ EVITAR: Validação misturada com lógica de negócio
public class BadSinistroHandler {
    public CommandResult handle(CriarSinistroCommand command) {
        // Validação inline (ruim)
        if (command.getCpf() == null) {
            return CommandResult.failure("CPF obrigatório");
        }
        
        // Lógica de negócio misturada
        // ...
    }
}

// ✅ PREFERIR: Validação separada
@Component
public class GoodSinistroHandler {
    
    @Autowired
    private CommandValidator<CriarSinistroCommand> validator;
    
    public CommandResult handle(CriarSinistroCommand command) {
        // Validação já foi feita pelo Command Bus
        // Foco apenas na lógica de negócio
        SinistroAggregate sinistro = new SinistroAggregate();
        sinistro.criar(command);
        
        repository.save(sinistro);
        return CommandResult.success();
    }
}
```

#### **2. Validação Fail-Fast**
```java
// Pare na primeira validação que falhar
public ValidationResult validate(Command command) {
    // Validações básicas primeiro
    if (basicValidation.isInvalid()) {
        return basicValidation; // Para aqui
    }
    
    // Validações custosas só se básicas passaram
    return expensiveValidation(command);
}
```

#### **3. Mensagens de Erro Claras**
```java
// ❌ Mensagem genérica
return ValidationResult.invalid("Dados inválidos");

// ✅ Mensagem específica e acionável
return ValidationResult.invalid(
    "CPF '12345678901' é inválido. Verifique se todos os 11 dígitos estão corretos."
);
```

---

## 🔍 **DEBUGGING E TROUBLESHOOTING**

### **📊 Logs de Validação**

```java
@Component
public class LoggingCommandValidator<T extends Command> 
    implements CommandValidator<T> {
    
    private static final Logger log = LoggerFactory.getLogger(LoggingCommandValidator.class);
    
    @Override
    public ValidationResult validate(T command) {
        log.debug("Validating command: {} with ID: {}", 
                 command.getCommandType(), command.getCommandId());
        
        ValidationResult result = doValidate(command);
        
        if (result.isInvalid()) {
            log.warn("Validation failed for command {}: {}", 
                    command.getCommandId(), result.getErrorMessages());
        } else {
            log.debug("Validation successful for command: {}", 
                     command.getCommandId());
        }
        
        return result;
    }
}
```

### **🎯 Métricas de Validação**

```java
@Component
public class ValidationMetrics {
    
    private final Counter validationFailures;
    private final Timer validationTime;
    
    public ValidationMetrics(MeterRegistry meterRegistry) {
        this.validationFailures = Counter.builder("command.validation.failures")
            .description("Number of command validation failures")
            .tag("type", "validation")
            .register(meterRegistry);
            
        this.validationTime = Timer.builder("command.validation.time")
            .description("Time spent validating commands")
            .register(meterRegistry);
    }
    
    public void recordValidationFailure(String commandType, String reason) {
        validationFailures.increment(
            Tags.of("command.type", commandType, "reason", reason)
        );
    }
}
```

---

## 🎯 **EXERCÍCIOS PRÁTICOS**

### **📝 Exercício 1: Validador Básico**
Implemente um validador para `AtualizarSeguradoCommand` que:
- Valide formato do email
- Verifique se telefone tem formato válido
- Garanta que pelo menos um campo foi alterado

### **📝 Exercício 2: Validação Condicional**
Crie um validador que:
- Valide regras diferentes baseadas no tipo de sinistro
- Implemente validação de documentos obrigatórios por tipo
- Trate casos especiais (sinistros de alta complexidade)

### **📝 Exercício 3: Validação Assíncrona**
Desenvolva um validador que:
- Consulte serviços externos (DETRAN, SPC)
- Implemente cache para otimizar consultas
- Trate timeouts e falhas de rede

---

## 🔗 **PRÓXIMOS PASSOS**

Na **Parte 5** do Command Bus, abordaremos:
- **Métricas e Monitoramento** de comandos
- **Performance e Otimização** do Command Bus
- **Testes Avançados** de handlers e validadores
- **Integração** com sistemas externos

---

## 📚 **REFERÊNCIAS**

### **📖 Documentação Técnica**
- [Command Validation Patterns](https://martinfowler.com/articles/replaceThrowWithNotification.html)
- [Domain Validation](https://enterprisecraftsmanship.com/posts/validation-in-domain-driven-design/)
- [Error Handling Best Practices](https://docs.spring.io/spring-framework/docs/current/reference/html/web.html#mvc-ann-exceptionhandler)

### **🔧 Código de Referência**
- `CommandValidator.java` - Interface base
- `ValidationResult.java` - Resultado de validação
- `SimpleCommandBus.java` - Implementação com tratamento de erros
- `CommandValidationException.java` - Exceções específicas

---

**📘 Capítulo:** 05 - Command Bus - Parte 4  
**⏱️ Tempo Estimado:** 45 minutos  
**🎯 Próximo:** [05 - Command Bus - Parte 5](./05-command-bus-parte-5.md)  
**📋 Checklist:** Validação ✅ | Tratamento de Erros ✅ | Práticas ✅