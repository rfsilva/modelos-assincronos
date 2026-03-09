package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.Instant;
import java.util.UUID;

/**
 * Comando para atualizar endereço do segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
public class AtualizarEnderecoCommand implements Command {
    
    @NotBlank(message = "ID do segurado é obrigatório")
    private String seguradoId;
    
    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private Endereco endereco;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private String operadorId;
    
    private Long versaoEsperada;
    
    // Campos obrigatórios da interface Command
    private final UUID commandId = UUID.randomUUID();
    private final Instant timestamp = Instant.now();
    private UUID correlationId;
    private String userId;
    
    public AtualizarEnderecoCommand(String seguradoId, Endereco endereco, String operadorId) {
        this.seguradoId = seguradoId;
        this.endereco = endereco;
        this.operadorId = operadorId;
        this.userId = operadorId;
    }
    
    public AtualizarEnderecoCommand(String seguradoId, Endereco endereco, String operadorId, Long versaoEsperada) {
        this(seguradoId, endereco, operadorId);
        this.versaoEsperada = versaoEsperada;
    }
}