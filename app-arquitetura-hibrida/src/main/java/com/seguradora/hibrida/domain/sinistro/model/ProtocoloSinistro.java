package com.seguradora.hibrida.domain.sinistro.model;

import java.time.Year;
import java.util.Objects;

/**
 * Value Object que representa o protocolo de um sinistro.
 *
 * <p>Formato: ANO-SEQUENCIAL (ex: 2024-000001)
 * <ul>
 *   <li>ANO: 4 dígitos do ano corrente</li>
 *   <li>SEQUENCIAL: 6 dígitos sequenciais (reset anual)</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public class ProtocoloSinistro {

    private final String valor;

    private ProtocoloSinistro(String valor) {
        this.valor = valor;
    }

    /**
     * Cria um protocolo a partir de uma string.
     *
     * @param protocolo String no formato ANO-SEQUENCIAL
     * @return ProtocoloSinistro criado
     * @throws IllegalArgumentException se o formato for inválido
     */
    public static ProtocoloSinistro of(String protocolo) {
        if (protocolo == null || protocolo.isBlank()) {
            throw new IllegalArgumentException("Protocolo não pode ser nulo ou vazio");
        }

        if (!protocolo.matches("\\d{4}-\\d{6}")) {
            throw new IllegalArgumentException(
                "Protocolo deve estar no formato ANO-SEQUENCIAL (ex: 2024-000001). Recebido: " + protocolo
            );
        }

        return new ProtocoloSinistro(protocolo);
    }

    /**
     * Gera um novo protocolo baseado no ano e sequencial.
     *
     * @param sequencial Número sequencial (será formatado com 6 dígitos)
     * @return ProtocoloSinistro gerado
     */
    public static ProtocoloSinistro gerar(long sequencial) {
        return gerar(Year.now().getValue(), sequencial);
    }

    /**
     * Gera um novo protocolo baseado no ano e sequencial específicos.
     *
     * @param ano Ano de 4 dígitos
     * @param sequencial Número sequencial (será formatado com 6 dígitos)
     * @return ProtocoloSinistro gerado
     */
    public static ProtocoloSinistro gerar(int ano, long sequencial) {
        if (ano < 2000 || ano > 9999) {
            throw new IllegalArgumentException("Ano deve estar entre 2000 e 9999");
        }

        if (sequencial < 1 || sequencial > 999999) {
            throw new IllegalArgumentException("Sequencial deve estar entre 1 e 999999");
        }

        String protocolo = String.format("%04d-%06d", ano, sequencial);
        return new ProtocoloSinistro(protocolo);
    }

    public String getValor() {
        return valor;
    }

    /**
     * Obtém o ano do protocolo.
     */
    public int getAno() {
        return Integer.parseInt(valor.substring(0, 4));
    }

    /**
     * Obtém o sequencial do protocolo.
     */
    public long getSequencial() {
        return Long.parseLong(valor.substring(5));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProtocoloSinistro that = (ProtocoloSinistro) o;
        return Objects.equals(valor, that.valor);
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
