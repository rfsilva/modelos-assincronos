package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository otimizado para consultas de listagem de segurados.
 * 
 * <p>Implementa consultas customizadas conforme US011:
 * <ul>
 *   <li>Consultas por CPF, email, nome</li>
 *   <li>Filtros por status e cidade</li>
 *   <li>Consultas por período</li>
 *   <li>Paginação otimizada</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SeguradoListViewRepository extends JpaRepository<SeguradoListView, String> {
    
    /**
     * Busca segurado por CPF.
     */
    Optional<SeguradoListView> findByCpf(String cpf);
    
    /**
     * Busca segurado por email.
     */
    Optional<SeguradoListView> findByEmail(String email);
    
    /**
     * Verifica se CPF já existe.
     */
    boolean existsByCpf(String cpf);
    
    /**
     * Verifica se email já existe.
     */
    boolean existsByEmail(String email);
    
    /**
     * Busca segurados por nome (contém).
     */
    @Query("SELECT s FROM SeguradoListView s WHERE UPPER(s.nome) LIKE UPPER(CONCAT('%', :nome, '%'))")
    Page<SeguradoListView> findByNomeContaining(@Param("nome") String nome, Pageable pageable);
    
    /**
     * Busca segurados por status e cidade.
     */
    Page<SeguradoListView> findByStatusAndCidade(StatusSegurado status, String cidade, Pageable pageable);
    
    /**
     * Busca segurados por status.
     */
    Page<SeguradoListView> findByStatus(StatusSegurado status, Pageable pageable);
    
    /**
     * Busca segurados por cidade.
     */
    Page<SeguradoListView> findByCidade(String cidade, Pageable pageable);
    
    /**
     * Busca segurados por estado.
     */
    Page<SeguradoListView> findByEstado(String estado, Pageable pageable);
    
    /**
     * Busca segurados criados em período.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE s.dataCriacao BETWEEN :inicio AND :fim ORDER BY s.dataCriacao DESC")
    Page<SeguradoListView> findByDataCriacaoBetween(@Param("inicio") Instant inicio, 
                                                   @Param("fim") Instant fim, 
                                                   Pageable pageable);
    
    /**
     * Busca segurados por faixa etária.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE s.idade BETWEEN :idadeMin AND :idadeMax")
    Page<SeguradoListView> findByIdadeBetween(@Param("idadeMin") Integer idadeMin, 
                                             @Param("idadeMax") Integer idadeMax, 
                                             Pageable pageable);
    
    /**
     * Busca segurados com WhatsApp.
     */
    Page<SeguradoListView> findByTemWhatsappTrue(Pageable pageable);
    
    /**
     * Busca fuzzy por nome usando LIKE com múltiplas variações.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE " +
           "UPPER(s.nome) LIKE UPPER(CONCAT('%', :termo, '%')) OR " +
           "UPPER(s.nome) LIKE UPPER(CONCAT(:termo, '%')) OR " +
           "UPPER(s.nome) LIKE UPPER(CONCAT('%', :termo)) " +
           "ORDER BY " +
           "CASE " +
           "  WHEN UPPER(s.nome) LIKE UPPER(CONCAT(:termo, '%')) THEN 1 " +
           "  WHEN UPPER(s.nome) LIKE UPPER(CONCAT('%', :termo, '%')) THEN 2 " +
           "  ELSE 3 " +
           "END")
    Page<SeguradoListView> findByNomeFuzzy(@Param("termo") String termo, Pageable pageable);
    
    /**
     * Busca segurados por múltiplos critérios.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE " +
           "(:nome IS NULL OR UPPER(s.nome) LIKE UPPER(CONCAT('%', :nome, '%'))) AND " +
           "(:cpf IS NULL OR s.cpf = :cpf) AND " +
           "(:email IS NULL OR UPPER(s.email) LIKE UPPER(CONCAT('%', :email, '%'))) AND " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:cidade IS NULL OR UPPER(s.cidade) LIKE UPPER(CONCAT('%', :cidade, '%'))) AND " +
           "(:estado IS NULL OR s.estado = :estado)")
    Page<SeguradoListView> findWithMultipleCriteria(
        @Param("nome") String nome,
        @Param("cpf") String cpf,
        @Param("email") String email,
        @Param("status") StatusSegurado status,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        Pageable pageable
    );
    
    /**
     * Busca segurados por múltiplos critérios avançados.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE " +
           "(:status IS NULL OR s.status = :status) AND " +
           "(:cidade IS NULL OR s.cidade = :cidade) AND " +
           "(:estado IS NULL OR s.estado = :estado) AND " +
           "(:idadeMin IS NULL OR s.idade >= :idadeMin) AND " +
           "(:idadeMax IS NULL OR s.idade <= :idadeMax) AND " +
           "(:temWhatsapp IS NULL OR s.temWhatsapp = :temWhatsapp)")
    Page<SeguradoListView> findByMultiplosCriterios(
        @Param("status") StatusSegurado status,
        @Param("cidade") String cidade,
        @Param("estado") String estado,
        @Param("idadeMin") Integer idadeMin,
        @Param("idadeMax") Integer idadeMax,
        @Param("temWhatsapp") Boolean temWhatsapp,
        Pageable pageable
    );
    
    /**
     * Conta segurados por status.
     */
    long countByStatus(StatusSegurado status);
    
    /**
     * Conta segurados por cidade.
     */
    long countByCidade(String cidade);
    
    /**
     * Busca segurados recém-criados (últimas 24h).
     */
    @Query("SELECT s FROM SeguradoListView s WHERE s.dataCriacao >= :desde ORDER BY s.dataCriacao DESC")
    List<SeguradoListView> findRecentlyCreated(@Param("desde") Instant desde);
    
    /**
     * Busca segurados atualizados recentemente.
     */
    @Query("SELECT s FROM SeguradoListView s WHERE s.dataUltimaAtualizacao >= :desde ORDER BY s.dataUltimaAtualizacao DESC")
    List<SeguradoListView> findRecentlyUpdated(@Param("desde") Instant desde);
}