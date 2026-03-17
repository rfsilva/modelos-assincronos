package com.seguradora.hibrida;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * Testes unitários para {@link ArquiteturaHibridaApplication}.
 */
@DisplayName("ArquiteturaHibridaApplication Tests")
class ArquiteturaHibridaApplicationTests {

    @Test
    @DisplayName("Deve ter anotações Spring Boot configuradas")
    void shouldHaveSpringBootAnnotations() {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // Then
        assertThatCode(() -> appClass.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class))
                .doesNotThrowAnyException();
        assertThatCode(() -> appClass.getAnnotation(org.springframework.cache.annotation.EnableCaching.class))
                .doesNotThrowAnyException();
        assertThatCode(() -> appClass.getAnnotation(org.springframework.kafka.annotation.EnableKafka.class))
                .doesNotThrowAnyException();
        assertThatCode(() -> appClass.getAnnotation(org.springframework.scheduling.annotation.EnableAsync.class))
                .doesNotThrowAnyException();
        assertThatCode(() -> appClass.getAnnotation(org.springframework.transaction.annotation.EnableTransactionManagement.class))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Deve ter anotação @SpringBootApplication presente")
    void shouldHaveSpringBootApplicationAnnotation() {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // Then
        org.springframework.boot.autoconfigure.SpringBootApplication annotation =
                appClass.getAnnotation(org.springframework.boot.autoconfigure.SpringBootApplication.class);
        org.assertj.core.api.Assertions.assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve ter anotação @EnableCaching presente")
    void shouldHaveEnableCachingAnnotation() {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // Then
        org.springframework.cache.annotation.EnableCaching annotation =
                appClass.getAnnotation(org.springframework.cache.annotation.EnableCaching.class);
        org.assertj.core.api.Assertions.assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve ter anotação @EnableAsync presente")
    void shouldHaveEnableAsyncAnnotation() {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // Then
        org.springframework.scheduling.annotation.EnableAsync annotation =
                appClass.getAnnotation(org.springframework.scheduling.annotation.EnableAsync.class);
        org.assertj.core.api.Assertions.assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve ter anotação @EnableTransactionManagement presente")
    void shouldHaveEnableTransactionManagementAnnotation() {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // Then
        org.springframework.transaction.annotation.EnableTransactionManagement annotation =
                appClass.getAnnotation(org.springframework.transaction.annotation.EnableTransactionManagement.class);
        org.assertj.core.api.Assertions.assertThat(annotation).isNotNull();
    }

    @Test
    @DisplayName("Deve ter método main público e estático")
    void shouldHavePublicStaticMainMethod() throws NoSuchMethodException {
        // Given
        Class<?> appClass = ArquiteturaHibridaApplication.class;

        // When
        java.lang.reflect.Method mainMethod = appClass.getMethod("main", String[].class);

        // Then
        org.assertj.core.api.Assertions.assertThat(mainMethod).isNotNull();
        org.assertj.core.api.Assertions.assertThat(
                java.lang.reflect.Modifier.isPublic(mainMethod.getModifiers())).isTrue();
        org.assertj.core.api.Assertions.assertThat(
                java.lang.reflect.Modifier.isStatic(mainMethod.getModifiers())).isTrue();
    }
}
