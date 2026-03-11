package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroAnalyticsView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para analytics e relatórios avançados de sinistros.
 *
 * <p>Fornece queries complexas com agregações, window functions
 * e análises estatísticas para dashboards executivos e relatórios.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SinistroAnalyticsRepository extends JpaRepository<SinistroAnalyticsView, UUID> {

    // === CONSULTAS POR DIMENSÕES ===

    /**
     * Busca analytics por período e tipo.
     *
     * @param periodo período (YYYY-MM-DD, YYYY-WW, YYYY-MM)
     * @param tipoPeriodo tipo do período
     * @param tipoSinistro tipo do sinistro (null = agregado)
     * @return analytics se encontrado
     */
    Optional<SinistroAnalyticsView> findByPeriodoAndTipoPeriodoAndTipoSinistro(
        String periodo,
        String tipoPeriodo,
        String tipoSinistro
    );

    /**
     * Busca analytics agregado de um período (todos os tipos).
     *
     * @param periodo período
     * @param tipoPeriodo tipo do período
     * @return analytics agregado
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.periodo = :periodo
        AND a.tipoPeriodo = :tipoPeriodo
        AND a.tipoSinistro IS NULL
        AND a.regiao IS NULL
        AND a.analistaId IS NULL
        """)
    Optional<SinistroAnalyticsView> findAgregadoPorPeriodo(
        @Param("periodo") String periodo,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Busca analytics por período e região.
     *
     * @param periodo período
     * @param tipoPeriodo tipo do período
     * @param regiao sigla do estado
     * @return analytics da região
     */
    Optional<SinistroAnalyticsView> findByPeriodoAndTipoPeriodoAndRegiao(
        String periodo,
        String tipoPeriodo,
        String regiao
    );

    /**
     * Busca analytics por período e analista.
     *
     * @param periodo período
     * @param tipoPeriodo tipo do período
     * @param analistaId ID do analista
     * @return analytics do analista
     */
    Optional<SinistroAnalyticsView> findByPeriodoAndTipoPeriodoAndAnalistaId(
        String periodo,
        String tipoPeriodo,
        String analistaId
    );

    // === CONSULTAS DE SÉRIES TEMPORAIS ===

    /**
     * Busca série temporal agregada.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @return lista ordenada por data
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.tipoPeriodo = :tipoPeriodo
        AND a.tipoSinistro IS NULL
        AND a.regiao IS NULL
        AND a.analistaId IS NULL
        ORDER BY a.dataReferencia ASC
        """)
    List<SinistroAnalyticsView> findSerieTemporalAgregada(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Busca série temporal por tipo de sinistro.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @param tipoSinistro tipo do sinistro
     * @return lista ordenada por data
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.tipoPeriodo = :tipoPeriodo
        AND a.tipoSinistro = :tipoSinistro
        ORDER BY a.dataReferencia ASC
        """)
    List<SinistroAnalyticsView> findSerieTemporalPorTipo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo,
        @Param("tipoSinistro") String tipoSinistro
    );

    /**
     * Busca série temporal por região.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @param regiao sigla do estado
     * @return lista ordenada por data
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.tipoPeriodo = :tipoPeriodo
        AND a.regiao = :regiao
        ORDER BY a.dataReferencia ASC
        """)
    List<SinistroAnalyticsView> findSerieTemporalPorRegiao(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo,
        @Param("regiao") String regiao
    );

    // === ANÁLISE DE TENDÊNCIAS ===

    /**
     * Busca últimos N períodos para análise de tendência.
     *
     * @param tipoPeriodo tipo do período
     * @param quantidade quantidade de períodos
     * @return lista ordenada (mais recente primeiro)
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_analytics_view
        WHERE tipo_periodo = :tipoPeriodo
        AND tipo_sinistro IS NULL
        AND regiao IS NULL
        AND analista_id IS NULL
        ORDER BY data_referencia DESC
        LIMIT :quantidade
        """, nativeQuery = true)
    List<SinistroAnalyticsView> findTendencias(
        @Param("tipoPeriodo") String tipoPeriodo,
        @Param("quantidade") int quantidade
    );

    /**
     * Busca períodos com tendência específica.
     *
     * @param tendencia CRESCENTE, ESTAVEL, DECRESCENTE
     * @param dataLimite data limite
     * @return lista de períodos
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.tendencia = :tendencia
        AND a.dataReferencia >= :dataLimite
        AND a.tipoSinistro IS NULL
        ORDER BY a.dataReferencia DESC
        """)
    List<SinistroAnalyticsView> findByTendencia(
        @Param("tendencia") String tendencia,
        @Param("dataLimite") LocalDate dataLimite
    );

    // === COMPARAÇÕES E RANKINGS ===

    /**
     * Ranking de tipos de sinistro por quantidade.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista ordenada por quantidade (DESC)
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.tipoSinistro IS NOT NULL
        AND a.regiao IS NULL
        AND a.analistaId IS NULL
        ORDER BY a.quantidade DESC
        """)
    List<SinistroAnalyticsView> findRankingPorTipo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Ranking de regiões por quantidade.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista ordenada por quantidade (DESC)
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.regiao IS NOT NULL
        AND a.tipoSinistro IS NULL
        AND a.analistaId IS NULL
        ORDER BY a.quantidade DESC
        """)
    List<SinistroAnalyticsView> findRankingPorRegiao(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Ranking de analistas por performance (score de qualidade).
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista ordenada por score (DESC)
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND a.analistaId IS NOT NULL
        AND a.quantidade >= 5
        ORDER BY a.scoreQualidade DESC, a.quantidade DESC
        """)
    List<SinistroAnalyticsView> findRankingAnalistas(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    // === ANÁLISES DE QUALIDADE ===

    /**
     * Busca períodos com problemas de qualidade.
     *
     * @param scoreMinimo score mínimo aceitável
     * @param dataLimite data limite
     * @return lista de períodos com baixa qualidade
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.scoreQualidade < :scoreMinimo
        AND a.dataReferencia >= :dataLimite
        AND a.quantidade >= 3
        ORDER BY a.scoreQualidade ASC
        """)
    List<SinistroAnalyticsView> findComProblemasQualidade(
        @Param("scoreMinimo") BigDecimal scoreMinimo,
        @Param("dataLimite") LocalDate dataLimite
    );

    /**
     * Busca períodos com performance acima da média.
     *
     * @param scoreMinimo score mínimo
     * @param dataLimite data limite
     * @return lista de períodos com alta performance
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.scoreQualidade >= :scoreMinimo
        AND a.dataReferencia >= :dataLimite
        ORDER BY a.scoreQualidade DESC
        """)
    List<SinistroAnalyticsView> findComAltaPerformance(
        @Param("scoreMinimo") BigDecimal scoreMinimo,
        @Param("dataLimite") LocalDate dataLimite
    );

    /**
     * Busca períodos com SLA crítico.
     *
     * @param percentualMaximo percentual máximo aceitável de SLA
     * @param dataLimite data limite
     * @return lista de períodos com SLA crítico
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        WHERE a.percentualSla < :percentualMaximo
        AND a.dataReferencia >= :dataLimite
        AND a.quantidade >= 3
        ORDER BY a.percentualSla ASC
        """)
    List<SinistroAnalyticsView> findComSlaCritico(
        @Param("percentualMaximo") BigDecimal percentualMaximo,
        @Param("dataLimite") LocalDate dataLimite
    );

    // === AGREGAÇÕES COMPLEXAS ===

    /**
     * Calcula média consolidada de múltiplas métricas.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @return array com [qtd_media, valor_medio, tempo_medio, taxa_aprov, score]
     */
    @Query(value = """
        SELECT
            AVG(quantidade) as qtd_media,
            AVG(valor_medio) as valor_medio,
            AVG(tempo_medio_processamento) as tempo_medio,
            AVG(taxa_aprovacao) as taxa_aprovacao,
            AVG(score_qualidade) as score_qualidade
        FROM projections.sinistro_analytics_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND tipo_periodo = :tipoPeriodo
        AND tipo_sinistro IS NULL
        AND quantidade > 0
        """, nativeQuery = true)
    Object[] calcularMediasConsolidadas(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Calcula totais consolidados.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @return array com [total_sinistros, total_aprovados, total_reprovados, valor_total]
     */
    @Query(value = """
        SELECT
            SUM(quantidade) as total_sinistros,
            SUM(quantidade_aprovados) as total_aprovados,
            SUM(quantidade_reprovados) as total_reprovados,
            SUM(valor_total) as valor_total
        FROM projections.sinistro_analytics_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND tipo_periodo = :tipoPeriodo
        AND tipo_sinistro IS NULL
        """, nativeQuery = true)
    Object[] calcularTotaisConsolidados(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    // === COMPARAÇÕES TEMPORAIS COM WINDOW FUNCTIONS ===

    /**
     * Calcula variação percentual comparada ao período anterior.
     *
     * @param dataReferencia data de referência
     * @param tipoPeriodo tipo do período
     * @return analytics com variação calculada
     */
    @Query(value = """
        WITH periodos AS (
            SELECT
                *,
                LAG(quantidade) OVER (ORDER BY data_referencia) as quantidade_anterior
            FROM projections.sinistro_analytics_view
            WHERE tipo_periodo = :tipoPeriodo
            AND tipo_sinistro IS NULL
            AND regiao IS NULL
            AND analista_id IS NULL
            AND data_referencia <= :dataReferencia
            ORDER BY data_referencia DESC
            LIMIT 2
        )
        SELECT
            *,
            CASE
                WHEN quantidade_anterior IS NOT NULL AND quantidade_anterior > 0
                THEN ((quantidade - quantidade_anterior)::decimal / quantidade_anterior * 100)
                ELSE 0
            END as variacao_calculada
        FROM periodos
        WHERE data_referencia = :dataReferencia
        """, nativeQuery = true)
    Object[] calcularVariacaoPeriodoAnterior(
        @Param("dataReferencia") LocalDate dataReferencia,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Busca comparativo de N períodos mais recentes.
     *
     * @param tipoPeriodo tipo do período
     * @param quantidade quantidade de períodos
     * @return lista com variações calculadas
     */
    @Query(value = """
        WITH periodos_recentes AS (
            SELECT
                *,
                LAG(quantidade) OVER (ORDER BY data_referencia) as quantidade_anterior,
                LAG(valor_total) OVER (ORDER BY data_referencia) as valor_anterior
            FROM projections.sinistro_analytics_view
            WHERE tipo_periodo = :tipoPeriodo
            AND tipo_sinistro IS NULL
            AND regiao IS NULL
            AND analista_id IS NULL
            ORDER BY data_referencia DESC
            LIMIT :quantidade
        )
        SELECT * FROM periodos_recentes
        ORDER BY data_referencia DESC
        """, nativeQuery = true)
    List<SinistroAnalyticsView> findComparativoRecente(
        @Param("tipoPeriodo") String tipoPeriodo,
        @Param("quantidade") int quantidade
    );

    // === ANÁLISE DE PERFORMANCE POR DIMENSÃO ===

    /**
     * Performance média por tipo de sinistro.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista agregada por tipo
     */
    @Query(value = """
        SELECT
            tipo_sinistro,
            SUM(quantidade) as total,
            AVG(tempo_medio_processamento) as tempo_medio,
            AVG(taxa_aprovacao) as taxa_aprovacao,
            AVG(score_qualidade) as score_qualidade
        FROM projections.sinistro_analytics_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND tipo_sinistro IS NOT NULL
        GROUP BY tipo_sinistro
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> calcularPerformancePorTipo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Performance média por região.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista agregada por região
     */
    @Query(value = """
        SELECT
            regiao,
            SUM(quantidade) as total,
            AVG(tempo_medio_processamento) as tempo_medio,
            AVG(taxa_aprovacao) as taxa_aprovacao,
            AVG(score_qualidade) as score_qualidade
        FROM projections.sinistro_analytics_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND regiao IS NOT NULL
        GROUP BY regiao
        ORDER BY total DESC
        """, nativeQuery = true)
    List<Object[]> calcularPerformancePorRegiao(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Performance por analista com ranking.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return lista de analistas com métricas
     */
    @Query(value = """
        SELECT
            analista_id,
            analista_nome,
            SUM(quantidade) as total_processados,
            AVG(tempo_medio_processamento) as tempo_medio,
            AVG(taxa_aprovacao) as taxa_aprovacao,
            AVG(score_qualidade) as score_qualidade,
            AVG(percentual_sla) as percentual_sla
        FROM projections.sinistro_analytics_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND analista_id IS NOT NULL
        GROUP BY analista_id, analista_nome
        HAVING SUM(quantidade) >= 3
        ORDER BY score_qualidade DESC, total_processados DESC
        """, nativeQuery = true)
    List<Object[]> calcularPerformancePorAnalista(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    // === VERIFICAÇÕES E UTILIDADES ===

    /**
     * Verifica se existe analytics para um período.
     *
     * @param periodo período
     * @param tipoPeriodo tipo do período
     * @return true se existe
     */
    boolean existsByPeriodoAndTipoPeriodo(String periodo, String tipoPeriodo);

    /**
     * Deleta analytics antigos (manutenção).
     *
     * @param dataLimite data limite
     */
    void deleteByDataReferenciaLessThan(LocalDate dataLimite);

    /**
     * Busca último analytics atualizado.
     *
     * @return último registro atualizado
     */
    @Query("""
        SELECT a FROM SinistroAnalyticsView a
        ORDER BY a.updatedAt DESC
        LIMIT 1
        """)
    Optional<SinistroAnalyticsView> findLastUpdated();

    /**
     * Conta registros por tipo de período.
     *
     * @return lista de [tipoPeriodo, count]
     */
    @Query("""
        SELECT a.tipoPeriodo, COUNT(a)
        FROM SinistroAnalyticsView a
        GROUP BY a.tipoPeriodo
        """)
    List<Object[]> countByTipoPeriodo();
}
