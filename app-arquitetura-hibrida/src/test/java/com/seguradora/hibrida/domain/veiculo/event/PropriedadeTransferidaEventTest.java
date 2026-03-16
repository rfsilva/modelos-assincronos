package com.seguradora.hibrida.domain.veiculo.event;

import com.seguradora.hibrida.domain.veiculo.model.Proprietario;
import com.seguradora.hibrida.domain.veiculo.model.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link PropriedadeTransferidaEvent}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("PropriedadeTransferidaEvent - Testes Unitários")
class PropriedadeTransferidaEventTest {

    private static final String AGGREGATE_ID = "VEI-001";
    private static final String OPERADOR_ID = "OP-123";

    @Nested
    @DisplayName("Testes de Criação do Evento")
    class CriacaoEventoTests {

        @Test
        @DisplayName("Deve criar evento com dados válidos")
        void deveCriarEventoComDadosValidos() {
            // Arrange
            Proprietario proprietarioAnterior = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate dataTransferencia = LocalDate.now();

            // Act
            PropriedadeTransferidaEvent evento = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                dataTransferencia, OPERADOR_ID, "Venda particular"
            );

            // Assert
            assertThat(evento).isNotNull();
            assertThat(evento.getAggregateId()).isEqualTo(AGGREGATE_ID);
            assertThat(evento.getVersion()).isEqualTo(4L);
            assertThat(evento.getProprietarioAnterior()).isEqualTo(proprietarioAnterior);
            assertThat(evento.getNovoProprietario()).isEqualTo(novoProprietario);
            assertThat(evento.getDataTransferencia()).isEqualTo(dataTransferencia);
            assertThat(evento.getOperadorId()).isEqualTo(OPERADOR_ID);
            assertThat(evento.getObservacoes()).isEqualTo("Venda particular");
            assertThat(evento.getEventType()).isEqualTo("PropriedadeTransferida");
        }

