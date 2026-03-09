package com.seguradora.hibrida.domain.segurado.controller.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para requisição de reativação de segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReativarSeguradoRequestDTO {
    
    @NotBlank(message = "Motivo da reativação é obrigatório")
    private String motivo;
}
