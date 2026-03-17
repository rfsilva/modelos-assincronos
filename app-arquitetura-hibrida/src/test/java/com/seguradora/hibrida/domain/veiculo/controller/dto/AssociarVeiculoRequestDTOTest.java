package com.seguradora.hibrida.domain.veiculo.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AssociarVeiculoRequestDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AssociarVeiculoRequestDTO - Testes Unitários")
class AssociarVeiculoRequestDTOTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar DTO com todos os campos")
        void deveCriarDtoComTodosCampos() {
            // Arrange
            LocalDate dataInicio = LocalDate.now();

            // Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-001",
                dataInicio,
                "OP-001"
            );

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.apoliceId()).isEqualTo("APO-2024-001");
            assertThat(dto.dataInicio()).isEqualTo(dataInicio);
            assertThat(dto.operadorId()).isEqualTo("OP-001");
        }

        @Test
        @DisplayName("Deve criar DTO com data futura")
        void deveCriarDtoComDataFutura() {
            // Arrange
            LocalDate dataFutura = LocalDate.now().plusDays(10);

            // Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-002",
                dataFutura,
                "OP-002"
            );

            // Assert
            assertThat(dto.dataInicio()).isEqualTo(dataFutura);
            assertThat(dto.isDataInicioFutura()).isTrue();
        }

        @Test
        @DisplayName("Deve criar DTO com data de hoje")
        void deveCriarDtoComDataDeHoje() {
            // Arrange
            LocalDate hoje = LocalDate.now();

            // Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-003",
                hoje,
                "OP-003"
            );

            // Assert
            assertThat(dto.dataInicio()).isEqualTo(hoje);
            assertThat(dto.isDataInicioHoje()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Data Futura")
    class ValidacaoDataFuturaTests {

        @Test
        @DisplayName("Deve identificar data futura corretamente")
        void deveIdentificarDataFuturaCorretamente() {
            // Arrange
            LocalDate dataFutura = LocalDate.now().plusDays(5);
            AssociarVeiculoRequestDTO dto = criarDtoComData(dataFutura);

            // Act & Assert
            assertThat(dto.isDataInicioFutura()).isTrue();
        }

        @Test
        @DisplayName("Data de hoje não é futura")
        void dataDeHojeNaoEFutura() {
            // Arrange
            LocalDate hoje = LocalDate.now();
            AssociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataInicioFutura()).isFalse();
        }

        @Test
        @DisplayName("Data passada não é futura")
        void dataPassadaNaoEFutura() {
            // Arrange
            LocalDate passado = LocalDate.now().minusDays(5);
            AssociarVeiculoRequestDTO dto = criarDtoComData(passado);

            // Act & Assert
            assertThat(dto.isDataInicioFutura()).isFalse();
        }

        @Test
        @DisplayName("Data amanhã é futura")
        void dataAmanhaEFutura() {
            // Arrange
            LocalDate amanha = LocalDate.now().plusDays(1);
            AssociarVeiculoRequestDTO dto = criarDtoComData(amanha);

            // Act & Assert
            assertThat(dto.isDataInicioFutura()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Data Hoje")
    class ValidacaoDataHojeTests {

        @Test
        @DisplayName("Deve identificar data de hoje corretamente")
        void deveIdentificarDataDeHojeCorretamente() {
            // Arrange
            LocalDate hoje = LocalDate.now();
            AssociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataInicioHoje()).isTrue();
        }

        @Test
        @DisplayName("Data de ontem não é hoje")
        void dataDeOntemNaoEHoje() {
            // Arrange
            LocalDate ontem = LocalDate.now().minusDays(1);
            AssociarVeiculoRequestDTO dto = criarDtoComData(ontem);

            // Act & Assert
            assertThat(dto.isDataInicioHoje()).isFalse();
        }

        @Test
        @DisplayName("Data de amanhã não é hoje")
        void dataDeAmanhaNaoEHoje() {
            // Arrange
            LocalDate amanha = LocalDate.now().plusDays(1);
            AssociarVeiculoRequestDTO dto = criarDtoComData(amanha);

            // Act & Assert
            assertThat(dto.isDataInicioHoje()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Data Válida")
    class ValidacaoDataValidaTests {

        @Test
        @DisplayName("Data de hoje é válida")
        void dataDeHojeEValida() {
            // Arrange
            LocalDate hoje = LocalDate.now();
            AssociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Data futura é válida")
        void dataFuturaEValida() {
            // Arrange
            LocalDate futura = LocalDate.now().plusDays(10);
            AssociarVeiculoRequestDTO dto = criarDtoComData(futura);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Data de ontem é válida")
        void dataDeOntemEValida() {
            // Arrange
            LocalDate ontem = LocalDate.now().minusDays(1);
            AssociarVeiculoRequestDTO dto = criarDtoComData(ontem);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 29 dias atrás é válida")
        void data29DiasAtrasEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(29);
            AssociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 30 dias atrás é válida (limite)")
        void data30DiasAtrasEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(30);
            AssociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 31 dias atrás não é válida")
        void data31DiasAtrasNaoEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(31);
            AssociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isFalse();
        }

        @Test
        @DisplayName("Data muito antiga não é válida")
        void dataMuitoAntigaNaoEValida() {
            // Arrange
            LocalDate dataMuitoAntiga = LocalDate.now().minusDays(365);
            AssociarVeiculoRequestDTO dto = criarDtoComData(dataMuitoAntiga);

            // Act & Assert
            assertThat(dto.isDataInicioValida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Records")
    class RecordsTests {

        @Test
        @DisplayName("Deve ter equals correto")
        void deveTermEqualsCorreto() {
            // Arrange
            LocalDate data = LocalDate.of(2024, 6, 15);
            AssociarVeiculoRequestDTO dto1 = new AssociarVeiculoRequestDTO(
                "APO-001", data, "OP-001"
            );
            AssociarVeiculoRequestDTO dto2 = new AssociarVeiculoRequestDTO(
                "APO-001", data, "OP-001"
            );

            // Act & Assert
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString legível")
        void deveTermToStringLegivel() {
            // Arrange
            LocalDate data = LocalDate.of(2024, 6, 15);
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-001", data, "OP-SUPERVISOR"
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertThat(toString).contains("APO-2024-001");
            assertThat(toString).contains("OP-SUPERVISOR");
            assertThat(toString).contains("2024-06-15");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar associação imediata")
        void deveRepresentarAssociacaoImediata() {
            // Arrange & Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-NOVO",
                LocalDate.now(),
                "OP-VENDAS"
            );

            // Assert
            assertThat(dto.apoliceId()).startsWith("APO-");
            assertThat(dto.isDataInicioHoje()).isTrue();
            assertThat(dto.isDataInicioFutura()).isFalse();
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Deve representar associação agendada")
        void deveRepresentarAssociacaoAgendada() {
            // Arrange & Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-AGENDADA",
                LocalDate.now().plusDays(15),
                "OP-AGENDAMENTO"
            );

            // Assert
            assertThat(dto.isDataInicioFutura()).isTrue();
            assertThat(dto.isDataInicioHoje()).isFalse();
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Deve representar associação retroativa válida")
        void deveRepresentarAssociacaoRetroativaValida() {
            // Arrange & Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-RETROATIVA",
                LocalDate.now().minusDays(15),
                "OP-RETROATIVO"
            );

            // Assert
            assertThat(dto.isDataInicioFutura()).isFalse();
            assertThat(dto.isDataInicioHoje()).isFalse();
            assertThat(dto.isDataInicioValida()).isTrue();
        }

        @Test
        @DisplayName("Deve representar associação retroativa inválida")
        void deveRepresentarAssociacaoRetroativaInvalida() {
            // Arrange & Act
            AssociarVeiculoRequestDTO dto = new AssociarVeiculoRequestDTO(
                "APO-2024-INVALIDA",
                LocalDate.now().minusDays(60),
                "OP-ERRO"
            );

            // Assert
            assertThat(dto.isDataInicioValida()).isFalse();
        }
    }

    // === Métodos auxiliares ===

    private AssociarVeiculoRequestDTO criarDtoComData(LocalDate data) {
        return new AssociarVeiculoRequestDTO("APO-TEST", data, "OP-TEST");
    }
}
