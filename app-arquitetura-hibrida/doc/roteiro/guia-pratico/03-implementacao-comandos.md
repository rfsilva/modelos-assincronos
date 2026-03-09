# ⚡ ETAPA 03: IMPLEMENTAÇÃO DE COMANDOS
## Command Side (Write) - Integração com Command Bus

### 🎯 **OBJETIVO DA ETAPA**

Implementar o lado de comando (write) da arquitetura CQRS, integrando os agregados com o Command Bus, implementando validações robustas e garantindo processamento confiável de comandos.

**⏱️ Duração Estimada:** 4-8 horas  
**👥 Participantes:** Desenvolvedor + Tech Lead  
**📋 Pré-requisitos:** Etapa 02 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **⚡ 1. INTEGRAÇÃO COM COMMAND BUS**

#### **📦 Registro de Command Handlers:**
- [ ] **Command handlers** anotados com `@Component`
- [ ] **Implementação** da interface `CommandHandler<T>` completa
- [ ] **Método getCommandType()** retornando classe correta
- [ ] **Auto-registro** no Command Bus funcionando
- [ ] **Logs de inicialização** confirmando registro

#### **🔧 Configuração do Command Bus:**
```java
// Verificar se está configurado em application.yml
command-bus:
  enabled: true
  thread-pool:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
  timeout:
    default-seconds: 30
  monitoring:
    enabled: true
    detailed-logging: true
```

#### **✅ Validação da Integração:**
```java
// Teste de integração básico
@SpringBootTest
class CommandBusIntegrationTest {
    
    @Autowired
    private CommandBus commandBus;
    
    @Test
    void deveProcessarComandoComSucesso() {
        // Given
        var command = [Criar][Dominio]Command.builder()
            .aggregateId(UUID.randomUUID().toString())
            .userId("test-user")
            .[campo]("valor")
            .build();
        
        // When
        CommandResult result = commandBus.send(command);
        
        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();
    }
}
```

---

### **📝 2. REFINAMENTO DE COMANDOS**

#### **🎯 Estrutura Completa de Comandos:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor // Para serialização
public class [Acao][Dominio]Command implements Command {
    
    // ========== IDENTIFICAÇÃO OBRIGATÓRIA ==========
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    
    @NotBlank(message = "User ID é obrigatório")
    private String userId;
    
    // ========== DADOS DO COMANDO ==========
    @NotBlank(message = "Aggregate ID é obrigatório")
    private String aggregateId;
    
    // Campos específicos do comando com validações
    @NotBlank(message = "[Campo] é obrigatório")
    @Size(max = 255, message = "[Campo] deve ter no máximo 255 caracteres")
    private String [campo];
    
    @Valid
    private [ValueObject] [valueObject];
    
    // ========== VALIDAÇÕES CUSTOMIZADAS ==========
    @AssertTrue(message = "Dados do comando devem ser consistentes")
    public boolean isDataConsistent() {
        // Validações específicas do negócio
        return [condicao];
    }
    
    // ========== MÉTODOS DE CONVENIÊNCIA ==========
    public static [Acao][Dominio]Command create(String aggregateId, String userId, 
                                                String [campo]) {
        return builder()
            .aggregateId(aggregateId)
            .userId(userId)
            .[campo]([campo])
            .correlationId(UUID.randomUUID())
            .build();
    }
    
    @Override
    public String toString() {
        return String.format("[Acao][Dominio]Command{commandId=%s, aggregateId='%s', userId='%s'}",
                           commandId, aggregateId, userId);
    }
}
```

#### **🔍 Validadores de Comando:**
```java
@Component
@Slf4j
public class [Acao][Dominio]CommandValidator implements CommandValidator<[Acao][Dominio]Command> {
    
    private final [Dominio]Service [dominio]Service;
    
