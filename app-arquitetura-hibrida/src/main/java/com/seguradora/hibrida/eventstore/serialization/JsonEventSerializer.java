package com.seguradora.hibrida.eventstore.serialization;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.seguradora.hibrida.eventstore.exception.SerializationException;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * Implementação de EventSerializer usando Jackson JSON.
 * 
 * Fornece serialização/deserialização de eventos com suporte a
 * compressão GZIP para eventos grandes e versionamento automático.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JsonEventSerializer implements EventSerializer {
    
    private final ObjectMapper objectMapper;
    
    private static final String COMPRESSION_ALGORITHM = "GZIP";
    private static final int DEFAULT_COMPRESSION_THRESHOLD = 1024; // 1KB
    
    @Override
    public String serialize(DomainEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (JsonProcessingException e) {
            log.error("Erro ao serializar evento: {}", event.getClass().getSimpleName(), e);
            throw new SerializationException("Falha na serialização do evento", e);
        }
    }
    
    @Override
    public DomainEvent deserialize(String eventData, String eventType) {
        try {
            Class<?> eventClass = Class.forName(eventType);
            if (!DomainEvent.class.isAssignableFrom(eventClass)) {
                throw new SerializationException("Tipo não é um DomainEvent válido: " + eventType);
            }
            
            @SuppressWarnings("unchecked")
            Class<? extends DomainEvent> domainEventClass = (Class<? extends DomainEvent>) eventClass;
            
            return objectMapper.readValue(eventData, domainEventClass);
        } catch (ClassNotFoundException e) {
            log.error("Classe de evento não encontrada: {}", eventType, e);
            throw new SerializationException("Tipo de evento não encontrado: " + eventType, e);
        } catch (JsonProcessingException e) {
            log.error("Erro ao deserializar evento do tipo: {}", eventType, e);
            throw new SerializationException("Falha na deserialização do evento", e);
        }
    }
    
    @Override
    public SerializationResult serializeWithCompression(DomainEvent event, int compressionThreshold) {
        String serializedData = serialize(event);
        int originalSize = serializedData.getBytes(StandardCharsets.UTF_8).length;
        
        if (originalSize < compressionThreshold) {
            return SerializationResult.builder()
                    .data(serializedData)
                    .compressed(false)
                    .originalSize(originalSize)
                    .finalSize(originalSize)
                    .build();
        }
        
        try {
            String compressedData = compressData(serializedData);
            int finalSize = compressedData.getBytes(StandardCharsets.UTF_8).length;
            
            // Só usa compressão se realmente economizar espaço
            if (finalSize < originalSize) {
                log.debug("Evento comprimido: {} -> {} bytes ({}% economia)", 
                         originalSize, finalSize, 
                         Math.round((1.0 - (double) finalSize / originalSize) * 100));
                
                return SerializationResult.builder()
                        .data(compressedData)
                        .compressed(true)
                        .originalSize(originalSize)
                        .finalSize(finalSize)
                        .compressionAlgorithm(COMPRESSION_ALGORITHM)
                        .build();
            } else {
                return SerializationResult.builder()
                        .data(serializedData)
                        .compressed(false)
                        .originalSize(originalSize)
                        .finalSize(originalSize)
                        .build();
            }
        } catch (IOException e) {
            log.warn("Falha na compressão, usando dados não comprimidos", e);
            return SerializationResult.builder()
                    .data(serializedData)
                    .compressed(false)
                    .originalSize(originalSize)
                    .finalSize(originalSize)
                    .build();
        }
    }
    
    @Override
    public DomainEvent deserializeCompressed(String eventData, String eventType, boolean compressed) {
        if (!compressed) {
            return deserialize(eventData, eventType);
        }
        
        try {
            String decompressedData = decompressData(eventData);
            return deserialize(decompressedData, eventType);
        } catch (IOException e) {
            log.error("Erro ao descomprimir evento do tipo: {}", eventType, e);
            throw new SerializationException("Falha na descompressão do evento", e);
        }
    }
    
    @Override
    public boolean supports(String eventType) {
        try {
            Class<?> eventClass = Class.forName(eventType);
            return DomainEvent.class.isAssignableFrom(eventClass);
        } catch (ClassNotFoundException e) {
            return false;
        }
    }
    
    /**
     * Comprime dados usando GZIP e codifica em Base64.
     */
    private String compressData(String data) throws IOException {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
             GZIPOutputStream gzipOut = new GZIPOutputStream(baos)) {
            
            gzipOut.write(data.getBytes(StandardCharsets.UTF_8));
            gzipOut.finish();
            
            return Base64.getEncoder().encodeToString(baos.toByteArray());
        }
    }
    
    /**
     * Descomprime dados de Base64 usando GZIP.
     */
    private String decompressData(String compressedData) throws IOException {
        byte[] compressed = Base64.getDecoder().decode(compressedData);
        
        try (ByteArrayInputStream bais = new ByteArrayInputStream(compressed);
             GZIPInputStream gzipIn = new GZIPInputStream(bais);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            
            byte[] buffer = new byte[1024];
            int len;
            while ((len = gzipIn.read(buffer)) != -1) {
                baos.write(buffer, 0, len);
            }
            
            return baos.toString(StandardCharsets.UTF_8);
        }
    }
}