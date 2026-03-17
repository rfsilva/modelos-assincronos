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
 * Testes unitários para {@link ReadJpaConfiguration}.
 */
@DisplayName("ReadJpaConfiguration Tests")
class ReadJpaConfigurationTest {

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Nested
    @DisplayName("Anotações de classe")
    class AnotacoesDeClasse {

        @Test
        @DisplayName("Deve estar anotado com @Configuration")
        void shouldBeAnnotatedWithConfiguration() {
            assertThat(ReadJpaConfiguration.class.isAnnotationPresent(Configuration.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableTransactionManagement")
        void shouldBeAnnotatedWithEnableTransactionManagement() {
            assertThat(ReadJpaConfiguration.class.isAnnotationPresent(EnableTransactionManagement.class)).isTrue();
        }

        @Test
        @DisplayName("Deve estar anotado com @EnableJpaRepositories")
        void shouldBeAnnotatedWithEnableJpaRepositories() {
            assertThat(ReadJpaConfiguration.class.isAnnotationPresent(EnableJpaRepositories.class)).isTrue();
        }
    }

    // =========================================================================
    // @EnableJpaRepositories — configuração de leitura
    // =========================================================================

    @Nested
    @DisplayName("@EnableJpaRepositories — side de leitura")
    class EnableJpaRepositoriesConfig {

        private EnableJpaRepositories annotation() {
            return ReadJpaConfiguration.class.getAnnotation(EnableJpaRepositories.class);
        }

        @Test
        @DisplayName("entityManagerFactoryRef deve ser 'readEntityManagerFactory'")
        void entityManagerFactoryRefShouldBeRead() {
            assertThat(annotation().entityManagerFactoryRef()).isEqualTo("readEntityManagerFactory");
        }

        @Test
        @DisplayName("transactionManagerRef deve ser 'readTransactionManager'")
        void transactionManagerRefShouldBeRead() {
            assertThat(annotation().transactionManagerRef()).isEqualTo("readTransactionManager");
        }

        @Test
        @DisplayName("basePackages deve incluir repositórios query dos domínios")
        void basePackagesShouldIncludeQueryRepositories() {
            Set<String> packages = Arrays.stream(annotation().basePackages()).collect(Collectors.toSet());
            assertThat(packages).contains(
                    "com.seguradora.hibrida.domain.sinistro.query.repository",
                    "com.seguradora.hibrida.domain.segurado.query.repository",
                    "com.seguradora.hibrida.domain.veiculo.query.repository",
                    "com.seguradora.hibrida.domain.apolice.query.repository"
            );
        }
    }
}
