package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("AprovadorPolicy Tests")
class AprovadorPolicyTest {

    private AprovadorPolicy policy;

    @BeforeEach
    void setUp() {
        policy = new AprovadorPolicy();
    }

    // =========================================================================
    // obterAprovadores
    // =========================================================================

    @Nested
    @DisplayName("obterAprovadores()")
    class ObterAprovadores {

        @Test
        @DisplayName("Deve retornar aprovadores para NIVEL_1")
        void shouldReturnAprovadoresForNivel1() {
            var aprovadores = policy.obterAprovadores(NivelAprovacao.NIVEL_1_ANALISTA, "SIN-001");
            assertThat(aprovadores).isNotEmpty();
            assertThat(aprovadores).contains("ANALISTA_001", "ANALISTA_002", "ANALISTA_003");
        }

        @Test
        @DisplayName("Deve retornar aprovadores para NIVEL_2")
        void shouldReturnAprovadoresForNivel2() {
            var aprovadores = policy.obterAprovadores(NivelAprovacao.NIVEL_2_SUPERVISOR, "SIN-001");
            assertThat(aprovadores).contains("SUPERVISOR_001", "SUPERVISOR_002");
        }

        @Test
        @DisplayName("Deve retornar aprovadores para NIVEL_3")
        void shouldReturnAprovadoresForNivel3() {
            var aprovadores = policy.obterAprovadores(NivelAprovacao.NIVEL_3_GERENTE, "SIN-001");
            assertThat(aprovadores).contains("GERENTE_001");
        }

        @Test
        @DisplayName("Deve retornar aprovadores para NIVEL_4")
        void shouldReturnAprovadoresForNivel4() {
            var aprovadores = policy.obterAprovadores(NivelAprovacao.NIVEL_4_DIRETOR, "SIN-001");
            assertThat(aprovadores).contains("DIRETOR_001");
        }

        @Test
        @DisplayName("Deve retornar lista independente (não modificar original)")
        void shouldReturnIndependentList() {
            var a1 = policy.obterAprovadores(NivelAprovacao.NIVEL_1_ANALISTA, "SIN-001");
            var a2 = policy.obterAprovadores(NivelAprovacao.NIVEL_1_ANALISTA, "SIN-001");
            assertThat(a1).isNotSameAs(a2);
        }
    }

    // =========================================================================
    // validarPermissao
    // =========================================================================

    @Nested
    @DisplayName("validarPermissao()")
    class ValidarPermissao {

        @Test
        @DisplayName("Deve validar com sucesso para aprovador e valor corretos")
        void shouldValidateSuccessfully() {
            policy.validarPermissao("ANALISTA_001", NivelAprovacao.NIVEL_1_ANALISTA,
                    new BigDecimal("5000.00"));
        }

