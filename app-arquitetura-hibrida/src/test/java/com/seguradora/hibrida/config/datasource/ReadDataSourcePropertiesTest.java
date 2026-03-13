package com.seguradora.hibrida.config.datasource;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReadDataSourceProperties Tests")
class ReadDataSourcePropertiesTest {

    private ReadDataSourceProperties properties;

    @BeforeEach
    void setUp() {
        properties = new ReadDataSourceProperties();
    }

    @Test
    @DisplayName("Deve criar instância")
    void shouldCreateInstance() {
        assertThat(properties).isNotNull();
    }

    @Test
    @DisplayName("Deve configurar url")
    void shouldConfigureUrl() {
        properties.setUrl("jdbc:postgresql://localhost:5432/readdb");
        assertThat(properties.getUrl()).isEqualTo("jdbc:postgresql://localhost:5432/readdb");
    }

    @Test
    @DisplayName("Deve configurar username")
    void shouldConfigureUsername() {
        properties.setUsername("readuser");
        assertThat(properties.getUsername()).isEqualTo("readuser");
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
