# 📡 ETAPA 04: IMPLEMENTAÇÃO DE EVENTOS
## Event Bus Integration - Processamento Assíncrono

### 🎯 **OBJETIVO DA ETAPA**

Implementar a integração com o Event Bus para processamento assíncrono de eventos, garantindo comunicação confiável entre bounded contexts, tratamento de falhas e eventual consistency.

**⏱️ Duração Estimada:** 2-4 horas  
**👥 Participantes:** Desenvolvedor + Tech Lead  
**📋 Pré-requisitos:** Etapa 03 concluída e aprovada

---

## 📋 **CHECKLIST DE IMPLEMENTAÇÃO**

### **📡 1. INTEGRAÇÃO COM EVENT BUS**

#### **🔧 Configuração do Event Bus:**
```yaml
# Verificar configuração em application.yml
event-bus:
  enabled: true
  type: simple # ou kafka para produção
  thread-pool:
    core-size: 10
    max-size: 50
    queue-capacity: 1000
  retry:
    max-attempts: 3
    initial-delay-ms: 1000
    max-delay-ms: 10000
    backoff-multiplier: 2.0
  monitoring:
    enabled: true
    detailed-logging: true
```

#### **📦 Registro de Event Handlers:**
- [ ] **Event handlers** anotados com `@Component`
- [ ] **Implementação** da interface `EventHandler<T>` completa
- [ ] **Método getEventType()** retornando classe correta
- [ ] **Auto-registro** no Event Bus funcionando
- [ ] **Logs de inicialização** confirmando registro

#### **✅ Validação da Integração:**
```java
@SpringBootTest
class EventBusIntegrationTest {
    
    @Autowired
    private EventBus eventBus;
    
    @Test
    void devePublicarEventoComSucesso() {
        // Given
        var event = [Dominio][Acao]Event.create(
            "test-aggregate-id",
            1L,
            "test-data"
        );
        
        // When & Then
        assertDoesNotThrow(() -> eventBus.publish(event));
    }
}
```

---

### **🔄 2. IMPLEMENTAÇÃO DE EVENT HANDLERS**

#### **📡 Event Handler Base:**
```java
@Component
@Slf4j
public class [Dominio][Acao]EventHandler implements EventHandler<[Dominio][Acao]Event> {
    
    private final [ServicoIntegracao] [servicoIntegracao];
    private final MeterRegistry meterRegistry;
    private final ApplicationEventPublisher eventPublisher;
    
    // ========== CONSTRUTOR ==========
    public [Dominio][Acao]EventHandler(
            [ServicoIntegracao] [servicoIntegracao],
            MeterRegistry meterRegistry,
            ApplicationEventPublisher eventPublisher) {
        this.[servicoIntegracao] = [servicoIntegracao];
        this.meterRegistry = meterRegistry;
        this.eventPublisher = eventPublisher;
    }
    
    // ========== PROCESSAMENTO PRINCIPAL ==========
    @Override
    public void handle([Dominio][Acao]Event event) {
        Timer.Sample sample = Timer.start(meterRegistry);
        
        try {
            log.info("Processando evento: {} para agregado: {}", 
                    event.getClass().getSimpleName(), event.getAggregateId());
            
            // 1. Validar se deve processar o evento
            if (!shouldProcess(event)) {
                log.debug("Evento ignorado: {}", event);
                return;
            }
            
            // 2. Executar processamento específico
            processEvent(event);
            
            // 3. Publicar eventos de integração se necessário
            publishIntegrationEvents(event);
            
            // 4. Registrar métricas de sucesso
            recordSuccessMetrics(event);
            
            log.info("Evento processado com sucesso: {}", event);
            
        } catch (Exception e) {
            log.error("Erro ao processar evento: {}", event, e);
            recordFailureMetrics(event, e);
            
            // Re-lançar para trigger do retry mechanism
            throw new EventHandlingException(
                event, 
                this.getClass().getSimpleName(),
                "Erro no processamento do evento",
                e
            );
        } finally {
            sample.stop(Timer.builder("event.processing.time")
                .tag("event", getEventType().getSimpleName())
                .tag("handler", this.getClass().getSimpleName())
                .register(meterRegistry));
        }
    }
    
    // ========== MÉTODOS ESPECÍFICOS ==========
    private boolean shouldProcess([Dominio][Acao]Event event) {
        // Validações específicas para decidir se deve processar
        // Ex: verificar se já foi processado, filtros de negócio, etc.
        return true;
    }
    
    private void processEvent([Dominio][Acao]Event event) {
        // Implementar lógica específica do handler
        // Ex: atualizar cache, enviar notificação, integrar com sistema externo
        
        [servicoIntegracao].processar(
            event.getAggregateId(),
            event.get[Campo]()
        );
    }
    
    private void publishIntegrationEvents([Dominio][Acao]Event event) {
        // Publicar eventos de integração se necessário
        // Ex: eventos para outros bounded contexts
        
        var integrationEvent = [Dominio]IntegrationEvent.builder()
            .sourceAggregateId(event.getAggregateId())
            .eventType(event.getClass().getSimpleName())
            .timestamp(Instant.now())
            .data(Map.of(
                "[campo]", event.get[Campo]()
            ))
            .build();
            
        eventPublisher.publishEvent(integrationEvent);
    }
    
    private void recordSuccessMetrics([Dominio][Acao]Event event) {
        meterRegistry.counter("event.processed.success",
            "event", getEventType().getSimpleName(),
            "handler", this.getClass().getSimpleName())
            .increment();
    }
    
    private void recordFailureMetrics([Dominio][Acao]Event event, Exception e) {
        meterRegistry.counter("event.processed.failure",
            "event", getEventType().getSimpleName(),
            "handler", this.getClass().getSimpleName(),
            "error_type", e.getClass().getSimpleName())
            .increment();
    }
    
    // ========== CONFIGURAÇÃO ==========
    @Override
    public Class<[Dominio][Acao]Event> getEventType() {
        return [Dominio][Acao]Event.class;
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Permite retry em caso de falha
    }
    
    @Override
    public int getMaxRetries() {
        return 3; // Máximo de tentativas
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Timeout para processamento
    }
    
    @Override
    public int getPriority() {
        return 100; // Prioridade de processamento
    }
    
    @Override
    public boolean supports([Dominio][Acao]Event event) {
        // Validações adicionais para suporte
        return event != null && event.getAggregateId() != null;
    }
}
```

