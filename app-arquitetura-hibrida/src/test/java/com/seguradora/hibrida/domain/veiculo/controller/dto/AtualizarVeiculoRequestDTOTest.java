package com.seguradora.hibrida.domain.veiculo.controller.dto;

import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link AtualizarVeiculoRequestDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("AtualizarVeiculoRequestDTO - Testes Unitários")
class AtualizarVeiculoRequestDTOTest {

    @Nested
    @DisplayName("Testes de Criação")
    class CriacaoTests {

        @Test
        @DisplayName("Deve criar DTO com todos os campos")
        void deveCriarDtoComTodosCampos() {
            // Arrange
            Especificacao especificacao = Especificacao.exemplo();

            // Act
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                especificacao,
                "OP-001",
                3L,
                "Alteração de cor"
            );

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.especificacao()).isEqualTo(especificacao);
            assertThat(dto.operadorId()).isEqualTo("OP-001");
            assertThat(dto.versaoEsperada()).isEqualTo(3L);
            assertThat(dto.motivo()).isEqualTo("Alteração de cor");
        }

        @Test
        @DisplayName("Deve criar DTO sem versão esperada")
        void deveCriarDtoSemVersaoEsperada() {
            // Arrange
            Especificacao especificacao = Especificacao.exemplo();

            // Act
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                especificacao,
                "OP-001",
                null,
                "Atualização"
            );

            // Assert
            assertThat(dto.versaoEsperada()).isNull();
        }

        @Test
        @DisplayName("Deve criar DTO sem motivo")
        void deveCriarDtoSemMotivo() {
            // Arrange
            Especificacao especificacao = Especificacao.exemplo();

            // Act
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                especificacao,
                "OP-001",
                2L,
                null
            );

            // Assert
            assertThat(dto.motivo()).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Controle de Versão")
    class ControleVersaoTests {

        @Test
        @DisplayName("Deve ter controle de versão quando versão informada")
        void deveTermControleVersaoQuandoVersaoInformada() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComVersao(5L);

            // Act & Assert
            assertThat(dto.temControleVersao()).isTrue();
        }

        @Test
        @DisplayName("Não deve ter controle de versão quando versão não informada")
        void naoDeveTermControleVersaoQuandoVersaoNaoInformada() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComVersao(null);

            // Act & Assert
            assertThat(dto.temControleVersao()).isFalse();
        }

        @Test
        @DisplayName("Deve ter controle de versão para versão zero")
        void deveTermControleVersaoParaVersaoZero() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComVersao(0L);

            // Act & Assert
            assertThat(dto.temControleVersao()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Motivo")
    class MotivoTests {

        @Test
        @DisplayName("Deve retornar motivo quando informado")
        void deveRetornarMotivoQuandoInformado() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComMotivo("Alteração de cor do veículo");

            // Act
            String motivo = dto.getMotivoOuPadrao();

            // Assert
            assertThat(motivo).isEqualTo("Alteração de cor do veículo");
        }

        @Test
        @DisplayName("Deve retornar motivo padrão quando não informado")
        void deveRetornarMotivoPadraoQuandoNaoInformado() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComMotivo(null);

            // Act
            String motivo = dto.getMotivoOuPadrao();

            // Assert
            assertThat(motivo).isEqualTo("Atualização de especificações");
        }

        @Test
        @DisplayName("Deve retornar motivo padrão quando vazio")
        void deveRetornarMotivoPadraoQuandoVazio() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComMotivo("");

            // Act
            String motivo = dto.getMotivoOuPadrao();

            // Assert
            assertThat(motivo).isEqualTo("Atualização de especificações");
        }

        @Test
        @DisplayName("Deve retornar motivo padrão quando apenas espaços")
        void deveRetornarMotivoPadraoQuandoApenasEspacos() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComMotivo("   ");

            // Act
            String motivo = dto.getMotivoOuPadrao();

            // Assert
            assertThat(motivo).isEqualTo("Atualização de especificações");
        }

        @Test
        @DisplayName("Deve retornar motivo mesmo com espaços ao redor")
        void deveRetornarMotivoMesmoComEspacosAoRedor() {
            // Arrange
            AtualizarVeiculoRequestDTO dto = criarDtoComMotivo("  Motivo válido  ");

            // Act
            String motivo = dto.getMotivoOuPadrao();

            // Assert
            assertThat(motivo).isEqualTo("  Motivo válido  ");
        }
    }

    @Nested
    @DisplayName("Testes de Especificação")
    class EspecificacaoTests {

        @Test
        @DisplayName("Deve aceitar especificação com diferentes combustíveis compatíveis")
        void deveAceitarEspecificacaoComDiferentesCombustiveisCompativeis() {
            // Arrange - Testar apenas combinações válidas (cilindrada > 0 para não elétricos)
            Especificacao spec1 = Especificacao.of("Azul", TipoCombustivel.GASOLINA, CategoriaVeiculo.PASSEIO, 1800);
            Especificacao spec2 = Especificacao.of("Verde", TipoCombustivel.FLEX, CategoriaVeiculo.PASSEIO, 1600);
            Especificacao spec3 = Especificacao.of("Preto", TipoCombustivel.ETANOL, CategoriaVeiculo.PASSEIO, 1400);

            // Act
            AtualizarVeiculoRequestDTO dto1 = new AtualizarVeiculoRequestDTO(spec1, "OP-001", 1L, "Teste");
            AtualizarVeiculoRequestDTO dto2 = new AtualizarVeiculoRequestDTO(spec2, "OP-001", 1L, "Teste");
            AtualizarVeiculoRequestDTO dto3 = new AtualizarVeiculoRequestDTO(spec3, "OP-001", 1L, "Teste");

            // Assert
            assertThat(dto1.especificacao().getTipoCombustivel()).isEqualTo(TipoCombustivel.GASOLINA);
            assertThat(dto2.especificacao().getTipoCombustivel()).isEqualTo(TipoCombustivel.FLEX);
            assertThat(dto3.especificacao().getTipoCombustivel()).isEqualTo(TipoCombustivel.ETANOL);
        }

        @Test
        @DisplayName("Deve aceitar especificação com diferentes categorias compatíveis")
        void deveAceitarEspecificacaoComDiferentesCategoriasCompativeis() {
            // Arrange - Testar apenas combinações válidas
            Especificacao spec1 = Especificacao.of("Verde", TipoCombustivel.GASOLINA, CategoriaVeiculo.PASSEIO, 1600);
            Especificacao spec2 = Especificacao.of("Branco", TipoCombustivel.DIESEL, CategoriaVeiculo.UTILITARIO, 2500);
            Especificacao spec3 = Especificacao.of("Vermelho", TipoCombustivel.GASOLINA, CategoriaVeiculo.MOTOCICLETA, 250);

            // Act
            AtualizarVeiculoRequestDTO dto1 = new AtualizarVeiculoRequestDTO(spec1, "OP-001", 1L, "Teste");
            AtualizarVeiculoRequestDTO dto2 = new AtualizarVeiculoRequestDTO(spec2, "OP-001", 1L, "Teste");
            AtualizarVeiculoRequestDTO dto3 = new AtualizarVeiculoRequestDTO(spec3, "OP-001", 1L, "Teste");

            // Assert
            assertThat(dto1.especificacao().getCategoria()).isEqualTo(CategoriaVeiculo.PASSEIO);
            assertThat(dto2.especificacao().getCategoria()).isEqualTo(CategoriaVeiculo.UTILITARIO);
            assertThat(dto3.especificacao().getCategoria()).isEqualTo(CategoriaVeiculo.MOTOCICLETA);
        }
    }

    @Nested
    @DisplayName("Testes de Records")
    class RecordsTests {

        @Test
        @DisplayName("Deve ter equals correto")
        void deveTermEqualsCorreto() {
            // Arrange
            Especificacao spec = Especificacao.exemplo();
            AtualizarVeiculoRequestDTO dto1 = new AtualizarVeiculoRequestDTO(
                spec, "OP-001", 2L, "Motivo"
            );
            AtualizarVeiculoRequestDTO dto2 = new AtualizarVeiculoRequestDTO(
                spec, "OP-001", 2L, "Motivo"
            );

            // Act & Assert
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString legível")
        void deveTermToStringLegivel() {
            // Arrange
            Especificacao spec = Especificacao.exemplo();
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                spec, "OP-001", 2L, "Atualização"
            );

            // Act
            String toString = dto.toString();

            // Assert
            assertThat(toString).contains("OP-001");
            assertThat(toString).contains("Atualização");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar atualização com controle de concorrência")
        void deveRepresentarAtualizacaoComControleConcorrencia() {
            // Arrange
            Especificacao novaSpec = Especificacao.of("Vermelho", TipoCombustivel.FLEX,
                CategoriaVeiculo.PASSEIO, 2000);

            // Act
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                novaSpec,
                "OP-SUPERVISOR",
                7L,
                "Alteração de cor conforme solicitação do cliente"
            );

            // Assert
            assertThat(dto.especificacao().getCor()).isEqualTo("Vermelho");
            assertThat(dto.especificacao().getCilindrada()).isEqualTo(2000);
            assertThat(dto.versaoEsperada()).isEqualTo(7L);
            assertThat(dto.temControleVersao()).isTrue();
            assertThat(dto.getMotivoOuPadrao()).contains("solicitação do cliente");
        }

        @Test
        @DisplayName("Deve representar atualização sem controle de concorrência")
        void deveRepresentarAtualizacaoSemControleConcorrencia() {
            // Arrange
            Especificacao novaSpec = Especificacao.of("Preto", TipoCombustivel.DIESEL,
                CategoriaVeiculo.UTILITARIO, 2500);

            // Act
            AtualizarVeiculoRequestDTO dto = new AtualizarVeiculoRequestDTO(
                novaSpec,
                "OP-BASIC",
                null,
                null
            );

            // Assert
            assertThat(dto.especificacao().getCor()).isEqualTo("Preto");
            assertThat(dto.versaoEsperada()).isNull();
            assertThat(dto.temControleVersao()).isFalse();
            assertThat(dto.getMotivoOuPadrao()).isEqualTo("Atualização de especificações");
        }
    }

    // === Métodos auxiliares ===

    private AtualizarVeiculoRequestDTO criarDtoComVersao(Long versao) {
        Especificacao spec = Especificacao.exemplo();
        return new AtualizarVeiculoRequestDTO(spec, "OP-001", versao, "Teste");
    }

    private AtualizarVeiculoRequestDTO criarDtoComMotivo(String motivo) {
        Especificacao spec = Especificacao.exemplo();
        return new AtualizarVeiculoRequestDTO(spec, "OP-001", 1L, motivo);
    }
}
