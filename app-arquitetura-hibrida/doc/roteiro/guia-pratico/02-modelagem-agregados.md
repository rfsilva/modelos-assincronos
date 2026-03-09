# 🧩 ETAPA 02: MODELAGEM DE AGREGADOS
## Implementação de Agregados seguindo DDD e Event Sourcing

### 🎯 **OBJETIVO DA ETAPA**

Implementar agregados de domínio seguindo os princípios de Domain-Driven Design, Event Sourcing e os padrões estabelecidos na arquitetura híbrida, garantindo consistência, performance e manutenibilidade.

**⏱️ Duração Estimada:** 3-6 horas  
**👥 Participantes:** Desenvolvedor + Tech Lead  
**📋 Pré-requisitos:** Etapa 01 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **🏗️ 1. ESTRUTURA BASE DO AGREGADO**

#### **📦 Organização de Pacotes:**
- [ ] **Pacote do domínio** criado: `com.seguradora.hibrida.[dominio]`
- [ ] **Subpacote aggregate** criado: `com.seguradora.hibrida.[dominio].aggregate`
- [ ] **Subpacote events** criado: `com.seguradora.hibrida.[dominio].events`
- [ ] **Subpacote commands** criado: `com.seguradora.hibrida.[dominio].commands`
- [ ] **Subpacote exceptions** criado: `com.seguradora.hibrida.[dominio].exceptions`

#### **🧩 Classe Base do Agregado:**
```java
// TEMPLATE OBRIGATÓRIO
@Entity
@Table(name = "[dominio]_aggregate")
public class [Dominio]Aggregate extends AggregateRoot {
    
    // ========== CAMPOS DE IDENTIDADE ==========
    @Id
    private String id;
    
    // ========== CAMPOS DE NEGÓCIO ==========
    // Campos essenciais do agregado
    
    // ========== CAMPOS DE CONTROLE ==========
    private Instant createdAt;
    private Instant updatedAt;
    private String createdBy;
    private String lastModifiedBy;
    
    // ========== CONSTRUTORES ==========
    protected [Dominio]Aggregate() {
        // Construtor JPA
    }
    
    // ========== MÉTODOS DE NEGÓCIO ==========
    // Operações que geram eventos
    
    // ========== EVENT HANDLERS ==========
    // Métodos que aplicam eventos ao estado
    
    // ========== BUSINESS RULES ==========
    // Validações e invariantes
    
    // ========== SNAPSHOT SUPPORT ==========
    // Suporte a snapshots para performance
}
```

#### **✅ Validações da Estrutura:**
- [ ] **Herda de AggregateRoot** corretamente
- [ ] **Anotações JPA** configuradas adequadamente
- [ ] **Construtor protegido** para JPA presente
- [ ] **Campos de auditoria** implementados
- [ ] **Métodos equals/hashCode** baseados no ID

---

### **⚡ 2. IMPLEMENTAÇÃO DE COMANDOS**

#### **📝 Estrutura de Comandos:**
```java
// TEMPLATE DE COMANDO
@Data
@Builder
@AllArgsConstructor
public class [Acao][Dominio]Command implements Command {
    
    // ========== IDENTIFICAÇÃO ==========
    @Builder.Default
    private final UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private final Instant timestamp = Instant.now();
    
    private final UUID correlationId;
    private final String userId;
    
    // ========== DADOS DO COMANDO ==========
    @NotNull(message = "ID é obrigatório")
    private final String aggregateId;
    
    // Outros campos necessários para a operação
    
    // ========== VALIDAÇÕES ==========
    @AssertTrue(message = "Dados do comando devem ser válidos")
    public boolean isValid() {
        // Validações específicas do comando
        return true;
    }
}
```

#### **🎯 Implementação de Command Handlers:**
```java
// TEMPLATE DE COMMAND HANDLER
@Component
@Slf4j
public class [Acao][Dominio]CommandHandler implements CommandHandler<[Acao][Dominio]Command> {
    
    private final AggregateRepository<[Dominio]Aggregate> repository;
    
    @Override
    public CommandResult handle([Acao][Dominio]Command command) {
        try {
            // 1. Validar comando
            validateCommand(command);
            
            // 2. Carregar ou criar agregado
            [Dominio]Aggregate aggregate = loadOrCreateAggregate(command);
            
            // 3. Executar operação de negócio
            aggregate.[operacao](/* parâmetros */);
            
            // 4. Salvar agregado (persiste eventos)
            repository.save(aggregate);
            
            // 5. Retornar sucesso
            return CommandResult.success()
                .withCorrelationId(command.getCorrelationId())
                .withMetadata("aggregateId", aggregate.getId());
                
        } catch (Exception e) {
            log.error("Erro ao processar comando: {}", command, e);
            return CommandResult.failure(e);
        }
    }
    
    @Override
    public Class<[Acao][Dominio]Command> getCommandType() {
        return [Acao][Dominio]Command.class;
    }
    
    private void validateCommand([Acao][Dominio]Command command) {
        // Validações específicas
    }
    
    private [Dominio]Aggregate loadOrCreateAggregate([Acao][Dominio]Command command) {
        // Lógica de carregamento/criação
    }
}
```

