package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AdicionarContatoCommand;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AdicionarContatoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AdicionarContatoCommandHandler - Testes Unitários")
class AdicionarContatoCommandHandlerTest {

    @Mock
    private AggregateRepository<SeguradoAggregate> aggregateRepository;

    @Mock
    private SeguradoAggregate aggregate;

    @InjectMocks
    private AdicionarContatoCommandHandler handler;

    @Test
    @DisplayName("Deve adicionar contato com sucesso")
    void shouldAddContactSuccessfully() {
        // Given
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.EMAIL)
            .valor("contato@example.com")
            .principal(true)
            .operadorId("OP-001")
            .build();

        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getId()).thenReturn("SEG-001");

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("SEG-001");

        verify(aggregateRepository).getById("SEG-001");
        verify(aggregate).adicionarContato(TipoContato.EMAIL, "contato@example.com", true);
        verify(aggregateRepository).save(aggregate);
    }

    @Test
    @DisplayName("Deve retornar falha quando ocorre exceção")
    void shouldReturnFailureWhenExceptionOccurs() {
        // Given
        AdicionarContatoCommand command = AdicionarContatoCommand.builder()
            .seguradoId("SEG-001")
            .tipo(TipoContato.CELULAR)
            .valor("11987654321")
            .principal(false)
            .operadorId("OP-001")
            .build();

        when(aggregateRepository.getById("SEG-001"))
            .thenThrow(new RuntimeException("Aggregate não encontrado"));

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).isNotBlank();

        verify(aggregateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar tipo de comando correto")
    void shouldReturnCorrectCommandType() {
        // When
        Class<AdicionarContatoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(AdicionarContatoCommand.class);
    }
}
