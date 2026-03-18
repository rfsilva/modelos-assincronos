package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.ReprovarSinistroCommand;
import com.seguradora.hibrida.domain.sinistro.model.Sinistro;
import com.seguradora.hibrida.domain.sinistro.model.StatusSinistro;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ReprovarSinistroCommandHandler Tests")
class ReprovarSinistroCommandHandlerTest {

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    @Mock
    private SinistroAggregate aggregate;

    @Mock
    private Sinistro sinistroMock;

    private ReprovarSinistroCommandHandler handler;

    private String justificativa30Palavras() {
        return "A reprovação foi decidida após análise técnica detalhada verificando que o sinistro " +
               "não possui cobertura contratual vigente conforme apólice aplicável ao segurado " +
               "conforme regulamento interno da seguradora devidamente registrado e verificado pelo analista";
    }

    @BeforeEach
    void setUp() {
        handler = new ReprovarSinistroCommandHandler(sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar ReprovarSinistroCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(ReprovarSinistroCommand.class);
    }

    @Test
    @DisplayName("handle deve retornar sucesso para reprovação válida")
    void handleShouldReturnSuccessForValidReprovacao() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.EM_ANALISE);
        when(aggregate.getVersion()).thenReturn(1L);
        doNothing().when(aggregate).reprovar(any(), any(), any(), any());
        doNothing().when(sinistroRepository).save(any());

        ReprovarSinistroCommand command = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .motivo("FORA_COBERTURA")
                .justificativaDetalhada(justificativa30Palavras())
                .analistaId("ANALISTA-01")
                .fundamentoLegal("Art. 5, Cláusula 10 do contrato de seguro")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(aggregate).reprovar(any(), any(), eq("ANALISTA-01"), any());
        verify(sinistroRepository).save(aggregate);
    }

    @Test
    @DisplayName("handle deve retornar falha quando sinistro não está aberto")
    void handleShouldReturnFailureWhenNotOpen() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(false);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.PAGO);

        ReprovarSinistroCommand command = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .motivo("FORA_COBERTURA")
                .justificativaDetalhada("just")
                .analistaId("ANALISTA-01")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha para justificativa inadequada (menos de 30 palavras)")
    void handleShouldReturnFailureForInvalidMotivo() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.EM_ANALISE);

        ReprovarSinistroCommand command = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .motivo("FORA_COBERTURA")
                .justificativaDetalhada("Justificativa muito curta.")
                .analistaId("ANALISTA-01")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
}