#### **✅ Checklist de Comandos:**
- [ ] **Todos os comandos** identificados na análise implementados
- [ ] **Validações de entrada** implementadas
- [ ] **Command handlers** seguindo padrão estabelecido
- [ ] **Tratamento de erros** adequado
- [ ] **Logging** estruturado implementado

---

### **📡 3. DEFINIÇÃO DE EVENTOS**

#### **🔄 Estrutura de Eventos:**
```java
// TEMPLATE DE EVENTO
@Data
@EqualsAndHashCode(callSuper = true)
@JsonTypeName("[Dominio][Acao]Event")
public class [Dominio][Acao]Event extends DomainEvent {
    
    // ========== DADOS DO EVENTO ==========
    private final String [campo1];
    private final String [campo2];
    // Outros campos relevantes (IMUTÁVEIS)
    
    // ========== CONSTRUTORES ==========
    public [Dominio][Acao]Event(String aggregateId, long version, 
                                String [campo1], String [campo2]) {
        super(aggregateId, version);
        this.[campo1] = [campo1];
        this.[campo2] = [campo2];
    }
    
    // ========== FACTORY METHODS ==========
    public static [Dominio][Acao]Event create(String aggregateId, long version,
                                              String [campo1], String [campo2]) {
        return new [Dominio][Acao]Event(aggregateId, version, [campo1], [campo2]);
    }
    
    @Override
    public String toString() {
        return String.format("[Dominio][Acao]Event{aggregateId='%s', version=%d, [campo1]='%s'}",
                           getAggregateId(), getVersion(), [campo1]);
    }
}
```

#### **🎯 Event Handlers no Agregado:**
```java
// DENTRO DO AGREGADO
public void on([Dominio][Acao]Event event) {
    // Aplicar mudanças de estado baseadas no evento
    this.[campo] = event.get[Campo]();
    this.updatedAt = event.getTimestamp();
    this.version = event.getVersion();
    
    // Log para auditoria
    log.debug("Evento aplicado: {} no agregado: {}", event, this.id);
}
```

#### **✅ Checklist de Eventos:**
- [ ] **Todos os eventos** identificados na análise implementados
- [ ] **Campos imutáveis** e bem definidos
- [ ] **Factory methods** para criação
- [ ] **Event handlers** no agregado implementados
- [ ] **Serialização JSON** funcionando corretamente

---

### **🛡️ 4. BUSINESS RULES E VALIDAÇÕES**

#### **📋 Implementação de Business Rules:**
```java
// TEMPLATE DE BUSINESS RULE
@Component
public class [Nome]BusinessRule implements BusinessRule {
    
    @Override
    public boolean isValid(AggregateRoot aggregate) {
        if (!(aggregate instanceof [Dominio]Aggregate)) {
            return true; // Não se aplica a este tipo
        }
        
        [Dominio]Aggregate [dominio] = ([Dominio]Aggregate) aggregate;
        
        // Implementar lógica de validação
        return [condicao];
    }
    
    @Override
    public String getErrorMessage() {
        return "Mensagem de erro clara e específica";
    }
    
    @Override
    public String getRuleName() {
        return "[Nome]BusinessRule";
    }
    
    @Override
    public int getPriority() {
        return 100; // Ajustar conforme necessário
    }
    
    @Override
    public boolean appliesTo(Class<? extends AggregateRoot> aggregateType) {
        return [Dominio]Aggregate.class.equals(aggregateType);
    }
}
```

