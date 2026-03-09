# 💻 PRÁTICAS DE DESENVOLVIMENTO - PARTE 1
## Padrões de Código e Convenções

### 🎯 **OBJETIVOS DESTA PARTE**
- Estabelecer padrões de código consistentes
- Definir convenções de nomenclatura
- Implementar estruturas de projeto padronizadas
- Configurar ferramentas de qualidade de código

---

## 📋 **PADRÕES DE NOMENCLATURA**

### **🏗️ Estrutura de Pacotes**

A organização de pacotes segue a arquitetura hexagonal e DDD:

```
com.seguradora.hibrida/
├── aggregate/                    # Agregados de domínio
│   ├── {AggregateRoot}.java
│   ├── exception/
│   ├── repository/
│   └── validation/
├── command/                      # Command Side (Write)
│   ├── {Command}.java
│   ├── {CommandHandler}.java
│   ├── impl/
│   └── validation/
├── query/                        # Query Side (Read)
│   ├── dto/
│   ├── model/
│   ├── repository/
│   └── service/
├── eventbus/                     # Event Bus e Handlers
│   ├── {EventHandler}.java
│   ├── impl/
│   └── exception/
├── eventstore/                   # Event Store
│   ├── impl/
│   ├── serialization/
│   └── partition/
├── projection/                   # Projection Handlers
│   ├── {ProjectionHandler}.java
│   ├── tracking/
│   └── rebuild/
├── config/                       # Configurações
│   ├── datasource/
│   ├── security/
│   └── monitoring/
└── controller/                   # Controllers REST
    ├── {DomainController}.java
    └── dto/
```

### **📝 Convenções de Nomenclatura**

#### **Classes e Interfaces:**
```java
// ✅ Correto - Agregados
public class SinistroAggregate extends AggregateRoot { }
public class SeguradoAggregate extends AggregateRoot { }

// ✅ Correto - Comandos
public class CriarSinistroCommand implements Command { }
public class AtualizarSinistroCommand implements Command { }

// ✅ Correto - Command Handlers
@Component
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> { }

// ✅ Correto - Eventos
public class SinistroCriadoEvent extends DomainEvent { }
public class SinistroAtualizadoEvent extends DomainEvent { }

// ✅ Correto - Event Handlers
@Component
public class SinistroEventHandler implements EventHandler<SinistroEvent> { }

// ✅ Correto - Projection Handlers
@Component
public class SinistroProjectionHandler extends AbstractProjectionHandler<SinistroEvent> { }

// ✅ Correto - Query Models
@Entity
@Table(name = "sinistro_view", schema = "projections")
public class SinistroQueryModel { }

// ✅ Correto - DTOs
public class SinistroDetailView { }
public class SinistroListView { }
public class CriarSinistroRequest { }
```

#### **Métodos e Variáveis:**
```java
// ✅ Correto - Métodos de negócio
public void criarSinistro(String protocolo, String descricao) { }
public void atualizarStatus(StatusSinistro novoStatus) { }
public void adicionarDocumento(Documento documento) { }

// ✅ Correto - Métodos de consulta
public Optional<SinistroDetailView> buscarPorId(UUID id) { }
public Page<SinistroListView> listarPorCpf(String cpf, Pageable pageable) { }

// ✅ Correto - Variáveis
private final EventStore eventStore;
private final SinistroQueryRepository queryRepository;
private final CommandBus commandBus;

// ✅ Correto - Constantes
public static final String SINISTRO_CRIADO_EVENT = "SinistroCriadoEvent";
public static final int MAX_RETRY_ATTEMPTS = 3;
public static final Duration DEFAULT_TIMEOUT = Duration.ofSeconds(30);
```

---

## 🎨 **PADRÕES DE IMPLEMENTAÇÃO**

### **🏗️ Padrão para Agregados**

