# 📖 CAPÍTULO 05: COMMAND BUS - PARTE 1
## Fundamentos e Arquitetura do Command Bus

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender a arquitetura do Command Bus
- Entender o padrão Command e suas responsabilidades
- Explorar a implementação no projeto
- Conhecer os componentes principais

---

## 🚌 **ARQUITETURA DO COMMAND BUS**

### **📚 Conceitos Fundamentais**

O **Command Bus** é o componente central que **recebe, roteia e executa comandos** na arquitetura CQRS. Ele atua como um **mediador** entre a camada de apresentação e a camada de domínio.

```
┌─────────────────────────────────────────────────────────┐
│                   COMMAND BUS FLOW                      │
├─────────────────────────────────────────────────────────┤
│                                                         │
│  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐ │
│  │   Client    │───►│ Command Bus │───►│   Handler   │ │
│  │ (REST API)  │    │             │    │             │ │
│  └─────────────┘    └─────────────┘    └─────────────┘ │
│                             │                   │       │
│                             ▼                   ▼       │
│                    ┌─────────────┐    ┌─────────────┐   │
│                    │ Validation  │    │  Aggregate  │   │
│                    │ Middleware  │    │   Domain    │   │
│                    └─────────────┘    └─────────────┘   │
│                             │                   │       │
│                             ▼                   ▼       │
│                    ┌─────────────┐    ┌─────────────┐   │
│                    │   Metrics   │    │ Event Store │   │
│                    │   Logging   │    │             │   │
│                    └─────────────┘    └─────────────┘   │
│                                                         │
└─────────────────────────────────────────────────────────┘
```

### **🎯 Responsabilidades do Command Bus**

```java
// Localização: command/CommandBus.java
public interface CommandBus {
    
    /**
     * RESPONSABILIDADES PRINCIPAIS:
     * 
     * 1. ROTEAMENTO: Encontrar o handler correto para cada comando
     * 2. VALIDAÇÃO: Executar validações antes do processamento
     * 3. MIDDLEWARE: Aplicar cross-cutting concerns (logs, métricas, etc.)
     * 4. EXECUÇÃO: Executar o handler com timeout e error handling
     * 5. RESULTADO: Retornar resultado padronizado
     */
    
    /**
     * Envia comando para processamento síncrono
     */
    CommandResult send(Command command);
    
    /**
     * Envia comando para processamento assíncrono
     */
    CompletableFuture<CommandResult> sendAsync(Command command);
    
    /**
     * Registra handler para tipo de comando
     */
    <T extends Command> void registerHandler(CommandHandler<T> handler);
    
    /**
     * Remove handler registrado
     */
    void unregisterHandler(Class<? extends Command> commandType);
    
    /**
     * Verifica se existe handler para comando
     */
    boolean hasHandler(Class<? extends Command> commandType);
    
    /**
     * Obtém estatísticas de execução
     */
    CommandBusStatistics getStatistics();
}
```

---

## 📝 **PADRÃO COMMAND**

### **🏗️ Estrutura de um Command**

```java
// Localização: command/Command.java
public interface Command {
    
    /**
     * Identificador único do comando
     */
    UUID getCommandId();
    
    /**
     * Timestamp de criação
     */
    Instant getTimestamp();
    
    /**
     * ID de correlação para rastreamento
     */
    UUID getCorrelationId();
    
    /**
     * Usuário que emitiu o comando
     */
    String getUserId();
    
    /**
     * Tipo do comando (nome da classe por padrão)
     */
    default String getCommandType() {
        return this.getClass().getSimpleName();
    }
}

// Implementação base para comandos
public abstract class BaseCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    protected BaseCommand(String userId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID();
        this.userId = userId;
    }
    
    protected BaseCommand(String userId, UUID correlationId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
    }
    
    // Getters...
    @Override
    public UUID getCommandId() { return commandId; }
    
    @Override
    public Instant getTimestamp() { return timestamp; }
    
    @Override
    public UUID getCorrelationId() { return correlationId; }
    
    @Override
    public String getUserId() { return userId; }
    
    @Override
    public String toString() {
        return String.format("%s{id=%s, user=%s, timestamp=%s}", 
                           getCommandType(), commandId, userId, timestamp);
    }
}
```

### **📋 Exemplo de Command Específico**

