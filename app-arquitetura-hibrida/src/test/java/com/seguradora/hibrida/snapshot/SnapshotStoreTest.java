package com.seguradora.hibrida.snapshot;

import com.seguradora.hibrida.snapshot.model.AggregateSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a interface {@link SnapshotStore}.
 */
@DisplayName("SnapshotStore Tests")
class SnapshotStoreTest {

    // =========================================================================
    // Contrato da interface — métodos declarados
    // =========================================================================

    @Test
    @DisplayName("Deve declarar método saveSnapshot(AggregateSnapshot)")
    void shouldDeclareSaveSnapshot() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("saveSnapshot", AggregateSnapshot.class);
        assertThat(m.getReturnType()).isEqualTo(void.class);
    }

    @Test
    @DisplayName("Deve declarar método getLatestSnapshot(String) retornando Optional")
    void shouldDeclareGetLatestSnapshot() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getLatestSnapshot", String.class);
        assertThat(m.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("Deve declarar método getSnapshotAtOrBeforeVersion(String, long) retornando Optional")
    void shouldDeclareGetSnapshotAtOrBeforeVersion() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getSnapshotAtOrBeforeVersion", String.class, long.class);
        assertThat(m.getReturnType()).isEqualTo(Optional.class);
    }

    @Test
    @DisplayName("Deve declarar método getSnapshotHistory(String) retornando List")
    void shouldDeclareGetSnapshotHistory() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getSnapshotHistory", String.class);
        assertThat(m.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve declarar método cleanupOldSnapshots(String, int) retornando int")
    void shouldDeclareCleanupOldSnapshots() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("cleanupOldSnapshots", String.class, int.class);
        assertThat(m.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("Deve declarar método cleanupAllOldSnapshots(int) retornando int")
    void shouldDeclareCleanupAllOldSnapshots() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("cleanupAllOldSnapshots", int.class);
        assertThat(m.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("Deve declarar método hasSnapshots(String) retornando boolean")
    void shouldDeclareHasSnapshots() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("hasSnapshots", String.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método getSnapshotStatistics(String) retornando SnapshotStatistics")
    void shouldDeclareGetSnapshotStatistics() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getSnapshotStatistics", String.class);
        assertThat(m.getReturnType()).isEqualTo(SnapshotStatistics.class);
    }

    @Test
    @DisplayName("Deve declarar método getGlobalStatistics() retornando SnapshotStatistics")
    void shouldDeclareGetGlobalStatistics() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getGlobalStatistics");
        assertThat(m.getReturnType()).isEqualTo(SnapshotStatistics.class);
    }

    @Test
    @DisplayName("Deve declarar método shouldCreateSnapshot(String, long) retornando boolean")
    void shouldDeclareShouldCreateSnapshot() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("shouldCreateSnapshot", String.class, long.class);
        assertThat(m.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve declarar método deleteAllSnapshots(String) retornando int")
    void shouldDeclareDeleteAllSnapshots() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("deleteAllSnapshots", String.class);
        assertThat(m.getReturnType()).isEqualTo(int.class);
    }

    @Test
    @DisplayName("Deve declarar método getEfficiencyMetrics(String, int) retornando SnapshotEfficiencyMetrics")
    void shouldDeclareGetEfficiencyMetrics() throws NoSuchMethodException {
        Method m = SnapshotStore.class.getMethod("getEfficiencyMetrics", String.class, int.class);
        assertThat(m.getReturnType()).isEqualTo(SnapshotEfficiencyMetrics.class);
    }

    @Test
    @DisplayName("Deve ter exatamente 12 métodos na interface")
    void shouldHaveTwelveMethodsInInterface() {
        Set<String> methodNames = Arrays.stream(SnapshotStore.class.getMethods())
                .map(Method::getName)
                .collect(Collectors.toSet());

        assertThat(methodNames).containsExactlyInAnyOrder(
                "saveSnapshot", "getLatestSnapshot", "getSnapshotAtOrBeforeVersion",
                "getSnapshotHistory", "cleanupOldSnapshots", "cleanupAllOldSnapshots",
                "hasSnapshots", "getSnapshotStatistics", "getGlobalStatistics",
                "shouldCreateSnapshot", "deleteAllSnapshots", "getEfficiencyMetrics"
        );
    }

    @Test
    @DisplayName("Deve ser uma interface")
    void shouldBeAnInterface() {
        assertThat(SnapshotStore.class.isInterface()).isTrue();
    }
}
