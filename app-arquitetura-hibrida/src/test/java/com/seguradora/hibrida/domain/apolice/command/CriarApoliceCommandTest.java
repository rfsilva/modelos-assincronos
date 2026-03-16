package com.seguradora.hibrida.domain.apolice.command;

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
 * Testes unitários para {@link CriarApoliceCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CriarApoliceCommand - Testes Unitários")
class CriarApoliceCommandTest {

    private Validator validator;
    private Vigencia vigenciaValida;
    private Valor valorSeguradoValido;
    private List<Cobertura> coberturasValidas;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        // Preparar dados válidos para reutilização
        vigenciaValida = Vigencia.anual(LocalDate.now().plusDays(1));
        valorSeguradoValido = Valor.reais(50000.00);

        Cobertura coberturaTotal = Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(50000.00));
        coberturasValidas = List.of(coberturaTotal);
    }

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos usando construtor")
    void deveCriarComandoComTodosCamposValidosUsandoConstrutor() {
        // Arrange & Act
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-001",
                "SEG-123",
                "Seguro Auto Completo",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                "Observações de teste"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-001");
        assertThat(command.getSeguradoId()).isEqualTo("SEG-123");
        assertThat(command.getProduto()).isEqualTo("Seguro Auto Completo");
        assertThat(command.getVigencia()).isEqualTo(vigenciaValida);
        assertThat(command.getValorSegurado()).isEqualTo(valorSeguradoValido);
        assertThat(command.getFormaPagamento()).isEqualTo(FormaPagamento.MENSAL);
        assertThat(command.getCoberturas()).hasSize(1);
        assertThat(command.getOperadorId()).isEqualTo("OPR-456");
        assertThat(command.getObservacoes()).isEqualTo("Observações de teste");
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'of'")
    void deveCriarComandoUsandoFactoryMethodOf() {
        // Arrange & Act
        CriarApoliceCommand command = CriarApoliceCommand.of(
                "APL-2026-002",
                "SEG-789",
                "Seguro Auto Básico",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.ANUAL,
                coberturasValidas,
                "OPR-999"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-002");
        assertThat(command.getSeguradoId()).isEqualTo("SEG-789");
        assertThat(command.getProduto()).isEqualTo("Seguro Auto Básico");
        assertThat(command.getVigencia()).isEqualTo(vigenciaValida);
        assertThat(command.getValorSegurado()).isEqualTo(valorSeguradoValido);
        assertThat(command.getFormaPagamento()).isEqualTo(FormaPagamento.ANUAL);
        assertThat(command.getCoberturas()).hasSize(1);
        assertThat(command.getOperadorId()).isEqualTo("OPR-999");
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve criar lista imutável de coberturas")
    void deveCriarListaImutavelDeCoberturas() {
        // Arrange
        List<Cobertura> listaMutavel = new ArrayList<>(coberturasValidas);

        // Act
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-003",
                "SEG-111",
                "Seguro Test",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.TRIMESTRAL,
                listaMutavel,
                "OPR-222",
                null
        );

        // Assert
        assertThat(command.getCoberturas()).isUnmodifiable();
    }

    @Test
    @DisplayName("Deve criar lista vazia quando coberturas é null")
    void deveCriarListaVaziaQuandoCoberturasEhNull() {
        // Arrange & Act
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-004",
                "SEG-222",
                "Seguro Test",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.SEMESTRAL,
                null,
                "OPR-333",
                null
        );

        // Assert
        assertThat(command.getCoberturas()).isEmpty();
    }

    @Test
    @DisplayName("Deve validar que apoliceId não pode ser vazio")
    void deveValidarQueApoliceIdNaoPodeSerVazio() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("apoliceId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que seguradoId não pode ser vazio")
    void deveValidarQueSeguradoIdNaoPodeSerVazio() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-005",
                "",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("seguradoId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que produto não pode ser vazio")
    void deveValidarQueProdutoNaoPodeSerVazio() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-006",
                "SEG-123",
                "",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("produto") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho mínimo do produto")
    void deveValidarTamanhoMinimoDoProduto() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-007",
                "SEG-123",
                "AB",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("produto") &&
                v.getMessage().contains("entre 3 e 100 caracteres")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho máximo do produto")
    void deveValidarTamanhoMaximoDoProduto() {
        // Arrange
        String produtoLongo = "A".repeat(101);
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-008",
                "SEG-123",
                produtoLongo,
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("produto") &&
                v.getMessage().contains("entre 3 e 100 caracteres")
        );
    }

    @Test
    @DisplayName("Deve validar que vigência não pode ser nula")
    void deveValidarQueVigenciaNaoPodeSerNula() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-009",
                "SEG-123",
                "Seguro Auto",
                null,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("vigencia") &&
                v.getMessage().contains("não pode ser nula")
        );
    }

    @Test
    @DisplayName("Deve validar que valorSegurado não pode ser nulo")
    void deveValidarQueValorSeguradoNaoPodeSerNulo() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-010",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                null,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("valorSegurado") &&
                v.getMessage().contains("não pode ser nulo")
        );
    }

    @Test
    @DisplayName("Deve validar que formaPagamento não pode ser nula")
    void deveValidarQueFormaPagamentoNaoPodeSerNula() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-011",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                null,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("formaPagamento") &&
                v.getMessage().contains("não pode ser nula")
        );
    }

    @Test
    @DisplayName("Deve validar que coberturas não pode ser vazia")
    void deveValidarQueCoberturasNaoPodeSerVazia() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-012",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                List.of(),
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("coberturas") &&
                v.getMessage().contains("pelo menos uma cobertura")
        );
    }

    @Test
    @DisplayName("Deve validar que operadorId não pode ser vazio")
    void deveValidarQueOperadorIdNaoPodeSerVazio() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-013",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("operadorId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho máximo das observações")
    void deveValidarTamanhoMaximoDasObservacoes() {
        // Arrange
        String observacoesLongas = "A".repeat(501);
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-014",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                observacoesLongas
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("observacoes") &&
                v.getMessage().contains("500 caracteres")
        );
    }

    @Test
    @DisplayName("Deve aceitar observações null")
    void deveAceitarObservacoesNull() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-015",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CriarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade entre comandos idênticos")
    void deveVerificarIgualdadeEntreComandosIdenticos() {
        // Arrange
        CriarApoliceCommand command1 = new CriarApoliceCommand(
                "APL-2026-016",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                "Obs"
        );

        CriarApoliceCommand command2 = new CriarApoliceCommand(
                "APL-2026-016",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                "Obs"
        );

        // Act & Assert
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre comandos diferentes")
    void deveVerificarDesigualdadeEntreComandosDiferentes() {
        // Arrange
        CriarApoliceCommand command1 = new CriarApoliceCommand(
                "APL-2026-017",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                null
        );

        CriarApoliceCommand command2 = new CriarApoliceCommand(
                "APL-2026-018",
                "SEG-456",
                "Seguro Residencial",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.ANUAL,
                coberturasValidas,
                "OPR-789",
                null
        );

        // Act & Assert
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    @DisplayName("Deve retornar toString formatado corretamente")
    void deveRetornarToStringFormatadoCorretamente() {
        // Arrange
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-019",
                "SEG-123",
                "Seguro Auto",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                coberturasValidas,
                "OPR-456",
                "Teste"
        );

        // Act
        String toString = command.toString();

        // Assert
        assertThat(toString)
                .contains("CriarApoliceCommand")
                .contains("APL-2026-019")
                .contains("SEG-123")
                .contains("Seguro Auto")
                .contains("OPR-456");
    }

    @Test
    @DisplayName("Deve aceitar múltiplas coberturas")
    void deveAceitarMultiplasCoberturas() {
        // Arrange
        List<Cobertura> multiplasCoberturasValidas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(50000.00)),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, Valor.reais(45000.00)),
                Cobertura.basica(TipoCobertura.COLISAO, Valor.reais(40000.00))
        );

        // Act
        CriarApoliceCommand command = new CriarApoliceCommand(
                "APL-2026-020",
                "SEG-123",
                "Seguro Auto Premium",
                vigenciaValida,
                valorSeguradoValido,
                FormaPagamento.MENSAL,
                multiplasCoberturasValidas,
                "OPR-456",
                null
        );

        // Assert
        assertThat(command.getCoberturas()).hasSize(3);
        assertThat(validator.validate(command)).isEmpty();
    }

    @Test
    @DisplayName("Deve aceitar todas as formas de pagamento")
    void deveAceitarTodasAsFormasDePagamento() {
        // Arrange & Act & Assert
        for (FormaPagamento formaPagamento : FormaPagamento.values()) {
            CriarApoliceCommand command = new CriarApoliceCommand(
                    "APL-2026-" + formaPagamento.name(),
                    "SEG-123",
                    "Seguro Auto",
                    vigenciaValida,
                    valorSeguradoValido,
                    formaPagamento,
                    coberturasValidas,
                    "OPR-456",
                    null
            );

            assertThat(command.getFormaPagamento()).isEqualTo(formaPagamento);
            assertThat(validator.validate(command)).isEmpty();
        }
    }
}
