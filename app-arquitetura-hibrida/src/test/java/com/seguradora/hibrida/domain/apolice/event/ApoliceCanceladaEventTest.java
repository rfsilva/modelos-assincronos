package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent.TipoCancelamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceCanceladaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ApoliceCanceladaEvent - Testes Unitários")
class ApoliceCanceladaEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Deve criar evento com todos os campos válidos")
    void deveCriarEventoComTodosCamposValidos() {
        // Arrange
        String aggregateId = "apolice-123";
        long version = 2L;
        String numeroApolice = "AP-2026-001";
        String seguradoId = "segurado-456";
        String valorSegurado = "50000.00";
        String motivo = "Solicitação do segurado";
        String dataEfeito = "2026-06-01";
        String valorReembolso = "1200.00";
        String operadorId = "operador-789";
        String observacoes = "Cliente vendeu o veículo";
        String tipoCancelamento = "SOLICITACAO_SEGURADO";

        // Act
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                aggregateId,
                version,
                numeroApolice,
                seguradoId,
                valorSegurado,
                motivo,
                dataEfeito,
                valorReembolso,
                operadorId,
                observacoes,
                tipoCancelamento
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getVersion()).isEqualTo(version);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getValorSegurado()).isEqualTo(valorSegurado);
        assertThat(evento.getMotivo()).isEqualTo(motivo);
        assertThat(evento.getDataEfeito()).isEqualTo(dataEfeito);
        assertThat(evento.getValorReembolso()).isEqualTo(valorReembolso);
        assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        assertThat(evento.getObservacoes()).isEqualTo(observacoes);
        assertThat(evento.getTipoCancelamento()).isEqualTo(tipoCancelamento);
        assertThat(evento.getEventType()).isEqualTo("ApoliceCanceladaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar evento usando factory method")
    void deveCriarEventoUsandoFactoryMethod() {
        // Arrange
        LocalDate dataEfeito = LocalDate.of(2026, 6, 1);

        // Act
        ApoliceCanceladaEvent evento = ApoliceCanceladaEvent.create(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                dataEfeito,
                "1200.00",
                "operador-789",
                "Cliente vendeu o veículo",
                TipoCancelamento.SOLICITACAO_SEGURADO
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getDataEfeito()).isEqualTo("2026-06-01");
        assertThat(evento.getTipoCancelamento()).isEqualTo("SOLICITACAO_SEGURADO");
    }

    @Test
    @DisplayName("Deve remover espaços em branco dos campos String")
    void deveRemoverEspacosEmBrancoCamposString() {
        // Arrange & Act
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "  AP-2026-001  ",
                "  segurado-456  ",
                "  50000.00  ",
                "  Solicitação do segurado  ",
                "  2026-06-01  ",
                "  1200.00  ",
                "  operador-789  ",
                "Observações",
                "  SOLICITACAO_SEGURADO  "
        );

        // Assert
        assertThat(evento.getNumeroApolice()).isEqualTo("AP-2026-001");
        assertThat(evento.getSeguradoId()).isEqualTo("segurado-456");
        assertThat(evento.getValorSegurado()).isEqualTo("50000.00");
        assertThat(evento.getMotivo()).isEqualTo("Solicitação do segurado");
        assertThat(evento.getDataEfeito()).isEqualTo("2026-06-01");
        assertThat(evento.getValorReembolso()).isEqualTo("1200.00");
        assertThat(evento.getOperadorId()).isEqualTo("operador-789");
        assertThat(evento.getTipoCancelamento()).isEqualTo("SOLICITACAO_SEGURADO");
    }

    @Test
    @DisplayName("Deve verificar se cancelamento foi por solicitação do segurado")
    void deveVerificarCancelamentoPorSegurado() {
        // Arrange
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        );

        // Act & Assert
        assertThat(evento.isCancelamentoPorSegurado()).isTrue();
        assertThat(evento.isCancelamentoPorInadimplencia()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se cancelamento foi por inadimplência")
    void deveVerificarCancelamentoPorInadimplencia() {
        // Arrange
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Falta de pagamento",
                "2026-06-01",
                "0.00",
                "operador-789",
                null,
                "INADIMPLENCIA"
        );

        // Act & Assert
        assertThat(evento.isCancelamentoPorInadimplencia()).isTrue();
        assertThat(evento.isCancelamentoPorSegurado()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se há valor de reembolso")
    void deveVerificarSeHaReembolso() {
        // Arrange
        ApoliceCanceladaEvent eventoComReembolso = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        );

        ApoliceCanceladaEvent eventoSemReembolso = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Inadimplência",
                "2026-06-01",
                "0.00",
                "operador-789",
                null,
                "INADIMPLENCIA"
        );

        // Act & Assert
        assertThat(eventoComReembolso.temReembolso()).isTrue();
        assertThat(eventoSemReembolso.temReembolso()).isFalse();
    }

    @Test
    @DisplayName("Deve permitir observações nulas")
    void devePermitirObservacoesNulas() {
        // Act
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        );

        // Assert
        assertThat(evento.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é nulo")
    void deveLancarExcecaoQuandoNumeroApoliceNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                null,
                "segurado-456",
                "50000.00",
                "Motivo",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado ID é nulo")
    void deveLancarExcecaoQuandoSeguradoIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                null,
                "50000.00",
                "Motivo",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor segurado é nulo")
    void deveLancarExcecaoQuandoValorSeguradoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                null,
                "Motivo",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo é nulo")
    void deveLancarExcecaoQuandoMotivoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                null,
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo do cancelamento não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de efeito é nula")
    void deveLancarExcecaoQuandoDataEfeitoNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Motivo",
                null,
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de efeito do cancelamento não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor reembolso é nulo")
    void deveLancarExcecaoQuandoValorReembolsoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Motivo",
                "2026-06-01",
                null,
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor de reembolso não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador ID é nulo")
    void deveLancarExcecaoQuandoOperadorIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Motivo",
                "2026-06-01",
                "1200.00",
                null,
                null,
                "SOLICITACAO_SEGURADO"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do operador não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de cancelamento é nulo")
    void deveLancarExcecaoQuandoTipoCancelamentoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Motivo",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de cancelamento não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        ApoliceCanceladaEvent eventoOriginal = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                "2026-06-01",
                "1200.00",
                "operador-789",
                "Observações",
                "SOLICITACAO_SEGURADO"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceCanceladaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceCanceladaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getTipoCancelamento()).isEqualTo(eventoOriginal.getTipoCancelamento());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        ApoliceCanceladaEvent evento = new ApoliceCanceladaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "50000.00",
                "Solicitação do segurado",
                "2026-06-01",
                "1200.00",
                "operador-789",
                null,
                "SOLICITACAO_SEGURADO"
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("ApoliceCanceladaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("Solicitação do segurado")
                .contains("2026-06-01")
                .contains("operador-789");
    }

    @Test
    @DisplayName("Deve testar enum TipoCancelamento")
    void deveTestarEnumTipoCancelamento() {
        // Assert
        assertThat(TipoCancelamento.SOLICITACAO_SEGURADO.getDescricao()).isEqualTo("Solicitação do Segurado");
        assertThat(TipoCancelamento.INADIMPLENCIA.getDescricao()).isEqualTo("Inadimplência");
        assertThat(TipoCancelamento.FRAUDE.getDescricao()).isEqualTo("Fraude Detectada");
        assertThat(TipoCancelamento.DECISAO_SEGURADORA.getDescricao()).isEqualTo("Decisão da Seguradora");
        assertThat(TipoCancelamento.VENDA_VEICULO.getDescricao()).isEqualTo("Venda do Veículo");
        assertThat(TipoCancelamento.PERDA_TOTAL.getDescricao()).isEqualTo("Perda Total do Veículo");
    }
}