#### **✅ Checklist de Event Handlers:**
- [ ] **Processamento assíncrono** configurado
- [ ] **Retry mechanism** implementado
- [ ] **Timeout** configurado adequadamente
- [ ] **Métricas** de sucesso e falha registradas
- [ ] **Logs estruturados** implementados

---

### **🔄 3. HANDLERS PARA DIFERENTES PROPÓSITOS**

#### **📊 Handler para Atualização de Projeções:**
```java
@Component
@Slf4j
public class [Dominio]ProjectionEventHandler implements EventHandler<[Dominio][Acao]Event> {
    
    private final [Dominio]ProjectionService projectionService;
    
    @Override
    public void handle([Dominio][Acao]Event event) {
        log.debug("Atualizando projeção para evento: {}", event);
        
        // Atualizar projeção baseada no evento
        projectionService.updateProjection(
            event.getAggregateId(),
            event.get[Campo](),
            event.getTimestamp()
        );
    }
    
    @Override
    public Class<[Dominio][Acao]Event> getEventType() {
        return [Dominio][Acao]Event.class;
    }
    
    @Override
    public int getPriority() {
        return 50; // Alta prioridade para projeções
    }
}
```

#### **📧 Handler para Notificações:**
```java
@Component
@Slf4j
public class [Dominio]NotificationEventHandler implements EventHandler<[Dominio][Acao]Event> {
    
    private final NotificationService notificationService;
    
    @Override
    public void handle([Dominio][Acao]Event event) {
        log.debug("Enviando notificação para evento: {}", event);
        
        // Enviar notificação baseada no evento
        notificationService.sendNotification(
            NotificationRequest.builder()
                .recipient(event.get[Usuario]())
                .template("[dominio]_[acao]")
                .data(Map.of(
                    "aggregateId", event.getAggregateId(),
                    "[campo]", event.get[Campo]()
                ))
                .build()
        );
    }
    
    @Override
    public Class<[Dominio][Acao]Event> getEventType() {
        return [Dominio][Acao]Event.class;
    }
    
    @Override
    public int getPriority() {
        return 200; // Baixa prioridade para notificações
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Retry para notificações
    }
}
```

