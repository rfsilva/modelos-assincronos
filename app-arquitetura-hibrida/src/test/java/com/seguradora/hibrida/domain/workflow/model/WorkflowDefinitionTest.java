package com.seguradora.hibrida.domain.workflow.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("WorkflowDefinition Tests")
class WorkflowDefinitionTest {

    private EtapaWorkflow etapa(int ordem, TipoEtapa tipo) {
        return EtapaWorkflow.builder()
                .id("etapa-" + ordem)
                .nome("Etapa " + ordem)
                .tipo(tipo)
                .ordem(ordem)
                .build();
    }

    private EtapaWorkflow etapaAprovacao(int ordem) {
        return EtapaWorkflow.builder()
                .id("etapa-" + ordem)
                .nome("Aprovação " + ordem)
                .tipo(TipoEtapa.APROVACAO)
                .ordem(ordem)
                .nivelAprovacao(NivelAprovacao.NIVEL_1_ANALISTA)
                .build();
    }

    private WorkflowDefinition definicaoComEtapas() {
        return WorkflowDefinition.builder()
                .id("def-001")
                .nome("Workflow Simples")
                .versao(1)
                .tipoSinistro("SIMPLES")
                .criadoEm(LocalDateTime.now())
                .etapas(new ArrayList<>(List.of(
                        etapa(1, TipoEtapa.AUTOMATICA),
                        etapa(2, TipoEtapa.MANUAL),
                        etapaAprovacao(3)
                )))
                .build();
    }

    // =========================================================================
    // validar
    // =========================================================================

    @Nested
    @DisplayName("validar()")
    class Validar {

        @Test
        @DisplayName("Definição válida não deve lançar exceção")
        void validDefinitionShouldNotThrow() {
            definicaoComEtapas().validar();
        }

        @Test
        @DisplayName("Nome nulo deve lançar exceção")
        void nullNameShouldThrow() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .tipoSinistro("SIMPLES")
                    .etapas(List.of(etapa(1, TipoEtapa.AUTOMATICA))).build();
            assertThatThrownBy(def::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Nome");
        }

        @Test
        @DisplayName("Tipo sinistro nulo deve lançar exceção")
        void nullTipoSinistrooShouldThrow() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .nome("Workflow")
                    .etapas(List.of(etapa(1, TipoEtapa.AUTOMATICA))).build();
            assertThatThrownBy(def::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sinistro");
        }

        @Test
        @DisplayName("Lista de etapas vazia deve lançar exceção")
        void emptyEtapasShouldThrow() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .nome("Workflow").tipoSinistro("SIMPLES").build();
            assertThatThrownBy(def::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("etapa");
        }

