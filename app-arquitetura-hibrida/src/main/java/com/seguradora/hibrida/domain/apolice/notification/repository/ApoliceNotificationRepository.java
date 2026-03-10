package com.seguradora.hibrida.domain.apolice.notification.repository;

import com.seguradora.hibrida.domain.apolice.notification.model.ApoliceNotification;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationChannel;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationStatus;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Repositório para notificações de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Repository
public interface ApoliceNotificationRepository extends JpaRepository<ApoliceNotification, String> {
    
    // === CONSULTAS POR APÓLICE ===
    
    /**
     * Busca notificações por apólice.
     */
    List<ApoliceNotification> findByApoliceIdOrderByCreatedAtDesc(String apoliceId);
    
    /**
     * Busca notificações por número da apólice.
     */
    List<ApoliceNotification> findByApoliceNumeroOrderByCreatedAtDesc(String apoliceNumero);
    
    /**
     * Conta notificações por apólice e status.
     */
    long countByApoliceIdAndStatus(String apoliceId, NotificationStatus status);
    
    // === CONSULTAS POR SEGURADO ===
    
    /**
     * Busca notificações por CPF do segurado.
     */
    Page<ApoliceNotification> findBySeguradoCpfOrderByCreatedAtDesc(String cpf, Pageable pageable);
    
    /**
     * Busca notificações não lidas por segurado.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.seguradoCpf = :cpf AND n.status NOT IN ('SENT', 'CANCELLED') ORDER BY n.priority ASC, n.criadaEm DESC")
    List<ApoliceNotification> findUnreadBySeguradoCpf(@Param("cpf") String cpf);
    
    // === CONSULTAS POR STATUS ===
    
    /**
     * Busca notificações por status.
     */
    Page<ApoliceNotification> findByStatusOrderByPriorityAscCriadaEmAsc(NotificationStatus status, Pageable pageable);
    
    /**
     * Busca notificações pendentes para envio.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.status = 'PENDING' AND (n.agendadaPara IS NULL OR n.agendadaPara <= :now) AND (n.expiraEm IS NULL OR n.expiraEm > :now) ORDER BY n.priority ASC, n.criadaEm ASC")
    List<ApoliceNotification> findPendingForSending(@Param("now") Instant now);
    
    /**
     * Busca notificações que falharam e podem ser reprocessadas.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.status = 'FAILED' AND n.tentativas < n.maxTentativas AND (n.expiraEm IS NULL OR n.expiraEm > :now) ORDER BY n.priority ASC, n.atualizadaEm ASC")
    List<ApoliceNotification> findFailedForRetry(@Param("now") Instant now);
    
    /**
     * Busca notificações expiradas.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.expiraEm IS NOT NULL AND n.expiraEm <= :now AND n.status NOT IN ('SENT', 'CANCELLED', 'EXPIRED')")
    List<ApoliceNotification> findExpiredNotifications(@Param("now") Instant now);
    
    // === MÉTODOS PARA SCHEDULER ===
    
    /**
     * Busca notificações por status e agendamento.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.status = :status AND (n.agendadaPara IS NULL OR n.agendadaPara <= :scheduledAt) ORDER BY n.priority ASC, n.criadaEm ASC")
    List<ApoliceNotification> findByStatusAndScheduledAtLessThanEqual(@Param("status") NotificationStatus status, 
                                                                      @Param("scheduledAt") Instant scheduledAt);
    
    /**
     * Verifica se já existe notificação do tipo para a apólice em período recente.
     */
    @Query("SELECT COUNT(n) > 0 FROM ApoliceNotification n WHERE n.apoliceId = :apoliceId AND n.type = :type AND n.criadaEm > :criadaEm")
    boolean existsByApoliceIdAndTypeAndCriadaEmGreaterThan(@Param("apoliceId") String apoliceId, 
                                                           @Param("type") NotificationType type, 
                                                           @Param("criadaEm") Instant criadaEm);
    
    /**
     * Remove notificações antigas.
     */
    @Modifying
    @Query("DELETE FROM ApoliceNotification n WHERE n.atualizadaEm < :cutoff")
    int deleteByUpdatedAtLessThan(@Param("cutoff") Instant cutoff);
    
    // === CONSULTAS POR TIPO E CANAL ===
    
    /**
     * Busca notificações por tipo.
     */
    List<ApoliceNotification> findByTypeOrderByCriadaEmDesc(NotificationType type);
    
    /**
     * Busca notificações por canal.
     */
    Page<ApoliceNotification> findByChannelOrderByCriadaEmDesc(NotificationChannel channel, Pageable pageable);
    
