package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando para associar um veículo a uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class AssociarVeiculoCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    @NotBlank(message = "ID do veículo é obrigatório")
    private final String veiculoId;
    
    @NotBlank(message = "ID da apólice é obrigatório")
    private final String apoliceId;
    
    @NotNull(message = "Data de início é obrigatória")
    private final LocalDate dataInicio;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private final String operadorId;
    
    public AssociarVeiculoCommand(String veiculoId, String apoliceId, LocalDate dataInicio, 
                                 String operadorId, UUID correlationId, String userId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
        this.veiculoId = veiculoId;
        this.apoliceId = apoliceId;
        this.dataInicio = dataInicio;
        this.operadorId = operadorId;
    }
    
    @Override
    public UUID getCommandId() {
        return commandId;
    }
    
    @Override
    public Instant getTimestamp() {
        return timestamp;
    }
    
    @Override
    public UUID getCorrelationId() {
        return correlationId;
    }
    
    @Override
    public String getUserId() {
        return userId;
    }
    
    public String getVeiculoId() {
        return veiculoId;
    }
    
    public String getApoliceId() {
        return apoliceId;
    }
    
    public LocalDate getDataInicio() {
        return dataInicio;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AssociarVeiculoCommand that = (AssociarVeiculoCommand) obj;
        return Objects.equals(commandId, that.commandId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }
    
    @Override
    public String toString() {
        return String.format("AssociarVeiculoCommand{id=%s, veiculoId='%s', apoliceId='%s'}", 
                           commandId, veiculoId, apoliceId);
    }
}