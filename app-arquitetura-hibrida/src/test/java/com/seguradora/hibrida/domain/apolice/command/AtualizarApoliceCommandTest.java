package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.model.Cobertura;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.model.Valor;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link AtualizarApoliceCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AtualizarApoliceCommand - Testes Unitários")
class AtualizarApoliceCommandTest {

    private Validator validator;
    private Valor valorSeguradoValido;
    private List<Cobertura> coberturasValidas;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        valorSeguradoValido = Valor.reais(60000.00);
        Cobertura coberturaTotal = Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(60000.00));
        coberturasValidas = List.of(coberturaTotal);
    }

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos usando construtor")
    void deveCriarComandoComTodosCamposValidosUsandoConstrutor() {
        // Arrange & Act
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-001",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Atualização devido a aumento do valor do veículo conforme tabela FIPE",
                5L,
                "Observações adicionais"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-001");
        assertThat(command.getNovoValorSegurado()).isEqualTo(valorSeguradoValido);
        assertThat(command.getNovasCoberturas()).hasSize(1);
        assertThat(command.getOperadorId()).isEqualTo("OPR-456");
        assertThat(command.getMotivo()).isEqualTo("Atualização devido a aumento do valor do veículo conforme tabela FIPE");
        assertThat(command.getVersaoEsperada()).isEqualTo(5L);
        assertThat(command.getObservacoes()).isEqualTo("Observações adicionais");
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'of'")
    void deveCriarComandoUsandoFactoryMethodOf() {
        // Arrange & Act
        AtualizarApoliceCommand command = AtualizarApoliceCommand.of(
                "APL-2026-002",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-789",
                "Atualização solicitada pelo cliente após revisão de valores"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-002");
        assertThat(command.getNovoValorSegurado()).isEqualTo(valorSeguradoValido);
        assertThat(command.getNovasCoberturas()).hasSize(1);
        assertThat(command.getOperadorId()).isEqualTo("OPR-789");
        assertThat(command.getMotivo()).isEqualTo("Atualização solicitada pelo cliente após revisão de valores");
        assertThat(command.getVersaoEsperada()).isNull();
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'withVersion'")
    void deveCriarComandoUsandoFactoryMethodWithVersion() {
        // Arrange & Act
        AtualizarApoliceCommand command = AtualizarApoliceCommand.withVersion(
                "APL-2026-003",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-111",
                "Atualização com controle de versão para evitar conflitos",
                10L
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-003");
        assertThat(command.getNovoValorSegurado()).isEqualTo(valorSeguradoValido);
        assertThat(command.getNovasCoberturas()).hasSize(1);
        assertThat(command.getOperadorId()).isEqualTo("OPR-111");
        assertThat(command.getMotivo()).isEqualTo("Atualização com controle de versão para evitar conflitos");
        assertThat(command.getVersaoEsperada()).isEqualTo(10L);
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve criar lista imutável de coberturas")
    void deveCriarListaImutavelDeCoberturas() {
        // Arrange
        List<Cobertura> listaMutavel = new ArrayList<>(coberturasValidas);

        // Act
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-004",
                valorSeguradoValido,
                listaMutavel,
                "OPR-222",
                "Motivo válido de teste para atualização da apólice",
                null,
                null
        );

        // Assert
        assertThat(command.getNovasCoberturas()).isUnmodifiable();
    }

    @Test
    @DisplayName("Deve criar lista vazia quando coberturas é null")
    void deveCriarListaVaziaQuandoCoberturasEhNull() {
        // Arrange & Act
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-005",
                valorSeguradoValido,
                null,
                "OPR-333",
                "Motivo válido para teste de coberturas nulas",
                null,
                null
        );

        // Assert
        assertThat(command.getNovasCoberturas()).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar true quando hasVersionControl e versão existe")
    void deveRetornarTrueQuandoHasVersionControlEVersaoExiste() {
        // Arrange
        AtualizarApoliceCommand command = AtualizarApoliceCommand.withVersion(
                "APL-2026-006",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-444",
                "Motivo com controle de versão presente",
                7L
        );

        // Act & Assert
        assertThat(command.hasVersionControl()).isTrue();
        assertThat(command.getVersaoEsperada()).isEqualTo(7L);
    }

    @Test
    @DisplayName("Deve retornar false quando hasVersionControl e versão não existe")
    void deveRetornarFalseQuandoHasVersionControlEVersaoNaoExiste() {
        // Arrange
        AtualizarApoliceCommand command = AtualizarApoliceCommand.of(
                "APL-2026-007",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-555",
                "Motivo sem controle de versão"
        );

        // Act & Assert
        assertThat(command.hasVersionControl()).isFalse();
        assertThat(command.getVersaoEsperada()).isNull();
    }

    @Test
    @DisplayName("Deve validar que apoliceId não pode ser vazio")
    void deveValidarQueApoliceIdNaoPodeSerVazio() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para validação de ID vazio",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("apoliceId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que novoValorSegurado não pode ser nulo")
    void deveValidarQueNovoValorSeguradoNaoPodeSerNulo() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-008",
                null,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de valor nulo",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

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
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-009",
                valorSeguradoValido,
                List.of(),
                "OPR-456",
                "Motivo válido para teste de coberturas vazias",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("novasCoberturas") &&
                v.getMessage().contains("pelo menos uma cobertura")
        );
    }

    @Test
    @DisplayName("Deve validar que operadorId não pode ser vazio")
    void deveValidarQueOperadorIdNaoPodeSerVazio() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-010",
                valorSeguradoValido,
                coberturasValidas,
                "",
                "Motivo válido para teste de operador vazio",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("operadorId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que motivo não pode ser vazio")
    void deveValidarQueMotivoNaoPodeSerVazio() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-011",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("motivo") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho mínimo do motivo")
    void deveValidarTamanhoMinimodoMotivo() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-012",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Curto",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("motivo") &&
                v.getMessage().contains("entre 10 e 500 caracteres")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho máximo do motivo")
    void deveValidarTamanhoMaximoDoMotivo() {
        // Arrange
        String motivoLongo = "A".repeat(501);
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-013",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                motivoLongo,
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("motivo") &&
                v.getMessage().contains("entre 10 e 500 caracteres")
        );
    }

    @Test
    @DisplayName("Deve validar que versaoEsperada não pode ser negativa")
    void deveValidarQueVersaoEsperadaNaoPodeSerNegativa() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-014",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de versão negativa",
                -1L,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("versaoEsperada") &&
                v.getMessage().contains("não negativa")
        );
    }

    @Test
    @DisplayName("Deve aceitar versaoEsperada zero")
    void deveAceitarVersaoEsperadaZero() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-015",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de versão zero",
                0L,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getVersaoEsperada()).isEqualTo(0L);
    }

    @Test
    @DisplayName("Deve validar tamanho máximo das observações")
    void deveValidarTamanhoMaximoDasObservacoes() {
        // Arrange
        String observacoesLongas = "A".repeat(1001);
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-016",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de observações longas",
                null,
                observacoesLongas
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

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
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-017",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de observações nulas",
                null,
                null
        );

        // Act
        Set<ConstraintViolation<AtualizarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade entre comandos idênticos")
    void deveVerificarIgualdadeEntreComandosIdenticos() {
        // Arrange
        AtualizarApoliceCommand command1 = new AtualizarApoliceCommand(
                "APL-2026-018",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo de teste para igualdade de comandos",
                5L,
                "Obs"
        );

        AtualizarApoliceCommand command2 = new AtualizarApoliceCommand(
                "APL-2026-018",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo de teste para igualdade de comandos",
                5L,
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
        AtualizarApoliceCommand command1 = new AtualizarApoliceCommand(
                "APL-2026-019",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Primeiro motivo diferente para teste",
                5L,
                null
        );

        AtualizarApoliceCommand command2 = new AtualizarApoliceCommand(
                "APL-2026-020",
                Valor.reais(70000.00),
                coberturasValidas,
                "OPR-789",
                "Segundo motivo diferente para teste",
                10L,
                null
        );

        // Act & Assert
        assertThat(command1).isNotEqualTo(command2);
    }

    @Test
    @DisplayName("Deve retornar toString formatado corretamente")
    void deveRetornarToStringFormatadoCorretamente() {
        // Arrange
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-021",
                valorSeguradoValido,
                coberturasValidas,
                "OPR-456",
                "Motivo válido para teste de toString",
                5L,
                "Teste"
        );

        // Act
        String toString = command.toString();

        // Assert
        assertThat(toString)
                .contains("AtualizarApoliceCommand")
                .contains("APL-2026-021")
                .contains("OPR-456");
    }

    @Test
    @DisplayName("Deve aceitar múltiplas coberturas")
    void deveAceitarMultiplasCoberturas() {
        // Arrange
        List<Cobertura> multiplasCoberturasValidas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, Valor.reais(60000.00)),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, Valor.reais(55000.00)),
                Cobertura.basica(TipoCobertura.INCENDIO, Valor.reais(50000.00))
        );

        // Act
        AtualizarApoliceCommand command = new AtualizarApoliceCommand(
                "APL-2026-022",
                valorSeguradoValido,
                multiplasCoberturasValidas,
                "OPR-456",
                "Atualização com múltiplas coberturas conforme solicitado",
                null,
                null
        );

        // Assert
        assertThat(command.getNovasCoberturas()).hasSize(3);
        assertThat(validator.validate(command)).isEmpty();
    }
}
