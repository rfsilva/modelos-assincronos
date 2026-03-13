package com.seguradora.hibrida.domain.segurado.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link BureauValidationResult}.
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@DisplayName("BureauValidationResult - Testes Unitários")
class BureauValidationResultTest {

    @Test
    @DisplayName("Deve criar resultado de sucesso com score")
    void shouldCreateSuccessResultWithScore() {
        // Given
        Integer score = 750;

        // When
        BureauValidationResult result = BureauValidationResult.sucesso(score);

        // Then
        assertThat(result.isValido()).isTrue();
        assertThat(result.getScore()).isEqualTo(750);
        assertThat(result.getMotivo()).isNull();
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.temRestricoes()).isFalse();
    }

    @Test
    @DisplayName("Deve criar resultado de falha com motivo")
    void shouldCreateFailureResultWithReason() {
        // Given
        String motivo = "CPF com restrições cadastrais";

        // When
        BureauValidationResult result = BureauValidationResult.falha(motivo);

        // Then
        assertThat(result.isValido()).isFalse();
        assertThat(result.getMotivo()).isEqualTo(motivo);
        assertThat(result.getScore()).isNull();
        assertThat(result.getTimestamp()).isNotNull();
        assertThat(result.temRestricoes()).isTrue();
    }

    @Test
    @DisplayName("Deve criar resultado de erro")
    void shouldCreateErrorResult() {
        // Given
        String erro = "Timeout na consulta";

        // When
        BureauValidationResult result = BureauValidationResult.erro(erro);

        // Then
        assertThat(result.isValido()).isFalse();
        assertThat(result.getMotivo()).contains("Erro na consulta");
        assertThat(result.getMotivo()).contains(erro);
        assertThat(result.temRestricoes()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar score alto corretamente")
    void shouldIdentifyHighScore() {
        // Given
        BureauValidationResult result700 = BureauValidationResult.sucesso(700);
        BureauValidationResult result800 = BureauValidationResult.sucesso(800);
        BureauValidationResult result1000 = BureauValidationResult.sucesso(1000);

        // Then
        assertThat(result700.isScoreAlto()).isTrue();
        assertThat(result800.isScoreAlto()).isTrue();
        assertThat(result1000.isScoreAlto()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar score médio corretamente")
    void shouldIdentifyMediumScore() {
        // Given
        BureauValidationResult result400 = BureauValidationResult.sucesso(400);
        BureauValidationResult result550 = BureauValidationResult.sucesso(550);
        BureauValidationResult result699 = BureauValidationResult.sucesso(699);

        // Then
        assertThat(result400.isScoreMedio()).isTrue();
        assertThat(result550.isScoreMedio()).isTrue();
        assertThat(result699.isScoreMedio()).isTrue();
    }

    @Test
    @DisplayName("Deve identificar score baixo corretamente")
    void shouldIdentifyLowScore() {
        // Given
        BureauValidationResult result0 = BureauValidationResult.sucesso(0);
        BureauValidationResult result200 = BureauValidationResult.sucesso(200);
        BureauValidationResult result399 = BureauValidationResult.sucesso(399);

        // Then
        assertThat(result0.isScoreBaixo()).isTrue();
        assertThat(result200.isScoreBaixo()).isTrue();
        assertThat(result399.isScoreBaixo()).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false para score checks quando score é null")
    void shouldReturnFalseForScoreChecksWhenScoreIsNull() {
        // Given
        BureauValidationResult result = BureauValidationResult.falha("Sem score disponível");

        // Then
        assertThat(result.isScoreAlto()).isFalse();
        assertThat(result.isScoreMedio()).isFalse();
        assertThat(result.isScoreBaixo()).isFalse();
    }

    @Test
    @DisplayName("Deve permitir dados adicionais")
    void shouldAllowAdditionalData() {
        // Given
        Map<String, Object> dadosAdicionais = new HashMap<>();
        dadosAdicionais.put("bureau", "Serasa");
        dadosAdicionais.put("consultaId", "12345");

        // When
        BureauValidationResult result = new BureauValidationResult(
            true, null, 750, null, dadosAdicionais
        );

        // Then
        assertThat(result.getDadosAdicionais()).isNotNull();
        assertThat(result.getDadosAdicionais()).containsEntry("bureau", "Serasa");
        assertThat(result.getDadosAdicionais()).containsEntry("consultaId", "12345");
    }

    @Test
    @DisplayName("Deve criar com construtor completo")
    void shouldCreateWithFullConstructor() {
        // Given
        boolean valido = true;
        String motivo = null;
        Integer score = 850;
        Map<String, Object> dados = new HashMap<>();

        // When
        BureauValidationResult result = new BureauValidationResult(
            valido, motivo, score, null, dados
        );

        // Then
        assertThat(result.isValido()).isTrue();
        assertThat(result.getScore()).isEqualTo(850);
    }

    @Test
    @DisplayName("Deve verificar limites dos scores")
    void shouldCheckScoreBoundaries() {
        // Score alto: >= 700
        assertThat(BureauValidationResult.sucesso(699).isScoreAlto()).isFalse();
        assertThat(BureauValidationResult.sucesso(700).isScoreAlto()).isTrue();

        // Score médio: 400-699
        assertThat(BureauValidationResult.sucesso(399).isScoreMedio()).isFalse();
        assertThat(BureauValidationResult.sucesso(400).isScoreMedio()).isTrue();
        assertThat(BureauValidationResult.sucesso(699).isScoreMedio()).isTrue();
        assertThat(BureauValidationResult.sucesso(700).isScoreMedio()).isFalse();

        // Score baixo: < 400
        assertThat(BureauValidationResult.sucesso(399).isScoreBaixo()).isTrue();
        assertThat(BureauValidationResult.sucesso(400).isScoreBaixo()).isFalse();
    }
}