    @Override
    public ValidationResult validate([Acao][Dominio]Command command) {
        List<String> errors = new ArrayList<>();
        
        // Validações de negócio específicas
        if ([condicaoInvalida]) {
            errors.add("Mensagem de erro específica");
        }
        
        // Validações de existência/unicidade
        if ([dominio]Service.exists(command.getAggregateId())) {
            errors.add("Agregado já existe");
        }
        
        // Validações de autorização
        if (!isUserAuthorized(command.getUserId(), command.getAggregateId())) {
            errors.add("Usuário não autorizado para esta operação");
        }
        
        return errors.isEmpty() 
            ? ValidationResult.valid()
            : ValidationResult.invalid(errors);
    }
    
    @Override
    public Class<[Acao][Dominio]Command> getCommandType() {
        return [Acao][Dominio]Command.class;
    }
    
    @Override
    public int getPriority() {
        return 100; // Ajustar conforme necessário
    }
    
    private boolean isUserAuthorized(String userId, String aggregateId) {
        // Lógica de autorização
        return true;
    }
}
```

#### **✅ Checklist de Comandos:**
- [ ] **Validações Bean Validation** implementadas
- [ ] **Validadores customizados** criados quando necessário
- [ ] **Factory methods** para criação conveniente
- [ ] **ToString() informativos** implementados
- [ ] **Serialização/deserialização** funcionando

---

### **🎯 3. COMMAND HANDLERS ROBUSTOS**

#### **⚡ Implementação Completa:**
```java
@Component
@Slf4j
@Transactional
public class [Acao][Dominio]CommandHandler implements CommandHandler<[Acao][Dominio]Command> {
    
    private final AggregateRepository<[Dominio]Aggregate> repository;
    private final [Dominio]Service [dominio]Service;
    private final ApplicationEventPublisher eventPublisher;
    private final MeterRegistry meterRegistry;
    
    // ========== CONSTRUTOR ==========
    public [Acao][Dominio]CommandHandler(
            AggregateRepository<[Dominio]Aggregate> repository,
            [Dominio]Service [dominio]Service,
            ApplicationEventPublisher eventPublisher,
            MeterRegistry meterRegistry) {
        this.repository = repository;
        this.[dominio]Service = [dominio]Service;
        this.eventPublisher = eventPublisher;
        this.meterRegistry = meterRegistry;
    }
    
    // ========== PROCESSAMENTO PRINCIPAL ==========
    @Override
    public CommandResult handle([Acao][Dominio]Command command) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.info("Processando comando: {}", command);
            
            // 1. Validações pré-processamento
            validatePreConditions(command);
            
            // 2. Carregar ou criar agregado
            [Dominio]Aggregate aggregate = loadOrCreateAggregate(command);
            
            // 3. Executar operação de negócio
            executeBusinessOperation(aggregate, command);
            
            // 4. Salvar agregado (persiste eventos)
            repository.save(aggregate);
            
            // 5. Publicar eventos de integração se necessário
            publishIntegrationEvents(aggregate, command);
            
            // 6. Registrar métricas de sucesso
            recordSuccessMetrics(command);
            
            log.info("Comando processado com sucesso: {}", command.getCommandId());
            
