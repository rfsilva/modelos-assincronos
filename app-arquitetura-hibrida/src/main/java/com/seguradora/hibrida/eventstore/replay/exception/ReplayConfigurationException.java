package com.seguradora.hibrida.eventstore.replay.exception;

import com.seguradora.hibrida.eventstore.replay.ReplayConfiguration;

/**
 * Exceção para erros de configuração de replay.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ReplayConfigurationException extends ReplayException {
    
    private final ReplayConfiguration configuration;
    
    public ReplayConfigurationException(String message, ReplayConfiguration configuration) {
        super(configuration != null ? configuration.getReplayId() : null,
              configuration != null ? configuration.getName() : null,
              message);
        this.configuration = configuration;
    }
    
    public ReplayConfigurationException(String message, ReplayConfiguration configuration, Throwable cause) {
        super(configuration != null ? configuration.getReplayId() : null,
              configuration != null ? configuration.getName() : null,
              message, cause);
        this.configuration = configuration;
    }
    
    public ReplayConfiguration getConfiguration() {
        return configuration;
    }
}