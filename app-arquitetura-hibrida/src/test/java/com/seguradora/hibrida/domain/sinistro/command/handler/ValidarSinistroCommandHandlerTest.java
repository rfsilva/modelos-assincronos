package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.ValidarSinistroCommand;
import com.seguradora.hibrida.domain.sinistro.model.Sinistro;
import com.seguradora.hibrida.domain.sinistro.model.StatusSinistro;
import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ValidarSinistroCommandHandler Tests")
class ValidarSinistroCommandHandlerTest {

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    @Mock
    private SinistroAggregate aggregate;

    @Mock
    private Sinistro sinistroMock;

    private ValidarSinistroCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new ValidarSinistroCommandHandler(sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar ValidarSinistroCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(ValidarSinistroCommand.class);
    }

    @Test
    @DisplayName("handle deve retornar sucesso para validação válida")
    void handleShouldReturnSuccessForValidCommand() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getVersion()).thenReturn(1L);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getTipoSinistro()).thenReturn(TipoSinistro.TERCEIROS);
        doNothing().when(aggregate).validarDados(any(), any(), any());
        doNothing().when(sinistroRepository).save(any());

        Map<String, Object> dados = new HashMap<>();
        dados.put("condicaoClimatica", "Seco");
        dados.put("condicaoPista", "Molhada");
        dados.put("velocidadeEstimada", "60");

        ValidarSinistroCommand command = ValidarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .dadosComplementares(dados)
                .documentosAnexados(List.of("DOC-001"))
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(aggregate).validarDados(any(), any(), eq("OP-001"));
        verify(sinistroRepository).save(aggregate);
    }

    @Test
    @DisplayName("handle deve retornar falha quando sinistro não está aberto")
    void handleShouldReturnFailureWhenNotOpen() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(false);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.ARQUIVADO);

        ValidarSinistroCommand command = ValidarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar PROCESSING_ERROR quando repositório falhar")
    void handleShouldReturnProcessingErrorWhenRepositoryFails() {
        when(sinistroRepository.getById("SIN-001"))
                .thenThrow(new RuntimeException("DB error"));

        ValidarSinistroCommand command = ValidarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("PROCESSING_ERROR");
    }
}
