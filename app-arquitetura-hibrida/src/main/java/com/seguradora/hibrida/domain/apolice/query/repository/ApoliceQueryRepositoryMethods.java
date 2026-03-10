package com.seguradora.hibrida.domain.apolice.query.repository;

import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

/**
 * Métodos adicionais para o repositório de apólices.
 * Estes métodos são usados pelo scheduler de notificações.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface ApoliceQueryRepositoryMethods {
    
    /**
     * Busca apólices que vencem em uma data específica com status específico.
     * Usado pelo scheduler para detectar vencimentos.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim = :data AND a.status = :status ORDER BY a.numeroApolice")
    List<ApoliceQueryModel> findByVigenciaFimAndStatusOrderByNumeroApolice(@Param("data") LocalDate data, @Param("status") String status);
    
    /**
     * Busca apólices que vencem antes de uma data com status específico.
     * Usado para detectar apólices já vencidas.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim < :data AND a.status = :status ORDER BY a.vigenciaFim")
    List<ApoliceQueryModel> findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(@Param("data") LocalDate data, @Param("status") String status);
    
    /**
     * Busca apólices que vencem até uma data com status específico.
     * Usado para detectar apólices elegíveis para renovação.
     */
    @Query("SELECT a FROM ApoliceQueryModel a WHERE a.vigenciaFim <= :data AND a.status = :status ORDER BY a.vigenciaFim")
    List<ApoliceQueryModel> findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(@Param("data") LocalDate data, @Param("status") String status);
}