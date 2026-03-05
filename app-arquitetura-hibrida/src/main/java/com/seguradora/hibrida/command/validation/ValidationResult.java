package com.seguradora.hibrida.command.validation;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Resultado de uma validação de comando.
 * 
 * <p>Esta classe encapsula o resultado de validações customizadas,
 * incluindo status de validade, mensagens de erro e metadados
 * adicionais sobre a validação.</p>
 * 
 * <p><strong>Padrões de Uso:</strong></p>
 * <ul>
 *   <li>Usar métodos estáticos para criação (valid/invalid)</li>
 *   <li>Incluir mensagens descritivas em falhas</li>
 *   <li>Adicionar metadados quando necessário</li>
 *   <li>Combinar múltiplos resultados quando aplicável</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ValidationResult {
    
    /**
     * Indica se a validação passou.
     */
    private boolean valid;
    
    /**
     * Lista de mensagens de erro encontradas.
     */
    @Builder.Default
    private List<String> errorMessages = new ArrayList<>();
    
    /**
     * Código de erro específico para categorização.
     */
    private String errorCode;
    
    /**
     * Metadados adicionais sobre a validação.
     */
    private Map<String, Object> metadata;
    
    /**
     * Nome do validador que gerou este resultado.
     */
    private String validatorName;
    
    /**
     * Cria um resultado de validação válida.
     * 
     * @return ValidationResult indicando sucesso
     */
    public static ValidationResult valid() {
        return ValidationResult.builder()
                .valid(true)
                .build();
    }
    
    /**
     * Cria um resultado de validação válida com metadados.
     * 
     * @param metadata Metadados adicionais
     * @return ValidationResult indicando sucesso com metadados
     */
    public static ValidationResult valid(Map<String, Object> metadata) {
        return ValidationResult.builder()
                .valid(true)
                .metadata(metadata)
                .build();
    }
    
    /**
     * Cria um resultado de validação inválida com mensagem.
     * 
     * @param errorMessage Mensagem descrevendo o erro
     * @return ValidationResult indicando falha
     */
    public static ValidationResult invalid(String errorMessage) {
        List<String> messages = new ArrayList<>();
        messages.add(errorMessage);
        
        return ValidationResult.builder()
                .valid(false)
                .errorMessages(messages)
                .build();
    }
    
    /**
     * Cria um resultado de validação inválida com mensagem e código.
     * 
     * @param errorMessage Mensagem descrevendo o erro
     * @param errorCode Código específico do erro
     * @return ValidationResult indicando falha com código
     */
    public static ValidationResult invalid(String errorMessage, String errorCode) {
        List<String> messages = new ArrayList<>();
        messages.add(errorMessage);
        
        return ValidationResult.builder()
                .valid(false)
                .errorMessages(messages)
                .errorCode(errorCode)
                .build();
    }
    
    /**
     * Cria um resultado de validação inválida com múltiplas mensagens.
     * 
     * @param errorMessages Lista de mensagens de erro
     * @return ValidationResult indicando falha com múltiplas mensagens
     */
    public static ValidationResult invalid(List<String> errorMessages) {
        return ValidationResult.builder()
                .valid(false)
                .errorMessages(new ArrayList<>(errorMessages))
                .build();
    }
    
    /**
     * Cria um resultado de validação inválida completo.
     * 
     * @param errorMessages Lista de mensagens de erro
     * @param errorCode Código específico do erro
     * @param metadata Metadados adicionais
     * @return ValidationResult indicando falha completo
     */
    public static ValidationResult invalid(List<String> errorMessages, String errorCode, 
                                         Map<String, Object> metadata) {
        return ValidationResult.builder()
                .valid(false)
                .errorMessages(new ArrayList<>(errorMessages))
                .errorCode(errorCode)
                .metadata(metadata)
                .build();
    }
    
    /**
     * Verifica se a validação falhou.
     * 
     * @return true se inválida, false caso contrário
     */
    public boolean isInvalid() {
        return !valid;
    }
    
    /**
     * Verifica se existem mensagens de erro.
     * 
     * @return true se existem mensagens, false caso contrário
     */
    public boolean hasErrorMessages() {
        return errorMessages != null && !errorMessages.isEmpty();
    }
    
    /**
     * Obtém a primeira mensagem de erro.
     * 
     * @return Primeira mensagem ou null se não há mensagens
     */
    public String getFirstErrorMessage() {
        return hasErrorMessages() ? errorMessages.get(0) : null;
    }
    
    /**
     * Adiciona uma mensagem de erro ao resultado.
     * 
     * @param message Mensagem a ser adicionada
     * @return Esta instância para method chaining
     */
    public ValidationResult addErrorMessage(String message) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        errorMessages.add(message);
        this.valid = false;
        return this;
    }
    
    /**
     * Adiciona múltiplas mensagens de erro ao resultado.
     * 
     * @param messages Mensagens a serem adicionadas
     * @return Esta instância para method chaining
     */
    public ValidationResult addErrorMessages(List<String> messages) {
        if (errorMessages == null) {
            errorMessages = new ArrayList<>();
        }
        errorMessages.addAll(messages);
        this.valid = false;
        return this;
    }
    
    /**
     * Define o nome do validador que gerou este resultado.
     * 
     * @param validatorName Nome do validador
     * @return Esta instância para method chaining
     */
    public ValidationResult withValidatorName(String validatorName) {
        this.validatorName = validatorName;
        return this;
    }
    
    /**
     * Adiciona metadados ao resultado.
     * 
     * @param key Chave do metadado
     * @param value Valor do metadado
     * @return Esta instância para method chaining
     */
    public ValidationResult withMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new java.util.HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
    
    /**
     * Combina este resultado com outro resultado.
     * 
     * <p>O resultado combinado será válido apenas se ambos forem válidos.
     * Mensagens de erro são combinadas.</p>
     * 
     * @param other Outro resultado para combinar
     * @return Novo resultado combinado
     */
    public ValidationResult combine(ValidationResult other) {
        if (other == null) {
            return this;
        }
        
        boolean combinedValid = this.valid && other.valid;
        List<String> combinedMessages = new ArrayList<>();
        
        if (this.errorMessages != null) {
            combinedMessages.addAll(this.errorMessages);
        }
        if (other.errorMessages != null) {
            combinedMessages.addAll(other.errorMessages);
        }
        
        Map<String, Object> combinedMetadata = new java.util.HashMap<>();
        if (this.metadata != null) {
            combinedMetadata.putAll(this.metadata);
        }
        if (other.metadata != null) {
            combinedMetadata.putAll(other.metadata);
        }
        
        return ValidationResult.builder()
                .valid(combinedValid)
                .errorMessages(combinedMessages)
                .errorCode(this.errorCode != null ? this.errorCode : other.errorCode)
                .metadata(combinedMetadata.isEmpty() ? null : combinedMetadata)
                .build();
    }
}