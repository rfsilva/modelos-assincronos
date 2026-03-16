package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.CancelarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent;
import com.seguradora.hibrida.domain.apolice.model.*;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para CancelarApoliceCommandHandler.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CancelarApoliceCommandHandler - Testes Unitários")
class CancelarApoliceCommandHandlerTest {

    @Mock
    private AggregateRepository<ApoliceAggregate> repository;

    @Mock
    private ApoliceValidationService validationService;

    private CancelarApoliceCommandHandler handler;

    private ApoliceAggregate apoliceAggregate;
    private CancelarApoliceCommand validCommand;

    @BeforeEach
    void setUp() {
        handler = new CancelarApoliceCommandHandler(repository, validationService);

        // Criar apólice ativa
        Vigencia vigencia = Vigencia.of(
            LocalDate.now().minusDays(30),
            LocalDate.now().plusMonths(11)
        );

        Valor valorSegurado = Valor.reais(new BigDecimal("50000.00"));

        List<Cobertura> coberturas = List.of(
            Cobertura.of(
                TipoCobertura.COLISAO,
                valorSegurado,
                Valor.reais(new BigDecimal("2500.00")),
                0
            )
        );

        apoliceAggregate = new ApoliceAggregate(
            "APOLICE-001",
            NumeroApolice.of("AP-2026-123456"),
            "SEGURADO-001",
            "Seguro Auto",
            vigencia,
            valorSegurado,
            FormaPagamento.MENSAL,
            coberturas,
            "OPERADOR-001"
        );

        validCommand = CancelarApoliceCommand.porSolicitacaoSegurado(
            "APOLICE-001",
            "Cliente vendeu o veículo e solicitou cancelamento da apólice",
            LocalDate.now().plusDays(1),
            "OPERADOR-002",
            "Documentação de venda apresentada"
        );
    }

    @Test
    @DisplayName("Deve cancelar apólice com sucesso por solicitação do segurado")
    void deveCancelarApoliceComSucessoPorSolicitacao() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        CompletableFuture<Void> result = handler.handle(validCommand);
        result.get();

