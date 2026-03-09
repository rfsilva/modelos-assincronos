# 🏗️ AGREGADOS - PARTE 2: EVENT SOURCING HANDLERS
## Roteiro Técnico para Analistas Java Junior

### 🎯 **OBJETIVO DESTA PARTE**
Dominar a implementação de Event Sourcing Handlers, repositórios especializados e padrões de persistência para agregados.

---

## 🔄 **EVENT SOURCING HANDLERS**

### **📋 Conceito e Responsabilidades**

**Event Sourcing Handler** é responsável por:
- ✅ **Reconstruir** agregados a partir de eventos
- ✅ **Persistir** novos eventos no Event Store
- ✅ **Gerenciar** snapshots para otimização
- ✅ **Coordenar** com Event Bus para publicação
- ✅ **Controlar** concorrência e versionamento

### **🎯 EventSourcingHandler - Implementação**

**Localização**: `com.seguradora.hibrida.aggregate.EventSourcingHandler`

```java
@Component
public class EventSourcingHandler {
    
    private static final Logger log = LoggerFactory.getLogger(EventSourcingHandler.class);
    
    private final EventStore eventStore;
    private final EventBus eventBus;
    private final SnapshotStore snapshotStore;
    private final AggregateMetrics metrics;
    
    public EventSourcingHandler(EventStore eventStore,
                              EventBus eventBus,
                              SnapshotStore snapshotStore,
                              AggregateMetrics metrics) {
        this.eventStore = eventStore;
        this.eventBus = eventBus;
        this.snapshotStore = snapshotStore;
        this.metrics = metrics;
    }
    
    /**
     * Salva um agregado persistindo seus eventos não commitados.
     */
    public <T extends AggregateRoot> void save(T aggregate) {
        Timer.Sample sample = metrics.startSaveTimer();
        
        try {
            List<DomainEvent> uncommittedEvents = aggregate.getUncommittedEvents();
            
            if (uncommittedEvents.isEmpty()) {
                log.debug("Nenhum evento para persistir: {}", aggregate.getAggregateId());
                return;
            }
            
            log.info("Salvando agregado: {} com {} eventos", 
                aggregate.getAggregateId(), uncommittedEvents.size());
            
            // Persistir eventos no Event Store
            eventStore.saveEvents(
                aggregate.getAggregateId(),
                uncommittedEvents,
                aggregate.getVersion() - uncommittedEvents.size()
            );
            
            // Marcar eventos como commitados
            aggregate.markEventsAsCommitted();
            
            // Publicar eventos no Event Bus
            publishEvents(uncommittedEvents);
            
            // Criar snapshot se necessário
            createSnapshotIfNeeded(aggregate);
            
            metrics.incrementSnapshotsUsed();
            
            log.info("Agregado salvo com sucesso: {}", aggregate.getAggregateId());
            
        } catch (ConcurrencyException e) {
            metrics.incrementErrors("concurrency");
            log.error("Erro de concorrência ao salvar agregado: {}", 
                aggregate.getAggregateId(), e);
            throw e;
            
        } catch (Exception e) {
            metrics.incrementErrors("save");
            log.error("Erro ao salvar agregado: {}", aggregate.getAggregateId(), e);
            throw new AggregateException(aggregate.getAggregateId(), 
                aggregate.getClass().getSimpleName(), 
                "Erro ao salvar agregado", e);
                
        } finally {
            sample.stop(metrics.getSaveTimer());
        }
    }
    
    /**
     * Carrega um agregado por ID.
     */
    public <T extends AggregateRoot> Optional<T> load(String aggregateId, Class<T> aggregateType) {
        Timer.Sample sample = metrics.startLoadTimer();
        
        try {
            log.debug("Carregando agregado: {} do tipo {}", aggregateId, aggregateType.getSimpleName());
            
            // Tentar carregar snapshot primeiro
            Optional<T> aggregateFromSnapshot = loadFromSnapshot(aggregateId, aggregateType);
            
            if (aggregateFromSnapshot.isPresent()) {
                T aggregate = aggregateFromSnapshot.get();
                
                // Carregar eventos incrementais desde o snapshot
                List<DomainEvent> incrementalEvents = eventStore.loadEvents(
                    aggregateId, aggregate.getVersion() + 1);
                
                if (!incrementalEvents.isEmpty()) {
                    log.debug("Aplicando {} eventos incrementais ao snapshot", 
                        incrementalEvents.size());
                    aggregate.loadFromHistory(incrementalEvents);
                }
                
                log.info("Agregado carregado via snapshot: {} (versão {})", 
                    aggregateId, aggregate.getVersion());
                
                return Optional.of(aggregate);
            }
            
            // Carregar todos os eventos se não há snapshot
            List<DomainEvent> events = eventStore.loadEvents(aggregateId);
            
            if (events.isEmpty()) {
                log.debug("Nenhum evento encontrado para agregado: {}", aggregateId);
                return Optional.empty();
            }
            
            // Reconstruir agregado a partir dos eventos
            T aggregate = reconstructFromEvents(events, aggregateType);
            
            log.info("Agregado reconstruído a partir de {} eventos: {} (versão {})", 
                events.size(), aggregateId, aggregate.getVersion());
            
            return Optional.of(aggregate);
            
        } catch (Exception e) {
            metrics.incrementErrors("load");
            log.error("Erro ao carregar agregado: {} do tipo {}", 
                aggregateId, aggregateType.getSimpleName(), e);
            throw new AggregateException(aggregateId, aggregateType.getSimpleName(), 
                "Erro ao carregar agregado", e);
                
        } finally {
            sample.stop(metrics.getLoadTimer());
        }
    }
    
    /**
     * Carrega agregado em uma versão específica.
     */
    public <T extends AggregateRoot> Optional<T> loadVersion(String aggregateId, 
                                                           Class<T> aggregateType, 
                                                           long version) {
        Timer.Sample sample = metrics.startReconstructionTimer();
        
        try {
            log.debug("Carregando agregado: {} versão {} do tipo {}", 
                aggregateId, version, aggregateType.getSimpleName());
            
            // Carregar eventos até a versão especificada
            List<DomainEvent> events = eventStore.loadEvents(aggregateId)
                .stream()
                .filter(event -> event.getVersion() <= version)
                .collect(Collectors.toList());
            
            if (events.isEmpty()) {
                return Optional.empty();
            }
            
            T aggregate = reconstructFromEvents(events, aggregateType);
            
            log.info("Agregado carregado na versão {}: {} (eventos: {})", 
                version, aggregateId, events.size());
            
            return Optional.of(aggregate);
            
        } catch (Exception e) {
            metrics.incrementErrors("load_version");
            log.error("Erro ao carregar agregado {} na versão {}", aggregateId, version, e);
            throw new AggregateException(aggregateId, aggregateType.getSimpleName(), 
                "Erro ao carregar versão específica", e);
                
        } finally {
            sample.stop(metrics.getReconstructionTimer());
        }
    }
    
    /**
     * Verifica se um agregado existe.
     */
    public boolean exists(String aggregateId) {
        try {
            return eventStore.aggregateExists(aggregateId);
        } catch (Exception e) {
            log.error("Erro ao verificar existência do agregado: {}", aggregateId, e);
            return false;
        }
    }
    
    /**
     * Obtém a versão atual de um agregado.
     */
    public long getCurrentVersion(String aggregateId) {
        try {
            return eventStore.getCurrentVersion(aggregateId);
        } catch (Exception e) {
            log.error("Erro ao obter versão atual do agregado: {}", aggregateId, e);
            return 0;
        }
    }
    
    /**
     * Remove todos os eventos de um agregado (CUIDADO!).
     */
    public boolean delete(String aggregateId) {
        try {
            log.warn("DELETANDO agregado: {} - Esta operação é irreversível!", aggregateId);
            
            boolean deleted = eventStore.delete(aggregateId);
            
            if (deleted) {
                // Remover snapshot também
                snapshotStore.deleteSnapshot(aggregateId);
                log.warn("Agregado deletado: {}", aggregateId);
            }
            
            return deleted;
            
        } catch (Exception e) {
            metrics.incrementErrors("delete");
            log.error("Erro ao deletar agregado: {}", aggregateId, e);
            return false;
        }
    }
    
    // === MÉTODOS PRIVADOS ===
    
    /**
     * Reconstrói agregado a partir de lista de eventos.
     */
    private <T extends AggregateRoot> T reconstructFromEvents(List<DomainEvent> events, 
                                                            Class<T> aggregateType) {
        Timer.Sample sample = metrics.startReconstructionTimer();
        
        try {
            if (events.isEmpty()) {
                throw new IllegalArgumentException("Lista de eventos não pode estar vazia");
            }
            
            // Criar instância do agregado
            T aggregate = createAggregateInstance(aggregateType);
            
            // Aplicar eventos em ordem
            aggregate.loadFromHistory(events);
            
            return aggregate;
            
        } catch (Exception e) {
            log.error("Erro ao reconstruir agregado do tipo: {}", aggregateType.getSimpleName(), e);
            throw new AggregateException(null, aggregateType.getSimpleName(), 
                "Erro na reconstrução a partir de eventos", e);
                
        } finally {
            sample.stop(metrics.getReconstructionTimer());
        }
    }
    
    /**
     * Cria instância do agregado via reflexão.
     */
    private <T extends AggregateRoot> T createAggregateInstance(Class<T> aggregateType) {
        try {
            Constructor<T> constructor = aggregateType.getDeclaredConstructor();
            constructor.setAccessible(true);
            return constructor.newInstance();
            
        } catch (Exception e) {
            throw new AggregateException(null, aggregateType.getSimpleName(), 
                "Erro ao criar instância do agregado", e);
        }
    }
    
    /**
     * Carrega agregado a partir de snapshot.
     */
    private <T extends AggregateRoot> Optional<T> loadFromSnapshot(String aggregateId, 
                                                                 Class<T> aggregateType) {
        try {
            Optional<Object> snapshotData = snapshotStore.loadSnapshot(aggregateId);
            
            if (snapshotData.isEmpty()) {
                return Optional.empty();
            }
            
            T aggregate = createAggregateInstance(aggregateType);
            aggregate.restoreFromSnapshot(snapshotData.get());
            
            log.debug("Snapshot carregado para agregado: {} (versão {})", 
                aggregateId, aggregate.getVersion());
            
            return Optional.of(aggregate);
            
        } catch (Exception e) {
            log.warn("Erro ao carregar snapshot para agregado: {} - Usando eventos completos", 
                aggregateId, e);
            return Optional.empty();
        }
    }
    
    /**
     * Publica eventos no Event Bus.
     */
    private void publishEvents(List<DomainEvent> events) {
        try {
            for (DomainEvent event : events) {
                eventBus.publish(event);
            }
            
            log.debug("Publicados {} eventos no Event Bus", events.size());
            
        } catch (Exception e) {
            log.error("Erro ao publicar eventos no Event Bus", e);
            // Não relançar exceção - eventos já foram persistidos
        }
    }
    
    /**
     * Cria snapshot se necessário baseado em configuração.
     */
    private <T extends AggregateRoot> void createSnapshotIfNeeded(T aggregate) {
        try {
            // Verificar se deve criar snapshot (ex: a cada 100 eventos)
            if (shouldCreateSnapshot(aggregate)) {
                Object snapshotData = aggregate.createSnapshot();
                
                snapshotStore.saveSnapshot(
                    aggregate.getAggregateId(),
                    aggregate.getVersion(),
                    snapshotData
                );
                
                log.info("Snapshot criado para agregado: {} (versão {})", 
                    aggregate.getAggregateId(), aggregate.getVersion());
            }
            
        } catch (Exception e) {
            log.warn("Erro ao criar snapshot para agregado: {} - Continuando sem snapshot", 
                aggregate.getAggregateId(), e);
            // Não relançar exceção - snapshot é otimização
        }
    }
    
    /**
     * Determina se deve criar snapshot.
     */
    private boolean shouldCreateSnapshot(AggregateRoot aggregate) {
        // Criar snapshot a cada 100 eventos ou se não existe snapshot
        long version = aggregate.getVersion();
        
        if (version % 100 == 0) {
            return true;
        }
        
        // Verificar se já existe snapshot
        try {
            return snapshotStore.loadSnapshot(aggregate.getAggregateId()).isEmpty();
        } catch (Exception e) {
            return false;
        }
    }
}
```

