package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ContatoRemovidoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("ContatoRemovidoEvent - Testes Unitários")
class ContatoRemovidoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os dados")
    void shouldCreateEventWithAllData() {
        // Given
        String aggregateId = "SEG-001";
        TipoContato tipo = TipoContato.CELULAR;
        String valor = "11987654321";

        // When
        ContatoRemovidoEvent event = new ContatoRemovidoEvent(aggregateId, tipo, valor);

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getTipo()).isEqualTo(tipo);
        assertThat(event.getValor()).isEqualTo(valor);
        assertThat(event.getEventType()).isEqualTo("ContatoRemovido");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        ContatoRemovidoEvent event = new ContatoRemovidoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getTipo()).isNull();
        assertThat(event.getValor()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        ContatoRemovidoEvent event = new ContatoRemovidoEvent(
            "SEG-001", TipoContato.EMAIL, "joao@example.com"
        );

        // Then
        assertThat(event.getEventType()).isEqualTo("ContatoRemovido");
    }

    @Test
    @DisplayName("Deve suportar todos os tipos de contato")
    void shouldSupportAllContactTypes() {
        assertThatNoException().isThrownBy(() ->
            new ContatoRemovidoEvent("SEG-001", TipoContato.EMAIL, "email@test.com")
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoRemovidoEvent("SEG-001", TipoContato.TELEFONE, "1134567890")
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoRemovidoEvent("SEG-001", TipoContato.CELULAR, "11987654321")
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoRemovidoEvent("SEG-001", TipoContato.WHATSAPP, "11987654321")
        );
    }
}
