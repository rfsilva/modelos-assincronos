package com.seguradora.hibrida.domain.segurado.command;

import com.seguradora.hibrida.command.Command;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Command para atualizar dados de um segurado existente.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@Builder
public class AtualizarSeguradoCommand implements Command {
    
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    private UUID correlationId;
    private String userId;
    
    @NotBlank(message = "ID do segurado é obrigatório")
    private String seguradoId;
    
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
