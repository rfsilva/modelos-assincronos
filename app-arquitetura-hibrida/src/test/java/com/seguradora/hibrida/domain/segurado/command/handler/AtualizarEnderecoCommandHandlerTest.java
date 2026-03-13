package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.AtualizarEnderecoCommand;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.domain.segurado.service.CepValidationService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AtualizarEnderecoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarEnderecoCommandHandler - Testes Unitários")
class AtualizarEnderecoCommandHandlerTest {

    @Mock
    private AggregateRepository<SeguradoAggregate> aggregateRepository;

    @Mock
    private CepValidationService cepValidationService;

    @Mock
    private SeguradoAggregate aggregate;

    @InjectMocks
    private AtualizarEnderecoCommandHandler handler;

    @Test
    @DisplayName("Deve atualizar endereço com sucesso")
    void shouldUpdateAddressSuccessfully() {
        // Given
        Endereco novoEndereco = new Endereco(
            "Rua Nova", "500", "Sala 10", "Jardins", "São Paulo", "SP", "01310200"
        );

        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001",
            novoEndereco,
            "OP-001"
        );

        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(aggregate.getId()).thenReturn("SEG-001");
        when(cepValidationService.isCepValido(anyString())).thenReturn(true);

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isEqualTo("SEG-001");

        verify(aggregateRepository).getById("SEG-001");
        verify(cepValidationService).isCepValido("01310200");
        verify(aggregate).atualizarEndereco(novoEndereco);
        verify(aggregateRepository).save(aggregate);
    }

    @Test
    @DisplayName("Deve retornar falha quando CEP é inválido")
    void shouldReturnFailureWhenCepIsInvalid() {
        // Given
        Endereco endereco = new Endereco(
            "Rua Teste", "100", null, "Centro", "São Paulo", "SP", "99999999"
        );

        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001",
            endereco,
            "OP-001"
        );

        when(aggregateRepository.getById("SEG-001")).thenReturn(aggregate);
        when(cepValidationService.isCepValido("99999999")).thenReturn(false);

        // When
        CommandResult result = handler.handle(command);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("CEP inválido");

        verify(aggregateRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve retornar falha quando ocorre exceção")
    void shouldReturnFailureWhenExceptionOccurs() {
        // Given
        Endereco endereco = new Endereco(
            "Rua Teste", "100", null, "Centro", "São Paulo", "SP", "01310100"
        );

        AtualizarEnderecoCommand command = new AtualizarEnderecoCommand(
            "SEG-001",
            endereco,
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
        Class<AtualizarEnderecoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(AtualizarEnderecoCommand.class);
    }
}
