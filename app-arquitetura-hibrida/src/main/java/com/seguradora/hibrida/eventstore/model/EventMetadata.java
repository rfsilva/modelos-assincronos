package com.seguradora.hibrida.eventstore.model;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * Classe para armazenar metadados adicionais dos eventos.
 * 
 * Permite extensibilidade sem quebrar a estrutura base dos eventos,
 * facilitando debugging, auditoria e análise.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Data
@NoArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class EventMetadata extends HashMap<String, Object> {
    
    private static final long serialVersionUID = 1L;
    
    /**
     * Construtor com mapa inicial.
     */
    public EventMetadata(Map<String, Object> initialData) {
        super(initialData);
    }
    
    /**
     * Adiciona metadado de forma fluente.
     */
    public EventMetadata with(String key, Object value) {
        this.put(key, value);
        return this;
    }
    
    /**
     * Obtém valor tipado do metadado.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, Class<T> type) {
        Object value = this.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Obtém valor com default.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(String key, T defaultValue) {
        Object value = this.get(key);
        if (value != null) {
            try {
                return (T) value;
            } catch (ClassCastException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
}