    /**
     * Busca notificações críticas pendentes.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.priority <= 2 AND n.status IN ('PENDING', 'PROCESSING') ORDER BY n.priority ASC, n.criadaEm ASC")
    List<ApoliceNotification> findCriticalPending();
    
    // === CONSULTAS POR PERÍODO ===
    
    /**
     * Busca notificações criadas em um período.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.criadaEm BETWEEN :inicio AND :fim ORDER BY n.criadaEm DESC")
    Page<ApoliceNotification> findByPeriod(@Param("inicio") Instant inicio, @Param("fim") Instant fim, Pageable pageable);
    
    /**
     * Busca notificações agendadas para um período.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.agendadaPara BETWEEN :inicio AND :fim AND n.status = 'PENDING' ORDER BY n.agendadaPara ASC")
    List<ApoliceNotification> findScheduledForPeriod(@Param("inicio") Instant inicio, @Param("fim") Instant fim);
    
    // === CONSULTAS DE CONTROLE ===
    
    /**
     * Conta notificações enviadas por canal hoje.
     */
    @Query("SELECT COUNT(n) FROM ApoliceNotification n WHERE n.channel = :channel AND n.enviadaEm >= :startOfDay")
    long countSentTodayByChannel(@Param("channel") NotificationChannel channel, @Param("startOfDay") Instant startOfDay);
    
    /**
     * Verifica se já existe notificação do tipo para a apólice.
     */
    boolean existsByApoliceIdAndTypeAndStatusNot(String apoliceId, NotificationType type, NotificationStatus status);
    
    /**
     * Busca última notificação de um tipo para uma apólice.
     */
    @Query("SELECT n FROM ApoliceNotification n WHERE n.apoliceId = :apoliceId AND n.type = :type ORDER BY n.criadaEm DESC LIMIT 1")
    ApoliceNotification findLastByApoliceAndType(@Param("apoliceId") String apoliceId, @Param("type") NotificationType type);
    
    // === CONSULTAS ANALÍTICAS ===
    
    /**
     * Estatísticas por status.
     */
    @Query("SELECT n.status, COUNT(n) FROM ApoliceNotification n GROUP BY n.status")
    List<Object[]> getStatisticsByStatus();
    
    /**
     * Estatísticas por canal.
     */
    @Query("SELECT n.channel, COUNT(n), AVG(CASE WHEN n.enviadaEm IS NOT NULL AND n.criadaEm IS NOT NULL THEN EXTRACT(EPOCH FROM (n.enviadaEm - n.criadaEm)) ELSE NULL END) FROM ApoliceNotification n GROUP BY n.channel")
    List<Object[]> getStatisticsByChannel();
    
    /**
     * Estatísticas por tipo de notificação.
     */
    @Query("SELECT n.type, COUNT(n), SUM(CASE WHEN n.status IN ('SENT', 'DELIVERED', 'READ') THEN 1 ELSE 0 END) FROM ApoliceNotification n GROUP BY n.type")
    List<Object[]> getStatisticsByType();
    
    /**
     * Taxa de sucesso por canal nas últimas 24h.
     */
    @Query(value = """
        SELECT 
            channel,
            COUNT(*) as total,
            SUM(CASE WHEN status IN ('SENT', 'DELIVERED', 'READ') THEN 1 ELSE 0 END) as success,
            ROUND(
                SUM(CASE WHEN status IN ('SENT', 'DELIVERED', 'READ') THEN 1 ELSE 0 END) * 100.0 / COUNT(*), 2
            ) as success_rate
        FROM projections.apolice_notifications 
        WHERE criada_em >= :since
        GROUP BY channel
        ORDER BY success_rate DESC
        """, nativeQuery = true)
    List<Object[]> getSuccessRateByChannel(@Param("since") Instant since);
    
    /**
     * Relatório de performance por hora.
     */
    @Query(value = """
        SELECT 
            DATE_TRUNC('hour', criada_em) as hour,
            COUNT(*) as total,
            SUM(CASE WHEN status IN ('SENT', 'DELIVERED', 'READ') THEN 1 ELSE 0 END) as sent,
            SUM(CASE WHEN status = 'FAILED' THEN 1 ELSE 0 END) as failed,
            AVG(CASE WHEN enviada_em IS NOT NULL THEN EXTRACT(EPOCH FROM (enviada_em - criada_em)) ELSE NULL END) as avg_processing_time
        FROM projections.apolice_notifications 
        WHERE criada_em >= :since
        GROUP BY DATE_TRUNC('hour', criada_em)
        ORDER BY hour DESC
        """, nativeQuery = true)
    List<Object[]> getHourlyPerformanceReport(@Param("since") Instant since);
    
    // === OPERAÇÕES DE LIMPEZA ===
    
    /**
     * Marca notificações expiradas.
     */
    @Modifying
    @Query("UPDATE ApoliceNotification n SET n.status = 'EXPIRED', n.atualizadaEm = CURRENT_TIMESTAMP WHERE n.expiraEm <= :now AND n.status NOT IN ('SENT', 'CANCELLED', 'EXPIRED')")
    int markExpiredNotifications(@Param("now") Instant now);
    
    /**
     * Remove notificações antigas (mais de 90 dias).
     */
    @Modifying
    @Query("DELETE FROM ApoliceNotification n WHERE n.criadaEm < :cutoff AND n.status IN ('SENT', 'CANCELLED', 'EXPIRED')")
    int deleteOldNotifications(@Param("cutoff") Instant cutoff);
    
    /**
     * Atualiza status de notificações em lote.
     */
    @Modifying
    @Query("UPDATE ApoliceNotification n SET n.status = :newStatus, n.atualizadaEm = CURRENT_TIMESTAMP WHERE n.id IN :ids")
    int updateStatusBatch(@Param("ids") List<String> ids, @Param("newStatus") NotificationStatus newStatus);
}