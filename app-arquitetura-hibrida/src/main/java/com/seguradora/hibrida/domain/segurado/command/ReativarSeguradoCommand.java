package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Command para reativar um segurado previamente desativado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
public class ReativarSeguradoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do segurado é obrigatório")
    private String seguradoId;
    
    @NotBlank(message = "Motivo da reativação é obrigatório")
    private String motivo;
}
