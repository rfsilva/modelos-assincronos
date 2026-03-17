package com.seguradora.hibrida.aggregate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para a anotação {@link EventSourcingHandler}.
 */
@DisplayName("EventSourcingHandler Tests")
class EventSourcingHandlerTest {

    // =========================================================================
    // Presença e retenção da anotação
    // =========================================================================

    @Nested
    @DisplayName("Meta-anotações")
    class MetaAnotacoes {

        @Test
        @DisplayName("Deve ter @Retention RUNTIME")
        void shouldHaveRuntimeRetention() {
            // Given
            Retention retention = EventSourcingHandler.class.getAnnotation(Retention.class);

            // Then
            assertThat(retention).isNotNull();
            assertThat(retention.value()).isEqualTo(RetentionPolicy.RUNTIME);
        }

        @Test
        @DisplayName("Deve ter @Target apenas METHOD")
        void shouldTargetOnlyMethod() {
            // Given
            Target target = EventSourcingHandler.class.getAnnotation(Target.class);

            // Then
            assertThat(target).isNotNull();
            assertThat(target.value()).containsExactly(ElementType.METHOD);
        }
    }

    // =========================================================================
    // Atributos da anotação
    // =========================================================================

    @Nested
    @DisplayName("Atributos e valores padrão")
    class AtributosEValoresPadrao {

        @Test
        @DisplayName("Valor padrão de 'value' deve ser string vazia")
        void defaultValueShouldBeEmptyString() throws Exception {
            // Given
            Method valueMethod = EventSourcingHandler.class.getMethod("value");

            // Then
            assertThat(valueMethod.getDefaultValue()).isEqualTo("");
        }

        @Test
        @DisplayName("Valor padrão de 'replayable' deve ser true")
        void defaultReplayableShouldBeTrue() throws Exception {
            // Given
            Method replayableMethod = EventSourcingHandler.class.getMethod("replayable");

            // Then
            assertThat(replayableMethod.getDefaultValue()).isEqualTo(true);
        }

        @Test
        @DisplayName("Valor padrão de 'order' deve ser zero")
        void defaultOrderShouldBeZero() throws Exception {
            // Given
            Method orderMethod = EventSourcingHandler.class.getMethod("order");

            // Then
            assertThat(orderMethod.getDefaultValue()).isEqualTo(0);
        }
    }

    // =========================================================================
    // Uso em métodos concretos
    // =========================================================================

    @Nested
    @DisplayName("Uso em métodos de aggregate")
    class UsoEmMetodosDeAggregate {

        @Test
        @DisplayName("Deve ser detectável via reflection em método anotado")
        void shouldBeDetectableViaReflectionOnAnnotatedMethod()
                throws NoSuchMethodException {
            // Given
            Class<?> aggregateClass = com.seguradora.hibrida.aggregate.example.ExampleAggregate.class;
            Method handlerMethod = getDeclaredMethodInHierarchy(
                    aggregateClass, "on",
                    com.seguradora.hibrida.aggregate.example.ExampleCreatedEvent.class);

            // Then
            assertThat(handlerMethod).isNotNull();
            assertThat(handlerMethod.isAnnotationPresent(EventSourcingHandler.class)).isTrue();
        }

        @Test
        @DisplayName("Deve ser detectável nos quatro handlers do ExampleAggregate")
        void shouldBeDetectableInAllFourExampleHandlers() {
            // Given
            Class<?> aggregateClass = com.seguradora.hibrida.aggregate.example.ExampleAggregate.class;

            Class<?>[] eventTypes = {
                com.seguradora.hibrida.aggregate.example.ExampleCreatedEvent.class,
                com.seguradora.hibrida.aggregate.example.ExampleUpdatedEvent.class,
                com.seguradora.hibrida.aggregate.example.ExampleActivatedEvent.class,
                com.seguradora.hibrida.aggregate.example.ExampleDeactivatedEvent.class
            };

            // Then
            for (Class<?> eventType : eventTypes) {
                Method handler = getDeclaredMethodInHierarchy(aggregateClass, "on",
                        (Class<? extends com.seguradora.hibrida.eventstore.model.DomainEvent>) eventType);
                assertThat(handler)
                        .as("Handler para %s", eventType.getSimpleName())
                        .isNotNull();
                assertThat(handler.isAnnotationPresent(EventSourcingHandler.class))
                        .as("@EventSourcingHandler em handler de %s", eventType.getSimpleName())
                        .isTrue();
            }
        }

        @Test
        @DisplayName("Atributo 'value' deve permitir nome descritivo")
        void valueShouldAllowDescriptiveName() throws Exception {
            // Given – classe interna com handler nomeado
            class DummyAggregate {
                @EventSourcingHandler("meuHandler")
                void handle() {}
            }

            Method method = DummyAggregate.class.getDeclaredMethod("handle");
            EventSourcingHandler annotation = method.getAnnotation(EventSourcingHandler.class);

            // Then
            assertThat(annotation.value()).isEqualTo("meuHandler");
        }

        @Test
        @DisplayName("Atributo 'replayable' deve poder ser sobrescrito para false")
        void replayableShouldBeOverridableToFalse() throws Exception {
            // Given
            class DummyAggregate {
                @EventSourcingHandler(replayable = false)
                void handle() {}
            }

            Method method = DummyAggregate.class.getDeclaredMethod("handle");
            EventSourcingHandler annotation = method.getAnnotation(EventSourcingHandler.class);

            // Then
            assertThat(annotation.replayable()).isFalse();
        }

        @Test
        @DisplayName("Atributo 'order' deve poder ser sobrescrito para valor positivo")
        void orderShouldBeOverridableToPositiveValue() throws Exception {
            // Given
            class DummyAggregate {
                @EventSourcingHandler(order = 5)
                void handle() {}
            }

            Method method = DummyAggregate.class.getDeclaredMethod("handle");
            EventSourcingHandler annotation = method.getAnnotation(EventSourcingHandler.class);

            // Then
            assertThat(annotation.order()).isEqualTo(5);
        }
    }

    // =========================================================================
    // Helpers
    // =========================================================================

    @SuppressWarnings("unchecked")
    private <E extends com.seguradora.hibrida.eventstore.model.DomainEvent> Method getDeclaredMethodInHierarchy(
            Class<?> clazz, String methodName, Class<E> paramType) {
        Class<?> current = clazz;
        while (current != null && current != Object.class) {
            for (Method m : current.getDeclaredMethods()) {
                if (m.getName().equals(methodName)
                        && m.getParameterCount() == 1
                        && m.getParameterTypes()[0].isAssignableFrom(paramType)) {
                    return m;
                }
            }
            current = current.getSuperclass();
        }
        return null;
    }
}
