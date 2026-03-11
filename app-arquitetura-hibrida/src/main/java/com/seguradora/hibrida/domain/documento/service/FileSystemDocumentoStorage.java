package com.seguradora.hibrida.domain.documento.service;

import com.seguradora.hibrida.domain.documento.config.DocumentoProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.*;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Base64;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * Implementação de armazenamento de documentos em filesystem.
 *
 * <p>Características:
 * <ul>
 *   <li>Organização hierárquica: /ano/mes/dia/sinistroId/documentoId_versao</li>
 *   <li>Suporte a criptografia AES-256 opcional</li>
 *   <li>Backup automático em diretório secundário</li>
 *   <li>Validação de integridade</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class FileSystemDocumentoStorage implements DocumentoStorageService {

    private final DocumentoProperties properties;

    private static final String ENCRYPTION_ALGORITHM = "AES/CBC/PKCS5Padding";
    private static final int IV_SIZE = 16;

    // Chave de criptografia (em produção deve vir de configuração segura)
    private static final String ENCRYPTION_KEY = "seguradora2024key1234567890123456"; // 32 bytes

    @Override
    public String salvar(String documentoId, int versao, String sinistroId, byte[] conteudo)
            throws IOException {
        Objects.requireNonNull(documentoId, "Documento ID não pode ser nulo");
        Objects.requireNonNull(sinistroId, "Sinistro ID não pode ser nulo");
        Objects.requireNonNull(conteudo, "Conteúdo não pode ser nulo");

        if (conteudo.length == 0) {
            throw new IllegalArgumentException("Conteúdo não pode ser vazio");
        }

        try {
            // Construir path hierárquico
            Path targetPath = construirPath(sinistroId, documentoId, versao);

            // Criar diretórios se necessário
            Files.createDirectories(targetPath.getParent());

            // Criptografar se habilitado
            byte[] conteudoFinal = conteudo;
            if (properties.getStorage().getEncryption().isEnabled()) {
                conteudoFinal = criptografar(conteudo);
                log.debug("Conteúdo criptografado para documento {}", documentoId);
            }

            // Salvar arquivo
            Files.write(targetPath, conteudoFinal, StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING);

            log.info("Documento salvo: {} (versão {}) - {} bytes",
                    documentoId, versao, conteudoFinal.length);

            // Criar backup se configurado
            if (properties.getStorage().getPath().getSecondary() != null) {
                fazerBackup(targetPath, sinistroId, documentoId, versao);
            }

            return targetPath.toString();

        } catch (Exception e) {
            log.error("Erro ao salvar documento {}: {}", documentoId, e.getMessage(), e);
            throw new IOException("Erro ao salvar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public byte[] recuperar(String path) throws IOException {
        Objects.requireNonNull(path, "Path não pode ser nulo");

        Path filePath = Paths.get(path);

        if (!Files.exists(filePath)) {
            throw new IOException("Documento não encontrado: " + path);
        }

        try {
            byte[] conteudo = Files.readAllBytes(filePath);

            // Descriptografar se habilitado
            if (properties.getStorage().getEncryption().isEnabled()) {
                conteudo = descriptografar(conteudo);
                log.debug("Conteúdo descriptografado de {}", path);
            }

            log.info("Documento recuperado: {} - {} bytes", path, conteudo.length);
            return conteudo;

        } catch (Exception e) {
            log.error("Erro ao recuperar documento {}: {}", path, e.getMessage(), e);
            throw new IOException("Erro ao recuperar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean deletar(String path) throws IOException {
        Objects.requireNonNull(path, "Path não pode ser nulo");

        Path filePath = Paths.get(path);

        if (!Files.exists(filePath)) {
            log.warn("Tentativa de deletar arquivo inexistente: {}", path);
            return false;
        }

        try {
            Files.delete(filePath);
            log.info("Documento deletado: {}", path);
            return true;

        } catch (IOException e) {
            log.error("Erro ao deletar documento {}: {}", path, e.getMessage(), e);
            throw new IOException("Erro ao deletar documento: " + e.getMessage(), e);
        }
    }

    @Override
    public boolean exists(String path) {
        if (path == null) {
            return false;
        }

        return Files.exists(Paths.get(path));
    }

    @Override
    public long getTamanho(String path) {
        if (path == null || !exists(path)) {
            return -1;
        }

        try {
            return Files.size(Paths.get(path));
        } catch (IOException e) {
            log.error("Erro ao obter tamanho de {}: {}", path, e.getMessage());
            return -1;
        }
    }

    @Override
    public String backup(String path) throws IOException {
        if (properties.getStorage().getPath().getSecondary() == null) {
            throw new IOException("Path de backup não configurado");
        }

        Path sourcePath = Paths.get(path);
        if (!Files.exists(sourcePath)) {
            throw new IOException("Arquivo fonte não existe: " + path);
        }

        // Construir path de backup mantendo estrutura
        String relativePath = Paths.get(properties.getStorage().getPath().getPrimary())
                .relativize(sourcePath).toString();

        Path backupPath = Paths.get(properties.getStorage().getPath().getSecondary(), relativePath);

        // Criar diretórios
        Files.createDirectories(backupPath.getParent());

        // Copiar arquivo
        Files.copy(sourcePath, backupPath, StandardCopyOption.REPLACE_EXISTING);

        log.info("Backup criado: {} -> {}", path, backupPath);
        return backupPath.toString();
    }

    @Override
    public String[] listarDocumentosSinistro(String sinistroId) {
        Objects.requireNonNull(sinistroId, "Sinistro ID não pode ser nulo");

        try {
            Path basePath = Paths.get(properties.getStorage().getPath().getPrimary());

            // Buscar recursivamente por sinistroId
            try (Stream<Path> paths = Files.walk(basePath)) {
                return paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().contains(sinistroId))
                        .map(Path::toString)
                        .toArray(String[]::new);
            }

        } catch (IOException e) {
            log.error("Erro ao listar documentos do sinistro {}: {}", sinistroId, e.getMessage());
            return new String[0];
        }
    }

    // ==================== Métodos Auxiliares ====================

    /**
     * Constrói o path hierárquico para armazenamento.
     * Estrutura: /basePath/YYYY/MM/DD/sinistroId/documentoId_vX.dat
     */
    private Path construirPath(String sinistroId, String documentoId, int versao) {
        LocalDate hoje = LocalDate.now();

        String ano = String.valueOf(hoje.getYear());
        String mes = String.format("%02d", hoje.getMonthValue());
        String dia = String.format("%02d", hoje.getDayOfMonth());

        String nomeArquivo = String.format("%s_v%d.dat", documentoId, versao);

        return Paths.get(
                properties.getStorage().getPath().getPrimary(),
                ano, mes, dia, sinistroId, nomeArquivo
        );
    }

    /**
     * Cria backup do arquivo.
     */
    private void fazerBackup(Path originalPath, String sinistroId, String documentoId, int versao)
            throws IOException {
        try {
            String relativePath = Paths.get(properties.getStorage().getPath().getPrimary())
                    .relativize(originalPath).toString();

            Path backupPath = Paths.get(properties.getStorage().getPath().getSecondary(), relativePath);

            Files.createDirectories(backupPath.getParent());
            Files.copy(originalPath, backupPath, StandardCopyOption.REPLACE_EXISTING);

            log.debug("Backup criado para documento {}", documentoId);

        } catch (IOException e) {
            log.warn("Falha ao criar backup para documento {}: {}", documentoId, e.getMessage());
            // Não propaga a exceção para não falhar o salvamento principal
        }
    }

    /**
     * Criptografa conteúdo usando AES-256.
     */
    private byte[] criptografar(byte[] conteudo) throws Exception {
        String algoritmo = properties.getStorage().getEncryption().getAlgorithm();

        if (!"AES-256".equals(algoritmo)) {
            throw new IllegalStateException("Algoritmo não suportado: " + algoritmo);
        }

        // Gerar IV aleatório
        SecureRandom random = new SecureRandom();
        byte[] iv = new byte[IV_SIZE];
        random.nextBytes(iv);

        // Criar cipher
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

        // Criptografar
        byte[] encrypted = cipher.doFinal(conteudo);

        // Combinar IV + dados criptografados
        byte[] resultado = new byte[IV_SIZE + encrypted.length];
        System.arraycopy(iv, 0, resultado, 0, IV_SIZE);
        System.arraycopy(encrypted, 0, resultado, IV_SIZE, encrypted.length);

        return resultado;
    }

    /**
     * Descriptografa conteúdo usando AES-256.
     */
    private byte[] descriptografar(byte[] conteudo) throws Exception {
        if (conteudo.length < IV_SIZE) {
            throw new IllegalArgumentException("Conteúdo criptografado inválido");
        }

        // Extrair IV
        byte[] iv = Arrays.copyOfRange(conteudo, 0, IV_SIZE);
        byte[] encrypted = Arrays.copyOfRange(conteudo, IV_SIZE, conteudo.length);

        // Criar cipher
        Cipher cipher = Cipher.getInstance(ENCRYPTION_ALGORITHM);
        SecretKeySpec keySpec = new SecretKeySpec(ENCRYPTION_KEY.getBytes(), "AES");
        IvParameterSpec ivSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

        // Descriptografar
        return cipher.doFinal(encrypted);
    }
}
