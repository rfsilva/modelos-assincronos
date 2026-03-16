package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AtualizarVeiculoCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AtualizarVeiculoCommand - Testes Unitários")
class AtualizarVeiculoCommandTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar comando com dados válidos")
        void deveCriarComandoComDadosValidos() {
            // Arrange
            Especificacao especificacao = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                          CategoriaVeiculo.PASSEIO, 2000);

            // Act
            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                "VEI-001", especificacao, "OP-123", 1L, UUID.randomUUID(), "USER-001"
            );

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getCommandId()).isNotNull();
            assertThat(command.getTimestamp()).isNotNull();
            assertThat(command.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(command.getNovaEspecificacao()).isEqualTo(especificacao);
            assertThat(command.getOperadorId()).isEqualTo("OP-123");
            assertThat(command.getVersaoEsperada()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve criar comando sem versão esperada")
        void deveCriarComandoSemVersaoEsperada() {
            Especificacao especificacao = Especificacao.exemplo();

            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                "VEI-001", especificacao, "OP-123", null, UUID.randomUUID(), "USER-001"
            );

            assertThat(command.getVersaoEsperada()).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Comandos com mesmo ID devem ser iguais")
        void comandosComMesmoIdDevemSerIguais() {
            Especificacao especificacao = Especificacao.exemplo();

            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                "VEI-001", especificacao, "OP-123", 1L, UUID.randomUUID(), "USER-001"
            );

            assertThat(command).isEqualTo(command);
            assertThat(command.hashCode()).isEqualTo(command.hashCode());
        }

        @Test
        @DisplayName("Comandos diferentes não devem ser iguais")
        void comandosDiferentesNaoDevemSerIguais() {
            Especificacao especificacao = Especificacao.exemplo();

            AtualizarVeiculoCommand command1 = new AtualizarVeiculoCommand(
                "VEI-001", especificacao, "OP-123", 1L, UUID.randomUUID(), "USER-001"
            );

            AtualizarVeiculoCommand command2 = new AtualizarVeiculoCommand(
                "VEI-002", especificacao, "OP-456", 2L, UUID.randomUUID(), "USER-002"
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
            Especificacao especificacao = Especificacao.exemplo();

            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                "VEI-001", especificacao, "OP-123", 1L, UUID.randomUUID(), "USER-001"
            );

            String toString = command.toString();

            assertThat(toString).contains("AtualizarVeiculoCommand");
            assertThat(toString).contains("VEI-001");
        }
    }
}
