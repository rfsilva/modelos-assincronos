package com.seguradora.hibrida.eventbus;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes de contrato para a interface {@link EventBus}.
 */
@DisplayName("EventBus Interface Tests")
class EventBusTest {

    // =========================================================================
    // Estrutura da interface
    // =========================================================================

    @Test
    @DisplayName("EventBus deve ser interface")
    void eventBusShouldBeInterface() {
        assertThat(EventBus.class.isInterface()).isTrue();
    }

    @Test
    @DisplayName("EventBus deve declarar método publish")
    void eventBusShouldDeclarePublishMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("publish",
                com.seguradora.hibrida.eventstore.model.DomainEvent.class))
                .isNotNull();
    }

    @Test
    @DisplayName("EventBus deve declarar método publishAsync")
    void eventBusShouldDeclarePublishAsyncMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("publishAsync",
                com.seguradora.hibrida.eventstore.model.DomainEvent.class))
                .isNotNull();
    }

    @Test
    @DisplayName("EventBus deve declarar método publishBatch")
    void eventBusShouldDeclarePublishBatchMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("publishBatch", java.util.List.class))
                .isNotNull();
    }

    @Test
    @DisplayName("EventBus deve declarar método getStatistics")
    void eventBusShouldDeclareGetStatisticsMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("getStatistics")).isNotNull();
    }

    @Test
    @DisplayName("EventBus deve declarar método isHealthy")
    void eventBusShouldDeclareIsHealthyMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("isHealthy")).isNotNull();
    }

    @Test
    @DisplayName("EventBus deve declarar método shutdown com timeout")
    void eventBusShouldDeclareShutdownMethod() throws NoSuchMethodException {
        assertThat(EventBus.class.getMethod("shutdown", int.class)).isNotNull();
    }
}