            return CommandResult.success()
                .withCorrelationId(command.getCorrelationId())
                .withMetadata("aggregateId", aggregate.getId())
                .withMetadata("version", aggregate.getVersion());
                
        } catch (BusinessRuleViolationException e) {
            log.warn("Violação de regra de negócio: {}", e.getMessage());
            recordFailureMetrics(command, "business_rule_violation");
            return CommandResult.failure(e.getMessage(), "BUSINESS_RULE_VIOLATION");
            
        } catch (AggregateNotFoundException e) {
            log.warn("Agregado não encontrado: {}", e.getMessage());
            recordFailureMetrics(command, "aggregate_not_found");
            return CommandResult.failure(e.getMessage(), "AGGREGATE_NOT_FOUND");
            
        } catch (ConcurrencyException e) {
            log.warn("Conflito de concorrência: {}", e.getMessage());
            recordFailureMetrics(command, "concurrency_conflict");
            return CommandResult.failure(e.getMessage(), "CONCURRENCY_CONFLICT");
            
        } catch (Exception e) {
            log.error("Erro inesperado ao processar comando: {}", command, e);
            recordFailureMetrics(command, "unexpected_error");
            return CommandResult.failure(e);
            
        } finally {
            sample.stop(Timer.builder("command.processing.time")
                .tag("command", getCommandType().getSimpleName())
                .register(meterRegistry));
        }
    }
    
    // ========== MÉTODOS AUXILIARES ==========
    private void validatePreConditions([Acao][Dominio]Command command) {
        // Validações específicas antes do processamento
        if ([condicaoInvalida]) {
            throw new CommandValidationException(
                getCommandType(),
                Set.of("Mensagem de erro específica")
            );
        }
    }
    
    private [Dominio]Aggregate loadOrCreateAggregate([Acao][Dominio]Command command) {
        // Para comandos de criação
        if (command instanceof Criar[Dominio]Command) {
            return new [Dominio]Aggregate();
        }
        
        // Para comandos de atualização
        return repository.getById(command.getAggregateId());
    }
    
    private void executeBusinessOperation([Dominio]Aggregate aggregate, 
                                        [Acao][Dominio]Command command) {
        // Executar operação específica no agregado
        aggregate.[operacao](
            command.get[Campo](),
            command.getUserId()
        );
    }
    
    private void publishIntegrationEvents([Dominio]Aggregate aggregate, 
                                        [Acao][Dominio]Command command) {
        // Publicar eventos de integração se necessário
        // Exemplo: notificações, integrações externas
    }
    
    private void recordSuccessMetrics([Acao][Dominio]Command command) {
        meterRegistry.counter("command.processed.success",
            "command", getCommandType().getSimpleName())
            .increment();
    }
    
    private void recordFailureMetrics([Acao][Dominio]Command command, String errorType) {
        meterRegistry.counter("command.processed.failure",
            "command", getCommandType().getSimpleName(),
            "error_type", errorType)
            .increment();
    }
    
    // ========== CONFIGURAÇÃO ==========
    @Override
    public Class<[Acao][Dominio]Command> getCommandType() {
        return [Acao][Dominio]Command.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Ajustar conforme necessário
    }
}
```

#### **✅ Checklist de Command Handlers:**
- [ ] **Tratamento de erros** completo e específico
- [ ] **Logging estruturado** implementado
- [ ] **Métricas** de sucesso e falha registradas
- [ ] **Transações** configuradas adequadamente
- [ ] **Timeout** configurado apropriadamente

---

### **🔒 4. VALIDAÇÕES E SEGURANÇA**

#### **🛡️ Validações de Autorização:**
```java
@Component
public class [Dominio]AuthorizationService {
    
    public void validateUserCanExecute(String userId, String operation, String aggregateId) {
        // Validações de autorização específicas
        if (!hasPermission(userId, operation, aggregateId)) {
            throw new UnauthorizedException(
                String.format("Usuário %s não autorizado para %s no agregado %s",
                            userId, operation, aggregateId)
            );
        }
    }
    
    private boolean hasPermission(String userId, String operation, String aggregateId) {
        // Implementar lógica de autorização
        // Pode consultar base de dados, cache, serviços externos, etc.
        return true;
    }
}
```

#### **🔍 Validações de Integridade:**
```java
@Component
public class [Dominio]IntegrityValidator {
    
