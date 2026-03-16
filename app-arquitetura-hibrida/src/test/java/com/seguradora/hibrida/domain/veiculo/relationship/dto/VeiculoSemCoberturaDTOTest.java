package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoSemCoberturaDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoSemCoberturaDTO - Testes Unitários")
class VeiculoSemCoberturaDTOTest {

    @Nested
    @DisplayName("Testes de Criação com Builder")
    class CriacaoBuilderTests {

        @Test
        @DisplayName("Deve criar DTO com builder")
        void deveCriarDtoComBuilder() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .seguradoCpf("12345678909")
                .seguradoNome("João Silva")
                .ultimaApolice("APO-2024-001")
                .dataFimUltimaCobertura(LocalDate.now().minusDays(10))
                .diasSemCobertura(10)
                .build();

            assertThat(dto).isNotNull();
            assertThat(dto.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(dto.getDiasSemCobertura()).isEqualTo(10);
        }
    }

    @Nested
    @DisplayName("Testes de Método isSituacaoCritica")
    class SituacaoCriticaTests {

        @Test
        @DisplayName("Deve ser crítica com mais de 30 dias")
        void deveSerCriticaComMaisDe30Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(45)
                .build();

            assertThat(dto.isSituacaoCritica()).isTrue();
        }

        @Test
        @DisplayName("Não deve ser crítica com 30 dias ou menos")
        void naoDeveSerCriticaCom30DiasOuMenos() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(30)
                .build();

            assertThat(dto.isSituacaoCritica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser crítica com menos de 30 dias")
        void naoDeveSerCriticaComMenosDe30Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(15)
                .build();

            assertThat(dto.isSituacaoCritica()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método requerAcaoUrgente")
    class RequerAcaoUrgenteTests {

        @Test
        @DisplayName("Deve requerer ação urgente com mais de 7 dias")
        void deveRequererAcaoUrgenteComMaisDe7Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(10)
                .build();

            assertThat(dto.requerAcaoUrgente()).isTrue();
        }

        @Test
        @DisplayName("Não deve requerer ação urgente com 7 dias ou menos")
        void naoDeveRequererAcaoUrgenteCom7DiasOuMenos() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(7)
                .build();

            assertThat(dto.requerAcaoUrgente()).isFalse();
        }

        @Test
        @DisplayName("Não deve requerer ação urgente com poucos dias")
        void naoDeveRequererAcaoUrgenteComPoucosDias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(3)
                .build();

            assertThat(dto.requerAcaoUrgente()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Combinados")
    class CenariosCombinadosTests {

        @Test
        @DisplayName("Situação normal - até 7 dias")
        void situacaoNormalAte7Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .diasSemCobertura(5)
                .build();

            assertThat(dto.isSituacaoCritica()).isFalse();
            assertThat(dto.requerAcaoUrgente()).isFalse();
        }

        @Test
        @DisplayName("Situação urgente - 8 a 30 dias")
        void situacaoUrgente8A30Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .diasSemCobertura(15)
                .build();

            assertThat(dto.isSituacaoCritica()).isFalse();
            assertThat(dto.requerAcaoUrgente()).isTrue();
        }

        @Test
        @DisplayName("Situação crítica - mais de 30 dias")
        void situacaoCriticaMaisDe30Dias() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .diasSemCobertura(45)
                .build();

            assertThat(dto.isSituacaoCritica()).isTrue();
            assertThat(dto.requerAcaoUrgente()).isTrue();
        }

        @Test
        @DisplayName("Limite exato - 31 dias deve ser crítico")
        void limiteExato31DiasDeveSerCritico() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(31)
                .build();

            assertThat(dto.isSituacaoCritica()).isTrue();
            assertThat(dto.requerAcaoUrgente()).isTrue();
        }

        @Test
        @DisplayName("Limite exato - 8 dias deve requerer ação")
        void limiteExato8DiasDeveRequererAcao() {
            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .diasSemCobertura(8)
                .build();

            assertThat(dto.isSituacaoCritica()).isFalse();
            assertThat(dto.requerAcaoUrgente()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Dados Completos")
    class DadosCompletosTests {

        @Test
        @DisplayName("Deve armazenar todos os dados do veículo")
        void deveArmazenarTodosDadosVeiculo() {
            LocalDate dataFim = LocalDate.of(2024, 1, 15);

            VeiculoSemCoberturaDTO dto = VeiculoSemCoberturaDTO.builder()
                .veiculoId("VEI-001")
                .placa("ABC1234")
                .seguradoCpf("12345678909")
                .seguradoNome("João Silva")
                .ultimaApolice("APO-2024-001234")
                .dataFimUltimaCobertura(dataFim)
                .diasSemCobertura(20)
                .build();

            assertThat(dto.getVeiculoId()).isEqualTo("VEI-001");
            assertThat(dto.getPlaca()).isEqualTo("ABC1234");
            assertThat(dto.getSeguradoCpf()).isEqualTo("12345678909");
            assertThat(dto.getSeguradoNome()).isEqualTo("João Silva");
            assertThat(dto.getUltimaApolice()).isEqualTo("APO-2024-001234");
            assertThat(dto.getDataFimUltimaCobertura()).isEqualTo(dataFim);
            assertThat(dto.getDiasSemCobertura()).isEqualTo(20);
        }
    }
}
