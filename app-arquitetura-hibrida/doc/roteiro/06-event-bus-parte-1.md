# 📘 EVENT BUS - PARTE 1
## Fundamentos e Arquitetura do Event Bus

### 🎯 **OBJETIVOS DESTA PARTE**
- Compreender os fundamentos do Event Bus
- Entender a diferença entre Command Bus e Event Bus
- Conhecer a arquitetura de eventos do projeto
- Dominar os conceitos de Domain Events

---

## 🌟 **INTRODUÇÃO AO EVENT BUS**

### **🎯 O que é um Event Bus?**

O **Event Bus** é o componente central da arquitetura orientada a eventos, responsável por:
- **Publicar** eventos de domínio
- **Rotear** eventos para handlers apropriados
- **Garantir** entrega confiável de eventos
- **Desacoplar** produtores de consumidores

### **🔄 Command Bus vs Event Bus**

| **Aspecto** | **Command Bus** | **Event Bus** |
|-------------|-----------------|---------------|
| **Propósito** | Executar ações | Notificar sobre fatos |
| **Direção** | 1:1 (um handler) | 1:N (múltiplos handlers) |
| **Timing** | Síncrono/Assíncrono | Principalmente assíncrono |
| **Falha** | Falha impede execução | Falha não impede outros handlers |
| **Resultado** | Retorna resultado | Fire-and-forget |

---

## 🏗️ **ARQUITETURA DO EVENT BUS**

### **📊 Visão Geral da Arquitetura**

```
Domain Event → Event Bus → Event Handlers → Side Effects
     ↓             ↓            ↓              ↓
  Aggregate    Publisher    Projections    Notifications
   Changes      Router      Updates        Integrations
```

### **🎯 Componentes Principais**

#### **1. Domain Events**
```java
// Interface base para eventos de domínio
public interface DomainEvent {
    UUID getEventId();
    String getAggregateId();
    Instant getTimestamp();
    String getEventType();
    Long getVersion();
}
```

#### **2. Event Bus Interface**
```java
public interface EventBus {
    // Publicação síncrona
    void publish(DomainEvent event);
    
    // Publicação assíncrona
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    // Publicação em lote
    void publishBatch(List<DomainEvent> events);
    
    // Registro de handlers
    <T extends DomainEvent> void registerHandler(
        Class<T> eventType, EventHandler<T> handler);
}
```

#### **3. Event Handlers**
```java
public interface EventHandler<T extends DomainEvent> {
    void handle(T event);
    Class<T> getEventType();
    boolean isAsync();
    int getPriority();
}
```

---

## 📋 **IMPLEMENTAÇÃO NO PROJETO**

### **🔍 Estrutura de Pastas**

```
src/main/java/com/seguradora/hibrida/eventbus/
├── EventBus.java                    # Interface principal
├── EventHandler.java                # Interface para handlers
├── EventHandlerRegistry.java        # Registro de handlers
├── EventBusStatistics.java          # Métricas e estatísticas
├── impl/
│   ├── SimpleEventBus.java          # Implementação simples
│   └── KafkaEventBus.java           # Implementação com Kafka
├── config/
│   ├── EventBusConfiguration.java   # Configuração Spring
│   ├── EventBusProperties.java      # Propriedades
│   └── EventBusMetrics.java         # Métricas Micrometer
├── exception/
│   ├── EventHandlingException.java  # Exceções de handling
│   └── EventPublishingException.java # Exceções de publicação
└── example/
    ├── SinistroEvent.java           # Evento de exemplo
    └── SinistroEventHandler.java    # Handler de exemplo
```

### **🎯 EventBus Interface Completa**

Localização: `com.seguradora.hibrida.eventbus.EventBus`

