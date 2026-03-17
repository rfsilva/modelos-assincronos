package com.seguradora.hibrida.snapshot.repository;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link SnapshotRepository}.
 */
@DisplayName("SnapshotRepository Tests")
class SnapshotRepositoryTest {

    // =========================================================================
    // Anotações e hierarquia
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Repository")
    void shouldBeAnnotatedWithRepository() {
        assertThat(SnapshotRepository.class.isAnnotationPresent(Repository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository<SnapshotEntry, String>")
    void shouldExtendJpaRepository() {
        assertThat(JpaRepository.class.isAssignableFrom(SnapshotRepository.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeInterface() {
        assertThat(SnapshotRepository.class.isInterface()).isTrue();
    }

    // =========================================================================
    // Métodos declarados
    // =========================================================================

    @Test
    @DisplayName("Deve declarar findByAggregateIdOrderByVersionDesc(String) retornando List")
    void shouldDeclareFindByAggregateIdOrderByVersionDesc() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("findByAggregateIdOrderByVersionDesc", String.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar findFirstByAggregateIdOrderByVersionDesc(String) retornando Optional")
    void shouldDeclareFindFirstByAggregateId() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("findFirstByAggregateIdOrderByVersionDesc", String.class);
        assertThat(m.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("Deve declarar findFirstByAggregateIdAndVersionLessThanEqualOrderByVersionDesc retornando Optional")
    void shouldDeclareFindAtOrBeforeVersion() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod(
                "findFirstByAggregateIdAndVersionLessThanEqualOrderByVersionDesc",
                String.class, Long.class);
        assertThat(m.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("Deve declarar existsByAggregateId(String) retornando boolean")
    void shouldDeclareExistsByAggregateId() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("existsByAggregateId", String.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar countByAggregateId(String) retornando long")
    void shouldDeclareCountByAggregateId() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("countByAggregateId", String.class);
        assertThat(m.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("Deve declarar deleteByAggregateId(String) retornando int")
    void shouldDeclareDeleteByAggregateId() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("deleteByAggregateId", String.class);
        assertThat(m.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("Deve declarar findByAggregateTypeOrderByTimestampDesc(String) retornando List")
    void shouldDeclareFindByAggregateType() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("findByAggregateTypeOrderByTimestampDesc", String.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar findByTimestampBetweenOrderByTimestampDesc retornando List")
    void shouldDeclareFindByTimestampBetween() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod(
                "findByTimestampBetweenOrderByTimestampDesc", Instant.class, Instant.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar findByTimestampBetween paginado retornando Page")
    void shouldDeclareFindByTimestampBetweenPaged() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod(
                "findByTimestampBetween", Instant.class, Instant.class, Pageable.class);
        assertThat(m.getReturnType()).isEqualTo(Page.class);
    }

    @Test
    @DisplayName("Deve declarar countByCompressedTrue() retornando long")
    void shouldDeclareCountByCompressedTrue() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("countByCompressedTrue");
        assertThat(m.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("Deve declarar countByTimestampGreaterThanEqual(Instant) retornando long")
    void shouldDeclareCountByTimestampGreaterThanEqual() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("countByTimestampGreaterThanEqual", Instant.class);
        assertThat(m.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("Deve declarar findAggregatesNeedingCleanup(long) retornando List")
    void shouldDeclareFindAggregatesNeedingCleanup() throws NoSuchMethodException {
        Method m = SnapshotRepository.class.getMethod("findAggregatesNeedingCleanup", long.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }
}