```java
// Localização: command/sinistro/CriarSinistroCommand.java
public class CriarSinistroCommand extends BaseCommand {
    
    // Dados obrigatórios
    private final String cpfSegurado;
    private final String placaVeiculo;
    private final String descricaoOcorrencia;
    private final LocalDateTime dataOcorrencia;
    private final String enderecoOcorrencia;
    
    // Dados opcionais
    private final BigDecimal valorEstimado;
    private final String canalAbertura;
    private final Map<String, String> metadados;
    
    // Construtor principal
    public CriarSinistroCommand(String userId,
                               String cpfSegurado,
                               String placaVeiculo,
                               String descricaoOcorrencia,
                               LocalDateTime dataOcorrencia,
                               String enderecoOcorrencia) {
        super(userId);
        this.cpfSegurado = Objects.requireNonNull(cpfSegurado, "CPF do segurado é obrigatório");
        this.placaVeiculo = Objects.requireNonNull(placaVeiculo, "Placa do veículo é obrigatória");
        this.descricaoOcorrencia = Objects.requireNonNull(descricaoOcorrencia, "Descrição é obrigatória");
        this.dataOcorrencia = Objects.requireNonNull(dataOcorrencia, "Data da ocorrência é obrigatória");
        this.enderecoOcorrencia = Objects.requireNonNull(enderecoOcorrencia, "Endereço é obrigatório");
        
        // Opcionais
        this.valorEstimado = null;
        this.canalAbertura = "WEB";
        this.metadados = new HashMap<>();
    }
    
    // Construtor completo
    public CriarSinistroCommand(String userId,
                               String cpfSegurado,
                               String placaVeiculo,
                               String descricaoOcorrencia,
                               LocalDateTime dataOcorrencia,
                               String enderecoOcorrencia,
                               BigDecimal valorEstimado,
                               String canalAbertura,
                               Map<String, String> metadados) {
        super(userId);
        this.cpfSegurado = Objects.requireNonNull(cpfSegurado);
        this.placaVeiculo = Objects.requireNonNull(placaVeiculo);
        this.descricaoOcorrencia = Objects.requireNonNull(descricaoOcorrencia);
        this.dataOcorrencia = Objects.requireNonNull(dataOcorrencia);
        this.enderecoOcorrencia = Objects.requireNonNull(enderecoOcorrencia);
        this.valorEstimado = valorEstimado;
        this.canalAbertura = canalAbertura != null ? canalAbertura : "WEB";
        this.metadados = metadados != null ? new HashMap<>(metadados) : new HashMap<>();
    }
    
    // Builder pattern para facilitar criação
    public static Builder builder(String userId) {
        return new Builder(userId);
    }
    
    public static class Builder {
        private final String userId;
        private String cpfSegurado;
        private String placaVeiculo;
        private String descricaoOcorrencia;
        private LocalDateTime dataOcorrencia;
        private String enderecoOcorrencia;
        private BigDecimal valorEstimado;
        private String canalAbertura = "WEB";
        private Map<String, String> metadados = new HashMap<>();
        
        private Builder(String userId) {
            this.userId = userId;
        }
        
        public Builder cpfSegurado(String cpfSegurado) {
            this.cpfSegurado = cpfSegurado;
            return this;
        }
        
        public Builder placaVeiculo(String placaVeiculo) {
            this.placaVeiculo = placaVeiculo;
            return this;
        }
        
        public Builder descricaoOcorrencia(String descricaoOcorrencia) {
            this.descricaoOcorrencia = descricaoOcorrencia;
            return this;
        }
        
        public Builder dataOcorrencia(LocalDateTime dataOcorrencia) {
            this.dataOcorrencia = dataOcorrencia;
            return this;
        }
        
        public Builder enderecoOcorrencia(String enderecoOcorrencia) {
            this.enderecoOcorrencia = enderecoOcorrencia;
            return this;
        }
        
        public Builder valorEstimado(BigDecimal valorEstimado) {
            this.valorEstimado = valorEstimado;
            return this;
        }
        
        public Builder canalAbertura(String canalAbertura) {
            this.canalAbertura = canalAbertura;
            return this;
        }
        
        public Builder metadado(String chave, String valor) {
            this.metadados.put(chave, valor);
            return this;
        }
        
        public Builder metadados(Map<String, String> metadados) {
            this.metadados.putAll(metadados);
            return this;
        }
        
        public CriarSinistroCommand build() {
            return new CriarSinistroCommand(
                userId, cpfSegurado, placaVeiculo, descricaoOcorrencia,
                dataOcorrencia, enderecoOcorrencia, valorEstimado,
                canalAbertura, metadados
            );
        }
    }
    
    // Getters
    public String getCpfSegurado() { return cpfSegurado; }
    public String getPlacaVeiculo() { return placaVeiculo; }
    public String getDescricaoOcorrencia() { return descricaoOcorrencia; }
    public LocalDateTime getDataOcorrencia() { return dataOcorrencia; }
    public String getEnderecoOcorrencia() { return enderecoOcorrencia; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public String getCanalAbertura() { return canalAbertura; }
    public Map<String, String> getMetadados() { return new HashMap<>(metadados); }
    
    // Métodos de conveniência
    public boolean temValorEstimado() {
        return valorEstimado != null && valorEstimado.compareTo(BigDecimal.ZERO) > 0;
    }
    
    public boolean isCanal(String canal) {
        return canalAbertura.equalsIgnoreCase(canal);
    }
    
    public String getMetadado(String chave) {
        return metadados.get(chave);
    }
    
    public boolean temMetadado(String chave) {
        return metadados.containsKey(chave);
    }
}
```