#### **🔒 Invariantes do Agregado:**
```java
// DENTRO DO AGREGADO
private void validateInvariants() {
    List<String> violations = new ArrayList<>();
    
    // Validar regras de negócio
    if ([condicao_invalida]) {
        violations.add("Mensagem de erro específica");
    }
    
    if (!violations.isEmpty()) {
        throw new BusinessRuleViolationException(
            "Violação de regras de negócio no agregado " + this.id,
            violations
        );
    }
}

// Chamar em todas as operações que modificam estado
public void [operacao](/* parâmetros */) {
    // Executar operação
    
    // Validar invariantes
    validateInvariants();
    
    // Aplicar evento
    applyEvent([Evento].create(/* parâmetros */));
}
```

#### **✅ Checklist de Validações:**
- [ ] **Business rules** implementadas como componentes
- [ ] **Invariantes** validadas em todas as operações
- [ ] **Mensagens de erro** claras e específicas
- [ ] **Prioridades** das regras definidas
- [ ] **Aplicabilidade** das regras configurada

---

### **📊 5. SUPORTE A SNAPSHOTS**

#### **💾 Implementação de Snapshots:**
```java
// DENTRO DO AGREGADO
@Override
public Object createSnapshot() {
    return [Dominio]Snapshot.builder()
        .id(this.id)
        .[campo1](this.[campo1])
        .[campo2](this.[campo2])
        .createdAt(this.createdAt)
        .updatedAt(this.updatedAt)
        .version(this.getVersion())
        .build();
}

@Override
public void restoreFromSnapshot(Object snapshotData) {
    if (!(snapshotData instanceof [Dominio]Snapshot)) {
        throw new IllegalArgumentException("Snapshot inválido para " + getClass().getSimpleName());
    }
    
    [Dominio]Snapshot snapshot = ([Dominio]Snapshot) snapshotData;
    
    this.id = snapshot.getId();
    this.[campo1] = snapshot.get[Campo1]();
    this.[campo2] = snapshot.get[Campo2]();
    this.createdAt = snapshot.getCreatedAt();
    this.updatedAt = snapshot.getUpdatedAt();
    this.setVersion(snapshot.getVersion());
    
    // Limpar eventos não commitados
    this.clearUncommittedEvents();
}
```

#### **📋 Classe de Snapshot:**
```java
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class [Dominio]Snapshot {
    private String id;
    private String [campo1];
    private String [campo2];
    private Instant createdAt;
    private Instant updatedAt;
    private long version;
    
    // Outros campos necessários para reconstruir o estado
}
```

#### **✅ Checklist de Snapshots:**
- [ ] **Método createSnapshot** implementado
- [ ] **Método restoreFromSnapshot** implementado
- [ ] **Classe de snapshot** criada
- [ ] **Todos os campos essenciais** incluídos no snapshot
- [ ] **Testes de snapshot** implementados

---

### **🔧 6. CONFIGURAÇÃO E INTEGRAÇÃO**

#### **📦 Repository Configuration:**
```java
@Configuration
public class [Dominio]AggregateConfiguration {
    
    @Bean
    public AggregateRepository<[Dominio]Aggregate> [dominio]AggregateRepository(
            EventStore eventStore,
            EventBus eventBus,
            SnapshotStore snapshotStore) {
        
        return new EventSourcingAggregateRepository<>(
            [Dominio]Aggregate.class,
            eventStore,
            eventBus,
            snapshotStore
        );
    }
    
    @Bean
    public [Dominio]Service [dominio]Service(
            AggregateRepository<[Dominio]Aggregate> repository) {
        return new [Dominio]Service(repository);
    }
}
```

#### **🎯 Service Layer (Opcional):**
```java
@Service
@Transactional
@Slf4j
public class [Dominio]Service {
    
    private final AggregateRepository<[Dominio]Aggregate> repository;
    
    public [Dominio]Service(AggregateRepository<[Dominio]Aggregate> repository) {
        this.repository = repository;
    }
    
    // Métodos de conveniência se necessário
    public Optional<[Dominio]Aggregate> findById(String id) {
        return repository.findById(id);
    }
    
    public boolean exists(String id) {
        return repository.exists(id);
    }
}
```

#### **✅ Checklist de Configuração:**
- [ ] **Repository bean** configurado
- [ ] **Service layer** implementado (se necessário)
- [ ] **Dependências** injetadas corretamente
- [ ] **Transações** configuradas adequadamente
- [ ] **Logs** estruturados implementados

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **🏗️ Estrutura e Padrões:**
- [ ] **Agregado herda** de AggregateRoot corretamente
- [ ] **Pacotes organizados** seguindo convenção
- [ ] **Naming conventions** respeitadas
- [ ] **Anotações JPA** configuradas adequadamente
- [ ] **Construtores** implementados corretamente

