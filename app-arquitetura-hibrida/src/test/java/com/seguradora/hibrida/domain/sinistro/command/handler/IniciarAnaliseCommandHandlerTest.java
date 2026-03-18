package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.IniciarAnaliseCommand;
import com.seguradora.hibrida.domain.sinistro.model.Sinistro;
import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("IniciarAnaliseCommandHandler Tests")
class IniciarAnaliseCommandHandlerTest {

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    @Mock
    private SinistroAggregate aggregate;

    @Mock
    private Sinistro sinistroMock;

    private IniciarAnaliseCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new IniciarAnaliseCommandHandler(sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar IniciarAnaliseCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(IniciarAnaliseCommand.class);
    }

    @Test
    @DisplayName("handle deve retornar falha quando prioridade inválida")
    void handleShouldReturnFailureForInvalidPriority() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getTipoSinistro()).thenReturn(TipoSinistro.TERCEIROS);

        IniciarAnaliseCommand command = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001")
                .analistaId("ANALISTA-01")
                .prioridadeAnalise("INVALIDA")
                .prazoEstimado(5)
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha quando analistaId é nulo")
    void handleShouldReturnFailureForNullAnalistaId() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getTipoSinistro()).thenReturn(TipoSinistro.TERCEIROS);

        IniciarAnaliseCommand command = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001")
                .analistaId(null)
                .prioridadeAnalise("ALTA")
                .prazoEstimado(5)
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve propagar erro de repositório como PROCESSING_ERROR")
    void handleShouldReturnProcessingErrorWhenRepositoryFails() {
        org.mockito.Mockito.when(sinistroRepository.getById("SIN-001"))
                .thenThrow(new RuntimeException("Repository unavailable"));

        IniciarAnaliseCommand command = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001")
                .analistaId("ANALISTA-01")
                .prioridadeAnalise("ALTA")
                .prazoEstimado(5)
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("PROCESSING_ERROR");
    }
}
