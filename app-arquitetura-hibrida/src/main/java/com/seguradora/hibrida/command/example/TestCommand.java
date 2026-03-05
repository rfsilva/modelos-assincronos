package com.seguradora.hibrida.command.example;

import com.seguradora.hibrida.command.Command;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Comando de exemplo para demonstrar o uso do Command Bus.
 * 
 * <p>Este comando serve como exemplo de implementação e pode ser usado
 * para testes e demonstrações do sistema de comandos.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TestCommand implements Command {
    
    /**
     * Identificador único do comando.
     */
    @Builder.Default
    private UUID commandId = UUID.randomUUID();
    
    /**
     * Timestamp de criação do comando.
     */
    @Builder.Default
    private Instant timestamp = Instant.now();
    
    /**
     * ID de correlação para rastreamento.
     */
    private UUID correlationId;
    
    /**
     * ID do usuário que emitiu o comando.
     */
    private String userId;
    
    /**
     * Dados de exemplo do comando.
     */
    private String data;
    
    /**
     * Valor numérico de exemplo.
     */
    private Integer value;
    
    /**
     * Flag de exemplo.
     */
    @Builder.Default
    private Boolean active = true;
}