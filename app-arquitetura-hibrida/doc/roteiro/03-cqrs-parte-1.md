# 📖 CAPÍTULO 03: CQRS - PARTE 1
## Fundamentos e Separação de Responsabilidades

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os princípios fundamentais do CQRS
- Entender a separação entre Command Side e Query Side
- Explorar as vantagens e trade-offs do padrão
- Conhecer a implementação no projeto

---

## 🧠 **O QUE É CQRS?**

### **📚 Definição**
**CQRS (Command Query Responsibility Segregation)** é um padrão arquitetural que **separa as operações de leitura (Query) das operações de escrita (Command)** em modelos de dados distintos.

### **🔄 Arquitetura Tradicional vs CQRS**

#### **Arquitetura Tradicional (CRUD):**
```
┌─────────────────────────────────────────────────────┐
│                 APLICAÇÃO CRUD                      │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────┐    ┌─────────────┐                │
│  │   Create    │    │    Read     │                │
│  │   Update    │◄──►│   Queries   │                │
│  │   Delete    │    │             │                │
│  └─────────────┘    └─────────────┘                │
│         │                   ▲                      │
│         ▼                   │                      │
│  ┌─────────────────────────────────────────────────┤
│  │          MESMO MODELO DE DADOS                  │
│  │         (Tabelas Relacionais)                   │
│  └─────────────────────────────────────────────────┘
│                                                     │
└─────────────────────────────────────────────────────┘

❌ Problemas:
- Modelo único serve mal a ambos os propósitos
- Consultas complexas afetam performance de escrita
- Difícil otimizar para casos específicos
- Acoplamento entre leitura e escrita
```

#### **Arquitetura CQRS:**
```
┌─────────────────────────────────────────────────────┐
│                ARQUITETURA CQRS                     │
├─────────────────────────────────────────────────────┤
│                                                     │
│  ┌─────────────────┐    ┌─────────────────┐        │
│  │  COMMAND SIDE   │    │   QUERY SIDE    │        │
│  │   (Escrita)     │    │   (Leitura)     │        │
│  │                 │    │                 │        │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │        │
│  │ │  Commands   │ │    │ │   Queries   │ │        │
│  │ │  Handlers   │ │    │ │  Services   │ │        │
│  │ └─────────────┘ │    │ └─────────────┘ │        │
│  │        │        │    │        ▲        │        │
│  │        ▼        │    │        │        │        │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │        │
│  │ │   Write     │ │    │ │    Read     │ │        │
│  │ │   Model     │ │    │ │   Model     │ │        │
│  │ └─────────────┘ │    │ └─────────────┘ │        │
│  │        │        │    │        ▲        │        │
│  │        ▼        │    │        │        │        │
│  │ ┌─────────────┐ │    │ ┌─────────────┐ │        │
│  │ │ Event Store │ │────┼─│ Projections │ │        │
│  │ │(PostgreSQL) │ │    │ │(PostgreSQL) │ │        │
│  │ └─────────────┘ │    │ └─────────────┘ │        │
│  └─────────────────┘    └─────────────────┘        │
│                                                     │
└─────────────────────────────────────────────────────┘

✅ Vantagens:
- Modelos otimizados para cada propósito
- Escalabilidade independente
- Performance otimizada
- Flexibilidade de tecnologias
```

---

## 🏗️ **COMMAND SIDE (LADO DE ESCRITA)**

### **🎯 Responsabilidades do Command Side**

1. **Processar comandos** (intenções de mudança)
2. **Aplicar regras de negócio**
3. **Manter consistência transacional**
4. **Gerar eventos de domínio**
5. **Persistir no Event Store**

### **📝 Componentes do Command Side**

