package com.seguradora.hibrida.eventstore.partition;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Gerenciador de partições do Event Store.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Criar partições mensais automaticamente</li>
 *   <li>Manter partições futuras (próximos 3 meses)</li>
 *   <li>Monitorar estatísticas de partições</li>
 *   <li>Executar manutenção automática</li>
 * </ul>
 * 
 * <p>As partições são criadas mensalmente baseadas no timestamp dos eventos,
 * permitindo consultas eficientes e arquivamento de dados antigos.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PartitionManager {
    
    private final JdbcTemplate jdbcTemplate;
    private static final DateTimeFormatter PARTITION_FORMAT = DateTimeFormatter.ofPattern("yyyy_MM");
    
    /**
     * Cria partição mensal para a data especificada.
     * 
     * @param tableName Nome da tabela base
     * @param startDate Data de início da partição
     * @return true se a partição foi criada com sucesso
     */
    @Transactional
    public boolean createMonthlyPartition(String tableName, LocalDate startDate) {
        try {
            log.info("Criando partição mensal para tabela {} na data {}", tableName, startDate);
            
            jdbcTemplate.update("SELECT create_monthly_partition(?, ?)", 
                tableName, startDate);
            
            log.info("Partição criada com sucesso: {}_{}", tableName, startDate.format(PARTITION_FORMAT));
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao criar partição para {} na data {}: {}", 
                tableName, startDate, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Executa manutenção automática de partições.
     * 
     * <p>Cria partições para os próximos 3 meses se não existirem.
     * 
     * @return true se a manutenção foi executada com sucesso
     */
    @Transactional
    public boolean maintainPartitions() {
        try {
            log.info("Iniciando manutenção automática de partições");
            
            jdbcTemplate.update("SELECT maintain_event_partitions()");
            
            log.info("Manutenção de partições concluída com sucesso");
            return true;
            
        } catch (Exception e) {
            log.error("Erro na manutenção de partições: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Executa manutenção agendada com log.
     * 
     * <p>Método usado pelo scheduler para manutenção automática
     * com registro de execução.
     * 
     * @return true se executado com sucesso
     */
    @Transactional
    public boolean schedulePartitionMaintenance() {
        try {
            log.info("Executando manutenção agendada de partições");
            
            jdbcTemplate.update("SELECT schedule_partition_maintenance()");
            
            log.info("Manutenção agendada executada com sucesso");
            return true;
            
        } catch (Exception e) {
            log.error("Erro na manutenção agendada: {}", e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém estatísticas das partições existentes.
     * 
     * @return Lista de estatísticas por partição
     */
    public List<PartitionStatistics> getPartitionStatistics() {
        try {
            log.debug("Consultando estatísticas de partições");
            
            String sql = """
                SELECT 
                    partition_name,
                    row_count,
                    size_bytes,
                    size_pretty,
                    min_timestamp,
                    max_timestamp
                FROM get_partition_statistics()
                ORDER BY partition_name
                """;
            
            return jdbcTemplate.query(sql, (rs, rowNum) -> 
                PartitionStatistics.builder()
                    .partitionName(rs.getString("partition_name"))
                    .rowCount(rs.getLong("row_count"))
                    .sizeBytes(rs.getLong("size_bytes"))
                    .sizePretty(rs.getString("size_pretty"))
                    .minTimestamp(rs.getTimestamp("min_timestamp") != null ? 
                        rs.getTimestamp("min_timestamp").toInstant() : null)
                    .maxTimestamp(rs.getTimestamp("max_timestamp") != null ? 
                        rs.getTimestamp("max_timestamp").toInstant() : null)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Erro ao consultar estatísticas de partições: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Lista todas as partições existentes.
     * 
     * @return Lista de nomes das partições
     */
    public List<String> listPartitions() {
        try {
            String sql = """
                SELECT tablename 
                FROM pg_tables 
                WHERE schemaname = 'public' 
                AND tablename LIKE 'events_%'
                ORDER BY tablename
                """;
            
            return jdbcTemplate.queryForList(sql, String.class);
            
        } catch (Exception e) {
            log.error("Erro ao listar partições: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Verifica se uma partição existe.
     * 
     * @param partitionName Nome da partição
     * @return true se a partição existe
     */
    public boolean partitionExists(String partitionName) {
        try {
            String sql = """
                SELECT COUNT(*) 
                FROM pg_tables 
                WHERE schemaname = 'public' 
                AND tablename = ?
                """;
            
            Integer count = jdbcTemplate.queryForObject(sql, Integer.class, partitionName);
            return count != null && count > 0;
            
        } catch (Exception e) {
            log.error("Erro ao verificar existência da partição {}: {}", 
                partitionName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Obtém log de manutenção de partições.
     * 
     * @param limit Número máximo de registros
     * @return Lista de logs de manutenção
     */
    public List<Map<String, Object>> getMaintenanceLog(int limit) {
        try {
            String sql = """
                SELECT 
                    execution_time,
                    status,
                    message,
                    error_detail,
                    created_at
                FROM partition_maintenance_log 
                ORDER BY execution_time DESC 
                LIMIT ?
                """;
            
            return jdbcTemplate.queryForList(sql, limit);
            
        } catch (Exception e) {
            log.error("Erro ao consultar log de manutenção: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Calcula o nome da partição para uma data específica.
     * 
     * @param date Data de referência
     * @return Nome da partição
     */
    public String calculatePartitionName(LocalDate date) {
        return "events_" + date.format(PARTITION_FORMAT);
    }
    
    /**
     * Verifica se as partições estão saudáveis.
     * 
     * @return true se todas as partições necessárias existem
     */
    public boolean arePartitionsHealthy() {
        try {
            LocalDate currentMonth = LocalDate.now().withDayOfMonth(1);
            
            // Verificar se existem partições para os próximos 2 meses
            for (int i = 0; i < 2; i++) {
                LocalDate checkDate = currentMonth.plusMonths(i);
                String partitionName = calculatePartitionName(checkDate);
                
                if (!partitionExists(partitionName)) {
                    log.warn("Partição necessária não encontrada: {}", partitionName);
                    return false;
                }
            }
            
            return true;
            
        } catch (Exception e) {
            log.error("Erro ao verificar saúde das partições: {}", e.getMessage(), e);
            return false;
        }
    }
}