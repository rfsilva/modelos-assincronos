package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.AtualizarDocumentoCommand;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para AtualizarDocumentoCommandHandler.
 *
 * <p>Testa todos os cenários de atualização de documentos incluindo:
 * <ul>
 *   <li>Atualização com sucesso e versionamento</li>
 *   <li>Backup de versão anterior</li>
 *   <li>Validações de novo conteúdo</li>
 *   <li>Verificação de estado do aggregate</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarDocumentoCommandHandler - Testes Unitários")
class AtualizarDocumentoCommandHandlerTest {

    @Mock
    private AggregateRepository<DocumentoAggregate> aggregateRepository;

    @Mock
    private DocumentoStorageService storageService;

    @Mock
    private DocumentoValidatorService validatorService;

    @InjectMocks
    private AtualizarDocumentoCommandHandler handler;

    private AtualizarDocumentoCommand validCommand;
    private DocumentoAggregate existingAggregate;
    private byte[] newContent;

    @BeforeEach
    void setUp() {
        newContent = "New PDF Content".getBytes();

        validCommand = AtualizarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .novoConteudo(newContent)
                .motivo("Correção de informações")
                .operadorId("OP-001")
                .operadorNome("João Silva")
                .build();

        // Criar aggregate existente
        byte[] originalContent = "Original Content".getBytes();
        existingAggregate = new DocumentoAggregate(
                "DOC-001",
                "boletim_ocorrencia.pdf",
                TipoDocumento.BOLETIM_OCORRENCIA,
                originalContent,
                "application/pdf",
                "/storage/sinistros/SIN-001/DOC-001_v1.pdf",
                "SIN-001",
                "OP-000"
        );
    }

