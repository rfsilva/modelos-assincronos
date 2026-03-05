package com.seguradora.hibrida.command;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Representa o resultado da execução de um comando.
 * 
 * <p>Esta classe encapsula todas as informações sobre o resultado
 * da execução de um comando, incluindo sucesso/falha, dados retornados,
 * mensagens de erro e metadados de execução.</p>
 * 
 * <p><strong>Padrões de Uso:</strong></p>
 * <ul>
 *   <li>Usar métodos estáticos para criação (success/failure)</li>
 *   <li>Incluir dados relevantes no resultado de sucesso</li>
 *   <li>Fornecer mensagens de erro claras em falhas</li>
 *   <li>Adicionar metadados para debugging quando necessário</li>
 * </ul>
 * 
 * <p><strong>Exemplo de Uso:</strong></p>
 * <pre>{@code
 * // Sucesso com dados
 * return CommandResult.success(seguradoId, Map.of("version", 1L));
 * 
 * // Sucesso simples
 * return CommandResult.success();
 * 
 * // Falha com mensagem
 * return CommandResult.failure("CPF já cadastrado no sistema");
 * 
 * // Falha com exceção
 * return CommandResult.failure(exception);
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class CommandResult {
    
    /**
     * Indica se o comando foi executado com sucesso.
     */
    private boolean success;
    
    /**
     * Dados retornados pela execução do comando.
     * Geralmente contém ID do aggregate criado/modificado.
     */
    private Object data;
    
    /**
     * Mensagem de erro em caso de falha.
     */
    private String errorMessage;
    
    /**
     * Código de erro específico para categorização.
     */
    private String errorCode;
    
    /**
     * Timestamp da execução do comando.
     */
    @Builder.Default
    private Instant executedAt = Instant.now();
    
    /**
     * Tempo de execução em milissegundos.
     */
    private Long executionTimeMs;
    
    /**
     * ID de correlação para rastreamento.
     */
    private UUID correlationId;
    
    /**
     * Metadados adicionais sobre a execução.
     */
    private Map<String, Object> metadata;
    
    /**
     * Cria um resultado de sucesso sem dados.
     * 
     * @return CommandResult indicando sucesso
     */
    public static CommandResult success() {
        return CommandResult.builder()
                .success(true)
                .build();
    }
    
    /**
     * Cria um resultado de sucesso com dados.
     * 
     * @param data Dados a serem retornados
     * @return CommandResult indicando sucesso com dados
     */
    public static CommandResult success(Object data) {
        return CommandResult.builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * Cria um resultado de sucesso com dados e metadados.
     * 
     * @param data Dados a serem retornados
     * @param metadata Metadados adicionais
     * @return CommandResult indicando sucesso com dados e metadados
     */
    public static CommandResult success(Object data, Map<String, Object> metadata) {
        return CommandResult.builder()
                .success(true)
                .data(data)
                .metadata(metadata)
                .build();
    }
    
    /**
     * Cria um resultado de falha com mensagem de erro.
     * 
     * @param errorMessage Mensagem descrevendo o erro
     * @return CommandResult indicando falha
     */
    public static CommandResult failure(String errorMessage) {
        return CommandResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .build();
    }
    
    /**
     * Cria um resultado de falha com mensagem e código de erro.
     * 
     * @param errorMessage Mensagem descrevendo o erro
     * @param errorCode Código específico do erro
     * @return CommandResult indicando falha com código
     */
    public static CommandResult failure(String errorMessage, String errorCode) {
        return CommandResult.builder()
                .success(false)
                .errorMessage(errorMessage)
                .errorCode(errorCode)
                .build();
    }
    
    /**
     * Cria um resultado de falha a partir de uma exceção.
     * 
     * @param exception Exceção que causou a falha
     * @return CommandResult indicando falha
     */
    public static CommandResult failure(Exception exception) {
        return CommandResult.builder()
                .success(false)
                .errorMessage(exception.getMessage())
                .errorCode(exception.getClass().getSimpleName())
                .build();
    }
    
    /**
     * Cria um resultado de falha com exceção e metadados.
     * 
     * @param exception Exceção que causou a falha
     * @param metadata Metadados adicionais sobre o erro
     * @return CommandResult indicando falha com metadados
     */
    public static CommandResult failure(Exception exception, Map<String, Object> metadata) {
        return CommandResult.builder()
                .success(false)
                .errorMessage(exception.getMessage())
                .errorCode(exception.getClass().getSimpleName())
                .metadata(metadata)
                .build();
    }
    
    /**
     * Verifica se o resultado indica falha.
     * 
     * @return true se houve falha, false caso contrário
     */
    public boolean isFailure() {
        return !success;
    }
    
    /**
     * Adiciona correlation ID ao resultado.
     * 
     * @param correlationId ID de correlação
     * @return Esta instância para method chaining
     */
    public CommandResult withCorrelationId(UUID correlationId) {
        this.correlationId = correlationId;
        return this;
    }
    
    /**
     * Adiciona tempo de execução ao resultado.
     * 
     * @param executionTimeMs Tempo de execução em milissegundos
     * @return Esta instância para method chaining
     */
    public CommandResult withExecutionTime(Long executionTimeMs) {
        this.executionTimeMs = executionTimeMs;
        return this;
    }
    
    /**
     * Adiciona metadados ao resultado.
     * 
     * @param key Chave do metadado
     * @param value Valor do metadado
     * @return Esta instância para method chaining
     */
    public CommandResult withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}