# 🏗️ AGREGADOS - PARTE 1: FUNDAMENTOS E CONCEITOS DDD
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Compreender os fundamentos dos Agregados no contexto de Domain-Driven Design (DDD) e Event Sourcing, sua importância na arquitetura e implementação no projeto.

---

## 🏛️ **CONCEITOS FUNDAMENTAIS DE AGREGADOS**

### **📋 O que são Agregados?**

**Definição DDD:**
Um **Agregado** é um cluster de objetos de domínio que podem ser tratados como uma única unidade para propósitos de mudança de dados. Um dos objetos serve como **Aggregate Root** (raiz do agregado).

**Analogia Simples:**
```
Pedido (Aggregate Root)
├── Itens do Pedido (Entidades)
├── Endereço de Entrega (Value Object)
├── Dados de Pagamento (Value Object)
└── Status do Pedido (Enum)

→ Todas as mudanças passam pela raiz (Pedido)
→ Garantia de consistência dentro do agregado
→ Transações respeitam limites do agregado
```

### **🎯 Características dos Agregados**

**Princípios Fundamentais:**
1. **Consistência**: Garantida dentro dos limites do agregado
2. **Encapsulamento**: Estado interno protegido
3. **Identidade**: Cada agregado tem ID único
4. **Transações**: Uma transação = um agregado
5. **Persistência**: Salvo/carregado como unidade

**No Contexto Event Sourcing:**
```
Agregado + Event Sourcing
├── Estado atual = Aplicação de todos os eventos
├── Mudanças = Novos eventos gerados
├── Persistência = Eventos no Event Store
└── Reconstrução = Replay dos eventos
```

---

## 🏗️ **ARQUITETURA DOS AGREGADOS NO PROJETO**

### **📐 Estrutura Geral**

```
Aggregate Architecture
├── AggregateRoot (Classe base abstrata)
├── DomainEvent (Eventos de domínio)
├── BusinessRule (Regras de negócio)
├── AggregateRepository (Repositório especializado)
└── EventSourcingHandler (Manipulação de eventos)
```

### **🎯 AggregateRoot - Classe Base**

**Localização**: `com.seguradora.hibrida.aggregate.AggregateRoot`

