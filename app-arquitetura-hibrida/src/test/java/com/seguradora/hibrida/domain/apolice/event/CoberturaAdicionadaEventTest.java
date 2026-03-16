package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para CoberturaAdicionadaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("CoberturaAdicionadaEvent - Testes Unitários")
class CoberturaAdicionadaEventTest {

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
        String tipoCobertura = "VIDROS";
        String valorCobertura = "5000.00";
        String franquia = "200.00";
        int carenciaDias = 15;
        String valorAdicionalPremio = "150.00";
        String dataEfeito = "2026-04-01";
        String operadorId = "operador-789";
        String motivo = "Solicitação do segurado para proteção adicional";

        // Act
        CoberturaAdicionadaEvent evento = new CoberturaAdicionadaEvent(
                aggregateId,
                version,
                numeroApolice,
                seguradoId,
                tipoCobertura,
                valorCobertura,
                franquia,
                carenciaDias,
                valorAdicionalPremio,
                dataEfeito,
                operadorId,
                motivo
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getVersion()).isEqualTo(version);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getTipoCobertura()).isEqualTo(tipoCobertura);
        assertThat(evento.getValorCobertura()).isEqualTo(valorCobertura);
        assertThat(evento.getFranquia()).isEqualTo(franquia);
        assertThat(evento.getCarenciaDias()).isEqualTo(carenciaDias);
        assertThat(evento.getValorAdicionalPremio()).isEqualTo(valorAdicionalPremio);
        assertThat(evento.getDataEfeito()).isEqualTo(dataEfeito);
        assertThat(evento.getOperadorId()).isEqualTo(operadorId);
        assertThat(evento.getMotivo()).isEqualTo(motivo);
        assertThat(evento.getEventType()).isEqualTo("CoberturaAdicionadaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar evento usando factory method")
    void deveCriarEventoUsandoFactoryMethod() {
        // Act
        CoberturaAdicionadaEvent evento = CoberturaAdicionadaEvent.create(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "VIDROS",
                "5000.00",
                "200.00",
                15,
                "150.00",
                "2026-04-01",
                "operador-789",
                "Solicitação do segurado"
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo("apolice-123");
        assertThat(evento.getVersion()).isEqualTo(2L);
        assertThat(evento.getTipoCobertura()).isEqualTo("VIDROS");
    }

    @Test
    @DisplayName("Deve remover espaços em branco dos campos String")
    void deveRemoverEspacosEmBrancoCamposString() {
        // Arrange & Act
        CoberturaAdicionadaEvent evento = new CoberturaAdicionadaEvent(
                "apolice-123",
                2L,
                "  AP-2026-001  ",
                "  segurado-456  ",
                "  VIDROS  ",
                "  5000.00  ",
                "  200.00  ",
                15,
                "  150.00  ",
                "  2026-04-01  ",
                "  operador-789  ",
                "  Solicitação do segurado  "
        );

        // Assert
        assertThat(evento.getNumeroApolice()).isEqualTo("AP-2026-001");
        assertThat(evento.getSeguradoId()).isEqualTo("segurado-456");
        assertThat(evento.getTipoCobertura()).isEqualTo("VIDROS");
        assertThat(evento.getValorCobertura()).isEqualTo("5000.00");
        assertThat(evento.getFranquia()).isEqualTo("200.00");
        assertThat(evento.getValorAdicionalPremio()).isEqualTo("150.00");
        assertThat(evento.getDataEfeito()).isEqualTo("2026-04-01");
        assertThat(evento.getOperadorId()).isEqualTo("operador-789");
        assertThat(evento.getMotivo()).isEqualTo("Solicitação do segurado");
    }

    @Test
    @DisplayName("Deve verificar se cobertura tem carência")
    void deveVerificarSeCoberturaTemCarencia() {
        // Arrange
        CoberturaAdicionadaEvent eventoComCarencia = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 30, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        CoberturaAdicionadaEvent eventoSemCarencia = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 0, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        // Act & Assert
        assertThat(eventoComCarencia.temCarencia()).isTrue();
        assertThat(eventoSemCarencia.temCarencia()).isFalse();
    }

    @Test
    @DisplayName("Deve verificar se há valor adicional no prêmio")
    void deveVerificarSeHaValorAdicional() {
        // Arrange
        CoberturaAdicionadaEvent eventoComValorAdicional = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        CoberturaAdicionadaEvent eventoSemValorAdicional = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "0.00", "2026-04-01", "operador-789", "Motivo"
        );

        CoberturaAdicionadaEvent eventoComValorInvalido = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "invalido", "2026-04-01", "operador-789", "Motivo"
        );

        // Act & Assert
        assertThat(eventoComValorAdicional.temValorAdicional()).isTrue();
        assertThat(eventoSemValorAdicional.temValorAdicional()).isFalse();
        assertThat(eventoComValorInvalido.temValorAdicional()).isFalse();
    }

    @Test
    @DisplayName("Deve criar cobertura sem carência")
    void deveCriarCoberturaSemCarencia() {
        // Act
        CoberturaAdicionadaEvent evento = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 0, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        // Assert
        assertThat(evento.getCarenciaDias()).isEqualTo(0);
        assertThat(evento.temCarencia()).isFalse();
    }

    @Test
    @DisplayName("Deve criar cobertura com carência máxima")
    void deveCriarCoberturaComCarenciaMaxima() {
        // Act
        CoberturaAdicionadaEvent evento = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 365, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        // Assert
        assertThat(evento.getCarenciaDias()).isEqualTo(365);
        assertThat(evento.temCarencia()).isTrue();
    }

    @Test
    @DisplayName("Deve lançar exceção quando número da apólice é nulo")
    void deveLancarExcecaoQuandoNumeroApoliceNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, null, "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Número da apólice não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando segurado ID é nulo")
    void deveLancarExcecaoQuandoSeguradoIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", null, "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do segurado não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando tipo de cobertura é nulo")
    void deveLancarExcecaoQuandoTipoCoberturaOuloNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", null,
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Tipo de cobertura não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor cobertura é nulo")
    void deveLancarExcecaoQuandoValorCoberturaNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                null, "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor da cobertura não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando franquia é nula")
    void deveLancarExcecaoQuandoFranquiaNula() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", null, 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Franquia não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando carência é negativa")
    void deveLancarExcecaoQuandoCarenciaNegativa() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", -1, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Carência deve estar entre 0 e 365 dias");
    }

    @Test
    @DisplayName("Deve lançar exceção quando carência é maior que 365 dias")
    void deveLancarExcecaoQuandoCarenciaMaiorQue365() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 366, "150.00", "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Carência deve estar entre 0 e 365 dias");
    }

    @Test
    @DisplayName("Deve lançar exceção quando valor adicional prêmio é nulo")
    void deveLancarExcecaoQuandoValorAdicionalPremioNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, null, "2026-04-01", "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Valor adicional do prêmio não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando data de efeito é nula")
    void deveLancarExcecaoQuandoDataEfeitoNula() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", null, "operador-789", "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Data de efeito não pode ser nula ou vazia");
    }

    @Test
    @DisplayName("Deve lançar exceção quando operador ID é nulo")
    void deveLancarExcecaoQuandoOperadorIdNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", null, "Motivo"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("ID do operador não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve lançar exceção quando motivo é nulo")
    void deveLancarExcecaoQuandoMotivoNulo() {
        // Act & Assert
        assertThatThrownBy(() -> new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", null
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Motivo da adição não pode ser nulo ou vazio");
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        CoberturaAdicionadaEvent eventoOriginal = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789",
                "Solicitação do segurado"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        CoberturaAdicionadaEvent eventoDesserializado = objectMapper.readValue(json, CoberturaAdicionadaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getTipoCobertura()).isEqualTo(eventoOriginal.getTipoCobertura());
        assertThat(eventoDesserializado.getCarenciaDias()).isEqualTo(eventoOriginal.getCarenciaDias());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        CoberturaAdicionadaEvent evento = new CoberturaAdicionadaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "VIDROS",
                "5000.00", "200.00", 15, "150.00", "2026-04-01", "operador-789", "Motivo"
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("CoberturaAdicionadaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("VIDROS")
                .contains("5000.00")
                .contains("2026-04-01")
                .contains("operador-789");
    }
}
