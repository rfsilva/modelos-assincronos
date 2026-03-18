package com.seguradora.hibrida.domain.workflow.repository;

import com.seguradora.hibrida.domain.workflow.approval.Aprovacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AprovacaoRepository Tests")
class AprovacaoRepositoryTest {

    @Test
    @DisplayName("Deve ter anotação @Repository")
    void shouldHaveRepositoryAnnotation() {
        assertThat(AprovacaoRepository.class)
                .hasAnnotation(Repository.class);
    }

    @Test
    @DisplayName("Deve estender JpaRepository com Aprovacao e String")
    void shouldExtendJpaRepository() {
        boolean extendsJpa = Arrays.stream(AprovacaoRepository.class.getInterfaces())
                .anyMatch(i -> i.equals(JpaRepository.class));
        assertThat(extendsJpa).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findBySinistroId")
    void shouldHaveFindBySinistroId() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findBySinistroId"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findByStatusIn")
    void shouldHaveFindByStatusIn() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findByStatusIn"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findBySinistroIdAndStatusIn")
    void shouldHaveFindBySinistroIdAndStatusIn() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findBySinistroIdAndStatusIn"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findByAprovadoresContainingAndStatusIn")
    void shouldHaveFindByAprovadoresContainingAndStatusIn() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findByAprovadoresContainingAndStatusIn"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método findPendentesPorAprovador com @Query")
    void shouldHaveFindPendentesPorAprovadorWithQuery() throws NoSuchMethodException {
        Method method = AprovacaoRepository.class.getDeclaredMethod(
                "findPendentesPorAprovador", String.class);
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método countPendentesPorAprovador com @Query")
    void shouldHaveCountPendentesPorAprovadorWithQuery() throws NoSuchMethodException {
        Method method = AprovacaoRepository.class.getDeclaredMethod(
                "countPendentesPorAprovador", String.class);
        assertThat(method).isNotNull();
        assertThat(method.isAnnotationPresent(Query.class)).isTrue();
    }

    @Test
    @DisplayName("Deve ter método calcularTempoMedioPorNivel com @Query")
    void shouldHaveCalcularTempoMedioPorNivelWithQuery() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("calcularTempoMedioPorNivel")
                        && m.isAnnotationPresent(Query.class));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("Deve ter método countByStatusAndNivel")
    void shouldHaveCountByStatusAndNivel() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("countByStatusAndNivel"));
        assertThat(found).isTrue();
    }

    @Test
    @DisplayName("findBySinistroId deve retornar List<Aprovacao>")
    void findBySinistroIdShouldReturnListOfAprovacao() throws NoSuchMethodException {
        Method method = AprovacaoRepository.class.getDeclaredMethod("findBySinistroId", String.class);
        assertThat(method.getReturnType()).isEqualTo(List.class);
    }

    @Test
    @DisplayName("Deve ser interface")
    void shouldBeInterface() {
        assertThat(AprovacaoRepository.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("findProximasDoTimeout deve ter @Query")
    void findProximasDoTimeoutShouldHaveQuery() {
        List<Method> methods = Arrays.asList(AprovacaoRepository.class.getDeclaredMethods());
        boolean found = methods.stream()
                .anyMatch(m -> m.getName().equals("findProximasDoTimeout")
                        && m.isAnnotationPresent(Query.class));
        assertThat(found).isTrue();
    }
}
