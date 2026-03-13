package com.seguradora.hibrida.domain.segurado.service;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ViaCepService}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ViaCepService - Testes Unitários")
class ViaCepServiceTest {

    @Mock
    private RestTemplate restTemplate;

    private ViaCepService service;

    @BeforeEach
    void setUp() {
        service = new ViaCepService(restTemplate);
    }

    @Test
    @DisplayName("Deve retornar null para CEP com formato inválido")
    void shouldReturnNullForInvalidCepFormat() {
        // Given
        String cepInvalido = "123";

        // When
        Endereco result = service.consultarCep(cepInvalido);

        // Then
        assertThat(result).isNull();
        verify(restTemplate, never()).getForEntity(anyString(), any(), anyString());
    }

    @Test
    @DisplayName("Deve retornar null quando ViaCEP retorna erro")
    void shouldReturnNullWhenViaCepReturnsError() {
        // Given
        String cep = "99999999";
        ViaCepService.ViaCepResponse errorResponse = new ViaCepService.ViaCepResponse();
        errorResponse.setErro(true);

        when(restTemplate.getForEntity(anyString(), eq(ViaCepService.ViaCepResponse.class), eq(cep)))
            .thenReturn(new ResponseEntity<>(errorResponse, HttpStatus.OK));

        // When
        Endereco result = service.consultarCep(cep);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve retornar null quando resposta é inválida")
    void shouldReturnNullWhenResponseIsInvalid() {
        // Given
        String cep = "01310100";

        when(restTemplate.getForEntity(anyString(), eq(ViaCepService.ViaCepResponse.class), eq(cep)))
            .thenReturn(new ResponseEntity<>(null, HttpStatus.OK));

        // When
        Endereco result = service.consultarCep(cep);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve retornar null quando ocorre exceção")
    void shouldReturnNullWhenExceptionOccurs() {
        // Given
        String cep = "01310100";

        when(restTemplate.getForEntity(anyString(), eq(ViaCepService.ViaCepResponse.class), eq(cep)))
            .thenThrow(new RuntimeException("Erro de rede"));

        // When
        Endereco result = service.consultarCep(cep);

        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Deve validar formato de CEP corretamente")
    void shouldValidateCepFormatCorrectly() {
        // Given - CEPs válidos
        assertThatNoException().isThrownBy(() -> service.consultarCep("01310100"));

        // Given - CEPs inválidos
        assertThat(service.consultarCep("123")).isNull();
        assertThat(service.consultarCep("abc12345")).isNull();
        assertThat(service.consultarCep("")).isNull();
        assertThat(service.consultarCep(null)).isNull();
    }
}