```java
public abstract class AggregateRoot {
    
    // === IDENTIFICAÇÃO ===
    protected String aggregateId;
    protected long version = 0;
    protected String aggregateType;
    
    // === CONTROLE DE EVENTOS ===
    private final List<DomainEvent> uncommittedEvents = new ArrayList<>();
    private final List<BusinessRule> businessRules = new ArrayList<>();
    
    // === METADADOS ===
    protected Instant createdAt;
    protected Instant updatedAt;
    protected Map<String, Object> metadata = new HashMap<>();
    
    /**
     * Construtor protegido - apenas subclasses podem instanciar.
     */
    protected AggregateRoot() {
        this.aggregateType = this.getClass().getSimpleName();
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }
    
    /**
     * Construtor com ID específico.
     */
    protected AggregateRoot(String aggregateId) {
        this();
        this.aggregateId = aggregateId;
    }
    
    // === MÉTODOS DE EVENTOS ===
    
    /**
     * Aplica um evento ao agregado e o adiciona à lista de não commitados.
     */
    protected void applyEvent(DomainEvent event) {
        // Configurar metadados do evento
        event.setAggregateId(this.aggregateId);
        event.setAggregateType(this.aggregateType);
        event.setVersion(this.version + 1);
        event.setTimestamp(Instant.now());
        
        // Aplicar mudança de estado
        applyEventToState(event);
        
        // Adicionar à lista de eventos não commitados
        uncommittedEvents.add(event);
        
        // Incrementar versão
        this.version++;
        this.updatedAt = Instant.now();
    }
    
    /**
     * Aplica evento ao estado interno (implementado pelas subclasses).
     */
    protected abstract void applyEventToState(DomainEvent event);
    
    /**
     * Carrega agregado a partir do histórico de eventos.
     */
    public void loadFromHistory(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            applyEventToState(event);
            this.version = event.getVersion();
        }
        
        // Limpar eventos não commitados após carregar histórico
        uncommittedEvents.clear();
    }
    
    /**
     * Marca eventos como commitados (após persistência).
     */
    public void markEventsAsCommitted() {
        uncommittedEvents.clear();
    }
    
    /**
     * Obtém eventos não commitados.
     */
    public List<DomainEvent> getUncommittedEvents() {
        return new ArrayList<>(uncommittedEvents);
    }
    
    /**
     * Verifica se há eventos não commitados.
     */
    public boolean hasUncommittedEvents() {
        return !uncommittedEvents.isEmpty();
    }
    
    // === REGRAS DE NEGÓCIO ===
    
    /**
     * Registra uma regra de negócio.
     */
    protected void registerBusinessRule(BusinessRule rule) {
        if (rule.appliesTo(this.getClass())) {
            businessRules.add(rule);
        }
    }
    
    /**
     * Valida todas as regras de negócio registradas.
     */
    protected void validateBusinessRules() {
        List<String> violations = new ArrayList<>();
        
        for (BusinessRule rule : businessRules) {
            if (!rule.isValid(this)) {
                violations.add(rule.getErrorMessage());
            }
        }
        
        if (!violations.isEmpty()) {
            throw new BusinessRuleViolationException(
                "Violações de regras de negócio encontradas", violations);
        }
    }
    
    // === SNAPSHOT SUPPORT ===
    
    /**
     * Cria snapshot do estado atual (implementado pelas subclasses).
     */
    public abstract Object createSnapshot();
    
    /**
     * Restaura estado a partir de snapshot.
     */
    public abstract void restoreFromSnapshot(Object snapshotData);
    
    /**
     * Carrega a partir de snapshot + eventos incrementais.
     */
    public void loadFromSnapshot(Object snapshotData, List<DomainEvent> incrementalEvents) {
        restoreFromSnapshot(snapshotData);
        loadFromHistory(incrementalEvents);
    }
    
    // === MÉTODOS UTILITÁRIOS ===
    
    /**
     * Verifica se o agregado foi modificado.
     */
    public boolean isModified() {
        return hasUncommittedEvents();
    }
    
    /**
     * Limpa estado do agregado.
     */
    protected void clearState() {
        // Implementação padrão - subclasses podem sobrescrever
        this.version = 0;
        this.uncommittedEvents.clear();
        this.businessRules.clear();
        this.updatedAt = Instant.now();
    }
    
    /**
     * Adiciona metadados ao agregado.
     */
    public void addMetadata(String key, Object value) {
        this.metadata.put(key, value);
    }
    
    /**
     * Obtém informações de debug.
     */
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> debug = new HashMap<>();
        debug.put("aggregateId", aggregateId);
        debug.put("aggregateType", aggregateType);
        debug.put("version", version);
        debug.put("uncommittedEvents", uncommittedEvents.size());
        debug.put("businessRules", businessRules.size());
        debug.put("createdAt", createdAt);
        debug.put("updatedAt", updatedAt);
        debug.put("metadata", metadata);
        return debug;
    }
    
    // === REFLECTION HELPER ===
    
    /**
     * Encontra método handler para um tipo de evento específico.
     */
    private Method findEventHandler(Class<?> aggregateClass, Class<? extends DomainEvent> eventClass) {
        String methodName = "on";
        
        try {
            return aggregateClass.getDeclaredMethod(methodName, eventClass);
        } catch (NoSuchMethodException e) {
            // Tentar na superclasse
            Class<?> superClass = aggregateClass.getSuperclass();
            if (superClass != null && !superClass.equals(AggregateRoot.class)) {
                return findEventHandler(superClass, eventClass);
            }
            
            throw new EventHandlerNotFoundException(
                "Handler não encontrado para evento: " + eventClass.getSimpleName() +
                " no agregado: " + aggregateClass.getSimpleName());
        }
    }
    
    // === GETTERS ===
    
    public String getAggregateId() { return aggregateId; }
    public long getVersion() { return version; }
    public String getAggregateType() { return aggregateType; }
    public Instant getCreatedAt() { return createdAt; }
    public Instant getUpdatedAt() { return updatedAt; }
    public Map<String, Object> getMetadata() { return new HashMap<>(metadata); }
    
    // === EQUALS & HASHCODE ===
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AggregateRoot that = (AggregateRoot) obj;
        return Objects.equals(aggregateId, that.aggregateId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(aggregateId);
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', version=%d, events=%d}", 
            getClass().getSimpleName(), aggregateId, version, uncommittedEvents.size());
    }
}
```

---

