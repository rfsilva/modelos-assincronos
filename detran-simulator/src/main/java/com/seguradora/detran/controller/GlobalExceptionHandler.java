package com.seguradora.detran.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(ConstraintViolationException e) {
        log.warn("❌ Erro de validação: {}", e.getMessage());
        
        String errors = e.getConstraintViolations()
            .stream()
            .map(ConstraintViolation::getMessage)
            .collect(Collectors.joining(", "));
        
        Map<String, Object> response = new HashMap<>();
        response.put("erro", "DADOS_INVALIDOS");
        response.put("message", "Parâmetros inválidos: " + errors);
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<Map<String, Object>> handleMissingParameter(MissingServletRequestParameterException e) {
        log.warn("❌ Parâmetro obrigatório ausente: {}", e.getParameterName());
        
        Map<String, Object> response = new HashMap<>();
        response.put("erro", "PARAMETRO_OBRIGATORIO");
        response.put("message", "Parâmetro obrigatório ausente: " + e.getParameterName());
        response.put("parametro", e.getParameterName());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("❌ Tipo de parâmetro inválido: {} = {}", e.getName(), e.getValue());
        
        Map<String, Object> response = new HashMap<>();
        response.put("erro", "TIPO_PARAMETRO_INVALIDO");
        response.put("message", "Tipo de parâmetro inválido: " + e.getName());
        response.put("parametro", e.getName());
        response.put("valor_recebido", e.getValue());
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.badRequest().body(response);
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception e) {
        log.error("💥 Erro interno não tratado", e);
        
        Map<String, Object> response = new HashMap<>();
        response.put("erro", "ERRO_INTERNO");
        response.put("message", "Erro interno do servidor");
        response.put("timestamp", System.currentTimeMillis());
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
}