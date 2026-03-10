package com.seguradora.hibrida.domain.veiculo.controller.dto;

import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import com.seguradora.hibrida.domain.veiculo.model.Proprietario;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

/**
 * DTO para requisição de criação de veículo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Schema(description = "Dados para criação de novo veículo")
public record CriarVeiculoRequestDTO(
    
    @Schema(description = "Placa do veículo", example = "ABC1234")
    @NotBlank(message = "Placa é obrigatória")
    @Pattern(regexp = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$", 
             message = "Placa deve estar no formato ABC1234 ou ABC1D23")
    String placa,
    
    @Schema(description = "RENAVAM do veículo", example = "12345678901")
    @NotBlank(message = "RENAVAM é obrigatório")
    @Pattern(regexp = "^[0-9]{11}$", message = "RENAVAM deve ter 11 dígitos")
    String renavam,
    
    @Schema(description = "Chassi do veículo", example = "1HGBH41JXMN109186")
    @NotBlank(message = "Chassi é obrigatório")
    @Pattern(regexp = "^[A-HJ-NPR-Z0-9]{17}$", message = "Chassi deve ter 17 caracteres alfanuméricos")
    String chassi,
    
    @Schema(description = "Marca do veículo", example = "Honda")
    @NotBlank(message = "Marca é obrigatória")
    @Size(min = 2, max = 50, message = "Marca deve ter entre 2 e 50 caracteres")
    String marca,
    
    @Schema(description = "Modelo do veículo", example = "Civic")
    @NotBlank(message = "Modelo é obrigatório")
    @Size(min = 2, max = 100, message = "Modelo deve ter entre 2 e 100 caracteres")
    String modelo,
    
    @Schema(description = "Ano de fabricação", example = "2020")
    @NotNull(message = "Ano de fabricação é obrigatório")
    @Min(value = 1900, message = "Ano de fabricação deve ser maior que 1900")
    @Max(value = 2030, message = "Ano de fabricação não pode ser maior que 2030")
    Integer anoFabricacao,
    
    @Schema(description = "Ano do modelo", example = "2021")
    @NotNull(message = "Ano do modelo é obrigatório")
    @Min(value = 1900, message = "Ano do modelo deve ser maior que 1900")
    @Max(value = 2030, message = "Ano do modelo não pode ser maior que 2030")
    Integer anoModelo,
    
    @Schema(description = "Especificações técnicas do veículo")
    @NotNull(message = "Especificação é obrigatória")
    @Valid
    Especificacao especificacao,
    
    @Schema(description = "Dados do proprietário")
    @NotNull(message = "Proprietário é obrigatório")
    @Valid
    Proprietario proprietario,
    
    @Schema(description = "ID do operador responsável", example = "operador123")
    @NotBlank(message = "ID do operador é obrigatório")
    String operadorId
) {
    
    /**
     * Valida se ano modelo não é mais de 1 ano posterior ao de fabricação.
     */
    public boolean isAnoModeloValido() {
        return anoModelo <= anoFabricacao + 1;
    }
    
    /**
     * Calcula a idade do veículo.
     */
    public int getIdade() {
        return java.time.Year.now().getValue() - anoFabricacao;
    }
    
    /**
     * Verifica se é veículo novo (até 3 anos).
     */
    public boolean isVeiculoNovo() {
        return getIdade() <= 3;
    }
}