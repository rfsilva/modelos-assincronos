package com.seguradora.hibrida.config.datasource;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.actuate.health.Status;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.SQLException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * Testes unitários para {@link SimpleDataSourceHealthIndicator}.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SimpleDataSourceHealthIndicator Tests")
class SimpleDataSourceHealthIndicatorTest {

    @Mock
    private DataSource dataSource;

    @Mock
    private Connection connection;

    @Mock
    private DatabaseMetaData metaData;

    // =========================================================================
    // Hierarquia de tipos
    // =========================================================================

    @Test
    @DisplayName("Deve implementar HealthIndicator")
    void shouldImplementHealthIndicator() {
        SimpleDataSourceHealthIndicator indicator =
                new SimpleDataSourceHealthIndicator(dataSource, "TestDS");
        assertThat(indicator).isInstanceOf(HealthIndicator.class);
    }

    // =========================================================================
    // Construtor com testQuery padrão
    // =========================================================================

    @Nested
    @DisplayName("health() — query padrão 'SELECT 1'")
    class HealthComQueryPadrao {

        @Test
        @DisplayName("Deve retornar DOWN quando o DataSource lança exceção na conexão")
        void shouldReturnDownWhenDataSourceThrows() {
            // Given – DataSource que falha ao conectar
            DataSource faultyDs = new FaultyDataSource();
            SimpleDataSourceHealthIndicator indicator =
                    new SimpleDataSourceHealthIndicator(faultyDs, "FaultyDS");

            // When
            Health health = indicator.health();

            // Then
            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
            assertThat(health.getDetails()).containsKey("error");
        }

        @Test
        @DisplayName("Deve incluir 'datasource' nos detalhes do Health DOWN")
        void shouldIncludeDatasourceDetailOnDown() {
            DataSource faultyDs = new FaultyDataSource();
            SimpleDataSourceHealthIndicator indicator =
                    new SimpleDataSourceHealthIndicator(faultyDs, "NomeDS");

            Health health = indicator.health();

            assertThat(health.getDetails().get("datasource")).isEqualTo("NomeDS");
        }
    }

    // =========================================================================
    // Construtor com testQuery personalizada
    // =========================================================================

    @Nested
    @DisplayName("Construtor com testQuery customizada")
    class ConstrutorComQueryCustomizada {

        @Test
        @DisplayName("Deve aceitar query customizada sem falhar na construção")
        void shouldAcceptCustomTestQuery() {
            SimpleDataSourceHealthIndicator indicator =
                    new SimpleDataSourceHealthIndicator(dataSource, "DS", "SELECT 42");
            assertThat(indicator).isNotNull();
        }

        @Test
        @DisplayName("Deve retornar DOWN com query customizada quando DataSource falha")
        void shouldReturnDownWithCustomQueryOnFailure() {
            DataSource faultyDs = new FaultyDataSource();
            SimpleDataSourceHealthIndicator indicator =
                    new SimpleDataSourceHealthIndicator(faultyDs, "DS", "SELECT 42");

            Health health = indicator.health();

            assertThat(health.getStatus()).isEqualTo(Status.DOWN);
        }
    }

    // =========================================================================
    // Helpers internos
    // =========================================================================

    /**
     * DataSource que lança RuntimeException em qualquer tentativa de uso.
     * Não implementa a interface completa de DataSource – apenas getConnection.
     */
    private static class FaultyDataSource implements javax.sql.DataSource {
        @Override
        public Connection getConnection() throws SQLException {
            throw new SQLException("Conexão recusada");
        }
        @Override
        public Connection getConnection(String username, String password) throws SQLException {
            throw new SQLException("Conexão recusada");
        }
        @Override
        public java.io.PrintWriter getLogWriter() { return null; }
        @Override
        public void setLogWriter(java.io.PrintWriter out) {}
        @Override
        public void setLoginTimeout(int seconds) {}
        @Override
        public int getLoginTimeout() { return 0; }
        @Override
        public java.util.logging.Logger getParentLogger() { return null; }
        @Override
        public <T> T unwrap(Class<T> iface) { return null; }
        @Override
        public boolean isWrapperFor(Class<?> iface) { return false; }
    }
}
