package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.event.ApoliceRenovadaEvent;
import com.seguradora.hibrida.domain.apolice.model.Cobertura;
import com.seguradora.hibrida.domain.apolice.model.FormaPagamento;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.model.Valor;
import com.seguradora.hibrida.domain.apolice.model.Vigencia;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link RenovarApoliceCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("RenovarApoliceCommand - Testes Unitários")
class RenovarApoliceCommandTest {

    private Validator validator;
    private Vigencia novaVigenciaValida;
    private Valor novoValorSeguradoValido;
    private List<Cobertura> novasCoberturasValidas;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        novaVigenciaValida = Vigencia.anual(LocalDate.now().plusYears(1).plusDays(1));
        novoValorSeguradoValido = Valor.reais(55000.00);

        Cobertura coberturaTotal = Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(55000.00));
        novasCoberturasValidas = List.of(coberturaTotal);
    }

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos usando construtor")
    void deveCriarComandoComTodosCamposValidosUsandoConstrutor() {
        // Arrange & Act
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-001",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                "Renovação com ajuste de valores conforme FIPE",
                true,
                10.0
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-001");
        assertThat(command.getNovaVigencia()).isEqualTo(novaVigenciaValida);
        assertThat(command.getNovoValorSegurado()).isEqualTo(novoValorSeguradoValido);
        assertThat(command.getNovasCoberturas()).hasSize(1);
        assertThat(command.getNovaFormaPagamento()).isEqualTo(FormaPagamento.ANUAL);
        assertThat(command.getOperadorId()).isEqualTo("OPR-456");
        assertThat(command.getTipoRenovacao()).isEqualTo(ApoliceRenovadaEvent.TipoRenovacao.MANUAL);
        assertThat(command.getObservacoes()).isEqualTo("Renovação com ajuste de valores conforme FIPE");
        assertThat(command.isAplicarDesconto()).isTrue();
        assertThat(command.getPercentualDesconto()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'automatica'")
    void deveCriarComandoUsandoFactoryMethodAutomatica() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.automatica(
                "APL-2026-002",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.MENSAL,
                "OPR-789"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-002");
        assertThat(command.getTipoRenovacao()).isEqualTo(ApoliceRenovadaEvent.TipoRenovacao.AUTOMATICA);
        assertThat(command.isAplicarDesconto()).isTrue();
        assertThat(command.getPercentualDesconto()).isEqualTo(5.0);
        assertThat(command.getObservacoes()).contains("Renovação automática");
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'manual'")
    void deveCriarComandoUsandoFactoryMethodManual() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                "APL-2026-003",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.TRIMESTRAL,
                "OPR-111",
                "Renovação manual solicitada pelo cliente"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-003");
        assertThat(command.getTipoRenovacao()).isEqualTo(ApoliceRenovadaEvent.TipoRenovacao.MANUAL);
        assertThat(command.isAplicarDesconto()).isFalse();
        assertThat(command.getPercentualDesconto()).isNull();
        assertThat(command.getObservacoes()).isEqualTo("Renovação manual solicitada pelo cliente");
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'antecipada'")
    void deveCriarComandoUsandoFactoryMethodAntecipada() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.antecipada(
                "APL-2026-004",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.SEMESTRAL,
                "OPR-222",
                "Renovação antecipada com desconto especial",
                15.0
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-004");
        assertThat(command.getTipoRenovacao()).isEqualTo(ApoliceRenovadaEvent.TipoRenovacao.ANTECIPADA);
        assertThat(command.isAplicarDesconto()).isTrue();
        assertThat(command.getPercentualDesconto()).isEqualTo(15.0);
    }

    @Test
    @DisplayName("Deve criar comando antecipada sem desconto quando percentual é zero")
    void deveCriarComandoAntecipadaSemDescontoQuandoPercentualEhZero() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.antecipada(
                "APL-2026-005",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-333",
                "Renovação antecipada sem desconto",
                0.0
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.isAplicarDesconto()).isFalse();
        assertThat(command.getPercentualDesconto()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("Deve criar comando antecipada sem desconto quando percentual é null")
    void deveCriarComandoAntecipadaSemDescontoQuandoPercentualEhNull() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.antecipada(
                "APL-2026-006",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-444",
                "Renovação antecipada sem percentual",
                null
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.isAplicarDesconto()).isFalse();
        assertThat(command.getPercentualDesconto()).isNull();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'comAlteracoes'")
    void deveCriarComandoUsandoFactoryMethodComAlteracoes() {
        // Arrange & Act
        RenovarApoliceCommand command = RenovarApoliceCommand.comAlteracoes(
                "APL-2026-007",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.MENSAL,
                "OPR-555",
                "Renovação com alteração nas coberturas"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-007");
        assertThat(command.getTipoRenovacao()).isEqualTo(ApoliceRenovadaEvent.TipoRenovacao.COM_ALTERACOES);
        assertThat(command.isAplicarDesconto()).isFalse();
        assertThat(command.getPercentualDesconto()).isNull();
    }

    @Test
    @DisplayName("Deve criar lista imutável de coberturas")
    void deveCriarListaImutavelDeCoberturas() {
        // Arrange
        List<Cobertura> listaMutavel = new ArrayList<>(novasCoberturasValidas);

        // Act
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-008",
                novaVigenciaValida,
                novoValorSeguradoValido,
                listaMutavel,
                FormaPagamento.ANUAL,
                "OPR-666",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Assert
        assertThat(command.getNovasCoberturas()).isUnmodifiable();
    }

    @Test
    @DisplayName("Deve criar lista vazia quando coberturas é null")
    void deveCriarListaVaziaQuandoCoberturasEhNull() {
        // Arrange & Act
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-009",
                novaVigenciaValida,
                novoValorSeguradoValido,
                null,
                FormaPagamento.ANUAL,
                "OPR-777",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Assert
        assertThat(command.getNovasCoberturas()).isEmpty();
    }

    @Test
    @DisplayName("Deve verificar se é renovação automática")
    void deveVerificarSeEhRenovacaoAutomatica() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.automatica(
                "APL-2026-010",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-888"
        );

        // Act & Assert
        assertThat(command.isRenovacaoAutomatica()).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se não é renovação automática")
    void deveVerificarSeNaoEhRenovacaoAutomatica() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                "APL-2026-011",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-999",
                "Manual"
        );

        // Act & Assert
        assertThat(command.isRenovacaoAutomatica()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se há alterações nas coberturas")
    void deveVerificarSeHaAlteracoesNasCoberturas() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.comAlteracoes(
                "APL-2026-012",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-101",
                "Com alterações"
        );

        // Act & Assert
        assertThat(command.hasAlteracoesCoberturas()).isTrue();
    }

    @Test
    @DisplayName("Deve calcular desconto quando aplicável")
    void deveCalcularDescontoQuandoAplicavel() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.automatica(
                "APL-2026-013",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-202"
        );

        // Act
        Valor desconto = command.calcularDesconto();

        // Assert
        assertThat(desconto).isNotNull();
        assertThat(desconto.isPositivo()).isTrue();
        assertThat(desconto).isEqualTo(novoValorSeguradoValido.porcentagem(5.0));
    }

    @Test
    @DisplayName("Deve retornar valor zero quando não há desconto")
    void deveRetornarValorZeroQuandoNaoHaDesconto() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                "APL-2026-014",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-303",
                "Sem desconto"
        );

        // Act
        Valor desconto = command.calcularDesconto();

        // Assert
        assertThat(desconto).isNotNull();
        assertThat(desconto.isZero()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar valor zero quando percentual é null")
    void deveRetornarValorZeroQuandoPercentualEhNull() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-015",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-404",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                true,
                null
        );

        // Act
        Valor desconto = command.calcularDesconto();

        // Assert
        assertThat(desconto.isZero()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar valor zero quando percentual é negativo")
    void deveRetornarValorZeroQuandoPercentualEhNegativo() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-016",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-505",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                true,
                -5.0
        );

        // Act
        Valor desconto = command.calcularDesconto();

        // Assert
        assertThat(desconto.isZero()).isTrue();
    }

    @Test
    @DisplayName("Deve validar que apoliceId não pode ser vazio")
    void deveValidarQueApoliceIdNaoPodeSerVazio() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("apoliceId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que novaVigencia não pode ser nula")
    void deveValidarQueNovaVigenciaNaoPodeSerNula() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-017",
                null,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("novaVigencia") &&
                v.getMessage().contains("não pode ser nula")
        );
    }

    @Test
    @DisplayName("Deve validar que novoValorSegurado não pode ser nulo")
    void deveValidarQueNovoValorSeguradoNaoPodeSerNulo() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-018",
                novaVigenciaValida,
                null,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("novoValorSegurado") &&
                v.getMessage().contains("não pode ser nulo")
        );
    }

    @Test
    @DisplayName("Deve validar que coberturas não pode ser vazia")
    void deveValidarQueCoberturasNaoPodeSerVazia() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-019",
                novaVigenciaValida,
                novoValorSeguradoValido,
                List.of(),
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("novasCoberturas") &&
                v.getMessage().contains("pelo menos uma cobertura")
        );
    }

    @Test
    @DisplayName("Deve validar que novaFormaPagamento não pode ser nula")
    void deveValidarQueNovaFormaPagamentoNaoPodeSerNula() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-020",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                null,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("novaFormaPagamento") &&
                v.getMessage().contains("não pode ser nula")
        );
    }

    @Test
    @DisplayName("Deve validar que operadorId não pode ser vazio")
    void deveValidarQueOperadorIdNaoPodeSerVazio() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-021",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("operadorId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que tipoRenovacao não pode ser nulo")
    void deveValidarQueTipoRenovacaoNaoPodeSerNulo() {
        // Arrange
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-022",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                null,
                null,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("tipoRenovacao") &&
                v.getMessage().contains("não pode ser nulo")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho máximo das observações")
    void deveValidarTamanhoMaximoDasObservacoes() {
        // Arrange
        String observacoesLongas = "A".repeat(1001);
        RenovarApoliceCommand command = new RenovarApoliceCommand(
                "APL-2026-023",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                observacoesLongas,
                false,
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("observacoes") &&
                v.getMessage().contains("1000 caracteres")
        );
    }

    @Test
    @DisplayName("Deve aceitar observações null")
    void deveAceitarObservacoesNull() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                "APL-2026-024",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<RenovarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade entre comandos idênticos")
    void deveVerificarIgualdadeEntreComandosIdenticos() {
        // Arrange
        RenovarApoliceCommand command1 = new RenovarApoliceCommand(
                "APL-2026-025",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                "Obs",
                true,
                10.0
        );

        RenovarApoliceCommand command2 = new RenovarApoliceCommand(
                "APL-2026-025",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                ApoliceRenovadaEvent.TipoRenovacao.MANUAL,
                "Obs",
                true,
                10.0
        );

        // Act & Assert
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre comandos diferentes")
    void deveVerificarDesigualdadeEntreComandosDiferentes() {
        // Arrange
        RenovarApoliceCommand command1 = RenovarApoliceCommand.automatica(
                "APL-2026-026",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456"
        );

        RenovarApoliceCommand command2 = RenovarApoliceCommand.manual(
                "APL-2026-027",
                novaVigenciaValida,
                Valor.reais(60000.00),
                novasCoberturasValidas,
                FormaPagamento.MENSAL,
                "OPR-789",
                "Manual"
        );

        // Act & Assert
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    @DisplayName("Deve retornar toString formatado corretamente")
    void deveRetornarToStringFormatadoCorretamente() {
        // Arrange
        RenovarApoliceCommand command = RenovarApoliceCommand.automatica(
                "APL-2026-028",
                novaVigenciaValida,
                novoValorSeguradoValido,
                novasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456"
        );

        // Act
        String toString = command.toString();

        // Assert
        assertThat(toString)
                .contains("RenovarApoliceCommand")
                .contains("APL-2026-028")
                .contains("AUTOMATICA")
                .contains("OPR-456");
    }

    @Test
    @DisplayName("Deve aceitar múltiplas coberturas")
    void deveAceitarMultiplasCoberturas() {
        // Arrange
        List<Cobertura> multiplasCoberturasValidas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(55000.00)),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, Valor.reais(50000.00)),
                Cobertura.basica(TipoCobertura.COLISAO, Valor.reais(45000.00))
        );

        // Act
        RenovarApoliceCommand command = RenovarApoliceCommand.comAlteracoes(
                "APL-2026-029",
                novaVigenciaValida,
                novoValorSeguradoValido,
                multiplasCoberturasValidas,
                FormaPagamento.ANUAL,
                "OPR-456",
                "Com múltiplas coberturas"
        );

        // Assert
        assertThat(command.getNovasCoberturas()).hasSize(3);
        assertThat(validator.validate(command)).isEmpty();
    }

    @Test
    @DisplayName("Deve aceitar todas as formas de pagamento")
    void deveAceitarTodasAsFormasDePagamento() {
        // Arrange & Act & Assert
        for (FormaPagamento formaPagamento : FormaPagamento.values()) {
            RenovarApoliceCommand command = RenovarApoliceCommand.manual(
                    "APL-2026-" + formaPagamento.name(),
                    novaVigenciaValida,
                    novoValorSeguradoValido,
                    novasCoberturasValidas,
                    formaPagamento,
                    "OPR-456",
                    "Teste " + formaPagamento.name()
            );

            assertThat(command.getNovaFormaPagamento()).isEqualTo(formaPagamento);
            assertThat(validator.validate(command)).isEmpty();
        }
    }

    @Test
    @DisplayName("Deve testar todos os tipos de renovação")
    void deveTestarTodosTiposDeRenovacao() {
        // Arrange & Act & Assert
        for (ApoliceRenovadaEvent.TipoRenovacao tipo : ApoliceRenovadaEvent.TipoRenovacao.values()) {
            RenovarApoliceCommand command = new RenovarApoliceCommand(
                    "APL-2026-" + tipo.name(),
                    novaVigenciaValida,
                    novoValorSeguradoValido,
                    novasCoberturasValidas,
                    FormaPagamento.ANUAL,
                    "OPR-456",
                    tipo,
                    "Teste " + tipo.name(),
                    false,
                    null
            );

            assertThat(command.getTipoRenovacao()).isEqualTo(tipo);
            assertThat(validator.validate(command)).isEmpty();
        }
    }
}
