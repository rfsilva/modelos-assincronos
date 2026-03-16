package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.RenovarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.event.ApoliceRenovadaEvent;
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
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para RenovarApoliceCommandHandler.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RenovarApoliceCommandHandler - Testes Unitários")
class RenovarApoliceCommandHandlerTest {

    @Mock
    private AggregateRepository<ApoliceAggregate> repository;

    @Mock
    private ApoliceValidationService apoliceValidationService;

    @Mock
    private ApoliceValidationService seguradoValidationService;

    private RenovarApoliceCommandHandler handler;

    private ApoliceAggregate apoliceAggregate;
    private RenovarApoliceCommand validCommand;
    private Vigencia novaVigencia;
    private Valor novoValorSegurado;
    private List<Cobertura> novasCoberturas;

    @BeforeEach
    void setUp() {
        handler = new RenovarApoliceCommandHandler(
            repository,
            apoliceValidationService,
            seguradoValidationService
        );

        // Criar apólice próxima ao vencimento
        LocalDate hoje = LocalDate.now();
        LocalDate fimVigenciaAtual = hoje.plusDays(30);

        Vigencia vigenciaAtual = Vigencia.of(
            hoje.minusDays(20),
            fimVigenciaAtual
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
            vigenciaAtual,
            valorSegurado,
            FormaPagamento.MENSAL,
            coberturas,
            "OPERADOR-001"
        );

        // Dados da renovação - deve começar APÓS o fim da vigência atual
        novaVigencia = Vigencia.of(
            fimVigenciaAtual.plusDays(1),
            fimVigenciaAtual.plusYears(1)
        );

        novoValorSegurado = Valor.reais(new BigDecimal("55000.00"));

        novasCoberturas = List.of(
            Cobertura.of(
                TipoCobertura.COLISAO,
                novoValorSegurado,
                Valor.reais(new BigDecimal("2750.00")),
                0
            ),
            Cobertura.of(
                TipoCobertura.ROUBO_FURTO,
                novoValorSegurado,
                Valor.reais(new BigDecimal("2750.00")),
                30
            )
        );

        validCommand = RenovarApoliceCommand.manual(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            novasCoberturas,
            FormaPagamento.MENSAL,
            "OPERADOR-002",
            "Renovação com aumento de valor e inclusão de cobertura adicional"
        );
    }

    @Test
    @DisplayName("Deve renovar apólice manualmente com sucesso")
    void deveRenovarApoliceManualmenteComSucesso() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        CompletableFuture<Void> result = handler.handle(validCommand);
        result.get();

