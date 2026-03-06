package com.seguradora.hibrida.aggregate.example;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Exemplo de implementação de Aggregate usando o sistema Event Sourcing.
 * 
 * <p>Este exemplo demonstra:
 * <ul>
 *   <li>Extensão da classe AggregateRoot</li>
 *   <li>Implementação de handlers de eventos</li>
 *   <li>Aplicação de eventos de domínio</li>
 *   <li>Validação de regras de negócio</li>
 *   <li>Criação e restauração de snapshots</li>
 * </ul>
 * 
 * <p><strong>Eventos suportados:</strong>
 * <ul>
 *   <li>ExampleCreatedEvent - Criação do aggregate</li>
 *   <li>ExampleUpdatedEvent - Atualização de dados</li>
 *   <li>ExampleActivatedEvent - Ativação</li>
 *   <li>ExampleDeactivatedEvent - Desativação</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Getter
public class ExampleAggregate extends AggregateRoot {
    
    // Estado interno do aggregate
    private String name;
    private String description;
    private ExampleStatus status;
    private Instant createdAt;
    private Instant lastUpdatedAt;
    private Map<String, Object> metadata;
    
    /**
     * Construtor padrão para reconstrução do aggregate.
     */
    public ExampleAggregate() {
        super();
        this.metadata = new HashMap<>();
        registerBusinessRules();
    }
    
    /**
     * Construtor para criação de novo aggregate.
     */
    public ExampleAggregate(String id) {
        super(id);
        this.metadata = new HashMap<>();
        registerBusinessRules();
    }
    
    /**
     * Cria um novo exemplo com dados iniciais.
     */
    public void create(String name, String description) {
        log.debug("Criando novo exemplo: {} - {}", name, description);
        
        // Validar parâmetros
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        if (description == null || description.trim().isEmpty()) {
            throw new IllegalArgumentException("Descrição é obrigatória");
        }
        
        // Aplicar evento de criação
        ExampleCreatedEvent event = ExampleCreatedEvent.create(
                getId() != null ? getId() : UUID.randomUUID().toString(),
                name,
                description,
                Instant.now()
        );
        
        applyEvent(event);
    }
    
    /**
     * Atualiza dados do exemplo.
     */
    public void update(String newName, String newDescription) {
        log.debug("Atualizando exemplo {}: {} - {}", getId(), newName, newDescription);
        
        // Validar estado atual
        if (status == ExampleStatus.INACTIVE) {
            throw new IllegalStateException("Não é possível atualizar exemplo inativo");
        }
        
        // Validar parâmetros
        if (newName == null || newName.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome é obrigatório");
        }
        
        // Aplicar evento de atualização apenas se houver mudança
        if (!newName.equals(this.name) || !newDescription.equals(this.description)) {
            ExampleUpdatedEvent event = ExampleUpdatedEvent.create(
                    getId(),
                    getVersion() + 1,
                    newName,
                    newDescription,
                    Instant.now()
            );
            
            applyEvent(event);
        }
    }
    
    /**
     * Ativa o exemplo.
     */
    public void activate() {
        log.debug("Ativando exemplo {}", getId());
        
        if (status == ExampleStatus.ACTIVE) {
            log.debug("Exemplo {} já está ativo", getId());
            return;
        }
        
        ExampleActivatedEvent event = ExampleActivatedEvent.create(
                getId(),
                getVersion() + 1,
                Instant.now()
        );
        
        applyEvent(event);
    }
    
    /**
     * Desativa o exemplo.
     */
    public void deactivate() {
        log.debug("Desativando exemplo {}", getId());
        
        if (status == ExampleStatus.INACTIVE) {
            log.debug("Exemplo {} já está inativo", getId());
            return;
        }
        
        ExampleDeactivatedEvent event = ExampleDeactivatedEvent.create(
                getId(),
                getVersion() + 1,
                Instant.now()
        );
        
        applyEvent(event);
    }
    
    /**
     * Adiciona metadado customizado.
     */
    public void addMetadata(String key, Object value) {
        if (key != null && !key.trim().isEmpty()) {
            this.metadata.put(key, value);
        }
    }
    
    // === EVENT HANDLERS ===
    
    /**
     * Handler para evento de criação.
     */
    @EventSourcingHandler
    protected void on(ExampleCreatedEvent event) {
        log.debug("Aplicando evento de criação: {}", event);
        
        this.name = event.getName();
        this.description = event.getDescription();
        this.status = ExampleStatus.ACTIVE;
        this.createdAt = event.getTimestamp();
        this.lastUpdatedAt = event.getTimestamp();
    }
    
    /**
     * Handler para evento de atualização.
     */
    @EventSourcingHandler
    protected void on(ExampleUpdatedEvent event) {
        log.debug("Aplicando evento de atualização: {}", event);
        
        this.name = event.getNewName();
        this.description = event.getNewDescription();
        this.lastUpdatedAt = event.getTimestamp();
    }
    
