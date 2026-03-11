package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando para desassociar um veículo de uma apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class DesassociarVeiculoCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    @NotBlank(message = "ID do veículo é obrigatório")
    private final String veiculoId;
    
    @NotBlank(message = "ID da apólice é obrigatório")
    private final String apoliceId;
    
    @NotNull(message = "Data de fim é obrigatória")
    private final LocalDate dataFim;
    
    @NotBlank(message = "Motivo é obrigatório")
    private final String motivo;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private final String operadorId;
    
    public DesassociarVeiculoCommand(String veiculoId, String apoliceId, LocalDate dataFim, 
                                    String motivo, String operadorId, 
                                    UUID correlationId, String userId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
        this.veiculoId = veiculoId;
        this.apoliceId = apoliceId;
        this.dataFim = dataFim;
        this.motivo = motivo;
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
    
    public LocalDate getDataFim() {
        return dataFim;
    }
    
    public String getMotivo() {
        return motivo;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        DesassociarVeiculoCommand that = (DesassociarVeiculoCommand) obj;
        return Objects.equals(commandId, that.commandId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }
    
    @Override
    public String toString() {
        return String.format("DesassociarVeiculoCommand{id=%s, veiculoId='%s', apoliceId='%s'}", 
                           commandId, veiculoId, apoliceId);
    }
}