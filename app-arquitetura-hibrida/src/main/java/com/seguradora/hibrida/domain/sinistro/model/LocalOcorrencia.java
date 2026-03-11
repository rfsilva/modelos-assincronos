package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object que representa o local de ocorrência de um sinistro.
 *
 * <p>Contém endereço completo e coordenadas geográficas para
 * análises espaciais e estatísticas regionais.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class LocalOcorrencia {

    private final String cep;
    private final String logradouro;
    private final String numero;
    private final String complemento;
    private final String bairro;
    private final String cidade;
    private final String estado;
    private final String pais;

    // Coordenadas geográficas (opcional)
    private final BigDecimal latitude;
    private final BigDecimal longitude;

    /**
     * Obtém o endereço completo formatado.
     */
    public String getEnderecoCompleto() {
        StringBuilder sb = new StringBuilder();

        if (logradouro != null && !logradouro.isBlank()) {
            sb.append(logradouro);
        }

        if (numero != null && !numero.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(numero);
        }

        if (complemento != null && !complemento.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(complemento);
        }

        if (bairro != null && !bairro.isBlank()) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(bairro);
        }

        if (cidade != null && !cidade.isBlank()) {
            if (sb.length() > 0) sb.append(" - ");
            sb.append(cidade);
        }

        if (estado != null && !estado.isBlank()) {
            if (sb.length() > 0) sb.append("/");
            sb.append(estado);
        }

        if (cep != null && !cep.isBlank()) {
            if (sb.length() > 0) sb.append(" - CEP: ");
            sb.append(formatarCep(cep));
        }

        return sb.toString();
    }

    /**
     * Formata CEP no padrão XXXXX-XXX.
     */
    private String formatarCep(String cep) {
        String cepLimpo = cep.replaceAll("\\D", "");
        if (cepLimpo.length() == 8) {
            return cepLimpo.substring(0, 5) + "-" + cepLimpo.substring(5);
        }
        return cep;
    }

    /**
     * Verifica se possui coordenadas geográficas.
     */
    public boolean possuiCoordenadas() {
        return latitude != null && longitude != null;
    }

    /**
     * Valida se o local está completo (possui informações mínimas).
     */
    public boolean isCompleto() {
        return logradouro != null && !logradouro.isBlank() &&
               cidade != null && !cidade.isBlank() &&
               estado != null && !estado.isBlank();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LocalOcorrencia that = (LocalOcorrencia) o;
        return Objects.equals(cep, that.cep) &&
               Objects.equals(logradouro, that.logradouro) &&
               Objects.equals(numero, that.numero) &&
               Objects.equals(complemento, that.complemento) &&
               Objects.equals(bairro, that.bairro) &&
               Objects.equals(cidade, that.cidade) &&
               Objects.equals(estado, that.estado);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cep, logradouro, numero, bairro, cidade, estado);
    }

    @Override
    public String toString() {
        return getEnderecoCompleto();
    }
}