    public void validateBusinessIntegrity([Acao][Dominio]Command command) {
        // Validações de integridade referencial
        // Validações de regras de negócio complexas
        // Validações que requerem consultas externas
        
        if ([violacaoDeIntegridade]) {
            throw new BusinessRuleViolationException(
                "Violação de integridade detectada",
                List.of("Detalhes específicos da violação")
            );
        }
    }
}
```

#### **✅ Checklist de Validações:**
- [ ] **Validações de entrada** (Bean Validation)
- [ ] **Validações de autorização** implementadas
- [ ] **Validações de integridade** configuradas
- [ ] **Validações de negócio** específicas
- [ ] **Mensagens de erro** claras e específicas

---

### **📊 5. MONITORAMENTO E MÉTRICAS**

#### **📈 Métricas Customizadas:**
```java
@Component
public class [Dominio]CommandMetrics {
    
    private final MeterRegistry meterRegistry;
    private final Counter commandsProcessed;
    private final Timer processingTime;
    private final Gauge activeCommands;
    
    public [Dominio]CommandMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        this.commandsProcessed = Counter.builder("commands.processed")
            .tag("domain", "[dominio]")
            .register(meterRegistry);
        this.processingTime = Timer.builder("commands.processing.time")
            .tag("domain", "[dominio]")
            .register(meterRegistry);
        this.activeCommands = Gauge.builder("commands.active")
            .tag("domain", "[dominio]")
            .register(meterRegistry, this, [Dominio]CommandMetrics::getActiveCommandsCount);
    }
    
    public void recordCommandProcessed(String commandType, boolean success) {
        commandsProcessed.increment(
            Tags.of(
                "command", commandType,
                "status", success ? "success" : "failure"
            )
        );
    }
    
    public Timer.Sample startProcessingTimer() {
        return Timer.start(meterRegistry);
    }
    
    public void stopProcessingTimer(Timer.Sample sample, String commandType) {
        sample.stop(processingTime.withTags("command", commandType));
    }
    
    private double getActiveCommandsCount() {
        // Implementar lógica para contar comandos ativos
        return 0.0;
    }
}
```

#### **🏥 Health Checks:**
```java
@Component
public class [Dominio]CommandHealthIndicator implements HealthIndicator {
    
    private final AggregateRepository<[Dominio]Aggregate> repository;
    private final CommandBus commandBus;
    
