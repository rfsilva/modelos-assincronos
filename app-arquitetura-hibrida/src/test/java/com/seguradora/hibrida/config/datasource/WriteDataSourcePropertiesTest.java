package com.seguradora.hibrida.config.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("WriteDataSourceProperties Tests")
class WriteDataSourcePropertiesTest {

    private WriteDataSourceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new WriteDataSourceProperties();
    }

    @Test
    @DisplayName("Deve criar instância")
    void shouldCreateInstance() {
        assertThat(properties).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar url")
    void shouldConfigureUrl() {
        properties.setUrl("jdbc:postgresql://localhost:5432/writedb");
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/writedb");
    }

    @Test
    @DisplayName("Deve configurar username")
    void shouldConfigureUsername() {
        properties.setUsername("writeuser");
        assertThat(properties.getUsername()).isEqualTo("writeuser");
    }

    @Test
    @DisplayName("Deve configurar password")
    void shouldConfigurePassword() {
        properties.setPassword("password123");
        assertThat(properties.getPassword()).isEqualTo("password123");
    }

    @Test
    @DisplayName("Deve configurar driver-class-name")
    void shouldConfigureDriverClassName() {
        properties.setDriverClassName("org.postgresql.Driver");
        assertThat(properties.getDriverClassName()).isEqualTo("org.postgresql.Driver");
    }
}
