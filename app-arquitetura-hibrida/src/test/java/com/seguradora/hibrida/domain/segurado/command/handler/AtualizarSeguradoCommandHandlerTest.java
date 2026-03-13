package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AtualizarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.domain.segurado.service.SeguradoValidationService;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AtualizarSeguradoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarSeguradoCommandHandler - Testes Unitários")
class AtualizarSeguradoCommandHandlerTest {

    @Mock
    private AggregateRepository<SeguradoAggregate> aggregateRepository;

    @Mock
    private SeguradoValidationService validationService;

    @Mock
    private SeguradoAggregate aggregate;

    @InjectMocks
    private AtualizarSeguradoCommandHandler handler;

    private AtualizarSeguradoCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = AtualizarSeguradoCommand.builder()
            .seguradoId("SEG-001")
            .nome("João Silva Atualizado")
            .email("joao.novo@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(new Endereco(
                "Rua Nova", "200", "Apto 202", "Centro", "São Paulo", "SP", "01310100"
            ))
            .versaoEsperada(1L)
            .build();
    }

    @Test
    @DisplayName("Deve atualizar segurado com sucesso")
    void shouldUpdateSeguradoSuccessfully() {
        // Given
        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getVersion()).thenReturn(1L);
        when(aggregate.getEmail()).thenReturn("joao.antigo@example.com");
        when(validationService.isEmailUnico(anyString())).thenReturn(true);

        // When
        CommandResult result = handler.handle(validCommand);

        // Then
        assertThat(result.isSuccess()).isTrue();
        verify(aggregateRepository).getById("SEG-001");
        verify(aggregate).atualizarDados(
            anyString(), anyString(), anyString(), any(LocalDate.class), any(Endereco.class)
        );
        verify(aggregateRepository).save(aggregate);
    }

    @Test
    @DisplayName("Deve falhar quando email não é único")
    void shouldFailWhenEmailIsNotUnique() {
        // Given
        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getVersion()).thenReturn(1L);
        when(aggregate.getEmail()).thenReturn("joao.antigo@example.com");
        when(validationService.isEmailUnico(anyString())).thenReturn(false);

        // When
        CommandResult result = handler.handle(validCommand);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Email já está em uso");
        verify(aggregateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve lançar exceção quando há conflito de versão")
    void shouldThrowExceptionWhenVersionConflict() {
        // Given
        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getVersion()).thenReturn(2L); // Versão diferente da esperada

        // When/Then
        assertThatThrownBy(() -> handler.handle(validCommand))
            .isInstanceOf(ConcurrencyException.class);

        verify(aggregateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Não deve validar email quando não foi alterado")
    void shouldNotValidateEmailWhenUnchanged() {
        // Given
        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getVersion()).thenReturn(1L);
        when(aggregate.getEmail()).thenReturn("joao.novo@example.com"); // Mesmo email

        // When
        handler.handle(validCommand);

        // Then
        verify(validationService, never()).isEmailUnico(anyString());
    }

    @Test
    @DisplayName("Deve retornar tipo de comando correto")
    void shouldReturnCorrectCommandType() {
        // When
        Class<AtualizarSeguradoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(AtualizarSeguradoCommand.class);
    }

    @Test
    @DisplayName("Deve ter timeout configurado")
    void shouldHaveConfiguredTimeout() {
        // When
        int timeout = handler.getTimeoutSeconds();

        // Then
        assertThat(timeout).isEqualTo(15);
    }
}
