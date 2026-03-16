package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AtualizarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.model.*;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link AtualizarVeiculoCommandHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AtualizarVeiculoCommandHandler - Testes Unitários")
class AtualizarVeiculoCommandHandlerTest {

    @Mock
    private AggregateRepository<VeiculoAggregate> veiculoRepository;

    @InjectMocks
    private AtualizarVeiculoCommandHandler handler;

    private VeiculoAggregate veiculoMock;
    private AtualizarVeiculoCommand commandValido;
    private Especificacao novaEspecificacao;

    @BeforeEach
    void setUp() {
        // Criar aggregate mock
        Especificacao especificacaoInicial = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                              CategoriaVeiculo.PASSEIO, 1600);
        Proprietario proprietario = Proprietario.exemplo();

        veiculoMock = VeiculoAggregate.criarVeiculo(
            "VEI-001", "ABC1234", "12345678900", "1HGBH41J6MN109186",
            "Honda", "Civic", 2023, 2024, especificacaoInicial, proprietario, "OP-123"
        );

        // Nova especificação
        novaEspecificacao = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                            CategoriaVeiculo.PASSEIO, 2000);

        // Comando válido
        commandValido = new AtualizarVeiculoCommand(
            "VEI-001", novaEspecificacao, "OP-456", 1L, UUID.randomUUID(), "USER-001"
        );

        // Configuração padrão
        lenient().when(veiculoRepository.getById(anyString())).thenReturn(veiculoMock);
    }

    @Nested
    @DisplayName("Testes de Execução com Sucesso")
    class ExecucaoSucessoTests {

        @Test
        @DisplayName("Deve atualizar veículo com sucesso")
        void deveAtualizarVeiculoComSucesso() {
            // Act
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isSuccess()).isTrue();
            assertThat(result.getData()).isEqualTo("VEI-001");
            assertThat(result.getMetadata()).containsKeys("novaVersao", "especificacao");
        }

        @Test
        @DisplayName("Deve salvar aggregate atualizado no repositório")
        void deveSalvarAggregateAtualizadoNoRepositorio() {
            // Act
            handler.handle(commandValido);

            // Assert
            verify(veiculoRepository).save(veiculoMock);
        }

        @Test
        @DisplayName("Deve atualizar sem verificar versão quando não informada")
        void deveAtualizarSemVerificarVersaoQuandoNaoInformada() {
            // Arrange
            AtualizarVeiculoCommand commandSemVersao = new AtualizarVeiculoCommand(
                "VEI-001", novaEspecificacao, "OP-456", null, UUID.randomUUID(), "USER-001"
            );

            // Act
            CommandResult result = handler.handle(commandSemVersao);

            // Assert
            assertThat(result.isSuccess()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Controle de Concorrência")
    class ControleConcorrenciaTests {

        @Test
        @DisplayName("Deve lançar exceção quando versão esperada não corresponde")
        void deveLancarExcecaoQuandoVersaoEsperadaNaoCorresponde() {
            // Arrange - versão atual é 1, mas comando espera versão 5
            AtualizarVeiculoCommand commandVersaoInvalida = new AtualizarVeiculoCommand(
                "VEI-001", novaEspecificacao, "OP-456", 5L, UUID.randomUUID(), "USER-001"
            );

            // Act
            CommandResult result = handler.handle(commandVersaoInvalida);

            // Assert
            assertThat(result.isSuccess()).isFalse();
            verify(veiculoRepository, never()).save(any());
        }

        @Test
        @DisplayName("Deve atualizar quando versão esperada corresponde")
        void deveAtualizarQuandoVersaoEsperadaCorresponde() {
            // Act - versão esperada 1 corresponde à versão atual
            CommandResult result = handler.handle(commandValido);

            // Assert
            assertThat(result.isSuccess()).isTrue();
            verify(veiculoRepository).save(veiculoMock);
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

            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                "VEI-999", novaEspecificacao, "OP-456", null, UUID.randomUUID(), "USER-001"
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
    @DisplayName("Testes de Configuração do Handler")
    class ConfiguracaoHandlerTests {

        @Test
        @DisplayName("Deve retornar tipo de comando correto")
        void deveRetornarTipoDeComandoCorreto() {
            assertThat(handler.getCommandType()).isEqualTo(AtualizarVeiculoCommand.class);
        }

        @Test
        @DisplayName("Deve ter timeout de 20 segundos")
        void deveTermTimeout20Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(20);
        }
    }
}
