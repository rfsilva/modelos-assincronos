package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Value Object que representa o prazo de processamento de um sinistro.
 *
 * <p>Gerencia SLAs (Service Level Agreements) por tipo de sinistro:
 * <ul>
 *   <li>Sinistros simples: 48 horas</li>
 *   <li>Sinistros complexos: 5 dias úteis</li>
 *   <li>Roubo/furto: 10 dias úteis</li>
 *   <li>Terceiros: 15 dias úteis</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class PrazoProcessamento {

    private final LocalDateTime dataInicio;
    private final LocalDateTime dataLimite;
    private final int diasUteis;
    private final TipoSinistro tipoSinistro;

    /**
     * Cria um prazo de processamento baseado no tipo de sinistro.
     *
     * @param tipoSinistro Tipo do sinistro
     * @return PrazoProcessamento criado
     */
    public static PrazoProcessamento criar(TipoSinistro tipoSinistro) {
        return criar(tipoSinistro, LocalDateTime.now());
    }

    /**
     * Cria um prazo de processamento com data de início específica.
     *
     * @param tipoSinistro Tipo do sinistro
     * @param dataInicio Data de início do prazo
     * @return PrazoProcessamento criado
     */
    public static PrazoProcessamento criar(TipoSinistro tipoSinistro, LocalDateTime dataInicio) {
        int diasUteis = tipoSinistro.getPrazoProcessamentoDias();
        LocalDateTime dataLimite = calcularDataLimite(dataInicio, diasUteis);

        return PrazoProcessamento.builder()
            .dataInicio(dataInicio)
            .dataLimite(dataLimite)
            .diasUteis(diasUteis)
            .tipoSinistro(tipoSinistro)
            .build();
    }

    /**
     * Calcula a data limite considerando apenas dias úteis.
     *
     * Simplificado: considera 7 dias corridos para cada 5 dias úteis
     */
    private static LocalDateTime calcularDataLimite(LocalDateTime inicio, int diasUteis) {
        // Aproximação: 5 dias úteis ≈ 7 dias corridos
        int diasCorridos = (int) Math.ceil(diasUteis * 1.4);
        return inicio.plusDays(diasCorridos);
    }

    /**
     * Verifica se o prazo está vencido.
     */
    public boolean isVencido() {
        return LocalDateTime.now().isAfter(dataLimite);
    }

    /**
     * Verifica se o prazo está próximo do vencimento (80% do tempo decorrido).
     */
    public boolean isProximoVencimento() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(dataInicio) || now.isAfter(dataLimite)) {
            return false;
        }

        Duration prazoTotal = Duration.between(dataInicio, dataLimite);
        Duration prazoDecorrido = Duration.between(dataInicio, now);

        long percentualDecorrido = (prazoDecorrido.toMinutes() * 100) / prazoTotal.toMinutes();

        return percentualDecorrido >= 80;
    }

    /**
     * Calcula o tempo restante em horas.
     */
    public long getHorasRestantes() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isAfter(dataLimite)) {
            return 0;
        }

        Duration restante = Duration.between(now, dataLimite);
        return restante.toHours();
    }

    /**
     * Calcula o percentual de tempo decorrido.
     */
    public int getPercentualDecorrido() {
        LocalDateTime now = LocalDateTime.now();

        if (now.isBefore(dataInicio)) {
            return 0;
        }

        if (now.isAfter(dataLimite)) {
            return 100;
        }

        Duration prazoTotal = Duration.between(dataInicio, dataLimite);
        Duration prazoDecorrido = Duration.between(dataInicio, now);

        return (int) ((prazoDecorrido.toMinutes() * 100) / prazoTotal.toMinutes());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PrazoProcessamento that = (PrazoProcessamento) o;
        return diasUteis == that.diasUteis &&
               Objects.equals(dataInicio, that.dataInicio) &&
               Objects.equals(dataLimite, that.dataLimite) &&
               tipoSinistro == that.tipoSinistro;
    }

    @Override
    public int hashCode() {
        return Objects.hash(dataInicio, dataLimite, diasUteis, tipoSinistro);
    }

    @Override
    public String toString() {
        return String.format("Prazo: %d dias úteis (Limite: %s) - %d%% decorrido",
            diasUteis,
            dataLimite,
            getPercentualDecorrido()
        );
    }
}
