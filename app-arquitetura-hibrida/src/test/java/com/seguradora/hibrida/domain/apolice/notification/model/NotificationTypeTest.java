package com.seguradora.hibrida.domain.apolice.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link NotificationType}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("NotificationType - Testes Unitários")
class NotificationTypeTest {

    @Nested
    @DisplayName("Testes de Propriedades Básicas")
    class PropriedadesBasicasTests {

        @Test
        @DisplayName("APOLICE_CRIADA deve ter propriedades corretas")
        void apoliceCriadaDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.APOLICE_CRIADA;

            assertThat(type.getTitulo()).isEqualTo("Apólice Criada");
            assertThat(type.getDescricao()).isEqualTo("Sua apólice foi criada com sucesso");
            assertThat(type.isObrigatoria()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_ATUALIZADA deve ter propriedades corretas")
        void apoliceAtualizadaDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.APOLICE_ATUALIZADA;

            assertThat(type.getTitulo()).isEqualTo("Apólice Atualizada");
            assertThat(type.getDescricao()).isEqualTo("Sua apólice foi atualizada");
            assertThat(type.isObrigatoria()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_CANCELADA deve ter propriedades corretas")
        void apoliceCanceladaDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.APOLICE_CANCELADA;

            assertThat(type.getTitulo()).isEqualTo("Apólice Cancelada");
            assertThat(type.getDescricao()).isEqualTo("Sua apólice foi cancelada");
            assertThat(type.isObrigatoria()).isTrue();
        }

        @Test
        @DisplayName("VENCIMENTO_1_DIA deve ter propriedades corretas")
        void vencimento1DiaDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.VENCIMENTO_1_DIA;

            assertThat(type.getTitulo()).isEqualTo("Vencimento amanhã");
            assertThat(type.getDescricao()).isEqualTo("Sua apólice vence amanhã");
            assertThat(type.isObrigatoria()).isTrue();
        }

        @Test
        @DisplayName("COBERTURA_ADICIONADA deve ter propriedades corretas")
        void coberturaAdicionadaDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.COBERTURA_ADICIONADA;

            assertThat(type.getTitulo()).isEqualTo("Cobertura Adicionada");
            assertThat(type.getDescricao()).isEqualTo("Nova cobertura foi adicionada à sua apólice");
            assertThat(type.isObrigatoria()).isFalse();
        }