        // Assert
        verify(repository).getById("APOLICE-001");
        verify(apoliceValidationService).validarRenovacao(any(), any(), any());
        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(seguradoValidationService).obterScoreCredito("SEGURADO-001");

        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getId()).isEqualTo("APOLICE-001");
    }

    @Test
    @DisplayName("Deve renovar apólice automaticamente com desconto")
    void deveRenovarAutomaticamenteComDesconto() throws ExecutionException, InterruptedException {
        // Arrange
        RenovarApoliceCommand commandAutomatico = RenovarApoliceCommand.automatica(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            novasCoberturas,
            FormaPagamento.MENSAL,
            "SISTEMA-AUTO"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(700);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(0));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(commandAutomatico).get();

        // Assert
        verify(repository).save(any());
        assertThat(commandAutomatico.isAplicarDesconto()).isTrue();
        assertThat(commandAutomatico.getPercentualDesconto()).isEqualTo(5.0);
    }

    @Test
    @DisplayName("Deve renovar apólice antecipadamente")
    void deveRenovarAntecipadamente() throws ExecutionException, InterruptedException {
        // Arrange
        RenovarApoliceCommand commandAntecipado = RenovarApoliceCommand.antecipada(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            novasCoberturas,
            FormaPagamento.ANUAL,
            "OPERADOR-003",
            "Renovação antecipada com desconto especial",
            10.0 // 10% de desconto
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(680);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(commandAntecipado).get();

        // Assert
        verify(repository).save(any());
        assertThat(commandAntecipado.getPercentualDesconto()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Deve renovar apólice com alterações")
    void deveRenovarComAlteracoes() throws ExecutionException, InterruptedException {
        // Arrange
        RenovarApoliceCommand commandComAlteracoes = RenovarApoliceCommand.comAlteracoes(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            novasCoberturas,
            FormaPagamento.TRIMESTRAL,
            "OPERADOR-004",
            "Renovação com mudança de forma de pagamento e coberturas"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(620);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(2));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(commandComAlteracoes).get();

        // Assert
        verify(repository).save(any());
        assertThat(commandComAlteracoes.hasAlteracoesCoberturas()).isTrue();
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
    @DisplayName("Deve falhar quando apólice não pode ser renovada")
    void deveFalharQuandoApolicaNaoPodeSerRenovada() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doThrow(new IllegalStateException("Apólice cancelada não pode ser renovada"))
            .when(apoliceValidationService).validarRenovacao(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apólice cancelada não pode ser renovada");

        verify(apoliceValidationService).validarRenovacao(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando segurado não está mais ativo")
    void deveFalharQuandoSeguradoNaoEstaAtivo() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Segurado não está mais ativo");

        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando segurado tem restrições e renovação é automática")
    void deveFalharQuandoSeguradoTemRestricoesNaRenovacaoAutomatica() {
        // Arrange
        RenovarApoliceCommand commandAutomatico = RenovarApoliceCommand.automatica(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            novasCoberturas,
            FormaPagamento.MENSAL,
            "SISTEMA-AUTO"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(true);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandAutomatico).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Renovação automática bloqueada devido a restrições do segurado");

        verify(seguradoValidationService).temRestricoes("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve permitir renovação manual mesmo com restrições do segurado")
    void devePermitirRenovacaoManualComRestricoes() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(true);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(600);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(2));

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get(); // Renovação manual

        // Assert
        verify(repository).save(any());
        // Renovação manual permite mesmo com restrições
    }

    @Test
    @DisplayName("Deve falhar quando score de crédito é insuficiente")
    void deveFalharQuandoScoreCreditoInsuficiente() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(200);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Score de crédito insuficiente para renovação");

        verify(seguradoValidationService).obterScoreCredito("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando nova vigência é inválida")
    void deveFalharQuandoNovaVigenciaInvalida() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);

        // Fazer validarRenovacao lançar exceção para vigência inválida
        doThrow(new IllegalArgumentException("Vigência inválida"))
            .when(apoliceValidationService).validarRenovacao(any(), any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Vigência inválida");

        verify(apoliceValidationService).validarRenovacao(any(), any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando novo valor segurado não é positivo")
    void deveFalharQuandoNovoValorNaoPositivo() {
        // Arrange
        RenovarApoliceCommand commandInvalido = RenovarApoliceCommand.manual(
            "APOLICE-001",
            novaVigencia,
            Valor.zero(),
            novasCoberturas,
            FormaPagamento.MENSAL,
            "OPERADOR-002",
            "Tentativa com valor zero"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandInvalido).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Novo valor segurado deve ser positivo");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando não há coberturas")
    void deveFalharQuandoNaoHaCoberturas() {
        // Arrange
        RenovarApoliceCommand commandInvalido = RenovarApoliceCommand.manual(
            "APOLICE-001",
            novaVigencia,
            novoValorSegurado,
            List.of(),
            FormaPagamento.MENSAL,
            "OPERADOR-002",
            "Tentativa sem coberturas"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarVigencia(any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandInvalido).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Deve haver pelo menos uma cobertura na renovação");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve calcular prêmio com fator de risco baseado no histórico")
    void deveCalcularPremioComFatorRisco() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        // Histórico com múltiplos sinistros
        ApoliceValidationService.HistoricoSinistros historico =
            new ApoliceValidationService.HistoricoSinistros(4);
        when(seguradoValidationService.obterHistoricoSinistros(anyString())).thenReturn(historico);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        verify(seguradoValidationService).obterHistoricoSinistros("SEGURADO-001");
        assertThat(historico.getFatorRisco()).isEqualTo(1.4); // 1.0 + (4 * 0.1)
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve validar na ordem correta")
    void deveValidarNaOrdemCorreta() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert - verificar ordem de execução
        var inOrder = inOrder(repository, apoliceValidationService, seguradoValidationService);

        // 1. Carregar aggregate
        inOrder.verify(repository).getById("APOLICE-001");

        // 2. Validar elegibilidade
        inOrder.verify(apoliceValidationService).validarRenovacao(any(), any(), any());

        // 3. Verificar segurado
        inOrder.verify(seguradoValidationService).isSeguradoAtivo(anyString());
        inOrder.verify(seguradoValidationService).temRestricoes(anyString());
        inOrder.verify(seguradoValidationService).obterScoreCredito(anyString());

        // 4. Validar dados
        inOrder.verify(apoliceValidationService).validarVigencia(any());
        inOrder.verify(apoliceValidationService).validarCombinacaoCoberturas(any());
        inOrder.verify(apoliceValidationService).validarFormaPagamento(any(), any());

        // 5. Calcular prêmios
        inOrder.verify(seguradoValidationService).obterHistoricoSinistros(anyString());

        // 6. Salvar
        inOrder.verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve registrar operador responsável pela renovação")
    void deveRegistrarOperadorResponsavel() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getOperadorResponsavel()).isEqualTo("OPERADOR-002");
    }

    @Test
    @DisplayName("Deve permitir score de crédito no limite mínimo para renovação")
    void devePermitirScoreMinimoParaRenovacao() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(250); // Limite mínimo
        when(seguradoValidationService.obterHistoricoSinistros(anyString()))
            .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve processar diferentes formas de pagamento na renovação")
    void deveProcessarDiferentesFormasPagamento() throws ExecutionException, InterruptedException {
        // Arrange
        FormaPagamento[] formasPagamento = {
            FormaPagamento.MENSAL,
            FormaPagamento.TRIMESTRAL,
            FormaPagamento.SEMESTRAL,
            FormaPagamento.ANUAL
        };

        for (FormaPagamento forma : formasPagamento) {
            // Create fresh aggregate for each iteration
            LocalDate hoje = LocalDate.now();
            LocalDate fimVigenciaAtual = hoje.plusDays(30);
            Vigencia vigenciaAtual = Vigencia.of(
                hoje.minusDays(20),
                fimVigenciaAtual
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
                vigenciaAtual,
                valorSegurado,
                FormaPagamento.MENSAL,
                coberturas,
                "OPERADOR-001"
            );

            // Reset mocks
            reset(repository, apoliceValidationService, seguradoValidationService);

            RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                "APOLICE-001",
                novaVigencia,
                novoValorSegurado,
                novasCoberturas,
                forma,
                "OPERADOR-002",
                "Teste com forma de pagamento: " + forma
            );

            when(repository.getById("APOLICE-001")).thenReturn(freshAggregate);
            when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
            when(seguradoValidationService.temRestricoes(anyString())).thenReturn(false);
            when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);
            when(seguradoValidationService.obterHistoricoSinistros(anyString()))
                .thenReturn(new ApoliceValidationService.HistoricoSinistros(1));

            doNothing().when(apoliceValidationService).validarRenovacao(any(), any(), any());
            doNothing().when(apoliceValidationService).validarVigencia(any());
            doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
            doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

            // Act
            handler.handle(command).get();

            // Assert
            verify(repository).save(any());
            verify(apoliceValidationService).validarFormaPagamento(eq(forma), any());
        }
    }
}