## 🎯 **IMPLEMENTAÇÃO DE AGREGADO CONCRETO**

### **📋 ExampleAggregate - Exemplo Didático**

**Localização**: `com.seguradora.hibrida.aggregate.example.ExampleAggregate`

```java
public class ExampleAggregate extends AggregateRoot {
    
    // === ESTADO DO AGREGADO ===
    private String name;
    private String description;
    private boolean active;
    private Instant activationTimestamp;
    private String errorMessage;
    
    // === CONSTRUTORES ===
    
    /**
     * Construtor padrão (para reconstrução a partir de eventos).
     */
    public ExampleAggregate() {
        super();
        registerBusinessRules();
    }
    
    /**
     * Construtor para criação de novo agregado.
     */
    public ExampleAggregate(String aggregateId) {
        super(aggregateId);
        registerBusinessRules();
    }
    
    // === MÉTODOS DE COMANDO (COMPORTAMENTOS) ===
    
    /**
     * Cria um novo exemplo.
     */
    public void create(String name, String description) {
        // Validar precondições
        if (this.name != null) {
            throw new IllegalStateException("Exemplo já foi criado");
        }
        
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        // Aplicar evento
        ExampleCreatedEvent event = ExampleCreatedEvent.create(
            this.aggregateId, name, description, Instant.now());
        
        applyEvent(event);
        
        // Validar regras de negócio após mudança
        validateBusinessRules();
    }
    
    /**
     * Atualiza o exemplo.
     */
    public void update(String newName, String newDescription) {
        // Validar precondições
        if (this.name == null) {
            throw new IllegalStateException("Exemplo deve ser criado antes de ser atualizado");
        }
        
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        // Verificar se há mudanças
        if (Objects.equals(this.name, newName) && 
            Objects.equals(this.description, newDescription)) {
            return; // Nenhuma mudança
        }
        
        // Aplicar evento
        ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
            this.aggregateId, this.version, newName, newDescription, Instant.now());
        
        applyEvent(event);
        
        // Validar regras de negócio
        validateBusinessRules();
    }
    
    /**
     * Ativa o exemplo.
     */
    public void activate() {
        if (this.name == null) {
            throw new IllegalStateException("Exemplo deve ser criado antes de ser ativado");
        }
        
        if (this.active) {
            return; // Já está ativo
        }
        
        // Aplicar evento
        ExampleActivatedEvent event = ExampleActivatedEvent.create(
            this.aggregateId, this.version, Instant.now());
        
        applyEvent(event);
        
        validateBusinessRules();
    }
    
    /**
     * Desativa o exemplo.
     */
    public void deactivate() {
        if (!this.active) {
            return; // Já está inativo
        }
        
        // Aplicar evento
        ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(
            this.aggregateId, this.version, Instant.now());
        
        applyEvent(event);
        
        validateBusinessRules();
    }
    
    // === EVENT HANDLERS (APLICAÇÃO DE ESTADO) ===
    
    /**
     * Aplica evento ao estado interno.
     */
    @Override
    protected void applyEventToState(DomainEvent event) {
        switch (event.getEventType()) {
            case "ExampleCreated" -> on((ExampleCreatedEvent) event);
            case "ExampleUpdated" -> on((ExampleUpdatedEvent) event);
            case "ExampleActivated" -> on((ExampleActivatedEvent) event);
            case "ExampleDeactivated" -> on((ExampleDeactivatedEvent) event);
            default -> throw new IllegalArgumentException(
                "Tipo de evento não suportado: " + event.getEventType());
        }
    }
    
    /**
     * Handler para ExampleCreatedEvent.
     */
    private void on(ExampleCreatedEvent event) {
        this.name = event.getName();
        this.description = event.getDescription();
        this.active = false; // Criado inativo por padrão
        this.activationTimestamp = null;
        this.errorMessage = null;
    }
    
    /**
     * Handler para ExampleUpdatedEvent.
     */
    private void on(ExampleUpdatedEvent event) {
        this.name = event.getNewName();
        this.description = event.getNewDescription();
    }
    
    /**
     * Handler para ExampleActivatedEvent.
     */
    private void on(ExampleActivatedEvent event) {
        this.active = true;
        this.activationTimestamp = event.getActivationTimestamp();
        this.errorMessage = null;
    }
    
    /**
     * Handler para ExampleDeactivatedEvent.
     */
    private void on(ExampleDeactivatedEvent event) {
        this.active = false;
        this.activationTimestamp = null;
    }
    
    // === REGRAS DE NEGÓCIO ===
    
    /**
     * Registra regras de negócio específicas.
     */
    private void registerBusinessRules() {
        // Regra: Nome deve ter pelo menos 3 caracteres
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                ExampleAggregate example = (ExampleAggregate) aggregate;
                return example.name == null || example.name.length() >= 3;
            }
            
            @Override
            public String getErrorMessage() {
                return "Nome deve ter pelo menos 3 caracteres";
            }
            
            @Override
            public String getRuleName() {
                return "MinimumNameLength";
            }
        });
        
        // Regra: Exemplo ativo deve ter descrição
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                ExampleAggregate example = (ExampleAggregate) aggregate;
                return !example.active || 
                       (example.description != null && !example.description.trim().isEmpty());
            }
            
            @Override
            public String getErrorMessage() {
                return "Exemplo ativo deve ter descrição";
            }
            
            @Override
            public String getRuleName() {
                return "ActiveExampleMustHaveDescription";
            }
        });
    }
    
    // === SNAPSHOT SUPPORT ===
    
    @Override
    public Object createSnapshot() {
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("aggregateId", aggregateId);
        snapshot.put("version", version);
        snapshot.put("name", name);
        snapshot.put("description", description);
        snapshot.put("active", active);
        snapshot.put("activationTimestamp", activationTimestamp);
        snapshot.put("errorMessage", errorMessage);
        snapshot.put("createdAt", createdAt);
        snapshot.put("updatedAt", updatedAt);
        snapshot.put("metadata", metadata);
        
        return snapshot;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    public void restoreFromSnapshot(Object snapshotData) {
        if (!(snapshotData instanceof Map)) {
            throw new IllegalArgumentException("Snapshot data deve ser um Map");
        }
        
        Map<String, Object> snapshot = (Map<String, Object>) snapshotData;
        
        this.aggregateId = (String) snapshot.get("aggregateId");
        this.version = ((Number) snapshot.get("version")).longValue();
        this.name = (String) snapshot.get("name");
        this.description = (String) snapshot.get("description");
        this.active = (Boolean) snapshot.get("active");
        this.activationTimestamp = (Instant) snapshot.get("activationTimestamp");
        this.errorMessage = (String) snapshot.get("errorMessage");
        this.createdAt = (Instant) snapshot.get("createdAt");
        this.updatedAt = (Instant) snapshot.get("updatedAt");
        
        Object metadataObj = snapshot.get("metadata");
        if (metadataObj instanceof Map) {
            this.metadata = new HashMap<>((Map<String, Object>) metadataObj);
        }
    }
    
    @Override
    protected void clearState() {
        super.clearState();
        this.name = null;
        this.description = null;
        this.active = false;
        this.activationTimestamp = null;
        this.errorMessage = null;
    }
    
    // === MÉTODOS DE CONSULTA (GETTERS) ===
    
    public String getName() { return name; }
    public String getDescription() { return description; }
    public boolean isActive() { return active; }
    public Instant getActivationTimestamp() { return activationTimestamp; }
    public String getErrorMessage() { return errorMessage; }
    
    // === MÉTODOS DE NEGÓCIO (QUERIES) ===
    
    /**
     * Verifica se foi criado recentemente (últimas 24h).
     */
    public boolean isRecentlyCreated() {
        if (createdAt == null) return false;
        
        Instant oneDayAgo = Instant.now().minus(1, ChronoUnit.DAYS);
        return createdAt.isAfter(oneDayAgo);
    }
    
    /**
     * Calcula idade em dias.
     */
    public long getAgeInDays() {
        if (createdAt == null) return 0;
        
        return ChronoUnit.DAYS.between(createdAt, Instant.now());
    }
    
    @Override
    public String toString() {
        return String.format("ExampleAggregate{id='%s', name='%s', active=%s, version=%d}", 
            aggregateId, name, active, version);
    }
}
```

