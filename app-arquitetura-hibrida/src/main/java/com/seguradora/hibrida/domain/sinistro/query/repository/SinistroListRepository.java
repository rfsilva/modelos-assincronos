package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository para consultas otimizadas de listagens de sinistros.
 *
 * <p>Fornece métodos eficientes para paginação e filtros em listas,
 * com foco em performance e usabilidade em interfaces de usuário.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SinistroListRepository extends JpaRepository<SinistroListView, UUID>,
                                               JpaSpecificationExecutor<SinistroListView> {

    // === CONSULTAS BÁSICAS ===

    /**
     * Busca sinistro por protocolo.
     *
     * @param protocolo protocolo único do sinistro
     * @return sinistro se encontrado
     */
    Optional<SinistroListView> findByProtocolo(String protocolo);

    /**
     * Verifica se existe sinistro com protocolo.
     *
     * @param protocolo protocolo do sinistro
     * @return true se existe
     */
    boolean existsByProtocolo(String protocolo);

    // === CONSULTAS POR STATUS ===

    /**
     * Busca sinistros por status com paginação.
     *
     * @param status status do sinistro
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByStatusOrderByDataOcorrenciaDesc(String status, Pageable pageable);

    /**
     * Busca sinistros abertos.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros abertos
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.status IN ('ABERTO', 'EM_ANALISE', 'AGUARDANDO_DOCUMENTOS')
        ORDER BY s.prioridade DESC, s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findSinistrosAbertos(Pageable pageable);

    /**
     * Busca sinistros finalizados.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros finalizados
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.status IN ('APROVADO', 'REPROVADO', 'FECHADO', 'CANCELADO')
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findSinistrosFinalizados(Pageable pageable);

    // === CONSULTAS POR SEGURADO ===

    /**
     * Busca sinistros por nome do segurado (LIKE).
     *
     * @param nomePattern padrão de busca (ex: %nome%)
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE UPPER(s.seguradoNome) LIKE UPPER(:nomePattern)
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findBySeguradoNomeContaining(
        @Param("nomePattern") String nomePattern,
        Pageable pageable
    );

    /**
     * Busca sinistros por CPF do segurado.
     *
     * @param cpf CPF do segurado
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findBySeguradoCpfOrderByDataOcorrenciaDesc(String cpf, Pageable pageable);

    /**
     * Conta sinistros de um segurado.
     *
     * @param cpf CPF do segurado
     * @return quantidade de sinistros
     */
    long countBySeguradoCpf(String cpf);

    // === CONSULTAS POR VEÍCULO ===

    /**
     * Busca sinistros por placa do veículo.
     *
     * @param placa placa do veículo
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByVeiculoPlacaOrderByDataOcorrenciaDesc(String placa, Pageable pageable);

    /**
     * Conta sinistros de um veículo.
     *
     * @param placa placa do veículo
     * @return quantidade de sinistros
     */
    long countByVeiculoPlaca(String placa);

    // === CONSULTAS POR PERÍODO ===

    /**
     * Busca sinistros por período de ocorrência.
     *
     * @param inicio data/hora inicial
     * @param fim data/hora final
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.dataOcorrencia BETWEEN :inicio AND :fim
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findByPeriodoOcorrencia(
        @Param("inicio") Instant inicio,
        @Param("fim") Instant fim,
        Pageable pageable
    );

    /**
     * Busca sinistros recentes (últimos N dias).
     *
     * @param dataLimite data limite
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.dataOcorrencia >= :dataLimite
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findSinistrosRecentes(
        @Param("dataLimite") Instant dataLimite,
        Pageable pageable
    );

    // === CONSULTAS POR ANALISTA ===

    /**
     * Busca sinistros por analista responsável.
     *
     * @param analistaNome nome do analista
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByAnalistaResponsavelOrderByDataOcorrenciaDesc(
        String analistaNome,
        Pageable pageable
    );

    /**
     * Busca sinistros sem analista atribuído.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.analistaResponsavel IS NULL
        AND s.status IN ('ABERTO', 'EM_ANALISE')
        ORDER BY s.prioridade DESC, s.dataOcorrencia ASC
        """)
    Page<SinistroListView> findSinistrosSemAnalista(Pageable pageable);

    /**
     * Conta sinistros de um analista.
     *
     * @param analistaNome nome do analista
     * @return quantidade de sinistros
     */
    long countByAnalistaResponsavel(String analistaNome);

    // === CONSULTAS POR LOCALIZAÇÃO ===

    /**
     * Busca sinistros por estado.
     *
     * @param uf sigla do estado
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByEstadoOcorrenciaOrderByDataOcorrenciaDesc(String uf, Pageable pageable);

    /**
     * Busca sinistros por cidade.
     *
     * @param cidade nome da cidade
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByCidadeOcorrenciaOrderByDataOcorrenciaDesc(String cidade, Pageable pageable);

    // === CONSULTAS POR TIPO ===

    /**
     * Busca sinistros por tipo.
     *
     * @param tipo tipo do sinistro
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByTipoOrderByDataOcorrenciaDesc(String tipo, Pageable pageable);

    /**
     * Conta sinistros por tipo.
     *
     * @param tipo tipo do sinistro
     * @return quantidade de sinistros
     */
    long countByTipo(String tipo);

    // === CONSULTAS POR PRIORIDADE E SLA ===

    /**
     * Busca sinistros urgentes.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros urgentes
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.prioridade = 'URGENTE'
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.dataOcorrencia ASC
        """)
    Page<SinistroListView> findSinistrosUrgentes(Pageable pageable);

    /**
     * Busca sinistros fora do SLA.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.dentroSla = false
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.dataOcorrencia ASC
        """)
    Page<SinistroListView> findSinistrosForaSla(Pageable pageable);

    /**
     * Busca sinistros que precisam de atenção.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE (s.prioridade IN ('ALTA', 'URGENTE')
           OR s.dentroSla = false
           OR s.documentosPendentes = true)
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.prioridade DESC, s.dataOcorrencia ASC
        """)
    Page<SinistroListView> findSinistrosComAtencao(Pageable pageable);

    // === CONSULTAS POR DOCUMENTOS ===

    /**
     * Busca sinistros com documentos pendentes.
     *
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE s.documentosPendentes = true
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.dataOcorrencia ASC
        """)
    Page<SinistroListView> findSinistrosComDocumentosPendentes(Pageable pageable);

    /**
     * Busca sinistros com Detran consultado.
     *
     * @param consultado true/false
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    Page<SinistroListView> findByDetranConsultadoOrderByDataOcorrenciaDesc(
        Boolean consultado,
        Pageable pageable
    );

    // === BUSCA AVANÇADA COM FILTROS MÚLTIPLOS ===

    /**
     * Busca sinistros com múltiplos filtros opcionais.
     *
     * @param status status (null = todos)
     * @param tipo tipo (null = todos)
     * @param analista analista (null = todos)
     * @param estado estado (null = todos)
     * @param prioridade prioridade (null = todas)
     * @param dataInicio data inicial (null = sem limite)
     * @param dataFim data final (null = sem limite)
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE (:status IS NULL OR s.status = :status)
        AND (:tipo IS NULL OR s.tipo = :tipo)
        AND (:analista IS NULL OR s.analistaResponsavel = :analista)
        AND (:estado IS NULL OR s.estadoOcorrencia = :estado)
        AND (:prioridade IS NULL OR s.prioridade = :prioridade)
        AND (:dataInicio IS NULL OR s.dataOcorrencia >= :dataInicio)
        AND (:dataFim IS NULL OR s.dataOcorrencia <= :dataFim)
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> findWithFilters(
        @Param("status") String status,
        @Param("tipo") String tipo,
        @Param("analista") String analista,
        @Param("estado") String estado,
        @Param("prioridade") String prioridade,
        @Param("dataInicio") Instant dataInicio,
        @Param("dataFim") Instant dataFim,
        Pageable pageable
    );

    // === BUSCA POR TEXTO ===

    /**
     * Busca por texto livre (protocolo, segurado, placa).
     *
     * @param termo termo de busca
     * @param pageable configuração de paginação
     * @return página de sinistros
     */
    @Query("""
        SELECT s FROM SinistroListView s
        WHERE UPPER(s.protocolo) LIKE UPPER(:termo)
        OR UPPER(s.seguradoNome) LIKE UPPER(:termo)
        OR UPPER(s.veiculoPlaca) LIKE UPPER(:termo)
        OR UPPER(s.seguradoCpf) LIKE UPPER(:termo)
        ORDER BY s.dataOcorrencia DESC
        """)
    Page<SinistroListView> searchByText(
        @Param("termo") String termo,
        Pageable pageable
    );

    // === ESTATÍSTICAS RÁPIDAS ===

    /**
     * Conta sinistros por status.
     *
     * @return lista de [status, count]
     */
    @Query("""
        SELECT s.status, COUNT(s)
        FROM SinistroListView s
        GROUP BY s.status
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> countByStatus();

    /**
     * Conta sinistros por tipo.
     *
     * @return lista de [tipo, count]
     */
    @Query("""
        SELECT s.tipo, COUNT(s)
        FROM SinistroListView s
        GROUP BY s.tipo
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> countByTipo();

    /**
     * Conta sinistros por prioridade.
     *
     * @return lista de [prioridade, count]
     */
    @Query("""
        SELECT s.prioridade, COUNT(s)
        FROM SinistroListView s
        WHERE s.status NOT IN ('FECHADO', 'CANCELADO')
        GROUP BY s.prioridade
        ORDER BY
            CASE s.prioridade
                WHEN 'URGENTE' THEN 1
                WHEN 'ALTA' THEN 2
                WHEN 'NORMAL' THEN 3
                WHEN 'BAIXA' THEN 4
            END
        """)
    List<Object[]> countByPrioridade();

    /**
     * Conta sinistros por estado.
     *
     * @return lista de [estado, count]
     */
    @Query("""
        SELECT s.estadoOcorrencia, COUNT(s)
        FROM SinistroListView s
        WHERE s.estadoOcorrencia IS NOT NULL
        GROUP BY s.estadoOcorrencia
        ORDER BY COUNT(s) DESC
        """)
    List<Object[]> countByEstado();

    /**
     * Resumo executivo de métricas.
     *
     * @return array com [total, abertos, finalizados, urgentes, foraSla]
     */
    @Query(value = """
        SELECT
            COUNT(*) as total,
            COUNT(CASE WHEN status IN ('ABERTO', 'EM_ANALISE', 'AGUARDANDO_DOCUMENTOS') THEN 1 END) as abertos,
            COUNT(CASE WHEN status IN ('APROVADO', 'REPROVADO', 'FECHADO', 'CANCELADO') THEN 1 END) as finalizados,
            COUNT(CASE WHEN prioridade = 'URGENTE' THEN 1 END) as urgentes,
            COUNT(CASE WHEN dentro_sla = false THEN 1 END) as fora_sla
        FROM projections.sinistro_list_view
        """, nativeQuery = true)
    Object[] getResumoExecutivo();

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Busca sinistros atualizados recentemente.
     *
     * @param since timestamp a partir do qual buscar
     * @return lista de sinistros atualizados
     */
    List<SinistroListView> findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(Instant since);

    /**
     * Deleta registros de sinistros cancelados antigos (manutenção).
     *
     * @param dataLimite data limite
     */
    @Query("""
        DELETE FROM SinistroListView s
        WHERE s.status = 'CANCELADO'
        AND s.dataAbertura < :dataLimite
        """)
    void deleteOldCanceledSinistros(@Param("dataLimite") Instant dataLimite);
}
