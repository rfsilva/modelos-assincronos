package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.seguradora.hibrida.domain.apolice.event.ApoliceRenovadaEvent.TipoRenovacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceRenovadaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ApoliceRenovadaEvent - Testes Unitários")
class ApoliceRenovadaEventTest {

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
        String novaVigenciaInicio = "2027-01-01";
        String novaVigenciaFim = "2028-01-01";
        String novoValorSegurado = "60000.00";
        String novoPremioTotal = "2800.00";
        List<Map<String, Object>> alteracoesCoberturas = List.of(
                Map.of("tipo", "COLISAO", "acao", "ATUALIZADA", "novoValor", "60000.00")
        );
        String novaFormaPagamento = "ANUAL";
        String operadorId = "operador-789";
        String tipoRenovacao = "AUTOMATICA";
        String observacoes = "Renovação automática com reajuste";

        // Act
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                aggregateId,
                version,
                numeroApolice,
                seguradoId,
                novaVigenciaInicio,
                novaVigenciaFim,
                novoValorSegurado,
                novoPremioTotal,
                alteracoesCoberturas,
                novaFormaPagamento,
                operadorId,
                tipoRenovacao,
                observacoes
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getVersion()).isEqualTo(version);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getNovaVigenciaInicio()).isEqualTo(novaVigenciaInicio);
        assertThat(evento.getNovaVigenciaFim()).isEqualTo(novaVigenciaFim);
        assertThat(evento.getNovoValorSegurado()).isEqualTo(novoValorSegurado);
        assertThat(evento.getNovoPremioTotal()).isEqualTo(novoPremioTotal);
        assertThat(evento.getAlteracoesCoberturas()).isEqualTo(alteracoesCoberturas);
        assertThat(evento.getNovaFormaPagamento()).isEqualTo(novaFormaPagamento);
        assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        assertThat(evento.getTipoRenovacao()).isEqualTo(tipoRenovacao);
        assertThat(evento.getObservacoes()).isEqualTo(observacoes);
        assertThat(evento.getEventType()).isEqualTo("ApoliceRenovadaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar evento usando factory method")
    void deveCriarEventoUsandoFactoryMethod() {
        // Arrange
        List<Map<String, Object>> alteracoesCoberturas = List.of(
                Map.of("tipo", "COLISAO", "acao", "ATUALIZADA")
        );

        // Act
        ApoliceRenovadaEvent evento = ApoliceRenovadaEvent.create(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                alteracoesCoberturas,
                "ANUAL",
                "operador-789",
                TipoRenovacao.AUTOMATICA,
                "Renovação automática"
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getTipoRenovacao()).isEqualTo("AUTOMATICA");
    }

    @Test
    @DisplayName("Deve remover espaços em branco dos campos String")
    void deveRemoverEspacosEmBrancoCamposString() {
        // Arrange & Act
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "  AP-2026-001  ",
                "  segurado-456  ",
                "  2027-01-01  ",
                "  2028-01-01  ",
                "  60000.00  ",
                "  2800.00  ",
                List.of(),
                "  ANUAL  ",
                "  operador-789  ",
                "  AUTOMATICA  ",
                "Observações"
        );

        // Assert
        assertThat(evento.getNumeroApolice()).isEqualTo("AP-2026-001");
        assertThat(evento.getSeguradoId()).isEqualTo("segurado-456");
        assertThat(evento.getNovaVigenciaInicio()).isEqualTo("2027-01-01");
        assertThat(evento.getNovaVigenciaFim()).isEqualTo("2028-01-01");
        assertThat(evento.getNovoValorSegurado()).isEqualTo("60000.00");
        assertThat(evento.getNovoPremioTotal()).isEqualTo("2800.00");
        assertThat(evento.getNovaFormaPagamento()).isEqualTo("ANUAL");
        assertThat(evento.getOperadorId()).isEqualTo("operador-789");
        assertThat(evento.getTipoRenovacao()).isEqualTo("AUTOMATICA");
    }

    @Test
    @DisplayName("Deve verificar se houve alterações nas coberturas")
    void deveVerificarAlteracoesCoberturas() {
        // Arrange
        ApoliceRenovadaEvent eventoComAlteracoes = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                List.of(Map.of("tipo", "COLISAO", "acao", "ATUALIZADA")),
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        ApoliceRenovadaEvent eventoSemAlteracoes = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                List.of(),
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        // Act & Assert
        assertThat(eventoComAlteracoes.houveAlteracoesCoberturas()).isTrue();
        assertThat(eventoSemAlteracoes.houveAlteracoesCoberturas()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se renovação é automática")
    void deveVerificarRenovacaoAutomatica() {
        // Arrange
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        // Act & Assert
        assertThat(evento.isRenovacaoAutomatica()).isTrue();
        assertThat(evento.isRenovacaoManual()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se renovação é manual")
    void deveVerificarRenovacaoManual() {
        // Arrange
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "MANUAL",
                null
        );

        // Act & Assert
        assertThat(evento.isRenovacaoManual()).isTrue();
        assertThat(evento.isRenovacaoAutomatica()).isFalse();
    }

    @Test
    @DisplayName("Deve permitir observações nulas")
    void devePermitirObservacoesNulas() {
        // Act
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        // Assert
        assertThat(evento.getObservacoes()).isNull();
    }

    @Test
    @DisplayName("Deve permitir alterações coberturas nulas ou vazias")
    void devePermitirAlteracoesCoberturasNulasOuVazias() {
        // Act
        ApoliceRenovadaEvent eventoNulo = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        ApoliceRenovadaEvent eventoVazio = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                List.of(),
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        // Assert
        assertThat(eventoNulo.getAlteracoesCoberturas()).isNull();
        assertThat(eventoVazio.getAlteracoesCoberturas()).isEmpty();
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é nulo")
    void deveLancarExcecaoQuandoNumeroApoliceNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                null,
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado ID é nulo")
    void deveLancarExcecaoQuandoSeguradoIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                null,
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nova vigência início é nula")
    void deveLancarExcecaoQuandoNovaVigenciaInicioNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                null,
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nova data de início da vigência não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nova vigência fim é nula")
    void deveLancarExcecaoQuandoNovaVigenciaFimNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                null,
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nova data de fim da vigência não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando novo valor segurado é nulo")
    void deveLancarExcecaoQuandoNovoValorSeguradoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                null,
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Novo valor segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando novo prêmio total é nulo")
    void deveLancarExcecaoQuandoNovoPremioTotalNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                null,
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Novo prêmio total não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando nova forma de pagamento é nula")
    void deveLancarExcecaoQuandoNovaFormaPagamentoNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                null,
                "operador-789",
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Nova forma de pagamento não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador ID é nulo")
    void deveLancarExcecaoQuandoOperadorIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                null,
                "AUTOMATICA",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do operador não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de renovação é nulo")
    void deveLancarExcecaoQuandoTipoRenovacaoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                null,
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de renovação não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        ApoliceRenovadaEvent eventoOriginal = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                List.of(Map.of("tipo", "COLISAO", "acao", "ATUALIZADA")),
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                "Renovação automática"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceRenovadaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceRenovadaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getTipoRenovacao()).isEqualTo(eventoOriginal.getTipoRenovacao());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        ApoliceRenovadaEvent evento = new ApoliceRenovadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "2028-01-01",
                "60000.00",
                "2800.00",
                null,
                "ANUAL",
                "operador-789",
                "AUTOMATICA",
                null
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("ApoliceRenovadaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("2027-01-01")
                .contains("2028-01-01")
                .contains("AUTOMATICA")
                .contains("operador-789");
    }

    @Test
    @DisplayName("Deve testar enum TipoRenovacao")
    void deveTestarEnumTipoRenovacao() {
        // Assert
        assertThat(TipoRenovacao.AUTOMATICA.getDescricao()).isEqualTo("Renovação Automática");
        assertThat(TipoRenovacao.MANUAL.getDescricao()).isEqualTo("Renovação Manual");
        assertThat(TipoRenovacao.ANTECIPADA.getDescricao()).isEqualTo("Renovação Antecipada");
        assertThat(TipoRenovacao.COM_ALTERACOES.getDescricao()).isEqualTo("Renovação com Alterações");
        assertThat(TipoRenovacao.NEGOCIADA.getDescricao()).isEqualTo("Renovação Negociada");
    }
}
