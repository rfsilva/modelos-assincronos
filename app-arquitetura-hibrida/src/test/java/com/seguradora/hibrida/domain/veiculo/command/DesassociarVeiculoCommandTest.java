package com.seguradora.hibrida.domain.veiculo.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link DesassociarVeiculoCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("DesassociarVeiculoCommand - Testes Unitários")
class DesassociarVeiculoCommandTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar comando com dados válidos")
        void deveCriarComandoComDadosValidos() {
            // Arrange
            LocalDate dataFim = LocalDate.now();

            // Act
            DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", dataFim, "Cancelamento",
                "OP-123", UUID.randomUUID(), "USER-001"
            );

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getCommandId()).isNotNull();
            assertThat(command.getTimestamp()).isNotNull();
            assertThat(command.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(command.getApoliceId()).isEqualTo("APO-001");
            assertThat(command.getDataFim()).isEqualTo(dataFim);
            assertThat(command.getMotivo()).isEqualTo("Cancelamento");
            assertThat(command.getOperadorId()).isEqualTo("OP-123");
        }

        @Test
        @DisplayName("Deve aceitar data no passado")
        void deveAceitarDataNoPassado() {
            LocalDate dataPassado = LocalDate.now().minusDays(10);

            assertThatCode(() -> new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", dataPassado, "Venda", "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data no futuro")
        void deveAceitarDataNoFuturo() {
            LocalDate dataFuturo = LocalDate.now().plusDays(30);

            assertThatCode(() -> new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", dataFuturo, "Término previsto", "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar diferentes motivos")
        void deveAceitarDiferentesMotivos() {
            assertThatCode(() -> new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Cancelamento por solicitação do cliente",
                "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();

            assertThatCode(() -> new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Venda do veículo",
                "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();

            assertThatCode(() -> new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Término de vigência",
                "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Comandos com mesmo ID devem ser iguais")
        void comandosComMesmoIdDevemSerIguais() {
            DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Cancelamento",
                "OP-123", UUID.randomUUID(), "USER-001"
            );

            assertThat(command).isEqualTo(command);
            assertThat(command.hashCode()).isEqualTo(command.hashCode());
        }

        @Test
        @DisplayName("Comandos diferentes não devem ser iguais")
        void comandosDiferentesNaoDevemSerIguais() {
            DesassociarVeiculoCommand command1 = new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Cancelamento",
                "OP-123", UUID.randomUUID(), "USER-001"
            );

            DesassociarVeiculoCommand command2 = new DesassociarVeiculoCommand(
                "VEI-002", "APO-002", LocalDate.now(), "Venda",
                "OP-456", UUID.randomUUID(), "USER-002"
            );

            assertThat(command1).isNotEqualTo(command2);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "Cancelamento",
                "OP-123", UUID.randomUUID(), "USER-001"
            );

            String toString = command.toString();

            assertThat(toString).contains("DesassociarVeiculoCommand");
            assertThat(toString).contains("VEI-001");
            assertThat(toString).contains("APO-001");
        }
    }
}
