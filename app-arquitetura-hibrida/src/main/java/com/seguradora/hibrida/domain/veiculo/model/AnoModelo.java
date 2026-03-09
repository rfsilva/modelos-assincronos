package com.seguradora.hibrida.domain.veiculo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.time.Year;

/**
 * Value Object representando o ano de fabricação e ano do modelo de um veículo.
 * Implementa validações específicas da indústria automotiva.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Getter
@EqualsAndHashCode
@ToString
public class AnoModelo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final int ANO_MINIMO = 1900;
    
    private final int anoFabricacao;
    private final int anoModelo;
    
    /**
     * Construtor privado para criar um AnoModelo válido.
     * 
     * @param anoFabricacao Ano de fabricação do veículo
     * @param anoModelo Ano do modelo do veículo
     */
    private AnoModelo(int anoFabricacao, int anoModelo) {
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
    }
    
    /**
     * Factory method para criar um AnoModelo.
     * 
     * @param anoFabricacao Ano de fabricação
     * @param anoModelo Ano do modelo
     * @return Instância de AnoModelo
     * @throws IllegalArgumentException se os anos forem inválidos
     */
    public static AnoModelo of(int anoFabricacao, int anoModelo) {
        validarAnoFabricacao(anoFabricacao);
        validarAnoModelo(anoModelo);
        validarRelacionamento(anoFabricacao, anoModelo);
        
        return new AnoModelo(anoFabricacao, anoModelo);
    }
    
    /**
     * Valida o ano de fabricação.
     * 
     * @param ano Ano de fabricação
     * @throws IllegalArgumentException se o ano for inválido
     */
    private static void validarAnoFabricacao(int ano) {
        int anoAtual = Year.now().getValue();
        int anoProximo = anoAtual + 1;
        
        if (ano < ANO_MINIMO) {
            throw new IllegalArgumentException(
                    "Ano de fabricação não pode ser anterior a " + ANO_MINIMO + ". Recebido: " + ano);
        }
        
        if (ano > anoProximo) {
            throw new IllegalArgumentException(
                    "Ano de fabricação não pode ser superior a " + anoProximo + ". Recebido: " + ano);
        }
    }
    
    /**
     * Valida o ano do modelo.
     * 
     * @param ano Ano do modelo
     * @throws IllegalArgumentException se o ano for inválido
     */
    private static void validarAnoModelo(int ano) {
        int anoAtual = Year.now().getValue();
        int anoProximo = anoAtual + 1;
        
        if (ano < ANO_MINIMO) {
            throw new IllegalArgumentException(
                    "Ano do modelo não pode ser anterior a " + ANO_MINIMO + ". Recebido: " + ano);
        }
        
        if (ano > anoProximo) {
            throw new IllegalArgumentException(
                    "Ano do modelo não pode ser superior a " + anoProximo + ". Recebido: " + ano);
        }
    }
    
    /**
     * Valida o relacionamento entre ano de fabricação e ano do modelo.
     * Na indústria automotiva, o ano do modelo pode ser:
     * - Igual ao ano de fabricação
     * - Um ano posterior ao ano de fabricação (veículo 0 km do próximo ano)
     * - Um ano anterior ao ano de fabricação (modelos do ano anterior fabricados no fim do ano)
     * 
     * @param anoFabricacao Ano de fabricação
     * @param anoModelo Ano do modelo
     * @throws IllegalArgumentException se o relacionamento for inválido
     */
    private static void validarRelacionamento(int anoFabricacao, int anoModelo) {
        int diferenca = Math.abs(anoModelo - anoFabricacao);
        
        if (diferenca > 1) {
            throw new IllegalArgumentException(
                    String.format(
                            "Diferença entre ano de fabricação (%d) e ano do modelo (%d) não pode ser superior a 1 ano",
                            anoFabricacao, anoModelo));
        }
        
        // Validação adicional: ano do modelo não pode ser anterior ao ano de fabricação em mais de 1 ano
        if (anoModelo < anoFabricacao - 1) {
            throw new IllegalArgumentException(
                    String.format(
                            "Ano do modelo (%d) não pode ser anterior ao ano de fabricação (%d) em mais de 1 ano",
                            anoModelo, anoFabricacao));
        }
    }
    
    /**
     * Retorna uma representação formatada do ano fabricação/modelo.
     * 
     * @return String no formato "fabricação/modelo" (ex: "2023/2024")
     */
    public String getFormatado() {
        return String.format("%d/%d", anoFabricacao, anoModelo);
    }
    
    /**
     * Verifica se o veículo é 0 km do próximo ano (ano modelo superior ao ano de fabricação).
     * 
     * @return true se o ano do modelo for superior ao ano de fabricação
     */
    public boolean isAnoProximo() {
        return anoModelo > anoFabricacao;
    }
    
    /**
     * Calcula a idade do veículo em anos (baseado no ano de fabricação).
     * 
     * @return Idade do veículo em anos
     */
    public int getIdade() {
        return Year.now().getValue() - anoFabricacao;
    }
}
