package com.seguradora.hibrida.domain.segurado.service;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link CepValidationService}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CepValidationService - Testes Unitários")
class CepValidationServiceTest {

    @Mock
    private ViaCepService viaCepService;

    @InjectMocks
    private CepValidationService service;

    @Test
    @DisplayName("Deve validar CEP via ViaCEP com sucesso")
    void shouldValidateCepViaViaCepSuccessfully() {
        // Given - usando CEP que passa na validação básica (não começa com 0)
        String cep = "20040020";
        Endereco enderecoEsperado = new Endereco(
            "Rua da Assembleia", "100", null, "Centro", "Rio de Janeiro", "RJ", "20040020"
        );

        when(viaCepService.consultarCep(cep)).thenReturn(enderecoEsperado);

        // When
        Endereco result = service.validarCep(cep);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getCep()).isEqualTo("20040020");

        verify(viaCepService).consultarCep(cep);
    }

    @Test
    @DisplayName("Deve retornar null para CEP com formato inválido")
    void shouldReturnNullForInvalidCepFormat() {
        // Given
        String cepInvalido = "123";

        // When
        Endereco result = service.validarCep(cepInvalido);

        // Then
        assertThat(result).isNull();
        verify(viaCepService, never()).consultarCep(anyString());
    }

    @Test
    @DisplayName("Deve usar fallback quando ViaCEP falha")
    void shouldUseFallbackWhenViaCepFails() {
        // Given - usando CEP conhecido que tem fallback
        String cep = "20040020";
        when(viaCepService.consultarCep(cep)).thenThrow(new RuntimeException("Erro de rede"));

        // When
        Endereco result = service.validarCep(cep);

        // Then
        // Fallback retorna endereço mock para CEP conhecido 20040020
        assertThat(result).isNotNull();
        assertThat(result.getCep()).isEqualTo("20040020");
        assertThat(result.getLogradouro()).isEqualTo("Rua da Assembleia");
        verify(viaCepService).consultarCep(cep);
    }

    @Test
    @DisplayName("Deve validar se CEP é válido")
    void shouldValidateIfCepIsValid() {
        // Given
        String cepValido = "20040020";
        Endereco endereco = new Endereco(
            "Rua da Assembleia", "100", null, "Centro", "Rio de Janeiro", "RJ", "20040020"
        );

        when(viaCepService.consultarCep(cepValido)).thenReturn(endereco);

        // When
        boolean isValido = service.isCepValido(cepValido);

        // Then
        assertThat(isValido).isTrue();
        verify(viaCepService).consultarCep(cepValido);
    }

    @Test
    @DisplayName("Deve retornar false para CEP com formato inválido usando isCepValido")
    void shouldReturnFalseForInvalidCepFormatUsingIsCepValido() {
        // Given
        String cepInvalido = "123";

        // When
        boolean isValido = service.isCepValido(cepInvalido);

        // Then
        assertThat(isValido).isFalse();
        // Não deve chamar ViaCEP para formato inválido
        verify(viaCepService, never()).consultarCep(anyString());
    }
}
