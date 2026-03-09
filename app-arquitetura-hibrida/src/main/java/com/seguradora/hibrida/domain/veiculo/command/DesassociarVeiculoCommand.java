package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Comando para desassociar um veículo de uma apólice de seguro.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Data
@Builder
public class DesassociarVeiculoCommand implements Command {
    
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
    
    @NotNull(message = "Data de fim não pode ser nula")
    private LocalDate dataFim;
    
    @NotBlank(message = "Motivo não pode ser vazio")
    @Size(max = 500, message = "Motivo não pode ter mais de 500 caracteres")
    private String motivo;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private String operadorId;
}
