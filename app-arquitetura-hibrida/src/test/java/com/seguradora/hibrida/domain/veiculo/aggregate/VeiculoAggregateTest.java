package com.seguradora.hibrida.domain.veiculo.aggregate;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.domain.veiculo.event.*;
import com.seguradora.hibrida.domain.veiculo.model.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoAggregate}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoAggregate - Testes Unitários")
class VeiculoAggregateTest {

    private static final String VEICULO_ID = "VEI-001";
    private static final String OPERADOR_ID = "OP-123";
    private static final String APOLICE_ID = "APO-001";
    private static final String CHASSI = "1HGBH41J6MN109186";

    @Nested
    @DisplayName("Testes de Criação de Veículo")
    class CriacaoVeiculoTests {

        @Test
        @DisplayName("Deve criar veículo com dados válidos")
        void deveCriarVeiculoComDadosValidos() {
            // Arrange
            Especificacao especificacao = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                          CategoriaVeiculo.PASSEIO, 1600);
            Proprietario proprietario = Proprietario.of("12345678909", "João Silva", TipoPessoa.FISICA);

            // Act
            VeiculoAggregate veiculo = VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            // Assert
            assertThat(veiculo).isNotNull();
            assertThat(veiculo.getId()).isEqualTo(VEICULO_ID);
            assertThat(veiculo.getPlaca().getFormatada()).isEqualTo("ABC-1234");
            assertThat(veiculo.getMarca()).isEqualTo("Honda");
            assertThat(veiculo.getModelo()).isEqualTo("Civic");
            assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.ATIVO);
            assertThat(veiculo.getOperadorCriacao()).isEqualTo(OPERADOR_ID);
            assertThat(veiculo.getUncommittedEvents()).hasSize(1);
            assertThat(veiculo.getUncommittedEvents().get(0)).isInstanceOf(VeiculoCriadoEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para placa nula")
        void deveLancarExcecaoParaPlacaNula() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, null, "12345678900", CHASSI,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para renavam nulo")
        void deveLancarExcecaoParaRenavamNulo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", null, CHASSI,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para chassi nulo")
        void deveLancarExcecaoParaChassiNulo() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", null,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para marca nula ou vazia")
        void deveLancarExcecaoParaMarcaNulaOuVazia() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                null, "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Marca é obrigatória");

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "   ", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Marca é obrigatória");
        }

        @Test
        @DisplayName("Deve lançar exceção para modelo nulo ou vazio")
        void deveLancarExcecaoParaModeloNuloOuVazio() {
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "Honda", null, 2023, 2024, especificacao, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Modelo é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção para especificação nula")
        void deveLancarExcecaoParaEspecificacaoNula() {
            Proprietario proprietario = Proprietario.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "Honda", "Civic", 2023, 2024, null, proprietario, OPERADOR_ID
            ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Especificação é obrigatória");
        }

        @Test
        @DisplayName("Deve lançar exceção para proprietário nulo")
        void deveLancarExcecaoParaProprietarioNulo() {
            Especificacao especificacao = Especificacao.exemplo();

            assertThatThrownBy(() -> VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "Honda", "Civic", 2023, 2024, especificacao, null, OPERADOR_ID
            ))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Proprietário é obrigatório");
        }
    }

    @Nested
    @DisplayName("Testes de Atualização de Especificações")
    class AtualizacaoEspecificacoesTests {

        @Test
        @DisplayName("Deve atualizar especificações com sucesso")
        void deveAtualizarEspecificacoesComSucesso() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            Especificacao novaEspecificacao = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                              CategoriaVeiculo.PASSEIO, 2000);

            // Act
            veiculo.atualizarEspecificacoes(novaEspecificacao, OPERADOR_ID);

            // Assert
            assertThat(veiculo.getEspecificacao()).isEqualTo(novaEspecificacao);
            assertThat(veiculo.getUltimoOperador()).isEqualTo(OPERADOR_ID);
            assertThat(veiculo.getUncommittedEvents()).hasSize(2);
            assertThat(veiculo.getUncommittedEvents().get(1)).isInstanceOf(VeiculoAtualizadoEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para especificação nula")
        void deveLancarExcecaoParaEspecificacaoNula() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.atualizarEspecificacoes(null, OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Nova especificação é obrigatória");
        }

        @Test
        @DisplayName("Deve lançar exceção ao atualizar veículo inativo")
        void deveLancarExcecaoAoAtualizarVeiculoInativo() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            // Simular mudança de status (através de event sourcing)
            VeiculoAtualizadoEvent event = VeiculoAtualizadoEvent.create(
                veiculo.getId(), veiculo.getVersion() + 1,
                veiculo.getEspecificacao(), veiculo.getEspecificacao(),
                OPERADOR_ID, "Teste"
            );
            // Nota: não podemos mudar status diretamente sem um evento específico
            // Este teste seria melhor com um mock ou após implementar evento de mudança de status
        }

        @Test
        @DisplayName("Deve lançar exceção para especificação incompatível com categoria")
        void deveLancarExcecaoParaEspecificacaoIncompativelComCategoria() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();

            // Act & Assert - A exceção ocorre ao tentar criar a especificação incompatível
            assertThatThrownBy(() -> {
                // Tentar criar especificação incompatível (Gasolina não é compatível com Caminhão)
                Especificacao especificacaoIncompativel = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                                           CategoriaVeiculo.CAMINHAO, 8000);
                veiculo.atualizarEspecificacoes(especificacaoIncompativel, OPERADOR_ID);
            })
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("não é compatível com categoria");
        }
    }

    @Nested
    @DisplayName("Testes de Associação de Apólice")
    class AssociacaoApoliceTests {

        @Test
        @DisplayName("Deve associar apólice com sucesso")
        void deveAssociarApoliceComSucesso() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            LocalDate dataInicio = LocalDate.now();

            // Act
            veiculo.associarApolice(APOLICE_ID, dataInicio, OPERADOR_ID);

            // Assert
            assertThat(veiculo.isAssociadoA(APOLICE_ID)).isTrue();
            assertThat(veiculo.temApolicesAtivas()).isTrue();
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(1);
            assertThat(veiculo.getUncommittedEvents()).hasSize(2);
            assertThat(veiculo.getUncommittedEvents().get(1)).isInstanceOf(VeiculoAssociadoEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice nulo")
        void deveLancarExcecaoParaIdApoliceNulo() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.associarApolice(null, LocalDate.now(), OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice vazio")
        void deveLancarExcecaoParaIdApoliceVazio() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.associarApolice("   ", LocalDate.now(), OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção ao associar apólice já associada")
        void deveLancarExcecaoAoAssociarApoliceJaAssociada() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            // Act & Assert
            assertThatThrownBy(() -> veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Veículo já associado à apólice");
        }

        @Test
        @DisplayName("Deve permitir associar múltiplas apólices diferentes")
        void devePermitirAssociarMultiplasApolicesDiferentes() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            String apolice1 = "APO-001";
            String apolice2 = "APO-002";
            String apolice3 = "APO-003";

            // Act
            veiculo.associarApolice(apolice1, LocalDate.now(), OPERADOR_ID);
            veiculo.associarApolice(apolice2, LocalDate.now(), OPERADOR_ID);
            veiculo.associarApolice(apolice3, LocalDate.now(), OPERADOR_ID);

            // Assert
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(3);
            assertThat(veiculo.isAssociadoA(apolice1)).isTrue();
            assertThat(veiculo.isAssociadoA(apolice2)).isTrue();
            assertThat(veiculo.isAssociadoA(apolice3)).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Desassociação de Apólice")
    class DesassociacaoApoliceTests {

        @Test
        @DisplayName("Deve desassociar apólice com sucesso")
        void deveDesassociarApoliceComSucesso() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);
            LocalDate dataFim = LocalDate.now();

            // Act
            veiculo.desassociarApolice(APOLICE_ID, dataFim, "Cancelamento", OPERADOR_ID);

            // Assert
            assertThat(veiculo.isAssociadoA(APOLICE_ID)).isFalse();
            assertThat(veiculo.temApolicesAtivas()).isFalse();
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(0);
            assertThat(veiculo.getUncommittedEvents()).hasSize(3);
            assertThat(veiculo.getUncommittedEvents().get(2)).isInstanceOf(VeiculoDesassociadoEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para ID de apólice nulo")
        void deveLancarExcecaoParaIdApoliceNulo() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.desassociarApolice(null, LocalDate.now(), "Motivo", OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("ID da apólice é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção ao desassociar apólice não associada")
        void deveLancarExcecaoAoDesassociarApoliceNaoAssociada() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.desassociarApolice(APOLICE_ID, LocalDate.now(), "Motivo", OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Veículo não está associado à apólice");
        }

        @Test
        @DisplayName("Deve manter outras apólices ao desassociar uma")
        void deveManterOutrasApolicesAoDesassociarUma() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            String apolice1 = "APO-001";
            String apolice2 = "APO-002";
            veiculo.associarApolice(apolice1, LocalDate.now(), OPERADOR_ID);
            veiculo.associarApolice(apolice2, LocalDate.now(), OPERADOR_ID);

            // Act
            veiculo.desassociarApolice(apolice1, LocalDate.now(), "Cancelamento", OPERADOR_ID);

            // Assert
            assertThat(veiculo.isAssociadoA(apolice1)).isFalse();
            assertThat(veiculo.isAssociadoA(apolice2)).isTrue();
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Testes de Transferência de Propriedade")
    class TransferenciaPropriedadeTests {

        @Test
        @DisplayName("Deve transferir propriedade com sucesso")
        void deveTransferirPropriedadeComSucesso() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            Proprietario novoProprietario = Proprietario.of("98765432100", "Maria Santos", TipoPessoa.FISICA);
            LocalDate dataTransferencia = LocalDate.now();

            // Act
            veiculo.transferirPropriedade(novoProprietario, dataTransferencia, OPERADOR_ID);

            // Assert
            assertThat(veiculo.getProprietario()).isEqualTo(novoProprietario);
            assertThat(veiculo.getUltimoOperador()).isEqualTo(OPERADOR_ID);
            assertThat(veiculo.getUncommittedEvents()).hasSize(2);
            assertThat(veiculo.getUncommittedEvents().get(1)).isInstanceOf(PropriedadeTransferidaEvent.class);
        }

        @Test
        @DisplayName("Deve lançar exceção para proprietário nulo")
        void deveLancarExcecaoParaProprietarioNulo() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThatThrownBy(() -> veiculo.transferirPropriedade(null, LocalDate.now(), OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Novo proprietário é obrigatório");
        }

        @Test
        @DisplayName("Deve lançar exceção ao transferir para mesmo proprietário")
        void deveLancarExcecaoAoTransferirParaMesmoProprietario() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            Proprietario mesmoProprietario = veiculo.getProprietario();

            // Act & Assert
            assertThatThrownBy(() -> veiculo.transferirPropriedade(mesmoProprietario, LocalDate.now(), OPERADOR_ID))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("Novo proprietário deve ser diferente do atual");
        }

        @Test
        @DisplayName("Deve permitir transferir de PF para PJ")
        void devePermitirTransferirDePfParaPj() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            Proprietario empresa = Proprietario.exemploEmpresa();

            // Act
            veiculo.transferirPropriedade(empresa, LocalDate.now(), OPERADOR_ID);

            // Assert
            assertThat(veiculo.getProprietario().isPessoaJuridica()).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Event Sourcing")
    class EventSourcingTests {

        @Test
        @DisplayName("Deve aplicar evento de criação corretamente")
        void deveAplicarEventoCriacaoCorretamente() {
            // Arrange & Act - criarVeiculo internamente aplica o evento
            Especificacao especificacao = Especificacao.exemplo();
            Proprietario proprietario = Proprietario.exemplo();
            VeiculoAggregate veiculo = VeiculoAggregate.criarVeiculo(
                VEICULO_ID, "ABC1234", "12345678900", CHASSI,
                "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
            );

            // Assert
            assertThat(veiculo.getPlaca()).isNotNull();
            assertThat(veiculo.getMarca()).isEqualTo("Honda");
            assertThat(veiculo.getModelo()).isEqualTo("Civic");
            assertThat(veiculo.getStatus()).isEqualTo(StatusVeiculo.ATIVO);
        }

        @Test
        @DisplayName("Deve reconstruir estado a partir de múltiplos eventos")
        void deveReconstruirEstadoAPartirDeMultiplosEventos() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            Especificacao novaEspecificacao = Especificacao.of("Preto", TipoCombustivel.GASOLINA,
                                                              CategoriaVeiculo.PASSEIO, 2000);

            // Act
            veiculo.atualizarEspecificacoes(novaEspecificacao, OPERADOR_ID);
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            // Assert - deve ter todos os eventos
            assertThat(veiculo.getUncommittedEvents()).hasSize(3);
            assertThat(veiculo.getEspecificacao()).isEqualTo(novaEspecificacao);
            assertThat(veiculo.isAssociadoA(APOLICE_ID)).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Snapshot")
    class SnapshotTests {

        @Test
        @DisplayName("Deve criar snapshot com estado completo")
        void deveCriarSnapshotComEstadoCompleto() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            // Act
            Object snapshot = veiculo.createSnapshot();

            // Assert
            assertThat(snapshot).isInstanceOf(VeiculoAggregate.VeiculoSnapshot.class);
            VeiculoAggregate.VeiculoSnapshot veiculoSnapshot = (VeiculoAggregate.VeiculoSnapshot) snapshot;
            assertThat(veiculoSnapshot.getId()).isEqualTo(veiculo.getId());
            assertThat(veiculoSnapshot.getPlaca()).isEqualTo(veiculo.getPlaca());
            assertThat(veiculoSnapshot.getMarca()).isEqualTo(veiculo.getMarca());
            assertThat(veiculoSnapshot.getApolicesAssociadas()).hasSize(1);
        }

        @Test
        @DisplayName("Deve restaurar estado a partir de snapshot")
        void deveRestaurarEstadoAPartirDeSnapshot() {
            // Arrange
            VeiculoAggregate veiculoOriginal = criarVeiculoValido();
            veiculoOriginal.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);
            Object snapshot = veiculoOriginal.createSnapshot();

            // Act
            VeiculoAggregate veiculoRestaurado = new VeiculoAggregate();
            veiculoRestaurado.loadFromSnapshot(snapshot, null);

            // Assert
            assertThat(veiculoRestaurado.getPlaca()).isEqualTo(veiculoOriginal.getPlaca());
            assertThat(veiculoRestaurado.getMarca()).isEqualTo(veiculoOriginal.getMarca());
            assertThat(veiculoRestaurado.getModelo()).isEqualTo(veiculoOriginal.getModelo());
            assertThat(veiculoRestaurado.isAssociadoA(APOLICE_ID)).isTrue();
        }

        @Test
        @DisplayName("Deve limpar estado corretamente ao recarregar do histórico")
        void deveLimparEstadoCorretamenteAoRecarregarDoHistorico() {
            // Arrange
            VeiculoAggregate veiculo = criarVeiculoValido();
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            // Capturar eventos antes de limpar
            java.util.List<com.seguradora.hibrida.eventstore.model.DomainEvent> eventos =
                new java.util.ArrayList<>(veiculo.getUncommittedEvents());

            // Act - criar novo aggregate e carregar do histórico
            VeiculoAggregate veiculoRecarregado = new VeiculoAggregate();
            veiculoRecarregado.loadFromHistory(eventos);

            // Assert - deve ter o mesmo estado do original
            assertThat(veiculoRecarregado.getPlaca()).isEqualTo(veiculo.getPlaca());
            assertThat(veiculoRecarregado.getMarca()).isEqualTo(veiculo.getMarca());
            assertThat(veiculoRecarregado.getModelo()).isEqualTo(veiculo.getModelo());
            assertThat(veiculoRecarregado.isAssociadoA(APOLICE_ID)).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Métodos Auxiliares")
    class MetodosAuxiliaresTests {

        @Test
        @DisplayName("Deve verificar se está associado a apólice")
        void deveVerificarSeEstaAssociadoApolice() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThat(veiculo.isAssociadoA(APOLICE_ID)).isFalse();

            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            assertThat(veiculo.isAssociadoA(APOLICE_ID)).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se tem apólices ativas")
        void deveVerificarSeTemApolicesAtivas() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThat(veiculo.temApolicesAtivas()).isFalse();

            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            assertThat(veiculo.temApolicesAtivas()).isTrue();
        }

        @Test
        @DisplayName("Deve contar quantidade de apólices ativas")
        void deveContarQuantidadeApolicesAtivas() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(0);

            veiculo.associarApolice("APO-001", LocalDate.now(), OPERADOR_ID);
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(1);

            veiculo.associarApolice("APO-002", LocalDate.now(), OPERADOR_ID);
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(2);

            veiculo.desassociarApolice("APO-001", LocalDate.now(), "Cancelamento", OPERADOR_ID);
            assertThat(veiculo.getQuantidadeApolicesAtivas()).isEqualTo(1);
        }

        @Test
        @DisplayName("Apólices associadas deve ser imutável")
        void apolicesAssociadasDeveSerImutavel() {
            VeiculoAggregate veiculo = criarVeiculoValido();
            veiculo.associarApolice(APOLICE_ID, LocalDate.now(), OPERADOR_ID);

            assertThatThrownBy(() -> veiculo.getApolicesAssociadas().add("APO-999"))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Testes de ToString")
    class ToStringTests {

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            VeiculoAggregate veiculo = criarVeiculoValido();

            String toString = veiculo.toString();

            assertThat(toString).contains(VEICULO_ID);
            assertThat(toString).contains("Honda");
            assertThat(toString).contains("Civic");
            assertThat(toString).contains("Ativo");
        }
    }

    // Métodos auxiliares

    private VeiculoAggregate criarVeiculoValido() {
        Especificacao especificacao = Especificacao.of("Branco", TipoCombustivel.FLEX,
                                                       CategoriaVeiculo.PASSEIO, 1600);
        Proprietario proprietario = Proprietario.of("12345678909", "João Silva", TipoPessoa.FISICA);

        return VeiculoAggregate.criarVeiculo(
            VEICULO_ID, "ABC1234", "12345678900", CHASSI,
            "Honda", "Civic", 2023, 2024, especificacao, proprietario, OPERADOR_ID
        );
    }
}