        @Test
        @DisplayName("Deve lançar exceção quando aprovador não pertence ao nível")
        void shouldThrowWhenAprovadorNotInLevel() {
            assertThatThrownBy(() ->
                    policy.validarPermissao("GERENTE_001", NivelAprovacao.NIVEL_1_ANALISTA,
                            new BigDecimal("5000.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não tem permissão");
        }

        @Test
        @DisplayName("Deve lançar exceção quando valor excede alçada do nível")
        void shouldThrowWhenValueExceedsLimit() {
            assertThatThrownBy(() ->
                    policy.validarPermissao("ANALISTA_001", NivelAprovacao.NIVEL_1_ANALISTA,
                            new BigDecimal("50000.00")))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("não pode aprovar");
        }

        @Test
        @DisplayName("DIRETOR deve validar qualquer valor")
        void directorShouldValidateAnyValue() {
            policy.validarPermissao("DIRETOR_001", NivelAprovacao.NIVEL_4_DIRETOR,
                    new BigDecimal("1000000.00"));
        }
    }

    // =========================================================================
    // obterProximoNivel
    // =========================================================================

    @Test
    @DisplayName("obterProximoNivel deve retornar nível seguinte")
    void obterProximoNivelShouldReturnNextLevel() {
        assertThat(policy.obterProximoNivel(NivelAprovacao.NIVEL_1_ANALISTA))
                .isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
        assertThat(policy.obterProximoNivel(NivelAprovacao.NIVEL_4_DIRETOR)).isNull();
    }

    // =========================================================================
    // determinarNivelPorValor
    // =========================================================================

    @Test
    @DisplayName("determinarNivelPorValor deve delegar para NivelAprovacao.determinarNivel")
    void determinarNivelPorValorShouldDelegate() {
        assertThat(policy.determinarNivelPorValor(new BigDecimal("5000.00")))
                .isEqualTo(NivelAprovacao.NIVEL_1_ANALISTA);
        assertThat(policy.determinarNivelPorValor(new BigDecimal("25000.00")))
                .isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
    }

    // =========================================================================
    // isAprovadorValido
    // =========================================================================

    @Test
    @DisplayName("isAprovadorValido deve retornar true para aprovadores conhecidos")
    void isAprovadorValidoShouldReturnTrueForKnownAprovadores() {
        assertThat(policy.isAprovadorValido("ANALISTA_001")).isTrue();
        assertThat(policy.isAprovadorValido("SUPERVISOR_001")).isTrue();
        assertThat(policy.isAprovadorValido("GERENTE_001")).isTrue();
        assertThat(policy.isAprovadorValido("DIRETOR_001")).isTrue();
    }

    @Test
    @DisplayName("isAprovadorValido deve retornar false para aprovador desconhecido")
    void isAprovadorValidoShouldReturnFalseForUnknown() {
        assertThat(policy.isAprovadorValido("DESCONHECIDO")).isFalse();
    }

    // =========================================================================
    // obterNivelAprovador
    // =========================================================================

    @Test
    @DisplayName("obterNivelAprovador deve retornar nível correto")
    void obterNivelAprovadorShouldReturnCorrectLevel() {
        assertThat(policy.obterNivelAprovador("ANALISTA_001")).isEqualTo(NivelAprovacao.NIVEL_1_ANALISTA);
        assertThat(policy.obterNivelAprovador("SUPERVISOR_001")).isEqualTo(NivelAprovacao.NIVEL_2_SUPERVISOR);
        assertThat(policy.obterNivelAprovador("GERENTE_001")).isEqualTo(NivelAprovacao.NIVEL_3_GERENTE);
        assertThat(policy.obterNivelAprovador("DIRETOR_001")).isEqualTo(NivelAprovacao.NIVEL_4_DIRETOR);
    }

    @Test
    @DisplayName("obterNivelAprovador deve retornar null para aprovador desconhecido")
    void obterNivelAprovadorShouldReturnNullForUnknown() {
        assertThat(policy.obterNivelAprovador("NINGUEM")).isNull();
    }

    // =========================================================================
    // listarTodosAprovadores
    // =========================================================================

    @Test
    @DisplayName("listarTodosAprovadores deve retornar todos os aprovadores")
    void listarTodosAprovadoresShouldReturnAll() {
        var todos = policy.listarTodosAprovadores();
        assertThat(todos).hasSizeGreaterThanOrEqualTo(7);
        assertThat(todos).contains("ANALISTA_001", "SUPERVISOR_001", "GERENTE_001", "DIRETOR_001");
    }

    // =========================================================================
    // hasAprovadoresDisponiveis
    // =========================================================================

    @Test
    @DisplayName("hasAprovadoresDisponiveis deve retornar true para todos os níveis")
    void hasAprovadoresDisponiveisShouldReturnTrueForAllLevels() {
        for (NivelAprovacao nivel : NivelAprovacao.values()) {
            assertThat(policy.hasAprovadoresDisponiveis(nivel))
                    .as("Deve ter aprovadores para %s", nivel)
                    .isTrue();
        }
    }
}
