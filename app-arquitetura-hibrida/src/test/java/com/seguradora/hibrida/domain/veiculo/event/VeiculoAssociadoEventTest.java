package com.seguradora.hibrida.domain.veiculo.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoAssociadoEvent}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoAssociadoEvent - Testes Unitários")
class VeiculoAssociadoEventTest {

    private static final String AGGREGATE_ID = "VEI-001";
    private static final String APOLICE_ID = "APO-001";
    private static final String OPERADOR_ID = "OP-123";

    @Nested
    @DisplayName("Testes de Criação do Evento")
    class CriacaoEventoTests {

        @Test
        @DisplayName("Deve criar evento com dados válidos")
        void deveCriarEventoComDadosValidos() {
            // Arrange
            LocalDate dataInicio = LocalDate.now();

            // Act
            VeiculoAssociadoEvent evento = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, dataInicio, OPERADOR_ID
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(evento.getVersion()).isEqualTo(2L);
            assertThat(evento.getApoliceId()).isEqualTo(APOLICE_ID);
            assertThat(evento.getDataInicio()).isEqualTo(dataInicio);
            assertThat(evento.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(evento.getEventType()).isEqualTo("VeiculoAssociado");
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice nulo")
        void deveLancarExcecaoParaIdApoliceNulo() {
            assertThatThrownBy(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, null, LocalDate.now(), OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice vazio")
        void deveLancarExcecaoParaIdApoliceVazio() {
            assertThatThrownBy(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, "   ", LocalDate.now(), OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para data de início nula")
        void deveLancarExcecaoParaDataInicioNula() {
            assertThatThrownBy(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, null, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de início não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID nulo")
        void deveLancarExcecaoParaOperadorIdNulo() {
            assertThatThrownBy(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, LocalDate.now(), null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do operador não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID vazio")
        void deveLancarExcecaoParaOperadorIdVazio() {
            assertThatThrownBy(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, LocalDate.now(), "   "
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
            VeiculoAssociadoEvent evento = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, "  APO-001  ", LocalDate.now(), OPERADOR_ID
            );

            assertThat(evento.getApoliceId()).isEqualTo("APO-001");
        }

        @Test
        @DisplayName("Deve remover espaços do operador ID")
        void deveRemoverEspacosDoOperadorId() {
            VeiculoAssociadoEvent evento = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, LocalDate.now(), "  OP-123  "
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

            assertThatCode(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, dataPassado, OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data no futuro")
        void deveAceitarDataNoFuturo() {
            LocalDate dataFuturo = LocalDate.now().plusDays(30);

            assertThatCode(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, dataFuturo, OPERADOR_ID
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data atual")
        void deveAceitarDataAtual() {
            LocalDate dataAtual = LocalDate.now();

            assertThatCode(() -> VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, dataAtual, OPERADOR_ID
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

            VeiculoAssociadoEvent evento1 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, data, OPERADOR_ID
            );

            VeiculoAssociadoEvent evento2 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, data, OPERADOR_ID
            );

            assertThat(evento1).isEqualTo(evento2);
            assertThat(evento1.hashCode()).isEqualTo(evento2.hashCode());
        }

        @Test
        @DisplayName("Eventos com apólices diferentes não devem ser iguais")
        void eventosComApolicesDiferentesNaoDevemSerIguais() {
            LocalDate data = LocalDate.now();

            VeiculoAssociadoEvent evento1 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, "APO-001", data, OPERADOR_ID
            );

            VeiculoAssociadoEvent evento2 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, "APO-002", data, OPERADOR_ID
            );

            assertThat(evento1).isNotEqualTo(evento2);
        }

        @Test
        @DisplayName("Eventos com datas diferentes não devem ser iguais")
        void eventosComDatasDiferentesNaoDevemSerIguais() {
            VeiculoAssociadoEvent evento1 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, LocalDate.now(), OPERADOR_ID
            );

            VeiculoAssociadoEvent evento2 = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, LocalDate.now().plusDays(1), OPERADOR_ID
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
            LocalDate data = LocalDate.now();

            VeiculoAssociadoEvent evento = VeiculoAssociadoEvent.create(
                AGGREGATE_ID, 2L, APOLICE_ID, data, OPERADOR_ID
            );

            String toString = evento.toString();

            assertThat(toString).contains(AGGREGATE_ID);
            assertThat(toString).contains(APOLICE_ID);
            assertThat(toString).contains(data.toString());
        }
    }
}