---

## 🎯 **PADRÕES E BOAS PRÁTICAS**

### **✅ Padrões Recomendados**

**1. Estrutura de Métodos:**
```java
public class SinistroAggregate extends AggregateRoot {
    
    // === ESTADO (private fields) ===
    private String numeroSinistro;
    private SinistroStatus status;
    
    // === CONSTRUTORES ===
    public SinistroAggregate() { super(); }
    public SinistroAggregate(String id) { super(id); }
    
    // === COMANDOS (public methods) ===
    public void criar(...) { /* validar + aplicar evento */ }
    public void atualizar(...) { /* validar + aplicar evento */ }
    
    // === EVENT HANDLERS (private methods) ===
    private void on(SinistroCriadoEvent event) { /* atualizar estado */ }
    private void on(SinistroAtualizadoEvent event) { /* atualizar estado */ }
    
    // === QUERIES (public getters) ===
    public String getNumeroSinistro() { return numeroSinistro; }
    public boolean isAberto() { return status == SinistroStatus.ABERTO; }
}
```

**2. Validações e Precondições:**
```java
public void finalizar(BigDecimal valorFinal) {
    // ✅ Validar precondições
    if (status != SinistroStatus.EM_ANALISE) {
        throw new IllegalStateException("Sinistro deve estar em análise para ser finalizado");
    }
    
    if (valorFinal == null || valorFinal.compareTo(BigDecimal.ZERO) <= 0) {
        throw new IllegalArgumentException("Valor final deve ser positivo");
    }
    
    // ✅ Aplicar evento
    SinistroFinalizadoEvent event = new SinistroFinalizadoEvent(aggregateId, valorFinal);
    applyEvent(event);
    
    // ✅ Validar regras de negócio
    validateBusinessRules();
}
```

