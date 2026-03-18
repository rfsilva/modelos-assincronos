package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("OcorrenciaSinistro Tests")
class OcorrenciaSinistroTest {

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

    private OcorrenciaSinistro ocorrenciaValida() {
        return OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now().minusSeconds(3600))
                .localOcorrencia(localCompleto())
                .descricao("Colisão na esquina com danos no para-choque dianteiro")
                .boletimOcorrencia("BO-2024-001")
                .circunstancias("Chuva forte")
                .build();
    }

    @Test
    @DisplayName("isValida deve retornar true quando todos campos obrigatórios presentes")
    void isValidaShouldReturnTrueWhenAllRequired() {
        assertThat(ocorrenciaValida().isValida()).isTrue();
    }

    @Test
    @DisplayName("isValida deve retornar false quando dataOcorrencia nula")
    void isValidaShouldReturnFalseWhenDateNull() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .localOcorrencia(localCompleto())
                .descricao("Colisão na esquina com danos no para-choque dianteiro")
                .build();
        assertThat(ocorrencia.isValida()).isFalse();
    }

    @Test
    @DisplayName("isValida deve retornar false quando localOcorrencia nulo")
    void isValidaShouldReturnFalseWhenLocalNull() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now())
                .descricao("Colisão na esquina com danos no para-choque dianteiro")
                .build();
        assertThat(ocorrencia.isValida()).isFalse();
    }

    @Test
    @DisplayName("isValida deve retornar false quando descricao tem menos de 20 chars")
    void isValidaShouldReturnFalseWhenDescricaoTooShort() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now())
                .localOcorrencia(localCompleto())
                .descricao("Colisão curta")
                .build();
        assertThat(ocorrencia.isValida()).isFalse();
    }

    @Test
    @DisplayName("possuiBoletimOcorrencia deve retornar true quando BO preenchido")
    void possuiBoletimOcorrenciaShouldReturnTrueWhenFilled() {
        assertThat(ocorrenciaValida().possuiBoletimOcorrencia()).isTrue();
    }

    @Test
    @DisplayName("possuiBoletimOcorrencia deve retornar false quando BO nulo")
    void possuiBoletimOcorrenciaShouldReturnFalseWhenNull() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now())
                .localOcorrencia(localCompleto())
                .descricao("Descrição da ocorrência detalhada")
                .build();
        assertThat(ocorrencia.possuiBoletimOcorrencia()).isFalse();
    }

    @Test
    @DisplayName("isRecente deve retornar true quando ocorrência nas últimas 72h")
    void isRecenteShouldReturnTrueWithin72Hours() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now().minusSeconds(3600))
                .build();
        assertThat(ocorrencia.isRecente()).isTrue();
    }

    @Test
    @DisplayName("isRecente deve retornar false quando ocorrência há mais de 72h")
    void isRecenteShouldReturnFalseAfter72Hours() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now().minusSeconds(73 * 3600))
                .build();
        assertThat(ocorrencia.isRecente()).isFalse();
    }

    @Test
    @DisplayName("isRecente deve retornar false quando dataOcorrencia nula")
    void isRecenteShouldReturnFalseWhenNull() {
        OcorrenciaSinistro ocorrencia = OcorrenciaSinistro.builder().build();
        assertThat(ocorrencia.isRecente()).isFalse();
    }

    @Test
    @DisplayName("equals deve comparar dataOcorrencia, localOcorrencia e boletimOcorrencia")
    void equalsShouldCompareRelevantFields() {
        OcorrenciaSinistro o1 = ocorrenciaValida();
        OcorrenciaSinistro o2 = ocorrenciaValida();
        assertThat(o1).isEqualTo(o2);
    }

    @Test
    @DisplayName("toString deve conter cidade")
    void toStringShouldContainCity() {
        assertThat(ocorrenciaValida().toString()).contains("São Paulo");
    }
}
