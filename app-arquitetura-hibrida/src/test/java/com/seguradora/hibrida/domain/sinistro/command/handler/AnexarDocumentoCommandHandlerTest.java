package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.AnexarDocumentoCommand;
import com.seguradora.hibrida.domain.sinistro.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnexarDocumentoCommandHandler Tests")
class AnexarDocumentoCommandHandlerTest {

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    @Mock
    private SinistroAggregate aggregate;

    @Mock
    private Sinistro sinistroMock;

    private AnexarDocumentoCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AnexarDocumentoCommandHandler(sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar AnexarDocumentoCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(AnexarDocumentoCommand.class);
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 30")
    void getTimeoutSecondsShouldReturn30() {
        assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("handle deve retornar sucesso para documento válido")
    void handleShouldReturnSuccessForValidDocument() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getVersion()).thenReturn(1L);
        doNothing().when(aggregate).anexarDocumento(any(), any(), any(), any());
        doNothing().when(sinistroRepository).save(any());

        AnexarDocumentoCommand command = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001")
                .documentoId("DOC-12345678") // >= 10 chars
                .tipoDocumento("FOTO_VEICULO")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(aggregate).anexarDocumento(eq("DOC-12345678"), eq("FOTO_VEICULO"), eq("OP-001"), any());
        verify(sinistroRepository).save(aggregate);
    }

    @Test
    @DisplayName("handle deve retornar falha quando sinistro não está aberto")
    void handleShouldReturnFailureWhenNotOpen() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(false);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.ARQUIVADO);

        AnexarDocumentoCommand command = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001")
                .documentoId("DOC-12345678")
                .tipoDocumento("FOTO_VEICULO")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha para tipo de documento inválido")
    void handleShouldReturnFailureForInvalidDocumentType() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);

        AnexarDocumentoCommand command = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001")
                .documentoId("DOC-12345678")
                .tipoDocumento("TIPO_INVALIDO_XXX")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
}
