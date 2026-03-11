package com.seguradora.hibrida.domain.documento.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Objects;

/**
 * Value Object que representa o hash de um documento para garantir integridade.
 *
 * <p>Utiliza SHA-256 como algoritmo padrão para cálculo de hash.
 * Fornece métodos para:
 * <ul>
 *   <li>Calcular hash de conteúdo</li>
 *   <li>Validar integridade de documentos</li>
 *   <li>Comparar hashes de forma segura</li>
 * </ul>
 *
 * <p>Imutável e thread-safe.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@ToString
@EqualsAndHashCode
public final class HashDocumento {

    /**
     * Algoritmo padrão utilizado para hash.
     */
    public static final String ALGORITMO_PADRAO = "SHA-256";

    private final String algoritmo;
    private final String valor;

    /**
     * Construtor privado - use os métodos factory.
     */
    private HashDocumento(String algoritmo, String valor) {
        this.algoritmo = algoritmo;
        this.valor = valor;
    }

    /**
     * Calcula o hash de um conteúdo usando SHA-256.
     *
     * @param conteudo Conteúdo em bytes
     * @return Hash calculado
     * @throws IllegalArgumentException se conteúdo for nulo ou vazio
     */
    public static HashDocumento calcular(byte[] conteudo) {
        Objects.requireNonNull(conteudo, "Conteúdo não pode ser nulo");

        if (conteudo.length == 0) {
            throw new IllegalArgumentException("Conteúdo não pode ser vazio");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance(ALGORITMO_PADRAO);
            byte[] hashBytes = digest.digest(conteudo);
            String hashHex = HexFormat.of().formatHex(hashBytes);

            return new HashDocumento(ALGORITMO_PADRAO, hashHex);

        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("Algoritmo de hash não disponível: " + ALGORITMO_PADRAO, e);
        }
    }

    /**
     * Calcula o hash de uma string usando SHA-256.
     *
     * @param conteudo Conteúdo em string
     * @return Hash calculado
     */
    public static HashDocumento calcular(String conteudo) {
        Objects.requireNonNull(conteudo, "Conteúdo não pode ser nulo");
        return calcular(conteudo.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * Cria um HashDocumento a partir de um valor hexadecimal existente.
     *
     * @param valorHex Valor do hash em hexadecimal
     * @return HashDocumento
     * @throws IllegalArgumentException se o valor não for válido
     */
    public static HashDocumento fromHex(String valorHex) {
        Objects.requireNonNull(valorHex, "Valor do hash não pode ser nulo");

        if (!isValidoHex(valorHex)) {
            throw new IllegalArgumentException("Valor de hash inválido: " + valorHex);
        }

        return new HashDocumento(ALGORITMO_PADRAO, valorHex.toLowerCase());
    }

    /**
     * Valida se o conteúdo corresponde a este hash.
     *
     * @param conteudo Conteúdo a validar
     * @return true se o hash corresponder
     */
    public boolean validar(byte[] conteudo) {
        if (conteudo == null || conteudo.length == 0) {
            return false;
        }

        HashDocumento hashCalculado = calcular(conteudo);
        return this.equals(hashCalculado);
    }

    /**
     * Valida se o conteúdo corresponde ao hash fornecido.
     *
     * @param conteudo Conteúdo a validar
     * @param hash Hash esperado
     * @return true se o hash corresponder
     */
    public static boolean validar(byte[] conteudo, HashDocumento hash) {
        if (hash == null) {
            return false;
        }
        return hash.validar(conteudo);
    }

    /**
     * Verifica se o hash é válido (não nulo e no formato correto).
     *
     * @return true se válido
     */
    public boolean isValido() {
        return valor != null && !valor.isEmpty() && isValidoHex(valor);
    }

    /**
     * Verifica se uma string está no formato hexadecimal válido para SHA-256.
     *
     * @param hex String a verificar
     * @return true se for hexadecimal válido
     */
    private static boolean isValidoHex(String hex) {
        if (hex == null || hex.isEmpty()) {
            return false;
        }

        // SHA-256 produz 64 caracteres hexadecimais (32 bytes * 2)
        if (hex.length() != 64) {
            return false;
        }

        // Verificar se contém apenas caracteres hexadecimais
        return hex.matches("^[0-9a-fA-F]{64}$");
    }

    /**
     * Retorna uma versão abreviada do hash (primeiros 8 caracteres).
     *
     * @return Hash abreviado
     */
    public String getValorAbreviado() {
        if (valor == null || valor.length() < 8) {
            return valor;
        }
        return valor.substring(0, 8) + "...";
    }

    /**
     * Retorna uma versão curta do hash (primeiros 16 caracteres).
     *
     * @return Hash curto
     */
    public String getValorCurto() {
        if (valor == null || valor.length() < 16) {
            return valor;
        }
        return valor.substring(0, 16);
    }

    /**
     * Compara de forma segura contra timing attacks.
     *
     * @param outro Outro hash para comparação
     * @return true se forem iguais
     */
    public boolean equalsSeguro(HashDocumento outro) {
        if (outro == null) {
            return false;
        }

        if (!this.algoritmo.equals(outro.algoritmo)) {
            return false;
        }

        return MessageDigest.isEqual(
                this.valor.getBytes(StandardCharsets.UTF_8),
                outro.valor.getBytes(StandardCharsets.UTF_8)
        );
    }

    /**
     * Retorna o valor do hash em maiúsculas.
     *
     * @return Hash em maiúsculas
     */
    public String getValorUpperCase() {
        return valor != null ? valor.toUpperCase() : null;
    }

    /**
     * Converte o hash para array de bytes.
     *
     * @return Array de bytes do hash
     */
    public byte[] toBytes() {
        if (valor == null) {
            return new byte[0];
        }
        return HexFormat.of().parseHex(valor);
    }
}
