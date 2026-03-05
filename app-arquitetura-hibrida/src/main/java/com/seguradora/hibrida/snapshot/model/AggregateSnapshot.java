package com.seguradora.hibrida.snapshot.model;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Representa um snapshot de um aggregate em um ponto específico no tempo.
 * 
 * <p>Um snapshot contém o estado completo de um aggregate em uma versão específica,
 * permitindo reconstrução rápida sem necessidade de replay de todos os eventos.
 * 
 * <p>Características principais:
 * <ul>
 *   <li>Imutável após criação</li>
 *   <li>Contém metadados para rastreamento</li>
 *   <li>Suporte a compressão automática</li>
 *   <li>Versionamento para evolução de schema</li>
 * </ul>
 * 
 * <p>Exemplo de uso:
 * <pre>{@code
 * // Criar snapshot
 * Map<String, Object> data = Map.of(
 *     "id", "segurado-123",
 *     "nome", "João Silva",
 *     "cpf", "12345678901"
 * );
 * 
 * AggregateSnapshot snapshot = new AggregateSnapshot(
 *     "segurado-123",
 *     "SeguradoAggregate", 
 *     50,
 *     data
 * );
 * }</pre>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@ToString
@EqualsAndHashCode
public class AggregateSnapshot {
    
    /**
     * Identificador único do snapshot.
     */
    private final String snapshotId;
    
    /**
     * ID do aggregate que este snapshot representa.
     */
    private final String aggregateId;
    
    /**
     * Tipo do aggregate (nome da classe).
     */
    private final String aggregateType;
    
    /**
     * Versão do aggregate no momento do snapshot.
     */
    private final long version;
    
    /**
     * Dados serializados do aggregate.
     */
    private final Map<String, Object> data;
    
    /**
     * Timestamp de criação do snapshot.
     */
    private final Instant timestamp;
    
    /**
     * Metadados adicionais do snapshot.
     */
    private final Map<String, Object> metadata;
    
    /**
     * Versão do schema do snapshot para evolução.
     */
    private final int schemaVersion;
    
    /**
     * Indica se os dados estão comprimidos.
     */
    private final boolean compressed;
    
    /**
     * Tamanho original dos dados (antes da compressão).
     */
    private final int originalSize;
    
    /**
     * Tamanho comprimido dos dados.
     */
    private final int compressedSize;
    
    /**
     * Construtor principal para criação de snapshot.
     * 
     * @param aggregateId ID único do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     * @param data Dados do aggregate
     */
    public AggregateSnapshot(String aggregateId, String aggregateType, long version, Map<String, Object> data) {
        this(aggregateId, aggregateType, version, data, new HashMap<>());
    }
    
    /**
     * Construtor com metadados customizados.
     * 
     * @param aggregateId ID único do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     * @param data Dados do aggregate
     * @param metadata Metadados adicionais
     */
    public AggregateSnapshot(String aggregateId, String aggregateType, long version, 
                           Map<String, Object> data, Map<String, Object> metadata) {
        this.snapshotId = UUID.randomUUID().toString();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.data = new HashMap<>(data);
        this.timestamp = Instant.now();
        this.metadata = new HashMap<>(metadata);
        this.schemaVersion = 1; // Versão inicial
        this.compressed = false;
        this.originalSize = 0;
        this.compressedSize = 0;
    }
    