#### **Template Base para Agregados:**
```java
@Slf4j
public class SinistroAggregate extends AggregateRoot {
    
    // === ESTADO DO AGREGADO ===
    private String protocolo;
    private StatusSinistro status;
    private String cpfSegurado;
    private String descricao;
    private BigDecimal valorEstimado;
    private Instant dataAbertura;
    
    // === REGRAS DE NEGÓCIO ===
    private final List<BusinessRule> businessRules = new ArrayList<>();
    
    // === CONSTRUTOR PADRÃO ===
    public SinistroAggregate() {
        super();
        registerBusinessRules();
    }
    
    // === MÉTODOS DE COMANDO (PÚBLICOS) ===
    public void criar(String protocolo, String cpfSegurado, String descricao, 
                     BigDecimal valorEstimado) {
        
        // Validações básicas
        validateNotNull(protocolo, "Protocolo é obrigatório");
        validateNotNull(cpfSegurado, "CPF do segurado é obrigatório");
        validateNotNull(descricao, "Descrição é obrigatória");
        validateNotNull(valorEstimado, "Valor estimado é obrigatório");
        
        // Validações de negócio
        validateBusinessRules();
        
        // Aplicar evento
        applyEvent(SinistroCriadoEvent.builder()
            .aggregateId(getId())
            .protocolo(protocolo)
            .cpfSegurado(cpfSegurado)
            .descricao(descricao)
            .valorEstimado(valorEstimado)
            .dataAbertura(Instant.now())
            .build());
    }
    
    public void atualizarStatus(StatusSinistro novoStatus, String motivo) {
        validateNotNull(novoStatus, "Novo status é obrigatório");
        validateStatusTransition(this.status, novoStatus);
        
        applyEvent(SinistroAtualizadoEvent.builder()
            .aggregateId(getId())
            .statusAnterior(this.status)
            .novoStatus(novoStatus)
            .motivo(motivo)
            .dataAtualizacao(Instant.now())
            .build());
    }
    
    // === EVENT HANDLERS (PRIVADOS) ===
    @EventSourcingHandler
    private void on(SinistroCriadoEvent event) {
        this.protocolo = event.getProtocolo();
        this.cpfSegurado = event.getCpfSegurado();
        this.descricao = event.getDescricao();
        this.valorEstimado = event.getValorEstimado();
        this.dataAbertura = event.getDataAbertura();
        this.status = StatusSinistro.ABERTO;
        
        log.debug("Sinistro criado: {}", this.protocolo);
    }
    
    @EventSourcingHandler
    private void on(SinistroAtualizadoEvent event) {
        this.status = event.getNovoStatus();
        
        log.debug("Status do sinistro {} atualizado para: {}", 
                 this.protocolo, this.status);
    }
    
    // === VALIDAÇÕES PRIVADAS ===
    private void validateStatusTransition(StatusSinistro atual, StatusSinistro novo) {
        if (!StatusSinistro.isValidTransition(atual, novo)) {
            throw new BusinessRuleViolationException(
                String.format("Transição inválida de %s para %s", atual, novo)
            );
        }
    }
    
    private void validateNotNull(Object value, String message) {
        if (value == null) {
            throw new IllegalArgumentException(message);
        }
    }
    
    // === REGRAS DE NEGÓCIO ===
    private void registerBusinessRules() {
        businessRules.add(new ValorEstimadoPositivoRule());
        businessRules.add(new CpfValidoRule());
        businessRules.add(new ProtocoloUnicoRule());
    }
    
    // === SNAPSHOT SUPPORT ===
    @Override
    public Object createSnapshot() {
        return SinistroSnapshot.builder()
            .protocolo(protocolo)
            .status(status)
            .cpfSegurado(cpfSegurado)
            .descricao(descricao)
            .valorEstimado(valorEstimado)
            .dataAbertura(dataAbertura)
            .build();
    }
    
    @Override
    public void restoreFromSnapshot(Object snapshotData) {
        if (snapshotData instanceof SinistroSnapshot snapshot) {
            this.protocolo = snapshot.getProtocolo();
            this.status = snapshot.getStatus();
            this.cpfSegurado = snapshot.getCpfSegurado();
            this.descricao = snapshot.getDescricao();
            this.valorEstimado = snapshot.getValorEstimado();
            this.dataAbertura = snapshot.getDataAbertura();
        }
    }
    
    // === GETTERS (SOMENTE LEITURA) ===
    public String getProtocolo() { return protocolo; }
    public StatusSinistro getStatus() { return status; }
    public String getCpfSegurado() { return cpfSegurado; }
    public String getDescricao() { return descricao; }
    public BigDecimal getValorEstimado() { return valorEstimado; }
    public Instant getDataAbertura() { return dataAbertura; }
}
```

### **🎯 Padrão para Command Handlers**