```java
public interface EventBus {
    
    /**
     * Publica um evento de domínio de forma síncrona.
     * 
     * <p>O evento é processado imediatamente por todos os handlers
     * registrados. Se algum handler falhar, uma exceção é lançada.
     * 
     * @param event Evento a ser publicado
     * @throws EventPublishingException se houver erro na publicação
     * @throws IllegalArgumentException se o evento for null
     */
    void publish(DomainEvent event);
    
    /**
     * Publica um evento de domínio de forma assíncrona.
     * 
     * <p>O evento é processado em background. Falhas são logadas
     * mas não impedem a execução do código chamador.
     * 
     * @param event Evento a ser publicado
     * @return CompletableFuture que completa quando processamento termina
     * @throws IllegalArgumentException se o evento for null
     */
    CompletableFuture<Void> publishAsync(DomainEvent event);
    
    /**
     * Publica múltiplos eventos em lote de forma assíncrona.
     * 
     * <p>Otimização para cenários onde múltiplos eventos precisam
     * ser publicados simultaneamente.
     * 
     * @param events Lista de eventos a serem publicados
     * @return CompletableFuture que completa quando todos são processados
     * @throws IllegalArgumentException se a lista for null ou vazia
     */
    CompletableFuture<Void> publishBatchAsync(List<DomainEvent> events);
    
    /**
     * Registra um handler para um tipo específico de evento.
     * 
     * <p>Este método é usado internamente pelo sistema de descoberta automática.
     * Normalmente não deve ser chamado diretamente.
     * 
     * @param eventType Tipo do evento (classe)
     * @param handler Handler a ser registrado
     * @param <T> Tipo do evento
     */
    <T extends DomainEvent> void registerHandler(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Remove um handler registrado para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @param handler Handler a ser removido
     * @param <T> Tipo do evento
     */
    <T extends DomainEvent> void unregisterHandler(Class<T> eventType, EventHandler<T> handler);
    
    /**
     * Verifica se existem handlers registrados para um tipo de evento.
     * 
     * @param eventType Tipo do evento
     * @return true se existirem handlers, false caso contrário
     */
    boolean hasHandlers(Class<? extends DomainEvent> eventType);
    
    /**
     * Obtém estatísticas de execução do Event Bus.
     * 
     * @return Estatísticas detalhadas
     */
    EventBusStatistics getStatistics();
    
    /**
     * Verifica se o Event Bus está saudável e operacional.
     * 
     * @return true se estiver saudável, false caso contrário
     */
    boolean isHealthy();
    
    /**
     * Para o Event Bus graciosamente, aguardando o processamento
     * de eventos pendentes.
     * 
     * @param timeoutSeconds Timeout em segundos para aguardar
     * @return true se parou graciosamente, false se houve timeout
     */
    boolean shutdown(int timeoutSeconds);
}
```

---

## 🎯 **DOMAIN EVENTS**

### **📝 Classe Base DomainEvent**

Localização: `com.seguradora.hibrida.eventstore.model.DomainEvent`

```java
@MappedSuperclass
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "eventType")
public abstract class DomainEvent {
    
    @Id
    private UUID eventId;
    
    @Column(name = "aggregate_id", nullable = false)
    private String aggregateId;
    
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    @Column(name = "version", nullable = false)
    private Long version;
    
    @Embedded
    private EventMetadata metadata;
    
    protected DomainEvent() {
        this.eventId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.metadata = new EventMetadata();
    }
    
    protected DomainEvent(String aggregateId, Long version) {
        this();
        this.aggregateId = aggregateId;
        this.version = version;
    }
    
    /**
     * Retorna o tipo do evento baseado no nome da classe.
     */
    public String getEventType() {
        return this.getClass().getSimpleName();
    }
    
    /**
     * Adiciona metadados ao evento.
     */
    public void addMetadata(String key, Object value) {
        ensureMetadata();
        this.metadata.put(key, value);
    }
    
    /**
     * Define ID de correlação para rastreamento.
     */
    public void setCorrelationId(UUID correlationId) {
        addMetadata("correlationId", correlationId.toString());
    }
    
    /**
     * Define usuário que causou o evento.
     */
    public void setUserId(String userId) {
        addMetadata("userId", userId);
    }
    
    private void ensureMetadata() {
        if (this.metadata == null) {
            this.metadata = new EventMetadata();
        }
    }
    
    // Getters e setters...
}
```

### **🎯 Exemplo: SinistroEvent**

Localização: `com.seguradora.hibrida.eventbus.example.SinistroEvent`

