package com.seguradora.hibrida.domain.segurado.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para resposta de comandos.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CommandResponseDTO {
    
    private boolean success;
    private String message;
    private Object data;
}
