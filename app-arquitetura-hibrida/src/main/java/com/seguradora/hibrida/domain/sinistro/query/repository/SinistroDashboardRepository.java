package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDashboardView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para consultas de dashboard de sinistros.
 *
 * <p>Fornece acesso otimizado às métricas agregadas e KPIs
 * para dashboards e painéis gerenciais.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SinistroDashboardRepository extends JpaRepository<SinistroDashboardView, UUID> {

    // === CONSULTAS POR PERÍODO ===

    /**
     * Busca dashboard por período e tipo específicos.
     *
     * @param periodo período no formato YYYY-MM-DD, YYYY-WW, YYYY-MM
     * @param tipoPeriodo tipo do período (DIA, SEMANA, MES)
     * @return dashboard view se encontrado
     */
    Optional<SinistroDashboardView> findByPeriodoAndTipoPeriodo(String periodo, String tipoPeriodo);

    /**
     * Busca dashboards dos últimos N dias.
     *
     * @param dataLimite data limite
     * @param tipoPeriodo tipo do período
     * @return lista de dashboards ordenados por data
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia >= :dataLimite
        AND d.tipoPeriodo = :tipoPeriodo
        ORDER BY d.dataReferencia DESC
        """)
    List<SinistroDashboardView> findUltimosDias(
        @Param("dataLimite") LocalDate dataLimite,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Busca dashboard do dia atual.
     *
     * @param hoje data de hoje
     * @return dashboard do dia se encontrado
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia = :hoje
        AND d.tipoPeriodo = 'DIA'
        """)
    Optional<SinistroDashboardView> findDashboardHoje(@Param("hoje") LocalDate hoje);

    /**
     * Busca dashboards do mês atual.
     *
     * @param anoMes período no formato YYYY-MM
     * @return dashboard do mês se encontrado
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.periodo = :anoMes
        AND d.tipoPeriodo = 'MES'
        """)
    Optional<SinistroDashboardView> findDashboardMes(@Param("anoMes") String anoMes);

    // === CONSULTAS PARA COMPARAÇÕES ===

    /**
     * Busca dashboards para comparação mensal.
     *
     * @param mesAtual mês atual (YYYY-MM)
     * @param mesAnterior mês anterior (YYYY-MM)
     * @return lista com 2 elementos (mes atual, mes anterior)
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.periodo IN (:mesAtual, :mesAnterior)
        AND d.tipoPeriodo = 'MES'
        ORDER BY d.dataReferencia DESC
        """)
    List<SinistroDashboardView> findComparacaoMensal(
        @Param("mesAtual") String mesAtual,
        @Param("mesAnterior") String mesAnterior
    );

    /**
     * Busca últimos N períodos para análise de tendência.
     *
     * @param tipoPeriodo tipo do período
     * @param quantidade quantidade de períodos
     * @return lista ordenada por data (mais recente primeiro)
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_dashboard_view
        WHERE tipo_periodo = :tipoPeriodo
        ORDER BY data_referencia DESC
        LIMIT :quantidade
        """, nativeQuery = true)
    List<SinistroDashboardView> findUltimosPeriodos(
        @Param("tipoPeriodo") String tipoPeriodo,
        @Param("quantidade") int quantidade
    );

    /**
     * Busca dashboards de um intervalo de datas.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @return lista ordenada por data
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND d.tipoPeriodo = :tipoPeriodo
        ORDER BY d.dataReferencia ASC
        """)
    List<SinistroDashboardView> findByIntervalo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    // === CONSULTAS DE ALERTAS ===

    /**
     * Busca períodos com sinistros acima do SLA.
     *
     * @param dataLimite data limite para análise
     * @return lista de períodos com problemas de SLA
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia >= :dataLimite
        AND d.sinistrosForaSla > 0
        ORDER BY d.sinistrosForaSla DESC, d.dataReferencia DESC
        """)
    List<SinistroDashboardView> findPeriodsComProblemasSla(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Busca períodos com sinistros urgentes.
     *
     * @param dataLimite data limite para análise
     * @return lista de períodos com sinistros urgentes
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia >= :dataLimite
        AND d.sinistrosUrgentes > 0
        ORDER BY d.sinistrosUrgentes DESC, d.dataReferencia DESC
        """)
    List<SinistroDashboardView> findPeriodsComSinistrosUrgentes(@Param("dataLimite") LocalDate dataLimite);

    /**
     * Busca períodos com taxa de aprovação abaixo do esperado.
     *
     * @param dataLimite data limite
     * @param taxaMinima taxa mínima aceitável (ex: 70.0)
     * @return lista de períodos com baixa aprovação
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        WHERE d.dataReferencia >= :dataLimite
        AND d.taxaAprovacao < :taxaMinima
        AND d.totalSinistros >= 10
        ORDER BY d.taxaAprovacao ASC, d.dataReferencia DESC
        """)
    List<SinistroDashboardView> findPeriodsComBaixaAprovacao(
        @Param("dataLimite") LocalDate dataLimite,
        @Param("taxaMinima") java.math.BigDecimal taxaMinima
    );

    // === CONSULTAS AGREGADAS ===

    /**
     * Calcula totais consolidados de um período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @param tipoPeriodo tipo do período
     * @return array com [totalSinistros, valorTotal, tempoMedio, taxaAprovacao]
     */
    @Query(value = """
        SELECT
            SUM(total_sinistros) as total,
            SUM(valor_total) as valor,
            AVG(tempo_medio_processamento) as tempo_medio,
            AVG(taxa_aprovacao) as taxa_aprov
        FROM projections.sinistro_dashboard_view
        WHERE data_referencia BETWEEN :dataInicio AND :dataFim
        AND tipo_periodo = :tipoPeriodo
        """, nativeQuery = true)
    Object[] calcularTotaisConsolidados(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim,
        @Param("tipoPeriodo") String tipoPeriodo
    );

    /**
     * Busca períodos com maior volume de sinistros.
     *
     * @param dataLimite data limite
     * @param limite quantidade de resultados
     * @return top N períodos com mais sinistros
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_dashboard_view
        WHERE data_referencia >= :dataLimite
        ORDER BY total_sinistros DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<SinistroDashboardView> findTopPeriodosPorVolume(
        @Param("dataLimite") LocalDate dataLimite,
        @Param("limite") int limite
    );

    /**
     * Busca períodos com maiores valores.
     *
     * @param dataLimite data limite
     * @param limite quantidade de resultados
     * @return top N períodos com maiores valores
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_dashboard_view
        WHERE data_referencia >= :dataLimite
        ORDER BY valor_total DESC
        LIMIT :limite
        """, nativeQuery = true)
    List<SinistroDashboardView> findTopPeriodosPorValor(
        @Param("dataLimite") LocalDate dataLimite,
        @Param("limite") int limite
    );

    // === MÉTRICAS RÁPIDAS ===

    /**
     * Conta total de sinistros em um período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return total de sinistros
     */
    @Query("""
        SELECT COALESCE(SUM(d.totalSinistros), 0)
        FROM SinistroDashboardView d
        WHERE d.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND d.tipoPeriodo = 'DIA'
        """)
    Long countTotalSinistrosPeriodo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Calcula taxa de aprovação média do período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return taxa média de aprovação
     */
    @Query("""
        SELECT AVG(d.taxaAprovacao)
        FROM SinistroDashboardView d
        WHERE d.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND d.tipoPeriodo = 'DIA'
        AND d.totalSinistros > 0
        """)
    java.math.BigDecimal calcularTaxaAprovacaoMedia(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Calcula tempo médio de processamento do período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return tempo médio em minutos
     */
    @Query("""
        SELECT AVG(d.tempoMedioProcessamento)
        FROM SinistroDashboardView d
        WHERE d.dataReferencia BETWEEN :dataInicio AND :dataFim
        AND d.tipoPeriodo = 'DIA'
        AND d.totalSinistros > 0
        """)
    Long calcularTempoMedioProcessamento(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    // === ANÁLISE DE DISTRIBUIÇÕES (JSONB) ===

    /**
     * Busca distribuição consolidada por status em um período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return agregação de sinistros por status
     */
    @Query(value = """
        SELECT
            jsonb_object_agg(status_key, status_value) as distribuicao
        FROM (
            SELECT
                d.key as status_key,
                SUM((d.value)::int) as status_value
            FROM projections.sinistro_dashboard_view,
                 jsonb_each(sinistros_por_status) d
            WHERE data_referencia BETWEEN :dataInicio AND :dataFim
            GROUP BY d.key
        ) agg
        """, nativeQuery = true)
    String findDistribuicaoPorStatus(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    /**
     * Busca distribuição consolidada por tipo em um período.
     *
     * @param dataInicio data inicial
     * @param dataFim data final
     * @return agregação de sinistros por tipo
     */
    @Query(value = """
        SELECT
            jsonb_object_agg(tipo_key, tipo_value) as distribuicao
        FROM (
            SELECT
                d.key as tipo_key,
                SUM((d.value)::int) as tipo_value
            FROM projections.sinistro_dashboard_view,
                 jsonb_each(sinistros_por_tipo) d
            WHERE data_referencia BETWEEN :dataInicio AND :dataFim
            GROUP BY d.key
        ) agg
        """, nativeQuery = true)
    String findDistribuicaoPorTipo(
        @Param("dataInicio") LocalDate dataInicio,
        @Param("dataFim") LocalDate dataFim
    );

    // === VERIFICAÇÕES ===

    /**
     * Verifica se existe dashboard para um período.
     *
     * @param periodo período
     * @param tipoPeriodo tipo do período
     * @return true se existe
     */
    boolean existsByPeriodoAndTipoPeriodo(String periodo, String tipoPeriodo);

    /**
     * Deleta dashboards antigos (útil para manutenção).
     *
     * @param dataLimite data limite (deleta tudo antes desta data)
     */
    void deleteByDataReferenciaLessThan(LocalDate dataLimite);

    /**
     * Busca último dashboard atualizado (para debugging).
     *
     * @return dashboard mais recentemente atualizado
     */
    @Query("""
        SELECT d FROM SinistroDashboardView d
        ORDER BY d.updatedAt DESC
        LIMIT 1
        """)
    Optional<SinistroDashboardView> findLastUpdated();
}
