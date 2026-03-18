package com.seguradora.hibrida.domain.sinistro.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ReprovarSinistroCommand Tests")
class ReprovarSinistroCommandTest {

    private String justificativa30Palavras() {
        return "O sinistro foi reprovado após análise técnica detalhada pois verificamos que o veículo " +
               "não possuía cobertura vigente para este tipo de ocorrência conforme cláusula contratual " +
               "aplicável ao caso em questão seguindo as normativas regulatórias vigentes";
    }

    private ReprovarSinistroCommand comando() {
        return ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .motivo("FORA_COBERTURA")
                .justificativaDetalhada(justificativa30Palavras())
                .analistaId("ANALISTA-01")
                .fundamentoLegal("Art. 5, Cláusula 3.2")
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        ReprovarSinistroCommand c = comando();
        assertThat(c.getSinistroId()).isEqualTo("SIN-001");
        assertThat(c.getMotivo()).isEqualTo("FORA_COBERTURA");
        assertThat(c.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(c.getFundamentoLegal()).isEqualTo("Art. 5, Cláusula 3.2");
    }

    @Test
    @DisplayName("getCommandType deve retornar ReprovarSinistroCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("ReprovarSinistroCommand");
    }

    @Test
    @DisplayName("isMotivoValido deve retornar true para motivo padronizado")
    void isMotivoValidoShouldReturnTrueForStandardMotivo() {
        assertThat(comando().isMotivoValido()).isTrue();
    }

    @Test
    @DisplayName("isMotivoValido deve retornar false para motivo não padronizado")
    void isMotivoValidoShouldReturnFalseForNonStandard() {
        ReprovarSinistroCommand c = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001").motivo("OUTRO_MOTIVO_CUSTOM")
                .justificativaDetalhada("just").analistaId("A").build();
        assertThat(c.isMotivoValido()).isFalse();
    }

    @Test
    @DisplayName("hasFundamentoLegal deve retornar true quando fundamento preenchido")
    void hasFundamentoLegalShouldReturnTrueWhenFilled() {
        assertThat(comando().hasFundamentoLegal()).isTrue();
    }

    @Test
    @DisplayName("hasFundamentoLegal deve retornar false quando fundamento nulo")
    void hasFundamentoLegalShouldReturnFalseWhenNull() {
        ReprovarSinistroCommand c = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001").motivo("FORA_COBERTURA")
                .justificativaDetalhada("just").analistaId("A").build();
        assertThat(c.hasFundamentoLegal()).isFalse();
    }

    @Test
    @DisplayName("isJustificativaAdequada deve retornar true quando >= 30 palavras")
    void isJustificativaAdequadaShouldReturnTrueWhenEnoughWords() {
        assertThat(comando().isJustificativaAdequada()).isTrue();
    }

    @Test
    @DisplayName("isJustificativaAdequada deve retornar false quando < 30 palavras")
    void isJustificativaAdequadaShouldReturnFalseWhenFewWords() {
        ReprovarSinistroCommand c = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001").motivo("FORA_COBERTURA")
                .justificativaDetalhada("Justificativa curta demais.").analistaId("A").build();
        assertThat(c.isJustificativaAdequada()).isFalse();
    }

    @Test
    @DisplayName("isReprovacaoPorFraude deve retornar true quando motivo contém PROPOSITAL")
    void isReprovacaoPorFraudeShouldReturnTrueForProposital() {
        ReprovarSinistroCommand c = ReprovarSinistroCommand.builder()
                .sinistroId("SIN-001").motivo("SINISTRO_PROPOSITAL")
                .justificativaDetalhada("just").analistaId("A").build();
        assertThat(c.isReprovacaoPorFraude()).isTrue();
    }

    @Test
    @DisplayName("isReprovacaoPorFraude deve retornar false para motivo normal")
    void isReprovacaoPorFraudeShouldReturnFalseForNormalMotivo() {
        assertThat(comando().isReprovacaoPorFraude()).isFalse();
    }

    @Test
    @DisplayName("toString deve conter sinistroId e motivo")
    void toStringShouldContainSinistroAndMotivo() {
        assertThat(comando().toString()).contains("SIN-001").contains("FORA_COBERTURA");
    }
}