**3. Tratamento de Eventos:**
```java
@Override
protected void applyEventToState(DomainEvent event) {
    // ✅ Usar switch com pattern matching (Java 17+)
    switch (event.getEventType()) {
        case "SinistroCriado" -> on((SinistroCriadoEvent) event);
        case "SinistroAtualizado" -> on((SinistroAtualizadoEvent) event);
        case "SinistroFinalizado" -> on((SinistroFinalizadoEvent) event);
        default -> throw new IllegalArgumentException(
            "Evento não suportado: " + event.getEventType());
    }
}
```

### **⚠️ Armadilhas Comuns**

1. **Não validar precondições**: Sempre verificar estado antes de aplicar eventos
2. **Lógica nos event handlers**: Handlers devem apenas atualizar estado
3. **Não usar regras de negócio**: Implementar validações como BusinessRules
4. **Estado público**: Manter campos privados, expor via getters
5. **Não implementar snapshot**: Necessário para performance com muitos eventos

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Agregado Básico**

Crie um `SeguradoAggregate` que:

1. **Gerencie dados básicos** (nome, CPF, email)
2. **Controle status** (ativo/inativo)
3. **Mantenha endereços** (lista de endereços)
4. **Implemente regras de negócio** (CPF válido, email único)
5. **Suporte snapshot** para otimização

**Template:**
```java
public class SeguradoAggregate extends AggregateRoot {
    
    // Estado do agregado
    private String nome;
    private String cpf;
    private String email;
    private SeguradoStatus status;
    private List<Endereco> enderecos = new ArrayList<>();
    
    // Construtores
    public SeguradoAggregate() { super(); }
    public SeguradoAggregate(String id) { super(id); }
    
    // Comandos
    public void criar(String nome, String cpf, String email) {
        // Sua implementação aqui
    }
    
    public void adicionarEndereco(Endereco endereco) {
        // Sua implementação aqui
    }
    
    // Event handlers
    private void on(SeguradoCriadoEvent event) {
        // Sua implementação aqui
    }
    
    // Regras de negócio
    private void registerBusinessRules() {
        // Sua implementação aqui
    }
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.aggregate`
- **Exemplo**: `ExampleAggregate`
- **Base**: `AggregateRoot`
- **Eventos**: `com.seguradora.hibrida.aggregate.example`

### **📖 Leitura Complementar**
- [Eric Evans - Domain-Driven Design](https://domainlanguage.com/ddd/)
- [Vaughn Vernon - Implementing Domain-Driven Design](https://vaughnvernon.co/)
- [Martin Fowler - DDD Aggregate](https://martinfowler.com/bliki/DDD_Aggregate.html)

---

**📍 Próxima Parte**: [Agregados - Parte 2: Event Sourcing Handlers](./08-agregados-parte-2.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Fundamentos e conceitos DDD dos agregados  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Implementação de agregado básico