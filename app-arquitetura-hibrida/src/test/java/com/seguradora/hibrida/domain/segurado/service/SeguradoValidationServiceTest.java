package com.seguradora.hibrida.domain.segurado.service;

import com.seguradora.hibrida.domain.segurado.query.repository.SeguradoQueryRepository;
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
 * Testes unitários para {@link SeguradoValidationService}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeguradoValidationService - Testes Unitários")
class SeguradoValidationServiceTest {

    @Mock
    private SeguradoQueryRepository seguradoQueryRepository;

    @Mock
    private BureauCreditoService bureauCreditoService;

    @InjectMocks
    private SeguradoValidationService service;

    @Test
    @DisplayName("Deve retornar true quando CPF é único")
    void shouldReturnTrueWhenCpfIsUnique() {
        // Given
        String cpf = "12345678909";
        when(seguradoQueryRepository.existsByCpf(cpf)).thenReturn(false);

        // When
        boolean isUnico = service.isCpfUnico(cpf);

        // Then
        assertThat(isUnico).isTrue();
        verify(seguradoQueryRepository).existsByCpf(cpf);
    }

    @Test
    @DisplayName("Deve retornar false quando CPF já existe")
    void shouldReturnFalseWhenCpfExists() {
        // Given
        String cpf = "12345678909";
        when(seguradoQueryRepository.existsByCpf(cpf)).thenReturn(true);

        // When
        boolean isUnico = service.isCpfUnico(cpf);

        // Then
        assertThat(isUnico).isFalse();
        verify(seguradoQueryRepository).existsByCpf(cpf);
    }

    @Test
    @DisplayName("Deve retornar true quando email é único")
    void shouldReturnTrueWhenEmailIsUnique() {
        // Given
        String email = "joao@example.com";
        when(seguradoQueryRepository.existsByEmail(email)).thenReturn(false);

        // When
        boolean isUnico = service.isEmailUnico(email);

        // Then
        assertThat(isUnico).isTrue();
        verify(seguradoQueryRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve retornar false quando email já existe")
    void shouldReturnFalseWhenEmailExists() {
        // Given
        String email = "joao@example.com";
        when(seguradoQueryRepository.existsByEmail(email)).thenReturn(true);

        // When
        boolean isUnico = service.isEmailUnico(email);

        // Then
        assertThat(isUnico).isFalse();
        verify(seguradoQueryRepository).existsByEmail(email);
    }

    @Test
    @DisplayName("Deve validar telefone corretamente")
    void shouldValidatePhoneCorrectly() {
        // When - telefone válido
        boolean isValido = service.isTelefoneValido("11987654321");

        // Then
        assertThat(isValido).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para telefone inválido")
    void shouldReturnFalseForInvalidPhone() {
        // When - telefone inválido
        boolean isValido = service.isTelefoneValido("123");

        // Then
        assertThat(isValido).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando ocorre erro na validação de CPF")
    void shouldReturnFalseWhenCpfValidationErrors() {
        // Given
        String cpfInvalido = "123";

        // When
        boolean isUnico = service.isCpfUnico(cpfInvalido);

        // Then
        assertThat(isUnico).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando ocorre erro na validação de email")
    void shouldReturnFalseWhenEmailValidationErrors() {
        // Given
        String emailInvalido = "email-invalido";

        // When
        boolean isUnico = service.isEmailUnico(emailInvalido);

        // Then
        assertThat(isUnico).isFalse();
    }

    @Test
    @DisplayName("Deve validar criação de segurado com sucesso")
    void shouldValidateSeguradoCreationSuccessfully() {
        // Given
        String cpf = "12345678909";
        String email = "joao@example.com";

        when(seguradoQueryRepository.existsByCpf(cpf)).thenReturn(false);
        when(seguradoQueryRepository.existsByEmail(email)).thenReturn(false);

        BureauValidationResult bureauResult = BureauValidationResult.sucesso(750);
        when(bureauCreditoService.validarCpf(cpf)).thenReturn(bureauResult);

        // When
        SeguradoValidationService.ValidationResult result =
            service.validarCriacaoSegurado(cpf, email);

        // Then
        assertThat(result.isValido()).isTrue();
        assertThat(result.getErros()).isEmpty();
    }

    @Test
    @DisplayName("Deve falhar validação quando CPF não é único")
    void shouldFailValidationWhenCpfIsNotUnique() {
        // Given
        String cpf = "12345678909";
        String email = "joao@example.com";

        when(seguradoQueryRepository.existsByCpf(cpf)).thenReturn(true);
        when(seguradoQueryRepository.existsByEmail(email)).thenReturn(false);

        // Bureau também é chamado sempre no validarCriacaoSegurado
        BureauValidationResult bureauResult = BureauValidationResult.sucesso(750);
        when(bureauCreditoService.validarCpf(cpf)).thenReturn(bureauResult);

        // When
        SeguradoValidationService.ValidationResult result =
            service.validarCriacaoSegurado(cpf, email);

        // Then
        assertThat(result.isValido()).isFalse();
        assertThat(result.getErros()).isNotEmpty();
        assertThat(result.getErros()).anyMatch(erro -> erro.contains("CPF"));
    }
}
