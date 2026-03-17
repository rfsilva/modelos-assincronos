package com.seguradora.hibrida.projection;

import com.seguradora.hibrida.eventbus.example.TestEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para {@link ProjectionRegistry}.
 */
@DisplayName("ProjectionRegistry Tests")
class ProjectionRegistryTest {

    private ProjectionRegistry registry;

    @BeforeEach
    void setUp() {
        registry = new ProjectionRegistry();
    }

    // =========================================================================
    // Anotações
    // =========================================================================

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(ProjectionRegistry.class.isAnnotationPresent(Component.class)).isTrue();
    }

    // =========================================================================
    // Estado inicial
    // =========================================================================

    @Test
    @DisplayName("Registry deve iniciar vazio")
    void shouldStartEmpty() {
        assertThat(registry.getRegisteredEventTypes()).isEmpty();
        assertThat(registry.getRegisteredProjectionNames()).isEmpty();
        assertThat(registry.getStatistics().get("totalProjections")).isEqualTo(0);
    }

    // =========================================================================
    // registerHandler
    // =========================================================================

    @Nested
    @DisplayName("registerHandler()")
    class RegisterHandler {

        @Test
        @DisplayName("Deve registrar handler com sucesso")
        void shouldRegisterHandlerSuccessfully() {
            ProjectionHandler<TestEvent> handler = buildHandler("TestProjection", 0);
            registry.registerHandler(handler);

            assertThat(registry.hasHandlers(TestEvent.class)).isTrue();
            assertThat(registry.getRegisteredProjectionNames()).contains("TestProjection");
        }

        @Test
        @DisplayName("Deve lançar exceção ao registrar handler com nome duplicado")
        void shouldThrowWhenRegisteringDuplicateProjectionName() {
            registry.registerHandler(buildHandler("TestProjection", 0));

            assertThatThrownBy(() -> registry.registerHandler(buildHandler("TestProjection", 1)))
                    .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve ordenar handlers por ordem crescente")
        void shouldSortHandlersByOrderAscending() {
            registry.registerHandler(buildHandler("Proj1", 10));
            registry.registerHandler(buildHandler("Proj2", 5));
            registry.registerHandler(buildHandler("Proj3", 20));

            List<ProjectionHandler<? extends DomainEvent>> handlers =
                    registry.getHandlers(TestEvent.class);

            assertThat(handlers).hasSize(3);
            assertThat(handlers.get(0).getOrder()).isLessThanOrEqualTo(handlers.get(1).getOrder());
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
            registry.registerHandler(buildHandler("TestProjection", 0));

            boolean result = registry.unregisterHandler("TestProjection");

            assertThat(result).isTrue();
            assertThat(registry.hasHandlers(TestEvent.class)).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false ao tentar remover handler inexistente")
        void shouldReturnFalseWhenRemovingNonExistentHandler() {
            boolean result = registry.unregisterHandler("NonExistent");
            assertThat(result).isFalse();
        }
    }

    // =========================================================================
    // getHandler / getHandlers
    // =========================================================================

    @Nested
    @DisplayName("getHandler() e getHandlers()")
    class GetHandlerTests {

        @Test
        @DisplayName("getHandler deve retornar handler por nome")
        void getHandlerShouldReturnHandlerByName() {
            ProjectionHandler<TestEvent> handler = buildHandler("TestProjection", 0);
            registry.registerHandler(handler);

            assertThat(registry.getHandler("TestProjection")).isEqualTo(handler);
        }

        @Test
        @DisplayName("getHandler deve retornar null para nome inexistente")
        void getHandlerShouldReturnNullForNonExistentName() {
            assertThat(registry.getHandler("NonExistent")).isNull();
        }

        @Test
        @DisplayName("getHandlers deve retornar lista vazia para tipo sem handlers")
        void getHandlersShouldReturnEmptyListForTypeWithNoHandlers() {
            assertThat(registry.getHandlers(TestEvent.class)).isEmpty();
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
            registry.registerHandler(buildHandler("TestProjection", 0));

            assertThat(registry.getStatistics()).containsKeys(
                    "totalEventTypes", "totalProjections", "handlersByEventType");
        }

        @Test
        @DisplayName("validateConfiguration deve retornar lista vazia quando tudo OK")
        void validateConfigurationShouldReturnEmptyListWhenOk() {
            registry.registerHandler(buildHandler("TestProjection", 0));
            assertThat(registry.validateConfiguration()).isEmpty();
        }
    }

    // =========================================================================
    // clear
    // =========================================================================

    @Test
    @DisplayName("clear() deve remover todos os handlers")
    void clearShouldRemoveAllHandlers() {
        registry.registerHandler(buildHandler("Proj1", 0));
        registry.registerHandler(buildHandler("Proj2", 1));

        registry.clear();

        assertThat(registry.getRegisteredProjectionNames()).isEmpty();
        assertThat(registry.hasHandlers(TestEvent.class)).isFalse();
    }

    // =========================================================================
    // Helper
    // =========================================================================

    private ProjectionHandler<TestEvent> buildHandler(String name, int order) {
        return new ProjectionHandler<TestEvent>() {
            @Override
            public void handle(TestEvent event) {}

            @Override
            public Class<TestEvent> getEventType() {
                return TestEvent.class;
            }

            @Override
            public String getProjectionName() {
                return name;
            }

            @Override
            public int getOrder() {
                return order;
            }
        };
    }
}
