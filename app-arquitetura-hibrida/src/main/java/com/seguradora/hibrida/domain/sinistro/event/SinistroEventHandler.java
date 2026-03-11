package com.seguradora.hibrida.domain.sinistro.event;

import com.seguradora.hibrida.eventbus.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler para eventos de sinistro.
 * 
 * <p>Este handler processa eventos de domínio relacionados a sinistros
 * com diferentes estratégias baseadas no tipo de evento.
 * 
 * <p>Funcionalidades implementadas:
 * <ul>
 *   <li>Processamento assíncrono de eventos</li>
 *   <li>Retry automático em caso de falha</li>
 *   <li>Timeout configurável por tipo de operação</li>
 *   <li>Logs estruturados para auditoria</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class SinistroEventHandler implements EventHandler<SinistroEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SinistroEventHandler.class);
    
    @Override
    public void handle(SinistroEvent event) {
        String correlationId = event.getCorrelationId() != null ? 
                event.getCorrelationId().toString() : "unknown";
        
        // Obter tipo do evento dos metadados
        String eventType = event.getMetadata().getValue("eventType", String.class);
        if (eventType == null) {
            eventType = event.getClass().getSimpleName();
        }
        
        log.info("Processing sinistro event: {} for aggregate {} [correlationId={}]", 
                eventType, event.getAggregateId(), correlationId);
        
        try {
            switch (eventType) {
                case "SinistroCriado":
                    handleSinistroCriado(event);
                    break;
                case "SinistroAtualizado":
                    handleSinistroAtualizado(event);
                    break;
                case "SinistroFinalizado":
                    handleSinistroFinalizado(event);
                    break;
                case "SinistroCancelado":
                    handleSinistroCancelado(event);
                    break;
                default:
                    log.warn("Unknown sinistro event type: {} [correlationId={}]", 
                            eventType, correlationId);
                    // Processar como evento genérico
                    handleGenericSinistroEvent(event);
            }
            
            log.info("Successfully processed sinistro event: {} [correlationId={}]", 
                    eventType, correlationId);
            
        } catch (Exception e) {
            log.error("Failed to process sinistro event: {} [correlationId={}]", 
                     eventType, correlationId, e);
            throw e; // Re-throw para acionar retry
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
    public boolean isRetryable() {
        return true; // Permitir retry em caso de falha
    }
    
    @Override
    public int getPriority() {
        return 100; // Prioridade normal
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Timeout de 30 segundos
    }
    
    @Override
    public boolean supports(SinistroEvent event) {
        // Verificar se o evento é suportado
        return event != null && 
               event.getAggregateId() != null && 
               event.getNumeroSinistro() != null;
    }
    
    /**
     * Processa evento de criação de sinistro.
     */
    private void handleSinistroCriado(SinistroEvent event) {
        log.info("Handling SinistroCriado for aggregate {} - Número: {}", 
                event.getAggregateId(), event.getNumeroSinistro());
        
        // Simular processamento
        simulateProcessing(500);
        
        // Aqui seria implementada a lógica específica:
        // - Atualizar projeções
        // - Enviar notificações
        // - Integrar com sistemas externos
        // - Etc.
        
        log.debug("SinistroCriado processed successfully for aggregate {} - Número: {}", 
                 event.getAggregateId(), event.getNumeroSinistro());
    }
    
    /**
     * Processa evento de atualização de sinistro.
     */
    private void handleSinistroAtualizado(SinistroEvent event) {
        log.info("Handling SinistroAtualizado for aggregate {} - Número: {} - Status: {}", 
                event.getAggregateId(), event.getNumeroSinistro(), event.getStatus());
        
        // Simular processamento
        simulateProcessing(300);
        
        // Lógica específica para atualização
        
        log.debug("SinistroAtualizado processed successfully for aggregate {} - Status: {}", 
                 event.getAggregateId(), event.getStatus());
    }
    
    /**
     * Processa evento de finalização de sinistro.
     */
    private void handleSinistroFinalizado(SinistroEvent event) {
        log.info("Handling SinistroFinalizado for aggregate {} - Número: {} - Valor: {}", 
                event.getAggregateId(), event.getNumeroSinistro(), event.getValorEstimado());
        
        // Simular processamento mais demorado
        simulateProcessing(1000);
        
        // Lógica específica para finalização
        
        log.debug("SinistroFinalizado processed successfully for aggregate {} - Valor: {}", 
                 event.getAggregateId(), event.getValorEstimado());
    }
    
    /**
     * Processa evento de cancelamento de sinistro.
     */
    private void handleSinistroCancelado(SinistroEvent event) {
        log.info("Handling SinistroCancelado for aggregate {} - Número: {}", 
                event.getAggregateId(), event.getNumeroSinistro());
        
        // Simular processamento
        simulateProcessing(200);
        
        // Lógica específica para cancelamento
        if (event instanceof SinistroEvent.SinistroCancelado) {
            SinistroEvent.SinistroCancelado cancelado = (SinistroEvent.SinistroCancelado) event;
            log.info("Motivo do cancelamento: {}", cancelado.getMotivoCancelamento());
        }
        
        log.debug("SinistroCancelado processed successfully for aggregate {}", event.getAggregateId());
    }
    
    /**
     * Processa evento genérico de sinistro.
     */
    private void handleGenericSinistroEvent(SinistroEvent event) {
        log.info("Handling generic sinistro event for aggregate {} - Número: {}", 
                event.getAggregateId(), event.getNumeroSinistro());
        
        // Processamento genérico
        simulateProcessing(100);
        
        log.debug("Generic sinistro event processed successfully for aggregate {}", event.getAggregateId());
    }
    
    /**
     * Simula processamento com delay.
     */
    private void simulateProcessing(long delayMs) {
        try {
            Thread.sleep(delayMs);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Processing interrupted", e);
        }
    }
}