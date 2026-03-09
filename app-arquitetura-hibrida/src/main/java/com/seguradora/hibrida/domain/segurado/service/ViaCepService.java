package com.seguradora.hibrida.domain.segurado.service;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.HttpClientErrorException;

/**
 * Serviço para integração com a API ViaCEP.
 * 
 * <p>Implementa consulta de CEP conforme requisitos da US010:
 * <ul>
 *   <li>Integração com ViaCEP</li>
 *   <li>Cache de consultas (TTL 24 horas)</li>
 *   <li>Tratamento de erros e timeouts</li>
 *   <li>RestTemplate configurado com timeouts</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Service
public class ViaCepService {
    
    private final RestTemplate restTemplate;
    private static final String VIACEP_URL = "https://viacep.com.br/ws/{cep}/json/";
    
    public ViaCepService(@Qualifier("seguradoRestTemplate") RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }
    
    /**
     * Consulta CEP na API ViaCEP com cache.
     * 
     * @param cep CEP a ser consultado (8 dígitos)
     * @return Endereco com dados do CEP ou null se não encontrado
     */
    @Cacheable(value = "viacep-cache", key = "#cep", unless = "#result == null")
    public Endereco consultarCep(String cep) {
        if (!isValidCepFormat(cep)) {
            log.warn("Formato de CEP inválido: {}", cep);
            return null;
        }
        
        try {
            log.debug("Consultando CEP na ViaCEP: {}", cep);
            
            ResponseEntity<ViaCepResponse> response = restTemplate.getForEntity(
                VIACEP_URL, ViaCepResponse.class, cep
            );
            
            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                ViaCepResponse viaCepResponse = response.getBody();
                
                // ViaCEP retorna erro: true quando CEP não existe
                if (viaCepResponse.isErro()) {
                    log.warn("CEP não encontrado na ViaCEP: {}", cep);
                    return null;
                }
                
                Endereco endereco = convertToEndereco(viaCepResponse);
                log.info("CEP consultado com sucesso: {} - {}, {}", 
                        cep, endereco.getCidade(), endereco.getEstado());
                
                return endereco;
            }
            
            log.warn("Resposta inválida da ViaCEP para CEP: {}", cep);
            return null;
            
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("CEP não encontrado: {}", cep);
            } else {
                log.error("Erro HTTP ao consultar ViaCEP para CEP {}: {}", cep, e.getMessage());
            }
            return null;
            
        } catch (ResourceAccessException e) {
            log.error("Timeout ou erro de conectividade com ViaCEP para CEP {}: {}", cep, e.getMessage());
            return null;
            
        } catch (Exception e) {
            log.error("Erro inesperado ao consultar ViaCEP para CEP {}: {}", cep, e.getMessage(), e);
            return null;
        }
    }
    
    /**
     * Valida se o CEP tem formato válido (8 dígitos).
     */
    private boolean isValidCepFormat(String cep) {
        return cep != null && cep.matches("\\d{8}");
    }
    
    /**
     * Converte resposta da ViaCEP para objeto Endereco.
     */
    private Endereco convertToEndereco(ViaCepResponse response) {
        return new Endereco(
            response.getLogradouro() != null ? response.getLogradouro() : "",
            "", // número não vem da ViaCEP
            response.getComplemento() != null ? response.getComplemento() : "",
            response.getBairro() != null ? response.getBairro() : "",
            response.getLocalidade() != null ? response.getLocalidade() : "",
            response.getUf() != null ? response.getUf() : "",
            response.getCep() != null ? response.getCep().replaceAll("[^0-9]", "") : ""
        );
    }
    
    /**
     * DTO para resposta da API ViaCEP.
     */
    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ViaCepResponse {
        
        @JsonProperty("cep")
        private String cep;
        
        @JsonProperty("logradouro")
        private String logradouro;
        
        @JsonProperty("complemento")
        private String complemento;
        
        @JsonProperty("bairro")
        private String bairro;
        
        @JsonProperty("localidade")
        private String localidade;
        
        @JsonProperty("uf")
        private String uf;
        
        @JsonProperty("ibge")
        private String ibge;
        
        @JsonProperty("gia")
        private String gia;
        
        @JsonProperty("ddd")
        private String ddd;
        
        @JsonProperty("siafi")
        private String siafi;
        
        @JsonProperty("erro")
        private Boolean erro;
        
        public boolean isErro() {
            return erro != null && erro;
        }
    }
}