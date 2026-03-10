package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;
import java.util.Objects;
import java.util.UUID;

/**
 * Comando para atualizar especificações de um veículo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class AtualizarVeiculoCommand implements Command {
    
    private final UUID commandId;
    private final Instant timestamp;
    private final UUID correlationId;
    private final String userId;
    
    @NotBlank(message = "ID do veículo é obrigatório")
    private final String veiculoId;
    
    @NotNull(message = "Nova especificação é obrigatória")
    private final Especificacao novaEspecificacao;
    
    @NotBlank(message = "ID do operador é obrigatório")
    private final String operadorId;
    
    private final Long versaoEsperada;
    
    public AtualizarVeiculoCommand(String veiculoId, Especificacao novaEspecificacao, 
                                  String operadorId, Long versaoEsperada, 
                                  UUID correlationId, String userId) {
        this.commandId = UUID.randomUUID();
        this.timestamp = Instant.now();
        this.correlationId = correlationId;
        this.userId = userId;
        this.veiculoId = veiculoId;
        this.novaEspecificacao = novaEspecificacao;
        this.operadorId = operadorId;
        this.versaoEsperada = versaoEsperada;
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
    
    public Especificacao getNovaEspecificacao() {
        return novaEspecificacao;
    }
    
    public String getOperadorId() {
        return operadorId;
    }
    
    public Long getVersaoEsperada() {
        return versaoEsperada;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        AtualizarVeiculoCommand that = (AtualizarVeiculoCommand) obj;
        return Objects.equals(commandId, that.commandId);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(commandId);
    }
    
    @Override
    public String toString() {
        return String.format("AtualizarVeiculoCommand{id=%s, veiculoId='%s'}", 
                           commandId, veiculoId);
    }
}