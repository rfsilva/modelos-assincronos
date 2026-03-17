package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.ValidarDocumentoCommand;
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
 * Testes unitários para ValidarDocumentoCommandHandler.
 *
 * <p>Testa todos os cenários de validação de documentos incluindo:
 * <ul>
 *   <li>Validação com sucesso</li>
 *   <li>Verificação de integridade e hash</li>
 *   <li>Validações de estado do documento</li>
 *   <li>Verificação de permissões</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ValidarDocumentoCommandHandler - Testes Unitários")
class ValidarDocumentoCommandHandlerTest {

    @Mock
    private AggregateRepository<DocumentoAggregate> aggregateRepository;

    @Mock
    private DocumentoStorageService storageService;

    @Mock
    private DocumentoValidatorService validatorService;

    @InjectMocks
    private ValidarDocumentoCommandHandler handler;

    private ValidarDocumentoCommand validCommand;
    private DocumentoAggregate existingAggregate;
    private byte[] documentContent;

    @BeforeEach
    void setUp() {
        validCommand = ValidarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .validadorId("VAL-001")
                .validadorNome("Dr. Carlos Pereira")
                .observacoes("Documento conforme e completo")
                .build();

        documentContent = "PDF Content".getBytes();

        // Criar aggregate existente no estado PENDENTE - usar ORCAMENTO que não requer assinatura
        existingAggregate = new DocumentoAggregate(
                "DOC-001",
                "orcamento_reparo.pdf",
                TipoDocumento.ORCAMENTO,
                documentContent,
                "application/pdf",
                "/storage/sinistros/SIN-001/DOC-001_v1.pdf",
                "SIN-001",
                "OP-001"
        );
    }

    /**
     * Configura mocks para cenário de sucesso.
     */
    private void setupSuccessScenario() {
        try {
            lenient().when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            lenient().when(storageService.exists(any())).thenReturn(true);
            lenient().when(storageService.recuperar(any())).thenReturn(documentContent);
            lenient().when(validatorService.validarHash(any(byte[].class), any()))
                    .thenReturn(new java.util.ArrayList<>());
            lenient().when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(new java.util.ArrayList<>());
        } catch (Exception e) {
            throw new RuntimeException("Erro ao configurar mocks", e);
        }
    }

    @Nested
    @DisplayName("Execução com Sucesso")
    class ExecucaoSucesso {

        @Test
        @DisplayName("Deve validar documento com sucesso")
        void deveValidarDocumentoComSucesso() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            String documentoId = handler.handle(validCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");

            verify(aggregateRepository).findById("DOC-001");
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService).recuperar("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(validatorService).validarHash(eq(documentContent), any());
            verify(validatorService).validarConteudo(documentContent, TipoDocumento.ORCAMENTO);

            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate validatedAggregate = aggregateCaptor.getValue();
            assertThat(validatedAggregate.getDocumento().getAtualizadoPor()).isEqualTo("VAL-001");
        }

        @Test
        @DisplayName("Deve validar documento sem observações")
        void deveValidarDocumentoSemObservacoes() throws Exception {
            // Arrange
            setupSuccessScenario();
            ValidarDocumentoCommand commandSemObservacoes = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos Pereira")
                    .observacoes(null) // Sem observações
                    .build();

            // Act
            String documentoId = handler.handle(commandSemObservacoes);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve validar documento com observações detalhadas")
        void deveValidarDocumentoComObservacoesDetalhadas() throws Exception {
            // Arrange
            setupSuccessScenario();
            ValidarDocumentoCommand commandComObservacoes = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos Pereira")
                    .observacoes("Documento verificado. Todas as assinaturas conferidas. Informações completas.")
                    .build();

            // Act
            String documentoId = handler.handle(commandComObservacoes);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve verificar integridade antes de validar")
        void deveVerificarIntegridadeAntesValidar() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(storageService).recuperar("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(validatorService).validarHash(eq(documentContent), any());
        }

        @Test
        @DisplayName("Deve validar conteúdo do storage")
        void deveValidarConteudoDoStorage() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(validatorService).validarConteudo(
                    documentContent,
                    TipoDocumento.ORCAMENTO
            );
        }
    }

    @Nested
    @DisplayName("Validações de Comando")
    class ValidacoesComando {