---

## 🎛️ **COMMAND HANDLERS**

### **🔧 Interface CommandHandler**

```java
// Localização: command/CommandHandler.java
public interface CommandHandler<T extends Command> {
    
    /**
     * Processa o comando e retorna resultado
     */
    CommandResult handle(T command);
    
    /**
     * Tipo de comando processado por este handler
     */
    Class<T> getCommandType();
    
    /**
     * Indica se handler suporta o comando
     */
    default boolean supports(Class<? extends Command> commandType) {
        return getCommandType().equals(commandType);
    }
    
    /**
     * Timeout para execução (segundos)
     */
    default int getTimeoutSeconds() {
        return 30;
    }
    
    /**
     * Prioridade do handler (menor = maior prioridade)
     */
    default int getPriority() {
        return 100;
    }
    
    /**
     * Indica se handler pode ser executado em paralelo
     */
    default boolean isParallelizable() {
        return false;
    }
}

// Handler base com funcionalidades comuns
public abstract class BaseCommandHandler<T extends Command> implements CommandHandler<T> {
    
    protected final Logger log = LoggerFactory.getLogger(getClass());
    
    @Override
    public final CommandResult handle(T command) {
        
        log.debug("Processando comando: {}", command);
        
        try {
            // Template method pattern
            validateCommand(command);
            CommandResult result = doHandle(command);
            
            log.debug("Comando processado com sucesso: {}", command.getCommandId());
            return result;
            
        } catch (ValidationException e) {
            log.warn("Erro de validação no comando {}: {}", command.getCommandId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR");
            
        } catch (BusinessRuleException e) {
            log.warn("Violação de regra de negócio no comando {}: {}", command.getCommandId(), e.getMessage());
            return CommandResult.failure(e.getMessage(), "BUSINESS_RULE_VIOLATION");
            
        } catch (Exception e) {
            log.error("Erro inesperado no comando {}: {}", command.getCommandId(), e.getMessage(), e);
            return CommandResult.failure("Erro interno do sistema", "INTERNAL_ERROR");
        }
    }
    
    /**
     * Validação específica do comando (template method)
     */
    protected void validateCommand(T command) {
        // Implementação padrão vazia - sobrescrever se necessário
    }
    
    /**
     * Processamento específico do comando (template method)
     */
    protected abstract CommandResult doHandle(T command);
}
```

### **🏗️ Implementação de Handler Específico**

