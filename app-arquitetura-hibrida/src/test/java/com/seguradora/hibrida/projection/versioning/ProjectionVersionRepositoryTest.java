package com.seguradora.hibrida.projection.versioning;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para {@link ProjectionVersionRepository}.
 */
@DisplayName("ProjectionVersionRepository Tests")
class ProjectionVersionRepositoryTest {

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(ProjectionVersionRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @Repository")
    void shouldBeAnnotatedWithRepository() {
        assertThat(ProjectionVersionRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = false;
        for (Class<?> iface : ProjectionVersionRepository.class.getInterfaces()) {
            if (JpaRepository.class.isAssignableFrom(iface)) {
                extendsJpa = true;
                break;
            }
        }
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve declarar método findFirstByProjectionNameOrderByVersionDesc")
    void shouldDeclareFindFirstByProjectionNameOrderByVersionDesc() throws NoSuchMethodException {
        assertThat(ProjectionVersionRepository.class.getMethod(
                "findFirstByProjectionNameOrderByVersionDesc", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método existsByProjectionNameAndVersion")
    void shouldDeclareExistsByProjectionNameAndVersion() throws NoSuchMethodException {
        assertThat(ProjectionVersionRepository.class.getMethod(
                "existsByProjectionNameAndVersion", String.class, Integer.class))
                .isNotNull();
    }
}
