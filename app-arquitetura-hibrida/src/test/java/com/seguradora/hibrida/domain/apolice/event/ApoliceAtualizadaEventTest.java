package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceAtualizadaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ApoliceAtualizadaEvent - Testes Unitários")
class ApoliceAtualizadaEventTest {

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
        Map<String, Object> alteracoes = Map.of("valorSegurado", true, "formaPagamento", true);
        Map<String, Object> valoresAnteriores = Map.of("valorSegurado", "50000.00", "formaPagamento", "MENSAL");
        Map<String, Object> novosValores = Map.of("valorSegurado", "60000.00", "formaPagamento", "ANUAL");
        String operadorId = "operador-789";
        String motivo = "Solicitação de aumento de cobertura";

        // Act
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                aggregateId,
                version,
                numeroApolice,
                seguradoId,
                alteracoes,
                valoresAnteriores,
                novosValores,
                operadorId,
                motivo
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getVersion()).isEqualTo(version);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getAlteracoes()).isEqualTo(alteracoes);
        assertThat(evento.getValoresAnteriores()).isEqualTo(valoresAnteriores);
        assertThat(evento.getNovosValores()).isEqualTo(novosValores);
        assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        assertThat(evento.getMotivo()).isEqualTo(motivo);
        assertThat(evento.getEventType()).isEqualTo("ApoliceAtualizadaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar evento usando factory method")
    void deveCriarEventoUsandoFactoryMethod() {
        // Arrange
        Map<String, Object> alteracoes = Map.of("valorSegurado", true);
        Map<String, Object> valoresAnteriores = Map.of("valorSegurado", "50000.00");
        Map<String, Object> novosValores = Map.of("valorSegurado", "60000.00");

        // Act
        ApoliceAtualizadaEvent evento = ApoliceAtualizadaEvent.create(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                alteracoes,
                valoresAnteriores,
                novosValores,
                "operador-789",
                "Aumento de cobertura"
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo("apolice-123");
        assertThat(evento.getVersion()).isEqualTo(2L);
    }

    @Test
    @DisplayName("Deve remover espaços em branco dos campos String")
    void deveRemoverEspacosEmBrancoCamposString() {
        // Arrange & Act
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "  AP-2026-001  ",
                "  segurado-456  ",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "  operador-789  ",
                "  Aumento de cobertura  "
        );

        // Assert
        assertThat(evento.getNumeroApolice()).isEqualTo("AP-2026-001");
        assertThat(evento.getSeguradoId()).isEqualTo("segurado-456");
        assertThat(evento.getOperadorId()).isEqualTo("operador-789");
        assertThat(evento.getMotivo()).isEqualTo("Aumento de cobertura");
    }

    @Test
    @DisplayName("Deve verificar se campo foi alterado")
    void deveVerificarSeCampoFoiAlterado() {
        // Arrange
        Map<String, Object> alteracoes = Map.of("valorSegurado", true, "formaPagamento", true);
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                alteracoes,
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        );

        // Act & Assert
        assertThat(evento.foiAlterado("valorSegurado")).isTrue();
        assertThat(evento.foiAlterado("formaPagamento")).isTrue();
        assertThat(evento.foiAlterado("produto")).isFalse();
    }

    @Test
    @DisplayName("Deve obter valor anterior de campo alterado")
    void deveObterValorAnteriorCampoAlterado() {
        // Arrange
        Map<String, Object> valoresAnteriores = Map.of("valorSegurado", "50000.00");
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                valoresAnteriores,
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        );

        // Act
        String valorAnterior = evento.getValorAnterior("valorSegurado");

        // Assert
        assertThat(valorAnterior).isEqualTo("50000.00");
    }

    @Test
    @DisplayName("Deve obter novo valor de campo alterado")
    void deveObterNovoValorCampoAlterado() {
        // Arrange
        Map<String, Object> novosValores = Map.of("valorSegurado", "60000.00");
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                novosValores,
                "operador-789",
                "Atualização"
        );

        // Act
        String novoValor = evento.getNovoValor("valorSegurado");

        // Assert
        assertThat(novoValor).isEqualTo("60000.00");
    }

    @Test
    @DisplayName("Deve gerar descrição das alterações")
    void deveGerarDescricaoAlteracoes() {
        // Arrange
        Map<String, Object> alteracoes = new HashMap<>();
        alteracoes.put("valorSegurado", true);
        alteracoes.put("formaPagamento", true);

        Map<String, Object> valoresAnteriores = new HashMap<>();
        valoresAnteriores.put("valorSegurado", "50000.00");
        valoresAnteriores.put("formaPagamento", "MENSAL");

        Map<String, Object> novosValores = new HashMap<>();
        novosValores.put("valorSegurado", "60000.00");
        novosValores.put("formaPagamento", "ANUAL");

        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                alteracoes,
                valoresAnteriores,
                novosValores,
                "operador-789",
                "Atualização"
        );

        // Act
        String descricao = evento.getDescricaoAlteracoes();

        // Assert
        assertThat(descricao)
                .contains("valorSegurado")
                .contains("formaPagamento")
                .contains("50000.00")
                .contains("60000.00")
                .contains("MENSAL")
                .contains("ANUAL");
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é nulo")
    void deveLancarExcecaoQuandoNumeroApoliceNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                null,
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado ID é nulo")
    void deveLancarExcecaoQuandoSeguradoIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                null,
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando alterações é nulo")
    void deveLancarExcecaoQuandoAlteracoesNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                null,
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapa de alterações não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando alterações está vazio")
    void deveLancarExcecaoQuandoAlteracoesVazio() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of(),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Mapa de alterações não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valores anteriores é nulo")
    void deveLancarExcecaoQuandoValoresAnterioresNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                null,
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valores anteriores não podem ser nulos");
    }

    @Test
    @DisplayName("Deve lançar exceção quando novos valores é nulo")
    void deveLancarExcecaoQuandoNovosValoresNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                null,
                "operador-789",
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Novos valores não podem ser nulos");
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador ID é nulo")
    void deveLancarExcecaoQuandoOperadorIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                null,
                "Atualização"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do operador não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo é nulo")
    void deveLancarExcecaoQuandoMotivoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo da alteração não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        ApoliceAtualizadaEvent eventoOriginal = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Aumento de cobertura"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceAtualizadaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceAtualizadaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getSeguradoId()).isEqualTo(eventoOriginal.getSeguradoId());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        ApoliceAtualizadaEvent evento = new ApoliceAtualizadaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                Map.of("valorSegurado", true),
                Map.of("valorSegurado", "50000.00"),
                Map.of("valorSegurado", "60000.00"),
                "operador-789",
                "Atualização"
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("ApoliceAtualizadaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("operador-789");
    }
}
