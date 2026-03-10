package com.seguradora.hibrida.domain.analytics.repository;

import com.seguradora.hibrida.domain.analytics.model.AnalyticsProjection;
import com.seguradora.hibrida.domain.analytics.model.TipoMetrica;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para projeções analíticas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface AnalyticsProjectionRepository extends JpaRepository<AnalyticsProjection, String> {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca projeção por data e tipo.
     */
    Optional<AnalyticsProjection> findByDataReferenciaAndTipoMetrica(LocalDate dataReferencia, TipoMetrica tipoMetrica);
    
    /**
     * Busca projeção por data, tipo e dimensão.
     */
    Optional<AnalyticsProjection> findByDataReferenciaAndTipoMetricaAndDimensaoAndValorDimensao(
        LocalDate dataReferencia, TipoMetrica tipoMetrica, String dimensao, String valorDimensao);
    
    /**
     * Busca projeções por período.
     */
    List<AnalyticsProjection> findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia(
        LocalDate inicio, LocalDate fim, TipoMetrica tipoMetrica);
    
    /**
     * Busca projeções por tipo.
     */
    List<AnalyticsProjection> findByTipoMetricaOrderByDataReferenciaDesc(TipoMetrica tipoMetrica);
    
    // === CONSULTAS TEMPORAIS ===
    
    /**
     * Busca métricas dos últimos N dias.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia >= :dataInicio AND a.tipoMetrica = :tipo ORDER BY a.dataReferencia DESC")
    List<AnalyticsProjection> findUltimosDias(@Param("dataInicio") LocalDate dataInicio, @Param("tipo") TipoMetrica tipo);
    
    /**
     * Busca métricas mensais do ano.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE YEAR(a.dataReferencia) = :ano AND a.tipoMetrica = 'MENSAL' ORDER BY a.dataReferencia")
    List<AnalyticsProjection> findMetricasMensaisDoAno(@Param("ano") int ano);
    
    /**
     * Busca métricas trimestrais do ano.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE YEAR(a.dataReferencia) = :ano AND a.tipoMetrica = 'TRIMESTRAL' ORDER BY a.dataReferencia")
    List<AnalyticsProjection> findMetricasTrimestraisDoAno(@Param("ano") int ano);
    
    // === CONSULTAS DIMENSIONAIS ===
    
    /**
     * Busca métricas por região na data.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_REGIAO' ORDER BY a.valorDimensao")
    List<AnalyticsProjection> findMetricasPorRegiaoNaData(@Param("data") LocalDate data);
    
    /**
     * Busca métricas por produto na data.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_PRODUTO' ORDER BY a.valorDimensao")
    List<AnalyticsProjection> findMetricasPorProdutoNaData(@Param("data") LocalDate data);
    
    /**
     * Busca métricas por canal na data.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_CANAL' ORDER BY a.valorDimensao")
    List<AnalyticsProjection> findMetricasPorCanalNaData(@Param("data") LocalDate data);
    
    /**
     * Busca métricas por faixa etária na data.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_FAIXA_ETARIA' ORDER BY a.valorDimensao")
    List<AnalyticsProjection> findMetricasPorFaixaEtariaNaData(@Param("data") LocalDate data);
    
    // === CONSULTAS DE AGREGAÇÃO ===
    
    /**
     * Soma total de segurados por período.
     */
    @Query("SELECT SUM(a.totalSegurados) FROM AnalyticsProjection a WHERE a.dataReferencia BETWEEN :inicio AND :fim AND a.tipoMetrica = :tipo")
    Long sumTotalSeguradosPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("tipo") TipoMetrica tipo);
    
    /**
     * Soma total de apólices por período.
     */
    @Query("SELECT SUM(a.totalApolices) FROM AnalyticsProjection a WHERE a.dataReferencia BETWEEN :inicio AND :fim AND a.tipoMetrica = :tipo")
    Long sumTotalApolicesPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("tipo") TipoMetrica tipo);
    
    /**
     * Média de prêmio por período.
     */
    @Query("SELECT AVG(a.premioMedio) FROM AnalyticsProjection a WHERE a.dataReferencia BETWEEN :inicio AND :fim AND a.tipoMetrica = :tipo")
    Double avgPremioPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("tipo") TipoMetrica tipo);
    
    /**
     * Taxa média de renovação por período.
     */
    @Query("SELECT AVG(a.taxaRenovacao) FROM AnalyticsProjection a WHERE a.dataReferencia BETWEEN :inicio AND :fim AND a.tipoMetrica = :tipo")
    Double avgTaxaRenovacaoPorPeriodo(@Param("inicio") LocalDate inicio, @Param("fim") LocalDate fim, @Param("tipo") TipoMetrica tipo);
    
    // === CONSULTAS DE RANKING ===
    
    /**
     * Top regiões por número de apólices.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_REGIAO' ORDER BY a.totalApolices DESC")
    List<AnalyticsProjection> findTopRegioesPorApolices(@Param("data") LocalDate data, Pageable pageable);
    
    /**
     * Top produtos por receita.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_PRODUTO' ORDER BY a.premioTotal DESC")
    List<AnalyticsProjection> findTopProdutosPorReceita(@Param("data") LocalDate data, Pageable pageable);
    
    /**
     * Top canais por volume.
     */
    @Query("SELECT a FROM AnalyticsProjection a WHERE a.dataReferencia = :data AND a.tipoMetrica = 'POR_CANAL' ORDER BY a.novasApolices DESC")
    List<AnalyticsProjection> findTopCanaisPorVolume(@Param("data") LocalDate data, Pageable pageable);
    
    // === CONSULTAS DE TENDÊNCIA ===
    
    /**
     * Crescimento de segurados nos últimos meses.
     */
    @Query("""
        SELECT a.dataReferencia, a.novosSegurados 
        FROM AnalyticsProjection a 
        WHERE a.dataReferencia >= :dataInicio 
        AND a.tipoMetrica = 'MENSAL' 
        ORDER BY a.dataReferencia
        """)
    List<Object[]> findCrescimentoSegurados(@Param("dataInicio") LocalDate dataInicio);
    
    /**
     * Evolução de receita nos últimos meses.
     */
    @Query("""
        SELECT a.dataReferencia, a.premioTotal 
        FROM AnalyticsProjection a 
        WHERE a.dataReferencia >= :dataInicio 
        AND a.tipoMetrica = 'MENSAL' 
        ORDER BY a.dataReferencia
        """)
    List<Object[]> findEvolucaoReceita(@Param("dataInicio") LocalDate dataInicio);
    
    /**
     * Tendência de taxa de renovação.
     */
    @Query("""
        SELECT a.dataReferencia, a.taxaRenovacao 
        FROM AnalyticsProjection a 
        WHERE a.dataReferencia >= :dataInicio 
        AND a.tipoMetrica = 'MENSAL' 
        ORDER BY a.dataReferencia
        """)
    List<Object[]> findTendenciaTaxaRenovacao(@Param("dataInicio") LocalDate dataInicio);
    
    // === CONSULTAS DE COMPARAÇÃO ===
    
    /**
     * Compara métricas entre dois períodos.
     */
    @Query("""
        SELECT 
            SUM(CASE WHEN a.dataReferencia BETWEEN :inicioAtual AND :fimAtual THEN a.totalApolices ELSE 0 END) as atualApolices,
            SUM(CASE WHEN a.dataReferencia BETWEEN :inicioAnterior AND :fimAnterior THEN a.totalApolices ELSE 0 END) as anteriorApolices,
            SUM(CASE WHEN a.dataReferencia BETWEEN :inicioAtual AND :fimAtual THEN a.premioTotal ELSE 0 END) as atualReceita,
            SUM(CASE WHEN a.dataReferencia BETWEEN :inicioAnterior AND :fimAnterior THEN a.premioTotal ELSE 0 END) as anteriorReceita
        FROM AnalyticsProjection a 
        WHERE a.tipoMetrica = :tipo
        """)
    Object[] comparePeríodos(@Param("inicioAtual") LocalDate inicioAtual, 
                           @Param("fimAtual") LocalDate fimAtual,
                           @Param("inicioAnterior") LocalDate inicioAnterior, 
                           @Param("fimAnterior") LocalDate fimAnterior,
                           @Param("tipo") TipoMetrica tipo);
    
    // === CONSULTAS DE DASHBOARD ===
    
    /**
     * Resumo executivo para dashboard.
     */
    @Query("""
        SELECT 
            SUM(a.totalSegurados) as totalSegurados,
            SUM(a.totalApolices) as totalApolices,
            SUM(a.premioTotal) as receitaTotal,
            AVG(a.taxaRenovacao) as taxaRenovacaoMedia,
            AVG(a.taxaCancelamento) as taxaCancelamentoMedia
        FROM AnalyticsProjection a 
        WHERE a.dataReferencia = :data 
        AND a.tipoMetrica = 'GERAL'
        """)
    Object[] getResumoExecutivo(@Param("data") LocalDate data);
    
    /**
     * Métricas de performance atual.
     */
    @Query("""
        SELECT 
            a.novosSegurados,
            a.novasApolices,
            a.renovacoes,
            a.apolicesCanceladas,
            a.premioMedio,
            a.scoreMedioRenovacao
        FROM AnalyticsProjection a 
        WHERE a.dataReferencia = :data 
        AND a.tipoMetrica = 'PERFORMANCE'
        """)
    Object[] getMetricasPerformance(@Param("data") LocalDate data);
    
    // === LIMPEZA ===
    
    /**
     * Remove projeções antigas.
     */
    @Query("DELETE FROM AnalyticsProjection a WHERE a.dataReferencia < :dataLimite")
    int deleteByDataReferenciaLessThan(@Param("dataLimite") LocalDate dataLimite);
    
    /**
     * Conta projeções por tipo.
     */
    long countByTipoMetrica(TipoMetrica tipoMetrica);
    
    /**
     * Verifica se existe projeção para data e tipo.
     */
    boolean existsByDataReferenciaAndTipoMetrica(LocalDate dataReferencia, TipoMetrica tipoMetrica);
}