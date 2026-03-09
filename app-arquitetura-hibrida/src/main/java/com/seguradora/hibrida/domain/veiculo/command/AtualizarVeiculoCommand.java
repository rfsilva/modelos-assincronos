package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando para atualização de especificações de um veículo.
 * Permite alterar cor, tipo de combustível e cilindrada.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Data
@Builder
public class AtualizarVeiculoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do veículo não pode ser vazio")
    private String veiculoId;
    
    @NotNull(message = "Nova especificação não pode ser nula")
    @Valid
    private Especificacao novaEspecificacao;
    
    @NotBlank(message = "ID do operador não pode ser vazio")
    private String operadorId;
    
    @Min(value = 0, message = "Versão esperada deve ser maior ou igual a zero")
    private long versaoEsperada;
}
