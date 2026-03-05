package com.seguradora.hibrida.snapshot.serialization;

import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;

/**
 * Interface para serialização e deserialização de snapshots.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Serializar snapshots para persistência</li>
 *   <li>Deserializar snapshots do armazenamento</li>
 *   <li>Aplicar compressão quando necessário</li>
 *   <li>Validar integridade dos dados</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface SnapshotSerializer {
    
    /**
     * Serializa um snapshot para string JSON.
     * 
     * @param snapshot Snapshot a ser serializado
     * @return String JSON representando o snapshot
     * @throws SnapshotSerializationException em caso de erro na serialização
     */
    String serialize(AggregateSnapshot snapshot);
    
    /**
     * Deserializa uma string JSON para snapshot.
     * 
     * @param snapshotData String JSON do snapshot
     * @param aggregateType Tipo do aggregate para deserialização
     * @return Snapshot deserializado
     * @throws SnapshotSerializationException em caso de erro na deserialização
     */
    AggregateSnapshot deserialize(String snapshotData, String aggregateType);
    
    /**
     * Serializa com compressão se necessário.
     * 
     * @param snapshot Snapshot a ser serializado
     * @param compressionThreshold Tamanho mínimo para compressão (em bytes)
     * @return Resultado da serialização com informação de compressão
     */
    SnapshotSerializationResult serializeWithCompression(AggregateSnapshot snapshot, int compressionThreshold);
    
    /**
     * Deserializa dados possivelmente comprimidos.
     * 
     * @param snapshotData Dados do snapshot
     * @param aggregateType Tipo do aggregate
     * @param compressed Indica se os dados estão comprimidos
     * @param compressionAlgorithm Algoritmo de compressão usado
     * @return Snapshot deserializado
     */
    AggregateSnapshot deserializeCompressed(String snapshotData, String aggregateType, 
                                          boolean compressed, String compressionAlgorithm);
    
    /**
     * Verifica se o serializer suporta um tipo de aggregate.
     * 
     * @param aggregateType Tipo do aggregate
     * @return true se suportado, false caso contrário
     */
    boolean supports(String aggregateType);
    
    /**
     * Calcula hash dos dados para verificação de integridade.
     * 
     * @param data Dados a serem verificados
     * @return Hash SHA-256 dos dados
     */
    String calculateHash(String data);
    
    /**
     * Valida integridade dos dados usando hash.
     * 
     * @param data Dados a serem validados
     * @param expectedHash Hash esperado
     * @return true se dados são íntegros, false caso contrário
     */
    boolean validateIntegrity(String data, String expectedHash);
}