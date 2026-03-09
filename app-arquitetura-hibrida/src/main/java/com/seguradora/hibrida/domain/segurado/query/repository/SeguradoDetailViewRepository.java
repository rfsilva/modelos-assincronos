package com.seguradora.hibrida.domain.segurado.query.repository;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoDetailView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * Repository para consultas detalhadas de segurados.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Repository
public interface SeguradoDetailViewRepository extends JpaRepository<SeguradoDetailView, String> {
    
    /**
     * Busca segurado detalhado por CPF.
     */
    Optional<SeguradoDetailView> findByCpf(String cpf);
    
    /**
     * Busca segurado detalhado por email.
     */
    Optional<SeguradoDetailView> findByEmail(String email);
    
    /**
     * Busca segurados com apólices ativas.
     */
    @Query("SELECT s FROM SeguradoDetailView s WHERE s.totalApolicesAtivas > 0 ORDER BY s.valorTotalApolices DESC")
    List<SeguradoDetailView> findComApolicesAtivas();
    
    /**
     * Busca segurados por valor total de apólices.
     */
    @Query("SELECT s FROM SeguradoDetailView s WHERE s.valorTotalApolices >= :valorMinimo ORDER BY s.valorTotalApolices DESC")
    List<SeguradoDetailView> findByValorTotalApolicesGreaterThanEqual(@Param("valorMinimo") java.math.BigDecimal valorMinimo);
    
    /**
     * Busca segurados mais ativos (mais alterações).
     */
    @Query("SELECT s FROM SeguradoDetailView s WHERE s.totalAlteracoes > 0 ORDER BY s.totalAlteracoes DESC")
    List<SeguradoDetailView> findMaisAtivos();
    
    /**
     * Busca segurados por período de última alteração.
     */
    @Query("SELECT s FROM SeguradoDetailView s WHERE s.dataUltimaAlteracao BETWEEN :inicio AND :fim")
    List<SeguradoDetailView> findByUltimaAlteracaoEntre(@Param("inicio") Instant inicio, @Param("fim") Instant fim);
}