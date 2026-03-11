package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDetailView;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

/**
 * Repository para consultas detalhadas de sinistros.
 *
 * <p>Fornece acesso a views completas com todos os detalhes,
 * timeline, documentos e histórico de um sinistro específico.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SinistroDetailRepository extends JpaRepository<SinistroDetailView, UUID> {

    // === CONSULTAS PRINCIPAIS ===

    /**
     * Busca detalhes completos por protocolo.
     *
     * @param protocolo protocolo único do sinistro
     * @return detalhes completos se encontrado
     */
    Optional<SinistroDetailView> findByProtocolo(String protocolo);

    /**
     * Busca detalhes completos por protocolo com eager loading.
     * Otimiza carregamento de campos JSONB.
     *
     * @param protocolo protocolo único do sinistro
     * @return detalhes completos se encontrado
     */
    @Query("""
        SELECT DISTINCT s FROM SinistroDetailView s
        WHERE s.protocolo = :protocolo
        """)
    Optional<SinistroDetailView> findCompletoByProtocolo(@Param("protocolo") String protocolo);

    /**
     * Busca detalhes completos por ID.
     *
     * @param id ID do sinistro
     * @return detalhes completos se encontrado
     */
    @Query("""
        SELECT DISTINCT s FROM SinistroDetailView s
        WHERE s.id = :id
        """)
    Optional<SinistroDetailView> findCompletoById(@Param("id") UUID id);

    // === CONSULTAS POR RELACIONAMENTOS ===

    /**
     * Busca detalhes por ID do segurado.
     *
     * @param seguradoId ID do segurado
     * @return lista de detalhes de sinistros do segurado
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.seguradoId = :seguradoId
        ORDER BY s.dataOcorrencia DESC
        """)
    java.util.List<SinistroDetailView> findBySeguradoId(@Param("seguradoId") String seguradoId);

    /**
     * Busca detalhes por CPF do segurado.
     *
     * @param cpf CPF do segurado
     * @return lista de detalhes de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.seguradoCpf = :cpf
        ORDER BY s.dataOcorrencia DESC
        """)
    java.util.List<SinistroDetailView> findBySeguradoCpf(@Param("cpf") String cpf);

    /**
     * Busca detalhes por número da apólice.
     *
     * @param apoliceNumero número da apólice
     * @return lista de detalhes de sinistros da apólice
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.apoliceNumero = :apoliceNumero
        ORDER BY s.dataOcorrencia DESC
        """)
    java.util.List<SinistroDetailView> findByApoliceNumero(@Param("apoliceNumero") String apoliceNumero);

    /**
     * Busca detalhes por ID da apólice.
     *
     * @param apoliceId ID da apólice
     * @return lista de detalhes de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.apoliceId = :apoliceId
        ORDER BY s.dataOcorrencia DESC
        """)
    java.util.List<SinistroDetailView> findByApoliceId(@Param("apoliceId") String apoliceId);

    /**
     * Busca detalhes por placa do veículo.
     *
     * @param placa placa do veículo
     * @return lista de detalhes de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.veiculoPlaca = :placa
        ORDER BY s.dataOcorrencia DESC
        """)
    java.util.List<SinistroDetailView> findByVeiculoPlaca(@Param("placa") String placa);

    // === CONSULTAS POR ANALISTA ===

    /**
     * Busca sinistros em análise de um analista.
     *
     * @param analistaNome nome do analista
     * @return lista de sinistros em análise
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.analistaResponsavel = :analistaNome
        AND s.status IN ('EM_ANALISE', 'ABERTO', 'AGUARDANDO_DOCUMENTOS')
        ORDER BY s.prioridade DESC, s.dataOcorrencia ASC
        """)
    java.util.List<SinistroDetailView> findEmAnaliseByAnalista(@Param("analistaNome") String analistaNome);

    /**
     * Conta sinistros em análise de um analista.
     *
     * @param analistaNome nome do analista
     * @return quantidade de sinistros
     */
    @Query("""
        SELECT COUNT(s) FROM SinistroDetailView s
        WHERE s.analistaResponsavel = :analistaNome
        AND s.status IN ('EM_ANALISE', 'ABERTO', 'AGUARDANDO_DOCUMENTOS')
        """)
    long countEmAnaliseByAnalista(@Param("analistaNome") String analistaNome);

    // === CONSULTAS POR CONDIÇÕES ESPECÍFICAS ===

    /**
     * Busca sinistros com documentos pendentes.
     *
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.documentosPendentes = true
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.dataOcorrencia ASC
        """)
    java.util.List<SinistroDetailView> findComDocumentosPendentes();

    /**
     * Busca sinistros sem consulta Detran.
     *
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.consultaDetranRealizada = false
        AND s.status IN ('ABERTO', 'EM_ANALISE')
        ORDER BY s.dataAbertura ASC
        """)
    java.util.List<SinistroDetailView> findSemConsultaDetran();

    /**
     * Busca sinistros fora do SLA.
     *
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.dentroSla = false
        AND s.status NOT IN ('FECHADO', 'CANCELADO')
        ORDER BY s.dataAbertura ASC
        """)
    java.util.List<SinistroDetailView> findForaSla();

    /**
     * Busca sinistros que podem ser aprovados.
     * Critérios: documentos OK, Detran consultado, em análise.
     *
     * @return lista de sinistros prontos para aprovação
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.documentosPendentes = false
        AND s.consultaDetranRealizada = true
        AND s.status = 'EM_ANALISE'
        ORDER BY s.dataAbertura ASC
        """)
    java.util.List<SinistroDetailView> findProntosParaAprovacao();

    // === CONSULTAS DE DETRAN ===

    /**
     * Busca sinistros com restrições no Detran.
     *
     * @return lista de sinistros com restrições
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_detail_view s
        WHERE s.consulta_detran_realizada = true
        AND jsonb_array_length(
            COALESCE(s.historico_detran->'dados'->'restricoes', '[]'::jsonb)
        ) > 0
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    java.util.List<SinistroDetailView> findComRestricoesDetran();

    /**
     * Busca sinistros com multas pendentes (via Detran).
     *
     * @return lista de sinistros
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_detail_view s
        WHERE s.consulta_detran_realizada = true
        AND jsonb_array_length(
            COALESCE(s.historico_detran->'dados'->'multas', '[]'::jsonb)
        ) > 0
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    java.util.List<SinistroDetailView> findComMultasPendentes();

    // === CONSULTAS DE TIMELINE ===

    /**
     * Busca sinistros com eventos específicos na timeline.
     *
     * @param tipoEvento tipo do evento a buscar
     * @return lista de sinistros que possuem o evento
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_detail_view s
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements(s.timeline) AS evento
            WHERE evento->>'evento' = :tipoEvento
        )
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    java.util.List<SinistroDetailView> findComEventoNaTimeline(@Param("tipoEvento") String tipoEvento);

    /**
     * Busca sinistros com mais de N eventos na timeline.
     *
     * @param minEventos quantidade mínima de eventos
     * @return lista de sinistros
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_detail_view s
        WHERE jsonb_array_length(COALESCE(s.timeline, '[]'::jsonb)) >= :minEventos
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    java.util.List<SinistroDetailView> findComMuitosEventos(@Param("minEventos") int minEventos);

    // === CONSULTAS DE DOCUMENTOS ===

    /**
     * Busca sinistros com quantidade específica de documentos.
     *
     * @param minDocs quantidade mínima de documentos
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.quantidadeDocumentos >= :minDocs
        ORDER BY s.quantidadeDocumentos DESC
        """)
    java.util.List<SinistroDetailView> findComMinimoDocumentos(@Param("minDocs") int minDocs);

    /**
     * Busca sinistros sem documentos anexados.
     *
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE (s.quantidadeDocumentos IS NULL OR s.quantidadeDocumentos = 0)
        AND s.status IN ('ABERTO', 'EM_ANALISE')
        ORDER BY s.dataAbertura ASC
        """)
    java.util.List<SinistroDetailView> findSemDocumentos();

    /**
     * Busca sinistros com documentos de tipo específico.
     *
     * @param tipoDocumento tipo do documento
     * @return lista de sinistros
     */
    @Query(value = """
        SELECT * FROM projections.sinistro_detail_view s
        WHERE EXISTS (
            SELECT 1
            FROM jsonb_array_elements(s.documentos) AS doc
            WHERE doc->>'tipo' = :tipoDocumento
        )
        ORDER BY s.data_abertura DESC
        """, nativeQuery = true)
    java.util.List<SinistroDetailView> findComTipoDocumento(@Param("tipoDocumento") String tipoDocumento);

    // === CONSULTAS DE OBSERVAÇÕES ===

    /**
     * Busca sinistros com observações específicas.
     *
     * @param termo termo a buscar nas observações
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE UPPER(s.observacoes) LIKE UPPER(:termo)
        ORDER BY s.dataAbertura DESC
        """)
    java.util.List<SinistroDetailView> findComObservacoesContendo(@Param("termo") String termo);

    /**
     * Busca sinistros reprovados com motivo específico.
     *
     * @param termo termo a buscar no motivo de reprovação
     * @return lista de sinistros
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        WHERE s.status = 'REPROVADO'
        AND UPPER(s.motivoReprovacao) LIKE UPPER(:termo)
        ORDER BY s.dataFechamento DESC
        """)
    java.util.List<SinistroDetailView> findReprovadosComMotivo(@Param("termo") String termo);

    // === VERIFICAÇÕES ===

    /**
     * Verifica se existe detalhes para um protocolo.
     *
     * @param protocolo protocolo do sinistro
     * @return true se existe
     */
    boolean existsByProtocolo(String protocolo);

    /**
     * Conta sinistros de uma apólice.
     *
     * @param apoliceNumero número da apólice
     * @return quantidade de sinistros
     */
    long countByApoliceNumero(String apoliceNumero);

    /**
     * Conta sinistros de um segurado.
     *
     * @param seguradoCpf CPF do segurado
     * @return quantidade de sinistros
     */
    long countBySeguradoCpf(String seguradoCpf);

    // === MÉTODOS UTILITÁRIOS ===

    /**
     * Busca último sinistro atualizado (debug).
     *
     * @return último sinistro atualizado
     */
    @Query("""
        SELECT s FROM SinistroDetailView s
        ORDER BY s.updatedAt DESC
        LIMIT 1
        """)
    Optional<SinistroDetailView> findLastUpdated();

    /**
     * Busca sinistros atualizados após um timestamp.
     *
     * @param since timestamp
     * @return lista de sinistros atualizados
     */
    java.util.List<SinistroDetailView> findByUpdatedAtGreaterThanOrderByUpdatedAtDesc(
        java.time.Instant since
    );

    /**
     * Busca sinistros por último evento processado.
     *
     * @param eventId ID do último evento
     * @return lista de sinistros
     */
    java.util.List<SinistroDetailView> findByLastEventIdGreaterThanOrderByLastEventIdAsc(Long eventId);
}