        @Test
        @DisplayName("Ordens não sequenciais devem lançar exceção")
        void nonSequentialOrderShouldThrow() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .nome("Workflow")
                    .tipoSinistro("SIMPLES")
                    .etapas(List.of(
                            etapa(1, TipoEtapa.AUTOMATICA),
                            etapa(3, TipoEtapa.MANUAL) // Pula ordem 2
                    )).build();
            assertThatThrownBy(def::validar)
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("sequencial");
        }
    }

    // =========================================================================
    // primeiraEtapa / ultimaEtapa
    // =========================================================================

    @Nested
    @DisplayName("primeiraEtapa() / ultimaEtapa()")
    class PrimeiraUltimaEtapa {

        @Test
        @DisplayName("primeiraEtapa deve retornar etapa de ordem 1")
        void primeiraEtapaShouldReturnOrderOne() {
            WorkflowDefinition def = definicaoComEtapas();
            assertThat(def.primeiraEtapa().getOrdem()).isEqualTo(1);
        }

        @Test
        @DisplayName("primeiraEtapa deve lançar exceção se não houver etapa de ordem 1")
        void primeiraEtapaShouldThrowIfMissing() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .nome("W").tipoSinistro("S").build();
            assertThatThrownBy(def::primeiraEtapa)
                    .isInstanceOf(IllegalStateException.class);
        }

        @Test
        @DisplayName("ultimaEtapa deve retornar etapa de maior ordem")
        void ultimaEtapaShouldReturnHighestOrder() {
            WorkflowDefinition def = definicaoComEtapas();
            assertThat(def.ultimaEtapa().getOrdem()).isEqualTo(3);
        }

        @Test
        @DisplayName("ultimaEtapa deve lançar exceção se sem etapas")
        void ultimaEtapaShouldThrowIfEmpty() {
            WorkflowDefinition def = WorkflowDefinition.builder()
                    .nome("W").tipoSinistro("S").build();
            assertThatThrownBy(def::ultimaEtapa)
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    // =========================================================================
    // proximaEtapa
    // =========================================================================

    @Nested
    @DisplayName("proximaEtapa()")
    class ProximaEtapa {

        @Test
        @DisplayName("Deve retornar etapa com ordem + 1")
        void shouldReturnEtapaWithNextOrder() {
            WorkflowDefinition def = definicaoComEtapas();
            EtapaWorkflow primeira = def.primeiraEtapa();
            EtapaWorkflow proxima = def.proximaEtapa(primeira);
            assertThat(proxima.getOrdem()).isEqualTo(2);
        }

        @Test
        @DisplayName("Deve retornar null se não houver próxima etapa")
        void shouldReturnNullIfNoNextEtapa() {
            WorkflowDefinition def = definicaoComEtapas();
            EtapaWorkflow ultima = def.ultimaEtapa();
            assertThat(def.proximaEtapa(ultima)).isNull();
        }

        @Test
        @DisplayName("proximaEtapa(null) deve retornar primeira etapa")
        void proximaEtapaWithNullShouldReturnFirst() {
            WorkflowDefinition def = definicaoComEtapas();
            assertThat(def.proximaEtapa(null).getOrdem()).isEqualTo(1);
        }
    }

    // =========================================================================
    // buscarEtapa / buscarEtapaPorOrdem
    // =========================================================================

    @Nested
    @DisplayName("buscarEtapa() / buscarEtapaPorOrdem()")
    class BuscarEtapa {

        @Test
        @DisplayName("buscarEtapa deve encontrar etapa por ID")
        void buscarEtapaShouldFindById() {
            WorkflowDefinition def = definicaoComEtapas();
            EtapaWorkflow found = def.buscarEtapa("etapa-2");
            assertThat(found).isNotNull();
            assertThat(found.getOrdem()).isEqualTo(2);
        }

        @Test
        @DisplayName("buscarEtapa deve retornar null para ID inexistente")
        void buscarEtapaShouldReturnNullForMissingId() {
            assertThat(definicaoComEtapas().buscarEtapa("inexistente")).isNull();
        }

        @Test
        @DisplayName("buscarEtapaPorOrdem deve encontrar etapa pela ordem")
        void buscarEtapaPorOrdemShouldFindByOrder() {
            WorkflowDefinition def = definicaoComEtapas();
            assertThat(def.buscarEtapaPorOrdem(2).getNome()).isEqualTo("Etapa 2");
        }

        @Test
        @DisplayName("buscarEtapaPorOrdem deve retornar null para ordem inexistente")
        void buscarEtapaPorOrdemShouldReturnNullForMissingOrder() {
            assertThat(definicaoComEtapas().buscarEtapaPorOrdem(99)).isNull();
        }
    }

    // =========================================================================
    // adicionarEtapa / removerEtapa
    // =========================================================================

    @Nested
    @DisplayName("adicionarEtapa() / removerEtapa()")
    class AddRemoveEtapa {

        @Test
        @DisplayName("adicionarEtapa deve incrementar total")
        void adicionarEtapaShouldIncrementTotal() {
            WorkflowDefinition def = definicaoComEtapas();
            int before = def.totalEtapas();
            def.adicionarEtapa(etapa(4, TipoEtapa.AUTOMATICA));
            assertThat(def.totalEtapas()).isEqualTo(before + 1);
        }

        @Test
        @DisplayName("removerEtapa deve decrementar total")
        void removerEtapaShouldDecrementTotal() {
            WorkflowDefinition def = definicaoComEtapas();
            int before = def.totalEtapas();
            def.removerEtapa("etapa-3");
            assertThat(def.totalEtapas()).isEqualTo(before - 1);
        }
    }

    // =========================================================================
    // ativar / desativar / isAtivo
    // =========================================================================

    @Nested
    @DisplayName("ativar() / desativar() / isAtivo()")
    class AtivarDesativar {

        @Test
        @DisplayName("Definição deve ser ativa por padrão")
        void definitionShouldBeActiveByDefault() {
            assertThat(definicaoComEtapas().isAtivo()).isTrue();
        }

        @Test
        @DisplayName("desativar deve marcar como inativa")
        void desativarShouldMarkAsInactive() {
            WorkflowDefinition def = definicaoComEtapas();
            def.desativar();
            assertThat(def.isAtivo()).isFalse();
            assertThat(def.getAtualizadoEm()).isNotNull();
        }

        @Test
        @DisplayName("ativar deve chamar validar e marcar como ativo")
        void ativarShouldValidateAndMarkAsActive() {
            WorkflowDefinition def = definicaoComEtapas();
            def.desativar();
            def.ativar();
            assertThat(def.isAtivo()).isTrue();
        }
    }

    // =========================================================================
    // isCompleto / totalEtapas
    // =========================================================================

    @Test
    @DisplayName("isCompleto deve retornar true quando todas etapas são válidas")
    void isCompletoShouldReturnTrueWhenAllEtapasAreValid() {
        assertThat(definicaoComEtapas().isCompleto()).isTrue();
    }

    @Test
    @DisplayName("totalEtapas deve retornar contagem correta")
    void totalEtapasShouldReturnCorrectCount() {
        assertThat(definicaoComEtapas().totalEtapas()).isEqualTo(3);
    }

    // =========================================================================
    // criarNovaVersao
    // =========================================================================

    @Test
    @DisplayName("criarNovaVersao deve incrementar versão e copiar etapas")
    void criarNovaVersaoShouldIncrementVersionAndCopyEtapas() {
        WorkflowDefinition original = definicaoComEtapas();
        WorkflowDefinition nova = original.criarNovaVersao();
        assertThat(nova.getVersao()).isEqualTo(original.getVersao() + 1);
        assertThat(nova.getId()).isNotEqualTo(original.getId());
        assertThat(nova.isAtivo()).isFalse();
        assertThat(nova.totalEtapas()).isEqualTo(original.totalEtapas());
        assertThat(nova.getNome()).isEqualTo(original.getNome());
    }

    @Test
    @DisplayName("toString deve retornar representação não nula")
    void toStringShouldReturnNonNull() {
        assertThat(definicaoComEtapas().toString()).isNotNull();
    }
}
