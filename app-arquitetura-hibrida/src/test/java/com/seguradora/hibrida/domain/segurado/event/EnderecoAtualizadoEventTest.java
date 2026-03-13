package com.seguradora.hibrida.domain.segurado.event;

import com.seguradora.hibrida.domain.segurado.model.Endereco;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link EnderecoAtualizadoEvent}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("EnderecoAtualizadoEvent - Testes Unitários")
class EnderecoAtualizadoEventTest {

    @Test
    @DisplayName("Deve criar evento com endereços antigo e novo")
    void shouldCreateEventWithOldAndNewAddress() {
        // Given
        String aggregateId = "SEG-001";
        Endereco enderecoAnterior = new Endereco(
            "Rua Antiga", "100", "Apto 101", "Centro", "São Paulo", "SP", "01310100"
        );
        Endereco novoEndereco = new Endereco(
            "Avenida Nova", "500", "Apto 202", "Jardins", "São Paulo", "SP", "01310200"
        );

        // When
        EnderecoAtualizadoEvent event = new EnderecoAtualizadoEvent(
            aggregateId, enderecoAnterior, novoEndereco
        );

        // Then
        assertThat(event.getAggregateId()).isEqualTo(aggregateId);
        assertThat(event.getEnderecoAnterior()).isEqualTo(enderecoAnterior);
        assertThat(event.getNovoEndereco()).isEqualTo(novoEndereco);
        assertThat(event.getEventType()).isEqualTo("EnderecoAtualizado");
    }

    @Test
    @DisplayName("Deve criar evento com construtor vazio")
    void shouldCreateEventWithNoArgsConstructor() {
        // When
        EnderecoAtualizadoEvent event = new EnderecoAtualizadoEvent();

        // Then
        assertThat(event).isNotNull();
        assertThat(event.getEnderecoAnterior()).isNull();
        assertThat(event.getNovoEndereco()).isNull();
    }

    @Test
    @DisplayName("Deve ter eventType correto")
    void shouldHaveCorrectEventType() {
        // Given
        Endereco endereco1 = createEnderecoValido();
        Endereco endereco2 = createEnderecoValido();
        EnderecoAtualizadoEvent event = new EnderecoAtualizadoEvent("SEG-001", endereco1, endereco2);

        // Then
        assertThat(event.getEventType()).isEqualTo("EnderecoAtualizado");
    }

    @Test
    @DisplayName("Deve permitir endereços diferentes")
    void shouldAllowDifferentAddresses() {
        // Given
        Endereco endereco1 = new Endereco(
            "Rua A", "100", null, "Bairro A", "Cidade A", "SP", "01310100"
        );
        Endereco endereco2 = new Endereco(
            "Rua B", "200", "Casa", "Bairro B", "Cidade B", "RJ", "20000000"
        );

        // When
        EnderecoAtualizadoEvent event = new EnderecoAtualizadoEvent("SEG-001", endereco1, endereco2);

        // Then
        assertThat(event.getEnderecoAnterior()).isNotEqualTo(event.getNovoEndereco());
    }

    private Endereco createEnderecoValido() {
        return new Endereco(
            "Rua Teste", "100", "Apto 101", "Centro", "São Paulo", "SP", "01310100"
        );
    }
}
