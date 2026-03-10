package com.seguradora.hibrida.domain.apolice.query.repository;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repositório para consultas otimizadas de apólices.
 * 
 * <p>Fornece consultas especializadas para diferentes cenários
 * de negócio com performance otimizada.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface ApoliceQueryRepository extends JpaRepository<ApoliceQueryModel, String>, 
                                               JpaSpecificationExecutor<ApoliceQueryModel> {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca apólice por número.
     */
    Optional<ApoliceQueryModel> findByNumero(String numero);
    
    /**
     * Busca apólices por CPF do segurado.
     */
    List<ApoliceQueryModel> findBySeguradoCpfOrderByVigenciaInicioDesc(String cpf);
    
    /**
     * Busca apólices por ID do segurado.
     */
    List<ApoliceQueryModel> findBySeguradoIdOrderByVigenciaInicioDesc(String seguradoId);
    
    /**
     * Busca apólices por produto.
     */
    Page<ApoliceQueryModel> findByProdutoOrderByVigenciaInicioDesc(String produto, Pageable pageable);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca apólices por status.
     */
    Page<ApoliceQueryModel> findByStatusOrderByVigenciaFimAsc(StatusApolice status, Pageable pageable);
    
    /**
     * Busca apólices ativas.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.status = 'ATIVA' ORDER BY a.vigenciaFim ASC")
    Page<ApoliceQueryModel> findApolicesAtivas(Pageable pageable);
    
    /**
     * Busca apólices vencidas.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim < CURRENT_DATE ORDER BY a.vigenciaFim DESC")
    Page<ApoliceQueryModel> findApolicesVencidas(Pageable pageable);
    
    // === CONSULTAS POR VENCIMENTO ===
    
    /**
     * Busca apólices que vencem em um período.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim BETWEEN :inicio AND :fim AND a.status = 'ATIVA' ORDER BY a.vigenciaFim ASC")
    List<ApoliceQueryModel> findApolicesVencendoEntre(@Param("inicio") LocalDate inicio, 
                                                      @Param("fim") LocalDate fim);
    
    /**
     * Busca apólices que vencem nos próximos dias.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim <= :dataLimite AND a.vigenciaFim >= CURRENT_DATE AND a.status = 'ATIVA' ORDER BY a.vigenciaFim ASC")
    List<ApoliceQueryModel> findApolicesVencendoEm(@Param("dataLimite") LocalDate dataLimite);
    
    /**
     * Busca apólices com vencimento próximo (30 dias).
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vencimentoProximo = true AND a.status = 'ATIVA' ORDER BY a.diasParaVencimento ASC")
    List<ApoliceQueryModel> findApolicesComVencimentoProximo();
    
    // === MÉTODOS PARA NOTIFICAÇÕES ===
    
    /**
     * Busca apólices por vigência fim e status ordenadas por número.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim = :vigenciaFim AND a.status = :status ORDER BY a.numero")
    List<ApoliceQueryModel> findByVigenciaFimAndStatusOrderByNumeroApolice(@Param("vigenciaFim") LocalDate vigenciaFim, 
                                                                           @Param("status") String status);
    
    /**
     * Busca apólices com vigência fim menor que a data especificada.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim < :vigenciaFim AND a.status = :status ORDER BY a.vigenciaFim")
    List<ApoliceQueryModel> findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(@Param("vigenciaFim") LocalDate vigenciaFim, 
                                                                                 @Param("status") String status);
    
    /**
     * Busca apólices com vigência fim menor ou igual à data especificada.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim <= :vigenciaFim AND a.status = :status ORDER BY a.vigenciaFim")
    List<ApoliceQueryModel> findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(@Param("vigenciaFim") LocalDate vigenciaFim, 
                                                                                      @Param("status") String status);
    
    // === CONSULTAS POR SEGURADO ===
    
    /**
     * Busca apólices ativas por CPF do segurado.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.seguradoCpf = :cpf AND a.status = 'ATIVA' ORDER BY a.vigenciaFim ASC")
    List<ApoliceQueryModel> findApolicesAtivasPorCpf(@Param("cpf") String cpf);
    
    /**
     * Busca apólices por nome do segurado (busca parcial).
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE LOWER(a.seguradoNome) LIKE LOWER(CONCAT('%', :nome, '%')) ORDER BY a.seguradoNome, a.vigenciaFim DESC")
    Page<ApoliceQueryModel> findBySeguradoNomeContaining(@Param("nome") String nome, Pageable pageable);
    
    /**
     * Busca apólices por cidade do segurado.
     */
    Page<ApoliceQueryModel> findBySeguradoCidadeOrderByVigenciaFimDesc(String cidade, Pageable pageable);
    
    /**
     * Busca apólices por estado do segurado.
     */
    Page<ApoliceQueryModel> findBySeguradoEstadoOrderByVigenciaFimDesc(String estado, Pageable pageable);
    
    // === CONSULTAS POR COBERTURA ===
    
    /**
     * Busca apólices que possuem cobertura específica.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE :cobertura MEMBER OF a.coberturas ORDER BY a.vigenciaFim DESC")
    List<ApoliceQueryModel> findByCobertura(@Param("cobertura") TipoCobertura cobertura);
    
    /**
     * Busca apólices com cobertura total.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.temCoberturaTotal = true ORDER BY a.vigenciaFim DESC")
    List<ApoliceQueryModel> findApolicesComCoberturaTotal();
    
    // === CONSULTAS POR VALOR ===
    
    /**
     * Busca apólices por faixa de valor.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.valorTotal BETWEEN :valorMin AND :valorMax ORDER BY a.valorTotal DESC")
    Page<ApoliceQueryModel> findByValorTotalBetween(@Param("valorMin") BigDecimal valorMin, 
                                                    @Param("valorMax") BigDecimal valorMax, 
                                                    Pageable pageable);
    
    /**
     * Busca apólices com valor acima de um limite.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.valorTotal > :valor ORDER BY a.valorTotal DESC")
    List<ApoliceQueryModel> findApolicesAltoValor(@Param("valor") BigDecimal valor);
    
    // === CONSULTAS DE RENOVAÇÃO ===
    
    /**
     * Busca apólices elegíveis para renovação automática.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.renovacaoAutomatica = true AND a.vencimentoProximo = true AND a.scoreRenovacao >= 70 ORDER BY a.diasParaVencimento ASC")
    List<ApoliceQueryModel> findElegiveisRenovacaoAutomatica();
    
    /**
     * Busca apólices que precisam de atenção para renovação.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vencimentoProximo = true AND (a.renovacaoAutomatica = false OR a.scoreRenovacao < 70) ORDER BY a.diasParaVencimento ASC")
    List<ApoliceQueryModel> findPrecisandoAtencaoRenovacao();
    
    /**
     * Busca apólices por score de renovação.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.scoreRenovacao BETWEEN :scoreMin AND :scoreMax ORDER BY a.scoreRenovacao DESC")
    List<ApoliceQueryModel> findByScoreRenovacao(@Param("scoreMin") Integer scoreMin, 
                                                 @Param("scoreMax") Integer scoreMax);
    
    // === CONSULTAS ANALÍTICAS ===
    
    /**
     * Conta apólices por status.
     */
    @Query("SELECT a.status, COUNT(a) FROM ApoliceQueryModel a GROUP BY a.status")
    List<Object[]> countByStatus();
    
    /**
     * Conta apólices por produto.
     */
    @Query("SELECT a.produto, COUNT(a) FROM ApoliceQueryModel a GROUP BY a.produto ORDER BY COUNT(a) DESC")
    List<Object[]> countByProduto();
    
    /**
     * Conta apólices por estado do segurado.
     */
    @Query("SELECT a.seguradoEstado, COUNT(a) FROM ApoliceQueryModel a GROUP BY a.seguradoEstado ORDER BY COUNT(a) DESC")
    List<Object[]> countByEstado();
    
    /**
     * Estatísticas de valores por produto.
     */
    @Query("""
        SELECT a.produto, 
               COUNT(a) as quantidade,
               AVG(a.valorTotal) as valorMedio,
               SUM(a.valorTotal) as valorTotal,
               MIN(a.valorTotal) as valorMinimo,
               MAX(a.valorTotal) as valorMaximo
        FROM ApoliceQueryModel a 
        WHERE a.status = 'ATIVA'
        GROUP BY a.produto 
        ORDER BY SUM(a.valorTotal) DESC
        """)
    List<Object[]> getEstatisticasValoresPorProduto();
    
    /**
     * Relatório de vencimentos por mês.
     */
    @Query(value = """
        SELECT 
            EXTRACT(YEAR FROM vigencia_fim) as ano,
            EXTRACT(MONTH FROM vigencia_fim) as mes,
            COUNT(*) as quantidade,
            SUM(valor_total) as valor_total
        FROM projections.apolice_view 
        WHERE status = 'ATIVA' 
        AND vigencia_fim >= CURRENT_DATE
        GROUP BY EXTRACT(YEAR FROM vigencia_fim), EXTRACT(MONTH FROM vigencia_fim)
        ORDER BY ano, mes
        """, nativeQuery = true)
    List<Object[]> getRelatorioVencimentosPorMes();
    
    // === CONSULTAS DE PERFORMANCE ===
    
    /**
     * Busca apólices atualizadas recentemente.
     */
    List<ApoliceQueryModel> findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(java.time.Instant since);
    
    /**
     * Busca apólices por último evento processado.
     */
    List<ApoliceQueryModel> findByLastEventIdGreaterThanOrderByLastEventIdAsc(Long eventId);
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    /**
     * Busca com múltiplos filtros.
     */
    @Query("""
        SELECT a FROM ApoliceQueryModel a 
        WHERE (:status IS NULL OR a.status = :status)
        AND (:produto IS NULL OR a.produto = :produto)
        AND (:seguradoCpf IS NULL OR a.seguradoCpf = :seguradoCpf)
        AND (:vigenciaInicio IS NULL OR a.vigenciaInicio >= :vigenciaInicio)
        AND (:vigenciaFim IS NULL OR a.vigenciaFim <= :vigenciaFim)
        AND (:valorMin IS NULL OR a.valorTotal >= :valorMin)
        AND (:valorMax IS NULL OR a.valorTotal <= :valorMax)
        ORDER BY a.vigenciaFim DESC
        """)
    Page<ApoliceQueryModel> findWithFilters(@Param("status") StatusApolice status,
                                           @Param("produto") String produto,
                                           @Param("seguradoCpf") String seguradoCpf,
                                           @Param("vigenciaInicio") LocalDate vigenciaInicio,
                                           @Param("vigenciaFim") LocalDate vigenciaFim,
                                           @Param("valorMin") BigDecimal valorMin,
                                           @Param("valorMax") BigDecimal valorMax,
                                           Pageable pageable);
    
    // === VERIFICAÇÕES DE EXISTÊNCIA ===
    
    /**
     * Verifica se existe apólice com o número.
     */
    boolean existsByNumero(String numero);
    
    /**
     * Conta apólices ativas por segurado.
     */
    @Query("SELECT COUNT(a) FROM ApoliceQueryModel a WHERE a.seguradoCpf = :cpf AND a.status = 'ATIVA'")
    long countApolicesAtivasPorCpf(@Param("cpf") String cpf);
    
    /**
     * Verifica se segurado possui apólices ativas.
     */
    @Query("SELECT COUNT(a) > 0 FROM ApoliceQueryModel a WHERE a.seguradoCpf = :cpf AND a.status = 'ATIVA'")
    boolean seguradoPossuiApolicesAtivas(@Param("cpf") String cpf);
}