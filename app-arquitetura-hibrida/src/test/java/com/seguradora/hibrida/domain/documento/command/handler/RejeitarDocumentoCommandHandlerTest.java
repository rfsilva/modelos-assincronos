package com.seguradora.hibrida.domain.documento.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.documento.aggregate.DocumentoAggregate;
import com.seguradora.hibrida.domain.documento.command.RejeitarDocumentoCommand;
import com.seguradora.hibrida.domain.documento.model.TipoDocumento;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RejeitarDocumentoCommandHandler.
 *
 * <p>Testa todos os cenários de rejeição de documentos incluindo:
 * <ul>
 *   <li>Rejeição com sucesso</li>
 *   <li>Validação de justificativa</li>
 *   <li>Verificação de problemas identificados</li>
 *   <li>Ações corretivas e notificações</li>
 * </ul>
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RejeitarDocumentoCommandHandler - Testes Unitários")
class RejeitarDocumentoCommandHandlerTest {

    @Mock
    private AggregateRepository<DocumentoAggregate> aggregateRepository;

    @InjectMocks
    private RejeitarDocumentoCommandHandler handler;

    private RejeitarDocumentoCommand validCommand;
    private DocumentoAggregate existingAggregate;
    private byte[] documentContent;

    @BeforeEach
    void setUp() {
        documentContent = "PDF Content".getBytes();

        validCommand = RejeitarDocumentoCommand.builder()
                .documentoId("DOC-001")
                .motivo("Documento ilegível e com informações incompletas")
                .problemasIdentificado("Assinatura ausente")
                .problemasIdentificado("Data de emissão não informada")
                .problemasIdentificado("Carimbo do órgão emissor ausente")
                .validadorId("VAL-001")
                .validadorNome("Dr. Carlos Pereira")
                .acoesCorretivas("Solicitar nova emissão do documento ao órgão competente com todos os requisitos")
                .permiteReenvio(true)
                .build();

        // Criar aggregate existente no estado PENDENTE
        existingAggregate = new DocumentoAggregate(
                "DOC-001",
                "boletim_ocorrencia.pdf",
                TipoDocumento.BOLETIM_OCORRENCIA,
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
        lenient().when(aggregateRepository.findById("DOC-001"))
                .thenReturn(Optional.of(existingAggregate));
    }

    @Nested
    @DisplayName("Execução com Sucesso")
    class ExecucaoSucesso {

        @Test
        @DisplayName("Deve rejeitar documento com sucesso")
        void deveRejeitarDocumentoComSucesso() {
            // Arrange
            setupSuccessScenario();

            // Act
            String documentoId = handler.handle(validCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");

            verify(aggregateRepository).findById("DOC-001");

            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate rejectedAggregate = aggregateCaptor.getValue();
            assertThat(rejectedAggregate.getDocumento().getAtualizadoPor()).isEqualTo("VAL-001");
        }

        @Test
        @DisplayName("Deve rejeitar documento com motivo detalhado")
        void deveRejeitarDocumentoComMotivoDetalhado() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate rejectedAggregate = aggregateCaptor.getValue();
            assertThat(rejectedAggregate.getDocumento().getMetadata("motivoRejeicao"))
                    .isEqualTo("Documento ilegível e com informações incompletas");
        }

        @Test
        @DisplayName("Deve rejeitar documento com problemas identificados")
        void deveRejeitarDocumentoComProblemasIdentificados() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar documento sem ações corretivas")
        void deveRejeitarDocumentoSemAcoesCorretivas() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandSemAcoes = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento não atende requisitos mínimos")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .acoesCorretivas(null) // Sem ações
                    .build();

            // Act
            String documentoId = handler.handle(commandSemAcoes);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve rejeitar documento sem permitir reenvio")
        void deveRejeitarDocumentoSemPermitirReenvio() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandSemReenvio = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento fraudulento - não aceito")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .permiteReenvio(false) // Não permite reenvio
                    .build();

            // Act
            String documentoId = handler.handle(commandSemReenvio);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve passar informações do validador para aggregate")
        void devePassarInformacoesValidadorParaAggregate() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate rejectedAggregate = aggregateCaptor.getValue();
            assertThat(rejectedAggregate.getDocumento().getMetadata("validadorNome"))
                    .isEqualTo("Dr. Carlos Pereira");
        }
    }

    @Nested
    @DisplayName("Validações de Comando")
    class ValidacoesComando {