    /**
     * Handler para evento de ativação.
     */
    @EventSourcingHandler
    protected void on(ExampleActivatedEvent event) {
        log.debug("Aplicando evento de ativação: {}", event);
        
        this.status = ExampleStatus.ACTIVE;
        this.lastUpdatedAt = event.getTimestamp();
    }
    
    /**
     * Handler para evento de desativação.
     */
    @EventSourcingHandler
    protected void on(ExampleDeactivatedEvent event) {
        log.debug("Aplicando evento de desativação: {}", event);
        
        this.status = ExampleStatus.INACTIVE;
        this.lastUpdatedAt = event.getTimestamp();
    }
    
    // === SNAPSHOT SUPPORT ===
    
    @Override
    public Object createSnapshot() {
        log.debug("Criando snapshot do exemplo {}", getId());
        
        Map<String, Object> snapshot = new HashMap<>();
        snapshot.put("id", getId());
        snapshot.put("name", name);
        snapshot.put("description", description);
        snapshot.put("status", status != null ? status.name() : null);
        snapshot.put("createdAt", createdAt);
        snapshot.put("lastUpdatedAt", lastUpdatedAt);
        snapshot.put("metadata", new HashMap<>(metadata));
        snapshot.put("version", getVersion());
        
        return snapshot;
    }
    
    @Override
    @SuppressWarnings("unchecked")
    protected void restoreFromSnapshot(Object snapshotData) {
        log.debug("Restaurando exemplo {} do snapshot", getId());
        
        if (!(snapshotData instanceof Map)) {
            throw new IllegalArgumentException("Dados de snapshot inválidos");
        }
        
        Map<String, Object> snapshot = (Map<String, Object>) snapshotData;
        
        this.name = (String) snapshot.get("name");
        this.description = (String) snapshot.get("description");
        
        String statusStr = (String) snapshot.get("status");
        this.status = statusStr != null ? ExampleStatus.valueOf(statusStr) : ExampleStatus.ACTIVE;
        
        this.createdAt = (Instant) snapshot.get("createdAt");
        this.lastUpdatedAt = (Instant) snapshot.get("lastUpdatedAt");
        
        Object metadataObj = snapshot.get("metadata");
        if (metadataObj instanceof Map) {
            this.metadata = new HashMap<>((Map<String, Object>) metadataObj);
        } else {
            this.metadata = new HashMap<>();
        }
    }
    
    @Override
    protected void clearState() {
        log.debug("Limpando estado do exemplo {}", getId());
        
        this.name = null;
        this.description = null;
        this.status = null;
        this.createdAt = null;
        this.lastUpdatedAt = null;
        this.metadata.clear();
    }
    
    // === BUSINESS RULES ===
    
    /**
     * Registra regras de negócio do aggregate.
     */
    private void registerBusinessRules() {
        // Regra: Nome deve ter pelo menos 3 caracteres
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                if (!(aggregate instanceof ExampleAggregate)) {
                    return true;
                }
                ExampleAggregate example = (ExampleAggregate) aggregate;
                return example.name == null || example.name.length() >= 3;
            }
            
            @Override
            public String getErrorMessage() {
                return "Nome deve ter pelo menos 3 caracteres";
            }
        });
        
        // Regra: Descrição deve ter pelo menos 10 caracteres
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                if (!(aggregate instanceof ExampleAggregate)) {
                    return true;
                }
                ExampleAggregate example = (ExampleAggregate) aggregate;
                return example.description == null || example.description.length() >= 10;
            }
            
            @Override
            public String getErrorMessage() {
                return "Descrição deve ter pelo menos 10 caracteres";
            }
        });
    }
    
    // === UTILITY METHODS ===
    
    /**
     * Verifica se o exemplo está ativo.
     */
    public boolean isActive() {
        return status == ExampleStatus.ACTIVE;
    }
    
    /**
     * Verifica se o exemplo foi criado recentemente (últimas 24h).
     */
    public boolean isRecentlyCreated() {
        if (createdAt == null) {
            return false;
        }
        
        Instant yesterday = Instant.now().minusSeconds(24 * 60 * 60);
        return createdAt.isAfter(yesterday);
    }
    
    /**
     * Obtém idade do exemplo em dias.
     */
    public long getAgeInDays() {
        if (createdAt == null) {
            return 0;
        }
        
        return java.time.Duration.between(createdAt, Instant.now()).toDays();
    }
    
    @Override
    public String toString() {
        return String.format("ExampleAggregate{id='%s', name='%s', status=%s, version=%d}", 
                getId(), name, status, getVersion());
    }
    
    /**
     * Enum para status do exemplo.
     */
    public enum ExampleStatus {
        ACTIVE,
        INACTIVE
    }
}