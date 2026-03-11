package com.seguradora.hibrida.domain.veiculo.controller.dto;

import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para requisição de atualização de veículo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados para atualização de veículo")
public record AtualizarVeiculoRequestDTO(
    
    @Schema(description = "Novas especificações técnicas do veículo")
    @NotNull(message = "Especificação é obrigatória")
    @Valid
    Especificacao especificacao,
    
    @Schema(description = "ID do operador responsável", example = "operador123")
    @NotBlank(message = "ID do operador é obrigatório")
    String operadorId,
    
    @Schema(description = "Versão esperada do aggregate para controle de concorrência", example = "3")
    Long versaoEsperada,
    
    @Schema(description = "Motivo da alteração", example = "Alteração de cor conforme solicitação do cliente")
    @Size(max = 500, message = "Motivo deve ter no máximo 500 caracteres")
    String motivo
) {
    
    /**
     * Verifica se tem controle de concorrência.
     */
    public boolean temControleVersao() {
        return versaoEsperada != null;
    }
    
    /**
     * Retorna motivo padrão se não informado.
     */
    public String getMotivoOuPadrao() {
        return motivo != null && !motivo.trim().isEmpty() ? 
            motivo : "Atualização de especificações";
    }
}