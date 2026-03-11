package com.seguradora.hibrida.domain.sinistro.query.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Map;
import java.util.UUID;

/**
 * View materializada para métricas e dashboard de sinistros.
 *
 * <p>Esta entidade armazena agregações pré-computadas de sinistros
 * para otimizar consultas de dashboard e relatórios gerenciais.
 *
 * <p>Características:
 * <ul>
 *   <li>Métricas agregadas por período (dia, semana, mês)</li>
 *   <li>Campos JSONB para distribuições complexas</li>
 *   <li>Cálculos de SLA e performance</li>
 *   <li>Atualização incremental via projection handlers</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Entity
@Table(name = "sinistro_dashboard_view", schema = "projections", indexes = {
    @Index(name = "idx_dashboard_periodo", columnList = "periodo, tipo_periodo"),
    @Index(name = "idx_dashboard_data_ref", columnList = "data_referencia"),
    @Index(name = "idx_dashboard_tipo", columnList = "tipo_periodo"),
    @Index(name = "idx_dashboard_updated", columnList = "updated_at")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SinistroDashboardView {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    /**
     * Período de referência (formato: YYYY-MM-DD, YYYY-WW, YYYY-MM).
     */
    @Column(name = "periodo", length = 20, nullable = false)
    private String periodo;

    /**
     * Tipo do período (DIA, SEMANA, MES).
     */
    @Column(name = "tipo_periodo", length = 10, nullable = false)
    private String tipoPeriodo;

    /**
     * Data de referência para ordenação e filtros.
     */
    @Column(name = "data_referencia", nullable = false)
    private LocalDate dataReferencia;

    // === MÉTRICAS BÁSICAS ===

    /**
     * Total de sinistros no período.
     */
    @Column(name = "total_sinistros", nullable = false)
    @Builder.Default
    private Integer totalSinistros = 0;

    /**
     * Total de sinistros abertos.
     */
    @Column(name = "sinistros_abertos")
    @Builder.Default
    private Integer sinistrosAbertos = 0;

    /**
     * Total de sinistros em análise.
     */
    @Column(name = "sinistros_em_analise")
    @Builder.Default
    private Integer sinistrosEmAnalise = 0;

    /**
     * Total de sinistros aprovados.
     */
    @Column(name = "sinistros_aprovados")
    @Builder.Default
    private Integer sinistrosAprovados = 0;

    /**
     * Total de sinistros reprovados.
     */
    @Column(name = "sinistros_reprovados")
    @Builder.Default
    private Integer sinistrosReprovados = 0;

    /**
     * Total de sinistros cancelados.
     */
    @Column(name = "sinistros_cancelados")
    @Builder.Default
    private Integer sinistrosCancelados = 0;

    // === MÉTRICAS FINANCEIRAS ===

    /**
     * Valor total estimado dos sinistros.
     */
    @Column(name = "valor_total", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorTotal = BigDecimal.ZERO;

    /**
     * Valor médio dos sinistros.
     */
    @Column(name = "valor_medio", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorMedio = BigDecimal.ZERO;

    /**
     * Valor máximo de sinistro.
     */
    @Column(name = "valor_maximo", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorMaximo = BigDecimal.ZERO;

    /**
     * Valor mínimo de sinistro.
     */
    @Column(name = "valor_minimo", precision = 19, scale = 2)
    @Builder.Default
    private BigDecimal valorMinimo = BigDecimal.ZERO;

    // === MÉTRICAS DE PERFORMANCE ===

    /**
     * Tempo médio de processamento em minutos.
     */
    @Column(name = "tempo_medio_processamento")
    @Builder.Default
    private Long tempoMedioProcessamento = 0L;

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
     * Quantidade de sinistros dentro do SLA.
     */
    @Column(name = "sinistros_dentro_sla")
    @Builder.Default
    private Integer sinistrosDentroSla = 0;

    /**
     * Quantidade de sinistros fora do SLA.
     */
    @Column(name = "sinistros_fora_sla")
    @Builder.Default
    private Integer sinistrosForaSla = 0;

    // === DISTRIBUIÇÕES (JSONB) ===

    /**
     * Distribuição de sinistros por status.
     * Formato: {"ABERTO": 10, "EM_ANALISE": 5, "APROVADO": 20, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sinistros_por_status", columnDefinition = "jsonb")
    private Map<String, Integer> sinistrosPorStatus;

    /**
     * Distribuição de sinistros por tipo.
     * Formato: {"COLISAO": 15, "ROUBO": 5, "INCENDIO": 3, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sinistros_por_tipo", columnDefinition = "jsonb")
    private Map<String, Integer> sinistrosPorTipo;

    /**
     * Distribuição de sinistros por região.
     * Formato: {"SP": 30, "RJ": 15, "MG": 10, ...}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "sinistros_por_regiao", columnDefinition = "jsonb")
    private Map<String, Integer> sinistrosPorRegiao;

    /**
     * Distribuição de valores por faixa.
     * Formato: {"0-5000": 20, "5000-10000": 15, "10000+": 5}
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "distribuicao_valores", columnDefinition = "jsonb")
    private Map<String, Integer> distribuicaoValores;

    // === ALERTAS E SLA ===

    /**
     * Alertas de SLA e anomalias.
     * Formato: [{"tipo": "SLA_EXCEDIDO", "quantidade": 5, "severidade": "ALTA"}, ...]
     */
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "alertas_sla", columnDefinition = "jsonb")
    private Map<String, Object> alertasSla;

    /**
     * Sinistros que precisam de atenção urgente.
     */
    @Column(name = "sinistros_urgentes")
    @Builder.Default
    private Integer sinistrosUrgentes = 0;

    /**
     * Sinistros com documentação pendente.
     */
    @Column(name = "sinistros_doc_pendente")
    @Builder.Default
    private Integer sinistrosDocPendente = 0;

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

    /**
     * ID do último evento processado.
     */
    @Column(name = "last_event_id")
    private Long lastEventId;

    // === MÉTODOS DE NEGÓCIO ===

    /**
     * Calcula a taxa de aprovação com base nos sinistros finalizados.
     *
     * @return taxa de aprovação em percentual (0-100)
     */
    public BigDecimal calcularTaxaAprovacao() {
        int finalizados = (sinistrosAprovados != null ? sinistrosAprovados : 0) +
                         (sinistrosReprovados != null ? sinistrosReprovados : 0);

        if (finalizados == 0) {
            return BigDecimal.ZERO;
        }

        int aprovados = sinistrosAprovados != null ? sinistrosAprovados : 0;
        return BigDecimal.valueOf(aprovados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(finalizados), 2, RoundingMode.HALF_UP);
    }

    /**
     * Calcula a taxa de reprovação com base nos sinistros finalizados.
     *
     * @return taxa de reprovação em percentual (0-100)
     */
    public BigDecimal calcularTaxaReprovacao() {
        int finalizados = (sinistrosAprovados != null ? sinistrosAprovados : 0) +
                         (sinistrosReprovados != null ? sinistrosReprovados : 0);

        if (finalizados == 0) {
            return BigDecimal.ZERO;
        }

        int reprovados = sinistrosReprovados != null ? sinistrosReprovados : 0;
        return BigDecimal.valueOf(reprovados)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(finalizados), 2, RoundingMode.HALF_UP);
    }

    /**
     * Retorna o tempo médio de processamento em horas.
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
     * Retorna o tempo médio de processamento em dias.
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
     * Verifica se há sinistros acima do SLA.
     *
     * @return true se houver sinistros fora do SLA
     */
    public boolean isAcimaSla() {
        return sinistrosForaSla != null && sinistrosForaSla > 0;
    }

    /**
     * Calcula o percentual de sinistros dentro do SLA.
     *
     * @return percentual de conformidade com SLA (0-100)
     */
    public BigDecimal getPercentualSla() {
        int total = (sinistrosDentroSla != null ? sinistrosDentroSla : 0) +
                   (sinistrosForaSla != null ? sinistrosForaSla : 0);

        if (total == 0) {
            return BigDecimal.valueOf(100);
        }

        int dentroSla = sinistrosDentroSla != null ? sinistrosDentroSla : 0;
        return BigDecimal.valueOf(dentroSla)
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(total), 2, RoundingMode.HALF_UP);
    }

    /**
     * Verifica se há alertas críticos.
     *
     * @return true se houver sinistros urgentes ou muito acima do SLA
     */
    public boolean hasAlertasCriticos() {
        return (sinistrosUrgentes != null && sinistrosUrgentes > 0) ||
               (sinistrosForaSla != null && sinistrosForaSla > 5);
    }

    /**
     * Incrementa o contador de sinistros por status.
     *
     * @param status status do sinistro
     */
    public void incrementarStatus(String status) {
        if (sinistrosPorStatus == null) {
            sinistrosPorStatus = new java.util.HashMap<>();
        }
        sinistrosPorStatus.merge(status, 1, Integer::sum);
    }

    /**
     * Incrementa o contador de sinistros por tipo.
     *
     * @param tipo tipo do sinistro
     */
    public void incrementarTipo(String tipo) {
        if (sinistrosPorTipo == null) {
            sinistrosPorTipo = new java.util.HashMap<>();
        }
        sinistrosPorTipo.merge(tipo, 1, Integer::sum);
    }

    /**
     * Incrementa o contador de sinistros por região.
     *
     * @param regiao região (UF)
     */
    public void incrementarRegiao(String regiao) {
        if (sinistrosPorRegiao == null) {
            sinistrosPorRegiao = new java.util.HashMap<>();
        }
        sinistrosPorRegiao.merge(regiao, 1, Integer::sum);
    }

    /**
     * Recalcula métricas derivadas.
     * Deve ser chamado após atualizações nos contadores.
     */
    public void recalcularMetricas() {
        this.taxaAprovacao = calcularTaxaAprovacao();
        this.taxaReprovacao = calcularTaxaReprovacao();

        if (totalSinistros != null && totalSinistros > 0 && valorTotal != null) {
            this.valorMedio = valorTotal.divide(
                BigDecimal.valueOf(totalSinistros),
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
            "SinistroDashboardView{periodo='%s', tipo='%s', total=%d, aprovacao=%.2f%%, sla=%.2f%%}",
            periodo, tipoPeriodo, totalSinistros, taxaAprovacao, getPercentualSla()
        );
    }
}
