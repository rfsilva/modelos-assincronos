package com.seguradora.hibrida.domain.analytics.repository;

import com.seguradora.hibrida.domain.analytics.model.AnalyticsProjection;
import com.seguradora.hibrida.domain.analytics.model.TipoMetrica;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link AnalyticsProjectionRepository}.
 */
@DisplayName("AnalyticsProjectionRepository Tests")
class AnalyticsProjectionRepositoryTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(AnalyticsProjectionRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Repository")
    void shouldBeAnnotatedWithRepository() {
        assertThat(AnalyticsProjectionRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository<AnalyticsProjection, String>")
    void shouldExtendJpaRepository() {
        assertThat(JpaRepository.class.isAssignableFrom(AnalyticsProjectionRepository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve declarar findByDataReferenciaAndTipoMetrica")
    void shouldDeclareFindByDataReferenciaAndTipoMetrica() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findByDataReferenciaAndTipoMetrica", LocalDate.class, TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByDataReferenciaAndTipoMetricaAndDimensaoAndValorDimensao")
    void shouldDeclareFindByDataReferenciaAndTipoMetricaAndDimensaoAndValorDimensao() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findByDataReferenciaAndTipoMetricaAndDimensaoAndValorDimensao",
                LocalDate.class, TipoMetrica.class, String.class, String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia")
    void shouldDeclareFindByPeriodAndTipoMetrica() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findByDataReferenciaBetweenAndTipoMetricaOrderByDataReferencia",
                LocalDate.class, LocalDate.class, TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findByTipoMetricaOrderByDataReferenciaDesc")
    void shouldDeclareFindByTipoMetricaOrderByDataReferenciaDesc() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findByTipoMetricaOrderByDataReferenciaDesc", TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findUltimosDias")
    void shouldDeclareFindUltimosDias() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findUltimosDias", LocalDate.class, TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findMetricasMensaisDoAno")
    void shouldDeclareFindMetricasMensaisDoAno() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod("findMetricasMensaisDoAno", int.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findMetricasPorRegiaoNaData")
    void shouldDeclareFindMetricasPorRegiaoNaData() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod("findMetricasPorRegiaoNaData", LocalDate.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar findTopRegioesPorApolices com Pageable")
    void shouldDeclareFindTopRegioesPorApolices() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "findTopRegioesPorApolices", LocalDate.class, Pageable.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar sumTotalSeguradosPorPeriodo")
    void shouldDeclareSumTotalSeguradosPorPeriodo() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "sumTotalSeguradosPorPeriodo", LocalDate.class, LocalDate.class, TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar countByTipoMetrica")
    void shouldDeclareCountByTipoMetrica() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod("countByTipoMetrica", TipoMetrica.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar existsByDataReferenciaAndTipoMetrica")
    void shouldDeclareExistsByDataReferenciaAndTipoMetrica() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionRepository.class.getMethod(
                "existsByDataReferenciaAndTipoMetrica", LocalDate.class, TipoMetrica.class))
                .isNotNull();
    }
}
