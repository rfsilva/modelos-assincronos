package com.seguradora.hibrida.domain.segurado.service;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço para validação de CEP com integração ViaCEP.
 * 
 * <p>Implementa validação de CEP conforme requisitos da US009.
 * Por enquanto implementa validação básica, mas está preparado
 * para integração com ViaCEP na US010.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Service
public class CepValidationService {
    
    /**
     * Valida se o CEP é válido e retorna dados do endereço.
     * 
     * @param cep CEP a ser validado (8 dígitos)
     * @return Endereco com dados do CEP ou null se inválido
     */
    public Endereco validarCep(String cep) {
        if (cep == null || !cep.matches("\\d{8}")) {
            log.warn("CEP inválido: {}", cep);
            return null;
        }
        
        // TODO: Implementar integração com ViaCEP na US010
        // Por enquanto, implementação mock para validação básica
        return validarCepMock(cep);
    }
    
    /**
     * Verifica se o CEP existe (validação básica).
     * 
     * @param cep CEP a ser verificado
     * @return true se o CEP é válido
     */
    public boolean isCepValido(String cep) {
        if (cep == null || !cep.matches("\\d{8}")) {
            return false;
        }
        
        // Validação básica: rejeitar CEPs com todos os dígitos iguais
        if (cep.matches("(\\d)\\1{7}")) {
            return false;
        }
        
        // Validação básica: CEPs que começam com 0 são inválidos
        if (cep.startsWith("0")) {
            return false;
        }
        
        // TODO: Implementar validação real com ViaCEP na US010
        return true;
    }
    
    /**
     * Implementação mock para validação de CEP.
     * Será substituída por integração real na US010.
     */
    private Endereco validarCepMock(String cep) {
        // Mock de validação - alguns CEPs conhecidos
        switch (cep) {
            case "01310100":
                return new Endereco("Avenida Paulista", "", "", 
                                   "Bela Vista", "São Paulo", "SP", cep);
            case "20040020":
                return new Endereco("Rua da Assembleia", "", "", 
                                   "Centro", "Rio de Janeiro", "RJ", cep);
            case "70040010":
                return new Endereco("Esplanada dos Ministérios", "", "", 
                                   "Zona Cívico-Administrativa", "Brasília", "DF", cep);
            default:
                // Para outros CEPs, retorna estrutura básica se válido
                if (isCepValido(cep)) {
                    return new Endereco("", "", "", "", "", "", cep);
                }
                return null;
        }
    }
    
    /**
     * Formata CEP para exibição (XXXXX-XXX).
     */
    public String formatarCep(String cep) {
        if (cep == null || cep.length() != 8) {
            return cep;
        }
        return cep.substring(0, 5) + "-" + cep.substring(5);
    }
    
    /**
     * Remove formatação do CEP (apenas números).
     */
    public String limparCep(String cep) {
        if (cep == null) {
            return null;
        }
        return cep.replaceAll("[^0-9]", "");
    }
}