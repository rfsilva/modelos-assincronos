package com.seguradora.hibrida.domain.documento.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link DocumentoStorageService}.
 *
 * <p>Valida o contrato da interface de armazenamento:
 * <ul>
 *   <li>Operações de salvamento</li>
 *   <li>Operações de recuperação</li>
 *   <li>Operações de deleção</li>
 *   <li>Verificações de existência e tamanho</li>
 *   <li>Operações de backup</li>
 *   <li>Listagem de documentos</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DocumentoStorageService - Testes Unitários")
class DocumentoStorageServiceTest {

    @Mock
    private DocumentoStorageService storageService;

    private byte[] conteudoTeste;

    @BeforeEach
    void setUp() {
        conteudoTeste = "Conteúdo de teste do documento".getBytes();
        configurarMocksComuns();
    }

    // ==================== Helper Methods ====================

    /**
     * Configura comportamentos comuns dos mocks.
     */
    private void configurarMocksComuns() {
        try {
            // Salvar retorna path válido
            lenient().when(storageService.salvar(anyString(), anyInt(), anyString(), any(byte[].class)))
                    .thenAnswer(invocation -> {
                        String docId = invocation.getArgument(0);
                        int versao = invocation.getArgument(1);
                        String sinistroId = invocation.getArgument(2);
                        return String.format("/storage/%s/%s_v%d.dat", sinistroId, docId, versao);
                    });

            // Recuperar retorna conteúdo
            lenient().when(storageService.recuperar(anyString()))
                    .thenReturn(conteudoTeste);

            // Deletar retorna true
            lenient().when(storageService.deletar(anyString()))
                    .thenReturn(true);

            // Backup retorna path de backup
            lenient().when(storageService.backup(anyString()))
                    .thenAnswer(invocation -> {
                        String path = invocation.getArgument(0);
                        return path.replace("/storage/", "/backup/");
                    });
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        // Exists retorna true
        lenient().when(storageService.exists(anyString()))
                .thenReturn(true);

        // getTamanho retorna tamanho do conteúdo
        lenient().when(storageService.getTamanho(anyString()))
                .thenReturn((long) conteudoTeste.length);

        // Listar retorna array vazio
        lenient().when(storageService.listarDocumentosSinistro(anyString()))
                .thenReturn(new String[0]);
    }

    // ==================== Testes de Salvamento ====================

    @Nested
    @DisplayName("Operações de Salvamento")
    class OperacoesSalvamento {

        @Test
        @DisplayName("Deve salvar documento e retornar path")
        void deveSalvarDocumentoRetornarPath() throws IOException {
            // Given
            String documentoId = "DOC-001";
            int versao = 1;
            String sinistroId = "SIN-123";

            // When
            String path = storageService.salvar(documentoId, versao, sinistroId, conteudoTeste);

            // Then
            assertThat(path).isNotNull();
            assertThat(path).contains(documentoId);
            assertThat(path).contains(sinistroId);
            verify(storageService).salvar(documentoId, versao, sinistroId, conteudoTeste);
        }

        @Test
        @DisplayName("Deve invocar salvamento com parâmetros corretos")
        void deveInvocarSalvamentoComParametrosCorretos() throws IOException {
            // Given
            String documentoId = "DOC-002";
            int versao = 2;
            String sinistroId = "SIN-456";
            byte[] conteudo = "Novo conteúdo".getBytes();

            // When
            storageService.salvar(documentoId, versao, sinistroId, conteudo);

            // Then
            verify(storageService).salvar(
                    eq(documentoId),
                    eq(versao),
                    eq(sinistroId),
                    eq(conteudo)
            );
        }

        @Test
        @DisplayName("Deve propagar IOException em caso de erro")
        void devePropagar_IOException() throws IOException {
            // Given
            when(storageService.salvar(anyString(), anyInt(), anyString(), any(byte[].class)))
                    .thenThrow(new IOException("Erro de I/O"));

            // When & Then
            assertThatThrownBy(() -> storageService.salvar("DOC-001", 1, "SIN-123", conteudoTeste))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Erro de I/O");
        }

        @Test
        @DisplayName("Deve permitir múltiplas versões do mesmo documento")
        void devePermitirMultiplasVersoes() throws IOException {
            // Given
            String documentoId = "DOC-003";
            String sinistroId = "SIN-789";

            // When
            String pathV1 = storageService.salvar(documentoId, 1, sinistroId, conteudoTeste);
            String pathV2 = storageService.salvar(documentoId, 2, sinistroId, conteudoTeste);

            // Then
            assertThat(pathV1).isNotEqualTo(pathV2);
            assertThat(pathV1).contains("_v1");
            assertThat(pathV2).contains("_v2");
        }
    }

    // ==================== Testes de Recuperação ====================

    @Nested
    @DisplayName("Operações de Recuperação")
    class OperacoesRecuperacao {

        @Test
        @DisplayName("Deve recuperar conteúdo do documento")
        void deveRecuperarConteudoDocumento() throws IOException {
            // Given
            String path = "/storage/SIN-123/DOC-001_v1.dat";

            // When
            byte[] conteudo = storageService.recuperar(path);

            // Then
            assertThat(conteudo).isNotNull();
            assertThat(conteudo).isEqualTo(conteudoTeste);
            verify(storageService).recuperar(path);
        }

        @Test
        @DisplayName("Deve lançar IOException para path inexistente")
        void deveLancarIOExceptionParaPathInexistente() throws IOException {
            // Given
            String path = "/storage/nao-existe.dat";
            when(storageService.recuperar(path))
                    .thenThrow(new IOException("Documento não encontrado"));

            // When & Then
            assertThatThrownBy(() -> storageService.recuperar(path))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("não encontrado");
        }

        @Test
        @DisplayName("Deve invocar recuperação com path correto")
        void deveInvocarRecuperacaoComPathCorreto() throws IOException {
            // Given
            String path = "/storage/SIN-456/DOC-002_v1.dat";

            // When
            storageService.recuperar(path);

            // Then
            verify(storageService).recuperar(eq(path));
        }
    }

    // ==================== Testes de Deleção ====================

    @Nested
    @DisplayName("Operações de Deleção")
    class OperacoesDelecao {

        @Test
        @DisplayName("Deve deletar documento existente")
        void deveDeletarDocumentoExistente() throws IOException {
            // Given
            String path = "/storage/SIN-123/DOC-001_v1.dat";

            // When
            boolean deletado = storageService.deletar(path);

            // Then
            assertThat(deletado).isTrue();
            verify(storageService).deletar(path);
        }

        @Test
        @DisplayName("Deve retornar false para documento inexistente")
        void deveRetornarFalseParaDocumentoInexistente() throws IOException {
            // Given
            String path = "/storage/nao-existe.dat";
            when(storageService.deletar(path)).thenReturn(false);

            // When
            boolean deletado = storageService.deletar(path);

            // Then
            assertThat(deletado).isFalse();
        }

        @Test
        @DisplayName("Deve invocar deleção com path correto")
        void deveInvocarDelecaoComPathCorreto() throws IOException {
            // Given
            String path = "/storage/SIN-456/DOC-002_v1.dat";

            // When
            storageService.deletar(path);

            // Then
            verify(storageService).deletar(eq(path));
        }

        @Test
        @DisplayName("Deve propagar IOException em caso de erro")
        void devePropagar_IOExceptionAoDeletar() throws IOException {
            // Given
            String path = "/storage/erro.dat";
            when(storageService.deletar(path))
                    .thenThrow(new IOException("Erro ao deletar"));

            // When & Then
            assertThatThrownBy(() -> storageService.deletar(path))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("Erro ao deletar");
        }
    }

    // ==================== Testes de Verificação de Existência ====================

    @Nested
    @DisplayName("Verificação de Existência")
    class VerificacaoExistencia {

        @Test
        @DisplayName("Deve retornar true para documento existente")
        void deveRetornarTrueParaDocumentoExistente() {
            // Given
            String path = "/storage/SIN-123/DOC-001_v1.dat";

            // When
            boolean existe = storageService.exists(path);

            // Then
            assertThat(existe).isTrue();
            verify(storageService).exists(path);
        }

        @Test
        @DisplayName("Deve retornar false para documento inexistente")
        void deveRetornarFalseParaDocumentoInexistente() {
            // Given
            String path = "/storage/nao-existe.dat";
            when(storageService.exists(path)).thenReturn(false);

            // When
            boolean existe = storageService.exists(path);

            // Then
            assertThat(existe).isFalse();
        }

        @Test
        @DisplayName("Deve invocar verificação com path correto")
        void deveInvocarVerificacaoComPathCorreto() {
            // Given
            String path = "/storage/SIN-456/DOC-002_v1.dat";

            // When
            storageService.exists(path);

            // Then
            verify(storageService).exists(eq(path));
        }
    }

    // ==================== Testes de Obtenção de Tamanho ====================

    @Nested
    @DisplayName("Obtenção de Tamanho")
    class ObtencaoTamanho {

        @Test
        @DisplayName("Deve retornar tamanho do documento")
        void deveRetornarTamanhoDocumento() {
            // Given
            String path = "/storage/SIN-123/DOC-001_v1.dat";

            // When
            long tamanho = storageService.getTamanho(path);

            // Then
            assertThat(tamanho).isPositive();
            assertThat(tamanho).isEqualTo(conteudoTeste.length);
            verify(storageService).getTamanho(path);
        }

        @Test
        @DisplayName("Deve retornar -1 para documento inexistente")
        void deveRetornarMenosUmParaDocumentoInexistente() {
            // Given
            String path = "/storage/nao-existe.dat";
            when(storageService.getTamanho(path)).thenReturn(-1L);

            // When
            long tamanho = storageService.getTamanho(path);

            // Then
            assertThat(tamanho).isEqualTo(-1);
        }

        @Test
        @DisplayName("Deve invocar getTamanho com path correto")
        void deveInvocarGetTamanhoComPathCorreto() {
            // Given
            String path = "/storage/SIN-456/DOC-002_v1.dat";

            // When
            storageService.getTamanho(path);

            // Then
            verify(storageService).getTamanho(eq(path));
        }
    }

    // ==================== Testes de Backup ====================

    @Nested
    @DisplayName("Operações de Backup")
    class OperacoesBackup {

        @Test
        @DisplayName("Deve criar backup e retornar path de backup")
        void deveCriarBackupRetornarPathBackup() throws IOException {
            // Given
            String path = "/storage/SIN-123/DOC-001_v1.dat";

            // When
            String backupPath = storageService.backup(path);

            // Then
            assertThat(backupPath).isNotNull();
            assertThat(backupPath).contains("backup");
            verify(storageService).backup(path);
        }

        @Test
        @DisplayName("Deve lançar IOException se arquivo não existe")
        void deveLancarIOExceptionSeArquivoNaoExiste() throws IOException {
            // Given
            String path = "/storage/nao-existe.dat";
            when(storageService.backup(path))
                    .thenThrow(new IOException("Arquivo fonte não existe"));

            // When & Then
            assertThatThrownBy(() -> storageService.backup(path))
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("não existe");
        }

        @Test
        @DisplayName("Deve invocar backup com path correto")
        void deveInvocarBackupComPathCorreto() throws IOException {
            // Given
            String path = "/storage/SIN-456/DOC-002_v1.dat";

            // When
            storageService.backup(path);

            // Then
            verify(storageService).backup(eq(path));
        }
    }

    // ==================== Testes de Listagem ====================

    @Nested
    @DisplayName("Listagem de Documentos")
    class ListagemDocumentos {

        @Test
        @DisplayName("Deve listar documentos do sinistro")
        void deveListarDocumentosSinistro() {
            // Given
            String sinistroId = "SIN-123";
            String[] paths = {
                    "/storage/SIN-123/DOC-001_v1.dat",
                    "/storage/SIN-123/DOC-002_v1.dat"
            };
            when(storageService.listarDocumentosSinistro(sinistroId)).thenReturn(paths);

            // When
            String[] resultado = storageService.listarDocumentosSinistro(sinistroId);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).hasSize(2);
            assertThat(resultado).containsExactly(paths);
            verify(storageService).listarDocumentosSinistro(sinistroId);
        }

        @Test
        @DisplayName("Deve retornar array vazio para sinistro sem documentos")
        void deveRetornarArrayVazioParaSinistroSemDocumentos() {
            // Given
            String sinistroId = "SIN-999";

            // When
            String[] resultado = storageService.listarDocumentosSinistro(sinistroId);

            // Then
            assertThat(resultado).isNotNull();
            assertThat(resultado).isEmpty();
        }

        @Test
        @DisplayName("Deve invocar listagem com sinistro ID correto")
        void deveInvocarListagemComSinistroIdCorreto() {
            // Given
            String sinistroId = "SIN-456";

            // When
            storageService.listarDocumentosSinistro(sinistroId);

            // Then
            verify(storageService).listarDocumentosSinistro(eq(sinistroId));
        }

        @Test
        @DisplayName("Deve listar múltiplas versões do mesmo documento")
        void deveListarMultiplasVersoes() {
            // Given
            String sinistroId = "SIN-789";
            String[] paths = {
                    "/storage/SIN-789/DOC-001_v1.dat",
                    "/storage/SIN-789/DOC-001_v2.dat",
                    "/storage/SIN-789/DOC-001_v3.dat"
            };
            when(storageService.listarDocumentosSinistro(sinistroId)).thenReturn(paths);

            // When
            String[] resultado = storageService.listarDocumentosSinistro(sinistroId);

            // Then
            assertThat(resultado).hasSize(3);
            assertThat(resultado).allMatch(p -> p.contains("DOC-001"));
        }
    }

