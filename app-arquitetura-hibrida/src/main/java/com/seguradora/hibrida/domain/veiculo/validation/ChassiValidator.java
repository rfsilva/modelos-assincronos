package com.seguradora.hibrida.domain.veiculo.validation;

/**
 * Validador de Chassi/VIN (Vehicle Identification Number).
 *
 * <p>O VIN é um código alfanumérico de 17 caracteres usado para
 * identificar veículos de forma única em todo o mundo.
 *
 * <p>Regras de validação:
 * <ul>
 *   <li>Deve ter exatamente 17 caracteres</li>
 *   <li>Não pode conter as letras I, O ou Q (facilmente confundíveis com números)</li>
 *   <li>Apenas letras maiúsculas e números são permitidos</li>
 *   <li>Possui dígito verificador na posição 9 (para VINs norte-americanos)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ChassiValidator {

    private static final int VIN_LENGTH = 17;
    private static final String CARACTERES_PROIBIDOS = "IOQ";
    private static final String CARACTERES_VALIDOS = "ABCDEFGHJKLMNPRSTUVWXYZ0123456789";

    // Valores para cálculo do dígito verificador
    private static final int[] PESOS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

    // Mapeamento ISO 3779 para transliteração
    private static final String TRANSLITERACAO_CHARS = "0123456789ABCDEFGHJKLMNPRSTUVWXYZ";

    /**
     * Valida um número de chassi/VIN.
     *
     * @param chassi Chassi a ser validado
     * @return true se válido, false caso contrário
     */
    public static boolean isValid(String chassi) {
        if (chassi == null || chassi.isEmpty()) {
            return false;
        }

        // Converte para maiúsculas e remove espaços
        String chassiLimpo = chassi.toUpperCase().replaceAll("\\s+", "");

        // Verifica o tamanho
        if (chassiLimpo.length() != VIN_LENGTH) {
            return false;
        }

        // Verifica caracteres proibidos
        if (containsProhibitedCharacters(chassiLimpo)) {
            return false;
        }

        // Verifica se todos os caracteres são válidos
        if (!containsOnlyValidCharacters(chassiLimpo)) {
            return false;
        }

        // Validação do dígito verificador (para VINs norte-americanos)
        // Nota: Nem todos os países usam dígito verificador
        return validateCheckDigit(chassiLimpo);
    }

    /**
     * Verifica se contém caracteres proibidos (I, O, Q).
     */
    private static boolean containsProhibitedCharacters(String chassi) {
        for (char c : chassi.toCharArray()) {
            if (CARACTERES_PROIBIDOS.indexOf(c) >= 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * Verifica se contém apenas caracteres válidos.
     */
    private static boolean containsOnlyValidCharacters(String chassi) {
        for (char c : chassi.toCharArray()) {
            if (CARACTERES_VALIDOS.indexOf(c) < 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * Valida o dígito verificador (9ª posição) para VINs norte-americanos.
     *
     * <p>Nota: A validação do dígito verificador é obrigatória conforme ISO 3779.
     * O dígito pode ser 0-9 ou 'X' (representando 10).
     *
     * @param chassi Chassi a ser validado
     * @return true se o dígito verificador está correto
     */
    private static boolean validateCheckDigit(String chassi) {
        try {
            char digitoVerificador = chassi.charAt(8);

            int soma = 0;

            // Calcula a soma de todos os caracteres (exceto o dígito verificador na posição 8)
            for (int i = 0; i < VIN_LENGTH; i++) {
                if (i == 8) {
                    continue; // Pula o dígito verificador
                }

                char c = chassi.charAt(i);
                int valor = transliterar(c);
                soma += valor * PESOS[i];
            }

            // Calcula o dígito verificador esperado
            int calculado = calcularDigitoVerificador(soma);

            // O dígito pode ser um número ou 'X' (que representa 10)
            if (digitoVerificador == 'X') {
                return calculado == 10;
            } else if (Character.isDigit(digitoVerificador)) {
                return calculado == Character.getNumericValue(digitoVerificador);
            }

            // Se não for dígito nem X, invalido
            return false;

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Transliter um caractere para seu valor numérico conforme ISO 3779.
     */
    private static int transliterar(char c) {
        int index = TRANSLITERACAO_CHARS.indexOf(c);
        if (index >= 0 && index <= 9) {
            return index; // 0-9
        } else if (index >= 10) {
            return index - 9; // A=1, B=2, etc.
        }
        return 0;
    }

    /**
     * Calcula o dígito verificador.
     */
    private static int calcularDigitoVerificador(int soma) {
        int resto = soma % 11;
        return (resto == 10) ? 10 : resto; // 10 é representado por 'X'
    }

    /**
     * Formata um chassi com espaços para melhor legibilidade.
     *
     * @param chassi Chassi a ser formatado
     * @return Chassi formatado (ex: ABC123456789DEFGH -> ABC 123456 789 DEFGH)
     */
    public static String format(String chassi) {
        if (chassi == null || chassi.isEmpty()) {
            return "";
        }

        String chassiLimpo = chassi.toUpperCase().replaceAll("\\s+", "");

        if (chassiLimpo.length() != VIN_LENGTH) {
            return chassi; // Retorna sem formatação se inválido
        }

        // Formato: WMI(3) + VDS(6) + VIS(8) -> ABC 123456 78DEFGH9
        return chassiLimpo.substring(0, 3) + " " +
               chassiLimpo.substring(3, 9) + " " +
               chassiLimpo.substring(9, 11) + " " +
               chassiLimpo.substring(11);
    }

    /**
     * Extrai informações básicas do chassi.
     */
    public static VinInfo extractInfo(String chassi) {
        if (!isValid(chassi)) {
            return null;
        }

        String chassiLimpo = chassi.toUpperCase().replaceAll("\\s+", "");

        return new VinInfo(
            chassiLimpo.substring(0, 3),  // WMI - World Manufacturer Identifier
            chassiLimpo.substring(3, 9),  // VDS - Vehicle Descriptor Section
            chassiLimpo.substring(9, 17)  // VIS - Vehicle Identifier Section
        );
    }

    /**
     * Record para informações extraídas do VIN.
     */
    public record VinInfo(String wmi, String vds, String vis) {
        public String getManufacturerCode() {
            return wmi;
        }

        public String getVehicleDescriptor() {
            return vds;
        }

        public String getVehicleIdentifier() {
            return vis;
        }

        public char getCheckDigit() {
            // O check digit está na 9ª posição do VIN completo (índice 8)
            // que corresponde ao último caractere do VDS (índice 5 do VDS)
            return vds.charAt(5);
        }

        public int getModelYear() {
            // 10ª posição indica o ano do modelo
            char yearChar = vis.charAt(0);
            return decodeYearCharacter(yearChar);
        }

        private int decodeYearCharacter(char c) {
            // Mapeamento ISO 3779 para ano do modelo
            switch (c) {
                case 'A': return 2010;
                case 'B': return 2011;
                case 'C': return 2012;
                case 'D': return 2013;
                case 'E': return 2014;
                case 'F': return 2015;
                case 'G': return 2016;
                case 'H': return 2017;
                case 'J': return 2018;
                case 'K': return 2019;
                case 'L': return 2020;
                case 'M': return 2021;
                case 'N': return 2022;
                case 'P': return 2023;
                case 'R': return 2024;
                case 'S': return 2025;
                case 'T': return 2026;
                case 'V': return 2027;
                case 'W': return 2028;
                case 'X': return 2029;
                case 'Y': return 2000;
                case '1': return 2001;
                case '2': return 2002;
                case '3': return 2003;
                case '4': return 2004;
                case '5': return 2005;
                case '6': return 2006;
                case '7': return 2007;
                case '8': return 2008;
                case '9': return 2009;
                default: return 0;
            }
        }
    }
}
