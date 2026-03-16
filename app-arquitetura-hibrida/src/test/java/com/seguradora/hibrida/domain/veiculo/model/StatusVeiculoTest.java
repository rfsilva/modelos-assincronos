package com.seguradora.hibrida.domain.veiculo.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link StatusVeiculo}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("StatusVeiculo - Testes Unitários")
class StatusVeiculoTest {

    @Nested
    @DisplayName("Testes de Valores do Enum")
    class ValoresEnumTests {

        @Test
        @DisplayName("Deve ter todos os status esperados")
        void deveTerTodosStatusEsperados() {
            // Act
            StatusVeiculo[] valores = StatusVeiculo.values();

            // Assert
            assertThat(valores).hasSize(4);
            assertThat(valores).containsExactlyInAnyOrder(
                StatusVeiculo.ATIVO,
                StatusVeiculo.INATIVO,
                StatusVeiculo.BLOQUEADO,
                StatusVeiculo.SINISTRADO
            );
        }

        @Test
        @DisplayName("Deve obter status por nome")
        void deveObterStatusPorNome() {
            // Act & Assert
            assertThat(StatusVeiculo.valueOf("ATIVO")).isEqualTo(StatusVeiculo.ATIVO);
            assertThat(StatusVeiculo.valueOf("INATIVO")).isEqualTo(StatusVeiculo.INATIVO);
            assertThat(StatusVeiculo.valueOf("BLOQUEADO")).isEqualTo(StatusVeiculo.BLOQUEADO);
            assertThat(StatusVeiculo.valueOf("SINISTRADO")).isEqualTo(StatusVeiculo.SINISTRADO);
        }

