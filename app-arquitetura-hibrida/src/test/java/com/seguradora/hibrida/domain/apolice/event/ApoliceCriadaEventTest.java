package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceCriadaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ApoliceCriadaEvent - Testes Unitários")
class ApoliceCriadaEventTest {

    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule());

    @Test
    @DisplayName("Deve criar evento com todos os campos válidos")
    void deveCriarEventoComTodosCamposValidos() {
        // Arrange
        String aggregateId = "apolice-123";
        String numeroApolice = "AP-2026-001";
        String seguradoId = "segurado-456";
        String produto = "AUTO";
        String vigenciaInicio = "2026-01-01";
        String vigenciaFim = "2027-01-01";
        String valorSegurado = "50000.00";
        String formaPagamento = "MENSAL";
        List<Map<String, Object>> coberturas = List.of(
                Map.of(
                        "tipo", "COLISAO",
                        "valorCobertura", "50000.00",
                        "franquia", "1000.00",
                        "carenciaDias", 30,
                        "ativa", true
                )
        );
        String premioTotal = "2400.00";
        String operadorId = "operador-789";

        // Act
        ApoliceCriadaEvent evento = new ApoliceCriadaEvent(
                aggregateId,
                numeroApolice,
                seguradoId,
                produto,
                vigenciaInicio,
                vigenciaFim,
                valorSegurado,
                formaPagamento,
                coberturas,
                premioTotal,
                operadorId
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getProduto()).isEqualTo(produto);
        assertThat(evento.getVigenciaInicio()).isEqualTo(vigenciaInicio);
        assertThat(evento.getVigenciaFim()).isEqualTo(vigenciaFim);
        assertThat(evento.getValorSegurado()).isEqualTo(valorSegurado);
        assertThat(evento.getFormaPagamento()).isEqualTo(formaPagamento);
        assertThat(evento.getCoberturas()).isEqualTo(coberturas);
        assertThat(evento.getPremioTotal()).isEqualTo(premioTotal);
        assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        assertThat(evento.getEventType()).isEqualTo("ApoliceCriadaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getVersion()).isEqualTo(1L);
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve remover espaços em branco dos campos String")
    void deveRemoverEspacosEmBrancoCamposString() {
        // Arrange & Act
        ApoliceCriadaEvent evento = new ApoliceCriadaEvent(
                "  apolice-123  ",
                "  AP-2026-001  ",
                "  segurado-456  ",
                "  AUTO  ",
                "  2026-01-01  ",
                "  2027-01-01  ",
                "  50000.00  ",
                "  MENSAL  ",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "  2400.00  ",
                "  operador-789  "
        );

        // Assert
        assertThat(evento.getNumeroApolice()).isEqualTo("AP-2026-001");
        assertThat(evento.getSeguradoId()).isEqualTo("segurado-456");
        assertThat(evento.getProduto()).isEqualTo("AUTO");
        assertThat(evento.getVigenciaInicio()).isEqualTo("2026-01-01");
        assertThat(evento.getVigenciaFim()).isEqualTo("2027-01-01");
        assertThat(evento.getValorSegurado()).isEqualTo("50000.00");
        assertThat(evento.getFormaPagamento()).isEqualTo("MENSAL");
        assertThat(evento.getPremioTotal()).isEqualTo("2400.00");
        assertThat(evento.getOperadorId()).isEqualTo("operador-789");
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é nulo")
    void deveLancarExcecaoQuandoNumeroApoliceNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                null,
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é vazio")
    void deveLancarExcecaoQuandoNumeroApoliceVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "   ",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado ID é nulo")
    void deveLancarExcecaoQuandoSeguradoIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                null,
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando produto é nulo")
    void deveLancarExcecaoQuandoProdutoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                null,
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Produto não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando vigência início é nula")
    void deveLancarExcecaoQuandoVigenciaInicioNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                null,
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de início da vigência não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando vigência fim é nula")
    void deveLancarExcecaoQuandoVigenciaFimNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                null,
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de fim da vigência não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor segurado é nulo")
    void deveLancarExcecaoQuandoValorSeguradoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                null,
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando forma de pagamento é nula")
    void deveLancarExcecaoQuandoFormaPagamentoNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                null,
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Forma de pagamento não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando coberturas é nula")
    void deveLancarExcecaoQuandoCoberturasNula() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                null,
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lista de coberturas não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando coberturas está vazia")
    void deveLancarExcecaoQuandoCoberturasVazia() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(),
                "2400.00",
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Lista de coberturas não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando prêmio total é nulo")
    void deveLancarExcecaoQuandoPremioTotalNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                null,
                "operador-789"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prêmio total não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador ID é nulo")
    void deveLancarExcecaoQuandoOperadorIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do operador não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        ApoliceCriadaEvent eventoOriginal = new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceCriadaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceCriadaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getSeguradoId()).isEqualTo(eventoOriginal.getSeguradoId());
        assertThat(eventoDesserializado.getProduto()).isEqualTo(eventoOriginal.getProduto());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        ApoliceCriadaEvent evento = new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                List.of(Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true)),
                "2400.00",
                "operador-789"
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("ApoliceCriadaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("segurado-456")
                .contains("AUTO");
    }

    @Test
    @DisplayName("Deve criar evento com múltiplas coberturas")
    void deveCriarEventoComMultiplasCoberturas() {
        // Arrange
        List<Map<String, Object>> coberturas = List.of(
                Map.of("tipo", "COLISAO", "valorCobertura", "50000.00",
                        "franquia", "1000.00", "carenciaDias", 30, "ativa", true),
                Map.of("tipo", "ROUBO", "valorCobertura", "40000.00",
                        "franquia", "2000.00", "carenciaDias", 0, "ativa", true),
                Map.of("tipo", "INCENDIO", "valorCobertura", "50000.00",
                        "franquia", "500.00", "carenciaDias", 15, "ativa", true)
        );

        // Act
        ApoliceCriadaEvent evento = new ApoliceCriadaEvent(
                "apolice-123",
                "AP-2026-001",
                "segurado-456",
                "AUTO",
                "2026-01-01",
                "2027-01-01",
                "50000.00",
                "MENSAL",
                coberturas,
                "2400.00",
                "operador-789"
        );

        // Assert
        assertThat(evento.getCoberturas()).hasSize(3);
        assertThat(evento.getCoberturas()).containsAll(coberturas);
    }
}
