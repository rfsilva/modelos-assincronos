package com.seguradora.hibrida.eventstore.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários para {@link EventMetadata}.
 */
@DisplayName("EventMetadata Tests")
class EventMetadataTest {

    // =========================================================================
    // Construção
    // =========================================================================

    @Nested
    @DisplayName("Construção")
    class Construcao {

        @Test
        @DisplayName("Construtor padrão deve criar mapa vazio")
        void defaultConstructorShouldCreateEmptyMap() {
            EventMetadata metadata = new EventMetadata();
            assertThat(metadata).isEmpty();
        }

        @Test
        @DisplayName("Construtor com mapa deve copiar entradas")
        void constructorWithMapShouldCopyEntries() {
            EventMetadata metadata = new EventMetadata(Map.of("k1", "v1", "k2", 42));
            assertThat(metadata.get("k1")).isEqualTo("v1");
            assertThat(metadata.get("k2")).isEqualTo(42);
        }
    }

    // =========================================================================
    // with() fluente
    // =========================================================================

    @Test
    @DisplayName("with() deve adicionar entrada e retornar this")
    void withShouldAddEntryAndReturnSelf() {
        EventMetadata metadata = new EventMetadata();
        EventMetadata result = metadata.with("key", "value");

        assertThat(result).isSameAs(metadata);
        assertThat(metadata.get("key")).isEqualTo("value");
    }

    @Test
    @DisplayName("with() deve suportar encadeamento")
    void withShouldSupportChaining() {
        EventMetadata metadata = new EventMetadata()
                .with("a", 1)
                .with("b", "two");

        assertThat(metadata.get("a")).isEqualTo(1);
        assertThat(metadata.get("b")).isEqualTo("two");
    }

    // =========================================================================
    // getValue(key, Class)
    // =========================================================================

    @Nested
    @DisplayName("getValue(key, Class)")
    class GetValueByClass {

        @Test
        @DisplayName("Deve retornar valor tipado quando o tipo coincide")
        void shouldReturnTypedValueWhenTypeMatches() {
            EventMetadata metadata = new EventMetadata(Map.of("count", 100));
            Integer value = metadata.getValue("count", Integer.class);
            assertThat(value).isEqualTo(100);
        }

        @Test
        @DisplayName("Deve retornar null quando o tipo não coincide")
        void shouldReturnNullWhenTypeDoesNotMatch() {
            EventMetadata metadata = new EventMetadata(Map.of("count", 100));
            String value = metadata.getValue("count", String.class);
            assertThat(value).isNull();
        }

        @Test
        @DisplayName("Deve retornar null para chave inexistente")
        void shouldReturnNullForMissingKey() {
            EventMetadata metadata = new EventMetadata();
            assertThat(metadata.getValue("missing", String.class)).isNull();
        }
    }

    // =========================================================================
    // getValue(key, defaultValue)
    // =========================================================================

    @Nested
    @DisplayName("getValue(key, defaultValue)")
    class GetValueWithDefault {

        @Test
        @DisplayName("Deve retornar o valor quando a chave existe")
        void shouldReturnValueWhenKeyExists() {
            EventMetadata metadata = new EventMetadata(Map.of("name", "alice"));
            String value = metadata.getValue("name", "default");
            assertThat(value).isEqualTo("alice");
        }

        @Test
        @DisplayName("Deve retornar defaultValue quando a chave não existe")
        void shouldReturnDefaultValueWhenKeyMissing() {
            EventMetadata metadata = new EventMetadata();
            String value = metadata.getValue("missing", "default");
            assertThat(value).isEqualTo("default");
        }
    }
}
