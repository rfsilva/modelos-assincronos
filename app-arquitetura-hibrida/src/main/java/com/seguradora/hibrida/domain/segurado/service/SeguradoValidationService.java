package com.seguradora.hibrida.domain.segurado.service;

import com.seguradora.hibrida.domain.segurado.model.CPF;
import com.seguradora.hibrida.domain.segurado.model.Email;
import com.seguradora.hibrida.domain.segurado.model.Telefone;
import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

/**
 * Serviço para validações síncronas de segurado.
 * 
 * <p>Implementa validações em tempo real conforme requisitos da US010:
 * <ul>
 *   <li>Validação de unicidade de CPF</li>
 *   <li>Validação de unicidade de email</li>
 *   <li>Validação de telefone com DDD</li>
 *   <li>Cache de validações (TTL 1 hora)</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SeguradoValidationService {
    
    private final SeguradoQueryRepository seguradoQueryRepository;
    private final BureauCreditoService bureauCreditoService;
    
    /**
     * Valida se CPF é único no sistema.
     * 
     * @param cpf CPF a ser validado
     * @return true se CPF é único
     */
    @Cacheable(value = "cpf-validation", key = "#cpf", unless = "#result == false")
    public boolean isCpfUnico(String cpf) {
        try {
            CPF cpfValidado = CPF.of(cpf);
            boolean existe = seguradoQueryRepository.existsByCpf(cpfValidado.getNumero());
            
            log.debug("Validação de unicidade CPF: {} - Único: {}", 
                     cpfValidado.getFormatado(), !existe);
            
            return !existe;
        } catch (Exception e) {
            log.warn("Erro na validação de CPF: {}", cpf, e);
            return false;
        }
    }
    
    /**
     * Valida se email é único no sistema.
     * 
     * @param email Email a ser validado
     * @return true se email é único
     */
    @Cacheable(value = "email-validation", key = "#email", unless = "#result == false")
    public boolean isEmailUnico(String email) {
        try {
            Email emailValidado = Email.of(email);
            boolean existe = seguradoQueryRepository.existsByEmail(emailValidado.getEndereco());
            
            log.debug("Validação de unicidade email: {} - Único: {}", 
                     emailValidado.getEndereco(), !existe);
            
            return !existe;
        } catch (Exception e) {
            log.warn("Erro na validação de email: {}", email, e);
            return false;
        }
    }
    
    /**
     * Valida telefone com DDD brasileiro.
     * 
     * @param telefone Telefone a ser validado
     * @return true se telefone é válido
     */
    @Cacheable(value = "telefone-validation", key = "#telefone")
    public boolean isTelefoneValido(String telefone) {
        try {
            Telefone telefoneValidado = Telefone.of(telefone);
            
            log.debug("Validação de telefone: {} - Válido: true", 
                     telefoneValidado.getFormatado());
            
            return true;
        } catch (Exception e) {
            log.debug("Validação de telefone: {} - Válido: false - {}", 
                     telefone, e.getMessage());
            return false;
        }
    }
    
    /**
     * Valida CPF em bureaus de crédito.
     * 
     * @param cpf CPF a ser validado
     * @return resultado da validação
     */
    @Cacheable(value = "bureau-validation", key = "#cpf")
    public BureauValidationResult validarCpfBureau(String cpf) {
        try {
            CPF cpfValidado = CPF.of(cpf);
            return bureauCreditoService.validarCpf(cpfValidado.getNumero());
        } catch (Exception e) {
            log.warn("Erro na validação de bureau para CPF: {}", cpf, e);
            return BureauValidationResult.erro("Erro na consulta ao bureau de crédito");
        }
    }
    
    /**
     * Valida se segurado pode ser criado (validação completa).
     * 
     * @param cpf CPF do segurado
     * @param email Email do segurado
     * @return resultado da validação
     */
    public ValidationResult validarCriacaoSegurado(String cpf, String email) {
        ValidationResult result = new ValidationResult();
        
        // Validar unicidade de CPF
        if (!isCpfUnico(cpf)) {
            result.addError("CPF já cadastrado no sistema");
        }
        
        // Validar unicidade de email
        if (!isEmailUnico(email)) {
            result.addError("Email já cadastrado no sistema");
        }
        
        // Validar bureau de crédito
        BureauValidationResult bureauResult = validarCpfBureau(cpf);
        if (!bureauResult.isValido()) {
            result.addError("CPF com restrições: " + bureauResult.getMotivo());
        }
        
        log.info("Validação de criação de segurado - CPF: {} - Válido: {}", 
                CPF.of(cpf).getFormatado(), result.isValido());
        
        return result;
    }
    
    /**
     * Resultado de validação.
     */
    public static class ValidationResult {
        private boolean valido = true;
        private java.util.List<String> erros = new java.util.ArrayList<>();
        
        public void addError(String erro) {
            this.valido = false;
            this.erros.add(erro);
        }
        
        public boolean isValido() { return valido; }
        public java.util.List<String> getErros() { return erros; }
    }
}