package com.seguradora.hibrida.config.datasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DataSourceConfiguration}.
 */
@DisplayName("DataSourceConfiguration Tests")
class DataSourceConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(DataSourceConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableTransactionManagement")
        void shouldBeAnnotatedWithEnableTransactionManagement() {
            assertThat(DataSourceConfiguration.class.isAnnotationPresent(EnableTransactionManagement.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableConfigurationProperties")
        void shouldBeAnnotatedWithEnableConfigurationProperties() {
            assertThat(DataSourceConfiguration.class.isAnnotationPresent(EnableConfigurationProperties.class)).isTrue();
        }

        @Test
        @DisplayName("@EnableConfigurationProperties deve incluir WriteDataSourceProperties e ReadDataSourceProperties")
        void enableConfigPropertiesShouldIncludeWriteAndReadProperties() {
            EnableConfigurationProperties annotation =
                    DataSourceConfiguration.class.getAnnotation(EnableConfigurationProperties.class);
            Set<Class<?>> classes = Set.of(annotation.value());

            assertThat(classes).contains(WriteDataSourceProperties.class, ReadDataSourceProperties.class);
        }
    }

    // =========================================================================
    // Declaração de beans (@Bean)
    // =========================================================================

    @Nested
    @DisplayName("Declaração de métodos @Bean")
    class DeclaracaoDeBeans {

        private Set<String> beanMethodNames() {
            return Arrays.stream(DataSourceConfiguration.class.getDeclaredMethods())
                    .filter(m -> m.isAnnotationPresent(org.springframework.context.annotation.Bean.class))
                    .map(Method::getName)
                    .collect(Collectors.toSet());
        }

        @Test
        @DisplayName("Deve declarar bean writeDataSource")
        void shouldDeclareWriteDataSourceBean() {
            assertThat(beanMethodNames()).contains("writeDataSource");
        }

        @Test
        @DisplayName("Deve declarar bean readDataSource")
        void shouldDeclareReadDataSourceBean() {
            assertThat(beanMethodNames()).contains("readDataSource");
        }

        @Test
        @DisplayName("Deve declarar bean writeEntityManagerFactory")
        void shouldDeclareWriteEntityManagerFactoryBean() {
            assertThat(beanMethodNames()).contains("writeEntityManagerFactory");
        }

        @Test
        @DisplayName("Deve declarar bean readEntityManagerFactory")
        void shouldDeclareReadEntityManagerFactoryBean() {
            assertThat(beanMethodNames()).contains("readEntityManagerFactory");
        }

        @Test
        @DisplayName("Deve declarar bean writeTransactionManager")
        void shouldDeclareWriteTransactionManagerBean() {
            assertThat(beanMethodNames()).contains("writeTransactionManager");
        }

        @Test
        @DisplayName("Deve declarar bean readTransactionManager")
        void shouldDeclareReadTransactionManagerBean() {
            assertThat(beanMethodNames()).contains("readTransactionManager");
        }

        @Test
        @DisplayName("Deve declarar bean writeDataSourceHealthIndicator")
        void shouldDeclareWriteHealthIndicatorBean() {
            assertThat(beanMethodNames()).contains("writeDataSourceHealthIndicator");
        }

        @Test
        @DisplayName("Deve declarar bean readDataSourceHealthIndicator")
        void shouldDeclareReadHealthIndicatorBean() {
            assertThat(beanMethodNames()).contains("readDataSourceHealthIndicator");
        }
    }

    // =========================================================================
    // @Primary em writeDataSource e writeEntityManagerFactory
    // =========================================================================

    @Nested
    @DisplayName("@Primary nos beans de escrita")
    class PrimaryBeans {

        @Test
        @DisplayName("writeDataSource deve ser @Primary")
        void writeDataSourceShouldBePrimary() throws NoSuchMethodException {
            Method method = DataSourceConfiguration.class.getDeclaredMethod("writeDataSource");
            assertThat(method.isAnnotationPresent(Primary.class)).isTrue();
        }

        @Test
        @DisplayName("writeEntityManagerFactory deve ser @Primary")
        void writeEntityManagerFactoryShouldBePrimary() throws NoSuchMethodException {
            Method method = DataSourceConfiguration.class.getDeclaredMethod("writeEntityManagerFactory");
            assertThat(method.isAnnotationPresent(Primary.class)).isTrue();
        }

        @Test
        @DisplayName("writeTransactionManager deve ser @Primary")
        void writeTransactionManagerShouldBePrimary() throws NoSuchMethodException {
            Method method = DataSourceConfiguration.class.getDeclaredMethod("writeTransactionManager");
            assertThat(method.isAnnotationPresent(Primary.class)).isTrue();
        }
    }
}