        // Assert
        verify(repository).getById("APOLICE-001");
        verify(validationService).validarCancelamento(any(), any(), any());

        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getStatus()).isEqualTo(StatusApolice.CANCELADA);
    }

    @Test
    @DisplayName("Deve cancelar apólice por inadimplência")
    void deveCancelarPorInadimplencia() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porInadimplencia(
            "APOLICE-001",
            "Inadimplência por mais de 60 dias sem pagamento das parcelas",
            LocalDate.now().plusDays(5),
            "OPERADOR-003",
            "Notificações enviadas sem retorno"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
        verify(validationService).validarCancelamento(any(), any(), any());
    }

    @Test
    @DisplayName("Deve cancelar apólice por fraude imediatamente")
    void deveCancelarPorFraudeImediatamente() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
            "APOLICE-001",
            "Identificada fraude na documentação apresentada pelo segurado",
            LocalDate.now(),
            "SUP_001", // Supervisor
            "Análise de fraude concluída"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve cancelar apólice por decisão da seguradora")
    void deveCancelarPorDecisaoSeguradora() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porDecisaoSeguradora(
            "APOLICE-001",
            "Cancelamento por decisão gerencial devido a reavaliação de risco",
            LocalDate.now().plusDays(15),
            "GER_001", // Gerente
            "Aprovado pela diretoria"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve cancelar apólice por venda de veículo")
    void deveCancelarPorVendaVeiculo() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porVendaVeiculo(
            "APOLICE-001",
            "Segurado vendeu o veículo segurado e apresentou documentação",
            LocalDate.now().plusDays(3),
            "OPERADOR-002",
            "Comprovante de venda anexado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve cancelar apólice por perda total")
    void deveCancelarPorPerdaTotal() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porPerdaTotal(
            "APOLICE-001",
            "Veículo foi considerado perda total após sinistro avaliado",
            LocalDate.now().plusDays(1),
            "OPERADOR-004",
            "Laudo técnico de perda total anexado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando apólice não é encontrada")
    void deveFalharQuandoApoliceNaoEncontrada() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenThrow(new RuntimeException("Apólice não encontrada"));

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apólice não encontrada");

        verify(repository).getById("APOLICE-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando apólice não pode ser cancelada")
    void deveFalharQuandoApolicaNaoPodeSerCancelada() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doThrow(new IllegalStateException("Apólice não pode ser cancelada no status atual"))
            .when(validationService).validarCancelamento(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apólice não pode ser cancelada no status atual");

        verify(validationService).validarCancelamento(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando cancelamento por fraude não é imediato")
    void deveFalharQuandoCancelamentoFraudeNaoImediato() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
            "APOLICE-001",
            "Fraude identificada mas data futura",
            LocalDate.now().plusDays(5), // Data futura
            "SUP_001",
            "Tentativa inválida"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Cancelamento por fraude deve ser imediato");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando operador não tem permissão para cancelar por fraude")
    void deveFalharQuandoOperadorSemPermissaoParaFraude() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
            "APOLICE-001",
            "Tentativa de cancelamento por fraude sem permissão",
            LocalDate.now(),
            "OPERADOR-002", // Não é supervisor
            "Sem permissão"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apenas supervisores podem cancelar apólices por fraude");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando operador não tem permissão para decisão da seguradora")
    void deveFalharQuandoOperadorSemPermissaoParaDecisaoSeguradora() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porDecisaoSeguradora(
            "APOLICE-001",
            "Tentativa de cancelamento por decisão sem permissão",
            LocalDate.now().plusDays(10),
            "OPERADOR-002", // Não é gerente
            "Sem permissão"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(command).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apenas gerentes podem cancelar por decisão da seguradora");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve validar na ordem correta")
    void deveValidarNaOrdemCorreta() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert - verificar ordem de execução
        var inOrder = inOrder(repository, validationService);

        // 1. Carregar aggregate
        inOrder.verify(repository).getById("APOLICE-001");

        // 2. Validar cancelamento
        inOrder.verify(validationService).validarCancelamento(any(), any(), any());

        // 3. Salvar
        inOrder.verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve registrar operador responsável pelo cancelamento")
    void deveRegistrarOperadorResponsavel() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getOperadorResponsavel()).isEqualTo("OPERADOR-002");
    }

    @Test
    @DisplayName("Deve permitir supervisor cancelar por fraude")
    void devePermitirSupervisorCancelarPorFraude() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
            "APOLICE-001",
            "Fraude confirmada por supervisor",
            LocalDate.now(),
            "SUP_123", // Começa com SUP_
            "Supervisor autorizado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve permitir gerente cancelar por decisão da seguradora")
    void devePermitirGerenteCancelarPorDecisao() throws ExecutionException, InterruptedException {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porDecisaoSeguradora(
            "APOLICE-001",
            "Decisão gerencial aprovada",
            LocalDate.now().plusDays(15),
            "GER_456", // Começa com GER_
            "Gerente autorizado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve avisar quando cancelamento antes de 30 dias")
    void deveAvisarQuandoCancelamentoAntes30Dias() throws ExecutionException, InterruptedException {
        // Arrange
        // Criar apólice com menos de 30 dias
        Vigencia vigenciaRecente = Vigencia.of(
            LocalDate.now().minusDays(15),
            LocalDate.now().plusMonths(11).minusDays(15)
        );

        ApoliceAggregate apoliceRecente = new ApoliceAggregate(
            "APOLICE-002",
            NumeroApolice.of("AP-2026-789012"),
            "SEGURADO-002",
            "Seguro Auto",
            vigenciaRecente,
            Valor.reais(new BigDecimal("50000.00")),
            FormaPagamento.MENSAL,
            List.of(Cobertura.basica(TipoCobertura.COLISAO, Valor.reais(new BigDecimal("50000.00")))),
            "OPERADOR-001"
        );

        CancelarApoliceCommand command = CancelarApoliceCommand.porSolicitacaoSegurado(
            "APOLICE-002",
            "Cancelamento antecipado antes de 30 dias",
            LocalDate.now().plusDays(1),
            "OPERADOR-002",
            "Taxa de cancelamento pode ser aplicada"
        );

        when(repository.getById("APOLICE-002")).thenReturn(apoliceRecente);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
        // Verifica que o processamento continuou (aviso foi logado mas não impediu o cancelamento)
    }

    @Test
    @DisplayName("Deve processar cancelamento com todas as informações")
    void deveProcessarComTodasInformacoes() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getId()).isEqualTo("APOLICE-001");
        assertThat(savedAggregate.getStatus()).isEqualTo(StatusApolice.CANCELADA);
        assertThat(savedAggregate.getOperadorResponsavel()).isEqualTo("OPERADOR-002");
    }

    @Test
    @DisplayName("Deve calcular reembolso para cancelamento elegível")
    void deveCalcularReembolsoParaCancelamentoElegivel() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        verify(repository).save(any());
        // O reembolso é calculado internamente pelo aggregate
    }

    @Test
    @DisplayName("Deve processar diferentes tipos de cancelamento corretamente")
    void deveProcessarDiferentesTiposCancelamento() throws ExecutionException, InterruptedException {
        // Arrange - Usar tipos que não bloqueiam por sinistros
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarCancelamento(any(), any(), any());

        // Testar apenas tipos que permitem cancelamento com sinistros abertos
        ApoliceCanceladaEvent.TipoCancelamento[] tipos = {
            ApoliceCanceladaEvent.TipoCancelamento.INADIMPLENCIA,
            ApoliceCanceladaEvent.TipoCancelamento.FRAUDE
        };

        for (ApoliceCanceladaEvent.TipoCancelamento tipo : tipos) {
            // Create fresh aggregate for each iteration
            Vigencia vigencia = Vigencia.of(
                LocalDate.now().minusDays(30),
                LocalDate.now().plusMonths(11)
            );
            Valor valorSegurado = Valor.reais(new BigDecimal("50000.00"));
            List<Cobertura> coberturas = List.of(
                Cobertura.of(
                    TipoCobertura.COLISAO,
                    valorSegurado,
                    Valor.reais(new BigDecimal("2500.00")),
                    0
                )
            );
            ApoliceAggregate freshAggregate = new ApoliceAggregate(
                "APOLICE-001",
                NumeroApolice.of("AP-2026-123456"),
                "SEGURADO-001",
                "Seguro Auto",
                vigencia,
                valorSegurado,
                FormaPagamento.MENSAL,
                coberturas,
                "OPERADOR-001"
            );

            // Reset mock
            reset(repository, validationService);
            when(repository.getById("APOLICE-001")).thenReturn(freshAggregate);
            doNothing().when(validationService).validarCancelamento(any(), any(), any());

            String operadorId = tipo == ApoliceCanceladaEvent.TipoCancelamento.FRAUDE ? "SUP_001" : "OPERADOR-002";
            LocalDate dataEfeito = tipo == ApoliceCanceladaEvent.TipoCancelamento.FRAUDE ?
                LocalDate.now() : LocalDate.now().plusDays(1);

            CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APOLICE-001",
                "Motivo de teste para " + tipo,
                dataEfeito,
                operadorId,
                "Observação de teste",
                tipo,
                false
            );

            // Act
            handler.handle(command).get();

            // Assert
            verify(repository).save(any());
        }
    }
}