#### **⚡ Funcionalidade:**
- [ ] **Comandos** implementados e funcionais
- [ ] **Eventos** sendo gerados corretamente
- [ ] **Event handlers** aplicando mudanças de estado
- [ ] **Business rules** validando invariantes
- [ ] **Snapshots** funcionando (se implementados)

#### **🧪 Testes Básicos:**
- [ ] **Testes unitários** do agregado implementados
- [ ] **Testes de command handlers** funcionais
- [ ] **Testes de business rules** validados
- [ ] **Testes de eventos** verificados
- [ ] **Coverage mínimo** de 80% atingido

#### **📚 Documentação:**
- [ ] **Javadoc** nas classes principais
- [ ] **Comentários** em lógicas complexas
- [ ] **README** do domínio atualizado
- [ ] **Diagramas** atualizados se necessário

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Agregados Anêmicos:**
```java
// ❌ EVITAR: Agregado sem comportamento
public class [Dominio]Aggregate extends AggregateRoot {
    // Apenas getters e setters
    public void set[Campo](String valor) {
        this.[campo] = valor;
    }
}

// ✅ PREFERIR: Agregado com comportamento rico
public class [Dominio]Aggregate extends AggregateRoot {
    public void [operacaoDeNegocio](String parametro) {
        // Validações
        // Lógica de negócio
        // Aplicação de evento
        applyEvent([Evento].create(this.id, getNextVersion(), parametro));
    }
}
```

#### **🚫 Eventos Mutáveis:**
```java
// ❌ EVITAR: Eventos com setters
public class [Evento]Event extends DomainEvent {
    private String campo;
    
    public void setCampo(String campo) {
        this.campo = campo; // NUNCA!
    }
}

// ✅ PREFERIR: Eventos imutáveis
public class [Evento]Event extends DomainEvent {
    private final String campo;
    
    public [Evento]Event(String aggregateId, long version, String campo) {
        super(aggregateId, version);
        this.campo = campo;
    }
    
    public String getCampo() {
        return campo;
    }
}
```

#### **🚫 Validações Inconsistentes:**
```java
// ❌ EVITAR: Validações espalhadas
public void [operacao](String parametro) {
    if (parametro == null) throw new IllegalArgumentException();
    // Lógica sem outras validações
}

// ✅ PREFERIR: Validações centralizadas
public void [operacao](String parametro) {
    validateBusinessRules(); // Todas as invariantes
    // Lógica de negócio
    applyEvent(/* evento */);
}
```

### **✅ Boas Práticas:**

#### **🎯 Design do Agregado:**
- **Sempre** manter agregados pequenos e focados
- **Sempre** validar invariantes antes de aplicar eventos
- **Sempre** usar factory methods para eventos
- **Sempre** implementar toString() informativos

#### **⚡ Performance:**
- **Sempre** considerar snapshots para agregados grandes
- **Sempre** otimizar carregamento de eventos
- **Sempre** evitar N+1 queries
- **Sempre** monitorar performance das operações

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 03 - Implementação de Comandos](./03-implementacao-comandos.md)**
2. Integrar com Command Bus
3. Implementar validações avançadas
4. Configurar métricas e monitoramento

### **📋 Preparação para Próxima Etapa:**
- [ ] **Command Bus** estudado e compreendido
- [ ] **Padrões de validação** revisados
- [ ] **Exemplos existentes** analisados
- [ ] **Testes** do agregado passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Agregados](../08-agregados-README.md)**: Guia completo de implementação
- **[Event Sourcing](../02-event-sourcing-README.md)**: Fundamentos de ES
- **Código Existente**: `ExampleAggregate` como referência completa

### **🛠️ Ferramentas de Desenvolvimento:**
- **IDE**: Plugins para DDD e Event Sourcing
- **SonarQube**: Análise de qualidade de código
- **JaCoCo**: Coverage de testes
- **ArchUnit**: Validação de arquitetura

### **🧪 Testes de Referência:**
- **ExampleAggregateTest**: Testes unitários do agregado
- **ExampleCommandHandlerTest**: Testes de command handlers
- **BusinessRuleTest**: Testes de regras de negócio

---

**📋 Checklist Total:** 60+ itens de validação  
**⏱️ Tempo Médio:** 3-6 horas  
**🎯 Resultado:** Agregados implementados seguindo DDD e ES  
**✅ Próxima Etapa:** Implementação de Comandos