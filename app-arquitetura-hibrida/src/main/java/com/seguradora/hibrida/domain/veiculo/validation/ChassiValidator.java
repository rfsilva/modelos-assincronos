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
    private static final String TRANSLITERACAO = "0123456789ABCDEFGH..JKLMN.P.R..STUVWXYZ";
    private static final int[] PESOS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

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
     * @param chassi Chassi a ser validado
     * @return true se o dígito verificador está correto
     */
    private static boolean validateCheckDigit(String chassi) {
        try {
            int soma = 0;

            for (int i = 0; i < VIN_LENGTH; i++) {
                char c = chassi.charAt(i);
                int valor = transliterar(c);

                // Posição 9 (índice 8) é o dígito verificador
                if (i == 8) {
                    char digitoVerificador = chassi.charAt(8);
                    int calculado = calcularDigitoVerificador(soma);

                    // O dígito pode ser um número ou 'X' (que representa 10)
                    if (digitoVerificador == 'X') {
                        return calculado == 10;
                    } else if (Character.isDigit(digitoVerificador)) {
                        return calculado == Character.getNumericValue(digitoVerificador);
                    }
                    return true; // Se não for padrão americano, aceita qualquer valor
                }

                soma += valor * PESOS[i];
            }

            return true; // VIN válido (sem verificação de dígito para padrões não-americanos)

        } catch (Exception e) {
            return false;
        }
    }

    /**
     * Transliter um caractere para seu valor numérico.
     */
    private static int transliterar(char c) {
        int index = TRANSLITERACAO.indexOf(c);
        return index >= 0 ? (index % 10) : 0;
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
            return wmi.charAt(8); // 9ª posição (índice 8)
        }

        public int getModelYear() {
            // 10ª posição indica o ano do modelo
            char yearChar = vis.charAt(0);
            return decodeYearCharacter(yearChar);
        }

        private int decodeYearCharacter(char c) {
            // Implementação simplificada - anos 2000+
            // A=2010, B=2011, ..., Y=2000
            if (Character.isLetter(c)) {
                return 2010 + (c - 'A');
            } else if (Character.isDigit(c)) {
                return 2000 + Character.getNumericValue(c);
            }
            return 0;
        }
    }
}