#### **Template Base para Command Handlers:**
```java
@Component
@Slf4j
@Transactional
public class CriarSinistroCommandHandler implements CommandHandler<CriarSinistroCommand> {
    
    private final AggregateRepository<SinistroAggregate> repository;
    private final CommandValidator<CriarSinistroCommand> validator;
    private final DebugTraceService traceService;
    
    public CriarSinistroCommandHandler(
            AggregateRepository<SinistroAggregate> repository,
            CommandValidator<CriarSinistroCommand> validator,
            DebugTraceService traceService) {
        this.repository = repository;
        this.validator = validator;
        this.traceService = traceService;
    }
    
    @Override
    public CommandResult handle(CriarSinistroCommand command) {
        return traceService.traceOperation("criar-sinistro-command", () -> {
            
            // 1. Validação do comando
            ValidationResult validationResult = validator.validate(command);
            if (validationResult.isInvalid()) {
                log.warn("Comando inválido: {}", validationResult.getErrorMessages());
                return CommandResult.failure(
                    "Dados inválidos: " + String.join(", ", validationResult.getErrorMessages())
                );
            }
            
            try {
                // 2. Criar novo agregado
                SinistroAggregate sinistro = new SinistroAggregate();
                
                // 3. Executar comando de negócio
                sinistro.criar(
                    command.getProtocolo(),
                    command.getCpfSegurado(),
                    command.getDescricao(),
                    command.getValorEstimado()
                );
                
                // 4. Persistir agregado
                repository.save(sinistro);
                
                // 5. Retornar resultado de sucesso
                log.info("Sinistro criado com sucesso: {}", command.getProtocolo());
                
                return CommandResult.success(Map.of(
                    "id", sinistro.getId(),
                    "protocolo", sinistro.getProtocolo(),
                    "status", sinistro.getStatus().toString()
                ));
                
            } catch (BusinessRuleViolationException e) {
                log.warn("Violação de regra de negócio: {}", e.getMessage());
                return CommandResult.failure("Regra de negócio violada: " + e.getMessage());
                
            } catch (Exception e) {
                log.error("Erro inesperado ao processar comando: {}", e.getMessage(), e);
                return CommandResult.failure("Erro interno do sistema");
            }
        });
    }
    
    @Override
    public Class<CriarSinistroCommand> getCommandType() {
        return CriarSinistroCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Timeout específico para este comando
    }
}
```

### **📊 Padrão para Projection Handlers**

#### **Template Base para Projection Handlers:**
```java
@Component
@Slf4j
public class SinistroProjectionHandler extends AbstractProjectionHandler<SinistroEvent> {
    
    private final SinistroQueryRepository queryRepository;
    private final ProjectionTrackerRepository trackerRepository;
    
    public SinistroProjectionHandler(
            SinistroQueryRepository queryRepository,
            ProjectionTrackerRepository trackerRepository) {
        super("SinistroProjection");
        this.queryRepository = queryRepository;
        this.trackerRepository = trackerRepository;
    }
    
    @Override
    protected void doHandle(SinistroEvent event) {
        switch (event.getEventType()) {
            case "SinistroCriadoEvent" -> handleSinistroCriado((SinistroCriadoEvent) event);
            case "SinistroAtualizadoEvent" -> handleSinistroAtualizado((SinistroAtualizadoEvent) event);
            case "SinistroCanceladoEvent" -> handleSinistroCancelado((SinistroCanceladoEvent) event);
            default -> log.warn("Tipo de evento não suportado: {}", event.getEventType());
        }
    }
    
    private void handleSinistroCriado(SinistroCriadoEvent event) {
        log.debug("Processando criação de sinistro: {}", event.getProtocolo());
        
        SinistroQueryModel projecao = new SinistroQueryModel();
        projecao.setId(UUID.fromString(event.getAggregateId()));
        projecao.setProtocolo(event.getProtocolo());
        projecao.setCpfSegurado(event.getCpfSegurado());
        projecao.setDescricao(event.getDescricao());
        projecao.setValorEstimado(event.getValorEstimado());
        projecao.setDataAbertura(event.getDataAbertura());
        projecao.setStatus("ABERTO");
        projecao.setLastEventId(event.getEventId());
        
        queryRepository.save(projecao);
        
        log.info("Projeção criada para sinistro: {}", event.getProtocolo());
    }
    
    private void handleSinistroAtualizado(SinistroAtualizadoEvent event) {
        log.debug("Processando atualização de sinistro: {}", event.getAggregateId());
        
        Optional<SinistroQueryModel> projecaoOpt = 
            queryRepository.findById(UUID.fromString(event.getAggregateId()));
            
        if (projecaoOpt.isPresent()) {
            SinistroQueryModel projecao = projecaoOpt.get();
            
            // Verificar se evento já foi processado (idempotência)
            if (event.getEventId() <= projecao.getLastEventId()) {
                log.debug("Evento já processado, ignorando: {}", event.getEventId());
                return;
            }
            
            // Atualizar projeção
            if (event.getNovoStatus() != null) {
                projecao.setStatus(event.getNovoStatus().toString());
            }
            if (event.getNovaDescricao() != null) {
                projecao.setDescricao(event.getNovaDescricao());
            }
            
            projecao.setLastEventId(event.getEventId());
            projecao.setUpdatedAt(Instant.now());
            
            queryRepository.save(projecao);
            
            log.info("Projeção atualizada para sinistro: {}", event.getAggregateId());
            
        } else {
            log.warn("Projeção não encontrada para agregado: {}", event.getAggregateId());
        }
    }
    
    private void handleSinistroCancelado(SinistroCanceladoEvent event) {
        // Implementação similar...
    }
    
    @Override
    public Class<SinistroEvent> getEventType() {
        return SinistroEvent.class;
    }
    
    @Override
    public boolean supports(SinistroEvent event) {
        return event.getAggregateId() != null && 
               event.getEventId() != null;
    }
    
    @Override
    public int getOrder() {
        return 100; // Ordem de processamento
    }
    
    @Override
    public boolean isRetryable() {
        return true;
    }
    
    @Override
    public int getMaxRetries() {
        return 3;
    }
}
```

