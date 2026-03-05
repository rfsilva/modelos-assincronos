package com.seguradora.detran.repository;

import com.seguradora.detran.model.VeiculoEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface VeiculoRepository extends JpaRepository<VeiculoEntity, Long> {
    
    Optional<VeiculoEntity> findByPlacaAndRenavam(String placa, String renavam);
    
    Optional<VeiculoEntity> findByPlaca(String placa);
    
    Optional<VeiculoEntity> findByRenavam(String renavam);
    
    @Query("SELECT COUNT(v) FROM VeiculoEntity v WHERE v.placa = :placa AND v.renavam = :renavam")
    long countByPlacaAndRenavam(@Param("placa") String placa, @Param("renavam") String renavam);
    
    boolean existsByPlacaAndRenavam(String placa, String renavam);
}