#### **1. Commands (Comandos)**
```java
// Localização: command/Command.java
public interface Command {
    UUID getCommandId();
    Instant getTimestamp();
    UUID getCorrelationId();
    String getUserId();
    
    default String getCommandType() {
        return this.getClass().getSimpleName();
    }
}

// Exemplo prático - Comando para criar sinistro
public class CriarSinistroCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    // Dados específicos do comando
    private final String cpfSegurado;
    private final String placaVeiculo;
    private final String descricaoOcorrencia;
    private final LocalDateTime dataOcorrencia;
    private final String enderecoOcorrencia;
    private final BigDecimal valorEstimado;
    
    public CriarSinistroCommand(String cpfSegurado, 
                               String placaVeiculo,
                               String descricaoOcorrencia,
                               LocalDateTime dataOcorrencia,
                               String enderecoOcorrencia,
                               BigDecimal valorEstimado,
                               String userId) {
        
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = UUID.randomUUID();
        this.userId = userId;
        
        this.cpfSegurado = cpfSegurado;
        this.placaVeiculo = placaVeiculo;
        this.descricaoOcorrencia = descricaoOcorrencia;
        this.dataOcorrencia = dataOcorrencia;
        this.enderecoOcorrencia = enderecoOcorrencia;
        this.valorEstimado = valorEstimado;
    }
    
    // Getters...
}
```

#### **2. Command Handlers**
```java
// Localização: command/CommandHandler.java
public interface CommandHandler<T extends Command> {
    
    /**
     * Processa o comando e retorna resultado
     */
    CommandResult handle(T command);
    
    /**
     * Tipo de comando processado
     */
    Class<T> getCommandType();
    
    /**
     * Timeout para execução
     */
    default int getTimeoutSeconds() {
        return 30;
    }
}

// Implementação específica
@Component
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {
    
    private final SinistroAggregateRepository repository;
    private final SeguradoService seguradoService;
    private final VeiculoService veiculoService;
    
    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        
        try {
            // 1. Validações de negócio
            validarComando(command);
            
            // 2. Verificar se segurado existe e está ativo
            Segurado segurado = seguradoService.buscarPorCpf(command.getCpfSegurado());
            if (!segurado.isAtivo()) {
                return CommandResult.failure("Segurado não está ativo");
            }
            
            // 3. Verificar se veículo está coberto
            Veiculo veiculo = veiculoService.buscarPorPlaca(command.getPlacaVeiculo());
            if (!veiculo.isCobertoPorSegurado(segurado.getId())) {
                return CommandResult.failure("Veículo não está coberto pelo segurado");
            }
            
            // 4. Criar aggregate de sinistro
            SinistroAggregate sinistro = new SinistroAggregate();
            sinistro.criar(
                command.getCpfSegurado(),
                command.getPlacaVeiculo(),
                command.getDescricaoOcorrencia(),
                command.getDataOcorrencia(),
                command.getEnderecoOcorrencia(),
                command.getValorEstimado(),
                command.getUserId()
            );
            
            // 5. Salvar (persiste eventos no Event Store)
            repository.save(sinistro);
            
            // 6. Retornar sucesso com dados do sinistro criado
            return CommandResult.success(Map.of(
                "sinistroId", sinistro.getId(),
                "protocolo", sinistro.getProtocolo(),
                "status", sinistro.getStatus()
            ));
            
        } catch (ValidationException e) {
            return CommandResult.failure(e.getMessage(), "VALIDATION_ERROR");
        } catch (BusinessRuleException e) {
            return CommandResult.failure(e.getMessage(), "BUSINESS_RULE_VIOLATION");
        } catch (Exception e) {
            log.error("Erro ao processar comando CriarSinistro: {}", e.getMessage(), e);
            return CommandResult.failure("Erro interno do sistema", "INTERNAL_ERROR");
        }
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    private void validarComando(CriarSinistroCommand command) {
        List<String> erros = new ArrayList<>();
        
        if (StringUtils.isBlank(command.getCpfSegurado())) {
            erros.add("CPF do segurado é obrigatório");
        }
        
        if (StringUtils.isBlank(command.getPlacaVeiculo())) {
            erros.add("Placa do veículo é obrigatória");
        }
        
        if (StringUtils.isBlank(command.getDescricaoOcorrencia())) {
            erros.add("Descrição da ocorrência é obrigatória");
        }
        
        if (command.getDataOcorrencia() == null) {
            erros.add("Data da ocorrência é obrigatória");
        } else if (command.getDataOcorrencia().isAfter(LocalDateTime.now())) {
            erros.add("Data da ocorrência não pode ser futura");
        }
        
        if (command.getValorEstimado() != null && 
            command.getValorEstimado().compareTo(BigDecimal.ZERO) <= 0) {
            erros.add("Valor estimado deve ser positivo");
        }
        
        if (!erros.isEmpty()) {
            throw new ValidationException("Comando inválido: " + String.join(", ", erros));
        }
    }
}
```

