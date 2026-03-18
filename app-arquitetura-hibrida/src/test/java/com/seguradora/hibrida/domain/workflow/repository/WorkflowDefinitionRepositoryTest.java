package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.model.WorkflowDefinition;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WorkflowDefinitionRepository Tests")
class WorkflowDefinitionRepositoryTest {

    @Test
    @DisplayName("Deve ter anotação @Repository")
    void shouldHaveRepositoryAnnotation() {
        assertThat(WorkflowDefinitionRepository.class)
                .hasAnnotation(Repository.class);
    }

    @Test
    @DisplayName("Deve ser interface")
    void shouldBeInterface() {
        assertThat(WorkflowDefinitionRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("Deve estender JpaRepository")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = Arrays.stream(WorkflowDefinitionRepository.class.getInterfaces())
                .anyMatch(i -> i.equals(JpaRepository.class));
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findByTipoSinistroAndAtivoTrue")
    void shouldHaveFindByTipoSinistroAndAtivoTrue() throws NoSuchMethodException {
        Method method = WorkflowDefinitionRepository.class
                .getDeclaredMethod("findByTipoSinistroAndAtivoTrue", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findLatestByTipoSinistro com @Query")
    void shouldHaveFindLatestByTipoSinistroWithQuery() {
        List<Method> methods = Arrays.asList(WorkflowDefinitionRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findLatestByTipoSinistro")
                        && m.isAnnotationPresent(Query.class));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("findLatestByTipoSinistro deve retornar Optional")
    void findLatestByTipoSinistroShouldReturnOptional() {
        List<Method> methods = Arrays.asList(WorkflowDefinitionRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findLatestByTipoSinistro")
                        && m.getReturnType().equals(Optional.class));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findByAtivoTrue")
    void shouldHaveFindByAtivoTrue() {
        List<Method> methods = Arrays.asList(WorkflowDefinitionRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findByAtivoTrue"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método existsByTipoSinistroAndAtivoTrue")
    void shouldHaveExistsByTipoSinistroAndAtivoTrue() throws NoSuchMethodException {
        Method method = WorkflowDefinitionRepository.class
                .getDeclaredMethod("existsByTipoSinistroAndAtivoTrue", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(boolean.class);
    }

    @Test
    @DisplayName("Deve ter método countByAtivoTrue")
    void shouldHaveCountByAtivoTrue() {
        List<Method> methods = Arrays.asList(WorkflowDefinitionRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("countByAtivoTrue"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findByNomeContainingIgnoreCase")
    void shouldHaveFindByNomeContainingIgnoreCase() throws NoSuchMethodException {
        Method method = WorkflowDefinitionRepository.class
                .getDeclaredMethod("findByNomeContainingIgnoreCase", String.class);
        assertThat(method).isNotNull();
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ter método findAllByTipoSinistroOrderByVersaoDesc com @Query")
    void shouldHaveFindAllByTipoSinistroOrderByVersaoDesc() {
        List<Method> methods = Arrays.asList(WorkflowDefinitionRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findAllByTipoSinistroOrderByVersaoDesc")
                        && m.isAnnotationPresent(Query.class));
        assertThat(found).isTrue();
    }
}
