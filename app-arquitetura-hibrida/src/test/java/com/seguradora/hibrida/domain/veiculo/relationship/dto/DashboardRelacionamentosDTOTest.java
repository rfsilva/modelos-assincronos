package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link DashboardRelacionamentosDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("DashboardRelacionamentosDTO - Testes Unitários")
class DashboardRelacionamentosDTOTest {

    @Nested
    @DisplayName("Testes de Criação com Builder")
    class CriacaoBuilderTests {

        @Test
        @DisplayName("Deve criar DTO com builder")
        void deveCriarDtoComBuilder() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(100L)
                .totalRelacionamentosEncerrados(50L)
                .totalRelacionamentosCancelados(10L)
                .totalVeiculosSemCobertura(5L)
                .totalVencendoEm30Dias(15L)
                .totalComGapCobertura(3L)
                .dataAtualizacao(LocalDate.now())
                .build();

            assertThat(dto).isNotNull();
            assertThat(dto.getTotalRelacionamentosAtivos()).isEqualTo(100L);
            assertThat(dto.getTotalRelacionamentosEncerrados()).isEqualTo(50L);
        }
    }

    @Nested
    @DisplayName("Testes de Método calcularTaxaCobertura")
    class CalcularTaxaCoberturaTests {

        @Test
        @DisplayName("Deve calcular taxa de cobertura corretamente")
        void deveCalcularTaxaCoberturaCorretamente() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(80L)
                .totalVeiculosSemCobertura(20L)
                .build();

            double taxa = dto.calcularTaxaCobertura();

            assertThat(taxa).isEqualTo(80.0);
        }

        @Test
        @DisplayName("Deve retornar 0 quando não há veículos")
        void deveRetornar0QuandoNaoHaVeiculos() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(0L)
                .totalVeiculosSemCobertura(0L)
                .build();

            assertThat(dto.calcularTaxaCobertura()).isZero();
        }

        @Test
        @DisplayName("Deve calcular 100% quando todos têm cobertura")
        void deveCalcular100PorcentoQuandoTodosTemCobertura() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(100L)
                .totalVeiculosSemCobertura(0L)
                .build();

            assertThat(dto.calcularTaxaCobertura()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("Testes de Método calcularTaxaCancelamento")
    class CalcularTaxaCancelamentoTests {

        @Test
        @DisplayName("Deve calcular taxa de cancelamento corretamente")
        void deveCalcularTaxaCancelamentoCorretamente() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(70L)
                .totalRelacionamentosEncerrados(20L)
                .totalRelacionamentosCancelados(10L)
                .build();

            double taxa = dto.calcularTaxaCancelamento();

            assertThat(taxa).isEqualTo(10.0);
        }

        @Test
        @DisplayName("Deve retornar 0 quando não há relacionamentos")
        void deveRetornar0QuandoNaoHaRelacionamentos() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(0L)
                .totalRelacionamentosEncerrados(0L)
                .totalRelacionamentosCancelados(0L)
                .build();

            assertThat(dto.calcularTaxaCancelamento()).isZero();
        }

        @Test
        @DisplayName("Deve calcular com apenas cancelados")
        void deveCalcularComApenasCancelados() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(0L)
                .totalRelacionamentosEncerrados(0L)
                .totalRelacionamentosCancelados(50L)
                .build();

            assertThat(dto.calcularTaxaCancelamento()).isEqualTo(100.0);
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar dashboard saudável")
        void deveRepresentarDashboardSaudavel() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(950L)
                .totalRelacionamentosEncerrados(40L)
                .totalRelacionamentosCancelados(10L)
                .totalVeiculosSemCobertura(5L)
                .totalVencendoEm30Dias(20L)
                .totalComGapCobertura(2L)
                .dataAtualizacao(LocalDate.now())
                .build();

            assertThat(dto.calcularTaxaCobertura()).isGreaterThan(95.0);
            assertThat(dto.calcularTaxaCancelamento()).isLessThan(2.0);
        }

        @Test
        @DisplayName("Deve representar dashboard com problemas")
        void deveRepresentarDashboardComProblemas() {
            DashboardRelacionamentosDTO dto = DashboardRelacionamentosDTO.builder()
                .totalRelacionamentosAtivos(60L)
                .totalRelacionamentosEncerrados(20L)
                .totalRelacionamentosCancelados(20L)
                .totalVeiculosSemCobertura(40L)
                .totalVencendoEm30Dias(30L)
                .totalComGapCobertura(15L)
                .dataAtualizacao(LocalDate.now())
                .build();

            assertThat(dto.calcularTaxaCobertura()).isLessThan(65.0);
            assertThat(dto.calcularTaxaCancelamento()).isGreaterThan(15.0);
            assertThat(dto.getTotalVeiculosSemCobertura()).isGreaterThan(30L);
        }
    }
}
