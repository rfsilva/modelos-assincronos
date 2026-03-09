package com.seguradora.hibrida.eventstore.replay.exception;

import java.util.UUID;

/**
 * Exceção base para erros relacionados ao sistema de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ReplayException extends RuntimeException {
    
    private final UUID replayId;
    private final String replayName;
    
    public ReplayException(String message) {
        super(message);
        this.replayId = null;
        this.replayName = null;
    }
    
    public ReplayException(String message, Throwable cause) {
        super(message, cause);
        this.replayId = null;
        this.replayName = null;
    }
    
    public ReplayException(UUID replayId, String replayName, String message) {
        super(String.format("Replay '%s' (%s): %s", replayName, replayId, message));
        this.replayId = replayId;
        this.replayName = replayName;
    }
    
    public ReplayException(UUID replayId, String replayName, String message, Throwable cause) {
        super(String.format("Replay '%s' (%s): %s", replayName, replayId, message), cause);
        this.replayId = replayId;
        this.replayName = replayName;
    }
    
    public UUID getReplayId() {
        return replayId;
    }
    
    public String getReplayName() {
        return replayName;
    }
    
    public boolean hasReplayContext() {
        return replayId != null;
    }
}