#### **🔗 Handler para Integrações Externas:**
```java
@Component
@Slf4j
public class [Dominio]IntegrationEventHandler implements EventHandler<[Dominio][Acao]Event> {
    
    private final ExternalSystemClient externalSystemClient;
    private final RetryTemplate retryTemplate;
    
    @Override
    public void handle([Dominio][Acao]Event event) {
        log.debug("Integrando com sistema externo para evento: {}", event);
        
        // Integração com sistema externo usando retry template
        retryTemplate.execute(context -> {
            externalSystemClient.sendUpdate(
                ExternalUpdateRequest.builder()
                    .id(event.getAggregateId())
                    .data(event.get[Campo]())
                    .timestamp(event.getTimestamp())
                    .build()
            );
            return null;
        });
    }
    
    @Override
    public Class<[Dominio][Acao]Event> getEventType() {
        return [Dominio][Acao]Event.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 60; // Timeout maior para integrações externas
    }
    
    @Override
    public int getMaxRetries() {
        return 5; // Mais tentativas para integrações
    }
}
```

#### **✅ Checklist de Handlers Específicos:**
- [ ] **Handler de projeções** implementado
- [ ] **Handler de notificações** configurado
- [ ] **Handler de integrações** funcionando
- [ ] **Prioridades** definidas adequadamente
- [ ] **Timeouts específicos** configurados

---

### **🔄 4. TRATAMENTO DE FALHAS E RETRY**

#### **⚠️ Configuração de Retry:**
```java
@Configuration
public class EventRetryConfiguration {
    
    @Bean
    public RetryTemplate eventRetryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(3)
            .exponentialBackoff(1000, 2.0, 10000)
            .retryOn(EventHandlingException.class)
            .retryOn(TransientException.class)
            .build();
    }
    
    @Bean
    public RetryTemplate integrationRetryTemplate() {
        return RetryTemplate.builder()
            .maxAttempts(5)
            .exponentialBackoff(2000, 1.5, 30000)
            .retryOn(HttpServerErrorException.class)
            .retryOn(ResourceAccessException.class)
            .build();
    }
}
```

#### **💀 Dead Letter Queue Handler:**
```java
@Component
@Slf4j
public class [Dominio]DeadLetterHandler {
    
    private final DeadLetterService deadLetterService;
    private final AlertService alertService;
    
    @EventListener
    public void handleDeadLetterEvent(DeadLetterEvent deadLetterEvent) {
        log.error("Evento enviado para Dead Letter Queue: {}", deadLetterEvent);
        
        // Armazenar no Dead Letter Queue
        deadLetterService.store(
            DeadLetterRecord.builder()
                .originalEvent(deadLetterEvent.getOriginalEvent())
                .handler(deadLetterEvent.getHandlerClass())
                .attempts(deadLetterEvent.getAttempts())
                .lastError(deadLetterEvent.getLastError())
                .timestamp(Instant.now())
                .build()
        );
        
        // Enviar alerta
        alertService.sendAlert(
            Alert.builder()
                .severity(AlertSeverity.HIGH)
                .title("Evento em Dead Letter Queue")
                .description("Evento falhou após múltiplas tentativas")
                .metadata(Map.of(
                    "eventType", deadLetterEvent.getOriginalEvent().getClass().getSimpleName(),
                    "aggregateId", deadLetterEvent.getOriginalEvent().getAggregateId(),
                    "handler", deadLetterEvent.getHandlerClass()
                ))
                .build()
        );
    }
}
```

#### **🔄 Reprocessamento de Eventos:**
```java
@Service
@Slf4j
public class EventReprocessingService {
    
    private final EventBus eventBus;
    private final DeadLetterService deadLetterService;
    
    public void reprocessDeadLetterEvents(String eventType, int maxEvents) {
        log.info("Reprocessando eventos do tipo: {} (máximo: {})", eventType, maxEvents);
        
        List<DeadLetterRecord> records = deadLetterService.findByEventType(eventType, maxEvents);
        
        for (DeadLetterRecord record : records) {
            try {
                // Republicar evento
                eventBus.publish(record.getOriginalEvent());
                
                // Marcar como reprocessado
                deadLetterService.markAsReprocessed(record.getId());
                
                log.info("Evento reprocessado: {}", record.getId());
                
            } catch (Exception e) {
                log.error("Erro ao reprocessar evento: {}", record.getId(), e);
            }
        }
    }
}
```