    @Override
    public Health health() {
        try {
            // Verificar se o repository está funcionando
            boolean repositoryHealthy = repository.getStatistics() != null;
            
            // Verificar se o command bus está funcionando
            boolean commandBusHealthy = commandBus.hasHandler([Criar][Dominio]Command.class);
            
            if (repositoryHealthy && commandBusHealthy) {
                return Health.up()
                    .withDetail("repository", "healthy")
                    .withDetail("commandBus", "healthy")
                    .withDetail("registeredHandlers", getRegisteredHandlersCount())
                    .build();
            } else {
                return Health.down()
                    .withDetail("repository", repositoryHealthy ? "healthy" : "unhealthy")
                    .withDetail("commandBus", commandBusHealthy ? "healthy" : "unhealthy")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
    
    private int getRegisteredHandlersCount() {
        // Implementar contagem de handlers registrados
        return 0;
    }
}
```

#### **✅ Checklist de Monitoramento:**
- [ ] **Métricas customizadas** implementadas
- [ ] **Health checks** configurados
- [ ] **Logs estruturados** com correlation IDs
- [ ] **Alertas** configurados para falhas
- [ ] **Dashboards** criados para monitoramento

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **⚡ Funcionalidade:**
- [ ] **Comandos** sendo processados corretamente
- [ ] **Command handlers** registrados no Command Bus
- [ ] **Validações** funcionando adequadamente
- [ ] **Erros** sendo tratados apropriadamente
- [ ] **Eventos** sendo persistidos no Event Store

#### **🔒 Segurança e Validações:**
- [ ] **Autorização** implementada e funcionando
- [ ] **Validações de entrada** robustas
- [ ] **Validações de negócio** específicas
- [ ] **Tratamento de erros** consistente
- [ ] **Logs de auditoria** implementados

#### **📊 Observabilidade:**
- [ ] **Métricas** sendo coletadas
- [ ] **Health checks** funcionais
- [ ] **Logs estruturados** implementados
- [ ] **Correlation IDs** sendo propagados
- [ ] **Performance** dentro dos SLAs

#### **🧪 Testes:**
- [ ] **Testes unitários** dos command handlers
- [ ] **Testes de integração** com Command Bus
- [ ] **Testes de validação** funcionais
- [ ] **Testes de erro** implementados
- [ ] **Coverage** adequado (>80%)

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Command Handlers Anêmicos:**
```java
// ❌ EVITAR: Handler que apenas delega
@Override
public CommandResult handle(Command command) {
    aggregate.set[Campo](command.get[Campo]());
    repository.save(aggregate);
    return CommandResult.success();
}

// ✅ PREFERIR: Handler com lógica de negócio
@Override
public CommandResult handle(Command command) {
    validatePreConditions(command);
    aggregate.[operacaoDeNegocio](command.get[Campo]());
    repository.save(aggregate);
    recordMetrics(command);
    return CommandResult.success();
}
```

#### **🚫 Tratamento de Erro Genérico:**
```java
// ❌ EVITAR: Catch genérico
try {
    // processamento
} catch (Exception e) {
    return CommandResult.failure(e);
}

// ✅ PREFERIR: Tratamento específico
try {
    // processamento
} catch (BusinessRuleViolationException e) {
    return CommandResult.failure(e.getMessage(), "BUSINESS_RULE_VIOLATION");
} catch (ConcurrencyException e) {
    return CommandResult.failure(e.getMessage(), "CONCURRENCY_CONFLICT");
}
```

#### **🚫 Validações Inconsistentes:**
```java
// ❌ EVITAR: Validações espalhadas
public CommandResult handle(Command command) {
    if (command.get[Campo]() == null) throw new IllegalArgumentException();
    // ... mais validações espalhadas
}

// ✅ PREFERIR: Validações centralizadas
public CommandResult handle(Command command) {
    validateCommand(command);
    validateBusinessRules(command);
    validateAuthorization(command);
    // ... processamento
}
```

### **✅ Boas Práticas:**

#### **🎯 Design de Comandos:**
- **Sempre** usar validações Bean Validation
- **Sempre** implementar toString() informativos
- **Sempre** incluir correlation IDs
- **Sempre** validar autorização

#### **⚡ Command Handlers:**
- **Sempre** tratar erros específicos
- **Sempre** registrar métricas
- **Sempre** usar logs estruturados
- **Sempre** configurar timeouts adequados

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 04 - Implementação de Eventos](./04-implementacao-eventos.md)**
2. Integrar com Event Bus
3. Implementar event handlers
4. Configurar processamento assíncrono

### **📋 Preparação para Próxima Etapa:**
- [ ] **Event Bus** estudado e compreendido
- [ ] **Padrões de eventos** revisados
- [ ] **Processamento assíncrono** compreendido
- [ ] **Testes de comando** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Command Bus](../05-command-bus-README.md)**: Guia completo do Command Bus
- **[Testes](../10-testes-README.md)**: Estratégias de teste
- **Código Existente**: `TestCommandHandler` como referência

### **🛠️ Ferramentas de Desenvolvimento:**
- **Postman**: Para testar comandos via API
- **JMeter**: Para testes de carga
- **Micrometer**: Para métricas customizadas
- **Logback**: Para logs estruturados

### **🧪 Exemplos de Teste:**
- **CommandHandlerTest**: Testes unitários
- **CommandBusIntegrationTest**: Testes de integração
- **CommandValidationTest**: Testes de validação

---

**📋 Checklist Total:** 70+ itens de validação  
**⏱️ Tempo Médio:** 4-8 horas  
**🎯 Resultado:** Command Side completo e funcional  
**✅ Próxima Etapa:** Implementação de Eventos