package com.seguradora.hibrida.domain.veiculo.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ApoliceValidationService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ApoliceValidationService - Testes Unitários")
class ApoliceValidationServiceTest {

    private final ApoliceValidationService validationService = new ApoliceValidationService();

    private static final String APOLICE_ID_VALIDO = "APO-001";
    private static final String VEICULO_ID_VALIDO = "VEI-001";

    @Nested
    @DisplayName("Testes de Validação - Sucesso")
    class ValidacaoSucessoTests {

        @Test
        @DisplayName("Deve validar com sucesso quando IDs são válidos")
        void deveValidarComSucessoQuandoIdsValidos() {
            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                APOLICE_ID_VALIDO, VEICULO_ID_VALIDO
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar IDs com diferentes formatos")
        void deveAceitarIdsDiferentesFormatos() {
            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "APO-12345", "VEI-98765"
            )).doesNotThrowAnyException();

            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "APOLICE_001", "VEICULO_001"
            )).doesNotThrowAnyException();

            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "123456", "789012"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar IDs com espaços que serão trimados")
        void deveAceitarIdsComEspacosQueSeraoTrimados() {
            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "  APO-001  ", "  VEI-001  "
            )).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Validação - Falha por ID de Apólice")
    class ValidacaoFalhaApoliceTests {

        @Test
        @DisplayName("Deve lançar exceção quando ID da apólice é nulo")
        void deveLancarExcecaoQuandoIdApoliceNulo() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                null, VEICULO_ID_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID da apólice é vazio")
        void deveLancarExcecaoQuandoIdApoliceVazio() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                "", VEICULO_ID_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID da apólice contém apenas espaços")
        void deveLancarExcecaoQuandoIdApoliceApenasEspacos() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                "   ", VEICULO_ID_VALIDO
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }
    }

    @Nested
    @DisplayName("Testes de Validação - Falha por ID de Veículo")
    class ValidacaoFalhaVeiculoTests {

        @Test
        @DisplayName("Deve lançar exceção quando ID do veículo é nulo")
        void deveLancarExcecaoQuandoIdVeiculoNulo() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                APOLICE_ID_VALIDO, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do veículo é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID do veículo é vazio")
        void deveLancarExcecaoQuandoIdVeiculoVazio() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                APOLICE_ID_VALIDO, ""
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do veículo é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID do veículo contém apenas espaços")
        void deveLancarExcecaoQuandoIdVeiculoApenasEspacos() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                APOLICE_ID_VALIDO, "   "
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID do veículo é obrigatório");
        }
    }

    @Nested
    @DisplayName("Testes de Validação - Ambos Inválidos")
    class ValidacaoAmbosInvalidosTests {

        @Test
        @DisplayName("Deve lançar exceção para apólice primeiro quando ambos são nulos")
        void deveLancarExcecaoParaApoliceQuandoAmbosNulos() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                null, null
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção para apólice primeiro quando ambos são vazios")
        void deveLancarExcecaoParaApoliceQuandoAmbosVazios() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                "", ""
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção para apólice primeiro quando ambos contêm apenas espaços")
        void deveLancarExcecaoParaApoliceQuandoAmbosApenasEspacos() {
            assertThatThrownBy(() -> validationService.validarApoliceParaAssociacao(
                "   ", "   "
            ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve aceitar IDs muito longos")
        void deveAceitarIdsMuitoLongos() {
            String apoliceIdLongo = "APO-" + "1".repeat(100);
            String veiculoIdLongo = "VEI-" + "2".repeat(100);

            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                apoliceIdLongo, veiculoIdLongo
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar IDs com caracteres especiais")
        void deveAceitarIdsComCaracteresEspeciais() {
            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "APO-001_ABC-123", "VEI-001_XYZ-789"
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar IDs em diferentes casos")
        void deveAceitarIdsEmDiferentesCasos() {
            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "apo-001", "vei-001"
            )).doesNotThrowAnyException();

            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "APO-001", "VEI-001"
            )).doesNotThrowAnyException();

            assertThatCode(() -> validationService.validarApoliceParaAssociacao(
                "ApO-001", "VeI-001"
            )).doesNotThrowAnyException();
        }
    }
}