```java
// Localização: command/sinistro/CriarSinistroCommandHandler.java
@Component
public class CriarSinistroCommandHandler extends BaseCommandHandler<CriarSinistroCommand> {
    
    private final SinistroAggregateRepository sinistroRepository;
    private final SeguradoService seguradoService;
    private final VeiculoService veiculoService;
    private final ApoliceService apoliceService;
    private final SinistroFactory sinistroFactory;
    
    public CriarSinistroCommandHandler(SinistroAggregateRepository sinistroRepository,
                                      SeguradoService seguradoService,
                                      VeiculoService veiculoService,
                                      ApoliceService apoliceService,
                                      SinistroFactory sinistroFactory) {
        this.sinistroRepository = sinistroRepository;
        this.seguradoService = seguradoService;
        this.veiculoService = veiculoService;
        this.apoliceService = apoliceService;
        this.sinistroFactory = sinistroFactory;
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 45; // Timeout maior devido às validações externas
    }
    
    @Override
    protected void validateCommand(CriarSinistroCommand command) {
        
        List<String> erros = new ArrayList<>();
        
        // Validações básicas
        if (StringUtils.isBlank(command.getCpfSegurado())) {
            erros.add("CPF do segurado é obrigatório");
        } else if (!CpfValidator.isValid(command.getCpfSegurado())) {
            erros.add("CPF do segurado é inválido");
        }
        
        if (StringUtils.isBlank(command.getPlacaVeiculo())) {
            erros.add("Placa do veículo é obrigatória");
        } else if (!PlacaValidator.isValid(command.getPlacaVeiculo())) {
            erros.add("Placa do veículo é inválida");
        }
        
        if (StringUtils.isBlank(command.getDescricaoOcorrencia())) {
            erros.add("Descrição da ocorrência é obrigatória");
        } else if (command.getDescricaoOcorrencia().length() < 10) {
            erros.add("Descrição deve ter pelo menos 10 caracteres");
        } else if (command.getDescricaoOcorrencia().length() > 2000) {
            erros.add("Descrição não pode exceder 2000 caracteres");
        }
        
        // Validação de data
        if (command.getDataOcorrencia() == null) {
            erros.add("Data da ocorrência é obrigatória");
        } else {
            LocalDateTime agora = LocalDateTime.now();
            if (command.getDataOcorrencia().isAfter(agora)) {
                erros.add("Data da ocorrência não pode ser futura");
            } else if (command.getDataOcorrencia().isBefore(agora.minusYears(1))) {
                erros.add("Data da ocorrência não pode ser anterior a 1 ano");
            }
        }
        
        // Validação de valor estimado
        if (command.getValorEstimado() != null) {
            if (command.getValorEstimado().compareTo(BigDecimal.ZERO) <= 0) {
                erros.add("Valor estimado deve ser positivo");
            } else if (command.getValorEstimado().compareTo(new BigDecimal("1000000")) > 0) {
                erros.add("Valor estimado não pode exceder R$ 1.000.000,00");
            }
        }
        
        if (!erros.isEmpty()) {
            throw new ValidationException("Comando inválido: " + String.join(", ", erros));
        }
    }
    
    @Override
    protected CommandResult doHandle(CriarSinistroCommand command) {
        
        try {
            // 1. Validar segurado
            Segurado segurado = seguradoService.buscarPorCpf(command.getCpfSegurado());
            if (!segurado.isAtivo()) {
                return CommandResult.failure("Segurado não está ativo", "SEGURADO_INATIVO");
            }
            
            // 2. Validar veículo
            Veiculo veiculo = veiculoService.buscarPorPlaca(command.getPlacaVeiculo());
            if (!veiculo.pertenceAoSegurado(segurado.getId())) {
                return CommandResult.failure("Veículo não pertence ao segurado", "VEICULO_NAO_PERTENCE");
            }
            
            // 3. Validar apólice vigente
            Optional<Apolice> apoliceOpt = apoliceService.buscarApoliceVigente(
                segurado.getId(), 
                veiculo.getId(), 
                command.getDataOcorrencia().toLocalDate()
            );
            
            if (apoliceOpt.isEmpty()) {
                return CommandResult.failure(
                    "Não há apólice vigente para o veículo na data da ocorrência", 
                    "APOLICE_NAO_VIGENTE"
                );
            }
            
            Apolice apolice = apoliceOpt.get();
            
            // 4. Verificar limite de sinistros abertos
            long sinistrosAbertos = sinistroRepository.countSinistrosAbertos(segurado.getId());
            if (sinistrosAbertos >= 3) {
                return CommandResult.failure(
                    "Segurado já possui o limite máximo de sinistros abertos (3)", 
                    "LIMITE_SINISTROS_EXCEDIDO"
                );
            }
            
            // 5. Criar aggregate usando factory
            CriarSinistroRequest factoryRequest = CriarSinistroRequest.builder()
                .cpfSegurado(command.getCpfSegurado())
                .placaVeiculo(command.getPlacaVeiculo())
                .descricaoOcorrencia(command.getDescricaoOcorrencia())
                .dataOcorrencia(command.getDataOcorrencia())
                .enderecoOcorrencia(command.getEnderecoOcorrencia())
                .valorEstimado(command.getValorEstimado())
                .canalAbertura(command.getCanalAbertura())
                .metadados(command.getMetadados())
                .usuarioCriacao(command.getUserId())
                .build();
            
            SinistroAggregate sinistro = sinistroFactory.criarSinistro(factoryRequest);
            
            // 6. Salvar aggregate (persiste eventos)
            sinistroRepository.save(sinistro);
            
            // 7. Retornar resultado de sucesso
            return CommandResult.success(Map.of(
                "sinistroId", sinistro.getId().getValue(),
                "numeroSinistro", sinistro.getNumero().getValor(),
                "status", sinistro.getStatus().name(),
                "protocolo", sinistro.getNumero().getValor()
            ));
            
        } catch (SeguradoNaoEncontradoException e) {
            return CommandResult.failure("Segurado não encontrado", "SEGURADO_NAO_ENCONTRADO");
            
        } catch (VeiculoNaoEncontradoException e) {
            return CommandResult.failure("Veículo não encontrado", "VEICULO_NAO_ENCONTRADO");
            
        } catch (BusinessRuleException e) {
            return CommandResult.failure(e.getMessage(), "BUSINESS_RULE_VIOLATION");
            
        } catch (Exception e) {
            log.error("Erro inesperado ao criar sinistro: {}", e.getMessage(), e);
            return CommandResult.failure("Erro interno do sistema", "INTERNAL_ERROR");
        }
    }
}
```

