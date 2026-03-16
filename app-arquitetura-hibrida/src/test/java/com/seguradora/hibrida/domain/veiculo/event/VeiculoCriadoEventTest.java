package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoCriadoEvent}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoCriadoEvent - Testes Unitários")
class VeiculoCriadoEventTest {

    private static final String AGGREGATE_ID = "VEI-001";
    private static final String OPERADOR_ID = "OP-123";

    @Nested
    @DisplayName("Testes de Criação do Evento")
    class CriacaoEventoTests {

        @Test
        @DisplayName("Deve criar evento com dados válidos")
        void deveCriarEventoComDadosValidos() {
            // Arrange
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            // Act
            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(evento.getVersion()).isEqualTo(1L);
            assertThat(evento.getPlaca()).isEqualTo("ABC1234");
            assertThat(evento.getRenavam()).isEqualTo("12345678900");
            assertThat(evento.getChassi()).isEqualTo("1HGBH41J6MN109186");
            assertThat(evento.getMarca()).isEqualTo("Honda");
            assertThat(evento.getModelo()).isEqualTo("Civic");
            assertThat(evento.getAnoFabricacao()).isEqualTo(2023);
            assertThat(evento.getAnoModelo()).isEqualTo(2024);
            assertThat(evento.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(evento.getEventType()).isEqualTo("VeiculoCriado");
        }

        @Test
        @DisplayName("Deve lançar exceção para placa nula")
        void deveLancarExcecaoParaPlacaNula() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, null, "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa não pode ser nula ou vazia");
        }

        @Test
        @DisplayName("Deve lançar exceção para placa vazia")
        void deveLancarExcecaoParaPlacaVazia() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "   ", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Placa não pode ser nula ou vazia");
        }

        @Test
        @DisplayName("Deve lançar exceção para renavam nulo")
        void deveLancarExcecaoParaRenavamNulo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", null, "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("RENAVAM não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para chassi nulo")
        void deveLancarExcecaoParaChassiNulo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", null,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Chassi não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para marca nula")
        void deveLancarExcecaoParaMarcaNula() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                null, "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Marca não pode ser nula ou vazia");
        }

        @Test
        @DisplayName("Deve lançar exceção para marca muito longa")
        void deveLancarExcecaoParaMarcaMuitoLonga() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();
            String marcaLonga = "A".repeat(51);

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                marcaLonga, "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ter mais de 50 caracteres");
        }

        @Test
        @DisplayName("Deve lançar exceção para modelo nulo")
        void deveLancarExcecaoParaModeloNulo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", null, 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Modelo não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para modelo muito longo")
        void deveLancarExcecaoParaModeloMuitoLongo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();
            String modeloLongo = "A".repeat(101);

            assertThatThrownBy(() -> VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", modeloLongo, 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não pode ter mais de 100 caracteres");
        }
    }

    @Nested
    @DisplayName("Testes de Normalização")
    class NormalizacaoTests {

        @Test
        @DisplayName("Deve normalizar placa para uppercase")
        void deveNormalizarPlacaParaUppercase() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "abc1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento.getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve remover caracteres não numéricos do RENAVAM")
        void deveRemoverCaracteresNaoNumericosDoRenavam() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "123.456.789-01", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento.getRenavam()).isEqualTo("12345678901");
        }

        @Test
        @DisplayName("Deve normalizar chassi para uppercase")
        void deveNormalizarChassiParaUppercase() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1hgbh41j6mn109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento.getChassi()).isEqualTo("1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve remover espaços extras da marca")
        void deveRemoverEspacosExtrasDaMarca() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "  Honda  ", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento.getMarca()).isEqualTo("Honda");
        }
    }

    @Nested
    @DisplayName("Testes de Métodos Auxiliares")
    class MetodosAuxiliaresTests {

        @Test
        @DisplayName("Deve retornar especificação reconstruída")
        void deveRetornarEspecificacaoReconstruida() {
            Especificacao especificacao = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                          CategoriaVeiculo.PASSEIO, 1600);
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            Especificacao especificacaoReconstruida = evento.getEspecificacao();

            assertThat(especificacaoReconstruida).isNotNull();
            assertThat(especificacaoReconstruida.getCor()).isEqualTo("Branco");
            assertThat(especificacaoReconstruida.getTipoCombustivel()).isEqualTo(TipoCombustivel.FLEX);
            assertThat(especificacaoReconstruida.getCategoria()).isEqualTo(CategoriaVeiculo.PASSEIO);
            assertThat(especificacaoReconstruida.getCilindrada()).isEqualTo(1600);
        }

        @Test
        @DisplayName("Deve retornar proprietário reconstruído")
        void deveRetornarProprietarioReconstruido() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            Proprietario proprietarioReconstruido = evento.getProprietario();

            assertThat(proprietarioReconstruido).isNotNull();
            assertThat(proprietarioReconstruido.getCpfCnpj()).isEqualTo("11144477735");
            assertThat(proprietarioReconstruido.getNome()).isEqualTo("João Silva");
            assertThat(proprietarioReconstruido.getTipoPessoa()).isEqualTo(TipoPessoa.FISICA);
        }

        @Test
        @DisplayName("Deve retornar status ATIVO")
        void deveRetornarStatusAtivo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento.getStatus()).isEqualTo(StatusVeiculo.ATIVO);
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Eventos com mesmos dados devem ser iguais")
        void eventosComMesmosDadosDevemSerIguais() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento1 = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            VeiculoCriadoEvent evento2 = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento1).isEqualTo(evento2);
            assertThat(evento1.hashCode()).isEqualTo(evento2.hashCode());
        }

        @Test
        @DisplayName("Eventos com dados diferentes não devem ser iguais")
        void eventosComDadosDiferentesNaoDevemSerIguais() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento1 = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            VeiculoCriadoEvent evento2 = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "XYZ5678", "05587898780", "1HGBH41JXMN109999",
                "Toyota", "Corolla", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            assertThat(evento1).isNotEqualTo(evento2);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            VeiculoCriadoEvent evento = VeiculoCriadoEvent.create(
                AGGREGATE_ID, 1L, "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            String toString = evento.toString();

            assertThat(toString).contains(AGGREGATE_ID);
            assertThat(toString).contains("ABC1234");
            assertThat(toString).contains("Honda");
            assertThat(toString).contains("Civic");
        }
    }
}
