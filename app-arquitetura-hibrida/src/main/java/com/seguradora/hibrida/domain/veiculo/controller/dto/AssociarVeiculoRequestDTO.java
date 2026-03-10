package com.seguradora.hibrida.domain.veiculo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

/**
 * DTO para requisição de associação de veículo à apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados para associação de veículo à apólice")
public record AssociarVeiculoRequestDTO(
    
    @Schema(description = "ID da apólice", example = "POL-2024-001234")
    @NotBlank(message = "ID da apólice é obrigatório")
    String apoliceId,
    
    @Schema(description = "Data de início da cobertura", example = "2024-01-15")
    @NotNull(message = "Data de início é obrigatória")
    LocalDate dataInicio,
    
    @Schema(description = "ID do operador responsável", example = "operador123")
    @NotBlank(message = "ID do operador é obrigatório")
    String operadorId
) {
    
    /**
     * Verifica se a data de início é futura.
     */
    public boolean isDataInicioFutura() {
        return dataInicio.isAfter(LocalDate.now());
    }
    
    /**
     * Verifica se a data de início é hoje.
     */
    public boolean isDataInicioHoje() {
        return dataInicio.equals(LocalDate.now());
    }
    
    /**
     * Verifica se a data de início é válida (não muito no passado).
     */
    public boolean isDataInicioValida() {
        LocalDate limitePasado = LocalDate.now().minusDays(30);
        return !dataInicio.isBefore(limitePasado);
    }
}