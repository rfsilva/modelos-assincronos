package com.seguradora.hibrida.eventbus.example;

import com.seguradora.hibrida.eventbus.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Handler de exemplo para TestEvent.
 * 
 * <p>Este handler demonstra como implementar um EventHandler
 * e será automaticamente descoberto e registrado pelo Event Bus.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class TestEventHandler implements EventHandler<TestEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(TestEventHandler.class);
    
    @Override
    public void handle(TestEvent event) {
        log.info("Processing TestEvent: {}", event);
        
        // Simular processamento baseado na prioridade
        try {
            if (event.getPriority() > 5) {
                // Eventos de alta prioridade processam mais rápido
                Thread.sleep(100);
            } else {
                // Eventos de baixa prioridade demoram mais
                Thread.sleep(500);
            }
            
            // Simular diferentes tipos de processamento baseado na categoria
            switch (event.getCategory()) {
                case "notification":
                    processNotification(event);
                    break;
                case "audit":
                    processAudit(event);
                    break;
                case "integration":
                    processIntegration(event);
                    break;
                default:
                    processDefault(event);
                    break;
            }
            
            log.info("TestEvent processed successfully: aggregateId={}, message={}", 
                    event.getAggregateId(), event.getMessage());
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Handler interrupted", e);
        }
    }
    
    @Override
    public Class<TestEvent> getEventType() {
        return TestEvent.class;
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Permite retry em caso de falha
    }
    
    @Override
    public int getPriority() {
        return 10; // Prioridade alta para handler de exemplo
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30; // Timeout de 30 segundos
    }
    
    @Override
    public boolean supports(TestEvent event) {
        // Aceitar todos os TestEvents, mas poderia ter lógica específica
        return event != null && event.getMessage() != null;
    }
    
    /**
     * Processa eventos de notificação.
     */
    private void processNotification(TestEvent event) {
        log.debug("Processing notification event: {}", event.getMessage());
        // Aqui seria implementada a lógica de notificação
        // Por exemplo: enviar email, SMS, push notification, etc.
    }
    
    /**
     * Processa eventos de auditoria.
     */
    private void processAudit(TestEvent event) {
        log.debug("Processing audit event: {}", event.getMessage());
        // Aqui seria implementada a lógica de auditoria
        // Por exemplo: salvar log de auditoria, enviar para sistema externo, etc.
    }
    
    /**
     * Processa eventos de integração.
     */
    private void processIntegration(TestEvent event) {
        log.debug("Processing integration event: {}", event.getMessage());
        // Aqui seria implementada a lógica de integração
        // Por exemplo: chamar API externa, atualizar sistema legado, etc.
    }
    
    /**
     * Processamento padrão para eventos não categorizados.
     */
    private void processDefault(TestEvent event) {
        log.debug("Processing default event: {}", event.getMessage());
        // Processamento genérico
    }
}