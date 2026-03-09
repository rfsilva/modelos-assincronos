package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Comando para remover contato do segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
public class RemoverContatoCommand implements Command {
    
    private UUID commandId = UUID.randomUUID();
    private Instant timestamp = Instant.now();
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do segurado é obrigatório")
    private String seguradoId;
    
    @NotNull(message = "Tipo de contato é obrigatório")
    private TipoContato tipo;
    
    @NotBlank(message = "Valor do contato é obrigatório")
    private String valor;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private String operadorId;
    
    private Long versaoEsperada;
    
    public RemoverContatoCommand(String seguradoId, TipoContato tipo, String valor, String operadorId) {
        this.seguradoId = seguradoId;
        this.tipo = tipo;
        this.valor = valor;
        this.operadorId = operadorId;
        this.userId = operadorId;
    }
}