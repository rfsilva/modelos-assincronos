package com.seguradora.hibrida.domain.apolice.command;

import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CancelarApoliceCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CancelarApoliceCommand - Testes Unitários")
class CancelarApoliceCommandTest {

    private Validator validator;
    private LocalDate dataEfeitoValida;

    @BeforeEach
    void setUp() {
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        dataEfeitoValida = LocalDate.now().plusDays(15);
    }

    @Test
    @DisplayName("Deve criar comando com todos os campos válidos usando construtor")
    void deveCriarComandoComTodosCamposValidosUsandoConstrutor() {
        // Arrange & Act
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-001",
                "Cliente solicitou cancelamento devido à venda do veículo",
                dataEfeitoValida,
                "OPR-456",
                "Cliente apresentou comprovante de venda",
                ApoliceCanceladaEvent.TipoCancelamento.VENDA_VEICULO,
                true
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-001");
        assertThat(command.getMotivo()).isEqualTo("Cliente solicitou cancelamento devido à venda do veículo");
        assertThat(command.getDataEfeito()).isEqualTo(dataEfeitoValida);
        assertThat(command.getOperadorId()).isEqualTo("OPR-456");
        assertThat(command.getObservacoes()).isEqualTo("Cliente apresentou comprovante de venda");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.VENDA_VEICULO);
        assertThat(command.isCalcularReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porSolicitacaoSegurado'")
    void deveCriarComandoUsandoFactoryMethodPorSolicitacaoSegurado() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porSolicitacaoSegurado(
                "APL-2026-002",
                "Cliente solicitou cancelamento por motivos pessoais",
                dataEfeitoValida,
                "OPR-789",
                "Cliente desistiu da cobertura"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-002");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO);
        assertThat(command.isCalcularReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porInadimplencia'")
    void deveCriarComandoUsandoFactoryMethodPorInadimplencia() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porInadimplencia(
                "APL-2026-003",
                "Cliente com 3 parcelas em atraso sem acordo de pagamento",
                dataEfeitoValida,
                "OPR-111",
                "Notificações enviadas sem retorno"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-003");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.INADIMPLENCIA);
        assertThat(command.isCalcularReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porFraude'")
    void deveCriarComandoUsandoFactoryMethodPorFraude() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
                "APL-2026-004",
                "Detectada fraude nas informações fornecidas durante a contratação",
                dataEfeitoValida,
                "OPR-222",
                "Informações inverídicas sobre histórico de sinistros"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-004");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.FRAUDE);
        assertThat(command.isCalcularReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porDecisaoSeguradora'")
    void deveCriarComandoUsandoFactoryMethodPorDecisaoSeguradora() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porDecisaoSeguradora(
                "APL-2026-005",
                "Seguradora decidiu não renovar devido a alto índice de sinistralidade",
                dataEfeitoValida,
                "OPR-333",
                "Cliente com 5 sinistros no último ano"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-005");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.DECISAO_SEGURADORA);
        assertThat(command.isCalcularReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porVendaVeiculo'")
    void deveCriarComandoUsandoFactoryMethodPorVendaVeiculo() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porVendaVeiculo(
                "APL-2026-006",
                "Veículo segurado foi vendido conforme comprovante apresentado",
                dataEfeitoValida,
                "OPR-444",
                "Comprovante de venda anexado ao processo"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-006");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.VENDA_VEICULO);
        assertThat(command.isCalcularReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve criar comando usando factory method 'porPerdaTotal'")
    void deveCriarComandoUsandoFactoryMethodPorPerdaTotal() {
        // Arrange & Act
        CancelarApoliceCommand command = CancelarApoliceCommand.porPerdaTotal(
                "APL-2026-007",
                "Veículo teve perda total em sinistro coberto pela apólice",
                dataEfeitoValida,
                "OPR-555",
                "Laudo de perda total emitido pelo vistoriador"
        );

        // Assert
        assertThat(command).isNotNull();
        assertThat(command.getApoliceId()).isEqualTo("APL-2026-007");
        assertThat(command.getTipoCancelamento()).isEqualTo(ApoliceCanceladaEvent.TipoCancelamento.PERDA_TOTAL);
        assertThat(command.isCalcularReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se permite reembolso para SOLICITACAO_SEGURADO")
    void deveVerificarSePermiteReembolsoParaSolicitacaoSegurado() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porSolicitacaoSegurado(
                "APL-2026-008",
                "Cancelamento por solicitação do segurado com direito a reembolso",
                dataEfeitoValida,
                "OPR-666",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se permite reembolso para DECISAO_SEGURADORA")
    void deveVerificarSePermiteReembolsoParaDecisaoSeguradora() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porDecisaoSeguradora(
                "APL-2026-009",
                "Cancelamento por decisão da seguradora com reembolso proporcional",
                dataEfeitoValida,
                "OPR-777",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve verificar se permite reembolso para VENDA_VEICULO")
    void deveVerificarSePermiteReembolsoParaVendaVeiculo() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porVendaVeiculo(
                "APL-2026-010",
                "Cancelamento por venda com reembolso do período não utilizado",
                dataEfeitoValida,
                "OPR-888",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isTrue();
    }

    @Test
    @DisplayName("Deve verificar que não permite reembolso para INADIMPLENCIA")
    void deveVerificarQueNaoPermiteReembolsoParaInadimplencia() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porInadimplencia(
                "APL-2026-011",
                "Cancelamento por inadimplência sem direito a reembolso",
                dataEfeitoValida,
                "OPR-999",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar que não permite reembolso para FRAUDE")
    void deveVerificarQueNaoPermiteReembolsoParaFraude() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porFraude(
                "APL-2026-012",
                "Cancelamento por fraude sem qualquer reembolso",
                dataEfeitoValida,
                "OPR-101",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar que não permite reembolso para PERDA_TOTAL")
    void deveVerificarQueNaoPermiteReembolsoParaPerdaTotal() {
        // Arrange
        CancelarApoliceCommand command = CancelarApoliceCommand.porPerdaTotal(
                "APL-2026-013",
                "Cancelamento por perda total sem reembolso adicional",
                dataEfeitoValida,
                "OPR-202",
                null
        );

        // Act & Assert
        assertThat(command.permiteReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve validar que apoliceId não pode ser vazio")
    void deveValidarQueApoliceIdNaoPodeSerVazio() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "",
                "Motivo válido para teste de validação de ID",
                dataEfeitoValida,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("apoliceId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que motivo não pode ser vazio")
    void deveValidarQueMotivoNaoPodeSerVazio() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-014",
                "",
                dataEfeitoValida,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("motivo") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho mínimo do motivo")
    void deveValidarTamanhoMinimoDoMotivo() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-015",
                "Curto",
                dataEfeitoValida,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

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
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-016",
                motivoLongo,
                dataEfeitoValida,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("motivo") &&
                v.getMessage().contains("entre 10 e 500 caracteres")
        );
    }

    @Test
    @DisplayName("Deve validar que dataEfeito não pode ser nula")
    void deveValidarQueDataEfeitoNaoPodeSerNula() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-017",
                "Motivo válido para teste de data nula",
                null,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("dataEfeito") &&
                v.getMessage().contains("não pode ser nula")
        );
    }

    @Test
    @DisplayName("Deve validar que dataEfeito deve ser futura")
    void deveValidarQueDataEfeitoDeveSerFutura() {
        // Arrange
        LocalDate dataPassada = LocalDate.now().minusDays(1);
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-018",
                "Motivo válido para teste de data passada",
                dataPassada,
                "OPR-456",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("dataEfeito") &&
                v.getMessage().contains("futura")
        );
    }

    @Test
    @DisplayName("Deve validar que operadorId não pode ser vazio")
    void deveValidarQueOperadorIdNaoPodeSerVazio() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-019",
                "Motivo válido para teste de operador vazio",
                dataEfeitoValida,
                "",
                null,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("operadorId") &&
                v.getMessage().contains("não pode ser vazio")
        );
    }

    @Test
    @DisplayName("Deve validar que tipoCancelamento não pode ser nulo")
    void deveValidarQueTipoCancelamentoNaoPodeSerNulo() {
        // Arrange
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-020",
                "Motivo válido para teste de tipo nulo",
                dataEfeitoValida,
                "OPR-456",
                null,
                null,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isNotEmpty();
        assertThat(violations).anyMatch(v ->
                v.getPropertyPath().toString().equals("tipoCancelamento") &&
                v.getMessage().contains("não pode ser nulo")
        );
    }

    @Test
    @DisplayName("Deve validar tamanho máximo das observações")
    void deveValidarTamanhoMaximoDasObservacoes() {
        // Arrange
        String observacoesLongas = "A".repeat(1001);
        CancelarApoliceCommand command = new CancelarApoliceCommand(
                "APL-2026-021",
                "Motivo válido para teste de observações longas",
                dataEfeitoValida,
                "OPR-456",
                observacoesLongas,
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

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
        CancelarApoliceCommand command = CancelarApoliceCommand.porSolicitacaoSegurado(
                "APL-2026-022",
                "Motivo válido para teste de observações nulas",
                dataEfeitoValida,
                "OPR-456",
                null
        );

        // Act
        Set<ConstraintViolation<CancelarApoliceCommand>> violations = validator.validate(command);

        // Assert
        assertThat(violations).isEmpty();
        assertThat(command.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve verificar igualdade entre comandos idênticos")
    void deveVerificarIgualdadeEntreComandosIdenticos() {
        // Arrange
        CancelarApoliceCommand command1 = new CancelarApoliceCommand(
                "APL-2026-023",
                "Motivo de teste para verificação de igualdade",
                dataEfeitoValida,
                "OPR-456",
                "Obs",
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        CancelarApoliceCommand command2 = new CancelarApoliceCommand(
                "APL-2026-023",
                "Motivo de teste para verificação de igualdade",
                dataEfeitoValida,
                "OPR-456",
                "Obs",
                ApoliceCanceladaEvent.TipoCancelamento.SOLICITACAO_SEGURADO,
                true
        );

        // Act & Assert
        assertThat(command1).isEqualTo(command2);
        assertThat(command1.hashCode()).isEqualTo(command2.hashCode());
    }

    @Test
    @DisplayName("Deve verificar desigualdade entre comandos diferentes")
    void deveVerificarDesigualdadeEntreComandosDiferentes() {
        // Arrange
        CancelarApoliceCommand command1 = CancelarApoliceCommand.porSolicitacaoSegurado(
                "APL-2026-024",
                "Primeiro motivo para teste de desigualdade",
                dataEfeitoValida,
                "OPR-456",
                null
        );

        CancelarApoliceCommand command2 = CancelarApoliceCommand.porInadimplencia(
                "APL-2026-025",
                "Segundo motivo diferente para teste",
                dataEfeitoValida,
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
        CancelarApoliceCommand command = CancelarApoliceCommand.porVendaVeiculo(
                "APL-2026-026",
                "Motivo válido para teste de formatação toString",
                dataEfeitoValida,
                "OPR-456",
                "Teste"
        );

        // Act
        String toString = command.toString();

        // Assert
        assertThat(toString)
                .contains("CancelarApoliceCommand")
                .contains("APL-2026-026")
                .contains("VENDA_VEICULO")
                .contains("OPR-456");
    }

    @Test
    @DisplayName("Deve testar todos os tipos de cancelamento")
    void deveTestarTodosTiposDeCancelamento() {
        // Arrange & Act & Assert
        for (ApoliceCanceladaEvent.TipoCancelamento tipo : ApoliceCanceladaEvent.TipoCancelamento.values()) {
            CancelarApoliceCommand command = new CancelarApoliceCommand(
                    "APL-2026-" + tipo.name(),
                    "Motivo válido para tipo " + tipo.name(),
                    dataEfeitoValida,
                    "OPR-456",
                    null,
                    tipo,
                    false
            );

            assertThat(command.getTipoCancelamento()).isEqualTo(tipo);
            assertThat(validator.validate(command)).isEmpty();
        }
    }
}
