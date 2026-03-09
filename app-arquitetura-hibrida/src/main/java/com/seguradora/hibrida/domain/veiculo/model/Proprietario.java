package com.seguradora.hibrida.domain.veiculo.model;

import lombok.Value;

import java.io.Serializable;

/**
 * Value Object representando o proprietário de um veículo.
 * Encapsula informações sobre CPF/CNPJ, nome e tipo de pessoa.
 */
@Value
public class Proprietario implements Serializable {
    private static final long serialVersionUID = 1L;

    String cpfCnpj;
    String nome;
    TipoPessoa tipoPessoa;

    /**
     * Tipo de pessoa do proprietário.
     */
    public enum TipoPessoa {
        FISICA,
        JURIDICA
    }

    /**
     * Construtor com validações de negócio.
     *
     * @param cpfCnpj CPF ou CNPJ do proprietário
     * @param nome Nome completo ou razão social
     * @param tipoPessoa Tipo de pessoa (física ou jurídica)
     */
    public Proprietario(String cpfCnpj, String nome, TipoPessoa tipoPessoa) {
        this.tipoPessoa = validarTipoPessoa(tipoPessoa);
        this.cpfCnpj = validarCpfCnpj(cpfCnpj, tipoPessoa);
        this.nome = validarNome(nome);
    }

    private TipoPessoa validarTipoPessoa(TipoPessoa tipoPessoa) {
        if (tipoPessoa == null) {
            throw new IllegalArgumentException("Tipo de pessoa não pode ser nulo");
        }
        return tipoPessoa;
    }

    private String validarCpfCnpj(String cpfCnpj, TipoPessoa tipoPessoa) {
        if (cpfCnpj == null || cpfCnpj.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF/CNPJ não pode ser nulo ou vazio");
        }

        String documentoLimpo = cpfCnpj.replaceAll("[^0-9]", "");

        if (tipoPessoa == TipoPessoa.FISICA) {
            if (documentoLimpo.length() != 11) {
                throw new IllegalArgumentException("CPF deve ter 11 dígitos");
            }
            if (!validarCPF(documentoLimpo)) {
                throw new IllegalArgumentException("CPF inválido");
            }
        } else {
            if (documentoLimpo.length() != 14) {
                throw new IllegalArgumentException("CNPJ deve ter 14 dígitos");
            }
            if (!validarCNPJ(documentoLimpo)) {
                throw new IllegalArgumentException("CNPJ inválido");
            }
        }

        return documentoLimpo;
    }

    private String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }
        if (nome.trim().length() < 3) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 3 caracteres");
        }
        if (nome.length() > 200) {
            throw new IllegalArgumentException("Nome não pode ter mais de 200 caracteres");
        }
        return nome.trim();
    }

    /**
     * Valida CPF usando algoritmo oficial.
     */
    private boolean validarCPF(String cpf) {
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }

        try {
            // Calcula primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;

            if (Character.getNumericValue(cpf.charAt(9)) != primeiroDigito) {
                return false;
            }

            // Calcula segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;

            return Character.getNumericValue(cpf.charAt(10)) == segundoDigito;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Valida CNPJ usando algoritmo oficial.
     */
    private boolean validarCNPJ(String cnpj) {
        // Verifica se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }

        try {
            // Calcula primeiro dígito verificador
            int[] multiplicadores1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores1[i];
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;

            if (Character.getNumericValue(cnpj.charAt(12)) != primeiroDigito) {
                return false;
            }

            // Calcula segundo dígito verificador
            int[] multiplicadores2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * multiplicadores2[i];
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;

            return Character.getNumericValue(cnpj.charAt(13)) == segundoDigito;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Retorna CPF/CNPJ formatado.
     */
    public String getCpfCnpjFormatado() {
        if (tipoPessoa == TipoPessoa.FISICA) {
            return String.format("%s.%s.%s-%s",
                cpfCnpj.substring(0, 3),
                cpfCnpj.substring(3, 6),
                cpfCnpj.substring(6, 9),
                cpfCnpj.substring(9, 11)
            );
        } else {
            return String.format("%s.%s.%s/%s-%s",
                cpfCnpj.substring(0, 2),
                cpfCnpj.substring(2, 5),
                cpfCnpj.substring(5, 8),
                cpfCnpj.substring(8, 12),
                cpfCnpj.substring(12, 14)
            );
        }
    }

    /**
     * Verifica se é pessoa física.
     */
    public boolean isPessoaFisica() {
        return tipoPessoa == TipoPessoa.FISICA;
    }

    /**
     * Verifica se é pessoa jurídica.
     */
    public boolean isPessoaJuridica() {
        return tipoPessoa == TipoPessoa.JURIDICA;
    }

    @Override
    public String toString() {
        return String.format("Proprietario[%s=%s, nome=%s]",
            tipoPessoa == TipoPessoa.FISICA ? "CPF" : "CNPJ",
            getCpfCnpjFormatado(),
            nome
        );
    }
}
