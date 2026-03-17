package com.seguradora.hibrida.eventbus;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link EventHandlerRegistry}.
 */
@DisplayName("EventHandlerRegistry Tests")
class EventHandlerRegistryTest {

    private EventHandlerRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new EventHandlerRegistry();
    }

    // =========================================================================
    // Anotações de classe
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(EventHandlerRegistry.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // Estado inicial
    // =========================================================================

    @Test
    @DisplayName("Registry deve iniciar vazio")
    void shouldStartEmpty() {
        assertThat(registry.getTotalHandlers()).isZero();
        assertThat(registry.getEventTypesCount()).isZero();
        assertThat(registry.getRegisteredEventTypes()).isEmpty();
        assertThat(registry.getLastRegistrationTime()).isZero();
    }

    // =========================================================================
    // registerHandler
    // =========================================================================

    @Nested
    @DisplayName("registerHandler()")
    class RegisterHandler {

        @Test
        @DisplayName("Deve registrar handler e incrementar contador")
        void shouldRegisterHandlerAndIncrementCount() {
            EventHandler<TestEvent> handler = buildTestHandler("H1", 0);

            registry.registerHandler(TestEvent.class, handler);

            assertThat(registry.getTotalHandlers()).isEqualTo(1);
            assertThat(registry.hasHandlers(TestEvent.class)).isTrue();
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para eventType null")
        void shouldThrowForNullEventType() {
            assertThatThrownBy(() -> registry.registerHandler(null, buildTestHandler("H", 0)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar IllegalArgumentException para handler null")
        void shouldThrowForNullHandler() {
            assertThatThrownBy(() -> registry.registerHandler(TestEvent.class, null))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve retornar handlers ordenados por prioridade decrescente")
        void shouldReturnHandlersOrderedByPriorityDesc() {
            EventHandler<TestEvent> h1 = buildTestHandler("H1", 5);
            EventHandler<TestEvent> h2 = buildTestHandler("H2", 10);
            EventHandler<TestEvent> h3 = buildTestHandler("H3", 1);

            registry.registerHandler(TestEvent.class, h1);
            registry.registerHandler(TestEvent.class, h2);
            registry.registerHandler(TestEvent.class, h3);

            List<EventHandler<TestEvent>> handlers = registry.getHandlers(TestEvent.class);
            assertThat(handlers).hasSize(3);
            assertThat(handlers.get(0).getPriority()).isGreaterThanOrEqualTo(handlers.get(1).getPriority());
        }

        @Test
        @DisplayName("Deve atualizar lastRegistrationTime ao registrar")
        void shouldUpdateLastRegistrationTime() {
            assertThat(registry.getLastRegistrationTime()).isZero();

            registry.registerHandler(TestEvent.class, buildTestHandler("H1", 0));

            assertThat(registry.getLastRegistrationTime()).isPositive();
        }
    }

    // =========================================================================
    // unregisterHandler
    // =========================================================================

    @Nested
    @DisplayName("unregisterHandler()")
    class UnregisterHandler {

        @Test
        @DisplayName("Deve retornar true ao remover handler existente")
        void shouldReturnTrueWhenRemovingExistingHandler() {
            EventHandler<TestEvent> handler = buildTestHandler("H1", 0);
            registry.registerHandler(TestEvent.class, handler);

            boolean result = registry.unregisterHandler(TestEvent.class, handler);

            assertThat(result).isTrue();
            assertThat(registry.getTotalHandlers()).isZero();
        }

        @Test
        @DisplayName("Deve retornar false ao tentar remover handler inexistente")
        void shouldReturnFalseWhenRemovingNonExistentHandler() {
            EventHandler<TestEvent> handler = buildTestHandler("H1", 0);

            boolean result = registry.unregisterHandler(TestEvent.class, handler);

            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false para eventType null")
        void shouldReturnFalseForNullEventType() {
            assertThat(registry.unregisterHandler(null, buildTestHandler("H", 0))).isFalse();
        }
    }

    // =========================================================================
    // hasHandlers / getHandlers
    // =========================================================================

    @Nested
    @DisplayName("hasHandlers() e getHandlers()")
    class HasAndGetHandlers {

        @Test
        @DisplayName("hasHandlers deve retornar false para tipo sem handlers")
        void hasHandlersShouldReturnFalseForTypeWithNoHandlers() {
            assertThat(registry.hasHandlers(TestEvent.class)).isFalse();
        }

        @Test
        @DisplayName("getHandlers deve retornar lista vazia para tipo sem handlers")
        void getHandlersShouldReturnEmptyListForTypeWithNoHandlers() {
            assertThat(registry.getHandlers(TestEvent.class)).isEmpty();
        }

        @Test
        @DisplayName("getHandlers deve retornar lista vazia para eventType null")
        void getHandlersShouldReturnEmptyForNullEventType() {
            assertThat(registry.getHandlers(null)).isEmpty();
        }
    }

    // =========================================================================
    // getStatistics / validateConfiguration
    // =========================================================================

    @Nested
    @DisplayName("getStatistics() e validateConfiguration()")
    class StatisticsAndValidation {

        @Test
        @DisplayName("getStatistics deve retornar mapa com campos esperados")
        void getStatisticsShouldReturnMapWithExpectedFields() {
            registry.registerHandler(TestEvent.class, buildTestHandler("H1", 0));

            var stats = registry.getStatistics();
            assertThat(stats).containsKeys("totalHandlers", "eventTypesCount", "lastRegistrationTime");
        }

        @Test
        @DisplayName("validateConfiguration deve reportar problema quando não há handlers")
        void validateConfigurationShouldReportIssueWhenNoHandlers() {
            assertThat(registry.validateConfiguration()).isNotEmpty();
        }

        @Test
        @DisplayName("validateConfiguration deve retornar lista vazia quando há handlers registrados")
        void validateConfigurationShouldReturnEmptyListWhenHandlersRegistered() {
            registry.registerHandler(TestEvent.class, buildTestHandler("H1", 0));

            assertThat(registry.validateConfiguration()).isEmpty();
        }
    }

    // =========================================================================
    // clear
    // =========================================================================

    @Test
    @DisplayName("clear() deve remover todos os handlers")
    void clearShouldRemoveAllHandlers() {
        registry.registerHandler(TestEvent.class, buildTestHandler("H1", 0));
        registry.registerHandler(TestEvent.class, buildTestHandler("H2", 1));

        registry.clear();

        assertThat(registry.getTotalHandlers()).isZero();
        assertThat(registry.hasHandlers(TestEvent.class)).isFalse();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private EventHandler<TestEvent> buildTestHandler(String name, int priority) {
        return new EventHandler<TestEvent>() {
            @Override
            public void handle(TestEvent event) {}

            @Override
            public Class<TestEvent> getEventType() {
                return TestEvent.class;
            }

            @Override
            public int getPriority() {
                return priority;
            }

            @Override
            public String toString() {
                return name;
            }
        };
    }
}
