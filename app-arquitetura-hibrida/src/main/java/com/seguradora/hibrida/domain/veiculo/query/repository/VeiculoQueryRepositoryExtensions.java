package com.seguradora.hibrida.domain.veiculo.query.repository;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * Extensões do repositório de veículos com consultas customizadas.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface VeiculoQueryRepositoryExtensions {
    
    /**
     * Busca veículos por nome do proprietário (busca parcial).
     */
    @Query("SELECT v FROM VeiculoQueryModel v WHERE LOWER(v.proprietarioNome) LIKE LOWER(CONCAT('%', :nome, '%'))")
    Page<VeiculoQueryModel> findByProprietarioNomeContainingIgnoreCase(@Param("nome") String nome, Pageable pageable);
    
    /**
     * Busca veículos por cidade (busca parcial).
     */
    @Query("SELECT v FROM VeiculoQueryModel v WHERE LOWER(v.cidade) LIKE LOWER(CONCAT('%', :cidade, '%'))")
    Page<VeiculoQueryModel> findByCidadeContainingIgnoreCase(@Param("cidade") String cidade, Pageable pageable);
    
    /**
     * Conta veículos por status.
     */
    long countByStatus(StatusVeiculo status);
    
    /**
     * Conta veículos com apólice ativa.
     */
    long countByApoliceAtivaTrue();
    
    /**
     * Estatísticas por estado.
     */
    @Query("SELECT v.estado, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.estado ORDER BY COUNT(v) DESC")
    List<Object[]> countByEstado();
    
    /**
     * Estatísticas por marca.
     */
    @Query("SELECT v.marca, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.marca ORDER BY COUNT(v) DESC")
    List<Object[]> countByMarca();
    
    /**
     * Estatísticas por categoria.
     */
    @Query("SELECT v.categoria, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.categoria ORDER BY COUNT(v) DESC")
    List<Object[]> countByCategoria();
    
    /**
     * Estatísticas por combustível.
     */
    @Query("SELECT v.tipoCombustivel, COUNT(v) FROM VeiculoQueryModel v GROUP BY v.tipoCombustivel ORDER BY COUNT(v) DESC")
    List<Object[]> countByTipoCombustivel();
    
    /**
     * Veículos por faixa etária.
     */
    @Query(value = """
        SELECT 
            CASE 
                WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - v.ano_fabricacao) <= 3 THEN 'NOVO'
                WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - v.ano_fabricacao) <= 10 THEN 'SEMINOVO'
                ELSE 'USADO'
            END as faixa_etaria,
            COUNT(*) as quantidade
        FROM projections.veiculo_query_view v 
        GROUP BY 
            CASE 
                WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - v.ano_fabricacao) <= 3 THEN 'NOVO'
                WHEN (EXTRACT(YEAR FROM CURRENT_DATE) - v.ano_fabricacao) <= 10 THEN 'SEMINOVO'
                ELSE 'USADO'
            END
        ORDER BY quantidade DESC
        """, nativeQuery = true)
    List<Object[]> countByFaixaEtaria();
    
    /**
     * Top marcas por região.
     */
    @Query("""
        SELECT v.regiao, v.marca, COUNT(v) as quantidade
        FROM VeiculoQueryModel v 
        WHERE v.regiao IS NOT NULL 
        GROUP BY v.regiao, v.marca 
        ORDER BY v.regiao, COUNT(v) DESC
        """)
    List<Object[]> getTopMarcasPorRegiao();
    
    /**
     * Veículos sem apólice por estado.
     */
    @Query("""
        SELECT v.estado, COUNT(v) as sem_apolice
        FROM VeiculoQueryModel v 
        WHERE v.apoliceAtiva = false 
        GROUP BY v.estado 
        ORDER BY COUNT(v) DESC
        """)
    List<Object[]> countVeiculosSemApolicePorEstado();
}