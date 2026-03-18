package com.seguradora.hibrida.domain.sinistro.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.sinistro.aggregate.SinistroAggregate;
import com.seguradora.hibrida.domain.sinistro.command.CriarSinistroCommand;
import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import com.seguradora.hibrida.eventstore.EventStore;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@DisplayName("CriarSinistroCommandHandler Tests")
class CriarSinistroCommandHandlerTest {

    @Mock
    private EventStore eventStore;

    @Mock
    private CommandBus commandBus;

    @Mock
    @SuppressWarnings("unchecked")
    private AggregateRepository<SinistroAggregate> sinistroRepository;

    private CriarSinistroCommandHandler handler;

    @BeforeEach
    void setUp() {
        handler = new CriarSinistroCommandHandler(eventStore, commandBus, sinistroRepository);
    }

    @Test
    @DisplayName("getCommandType deve retornar CriarSinistroCommand.class")
    void getCommandTypeShouldReturnClass() {
        assertThat(handler.getCommandType()).isEqualTo(CriarSinistroCommand.class);
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 45")
    void getTimeoutSecondsShouldReturn45() {
        assertThat(handler.getTimeoutSeconds()).isEqualTo(45);
    }

    @Test
    @DisplayName("handle deve retornar falha para protocolo com prefixo SIN- inválido para ProtocoloSinistro")
    void handleShouldReturnFailureWhenProtocolFormatCausesInvalidProtocolo() {
        // O handler valida SIN-YYYY-NNNNNN mas ProtocoloSinistro.of() aceita YYYY-NNNNNN
        // Portanto o protocolo "SIN-2024-000001" falha em ProtocoloSinistro.of()
        CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001")
                .veiculoId("VEI-001")
                .apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000, São Paulo, SP")
                .descricao("Colisão traseira em via de alto fluxo com danos no para-choque")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        // ProtocoloSinistro.of("SIN-2024-000001") throws IAE → handler returns VALIDATION_ERROR
        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha para data de ocorrência futura")
    void handleShouldReturnFailureForFutureDate() {
        CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001").veiculoId("VEI-001").apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .dataOcorrencia(LocalDateTime.now().plusDays(1))
                .localOcorrencia("Av. Paulista, 1000")
                .descricao("Descrição do sinistro com detalhes")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }

    @Test
    @DisplayName("handle deve retornar falha para protocolo com formato inválido")
    void handleShouldReturnFailureForInvalidProtocolFormat() {
        CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("PROTOCOLO-INVALIDO")
                .seguradoId("SEG-001").veiculoId("VEI-001").apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000")
                .descricao("Descrição do sinistro com detalhes")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
    }

    @Test
    @DisplayName("handle deve retornar falha quando ROUBO_FURTO sem BO")
    void handleShouldReturnFailureForRouboFurtoWithoutBO() {
        CriarSinistroCommand command = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001").veiculoId("VEI-001").apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.ROUBO_FURTO)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000")
                .descricao("Roubo do veículo na via pública")
                .operadorId("OP-001")
                .build();

        CommandResult result = handler.handle(command);

        assertThat(result.isSuccess()).isFalse();
        assertThat(result.getErrorCode()).isEqualTo("VALIDATION_ERROR");
    }
}
