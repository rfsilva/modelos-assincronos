package com.seguradora.hibrida.projection.tracking;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link ProjectionTrackerRepository}.
 */
@DisplayName("ProjectionTrackerRepository Tests")
class ProjectionTrackerRepositoryTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(ProjectionTrackerRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Repository")
    void shouldBeAnnotatedWithRepository() {
        assertThat(ProjectionTrackerRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository com chave String")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = false;
        for (Class<?> iface : ProjectionTrackerRepository.class.getInterfaces()) {
            if (JpaRepository.class.isAssignableFrom(iface)) {
                extendsJpa = true;
                break;
            }
        }
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método findByProjectionName")
    void shouldDeclareFindByProjectionName() throws NoSuchMethodException {
        assertThat(ProjectionTrackerRepository.class.getMethod(
                "findByProjectionName", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método findByStatus")
    void shouldDeclareFindByStatus() throws NoSuchMethodException {
        assertThat(ProjectionTrackerRepository.class.getMethod(
                "findByStatus", ProjectionStatus.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método countByStatus")
    void shouldDeclareCountByStatus() throws NoSuchMethodException {
        assertThat(ProjectionTrackerRepository.class.getMethod(
                "countByStatus", ProjectionStatus.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método existsByStatus")
    void shouldDeclareExistsByStatus() throws NoSuchMethodException {
        assertThat(ProjectionTrackerRepository.class.getMethod(
                "existsByStatus", ProjectionStatus.class))
                .isNotNull();
    }
}
