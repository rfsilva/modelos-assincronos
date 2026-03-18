package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@DisplayName("ProtocoloSinistro Tests")
class ProtocoloSinistroTest {

    @Test
    @DisplayName("of deve criar protocolo com formato válido")
    void ofShouldCreateWithValidFormat() {
        ProtocoloSinistro p = ProtocoloSinistro.of("2024-000001");
        assertThat(p.getValor()).isEqualTo("2024-000001");
    }

    @Test
    @DisplayName("of deve lançar exceção para null ou vazio")
    void ofShouldThrowForNullOrBlank() {
        assertThatThrownBy(() -> ProtocoloSinistro.of(null))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.of(""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.of("   "))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("of deve lançar exceção para formato inválido")
    void ofShouldThrowForInvalidFormat() {
        assertThatThrownBy(() -> ProtocoloSinistro.of("2024-12"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.of("ABCD-000001"))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.of("2024-0000001"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("gerar deve criar protocolo com ano e sequencial")
    void gerarShouldCreateWithYearAndSequential() {
        ProtocoloSinistro p = ProtocoloSinistro.gerar(2024, 1L);
        assertThat(p.getValor()).isEqualTo("2024-000001");
        assertThat(p.getAno()).isEqualTo(2024);
        assertThat(p.getSequencial()).isEqualTo(1L);
    }

    @Test
    @DisplayName("gerar deve lançar exceção para ano fora do intervalo")
    void gerarShouldThrowForInvalidYear() {
        assertThatThrownBy(() -> ProtocoloSinistro.gerar(1999, 1L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.gerar(10000, 1L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("gerar deve lançar exceção para sequencial inválido")
    void gerarShouldThrowForInvalidSequential() {
        assertThatThrownBy(() -> ProtocoloSinistro.gerar(2024, 0L))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> ProtocoloSinistro.gerar(2024, 1000000L))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("gerar com sequencial deve usar ano atual")
    void gerarWithSequentialShouldUseCurrentYear() {
        ProtocoloSinistro p = ProtocoloSinistro.gerar(42L);
        assertThat(p.getValor()).matches("\\d{4}-000042");
    }

    @Test
    @DisplayName("equals deve comparar pelo valor")
    void equalsShouldCompareByValue() {
        ProtocoloSinistro p1 = ProtocoloSinistro.of("2024-000001");
        ProtocoloSinistro p2 = ProtocoloSinistro.of("2024-000001");
        assertThat(p1).isEqualTo(p2);
        assertThat(p1.hashCode()).isEqualTo(p2.hashCode());
    }

    @Test
    @DisplayName("toString deve retornar o valor")
    void toStringShouldReturnValue() {
        ProtocoloSinistro p = ProtocoloSinistro.of("2024-000001");
        assertThat(p.toString()).isEqualTo("2024-000001");
    }
}
