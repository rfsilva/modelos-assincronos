package com.seguradora.hibrida.command.config;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandHandlerRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link CommandBusConfiguration}.
 */
@DisplayName("CommandBusConfiguration Tests")
class CommandBusConfigurationTest {

    private CommandBusConfiguration configuration;

    @BeforeEach
    void setUp() {
        configuration = new CommandBusConfiguration();
    }

    // =========================================================================
    // Meta-anotações
    // =========================================================================

    @Nested
    @DisplayName("Meta-anotações da classe")
    class MetaAnotacoes {

        @Test
        @DisplayName("Deve ter anotação @Configuration")
        void shouldHaveConfigurationAnnotation() {
            assertThat(CommandBusConfiguration.class.isAnnotationPresent(Configuration.class))
                    .isTrue();
        }
    }

    // =========================================================================
    // Bean: commandBus
    // =========================================================================

    @Nested
    @DisplayName("Bean: commandBus")
    class BeanCommandBus {

        @Test
        @DisplayName("Deve criar bean CommandBus")
        void shouldCreateCommandBusBean() {
            // Given
            CommandHandlerRegistry registry = new CommandHandlerRegistry();

            // When
            CommandBus bus = configuration.commandBus(registry);

            // Then
            assertThat(bus).isNotNull();
        }

        @Test
        @DisplayName("CommandBus criado deve aceitar registerHandler sem erro")
        void createdCommandBusShouldAcceptRegisterHandlerWithoutError() {
            // Given
            CommandHandlerRegistry registry = new CommandHandlerRegistry();
            CommandBus bus = configuration.commandBus(registry);

            // When / Then – registrar handler não deve lançar exceção
            assertThat(bus.hasHandler(
                    com.seguradora.hibrida.command.example.TestCommand.class)).isFalse();
        }
    }

    // =========================================================================
    // Bean: commandHandlerRegistry
    // =========================================================================

    @Nested
    @DisplayName("Bean: commandHandlerRegistry")
    class BeanCommandHandlerRegistry {

        @Test
        @DisplayName("Deve criar bean CommandHandlerRegistry")
        void shouldCreateCommandHandlerRegistryBean() {
            CommandHandlerRegistry registry = configuration.commandHandlerRegistry();
            assertThat(registry).isNotNull();
        }

        @Test
        @DisplayName("Deve criar instâncias independentes a cada chamada")
        void shouldCreateIndependentInstances() {
            CommandHandlerRegistry r1 = configuration.commandHandlerRegistry();
            CommandHandlerRegistry r2 = configuration.commandHandlerRegistry();
            assertThat(r1).isNotSameAs(r2);
        }
    }

    // =========================================================================
    // Bean: commandBusProperties
    // =========================================================================

    @Nested
    @DisplayName("Bean: commandBusProperties")
    class BeanCommandBusProperties {

        @Test
        @DisplayName("Deve criar bean CommandBusProperties")
        void shouldCreateCommandBusPropertiesBean() {
            CommandBusProperties props = configuration.commandBusProperties();
            assertThat(props).isNotNull();
        }
    }

    // =========================================================================
    // Bean: commandBusMetrics
    // =========================================================================

    @Nested
    @DisplayName("Bean: commandBusMetrics")
    class BeanCommandBusMetrics {

        @Test
        @DisplayName("Deve criar bean CommandBusMetrics")
        void shouldCreateCommandBusMetricsBean() {
            CommandBusMetrics metrics = configuration.commandBusMetrics(new SimpleMeterRegistry());
            assertThat(metrics).isNotNull();
        }
    }

    // =========================================================================
    // Bean: commandBusHealthIndicator
    // =========================================================================

    @Nested
    @DisplayName("Bean: commandBusHealthIndicator")
    class BeanCommandBusHealthIndicator {

        @Test
        @DisplayName("Deve criar bean CommandBusHealthIndicator")
        void shouldCreateCommandBusHealthIndicatorBean() {
            // Given
            CommandHandlerRegistry registry = new CommandHandlerRegistry();
            CommandBus bus = configuration.commandBus(registry);

            // When
            CommandBusHealthIndicator indicator = configuration.commandBusHealthIndicator(bus);

            // Then
            assertThat(indicator).isNotNull();
        }
    }
}