#### **✅ Checklist de Tratamento de Falhas:**
- [ ] **Retry template** configurado
- [ ] **Dead Letter Queue** implementado
- [ ] **Alertas** para falhas configurados
- [ ] **Reprocessamento** de eventos implementado
- [ ] **Monitoramento** de falhas ativo

---

### **📊 5. MONITORAMENTO E MÉTRICAS**

#### **📈 Métricas de Eventos:**
```java
@Component
public class EventMetrics {
    
    private final MeterRegistry meterRegistry;
    
    public EventMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
        
        // Registrar gauges customizados
        Gauge.builder("events.queue.size")
            .tag("type", "processing")
            .register(meterRegistry, this, EventMetrics::getProcessingQueueSize);
            
        Gauge.builder("events.dead_letter.size")
            .register(meterRegistry, this, EventMetrics::getDeadLetterQueueSize);
    }
    
    public void recordEventPublished(String eventType) {
        meterRegistry.counter("events.published",
            "event_type", eventType)
            .increment();
    }
    
    public void recordEventProcessed(String eventType, String handler, boolean success) {
        meterRegistry.counter("events.processed",
            "event_type", eventType,
            "handler", handler,
            "status", success ? "success" : "failure")
            .increment();
    }
    
    public void recordProcessingTime(String eventType, String handler, long timeMs) {
        meterRegistry.timer("events.processing.time",
            "event_type", eventType,
            "handler", handler)
            .record(timeMs, TimeUnit.MILLISECONDS);
    }
    
    private double getProcessingQueueSize() {
        // Implementar lógica para obter tamanho da fila
        return 0.0;
    }
    
    private double getDeadLetterQueueSize() {
        // Implementar lógica para obter tamanho da DLQ
        return 0.0;
    }
}
```

#### **🏥 Health Check de Eventos:**
```java
@Component
public class EventBusHealthIndicator implements HealthIndicator {
    
    private final EventBus eventBus;
    private final DeadLetterService deadLetterService;
    
    @Override
    public Health health() {
        try {
            // Verificar se o Event Bus está funcionando
            boolean eventBusHealthy = eventBus.isHealthy();
            
            // Verificar tamanho da Dead Letter Queue
            long deadLetterCount = deadLetterService.count();
            boolean deadLetterHealthy = deadLetterCount < 100; // Threshold
            
            // Verificar estatísticas do Event Bus
            EventBusStatistics stats = eventBus.getStatistics();
            double errorRate = stats.getErrorRate();
            boolean errorRateHealthy = errorRate < 0.05; // 5% threshold
            
            if (eventBusHealthy && deadLetterHealthy && errorRateHealthy) {
                return Health.up()
                    .withDetail("eventBus", "healthy")
                    .withDetail("deadLetterQueue", deadLetterCount)
                    .withDetail("errorRate", String.format("%.2f%%", errorRate * 100))
                    .withDetail("totalProcessed", stats.getTotalProcessed())
                    .build();
            } else {
                return Health.down()
                    .withDetail("eventBus", eventBusHealthy ? "healthy" : "unhealthy")
                    .withDetail("deadLetterQueue", deadLetterHealthy ? "healthy" : "unhealthy")
                    .withDetail("errorRate", errorRateHealthy ? "healthy" : "unhealthy")
                    .build();
            }
        } catch (Exception e) {
            return Health.down()
                .withDetail("error", e.getMessage())
                .build();
        }
    }
}
```

#### **✅ Checklist de Monitoramento:**
- [ ] **Métricas customizadas** implementadas
- [ ] **Health checks** configurados
- [ ] **Dashboards** criados para eventos
- [ ] **Alertas** configurados para falhas
- [ ] **Logs estruturados** com correlation IDs

---

## ✅ **CHECKPOINT DE VALIDAÇÃO**

### **🎯 Critérios de Aprovação:**

#### **📡 Funcionalidade:**
- [ ] **Eventos** sendo publicados corretamente
- [ ] **Event handlers** processando eventos
- [ ] **Processamento assíncrono** funcionando
- [ ] **Retry mechanism** operacional
- [ ] **Dead Letter Queue** configurado

