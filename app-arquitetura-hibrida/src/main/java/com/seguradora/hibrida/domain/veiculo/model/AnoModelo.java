package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Value Object que representa o ano de fabricação e modelo de um veículo.
 * 
 * <p>Encapsula as regras específicas da indústria automotiva brasileira
 * para anos de fabricação e modelo, incluindo validações de relacionamento
 * e limites temporais.
 * 
 * <p>No Brasil, o ano modelo pode ser diferente do ano de fabricação,
 * seguindo regras específicas da indústria.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class AnoModelo implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int ANO_MINIMO = 1900;
    private static final int ANO_MAXIMO_FUTURO = 2; // Máximo 2 anos no futuro
    
    private final int anoFabricacao;
    private final int anoModelo;
    
    /**
     * Construtor privado.
     * 
     * @param anoFabricacao Ano de fabricação
     * @param anoModelo Ano modelo
     */
    private AnoModelo(int anoFabricacao, int anoModelo) {
        this.anoFabricacao = anoFabricacao;
        this.anoModelo = anoModelo;
    }
    
    /**
     * Cria uma instância de AnoModelo.
     * 
     * @param anoFabricacao Ano de fabricação do veículo
     * @param anoModelo Ano modelo do veículo
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
     * Cria uma instância com ano modelo igual ao de fabricação.
     * 
     * @param ano Ano de fabricação e modelo
     * @return Instância de AnoModelo
     */
    public static AnoModelo of(int ano) {
        return of(ano, ano);
    }
    
    /**
     * Retorna o ano de fabricação.
     * 
     * @return Ano de fabricação
     */
    public int getAnoFabricacao() {
        return anoFabricacao;
    }
    
    /**
     * Retorna o ano modelo.
     * 
     * @return Ano modelo
     */
    public int getAnoModelo() {
        return anoModelo;
    }
    
    /**
     * Retorna representação formatada dos anos.
     * 
     * @return String no formato "Fabricação/Modelo" (ex: "2020/2021")
     */
    public String getFormatado() {
        if (anoFabricacao == anoModelo) {
            return String.valueOf(anoFabricacao);
        }
        return String.format("%d/%d", anoFabricacao, anoModelo);
    }
    
    /**
     * Verifica se o ano modelo é diferente do ano de fabricação.
     * 
     * @return true se os anos são diferentes
     */
    public boolean isAnoProximo() {
        return anoModelo != anoFabricacao;
    }
    
    /**
     * Calcula a idade do veículo baseada no ano de fabricação.
     * 
     * @return Idade em anos
     */
    public int getIdade() {
        return LocalDate.now().getYear() - anoFabricacao;
    }
    
    /**
     * Verifica se o veículo é considerado novo (até 1 ano).
     * 
     * @return true se é veículo novo
     */
    public boolean isVeiculoNovo() {
        return getIdade() <= 1;
    }
    
    /**
     * Verifica se o veículo é considerado seminovo (2 a 5 anos).
     * 
     * @return true se é seminovo
     */
    public boolean isVeiculoSeminovo() {
        int idade = getIdade();
        return idade >= 2 && idade <= 5;
    }
    
    /**
     * Verifica se o veículo é considerado usado (mais de 5 anos).
     * 
     * @return true se é usado
     */
    public boolean isVeiculoUsado() {
        return getIdade() > 5;
    }
    
    /**
     * Verifica se o veículo é considerado antigo (mais de 20 anos).
     * 
     * @return true se é antigo
     */
    public boolean isVeiculoAntigo() {
        return getIdade() > 20;
    }
    
    /**
     * Retorna a categoria de idade do veículo.
     * 
     * @return Categoria de idade
     */
    public String getCategoriaIdade() {
        if (isVeiculoNovo()) {
            return "Novo";
        } else if (isVeiculoSeminovo()) {
            return "Seminovo";
        } else if (isVeiculoAntigo()) {
            return "Antigo";
        } else {
            return "Usado";
        }
    }
    
    /**
     * Calcula o fator de depreciação baseado na idade.
     * 
     * @return Fator de depreciação (0.0 a 1.0)
     */
    public double getFatorDepreciacao() {
        int idade = getIdade();
        
        if (idade <= 0) {
            return 1.0; // Sem depreciação
        } else if (idade <= 1) {
            return 0.85; // 15% no primeiro ano
        } else if (idade <= 3) {
            return 0.70; // 30% até 3 anos
        } else if (idade <= 5) {
            return 0.55; // 45% até 5 anos
        } else if (idade <= 10) {
            return 0.40; // 60% até 10 anos
        } else {
            return Math.max(0.15, 0.40 - (idade - 10) * 0.025); // Mínimo 15%
        }
    }
    
    /**
     * Verifica se o veículo está dentro da faixa de anos aceita para seguro.
     * 
     * @param idadeMaxima Idade máxima aceita
     * @return true se está dentro da faixa aceita
     */
    public boolean isAceitoParaSeguro(int idadeMaxima) {
        return getIdade() <= idadeMaxima;
    }
    
    /**
     * Valida o ano de fabricação.
     * 
     * @param ano Ano a ser validado
     * @throws IllegalArgumentException se o ano for inválido
     */
    private static void validarAnoFabricacao(int ano) {
        int anoAtual = LocalDate.now().getYear();
        
        if (ano < ANO_MINIMO) {
            throw new IllegalArgumentException(
                String.format("Ano de fabricação não pode ser anterior a %d. Fornecido: %d", 
                    ANO_MINIMO, ano));
        }
        
        if (ano > anoAtual + 1) {
            throw new IllegalArgumentException(
                String.format("Ano de fabricação não pode ser superior a %d. Fornecido: %d", 
                    anoAtual + 1, ano));
        }
    }
    
    /**
     * Valida o ano modelo.
     * 
     * @param ano Ano a ser validado
     * @throws IllegalArgumentException se o ano for inválido
     */
    private static void validarAnoModelo(int ano) {
        int anoAtual = LocalDate.now().getYear();
        
        if (ano < ANO_MINIMO) {
            throw new IllegalArgumentException(
                String.format("Ano modelo não pode ser anterior a %d. Fornecido: %d", 
                    ANO_MINIMO, ano));
        }
        
        if (ano > anoAtual + ANO_MAXIMO_FUTURO) {
            throw new IllegalArgumentException(
                String.format("Ano modelo não pode ser superior a %d. Fornecido: %d", 
                    anoAtual + ANO_MAXIMO_FUTURO, ano));
        }
    }
    
    /**
     * Valida o relacionamento entre ano de fabricação e modelo.
     * 
     * <p>Regras da indústria automotiva:
     * - Ano modelo pode ser igual ao de fabricação
     * - Ano modelo pode ser até 1 ano posterior ao de fabricação
     * - Ano modelo não pode ser anterior ao de fabricação
     * 
     * @param anoFabricacao Ano de fabricação
     * @param anoModelo Ano modelo
     * @throws IllegalArgumentException se o relacionamento for inválido
     */
    private static void validarRelacionamento(int anoFabricacao, int anoModelo) {
        if (anoModelo < anoFabricacao) {
            throw new IllegalArgumentException(
                String.format("Ano modelo (%d) não pode ser anterior ao ano de fabricação (%d)", 
                    anoModelo, anoFabricacao));
        }
        
        if (anoModelo > anoFabricacao + 1) {
            throw new IllegalArgumentException(
                String.format("Ano modelo (%d) não pode ser mais de 1 ano posterior ao de fabricação (%d)", 
                    anoModelo, anoFabricacao));
        }
    }
    
    /**
     * Gera um AnoModelo de exemplo para testes.
     * 
     * @return AnoModelo de exemplo
     */
    public static AnoModelo exemplo() {
        int anoAtual = LocalDate.now().getYear();
        return of(anoAtual - 2, anoAtual - 1);
    }
    
    /**
     * Verifica se uma combinação de anos é válida.
     * 
     * @param anoFabricacao Ano de fabricação
     * @param anoModelo Ano modelo
     * @return true se a combinação é válida
     */
    public static boolean isValido(int anoFabricacao, int anoModelo) {
        try {
            of(anoFabricacao, anoModelo);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        AnoModelo anoModelo = (AnoModelo) obj;
        return anoFabricacao == anoModelo.anoFabricacao &&
               this.anoModelo == anoModelo.anoModelo;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(anoFabricacao, anoModelo);
    }
    
    @Override
    public String toString() {
        return getFormatado();
    }
}