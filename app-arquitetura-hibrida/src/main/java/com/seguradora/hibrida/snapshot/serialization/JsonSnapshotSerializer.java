package com.seguradora.hibrida.snapshot.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.snapshot.exception.SnapshotCompressionException;
import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementação de serialização de snapshots usando JSON com compressão GZIP.
 * 
 * <p>Características:
 * <ul>
 *   <li>Serialização JSON com Jackson otimizado</li>
 *   <li>Compressão GZIP automática para dados grandes</li>
 *   <li>Validação de integridade com SHA-256</li>
 *   <li>Suporte a todos os tipos de aggregate</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JsonSnapshotSerializer implements SnapshotSerializer {
    
    private final ObjectMapper objectMapper;
    
    private static final String COMPRESSION_ALGORITHM = "GZIP";
    private static final String HASH_ALGORITHM = "SHA-256";
    
    @Override
    public String serialize(AggregateSnapshot snapshot) {
        try {
            long startTime = System.currentTimeMillis();
            String json = objectMapper.writeValueAsString(snapshot);
            long serializationTime = System.currentTimeMillis() - startTime;
            
            log.debug("Snapshot serialized in {}ms for aggregate {}", 
                     serializationTime, snapshot.getAggregateId());
            
            return json;
        } catch (JsonProcessingException e) {
            throw new SnapshotSerializationException(
                "Failed to serialize snapshot", e, "serialize", 
                snapshot.getAggregateId(), snapshot.getAggregateType()
            );
        }
    }
    
    @Override
    public AggregateSnapshot deserialize(String snapshotData, String aggregateType) {
        try {
            long startTime = System.currentTimeMillis();
            AggregateSnapshot snapshot = objectMapper.readValue(snapshotData, AggregateSnapshot.class);
            long deserializationTime = System.currentTimeMillis() - startTime;
            
            log.debug("Snapshot deserialized in {}ms for aggregate type {}", 
                     deserializationTime, aggregateType);
            
            return snapshot;
        } catch (JsonProcessingException e) {
            throw new SnapshotSerializationException(
                "Failed to deserialize snapshot", e, "deserialize", 
                null, aggregateType
            );
        }
    }
    
    @Override
    public SnapshotSerializationResult serializeWithCompression(AggregateSnapshot snapshot, int compressionThreshold) {
        long totalStartTime = System.currentTimeMillis();
        
        // Serialização inicial
        long serializationStartTime = System.currentTimeMillis();
        String json = serialize(snapshot);
        long serializationTime = System.currentTimeMillis() - serializationStartTime;
        
        int originalSize = json.getBytes(StandardCharsets.UTF_8).length;
        String finalData = json;
        boolean compressed = false;
        int compressedSize = originalSize;
        long compressionTime = 0;
        
        // Aplicar compressão se necessário
        if (originalSize > compressionThreshold) {
            long compressionStartTime = System.currentTimeMillis();
            try {
                String compressedData = compressData(json);
                int newSize = compressedData.getBytes(StandardCharsets.UTF_8).length;
                
                // Usar compressão apenas se for efetiva (economiza pelo menos 10%)
                if (newSize < originalSize * 0.9) {
                    finalData = compressedData;
                    compressed = true;
                    compressedSize = newSize;
                    
                    log.debug("Snapshot compressed from {} to {} bytes ({}% reduction) for aggregate {}", 
                             originalSize, compressedSize, 
                             Math.round((1.0 - (double) compressedSize / originalSize) * 100),
                             snapshot.getAggregateId());
                }
            } catch (Exception e) {
                log.warn("Failed to compress snapshot for aggregate {}, using uncompressed data", 
                        snapshot.getAggregateId(), e);
            }
            compressionTime = System.currentTimeMillis() - compressionStartTime;
        }
        
        // Calcular hash para integridade
        String dataHash = calculateHash(finalData);
        
        long totalTime = System.currentTimeMillis() - totalStartTime;
        log.debug("Snapshot serialization completed in {}ms (serialization: {}ms, compression: {}ms) for aggregate {}", 
                 totalTime, serializationTime, compressionTime, snapshot.getAggregateId());
        
        return SnapshotSerializationResult.builder()
                .serializedData(finalData)
                .compressed(compressed)
                .compressionAlgorithm(compressed ? COMPRESSION_ALGORITHM : null)
                .originalSize(originalSize)
                .compressedSize(compressedSize)
                .dataHash(dataHash)
                .serializationTimeMs(serializationTime)
                .compressionTimeMs(compressionTime)
                .build();
    }
    
    @Override
    public AggregateSnapshot deserializeCompressed(String snapshotData, String aggregateType, 
                                                 boolean compressed, String compressionAlgorithm) {
        try {
            String jsonData = snapshotData;
            
            if (compressed) {
                if (!COMPRESSION_ALGORITHM.equals(compressionAlgorithm)) {
                    throw new SnapshotCompressionException(
                        "Unsupported compression algorithm: " + compressionAlgorithm,
                        compressionAlgorithm
                    );
                }
                
                long decompressionStartTime = System.currentTimeMillis();
                jsonData = decompressData(snapshotData);
                long decompressionTime = System.currentTimeMillis() - decompressionStartTime;
                
                log.debug("Snapshot decompressed in {}ms for aggregate type {}", 
                         decompressionTime, aggregateType);
            }
            
            return deserialize(jsonData, aggregateType);
        } catch (Exception e) {
            if (e instanceof SnapshotSerializationException) {
                throw e;
            }
            throw new SnapshotSerializationException(
                "Failed to deserialize compressed snapshot", e, "deserializeCompressed",
                null, aggregateType
            );
        }
    }
    
    @Override
    public boolean supports(String aggregateType) {
        // Este serializer suporta todos os tipos de aggregate
        return true;
    }
    
    @Override
    public String calculateHash(String data) {
        try {
            MessageDigest digest = MessageDigest.getInstance(HASH_ALGORITHM);
            byte[] hashBytes = digest.digest(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new SnapshotSerializationException(
                "Hash algorithm not available: " + HASH_ALGORITHM, e, "calculateHash"
            );
        }
    }
    
    @Override
    public boolean validateIntegrity(String data, String expectedHash) {
        if (expectedHash == null || expectedHash.trim().isEmpty()) {
            return true; // Sem hash para validar
        }
        
        String actualHash = calculateHash(data);
        boolean valid = expectedHash.equals(actualHash);
        
        if (!valid) {
            log.warn("Snapshot integrity validation failed. Expected: {}, Actual: {}", 
                    expectedHash, actualHash);
        }
        
        return valid;
    }
    
    /**
     * Comprime dados usando GZIP.
     * 
     * @param data Dados a serem comprimidos
     * @return Dados comprimidos em Base64
     */
    private String compressData(String data) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            
            gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
            gzipOut.finish();
            
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        } catch (IOException e) {
            throw new SnapshotCompressionException(
                "Failed to compress snapshot data", e, COMPRESSION_ALGORITHM
            );
        }
    }
    
    /**
     * Descomprime dados GZIP.
     * 
     * @param compressedData Dados comprimidos em Base64
     * @return Dados descomprimidos
     */
    private String decompressData(String compressedData) {
        try {
            byte[] compressedBytes = Base64.getDecoder().decode(compressedData);
            
            try (ByteArrayInputStream bais = new ByteArrayInputStream(compressedBytes);
                 GZIPInputStream gzipIn = new GZIPInputStream(bais);
                 ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
                
                byte[] buffer = new byte[1024];
                int len;
                while ((len = gzipIn.read(buffer)) != -1) {
                    baos.write(buffer, 0, len);
                }
                
                return baos.toString(StandardCharsets.UTF_8);
            }
        } catch (IOException e) {
            throw new SnapshotCompressionException(
                "Failed to decompress snapshot data", e, COMPRESSION_ALGORITHM
            );
        }
    }
}