#### **3. Command Bus**
```java
// Localização: command/CommandBus.java
public interface CommandBus {
    
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
     * Verifica se existe handler para comando
     */
    boolean hasHandler(Class<? extends Command> commandType);
    
    /**
     * Obtém estatísticas do Command Bus
     */
    CommandBusStatistics getStatistics();
}

// Implementação
@Component
public class SimpleCommandBus implements CommandBus {
    
    private final CommandHandlerRegistry handlerRegistry;
    private final CommandBusMetrics metrics;
    private final ExecutorService asyncExecutor;
    
    @Override
    public CommandResult send(Command command) {
        
        Timer.Sample sample = metrics.startExecutionTimer();
        
        try {
            // 1. Validar comando
            validateCommand(command);
            
            // 2. Encontrar handler
            CommandHandler<Command> handler = getHandlerForCommand(command);
            
            // 3. Executar com timeout
            CommandResult result = executeWithTimeout(command, handler);
            
            // 4. Atualizar métricas
            if (result.isSuccess()) {
                metrics.incrementCommandsProcessed();
            } else {
                metrics.incrementCommandsFailed();
            }
            
            return result;
            
        } catch (CommandHandlerNotFoundException e) {
            metrics.incrementCommandsRejected();
            return CommandResult.failure("Handler não encontrado para: " + 
                                       command.getCommandType());
        } catch (CommandTimeoutException e) {
            metrics.incrementCommandsTimeout();
            return CommandResult.failure("Timeout ao processar comando");
        } catch (Exception e) {
            metrics.incrementCommandsFailed();
            return CommandResult.failure("Erro interno: " + e.getMessage());
        } finally {
            metrics.stopExecutionTimer(sample);
        }
    }
    
    @Override
    public CompletableFuture<CommandResult> sendAsync(Command command) {
        
        return CompletableFuture.supplyAsync(() -> send(command), asyncExecutor);
    }
    
    private CommandResult executeWithTimeout(Command command, 
                                           CommandHandler<Command> handler) {
        
        int timeoutSeconds = handler.getTimeoutSeconds();
        
        try {
            return CompletableFuture
                .supplyAsync(() -> handler.handle(command))
                .get(timeoutSeconds, TimeUnit.SECONDS);
                
        } catch (TimeoutException e) {
            throw new CommandTimeoutException(
                command.getCommandId().toString(),
                command.getClass(),
                timeoutSeconds,
                timeoutSeconds * 1000L
            );
        } catch (Exception e) {
            throw new CommandExecutionException(
                command.getCommandId().toString(),
                command.getClass(),
                e
            );
        }
    }
}
```

---

## 🔍 **QUERY SIDE (LADO DE LEITURA)**

### **🎯 Responsabilidades do Query Side**

1. **Executar consultas otimizadas**
2. **Fornecer dados desnormalizados**
3. **Suportar múltiplas visões dos dados**
4. **Garantir performance de leitura**
5. **Implementar cache inteligente**

### **📊 Componentes do Query Side**