---

## 🔧 **CONFIGURAÇÃO DE FERRAMENTAS DE QUALIDADE**

### **📊 Checkstyle Configuration**

#### **checkstyle.xml:**
```xml
<?xml version="1.0"?>
<!DOCTYPE module PUBLIC
    "-//Checkstyle//DTD Checkstyle Configuration 1.3//EN"
    "https://checkstyle.org/dtds/configuration_1_3.dtd">

<module name="Checker">
    <property name="charset" value="UTF-8"/>
    <property name="severity" value="warning"/>
    <property name="fileExtensions" value="java, properties, xml"/>
    
    <!-- Verificações de arquivo -->
    <module name="FileTabCharacter">
        <property name="eachLine" value="true"/>
    </module>
    
    <module name="LineLength">
        <property name="max" value="120"/>
        <property name="ignorePattern" value="^package.*|^import.*|a href|href|http://|https://|ftp://"/>
    </module>
    
    <module name="TreeWalker">
        <!-- Imports -->
        <module name="AvoidStarImport"/>
        <module name="IllegalImport"/>
        <module name="RedundantImport"/>
        <module name="UnusedImports"/>
        <module name="ImportOrder">
            <property name="groups" value="/^java\./,javax,org,com"/>
            <property name="ordered" value="true"/>
            <property name="separated" value="true"/>
        </module>
        
        <!-- Nomenclatura -->
        <module name="ConstantName"/>
        <module name="LocalFinalVariableName"/>
        <module name="LocalVariableName"/>
        <module name="MemberName"/>
        <module name="MethodName"/>
        <module name="PackageName"/>
        <module name="ParameterName"/>
        <module name="StaticVariableName"/>
        <module name="TypeName"/>
        
        <!-- Tamanho -->
        <module name="MethodLength">
            <property name="max" value="50"/>
        </module>
        <module name="ParameterNumber">
            <property name="max" value="7"/>
        </module>
        
        <!-- Espaçamento -->
        <module name="GenericWhitespace"/>
        <module name="MethodParamPad"/>
        <module name="NoWhitespaceAfter"/>
        <module name="NoWhitespaceBefore"/>
        <module name="OperatorWrap"/>
        <module name="ParenPad"/>
        <module name="TypecastParenPad"/>
        <module name="WhitespaceAfter"/>
        <module name="WhitespaceAround"/>
        
        <!-- Modificadores -->
        <module name="ModifierOrder"/>
        <module name="RedundantModifier"/>
        
        <!-- Blocos -->
        <module name="AvoidNestedBlocks"/>
        <module name="EmptyBlock"/>
        <module name="LeftCurly"/>
        <module name="NeedBraces"/>
        <module name="RightCurly"/>
        
        <!-- Problemas comuns -->
        <module name="EmptyStatement"/>
        <module name="EqualsHashCode"/>
        <module name="HiddenField">
            <property name="ignoreConstructorParameter" value="true"/>
            <property name="ignoreSetter" value="true"/>
        </module>
        <module name="IllegalInstantiation"/>
        <module name="InnerAssignment"/>
        <module name="MissingSwitchDefault"/>
        <module name="SimplifyBooleanExpression"/>
        <module name="SimplifyBooleanReturn"/>
        
        <!-- Complexidade -->
        <module name="CyclomaticComplexity">
            <property name="max" value="10"/>
        </module>
        
        <!-- Annotations -->
        <module name="AnnotationUseStyle"/>
        <module name="MissingDeprecated"/>
        <module name="MissingOverride"/>
    </module>
</module>
```

