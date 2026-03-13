package com.seguradora.hibrida.domain.veiculo.relationship.repository;

import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * Repository para gerenciar relacionamentos Veículo-Apólice.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface VeiculoApoliceRelacionamentoRepository extends JpaRepository<VeiculoApoliceRelacionamento, String> {

    /**
     * Busca relacionamentos ativos por veículo.
     */
    List<VeiculoApoliceRelacionamento> findByVeiculoIdAndStatus(String veiculoId, StatusRelacionamento status);

    /**
     * Busca relacionamentos ativos por apólice.
     */
    List<VeiculoApoliceRelacionamento> findByApoliceIdAndStatus(String apoliceId, StatusRelacionamento status);

    /**
     * Busca todos os relacionamentos de um veículo (histórico completo).
     */
    List<VeiculoApoliceRelacionamento> findByVeiculoIdOrderByDataInicioDesc(String veiculoId);

    /**
     * Busca todos os relacionamentos de uma apólice.
     */
    List<VeiculoApoliceRelacionamento> findByApoliceIdOrderByDataInicioDesc(String apoliceId);

    /**
     * Busca relacionamento ativo específico entre veículo e apólice.
     */
    @Query("SELECT r FROM VeiculoApoliceRelacionamento r WHERE r.veiculoId = :veiculoId " +
           "AND r.apoliceId = :apoliceId AND r.status = 'ATIVO'")
    Optional<VeiculoApoliceRelacionamento> findRelacionamentoAtivo(
        @Param("veiculoId") String veiculoId,
        @Param("apoliceId") String apoliceId
    );

    /**
     * Busca veículos sem cobertura ativa.
     */
    @Query("SELECT DISTINCT r.veiculoId FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.veiculoId NOT IN (" +
           "  SELECT r2.veiculoId FROM VeiculoApoliceRelacionamento r2 " +
           "  WHERE r2.status = 'ATIVO' AND (r2.dataFim IS NULL OR r2.dataFim >= CURRENT_DATE)" +
           ")")
    List<String> findVeiculosSemCobertura();

    /**
     * Busca relacionamentos vigentes em uma data específica.
     */
    @Query("SELECT r FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.veiculoId = :veiculoId " +
           "AND r.dataInicio <= :data " +
           "AND (r.dataFim IS NULL OR r.dataFim >= :data) " +
           "AND r.status = 'ATIVO'")
    List<VeiculoApoliceRelacionamento> findRelacionamentosVigentesEm(
        @Param("veiculoId") String veiculoId,
        @Param("data") LocalDate data
    );

    /**
     * Busca relacionamentos com gap de cobertura.
     */
    @Query("SELECT r FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.status = 'ATIVO' " +
           "AND r.dataFim IS NOT NULL " +
           "AND r.dataFim < CURRENT_DATE")
    List<VeiculoApoliceRelacionamento> findRelacionamentosComGap();

    /**
     * Busca relacionamentos que vencem nos próximos N dias.
     */
    @Query("SELECT r FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.status = 'ATIVO' " +
           "AND r.dataFim IS NOT NULL " +
           "AND r.dataFim BETWEEN CURRENT_DATE AND :dataLimite")
    List<VeiculoApoliceRelacionamento> findRelacionamentosVencendoAte(
        @Param("dataLimite") LocalDate dataLimite
    );

    /**
     * Conta relacionamentos ativos por veículo.
     */
    @Query("SELECT COUNT(r) FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.veiculoId = :veiculoId AND r.status = 'ATIVO'")
    long countRelacionamentosAtivos(@Param("veiculoId") String veiculoId);

    /**
     * Verifica se existe relacionamento ativo entre veículo e apólice.
     */
    @Query("SELECT CASE WHEN COUNT(r) > 0 THEN true ELSE false END " +
           "FROM VeiculoApoliceRelacionamento r " +
           "WHERE r.veiculoId = :veiculoId " +
           "AND r.apoliceId = :apoliceId " +
           "AND r.status = 'ATIVO'")
    boolean existsRelacionamentoAtivo(
        @Param("veiculoId") String veiculoId,
        @Param("apoliceId") String apoliceId
    );

    /**
     * Busca relacionamentos por CPF do segurado.
     */
    List<VeiculoApoliceRelacionamento> findBySeguradoCpfAndStatus(String cpf, StatusRelacionamento status);

    /**
     * Busca relacionamentos por placa do veículo.
     */
    List<VeiculoApoliceRelacionamento> findByVeiculoPlacaAndStatus(String placa, StatusRelacionamento status);
}
