package com.seguradora.hibrida.eventstore.archive;

/**
 * Interface para serviços de storage de arquivos.
 * 
 * <p>Permite implementações diferentes:
 * <ul>
 *   <li>Sistema de arquivos local</li>
 *   <li>Amazon S3</li>
 *   <li>MinIO</li>
 *   <li>Azure Blob Storage</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface ArchiveStorageService {
    
    /**
     * Armazena dados no storage frio.
     * 
     * @param key Chave única do arquivo
     * @param data Dados comprimidos
     * @return true se armazenado com sucesso
     */
    boolean store(String key, byte[] data);
    
    /**
     * Recupera dados do storage frio.
     * 
     * @param key Chave do arquivo
     * @return Dados comprimidos ou null se não encontrado
     */
    byte[] retrieve(String key);
    
    /**
     * Verifica se um arquivo existe.
     * 
     * @param key Chave do arquivo
     * @return true se existe
     */
    boolean exists(String key);
    
    /**
     * Remove um arquivo do storage.
     * 
     * @param key Chave do arquivo
     * @return true se removido com sucesso
     */
    boolean delete(String key);
    
    /**
     * Obtém tamanho de um arquivo.
     * 
     * @param key Chave do arquivo
     * @return Tamanho em bytes ou -1 se não encontrado
     */
    long getSize(String key);
}