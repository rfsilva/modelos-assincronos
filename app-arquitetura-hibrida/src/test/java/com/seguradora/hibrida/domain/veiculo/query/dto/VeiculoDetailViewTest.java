package com.seguradora.hibrida.domain.veiculo.query.dto;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Year;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoDetailView}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoDetailView - Testes Unitários")
class VeiculoDetailViewTest {

    @Nested
    @DisplayName("Testes de Criação do Record")
    class CriacaoRecordTests {

        @Test
        @DisplayName("Deve criar view detalhada com todos os campos")
        void deveCriarViewDetalhadaComTodosCampos() {
            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                "São Paulo", "SP", "SUDESTE", 1L, 123L,
                Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view).isNotNull();
            assertThat(view.id()).isEqualTo("VEI-001");
            assertThat(view.placa()).isEqualTo("ABC1234");
            assertThat(view.renavam()).isEqualTo("12345678900");
            assertThat(view.chassi()).isEqualTo("1HGBH41J6MN109186");
        }
    }

    @Nested
    @DisplayName("Testes de Método getDescricaoCompleta")
    class GetDescricaoCompletaTests {

        @Test
        @DisplayName("Deve retornar descrição completa")
        void deveRetornarDescricaoCompleta() {
            VeiculoDetailView view = criarViewPadrao();

            assertThat(view.getDescricaoCompleta()).isEqualTo("Honda Civic 2020/2021 - ABC1234");
        }
    }

    @Nested
    @DisplayName("Testes de Método getIdade")
    class GetIdadeTests {

        @Test
        @DisplayName("Deve calcular idade corretamente")
        void deveCalcularIdadeCorretamente() {
            int anoAtual = Year.now().getValue();

            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", anoAtual - 3, anoAtual - 2, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.getIdade()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos Booleanos")
    class MetodosBooleanosTests {

        @Test
        @DisplayName("isAtivo deve retornar true para ATIVO")
        void isAtivoDeveRetornarTrueParaAtivo() {
            VeiculoDetailView view = criarViewPadrao();
            assertThat(view.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("temApoliceAtiva deve retornar true quando ativo")
        void temApoliceAtivaDeveRetornarTrueQuandoAtivo() {
            VeiculoDetailView view = criarViewPadrao();
            assertThat(view.temApoliceAtiva()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Método getCpfCnpjFormatado")
    class GetCpfCnpjFormatadoTests {

        @Test
        @DisplayName("Deve formatar CPF corretamente")
        void deveFormatarCpfCorretamente() {
            VeiculoDetailView view = criarViewPadrao();

            assertThat(view.getCpfCnpjFormatado()).isEqualTo("123.456.789-09");
        }

        @Test
        @DisplayName("Deve formatar CNPJ corretamente")
        void deveFormatarCnpjCorretamente() {
            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "Empresa XYZ", "11222333000181", "JURIDICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.getCpfCnpjFormatado()).isEqualTo("11.222.333/0001-81");
        }

        @Test
        @DisplayName("Deve retornar sem formatação quando inválido")
        void deveRetornarSemFormatacaoQuandoInvalido() {
            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "123", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.getCpfCnpjFormatado()).isEqualTo("123");
        }
    }

    @Nested
    @DisplayName("Testes de Método getLocalizacaoCompleta")
    class GetLocalizacaoCompletaTests {

        @Test
        @DisplayName("Deve retornar localização completa")
        void deveRetornarLocalizacaoCompleta() {
            VeiculoDetailView view = criarViewPadrao();

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("São Paulo - SP");
        }

        @Test
        @DisplayName("Deve retornar 'Não informado' quando null")
        void deveRetornarNaoInformadoQuandoNull() {
            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("Não informado");
        }
    }

    @Nested
    @DisplayName("Testes de Método getNumeroApolicesAtivas")
    class GetNumeroApolicesAtivasTests {

        @Test
        @DisplayName("Deve retornar 0 quando lista é null")
        void deveRetornar0QuandoListaNull() {
            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                null, Collections.emptyList()
            );

            assertThat(view.getNumeroApolicesAtivas()).isZero();
        }

        @Test
        @DisplayName("Deve contar apólices ativas")
        void deveContarApolicesAtivas() {
            List<VeiculoDetailView.ApoliceAssociadaView> apolices = List.of(
                new VeiculoDetailView.ApoliceAssociadaView(
                    "APO-001", "123", "ATIVA", true, Instant.now(), null, "COMPREENSIVA"
                ),
                new VeiculoDetailView.ApoliceAssociadaView(
                    "APO-002", "456", "INATIVA", false, Instant.now(), Instant.now(), "COMPREENSIVA"
                )
            );

            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                apolices, Collections.emptyList()
            );

            assertThat(view.getNumeroApolicesAtivas()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Testes de Classificação de Idade")
    class ClassificacaoIdadeTests {

        @Test
        @DisplayName("Veículo novo tem até 3 anos")
        void veiculoNovoTemAte3Anos() {
            int anoAtual = Year.now().getValue();

            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", anoAtual - 2, anoAtual - 1, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.isVeiculoNovo()).isTrue();
            assertThat(view.isVeiculoSeminovo()).isFalse();
            assertThat(view.isVeiculoUsado()).isFalse();
        }

        @Test
        @DisplayName("Veículo seminovo tem 4 a 10 anos")
        void veiculoSeminovoTem4A10Anos() {
            int anoAtual = Year.now().getValue();

            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", anoAtual - 5, anoAtual - 4, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.isVeiculoNovo()).isFalse();
            assertThat(view.isVeiculoSeminovo()).isTrue();
            assertThat(view.isVeiculoUsado()).isFalse();
        }

        @Test
        @DisplayName("Veículo usado tem mais de 10 anos")
        void veiculoUsadoTemMaisDe10Anos() {
            int anoAtual = Year.now().getValue();

            VeiculoDetailView view = new VeiculoDetailView(
                "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", anoAtual - 12, anoAtual - 11, "Branco", "FLEX", "PASSEIO", 1600,
                "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
                null, null, null, 1L, 1L, Instant.now(), Instant.now(),
                Collections.emptyList(), Collections.emptyList()
            );

            assertThat(view.isVeiculoNovo()).isFalse();
            assertThat(view.isVeiculoSeminovo()).isFalse();
            assertThat(view.isVeiculoUsado()).isTrue();
        }
    }

    private VeiculoDetailView criarViewPadrao() {
        return new VeiculoDetailView(
            "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
            "Honda", "Civic", 2020, 2021, "Branco", "FLEX", "PASSEIO", 1600,
            "João Silva", "12345678909", "FISICA", StatusVeiculo.ATIVO, true,
            "São Paulo", "SP", "SUDESTE", 1L, 123L,
            Instant.now(), Instant.now(),
            Collections.emptyList(), Collections.emptyList()
        );
    }
}
