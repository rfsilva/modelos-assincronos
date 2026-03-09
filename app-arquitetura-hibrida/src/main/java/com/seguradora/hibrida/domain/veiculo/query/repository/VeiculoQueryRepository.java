package com.seguradora.hibrida.domain.veiculo.query.repository;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import com.seguradora.hibrida.domain.veiculo.query.model.VeiculoQueryModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository para consultas de veículo.
 * Implementa consultas otimizadas com índices específicos.
 * 
 * @author Principal Java Architect
 * @since 3.0.0
 */
@Repository
public interface VeiculoQueryRepository extends JpaRepository<VeiculoQueryModel, String> {
    
    /**
     * Busca veículo por placa.
     */
    Optional<VeiculoQueryModel> findByPlaca(String placa);
    
    /**
     * Busca veículo por RENAVAM.
     */
    Optional<VeiculoQueryModel> findByRenavam(String renavam);
    
    /**
     * Busca veículo por chassi.
     */
    Optional<VeiculoQueryModel> findByChassi(String chassi);
    
    /**
     * Busca veículos por CPF do proprietário.
     */
    List<VeiculoQueryModel> findByProprietarioCpf(String cpf);
    
    /**
     * Busca veículos por marca e modelo.
     */
    Page<VeiculoQueryModel> findByMarcaAndModelo(String marca, String modelo, Pageable pageable);
    
    /**
     * Busca veículos por marca (busca parcial).
     */
    Page<VeiculoQueryModel> findByMarcaContainingIgnoreCase(String marca, Pageable pageable);
    
    /**
     * Busca veículos por modelo (busca parcial).
     */
    Page<VeiculoQueryModel> findByModeloContainingIgnoreCase(String modelo, Pageable pageable);
    
    /**
     * Busca veículos por cidade e estado.
     */
    Page<VeiculoQueryModel> findByCidadeAndEstado(String cidade, String estado, Pageable pageable);
    
    /**
     * Busca veículos por estado.
     */
    Page<VeiculoQueryModel> findByEstado(String estado, Pageable pageable);
    
    /**
     * Busca veículos por região.
     */
    Page<VeiculoQueryModel> findByRegiao(String regiao, Pageable pageable);
    
    /**
     * Busca veículos por status.
     */
    Page<VeiculoQueryModel> findByStatus(StatusVeiculo status, Pageable pageable);
    
    /**
     * Busca veículos por status e categoria.
     */
    Page<VeiculoQueryModel> findByStatusAndCategoria(StatusVeiculo status, String categoria, Pageable pageable);
    
    /**
     * Busca veículos por ano de fabricação entre valores.
     */
    Page<VeiculoQueryModel> findByAnoFabricacaoBetween(Integer anoInicio, Integer anoFim, Pageable pageable);
    
    /**
     * Busca veículos com apólice ativa.
     */
    Page<VeiculoQueryModel> findByApoliceAtivaTrue(Pageable pageable);
    
    /**
     * Busca veículos sem apólice ativa.
     */
    Page<VeiculoQueryModel> findByApoliceAtivaFalse(Pageable pageable);
    
    /**
     * Verifica se existe veículo com a placa.
     */
    boolean existsByPlaca(String placa);
    
    /**
     * Verifica se existe veículo com o RENAVAM.
     */
    boolean existsByRenavam(String renavam);
    
    /**
     * Verifica se existe veículo com o chassi.
     */
    boolean existsByChassi(String chassi);
    
    /**
     * Conta veículos por proprietário.
     */
    @Query("SELECT COUNT(v) FROM VeiculoQueryModel v WHERE v.proprietarioCpf = :cpf AND v.status = :status")
    long countByProprietarioCpfAndStatus(@Param("cpf") String cpf, @Param("status") StatusVeiculo status);
    
    /**
     * Busca fuzzy por marca/modelo (Levenshtein-like usando LIKE).
     */
    @Query("SELECT v FROM VeiculoQueryModel v WHERE " +
           "LOWER(v.marca) LIKE LOWER(CONCAT('%', :termo, '%')) OR " +
           "LOWER(v.modelo) LIKE LOWER(CONCAT('%', :termo, '%'))")
    Page<VeiculoQueryModel> findByMarcaOrModeloFuzzy(@Param("termo") String termo, Pageable pageable);
    
    /**
     * Busca veículos por múltiplos critérios.
     */
    @Query("SELECT v FROM VeiculoQueryModel v WHERE " +
           "(:marca IS NULL OR LOWER(v.marca) LIKE LOWER(CONCAT('%', :marca, '%'))) AND " +
           "(:modelo IS NULL OR LOWER(v.modelo) LIKE LOWER(CONCAT('%', :modelo, '%'))) AND " +
           "(:status IS NULL OR v.status = :status) AND " +
           "(:anoInicio IS NULL OR v.anoFabricacao >= :anoInicio) AND " +
           "(:anoFim IS NULL OR v.anoFabricacao <= :anoFim) AND " +
           "(:estado IS NULL OR v.estado = :estado)")
    Page<VeiculoQueryModel> findByMultiplosCriterios(
        @Param("marca") String marca,
        @Param("modelo") String modelo,
        @Param("status") StatusVeiculo status,
        @Param("anoInicio") Integer anoInicio,
        @Param("anoFim") Integer anoFim,
        @Param("estado") String estado,
        Pageable pageable
    );
}
