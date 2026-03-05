package com.seguradora.hibrida.snapshot.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.Instant;
import java.util.Map;

/**
 * Entidade JPA para persistência de snapshots no banco de dados.
 * 
 * <p>Representa a tabela `snapshots` com otimizações para:
 * <ul>
 *   <li>Consultas rápidas por aggregate ID</li>
 *   <li>Ordenação eficiente por versão</li>
 *   <li>Armazenamento eficiente de dados JSON</li>
 *   <li>Suporte a compressão de dados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Entity
@Table(name = "snapshots", indexes = {
    @Index(name = "idx_snapshots_aggregate_version", columnList = "aggregate_id, version", unique = true),
    @Index(name = "idx_snapshots_aggregate_timestamp", columnList = "aggregate_id, timestamp"),
    @Index(name = "idx_snapshots_type_timestamp", columnList = "aggregate_type, timestamp"),
    @Index(name = "idx_snapshots_timestamp", columnList = "timestamp")
})
@Getter
@Setter
@NoArgsConstructor
public class SnapshotEntry {
    
    /**
     * Identificador único do snapshot.
     */
    @Id
    @Column(name = "snapshot_id", length = 36, nullable = false)
    private String snapshotId;
    
    /**
     * ID do aggregate que este snapshot representa.
     */
    @Column(name = "aggregate_id", length = 255, nullable = false)
    private String aggregateId;
    
    /**
     * Tipo do aggregate (nome da classe).
     */
    @Column(name = "aggregate_type", length = 100, nullable = false)
    private String aggregateType;
    
    /**
     * Versão do aggregate no momento do snapshot.
     */
    @Column(name = "version", nullable = false)
    private Long version;
    
    /**
     * Dados serializados do aggregate em formato JSON.
     */
    @Column(name = "snapshot_data", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> snapshotData;
    
    /**
     * Timestamp de criação do snapshot.
     */
    @Column(name = "timestamp", nullable = false)
    private Instant timestamp;
    
    /**
     * Metadados adicionais do snapshot.
     */
    @Column(name = "metadata", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private Map<String, Object> metadata;
    
    /**
     * Versão do schema do snapshot para evolução.
     */
    @Column(name = "schema_version", nullable = false)
    private Integer schemaVersion;
    
    /**
     * Indica se os dados estão comprimidos.
     */
    @Column(name = "compressed", nullable = false)
    private Boolean compressed;
    
    /**
     * Tamanho original dos dados (antes da compressão).
     */
    @Column(name = "original_size")
    private Integer originalSize;
    
    /**
     * Tamanho comprimido dos dados.
     */
    @Column(name = "compressed_size")
    private Integer compressedSize;
    
    /**
     * Algoritmo de compressão utilizado.
     */
    @Column(name = "compression_algorithm", length = 20)
    private String compressionAlgorithm;
    
    /**
     * Hash dos dados para verificação de integridade.
     */
    @Column(name = "data_hash", length = 64)
    private String dataHash;
    
    /**
     * Usuário que criou o snapshot (para auditoria).
     */
    @Column(name = "created_by", length = 100)
    private String createdBy;
    
    /**
     * Construtor com parâmetros essenciais.
     * 
     * @param snapshotId ID único do snapshot
     * @param aggregateId ID do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     * @param snapshotData Dados do snapshot
     * @param timestamp Timestamp de criação
     */
    public SnapshotEntry(String snapshotId, String aggregateId, String aggregateType, 
                        Long version, Map<String, Object> snapshotData, Instant timestamp) {
        this.snapshotId = snapshotId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.snapshotData = snapshotData;
        this.timestamp = timestamp;
        this.schemaVersion = 1;
        this.compressed = false;
    }
    
    /**
     * Callback executado antes da persistência.
     */
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = Instant.now();
        }
        if (schemaVersion == null) {
            schemaVersion = 1;
        }
        if (compressed == null) {
            compressed = false;
        }
    }
    
    /**
     * Verifica se o snapshot está comprimido.
     * 
     * @return true se comprimido, false caso contrário
     */
    public boolean isCompressed() {
        return Boolean.TRUE.equals(compressed);
    }
    
    /**
     * Calcula a taxa de compressão.
     * 
     * @return Taxa de compressão (0.0 a 1.0) ou 0.0 se não comprimido
     */
    public double getCompressionRatio() {
        if (!isCompressed() || originalSize == null || originalSize == 0) {
            return 0.0;
        }
        return 1.0 - ((double) compressedSize / originalSize);
    }
    
    /**
     * Calcula o espaço economizado pela compressão.
     * 
     * @return Bytes economizados ou 0 se não comprimido
     */
    public int getSpaceSaved() {
        if (!isCompressed() || originalSize == null || compressedSize == null) {
            return 0;
        }
        return originalSize - compressedSize;
    }
    
    /**
     * Verifica se a compressão foi efetiva (economizou pelo menos 10%).
     * 
     * @return true se compressão foi efetiva, false caso contrário
     */
    public boolean isCompressionEffective() {
        return isCompressed() && getCompressionRatio() >= 0.1;
    }
    
    /**
     * Obtém o tamanho efetivo dos dados (comprimido se aplicável).
     * 
     * @return Tamanho em bytes
     */
    public int getEffectiveSize() {
        if (isCompressed() && compressedSize != null) {
            return compressedSize;
        }
        if (originalSize != null) {
            return originalSize;
        }
        return 0;
    }
}