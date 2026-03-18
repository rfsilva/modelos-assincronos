package com.seguradora.hibrida.domain.analytics.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link AnalyticsProjection}.
 */
@DisplayName("AnalyticsProjection Tests")
class AnalyticsProjectionTest {

    // =========================================================================
    // Meta-informação
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Entity")
    void shouldBeAnnotatedWithEntity() {
        assertThat(AnalyticsProjection.class.isAnnotationPresent(Entity.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Table(name=analytics_projection)")
    void shouldBeAnnotatedWithTableName() {
        Table table = AnalyticsProjection.class.getAnnotation(Table.class);
        assertThat(table).isNotNull();
        assertThat(table.name()).isEqualTo("analytics_projection");
    }

    // =========================================================================
    // Defaults do builder
    // =========================================================================

    @Test
    @DisplayName("Builder deve inicializar contadores de segurados com zero")
    void builderShouldInitializeSeguradosCountersToZero() {
        AnalyticsProjection proj = AnalyticsProjection.builder().build();

        assertThat(proj.getTotalSegurados()).isZero();
        assertThat(proj.getSeguradosAtivos()).isZero();
        assertThat(proj.getSeguradosInativos()).isZero();
        assertThat(proj.getNovosSegurados()).isZero();
        assertThat(proj.getSeguradosCancelados()).isZero();
    }

    @Test
    @DisplayName("Builder deve inicializar contadores de apólices com zero")
    void builderShouldInitializeApolicesCountersToZero() {
        AnalyticsProjection proj = AnalyticsProjection.builder().build();

        assertThat(proj.getTotalApolices()).isZero();
        assertThat(proj.getApolicesAtivas()).isZero();
        assertThat(proj.getApolicesVencidas()).isZero();
        assertThat(proj.getApolicesCanceladas()).isZero();
        assertThat(proj.getNovasApolices()).isZero();
        assertThat(proj.getRenovacoes()).isZero();
    }

    @Test
    @DisplayName("Builder deve inicializar valores financeiros com BigDecimal.ZERO")
    void builderShouldInitializeFinancialValuesToZero() {
        AnalyticsProjection proj = AnalyticsProjection.builder().build();

        assertThat(proj.getValorTotalSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(proj.getPremioTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(proj.getPremioMedio()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(proj.getValorMedioSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Builder deve inicializar version com 0")
    void builderShouldInitializeVersionToZero() {
        AnalyticsProjection proj = AnalyticsProjection.builder().build();
        assertThat(proj.getVersion()).isZero();
    }

    // =========================================================================
    // incrementarSegurados
    // =========================================================================

    @Nested
    @DisplayName("incrementarSegurados()")
    class IncrementarSegurados {

        @Test
        @DisplayName("Deve incrementar totalSegurados, novosSegurados e seguradosAtivos")
        void shouldIncrementAllSeguradosCounters() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();

            proj.incrementarSegurados(3L);

            assertThat(proj.getTotalSegurados()).isEqualTo(3L);
            assertThat(proj.getNovosSegurados()).isEqualTo(3L);
            assertThat(proj.getSeguradosAtivos()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Deve acumular em chamadas sucessivas")
        void shouldAccumulateOnSuccessiveCalls() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();

            proj.incrementarSegurados(2L);
            proj.incrementarSegurados(3L);

            assertThat(proj.getTotalSegurados()).isEqualTo(5L);
        }
    }

    // =========================================================================
    // incrementarApolices
    // =========================================================================

    @Nested
    @DisplayName("incrementarApolices()")
    class IncrementarApolices {

        @Test
        @DisplayName("Deve incrementar contadores e somar valores financeiros")
        void shouldIncrementCountersAndSumFinancialValues() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();

            proj.incrementarApolices(1L, new BigDecimal("100000"), new BigDecimal("1200"));

            assertThat(proj.getTotalApolices()).isEqualTo(1L);
            assertThat(proj.getNovasApolices()).isEqualTo(1L);
            assertThat(proj.getApolicesAtivas()).isEqualTo(1L);
            assertThat(proj.getValorTotalSegurado()).isEqualByComparingTo(new BigDecimal("100000"));
            assertThat(proj.getPremioTotal()).isEqualByComparingTo(new BigDecimal("1200"));
        }

        @Test
        @DisplayName("Deve calcular premioMedio após incremento")
        void shouldCalculatePremioMedioAfterIncrement() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();

            proj.incrementarApolices(1L, new BigDecimal("100000"), new BigDecimal("1200"));
            proj.incrementarApolices(1L, new BigDecimal("200000"), new BigDecimal("1800"));

            // premioTotal=3000, apolicesAtivas=2 → premioMedio=1500
            assertThat(proj.getPremioMedio()).isEqualByComparingTo(new BigDecimal("1500.00"));
        }
    }

    // =========================================================================
    // registrarCancelamento
    // =========================================================================

    @Nested
    @DisplayName("registrarCancelamento()")
    class RegistrarCancelamento {

        @Test
        @DisplayName("Deve decrementar apolicesAtivas e incrementar apolicesCanceladas")
        void shouldDecrementActiveAndIncrementCancelled() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarApolices(3L, new BigDecimal("300000"), new BigDecimal("3600"));

            proj.registrarCancelamento(new BigDecimal("100000"), new BigDecimal("1200"));

            assertThat(proj.getApolicesAtivas()).isEqualTo(2L);
            assertThat(proj.getApolicesCanceladas()).isEqualTo(1L);
        }

        @Test
        @DisplayName("apolicesAtivas não deve ser negativo")
        void apolicesAtivasNeverNegative() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();

            proj.registrarCancelamento(BigDecimal.ZERO, BigDecimal.ZERO);

            assertThat(proj.getApolicesAtivas()).isZero();
        }

        @Test
        @DisplayName("Deve recalcular taxaCancelamento")
        void shouldRecalculateCancellationRate() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarApolices(10L, new BigDecimal("1000000"), new BigDecimal("12000"));

            proj.registrarCancelamento(new BigDecimal("100000"), new BigDecimal("1200"));

            // 1 cancelado / 10 total = 10%
            assertThat(proj.getTaxaCancelamento()).isEqualByComparingTo(new BigDecimal("10.0000"));
        }
    }

    // =========================================================================
    // registrarRenovacao
    // =========================================================================

    @Nested
    @DisplayName("registrarRenovacao()")
    class RegistrarRenovacao {

        @Test
        @DisplayName("Deve incrementar renovacoes e acumular valores")
        void shouldIncrementRenovacoesAndAccumulateValues() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarApolices(5L, new BigDecimal("500000"), new BigDecimal("6000"));

            proj.registrarRenovacao(new BigDecimal("100000"), new BigDecimal("1200"));

            assertThat(proj.getRenovacoes()).isEqualTo(1L);
            assertThat(proj.getPremioTotal()).isEqualByComparingTo(new BigDecimal("7200"));
        }

        @Test
        @DisplayName("Deve recalcular taxaRenovacao")
        void shouldRecalculateRenovacaoRate() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarApolices(10L, new BigDecimal("1000000"), new BigDecimal("12000"));

            proj.registrarRenovacao(new BigDecimal("100000"), new BigDecimal("1200"));

            // 1 renovação / 10 total = 10%
            assertThat(proj.getTaxaRenovacao()).isEqualByComparingTo(new BigDecimal("10.0000"));
        }
    }

    // =========================================================================
    // incrementarFaixaEtaria
    // =========================================================================

    @Nested
    @DisplayName("incrementarFaixaEtaria()")
    class IncrementarFaixaEtaria {

        @Test
        @DisplayName("Deve incrementar faixa18a25 para idade 18-25")
        void shouldIncrementFaixa18a25() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarFaixaEtaria(20);
            assertThat(proj.getFaixa18a25()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve incrementar faixa26a35 para idade 26-35")
        void shouldIncrementFaixa26a35() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarFaixaEtaria(30);
            assertThat(proj.getFaixa26a35()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve incrementar faixa65Mais para idade > 65")
        void shouldIncrementFaixa65Mais() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarFaixaEtaria(70);
            assertThat(proj.getFaixa65Mais()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Não deve incrementar nenhuma faixa para idade < 18")
        void shouldNotIncrementAnyFaixaForAgeUnder18() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarFaixaEtaria(15);
            assertThat(proj.getFaixa18a25()).isZero();
            assertThat(proj.getFaixa26a35()).isZero();
        }
    }

    // =========================================================================
    // incrementarRegiao
    // =========================================================================

    @Nested
    @DisplayName("incrementarRegiao()")
    class IncrementarRegiao {

        @Test
        @DisplayName("SP deve incrementar regiaoSudeste")
        void spShouldIncrementRegiaoSudeste() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("SP");
            assertThat(proj.getRegiaoSudeste()).isEqualTo(1L);
        }

        @Test
        @DisplayName("BA deve incrementar regiaoNordeste")
        void baShouldIncrementRegiaoNordeste() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("BA");
            assertThat(proj.getRegiaoNordeste()).isEqualTo(1L);
        }

        @Test
        @DisplayName("AM deve incrementar regiaoNorte")
        void amShouldIncrementRegiaoNorte() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("AM");
            assertThat(proj.getRegiaoNorte()).isEqualTo(1L);
        }

        @Test
        @DisplayName("PR deve incrementar regiaoSul")
        void prShouldIncrementRegiaoSul() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("PR");
            assertThat(proj.getRegiaoSul()).isEqualTo(1L);
        }

