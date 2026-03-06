package com.seguradora.hibrida.eventstore.archive.impl;

import com.seguradora.hibrida.eventstore.archive.ArchiveStorageService;
import com.seguradora.hibrida.eventstore.archive.EventArchiveProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 * Implementação de storage de arquivos usando sistema de arquivos local.
 * 
 * <p>Adequado para desenvolvimento e ambientes onde não há
 * necessidade de storage distribuído.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
@ConditionalOnProperty(name = "eventstore.archive.storage.type", havingValue = "filesystem", matchIfMissing = true)
public class FileSystemArchiveStorage implements ArchiveStorageService {
    
    private final EventArchiveProperties properties;
    
    @Override
    public boolean store(String key, byte[] data) {
        try {
            Path filePath = getFilePath(key);
            
            // Criar diretórios se não existirem
            Files.createDirectories(filePath.getParent());
            
            // Escrever dados
            Files.write(filePath, data, StandardOpenOption.CREATE, StandardOpenOption.WRITE);
            
            log.info("Arquivo armazenado com sucesso: {} ({} bytes)", key, data.length);
            return true;
            
        } catch (IOException e) {
            log.error("Erro ao armazenar arquivo {}: {}", key, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public byte[] retrieve(String key) {
        try {
            Path filePath = getFilePath(key);
            
            if (!Files.exists(filePath)) {
                log.warn("Arquivo não encontrado: {}", key);
                return null;
            }
            
            byte[] data = Files.readAllBytes(filePath);
            log.debug("Arquivo recuperado: {} ({} bytes)", key, data.length);
            return data;
            
        } catch (IOException e) {
            log.error("Erro ao recuperar arquivo {}: {}", key, e.getMessage(), e);
            return null;
        }
    }
    
    @Override
    public boolean exists(String key) {
        Path filePath = getFilePath(key);
        return Files.exists(filePath);
    }
    
    @Override
    public boolean delete(String key) {
        try {
            Path filePath = getFilePath(key);
            
            if (!Files.exists(filePath)) {
                log.warn("Tentativa de deletar arquivo inexistente: {}", key);
                return true; // Considerar sucesso se já não existe
            }
            
            Files.delete(filePath);
            log.info("Arquivo removido: {}", key);
            return true;
            
        } catch (IOException e) {
            log.error("Erro ao remover arquivo {}: {}", key, e.getMessage(), e);
            return false;
        }
    }
    
    @Override
    public long getSize(String key) {
        try {
            Path filePath = getFilePath(key);
            
            if (!Files.exists(filePath)) {
                return -1;
            }
            
            return Files.size(filePath);
            
        } catch (IOException e) {
            log.error("Erro ao obter tamanho do arquivo {}: {}", key, e.getMessage(), e);
            return -1;
        }
    }
    
    /**
     * Constrói o caminho completo do arquivo.
     */
    private Path getFilePath(String key) {
        String basePath = properties.getStorage().getBasePath();
        return Paths.get(basePath, key);
    }
}