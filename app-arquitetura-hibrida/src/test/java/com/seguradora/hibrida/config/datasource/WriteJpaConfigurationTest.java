package com.seguradora.hibrida.config.datasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link WriteJpaConfiguration}.
 */
@DisplayName("WriteJpaConfiguration Tests")
class WriteJpaConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(WriteJpaConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableTransactionManagement")
        void shouldBeAnnotatedWithEnableTransactionManagement() {
            assertThat(WriteJpaConfiguration.class.isAnnotationPresent(EnableTransactionManagement.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableJpaRepositories")
        void shouldBeAnnotatedWithEnableJpaRepositories() {
            assertThat(WriteJpaConfiguration.class.isAnnotationPresent(EnableJpaRepositories.class)).isTrue();
        }
    }

    // =========================================================================
    // @EnableJpaRepositories — configuração de escrita
    // =========================================================================

    @Nested
    @DisplayName("@EnableJpaRepositories — side de escrita")
    class EnableJpaRepositoriesConfig {

        private EnableJpaRepositories annotation() {
            return WriteJpaConfiguration.class.getAnnotation(EnableJpaRepositories.class);
        }

        @Test
        @DisplayName("entityManagerFactoryRef deve ser 'writeEntityManagerFactory'")
        void entityManagerFactoryRefShouldBeWrite() {
            assertThat(annotation().entityManagerFactoryRef()).isEqualTo("writeEntityManagerFactory");
        }

        @Test
        @DisplayName("transactionManagerRef deve ser 'writeTransactionManager'")
        void transactionManagerRefShouldBeWrite() {
            assertThat(annotation().transactionManagerRef()).isEqualTo("writeTransactionManager");
        }

        @Test
        @DisplayName("basePackages deve incluir repositórios de eventstore e snapshot")
        void basePackagesShouldIncludeWriteRepositories() {
            Set<String> packages = Arrays.stream(annotation().basePackages()).collect(Collectors.toSet());
            assertThat(packages).contains(
                    "com.seguradora.hibrida.eventstore.repository",
                    "com.seguradora.hibrida.snapshot.repository"
            );
        }
    }
}
