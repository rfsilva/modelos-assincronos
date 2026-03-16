package com.seguradora.hibrida.domain.apolice.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceVencidaEvent.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@DisplayName("ApoliceVencidaEvent - Testes Unitários")
class ApoliceVencidaEventTest {

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
        String dataVencimento = "2027-01-01";
        String valorSegurado = "50000.00";

        // Act
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                aggregateId,
                version,
                numeroApolice,
                seguradoId,
                dataVencimento,
                valorSegurado
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo(aggregateId);
        assertThat(evento.getVersion()).isEqualTo(version);
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApolice);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoId);
        assertThat(evento.getDataVencimento()).isEqualTo(dataVencimento);
        assertThat(evento.getValorSegurado()).isEqualTo(valorSegurado);
        assertThat(evento.getEventType()).isEqualTo("ApoliceVencidaEvent");
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar evento usando factory method")
    void deveCriarEventoUsandoFactoryMethod() {
        // Arrange
        LocalDate dataVencimento = LocalDate.of(2027, 1, 1);

        // Act
        ApoliceVencidaEvent evento = ApoliceVencidaEvent.create(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                dataVencimento,
                "50000.00"
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getAggregateId()).isEqualTo("apolice-123");
        assertThat(evento.getVersion()).isEqualTo(2L);
        assertThat(evento.getDataVencimento()).isEqualTo("2027-01-01");
    }

    @Test
    @DisplayName("Deve aceitar campos nulos sem validação")
    void deveAceitarCamposNulosSemValidacao() {
        // Act
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                null,
                null,
                null,
                null
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getNumeroApolice()).isNull();
        assertThat(evento.getSeguradoId()).isNull();
        assertThat(evento.getDataVencimento()).isNull();
        assertThat(evento.getValorSegurado()).isNull();
    }

    @Test
    @DisplayName("Deve aceitar campos vazios sem validação")
    void deveAceitarCamposVaziosSemValidacao() {
        // Act
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "   ",
                "   ",
                "   ",
                "   "
        );

        // Assert
        assertThat(evento).isNotNull();
        assertThat(evento.getNumeroApolice()).isEqualTo("   ");
        assertThat(evento.getSeguradoId()).isEqualTo("   ");
        assertThat(evento.getDataVencimento()).isEqualTo("   ");
        assertThat(evento.getValorSegurado()).isEqualTo("   ");
    }

    @Test
    @DisplayName("Deve manter valores exatamente como fornecidos")
    void deveManterValoresExatamenteComoFornecidos() {
        // Arrange
        String numeroApoliceComEspacos = "  AP-2026-001  ";
        String seguradoIdComEspacos = "  segurado-456  ";
        String dataVencimentoComEspacos = "  2027-01-01  ";
        String valorSeguradoComEspacos = "  50000.00  ";

        // Act
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                numeroApoliceComEspacos,
                seguradoIdComEspacos,
                dataVencimentoComEspacos,
                valorSeguradoComEspacos
        );

        // Assert - Não há trim, valores são mantidos como recebidos
        assertThat(evento.getNumeroApolice()).isEqualTo(numeroApoliceComEspacos);
        assertThat(evento.getSeguradoId()).isEqualTo(seguradoIdComEspacos);
        assertThat(evento.getDataVencimento()).isEqualTo(dataVencimentoComEspacos);
        assertThat(evento.getValorSegurado()).isEqualTo(valorSeguradoComEspacos);
    }

    @Test
    @DisplayName("Deve serializar e desserializar evento corretamente")
    void deveSerializarDesserializarEventoCorretamente() throws Exception {
        // Arrange
        ApoliceVencidaEvent eventoOriginal = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "50000.00"
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceVencidaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceVencidaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getAggregateId()).isEqualTo(eventoOriginal.getAggregateId());
        assertThat(eventoDesserializado.getVersion()).isEqualTo(eventoOriginal.getVersion());
        assertThat(eventoDesserializado.getNumeroApolice()).isEqualTo(eventoOriginal.getNumeroApolice());
        assertThat(eventoDesserializado.getSeguradoId()).isEqualTo(eventoOriginal.getSeguradoId());
        assertThat(eventoDesserializado.getDataVencimento()).isEqualTo(eventoOriginal.getDataVencimento());
        assertThat(eventoDesserializado.getValorSegurado()).isEqualTo(eventoOriginal.getValorSegurado());
        assertThat(eventoDesserializado.getEventType()).isEqualTo(eventoOriginal.getEventType());
    }

    @Test
    @DisplayName("Deve gerar toString com informações relevantes")
    void deveGerarToStringComInformacoesRelevantes() {
        // Arrange
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "50000.00"
        );

        // Act
        String toString = evento.toString();

        // Assert
        assertThat(toString)
                .contains("ApoliceVencidaEvent")
                .contains("apolice-123")
                .contains("AP-2026-001")
                .contains("2027-01-01");
    }

    @Test
    @DisplayName("Deve criar evento com version diferente")
    void deveCriarEventoComVersionDiferente() {
        // Arrange
        long version1 = 1L;
        long version2 = 5L;
        long version3 = 100L;

        // Act
        ApoliceVencidaEvent evento1 = new ApoliceVencidaEvent(
                "apolice-123", version1, "AP-2026-001", "segurado-456", "2027-01-01", "50000.00"
        );
        ApoliceVencidaEvent evento2 = new ApoliceVencidaEvent(
                "apolice-123", version2, "AP-2026-001", "segurado-456", "2027-01-01", "50000.00"
        );
        ApoliceVencidaEvent evento3 = new ApoliceVencidaEvent(
                "apolice-123", version3, "AP-2026-001", "segurado-456", "2027-01-01", "50000.00"
        );

        // Assert
        assertThat(evento1.getVersion()).isEqualTo(version1);
        assertThat(evento2.getVersion()).isEqualTo(version2);
        assertThat(evento3.getVersion()).isEqualTo(version3);
    }

    @Test
    @DisplayName("Deve criar evento com diferentes agregados")
    void deveCriarEventoComDiferentesAgregados() {
        // Arrange & Act
        ApoliceVencidaEvent evento1 = new ApoliceVencidaEvent(
                "apolice-111", 2L, "AP-2026-001", "segurado-456", "2027-01-01", "50000.00"
        );
        ApoliceVencidaEvent evento2 = new ApoliceVencidaEvent(
                "apolice-222", 2L, "AP-2026-002", "segurado-457", "2027-02-01", "60000.00"
        );
        ApoliceVencidaEvent evento3 = new ApoliceVencidaEvent(
                "apolice-333", 2L, "AP-2026-003", "segurado-458", "2027-03-01", "70000.00"
        );

        // Assert
        assertThat(evento1.getAggregateId()).isEqualTo("apolice-111");
        assertThat(evento2.getAggregateId()).isEqualTo("apolice-222");
        assertThat(evento3.getAggregateId()).isEqualTo("apolice-333");
    }

    @Test
    @DisplayName("Deve retornar tipo de agregado correto")
    void deveRetornarTipoAgregadoCorreto() {
        // Arrange
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "50000.00"
        );

        // Act & Assert
        assertThat(evento.getAggregateType()).isEqualTo("ApoliceAggregate");
    }

    @Test
    @DisplayName("Deve retornar tipo de evento correto")
    void deveRetornarTipoEventoCorreto() {
        // Arrange
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "50000.00"
        );

        // Act & Assert
        assertThat(evento.getEventType()).isEqualTo("ApoliceVencidaEvent");
    }

    @Test
    @DisplayName("Deve ter timestamp não nulo após criação")
    void deveTerTimestampNaoNuloAposCriacao() {
        // Arrange & Act
        ApoliceVencidaEvent evento = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                "AP-2026-001",
                "segurado-456",
                "2027-01-01",
                "50000.00"
        );

        // Assert
        assertThat(evento.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve criar múltiplos eventos com timestamps diferentes")
    void deveCriarMultiplosEventosComTimestampsDiferentes() throws InterruptedException {
        // Arrange & Act
        ApoliceVencidaEvent evento1 = new ApoliceVencidaEvent(
                "apolice-123", 2L, "AP-2026-001", "segurado-456", "2027-01-01", "50000.00"
        );

        Thread.sleep(10); // Pequena pausa para garantir timestamps diferentes

        ApoliceVencidaEvent evento2 = new ApoliceVencidaEvent(
                "apolice-124", 2L, "AP-2026-002", "segurado-457", "2027-02-01", "60000.00"
        );

        // Assert
        assertThat(evento1.getTimestamp()).isNotNull();
        assertThat(evento2.getTimestamp()).isNotNull();
        assertThat(evento1.getTimestamp()).isNotEqualTo(evento2.getTimestamp());
    }

    @Test
    @DisplayName("Deve serializar evento com campos nulos")
    void deveSerializarEventoComCamposNulos() throws Exception {
        // Arrange
        ApoliceVencidaEvent eventoOriginal = new ApoliceVencidaEvent(
                "apolice-123",
                2L,
                null,
                null,
                null,
                null
        );

        // Act
        String json = objectMapper.writeValueAsString(eventoOriginal);
        ApoliceVencidaEvent eventoDesserializado = objectMapper.readValue(json, ApoliceVencidaEvent.class);

        // Assert
        assertThat(eventoDesserializado).isNotNull();
        assertThat(eventoDesserializado.getNumeroApolice()).isNull();
        assertThat(eventoDesserializado.getSeguradoId()).isNull();
        assertThat(eventoDesserializado.getDataVencimento()).isNull();
        assertThat(eventoDesserializado.getValorSegurado()).isNull();
    }
}