#### **1. Query Models (Modelos de Leitura)**
```java
// Localização: query/model/SinistroQueryModel.java
@Entity
@Table(name = "sinistro_view", schema = "projections")
public class SinistroQueryModel {
    
    @Id
    private UUID id;
    
    // Dados básicos do sinistro
    private String protocolo;
    private String status;
    private String tipoSinistro;
    private String prioridade;
    
    // Dados do segurado (desnormalizados)
    private String cpfSegurado;
    private String nomeSegurado;
    private String emailSegurado;
    private String telefoneSegurado;
    
    // Dados do veículo (desnormalizados)
    private String placa;
    private String marca;
    private String modelo;
    private Integer anoFabricacao;
    private Integer anoModelo;
    private String cor;
    private String chassi;
    private String renavam;
    
    // Dados da ocorrência
    private Instant dataOcorrencia;
    private String enderecoOcorrencia;
    private String cidadeOcorrencia;
    private String estadoOcorrencia;
    private String cepOcorrencia;
    private String descricao;
    
    // Dados da apólice (desnormalizados)
    private String apoliceNumero;
    private LocalDate apoliceVigenciaInicio;
    private LocalDate apoliceVigenciaFim;
    private BigDecimal apoliceValorSegurado;
    private BigDecimal valorFranquia;
    
    // Dados operacionais
    private String operadorResponsavel;
    private String canalAbertura;
    private Instant dataAbertura;
    private Instant dataFechamento;
    private BigDecimal valorEstimado;
    
    // Dados DETRAN
    private Boolean consultaDetranRealizada;
    private String consultaDetranStatus;
    private Instant consultaDetranTimestamp;
    
    @Column(columnDefinition = "jsonb")
    private Map<String, Object> dadosDetran;
    
    // Tags para categorização
    @Column(columnDefinition = "text[]")
    private List<String> tags;
    
    // Controle de versão e auditoria
    private Long version;
    private Long lastEventId;
    private Instant createdAt;
    private Instant updatedAt;
    
    // Métodos de conveniência
    public boolean isAberto() {
        return "ABERTO".equals(status) || "EM_ANALISE".equals(status);
    }
    
    public boolean isFechado() {
        return "FECHADO".equals(status) || "CANCELADO".equals(status);
    }
    
    public boolean isApoliceVigenteNaOcorrencia() {
        if (dataOcorrencia == null || apoliceVigenciaInicio == null || apoliceVigenciaFim == null) {
            return false;
        }
        
        LocalDate dataOcorrenciaLocal = dataOcorrencia.atZone(ZoneId.systemDefault()).toLocalDate();
        return !dataOcorrenciaLocal.isBefore(apoliceVigenciaInicio) && 
               !dataOcorrenciaLocal.isAfter(apoliceVigenciaFim);
    }
    
    public boolean possuiTag(String tag) {
        return tags != null && tags.contains(tag);
    }
    
    public void adicionarTag(String tag) {
        if (tags == null) {
            tags = new ArrayList<>();
        }
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }
    
    @PreUpdate
    public void onUpdate() {
        this.updatedAt = Instant.now();
    }
    
    // Getters e Setters...
}
```

