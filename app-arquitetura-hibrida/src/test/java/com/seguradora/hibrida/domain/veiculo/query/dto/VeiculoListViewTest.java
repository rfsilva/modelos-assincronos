package com.seguradora.hibrida.domain.veiculo.query.dto;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Year;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoListView}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoListView - Testes Unitários")
class VeiculoListViewTest {

    @Nested
    @DisplayName("Testes de Criação do Record")
    class CriacaoRecordTests {

        @Test
        @DisplayName("Deve criar view com todos os campos")
        void deveCriarViewComTodosCampos() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001",
                "ABC1234",
                "Honda",
                "Civic",
                2020,
                2021,
                "Branco",
                "João Silva",
                "12345678909",
                StatusVeiculo.ATIVO,
                true,
                "São Paulo",
                "SP",
                Instant.now(),
                Instant.now()
            );

            assertThat(view).isNotNull();
            assertThat(view.id()).isEqualTo("VEI-001");
            assertThat(view.placa()).isEqualTo("ABC1234");
            assertThat(view.marca()).isEqualTo("Honda");
            assertThat(view.modelo()).isEqualTo("Civic");
        }

        @Test
        @DisplayName("Deve permitir campos opcionais nulos")
        void devePermitirCamposOpcionaisNulos() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001",
                "ABC1234",
                "Honda",
                "Civic",
                2020,
                2021,
                null, // cor
                "João Silva",
                "12345678909",
                StatusVeiculo.ATIVO,
                false,
                null, // cidade
                null, // estado
                Instant.now(),
                Instant.now()
            );

            assertThat(view.cor()).isNull();
            assertThat(view.cidade()).isNull();
            assertThat(view.estado()).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Método getDescricaoCompleta")
    class GetDescricaoCompletaTests {

        @Test
        @DisplayName("Deve retornar descrição completa do veículo")
        void deveRetornarDescricaoCompletaVeiculo() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", Instant.now(), Instant.now()
            );

            String descricao = view.getDescricaoCompleta();

            assertThat(descricao).isEqualTo("Honda Civic 2020/2021 - ABC1234");
        }

        @Test
        @DisplayName("Deve incluir marca, modelo, anos e placa")
        void deveIncluirMarcaModeloAnosPlaca() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "XYZ9876", "Toyota", "Corolla", 2019, 2020,
                "Prata", "Maria Santos", "98765432100", StatusVeiculo.ATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            String descricao = view.getDescricaoCompleta();

            assertThat(descricao).contains("Toyota");
            assertThat(descricao).contains("Corolla");
            assertThat(descricao).contains("2019");
            assertThat(descricao).contains("2020");
            assertThat(descricao).contains("XYZ9876");
        }
    }

    @Nested
    @DisplayName("Testes de Método getIdade")
    class GetIdadeTests {

        @Test
        @DisplayName("Deve calcular idade do veículo corretamente")
        void deveCalcularIdadeVeiculoCorretamente() {
            int anoAtual = Year.now().getValue();

            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", anoAtual - 3, anoAtual - 2,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getIdade()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve retornar zero para veículo do ano atual")
        void deveRetornarZeroParaVeiculoAnoAtual() {
            int anoAtual = Year.now().getValue();

            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", anoAtual, anoAtual + 1,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getIdade()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular idade para veículo antigo")
        void deveCalcularIdadeParaVeiculoAntigo() {
            int anoAtual = Year.now().getValue();

            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Volkswagen", "Fusca", anoAtual - 15, anoAtual - 14,
                "Azul", "Pedro Costa", "11122233344", StatusVeiculo.ATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getIdade()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Testes de Método isAtivo")
    class IsAtivoTests {

        @Test
        @DisplayName("Deve retornar true quando status é ATIVO")
        void deveRetornarTrueQuandoStatusAtivo() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando status é INATIVO")
        void deveRetornarFalseQuandoStatusInativo() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.INATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando status é BLOQUEADO")
        void deveRetornarFalseQuandoStatusBloqueado() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.BLOQUEADO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método temApoliceAtiva")
    class TemApoliceAtivaTests {

        @Test
        @DisplayName("Deve retornar true quando apoliceAtiva é true")
        void deveRetornarTrueQuandoApoliceAtivaTrue() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.temApoliceAtiva()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando apoliceAtiva é false")
        void deveRetornarFalseQuandoApoliceAtivaFalse() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.temApoliceAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando apoliceAtiva é null")
        void deveRetornarFalseQuandoApoliceAtivaNull() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                null, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.temApoliceAtiva()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método getCpfFormatado")
    class GetCpfFormatadoTests {

        @Test
        @DisplayName("Deve formatar CPF corretamente")
        void deveFormatarCpfCorretamente() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            String cpfFormatado = view.getCpfFormatado();

            assertThat(cpfFormatado).isEqualTo("123.456.789-09");
        }

        @Test
        @DisplayName("Deve retornar CPF sem formatação se não tiver 11 dígitos")
        void deveRetornarCpfSemFormatacaoSeNaoTiver11Digitos() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "123456789", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            String cpfFormatado = view.getCpfFormatado();

            assertThat(cpfFormatado).isEqualTo("123456789");
        }

        @Test
        @DisplayName("Deve retornar null quando CPF é null")
        void deveRetornarNullQuandoCpfNull() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", null, StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getCpfFormatado()).isNull();
        }

        @Test
        @DisplayName("Deve formatar diferentes CPFs corretamente")
        void deveFormatarDiferentesCpfsCorretamente() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "98765432100", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getCpfFormatado()).isEqualTo("987.654.321-00");
        }
    }

    @Nested
    @DisplayName("Testes de Método getLocalizacaoCompleta")
    class GetLocalizacaoCompletaTests {

        @Test
        @DisplayName("Deve retornar localização completa com cidade e estado")
        void deveRetornarLocalizacaoCompletaComCidadeEstado() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", Instant.now(), Instant.now()
            );

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("São Paulo - SP");
        }

        @Test
        @DisplayName("Deve retornar apenas cidade quando estado é null")
        void deveRetornarApenasCidadeQuandoEstadoNull() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "Rio de Janeiro", null, Instant.now(), Instant.now()
            );

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("Rio de Janeiro");
        }

        @Test
        @DisplayName("Deve retornar apenas estado quando cidade é null")
        void deveRetornarApenasEstadoQuandoCidadeNull() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, "MG", Instant.now(), Instant.now()
            );

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("MG");
        }

        @Test
        @DisplayName("Deve retornar 'Não informado' quando cidade e estado são null")
        void deveRetornarNaoInformadoQuandoCidadeEstadoNull() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.getLocalizacaoCompleta()).isEqualTo("Não informado");
        }
    }

    @Nested
    @DisplayName("Testes de Igualdade e HashCode")
    class IgualdadeHashCodeTests {

        @Test
        @DisplayName("Records com mesmos valores devem ser iguais")
        void recordsComMesmosValoresDevemSerIguais() {
            Instant agora = Instant.now();

            VeiculoListView view1 = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", agora, agora
            );

            VeiculoListView view2 = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", agora, agora
            );

            assertThat(view1).isEqualTo(view2);
            assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
        }

        @Test
        @DisplayName("Records com valores diferentes não devem ser iguais")
        void recordsComValoresDiferentesNaoDevemSerIguais() {
            VeiculoListView view1 = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, null, null, Instant.now(), Instant.now()
            );

            VeiculoListView view2 = new VeiculoListView(
                "VEI-002", "XYZ9876", "Toyota", "Corolla", 2019, 2020,
                "Prata", "Maria Santos", "98765432100", StatusVeiculo.ATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view1).isNotEqualTo(view2);
        }
    }

    @Nested
    @DisplayName("Testes de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", Instant.now(), Instant.now()
            );

            String resultado = view.toString();

            assertThat(resultado).contains("VEI-001");
            assertThat(resultado).contains("ABC1234");
            assertThat(resultado).contains("Honda");
            assertThat(resultado).contains("Civic");
            assertThat(resultado).contains("João Silva");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários de Uso")
    class CenariosUsoTests {

        @Test
        @DisplayName("Deve representar veículo ativo com apólice")
        void deveRepresentarVeiculoAtivoComApolice() {
            VeiculoListView view = new VeiculoListView(
                "VEI-001", "ABC1234", "Honda", "Civic", 2020, 2021,
                "Branco", "João Silva", "12345678909", StatusVeiculo.ATIVO,
                true, "São Paulo", "SP", Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isTrue();
            assertThat(view.temApoliceAtiva()).isTrue();
            assertThat(view.getDescricaoCompleta()).contains("Honda Civic");
            assertThat(view.getLocalizacaoCompleta()).contains("São Paulo");
        }

        @Test
        @DisplayName("Deve representar veículo inativo sem apólice")
        void deveRepresentarVeiculoInativoSemApolice() {
            VeiculoListView view = new VeiculoListView(
                "VEI-002", "XYZ9876", "Fiat", "Uno", 2010, 2011,
                "Vermelho", "Pedro Costa", "11122233344", StatusVeiculo.INATIVO,
                false, null, null, Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isFalse();
            assertThat(view.temApoliceAtiva()).isFalse();
            assertThat(view.getIdade()).isGreaterThan(10);
            assertThat(view.getLocalizacaoCompleta()).isEqualTo("Não informado");
        }

        @Test
        @DisplayName("Deve representar veículo novo bloqueado")
        void deveRepresentarVeiculoNovoBloqueado() {
            int anoAtual = Year.now().getValue();

            VeiculoListView view = new VeiculoListView(
                "VEI-003", "DEF5678", "BMW", "X5", anoAtual - 1, anoAtual,
                "Preto", "Carlos Lima", "55566677788", StatusVeiculo.BLOQUEADO,
                true, "Curitiba", "PR", Instant.now(), Instant.now()
            );

            assertThat(view.isAtivo()).isFalse();
            assertThat(view.status()).isEqualTo(StatusVeiculo.BLOQUEADO);
            assertThat(view.temApoliceAtiva()).isTrue();
            assertThat(view.getIdade()).isEqualTo(1);
        }
    }
}
