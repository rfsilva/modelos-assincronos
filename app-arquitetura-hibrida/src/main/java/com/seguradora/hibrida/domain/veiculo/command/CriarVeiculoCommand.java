package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.veiculo.model.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando para criação de um novo veículo no sistema.
 * Contém todos os dados necessários para criar um veículo, incluindo
 * placa, RENAVAM, chassi, marca, modelo, especificações e proprietário.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Data
@Builder
public class CriarVeiculoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotNull(message = "Placa não pode ser nula")
    @Valid
    private Placa placa;
    
    @NotNull(message = "RENAVAM não pode ser nulo")
    @Valid
    private Renavam renavam;
    
    @NotNull(message = "Chassi não pode ser nulo")
    @Valid
    private Chassi chassi;
    
    @NotBlank(message = "Marca não pode ser vazia")
    @Size(min = 2, max = 100, message = "Marca deve ter entre 2 e 100 caracteres")
    private String marca;
    
    @NotBlank(message = "Modelo não pode ser vazio")
    @Size(min = 2, max = 100, message = "Modelo deve ter entre 2 e 100 caracteres")
    private String modelo;
    
    @NotNull(message = "Ano/Modelo não pode ser nulo")
    @Valid
    private AnoModelo anoModelo;
    
    @NotNull(message = "Especificação não pode ser nula")
    @Valid
    private Especificacao especificacao;
    
    @NotNull(message = "Proprietário não pode ser nulo")
    @Valid
    private Proprietario proprietario;
}