```java
@Entity
@Table(name = "sinistro_events")
@JsonTypeName("SinistroEvent")
public class SinistroEvent extends DomainEvent {
    
    @Column(name = "numero_sinistro")
    private String numeroSinistro;
    
    @Column(name = "tipo_evento")
    private String tipoEvento;
    
    @Column(name = "descricao")
    private String descricao;
    
    @Column(name = "valor_estimado")
    private Double valorEstimado;
    
    @Column(name = "status")
    private String status;
    
    // Construtor protegido para JPA
    protected SinistroEvent() {
        super();
    }
    
    // Factory methods para diferentes tipos de eventos
    public static SinistroEvent sinistroCriado(String aggregateId, 
                                              String numeroSinistro, 
                                              String descricao, 
                                              Double valorEstimado) {
        SinistroEvent event = new SinistroEvent();
        event.setAggregateId(aggregateId);
        event.setVersion(1L);
        event.numeroSinistro = numeroSinistro;
        event.tipoEvento = "SINISTRO_CRIADO";
        event.descricao = descricao;
        event.valorEstimado = valorEstimado;
        event.status = "ABERTO";
        return event;
    }
    
    public static SinistroEvent sinistroAtualizado(String aggregateId, 
                                                  String numeroSinistro, 
                                                  String status, 
                                                  String descricao, 
                                                  Double valorEstimado) {
        SinistroEvent event = new SinistroEvent();
        event.setAggregateId(aggregateId);
        event.numeroSinistro = numeroSinistro;
        event.tipoEvento = "SINISTRO_ATUALIZADO";
        event.descricao = descricao;
        event.valorEstimado = valorEstimado;
        event.status = status;
        return event;
    }
    
    public static SinistroEvent sinistroFinalizado(String aggregateId, 
                                                  String numeroSinistro, 
                                                  Double valorFinal) {
        SinistroEvent event = new SinistroEvent();
        event.setAggregateId(aggregateId);
        event.numeroSinistro = numeroSinistro;
        event.tipoEvento = "SINISTRO_FINALIZADO";
        event.valorEstimado = valorFinal;
        event.status = "FECHADO";
        return event;
    }
    
    public static SinistroEvent sinistroCancelado(String aggregateId, 
                                                 String numeroSinistro, 
                                                 String motivo) {
        SinistroEvent event = new SinistroEvent();
        event.setAggregateId(aggregateId);
        event.numeroSinistro = numeroSinistro;
        event.tipoEvento = "SINISTRO_CANCELADO";
        event.descricao = motivo;
        event.status = "CANCELADO";
        return event;
    }
    
    @Override
    public String toString() {
        return String.format("SinistroEvent{id=%s, tipo=%s, numero=%s, status=%s}", 
                           getEventId(), tipoEvento, numeroSinistro, status);
    }
    
    // Getters e setters...
}
```

---

## 🔧 **EVENT HANDLERS**

### **📝 Interface EventHandler**

Localização: `com.seguradora.hibrida.eventbus.EventHandler`

```java
public interface EventHandler<T extends DomainEvent> {
    
    /**
     * Processa um evento de domínio.
     * 
     * <p>Este método deve ser idempotente, pois pode ser chamado
     * múltiplas vezes em caso de retry.
     * 
     * @param event Evento a ser processado
     * @throws EventHandlingException em caso de erro no processamento
     */
    void handle(T event);
    
    /**
     * Retorna o tipo de evento que este handler processa.
     * 
     * <p>Usado pelo Event Bus para roteamento automático.
     * 
     * @return Classe do tipo de evento
     */
    Class<T> getEventType();
    
    /**
     * Indica se este handler suporta retry em caso de falha.
     * 
     * <p>Handlers que retornam true terão suas falhas reprocessadas
     * de acordo com a política de retry configurada.
     * 
     * @return true se suporta retry, false caso contrário (padrão: true)
     */
    default boolean isRetryable() {
        return true;
    }
    
    /**
     * Retorna a prioridade deste handler.
     * 
     * <p>Handlers com prioridade maior são executados primeiro.
     * Útil quando há dependências entre handlers.
     * 
     * @return Prioridade (padrão: 0)
     */
    default int getPriority() {
        return 0;
    }
    
    /**
     * Indica se este handler deve ser executado de forma assíncrona.
     * 
     * <p>Handlers assíncronos são executados em thread separada,
     * permitindo maior throughput mas perdendo garantias de ordem.
     * 
     * @return true se deve ser assíncrono, false caso contrário (padrão: true)
     */
    default boolean isAsync() {
        return true;
    }
    
    /**
     * Retorna o timeout em segundos para execução deste handler.
     * 
     * <p>Se o handler não completar dentro do timeout, será cancelado
     * e tratado como falha.
     * 
     * @return Timeout em segundos (padrão: 30)
     */
    default int getTimeoutSeconds() {
        return 30;
    }
    
    /**
     * Verifica se este handler suporta o processamento do evento.
     * 
     * <p>Permite validações adicionais além do tipo do evento.
     * 
     * @param event Evento a ser verificado
     * @return true se suporta, false caso contrário (padrão: true)
     */
    default boolean supports(T event) {
        return true;
    }
}
```