---

## 🗃️ **REPOSITÓRIO DE AGREGADOS**

### **📋 AggregateRepository Interface**

**Localização**: `com.seguradora.hibrida.aggregate.repository.AggregateRepository`

```java
public interface AggregateRepository<T extends AggregateRoot> {
    
    /**
     * Salva um agregado persistindo seus eventos não commitados.
     */
    void save(T aggregate);
    
    /**
     * Busca um agregado por ID.
     */
    Optional<T> findById(String id);
    
    /**
     * Busca um agregado por ID, lançando exceção se não encontrado.
     */
    default T getById(String id) {
        return findById(id)
                .orElseThrow(() -> new AggregateNotFoundException(id, getAggregateType()));
    }
    
    /**
     * Busca um agregado em uma versão específica.
     */
    Optional<T> findByIdAndVersion(String id, long version);
    
    /**
     * Verifica se um agregado existe.
     */
    boolean exists(String id);
    
    /**
     * Retorna a versão atual de um agregado.
     */
    long getCurrentVersion(String id);
    
    /**
     * Remove todos os eventos de um agregado.
     */
    boolean delete(String id);
    
    /**
     * Força criação de snapshot para um agregado.
     */
    boolean createSnapshot(String id);
    
    /**
     * Retorna o tipo de agregado gerenciado.
     */
    Class<T> getAggregateType();
    
    /**
     * Retorna estatísticas do repositório.
     */
    Map<String, Object> getStatistics();
}
```