#### **2. Query Services**
```java
// Localização: query/service/SinistroQueryService.java
public interface SinistroQueryService {
    
    /**
     * Busca sinistro por ID
     */
    Optional<SinistroDetailView> buscarPorId(UUID id);
    
    /**
     * Busca sinistro por protocolo
     */
    Optional<SinistroDetailView> buscarPorProtocolo(String protocolo);
    
    /**
     * Lista sinistros com filtros e paginação
     */
    Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable);
    
    /**
     * Busca sinistros por CPF do segurado
     */
    Page<SinistroListView> buscarPorCpfSegurado(String cpf, Pageable pageable);
    
    /**
     * Busca sinistros por placa do veículo
     */
    Page<SinistroListView> buscarPorPlaca(String placa, Pageable pageable);
    
    /**
     * Busca textual em sinistros
     */
    Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable);
    
    /**
     * Obtém dados para dashboard
     */
    DashboardView obterDashboard();
}

// Implementação
@Service
@Transactional(readOnly = true)
public class SinistroQueryServiceImpl implements SinistroQueryService {
    
    private final SinistroQueryRepository repository;
    
    @Override
    @Cacheable(value = "sinistros", key = "#id")
    public Optional<SinistroDetailView> buscarPorId(UUID id) {
        
        return repository.findById(id)
            .map(this::toDetailView);
    }
    
    @Override
    @Cacheable(value = "sinistros", key = "#protocolo")
    public Optional<SinistroDetailView> buscarPorProtocolo(String protocolo) {
        
        return repository.findByProtocolo(protocolo)
            .map(this::toDetailView);
    }
    
    @Override
    public Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable) {
        
        // Usar Specification para consultas dinâmicas
        Specification<SinistroQueryModel> spec = buildSpecification(filter);
        
        return repository.findAll(spec, pageable)
            .map(this::toListView);
    }
    
    @Override
    public Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable) {
        
        // Usar full-text search do PostgreSQL
        return repository.findByFullTextSearchPaged(termo, pageable)
            .map(this::toListView);
    }
    
    @Override
    @Cacheable(value = "dashboard", key = "'sinistros'")
    public DashboardView obterDashboard() {
        
        Instant umMesAtras = Instant.now().minus(30, ChronoUnit.DAYS);
        
        // Estatísticas gerais
        Object[] resumo = repository.getResumoExecutivo(umMesAtras);
        
        // Distribuição por status
        List<Object[]> porStatus = repository.countByStatus();
        
        // Distribuição por tipo
        List<Object[]> porTipo = repository.countByTipo();
        
        // Tendência diária
        List<Object[]> tendencia = repository.getEstatisticasPorDia(
            umMesAtras, Instant.now()
        );
        
        return DashboardView.builder()
            .totalSinistros((Long) resumo[0])
            .sinistrosAbertos((Long) resumo[1])
            .sinistrosFechados((Long) resumo[2])
            .valorMedio((BigDecimal) resumo[3])
            .comConsultaDetran((Long) resumo[4])
            .distribuicaoPorStatus(convertToMap(porStatus))
            .distribuicaoPorTipo(convertToMap(porTipo))
            .tendenciaDiaria(convertToTrendData(tendencia))
            .ultimaAtualizacao(Instant.now())
            .build();
    }
    
    private Specification<SinistroQueryModel> buildSpecification(SinistroFilter filter) {
        
        return (root, query, criteriaBuilder) -> {
            
            List<Predicate> predicates = new ArrayList<>();
            
            // Filtro por status
            if (StringUtils.isNotBlank(filter.getStatus())) {
                predicates.add(criteriaBuilder.equal(root.get("status"), filter.getStatus()));
            }
            
            // Filtro por CPF
            if (StringUtils.isNotBlank(filter.getCpfSegurado())) {
                predicates.add(criteriaBuilder.equal(root.get("cpfSegurado"), filter.getCpfSegurado()));
            }
            
            // Filtro por período
            if (filter.getDataAberturaInicio() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                    root.get("dataAbertura"), filter.getDataAberturaInicio()
                ));
            }
            
            if (filter.getDataAberturaFim() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                    root.get("dataAbertura"), filter.getDataAberturaFim()
                ));
            }
            
            // Filtro por operador
            if (StringUtils.isNotBlank(filter.getOperadorResponsavel())) {
                predicates.add(criteriaBuilder.equal(
                    root.get("operadorResponsavel"), filter.getOperadorResponsavel()
                ));
            }
            
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
```

---

## 🔄 **SINCRONIZAÇÃO ENTRE COMMAND E QUERY**

### **📡 Event-Driven Synchronization**

