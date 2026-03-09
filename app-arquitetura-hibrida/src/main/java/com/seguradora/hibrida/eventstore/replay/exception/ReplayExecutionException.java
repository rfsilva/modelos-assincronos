package com.seguradora.hibrida.eventstore.replay.exception;

import java.util.UUID;

/**
 * Exceção para erros durante a execução de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ReplayExecutionException extends ReplayException {
    
    private final String eventId;
    private final String handlerName;
    private final int attemptNumber;
    
    public ReplayExecutionException(UUID replayId, String replayName, String message) {
        super(replayId, replayName, message);
        this.eventId = null;
        this.handlerName = null;
        this.attemptNumber = 0;
    }
    
    public ReplayExecutionException(UUID replayId, String replayName, String message, Throwable cause) {
        super(replayId, replayName, message, cause);
        this.eventId = null;
        this.handlerName = null;
        this.attemptNumber = 0;
    }
    
    public ReplayExecutionException(UUID replayId, String replayName, String eventId, 
                                  String handlerName, int attemptNumber, String message, Throwable cause) {
        super(replayId, replayName, 
              String.format("Erro no evento %s (handler: %s, tentativa: %d): %s", 
                          eventId, handlerName, attemptNumber, message), cause);
        this.eventId = eventId;
        this.handlerName = handlerName;
        this.attemptNumber = attemptNumber;
    }
    
    public String getEventId() {
        return eventId;
    }
    
    public String getHandlerName() {
        return handlerName;
    }
    
    public int getAttemptNumber() {
        return attemptNumber;
    }
    
    public boolean hasEventContext() {
        return eventId != null;
    }
}