        @Test
        @DisplayName("GO deve incrementar regiaoCentroOeste")
        void goShouldIncrementRegiaoCentroOeste() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("GO");
            assertThat(proj.getRegiaoCentroOeste()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Estado em lowercase deve funcionar")
        void lowercaseStateShouldWork() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarRegiao("sp");
            assertThat(proj.getRegiaoSudeste()).isEqualTo(1L);
        }
    }

    // =========================================================================
    // incrementarCanal
    // =========================================================================

    @Nested
    @DisplayName("incrementarCanal()")
    class IncrementarCanal {

        @Test
        @DisplayName("ONLINE deve incrementar canalOnline")
        void onlineShouldIncrementCanalOnline() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarCanal("ONLINE");
            assertThat(proj.getCanalOnline()).isEqualTo(1L);
        }

        @Test
        @DisplayName("WEB e SITE também devem incrementar canalOnline")
        void webAndSiteShouldIncrementCanalOnline() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarCanal("WEB");
            proj.incrementarCanal("SITE");
            assertThat(proj.getCanalOnline()).isEqualTo(2L);
        }

        @Test
        @DisplayName("TELEFONE deve incrementar canalTelefone")
        void telefoneShouldIncrementCanalTelefone() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarCanal("TELEFONE");
            assertThat(proj.getCanalTelefone()).isEqualTo(1L);
        }

        @Test
        @DisplayName("CORRETOR deve incrementar canalCorretor")
        void corretorShouldIncrementCanalCorretor() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarCanal("CORRETOR");
            assertThat(proj.getCanalCorretor()).isEqualTo(1L);
        }

        @Test
        @DisplayName("AGENCIA deve incrementar canalAgencia")
        void agenciaShouldIncrementCanalAgencia() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.incrementarCanal("AGENCIA");
            assertThat(proj.getCanalAgencia()).isEqualTo(1L);
        }
    }

    // =========================================================================
    // Callbacks JPA
    // =========================================================================

    @Nested
    @DisplayName("Callbacks JPA")
    class JpaCallbacks {

        @Test
        @DisplayName("onCreate deve estar anotado com @PrePersist")
        void onCreateShouldBeAnnotatedWithPrePersist() throws NoSuchMethodException {
            var method = AnalyticsProjection.class.getMethod("onCreate");
            assertThat(method.isAnnotationPresent(PrePersist.class)).isTrue();
        }

        @Test
        @DisplayName("onUpdate deve estar anotado com @PreUpdate")
        void onUpdateShouldBeAnnotatedWithPreUpdate() throws NoSuchMethodException {
            var method = AnalyticsProjection.class.getMethod("onUpdate");
            assertThat(method.isAnnotationPresent(PreUpdate.class)).isTrue();
        }

        @Test
        @DisplayName("onCreate deve definir createdAt quando nulo")
        void onCreateShouldSetCreatedAtWhenNull() {
            AnalyticsProjection proj = AnalyticsProjection.builder().build();
            proj.onCreate();
            assertThat(proj.getCreatedAt()).isNotNull();
            assertThat(proj.getUpdatedAt()).isNotNull();
        }

        @Test
        @DisplayName("onUpdate deve incrementar version")
        void onUpdateShouldIncrementVersion() {
            AnalyticsProjection proj = AnalyticsProjection.builder()
                    .dataReferencia(LocalDate.now())
                    .build();
            proj.onCreate();
            Long versaoAntes = proj.getVersion();

            proj.onUpdate();

            assertThat(proj.getVersion()).isEqualTo(versaoAntes + 1);
        }
    }
}
