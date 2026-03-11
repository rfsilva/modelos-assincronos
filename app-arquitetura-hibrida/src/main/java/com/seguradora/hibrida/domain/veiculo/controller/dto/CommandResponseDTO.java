package com.seguradora.hibrida.domain.veiculo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;
import java.util.Map;

/**
 * DTO para resposta de comandos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Resposta padrão para comandos")
public record CommandResponseDTO(
    
    @Schema(description = "ID do aggregate afetado", example = "123e4567-e89b-12d3-a456-426614174000")
    String aggregateId,
    
    @Schema(description = "Mensagem de resultado", example = "Operação realizada com sucesso")
    String message,
    
    @Schema(description = "Nova versão do aggregate", example = "5")
    Long version,
    
    @Schema(description = "Timestamp da operação")
    Instant timestamp,
    
    @Schema(description = "Detalhes adicionais da operação")
    Map<String, Object> details
) {
    
    /**
     * Verifica se a operação foi bem-sucedida.
     */
    public boolean isSuccess() {
        return aggregateId != null && !message.toLowerCase().contains("erro") && 
               !message.toLowerCase().contains("falha");
    }
    
    /**
     * Verifica se há detalhes adicionais.
     */
    public boolean hasDetails() {
        return details != null && !details.isEmpty();
    }
    
    /**
     * Cria resposta de sucesso.
     */
    public static CommandResponseDTO success(String aggregateId, String message, Long version) {
        return new CommandResponseDTO(aggregateId, message, version, Instant.now(), null);
    }
    
    /**
     * Cria resposta de sucesso com detalhes.
     */
    public static CommandResponseDTO success(String aggregateId, String message, Long version, 
                                           Map<String, Object> details) {
        return new CommandResponseDTO(aggregateId, message, version, Instant.now(), details);
    }
    
    /**
     * Cria resposta de erro.
     */
    public static CommandResponseDTO error(String message) {
        return new CommandResponseDTO(null, message, null, Instant.now(), null);
    }
    
    /**
     * Cria resposta de erro com detalhes.
     */
    public static CommandResponseDTO error(String message, Map<String, Object> details) {
        return new CommandResponseDTO(null, message, null, Instant.now(), details);
    }
}