    /**
     * Construtor completo para deserialização (usado pelo Jackson).
     * 
     * @param snapshotId ID único do snapshot
     * @param aggregateId ID único do aggregate
     * @param aggregateType Tipo do aggregate
     * @param version Versão do aggregate
     * @param data Dados do aggregate
     * @param timestamp Timestamp de criação
     * @param metadata Metadados adicionais
     * @param schemaVersion Versão do schema
     * @param compressed Indica se está comprimido
     * @param originalSize Tamanho original
     * @param compressedSize Tamanho comprimido
     */
    @JsonCreator
    public AggregateSnapshot(
            @JsonProperty("snapshotId") String snapshotId,
            @JsonProperty("aggregateId") String aggregateId,
            @JsonProperty("aggregateType") String aggregateType,
            @JsonProperty("version") long version,
            @JsonProperty("data") Map<String, Object> data,
            @JsonProperty("timestamp") Instant timestamp,
            @JsonProperty("metadata") Map<String, Object> metadata,
            @JsonProperty("schemaVersion") int schemaVersion,
            @JsonProperty("compressed") boolean compressed,
            @JsonProperty("originalSize") int originalSize,
            @JsonProperty("compressedSize") int compressedSize) {
        this.snapshotId = snapshotId;
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.version = version;
        this.data = data != null ? new HashMap<>(data) : new HashMap<>();
        this.timestamp = timestamp;
        this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
        this.schemaVersion = schemaVersion;
        this.compressed = compressed;
        this.originalSize = originalSize;
        this.compressedSize = compressedSize;
    }
    
    /**
     * Cria uma cópia do snapshot com dados comprimidos.
     * 
     * @param compressedData Dados comprimidos
     * @param originalSize Tamanho original dos dados
     * @param compressedSize Tamanho após compressão
     * @return Nova instância com dados comprimidos
     */
    public AggregateSnapshot withCompression(Map<String, Object> compressedData, 
                                           int originalSize, int compressedSize) {
        return new AggregateSnapshot(
            this.snapshotId,
            this.aggregateId,
            this.aggregateType,
            this.version,
            compressedData,
            this.timestamp,
            this.metadata,
            this.schemaVersion,
            true,
            originalSize,
            compressedSize
        );
    }
    
    /**
     * Adiciona metadado ao snapshot.
     * 
     * @param key Chave do metadado
     * @param value Valor do metadado
     * @return Nova instância com metadado adicionado
     */
    public AggregateSnapshot withMetadata(String key, Object value) {
        Map<String, Object> newMetadata = new HashMap<>(this.metadata);
        newMetadata.put(key, value);
        
        return new AggregateSnapshot(
            this.snapshotId,
            this.aggregateId,
            this.aggregateType,
            this.version,
            this.data,
            this.timestamp,
            newMetadata,
            this.schemaVersion,
            this.compressed,
            this.originalSize,
            this.compressedSize
        );
    }
    
    /**
     * Obtém valor de um metadado.
     * 
     * @param key Chave do metadado
     * @return Valor do metadado ou null se não existir
     */
    public Object getMetadataValue(String key) {
        return metadata.get(key);
    }
    
    /**
     * Obtém valor de um metadado com tipo específico.
     * 
     * @param key Chave do metadado
     * @param type Tipo esperado
     * @param <T> Tipo do valor
     * @return Valor tipado ou null se não existir ou tipo incorreto
     */
    @SuppressWarnings("unchecked")
    public <T> T getMetadataValue(String key, Class<T> type) {
        Object value = metadata.get(key);
        if (value != null && type.isAssignableFrom(value.getClass())) {
            return (T) value;
        }
        return null;
    }
    
    /**
     * Verifica se o snapshot tem um metadado específico.
     * 
     * @param key Chave do metadado
     * @return true se existe, false caso contrário
     */
    public boolean hasMetadata(String key) {
        return metadata.containsKey(key);
    }
    
    /**
     * Calcula a taxa de compressão.
     * 
     * @return Taxa de compressão (0.0 a 1.0) ou 0.0 se não comprimido
     */
    public double getCompressionRatio() {
        if (!compressed || originalSize == 0) {
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
        if (!compressed) {
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
        return compressed && getCompressionRatio() >= 0.1;
    }
    
    /**
     * Obtém uma cópia imutável dos dados.
     * 
     * @return Mapa imutável com os dados
     */
    public Map<String, Object> getDataCopy() {
        return new HashMap<>(data);
    }
    
    /**
     * Obtém uma cópia imutável dos metadados.
     * 
     * @return Mapa imutável com os metadados
     */
    public Map<String, Object> getMetadataCopy() {
        return new HashMap<>(metadata);
    }
}