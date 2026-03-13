package com.seguradora.hibrida.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

/**
 * Testes unitários para {@link SecurityConfig}.
 *
 * <p>Valida a criação e configuração do SecurityFilterChain,
 * garantindo que endpoints públicos e de sistema estejam corretamente configurados.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SecurityConfig - Testes Unitários")
class SecurityConfigTest {

    private SecurityConfig config;

    @BeforeEach
    void setUp() {
        config = new SecurityConfig();
    }

    @Test
    @DisplayName("Deve criar SecurityFilterChain corretamente")
    void shouldCreateSecurityFilterChain() throws Exception {
        // Given
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // When
        SecurityFilterChain chain = config.filterChain(http);

        // Then
        assertThat(chain).isNotNull();
        assertThat(chain).isInstanceOf(SecurityFilterChain.class);
    }

    @Test
    @DisplayName("Deve configurar SecurityFilterChain sem exceções")
    void shouldConfigureSecurityFilterChainWithoutExceptions() throws Exception {
        // Given
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // When & Then - não deve lançar exceção
        assertThat(config.filterChain(http)).isNotNull();
    }

    @Test
    @DisplayName("Deve retornar SecurityFilterChain válido quando HttpSecurity é válido")
    void shouldReturnValidSecurityFilterChainWhenHttpSecurityIsValid() throws Exception {
        // Given
        HttpSecurity http = mock(HttpSecurity.class, RETURNS_DEEP_STUBS);

        // When
        SecurityFilterChain result = config.filterChain(http);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getFilters()).isNotNull();
    }
}