```java
// O Command Side gera eventos que são consumidos pelo Query Side
// através de Projection Handlers (veremos em detalhes no Capítulo 07)

// Fluxo simplificado:
// 1. Command Handler processa comando
// 2. Aggregate gera eventos
// 3. Eventos são salvos no Event Store
// 4. Event Bus publica eventos
// 5. Projection Handlers consomem eventos
// 6. Query Models são atualizados

// Exemplo de sincronização:
@Component
public class SinistroProjectionHandler extends AbstractProjectionHandler<SinistroEvent> {
    
    private final SinistroQueryRepository repository;
    
    @Override
    public void doHandle(SinistroEvent event) {
        
        switch (event.getEventType()) {
            case "SinistroCriado":
                handleSinistroCriado(event);
                break;
            case "SinistroAtualizado":
                handleSinistroAtualizado(event);
                break;
            case "StatusAlterado":
                handleStatusAlterado(event);
                break;
            // ... outros tipos de evento
        }
    }
    
    private void handleSinistroCriado(SinistroEvent event) {
        
        SinistroQueryModel queryModel = new SinistroQueryModel();
        queryModel.setId(UUID.fromString(event.getAggregateId()));
        queryModel.setProtocolo(event.getNumeroSinistro());
        queryModel.setStatus("ABERTO");
        queryModel.setDescricao(event.getDescricao());
        queryModel.setValorEstimado(event.getValorEstimado());
        queryModel.setDataAbertura(event.getTimestamp());
        queryModel.setCreatedAt(event.getTimestamp());
        queryModel.setVersion(event.getVersion());
        queryModel.setLastEventId(event.getEventId());
        
        repository.save(queryModel);
    }
}
```

---

## 🧪 **EXERCÍCIO PRÁTICO**

### **🎯 Objetivo**: Explorar separação Command/Query

#### **Passo 1: Examinar Command Side**
```java
// 1. Abrir: command/CommandBus.java
// 2. Entender interface e responsabilidades
// 3. Ver implementação SimpleCommandBus

// 4. Abrir: command/example/TestCommand.java
// 5. Ver estrutura de um comando simples

// 6. Abrir: command/example/TestCommandHandler.java
// 7. Entender como handler processa comando
```

#### **Passo 2: Examinar Query Side**
```java
// 1. Abrir: query/model/SinistroQueryModel.java
// 2. Ver campos desnormalizados
// 3. Entender métodos de conveniência

// 4. Abrir: query/service/SinistroQueryService.java
// 5. Ver interface de consultas
// 6. Entender diferentes tipos de busca
```

#### **Passo 3: Testar Separação**
```bash
# Verificar bancos separados
docker exec -it postgres-write psql -U postgres -d sinistros_eventstore -c "\dt eventstore.*"
docker exec -it postgres-read psql -U postgres -d sinistros_projections -c "\dt projections.*"

# Testar Command Side (se houver dados)
curl -X POST http://localhost:8083/api/v1/commands/test \
  -H "Content-Type: application/json" \
  -d '{"message": "Teste CQRS"}'

# Testar Query Side
curl http://localhost:8083/api/v1/query/sinistros | jq
```

---

## 📚 **CHECKPOINT DE APRENDIZADO**

### **✅ Você deve ser capaz de:**

1. **Explicar** a diferença entre CQRS e CRUD tradicional
2. **Identificar** responsabilidades do Command Side vs Query Side
3. **Reconhecer** componentes: Commands, Handlers, Query Models
4. **Entender** como a sincronização funciona via eventos
5. **Listar** vantagens e desvantagens do CQRS

### **❓ Perguntas para Reflexão:**

1. Por que separar comando de consulta?
2. Como garantir consistência entre os lados?
3. Quando CQRS é apropriado vs desnecessário?
4. Qual o impacto na complexidade do sistema?

---

## 🔗 **PRÓXIMA PARTE**

Na **Parte 2**, vamos aprofundar:
- Implementação detalhada do Command Side
- Padrões de validação e tratamento de erros
- Métricas e monitoramento de comandos
- Otimizações de performance

---

**📖 Parte elaborada por:** Principal Java Architect  
**🎯 Público-Alvo:** Analistas Java Junior  
**⏱️ Duração Estimada:** 48 minutos  
**📋 Pré-requisitos:** Capítulos 01-02 completos