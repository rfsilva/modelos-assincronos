package com.seguradora.hibrida.domain.veiculo.controller.dto;

import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.Especificacao;
import com.seguradora.hibrida.domain.veiculo.model.Proprietario;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import com.seguradora.hibrida.domain.veiculo.model.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CriarVeiculoRequestDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CriarVeiculoRequestDTO - Testes Unitários")
class CriarVeiculoRequestDTOTest {

    @Nested
    @DisplayName("Testes de Criação e Validação")
    class CriacaoValidacaoTests {

        @Test
        @DisplayName("Deve criar DTO com todos os dados válidos")
        void deveCriarDtoComTodosDadosValidos() {
            // Arrange
            Especificacao especificacao = Especificacao.of("Branco", TipoCombustivel.FLEX,
                CategoriaVeiculo.PASSEIO, 1600);
            Proprietario proprietario = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);

            // Act
            CriarVeiculoRequestDTO dto = new CriarVeiculoRequestDTO(
                "ABC1234",
                "12345678900",
                "1HGBH41J6MN109186",
                "Honda",
                "Civic",
                2023,
                2024,
                especificacao,
                proprietario,
                "OP-001"
            );

            // Assert
            assertThat(dto).isNotNull();
            assertThat(dto.placa()).isEqualTo("ABC1234");
            assertThat(dto.renavam()).isEqualTo("12345678900");
            assertThat(dto.chassi()).isEqualTo("1HGBH41J6MN109186");
            assertThat(dto.marca()).isEqualTo("Honda");
            assertThat(dto.modelo()).isEqualTo("Civic");
            assertThat(dto.anoFabricacao()).isEqualTo(2023);
            assertThat(dto.anoModelo()).isEqualTo(2024);
            assertThat(dto.especificacao()).isEqualTo(especificacao);
            assertThat(dto.proprietario()).isEqualTo(proprietario);
            assertThat(dto.operadorId()).isEqualTo("OP-001");
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Ano Modelo")
    class ValidacaoAnoModeloTests {

        @Test
        @DisplayName("Deve validar ano modelo igual ao de fabricação")
        void deveValidarAnoModeloIgualAoFabricacao() {
            // Arrange
            CriarVeiculoRequestDTO dto = criarDtoComAnos(2023, 2023);

            // Act & Assert
            assertThat(dto.isAnoModeloValido()).isTrue();
        }

        @Test
        @DisplayName("Deve validar ano modelo 1 ano posterior ao de fabricação")
        void deveValidarAnoModeloUmAnoPosterior() {
            // Arrange
            CriarVeiculoRequestDTO dto = criarDtoComAnos(2023, 2024);

            // Act & Assert
            assertThat(dto.isAnoModeloValido()).isTrue();
        }

        @Test
        @DisplayName("Deve invalidar ano modelo mais de 1 ano posterior ao de fabricação")
        void deveInvalidarAnoModeloMaisDeUmAnoPosterior() {
            // Arrange
            CriarVeiculoRequestDTO dto = criarDtoComAnos(2023, 2025);

            // Act & Assert
            assertThat(dto.isAnoModeloValido()).isFalse();
        }

        @Test
        @DisplayName("Deve validar ano modelo dentro do range permitido")
        void deveValidarAnoModeloDentroDoRangePermitido() {
            // Arrange - A lógica permite anoModelo <= anoFabricacao + 1
            // Portanto, ano modelo pode ser até 1 ano posterior
            CriarVeiculoRequestDTO dto1 = criarDtoComAnos(2023, 2022); // 1 ano antes: válido
            CriarVeiculoRequestDTO dto2 = criarDtoComAnos(2023, 2023); // Mesmo ano: válido
            CriarVeiculoRequestDTO dto3 = criarDtoComAnos(2023, 2024); // 1 ano depois: válido

            // Act & Assert
            assertThat(dto1.isAnoModeloValido()).isTrue();
            assertThat(dto2.isAnoModeloValido()).isTrue();
            assertThat(dto3.isAnoModeloValido()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Cálculo de Idade")
    class CalculoIdadeTests {

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo novo")
        void deveCalcularIdadeVeiculoNovo() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual, anoAtual);

            // Act
            int idade = dto.getIdade();

            // Assert
            assertThat(idade).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo de 5 anos")
        void deveCalcularIdadeVeiculo5Anos() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual - 5, anoAtual - 5);

            // Act
            int idade = dto.getIdade();

            // Assert
            assertThat(idade).isEqualTo(5);
        }