        @Test
        @DisplayName("SCORE_BAIXO deve ter propriedades corretas")
        void scoreBaixoDeveSerPropriedadesCorretas() {
            NotificationType type = NotificationType.SCORE_BAIXO;

            assertThat(type.getTitulo()).isEqualTo("Score de Renovação Baixo");
            assertThat(type.getDescricao()).isEqualTo("Sua apólice tem score baixo para renovação");
            assertThat(type.isObrigatoria()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Notificação de Vencimento")
    class NotificacaoVencimentoTests {

        @Test
        @DisplayName("VENCIMENTO_30_DIAS deve ser notificação de vencimento")
        void vencimento30DiasDeveSerVencimento() {
            assertThat(NotificationType.VENCIMENTO_30_DIAS.isVencimento()).isTrue();
        }

        @Test
        @DisplayName("VENCIMENTO_15_DIAS deve ser notificação de vencimento")
        void vencimento15DiasDeveSerVencimento() {
            assertThat(NotificationType.VENCIMENTO_15_DIAS.isVencimento()).isTrue();
        }

        @Test
        @DisplayName("VENCIMENTO_7_DIAS deve ser notificação de vencimento")
        void vencimento7DiasDeveSerVencimento() {
            assertThat(NotificationType.VENCIMENTO_7_DIAS.isVencimento()).isTrue();
        }

        @Test
        @DisplayName("VENCIMENTO_1_DIA deve ser notificação de vencimento")
        void vencimento1DiaDeveSerVencimento() {
            assertThat(NotificationType.VENCIMENTO_1_DIA.isVencimento()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_VENCIDA deve ser notificação de vencimento")
        void apoliceVencidaDeveSerVencimento() {
            assertThat(NotificationType.APOLICE_VENCIDA.isVencimento()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_CRIADA não deve ser notificação de vencimento")
        void apoliceCriadaNaoDeveSerVencimento() {
            assertThat(NotificationType.APOLICE_CRIADA.isVencimento()).isFalse();
        }

        @Test
        @DisplayName("COBERTURA_ADICIONADA não deve ser notificação de vencimento")
        void coberturaAdicionadaNaoDeveSerVencimento() {
            assertThat(NotificationType.COBERTURA_ADICIONADA.isVencimento()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Notificação Crítica")
    class NotificacaoCriticaTests {

        @Test
        @DisplayName("VENCIMENTO_1_DIA deve ser notificação crítica")
        void vencimento1DiaDeveSerCritica() {
            assertThat(NotificationType.VENCIMENTO_1_DIA.isCritica()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_VENCIDA deve ser notificação crítica")
        void apoliceVencidaDeveSerCritica() {
            assertThat(NotificationType.APOLICE_VENCIDA.isCritica()).isTrue();
        }

        @Test
        @DisplayName("APOLICE_CANCELADA deve ser notificação crítica")
        void apoliceCanceladaDeveSerCritica() {
            assertThat(NotificationType.APOLICE_CANCELADA.isCritica()).isTrue();
        }

        @Test
        @DisplayName("VENCIMENTO_7_DIAS não deve ser notificação crítica")
        void vencimento7DiasNaoDeveSerCritica() {
            assertThat(NotificationType.VENCIMENTO_7_DIAS.isCritica()).isFalse();
        }

        @Test
        @DisplayName("APOLICE_CRIADA não deve ser notificação crítica")
        void apoliceCriadaNaoDeveSerCritica() {
            assertThat(NotificationType.APOLICE_CRIADA.isCritica()).isFalse();
        }

        @Test
        @DisplayName("COBERTURA_ADICIONADA não deve ser notificação crítica")
        void coberturaAdicionadaNaoDeveSerCritica() {
            assertThat(NotificationType.COBERTURA_ADICIONADA.isCritica()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Prioridade")
    class PrioridadeTests {

        @Test
        @DisplayName("APOLICE_VENCIDA deve ter prioridade máxima (1)")
        void apoliceVencidaDeveTerPrioridadeMaxima() {
            assertThat(NotificationType.APOLICE_VENCIDA.getPrioridade()).isEqualTo(1);
        }

        @Test
        @DisplayName("VENCIMENTO_1_DIA deve ter prioridade máxima (1)")
        void vencimento1DiaDeveTerPrioridadeMaxima() {
            assertThat(NotificationType.VENCIMENTO_1_DIA.getPrioridade()).isEqualTo(1);
        }

        @Test
        @DisplayName("VENCIMENTO_7_DIAS deve ter prioridade alta (2)")
        void vencimento7DiasDeveTerPrioridadeAlta() {
            assertThat(NotificationType.VENCIMENTO_7_DIAS.getPrioridade()).isEqualTo(2);
        }

        @Test
        @DisplayName("APOLICE_CANCELADA deve ter prioridade alta (2)")
        void apoliceCanceladaDeveTerPrioridadeAlta() {
            assertThat(NotificationType.APOLICE_CANCELADA.getPrioridade()).isEqualTo(2);
        }

        @Test
        @DisplayName("VENCIMENTO_15_DIAS deve ter prioridade média (3)")
        void vencimento15DiasDeveTerPrioridadeMedia() {
            assertThat(NotificationType.VENCIMENTO_15_DIAS.getPrioridade()).isEqualTo(3);
        }

        @Test
        @DisplayName("APOLICE_CRIADA deve ter prioridade média (3)")
        void apoliceCriadaDeveTerPrioridadeMedia() {
            assertThat(NotificationType.APOLICE_CRIADA.getPrioridade()).isEqualTo(3);
        }

        @Test
        @DisplayName("VENCIMENTO_30_DIAS deve ter prioridade média-baixa (4)")
        void vencimento30DiasDeveTerPrioridadeMediaBaixa() {
            assertThat(NotificationType.VENCIMENTO_30_DIAS.getPrioridade()).isEqualTo(4);
        }

        @Test
        @DisplayName("APOLICE_RENOVADA deve ter prioridade média-baixa (4)")
        void apoliceRenovadaDeveTerPrioridadeMediaBaixa() {
            assertThat(NotificationType.APOLICE_RENOVADA.getPrioridade()).isEqualTo(4);
        }

        @Test
        @DisplayName("COBERTURA_ADICIONADA deve ter prioridade baixa (5)")
        void coberturaAdicionadaDeveTerPrioridadeBaixa() {
            assertThat(NotificationType.COBERTURA_ADICIONADA.getPrioridade()).isEqualTo(5);
        }

        @Test
        @DisplayName("SCORE_BAIXO deve ter prioridade baixa (5)")
        void scoreBaixoDeveTerPrioridadeBaixa() {
            assertThat(NotificationType.SCORE_BAIXO.getPrioridade()).isEqualTo(5);
        }

        @Test
        @DisplayName("Prioridades devem estar entre 1 e 5")
        void prioridadesDevemEstarEntre1E5() {
            for (NotificationType type : NotificationType.values()) {
                assertThat(type.getPrioridade())
                    .as("Prioridade de %s deveria estar entre 1 e 5", type)
                    .isBetween(1, 5);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Todos os Valores")
    class ValidacaoTodosValoresTests {

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Todos os tipos devem ter título não nulo")
        void todosTiposDevemTerTitulo(NotificationType type) {
            assertThat(type.getTitulo()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Todos os tipos devem ter descrição não nula")
        void todosTiposDevemTerDescricao(NotificationType type) {
            assertThat(type.getDescricao()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Todos os tipos devem ter prioridade válida")
        void todosTiposDevemTerPrioridadeValida(NotificationType type) {
            assertThat(type.getPrioridade()).isBetween(1, 5);
        }
    }

    @Nested
    @DisplayName("Testes de Regras de Negócio")
    class RegrasNegocioTests {

        @Test
        @DisplayName("Notificações críticas devem ter prioridade alta (1 ou 2)")
        void notificacoesCriticasDevemTerPrioridadeAlta() {
            for (NotificationType type : NotificationType.values()) {
                if (type.isCritica()) {
                    assertThat(type.getPrioridade())
                        .as("Tipo crítico %s deveria ter prioridade 1 ou 2", type)
                        .isLessThanOrEqualTo(2);
                }
            }
        }

        @Test
        @DisplayName("Notificações de vencimento devem ser obrigatórias")
        void notificacoesVencimentoDevemSerObrigatorias() {
            for (NotificationType type : NotificationType.values()) {
                if (type.isVencimento()) {
                    assertThat(type.isObrigatoria())
                        .as("Tipo de vencimento %s deveria ser obrigatório", type)
                        .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Notificações críticas devem ser obrigatórias")
        void notificacoesCriticasDevemSerObrigatorias() {
            for (NotificationType type : NotificationType.values()) {
                if (type.isCritica()) {
                    assertThat(type.isObrigatoria())
                        .as("Tipo crítico %s deveria ser obrigatório", type)
                        .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Deve ter exatamente 12 tipos de notificação")
        void deveTerExatamente12Tipos() {
            assertThat(NotificationType.values()).hasSize(12);
        }

        @Test
        @DisplayName("Deve ter exatamente 5 notificações de vencimento")
        void deveTer5NotificacoesVencimento() {
            long vencimentos = java.util.Arrays.stream(NotificationType.values())
                .filter(NotificationType::isVencimento)
                .count();

            assertThat(vencimentos).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve ter exatamente 3 notificações críticas")
        void deveTer3NotificacoesCriticas() {
            long criticas = java.util.Arrays.stream(NotificationType.values())
                .filter(NotificationType::isCritica)
                .count();

            assertThat(criticas).isEqualTo(3);
        }

        @Test
        @DisplayName("Notificações de vencimento devem ter prioridade decrescente com o prazo")
        void notificacoesVencimentoDevemTerPrioridadeDecrescenteComPrazo() {
            // Quanto menor o prazo, maior a prioridade (menor número)
            assertThat(NotificationType.APOLICE_VENCIDA.getPrioridade())
                .isLessThan(NotificationType.VENCIMENTO_7_DIAS.getPrioridade());
            assertThat(NotificationType.VENCIMENTO_1_DIA.getPrioridade())
                .isLessThan(NotificationType.VENCIMENTO_7_DIAS.getPrioridade());
            assertThat(NotificationType.VENCIMENTO_7_DIAS.getPrioridade())
                .isLessThan(NotificationType.VENCIMENTO_15_DIAS.getPrioridade());
            assertThat(NotificationType.VENCIMENTO_15_DIAS.getPrioridade())
                .isLessThan(NotificationType.VENCIMENTO_30_DIAS.getPrioridade());
        }
    }

    @Nested
    @DisplayName("Testes de Categorização")
    class CategorizacaoTests {

        @Test
        @DisplayName("Notificações de ciclo de vida da apólice")
        void notificacoesCicloVidaApolice() {
            assertThat(NotificationType.APOLICE_CRIADA.isObrigatoria()).isTrue();
            assertThat(NotificationType.APOLICE_ATUALIZADA.isObrigatoria()).isTrue();
            assertThat(NotificationType.APOLICE_CANCELADA.isObrigatoria()).isTrue();
            assertThat(NotificationType.APOLICE_RENOVADA.isObrigatoria()).isTrue();

            assertThat(NotificationType.APOLICE_CRIADA.isVencimento()).isFalse();
            assertThat(NotificationType.APOLICE_ATUALIZADA.isVencimento()).isFalse();
        }

        @Test
        @DisplayName("Notificações informativas devem não ser obrigatórias")
        void notificacoesInformativasDevemNaoSerObrigatorias() {
            assertThat(NotificationType.COBERTURA_ADICIONADA.isObrigatoria()).isFalse();
            assertThat(NotificationType.RENOVACAO_AUTOMATICA.isObrigatoria()).isFalse();
            assertThat(NotificationType.SCORE_BAIXO.isObrigatoria()).isFalse();
        }

        @Test
        @DisplayName("Notificações informativas não devem ser críticas")
        void notificacoesInformativasNaoDevemSerCriticas() {
            assertThat(NotificationType.COBERTURA_ADICIONADA.isCritica()).isFalse();
            assertThat(NotificationType.RENOVACAO_AUTOMATICA.isCritica()).isFalse();
            assertThat(NotificationType.SCORE_BAIXO.isCritica()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("valueOf deve funcionar corretamente")
        void valueOfDeveFuncionarCorretamente() {
            assertThat(NotificationType.valueOf("APOLICE_CRIADA"))
                .isEqualTo(NotificationType.APOLICE_CRIADA);
            assertThat(NotificationType.valueOf("VENCIMENTO_1_DIA"))
                .isEqualTo(NotificationType.VENCIMENTO_1_DIA);
            assertThat(NotificationType.valueOf("SCORE_BAIXO"))
                .isEqualTo(NotificationType.SCORE_BAIXO);
        }

        @Test
        @DisplayName("valueOf deve lançar exceção para valor inválido")
        void valueOfDeveLancarExcecaoParaValorInvalido() {
            assertThatThrownBy(() -> NotificationType.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("values deve retornar todos os tipos")
        void valuesDeveRetornarTodosTipos() {
            NotificationType[] types = NotificationType.values();

            assertThat(types).contains(
                NotificationType.APOLICE_CRIADA,
                NotificationType.APOLICE_ATUALIZADA,
                NotificationType.APOLICE_CANCELADA,
                NotificationType.APOLICE_RENOVADA,
                NotificationType.VENCIMENTO_30_DIAS,
                NotificationType.VENCIMENTO_15_DIAS,
                NotificationType.VENCIMENTO_7_DIAS,
                NotificationType.VENCIMENTO_1_DIA,
                NotificationType.APOLICE_VENCIDA,
                NotificationType.COBERTURA_ADICIONADA,
                NotificationType.RENOVACAO_AUTOMATICA,
                NotificationType.SCORE_BAIXO
            );
        }

        @Test
        @DisplayName("Todos os títulos devem ser únicos")
        void todosTitulosDevemSerUnicos() {
            NotificationType[] types = NotificationType.values();
            long titulosUnicos = java.util.Arrays.stream(types)
                .map(NotificationType::getTitulo)
                .distinct()
                .count();

            assertThat(titulosUnicos).isEqualTo(types.length);
        }

        @Test
        @DisplayName("Todas as descrições devem ser únicas")
        void todasDescricoesDevemSerUnicas() {
            NotificationType[] types = NotificationType.values();
            long descricoesUnicas = java.util.Arrays.stream(types)
                .map(NotificationType::getDescricao)
                .distinct()
                .count();

            assertThat(descricoesUnicas).isEqualTo(types.length);
        }
    }

    @Nested
    @DisplayName("Testes de Integração com Lógica de Negócio")
    class IntegracaoLogicaNegocioTests {

        @Test
        @DisplayName("Prioridade inversamente proporcional ao número (menor número = maior prioridade)")
        void prioridadeInversamenteProporcionalAoNumero() {
            NotificationType maisImportante = NotificationType.APOLICE_VENCIDA;
            NotificationType menosImportante = NotificationType.COBERTURA_ADICIONADA;

            assertThat(maisImportante.getPrioridade())
                .isLessThan(menosImportante.getPrioridade());
        }

        @Test
        @DisplayName("Tipos obrigatórios devem ter prioridade maior ou igual a tipos não obrigatórios")
        void tiposObrigatoriosDevemTerPrioridadeMaiorOuIgual() {
            int menorPrioridadeObrigatoria = java.util.Arrays.stream(NotificationType.values())
                .filter(NotificationType::isObrigatoria)
                .mapToInt(NotificationType::getPrioridade)
                .max()
                .orElse(5);

            int maiorPrioridadeOpcional = java.util.Arrays.stream(NotificationType.values())
                .filter(t -> !t.isObrigatoria())
                .mapToInt(NotificationType::getPrioridade)
                .min()
                .orElse(1);

            // Notificações obrigatórias podem ter prioridade menor (mais importantes)
            // ou igual às opcionais, mas geralmente são mais importantes
            assertThat(menorPrioridadeObrigatoria).isLessThanOrEqualTo(5);
            assertThat(maiorPrioridadeOpcional).isGreaterThanOrEqualTo(5);
        }
    }
}
