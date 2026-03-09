package com.seguradora.hibrida.domain.segurado.controller.dto;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO para requisição de criação de segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriarSeguradoRequestDTO {
    
    @NotBlank(message = "CPF é obrigatório")
    @Pattern(regexp = "\\d{11}", message = "CPF deve conter 11 dígitos")
    private String cpf;
    
    @NotBlank(message = "Nome é obrigatório")
    @Size(min = 3, max = 100, message = "Nome deve ter entre 3 e 100 caracteres")
    private String nome;
    
    @NotBlank(message = "Email é obrigatório")
    @Email(message = "Email deve estar em formato válido")
    private String email;
    
    @NotBlank(message = "Telefone é obrigatório")
    @Pattern(regexp = "\\d{10,11}", message = "Telefone deve conter 10 ou 11 dígitos")
    private String telefone;
    
    @NotNull(message = "Data de nascimento é obrigatória")
    @Past(message = "Data de nascimento deve ser no passado")
    private LocalDate dataNascimento;
    
    @NotNull(message = "Endereço é obrigatório")
    @Valid
    private Endereco endereco;
}
