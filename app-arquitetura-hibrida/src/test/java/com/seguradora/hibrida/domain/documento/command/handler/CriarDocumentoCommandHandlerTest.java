package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.CriarDocumentoCommand;
import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import com.seguradora.hibrida.domain.documento.service.DocumentoStorageService;
import com.seguradora.hibrida.domain.documento.service.DocumentoValidatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CriarDocumentoCommandHandler.
 *
 * <p>Testa todos os cenários de criação de documentos incluindo:
 * <ul>
 *   <li>Criação com sucesso</li>
 *   <li>Validações de comando e conteúdo</li>
 *   <li>Interações com storage e repository</li>
 *   <li>Tratamento de erros e rollback</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CriarDocumentoCommandHandler - Testes Unitários")
class CriarDocumentoCommandHandlerTest {

    @Mock
    private AggregateRepository<DocumentoAggregate> aggregateRepository;

    @Mock
    private DocumentoStorageService storageService;

    @Mock
    private DocumentoValidatorService validatorService;

    @InjectMocks
    private CriarDocumentoCommandHandler handler;

    private CriarDocumentoCommand validCommand;
    private byte[] validContent;

    @BeforeEach
    void setUp() {
        validContent = "PDF Content".getBytes();

        validCommand = CriarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .nome("boletim_ocorrencia.pdf")
                .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                .conteudo(validContent)
                .formato("application/pdf")
                .sinistroId("SIN-001")
                .operadorId("OP-001")
                .operadorNome("João Silva")
                .build();
    }

    /**
     * Configura mocks para cenário de sucesso.
     * Usa lenient() para evitar UnnecessaryStubbingException em testes que não usam todos os mocks.
     */
    private void setupSuccessScenario() {
        try {
            lenient().when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            lenient().when(validatorService.validarTamanho(anyLong(), any())).thenReturn(Collections.emptyList());
            lenient().when(validatorService.validarConteudo(any(), any())).thenReturn(Collections.emptyList());
            lenient().when(validatorService.verificarFormatoReal(any(), any())).thenReturn(true);
            lenient().when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar mocks", e);
        }
    }

    @Nested
    @DisplayName("Execução com Sucesso")
    class ExecucaoSucesso {