        @Test
        @DisplayName("Deve falhar quando command é inválido")
        void deveFalharQuandoCommandInvalido() throws Exception {
            // Arrange
            ValidarDocumentoCommand invalidCommand = ValidarDocumentoCommand.builder()
                    .documentoId(null) // Inválido
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Documento ID");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando documento ID é vazio")
        void deveFalharQuandoDocumentoIdVazio() throws Exception {
            // Arrange
            ValidarDocumentoCommand invalidCommand = ValidarDocumentoCommand.builder()
                    .documentoId("   ") // Vazio
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Documento ID não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando validador ID é vazio")
        void deveFalharQuandoValidadorIdVazio() throws Exception {
            // Arrange
            ValidarDocumentoCommand invalidCommand = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .validadorId("   ") // Vazio
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validador ID não pode ser vazio");

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
    @DisplayName("Validações de Estado")
    class ValidacoesEstado {

        @Test
        @DisplayName("Deve falhar quando arquivo não existe no storage")
        void deveFalharQuandoArquivoNaoExisteNoStorage() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(false);

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Arquivo do documento não encontrado no storage");

            verify(storageService).exists("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando path é nulo")
        void deveFalharQuandoPathNulo() throws Exception {
            // Arrange
            // Criar aggregate com path nulo
            DocumentoAggregate aggregateComPathNulo = new DocumentoAggregate(
                    "DOC-002",
                    "documento.pdf",
                    TipoDocumento.ORCAMENTO,
                    documentContent,
                    "application/pdf",
                    null, // Path nulo
                    "SIN-001",
                    "OP-001"
            );

            when(aggregateRepository.findById("DOC-002"))
                    .thenReturn(Optional.of(aggregateComPathNulo));

            ValidarDocumentoCommand command = ValidarDocumentoCommand.builder()
                    .documentoId("DOC-002")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(command))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Arquivo do documento não encontrado no storage");

            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Validações de Integridade")
    class ValidacoesIntegridade {

        @Test
        @DisplayName("Deve falhar quando hash não corresponde")
        void deveFalharQuandoHashNaoCorresponde() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(true);
            when(storageService.recuperar(any())).thenReturn(documentContent);
            when(validatorService.validarHash(any(), any()))
                    .thenReturn(List.of("Hash não corresponde ao original"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento possui problemas que impedem validação")
                    .hasMessageContaining("Hash não corresponde ao original");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando conteúdo está corrompido")
        void deveFalharQuandoConteudoCorrompido() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(true);
            when(storageService.recuperar(any())).thenReturn(documentContent);
            when(validatorService.validarHash(any(), any())).thenReturn(Collections.emptyList());
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Arquivo corrompido ou ilegível"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento possui problemas que impedem validação")
                    .hasMessageContaining("Arquivo corrompido ou ilegível");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve acumular múltiplos erros de integridade")
        void deveAcumularMultiplosErrosIntegridade() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(true);
            when(storageService.recuperar(any())).thenReturn(documentContent);
            when(validatorService.validarHash(any(), any()))
                    .thenReturn(List.of("Erro hash 1", "Erro hash 2"));
            when(validatorService.validarConteudo(any(), any()))
                    .thenReturn(List.of("Erro conteúdo 1"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Erro hash 1")
                    .hasMessageContaining("Erro hash 2")
                    .hasMessageContaining("Erro conteúdo 1");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve validar documento do aggregate junto com storage")
        void deveValidarDocumentoAggregateJuntoComStorage() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - Verifica que validar() do documento também é chamado via aggregate
            verify(validatorService).validarHash(any(byte[].class), any());
            verify(validatorService).validarConteudo(any(), any());
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar exception quando storage falha ao recuperar")
        void deveLancarExceptionQuandoStorageFalhaRecuperar() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(true);
            when(storageService.recuperar(any()))
                    .thenThrow(new RuntimeException("Falha ao ler arquivo"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Erro ao verificar integridade");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar exception quando aggregate falha ao salvar")
        void deveLancarExceptionQuandoAggregateFalhaSalvar() throws Exception {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir eventos"))
                    .when(aggregateRepository).save(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao validar documento");
        }

        @Test
        @DisplayName("Deve preservar exceções de validação")
        void devePreservarExcecoesValidacao() throws Exception {
            // Arrange
            when(aggregateRepository.findById("DOC-001"))
                    .thenReturn(Optional.of(existingAggregate));
            when(storageService.exists(any())).thenReturn(true);
            when(storageService.recuperar(any())).thenReturn(documentContent);
            when(validatorService.validarHash(any(), any()))
                    .thenThrow(new IllegalStateException("Algoritmo de hash inválido"));

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Erro ao verificar integridade");

            verify(aggregateRepository, never()).save(any());
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
            var inOrder = inOrder(aggregateRepository, storageService, validatorService);

            // 1. Carregar aggregate
            inOrder.verify(aggregateRepository).findById("DOC-001");

            // 2. Verificar se arquivo existe
            inOrder.verify(storageService).exists(any());

            // 3. Recuperar conteúdo
            inOrder.verify(storageService).recuperar(any());

            // 4. Validar hash
            inOrder.verify(validatorService).validarHash(any(), any());

            // 5. Validar conteúdo
            inOrder.verify(validatorService).validarConteudo(any(), any());

            // 6. Salvar aggregate
            inOrder.verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve usar path do aggregate para recuperar conteúdo")
        void deveUsarPathDoAggregateParaRecuperarConteudo() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService).recuperar("/storage/sinistros/SIN-001/DOC-001_v1.pdf");
        }

        @Test
        @DisplayName("Deve validar hash com hash do aggregate")
        void deveValidarHashComHashDoAggregate() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(validatorService).validarHash(eq(documentContent), any());
        }

        @Test
        @DisplayName("Deve validar conteúdo com tipo do aggregate")
        void deveValidarConteudoComTipoDoAggregate() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(validatorService).validarConteudo(
                    documentContent,
                    TipoDocumento.ORCAMENTO
            );
        }

        @Test
        @DisplayName("Deve passar informações do validador para o aggregate")
        void devePassarInformacoesValidadorParaAggregate() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            // Verificar através dos metadados
            DocumentoAggregate savedAggregate = aggregateCaptor.getValue();
            assertThat(savedAggregate.getDocumento().getMetadata("validadorNome"))
                    .isEqualTo("Dr. Carlos Pereira");
        }

        @Test
        @DisplayName("Deve recuperar conteúdo do storage apenas uma vez")
        void deveRecuperarConteudoApenaUmaVez() throws Exception {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(storageService, times(1)).recuperar(any());
        }
    }
}
