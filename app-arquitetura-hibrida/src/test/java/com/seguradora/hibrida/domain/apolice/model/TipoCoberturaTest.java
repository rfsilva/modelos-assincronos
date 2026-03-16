package com.seguradora.hibrida.domain.apolice.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para o enum TipoCobertura.
 */
@DisplayName("TipoCobertura - Testes Unitários")
class TipoCoberturaTest {

    @Test
    @DisplayName("Deve ter todos os tipos de cobertura disponíveis")
    void deveTerTodosTiposCoberturaDisponiveis() {
        // Act & Assert
        assertThat(TipoCobertura.values()).containsExactly(
                TipoCobertura.TOTAL,
                TipoCobertura.PARCIAL,
                TipoCobertura.TERCEIROS,
                TipoCobertura.ROUBO_FURTO,
                TipoCobertura.COLISAO,
                TipoCobertura.INCENDIO,
                TipoCobertura.FENOMENOS_NATURAIS
        );
    }

    @Test
    @DisplayName("Deve retornar descrição correta para cada tipo")
    void deveRetornarDescricaoCorreta() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.getDescricao()).isEqualTo("Total");
        assertThat(TipoCobertura.PARCIAL.getDescricao()).isEqualTo("Parcial");
        assertThat(TipoCobertura.TERCEIROS.getDescricao()).isEqualTo("Terceiros");
        assertThat(TipoCobertura.ROUBO_FURTO.getDescricao()).isEqualTo("Roubo e Furto");
        assertThat(TipoCobertura.COLISAO.getDescricao()).isEqualTo("Colisão");
        assertThat(TipoCobertura.INCENDIO.getDescricao()).isEqualTo("Incêndio");
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.getDescricao()).isEqualTo("Fenômenos Naturais");
    }

    @Test
    @DisplayName("Deve retornar detalhamento correto para cada tipo")
    void deveRetornarDetalhamentoCorreto() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.getDetalhamento())
                .isEqualTo("Cobertura completa para todos os tipos de sinistros");
        assertThat(TipoCobertura.PARCIAL.getDetalhamento())
                .isEqualTo("Cobertura limitada para tipos específicos de sinistros");
        assertThat(TipoCobertura.TERCEIROS.getDetalhamento())
                .isEqualTo("Cobertura apenas para danos causados a terceiros");
        assertThat(TipoCobertura.ROUBO_FURTO.getDetalhamento())
                .isEqualTo("Cobertura específica para roubo e furto do veículo");
        assertThat(TipoCobertura.COLISAO.getDetalhamento())
                .isEqualTo("Cobertura para danos por colisão");
        assertThat(TipoCobertura.INCENDIO.getDetalhamento())
                .isEqualTo("Cobertura para danos por incêndio");
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.getDetalhamento())
                .isEqualTo("Cobertura para danos por fenômenos naturais");
    }

    @Test
    @DisplayName("Deve retornar fator de prêmio correto para cada tipo")
    void deveRetornarFatorPremioCorreto() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.getFatorPremio()).isEqualByComparingTo(new BigDecimal("1.0"));
        assertThat(TipoCobertura.PARCIAL.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.7"));
        assertThat(TipoCobertura.TERCEIROS.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.3"));
        assertThat(TipoCobertura.ROUBO_FURTO.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.5"));
        assertThat(TipoCobertura.COLISAO.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.6"));
        assertThat(TipoCobertura.INCENDIO.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.4"));
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.getFatorPremio()).isEqualByComparingTo(new BigDecimal("0.3"));
    }

    @Test
    @DisplayName("Deve identificar corretamente coberturas que incluem roubo/furto")
    void deveIdentificarCoberturasRouboFurto() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreRouboFurto()).isTrue();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreRouboFurto()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreRouboFurto()).isFalse();
        assertThat(TipoCobertura.TERCEIROS.cobreRouboFurto()).isFalse();
        assertThat(TipoCobertura.COLISAO.cobreRouboFurto()).isFalse();
        assertThat(TipoCobertura.INCENDIO.cobreRouboFurto()).isFalse();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreRouboFurto()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente coberturas que incluem colisão")
    void deveIdentificarCoberturasColisao() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreColisao()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreColisao()).isTrue();
        assertThat(TipoCobertura.COLISAO.cobreColisao()).isTrue();
        assertThat(TipoCobertura.TERCEIROS.cobreColisao()).isFalse();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreColisao()).isFalse();
        assertThat(TipoCobertura.INCENDIO.cobreColisao()).isFalse();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreColisao()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente coberturas que incluem terceiros")
    void deveIdentificarCoberturasTerceiros() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.TERCEIROS.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreTerceiros()).isFalse();
        assertThat(TipoCobertura.COLISAO.cobreTerceiros()).isFalse();
        assertThat(TipoCobertura.INCENDIO.cobreTerceiros()).isFalse();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreTerceiros()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente coberturas que incluem incêndio")
    void deveIdentificarCoberturasIncendio() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.INCENDIO.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.TERCEIROS.cobreIncendio()).isFalse();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreIncendio()).isFalse();
        assertThat(TipoCobertura.COLISAO.cobreIncendio()).isFalse();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreIncendio()).isFalse();
    }

    @Test
    @DisplayName("Deve identificar corretamente coberturas que incluem fenômenos naturais")
    void deveIdentificarCoberturasFenomenosNaturais() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreFenomenosNaturais()).isTrue();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreFenomenosNaturais()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreFenomenosNaturais()).isFalse();
        assertThat(TipoCobertura.TERCEIROS.cobreFenomenosNaturais()).isFalse();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreFenomenosNaturais()).isFalse();
        assertThat(TipoCobertura.COLISAO.cobreFenomenosNaturais()).isFalse();
        assertThat(TipoCobertura.INCENDIO.cobreFenomenosNaturais()).isFalse();
    }

    @Test
    @DisplayName("Cobertura TOTAL deve cobrir todos os tipos")
    void coberturaTotalDeveCobririTodos() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.cobreRouboFurto()).isTrue();
        assertThat(TipoCobertura.TOTAL.cobreColisao()).isTrue();
        assertThat(TipoCobertura.TOTAL.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.TOTAL.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.TOTAL.cobreFenomenosNaturais()).isTrue();
    }

    @Test
    @DisplayName("Cobertura PARCIAL deve cobrir tipos intermediários")
    void coberturaParcialDeveCobririTiposIntermediarios() {
        // Act & Assert
        assertThat(TipoCobertura.PARCIAL.cobreColisao()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.PARCIAL.cobreRouboFurto()).isFalse();
        assertThat(TipoCobertura.PARCIAL.cobreFenomenosNaturais()).isFalse();
    }

    @Test
    @DisplayName("Coberturas específicas devem cobrir apenas seu tipo")
    void coberturasEspecificasDevemCobrirApenasSeuTipo() {
        // Roubo/Furto
        assertThat(TipoCobertura.ROUBO_FURTO.cobreRouboFurto()).isTrue();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreColisao()).isFalse();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreTerceiros()).isFalse();
        assertThat(TipoCobertura.ROUBO_FURTO.cobreIncendio()).isFalse();

        // Colisão
        assertThat(TipoCobertura.COLISAO.cobreColisao()).isTrue();
        assertThat(TipoCobertura.COLISAO.cobreRouboFurto()).isFalse();

        // Terceiros
        assertThat(TipoCobertura.TERCEIROS.cobreTerceiros()).isTrue();
        assertThat(TipoCobertura.TERCEIROS.cobreColisao()).isFalse();

        // Incêndio
        assertThat(TipoCobertura.INCENDIO.cobreIncendio()).isTrue();
        assertThat(TipoCobertura.INCENDIO.cobreColisao()).isFalse();

        // Fenômenos Naturais
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreFenomenosNaturais()).isTrue();
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.cobreColisao()).isFalse();
    }

    @Test
    @DisplayName("Deve implementar toString corretamente")
    void deveImplementarToStringCorretamente() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.toString()).isEqualTo("Total");
        assertThat(TipoCobertura.PARCIAL.toString()).isEqualTo("Parcial");
        assertThat(TipoCobertura.TERCEIROS.toString()).isEqualTo("Terceiros");
        assertThat(TipoCobertura.ROUBO_FURTO.toString()).isEqualTo("Roubo e Furto");
        assertThat(TipoCobertura.COLISAO.toString()).isEqualTo("Colisão");
        assertThat(TipoCobertura.INCENDIO.toString()).isEqualTo("Incêndio");
        assertThat(TipoCobertura.FENOMENOS_NATURAIS.toString()).isEqualTo("Fenômenos Naturais");
    }

    @Test
    @DisplayName("Deve permitir conversão de string para enum")
    void devePermitirConversaoDeStringParaEnum() {
        // Act & Assert
        assertThat(TipoCobertura.valueOf("TOTAL")).isEqualTo(TipoCobertura.TOTAL);
        assertThat(TipoCobertura.valueOf("PARCIAL")).isEqualTo(TipoCobertura.PARCIAL);
        assertThat(TipoCobertura.valueOf("TERCEIROS")).isEqualTo(TipoCobertura.TERCEIROS);
        assertThat(TipoCobertura.valueOf("ROUBO_FURTO")).isEqualTo(TipoCobertura.ROUBO_FURTO);
        assertThat(TipoCobertura.valueOf("COLISAO")).isEqualTo(TipoCobertura.COLISAO);
        assertThat(TipoCobertura.valueOf("INCENDIO")).isEqualTo(TipoCobertura.INCENDIO);
        assertThat(TipoCobertura.valueOf("FENOMENOS_NATURAIS")).isEqualTo(TipoCobertura.FENOMENOS_NATURAIS);
    }

    @Test
    @DisplayName("Deve lançar exceção ao converter string inválida")
    void deveLancarExcecaoAoConverterStringInvalida() {
        // Act & Assert
        assertThatThrownBy(() -> TipoCobertura.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Deve verificar comparação entre tipos")
    void deveVerificarComparacaoEntreTipos() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL).isEqualTo(TipoCobertura.TOTAL);
        assertThat(TipoCobertura.TOTAL).isNotEqualTo(TipoCobertura.PARCIAL);
    }

    @Test
    @DisplayName("Deve ter hashCode consistente")
    void deveTerHashCodeConsistente() {
        // Act & Assert
        assertThat(TipoCobertura.TOTAL.hashCode()).isEqualTo(TipoCobertura.TOTAL.hashCode());
    }

    @Test
    @DisplayName("Deve validar hierarquia de coberturas por fator prêmio")
    void deveValidarHierarquiaCoberturasporFatorPremio() {
        // Total deve ter o maior fator
        assertThat(TipoCobertura.TOTAL.getFatorPremio())
                .isGreaterThan(TipoCobertura.PARCIAL.getFatorPremio());

        assertThat(TipoCobertura.PARCIAL.getFatorPremio())
                .isGreaterThan(TipoCobertura.TERCEIROS.getFatorPremio());

        // Fatores devem ser positivos
        for (TipoCobertura tipo : TipoCobertura.values()) {
            assertThat(tipo.getFatorPremio()).isGreaterThan(BigDecimal.ZERO);
        }
    }

    @Test
    @DisplayName("Deve validar lógica de inclusão de coberturas")
    void deveValidarLogicaInclusaoCoberturas() {
        // Total inclui todos
        // Parcial inclui colisão, terceiros e incêndio
        // Específicas incluem apenas seu tipo

        // Verificar que TOTAL é mais abrangente que PARCIAL
        int coberturasTotalCount = 0;
        int coberturasParcialCount = 0;

        if (TipoCobertura.TOTAL.cobreRouboFurto()) coberturasTotalCount++;
        if (TipoCobertura.TOTAL.cobreColisao()) coberturasTotalCount++;
        if (TipoCobertura.TOTAL.cobreTerceiros()) coberturasTotalCount++;
        if (TipoCobertura.TOTAL.cobreIncendio()) coberturasTotalCount++;
        if (TipoCobertura.TOTAL.cobreFenomenosNaturais()) coberturasTotalCount++;

        if (TipoCobertura.PARCIAL.cobreRouboFurto()) coberturasParcialCount++;
        if (TipoCobertura.PARCIAL.cobreColisao()) coberturasParcialCount++;
        if (TipoCobertura.PARCIAL.cobreTerceiros()) coberturasParcialCount++;
        if (TipoCobertura.PARCIAL.cobreIncendio()) coberturasParcialCount++;
        if (TipoCobertura.PARCIAL.cobreFenomenosNaturais()) coberturasParcialCount++;

        assertThat(coberturasTotalCount).isGreaterThan(coberturasParcialCount);
    }
}