    // ==================== Testes de Integração do Contrato ====================

    @Nested
    @DisplayName("Integração do Contrato")
    class IntegracaoContrato {

        @Test
        @DisplayName("Deve seguir fluxo completo: salvar -> recuperar -> deletar")
        void deveExecutarFluxoCompleto() throws IOException {
            // Given
            String documentoId = "DOC-100";
            int versao = 1;
            String sinistroId = "SIN-1000";

            // When & Then
            // 1. Salvar
            String path = storageService.salvar(documentoId, versao, sinistroId, conteudoTeste);
            assertThat(path).isNotNull();

            // 2. Verificar existência
            when(storageService.exists(path)).thenReturn(true);
            assertThat(storageService.exists(path)).isTrue();

            // 3. Recuperar
            byte[] conteudoRecuperado = storageService.recuperar(path);
            assertThat(conteudoRecuperado).isEqualTo(conteudoTeste);

            // 4. Deletar
            boolean deletado = storageService.deletar(path);
            assertThat(deletado).isTrue();

            // Verificar todas as chamadas
            verify(storageService).salvar(documentoId, versao, sinistroId, conteudoTeste);
            verify(storageService).exists(path);
            verify(storageService).recuperar(path);
            verify(storageService).deletar(path);
        }

        @Test
        @DisplayName("Deve seguir fluxo com backup")
        void deveExecutarFluxoComBackup() throws IOException {
            // Given
            String documentoId = "DOC-101";
            int versao = 1;
            String sinistroId = "SIN-1001";

            // When
            String path = storageService.salvar(documentoId, versao, sinistroId, conteudoTeste);
            String backupPath = storageService.backup(path);

            // Then
            assertThat(backupPath).isNotNull();
            assertThat(backupPath).isNotEqualTo(path);
            verify(storageService).salvar(documentoId, versao, sinistroId, conteudoTeste);
            verify(storageService).backup(path);
        }
    }
}
