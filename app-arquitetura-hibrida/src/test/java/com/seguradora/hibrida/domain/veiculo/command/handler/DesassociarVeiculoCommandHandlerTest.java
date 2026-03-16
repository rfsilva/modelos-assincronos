package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.DesassociarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.model.*;
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
 * Testes unitários para {@link DesassociarVeiculoCommandHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("DesassociarVeiculoCommandHandler - Testes Unitários")
class DesassociarVeiculoCommandHandlerTest {

    @Mock
    private AggregateRepository<VeiculoAggregate> veiculoRepository;

    @InjectMocks
    private DesassociarVeiculoCommandHandler handler;

    private VeiculoAggregate veiculoMock;
    private DesassociarVeiculoCommand commandValido;

    @BeforeEach
    void setUp() {
        // Criar aggregate mock com apólice já associada
        Especificacao especificacao = Especificacao.exemplo();
        Proprietario proprietario = Proprietario.exemplo();

        veiculoMock = VeiculoAggregate.criarVeiculo(
            "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
            "Honda", "Civic", 2023, 2024, especificacao, proprietario, "OP-123"
        );

        // Associar apólice primeiro
        veiculoMock.associarApolice("APO-001", LocalDate.now(), "OP-123");

        // Comando válido
        commandValido = new DesassociarVeiculoCommand(
            "VEI-001", "APO-001", LocalDate.now(), "Cancelamento",
            "OP-456", UUID.randomUUID(), "USER-001"
        );

        // Configuração padrão
        lenient().when(veiculoRepository.getById(anyString())).thenReturn(veiculoMock);
    }

    @Nested
    @DisplayName("Testes de Execução com Sucesso")
    class ExecucaoSucessoTests {

        @Test
        @DisplayName("Deve desassociar veículo da apólice com sucesso")
        void deveDesassociarVeiculoApoliceComSucesso() {
            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("VEI-001");
            assertThat(result.getMetadata()).containsKeys("apoliceId", "dataFim", "motivo", "version");
            assertThat(result.getMetadata().get("apoliceId")).isEqualTo("APO-001");
            assertThat(result.getMetadata().get("motivo")).isEqualTo("Cancelamento");
        }

        @Test
        @DisplayName("Deve salvar aggregate atualizado no repositório")
        void deveSalvarAggregateAtualizadoNoRepositorio() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(veiculoRepository).save(veiculoMock);
            assertThat(veiculoMock.isAssociadoA("APO-001")).isFalse();
        }

        @Test
        @DisplayName("Deve aceitar diferentes motivos de desassociação")
        void deveAceitarDiferentesMotivosDeDesassociacao() {
            // Cenários diferentes
            String[] motivos = {
                "Cancelamento por solicitação do cliente",
                "Venda do veículo",
                "Término de vigência"
            };

            for (String motivo : motivos) {
                // Criar novo aggregate para cada teste
                VeiculoAggregate veiculo = criarVeiculoComApolice();
                when(veiculoRepository.getById("VEI-001")).thenReturn(veiculo);

                DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                    "VEI-001", "APO-001", LocalDate.now(), motivo,
                    "OP-456", UUID.randomUUID(), "USER-001"
                );

                // Act
                CommandResult result = handler.handle(command);

                // Assert
                assertThat(result.isSuccess()).isTrue();
                assertThat(result.getMetadata().get("motivo")).isEqualTo(motivo);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Falha")
    class FalhaTests {

        @Test
        @DisplayName("Deve retornar falha quando veículo não existe")
        void deveRetornarFalhaQuandoVeiculoNaoExiste() {
            // Arrange
            when(veiculoRepository.getById("VEI-999")).thenThrow(
                new RuntimeException("Veículo não encontrado")
            );

            DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                "VEI-999", "APO-001", LocalDate.now(), "Cancelamento",
                "OP-456", UUID.randomUUID(), "USER-001"
            );

            // Act
            CommandResult result = handler.handle(command);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Veículo não encontrado");
        }

        @Test
        @DisplayName("Deve retornar falha quando repositório lança exceção")
        void deveRetornarFalhaQuandoRepositorioLancaExcecao() {
            // Arrange
            doThrow(new RuntimeException("Erro ao salvar"))
                .when(veiculoRepository).save(any());

            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            assertThat(result.getErrorMessage()).contains("Erro ao salvar");
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
                .when(veiculoRepository).save(any());

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
            assertThat(handler.getCommandType()).isEqualTo(DesassociarVeiculoCommand.class);
        }

        @Test
        @DisplayName("Deve ter timeout de 20 segundos")
        void deveTermTimeout20Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(20);
        }
    }

    private VeiculoAggregate criarVeiculoComApolice() {
        Especificacao especificacao = Especificacao.exemplo();
        Proprietario proprietario = Proprietario.exemplo();

        VeiculoAggregate veiculo = VeiculoAggregate.criarVeiculo(
            "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
            "Honda", "Civic", 2023, 2024, especificacao, proprietario, "OP-123"
        );

        veiculo.associarApolice("APO-001", LocalDate.now(), "OP-123");
        return veiculo;
    }
}