### **🔍 SpotBugs Configuration**

#### **spotbugs-exclude.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<FindBugsFilter>
    <!-- Excluir classes geradas -->
    <Match>
        <Class name="~.*\.Q.*"/>
    </Match>
    
    <!-- Excluir DTOs simples -->
    <Match>
        <Class name="~.*\.dto\..*"/>
        <Bug pattern="EI_EXPOSE_REP,EI_EXPOSE_REP2"/>
    </Match>
    
    <!-- Excluir configurações Spring -->
    <Match>
        <Class name="~.*Configuration"/>
        <Bug pattern="UWF_UNWRITTEN_FIELD"/>
    </Match>
    
    <!-- Excluir testes -->
    <Match>
        <Class name="~.*Test"/>
    </Match>
    
    <!-- Permitir serialVersionUID ausente em eventos -->
    <Match>
        <Class name="~.*Event"/>
        <Bug pattern="SE_NO_SERIALVERSIONID"/>
    </Match>
</FindBugsFilter>
```

### **📊 PMD Configuration**

#### **pmd-ruleset.xml:**
```xml
<?xml version="1.0"?>
<ruleset name="Arquitetura Híbrida PMD Rules"
         xmlns="http://pmd.sourceforge.net/ruleset/2.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://pmd.sourceforge.net/ruleset/2.0.0 
                             https://pmd.sourceforge.io/ruleset_2_0_0.xsd">
    
    <description>PMD Rules for Arquitetura Híbrida Project</description>
    
    <!-- Best Practices -->
    <rule ref="category/java/bestpractices.xml">
        <exclude name="GuardLogStatement"/>
        <exclude name="JUnitTestsShouldIncludeAssert"/>
    </rule>
    
    <!-- Code Style -->
    <rule ref="category/java/codestyle.xml">
        <exclude name="AtLeastOneConstructor"/>
        <exclude name="OnlyOneReturn"/>
        <exclude name="TooManyStaticImports"/>
        <exclude name="CommentDefaultAccessModifier"/>
    </rule>
    
    <!-- Design -->
    <rule ref="category/java/design.xml">
        <exclude name="LawOfDemeter"/>
        <exclude name="LoosePackageCoupling"/>
    </rule>
    
    <!-- Error Prone -->
    <rule ref="category/java/errorprone.xml">
        <exclude name="BeanMembersShouldSerialize"/>
        <exclude name="DataflowAnomalyAnalysis"/>
    </rule>
    
    <!-- Performance -->
    <rule ref="category/java/performance.xml"/>
    
    <!-- Security -->
    <rule ref="category/java/security.xml"/>
    
    <!-- Customizações específicas -->
    <rule ref="category/java/design.xml/TooManyMethods">
        <properties>
            <property name="maxmethods" value="15"/>
        </properties>
    </rule>
    
    <rule ref="category/java/design.xml/CyclomaticComplexity">
        <properties>
            <property name="cycloReportLevel" value="10"/>
        </properties>
    </rule>
