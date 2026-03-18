package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.domain.sinistro.model.ValorIndenizacao;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AprovarSinistroCommand Tests")
class AprovarSinistroCommandTest {

    private ValorIndenizacao valorValido() {
        return ValorIndenizacao.builder()
                .valorBruto(new BigDecimal("10000.00"))
                .franquia(new BigDecimal("1000.00"))
                .build();
    }

    private AprovarSinistroCommand comando() {
        return AprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .valorIndenizacao(valorValido())
                .justificativa("O sinistro foi aprovado após análise técnica completa com todos os documentos verificados e vistoria realizada no local do acidente com laudo pericial anexado")
                .analistaId("ANALISTA-01")
                .documentosComprobatorios(List.of("DOC-001", "DOC-002"))
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        AprovarSinistroCommand c = comando();
        assertThat(c.getSinistroId()).isEqualTo("SIN-001");
        assertThat(c.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(c.getValorIndenizacao()).isNotNull();
        assertThat(c.getDocumentosComprobatorios()).hasSize(2);
    }

    @Test
    @DisplayName("getCommandType deve retornar AprovarSinistroCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("AprovarSinistroCommand");
    }

    @Test
    @DisplayName("hasDocumentosComprobatoriosSuficientes deve retornar true quando >= 2")
    void hasDocumentosComprobatoriosShouldReturnTrueWhen2OrMore() {
        assertThat(comando().hasDocumentosComprobatoriosSuficientes()).isTrue();
    }

    @Test
    @DisplayName("hasDocumentosComprobatoriosSuficientes deve retornar false quando < 2")
    void hasDocumentosComprobatoriosShouldReturnFalseWhenLess() {
        AprovarSinistroCommand c = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001").valorIndenizacao(valorValido())
                .justificativa("Just").analistaId("ANALISTA-01")
                .documentosComprobatorios(List.of("DOC-001"))
                .build();
        assertThat(c.hasDocumentosComprobatoriosSuficientes()).isFalse();
    }

    @Test
    @DisplayName("isValorIndenizacaoValido deve retornar true para valor válido")
    void isValorIndenizacaoValidoShouldReturnTrueWhenValid() {
        assertThat(comando().isValorIndenizacaoValido()).isTrue();
    }

    @Test
    @DisplayName("isValorIndenizacaoValido deve retornar false quando null")
    void isValorIndenizacaoValidoShouldReturnFalseWhenNull() {
        AprovarSinistroCommand c = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001").analistaId("ANALISTA-01")
                .justificativa("Just")
                .build();
        assertThat(c.isValorIndenizacaoValido()).isFalse();
    }

    @Test
    @DisplayName("isJustificativaAdequada deve retornar true quando >= 20 palavras")
    void isJustificativaAdequadaShouldReturnTrueWhenEnoughWords() {
        assertThat(comando().isJustificativaAdequada()).isTrue();
    }

    @Test
    @DisplayName("isJustificativaAdequada deve retornar false quando < 20 palavras")
    void isJustificativaAdequadaShouldReturnFalseWhenFewWords() {
        AprovarSinistroCommand c = AprovarSinistroCommand.builder()
                .sinistroId("SIN-001").valorIndenizacao(valorValido())
                .justificativa("Curta justificativa.").analistaId("ANALISTA-01")
                .build();
        assertThat(c.isJustificativaAdequada()).isFalse();
    }

    @Test
    @DisplayName("toString deve conter sinistroId e analistaId")
    void toStringShouldContainSinistroAndAnalista() {
        assertThat(comando().toString()).contains("SIN-001").contains("ANALISTA-01");
    }
}