### **🎯 EventSourcingAggregateRepository - Implementação**

```java
public class EventSourcingAggregateRepository<T extends AggregateRoot> 
    implements AggregateRepository<T> {
    
    private static final Logger log = LoggerFactory.getLogger(EventSourcingAggregateRepository.class);
    
    private final Class<T> aggregateType;
    private final EventSourcingHandler eventSourcingHandler;
    private final AggregateMetrics metrics;
    
    public EventSourcingAggregateRepository(Class<T> aggregateType,
                                          EventSourcingHandler eventSourcingHandler,
                                          AggregateMetrics metrics) {
        this.aggregateType = aggregateType;
        this.eventSourcingHandler = eventSourcingHandler;
        this.metrics = metrics;
    }
    
    @Override
    public void save(T aggregate) {
        Timer.Sample sample = metrics.startSaveTimer();
        
        try {
            validateAggregate(aggregate);
            eventSourcingHandler.save(aggregate);
            
            log.info("Agregado salvo: {} (tipo: {})", 
                aggregate.getAggregateId(), aggregateType.getSimpleName());
                
        } catch (Exception e) {
            metrics.incrementErrors("repository_save");
            log.error("Erro ao salvar agregado no repositório: {}", 
                aggregate.getAggregateId(), e);
            throw e;
            
        } finally {
            sample.stop(metrics.getSaveTimer());
        }
    }
    
    @Override
    public Optional<T> findById(String id) {
        Timer.Sample sample = metrics.startLoadTimer();
        
        try {
            validateId(id);
            
            Optional<T> aggregate = eventSourcingHandler.load(id, aggregateType);
            
            if (aggregate.isPresent()) {
                log.debug("Agregado encontrado: {} (versão: {})", 
                    id, aggregate.get().getVersion());
            } else {
                log.debug("Agregado não encontrado: {}", id);
            }
            
            return aggregate;
            
        } catch (Exception e) {
            metrics.incrementErrors("repository_load");
            log.error("Erro ao carregar agregado: {}", id, e);
            throw e;
            
        } finally {
            sample.stop(metrics.getLoadTimer());
        }
    }
    
    @Override
    public Optional<T> findByIdAndVersion(String id, long version) {
        try {
            validateId(id);
            validateVersion(version);
            
            return eventSourcingHandler.loadVersion(id, aggregateType, version);
            
        } catch (Exception e) {
            metrics.incrementErrors("repository_load_version");
            log.error("Erro ao carregar agregado {} na versão {}", id, version, e);
            throw e;
        }
    }
    
    @Override
    public boolean exists(String id) {
        try {
            validateId(id);
            return eventSourcingHandler.exists(id);
            
        } catch (Exception e) {
            log.error("Erro ao verificar existência do agregado: {}", id, e);
            return false;
        }
    }
    
    @Override
    public long getCurrentVersion(String id) {
        try {
            validateId(id);
            return eventSourcingHandler.getCurrentVersion(id);
            
        } catch (Exception e) {
            log.error("Erro ao obter versão atual do agregado: {}", id, e);
            return 0;
        }
    }
    
    @Override
    public boolean delete(String id) {
        try {
            validateId(id);
            
            log.warn("DELETANDO agregado: {} do tipo {} - Operação irreversível!", 
                id, aggregateType.getSimpleName());
            
            return eventSourcingHandler.delete(id);
            
        } catch (Exception e) {
            metrics.incrementErrors("repository_delete");
            log.error("Erro ao deletar agregado: {}", id, e);
            return false;
        }
    }
    
    @Override
    public boolean createSnapshot(String id) {
        try {
            validateId(id);
            
            // Carregar agregado atual
            Optional<T> aggregateOpt = findById(id);
            if (aggregateOpt.isEmpty()) {
                log.warn("Não é possível criar snapshot - agregado não encontrado: {}", id);
                return false;
            }
            
            T aggregate = aggregateOpt.get();
            return createSnapshotForAggregate(aggregate);
            
        } catch (Exception e) {
            log.error("Erro ao criar snapshot para agregado: {}", id, e);
            return false;
        }
    }
    
    @Override
    public Class<T> getAggregateType() {
        return aggregateType;
    }
    
    @Override
    public Map<String, Object> getStatistics() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("aggregateType", aggregateType.getSimpleName());
        stats.put("totalLoads", metrics.getTotalLoads());
        stats.put("totalSaves", metrics.getTotalSaves());
        stats.put("totalErrors", metrics.getTotalErrors());
        stats.put("averageLoadTime", metrics.getAverageLoadTime());
        stats.put("averageSaveTime", metrics.getAverageSaveTime());
        return stats;
    }
    
    // === MÉTODOS PRIVADOS ===
    
    private void validateAggregate(T aggregate) {
        if (aggregate == null) {
            throw new IllegalArgumentException("Agregado não pode ser null");
        }
        
        if (aggregate.getAggregateId() == null || aggregate.getAggregateId().trim().isEmpty()) {
            throw new IllegalArgumentException("Agregado deve ter ID válido");
        }
        
        if (!aggregateType.isInstance(aggregate)) {
            throw new IllegalArgumentException(
                String.format("Agregado deve ser do tipo %s", aggregateType.getSimpleName()));
        }
    }
    
    private void validateId(String id) {
        if (id == null || id.trim().isEmpty()) {
            throw new IllegalArgumentException("ID do agregado não pode ser null ou vazio");
        }
    }
    
    private void validateVersion(long version) {
        if (version < 0) {
            throw new IllegalArgumentException("Versão deve ser não-negativa");
        }
    }
    
    private boolean createSnapshotForAggregate(T aggregate) {
        try {
            // Implementar criação de snapshot via EventSourcingHandler
            // Por enquanto, retornar true como placeholder
            log.info("Snapshot criado para agregado: {} (versão: {})", 
                aggregate.getAggregateId(), aggregate.getVersion());
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao criar snapshot para agregado: {}", 
                aggregate.getAggregateId(), e);
            return false;
        }
    }
}
```

