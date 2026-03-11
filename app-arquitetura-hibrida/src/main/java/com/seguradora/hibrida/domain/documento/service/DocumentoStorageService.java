package com.seguradora.hibrida.domain.documento.service;

import java.io.IOException;

/**
 * Interface para serviço de armazenamento de documentos.
 *
 * <p>Define operações básicas para persistência de documentos:
 * <ul>
 *   <li>Salvar conteúdo</li>
 *   <li>Recuperar conteúdo</li>
 *   <li>Deletar documentos</li>
 *   <li>Verificar existência</li>
 * </ul>
 *
 * <p>Implementações podem usar diferentes backends:
 * <ul>
 *   <li>Filesystem local</li>
 *   <li>S3 ou object storage</li>
 *   <li>Database BLOB</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
public interface DocumentoStorageService {

    /**
     * Salva o conteúdo de um documento.
     *
     * @param documentoId ID único do documento
     * @param versao Versão do documento
     * @param sinistroId ID do sinistro (para organização)
     * @param conteudo Conteúdo em bytes
     * @return Path ou referência onde foi salvo
     * @throws IOException se houver erro ao salvar
     */
    String salvar(String documentoId, int versao, String sinistroId, byte[] conteudo)
            throws IOException;

    /**
     * Recupera o conteúdo de um documento.
     *
     * @param path Path ou referência do documento
     * @return Conteúdo em bytes
     * @throws IOException se houver erro ao ler ou documento não existir
     */
    byte[] recuperar(String path) throws IOException;

    /**
     * Deleta um documento do storage.
     *
     * @param path Path ou referência do documento
     * @return true se foi deletado com sucesso
     * @throws IOException se houver erro ao deletar
     */
    boolean deletar(String path) throws IOException;

    /**
     * Verifica se um documento existe no storage.
     *
     * @param path Path ou referência do documento
     * @return true se existe
     */
    boolean exists(String path);

    /**
     * Retorna o tamanho de um documento em bytes.
     *
     * @param path Path ou referência do documento
     * @return Tamanho em bytes, ou -1 se não existir
     */
    long getTamanho(String path);

    /**
     * Move ou copia um documento para backup.
     *
     * @param path Path original
     * @return Path do backup
     * @throws IOException se houver erro
     */
    String backup(String path) throws IOException;

    /**
     * Lista todos os documentos de um sinistro.
     *
     * @param sinistroId ID do sinistro
     * @return Array de paths encontrados
     */
    String[] listarDocumentosSinistro(String sinistroId);
}
