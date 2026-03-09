package com.seguradora.hibrida.domain.segurado.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

/**
 * Resultado da validação em bureaus de crédito.
 * 
 * <p>Encapsula o resultado da consulta a bureaus de crédito conforme US010.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BureauValidationResult {
    
    /**
     * Indica se o CPF é válido (sem restrições).
     */
    private boolean valido;
    
    /**
     * Motivo da restrição (se houver).
     */
    private String motivo;
    
    /**
     * Score de crédito (0-1000).
     */
    private Integer score;
    
    /**
     * Timestamp da consulta.
     */
    private Instant timestamp;
    
    /**
     * Dados adicionais do bureau.
     */
    private Map<String, Object> dadosAdicionais;
    
    /**
     * Cria resultado de sucesso.
     */
    public static BureauValidationResult sucesso(Integer score) {
        return new BureauValidationResult(
            true, 
            null, 
            score, 
            Instant.now(), 
            null
        );
    }
    
    /**
     * Cria resultado de falha com motivo.
     */
    public static BureauValidationResult falha(String motivo) {
        return new BureauValidationResult(
            false, 
            motivo, 
            null, 
            Instant.now(), 
            null
        );
    }
    
    /**
     * Cria resultado de erro na consulta.
     */
    public static BureauValidationResult erro(String motivo) {
        return new BureauValidationResult(
            false, 
            "Erro na consulta: " + motivo, 
            null, 
            Instant.now(), 
            null
        );
    }
    
    /**
     * Verifica se tem restrições.
     */
    public boolean temRestricoes() {
        return !valido;
    }
    
    /**
     * Verifica se o score é alto (>= 700).
     */
    public boolean isScoreAlto() {
        return score != null && score >= 700;
    }
    
    /**
     * Verifica se o score é médio (400-699).
     */
    public boolean isScoreMedio() {
        return score != null && score >= 400 && score < 700;
    }
    
    /**
     * Verifica se o score é baixo (< 400).
     */
    public boolean isScoreBaixo() {
        return score != null && score < 400;
    }
}