        @Test
        @DisplayName("Deve criar documento com sucesso quando todos os dados são válidos")
        void deveCriarDocumentoComSucesso() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            String documentoId = handler.handle(validCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");

            // Verificar interações
            verify(validatorService).validarTipo(TipoDocumento.BOLETIM_OCORRENCIA, "application/pdf");
            verify(validatorService).validarTamanho(validContent.length, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(validatorService).validarConteudo(validContent, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(validatorService).verificarFormatoReal(validContent, "application/pdf");

            verify(storageService).salvar("DOC-001", 1, "SIN-001", validContent);

            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate savedAggregate = aggregateCaptor.getValue();
            assertThat(savedAggregate.getId()).isEqualTo("DOC-001");
            assertThat(savedAggregate.getTipo()).isEqualTo(TipoDocumento.BOLETIM_OCORRENCIA);
        }

        @Test
        @DisplayName("Deve criar documento de foto com formato JPEG")
        void deveCriarDocumentoFotoJPEG() throws Exception {
            // Arrange
            byte[] jpegContent = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
            CriarDocumentoCommand photoCommand = CriarDocumentoCommand.builder()
                    .documentoId("DOC-002")
                    .nome("dano_frontal.jpg")
                    .tipo(TipoDocumento.FOTO_DANOS)
                    .conteudo(jpegContent)
                    .formato("image/jpeg")
                    .sinistroId("SIN-001")
                    .operadorId("OP-001")
                    .operadorNome("João Silva")
                    .build();

            when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarTamanho(anyLong(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(true);
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-001/DOC-002_v1.jpg");

            // Act
            String documentoId = handler.handle(photoCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-002");
            verify(validatorService).validarTipo(TipoDocumento.FOTO_DANOS, "image/jpeg");
            verify(storageService).salvar("DOC-002", 1, "SIN-001", jpegContent);
        }

        @Test
        @DisplayName("Deve criar documento de laudo pericial em PDF")
        void deveCriarLaudoPericial() throws Exception {
            // Arrange
            byte[] pdfContent = "Laudo Pericial Content".getBytes();
            CriarDocumentoCommand laudoCommand = CriarDocumentoCommand.builder()
                    .documentoId("DOC-003")
                    .nome("laudo_tecnico.pdf")
                    .tipo(TipoDocumento.LAUDO_PERICIAL)
                    .conteudo(pdfContent)
                    .formato("application/pdf")
                    .sinistroId("SIN-002")
                    .operadorId("OP-002")
                    .operadorNome("Maria Santos")
                    .build();

            when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarTamanho(anyLong(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(true);
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-002/DOC-003_v1.pdf");

            // Act
            String documentoId = handler.handle(laudoCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-003");
            verify(validatorService).validarTipo(TipoDocumento.LAUDO_PERICIAL, "application/pdf");
        }

        @Test
        @DisplayName("Deve salvar documento no storage com path correto")
        void deveSalvarNoStorageComPathCorreto() throws Exception {
            // Arrange
            setupSuccessScenario();
            String expectedPath = "/storage/sinistros/SIN-001/DOC-001_v1.pdf";
            when(storageService.salvar(any(), anyInt(), any(), any())).thenReturn(expectedPath);

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).salvar("DOC-001", 1, "SIN-001", validContent);

            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate savedAggregate = aggregateCaptor.getValue();
            assertThat(savedAggregate.getDocumento().getConteudoPath()).isEqualTo(expectedPath);
        }
    }

    @Nested
    @DisplayName("Validações de Comando")
    class ValidacoesComando {

        @Test
        @DisplayName("Deve falhar quando command é inválido")
        void deveFalharQuandoCommandInvalido() throws Exception {
            // Arrange
            CriarDocumentoCommand invalidCommand = CriarDocumentoCommand.builder()
                    .documentoId(null) // Inválido
                    .nome("documento.pdf")
                    .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                    .conteudo(validContent)
                    .formato("application/pdf")
                    .sinistroId("SIN-001")
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .satisfiesAnyOf(
                            ex -> assertThat(ex).isInstanceOf(NullPointerException.class)
                                    .hasMessageContaining("Documento ID"),
                            ex -> assertThat(ex).isInstanceOf(IllegalArgumentException.class)
                                    .hasMessageContaining("Documento ID"),
                            ex -> assertThat(ex).isInstanceOf(RuntimeException.class)
                                    .hasMessageContaining("Documento ID")
                    );

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando nome é vazio")
        void deveFalharQuandoNomeVazio() throws Exception {
            // Arrange
            CriarDocumentoCommand invalidCommand = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("   ") // Vazio
                    .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                    .conteudo(validContent)
                    .formato("application/pdf")
                    .sinistroId("SIN-001")
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Nome não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando conteúdo é vazio")
        void deveFalharQuandoConteudoVazio() throws Exception {
            // Arrange
            CriarDocumentoCommand invalidCommand = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("documento.pdf")
                    .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                    .conteudo(new byte[0]) // Vazio
                    .formato("application/pdf")
                    .sinistroId("SIN-001")
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Conteúdo não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando formato é vazio")
        void deveFalharQuandoFormatoVazio() throws Exception {
            // Arrange
            CriarDocumentoCommand invalidCommand = CriarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .nome("documento.pdf")
                    .tipo(TipoDocumento.BOLETIM_OCORRENCIA)
                    .conteudo(validContent)
                    .formato("   ") // Vazio
                    .sinistroId("SIN-001")
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Formato não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validações de Conteúdo")
    class ValidacoesConteudo {

        @Test
        @DisplayName("Deve falhar quando tipo não corresponde ao formato")
        void deveFalharQuandoTipoNaoCorrespondeFormato() throws Exception {
            // Arrange
            when(validatorService.validarTipo(any(), any()))
                    .thenReturn(List.of("Formato não aceito para este tipo de documento"));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validação de documento falhou")
                    .hasMessageContaining("Formato não aceito para este tipo de documento");

            verify(validatorService).validarTipo(TipoDocumento.BOLETIM_OCORRENCIA, "application/pdf");
            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando tamanho excede limite")
        void deveFalharQuandoTamanhoExcedeLimite() throws Exception {
            // Arrange
            when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(List.of("Tamanho excede limite de 10 MB"));
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validação de documento falhou")
                    .hasMessageContaining("Tamanho excede limite");

            verify(validatorService).validarTamanho(validContent.length, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando conteúdo é inválido")
        void deveFalharQuandoConteudoInvalido() throws Exception {
            // Arrange
            when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarTamanho(anyLong(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Magic bytes inválidos para PDF"));
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validação de documento falhou")
                    .hasMessageContaining("Magic bytes inválidos");

            verify(validatorService).validarConteudo(validContent, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando formato real não corresponde ao declarado")
        void deveFalharQuandoFormatoRealNaoCorresponde() throws Exception {
            // Arrange
            when(validatorService.validarTipo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarTamanho(anyLong(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Formato declarado não corresponde ao formato real do arquivo");

            verify(validatorService).verificarFormatoReal(validContent, "application/pdf");
            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve acumular múltiplos erros de validação")
        void deveAcumularMultiplosErrosValidacao() throws Exception {
            // Arrange
            when(validatorService.validarTipo(any(), any()))
                    .thenReturn(List.of("Erro tipo 1"));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(List.of("Erro tamanho 1", "Erro tamanho 2"));
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Erro conteúdo 1"));
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(true);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Erro tipo 1")
                    .hasMessageContaining("Erro tamanho 1")
                    .hasMessageContaining("Erro tamanho 2")
                    .hasMessageContaining("Erro conteúdo 1");

            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar RuntimeException quando storage falha")
        void deveLancarExceptionQuandoStorageFalha() throws Exception {
            // Arrange
            setupSuccessScenario();
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenThrow(new RuntimeException("Falha ao salvar no storage"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao criar documento");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando aggregate falha")
        void deveLancarExceptionQuandoAggregateFalha() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir eventos"))
                    .when(aggregateRepository).save(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao criar documento");

            verify(storageService).listarDocumentosSinistro("SIN-001");
        }

        @Test
        @DisplayName("Deve tentar remover do storage em caso de erro no aggregate")
        void deveTentarRemoverDoStorageEmCasoErro() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir"))
                    .when(aggregateRepository).save(any());

            when(storageService.listarDocumentosSinistro(any()))
                    .thenReturn(new String[]{
                            "/storage/sinistros/SIN-001/DOC-001_v1.pdf",
                            "/storage/sinistros/SIN-001/DOC-002_v1.pdf"
                    });

            // Act
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class);

            // Assert
            verify(storageService).listarDocumentosSinistro("SIN-001");
            verify(storageService).deletar("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService, never()).deletar("/storage/sinistros/SIN-001/DOC-002_v1.pdf");
        }

        @Test
        @DisplayName("Deve continuar mesmo se rollback do storage falhar")
        void deveContinuarMesmoSeRollbackFalhar() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir"))
                    .when(aggregateRepository).save(any());

            when(storageService.listarDocumentosSinistro(any()))
                    .thenThrow(new RuntimeException("Falha ao listar"));

            // Act & Assert - Não deve lançar exceção adicional por falha no rollback
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao criar documento");
        }
    }

    @Nested
    @DisplayName("Interações com Dependências")
    class InteracoesComDependencias {

        @Test
        @DisplayName("Deve validar na ordem correta")
        void deveValidarNaOrdemCorreta() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - verificar ordem de execução
            var inOrder = inOrder(validatorService, storageService, aggregateRepository);

            // 1. Validações
            inOrder.verify(validatorService).validarTipo(any(), any());
            inOrder.verify(validatorService).validarTamanho(anyLong(), any());
            inOrder.verify(validatorService).validarConteudo(any(), any());
            inOrder.verify(validatorService).verificarFormatoReal(any(), any());

            // 2. Storage
            inOrder.verify(storageService).salvar(any(), anyInt(), any(), any());

            // 3. Repository
            inOrder.verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve passar parâmetros corretos para o storage")
        void devePassarParametrosCorretosParaStorage() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).salvar(
                    eq("DOC-001"),
                    eq(1), // Versão inicial
                    eq("SIN-001"),
                    eq(validContent)
            );
        }

        @Test
        @DisplayName("Deve criar aggregate com todos os dados do command")
        void deveCriarAggregateComTodosDados() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate savedAggregate = aggregateCaptor.getValue();
            assertThat(savedAggregate.getId()).isEqualTo("DOC-001");
            assertThat(savedAggregate.getDocumento().getNome()).isEqualTo("boletim_ocorrencia.pdf");
            assertThat(savedAggregate.getTipo()).isEqualTo(TipoDocumento.BOLETIM_OCORRENCIA);
            assertThat(savedAggregate.getDocumento().getFormato()).isEqualTo("application/pdf");
            assertThat(savedAggregate.getDocumento().getSinistroId()).isEqualTo("SIN-001");
            assertThat(savedAggregate.getDocumento().getCriadoPor()).isEqualTo("OP-001");
        }

        @Test
        @DisplayName("Deve executar todas as validações mesmo se algumas passarem")
        void deveExecutarTodasValidacoes() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - todas devem ser chamadas
            verify(validatorService, times(1)).validarTipo(any(), any());
            verify(validatorService, times(1)).validarTamanho(anyLong(), any());
            verify(validatorService, times(1)).validarConteudo(any(), any());
            verify(validatorService, times(1)).verificarFormatoReal(any(), any());
        }
    }
}