        @Test
        @DisplayName("Deve calcular idade corretamente para veículo antigo")
        void deveCalcularIdadeVeiculoAntigo() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual - 20, anoAtual - 20);

            // Act
            int idade = dto.getIdade();

            // Assert
            assertThat(idade).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Testes de Classificação de Veículo Novo")
    class ClassificacaoVeiculoNovoTests {

        @Test
        @DisplayName("Deve classificar como novo veículo com 0 anos")
        void deveClassificarComoNovoVeiculoZeroAnos() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual, anoAtual);

            // Act & Assert
            assertThat(dto.isVeiculoNovo()).isTrue();
        }

        @Test
        @DisplayName("Deve classificar como novo veículo com 3 anos")
        void deveClassificarComoNovoVeiculoTresAnos() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual - 3, anoAtual - 3);

            // Act & Assert
            assertThat(dto.isVeiculoNovo()).isTrue();
        }

        @Test
        @DisplayName("Não deve classificar como novo veículo com 4 anos")
        void naoDeveClassificarComoNovoVeiculoQuatroAnos() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual - 4, anoAtual - 4);

            // Act & Assert
            assertThat(dto.isVeiculoNovo()).isFalse();
        }

        @Test
        @DisplayName("Não deve classificar como novo veículo com 10 anos")
        void naoDeveClassificarComoNovoVeiculoDezAnos() {
            // Arrange
            int anoAtual = java.time.Year.now().getValue();
            CriarVeiculoRequestDTO dto = criarDtoComAnos(anoAtual - 10, anoAtual - 10);

            // Act & Assert
            assertThat(dto.isVeiculoNovo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Records")
    class RecordsTests {

        @Test
        @DisplayName("Deve ter equals e hashCode corretos")
        void deveTermEqualsEHashCodeCorretos() {
            // Arrange
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            CriarVeiculoRequestDTO dto1 = new CriarVeiculoRequestDTO(
                "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024,
                especificacao, proprietario, "OP-001"
            );

            CriarVeiculoRequestDTO dto2 = new CriarVeiculoRequestDTO(
                "ABC1234", "12345678900", "1HGBH41J6MN109186",
                "Honda", "Civic", 2023, 2024,
                especificacao, proprietario, "OP-001"
            );

            // Act & Assert
            assertThat(dto1).isEqualTo(dto2);
            assertThat(dto1.hashCode()).isEqualTo(dto2.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString legível")
        void deveTermToStringLegivel() {
            // Arrange
            CriarVeiculoRequestDTO dto = criarDtoCompleto();

            // Act
            String toString = dto.toString();

            // Assert
            assertThat(toString).contains("ABC1234");
            assertThat(toString).contains("Honda");
            assertThat(toString).contains("Civic");
        }
    }

    // === Métodos auxiliares ===

    private CriarVeiculoRequestDTO criarDtoComAnos(int anoFabricacao, int anoModelo) {
        Especificacao especificacao = Especificacao.exemplo();
        Proprietario proprietario = Proprietario.exemplo();

        return new CriarVeiculoRequestDTO(
            "ABC1234",
            "12345678900",
            "1HGBH41J6MN109186",
            "Honda",
            "Civic",
            anoFabricacao,
            anoModelo,
            especificacao,
            proprietario,
            "OP-001"
        );
    }

    private CriarVeiculoRequestDTO criarDtoCompleto() {
        Especificacao especificacao = Especificacao.of("Branco", TipoCombustivel.FLEX,
            CategoriaVeiculo.PASSEIO, 1600);
        Proprietario proprietario = Proprietario.of("11144477735", "João Silva", TipoPessoa.FISICA);

        return new CriarVeiculoRequestDTO(
            "ABC1234",
            "12345678900",
            "1HGBH41J6MN109186",
            "Honda",
            "Civic",
            2023,
            2024,
            especificacao,
            proprietario,
            "OP-001"
        );
    }
}