---

## 🎯 **CONFIGURAÇÃO E REGISTRO**

### **📋 Configuração de Repositórios**

```java
@Configuration
@EnableConfigurationProperties(AggregateProperties.class)
public class AggregateRepositoryConfiguration {
    
    @Bean
    public EventSourcingHandler eventSourcingHandler(EventStore eventStore,
                                                   EventBus eventBus,
                                                   SnapshotStore snapshotStore,
                                                   AggregateMetrics metrics) {
        return new EventSourcingHandler(eventStore, eventBus, snapshotStore, metrics);
    }
    
    @Bean
    public AggregateRepository<ExampleAggregate> exampleAggregateRepository(
            EventSourcingHandler eventSourcingHandler,
            AggregateMetrics metrics) {
        return new EventSourcingAggregateRepository<>(
            ExampleAggregate.class, eventSourcingHandler, metrics);
    }
    
    // Registrar outros repositórios conforme necessário
    @Bean
    public AggregateRepository<SinistroAggregate> sinistroAggregateRepository(
            EventSourcingHandler eventSourcingHandler,
            AggregateMetrics metrics) {
        return new EventSourcingAggregateRepository<>(
            SinistroAggregate.class, eventSourcingHandler, metrics);
    }
}
```

---

## 🎓 **EXERCÍCIO PRÁTICO**