    /**
     * Configura mocks para cenário de sucesso.
     */
    private void setupSuccessScenario() {
        try {
            lenient().when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            lenient().when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(Collections.emptyList());
            lenient().when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());
            lenient().when(validatorService.verificarFormatoReal(any(), any()))
                    .thenReturn(true);
            lenient().when(storageService.exists(any()))
                    .thenReturn(true);
            lenient().when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-001/DOC-001_v2.pdf");
        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar mocks", e);
        }
    }

    @Nested
    @DisplayName("Execução com Sucesso")
    class ExecucaoSucesso {

        @Test
        @DisplayName("Deve atualizar documento com sucesso e gerar nova versão")
        void deveAtualizarDocumentoComSucesso() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            int novaVersao = handler.handle(validCommand);

            // Assert
            assertThat(novaVersao).isEqualTo(2);

            verify(aggregateRepository).findById("DOC-001");
            verify(validatorService).validarTamanho(newContent.length, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(validatorService).validarConteudo(newContent, TipoDocumento.BOLETIM_OCORRENCIA);
            verify(validatorService).verificarFormatoReal(newContent, "application/pdf");

            verify(storageService).backup("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService).salvar("DOC-001", 2, "SIN-001", newContent);

            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate updatedAggregate = aggregateCaptor.getValue();
            assertThat(updatedAggregate.getDocumento().getNumeroVersao()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve fazer backup da versão anterior antes de atualizar")
        void deveFazerBackupVersaoAnterior() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService).backup("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
        }

        @Test
        @DisplayName("Deve continuar se não houver versão anterior para backup")
        void deveContinuarSeNaoHouverVersaoAnterior() throws Exception {
            // Arrange
            setupSuccessScenario();
            when(storageService.exists(any())).thenReturn(false);

            // Act
            int novaVersao = handler.handle(validCommand);

            // Assert
            assertThat(novaVersao).isEqualTo(2);
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService, never()).backup(any());
            verify(storageService).salvar("DOC-001", 2, "SIN-001", newContent);
        }

        @Test
        @DisplayName("Deve salvar nova versão com número incrementado")
        void deveSalvarNovaVersaoComNumeroIncrementado() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).salvar(
                    eq("DOC-001"),
                    eq(2), // Versão incrementada
                    eq("SIN-001"),
                    eq(newContent)
            );
        }

        @Test
        @DisplayName("Deve atualizar aggregate com novo conteúdo e path")
        void deveAtualizarAggregateComNovoConteudoPath() throws Exception {
            // Arrange
            setupSuccessScenario();
            String newPath = "/storage/sinistros/SIN-001/DOC-001_v2.pdf";
            when(storageService.salvar(any(), anyInt(), any(), any())).thenReturn(newPath);

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate updatedAggregate = aggregateCaptor.getValue();
            assertThat(updatedAggregate.getDocumento().getConteudoPath()).isEqualTo(newPath);
            assertThat(updatedAggregate.getDocumento().getAtualizadoPor()).isEqualTo("OP-001");
        }

        @Test
        @DisplayName("Deve permitir múltiplas atualizações sequenciais")
        void devePermitirMultiplasAtualizacoesSequenciais() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Primeira atualização
            handler.handle(validCommand);

            // Preparar para segunda atualização
            byte[] thirdContent = "Third Version Content".getBytes();
            AtualizarDocumentoCommand secondCommand = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(thirdContent)
                    .motivo("Segunda correção")
                    .operadorId("OP-002")
                    .operadorNome("Maria Santos")
                    .build();

            when(storageService.salvar(any(), eq(3), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-001/DOC-001_v3.pdf");

            // Act
            int terceiraVersao = handler.handle(secondCommand);

            // Assert
            assertThat(terceiraVersao).isEqualTo(3);
            verify(storageService).salvar("DOC-001", 3, "SIN-001", thirdContent);
        }
    }

    @Nested
    @DisplayName("Validações de Comando")
    class ValidacoesComando {

        @Test
        @DisplayName("Deve falhar quando command é inválido")
        void deveFalharQuandoCommandInvalido() throws Exception {
            // Arrange
            AtualizarDocumentoCommand invalidCommand = AtualizarDocumentoCommand.builder()
                    .documentoId(null) // Inválido
                    .novoConteudo(newContent)
                    .motivo("Correção")
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
        @DisplayName("Deve falhar quando novo conteúdo é vazio")
        void deveFalharQuandoNovoConteudoVazio() throws Exception {
            // Arrange
            AtualizarDocumentoCommand invalidCommand = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(new byte[0]) // Vazio
                    .motivo("Correção")
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Novo conteúdo não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando motivo é vazio")
        void deveFalharQuandoMotivoVazio() throws Exception {
            // Arrange
            AtualizarDocumentoCommand invalidCommand = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(newContent)
                    .motivo("   ") // Vazio
                    .operadorId("OP-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando documento não existe")
        void deveFalharQuandoDocumentoNaoExiste() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não encontrado: DOC-001");

            verify(aggregateRepository).findById("DOC-001");
            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validações de Negócio")
    class ValidacoesNegocio {

        @Test
        @DisplayName("Deve falhar quando novo tamanho excede limite")
        void deveFalharQuandoNovoTamanhoExcedeLimite() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(List.of("Tamanho excede limite de 10 MB"));
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validação do novo conteúdo falhou")
                    .hasMessageContaining("Tamanho excede limite");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando novo conteúdo é inválido")
        void deveFalharQuandoNovoConteudoInvalido() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Magic bytes inválidos"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validação do novo conteúdo falhou")
                    .hasMessageContaining("Magic bytes inválidos");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando formato real não corresponde ao original")
        void deveFalharQuandoFormatoRealNaoCorresponde() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.verificarFormatoReal(any(), any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Formato do novo arquivo não corresponde ao formato original");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve acumular múltiplos erros de validação do novo conteúdo")
        void deveAcumularMultiplosErrosValidacao() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(List.of("Erro tamanho 1"));
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Erro conteúdo 1", "Erro conteúdo 2"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Erro tamanho 1")
                    .hasMessageContaining("Erro conteúdo 1")
                    .hasMessageContaining("Erro conteúdo 2");

            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar RuntimeException quando backup falha")
        void deveLancarExceptionQuandoBackupFalha() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao fazer backup"))
                    .when(storageService).backup(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao atualizar documento");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando storage falha ao salvar nova versão")
        void deveLancarExceptionQuandoStorageFalhaSalvar() throws Exception {
            // Arrange
            setupSuccessScenario();
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenThrow(new RuntimeException("Falha ao salvar nova versão"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao atualizar documento");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar RuntimeException quando aggregate falha ao persistir")
        void deveLancarExceptionQuandoAggregateFalhaPersistir() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir eventos"))
                    .when(aggregateRepository).save(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao atualizar documento");
        }

        @Test
        @DisplayName("Deve preservar mensagem de erro original")
        void devePreservarMensagemErroOriginal() throws Exception {
            // Arrange
            setupSuccessScenario();
            String mensagemOriginal = "Erro específico do storage";
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenThrow(new RuntimeException(mensagemOriginal));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao atualizar documento")
                    .hasCauseInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("Interações com Dependências")
    class InteracoesComDependencias {

        @Test
        @DisplayName("Deve executar operações na ordem correta")
        void deveExecutarOperacoesNaOrdemCorreta() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - verificar ordem de execução
            var inOrder = inOrder(aggregateRepository, validatorService, storageService);

            // 1. Carregar aggregate
            inOrder.verify(aggregateRepository).findById("DOC-001");

            // 2. Validações
            inOrder.verify(validatorService).validarTamanho(anyLong(), any());
            inOrder.verify(validatorService).validarConteudo(any(), any());
            inOrder.verify(validatorService).verificarFormatoReal(any(), any());

            // 3. Backup
            inOrder.verify(storageService).exists(any());
            inOrder.verify(storageService).backup(any());

            // 4. Salvar nova versão
            inOrder.verify(storageService).salvar(any(), anyInt(), any(), any());

            // 5. Persistir aggregate
            inOrder.verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve usar sinistroId do aggregate existente")
        void deveUsarSinistroIdDoAggregateExistente() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).salvar(
                    eq("DOC-001"),
                    eq(2),
                    eq("SIN-001"), // Do aggregate existente
                    eq(newContent)
            );
        }

        @Test
        @DisplayName("Deve validar com tipo do aggregate existente")
        void deveValidarComTipoDoAggregateExistente() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(validatorService).validarTamanho(
                    newContent.length,
                    TipoDocumento.BOLETIM_OCORRENCIA // Do aggregate existente
            );
            verify(validatorService).validarConteudo(
                    newContent,
                    TipoDocumento.BOLETIM_OCORRENCIA
            );
        }

        @Test
        @DisplayName("Deve validar com formato do aggregate existente")
        void deveValidarComFormatoDoAggregateExistente() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(validatorService).verificarFormatoReal(
                    newContent,
                    "application/pdf" // Do aggregate existente
            );
        }

        @Test
        @DisplayName("Deve chamar backup apenas se path existe")
        void deveChamarBackupApenasSePathExiste() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Primeiro cenário: path existe
            when(storageService.exists(any())).thenReturn(true);

            // Criar comando com conteúdo DIFERENTE do original para não gerar erro "Conteúdo não foi modificado"
            byte[] newDifferentContent = "Completely Different Content Here".getBytes();
            AtualizarDocumentoCommand commandWithDifferentContent = AtualizarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .novoConteudo(newDifferentContent)
                    .motivo("Correção de informações")
                    .operadorId("OP-001")
                    .operadorNome("João Silva")
                    .build();

            // Act
            handler.handle(commandWithDifferentContent);

            // Assert
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService).backup("/storage/sinistros/SIN-001/DOC-001_v1.pdf");

            // Segundo cenário: path não existe - recriar aggregate
            reset(storageService);
            reset(aggregateRepository);
            reset(validatorService);

            // Recriar aggregate para segundo teste
            byte[] originalContent2 = "Second Original Content".getBytes();
            DocumentoAggregate existingAggregate2 = new DocumentoAggregate(
                    "DOC-001",
                    "boletim_ocorrencia.pdf",
                    TipoDocumento.BOLETIM_OCORRENCIA,
                    originalContent2,
                    "application/pdf",
                    "/storage/sinistros/SIN-001/DOC-001_v1.pdf",
                    "SIN-001",
                    "OP-000"
            );

            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate2));
            when(validatorService.validarTamanho(anyLong(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(Collections.emptyList());
            when(validatorService.verificarFormatoReal(any(), any()))
                    .thenReturn(true);
            when(storageService.exists(any())).thenReturn(false);
            when(storageService.salvar(any(), anyInt(), any(), any()))
                    .thenReturn("/storage/sinistros/SIN-001/DOC-001_v2.pdf");

            // Act
            handler.handle(commandWithDifferentContent);

            // Assert
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService, never()).backup(any());
        }
    }
}
