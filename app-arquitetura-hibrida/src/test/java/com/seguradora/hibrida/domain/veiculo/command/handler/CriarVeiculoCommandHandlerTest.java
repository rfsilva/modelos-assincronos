package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.CriarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.model.CategoriaVeiculo;
import com.seguradora.hibrida.domain.veiculo.model.TipoCombustivel;
import com.seguradora.hibrida.domain.veiculo.model.TipoPessoa;
import com.seguradora.hibrida.domain.veiculo.service.VeiculoValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link CriarVeiculoCommandHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("CriarVeiculoCommandHandler - Testes Unitários")
class CriarVeiculoCommandHandlerTest {

    @Mock
    private AggregateRepository<VeiculoAggregate> veiculoRepository;

    @Mock
    private VeiculoValidationService validationService;

    @InjectMocks
    private CriarVeiculoCommandHandler handler;

    @Captor
    private ArgumentCaptor<VeiculoAggregate> veiculoCaptor;

    private CriarVeiculoCommand commandValido;

    @BeforeEach
    void setUp() {
        commandValido = CriarVeiculoCommand.builder()
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
            .proprietarioCpfCnpj("11144477735")
            .proprietarioNome("João Silva")
            .proprietarioTipo(TipoPessoa.FISICA)
            .operadorId("OP-123")
            .correlationId(UUID.randomUUID())
            .userId("USER-001")
            .build();

        // Configuração padrão: validação passa
        lenient().doNothing().when(validationService).validarUnicidade(anyString(), anyString(), anyString());
    }

    @Nested
    @DisplayName("Testes de Execução com Sucesso")
    class ExecucaoSucessoTests {

        @Test
        @DisplayName("Deve criar veículo com sucesso")
        void deveCriarVeiculoComSucesso() {
            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isNotNull();
            assertThat(result.getMetadata()).containsKeys("placa", "marca", "modelo", "version");
            assertThat(result.getMetadata().get("placa")).isEqualTo("ABC1234");
            assertThat(result.getMetadata().get("marca")).isEqualTo("Honda");
            assertThat(result.getMetadata().get("modelo")).isEqualTo("Civic");
            assertThat(result.getMetadata().get("version")).isEqualTo(1L);
        }

        @Test
        @DisplayName("Deve validar unicidade antes de criar")
        void deveValidarUnicidadeAntesDeCriar() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(validationService).validarUnicidade("ABC1234", "12345678900", "1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve salvar aggregate no repositório")
        void deveSalvarAggregateNoRepositorio() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(veiculoRepository).save(veiculoCaptor.capture());
            VeiculoAggregate veiculoSalvo = veiculoCaptor.getValue();

            assertThat(veiculoSalvo).isNotNull();
            assertThat(veiculoSalvo.getId()).isNotNull();
            assertThat(veiculoSalvo.getPlaca().getFormatada()).isEqualTo("ABC-1234");
            assertThat(veiculoSalvo.getMarca()).isEqualTo("Honda");
            assertThat(veiculoSalvo.getModelo()).isEqualTo("Civic");
        }

        @Test
        @DisplayName("Deve preservar correlationId no resultado")
        void devePreservarCorrelationIdNoResultado() {
            // Arrange
            UUID correlationId = UUID.randomUUID();
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
                .proprietarioCpfCnpj("11144477735")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(correlationId)
                .userId("USER-001")
                .build();

            // Act
            CommandResult result = handler.handle(command);

            // Assert
            assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        }

