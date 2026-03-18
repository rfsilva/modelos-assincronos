package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LocalOcorrencia Tests")
class LocalOcorrenciaTest {

    private LocalOcorrencia localCompleto() {
        return LocalOcorrencia.builder()
                .logradouro("Rua das Flores")
                .numero("123")
                .bairro("Centro")
                .cidade("São Paulo")
                .estado("SP")
                .cep("01310100")
                .build();
    }

    @Test
    @DisplayName("isCompleto deve retornar true quando logradouro, cidade e estado preenchidos")
    void isCompletoShouldReturnTrueWhenRequired() {
        assertThat(localCompleto().isCompleto()).isTrue();
    }

    @Test
    @DisplayName("isCompleto deve retornar false quando falta campo obrigatório")
    void isCompletoShouldReturnFalseWhenMissingRequired() {
        LocalOcorrencia semCidade = LocalOcorrencia.builder()
                .logradouro("Rua A")
                .estado("SP")
                .build();
        assertThat(semCidade.isCompleto()).isFalse();
    }

    @Test
    @DisplayName("getEnderecoCompleto deve formatar endereço")
    void getEnderecoCompletoShouldFormatAddress() {
        String endereco = localCompleto().getEnderecoCompleto();
        assertThat(endereco)
                .contains("Rua das Flores")
                .contains("123")
                .contains("São Paulo")
                .contains("SP");
    }

    @Test
    @DisplayName("getEnderecoCompleto deve formatar CEP com hífen")
    void getEnderecoCompletoShouldFormatCepWithHyphen() {
        String endereco = localCompleto().getEnderecoCompleto();
        assertThat(endereco).contains("01310-100");
    }

    @Test
    @DisplayName("possuiCoordenadas deve retornar true quando lat e long estão preenchidas")
    void possuiCoordenadasShouldReturnTrueWhenBothSet() {
        LocalOcorrencia local = LocalOcorrencia.builder()
                .logradouro("Rua A").cidade("SP").estado("SP")
                .latitude(new BigDecimal("-23.5505"))
                .longitude(new BigDecimal("-46.6333"))
                .build();
        assertThat(local.possuiCoordenadas()).isTrue();
    }

    @Test
    @DisplayName("possuiCoordenadas deve retornar false quando lat ou long ausente")
    void possuiCoordenadasShouldReturnFalseWhenMissing() {
        LocalOcorrencia local = LocalOcorrencia.builder()
                .logradouro("Rua A").cidade("SP").estado("SP")
                .build();
        assertThat(local.possuiCoordenadas()).isFalse();
    }

    @Test
    @DisplayName("equals deve comparar campos de endereço")
    void equalsShouldCompareAddressFields() {
        LocalOcorrencia l1 = localCompleto();
        LocalOcorrencia l2 = localCompleto();
        assertThat(l1).isEqualTo(l2);
    }

    @Test
    @DisplayName("toString deve retornar endereço completo")
    void toStringShouldReturnFullAddress() {
        assertThat(localCompleto().toString()).contains("São Paulo");
    }
}
