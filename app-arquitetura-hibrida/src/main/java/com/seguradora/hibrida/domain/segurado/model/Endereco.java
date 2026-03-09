package com.seguradora.hibrida.domain.segurado.model;

import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value Object representando Endereço do Segurado.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Data
@NoArgsConstructor
public class Endereco {
    
    private String logradouro;
    private String numero;
    private String complemento;
    private String bairro;
    private String cidade;
    private String estado;
    private String cep;
    
    public Endereco(String logradouro, String numero, String complemento,
                    String bairro, String cidade, String estado, String cep) {
        this.logradouro = logradouro;
        this.numero = numero;
        this.complemento = complemento;
        this.bairro = bairro;
        this.cidade = cidade;
        this.estado = estado;
        this.cep = cep;
    }
    
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();
        sb.append(logradouro).append(", ").append(numero);
        
        if (complemento != null && !complemento.isBlank()) {
            sb.append(" - ").append(complemento);
        }
        
        sb.append(" - ").append(bairro)
          .append(", ").append(cidade)
          .append("/").append(estado)
          .append(" - CEP: ").append(cep);
        
        return sb.toString();
    }
}