### **🎯 Exemplo: SinistroEventHandler**

Localização: `com.seguradora.hibrida.eventbus.example.SinistroEventHandler`

```java
@Component
@Slf4j
public class SinistroEventHandler implements EventHandler<SinistroEvent> {
    
    @Override
    public void handle(SinistroEvent event) {
        log.info("Processing SinistroEvent: {}", event);
        
        try {
            // Roteamento baseado no tipo de evento
            switch (event.getTipoEvento()) {
                case "SINISTRO_CRIADO":
                    handleSinistroCriado(event);
                    break;
                case "SINISTRO_ATUALIZADO":
                    handleSinistroAtualizado(event);
                    break;
                case "SINISTRO_FINALIZADO":
                    handleSinistroFinalizado(event);
                    break;
                case "SINISTRO_CANCELADO":
                    handleSinistroCancelado(event);
                    break;
                default:
                    handleGenericSinistroEvent(event);
            }
            
            log.debug("Successfully processed SinistroEvent: {}", event.getEventId());
            
        } catch (Exception e) {
            log.error("Error processing SinistroEvent: {}", event.getEventId(), e);
            throw new EventHandlingException(event, this.getClass().getSimpleName(), e);
        }
    }
    
    private void handleSinistroCriado(SinistroEvent event) {
        log.info("Handling SINISTRO_CRIADO: {} - {}", 
                event.getNumeroSinistro(), event.getDescricao());
        
        // Simula processamento específico para criação
        simulateProcessing(100);
        
        // Aqui seria implementada a lógica específica:
        // - Atualizar projeções
        // - Enviar notificações
        // - Integrar com sistemas externos
    }
    
    private void handleSinistroAtualizado(SinistroEvent event) {
        log.info("Handling SINISTRO_ATUALIZADO: {} - Status: {}", 
                event.getNumeroSinistro(), event.getStatus());
        
        simulateProcessing(50);
        
        // Lógica específica para atualização
    }
    
    private void handleSinistroFinalizado(SinistroEvent event) {
        log.info("Handling SINISTRO_FINALIZADO: {} - Valor: {}", 
                event.getNumeroSinistro(), event.getValorEstimado());
        
        simulateProcessing(200);
        
        // Lógica específica para finalização
    }
    
    private void handleSinistroCancelado(SinistroEvent event) {
        log.info("Handling SINISTRO_CANCELADO: {} - Motivo: {}", 
                event.getNumeroSinistro(), event.getDescricao());
        
        simulateProcessing(75);
        
        // Lógica específica para cancelamento
    }
    
    private void handleGenericSinistroEvent(SinistroEvent event) {
        log.warn("Handling unknown SinistroEvent type: {} for event: {}", 
                event.getTipoEvento(), event.getEventId());
        
        // Processamento genérico ou log de evento desconhecido
    }
    
    private void simulateProcessing(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
    
    @Override
    public Class<SinistroEvent> getEventType() {
        return SinistroEvent.class;
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono
    }
    
    @Override
    public int getPriority() {
        return 10; // Prioridade alta para eventos de sinistro
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 60; // Timeout de 1 minuto
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Permite retry em caso de falha
    }
    
    @Override
    public boolean supports(SinistroEvent event) {
        // Verifica se o evento tem dados mínimos necessários
        return event.getNumeroSinistro() != null && 
               event.getTipoEvento() != null;
    }
}
```

---

