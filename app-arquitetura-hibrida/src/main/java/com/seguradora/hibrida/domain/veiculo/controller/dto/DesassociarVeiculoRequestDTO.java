package com.seguradora.hibrida.domain.veiculo.controller.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

/**
 * DTO para requisição de desassociação de veículo da apólice.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados para desassociação de veículo da apólice")
public record DesassociarVeiculoRequestDTO(
    
    @Schema(description = "ID da apólice", example = "POL-2024-001234")
    @NotBlank(message = "ID da apólice é obrigatório")
    String apoliceId,
    
    @Schema(description = "Data de fim da cobertura", example = "2024-12-31")
    @NotNull(message = "Data de fim é obrigatória")
    LocalDate dataFim,
    
    @Schema(description = "Motivo da desassociação", example = "Cancelamento da apólice por solicitação do cliente")
    @NotBlank(message = "Motivo é obrigatório")
    @Size(min = 10, max = 500, message = "Motivo deve ter entre 10 e 500 caracteres")
    String motivo,
    
    @Schema(description = "ID do operador responsável", example = "operador123")
    @NotBlank(message = "ID do operador é obrigatório")
    String operadorId
) {
    
    /**
     * Verifica se a data de fim é futura.
     */
    public boolean isDataFimFutura() {
        return dataFim.isAfter(LocalDate.now());
    }
    
    /**
     * Verifica se a data de fim é hoje.
     */
    public boolean isDataFimHoje() {
        return dataFim.equals(LocalDate.now());
    }
    
    /**
     * Verifica se a data de fim é válida (não muito no passado).
     */
    public boolean isDataFimValida() {
        LocalDate limitePasado = LocalDate.now().minusDays(7);
        return !dataFim.isBefore(limitePasado);
    }
    
    /**
     * Categoriza o motivo da desassociação.
     */
    public String categorizarMotivo() {
        String motivoLower = motivo.toLowerCase();
        
        if (motivoLower.contains("cancelamento") || motivoLower.contains("cancelar")) {
            return "CANCELAMENTO";
        } else if (motivoLower.contains("venda") || motivoLower.contains("vendido")) {
            return "VENDA_VEICULO";
        } else if (motivoLower.contains("sinistro") || motivoLower.contains("acidente")) {
            return "SINISTRO_TOTAL";
        } else if (motivoLower.contains("transferencia") || motivoLower.contains("transferir")) {
            return "TRANSFERENCIA";
        } else {
            return "OUTROS";
        }
    }
}