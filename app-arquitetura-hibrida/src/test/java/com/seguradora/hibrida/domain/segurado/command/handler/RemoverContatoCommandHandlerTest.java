package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.RemoverContatoCommand;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link RemoverContatoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RemoverContatoCommandHandler - Testes Unitários")
class RemoverContatoCommandHandlerTest {

    @Mock
    private AggregateRepository<SeguradoAggregate> aggregateRepository;

    @Mock
    private SeguradoAggregate aggregate;

    @InjectMocks
    private RemoverContatoCommandHandler handler;

    @Test
    @DisplayName("Deve remover contato com sucesso")
    void shouldRemoveContactSuccessfully() {
        // Given
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001",
            TipoContato.EMAIL,
            "contato@example.com",
            "OP-001"
        );

        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getId()).thenReturn("SEG-001");

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("SEG-001");

        verify(aggregateRepository).getById("SEG-001");
        verify(aggregate).removerContato(TipoContato.EMAIL, "contato@example.com");
        verify(aggregateRepository).save(aggregate);
    }

    @Test
    @DisplayName("Deve retornar falha quando ocorre exceção")
    void shouldReturnFailureWhenExceptionOccurs() {
        // Given
        RemoverContatoCommand command = new RemoverContatoCommand(
            "SEG-001",
            TipoContato.CELULAR,
            "11987654321",
            "OP-001"
        );

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
        Class<RemoverContatoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(RemoverContatoCommand.class);
    }
}
