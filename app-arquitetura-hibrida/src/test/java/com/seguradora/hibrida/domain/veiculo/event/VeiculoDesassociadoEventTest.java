package com.seguradora.hibrida.domain.veiculo.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoDesassociadoEvent}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoDesassociadoEvent - Testes Unitários")
class VeiculoDesassociadoEventTest {

    private static final String AGGREGATE_ID = "VEI-001";
    private static final String APOLICE_ID = "APO-001";
    private static final String OPERADOR_ID = "OP-123";
    private static final String MOTIVO = "Cancelamento por solicitação do cliente";

    @Nested
    @DisplayName("Testes de Criação do Evento")
    class CriacaoEventoTests {

        @Test
        @DisplayName("Deve criar evento com dados válidos")
        void deveCriarEventoComDadosValidos() {
            // Arrange
            LocalDate dataFim = LocalDate.now();

            // Act
            VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, dataFim, MOTIVO, OPERADOR_ID
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(evento.getVersion()).isEqualTo(3L);
            assertThat(evento.getApoliceId()).isEqualTo(APOLICE_ID);
            assertThat(evento.getDataFim()).isEqualTo(dataFim);
            assertThat(evento.getMotivo()).isEqualTo(MOTIVO);
            assertThat(evento.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(evento.getEventType()).isEqualTo("VeiculoDesassociado");
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice nulo")
        void deveLancarExcecaoParaIdApoliceNulo() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, null, LocalDate.now(), MOTIVO, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice vazio")
        void deveLancarExcecaoParaIdApoliceVazio() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, "   ", LocalDate.now(), MOTIVO, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para data de fim nula")
        void deveLancarExcecaoParaDataFimNula() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, null, MOTIVO, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de fim não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção para motivo nulo")
        void deveLancarExcecaoParaMotivoNulo() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), null, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Motivo não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para motivo vazio")
        void deveLancarExcecaoParaMotivoVazio() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), "   ", OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Motivo não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID nulo")
        void deveLancarExcecaoParaOperadorIdNulo() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), MOTIVO, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do operador não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID vazio")
        void deveLancarExcecaoParaOperadorIdVazio() {
            assertThatThrownBy(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), MOTIVO, "   "
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do operador não pode ser nulo ou vazio");
        }
    }

    @Nested
    @DisplayName("Testes de Normalização")
    class NormalizacaoTests {

        @Test
        @DisplayName("Deve remover espaços do ID da apólice")
        void deveRemoverEspacosDoIdApolice() {
            VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, "  APO-001  ", LocalDate.now(), MOTIVO, OPERADOR_ID
            );

            assertThat(evento.getApoliceId()).isEqualTo("APO-001");
        }

        @Test
        @DisplayName("Deve remover espaços do motivo")
        void deveRemoverEspacosDoMotivo() {
            VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), "  Cancelamento  ", OPERADOR_ID
            );

            assertThat(evento.getMotivo()).isEqualTo("Cancelamento");
        }

        @Test
        @DisplayName("Deve remover espaços do operador ID")
        void deveRemoverEspacosDoOperadorId() {
            VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), MOTIVO, "  OP-123  "
            );

            assertThat(evento.getOperadorId()).isEqualTo("OP-123");
        }
    }

    @Nested
    @DisplayName("Testes de Datas")
    class DatasTests {

        @Test
        @DisplayName("Deve aceitar data no passado")
        void deveAceitarDataNoPassado() {
            LocalDate dataPassado = LocalDate.now().minusDays(10);

            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, dataPassado, MOTIVO, OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data no futuro")
        void deveAceitarDataNoFuturo() {
            LocalDate dataFuturo = LocalDate.now().plusDays(30);

            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, dataFuturo, MOTIVO, OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data atual")
        void deveAceitarDataAtual() {
            LocalDate dataAtual = LocalDate.now();

            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, dataAtual, MOTIVO, OPERADOR_ID
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Motivos")
    class MotivosTests {

        @Test
        @DisplayName("Deve aceitar motivo de cancelamento")
        void deveAceitarMotivoCancelamento() {
            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), "Cancelamento", OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar motivo de venda")
        void deveAceitarMotivoVenda() {
            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), "Venda do veículo", OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar motivo de término de vigência")
        void deveAceitarMotivoTerminoVigencia() {
            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), "Término de vigência", OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar motivo longo")
        void deveAceitarMotivoLongo() {
            String motivoLongo = "Cancelamento solicitado pelo cliente após transferência do veículo " +
                               "para outro estado, resultando na necessidade de nova apólice regional.";

            assertThatCode(() -> VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), motivoLongo, OPERADOR_ID
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Eventos com mesmos dados devem ser iguais")
        void eventosComMesmosDadosDevemSerIguais() {
            LocalDate data = LocalDate.now();

            VeiculoDesassociadoEvent evento1 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, data, MOTIVO, OPERADOR_ID
            );

            VeiculoDesassociadoEvent evento2 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, data, MOTIVO, OPERADOR_ID
            );

            assertThat(evento1).isEqualTo(evento2);
            assertThat(evento1.hashCode()).isEqualTo(evento2.hashCode());
        }

        @Test
        @DisplayName("Eventos com apólices diferentes não devem ser iguais")
        void eventosComApolicesDiferentesNaoDevemSerIguais() {
            LocalDate data = LocalDate.now();

            VeiculoDesassociadoEvent evento1 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, "APO-001", data, MOTIVO, OPERADOR_ID
            );

            VeiculoDesassociadoEvent evento2 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, "APO-002", data, MOTIVO, OPERADOR_ID
            );

            assertThat(evento1).isNotEqualTo(evento2);
        }

        @Test
        @DisplayName("Eventos com motivos diferentes não devem ser iguais")
        void eventosComMotivosDiferentesNaoDevemSerIguais() {
            LocalDate data = LocalDate.now();

            VeiculoDesassociadoEvent evento1 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, data, "Cancelamento", OPERADOR_ID
            );

            VeiculoDesassociadoEvent evento2 = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, data, "Venda", OPERADOR_ID
            );

            assertThat(evento1).isNotEqualTo(evento2);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            VeiculoDesassociadoEvent evento = VeiculoDesassociadoEvent.create(
                AGGREGATE_ID, 3L, APOLICE_ID, LocalDate.now(), MOTIVO, OPERADOR_ID
            );

            String toString = evento.toString();

            assertThat(toString).contains(AGGREGATE_ID);
            assertThat(toString).contains(APOLICE_ID);
            assertThat(toString).contains(MOTIVO);
        }
    }
}
