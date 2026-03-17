package com.seguradora.hibrida.domain.veiculo.controller.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link DesassociarVeiculoRequestDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("DesassociarVeiculoRequestDTO - Testes Unitários")
class DesassociarVeiculoRequestDTOTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar DTO com todos os campos")
        void deveCriarDtoComTodosCampos() {
            // Arrange
            LocalDate dataFim = LocalDate.now();

            // Act
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-001",
                dataFim,
                "Cancelamento por solicitação do cliente",
                "OP-001"
            );

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.apoliceId()).isEqualTo("APO-2024-001");
            assertThat(dto.dataFim()).isEqualTo(dataFim);
            assertThat(dto.motivo()).contains("Cancelamento");
            assertThat(dto.operadorId()).isEqualTo("OP-001");
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
            DesassociarVeiculoRequestDTO dto = criarDtoComData(dataFutura);

            // Act & Assert
            assertThat(dto.isDataFimFutura()).isTrue();
        }

        @Test
        @DisplayName("Data de hoje não é futura")
        void dataDeHojeNaoEFutura() {
            // Arrange
            LocalDate hoje = LocalDate.now();
            DesassociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataFimFutura()).isFalse();
        }

        @Test
        @DisplayName("Data passada não é futura")
        void dataPassadaNaoEFutura() {
            // Arrange
            LocalDate passado = LocalDate.now().minusDays(5);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(passado);

            // Act & Assert
            assertThat(dto.isDataFimFutura()).isFalse();
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
            DesassociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataFimHoje()).isTrue();
        }

        @Test
        @DisplayName("Data de ontem não é hoje")
        void dataDeOntemNaoEHoje() {
            // Arrange
            LocalDate ontem = LocalDate.now().minusDays(1);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(ontem);

            // Act & Assert
            assertThat(dto.isDataFimHoje()).isFalse();
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
            DesassociarVeiculoRequestDTO dto = criarDtoComData(hoje);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isTrue();
        }

        @Test
        @DisplayName("Data de ontem é válida")
        void dataDeOntemEValida() {
            // Arrange
            LocalDate ontem = LocalDate.now().minusDays(1);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(ontem);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 6 dias atrás é válida")
        void data6DiasAtrasEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(6);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 7 dias atrás é válida (limite)")
        void data7DiasAtrasEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(7);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isTrue();
        }

        @Test
        @DisplayName("Data de 8 dias atrás não é válida")
        void data8DiasAtrasNaoEValida() {
            // Arrange
            LocalDate data = LocalDate.now().minusDays(8);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(data);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isFalse();
        }

        @Test
        @DisplayName("Data futura é válida")
        void dataFuturaEValida() {
            // Arrange
            LocalDate futura = LocalDate.now().plusDays(10);
            DesassociarVeiculoRequestDTO dto = criarDtoComData(futura);

            // Act & Assert
            assertThat(dto.isDataFimValida()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Categorização de Motivo")
    class CategorizacaoMotivoTests {

        @Test
        @DisplayName("Deve categorizar como CANCELAMENTO")
        void deveCategorizarComoCancelamento() {
            assertThat(criarDtoComMotivo("Cancelamento da apólice").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
            assertThat(criarDtoComMotivo("Cliente solicitou cancelar").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
            assertThat(criarDtoComMotivo("CANCELAMENTO TOTAL").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
        }

        @Test
        @DisplayName("Deve categorizar como VENDA_VEICULO")
        void deveCategorizarComoVendaVeiculo() {
            assertThat(criarDtoComMotivo("Venda do veículo").categorizarMotivo())
                .isEqualTo("VENDA_VEICULO");
            assertThat(criarDtoComMotivo("Veículo foi vendido").categorizarMotivo())
                .isEqualTo("VENDA_VEICULO");
            assertThat(criarDtoComMotivo("VENDA REALIZADA").categorizarMotivo())
                .isEqualTo("VENDA_VEICULO");
        }

        @Test
        @DisplayName("Deve categorizar como SINISTRO_TOTAL")
        void deveCategorizarComoSinistroTotal() {
            assertThat(criarDtoComMotivo("Sinistro total").categorizarMotivo())
                .isEqualTo("SINISTRO_TOTAL");
            assertThat(criarDtoComMotivo("Acidente com perda total").categorizarMotivo())
                .isEqualTo("SINISTRO_TOTAL");
            assertThat(criarDtoComMotivo("SINISTRO GRAVE").categorizarMotivo())
                .isEqualTo("SINISTRO_TOTAL");
        }

        @Test
        @DisplayName("Deve categorizar como TRANSFERENCIA")
        void deveCategorizarComoTransferencia() {
            assertThat(criarDtoComMotivo("Transferencia de propriedade").categorizarMotivo())
                .isEqualTo("TRANSFERENCIA");
            assertThat(criarDtoComMotivo("Transferir para outro proprietario").categorizarMotivo())
                .isEqualTo("TRANSFERENCIA");
            assertThat(criarDtoComMotivo("Processo de transferencia do veiculo").categorizarMotivo())
                .isEqualTo("TRANSFERENCIA");
        }

        @Test
        @DisplayName("Deve categorizar como OUTROS para motivos não identificados")
        void deveCategorizarComoOutrosParaMotivoNaoIdentificado() {
            assertThat(criarDtoComMotivo("Motivo específico do cliente").categorizarMotivo())
                .isEqualTo("OUTROS");
            assertThat(criarDtoComMotivo("Razões pessoais").categorizarMotivo())
                .isEqualTo("OUTROS");
            assertThat(criarDtoComMotivo("Não desejo mais o seguro").categorizarMotivo())
                .isEqualTo("OUTROS");
        }

        @Test
        @DisplayName("Categorização deve ser case-insensitive")
        void categorizacaoDeveSerCaseInsensitive() {
            assertThat(criarDtoComMotivo("CANCELAMENTO").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
            assertThat(criarDtoComMotivo("cancelamento").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
            assertThat(criarDtoComMotivo("Cancelamento").categorizarMotivo())
                .isEqualTo("CANCELAMENTO");
        }

        @Test
        @DisplayName("Deve identificar múltiplas palavras-chave no motivo")
        void deveIdentificarMultiplasPalavrasChaveNoMotivo() {
            // Quando há múltiplas palavras-chave, a primeira encontrada prevalece
            String motivo = "Cancelamento devido a venda do veículo";
            assertThat(criarDtoComMotivo(motivo).categorizarMotivo())
                .isIn("CANCELAMENTO", "VENDA_VEICULO"); // Aceita qualquer uma das duas
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
            DesassociarVeiculoRequestDTO dto1 = new DesassociarVeiculoRequestDTO(
                "APO-001", data, "Cancelamento da apólice", "OP-001"
            );
            DesassociarVeiculoRequestDTO dto2 = new DesassociarVeiculoRequestDTO(
                "APO-001", data, "Cancelamento da apólice", "OP-001"
            );

            // Act & Assert
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString legível")
        void deveTermToStringLegivel() {
            // Arrange
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-001",
                LocalDate.of(2024, 6, 15),
                "Cancelamento por solicitação",
                "OP-SUPERVISOR"
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertThat(toString).contains("APO-2024-001");
            assertThat(toString).contains("Cancelamento");
            assertThat(toString).contains("OP-SUPERVISOR");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar desassociação imediata por cancelamento")
        void deveRepresentarDesassociacaoImediataPorCancelamento() {
            // Arrange & Act
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-CANCEL",
                LocalDate.now(),
                "Cancelamento da apólice por solicitação do cliente",
                "OP-ATENDIMENTO"
            );

            // Assert
            assertThat(dto.isDataFimHoje()).isTrue();
            assertThat(dto.isDataFimValida()).isTrue();
            assertThat(dto.categorizarMotivo()).isEqualTo("CANCELAMENTO");
        }

        @Test
        @DisplayName("Deve representar desassociação por venda")
        void deveRepresentarDesassociacaoPorVenda() {
            // Arrange & Act
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-VENDA",
                LocalDate.now().minusDays(3),
                "Venda do veículo para terceiro",
                "OP-VENDAS"
            );

            // Assert
            assertThat(dto.isDataFimValida()).isTrue();
            assertThat(dto.categorizarMotivo()).isEqualTo("VENDA_VEICULO");
        }

        @Test
        @DisplayName("Deve representar desassociação por sinistro")
        void deveRepresentarDesassociacaoPorSinistro() {
            // Arrange & Act
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-SINISTRO",
                LocalDate.now().minusDays(1),
                "Sinistro total com perda completa do veículo",
                "OP-SINISTROS"
            );

            // Assert
            assertThat(dto.isDataFimValida()).isTrue();
            assertThat(dto.categorizarMotivo()).isEqualTo("SINISTRO_TOTAL");
        }

        @Test
        @DisplayName("Deve representar desassociação agendada")
        void deveRepresentarDesassociacaoAgendada() {
            // Arrange & Act
            DesassociarVeiculoRequestDTO dto = new DesassociarVeiculoRequestDTO(
                "APO-2024-FUTURO",
                LocalDate.now().plusDays(15),
                "Cancelamento agendado conforme solicitado",
                "OP-AGENDAMENTO"
            );

            // Assert
            assertThat(dto.isDataFimFutura()).isTrue();
            assertThat(dto.isDataFimValida()).isTrue();
        }
    }

    // === Métodos auxiliares ===

    private DesassociarVeiculoRequestDTO criarDtoComData(LocalDate data) {
        return new DesassociarVeiculoRequestDTO(
            "APO-TEST",
            data,
            "Motivo de teste para desassociação",
            "OP-TEST"
        );
    }

    private DesassociarVeiculoRequestDTO criarDtoComMotivo(String motivo) {
        return new DesassociarVeiculoRequestDTO(
            "APO-TEST",
            LocalDate.now(),
            motivo,
            "OP-TEST"
        );
    }
}