        @ParameterizedTest
        @EnumSource(StatusVeiculo.class)
        @DisplayName("Todos os status devem ter nome e descrição")
        void todosStatusDevemTermNomeEDescricao(StatusVeiculo status) {
            assertThat(status.getNome()).isNotNull().isNotEmpty();
            assertThat(status.getDescricao()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Status Ativo")
    class StatusAtivoTests {

        @Test
        @DisplayName("ATIVO deve ser identificado como ativo")
        void ativoDeveSerIdentificadoComoAtivo() {
            assertThat(StatusVeiculo.ATIVO.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Outros status não devem ser identificados como ativo")
        void outrosStatusNaoDevemSerIdentificadosComoAtivo() {
            assertThat(StatusVeiculo.INATIVO.isAtivo()).isFalse();
            assertThat(StatusVeiculo.BLOQUEADO.isAtivo()).isFalse();
            assertThat(StatusVeiculo.SINISTRADO.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Apenas ATIVO deve poder associar apólice")
        void apenasAtivoDevePoderAssociarApolice() {
            assertThat(StatusVeiculo.ATIVO.podeAssociarApolice()).isTrue();
            assertThat(StatusVeiculo.INATIVO.podeAssociarApolice()).isFalse();
            assertThat(StatusVeiculo.BLOQUEADO.podeAssociarApolice()).isFalse();
            assertThat(StatusVeiculo.SINISTRADO.podeAssociarApolice()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Permissões")
    class PermissoesTests {

        @Test
        @DisplayName("ATIVO e INATIVO devem permitir alterações")
        void ativoEInativoDevemPermitirAlteracoes() {
            assertThat(StatusVeiculo.ATIVO.permiteAlteracoes()).isTrue();
            assertThat(StatusVeiculo.INATIVO.permiteAlteracoes()).isTrue();
        }

        @Test
        @DisplayName("BLOQUEADO e SINISTRADO não devem permitir alterações")
        void bloqueadoESinistradoNaoDevemPermitirAlteracoes() {
            assertThat(StatusVeiculo.BLOQUEADO.permiteAlteracoes()).isFalse();
            assertThat(StatusVeiculo.SINISTRADO.permiteAlteracoes()).isFalse();
        }

        @Test
        @DisplayName("Apenas ATIVO deve permitir transferência")
        void apenasAtivoDevePermitirTransferencia() {
            assertThat(StatusVeiculo.ATIVO.permiteTransferencia()).isTrue();
            assertThat(StatusVeiculo.INATIVO.permiteTransferencia()).isFalse();
            assertThat(StatusVeiculo.BLOQUEADO.permiteTransferencia()).isFalse();
            assertThat(StatusVeiculo.SINISTRADO.permiteTransferencia()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Status Final")
    class StatusFinalTests {

        @Test
        @DisplayName("Apenas SINISTRADO deve ser status final")
        void apenasSinistradoDeveSerStatusFinal() {
            assertThat(StatusVeiculo.SINISTRADO.isFinal()).isTrue();
            assertThat(StatusVeiculo.ATIVO.isFinal()).isFalse();
            assertThat(StatusVeiculo.INATIVO.isFinal()).isFalse();
            assertThat(StatusVeiculo.BLOQUEADO.isFinal()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Formatação")
    class FormatacaoTests {

        @Test
        @DisplayName("ToString deve retornar o nome do status")
        void toStringDeveRetornarNomeDoStatus() {
            assertThat(StatusVeiculo.ATIVO.toString()).isEqualTo("Ativo");
            assertThat(StatusVeiculo.INATIVO.toString()).isEqualTo("Inativo");
            assertThat(StatusVeiculo.BLOQUEADO.toString()).isEqualTo("Bloqueado");
            assertThat(StatusVeiculo.SINISTRADO.toString()).isEqualTo("Sinistrado");
        }

        @ParameterizedTest
        @EnumSource(StatusVeiculo.class)
        @DisplayName("ToString não deve retornar null ou vazio")
        void toStringNaoDeveRetornarNullOuVazio(StatusVeiculo status) {
            assertThat(status.toString()).isNotNull().isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Descrições")
    class DescricoesTests {

        @Test
        @DisplayName("ATIVO deve ter descrição apropriada")
        void ativoDeveTerDescricaoApropriada() {
            assertThat(StatusVeiculo.ATIVO.getDescricao())
                .containsIgnoringCase("ativo")
                .containsIgnoringCase("operacional");
        }

        @Test
        @DisplayName("INATIVO deve ter descrição apropriada")
        void inativoDeveTerDescricaoApropriada() {
            assertThat(StatusVeiculo.INATIVO.getDescricao())
                .containsIgnoringCase("inativo");
        }

        @Test
        @DisplayName("BLOQUEADO deve ter descrição apropriada")
        void bloqueadoDeveTerDescricaoApropriada() {
            assertThat(StatusVeiculo.BLOQUEADO.getDescricao())
                .containsIgnoringCase("bloqueado");
        }

        @Test
        @DisplayName("SINISTRADO deve ter descrição apropriada")
        void sinistradoDeveTerDescricaoApropriada() {
            assertThat(StatusVeiculo.SINISTRADO.getDescricao())
                .containsIgnoringCase("perda")
                .containsIgnoringCase("sinistro");
        }
    }

    @Nested
    @DisplayName("Testes de Regras de Negócio")
    class RegrasNegocioTests {

        @Test
        @DisplayName("Status que permite alterações deve permitir reativação")
        void statusQuePermiteAlteracoesDevePermitirReativacao() {
            // Regra: se permite alterações, pode ser modificado
            StatusVeiculo[] statusAlteraveis = {StatusVeiculo.ATIVO, StatusVeiculo.INATIVO};

            for (StatusVeiculo status : statusAlteraveis) {
                assertThat(status.permiteAlteracoes())
                    .as("Status %s deve permitir alterações", status)
                    .isTrue();
            }
        }

        @Test
        @DisplayName("Status final não deve permitir nenhuma operação")
        void statusFinalNaoDevePermitirNenhumaOperacao() {
            StatusVeiculo sinistrado = StatusVeiculo.SINISTRADO;

            assertThat(sinistrado.isFinal()).isTrue();
            assertThat(sinistrado.permiteAlteracoes()).isFalse();
            assertThat(sinistrado.permiteTransferencia()).isFalse();
            assertThat(sinistrado.podeAssociarApolice()).isFalse();
        }

        @Test
        @DisplayName("Veículo bloqueado não deve permitir operações comerciais")
        void veiculoBloqueadoNaoDevePermitirOperacoesComerciais() {
            StatusVeiculo bloqueado = StatusVeiculo.BLOQUEADO;

            assertThat(bloqueado.podeAssociarApolice()).isFalse();
            assertThat(bloqueado.permiteTransferencia()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Consistência")
    class ConsistenciaTests {

        @ParameterizedTest
        @EnumSource(StatusVeiculo.class)
        @DisplayName("Enum deve ser consistente com valores definidos")
        void enumDeveSerConsistenteComValoresDefinidos(StatusVeiculo status) {
            // Verificar que o enum pode ser recuperado pelo nome
            StatusVeiculo recuperado = StatusVeiculo.valueOf(status.name());
            assertThat(recuperado).isEqualTo(status);
        }

        @Test
        @DisplayName("Métodos booleanos devem ser mutuamente exclusivos quando aplicável")
        void metodosBooleanosDevemSerMutuamenteExclusivos() {
            // isAtivo é true apenas para ATIVO
            int countAtivo = 0;
            for (StatusVeiculo status : StatusVeiculo.values()) {
                if (status.isAtivo()) countAtivo++;
            }
            assertThat(countAtivo).isEqualTo(1);

            // isFinal é true apenas para SINISTRADO
            int countFinal = 0;
            for (StatusVeiculo status : StatusVeiculo.values()) {
                if (status.isFinal()) countFinal++;
            }
            assertThat(countFinal).isEqualTo(1);
        }
    }
}