---

## 📊 **COMMAND RESULT**

### **🎯 Estrutura do Resultado**

```java
// Localização: command/CommandResult.java
public class CommandResult {
    
    private final boolean success;
    private final Object data;
    private final String errorMessage;
    private final String errorCode;
    private final Map<String, Object> metadata;
    private final Long executionTimeMs;
    private final UUID correlationId;
    
    private CommandResult(boolean success, 
                         Object data, 
                         String errorMessage, 
                         String errorCode,
                         Map<String, Object> metadata,
                         Long executionTimeMs,
                         UUID correlationId) {
        this.success = success;
        this.data = data;
        this.errorMessage = errorMessage;
        this.errorCode = errorCode;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.executionTimeMs = executionTimeMs;
        this.correlationId = correlationId;
    }
    
    // Factory methods para sucesso
    public static CommandResult success() {
        return new CommandResult(true, null, null, null, null, null, null);
    }
    
    public static CommandResult success(Object data) {
        return new CommandResult(true, data, null, null, null, null, null);
    }
    
    public static CommandResult success(Object data, Map<String, Object> metadata) {
        return new CommandResult(true, data, null, null, metadata, null, null);
    }
    
    // Factory methods para falha
    public static CommandResult failure(String errorMessage) {
        return new CommandResult(false, null, errorMessage, null, null, null, null);
    }
    
    public static CommandResult failure(String errorMessage, String errorCode) {
        return new CommandResult(false, null, errorMessage, errorCode, null, null, null);
    }
    
    public static CommandResult failure(Exception exception) {
        return new CommandResult(false, null, exception.getMessage(), 
                               exception.getClass().getSimpleName(), null, null, null);
    }
    
    // Métodos fluentes para enriquecer resultado
    public CommandResult withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        return new CommandResult(success, data, errorMessage, errorCode, 
                               newMetadata, executionTimeMs, correlationId);
    }
    
    public CommandResult withExecutionTime(Long executionTimeMs) {
        return new CommandResult(success, data, errorMessage, errorCode, 
                               metadata, executionTimeMs, correlationId);
    }
    
    public CommandResult withCorrelationId(UUID correlationId) {
        return new CommandResult(success, data, errorMessage, errorCode, 
                               metadata, executionTimeMs, correlationId);
    }
    
    // Getters
    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public Object getData() { return data; }
    public String getErrorMessage() { return errorMessage; }
    public String getErrorCode() { return errorCode; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    public Long getExecutionTimeMs() { return executionTimeMs; }
    public UUID getCorrelationId() { return correlationId; }
    
    // Métodos de conveniência
    @SuppressWarnings("unchecked")
    public <T> T getDataAs(Class<T> type) {
        if (data == null) {
            return null;
        }
        
        if (type.isInstance(data)) {
            return (T) data;
        }
        
        throw new ClassCastException("Data is not of type " + type.getSimpleName());
    }
    
    public Object getMetadata(String key) {
        return metadata.get(key);
    }
    
    @SuppressWarnings("unchecked")
    public <T> T getMetadata(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value == null) {
            return null;
        }
        
        if (type.isInstance(value)) {
            return (T) value;
        }
        
        throw new ClassCastException("Metadata '" + key + "' is not of type " + type.getSimpleName());
    }
    
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    @Override
    public String toString() {
        if (success) {
            return String.format("CommandResult{success=true, data=%s, executionTime=%dms}", 
                               data, executionTimeMs);
        } else {
            return String.format("CommandResult{success=false, error=%s, code=%s}", 
                               errorMessage, errorCode);
        }
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Implementar Command e Handler básicos

#### **Passo 1: Criar Command Simples**
```java
public class AtualizarSinistroCommand extends BaseCommand {
    