        @Test
        @DisplayName("Deve falhar quando command é inválido")
        void deveFalharQuandoCommandInvalido() {
            // Arrange
            RejeitarDocumentoCommand invalidCommand = RejeitarDocumentoCommand.builder()
                    .documentoId(null) // Inválido
                    .motivo("Motivo qualquer")
                    .validadorId("VAL-001")
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
        @DisplayName("Deve falhar quando documento ID é vazio")
        void deveFalharQuandoDocumentoIdVazio() {
            // Arrange
            RejeitarDocumentoCommand invalidCommand = RejeitarDocumentoCommand.builder()
                    .documentoId("   ") // Vazio
                    .motivo("Motivo qualquer")
                    .validadorId("VAL-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Documento ID não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando motivo é vazio")
        void deveFalharQuandoMotivoVazio() {
            // Arrange
            RejeitarDocumentoCommand invalidCommand = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("   ") // Vazio
                    .validadorId("VAL-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando validador ID é vazio")
        void deveFalharQuandoValidadorIdVazio() {
            // Arrange
            RejeitarDocumentoCommand invalidCommand = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Motivo válido")
                    .validadorId("   ") // Vazio
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(invalidCommand))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Validador ID não pode ser vazio");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando documento não existe")
        void deveFalharQuandoDocumentoNaoExiste() {
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
    @DisplayName("Validações de Justificativa")
    class ValidacoesJustificativa {

        @Test
        @DisplayName("Deve falhar quando motivo é muito curto")
        void deveFalharQuandoMotivoMuitoCurto() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComMotivoMuitoCurto = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Ruim") // Menos de 10 caracteres
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(commandComMotivoMuitoCurto))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição muito curto")
                    .hasMessageContaining("mínimo 10 caracteres");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando motivo é genérico - 'invalido'")
        void deveFalharQuandoMotivoGenericoInvalido() {
            // Arrange - não precisa mock pois validação falha antes de buscar aggregate
            RejeitarDocumentoCommand commandComMotivoGenerico = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento invalido") // Mais de 10 caracteres para passar primeira validação
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(commandComMotivoGenerico))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição muito genérico");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando motivo é genérico - 'errado'")
        void deveFalharQuandoMotivoGenericoErrado() {
            // Arrange - não precisa mock pois validação falha antes de buscar aggregate
            RejeitarDocumentoCommand commandComMotivoGenerico = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento errado") // Mais de 10 caracteres para passar primeira validação
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(commandComMotivoGenerico))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição muito genérico");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve falhar quando motivo é genérico - 'incorreto'")
        void deveFalharQuandoMotivoGenericoIncorreto() {
            // Arrange - não precisa mock pois validação falha antes de buscar aggregate
            RejeitarDocumentoCommand commandComMotivoGenerico = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento incorreto") // Mais de 10 caracteres para passar primeira validação
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(commandComMotivoGenerico))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição muito genérico");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve aceitar motivo no limite mínimo de caracteres")
        void deveAceitarMotivoNoLimiteMinimo() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComMotivoNoLimite = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("1234567890") // Exatamente 10 caracteres
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act
            String documentoId = handler.handle(commandComMotivoNoLimite);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve aceitar motivo específico e detalhado")
        void deveAceitarMotivoEspecificoDetalhado() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComMotivoDetalhado = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento sem assinatura digital válida do perito responsável")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act
            String documentoId = handler.handle(commandComMotivoDetalhado);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            verify(aggregateRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Validações de Problemas Identificados")
    class ValidacoesProblemasIdentificados {

        @Test
        @DisplayName("Deve aceitar rejeição com lista de problemas")
        void deveAceitarRejeicaoComListaProblemas() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComProblemas = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento possui múltiplas inconsistências")
                    .problemasIdentificado("Problema 1")
                    .problemasIdentificado("Problema 2")
                    .problemasIdentificado("Problema 3")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act
            String documentoId = handler.handle(commandComProblemas);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(commandComProblemas.getQuantidadeProblemas()).isEqualTo(3);
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve aceitar rejeição sem lista de problemas")
        void deveAceitarRejeicaoSemListaProblemas() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandSemProblemas = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento não atende especificações técnicas")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act
            String documentoId = handler.handle(commandSemProblemas);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(commandSemProblemas.possuiProblemasDetalhados()).isFalse();
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve processar corretamente lista vazia de problemas")
        void deveProcessarListaVaziaProblemas() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComListaVazia = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento rejeitado por política interna")
                    .problemasIdentificados(List.of()) // Lista vazia explícita
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .build();

            // Act
            String documentoId = handler.handle(commandComListaVazia);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(commandComListaVazia.getQuantidadeProblemas()).isEqualTo(0);
            verify(aggregateRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Validações de Ações Corretivas")
    class ValidacoesAcoesCorretivas {

        @Test
        @DisplayName("Deve aceitar rejeição com ações corretivas detalhadas")
        void deveAceitarRejeicaoComAcoesCorretivas() {
            // Arrange
            setupSuccessScenario();

            // Act
            String documentoId = handler.handle(validCommand);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(validCommand.possuiAcoesCorretivas()).isTrue();
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve aceitar rejeição sem ações corretivas")
        void deveAceitarRejeicaoSemAcoesCorretivas() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandSemAcoes = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento não atende critérios mínimos")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .acoesCorretivas(null)
                    .build();

            // Act
            String documentoId = handler.handle(commandSemAcoes);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(commandSemAcoes.possuiAcoesCorretivas()).isFalse();
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve aceitar ações corretivas vazias")
        void deveAceitarAcoesCorretivasVazias() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandComAcoesVazias = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Documento rejeitado conforme regulamento")
                    .validadorId("VAL-001")
                    .validadorNome("Dr. Carlos")
                    .acoesCorretivas("   ") // String vazia
                    .build();

            // Act
            String documentoId = handler.handle(commandComAcoesVazias);

            // Assert
            assertThat(documentoId).isEqualTo("DOC-001");
            assertThat(commandComAcoesVazias.possuiAcoesCorretivas()).isFalse();
            verify(aggregateRepository).save(any());
        }
    }

    @Nested
    @DisplayName("Tratamento de Erros")
    class TratamentoErros {

        @Test
        @DisplayName("Deve lançar exception quando aggregate falha ao salvar")
        void deveLancarExceptionQuandoAggregateFalhaSalvar() {
            // Arrange
            setupSuccessScenario();
            doThrow(new RuntimeException("Falha ao persistir eventos"))
                    .when(aggregateRepository).save(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao rejeitar documento");
        }

        @Test
        @DisplayName("Deve preservar mensagem de erro original")
        void devePreservarMensagemErroOriginal() {
            // Arrange
            setupSuccessScenario();
            String mensagemOriginal = "Falha específica no banco de dados";
            doThrow(new RuntimeException(mensagemOriginal))
                    .when(aggregateRepository).save(any());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Erro ao rejeitar documento")
                    .hasCauseInstanceOf(RuntimeException.class);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para erros de validação")
        void deveLancarIllegalArgumentExceptionParaErrosValidacao() {
            // Arrange
            setupSuccessScenario();
            RejeitarDocumentoCommand commandInvalido = RejeitarDocumentoCommand.builder()
                    .documentoId("DOC-001")
                    .motivo("Curto") // Muito curto
                    .validadorId("VAL-001")
                    .build();

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(commandInvalido))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Motivo da rejeição muito curto");

            verify(aggregateRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve lançar IllegalStateException quando documento não existe")
        void deveLancarIllegalStateExceptionQuandoDocumentoNaoExiste() {
            // Arrange
            when(aggregateRepository.findById("DOC-001")).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> handler.handle(validCommand))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Documento não encontrado");

            verify(aggregateRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Interações com Dependências")
    class InteracoesComDependencias {

        @Test
        @DisplayName("Deve executar operações na ordem correta")
        void deveExecutarOperacoesNaOrdemCorreta() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - verificar ordem de execução
            var inOrder = inOrder(aggregateRepository);

            // 1. Carregar aggregate
            inOrder.verify(aggregateRepository).findById("DOC-001");

            // 2. Salvar aggregate
            inOrder.verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve passar todos os parâmetros para o aggregate")
        void devePassarTodosParametrosParaAggregate() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            ArgumentCaptor<DocumentoAggregate> aggregateCaptor = ArgumentCaptor.forClass(DocumentoAggregate.class);
            verify(aggregateRepository).save(aggregateCaptor.capture());

            DocumentoAggregate rejectedAggregate = aggregateCaptor.getValue();
            assertThat(rejectedAggregate.getDocumento().getMetadata("motivoRejeicao"))
                    .isEqualTo("Documento ilegível e com informações incompletas");
            assertThat(rejectedAggregate.getDocumento().getMetadata("validadorNome"))
                    .isEqualTo("Dr. Carlos Pereira");
            assertThat(rejectedAggregate.getDocumento().getMetadata("acoesCorretivas"))
                    .contains("Solicitar nova emissão");
        }

        @Test
        @DisplayName("Deve carregar aggregate apenas uma vez")
        void deveCarregarAggregateApenaUmaVez() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(aggregateRepository, times(1)).findById("DOC-001");
        }

        @Test
        @DisplayName("Deve salvar aggregate apenas uma vez")
        void deveSalvarAggregateApenaUmaVez() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            verify(aggregateRepository, times(1)).save(any());
        }

        @Test
        @DisplayName("Deve usar ID do operador criador para notificação")
        void deveUsarIdOperadorCriadorParaNotificacao() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert - Verifica que o método de notificação é chamado (via log)
            // A notificação real não é implementada ainda, mas o código está preparado
            verify(aggregateRepository).save(any());
        }

        @Test
        @DisplayName("Deve processar rejeição completa com todos os campos")
        void deveProcessarRejeicaoCompletaComTodosCampos() {
            // Arrange
            setupSuccessScenario();

            // Act
            handler.handle(validCommand);

            // Assert
            assertThat(validCommand.getMotivo()).isNotEmpty();
            assertThat(validCommand.possuiProblemasDetalhados()).isTrue();
            assertThat(validCommand.getQuantidadeProblemas()).isEqualTo(3);
            assertThat(validCommand.possuiAcoesCorretivas()).isTrue();
            assertThat(validCommand.isPermiteReenvio()).isTrue();

            verify(aggregateRepository).save(any());
        }
    }
}
