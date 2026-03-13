package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoCriadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoCriadoEvent - Testes Unitários")
class SeguradoCriadoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os dados")
    void shouldCreateEventWithAllData() {
        // Given
        String aggregateId = "SEG-001";
        String cpf = "12345678909";
        String nome = "João da Silva";
        String email = "joao@example.com";
        String telefone = "11987654321";
        LocalDate dataNascimento = LocalDate.now().minusYears(30);
        Endereco endereco = createEnderecoValido();

        // When
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
            aggregateId, cpf, nome, email, telefone, dataNascimento, endereco
        );

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getCpf()).isEqualTo(cpf);
        assertThat(event.getNome()).isEqualTo(nome);
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getTelefone()).isEqualTo(telefone);
        assertThat(event.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(event.getEndereco()).isEqualTo(endereco);
        assertThat(event.getAggregateType()).isEqualTo("SeguradoAggregate");
        assertThat(event.getEventType()).isEqualTo("SeguradoCriado");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        SeguradoCriadoEvent event = new SeguradoCriadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getCpf()).isNull();
        assertThat(event.getNome()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
            "SEG-001", "12345678909", "João", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), createEnderecoValido()
        );

        // Then
        assertThat(event.getEventType()).isEqualTo("SeguradoCriado");
    }

    @Test
    @DisplayName("Deve ter timestamp preenchido")
    void shouldHaveTimestampFilled() {
        // When
        SeguradoCriadoEvent event = new SeguradoCriadoEvent(
            "SEG-001", "12345678909", "João", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), createEnderecoValido()
        );

        // Then
        assertThat(event.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("Deve ter eventId único")
    void shouldHaveUniqueEventId() {
        // When
        SeguradoCriadoEvent event1 = new SeguradoCriadoEvent(
            "SEG-001", "12345678909", "João", "joao@example.com",
            "11987654321", LocalDate.now().minusYears(30), createEnderecoValido()
        );
        SeguradoCriadoEvent event2 = new SeguradoCriadoEvent(
            "SEG-002", "98765432100", "Maria", "maria@example.com",
            "11987654321", LocalDate.now().minusYears(25), createEnderecoValido()
        );

        // Then
        assertThat(event1.getEventId()).isNotNull();
        assertThat(event2.getEventId()).isNotNull();
        assertThat(event1.getEventId()).isNotEqualTo(event2.getEventId());
    }

    private Endereco createEnderecoValido() {
        return new Endereco(
            "Rua Teste", "100", "Apto 101", "Centro", "São Paulo", "SP", "01310100"
        );
    }
}
