package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowInstance;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowInstanceRepository Tests")
class WorkflowInstanceRepositoryTest {

    @Test
    @DisplayName("Deve ter anotação @Repository")
    void shouldHaveRepositoryAnnotation() {
        assertThat(WorkflowInstanceRepository.class)
                .hasAnnotation(Repository.class);
    }

    @Test
    @DisplayName("Deve ser interface")
    void shouldBeInterface() {
        assertThat(WorkflowInstanceRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = Arrays.stream(WorkflowInstanceRepository.class.getInterfaces())
                .anyMatch(i -> i.equals(JpaRepository.class));
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findBySinistroId")
    void shouldHaveFindBySinistroId() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findBySinistroId", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findByStatus")
    void shouldHaveFindByStatus() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findByStatus", WorkflowInstance.StatusWorkflowInstance.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findByStatusIn")
    void shouldHaveFindByStatusIn() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findByStatusIn", List.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findByDefinicaoId")
    void shouldHaveFindByDefinicaoId() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findByDefinicaoId", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findByInicioEmBetween")
    void shouldHaveFindByInicioEmBetween() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findByInicioEmBetween", LocalDateTime.class, LocalDateTime.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findCompletosNoPeriodo com @Query")
    void shouldHaveFindCompletosNoPeriodoWithQuery() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findCompletosNoPeriodo", LocalDateTime.class, LocalDateTime.class);
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findAtivosAntigos com @Query")
    void shouldHaveFindAtivosAntigosWithQuery() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findAtivosAntigos", LocalDateTime.class);
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método countByStatus")
    void shouldHaveCountByStatus() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("countByStatus", WorkflowInstance.StatusWorkflowInstance.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(long.class);
    }

    @Test
    @DisplayName("Deve ter método findPendentes com @Query")
    void shouldHaveFindPendentesWithQuery() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class.getDeclaredMethod("findPendentes");
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método calcularTempoMedioPorTipo com @Query")
    void shouldHaveCalcularTempoMedioPorTipoWithQuery() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("calcularTempoMedioPorTipo", String.class);
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findBySinistroIdAndStatus")
    void shouldHaveFindBySinistroIdAndStatus() throws NoSuchMethodException {
        Method method = WorkflowInstanceRepository.class
                .getDeclaredMethod("findBySinistroIdAndStatus",
                        String.class, WorkflowInstance.StatusWorkflowInstance.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }
}