        @Test
        @DisplayName("Deve gerar ID único para cada veículo")
        void deveGerarIdUnicoParaCadaVeiculo() {
            // Act
            CommandResult result1 = handler.handle(commandValido);
            CommandResult result2 = handler.handle(commandValido);

            // Assert
            assertThat(result1.getData()).isNotEqualTo(result2.getData());
        }
    }

    @Nested
    @DisplayName("Testes de Falha por Validação")
    class FalhaValidacaoTests {

        @Test
        @DisplayName("Deve retornar falha quando placa já existe")
        void deveRetornarFalhaQuandoPlacaJaExiste() {
            // Arrange
            doThrow(new IllegalArgumentException("Placa já cadastrada no sistema: ABC1234"))
                .when(validationService).validarUnicidade(anyString(), anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Placa já cadastrada");
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar falha quando RENAVAM já existe")
        void deveRetornarFalhaQuandoRenavamJaExiste() {
            // Arrange
            doThrow(new IllegalArgumentException("RENAVAM já cadastrado no sistema: 12345678901"))
                .when(validationService).validarUnicidade(anyString(), anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("RENAVAM já cadastrado");
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar falha quando chassi já existe")
        void deveRetornarFalhaQuandoChassiJaExiste() {
            // Arrange
            doThrow(new IllegalArgumentException("Chassi já cadastrado no sistema: 1HGBH41J6MN109186"))
                .when(validationService).validarUnicidade(anyString(), anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Chassi já cadastrado");
            verify(veiculoRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Falha por Erro no Repositório")
    class FalhaRepositorioTests {

        @Test
        @DisplayName("Deve retornar falha quando repositório lança exceção")
        void deveRetornarFalhaQuandoRepositorioLancaExcecao() {
            // Arrange
            doThrow(new RuntimeException("Erro ao salvar no banco de dados"))
                .when(veiculoRepository).save(any());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Erro ao salvar no banco de dados");
        }
    }

    @Nested
    @DisplayName("Testes de Metadados")
    class MetadadosTests {

        @Test
        @DisplayName("Deve incluir nome do comando em caso de falha")
        void deveIncluirNomeDoComandoEmCasoDeFalha() {
            // Arrange
            doThrow(new RuntimeException("Erro genérico"))
                .when(validationService).validarUnicidade(anyString(), anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.getMetadata()).containsKey("command");
            assertThat(result.getMetadata().get("command")).isEqualTo("CriarVeiculoCommand");
        }

        @Test
        @DisplayName("Deve preservar correlationId em caso de falha")
        void devePreservarCorrelationIdEmCasoDeFalha() {
            // Arrange
            UUID correlationId = UUID.randomUUID();
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
                .proprietarioCpfCnpj("11144477735")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(correlationId)
                .userId("USER-001")
                .build();

            doThrow(new RuntimeException("Erro"))
                .when(validationService).validarUnicidade(anyString(), anyString(), anyString());

            // Act
            CommandResult result = handler.handle(command);

            // Assert
            assertThat(result.getCorrelationId()).isEqualTo(correlationId);
        }
    }

    @Nested
    @DisplayName("Testes de Configuração do Handler")
    class ConfiguracaoHandlerTests {

        @Test
        @DisplayName("Deve retornar tipo de comando correto")
        void deveRetornarTipoDeComandoCorreto() {
            assertThat(handler.getCommandType()).isEqualTo(CriarVeiculoCommand.class);
        }

        @Test
        @DisplayName("Deve ter timeout de 30 segundos")
        void deveTermTimeout30Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Testes de Integração de Dados")
    class IntegracaoDadosTests {

        @Test
        @DisplayName("Deve criar veículo com todos os dados do comando")
        void deveCriarVeiculoComTodosDadosDoComando() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(veiculoRepository).save(veiculoCaptor.capture());
            VeiculoAggregate veiculo = veiculoCaptor.getValue();

            assertThat(veiculo.getMarca()).isEqualTo("Honda");
            assertThat(veiculo.getModelo()).isEqualTo("Civic");
            assertThat(veiculo.getAnoModelo().getAnoFabricacao()).isEqualTo(2023);
            assertThat(veiculo.getAnoModelo().getAnoModelo()).isEqualTo(2024);
            assertThat(veiculo.getEspecificacao().getCor()).isEqualTo("Branco");
            assertThat(veiculo.getEspecificacao().getTipoCombustivel()).isEqualTo(TipoCombustivel.FLEX);
            assertThat(veiculo.getEspecificacao().getCategoria()).isEqualTo(CategoriaVeiculo.PASSEIO);
            assertThat(veiculo.getEspecificacao().getCilindrada()).isEqualTo(1600);
            assertThat(veiculo.getProprietario().getNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve criar veículo sem cilindrada quando não informada")
        void deveCriarVeiculoSemCilindradaQuandoNaoInformada() {
            // Arrange
            CriarVeiculoCommand commandSemCilindrada = CriarVeiculoCommand.builder()
                .placa("ABC1234")
                .renavam("12345678900")
                .chassi("1HGBH41J6MN109186")
                .marca("Tesla")
                .modelo("Model 3")
                .anoFabricacao(2023)
                .anoModelo(2024)
                .cor("Preto")
                .tipoCombustivel(TipoCombustivel.ELETRICO)
                .categoria(CategoriaVeiculo.PASSEIO)
                .proprietarioCpfCnpj("11144477735")
                .proprietarioNome("João Silva")
                .proprietarioTipo(TipoPessoa.FISICA)
                .operadorId("OP-123")
                .correlationId(UUID.randomUUID())
                .userId("USER-001")
                .build();

            // Act
            handler.handle(commandSemCilindrada);

            // Assert
            verify(veiculoRepository).save(veiculoCaptor.capture());
            VeiculoAggregate veiculo = veiculoCaptor.getValue();

            assertThat(veiculo.getEspecificacao().getCilindrada()).isNull();
        }
    }
}
