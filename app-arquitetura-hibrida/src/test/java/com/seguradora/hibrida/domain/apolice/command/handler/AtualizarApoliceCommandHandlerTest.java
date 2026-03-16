package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.AtualizarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.model.*;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
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
 * Testes unitários para AtualizarApoliceCommandHandler.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarApoliceCommandHandler - Testes Unitários")
class AtualizarApoliceCommandHandlerTest {

    @Mock
    private AggregateRepository<ApoliceAggregate> repository;

    @Mock
    private ApoliceValidationService validationService;

    private AtualizarApoliceCommandHandler handler;

    private ApoliceAggregate apoliceAggregate;
    private AtualizarApoliceCommand validCommand;
    private Valor novoValorSegurado;
    private List<Cobertura> novasCoberturas;

    @BeforeEach
    void setUp() {
        handler = new AtualizarApoliceCommandHandler(repository, validationService);

        // Criar apólice existente
        Vigencia vigencia = Vigencia.of(
            LocalDate.now().minusDays(10),
            LocalDate.now().plusMonths(11)
        );

        Valor valorOriginal = Valor.reais(new BigDecimal("50000.00"));

        List<Cobertura> coberturasOriginais = List.of(
            Cobertura.of(
                TipoCobertura.COLISAO,
                valorOriginal,
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
            valorOriginal,
            FormaPagamento.MENSAL,
            coberturasOriginais,
            "OPERADOR-001"
        );

        // Novos dados para atualização
        novoValorSegurado = Valor.reais(new BigDecimal("60000.00"));

        novasCoberturas = List.of(
            Cobertura.of(
                TipoCobertura.COLISAO,
                novoValorSegurado,
                Valor.reais(new BigDecimal("3000.00")),
                0
            ),
            Cobertura.of(
                TipoCobertura.ROUBO_FURTO,
                novoValorSegurado,
                Valor.reais(new BigDecimal("3000.00")),
                30
            )
        );

        validCommand = AtualizarApoliceCommand.of(
            "APOLICE-001",
            novoValorSegurado,
            novasCoberturas,
            "OPERADOR-002",
            "Aumento de valor segurado e inclusão de cobertura adicional"
        );
    }

    @Test
    @DisplayName("Deve atualizar apólice com sucesso quando todos os dados são válidos")
    void deveAtualizarApoliceComSucesso() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        CompletableFuture<Void> result = handler.handle(validCommand);
        result.get();

        // Assert
        verify(repository).getById("APOLICE-001");
        verify(validationService).validarAlteracao(any(), any());
        verify(validationService).validarCombinacaoCoberturas(novasCoberturas);

        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getId()).isEqualTo("APOLICE-001");
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
    @DisplayName("Deve falhar quando há conflito de concorrência")
    void deveFalharQuandoHaConflitoNaVersao() {
        // Arrange
        AtualizarApoliceCommand commandComVersao = AtualizarApoliceCommand.withVersion(
            "APOLICE-001",
            novoValorSegurado,
            novasCoberturas,
            "OPERADOR-002",
            "Atualização com controle de versão",
            5L // Versão esperada diferente
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandComVersao).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .satisfies(ex -> {
                // A ConcurrencyException está na cadeia de causas
                Throwable rootCause = ex;
                while (rootCause.getCause() != null) {
                    rootCause = rootCause.getCause();
                }
                assertThat(rootCause).isInstanceOf(com.seguradora.hibrida.eventstore.exception.ConcurrencyException.class);
            });

        verify(repository).getById("APOLICE-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando apólice não pode ser alterada")
    void deveFalharQuandoApolicaNaoPodeSerAlterada() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doThrow(new IllegalStateException("Apólice cancelada não pode ser alterada"))
            .when(validationService).validarAlteracao(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Apólice cancelada não pode ser alterada");

        verify(validationService).validarAlteracao(any(), any());
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando novo valor segurado não é positivo")
    void deveFalharQuandoNovoValorNaoPositivo() {
        // Arrange
        AtualizarApoliceCommand commandInvalido = AtualizarApoliceCommand.of(
            "APOLICE-001",
            Valor.zero(),
            novasCoberturas,
            "OPERADOR-002",
            "Tentativa de zerar valor segurado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());

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
        AtualizarApoliceCommand commandInvalido = AtualizarApoliceCommand.of(
            "APOLICE-001",
            novoValorSegurado,
            List.of(),
            "OPERADOR-002",
            "Tentativa de remover todas coberturas"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandInvalido).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Deve haver pelo menos uma cobertura");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando combinação de coberturas é inválida")
    void deveFalharQuandoCombinacaoCoberturasInvalida() {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doThrow(new IllegalArgumentException("Combinação de coberturas inválida"))
            .when(validationService).validarCombinacaoCoberturas(any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Combinação de coberturas inválida");

        verify(validationService).validarCombinacaoCoberturas(novasCoberturas);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando não há alterações detectadas")
    void deveFalharQuandoNaoHaAlteracoes() {
        // Arrange
        AtualizarApoliceCommand commandSemAlteracoes = AtualizarApoliceCommand.of(
            "APOLICE-001",
            Valor.reais(new BigDecimal("50000.00")), // Mesmo valor
            List.of(
                Cobertura.of(
                    TipoCobertura.COLISAO,
                    Valor.reais(new BigDecimal("50000.00")),
                    Valor.reais(new BigDecimal("2500.00")),
                    0
                )
            ), // Mesmas coberturas
            "OPERADOR-002",
            "Tentativa de atualizar sem alterações"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(commandSemAlteracoes).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Nenhuma alteração detectada");

        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve atualizar apenas o valor segurado")
    void deveAtualizarApenasValorSegurado() throws ExecutionException, InterruptedException {
        // Arrange
        AtualizarApoliceCommand command = AtualizarApoliceCommand.of(
            "APOLICE-001",
            novoValorSegurado,
            List.of(
                Cobertura.of(
                    TipoCobertura.COLISAO,
                    Valor.reais(new BigDecimal("50000.00")),
                    Valor.reais(new BigDecimal("2500.00")),
                    0
                )
            ), // Mesmas coberturas
            "OPERADOR-002",
            "Alteração apenas no valor segurado"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve atualizar apenas as coberturas")
    void deveAtualizarApenasCoberturas() throws ExecutionException, InterruptedException {
        // Arrange
        AtualizarApoliceCommand command = AtualizarApoliceCommand.of(
            "APOLICE-001",
            Valor.reais(new BigDecimal("50000.00")), // Mesmo valor
            novasCoberturas, // Novas coberturas
            "OPERADOR-002",
            "Alteração apenas nas coberturas"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
        verify(validationService).validarCombinacaoCoberturas(novasCoberturas);
    }

    @Test
    @DisplayName("Deve validar na ordem correta")
    void deveValidarNaOrdemCorreta() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(validCommand).get();

        // Assert - verificar ordem de execução
        var inOrder = inOrder(repository, validationService);

        // 1. Carregar aggregate
        inOrder.verify(repository).getById("APOLICE-001");

        // 2. Validar alteração
        inOrder.verify(validationService).validarAlteracao(any(), any());

        // 3. Validar combinação de coberturas
        inOrder.verify(validationService).validarCombinacaoCoberturas(any());

        // 4. Salvar
        inOrder.verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve atualizar com controle de versão bem sucedido")
    void deveAtualizarComControleVersaoBemSucedido() throws ExecutionException, InterruptedException {
        // Arrange
        AtualizarApoliceCommand commandComVersao = AtualizarApoliceCommand.withVersion(
            "APOLICE-001",
            novoValorSegurado,
            novasCoberturas,
            "OPERADOR-002",
            "Atualização com controle de versão",
            1L // Versão esperada correta
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(commandComVersao).get();

        // Assert
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve registrar operador responsável pela alteração")
    void deveRegistrarOperadorResponsavel() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getOperadorResponsavel()).isEqualTo("OPERADOR-002");
    }

    @Test
    @DisplayName("Deve aumentar valor segurado e adicionar cobertura")
    void deveAumentarValorEAdicionarCobertura() throws ExecutionException, InterruptedException {
        // Arrange
        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        verify(repository).save(any());
        verify(validationService).validarCombinacaoCoberturas(argThat(coberturas ->
            coberturas.size() == 2 &&
            coberturas.stream().anyMatch(c -> c.getTipo() == TipoCobertura.ROUBO_FURTO)
        ));
    }

    @Test
    @DisplayName("Deve permitir redução de valor segurado")
    void devePermitirReducaoValorSegurado() throws ExecutionException, InterruptedException {
        // Arrange
        Valor valorReduzido = Valor.reais(new BigDecimal("40000.00"));

        AtualizarApoliceCommand command = AtualizarApoliceCommand.of(
            "APOLICE-001",
            valorReduzido,
            List.of(
                Cobertura.of(
                    TipoCobertura.COLISAO,
                    valorReduzido,
                    Valor.reais(new BigDecimal("2000.00")),
                    0
                )
            ),
            "OPERADOR-002",
            "Redução de valor segurado conforme solicitação do cliente"
        );

        when(repository.getById("APOLICE-001")).thenReturn(apoliceAggregate);
        doNothing().when(validationService).validarAlteracao(any(), any());
        doNothing().when(validationService).validarCombinacaoCoberturas(any());

        // Act
        handler.handle(command).get();

        // Assert
        verify(repository).save(any());
    }
}
