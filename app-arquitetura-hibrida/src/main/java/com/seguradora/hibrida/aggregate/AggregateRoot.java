package com.seguradora.hibrida.aggregate;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.aggregate.exception.AggregateException;
import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Classe base abstrata para todos os Aggregates no sistema.
 * 
 * <p>Implementa o padrão Aggregate Root com funcionalidades completas:
 * <ul>
 *   <li>Controle de eventos não commitados com thread safety</li>
 *   <li>Aplicação automática de eventos via reflection otimizada</li>
 *   <li>Reconstrução de estado a partir de eventos históricos</li>
 *   <li>Validação automática de invariantes de negócio</li>
 *   <li>Versionamento com controle de concorrência</li>
 *   <li>Suporte a snapshots para otimização</li>
 * </ul>
 * 
 * <p><strong>Uso:</strong>
 * <pre>{@code
 * public class SeguradoAggregate extends AggregateRoot {
 *     
 *     @EventSourcingHandler
 *     protected void on(SeguradoCriadoEvent event) {
 *         this.nome = event.getNome();
 *         this.cpf = event.getCpf();
 *     }
 *     
 *     public void criarSegurado(String nome, String cpf) {
 *         validateBusinessRules();
 *         applyEvent(new SeguradoCriadoEvent(getId(), nome, cpf));
 *     }
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public abstract class AggregateRoot {
    
    /**
     * Cache de métodos de handler por classe para otimização.
     */
    private static final Map<Class<?>, Map<Class<? extends DomainEvent>, Method>> HANDLER_CACHE = 
            new ConcurrentHashMap<>();
    
    /**
     * ID único do aggregate.
     */
    @Getter
    private String id;
    
    /**
     * Versão atual do aggregate para controle de concorrência.
     */
    @Getter
    private long version = 0;
    
    /**
     * Timestamp da última modificação.
     */
    @Getter
    private Instant lastModified;
    
    /**
     * Lista thread-safe de eventos não commitados.
     */
    private final List<DomainEvent> uncommittedEvents = new CopyOnWriteArrayList<>();
    
    /**
     * Regras de negócio registradas para validação automática.
     */
    private final List<BusinessRule> businessRules = new ArrayList<>();
    
    /**
     * Flag indicando se o aggregate foi carregado do histórico.
     */
    private boolean loadedFromHistory = false;
    
    /**
     * Construtor protegido para subclasses.
     */
    protected AggregateRoot() {
        this.lastModified = Instant.now();
    }
    
    /**
     * Construtor com ID específico.
     */
    protected AggregateRoot(String id) {
        this.id = id;
        this.lastModified = Instant.now();
    }
    
    /**
     * Aplica um evento ao aggregate e o adiciona à lista de não commitados.
     * 
     * <p>Este método:
     * <ul>
     *   <li>Configura metadados do evento (aggregate ID, versão)</li>
     *   <li>Aplica o evento ao estado interno</li>
     *   <li>Adiciona à lista de eventos não commitados</li>
     *   <li>Valida invariantes de negócio</li>
     *   <li>Atualiza versão e timestamp</li>
     * </ul>
     * 
     * @param event Evento a ser aplicado
     * @throws AggregateException se houver erro na aplicação
     * @throws BusinessRuleViolationException se invariantes forem violadas
     */
    protected void applyEvent(DomainEvent event) {
        try {
            log.debug("Aplicando evento {} ao aggregate {} versão {}", 
                    event.getEventType(), getId(), getVersion());
            
            // Configurar metadados do evento
            event.setAggregateId(getId());
            event.setAggregateType(getClass().getSimpleName());
            event.setVersion(getVersion() + 1);
            event.setTimestamp(Instant.now());
            
            // Aplicar evento ao estado
            applyEventToState(event);
            
            // Adicionar à lista de não commitados apenas se não estiver carregando do histórico
            if (!loadedFromHistory) {
                uncommittedEvents.add(event);
            }
            
            // Atualizar versão e timestamp
            this.version = event.getVersion();
            this.lastModified = event.getTimestamp();
            
            // Validar invariantes após aplicação
            validateBusinessRules();
            
            log.debug("Evento {} aplicado com sucesso. Nova versão: {}", 
                    event.getEventType(), getVersion());
            
        } catch (Exception e) {
            log.error("Erro ao aplicar evento {} ao aggregate {}: {}", 
                    event.getEventType(), getId(), e.getMessage(), e);
            throw new AggregateException("Erro ao aplicar evento: " + e.getMessage(), e);
        }
    }
    
    /**
     * Aplica um evento ao estado interno usando reflection otimizada.
     * 
     * <p>Busca por método anotado com @EventSourcingHandler que aceite
     * o tipo específico do evento. Utiliza cache para otimizar performance.
     * 
     * @param event Evento a ser aplicado
     * @throws AggregateException se não encontrar handler ou houver erro
     */
    private void applyEventToState(DomainEvent event) {
        Class<?> aggregateClass = this.getClass();
        Class<? extends DomainEvent> eventClass = event.getClass();
        
        // Buscar no cache primeiro
        Map<Class<? extends DomainEvent>, Method> classHandlers = 
                HANDLER_CACHE.computeIfAbsent(aggregateClass, k -> new ConcurrentHashMap<>());
        
        Method handler = classHandlers.get(eventClass);
        
        if (handler == null) {
            // Buscar handler via reflection
            handler = findEventHandler(aggregateClass, eventClass);
            if (handler != null) {
                handler.setAccessible(true);
                classHandlers.put(eventClass, handler);
            }
        }
        
        if (handler == null) {
            throw new AggregateException(
                    String.format("Nenhum handler encontrado para evento %s no aggregate %s", 
                            eventClass.getSimpleName(), aggregateClass.getSimpleName()));
        }
        
        try {
            handler.invoke(this, event);
        } catch (Exception e) {
            throw new AggregateException(
                    String.format("Erro ao executar handler para evento %s: %s", 
                            eventClass.getSimpleName(), e.getMessage()), e);
        }
    }
    
    /**
     * Busca método handler para um tipo específico de evento.
     * 
     * @param aggregateClass Classe do aggregate
     * @param eventClass Classe do evento
     * @return Método handler ou null se não encontrado
     */
    private Method findEventHandler(Class<?> aggregateClass, Class<? extends DomainEvent> eventClass) {
        for (Method method : aggregateClass.getDeclaredMethods()) {
            if (method.isAnnotationPresent(EventSourcingHandler.class) &&
                method.getParameterCount() == 1 &&
                method.getParameterTypes()[0].isAssignableFrom(eventClass)) {
                return method;
            }
        }
        
        // Buscar em superclasses
        Class<?> superClass = aggregateClass.getSuperclass();
        if (superClass != null && superClass != AggregateRoot.class) {
            return findEventHandler(superClass, eventClass);
        }
        
        return null;
    }
    
    /**
     * Reconstrói o estado do aggregate a partir de uma lista de eventos históricos.
     * 
     * <p>Este método:
     * <ul>
     *   <li>Limpa o estado atual</li>
     *   <li>Aplica todos os eventos em ordem</li>
     *   <li>Não adiciona eventos à lista de não commitados</li>
     *   <li>Atualiza versão para a do último evento</li>
     * </ul>
     * 
     * @param events Lista ordenada de eventos históricos
     * @throws AggregateException se houver erro na reconstrução
     */
    public void loadFromHistory(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            log.debug("Nenhum evento para carregar no aggregate {}", getId());
            return;
        }
        
        log.debug("Carregando {} eventos para aggregate {}", events.size(), getId());
        
        try {
            loadedFromHistory = true;
            
            // Limpar estado atual
            clearState();
            
            // Aplicar eventos em ordem
            for (DomainEvent event : events) {
                applyEventToState(event);
                this.version = event.getVersion();
                this.lastModified = event.getTimestamp();
                
                // Definir ID se ainda não foi definido
                if (this.id == null) {
                    this.id = event.getAggregateId();
                }
            }
            
            log.debug("Aggregate {} reconstruído com sucesso. Versão final: {}", 
                    getId(), getVersion());
            
        } catch (Exception e) {
            log.error("Erro ao reconstruir aggregate {} do histórico: {}", 
                    getId(), e.getMessage(), e);
            throw new AggregateException("Erro ao reconstruir aggregate do histórico: " + e.getMessage(), e);
        } finally {
            loadedFromHistory = false;
        }
    }
    
    /**
     * Reconstrói o aggregate a partir de um snapshot e eventos incrementais.
     * 
     * <p>Otimização para aggregates com muitos eventos. Carrega o estado
     * do snapshot e aplica apenas os eventos posteriores.
     * 
     * @param snapshotData Dados do snapshot
     * @param incrementalEvents Eventos posteriores ao snapshot
     * @throws AggregateException se houver erro na reconstrução
     */
    public void loadFromSnapshot(Object snapshotData, List<DomainEvent> incrementalEvents) {
        log.debug("Carregando aggregate {} do snapshot com {} eventos incrementais", 
                getId(), incrementalEvents != null ? incrementalEvents.size() : 0);
        
        try {
            // Restaurar estado do snapshot
            restoreFromSnapshot(snapshotData);
            
            // Aplicar eventos incrementais se existirem
            if (incrementalEvents != null && !incrementalEvents.isEmpty()) {
                loadFromHistory(incrementalEvents);
            }
            
            log.debug("Aggregate {} carregado do snapshot com sucesso", getId());
            
        } catch (Exception e) {
            log.error("Erro ao carregar aggregate {} do snapshot: {}", 
                    getId(), e.getMessage(), e);
            throw new AggregateException("Erro ao carregar aggregate do snapshot: " + e.getMessage(), e);
        }
    }
    
    /**
     * Restaura o estado interno a partir dos dados do snapshot.
     * 
     * <p>Método abstrato que deve ser implementado pelas subclasses
     * para definir como restaurar seu estado específico.
     * 
     * @param snapshotData Dados do snapshot
     */
    protected abstract void restoreFromSnapshot(Object snapshotData);
    
    /**
     * Limpa o estado interno do aggregate.
     * 
     * <p>Método abstrato que deve ser implementado pelas subclasses
     * para limpar seu estado específico antes da reconstrução.
     */
    protected abstract void clearState();
    
    /**
     * Cria um snapshot do estado atual do aggregate.
     * 
     * <p>Método abstrato que deve ser implementado pelas subclasses
     * para definir como serializar seu estado para snapshot.
     * 
     * @return Dados do snapshot
     */
    public abstract Object createSnapshot();
    
    /**
     * Retorna lista imutável de eventos não commitados.
     * 
     * @return Lista de eventos não commitados
     */
    public List<DomainEvent> getUncommittedEvents() {
        return Collections.unmodifiableList(uncommittedEvents);
    }
    
    /**
     * Marca todos os eventos não commitados como commitados.
     * 
     * <p>Deve ser chamado após persistência bem-sucedida dos eventos.
     */
    public void markEventsAsCommitted() {
        log.debug("Marcando {} eventos como commitados para aggregate {}", 
                uncommittedEvents.size(), getId());
        uncommittedEvents.clear();
    }
    
    /**
     * Verifica se existem eventos não commitados.
     * 
     * @return true se existem eventos não commitados
     */
    public boolean hasUncommittedEvents() {
        return !uncommittedEvents.isEmpty();
    }
    
    /**
     * Registra uma regra de negócio para validação automática.
     * 
     * @param rule Regra de negócio
     */
    protected void registerBusinessRule(BusinessRule rule) {
        if (rule != null) {
            businessRules.add(rule);
        }
    }
    
    /**
     * Valida todas as regras de negócio registradas.
     * 
     * @throws BusinessRuleViolationException se alguma regra for violada
     */
    protected void validateBusinessRules() {
        List<String> violations = new ArrayList<>();
        
        for (BusinessRule rule : businessRules) {
            try {
                if (!rule.isValid(this)) {
                    violations.add(rule.getErrorMessage());
                }
            } catch (Exception e) {
                log.error("Erro ao validar regra de negócio {}: {}", 
                        rule.getClass().getSimpleName(), e.getMessage(), e);
                violations.add("Erro na validação: " + e.getMessage());
            }
        }
        
        if (!violations.isEmpty()) {
            throw new BusinessRuleViolationException(
                    "Violações de regras de negócio encontradas", violations);
        }
    }
    
    /**
     * Retorna o tipo do aggregate (nome da classe).
     * 
     * @return Tipo do aggregate
     */
    public String getAggregateType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Verifica se o aggregate foi modificado (tem eventos não commitados).
     * 
     * @return true se foi modificado
     */
    public boolean isModified() {
        return hasUncommittedEvents();
    }
    
    /**
     * Retorna informações de debug do aggregate.
     * 
     * @return Mapa com informações de debug
     */
    public Map<String, Object> getDebugInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("id", getId());
        info.put("type", getAggregateType());
        info.put("version", getVersion());
        info.put("lastModified", getLastModified());
        info.put("uncommittedEvents", getUncommittedEvents().size());
        info.put("businessRules", businessRules.size());
        info.put("loadedFromHistory", loadedFromHistory);
        return info;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AggregateRoot that = (AggregateRoot) obj;
        return Objects.equals(id, that.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
    
    @Override
    public String toString() {
        return String.format("%s{id='%s', version=%d, uncommittedEvents=%d}", 
                getClass().getSimpleName(), id, version, uncommittedEvents.size());
    }
}