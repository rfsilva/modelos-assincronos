package com.seguradora.hibrida.eventstore.replay;

import lombok.Builder;
import lombok.Data;
import lombok.extern.jackson.Jacksonized;

import java.time.Instant;

/**
 * Representa um erro ocorrido durante o replay de eventos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@Jacksonized
public class ReplayError {
    
    /**
     * Tipo do erro.
     */
    public enum ErrorType {
        EVENT_PROCESSING,    // Erro no processamento do evento
        HANDLER_EXECUTION,   // Erro na execução do handler
        SERIALIZATION,       // Erro de serialização/deserialização
        TIMEOUT,            // Timeout na execução
        VALIDATION,         // Erro de validação
        INFRASTRUCTURE,     // Erro de infraestrutura
        UNKNOWN            // Erro desconhecido
    }
    
    /**
     * Tipo do erro.
     */
    private ErrorType errorType;
    
    /**
     * Timestamp do erro.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * ID do evento que causou o erro.
     */
    private String eventId;
    
    /**
     * Tipo do evento que causou o erro.
     */
    private String eventType;
    
    /**
     * ID do aggregate relacionado.
     */
    private String aggregateId;
    
    /**
     * Nome do handler que falhou.
     */
    private String handlerName;
    
    /**
     * Mensagem de erro.
     */
    private String message;
    
    /**
     * Stack trace do erro.
     */
    private String stackTrace;
    
    /**
     * Número da tentativa (para erros com retry).
     */
    private int attemptNumber;
    
    /**
     * Indica se o erro é recuperável.
     */
    private boolean recoverable;
    
    /**
     * Cria erro de processamento de evento.
     * 
     * @param eventId ID do evento
     * @param eventType Tipo do evento
     * @param message Mensagem de erro
     * @return Erro criado
     */
    public static ReplayError eventProcessingError(String eventId, String eventType, String message) {
        return ReplayError.builder()
            .errorType(ErrorType.EVENT_PROCESSING)
            .eventId(eventId)
            .eventType(eventType)
            .message(message)
            .recoverable(true)
            .build();
    }
    
    /**
     * Cria erro de execução de handler.
     * 
     * @param handlerName Nome do handler
     * @param eventId ID do evento
     * @param exception Exceção ocorrida
     * @return Erro criado
     */
    public static ReplayError handlerExecutionError(String handlerName, String eventId, Exception exception) {
        return ReplayError.builder()
            .errorType(ErrorType.HANDLER_EXECUTION)
            .handlerName(handlerName)
            .eventId(eventId)
            .message(exception.getMessage())
            .stackTrace(getStackTrace(exception))
            .recoverable(true)
            .build();
    }
    
    /**
     * Cria erro de timeout.
     * 
     * @param handlerName Nome do handler
     * @param eventId ID do evento
     * @param timeoutSeconds Timeout configurado
     * @return Erro criado
     */
    public static ReplayError timeoutError(String handlerName, String eventId, int timeoutSeconds) {
        return ReplayError.builder()
            .errorType(ErrorType.TIMEOUT)
            .handlerName(handlerName)
            .eventId(eventId)
            .message(String.format("Handler timeout após %d segundos", timeoutSeconds))
            .recoverable(false)
            .build();
    }
    
    /**
     * Cria erro de infraestrutura.
     * 
     * @param message Mensagem de erro
     * @param exception Exceção ocorrida
     * @return Erro criado
     */
    public static ReplayError infrastructureError(String message, Exception exception) {
        return ReplayError.builder()
            .errorType(ErrorType.INFRASTRUCTURE)
            .message(message)
            .stackTrace(getStackTrace(exception))
            .recoverable(false)
            .build();
    }
    
    private static String getStackTrace(Exception exception) {
        if (exception == null) {
            return null;
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        return sw.toString();
    }
}