## 🎯 **PADRÕES DE EVENTOS**

### **📋 Convenções de Nomenclatura**

#### **1. Nomes de Eventos**
```java
// ✅ BOM: Verbos no passado, específicos
SinistroCriado
SinistroAtualizado
SinistroFinalizado
SeguradoCadastrado
ApoliceRenovada

// ❌ EVITAR: Verbos no presente, genéricos
CriarSinistro        // Isso é um comando, não evento
SinistroChange       // Muito genérico
EventoSinistro       // Redundante
```

#### **2. Estrutura de Dados**
```java
// ✅ BOM: Dados suficientes para processamento
public class SinistroCriadoEvent extends DomainEvent {
    private String numeroSinistro;
    private String cpfSegurado;
    private String placaVeiculo;
    private Instant dataOcorrencia;
    private String descricao;
    private BigDecimal valorEstimado;
    // ... outros dados relevantes
}

// ❌ EVITAR: Dados insuficientes
public class SinistroCriadoEvent extends DomainEvent {
    private String id; // Muito pouco contexto
}
```

### **🔄 Versionamento de Eventos**

```java
// Versão 1 do evento
@JsonTypeName("SinistroCriado")
public class SinistroCriadoEventV1 extends DomainEvent {
    private String numeroSinistro;
    private String descricao;
}

// Versão 2 - Adicionando novos campos
@JsonTypeName("SinistroCriado")
public class SinistroCriadoEventV2 extends DomainEvent {
    private String numeroSinistro;
    private String descricao;
    private BigDecimal valorEstimado; // Novo campo
    private String tipoSinistro;      // Novo campo
    
    // Construtor que aceita V1 para migração
    public SinistroCriadoEventV2(SinistroCriadoEventV1 v1) {
        super(v1.getAggregateId(), v1.getVersion());
        this.numeroSinistro = v1.getNumeroSinistro();
        this.descricao = v1.getDescricao();
        this.valorEstimado = BigDecimal.ZERO; // Valor padrão
        this.tipoSinistro = "GENERICO";       // Valor padrão
    }
}
```

---

## 🎯 **EXERCÍCIOS PRÁTICOS**

### **📝 Exercício 1: Criar Evento Customizado**
Implemente um evento `SeguradoAtualizadoEvent` que:
- Herde de `DomainEvent`
- Contenha dados do segurado (nome, email, telefone)
- Tenha factory methods para diferentes tipos de atualização
- Inclua validação de dados obrigatórios

### **📝 Exercício 2: Handler Básico**
Crie um handler para `SeguradoAtualizadoEvent` que:
- Implemente a interface `EventHandler`
- Processe diferentes tipos de atualização
- Tenha logs apropriados
- Configure timeout e retry adequados

### **📝 Exercício 3: Análise de Código**
Analise o código existente em:
- `SinistroEvent.java`
- `SinistroEventHandler.java`
- Identifique padrões e boas práticas utilizadas

---

## 🔗 **PRÓXIMOS PASSOS**

Na **Parte 2** do Event Bus, abordaremos:
- **Implementações** do Event Bus (Simple e Kafka)
- **Registro automático** de handlers
- **Configuração** e propriedades
- **Tratamento de erros** e retry

---

## 📚 **REFERÊNCIAS**

### **📖 Documentação Técnica**
- [Domain Events Pattern](https://martinfowler.com/eaaDev/DomainEvent.html)
- [Event-Driven Architecture](https://microservices.io/patterns/data/event-driven-architecture.html)
- [Spring Events](https://docs.spring.io/spring-framework/docs/current/reference/html/core.html#context-functionality-events)

### **🔧 Código de Referência**
- `EventBus.java` - Interface principal
- `DomainEvent.java` - Classe base de eventos
- `EventHandler.java` - Interface para handlers
- `SinistroEvent.java` - Exemplo de evento

---

**📘 Capítulo:** 06 - Event Bus - Parte 1  
**⏱️ Tempo Estimado:** 45 minutos  
**🎯 Próximo:** [06 - Event Bus - Parte 2](./06-event-bus-parte-2.md)  
**📋 Checklist:** Fundamentos ✅ | Arquitetura ✅ | Domain Events ✅ | Handlers ✅