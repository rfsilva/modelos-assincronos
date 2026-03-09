package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import lombok.Builder;
import lombok.Data;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Comando para adicionar contato ao segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
public class AdicionarContatoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do segurado é obrigatório")
    private String seguradoId;
    
    @NotNull(message = "Tipo de contato é obrigatório")
    private TipoContato tipo;
    
    @NotBlank(message = "Valor do contato é obrigatório")
    private String valor;
    
    @Builder.Default
    private boolean principal = false;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private String operadorId;
    
    private Long versaoEsperada;
}