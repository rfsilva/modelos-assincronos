package com.seguradora.hibrida.domain.sinistro.query.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

/**
 * View para analytics e relatórios de sinistros.
 *
 * <p>Contém dados agregados para análises estatísticas, comparações
 * e identificação de tendências em sinistros.
 *
 * <p>Características:
 * <ul>
 *   <li>Agregações por múltiplas dimensões (período, tipo, região, analista)</li>
 *   <li>Métricas estatísticas (média, moda, variação)</li>
 *   <li>Comparações temporais e tendências</li>
 *   <li>Base para dashboards executivos</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "sinistro_analytics_view", schema = "projections", indexes = {
    @Index(name = "idx_analytics_periodo_tipo", columnList = "periodo, tipo_sinistro"),
    @Index(name = "idx_analytics_periodo_regiao", columnList = "periodo, regiao"),
    @Index(name = "idx_analytics_periodo_analista", columnList = "periodo, analista_id"),
    @Index(name = "idx_analytics_data_ref", columnList = "data_referencia DESC"),
    @Index(name = "idx_analytics_tipo_sinistro", columnList = "tipo_sinistro")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistroAnalyticsView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    // === DIMENSÕES ===

    /**
     * Período de referência (YYYY-MM-DD, YYYY-WW, YYYY-MM).
     */
    @Column(name = "periodo", length = 20, nullable = false)
    private String periodo;

    /**
     * Tipo do período (DIA, SEMANA, MES, TRIMESTRE, ANO).
     */
    @Column(name = "tipo_periodo", length = 10, nullable = false)
    private String tipoPeriodo;

    /**
     * Data de referência para ordenação.
     */
    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    /**
     * Tipo do sinistro (dimensão de análise).
     */
    @Column(name = "tipo_sinistro", length = 50)
    private String tipoSinistro;

    /**
     * Região (UF) - dimensão de análise.
     */
    @Column(name = "regiao", length = 2)
    private String regiao;

    /**
     * ID do analista responsável - dimensão de análise.
     */
    @Column(name = "analista_id", length = 36)
    private String analistaId;

    /**
     * Nome do analista.
     */
    @Column(name = "analista_nome", length = 100)
    private String analistaNome;

    /**
     * Canal de abertura (WEB, APP, TELEFONE, etc).
     */
    @Column(name = "canal_abertura", length = 30)
    private String canalAbertura;

    // === MÉTRICAS AGREGADAS ===

    /**
     * Quantidade total de sinistros.
     */
    @Column(name = "quantidade", nullable = false)
    @Builder.Default
    private Integer quantidade = 0;

    /**
     * Quantidade de sinistros aprovados.
     */
    @Column(name = "quantidade_aprovados")
    @Builder.Default
    private Integer quantidadeAprovados = 0;

    /**
     * Quantidade de sinistros reprovados.
     */
    @Column(name = "quantidade_reprovados")
    @Builder.Default
    private Integer quantidadeReprovados = 0;

    /**
     * Quantidade de sinistros em andamento.
     */
    @Column(name = "quantidade_em_andamento")
    @Builder.Default
    private Integer quantidadeEmAndamento = 0;

    // === MÉTRICAS FINANCEIRAS ===

    /**
     * Valor total dos sinistros.
     */
    @Column(name = "valor_total", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /**
     * Valor médio dos sinistros.
     */
    @Column(name = "valor_medio", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorMedio = BigDecimal.ZERO;

    /**
     * Valor mediano dos sinistros.
     */
    @Column(name = "valor_mediano", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorMediano = BigDecimal.ZERO;

    /**
     * Desvio padrão dos valores.
     */
    @Column(name = "desvio_padrao_valor", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal desvioPadraoValor = BigDecimal.ZERO;

    /**
     * Valor mínimo registrado.
     */
    @Column(name = "valor_minimo", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorMinimo = BigDecimal.ZERO;

    /**
     * Valor máximo registrado.
     */
    @Column(name = "valor_maximo", precision = 15, scale = 2)
    @Builder.Default
    private BigDecimal valorMaximo = BigDecimal.ZERO;

    // === MÉTRICAS DE TEMPO ===

    /**
     * Tempo médio de processamento em minutos.
     */
    @Column(name = "tempo_medio_processamento")
    @Builder.Default
    private Long tempoMedioProcessamento = 0L;

    /**
     * Tempo mediano de processamento em minutos.
     */
    @Column(name = "tempo_mediano_processamento")
    @Builder.Default
    private Long tempoMedianoProcessamento = 0L;

    /**
     * Tempo mínimo de processamento em minutos.
     */
    @Column(name = "tempo_minimo_processamento")
    @Builder.Default
    private Long tempoMinimoProcessamento = 0L;

    /**
     * Tempo máximo de processamento em minutos.
     */
    @Column(name = "tempo_maximo_processamento")
    @Builder.Default
    private Long tempoMaximoProcessamento = 0L;

    // === TAXAS E PERCENTUAIS ===

    /**
     * Taxa de aprovação (0-100).
     */
    @Column(name = "taxa_aprovacao", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaAprovacao = BigDecimal.ZERO;

    /**
     * Taxa de reprovação (0-100).
     */
    @Column(name = "taxa_reprovacao", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaReprovacao = BigDecimal.ZERO;

    /**
     * Taxa de conversão (finalizados / total).
     */
    @Column(name = "taxa_conversao", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal taxaConversao = BigDecimal.ZERO;

    /**
     * Percentual dentro do SLA.
     */
    @Column(name = "percentual_sla", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal percentualSla = BigDecimal.ZERO;

    // === INDICADORES DE QUALIDADE ===

    /**
     * Quantidade de sinistros dentro do SLA.
     */
    @Column(name = "quantidade_dentro_sla")
    @Builder.Default
    private Integer quantidadeDentroSla = 0;

    /**
     * Quantidade de sinistros fora do SLA.
     */
    @Column(name = "quantidade_fora_sla")
    @Builder.Default
    private Integer quantidadeForaSla = 0;

    /**
     * Quantidade com retrabalho (documentos rejeitados).
     */
    @Column(name = "quantidade_retrabalho")
    @Builder.Default
    private Integer quantidadeRetrabalho = 0;

    /**
     * Score de qualidade (0-100).
     * Calculado com base em SLA, retrabalho e tempo de processamento.
     */
    @Column(name = "score_qualidade", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal scoreQualidade = BigDecimal.ZERO;

    // === COMPARAÇÕES TEMPORAIS ===

    /**
     * Variação percentual em relação ao período anterior.
     */
    @Column(name = "variacao_periodo_anterior", precision = 7, scale = 2)
    private BigDecimal variacaoPeriodoAnterior;

    /**
     * Tendência (CRESCENTE, ESTAVEL, DECRESCENTE).
     */
    @Column(name = "tendencia", length = 15)
    private String tendencia;

    // === AUDITORIA ===

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private Instant createdAt = Instant.now();

    @Column(name = "updated_at", nullable = false)
    @Builder.Default
    private Instant updatedAt = Instant.now();

    @Version
    @Column(name = "version")
    private Long version;

    @Column(name = "last_event_id")
    private Long lastEventId;

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Calcula a taxa de aprovação.
     *
     * @return taxa de aprovação em percentual
     */
    public BigDecimal calcularTaxaAprovacao() {
        int finalizados = (quantidadeAprovados != null ? quantidadeAprovados : 0) +
                         (quantidadeReprovados != null ? quantidadeReprovados : 0);

        if (finalizados == 0) {
            return BigDecimal.ZERO;
        }

        int aprovados = quantidadeAprovados != null ? quantidadeAprovados : 0;
        return BigDecimal.valueOf(aprovados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(finalizados), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a taxa de reprovação.
     *
     * @return taxa de reprovação em percentual
     */
    public BigDecimal calcularTaxaReprovacao() {
        int finalizados = (quantidadeAprovados != null ? quantidadeAprovados : 0) +
                         (quantidadeReprovados != null ? quantidadeReprovados : 0);

        if (finalizados == 0) {
            return BigDecimal.ZERO;
        }

        int reprovados = quantidadeReprovados != null ? quantidadeReprovados : 0;
        return BigDecimal.valueOf(reprovados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(finalizados), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a taxa de conversão (finalizados/total).
     *
     * @return taxa de conversão em percentual
     */
    public BigDecimal calcularTaxaConversao() {
        if (quantidade == null || quantidade == 0) {
            return BigDecimal.ZERO;
        }

        int finalizados = (quantidadeAprovados != null ? quantidadeAprovados : 0) +
                         (quantidadeReprovados != null ? quantidadeReprovados : 0);

        return BigDecimal.valueOf(finalizados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(quantidade), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o percentual de conformidade com SLA.
     *
     * @return percentual de SLA
     */
    public BigDecimal calcularPercentualSla() {
        int total = (quantidadeDentroSla != null ? quantidadeDentroSla : 0) +
                   (quantidadeForaSla != null ? quantidadeForaSla : 0);

        if (total == 0) {
            return BigDecimal.valueOf(100);
        }

        int dentroSla = quantidadeDentroSla != null ? quantidadeDentroSla : 0;
        return BigDecimal.valueOf(dentroSla)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula o score de qualidade baseado em múltiplos fatores.
     *
     * @return score de 0 a 100
     */
    public BigDecimal calcularScoreQualidade() {
        BigDecimal scoreSla = calcularPercentualSla();

        // Penalizar retrabalho
        BigDecimal scoreRetrabalho = BigDecimal.valueOf(100);
        if (quantidade != null && quantidade > 0 && quantidadeRetrabalho != null) {
            BigDecimal taxaRetrabalho = BigDecimal.valueOf(quantidadeRetrabalho)
                    .divide(BigDecimal.valueOf(quantidade), 4, RoundingMode.HALF_UP);
            scoreRetrabalho = scoreRetrabalho.subtract(taxaRetrabalho.multiply(BigDecimal.valueOf(50)));
        }

        // Penalizar tempo excessivo (acima de 48h = 2880 min)
        BigDecimal scoreTempo = BigDecimal.valueOf(100);
        if (tempoMedioProcessamento != null && tempoMedioProcessamento > 2880) {
            long horasExcedentes = (tempoMedioProcessamento - 2880) / 60;
            BigDecimal penalidade = BigDecimal.valueOf(horasExcedentes)
                    .multiply(BigDecimal.valueOf(2))
                    .min(BigDecimal.valueOf(30)); // Máximo 30 pontos de penalidade
            scoreTempo = scoreTempo.subtract(penalidade);
        }

        // Média ponderada: SLA (50%), Retrabalho (30%), Tempo (20%)
        return scoreSla.multiply(BigDecimal.valueOf(0.5))
                .add(scoreRetrabalho.multiply(BigDecimal.valueOf(0.3)))
                .add(scoreTempo.multiply(BigDecimal.valueOf(0.2)))
                .setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * Determina a tendência com base na variação.
     *
     * @return CRESCENTE, ESTAVEL ou DECRESCENTE
     */
    public String determinarTendencia() {
        if (variacaoPeriodoAnterior == null) {
            return "ESTAVEL";
        }

        if (variacaoPeriodoAnterior.compareTo(BigDecimal.valueOf(5)) > 0) {
            return "CRESCENTE";
        } else if (variacaoPeriodoAnterior.compareTo(BigDecimal.valueOf(-5)) < 0) {
            return "DECRESCENTE";
        }
        return "ESTAVEL";
    }

    /**
     * Verifica se a performance está acima da média esperada.
     *
     * @return true se score de qualidade >= 80
     */
    public boolean isPerformanceAcimaMedia() {
        return scoreQualidade != null && scoreQualidade.compareTo(BigDecimal.valueOf(80)) >= 0;
    }

    /**
     * Verifica se há problemas de qualidade.
     *
     * @return true se score < 60 ou SLA < 70%
     */
    public boolean hasProblemasQualidade() {
        return (scoreQualidade != null && scoreQualidade.compareTo(BigDecimal.valueOf(60)) < 0) ||
               (percentualSla != null && percentualSla.compareTo(BigDecimal.valueOf(70)) < 0);
    }

    /**
     * Retorna tempo médio em horas.
     *
     * @return tempo médio em horas
     */
    public Double getTempoMedioHoras() {
        if (tempoMedioProcessamento == null || tempoMedioProcessamento == 0) {
            return 0.0;
        }
        return tempoMedioProcessamento / 60.0;
    }

    /**
     * Retorna tempo médio em dias.
     *
     * @return tempo médio em dias
     */
    public Double getTempoMedioDias() {
        if (tempoMedioProcessamento == null || tempoMedioProcessamento == 0) {
            return 0.0;
        }
        return tempoMedioProcessamento / (60.0 * 24.0);
    }

    /**
     * Compara com outro período e retorna a diferença.
     *
     * @param outroPeriodo período para comparação
     * @return percentual de variação
     */
    public BigDecimal compararCom(SinistroAnalyticsView outroPeriodo) {
        if (outroPeriodo == null || outroPeriodo.getQuantidade() == 0) {
            return BigDecimal.ZERO;
        }

        BigDecimal diferenca = BigDecimal.valueOf(this.quantidade - outroPeriodo.getQuantidade());
        return diferenca
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(outroPeriodo.getQuantidade()), 2, RoundingMode.HALF_UP);
    }

    /**
     * Recalcula todas as métricas derivadas.
     */
    public void recalcularMetricas() {
        this.taxaAprovacao = calcularTaxaAprovacao();
        this.taxaReprovacao = calcularTaxaReprovacao();
        this.taxaConversao = calcularTaxaConversao();
        this.percentualSla = calcularPercentualSla();
        this.scoreQualidade = calcularScoreQualidade();
        this.tendencia = determinarTendencia();

        // Recalcular valor médio
        if (quantidade != null && quantidade > 0 && valorTotal != null) {
            this.valorMedio = valorTotal.divide(
                BigDecimal.valueOf(quantidade),
                2,
                RoundingMode.HALF_UP
            );
        }
    }

    // === CALLBACKS JPA ===

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = Instant.now();
        recalcularMetricas();
    }

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) {
            this.createdAt = Instant.now();
        }
        if (this.updatedAt == null) {
            this.updatedAt = Instant.now();
        }
        recalcularMetricas();
    }

    @Override
    public String toString() {
        return String.format(
            "SinistroAnalyticsView{periodo='%s', tipo='%s', quantidade=%d, scoreQualidade=%.2f, tendencia=%s}",
            periodo, tipoSinistro != null ? tipoSinistro : "TODOS", quantidade, scoreQualidade, tendencia
        );
    }
}
