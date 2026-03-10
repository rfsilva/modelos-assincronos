package com.seguradora.hibrida.domain.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * View para evolução temporal de métricas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Ponto de dados para evolução temporal")
public class EvolucaoTemporalView {
    
    @Schema(description = "Data do ponto de dados", example = "2024-12-19")
    @JsonFormat(pattern = "yyyy-MM-dd")
    private LocalDate data;
    
    @Schema(description = "Valor da métrica na data", example = "1250")
    private Long valor;
}