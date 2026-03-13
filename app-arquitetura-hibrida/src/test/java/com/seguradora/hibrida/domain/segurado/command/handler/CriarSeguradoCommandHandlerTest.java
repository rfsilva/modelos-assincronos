package com.seguradora.hibrida.domain.segurado.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.aggregate.SeguradoAggregate;
import com.seguradora.hibrida.domain.segurado.command.CriarSeguradoCommand;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.domain.segurado.service.SeguradoValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link CriarSeguradoCommandHandler}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CriarSeguradoCommandHandler - Testes Unitários")
class CriarSeguradoCommandHandlerTest {

    @Mock
    private AggregateRepository<SeguradoAggregate> aggregateRepository;

    @Mock
    private SeguradoValidationService validationService;

    @InjectMocks
    private CriarSeguradoCommandHandler handler;

    private CriarSeguradoCommand validCommand;

    @BeforeEach
    void setUp() {
        validCommand = CriarSeguradoCommand.builder()
            .cpf("12345678909")
            .nome("João Silva")
            .email("joao@example.com")
            .telefone("11987654321")
            .dataNascimento(LocalDate.of(1990, 1, 15))
            .endereco(new Endereco(
                "Rua Teste", "100", "Apto 101", "Centro", "São Paulo", "SP", "01310100"
            ))
            .build();
    }

    @Test
    @DisplayName("Deve criar segurado com sucesso quando validação passa")
    void shouldCreateSeguradoSuccessfullyWhenValidationPasses() {
        // Given
        SeguradoValidationService.ValidationResult validationResult =
            new SeguradoValidationService.ValidationResult();

        when(validationService.validarCriacaoSegurado(anyString(), anyString()))
            .thenReturn(validationResult);

        // When
        CommandResult result = handler.handle(validCommand);

        // Then
        assertThat(result.isSuccess()).isTrue();
        assertThat(result.getData()).isNotNull();

        verify(validationService).validarCriacaoSegurado("12345678909", "joao@example.com");
        verify(aggregateRepository).save(any(SeguradoAggregate.class));
    }

    @Test
    @DisplayName("Deve retornar falha quando validação falha")
    void shouldReturnFailureWhenValidationFails() {
        // Given
        SeguradoValidationService.ValidationResult validationResult =
            new SeguradoValidationService.ValidationResult();
        validationResult.addError("CPF já cadastrado");
        validationResult.addError("Email já existe");

        when(validationService.validarCriacaoSegurado(anyString(), anyString()))
            .thenReturn(validationResult);

        // When
        CommandResult result = handler.handle(validCommand);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Falha na validação");

        verify(validationService).validarCriacaoSegurado("12345678909", "joao@example.com");
        verify(aggregateRepository, never()).save(any(SeguradoAggregate.class));
    }

    @Test
    @DisplayName("Deve retornar falha quando ocorre exceção genérica")
    void shouldReturnFailureWhenGenericExceptionOccurs() {
        // Given
        when(validationService.validarCriacaoSegurado(anyString(), anyString()))
            .thenThrow(new RuntimeException("Erro de banco de dados"));

        // When
        CommandResult result = handler.handle(validCommand);

        // Then
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorMessage()).contains("Erro interno");

        verify(aggregateRepository, never()).save(any(SeguradoAggregate.class));
    }

    @Test
    @DisplayName("Deve retornar tipo de comando correto")
    void shouldReturnCorrectCommandType() {
        // When
        Class<CriarSeguradoCommand> commandType = handler.getCommandType();

        // Then
        assertThat(commandType).isEqualTo(CriarSeguradoCommand.class);
    }

    @Test
    @DisplayName("Deve gerar ID único para cada segurado")
    void shouldGenerateUniqueIdForEachSegurado() {
        // Given
        SeguradoValidationService.ValidationResult validationResult =
            new SeguradoValidationService.ValidationResult();

        when(validationService.validarCriacaoSegurado(anyString(), anyString()))
            .thenReturn(validationResult);

        // When
        CommandResult result1 = handler.handle(validCommand);
        CommandResult result2 = handler.handle(validCommand);

        // Then
        assertThat(result1.getData()).isNotEqualTo(result2.getData());
    }

    @Test
    @DisplayName("Deve chamar repository.save exatamente uma vez")
    void shouldCallRepositorySaveExactlyOnce() {
        // Given
        SeguradoValidationService.ValidationResult validationResult =
            new SeguradoValidationService.ValidationResult();

        when(validationService.validarCriacaoSegurado(anyString(), anyString()))
            .thenReturn(validationResult);

        // When
        handler.handle(validCommand);

        // Then
        verify(aggregateRepository, times(1)).save(any(SeguradoAggregate.class));
    }
}