        @Test
        @DisplayName("Deve aceitar observações nulas")
        void deveAceitarObservacoesNulas() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now(), OPERADOR_ID, null
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lançar exceção para proprietário anterior nulo")
        void deveLancarExcecaoParaProprietarioAnteriorNulo() {
            Proprietario novoProprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, null, novoProprietario,
                LocalDate.now(), OPERADOR_ID, "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Proprietário anterior não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para novo proprietário nulo")
        void deveLancarExcecaoParaNovoProprietarioNulo() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, null,
                LocalDate.now(), OPERADOR_ID, "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Novo proprietário não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção para data de transferência nula")
        void deveLancarExcecaoParaDataTransferenciaNula() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                null, OPERADOR_ID, "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de transferência não pode ser nula");
        }

        @Test
        @DisplayName("Deve lançar exceção para data de transferência futura")
        void deveLancarExcecaoParaDataTransferenciaFutura() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate dataFutura = LocalDate.now().plusDays(1);

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                dataFutura, OPERADOR_ID, "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Data de transferência não pode ser futura");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID nulo")
        void deveLancarExcecaoParaOperadorIdNulo() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now(), null, "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do operador não pode ser nulo ou vazio");
        }

        @Test
        @DisplayName("Deve lançar exceção para operador ID vazio")
        void deveLancarExcecaoParaOperadorIdVazio() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            assertThatThrownBy(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now(), "   ", "Transferência"
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do operador não pode ser nulo ou vazio");
        }
    }

    @Nested
    @DisplayName("Testes de Tipos de Transferência")
    class TiposTransferenciaTests {

        @Test
        @DisplayName("Deve permitir transferência de PF para PF")
        void devePermitirTransferenciaDePfParaPf() {
            Proprietario pf1 = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);
            Proprietario pf2 = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, pf1, pf2, LocalDate.now(), OPERADOR_ID, "Venda"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve permitir transferência de PF para PJ")
        void devePermitirTransferenciaDePfParaPj() {
            Proprietario pf = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);
            Proprietario pj = Proprietario.exemploEmpresa();

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, pf, pj, LocalDate.now(), OPERADOR_ID, "Incorporação"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve permitir transferência de PJ para PF")
        void devePermitirTransferenciaDePjParaPf() {
            Proprietario pj = Proprietario.exemploEmpresa();
            Proprietario pf = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, pj, pf, LocalDate.now(), OPERADOR_ID, "Venda"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve permitir transferência de PJ para PJ")
        void devePermitirTransferenciaDePjParaPj() {
            Proprietario pj1 = Proprietario.of("11222333000181", "Empresa A", TipoPessoa.JURIDICA);
            Proprietario pj2 = Proprietario.of("11444777000161", "Empresa B", TipoPessoa.JURIDICA);

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, pj1, pj2, LocalDate.now(), OPERADOR_ID, "Fusão"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Datas")
    class DatasTests {

        @Test
        @DisplayName("Deve aceitar data no passado")
        void deveAceitarDataNoPassado() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate dataPassado = LocalDate.now().minusDays(30);

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                dataPassado, OPERADOR_ID, "Transferência antiga"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar data atual")
        void deveAceitarDataAtual() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate dataAtual = LocalDate.now();

            assertThatCode(() -> PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                dataAtual, OPERADOR_ID, "Transferência"
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Observações")
    class ObservacoesTests {

        @Test
        @DisplayName("Deve aceitar observações com motivo de venda")
        void deveAceitarObservacoesComMotivoVenda() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            PropriedadeTransferidaEvent evento = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now(), OPERADOR_ID, "Venda particular por R$ 50.000,00"
            );

            assertThat(evento.getObservacoes()).contains("Venda");
            assertThat(evento.getObservacoes()).contains("50.000");
        }

        @Test
        @DisplayName("Deve aceitar observações com motivo de doação")
        void deveAceitarObservacoesComMotivoDoacao() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            PropriedadeTransferidaEvent evento = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now(), OPERADOR_ID, "Doação familiar"
            );

            assertThat(evento.getObservacoes()).contains("Doação");
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Eventos com mesmos dados devem ser iguais")
        void eventosComMesmosDadosDevemSerIguais() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate data = LocalDate.now().minusDays(1);

            PropriedadeTransferidaEvent evento1 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                data, OPERADOR_ID, "Venda"
            );

            PropriedadeTransferidaEvent evento2 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                data, OPERADOR_ID, "Venda"
            );

            assertThat(evento1).isEqualTo(evento2);
            assertThat(evento1.hashCode()).isEqualTo(evento2.hashCode());
        }

        @Test
        @DisplayName("Eventos com proprietários diferentes não devem ser iguais")
        void eventosComProprietariosDiferentesNaoDevemSerIguais() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario1 = Proprietario.of("98765432100", "Maria Santos", TipoPessoa.FISICA);
            Proprietario novoProprietario2 = Proprietario.of("52998224725", "Carlos Costa", TipoPessoa.FISICA);
            LocalDate data = LocalDate.now().minusDays(1);

            PropriedadeTransferidaEvent evento1 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario1,
                data, OPERADOR_ID, "Venda"
            );

            PropriedadeTransferidaEvent evento2 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario2,
                data, OPERADOR_ID, "Venda"
            );

            assertThat(evento1).isNotEqualTo(evento2);
        }

        @Test
        @DisplayName("Eventos com datas diferentes não devem ser iguais")
        void eventosComDatasDiferentesNaoDevemSerIguais() {
            Proprietario proprietarioAnterior = Proprietario.exemplo();
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);

            PropriedadeTransferidaEvent evento1 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now().minusDays(2), OPERADOR_ID, "Venda"
            );

            PropriedadeTransferidaEvent evento2 = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                LocalDate.now().minusDays(1), OPERADOR_ID, "Venda"
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
            Proprietario proprietarioAnterior = Proprietario.of("12345678909", "João Silva", TipoPessoa.FISICA);
            Proprietario novoProprietario = Proprietario.of("12345678909", "Maria Santos", TipoPessoa.FISICA);
            LocalDate data = LocalDate.now().minusDays(1);

            PropriedadeTransferidaEvent evento = PropriedadeTransferidaEvent.create(
                AGGREGATE_ID, 4L, proprietarioAnterior, novoProprietario,
                data, OPERADOR_ID, "Venda"
            );

            String toString = evento.toString();

            assertThat(toString).contains(AGGREGATE_ID);
            assertThat(toString).contains("João Silva");
            assertThat(toString).contains("Maria Santos");
            assertThat(toString).contains(data.toString());
        }
    }
}