</ruleset>
```

---

## 🎯 **PADRÕES DE DOCUMENTAÇÃO**

### **📝 JavaDoc Standards**

#### **Template para Classes:**
```java
/**
 * Agregado responsável por gerenciar o ciclo de vida de um sinistro.
 * 
 * <p>Este agregado implementa as regras de negócio relacionadas a:
 * <ul>
 *   <li>Criação de sinistros</li>
 *   <li>Atualização de status</li>
 *   <li>Validações de negócio</li>
 *   <li>Geração de eventos de domínio</li>
 * </ul>
 * 
 * <p><strong>Invariantes:</strong>
 * <ul>
 *   <li>Protocolo deve ser único</li>
 *   <li>Valor estimado deve ser positivo</li>
 *   <li>CPF do segurado deve ser válido</li>
 * </ul>
 * 
 * <p><strong>Eventos Gerados:</strong>
 * <ul>
 *   <li>{@link SinistroCriadoEvent} - Quando sinistro é criado</li>
 *   <li>{@link SinistroAtualizadoEvent} - Quando sinistro é atualizado</li>
 *   <li>{@link SinistroCanceladoEvent} - Quando sinistro é cancelado</li>
 * </ul>
 * 
 * @author Equipe Arquitetura
 * @version 1.0
 * @since 1.0
 * 
 * @see SinistroEvent
 * @see StatusSinistro
 */
@Slf4j
public class SinistroAggregate extends AggregateRoot {
    
    /**
     * Cria um novo sinistro com os dados fornecidos.
     * 
     * <p>Este método valida todos os dados de entrada e aplica as regras
     * de negócio antes de gerar o evento {@link SinistroCriadoEvent}.
     * 
     * <p><strong>Validações realizadas:</strong>
     * <ul>
     *   <li>Protocolo não pode ser nulo ou vazio</li>
     *   <li>CPF deve ter formato válido</li>
     *   <li>Valor estimado deve ser positivo</li>
     *   <li>Descrição deve ter pelo menos 10 caracteres</li>
     * </ul>
     * 
     * @param protocolo Protocolo único do sinistro (formato: SIN-YYYY-NNNNNN)
     * @param cpfSegurado CPF do segurado (apenas números)
     * @param descricao Descrição detalhada do sinistro
     * @param valorEstimado Valor estimado do sinistro (deve ser > 0)
     * 
     * @throws IllegalArgumentException se algum parâmetro for inválido
     * @throws BusinessRuleViolationException se alguma regra de negócio for violada
     * 
     * @since 1.0
     */
    public void criar(String protocolo, String cpfSegurado, 
                     String descricao, BigDecimal valorEstimado) {
        // Implementação...
    }
}
```

#### **Template para Métodos:**
```java
/**
 * Processa um comando de criação de sinistro.
 * 
 * <p>Este método executa o fluxo completo de processamento:
 * <ol>
 *   <li>Valida o comando usando {@link CommandValidator}</li>
 *   <li>Cria uma nova instância do agregado</li>
 *   <li>Executa o método de negócio no agregado</li>
 *   <li>Persiste o agregado no repositório</li>
 *   <li>Retorna o resultado da operação</li>
 * </ol>
 * 
 * @param command Comando contendo os dados para criação do sinistro
 * @return {@link CommandResult} com o resultado da operação
 * 
 * @throws CommandValidationException se o comando for inválido
 * @throws EventStoreException se houver erro na persistência
 * 
 * @see CriarSinistroCommand
 * @see SinistroAggregate#criar(String, String, String, BigDecimal)
 */
@Override
public CommandResult handle(CriarSinistroCommand command) {
    // Implementação...
}
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Google Java Style Guide](https://google.github.io/styleguide/javaguide.html)
- [Oracle Java Code Conventions](https://www.oracle.com/java/technologies/javase/codeconventions-contents.html)
- [Checkstyle Documentation](https://checkstyle.sourceforge.io/)
- [PMD Rules Reference](https://pmd.github.io/pmd-6.55.0/pmd_rules_java.html)

### **📖 Próximas Partes:**
- **Parte 2**: Git Workflow e Versionamento
- **Parte 3**: Code Review e Qualidade
- **Parte 4**: CI/CD e Automação
- **Parte 5**: Documentação e Knowledge Sharing

---

**📝 Parte 1 de 5 - Padrões de Código e Convenções**  
**⏱️ Tempo estimado**: 45 minutos  
**🎯 Próximo**: [Parte 2 - Git Workflow e Versionamento](./12-praticas-desenvolvimento-parte-2.md)