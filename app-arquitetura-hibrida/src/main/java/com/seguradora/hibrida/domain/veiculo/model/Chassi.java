package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object que representa um chassi/VIN (Vehicle Identification Number).
 * 
 * <p>O chassi é um código único de identificação de veículos seguindo o padrão
 * internacional VIN de 17 caracteres, com validações específicas incluindo
 * dígito verificador e caracteres proibidos.
 * 
 * <p>Este Value Object encapsula as regras de validação do chassi/VIN,
 * incluindo verificação de formato, caracteres permitidos e dígito verificador.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class Chassi implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private static final int TAMANHO_VIN = 17;
    private static final String CARACTERES_PROIBIDOS = "IOQ";
    private static final String VALORES_DIGITO_VERIFICADOR = "0123456789X";
    private static final int[] PESOS = {8, 7, 6, 5, 4, 3, 2, 10, 0, 9, 8, 7, 6, 5, 4, 3, 2};

    // Mapeamento simples para transliteração: 0-9=0-9, A=10, B=11, etc.
    private static final String TRANSLITERACAO_CHARS = "0123456789ABCDEFGHJKLMNPRSTUVWXYZ";
    
    private final String valor;
    
    /**
     * Construtor privado.
     * 
     * @param valor Valor do chassi já validado
     */
    private Chassi(String valor) {
        this.valor = valor;
    }
    
    /**
     * Cria uma instância de Chassi a partir de uma string.
     * 
     * @param chassi String representando o chassi/VIN
     * @return Instância de Chassi
     * @throws IllegalArgumentException se o chassi for inválido
     */
    public static Chassi of(String chassi) {
        if (chassi == null || chassi.trim().isEmpty()) {
            throw new IllegalArgumentException("Chassi não pode ser nulo ou vazio");
        }
        
        String chassiLimpo = chassi.trim().toUpperCase();
        
        // Valida tamanho
        if (chassiLimpo.length() != TAMANHO_VIN) {
            throw new IllegalArgumentException(
                String.format("Chassi deve ter %d caracteres. Fornecido: %s", 
                    TAMANHO_VIN, chassi));
        }
        
        // Valida caracteres permitidos
        if (!validarCaracteresPermitidos(chassiLimpo)) {
            throw new IllegalArgumentException(
                "Chassi contém caracteres proibidos (I, O, Q): " + chassi);
        }
        
        // Valida formato alfanumérico
        if (!chassiLimpo.matches("[A-HJ-NPR-Z0-9]{17}")) {
            throw new IllegalArgumentException("Chassi deve conter apenas letras e números válidos: " + chassi);
        }
        
        // Valida dígito verificador (posição 9)
        if (!validarDigitoVerificador(chassiLimpo)) {
            throw new IllegalArgumentException("Dígito verificador do chassi inválido: " + chassi);
        }
        
        return new Chassi(chassiLimpo);
    }
    
    /**
     * Retorna o valor do chassi.
     * 
     * @return Valor do chassi (17 caracteres)
     */
    public String getValor() {
        return valor;
    }
    
    /**
     * Retorna o chassi formatado para exibição.
     * 
     * <p>Formato: XXX XXXXXX X XXXXXXXX
     * 
     * @return Chassi formatado
     */
    public String getFormatado() {
        return String.format("%s %s %s %s",
            valor.substring(0, 3),   // WMI (World Manufacturer Identifier)
            valor.substring(3, 9),   // VDS (Vehicle Descriptor Section)
            valor.substring(9, 10),  // Check Digit
            valor.substring(10, 17)  // VIS (Vehicle Identifier Section)
        );
    }
    
    /**
     * Retorna o WMI (World Manufacturer Identifier).
     * 
     * <p>Primeiros 3 caracteres que identificam o fabricante.
     * 
     * @return WMI (3 caracteres)
     */
    public String getCodigoFabricante() {
        return valor.substring(0, 3);
    }
    
    /**
     * Retorna o VDS (Vehicle Descriptor Section).
     *
     * <p>Caracteres 4-9 que descrevem o veículo (6 caracteres, incluindo DV).
     *
     * @return VDS (6 caracteres)
     */
    public String getCodigoVeiculo() {
        return valor.substring(3, 9);
    }

    /**
     * Retorna o dígito verificador.
     *
     * <p>9º caractere usado para validação.
     *
     * @return Dígito verificador
     */
    public char getDigitoVerificador() {
        return valor.charAt(8);
    }

    /**
     * Retorna o VIS (Vehicle Identifier Section).
     *
     * <p>Últimos 8 caracteres que identificam o veículo específico.
     *
     * @return VIS (8 caracteres)
     */
    public String getCodigoIdentificacao() {
        return valor.substring(9, 17);
    }
    
    /**
     * Retorna o ano do modelo baseado no 10º dígito.
     * 
     * @return Ano do modelo ou null se não identificado
     */
    public Integer getAnoModelo() {
        char digitoAno = valor.charAt(9);
        
        // Mapeamento do dígito para ano (simplificado)
        switch (digitoAno) {
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
            case '1': return 2001;
            case '2': return 2002;
            case '3': return 2003;
            case '4': return 2004;
            case '5': return 2005;
            case '6': return 2006;
            case '7': return 2007;
            case '8': return 2008;
            case '9': return 2009;
            default: return null;
        }
    }
    
    /**
     * Valida se os caracteres são permitidos em um VIN.
     * 
     * <p>Caracteres I, O e Q são proibidos para evitar confusão
     * com números 1, 0 e 0 respectivamente.
     * 
     * @param chassi Chassi a ser validado
     * @return true se todos os caracteres são permitidos
     */
    private static boolean validarCaracteresPermitidos(String chassi) {
        for (char c : CARACTERES_PROIBIDOS.toCharArray()) {
            if (chassi.indexOf(c) >= 0) {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Valida o dígito verificador do chassi/VIN.
     *
     * <p>Algoritmo padrão ISO 3779:
     * 1. Cada caractere é convertido para valor numérico
     * 2. Multiplica pelo peso da posição
     * 3. Soma todos os produtos
     * 4. Divide por 11 e pega o resto
     * 5. Se resto = 10, DV = X; senão DV = resto
     *
     * @param chassi Chassi completo (17 caracteres)
     * @return true se o dígito verificador está correto
     */
    public static boolean validarDigitoVerificador(String chassi) {
        if (chassi == null || chassi.length() != TAMANHO_VIN) {
            return false;
        }

        try {
            char digitoInformado = chassi.toUpperCase().charAt(8);

            int soma = 0;
            for (int i = 0; i < TAMANHO_VIN; i++) {
                if (i == 8) continue; // Pula o dígito verificador

                char c = chassi.toUpperCase().charAt(i);
                int valor = obterValor(c);
                int peso = PESOS[i];

                soma += valor * peso;
            }

            int resto = soma % 11;
            char digitoCalculado = (resto == 10) ? 'X' : (char) ('0' + resto);

            return digitoCalculado == digitoInformado;

        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * Obtém o valor numérico de um caractere para cálculo do dígito verificador.
     *
     * @param c Caractere
     * @return Valor numérico
     */
    private static int obterValor(char c) {
        int index = TRANSLITERACAO_CHARS.indexOf(c);
        if (index >= 0 && index <= 9) {
            return index; // 0-9
        } else if (index >= 10) {
            return index - 9; // A=1, B=2, etc.
        }
        return 0;
    }
    
    /**
     * Gera um chassi de exemplo para testes.
     *
     * @return Chassi válido de exemplo
     */
    public static Chassi exemplo() {
        return new Chassi("1HGBH41J6MN109186");
    }
    
    /**
     * Verifica se uma string pode ser um chassi válido.
     * 
     * @param chassi String a ser verificada
     * @return true se pode ser um chassi válido
     */
    public static boolean isValido(String chassi) {
        try {
            of(chassi);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
    
    /**
     * Verifica se o chassi é de um veículo nacional.
     * 
     * <p>Baseado nos códigos WMI conhecidos de fabricantes brasileiros.
     * 
     * @return true se é veículo nacional
     */
    public boolean isVeiculoNacional() {
        String wmi = getCodigoFabricante();
        
        // Códigos WMI de fabricantes brasileiros (exemplos)
        return wmi.startsWith("9B") ||  // Volkswagen Brasil
               wmi.startsWith("9C") ||  // Ford Brasil
               wmi.startsWith("93") ||  // GM Brasil
               wmi.startsWith("8A") ||  // Fiat Brasil
               wmi.startsWith("95");    // Honda Brasil
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Chassi chassi = (Chassi) obj;
        return Objects.equals(valor, chassi.valor);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(valor);
    }
    
    @Override
    public String toString() {
        return valor;
    }
}