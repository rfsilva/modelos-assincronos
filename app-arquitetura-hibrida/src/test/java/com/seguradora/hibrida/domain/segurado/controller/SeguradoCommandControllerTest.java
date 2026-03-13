package com.seguradora.hibrida.domain.segurado.controller;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.command.*;
import com.seguradora.hibrida.domain.segurado.controller.dto.*;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link SeguradoCommandController}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("SeguradoCommandController - Testes Unitários")
class SeguradoCommandControllerTest {

    @Mock
    private CommandBus commandBus;

    @InjectMocks
    private SeguradoCommandController controller;

    @Test
    @DisplayName("Deve criar segurado com sucesso")
    void shouldCreateSeguradoSuccessfully() {
        // Given
        CriarSeguradoRequestDTO request = new CriarSeguradoRequestDTO();
        request.setCpf("12345678909");
        request.setNome("João Silva");
        request.setEmail("joao@example.com");
        request.setTelefone("11987654321");
        request.setDataNascimento(LocalDate.of(1990, 1, 1));

        CommandResult commandResult = CommandResult.success("SEG-001");
        when(commandBus.send(any(CriarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.criarSegurado(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        assertThat(response.getBody().getData()).isEqualTo("SEG-001");
        verify(commandBus).send(any(CriarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve retornar erro ao criar segurado com dados inválidos")
    void shouldReturnErrorWhenCreatingSeguradoWithInvalidData() {
        // Given
        CriarSeguradoRequestDTO request = new CriarSeguradoRequestDTO();
        request.setCpf("12345678909");

        CommandResult commandResult = CommandResult.failure("CPF já existe");
        when(commandBus.send(any(CriarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.criarSegurado(request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        verify(commandBus).send(any(CriarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve atualizar segurado com sucesso")
    void shouldUpdateSeguradoSuccessfully() {
        // Given
        String id = "SEG-001";
        AtualizarSeguradoRequestDTO request = new AtualizarSeguradoRequestDTO();
        request.setNome("João Silva Atualizado");

        CommandResult commandResult = CommandResult.success(id);
        when(commandBus.send(any(AtualizarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.atualizarSegurado(id, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(commandBus).send(any(AtualizarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve retornar erro ao atualizar segurado inexistente")
    void shouldReturnErrorWhenUpdatingNonExistentSegurado() {
        // Given
        String id = "SEG-999";
        AtualizarSeguradoRequestDTO request = new AtualizarSeguradoRequestDTO();

        CommandResult commandResult = CommandResult.failure("Segurado não encontrado");
        when(commandBus.send(any(AtualizarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.atualizarSegurado(id, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        verify(commandBus).send(any(AtualizarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve desativar segurado com sucesso")
    void shouldDeactivateSeguradoSuccessfully() {
        // Given
        String id = "SEG-001";
        DesativarSeguradoRequestDTO request = new DesativarSeguradoRequestDTO();
        request.setMotivo("Solicitação do cliente");

        CommandResult commandResult = CommandResult.success(id);
        when(commandBus.send(any(DesativarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.desativarSegurado(id, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(commandBus).send(any(DesativarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve reativar segurado com sucesso")
    void shouldReactivateSeguradoSuccessfully() {
        // Given
        String id = "SEG-001";
        ReativarSeguradoRequestDTO request = new ReativarSeguradoRequestDTO();
        request.setMotivo("Regularização de pendências");

        CommandResult commandResult = CommandResult.success(id);
        when(commandBus.send(any(ReativarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.reativarSegurado(id, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isTrue();
        verify(commandBus).send(any(ReativarSeguradoCommand.class));
    }

    @Test
    @DisplayName("Deve retornar erro ao reativar segurado já ativo")
    void shouldReturnErrorWhenReactivatingActiveSegurado() {
        // Given
        String id = "SEG-001";
        ReativarSeguradoRequestDTO request = new ReativarSeguradoRequestDTO();
        request.setMotivo("Teste");

        CommandResult commandResult = CommandResult.failure("Segurado já está ativo");
        when(commandBus.send(any(ReativarSeguradoCommand.class))).thenReturn(commandResult);

        // When
        ResponseEntity<CommandResponseDTO> response = controller.reativarSegurado(id, request);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().isSuccess()).isFalse();
        verify(commandBus).send(any(ReativarSeguradoCommand.class));
    }
}
