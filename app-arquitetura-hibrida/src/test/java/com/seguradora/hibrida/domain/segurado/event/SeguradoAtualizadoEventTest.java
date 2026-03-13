package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link SeguradoAtualizadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("SeguradoAtualizadoEvent - Testes Unitários")
class SeguradoAtualizadoEventTest {

    @Test
    @DisplayName("Deve criar evento com todos os dados")
    void shouldCreateEventWithAllData() {
        // Given
        String aggregateId = "SEG-001";
        String nome = "João Santos Silva";
        String email = "joao.santos@example.com";
        String telefone = "11998877665";
        LocalDate dataNascimento = LocalDate.now().minusYears(35);
        Endereco endereco = createEnderecoValido();

        // When
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
            aggregateId, nome, email, telefone, dataNascimento, endereco
        );

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getNome()).isEqualTo(nome);
        assertThat(event.getEmail()).isEqualTo(email);
        assertThat(event.getTelefone()).isEqualTo(telefone);
        assertThat(event.getDataNascimento()).isEqualTo(dataNascimento);
        assertThat(event.getEndereco()).isEqualTo(endereco);
        assertThat(event.getEventType()).isEqualTo("SeguradoAtualizado");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getNome()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        SeguradoAtualizadoEvent event = new SeguradoAtualizadoEvent(
            "SEG-001", "João", "joao@example.com", "11987654321",
            LocalDate.now().minusYears(30), createEnderecoValido()
        );

        // Then
        assertThat(event.getEventType()).isEqualTo("SeguradoAtualizado");
    }

    private Endereco createEnderecoValido() {
        return new Endereco(
            "Rua Teste", "100", "Apto 101", "Centro", "São Paulo", "SP", "01310100"
        );
    }
}
