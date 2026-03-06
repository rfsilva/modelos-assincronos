package com.seguradora.hibrida.eventstore.archive;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPOutputStream;

/**
 * Serviço de arquivamento de eventos antigos.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Identificar eventos elegíveis para arquivamento (> 2 anos)</li>
 *   <li>Comprimir e mover eventos para storage frio</li>
 *   <li>Manter índice de eventos arquivados</li>
 *   <li>Permitir consulta transparente em arquivos</li>
 * </ul>
 * 
 * <p>O arquivamento é feito por partição completa para otimizar
 * performance e facilitar restore quando necessário.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EventArchiver {
    
    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ArchiveStorageService storageService;
    private final EventArchiveProperties properties;
    
    /**
     * Identifica partições elegíveis para arquivamento.
     * 
     * @return Lista de partições que podem ser arquivadas
     */
    public List<String> findPartitionsForArchiving() {
        try {
            LocalDate cutoffDate = LocalDate.now().minusYears(properties.getArchiveAfterYears());
            
            log.info("Buscando partições para arquivamento anteriores a {}", cutoffDate);
            
            String sql = """
                SELECT tablename 
                FROM pg_tables 
                WHERE schemaname = 'public' 
                AND tablename LIKE 'events_%'
                AND tablename < ?
                ORDER BY tablename
                """;
            
            String cutoffPartition = "events_" + cutoffDate.format(java.time.format.DateTimeFormatter.ofPattern("yyyy_MM"));
            
            List<String> partitions = jdbcTemplate.queryForList(sql, String.class, cutoffPartition);
            
            log.info("Encontradas {} partições elegíveis para arquivamento", partitions.size());
            return partitions;
            
        } catch (Exception e) {
            log.error("Erro ao buscar partições para arquivamento: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Arquiva uma partição específica.
     * 
     * @param partitionName Nome da partição a ser arquivada
     * @return Resultado do arquivamento
     */
    @Transactional
    public ArchiveResult archivePartition(String partitionName) {
        try {
            log.info("Iniciando arquivamento da partição: {}", partitionName);
            
            // 1. Verificar se partição existe e não está vazia
            if (!partitionExists(partitionName)) {
                return ArchiveResult.error(partitionName, "Partição não encontrada");
            }
            
            long eventCount = getPartitionEventCount(partitionName);
            if (eventCount == 0) {
                return ArchiveResult.error(partitionName, "Partição vazia");
            }
            
            // 2. Verificar se já foi arquivada
            if (isPartitionArchived(partitionName)) {
                return ArchiveResult.error(partitionName, "Partição já arquivada");
            }
            
            // 3. Exportar dados da partição
            byte[] compressedData = exportPartitionData(partitionName);
            
            // 4. Salvar no storage frio
            String archiveKey = generateArchiveKey(partitionName);
            boolean stored = storageService.store(archiveKey, compressedData);
            
            if (!stored) {
                return ArchiveResult.error(partitionName, "Falha ao salvar no storage");
            }
            
            // 5. Registrar arquivamento
            registerArchive(partitionName, archiveKey, eventCount, compressedData.length);
            
            // 6. Remover partição original (se configurado)
            if (properties.isDeleteAfterArchive()) {
                dropPartition(partitionName);
            }
            
            log.info("Partição {} arquivada com sucesso. {} eventos, {} bytes comprimidos", 
                partitionName, eventCount, compressedData.length);
            
            return ArchiveResult.success(partitionName, eventCount, compressedData.length);
            
        } catch (Exception e) {
            log.error("Erro ao arquivar partição {}: {}", partitionName, e.getMessage(), e);
            return ArchiveResult.error(partitionName, e.getMessage());
        }
    }
    
    /**
     * Executa arquivamento automático de todas as partições elegíveis.
     * 
     * @return Resumo do arquivamento
     */
    public ArchiveSummary executeAutoArchiving() {
        log.info("Iniciando arquivamento automático");
        
        List<String> partitions = findPartitionsForArchiving();
        ArchiveSummary summary = new ArchiveSummary();
        
        for (String partition : partitions) {
            ArchiveResult result = archivePartition(partition);
            summary.addResult(result);
            
            // Pausa entre arquivamentos para não sobrecarregar o sistema
            if (properties.getArchivePauseMs() > 0) {
                try {
                    Thread.sleep(properties.getArchivePauseMs());
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                    break;
                }
            }
        }
        
        log.info("Arquivamento automático concluído. Sucesso: {}, Erro: {}", 
            summary.getSuccessCount(), summary.getErrorCount());
        
        return summary;
    }
    
    /**
     * Restaura uma partição arquivada.
     * 
     * @param partitionName Nome da partição a ser restaurada
     * @return true se restaurada com sucesso
     */
    @Transactional
    public boolean restorePartition(String partitionName) {
        try {
            log.info("Iniciando restore da partição: {}", partitionName);
            
            // 1. Verificar se está arquivada
            ArchiveMetadata metadata = getArchiveMetadata(partitionName);
            if (metadata == null) {
                log.warn("Partição {} não encontrada nos arquivos", partitionName);
                return false;
            }
            
            // 2. Verificar se partição já existe
            if (partitionExists(partitionName)) {
                log.warn("Partição {} já existe, não é necessário restore", partitionName);
                return true;
            }
            
            // 3. Recuperar dados do storage
            byte[] compressedData = storageService.retrieve(metadata.getArchiveKey());
            if (compressedData == null) {
                log.error("Falha ao recuperar dados do storage para {}", partitionName);
                return false;
            }
            
            // 4. Descomprimir e restaurar dados
            boolean restored = restorePartitionData(partitionName, compressedData);
            
            if (restored) {
                log.info("Partição {} restaurada com sucesso", partitionName);
                updateArchiveStatus(partitionName, "RESTORED");
            }
            
            return restored;
            
        } catch (Exception e) {
            log.error("Erro ao restaurar partição {}: {}", partitionName, e.getMessage(), e);
            return false;
        }
    }
    
    /**
     * Lista eventos arquivados por critérios.
     * 
     * @param aggregateId ID do aggregate (opcional)
     * @param fromDate Data inicial
     * @param toDate Data final
     * @return Lista de eventos encontrados nos arquivos
     */
    public List<Map<String, Object>> queryArchivedEvents(String aggregateId, 
                                                         Instant fromDate, 
                                                         Instant toDate) {
        try {
            log.debug("Consultando eventos arquivados: aggregate={}, from={}, to={}", 
                aggregateId, fromDate, toDate);
            
            // Identificar arquivos que podem conter os dados
            List<String> relevantArchives = findRelevantArchives(fromDate, toDate);
            
            // Consultar cada arquivo relevante
            // Implementação simplificada - em produção seria otimizada
            return List.of(); // TODO: Implementar consulta em arquivos
            
        } catch (Exception e) {
            log.error("Erro ao consultar eventos arquivados: {}", e.getMessage(), e);
            return List.of();
        }
    }
    
    /**
     * Obtém estatísticas de arquivamento.
     * 
     * @return Estatísticas dos arquivos
     */
    public ArchiveStatistics getArchiveStatistics() {
        try {
            String sql = """
                SELECT 
                    COUNT(*) as total_archives,
                    SUM(event_count) as total_events,
                    SUM(compressed_size) as total_size,
                    MIN(archived_at) as oldest_archive,
                    MAX(archived_at) as newest_archive
                FROM event_archives 
                WHERE status = 'ARCHIVED'
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> 
                ArchiveStatistics.builder()
                    .totalArchives(rs.getLong("total_archives"))
                    .totalEvents(rs.getLong("total_events"))
                    .totalSize(rs.getLong("total_size"))
                    .oldestArchive(rs.getTimestamp("oldest_archive") != null ? 
                        rs.getTimestamp("oldest_archive").toInstant() : null)
                    .newestArchive(rs.getTimestamp("newest_archive") != null ? 
                        rs.getTimestamp("newest_archive").toInstant() : null)
                    .build()
            );
            
        } catch (Exception e) {
            log.error("Erro ao obter estatísticas de arquivo: {}", e.getMessage(), e);
            return ArchiveStatistics.builder().build();
        }
    }
    
    // Métodos auxiliares privados
    
    private boolean partitionExists(String partitionName) {
        String sql = "SELECT COUNT(*) FROM pg_tables WHERE tablename = ?";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, partitionName);
        return count != null && count > 0;
    }
    
    private long getPartitionEventCount(String partitionName) {
        String sql = "SELECT COUNT(*) FROM " + partitionName;
        Long count = jdbcTemplate.queryForObject(sql, Long.class);
        return count != null ? count : 0;
    }
    
    private boolean isPartitionArchived(String partitionName) {
        String sql = "SELECT COUNT(*) FROM event_archives WHERE partition_name = ? AND status = 'ARCHIVED'";
        Integer count = jdbcTemplate.queryForObject(sql, Integer.class, partitionName);
        return count != null && count > 0;
    }
    
    private byte[] exportPartitionData(String partitionName) throws IOException {
        String sql = "SELECT * FROM " + partitionName + " ORDER BY timestamp";
        
        List<Map<String, Object>> events = jdbcTemplate.queryForList(sql);
        
        // Serializar para JSON
        String jsonData = objectMapper.writeValueAsString(events);
        
        // Comprimir com GZIP
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try (GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            gzipOut.write(jsonData.getBytes());
        }
        
        return baos.toByteArray();
    }
    
    private String generateArchiveKey(String partitionName) {
        return String.format("eventstore/archives/%s/%s.json.gz", 
            LocalDate.now().getYear(), partitionName);
    }
    
    private void registerArchive(String partitionName, String archiveKey, 
                               long eventCount, long compressedSize) {
        String sql = """
            INSERT INTO event_archives 
            (partition_name, archive_key, event_count, compressed_size, archived_at, status)
            VALUES (?, ?, ?, ?, ?, 'ARCHIVED')
            """;
        
        jdbcTemplate.update(sql, partitionName, archiveKey, eventCount, 
            compressedSize, Instant.now());
    }
    
    private void dropPartition(String partitionName) {
        String sql = "DROP TABLE IF EXISTS " + partitionName;
        jdbcTemplate.execute(sql);
        log.info("Partição {} removida após arquivamento", partitionName);
    }
    
    private ArchiveMetadata getArchiveMetadata(String partitionName) {
        try {
            String sql = """
                SELECT archive_key, event_count, compressed_size, archived_at, status
                FROM event_archives 
                WHERE partition_name = ? AND status = 'ARCHIVED'
                """;
            
            return jdbcTemplate.queryForObject(sql, (rs, rowNum) -> 
                ArchiveMetadata.builder()
                    .partitionName(partitionName)
                    .archiveKey(rs.getString("archive_key"))
                    .eventCount(rs.getLong("event_count"))
                    .compressedSize(rs.getLong("compressed_size"))
                    .archivedAt(rs.getTimestamp("archived_at").toInstant())
                    .status(rs.getString("status"))
                    .build(),
                partitionName
            );
            
        } catch (Exception e) {
            return null;
        }
    }
    
    private boolean restorePartitionData(String partitionName, byte[] compressedData) {
        // TODO: Implementar restore de dados
        // 1. Descomprimir dados
        // 2. Recriar partição
        // 3. Inserir dados
        return false;
    }
    
    private void updateArchiveStatus(String partitionName, String status) {
        String sql = "UPDATE event_archives SET status = ?, updated_at = ? WHERE partition_name = ?";
        jdbcTemplate.update(sql, status, Instant.now(), partitionName);
    }
    
    private List<String> findRelevantArchives(Instant fromDate, Instant toDate) {
        // TODO: Implementar busca de arquivos relevantes por período
        return List.of();
    }
}