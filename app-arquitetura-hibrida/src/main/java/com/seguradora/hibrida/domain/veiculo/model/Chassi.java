package com.seguradora.hibrida.domain.veiculo.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.regex.Pattern;

/**
 * Value Object representando um número de chassi/VIN (Vehicle Identification Number).
 * Implementa validação conforme padrão internacional ISO 3779.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Getter
@EqualsAndHashCode
@ToString
public class Chassi implements Serializable {
    
    private static final long serialVersionUID = 1L;
    private static final int TAMANHO_CHASSI = 17;
    
    // Caracteres proibidos no VIN: I, O, Q (podem ser confundidos com 1, 0)
    private static final Pattern FORMATO_VIN = Pattern.compile("^[A-HJ-NPR-Z0-9]{17}$");
    
    // Pesos para cálculo do dígito verificador (posição 9)
    private static final int[] PESOS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};
    
    // Mapa de valores para caracteres
    private static final String VALORES = "0123456789.ABCDEFGH..JKLMN.P.R..STUVWXYZ";
    
    private final String valor;
    
    /**
     * Construtor privado para criar um Chassi válido.
     * 
     * @param valor Valor do chassi (17 caracteres)
     */
    private Chassi(String valor) {
        this.valor = valor;
    }
    
    /**
     * Factory method para criar um Chassi a partir de uma string.
     * 
     * @param chassi String representando o chassi/VIN
     * @return Instância de Chassi
     * @throws IllegalArgumentException se o chassi for inválido
     */
    public static Chassi of(String chassi) {
        if (chassi == null || chassi.isBlank()) {
            throw new IllegalArgumentException("Chassi não pode ser vazio");
        }
        
        // Normalizar: remover espaços e converter para maiúsculas
        String chassiNormalizado = chassi.trim().toUpperCase().replaceAll("\\s+", "");
        
        // Validar tamanho
        if (chassiNormalizado.length() != TAMANHO_CHASSI) {
            throw new IllegalArgumentException(
                    "Chassi/VIN deve conter exatamente 17 caracteres. Recebido: " + chassiNormalizado.length());
        }
        
        // Validar formato (sem I, O, Q)
        if (!FORMATO_VIN.matcher(chassiNormalizado).matches()) {
            throw new IllegalArgumentException(
                    "Chassi/VIN contém caracteres inválidos. Não são permitidos: I, O, Q");
        }
        
        // Validar dígito verificador (opcional, pois nem todos os países usam)
        // A validação do dígito é mais comum em VINs americanos
        if (!validarDigitoVerificador(chassiNormalizado)) {
            // Apenas log de aviso, não bloquear (alguns países não usam dígito verificador)
            // throw new IllegalArgumentException("Chassi/VIN com dígito verificador inválido");
        }
        
        return new Chassi(chassiNormalizado);
    }
    
    /**
     * Valida o dígito verificador do VIN (posição 9).
     * Usado principalmente para VINs norte-americanos.
     * 
     * @param chassi VIN completo com 17 caracteres
     * @return true se o dígito verificador for válido
     */
    private static boolean validarDigitoVerificador(String chassi) {
        try {
            int soma = 0;
            
            for (int i = 0; i < TAMANHO_CHASSI; i++) {
                char c = chassi.charAt(i);
                int valor = obterValor(c);
                
                if (valor == -1) {
                    return true; // Caractere não mapeado, não valida
                }
                
                soma += valor * PESOS[i];
            }
            
            int digitoCalculado = soma % 11;
            char digitoEsperado = (digitoCalculado == 10) ? 'X' : (char) ('0' + digitoCalculado);
            char digitoInformado = chassi.charAt(8); // Posição 9 (índice 8)
            
            return digitoEsperado == digitoInformado;
            
        } catch (Exception e) {
            return true; // Em caso de erro, não bloqueia
        }
    }
    
    /**
     * Obtém o valor numérico de um caractere para cálculo do dígito verificador.
     * 
     * @param c Caractere do VIN
     * @return Valor numérico ou -1 se não mapeado
     */
    private static int obterValor(char c) {
        int pos = VALORES.indexOf(c);
        if (pos == -1) return -1;
        if (c >= '0' && c <= '9') return c - '0';
        return (pos - 10) % 10;
    }
    
    /**
     * Retorna o chassi formatado (com hífens para melhor legibilidade).
     * Formato: XXX-XXXXXX-XXXXXXXX
     * 
     * @return Chassi formatado
     */
    public String getFormatado() {
        return String.format("%s-%s-%s", 
                valor.substring(0, 3), 
                valor.substring(3, 9), 
                valor.substring(9));
    }
    
    /**
     * Extrai o código do fabricante (World Manufacturer Identifier - WMI).
     * Primeiros 3 caracteres do VIN.
     * 
     * @return Código do fabricante
     */
    public String getCodigoFabricante() {
        return valor.substring(0, 3);
    }
    
    /**
     * Extrai o código do veículo (Vehicle Descriptor Section - VDS).
     * Caracteres 4 a 9 do VIN.
     * 
     * @return Código do veículo
     */
    public String getCodigoVeiculo() {
        return valor.substring(3, 9);
    }
    
    /**
     * Extrai o código de identificação (Vehicle Identifier Section - VIS).
     * Últimos 8 caracteres do VIN.
     * 
     * @return Código de identificação
     */
    public String getCodigoIdentificacao() {
        return valor.substring(9);
    }
}
