package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AssociarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.model.*;
import com.seguradora.hibrida.domain.veiculo.service.ApoliceValidationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AssociarVeiculoCommandHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AssociarVeiculoCommandHandler - Testes Unitários")
class AssociarVeiculoCommandHandlerTest {

    @Mock
    private AggregateRepository<VeiculoAggregate> veiculoRepository;

    @Mock
    private ApoliceValidationService apoliceValidationService;

    @InjectMocks
    private AssociarVeiculoCommandHandler handler;

    private VeiculoAggregate veiculoMock;
    private AssociarVeiculoCommand commandValido;

    @BeforeEach
    void setUp() {
        // Criar aggregate mock
        Especificacao especificacao = Especificacao.exemplo();
        Proprietario proprietario = Proprietario.exemplo();

        veiculoMock = VeiculoAggregate.criarVeiculo(
            "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
            "Honda", "Civic", 2023, 2024, especificacao, proprietario, "OP-123"
        );

        // Comando válido
        commandValido = new AssociarVeiculoCommand(
            "VEI-001", "APO-001", LocalDate.now(), "OP-456", UUID.randomUUID(), "USER-001"
        );

        // Configuração padrão
        lenient().when(veiculoRepository.getById(anyString())).thenReturn(veiculoMock);
        lenient().doNothing().when(apoliceValidationService).validarApoliceParaAssociacao(anyString(), anyString());
    }

    @Nested
    @DisplayName("Testes de Execução com Sucesso")
    class ExecucaoSucessoTests {

        @Test
        @DisplayName("Deve associar veículo à apólice com sucesso")
        void deveAssociarVeiculoApoliceComSucesso() {
            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("VEI-001");
            assertThat(result.getMetadata()).containsKeys("apoliceId", "dataInicio", "version");
            assertThat(result.getMetadata().get("apoliceId")).isEqualTo("APO-001");
        }

        @Test
        @DisplayName("Deve validar apólice antes de associar")
        void deveValidarApoliceAntesDeAssociar() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(apoliceValidationService).validarApoliceParaAssociacao("APO-001", "VEI-001");
        }

        @Test
        @DisplayName("Deve salvar aggregate atualizado no repositório")
        void deveSalvarAggregateAtualizadoNoRepositorio() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(veiculoRepository).save(veiculoMock);
            assertThat(veiculoMock.isAssociadoA("APO-001")).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Falha por Validação")
    class FalhaValidacaoTests {

        @Test
        @DisplayName("Deve retornar falha quando apólice é inválida")
        void deveRetornarFalhaQuandoApoliceInvalida() {
            // Arrange
            doThrow(new IllegalArgumentException("Apólice não encontrada"))
                .when(apoliceValidationService).validarApoliceParaAssociacao(anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Apólice não encontrada");
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve retornar falha quando veículo não existe")
        void deveRetornarFalhaQuandoVeiculoNaoExiste() {
            // Arrange
            when(veiculoRepository.getById("VEI-999")).thenThrow(
                new RuntimeException("Veículo não encontrado")
            );

            AssociarVeiculoCommand command = new AssociarVeiculoCommand(
                "VEI-999", "APO-001", LocalDate.now(), "OP-456", UUID.randomUUID(), "USER-001"
            );

            // Act
            CommandResult result = handler.handle(command);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Veículo não encontrado");
        }
    }

    @Nested
    @DisplayName("Testes de Metadados")
    class MetadadosTests {

        @Test
        @DisplayName("Deve incluir IDs em caso de falha")
        void deveIncluirIdsEmCasoDeFalha() {
            // Arrange
            doThrow(new RuntimeException("Erro genérico"))
                .when(apoliceValidationService).validarApoliceParaAssociacao(anyString(), anyString());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.getMetadata()).containsKeys("veiculoId", "apoliceId");
            assertThat(result.getMetadata().get("veiculoId")).isEqualTo("VEI-001");
            assertThat(result.getMetadata().get("apoliceId")).isEqualTo("APO-001");
        }
    }

    @Nested
    @DisplayName("Testes de Configuração do Handler")
    class ConfiguracaoHandlerTests {

        @Test
        @DisplayName("Deve retornar tipo de comando correto")
        void deveRetornarTipoDeComandoCorreto() {
            assertThat(handler.getCommandType()).isEqualTo(AssociarVeiculoCommand.class);
        }

        @Test
        @DisplayName("Deve ter timeout de 20 segundos")
        void deveTermTimeout20Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(20);
        }
    }
}
