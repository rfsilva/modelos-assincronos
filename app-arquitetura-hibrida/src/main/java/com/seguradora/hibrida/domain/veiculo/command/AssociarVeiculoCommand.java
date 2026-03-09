package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para associar um veículo a uma apólice de seguro.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Data
@Builder
public class AssociarVeiculoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do veículo não pode ser vazio")
    private String veiculoId;
    
    @NotBlank(message = "ID da apólice não pode ser vazio")
    private String apoliceId;
    
    @NotNull(message = "Data de início não pode ser nula")
    private LocalDate dataInicio;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private String operadorId;
}
