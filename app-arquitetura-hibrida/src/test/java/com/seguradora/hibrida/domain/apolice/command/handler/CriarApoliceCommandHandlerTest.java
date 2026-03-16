package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.CriarApoliceCommand;
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
 * Testes unitários para CriarApoliceCommandHandler.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CriarApoliceCommandHandler - Testes Unitários")
class CriarApoliceCommandHandlerTest {

    @Mock
    private AggregateRepository<ApoliceAggregate> repository;

    @Mock
    private ApoliceValidationService apoliceValidationService;

    @Mock
    private ApoliceValidationService seguradoValidationService;

    private CriarApoliceCommandHandler handler;

    private CriarApoliceCommand validCommand;
    private Vigencia vigencia;
    private Valor valorSegurado;
    private List<Cobertura> coberturas;

    @BeforeEach
    void setUp() {
        handler = new CriarApoliceCommandHandler(
            repository,
            apoliceValidationService,
            seguradoValidationService
        );

        vigencia = Vigencia.of(
            LocalDate.now().plusDays(1),
            LocalDate.now().plusYears(1)
        );

        valorSegurado = Valor.reais(new BigDecimal("50000.00"));

        coberturas = List.of(
            Cobertura.of(
                TipoCobertura.COLISAO,
                valorSegurado,
                Valor.reais(new BigDecimal("2500.00")),
                0
            ),
            Cobertura.of(
                TipoCobertura.ROUBO_FURTO,
                valorSegurado,
                Valor.reais(new BigDecimal("2500.00")),
                30
            )
        );

        validCommand = CriarApoliceCommand.of(
            "APOLICE-001",
            "SEGURADO-001",
            "Seguro Auto Premium",
            vigencia,
            valorSegurado,
            FormaPagamento.MENSAL,
            coberturas,
            "OPERADOR-001"
        );
    }

