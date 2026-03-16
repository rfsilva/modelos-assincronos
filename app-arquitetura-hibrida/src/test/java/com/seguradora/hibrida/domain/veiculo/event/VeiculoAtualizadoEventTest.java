package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoAtualizadoEvent}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoAtualizadoEvent - Testes Unitários")
class VeiculoAtualizadoEventTest {

    private static final String AGGREGATE_ID = "VEI-001";
    private static final String OPERADOR_ID = "OP-123";

    @Nested
    @DisplayName("Testes de Criação do Evento")
    class CriacaoEventoTests {

        @Test
        @DisplayName("Deve criar evento com dados válidos")
        void deveCriarEventoComDadosValidos() {
            // Arrange
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                 CategoriaVeiculo.PASSEIO, 2000);

            // Act
            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Atualização de cor e motor"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(evento.getVersion()).isEqualTo(2L);
            assertThat(evento.getEspecificacaoAnterior()).isEqualTo(anterior);
            assertThat(evento.getNovaEspecificacao()).isEqualTo(nova);
            assertThat(evento.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(evento.getMotivoAlteracao()).isEqualTo("Atualização de cor e motor");
            assertThat(evento.getEventType()).isEqualTo("VeiculoAtualizado");
        }

        @Test
        @DisplayName("Deve aceitar motivo nulo")
        void deveAceitarMotivoNulo() {
            Especificacao anterior = Especificacao.exemplo();
            Especificacao nova = Especificacao.exemplo();

            assertThatCode(() -> VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, null
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Descrição de Alterações")
    class DescricaoAlteracoesTests {

        @Test
        @DisplayName("Deve gerar descrição quando há mudança de cor")
        void deveGerarDescricaoQuandoHaMudancaDeCor() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.FLEX,
                                                 CategoriaVeiculo.PASSEIO, 1600);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Troca de cor"
            );

            String descricao = evento.getDescricaoAlteracoes();

            assertThat(descricao).contains("Cor:");
            assertThat(descricao).contains("Branco");
            assertThat(descricao).contains("Preto");
            assertThat(descricao).contains("→");
        }

        @Test
        @DisplayName("Deve gerar descrição quando há mudança de combustível")
        void deveGerarDescricaoQuandoHaMudancaDeCombustivel() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Branco", TipoCombustivel.GASOLINA,
                                                 CategoriaVeiculo.PASSEIO, 1600);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Troca de combustível"
            );

            String descricao = evento.getDescricaoAlteracoes();

            assertThat(descricao).contains("Combustível:");
            assertThat(descricao).contains("Flex");
            assertThat(descricao).contains("Gasolina");
        }

        @Test
        @DisplayName("Deve gerar descrição quando há mudança de cilindrada")
        void deveGerarDescricaoQuandoHaMudancaDeCilindrada() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                 CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Upgrade de motor"
            );

            String descricao = evento.getDescricaoAlteracoes();

            assertThat(descricao).contains("Cilindrada:");
            assertThat(descricao).contains("1600");
            assertThat(descricao).contains("2000");
        }

        @Test
        @DisplayName("Deve gerar descrição com múltiplas alterações")
        void deveGerarDescricaoComMultiplasAlteracoes() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                 CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Múltiplas alterações"
            );

            String descricao = evento.getDescricaoAlteracoes();

            assertThat(descricao).contains("Cor:");
            assertThat(descricao).contains("Combustível:");
            assertThat(descricao).contains("Cilindrada:");
        }

        @Test
        @DisplayName("Deve retornar mensagem quando não há alteração")
        void deveRetornarMensagemQuandoNaoHaAlteracao() {
            Especificacao especificacao = Especificacao.exemplo();

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, especificacao, especificacao, OPERADOR_ID, "Sem mudanças"
            );

            String descricao = evento.getDescricaoAlteracoes();

            assertThat(descricao).isEqualTo("Nenhuma alteração detectada");
        }
    }

    @Nested
    @DisplayName("Testes de Verificação de Alteração em Campo")
    class VerificacaoAlteracaoCampoTests {

        @Test
        @DisplayName("Deve detectar alteração em cor")
        void deveDetectarAlteracaoEmCor() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.FLEX,
                                                 CategoriaVeiculo.PASSEIO, 1600);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Troca de cor"
            );

            assertThat(evento.houveAlteracaoEm("cor")).isTrue();
            assertThat(evento.houveAlteracaoEm("combustivel")).isFalse();
            assertThat(evento.houveAlteracaoEm("cilindrada")).isFalse();
        }

        @Test
        @DisplayName("Deve detectar alteração em combustível")
        void deveDetectarAlteracaoEmCombustivel() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Branco", TipoCombustivel.GASOLINA,
                                                 CategoriaVeiculo.PASSEIO, 1600);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Troca de combustível"
            );

            assertThat(evento.houveAlteracaoEm("combustivel")).isTrue();
            assertThat(evento.houveAlteracaoEm("cor")).isFalse();
        }

        @Test
        @DisplayName("Deve detectar alteração em cilindrada")
        void deveDetectarAlteracaoEmCilindrada() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                 CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Upgrade de motor"
            );

            assertThat(evento.houveAlteracaoEm("cilindrada")).isTrue();
            assertThat(evento.houveAlteracaoEm("cor")).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para campo desconhecido")
        void deveRetornarFalseParaCampoDesconhecido() {
            Especificacao especificacao = Especificacao.exemplo();

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, especificacao, especificacao, OPERADOR_ID, "Teste"
            );

            assertThat(evento.houveAlteracaoEm("campoInexistente")).isFalse();
        }

        @Test
        @DisplayName("Deve ser case-insensitive na verificação de campo")
        void deveSerCaseInsensitiveNaVerificacaoDeCampo() {
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.FLEX,
                                                 CategoriaVeiculo.PASSEIO, 1600);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Troca de cor"
            );

            assertThat(evento.houveAlteracaoEm("COR")).isTrue();
            assertThat(evento.houveAlteracaoEm("Cor")).isTrue();
            assertThat(evento.houveAlteracaoEm("cor")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Eventos com mesmos dados devem ser iguais")
        void eventosComMesmosDadosDevemSerIguais() {
            Especificacao anterior = Especificacao.exemplo();
            Especificacao nova = Especificacao.exemplo();

            VeiculoAtualizadoEvent evento1 = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Teste"
            );

            VeiculoAtualizadoEvent evento2 = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Teste"
            );

            assertThat(evento1).isEqualTo(evento2);
            assertThat(evento1.hashCode()).isEqualTo(evento2.hashCode());
        }

        @Test
        @DisplayName("Eventos com dados diferentes não devem ser iguais")
        void eventosComDadosDiferentesNaoDevemSerIguais() {
            Especificacao anterior = Especificacao.exemplo();
            Especificacao nova1 = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                  CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova2 = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                  CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent evento1 = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova1, OPERADOR_ID, "Teste"
            );

            VeiculoAtualizadoEvent evento2 = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova2, OPERADOR_ID, "Teste"
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
            Especificacao anterior = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                     CategoriaVeiculo.PASSEIO, 1600);
            Especificacao nova = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                 CategoriaVeiculo.PASSEIO, 2000);

            VeiculoAtualizadoEvent evento = VeiculoAtualizadoEvent.create(
                AGGREGATE_ID, 2L, anterior, nova, OPERADOR_ID, "Múltiplas alterações"
            );

            String toString = evento.toString();

            assertThat(toString).contains(AGGREGATE_ID);
            assertThat(toString).contains(OPERADOR_ID);
            assertThat(toString).contains("alteracoes");
        }
    }
}