    private final String sinistroId;
    private final String novaDescricao;
    private final BigDecimal novoValorEstimado;
    private final String observacoes;
    
    public AtualizarSinistroCommand(String userId,
                                   String sinistroId,
                                   String novaDescricao,
                                   BigDecimal novoValorEstimado,
                                   String observacoes) {
        super(userId);
        this.sinistroId = Objects.requireNonNull(sinistroId);
        this.novaDescricao = novaDescricao;
        this.novoValorEstimado = novoValorEstimado;
        this.observacoes = observacoes;
    }
    
    // Getters...
}
```

#### **Passo 2: Implementar Handler**
```java
@Component
public class AtualizarSinistroCommandHandler extends BaseCommandHandler<AtualizarSinistroCommand> {
    
    private final SinistroAggregateRepository repository;
    
    @Override
    public Class<AtualizarSinistroCommand> getCommandType() {
        return AtualizarSinistroCommand.class;
    }
    
    @Override
    protected CommandResult doHandle(AtualizarSinistroCommand command) {
        
        // 1. Carregar aggregate
        SinistroAggregate sinistro = repository.getById(
            SinistroId.of(command.getSinistroId())
        );
        
        // 2. Aplicar mudanças
        if (command.getNovaDescricao() != null) {
            sinistro.atualizarDescricao(command.getNovaDescricao());
        }
        
        if (command.getNovoValorEstimado() != null) {
            sinistro.atualizarValorEstimado(
                ValorMonetario.of(command.getNovoValorEstimado())
            );
        }
        
        if (command.getObservacoes() != null) {
            sinistro.adicionarObservacao(command.getObservacoes(), command.getUserId());
        }
        
        // 3. Salvar
        repository.save(sinistro);
        
        return CommandResult.success(Map.of(
            "sinistroId", sinistro.getId().getValue(),
            "versao", sinistro.getVersion()
        ));
    }
}
```

#### **Passo 3: Testar Command**
```java
@Test
public void testarCriacaoComando() {
    // Criar comando
    CriarSinistroCommand comando = CriarSinistroCommand.builder("user123")
        .cpfSegurado("12345678901")
        .placaVeiculo("ABC1234")
        .descricaoOcorrencia("Teste de comando")
        .dataOcorrencia(LocalDateTime.now().minusHours(2))
        .enderecoOcorrencia("Rua Teste, 123")
        .valorEstimado(new BigDecimal("5000.00"))
        .canalAbertura("API")
        .metadado("origem", "teste")
        .build();
    
    // Verificar propriedades
    assertThat(comando.getCpfSegurado()).isEqualTo("12345678901");
    assertThat(comando.getCommandId()).isNotNull();
    assertThat(comando.getTimestamp()).isNotNull();
    assertThat(comando.getUserId()).isEqualTo("user123");
    assertThat(comando.temValorEstimado()).isTrue();
    assertThat(comando.getMetadado("origem")).isEqualTo("teste");
}
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** a arquitetura e responsabilidades do Command Bus
2. **Implementar** Commands seguindo o padrão estabelecido
3. **Criar** Command Handlers com validação e tratamento de erros
4. **Usar** CommandResult para retornar resultados padronizados
5. **Aplicar** o padrão Template Method em handlers

### **❓ Perguntas para Reflexão:**

1. Por que usar Command Bus ao invés de chamar services diretamente?
2. Como garantir que Commands sejam imutáveis?
3. Qual a diferença entre validação técnica e de negócio?
4. Como tratar timeouts em Command Handlers?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 2**, vamos aprofundar:
- Implementação detalhada do Command Bus
- Sistema de registro de handlers
- Middleware e interceptadores
- Configuração e inicialização

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 36 minutos  
**📋 Pré-requisitos:** Capítulos 01-04 completos