    @Test
    @DisplayName("Deve criar apólice com sucesso quando todos os dados são válidos")
    void deveCriarApoliceComSucesso() throws ExecutionException, InterruptedException {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        CompletableFuture<String> result = handler.handle(validCommand);
        String apoliceId = result.get();

        // Assert
        assertThat(apoliceId).isEqualTo("APOLICE-001");

        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(seguradoValidationService).contarApolicesAtivas("SEGURADO-001");
        verify(seguradoValidationService).obterScoreCredito("SEGURADO-001");

        verify(apoliceValidationService).validarVigencia(vigencia);
        verify(apoliceValidationService).validarValorSegurado(valorSegurado, "Seguro Auto Premium");
        verify(apoliceValidationService).validarCoberturas(coberturas, "Seguro Auto Premium");
        verify(apoliceValidationService).validarCombinacaoCoberturas(coberturas);
        verify(apoliceValidationService).validarFormaPagamento(FormaPagamento.MENSAL, valorSegurado);

        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getId()).isEqualTo("APOLICE-001");
        assertThat(savedAggregate.getSeguradoId()).isEqualTo("SEGURADO-001");
    }

    @Test
    @DisplayName("Deve falhar quando segurado não está ativo")
    void deveFalharQuandoSeguradoNaoEstaAtivo() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(false);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Segurado não está ativo");

        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando segurado atingiu limite de apólices")
    void deveFalharQuandoSeguradoAtingiuLimite() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(5L);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("já possui 5 apólices ativas");

        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(seguradoValidationService).contarApolicesAtivas("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando score de crédito é insuficiente")
    void deveFalharQuandoScoreCreditoInsuficiente() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(250);

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Score de crédito insuficiente");

        verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        verify(seguradoValidationService).contarApolicesAtivas("SEGURADO-001");
        verify(seguradoValidationService).obterScoreCredito("SEGURADO-001");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando vigência é inválida")
    void deveFalharQuandoVigenciaInvalida() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doThrow(new IllegalArgumentException("Vigência inválida"))
            .when(apoliceValidationService).validarVigencia(any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Vigência inválida");

        verify(apoliceValidationService).validarVigencia(vigencia);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando valor segurado é inválido")
    void deveFalharQuandoValorSeguradoInvalido() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doThrow(new IllegalArgumentException("Valor segurado inválido"))
            .when(apoliceValidationService).validarValorSegurado(any(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Valor segurado inválido");

        verify(apoliceValidationService).validarValorSegurado(valorSegurado, "Seguro Auto Premium");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando coberturas são inválidas")
    void deveFalharQuandoCoberturasInvalidas() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doThrow(new IllegalArgumentException("Coberturas inválidas"))
            .when(apoliceValidationService).validarCoberturas(any(), anyString());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Coberturas inválidas");

        verify(apoliceValidationService).validarCoberturas(coberturas, "Seguro Auto Premium");
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando combinação de coberturas é inválida")
    void deveFalharQuandoCombinacaoCoberturasInvalida() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doThrow(new IllegalArgumentException("Combinação de coberturas inválida"))
            .when(apoliceValidationService).validarCombinacaoCoberturas(any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Combinação de coberturas inválida");

        verify(apoliceValidationService).validarCombinacaoCoberturas(coberturas);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve falhar quando forma de pagamento é inválida")
    void deveFalharQuandoFormaPagamentoInvalida() {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doThrow(new IllegalArgumentException("Forma de pagamento inválida"))
            .when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act & Assert
        assertThatThrownBy(() -> handler.handle(validCommand).get())
            .isInstanceOf(ExecutionException.class)
            .hasCauseInstanceOf(RuntimeException.class)
            .hasMessageContaining("Forma de pagamento inválida");

        verify(apoliceValidationService).validarFormaPagamento(FormaPagamento.MENSAL, valorSegurado);
        verify(repository, never()).save(any());
    }

    @Test
    @DisplayName("Deve salvar aggregate com número de apólice gerado")
    void deveSalvarAggregateComNumeroGerado() throws ExecutionException, InterruptedException {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert
        ArgumentCaptor<ApoliceAggregate> aggregateCaptor = ArgumentCaptor.forClass(ApoliceAggregate.class);
        verify(repository).save(aggregateCaptor.capture());

        ApoliceAggregate savedAggregate = aggregateCaptor.getValue();
        assertThat(savedAggregate.getNumeroApolice()).isNotNull();
        assertThat(savedAggregate.getNumeroApolice().getNumero()).matches("AP-\\d{4}-\\d{6}");
    }

    @Test
    @DisplayName("Deve criar apólice com produto diferente")
    void deveCriarApoliceComProdutoDiferente() throws ExecutionException, InterruptedException {
        // Arrange
        CriarApoliceCommand command = CriarApoliceCommand.of(
            "APOLICE-002",
            "SEGURADO-002",
            "Seguro Residencial",
            vigencia,
            Valor.reais(new BigDecimal("150000.00")),
            FormaPagamento.ANUAL,
            List.of(
                Cobertura.of(
                    TipoCobertura.INCENDIO,
                    Valor.reais(new BigDecimal("150000.00")),
                    Valor.reais(new BigDecimal("5000.00")),
                    0
                )
            ),
            "OPERADOR-002"
        );

        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(1L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(700);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        String apoliceId = handler.handle(command).get();

        // Assert
        assertThat(apoliceId).isEqualTo("APOLICE-002");
        verify(apoliceValidationService).validarValorSegurado(any(), eq("Seguro Residencial"));
    }

    @Test
    @DisplayName("Deve validar todas as dependências na ordem correta")
    void deveValidarDependenciasNaOrdemCorreta() throws ExecutionException, InterruptedException {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        handler.handle(validCommand).get();

        // Assert - verificar ordem de execução
        var inOrder = inOrder(seguradoValidationService, apoliceValidationService, repository);

        // 1. Validações de segurado
        inOrder.verify(seguradoValidationService).isSeguradoAtivo("SEGURADO-001");
        inOrder.verify(seguradoValidationService).contarApolicesAtivas("SEGURADO-001");
        inOrder.verify(seguradoValidationService).obterScoreCredito("SEGURADO-001");

        // 2. Validações de apólice
        inOrder.verify(apoliceValidationService).validarVigencia(any());
        inOrder.verify(apoliceValidationService).validarValorSegurado(any(), anyString());
        inOrder.verify(apoliceValidationService).validarCoberturas(any(), anyString());
        inOrder.verify(apoliceValidationService).validarCombinacaoCoberturas(any());
        inOrder.verify(apoliceValidationService).validarFormaPagamento(any(), any());

        // 3. Salvamento no repositório
        inOrder.verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve permitir score de crédito no limite mínimo")
    void devePermitirScoreCreditoNoLimiteMinimo() throws ExecutionException, InterruptedException {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(2L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(300); // Limite mínimo

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        String apoliceId = handler.handle(validCommand).get();

        // Assert
        assertThat(apoliceId).isEqualTo("APOLICE-001");
        verify(repository).save(any());
    }

    @Test
    @DisplayName("Deve permitir 4 apólices ativas (abaixo do limite)")
    void devePermitir4ApolicesAtivas() throws ExecutionException, InterruptedException {
        // Arrange
        when(seguradoValidationService.isSeguradoAtivo(anyString())).thenReturn(true);
        when(seguradoValidationService.contarApolicesAtivas(anyString())).thenReturn(4L);
        when(seguradoValidationService.obterScoreCredito(anyString())).thenReturn(650);

        doNothing().when(apoliceValidationService).validarVigencia(any());
        doNothing().when(apoliceValidationService).validarValorSegurado(any(), anyString());
        doNothing().when(apoliceValidationService).validarCoberturas(any(), anyString());
        doNothing().when(apoliceValidationService).validarCombinacaoCoberturas(any());
        doNothing().when(apoliceValidationService).validarFormaPagamento(any(), any());

        // Act
        String apoliceId = handler.handle(validCommand).get();

        // Assert
        assertThat(apoliceId).isEqualTo("APOLICE-001");
        verify(repository).save(any());
    }
}