### **📝 Implementar Repositório Customizado**

Crie um repositório especializado para `SeguradoAggregate` que:

1. **Implemente validações específicas** do domínio
2. **Adicione métodos de consulta** customizados
3. **Integre com métricas** personalizadas
4. **Trate erros** de forma específica

**Template:**
```java
@Repository
public class SeguradoAggregateRepository extends EventSourcingAggregateRepository<SeguradoAggregate> {
    
    public SeguradoAggregateRepository(EventSourcingHandler handler, AggregateMetrics metrics) {
        super(SeguradoAggregate.class, handler, metrics);
    }
    
    // Métodos customizados
    public Optional<SeguradoAggregate> findByCpf(String cpf) {
        // Sua implementação aqui
        return Optional.empty();
    }
    
    public List<SeguradoAggregate> findByStatus(SeguradoStatus status) {
        // Sua implementação aqui
        return new ArrayList<>();
    }
}
```

---

## 📚 **REFERÊNCIAS**

- **Código**: `com.seguradora.hibrida.aggregate`
- **Handler**: `EventSourcingHandler`
- **Repositório**: `EventSourcingAggregateRepository`
- **Configuração**: `AggregateRepositoryConfiguration`

---

**📍 Próxima Parte**: [Agregados - Parte 3: Snapshots e Otimização](./08-agregados-parte-3.md)

---

**📚 Roteiro elaborado por:** Principal Java Architect  
**🎯 Foco:** Event Sourcing Handlers e repositórios  
**⏱️ Tempo estimado:** 60 minutos  
**🔧 Hands-on:** Repositório customizado com validações