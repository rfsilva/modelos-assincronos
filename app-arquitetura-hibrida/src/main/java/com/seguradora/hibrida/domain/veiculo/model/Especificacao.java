package com.seguradora.hibrida.domain.veiculo.model;

import lombok.Value;

import java.io.Serializable;

/**
 * Value Object representando as especificações técnicas de um veículo.
 * Encapsula informações sobre cor, combustível, categoria e cilindrada.
 */
@Value
public class Especificacao implements Serializable {
    private static final long serialVersionUID = 1L;

    String cor;
    TipoCombustivel tipoCombustivel;
    CategoriaVeiculo categoria;
    Integer cilindrada;

    /**
     * Construtor com validações de negócio.
     *
     * @param cor Cor do veículo
     * @param tipoCombustivel Tipo de combustível
     * @param categoria Categoria do veículo
     * @param cilindrada Cilindrada do motor em cm³
     */
    public Especificacao(String cor, TipoCombustivel tipoCombustivel, CategoriaVeiculo categoria, Integer cilindrada) {
        this.cor = validarCor(cor);
        this.tipoCombustivel = validarTipoCombustivel(tipoCombustivel);
        this.categoria = validarCategoria(categoria);
        this.cilindrada = validarCilindrada(cilindrada, categoria);
    }

    private String validarCor(String cor) {
        if (cor == null || cor.trim().isEmpty()) {
            throw new IllegalArgumentException("Cor não pode ser nula ou vazia");
        }
        if (cor.length() > 50) {
            throw new IllegalArgumentException("Cor não pode ter mais de 50 caracteres");
        }
        return cor.trim().toUpperCase();
    }

    private TipoCombustivel validarTipoCombustivel(TipoCombustivel tipoCombustivel) {
        if (tipoCombustivel == null) {
            throw new IllegalArgumentException("Tipo de combustível não pode ser nulo");
        }
        return tipoCombustivel;
    }

    private CategoriaVeiculo validarCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) {
            throw new IllegalArgumentException("Categoria do veículo não pode ser nula");
        }
        return categoria;
    }

    private Integer validarCilindrada(Integer cilindrada, CategoriaVeiculo categoria) {
        if (cilindrada == null) {
            throw new IllegalArgumentException("Cilindrada não pode ser nula");
        }
        if (cilindrada <= 0) {
            throw new IllegalArgumentException("Cilindrada deve ser maior que zero");
        }
        
        // Validações específicas por categoria
        switch (categoria) {
            case MOTOCICLETA:
                if (cilindrada < 50 || cilindrada > 2500) {
                    throw new IllegalArgumentException(
                        "Cilindrada de motocicleta deve estar entre 50 e 2500 cm³"
                    );
                }
                break;
            case PASSEIO:
            case UTILITARIO:
                if (cilindrada < 800 || cilindrada > 8000) {
                    throw new IllegalArgumentException(
                        "Cilindrada de veículo de passeio/utilitário deve estar entre 800 e 8000 cm³"
                    );
                }
                break;
            case CAMINHAO:
                if (cilindrada < 3000 || cilindrada > 16000) {
                    throw new IllegalArgumentException(
                        "Cilindrada de caminhão deve estar entre 3000 e 16000 cm³"
                    );
                }
                break;
        }
        
        return cilindrada;
    }

    /**
     * Valida se a especificação é compatível com a categoria do veículo.
     *
     * @param categoria Categoria a ser validada
     * @return true se compatível
     */
    public boolean isCompativel(CategoriaVeiculo categoria) {
        return this.categoria == categoria;
    }

    /**
     * Verifica se o tipo de combustível é compatível com a categoria.
     *
     * @return true se compatível
     */
    public boolean isCombustivelCompativel() {
        // Motocicletas geralmente não usam diesel
        if (categoria == CategoriaVeiculo.MOTOCICLETA && 
            tipoCombustivel == TipoCombustivel.DIESEL) {
            return false;
        }
        
        // Caminhões geralmente não usam gasolina comum
        if (categoria == CategoriaVeiculo.CAMINHAO && 
            tipoCombustivel == TipoCombustivel.GASOLINA) {
            return false;
        }
        
        return true;
    }

    @Override
    public String toString() {
        return String.format("Especificacao[cor=%s, combustivel=%s, categoria=%s, cilindrada=%d cm³]",
            cor, tipoCombustivel, categoria, cilindrada);
    }
}
