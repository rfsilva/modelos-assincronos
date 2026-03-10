package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para consultas otimizadas de sinistros.
 * 
 * <p>Fornece métodos de consulta específicos para o Query Side,
 * incluindo consultas complexas, full-text search e agregações.
 */
@Repository
public interface SinistroQueryRepository extends JpaRepository<SinistroQueryModel, UUID>, 
                                               JpaSpecificationExecutor<SinistroQueryModel> {
    
    // === CONSULTAS BÁSICAS ===
    
    /**
     * Busca sinistro por protocolo.
     */
    Optional<SinistroQueryModel> findByProtocolo(String protocolo);
    
    /**
     * Busca sinistros por CPF do segurado.
     */
    List<SinistroQueryModel> findByCpfSeguradoOrderByDataAberturaDesc(String cpfSegurado);
    
    /**
     * Busca sinistros por placa do veículo.
     */
    List<SinistroQueryModel> findByPlacaOrderByDataAberturaDesc(String placa);
    
    /**
     * Busca sinistros por número da apólice.
     */
    List<SinistroQueryModel> findByApoliceNumeroOrderByDataAberturaDesc(String apoliceNumero);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca sinistros por status com paginação.
     */
    Page<SinistroQueryModel> findByStatusOrderByDataAberturaDesc(String status, Pageable pageable);
    
    /**
     * Busca sinistros abertos (múltiplos status).
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.status IN ('ABERTO', 'EM_ANALISE') ORDER BY s.dataAbertura DESC")
    Page<SinistroQueryModel> findSinistrosAbertos(Pageable pageable);
    
    /**
     * Busca sinistros fechados (múltiplos status).
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.status IN ('FECHADO', 'CANCELADO') ORDER BY s.dataFechamento DESC")
    Page<SinistroQueryModel> findSinistrosFechados(Pageable pageable);
    
    // === CONSULTAS POR PERÍODO ===
    
    /**
     * Busca sinistros por período de ocorrência.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.dataOcorrencia BETWEEN :inicio AND :fim ORDER BY s.dataOcorrencia DESC")
    Page<SinistroQueryModel> findByPeriodoOcorrencia(@Param("inicio") Instant inicio, 
                                                     @Param("fim") Instant fim, 
                                                     Pageable pageable);
    
    /**
     * Busca sinistros por período de abertura.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.dataAbertura BETWEEN :inicio AND :fim ORDER BY s.dataAbertura DESC")
    Page<SinistroQueryModel> findByPeriodoAbertura(@Param("inicio") Instant inicio, 
                                                   @Param("fim") Instant fim, 
                                                   Pageable pageable);
    
    // === CONSULTAS POR OPERADOR ===
    
    /**
     * Busca sinistros por operador responsável.
     */
    Page<SinistroQueryModel> findByOperadorResponsavelOrderByDataAberturaDesc(String operador, Pageable pageable);
    
    /**
     * Busca sinistros sem operador responsável.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.operadorResponsavel IS NULL ORDER BY s.dataAbertura ASC")
    Page<SinistroQueryModel> findSinistrosSemOperador(Pageable pageable);
    
    // === FULL-TEXT SEARCH ===
    
    /**
     * Busca por texto livre usando full-text search do PostgreSQL.
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    List<SinistroQueryModel> findByFullTextSearch(@Param("termo") String termo);
    
    /**
     * Busca por texto livre com paginação.
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        ORDER BY s.data_abertura DESC
        LIMIT :#{#pageable.pageSize} OFFSET :#{#pageable.offset}
        """, 
        countQuery = """
        SELECT COUNT(*) FROM projections.sinistro_view s 
        WHERE to_tsvector('portuguese', 
            COALESCE(s.protocolo, '') || ' ' || 
            COALESCE(s.nome_segurado, '') || ' ' || 
            COALESCE(s.descricao, '') || ' ' ||
            COALESCE(s.placa, '') || ' ' ||
            COALESCE(s.apolice_numero, '')
        ) @@ plainto_tsquery('portuguese', :termo)
        """, nativeQuery = true)
    Page<SinistroQueryModel> findByFullTextSearchPaged(@Param("termo") String termo, Pageable pageable);
    
    // === CONSULTAS POR TAGS ===
    
    /**
     * Busca sinistros que possuem uma tag específica.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE :tag = ANY(s.tags) ORDER BY s.dataAbertura DESC")
    List<SinistroQueryModel> findByTag(@Param("tag") String tag);
    
    /**
     * Busca sinistros que possuem qualquer uma das tags especificadas.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.tags && CAST(:tags AS text[]) ORDER BY s.dataAbertura DESC")
    List<SinistroQueryModel> findByAnyTag(@Param("tags") String[] tags);
    
    // === CONSULTAS DETRAN ===
    
    /**
     * Busca sinistros que ainda não tiveram consulta ao DETRAN.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.consultaDetranRealizada = false OR s.consultaDetranRealizada IS NULL ORDER BY s.dataAbertura ASC")
    List<SinistroQueryModel> findSinistrosSemConsultaDetran();
    
    /**
     * Busca sinistros com consulta DETRAN com erro.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.consultaDetranStatus = 'ERROR' ORDER BY s.consultaDetranTimestamp DESC")
    List<SinistroQueryModel> findSinistrosComErroDetran();
    
    // === CONSULTAS DE AGREGAÇÃO ===
    
    /**
     * Conta sinistros por status.
     */
    @Query("SELECT s.status, COUNT(s) FROM SinistroQueryModel s GROUP BY s.status")
    List<Object[]> countByStatus();
    
    /**
     * Conta sinistros por tipo.
     */
    @Query("SELECT s.tipoSinistro, COUNT(s) FROM SinistroQueryModel s GROUP BY s.tipoSinistro ORDER BY COUNT(s) DESC")
    List<Object[]> countByTipo();
    
    /**
     * Conta sinistros por operador.
     */
    @Query("SELECT s.operadorResponsavel, COUNT(s) FROM SinistroQueryModel s WHERE s.operadorResponsavel IS NOT NULL GROUP BY s.operadorResponsavel ORDER BY COUNT(s) DESC")
    List<Object[]> countByOperador();
    
    /**
     * Estatísticas de sinistros por período.
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('day', s.data_abertura) as dia,
            COUNT(*) as total,
            COUNT(CASE WHEN s.status IN ('ABERTO', 'EM_ANALISE') THEN 1 END) as abertos,
            COUNT(CASE WHEN s.status IN ('FECHADO', 'CANCELADO') THEN 1 END) as fechados
        FROM projections.sinistro_view s 
        WHERE s.data_abertura BETWEEN :inicio AND :fim
        GROUP BY DATE_TRUNC('day', s.data_abertura)
        ORDER BY dia DESC
        """, nativeQuery = true)
    List<Object[]> getEstatisticasPorDia(@Param("inicio") Instant inicio, @Param("fim") Instant fim);
    
    // === CONSULTAS DE PERFORMANCE ===
    
    /**
     * Busca sinistros atualizados recentemente.
     */
    List<SinistroQueryModel> findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(Instant since);
    
    /**
     * Busca sinistros por último evento processado.
     */
    List<SinistroQueryModel> findByLastEventIdGreaterThanOrderByLastEventIdAsc(Long eventId);
    
    // === CONSULTAS CUSTOMIZADAS ===
    
    /**
     * Busca sinistros com filtros múltiplos.
     */
    @Query("""
        SELECT s FROM SinistroQueryModel s 
        WHERE (:status IS NULL OR s.status = :status)
        AND (:tipoSinistro IS NULL OR s.tipoSinistro = :tipoSinistro)
        AND (:operador IS NULL OR s.operadorResponsavel = :operador)
        AND (:cpfSegurado IS NULL OR s.cpfSegurado = :cpfSegurado)
        AND (:dataInicio IS NULL OR s.dataAbertura >= :dataInicio)
        AND (:dataFim IS NULL OR s.dataAbertura <= :dataFim)
        ORDER BY s.dataAbertura DESC
        """)
    Page<SinistroQueryModel> findWithFilters(@Param("status") String status,
                                            @Param("tipoSinistro") String tipoSinistro,
                                            @Param("operador") String operador,
                                            @Param("cpfSegurado") String cpfSegurado,
                                            @Param("dataInicio") Instant dataInicio,
                                            @Param("dataFim") Instant dataFim,
                                            Pageable pageable);
    
    /**
     * Busca sinistros próximos ao vencimento da apólice.
     */
    @Query("""
        SELECT s FROM SinistroQueryModel s 
        WHERE s.apoliceVigenciaFim BETWEEN :hoje AND :limite
        AND s.status IN ('ABERTO', 'EM_ANALISE')
        ORDER BY s.apoliceVigenciaFim ASC
        """)
    List<SinistroQueryModel> findSinistrosProximosVencimento(@Param("hoje") LocalDate hoje, 
                                                            @Param("limite") LocalDate limite);
    
    /**
     * Busca sinistros com valor estimado acima de um threshold.
     */
    @Query("SELECT s FROM SinistroQueryModel s WHERE s.valorEstimado > :valor ORDER BY s.valorEstimado DESC")
    List<SinistroQueryModel> findSinistrosAltoValor(@Param("valor") java.math.BigDecimal valor);
    
    // === CONSULTAS DE DASHBOARD ===
    
    /**
     * Obtém resumo executivo de sinistros.
     */
    @Query(value = """
        SELECT 
            COUNT(*) as total,
            COUNT(CASE WHEN status IN ('ABERTO', 'EM_ANALISE') THEN 1 END) as abertos,
            COUNT(CASE WHEN status IN ('FECHADO', 'CANCELADO') THEN 1 END) as fechados,
            AVG(CASE WHEN valor_estimado IS NOT NULL THEN valor_estimado ELSE 0 END) as valor_medio,
            COUNT(CASE WHEN consulta_detran_realizada = true THEN 1 END) as com_detran
        FROM projections.sinistro_view
        WHERE data_abertura >= :desde
        """, nativeQuery = true)
    Object[] getResumoExecutivo(@Param("desde") Instant desde);
    
    /**
     * Verifica se existe sinistro com protocolo.
     */
    boolean existsByProtocolo(String protocolo);
    
    /**
     * Conta sinistros por CPF do segurado.
     */
    long countByCpfSegurado(String cpfSegurado);
    
    /**
     * Conta sinistros por placa.
     */
    long countByPlaca(String placa);
}