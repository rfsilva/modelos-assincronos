package com.seguradora.hibrida.domain.veiculo.command;

import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import com.seguradora.hibrida.domain.veiculo.model.TipoPessoa;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link CriarVeiculoCommand}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("CriarVeiculoCommand - Testes Unitários")
class CriarVeiculoCommandTest {

    @Nested
    @DisplayName("Testes de Criação com Builder")
    class CriacaoBuilderTests {

        @Test
        @DisplayName("Deve criar comando com todos os dados obrigatórios")
        void deveCriarComandoComTodosDadosObrigatorios() {
            // Act
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .cilindrada(1600)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            // Assert
            assertThat(command).isNotNull();
            assertThat(command.getCommandId()).isNotNull();
            assertThat(command.getTimestamp()).isNotNull();
            assertThat(command.getPlaca()).isEqualTo("ABC1234");
            assertThat(command.getRenavam()).isEqualTo("12345678900");
            assertThat(command.getChassi()).isEqualTo("1HGBH41J6MN109186");
            assertThat(command.getMarca()).isEqualTo("Honda");
            assertThat(command.getModelo()).isEqualTo("Civic");
            assertThat(command.getAnoFabricacao()).isEqualTo(2023);
            assertThat(command.getAnoModelo()).isEqualTo(2024);
            assertThat(command.getCor()).isEqualTo("Branco");
            assertThat(command.getTipoCombustivel()).isEqualTo(TipoCombustivel.FLEX);
            assertThat(command.getCategoria()).isEqualTo(CategoriaVeiculo.PASSEIO);
            assertThat(command.getCilindrada()).isEqualTo(1600);
            assertThat(command.getProprietarioCpfCnpj()).isEqualTo("12345678909");
            assertThat(command.getProprietarioNome()).isEqualTo("João Silva");
            assertThat(command.getProprietarioTipo()).isEqualTo(TipoPessoa.FISICA);
            assertThat(command.getOperadorId()).isEqualTo("OP-123");
        }

        @Test
        @DisplayName("Deve criar comando sem cilindrada")
        void deveCriarComandoSemCilindrada() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Tesla")
                .modelo("Model 3")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.ELETRICO)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getCilindrada()).isNull();
            assertThat(command.temCilindrada()).isFalse();
        }

        @Test
        @DisplayName("Deve criar comando sem observações")
        void deveCriarComandoSemObservacoes() {
            CriarVeiculoCommand command = criarCommandoValido();

            assertThat(command.getObservacoes()).isNull();
            assertThat(command.temObservacoes()).isFalse();
        }

        @Test
        @DisplayName("Deve criar comando com observações")
        void deveCriarComandoComObservacoes() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .cilindrada(1600)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .observacoes("Veículo importado")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getObservacoes()).isEqualTo("Veículo importado");
            assertThat(command.temObservacoes()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Normalização")
    class NormalizacaoTests {

        @Test
        @DisplayName("Deve normalizar placa para uppercase")
        void deveNormalizarPlacaParaUppercase() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("abc1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve remover não dígitos do RENAVAM")
        void deveRemoverNaoDigitosDoRenavam() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("123.456.789-01")
                .chassi("1HGBH41J6MN109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getRenavam()).isEqualTo("12345678901");
        }

        @Test
        @DisplayName("Deve normalizar chassi para uppercase")
        void deveNormalizarChassiParaUppercase() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1hgbh41j6mn109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getChassi()).isEqualTo("1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve remover espaços dos campos de texto")
        void deveRemoverEspacosDosCamposDeTexto() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("  ABC1234  ")
                .renavam("12345678900")
                .chassi("  1HGBH41J6MN109186  ")
                .marca("  Honda  ")
                .modelo("  Civic  ")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("  Branco  ")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("12345678909")
                .proprietarioNome("  João Silva  ")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("  OP-123  ")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getPlaca()).isEqualTo("ABC1234");
            assertThat(command.getChassi()).isEqualTo("1HGBH41J6MN109186");
            assertThat(command.getMarca()).isEqualTo("Honda");
            assertThat(command.getModelo()).isEqualTo("Civic");
            assertThat(command.getCor()).isEqualTo("Branco");
            assertThat(command.getProprietarioNome()).isEqualTo("João Silva");
            assertThat(command.getOperadorId()).isEqualTo("OP-123");
        }

        @Test
        @DisplayName("Deve remover não dígitos do CPF/CNPJ")
        void deveRemoverNaoDigitosDoCpfCnpj() {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Honda")
                .modelo("Civic")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Branco")
                .tipoCombustivel(TipoCombustivel.FLEX)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("123.456.789-09")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            assertThat(command.getProprietarioCpfCnpj()).isEqualTo("12345678909");
        }
    }

    @Nested
    @DisplayName("Testes de Equals e HashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Comandos com mesmo ID devem ser iguais")
        void comandosComMesmoIdDevemSerIguais() {
            CriarVeiculoCommand command1 = criarCommandoValido();

            assertThat(command1).isEqualTo(command1);
            assertThat(command1.hashCode()).isEqualTo(command1.hashCode());
        }

        @Test
        @DisplayName("Comandos diferentes não devem ser iguais")
        void comandosDiferentesNaoDevemSerIguais() {
            CriarVeiculoCommand command1 = criarCommandoValido();
            CriarVeiculoCommand command2 = criarCommandoValido();

            assertThat(command1).isNotEqualTo(command2);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            CriarVeiculoCommand command = criarCommandoValido();

            String toString = command.toString();

            assertThat(toString).contains("CriarVeiculoCommand");
            assertThat(toString).contains("ABC1234");
            assertThat(toString).contains("Honda");
            assertThat(toString).contains("Civic");
        }
    }

    private CriarVeiculoCommand criarCommandoValido() {
        return CriarVeiculoCommand.builder()
            .placa("ABC1234")
            .renavam("12345678900")
            .chassi("1HGBH41J6MN109186")
            .marca("Honda")
            .modelo("Civic")
            .anoFabricacao(2023)
            .anoModelo(2024)
            .cor("Branco")
            .tipoCombustivel(TipoCombustivel.FLEX)
            .categoria(CategoriaVeiculo.PASSEIO)
            .cilindrada(1600)
            .proprietarioCpfCnpj("12345678909")
            .proprietarioNome("João Silva")
            .proprietarioTipo(TipoPessoa.FISICA)
            .operadorId("OP-123")
            .correlationId(UUID.randomUUID())
            .userId("USER-001")
            .build();
    }
}
