package com.seguradora.hibrida.query.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * DTO para view de lista de sinistros.
 * 
 * <p>Contém informações resumidas de sinistros otimizadas
 * para listagens e consultas paginadas.
 */
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
@Schema(description = "View resumida de sinistro para listagens")
public record SinistroListView(
        
        @Schema(description = "ID único do sinistro", example = "123e4567-e89b-12d3-a456-426614174000")
        UUID id,
        
        @Schema(description = "Protocolo único do sinistro", example = "SIN-2024-001234")
        String protocolo,
        
        @Schema(description = "CPF do segurado", example = "12345678901")
        String cpfSegurado,
        
        @Schema(description = "Nome do segurado", example = "João da Silva")
        String nomeSegurado,
        
        @Schema(description = "Placa do veículo", example = "ABC1234")
        String placa,
        
        @Schema(description = "Tipo do sinistro", example = "COLISAO")
        String tipoSinistro,
        
        @Schema(description = "Status atual do sinistro", example = "ABERTO")
        String status,
        
        @Schema(description = "Data e hora da ocorrência")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant dataOcorrencia,
        
        @Schema(description = "Data e hora de abertura do sinistro")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
        Instant dataAbertura,
        
        @Schema(description = "Operador responsável pelo sinistro", example = "João Silva")
        String operadorResponsavel,
        
        @Schema(description = "Valor estimado do sinistro", example = "15000.00")
        BigDecimal valorEstimado,
        
        @Schema(description = "Indica se a consulta ao DETRAN foi realizada")
        Boolean consultaDetranRealizada,
        
        @Schema(description = "Tags associadas ao sinistro")
        List<String> tags,
        
        @Schema(description = "Prioridade do sinistro", example = "NORMAL")
        String prioridade
) {}