#### **🔄 Integração:**
- [ ] **Event Bus** integrado e funcionando
- [ ] **Handlers** registrados automaticamente
- [ ] **Prioridades** sendo respeitadas
- [ ] **Timeouts** configurados adequadamente
- [ ] **Correlation IDs** sendo propagados

#### **📊 Observabilidade:**
- [ ] **Métricas** sendo coletadas
- [ ] **Health checks** funcionais
- [ ] **Logs estruturados** implementados
- [ ] **Alertas** configurados
- [ ] **Dashboards** operacionais

#### **🧪 Testes:**
- [ ] **Testes unitários** dos event handlers
- [ ] **Testes de integração** com Event Bus
- [ ] **Testes de retry** funcionais
- [ ] **Testes de falha** implementados
- [ ] **Coverage** adequado (>80%)

---

## 🚨 **PONTOS DE ATENÇÃO**

### **⚠️ Armadilhas Comuns:**

#### **🚫 Handlers Síncronos Desnecessários:**
```java
// ❌ EVITAR: Handler síncrono para operações lentas
@Override
public boolean isAsync() {
    return false; // Bloqueia o processamento
}

// ✅ PREFERIR: Handler assíncrono
@Override
public boolean isAsync() {
    return true; // Processamento não-bloqueante
}
```

#### **🚫 Falta de Idempotência:**
```java
// ❌ EVITAR: Handler não idempotente
@Override
public void handle(Event event) {
    // Sempre executa, mesmo se já processado
    service.processEvent(event);
}

// ✅ PREFERIR: Handler idempotente
@Override
public void handle(Event event) {
    if (!alreadyProcessed(event)) {
        service.processEvent(event);
        markAsProcessed(event);
    }
}
```

#### **🚫 Tratamento de Erro Inadequado:**
```java
// ❌ EVITAR: Engolir exceções
@Override
public void handle(Event event) {
    try {
        service.processEvent(event);
    } catch (Exception e) {
        log.error("Erro", e); // Apenas log, não re-lança
    }
}

// ✅ PREFERIR: Re-lançar para retry
@Override
public void handle(Event event) {
    try {
        service.processEvent(event);
    } catch (Exception e) {
        log.error("Erro ao processar evento", e);
        throw new EventHandlingException(event, getClass().getSimpleName(), e);
    }
}
```

### **✅ Boas Práticas:**

#### **🎯 Design de Handlers:**
- **Sempre** implementar idempotência
- **Sempre** usar processamento assíncrono
- **Sempre** configurar timeouts adequados
- **Sempre** implementar retry para falhas transientes

#### **📡 Event Processing:**
- **Sempre** validar eventos antes de processar
- **Sempre** registrar métricas de processamento
- **Sempre** usar correlation IDs
- **Sempre** implementar Dead Letter Queue

---

## 🔄 **PRÓXIMOS PASSOS**

### **✅ Após Aprovação do Checkpoint:**
1. **[Etapa 05 - Implementação de Projeções](./05-implementacao-projecoes.md)**
2. Implementar projection handlers
3. Configurar query models
4. Otimizar consultas de leitura

### **📋 Preparação para Próxima Etapa:**
- [ ] **Projection patterns** estudados
- [ ] **Query optimization** compreendida
- [ ] **CQRS read side** revisado
- [ ] **Testes de eventos** passando

---

## 📚 **RECURSOS DE APOIO**

### **📖 Documentação de Referência:**
- **[Event Bus](../06-event-bus-README.md)**: Guia completo do Event Bus
- **[Projections](../07-projections-README.md)**: Preparação para próxima etapa
- **Código Existente**: `SinistroEventHandler` como referência

### **🛠️ Ferramentas de Monitoramento:**
- **Micrometer**: Métricas customizadas
- **Prometheus**: Coleta de métricas
- **Grafana**: Dashboards de monitoramento
- **ELK Stack**: Logs centralizados

### **🧪 Exemplos de Teste:**
- **EventHandlerTest**: Testes unitários
- **EventBusIntegrationTest**: Testes de integração
- **RetryMechanismTest**: Testes de retry

---

**📋 Checklist Total:** 50+ itens de validação  
**⏱️ Tempo Médio:** 2-4 horas  
**🎯 Resultado:** Event processing assíncrono funcional  
**✅ Próxima Etapa:** Implementação de Projeções