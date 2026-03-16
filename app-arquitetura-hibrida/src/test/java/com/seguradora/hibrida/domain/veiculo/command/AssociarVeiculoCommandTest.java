package com.seguradora.hibrida.domain.veiculo.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AssociarVeiculoCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AssociarVeiculoCommand - Testes Unitários")
class AssociarVeiculoCommandTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar comando com dados válidos")
        void deveCriarComandoComDadosValidos() {
            // Arrange
            LocalDate dataInicio = LocalDate.now();

            // Act
            AssociarVeiculoCommand command = new AssociarVeiculoCommand(
                "VEI-001", "APO-001", dataInicio, "OP-123", UUID.randomUUID(), "USER-001"
            );

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getCommandId()).isNotNull();
            assertThat(command.getTimestamp()).isNotNull();
            assertThat(command.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(command.getApoliceId()).isEqualTo("APO-001");
            assertThat(command.getDataInicio()).isEqualTo(dataInicio);
            assertThat(command.getOperadorId()).isEqualTo("OP-123");
        }

        @Test
        @DisplayName("Deve aceitar data no passado")
        void deveAceitarDataNoPassado() {
            LocalDate dataPassado = LocalDate.now().minusDays(10);

            assertThatCode(() -> new AssociarVeiculoCommand(
                "VEI-001", "APO-001", dataPassado, "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data no futuro")
        void deveAceitarDataNoFuturo() {
            LocalDate dataFuturo = LocalDate.now().plusDays(30);

            assertThatCode(() -> new AssociarVeiculoCommand(
                "VEI-001", "APO-001", dataFuturo, "OP-123", UUID.randomUUID(), "USER-001"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Comandos com mesmo ID devem ser iguais")
        void comandosComMesmoIdDevemSerIguais() {
            AssociarVeiculoCommand command = new AssociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "OP-123", UUID.randomUUID(), "USER-001"
            );

            assertThat(command).isEqualTo(command);
            assertThat(command.hashCode()).isEqualTo(command.hashCode());
        }

        @Test
        @DisplayName("Comandos diferentes não devem ser iguais")
        void comandosDiferentesNaoDevemSerIguais() {
            AssociarVeiculoCommand command1 = new AssociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "OP-123", UUID.randomUUID(), "USER-001"
            );

            AssociarVeiculoCommand command2 = new AssociarVeiculoCommand(
                "VEI-002", "APO-002", LocalDate.now(), "OP-456", UUID.randomUUID(), "USER-002"
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
            AssociarVeiculoCommand command = new AssociarVeiculoCommand(
                "VEI-001", "APO-001", LocalDate.now(), "OP-123", UUID.randomUUID(), "USER-001"
            );

            String toString = command.toString();

            assertThat(toString).contains("AssociarVeiculoCommand");
            assertThat(toString).contains("VEI-001");
            assertThat(toString).contains("APO-001");
        }
    }
}
