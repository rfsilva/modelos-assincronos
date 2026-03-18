package com.seguradora.hibrida.domain.workflow.metrics;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SlaConfiguration Tests")
class SlaConfigurationTest {

    private SlaConfiguration config;

    @BeforeEach
    void setUp() {
        config = new SlaConfiguration();
    }

    // =========================================================================
    // Valores padrão
    // =========================================================================

    @Test
    @DisplayName("Deve ter valores padrão corretos")
    void shouldHaveCorrectDefaults() {
        assertThat(config.getSinistroSimplesHoras()).isEqualTo(48);
        assertThat(config.getSinistroComplexoDiasUteis()).isEqualTo(5);
        assertThat(config.getRouboFurtoDiasUteis()).isEqualTo(10);
        assertThat(config.getTerceirosDiasUteis()).isEqualTo(15);
        assertThat(config.getAlertas()).isNotNull();
        assertThat(config.getEscalacao()).isNotNull();
    }

    @Test
    @DisplayName("Alertas devem ter valores padrão corretos")
    void alertasShouldHaveCorrectDefaults() {
        SlaConfiguration.Alertas alertas = config.getAlertas();
        assertThat(alertas.getPercentual50()).isEqualTo(50);
        assertThat(alertas.getPercentual80()).isEqualTo(80);
        assertThat(alertas.isEmailHabilitado()).isTrue();
        assertThat(alertas.isPushHabilitado()).isTrue();
        assertThat(alertas.getIntervaloMinutosEntreAlertas()).isEqualTo(60);
    }

    @Test
    @DisplayName("Escalacao deve ter valores padrão corretos")
    void escalacaoShouldHaveCorrectDefaults() {
        SlaConfiguration.Escalacao escalacao = config.getEscalacao();
        assertThat(escalacao.isAutomatica()).isTrue();
        assertThat(escalacao.getPercentualParaEscalar()).isEqualTo(90);
        assertThat(escalacao.getHorasSemMovimentacao()).isEqualTo(24);
    }

    // =========================================================================
    // NivelAlerta enum
    // =========================================================================

    @Test
    @DisplayName("NivelAlerta deve ter 3 valores")
    void nivelAlertaShouldHaveThreeValues() {
        assertThat(SlaConfiguration.NivelAlerta.values()).hasSize(3);
    }

    @Test
    @DisplayName("NivelAlerta deve ter descrição não nula")
    void nivelAlertaShouldHaveDescription() {
        for (SlaConfiguration.NivelAlerta nivel : SlaConfiguration.NivelAlerta.values()) {
            assertThat(nivel.getDescricao()).isNotNull().isNotBlank();
        }
    }

    // =========================================================================
    // getSlaHoras
    // =========================================================================

    @Nested
    @DisplayName("getSlaHoras()")
    class GetSlaHoras {

        @Test
        @DisplayName("SIMPLES deve retornar 48 horas")
        void simplesShouldReturn48Hours() {
            assertThat(config.getSlaHoras("SIMPLES")).isEqualTo(48);
            assertThat(config.getSlaHoras("simples")).isEqualTo(48);
        }

        @Test
        @DisplayName("COMPLEXO deve retornar dias úteis * 24")
        void complexoShouldReturnDiasUteis24() {
            assertThat(config.getSlaHoras("COMPLEXO")).isEqualTo(5 * 24);
        }

        @Test
        @DisplayName("ROUBO_FURTO deve retornar dias úteis * 24")
        void rouboFurtoShouldReturnDiasUteis24() {
            assertThat(config.getSlaHoras("ROUBO_FURTO")).isEqualTo(10 * 24);
        }

        @Test
        @DisplayName("TERCEIROS deve retornar dias úteis * 24")
        void terceirosShouldReturnDiasUteis24() {
            assertThat(config.getSlaHoras("TERCEIROS")).isEqualTo(15 * 24);
        }

        @Test
        @DisplayName("Tipo desconhecido deve retornar SLA padrão de complexo")
        void unknownTypeShouldReturnComplexoDefault() {
            assertThat(config.getSlaHoras("DESCONHECIDO")).isEqualTo(5 * 24);
        }

        @Test
        @DisplayName("Tipo customizado deve retornar valor customizado")
        void customTypeShouldReturnCustomValue() {
            config.getCustomizados().put("ESPECIAL", 96);
            assertThat(config.getSlaHoras("ESPECIAL")).isEqualTo(96);
        }
    }

    // =========================================================================
    // excedeuSla
    // =========================================================================

    @Nested
    @DisplayName("excedeuSla()")
    class ExcedeuSla {

        @Test
        @DisplayName("Deve retornar false quando dentro do SLA")
        void shouldReturnFalseWhenWithinSla() {
            assertThat(config.excedeuSla("SIMPLES", 24)).isFalse();
            assertThat(config.excedeuSla("SIMPLES", 48)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar true quando excede o SLA")
        void shouldReturnTrueWhenExceedsSla() {
            assertThat(config.excedeuSla("SIMPLES", 49)).isTrue();
        }
    }

    // =========================================================================
    // calcularPercentualSla
    // =========================================================================

    @Nested
    @DisplayName("calcularPercentualSla()")
    class CalcularPercentualSla {

        @Test
        @DisplayName("Deve calcular percentual corretamente para SIMPLES")
        void shouldCalculatePercentualCorrectly() {
            assertThat(config.calcularPercentualSla("SIMPLES", 24)).isEqualTo(50.0);
            assertThat(config.calcularPercentualSla("SIMPLES", 48)).isEqualTo(100.0);
        }

        @Test
        @DisplayName("Deve retornar percentual acima de 100 quando SLA excedido")
        void shouldReturnAbove100WhenExceeded() {
            assertThat(config.calcularPercentualSla("SIMPLES", 96)).isEqualTo(200.0);
        }
    }

    // =========================================================================
    // getNivelAlerta
    // =========================================================================

    @Nested
    @DisplayName("getNivelAlerta()")
    class GetNivelAlerta {

        @Test
        @DisplayName("Deve retornar CRITICO para percentual >= 100")
        void shouldReturnCriticoFor100Percent() {
            assertThat(config.getNivelAlerta(100)).isEqualTo(SlaConfiguration.NivelAlerta.CRITICO);
            assertThat(config.getNivelAlerta(150)).isEqualTo(SlaConfiguration.NivelAlerta.CRITICO);
        }

        @Test
        @DisplayName("Deve retornar ALTO para percentual entre 80 e 99")
        void shouldReturnAltoFor80to99Percent() {
            assertThat(config.getNivelAlerta(80)).isEqualTo(SlaConfiguration.NivelAlerta.ALTO);
            assertThat(config.getNivelAlerta(95)).isEqualTo(SlaConfiguration.NivelAlerta.ALTO);
        }

        @Test
        @DisplayName("Deve retornar MEDIO para percentual entre 50 e 79")
        void shouldReturnMedioFor50to79Percent() {
            assertThat(config.getNivelAlerta(50)).isEqualTo(SlaConfiguration.NivelAlerta.MEDIO);
            assertThat(config.getNivelAlerta(75)).isEqualTo(SlaConfiguration.NivelAlerta.MEDIO);
        }

        @Test
        @DisplayName("Deve retornar null para percentual abaixo de 50")
        void shouldReturnNullBelow50Percent() {
            assertThat(config.getNivelAlerta(0)).isNull();
            assertThat(config.getNivelAlerta(49)).isNull();
        }
    }
}
