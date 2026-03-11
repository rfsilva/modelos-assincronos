package com.seguradora.hibrida.domain.veiculo.validation;

/**
 * Validador de RENAVAM com algoritmo oficial de dígito verificador.
 *
 * <p>O RENAVAM (Registro Nacional de Veículos Automotores) possui 11 dígitos
 * onde o último é o dígito verificador calculado usando o algoritmo oficial.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class RenavamValidator {

    private static final String SEQUENCIA_VALIDACAO = "3298765432";

    /**
     * Valida um número de RENAVAM.
     *
     * @param renavam RENAVAM a ser validado
     * @return true se válido, false caso contrário
     */
    public static boolean isValid(String renavam) {
        if (renavam == null || renavam.isEmpty()) {
            return false;
        }

        // Remove caracteres não numéricos
        String renavamLimpo = renavam.replaceAll("[^0-9]", "");

        // RENAVAM deve ter 11 dígitos
        if (renavamLimpo.length() != 11) {
            return false;
        }

        try {
            // Extrai os primeiros 10 dígitos e o dígito verificador
            String renavamBase = renavamLimpo.substring(0, 10);
            int digitoVerificadorInformado = Integer.parseInt(renavamLimpo.substring(10, 11));

            // Calcula o dígito verificador
            int digitoVerificadorCalculado = calcularDigitoVerificador(renavamBase);

            // Compara os dígitos
            return digitoVerificadorInformado == digitoVerificadorCalculado;

        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     * Calcula o dígito verificador do RENAVAM.
     *
     * @param renavamBase Primeiros 10 dígitos do RENAVAM
     * @return Dígito verificador calculado
     */
    private static int calcularDigitoVerificador(String renavamBase) {
        int soma = 0;

        // Multiplica cada dígito pela sequência de validação
        for (int i = 0; i < 10; i++) {
            int digito = Character.getNumericValue(renavamBase.charAt(i));
            int multiplicador = Character.getNumericValue(SEQUENCIA_VALIDACAO.charAt(i));
            soma += digito * multiplicador;
        }

        // Calcula o módulo 11
        int resto = soma % 11;

        // Se o resto for 0 ou 1, o dígito verificador é 0, caso contrário é 11 - resto
        return (resto == 0 || resto == 1) ? 0 : 11 - resto;
    }

    /**
     * Formata um RENAVAM com separadores.
     *
     * @param renavam RENAVAM a ser formatado
     * @return RENAVAM formatado (ex: 12345678901 -> 1234567890-1)
     */
    public static String format(String renavam) {
        if (renavam == null || renavam.isEmpty()) {
            return "";
        }

        String renavamLimpo = renavam.replaceAll("[^0-9]", "");

        if (renavamLimpo.length() != 11) {
            return renavam; // Retorna sem formatação se inválido
        }

        return renavamLimpo.substring(0, 10) + "-" + renavamLimpo.substring(10);
    }
}
