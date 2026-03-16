package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para o enum StatusApolice.
 */
@DisplayName("StatusApolice - Testes Unitários")
class StatusApoliceTest {

    @Test
    @DisplayName("Deve ter todos os status disponíveis")
    void deveTerTodosStatusDisponiveis() {
        // Act & Assert
        assertThat(StatusApolice.values()).containsExactly(
                StatusApolice.ATIVA,
                StatusApolice.CANCELADA,
                StatusApolice.VENCIDA,
                StatusApolice.SUSPENSA
        );
    }

    @Test
    @DisplayName("Deve retornar descrição correta para cada status")
    void deveRetornarDescricaoCorreta() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.getDescricao()).isEqualTo("Ativa");
        assertThat(StatusApolice.CANCELADA.getDescricao()).isEqualTo("Cancelada");
        assertThat(StatusApolice.VENCIDA.getDescricao()).isEqualTo("Vencida");
        assertThat(StatusApolice.SUSPENSA.getDescricao()).isEqualTo("Suspensa");
    }

    @Test
    @DisplayName("Deve retornar detalhamento correto para cada status")
    void deveRetornarDetalhamentoCorreto() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.getDetalhamento()).isEqualTo("Apólice ativa e vigente");
        assertThat(StatusApolice.CANCELADA.getDetalhamento()).isEqualTo("Apólice cancelada");
        assertThat(StatusApolice.VENCIDA.getDetalhamento()).isEqualTo("Apólice vencida");
        assertThat(StatusApolice.SUSPENSA.getDetalhamento()).isEqualTo("Apólice suspensa temporariamente");
    }

    @Test
    @DisplayName("Deve identificar corretamente status ativo")
    void deveIdentificarStatusAtivo() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.isAtiva()).isTrue();
        assertThat(StatusApolice.CANCELADA.isAtiva()).isFalse();
        assertThat(StatusApolice.VENCIDA.isAtiva()).isFalse();
        assertThat(StatusApolice.SUSPENSA.isAtiva()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente se pode ser renovada")
    void deveIdentificarSePodeSerRenovada() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.podeSerRenovada()).isTrue();
        assertThat(StatusApolice.VENCIDA.podeSerRenovada()).isTrue();
        assertThat(StatusApolice.CANCELADA.podeSerRenovada()).isFalse();
        assertThat(StatusApolice.SUSPENSA.podeSerRenovada()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente se pode ser cancelada")
    void deveIdentificarSePodeSerCancelada() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.podeSerCancelada()).isTrue();
        assertThat(StatusApolice.SUSPENSA.podeSerCancelada()).isTrue();
        assertThat(StatusApolice.CANCELADA.podeSerCancelada()).isFalse();
        assertThat(StatusApolice.VENCIDA.podeSerCancelada()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente se pode gerar sinistros")
    void deveIdentificarSePodeGerarSinistros() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.podeGerarSinistros()).isTrue();
        assertThat(StatusApolice.CANCELADA.podeGerarSinistros()).isFalse();
        assertThat(StatusApolice.VENCIDA.podeGerarSinistros()).isFalse();
        assertThat(StatusApolice.SUSPENSA.podeGerarSinistros()).isFalse();
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.toString()).isEqualTo("Ativa");
        assertThat(StatusApolice.CANCELADA.toString()).isEqualTo("Cancelada");
        assertThat(StatusApolice.VENCIDA.toString()).isEqualTo("Vencida");
        assertThat(StatusApolice.SUSPENSA.toString()).isEqualTo("Suspensa");
    }

    @Test
    @DisplayName("Deve permitir conversão de string para enum")
    void devePermitirConversaoDeStringParaEnum() {
        // Act & Assert
        assertThat(StatusApolice.valueOf("ATIVA")).isEqualTo(StatusApolice.ATIVA);
        assertThat(StatusApolice.valueOf("CANCELADA")).isEqualTo(StatusApolice.CANCELADA);
        assertThat(StatusApolice.valueOf("VENCIDA")).isEqualTo(StatusApolice.VENCIDA);
        assertThat(StatusApolice.valueOf("SUSPENSA")).isEqualTo(StatusApolice.SUSPENSA);
    }

    @Test
    @DisplayName("Deve lançar exceção ao converter string inválida")
    void deveLancarExcecaoAoConverterStringInvalida() {
        // Act & Assert
        assertThatThrownBy(() -> StatusApolice.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve verificar comparação entre status")
    void deveVerificarComparacaoEntreStatus() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA).isEqualTo(StatusApolice.ATIVA);
        assertThat(StatusApolice.ATIVA).isNotEqualTo(StatusApolice.CANCELADA);
    }

    @Test
    @DisplayName("Deve ter hashCode consistente")
    void deveTerHashCodeConsistente() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.hashCode()).isEqualTo(StatusApolice.ATIVA.hashCode());
    }

    @Test
    @DisplayName("Deve validar regras de transição de status - ativa pode ser cancelada")
    void deveValidarTransicaoAtivaPodeCancelar() {
        // Act & Assert
        assertThat(StatusApolice.ATIVA.podeSerCancelada()).isTrue();
    }

    @Test
    @DisplayName("Deve validar regras de transição de status - vencida não pode ser cancelada")
    void deveValidarTransicaoVencidaNaoPodeCancelar() {
        // Act & Assert
        assertThat(StatusApolice.VENCIDA.podeSerCancelada()).isFalse();
    }

    @Test
    @DisplayName("Deve validar regras de transição de status - cancelada não pode ser renovada")
    void deveValidarTransicaoCanceladaNaoPodeRenovar() {
        // Act & Assert
        assertThat(StatusApolice.CANCELADA.podeSerRenovada()).isFalse();
    }

    @Test
    @DisplayName("Deve validar regras de transição de status - suspensa pode ser cancelada")
    void deveValidarTransicaoSuspensaPodeCancelar() {
        // Act & Assert
        assertThat(StatusApolice.SUSPENSA.podeSerCancelada()).isTrue();
    }

    @Test
    @DisplayName("Deve validar que apenas ativa pode gerar sinistros")
    void deveValidarQueApenasAtivaPodeGerarSinistros() {
        // Act & Assert
        for (StatusApolice status : StatusApolice.values()) {
            if (status == StatusApolice.ATIVA) {
                assertThat(status.podeGerarSinistros()).isTrue();
            } else {
                assertThat(status.podeGerarSinistros()).isFalse();
            }
        }
    }

    @Test
    @DisplayName("Deve validar estados finais - cancelada e vencida")
    void deveValidarEstadosFinais() {
        // Act & Assert
        assertThat(StatusApolice.CANCELADA.podeSerCancelada()).isFalse();
        assertThat(StatusApolice.CANCELADA.podeGerarSinistros()).isFalse();

        assertThat(StatusApolice.VENCIDA.podeSerCancelada()).isFalse();
        assertThat(StatusApolice.VENCIDA.podeGerarSinistros()).isFalse();
    }

    @Test
    @DisplayName("Deve validar estado transitório - suspensa")
    void deveValidarEstadoTransitorio() {
        // Suspensa é um estado transitório que pode voltar ou ser cancelada
        // Act & Assert
        assertThat(StatusApolice.SUSPENSA.podeSerCancelada()).isTrue();
        assertThat(StatusApolice.SUSPENSA.podeGerarSinistros()).isFalse();
        assertThat(StatusApolice.SUSPENSA.podeSerRenovada()).isFalse();
    }
}
