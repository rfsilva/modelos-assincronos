package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.AprovarSinistroCommand;
import com.seguradora.hibrida.domain.sinistro.model.Sinistro;
import com.seguradora.hibrida.domain.sinistro.model.StatusSinistro;
import com.seguradora.hibrida.domain.sinistro.model.ValorIndenizacao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AprovarSinistroCommandHandler Tests")
class AprovarSinistroCommandHandlerTest {

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    @Mock
    private SinistroAggregate aggregate;

    @Mock
    private Sinistro sinistroMock;

    private AprovarSinistroCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new AprovarSinistroCommandHandler(sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar AprovarSinistroCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(AprovarSinistroCommand.class);
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 30")
    void getTimeoutSecondsShouldReturn30() {
        assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
    }

    @Test
    @DisplayName("handle deve retornar sucesso para aprovação válida")
    void handleShouldReturnSuccessForValidApproval() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(aggregate.getVersion()).thenReturn(1L);
        doNothing().when(aggregate).aprovar(any(), any(), any(), any());
        doNothing().when(sinistroRepository).save(any());

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .franquia(new BigDecimal("1000.00"))
                .build();

        AprovarSinistroCommand command = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .valorIndenizacao(valor)
                .justificativa("O sinistro foi devidamente aprovado após análise técnica completa e verificação de todos os documentos apresentados com laudo pericial confirmado pelo perito")
                .analistaId("ANALISTA-01")
                .documentosComprobatorios(List.of("DOC-001", "DOC-002"))
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isTrue();
        verify(aggregate).aprovar(any(), any(), eq("ANALISTA-01"), any());
        verify(sinistroRepository).save(aggregate);
    }

    @Test
    @DisplayName("handle deve retornar falha quando sinistro não está aberto")
    void handleShouldReturnFailureWhenNotOpen() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(false);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);
        when(sinistroMock.getStatus()).thenReturn(StatusSinistro.ARQUIVADO);

        AprovarSinistroCommand command = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .valorIndenizacao(ValorIndenizacao.builder().valorBruto(new BigDecimal("5000")).build())
                .justificativa("Justificativa")
                .analistaId("ANALISTA-01")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha quando justificativa inadequada")
    void handleShouldReturnFailureWhenJustificativaInadequada() {
        when(sinistroRepository.getById("SIN-001")).thenReturn(aggregate);
        when(aggregate.isAberto()).thenReturn(true);
        when(aggregate.getSinistro()).thenReturn(sinistroMock);

        ValorIndenizacao valor = ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .franquia(new BigDecimal("1000.00"))
                .build();

        AprovarSinistroCommand command = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .valorIndenizacao(valor)
                .justificativa("Curta demais.")
                .analistaId("ANALISTA-01")
                .documentosComprobatorios(List.of("DOC-001", "DOC-002"))
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
}
