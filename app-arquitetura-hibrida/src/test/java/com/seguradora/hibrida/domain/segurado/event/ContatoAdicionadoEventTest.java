package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ContatoAdicionadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("ContatoAdicionadoEvent - Testes Unitários")
class ContatoAdicionadoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os dados")
    void shouldCreateEventWithAllData() {
        // Given
        String aggregateId = "SEG-001";
        TipoContato tipo = TipoContato.CELULAR;
        String valor = "11987654321";
        boolean principal = true;

        // When
        ContatoAdicionadoEvent event = new ContatoAdicionadoEvent(aggregateId, tipo, valor, principal);

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getTipo()).isEqualTo(tipo);
        assertThat(event.getValor()).isEqualTo(valor);
        assertThat(event.isPrincipal()).isTrue();
        assertThat(event.getEventType()).isEqualTo("ContatoAdicionado");
    }

    @Test
    @DisplayName("Deve criar evento de contato não principal")
    void shouldCreateNonPrincipalContactEvent() {
        // When
        ContatoAdicionadoEvent event = new ContatoAdicionadoEvent(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", false
        );

        // Then
        assertThat(event.isPrincipal()).isFalse();
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        ContatoAdicionadoEvent event = new ContatoAdicionadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getTipo()).isNull();
        assertThat(event.getValor()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        ContatoAdicionadoEvent event = new ContatoAdicionadoEvent(
            "SEG-001", TipoContato.EMAIL, "joao@example.com", true
        );

        // Then
        assertThat(event.getEventType()).isEqualTo("ContatoAdicionado");
    }

    @Test
    @DisplayName("Deve suportar todos os tipos de contato")
    void shouldSupportAllContactTypes() {
        assertThatNoException().isThrownBy(() ->
            new ContatoAdicionadoEvent("SEG-001", TipoContato.EMAIL, "email@test.com", true)
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoAdicionadoEvent("SEG-001", TipoContato.TELEFONE, "1134567890", true)
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoAdicionadoEvent("SEG-001", TipoContato.CELULAR, "11987654321", true)
        );
        assertThatNoException().isThrownBy(() ->
            new ContatoAdicionadoEvent("SEG-001", TipoContato.WHATSAPP, "11987654321", true)
        );
    }
}
