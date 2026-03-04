package com.seguradora.detran.repository;

import com.seguradora.detran.model.ConsultaLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ConsultaLogRepository extends JpaRepository<ConsultaLog, Long> {
    
    List<ConsultaLog> findByPlacaAndRenavamOrderByConsultaTimestampDesc(String placa, String renavam);
    
    @Query("SELECT c FROM ConsultaLog c WHERE c.consultaTimestamp >= :inicio AND c.consultaTimestamp <= :fim")
    List<ConsultaLog> findByPeriodo(@Param("inicio") LocalDateTime inicio, @Param("fim") LocalDateTime fim);
    
    @Query("SELECT COUNT(c) FROM ConsultaLog c WHERE c.status = :status AND c.consultaTimestamp >= :inicio")
    long countByStatusAndTimestampAfter(@Param("status") ConsultaLog.StatusConsulta status, 
                                       @Param("inicio") LocalDateTime inicio);
    
    @Query("SELECT AVG(c.responseTimeMs) FROM ConsultaLog c WHERE c.consultaTimestamp >= :inicio")
    Double averageResponseTimeAfter(@Param("inicio") LocalDateTime inicio);
}