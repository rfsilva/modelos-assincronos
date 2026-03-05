package com.seguradora.hibrida.command;

import java.time.Instant;
import java.util.UUID;

/**
 * Interface marker para todos os comandos do sistema.
 * 
 * <p>Esta interface define o contrato básico que todos os comandos devem seguir,
 * incluindo identificação única, timestamp de criação e correlation ID para rastreamento.</p>
 * 
 * <p><strong>Padrões de Implementação:</strong></p>
 * <ul>
 *   <li>Comandos devem ser imutáveis</li>
 *   <li>Usar Builder pattern para construção</li>
 *   <li>Implementar validações Bean Validation</li>
 *   <li>Incluir dados necessários para execução</li>
 * </ul>
 * 
 * <p><strong>Exemplo de Uso:</strong></p>
 * <pre>{@code
 * @Data
 * @Builder
 * @AllArgsConstructor
 * public class CriarSeguradoCommand implements Command {
 *     @NotBlank
 *     private final String cpf;
 *     
 *     @NotBlank
 *     private final String nome;
 *     
 *     @Email
 *     private final String email;
 *     
 *     private final UUID commandId = UUID.randomUUID();
 *     private final Instant timestamp = Instant.now();
 *     private final UUID correlationId;
 *     private final String userId;
 * }
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface Command {
    
    /**
     * Identificador único do comando.
     * 
     * @return UUID único para este comando
     */
    UUID getCommandId();
    
    /**
     * Timestamp de criação do comando.
     * 
     * @return Instant quando o comando foi criado
     */
    Instant getTimestamp();
    
    /**
     * ID de correlação para rastreamento de fluxos.
     * 
     * @return UUID de correlação ou null se não aplicável
     */
    UUID getCorrelationId();
    
    /**
     * ID do usuário que emitiu o comando.
     * 
     * @return ID do usuário ou null se comando do sistema
     */
    String getUserId();
    
    /**
     * Obtém o nome do tipo do comando.
     * 
     * <p>Por padrão, retorna o nome simples da classe.
     * Pode ser sobrescrito para customização.</p>
     * 
     * @return Nome do tipo do comando
     */
    default String getCommandType() {
        return this.getClass().getSimpleName();
    }
}