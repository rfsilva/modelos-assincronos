package com.seguradora.hibrida.eventstore.model;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link DomainEvent}.
 */
@DisplayName("DomainEvent Tests")
class DomainEventTest {

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção")
    class Construcao {

        @Test
        @DisplayName("Deve inicializar campos essenciais via construtor protegido")
        void shouldInitializeEssentialFieldsViaProtectedConstructor() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getAggregateId()).isEqualTo("agg-1");
            assertThat(event.getAggregateType()).isEqualTo("TestAggregate");
            assertThat(event.getVersion()).isEqualTo(1L);
            assertThat(event.getTimestamp()).isNotNull();
            assertThat(event.getCorrelationId()).isNotNull();
        }

        @Test
        @DisplayName("Deve inicializar metadata como mapa vazio")
        void shouldInitializeMetadataAsEmptyMap() {
            TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
            assertThat(event.getMetadata()).isNotNull();
        }
    }

    // =========================================================================
    // getEventType
    // =========================================================================

    @Test
    @DisplayName("getEventType deve retornar o nome simples da classe")
    void getEventTypeShouldReturnSimpleClassName() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
        assertThat(event.getEventType()).isEqualTo("TestEvent");
    }

    // =========================================================================
    // setCorrelationId / setUserId
    // =========================================================================

    @Test
    @DisplayName("setCorrelationId deve definir o correlation ID")
    void setCorrelationIdShouldUpdateField() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
        UUID newId = UUID.randomUUID();
        event.setCorrelationId(newId);
        assertThat(event.getCorrelationId()).isEqualTo(newId);
    }

    @Test
    @DisplayName("setUserId deve definir o user ID")
    void setUserIdShouldUpdateField() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
        event.setUserId("user-42");
        assertThat(event.getUserId()).isEqualTo("user-42");
    }

    // =========================================================================
    // addMetadata
    // =========================================================================

    @Test
    @DisplayName("addMetadata deve adicionar entrada ao mapa de metadados")
    void addMetadataShouldAddEntryToMetadataMap() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
        event.addMetadata("chave", "valor");
        assertThat(event.getMetadata().get("chave")).isEqualTo("valor");
    }

    @Test
    @DisplayName("getMetadata deve inicializar metadata se nulo")
    void getMetadataShouldInitializeIfNull() {
        TestEvent event = new TestEvent("agg-1", "msg", "cat", 1);
        assertThat(event.getMetadata()).isNotNull();
    }
}
