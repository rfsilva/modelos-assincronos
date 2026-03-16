package com.seguradora.hibrida.domain.apolice.query.model;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Testes unitários para ApoliceQueryModel.
 */
@DisplayName("ApoliceQueryModel Tests")
class ApoliceQueryModelTest {

    private ApoliceQueryModel model;

    @BeforeEach
    void setUp() {
        model = new ApoliceQueryModel("ap-001", "AP-2024-001", "seg-001");
        model.setProduto("Seguro Auto");
        model.setStatus(StatusApolice.ATIVA);
        model.setVigenciaInicio(LocalDate.now());
        model.setVigenciaFim(LocalDate.now().plusDays(365));
        model.setValorSegurado(new BigDecimal("100000"));
        model.setValorPremio(new BigDecimal("5000"));
        model.setValorTotal(new BigDecimal("5000"));
        model.setSeguradoNome("João Silva");
        model.setSeguradoCpf("12345678901");
        model.setCoberturas(List.of(TipoCobertura.TOTAL));
    }

    @Nested
    @DisplayName("Construtor e Validações")
    class ConstrutorValidacoes {

        @Test
        @DisplayName("Deve criar apólice com dados válidos")
        void deveCriarApoliceValida() {
            ApoliceQueryModel nova = new ApoliceQueryModel("ap-002", "AP-2024-002", "seg-002");
            assertThat(nova.getId()).isEqualTo("ap-002");
            assertThat(nova.getNumero()).isEqualTo("AP-2024-002");
            assertThat(nova.getSeguradoId()).isEqualTo("seg-002");
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID é nulo")
        void deveLancarExcecaoQuandoIdNulo() {
            assertThatThrownBy(() -> new ApoliceQueryModel(null, "AP-2024-001", "seg-001"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando número é nulo")
        void deveLancarExcecaoQuandoNumeroNulo() {
            assertThatThrownBy(() -> new ApoliceQueryModel("ap-001", null, "seg-001"))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("Número não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando seguradoId é nulo")
        void deveLancarExcecaoQuandoSeguradoIdNulo() {
            assertThatThrownBy(() -> new ApoliceQueryModel("ap-001", "AP-2024-001", null))
                .isInstanceOf(NullPointerException.class)
                .hasMessageContaining("ID do segurado não pode ser nulo");
        }
    }

    @Nested
    @DisplayName("Verificações de Status")
    class VerificacoesStatus {

        @Test
        @DisplayName("Deve identificar apólice ativa")
        void deveIdentificarApoliceAtiva() {
            assertThat(model.isAtiva()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar apólice não ativa")
        void deveIdentificarApoliceNaoAtiva() {
            model.setStatus(StatusApolice.CANCELADA);
            assertThat(model.isAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar apólice vencida")
        void deveIdentificarApoliceVencida() {
            model.setVigenciaFim(LocalDate.now().minusDays(1));
            assertThat(model.isVencida()).isTrue();
        }

        @Test
        @DisplayName("Deve identificar apólice não vencida")
        void deveIdentificarApoliceNaoVencida() {
            assertThat(model.isVencida()).isFalse();
        }

        @Test
        @DisplayName("Deve tratar vigenciaFim nulo")
        void deveTratarVigenciaFimNulo() {
            model.setVigenciaFim(null);
            assertThat(model.isVencida()).isFalse();
        }
    }

    @Nested
    @DisplayName("Verificações de Vencimento")
    class VerificacoesVencimento {

        @Test
        @DisplayName("Deve verificar se vence em X dias")
        void deveVerificarSeVenceEmXDias() {
            model.setVigenciaFim(LocalDate.now().plusDays(15));
            assertThat(model.venceEm(30)).isTrue();
            assertThat(model.venceEm(10)).isFalse();
        }

        @Test
        @DisplayName("Deve tratar vigenciaFim nulo ao verificar vencimento")
        void deveTratarVigenciaFimNuloAoVerificar() {
            model.setVigenciaFim(null);
            assertThat(model.venceEm(30)).isFalse();
        }

        @Test
        @DisplayName("Deve calcular dias para vencimento")
        void deveCalcularDiasParaVencimento() {
            model.setVigenciaFim(LocalDate.now().plusDays(30));
            int dias = model.calcularDiasParaVencimento();
            assertThat(dias).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve retornar -1 quando vigenciaFim é nulo")
        void deveRetornarMenos1QuandoVigenciaFimNulo() {
            model.setVigenciaFim(null);
            assertThat(model.calcularDiasParaVencimento()).isEqualTo(-1);
        }

        @Test
        @DisplayName("Deve retornar dias negativos quando vencida")
        void deveRetornarDiasNegativosQuandoVencida() {
            model.setVigenciaFim(LocalDate.now().minusDays(10));
            int dias = model.calcularDiasParaVencimento();
            assertThat(dias).isLessThan(0);
        }
    }

    @Nested
    @DisplayName("Verificações de Cobertura")
    class VerificacoesCobertura {

        @Test
        @DisplayName("Deve verificar se possui cobertura específica")
        void deveVerificarSePossuiCobertura() {
            assertThat(model.possuiCobertura(TipoCobertura.TOTAL)).isTrue();
            assertThat(model.possuiCobertura(TipoCobertura.PARCIAL)).isFalse();
        }

        @Test
        @DisplayName("Deve tratar coberturas nulas")
        void deveTratarCoberturasNulas() {
            model.setCoberturas(null);
            assertThat(model.possuiCobertura(TipoCobertura.TOTAL)).isFalse();
        }

        @Test
        @DisplayName("Deve tratar lista de coberturas vazia")
        void deveTratarListaVazia() {
            model.setCoberturas(List.of());
            assertThat(model.possuiCobertura(TipoCobertura.TOTAL)).isFalse();
        }
    }

    @Nested
    @DisplayName("Cálculo de Valores")
    class CalculoValores {

        @Test
        @DisplayName("Deve calcular valor da franquia estimado")
        void deveCalcularValorFranquia() {
            BigDecimal franquia = model.getValorFranquiaEstimado();
            assertThat(franquia).isEqualByComparingTo(new BigDecimal("10000"));
        }

        @Test
        @DisplayName("Deve retornar zero quando valor segurado é nulo")
        void deveRetornarZeroQuandoValorSeguradoNulo() {
            model.setValorSegurado(null);
            assertThat(model.getValorFranquiaEstimado()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Elegibilidade para Renovação")
    class ElegibilidadeRenovacao {

        @Test
        @DisplayName("Deve ser elegível para renovação automática")
        void deveSerElegivel() {
            model.setRenovacaoAutomatica(true);
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setScoreRenovacao(80);
            assertThat(model.isElegivelRenovacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Não deve ser elegível sem renovação automática")
        void naoDeveSerElegivelSemRenovacao() {
            model.setRenovacaoAutomatica(false);
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setScoreRenovacao(80);
            assertThat(model.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível quando não está ativa")
        void naoDeveSerElegivelQuandoNaoAtiva() {
            model.setStatus(StatusApolice.CANCELADA);
            model.setRenovacaoAutomatica(true);
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setScoreRenovacao(80);
            assertThat(model.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível quando não vence em 30 dias")
        void naoDeveSerElegivelQuandoNaoVence() {
            model.setRenovacaoAutomatica(true);
            model.setVigenciaFim(LocalDate.now().plusDays(100));
            model.setScoreRenovacao(80);
            assertThat(model.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser elegível com score baixo")
        void naoDeveSerElegivelComScoreBaixo() {
            model.setRenovacaoAutomatica(true);
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setScoreRenovacao(50);
            assertThat(model.isElegivelRenovacaoAutomatica()).isFalse();
        }

        @Test
        @DisplayName("Deve ser elegível com score nulo")
        void deveSerElegivelComScoreNulo() {
            model.setRenovacaoAutomatica(true);
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setScoreRenovacao(null);
            assertThat(model.isElegivelRenovacaoAutomatica()).isTrue();
        }
    }

    @Nested
    @DisplayName("Getters e Setters")
    class GettersSetters {

        @Test
        @DisplayName("Deve ter getNumeroApolice como alias")
        void deveTerAliasNumeroApolice() {
            assertThat(model.getNumeroApolice()).isEqualTo(model.getNumero());
        }

        @Test
        @DisplayName("Deve configurar e recuperar todos os campos")
        void deveConfigurarTodosCampos() {
            model.setId("ap-002");
            model.setNumero("AP-2024-002");
            model.setProduto("Seguro Vida");
            model.setStatus(StatusApolice.SUSPENSA);
            model.setSeguradoNome("Maria Silva");
            model.setSeguradoCpf("98765432100");
            model.setSeguradoEmail("maria@email.com");
            model.setSeguradoTelefone("11988888888");
            model.setSeguradoCidade("Rio de Janeiro");
            model.setSeguradoEstado("RJ");
            model.setFormaPagamento("Boleto");
            model.setParcelas(6);
            model.setOperadorResponsavel("Operador B");
            model.setCanalVenda("Presencial");
            model.setObservacoes("Observações importantes");
            model.setDiasParaVencimento(60);
            model.setVencimentoProximo(false);
            model.setRenovacaoAutomatica(false);
            model.setScoreRenovacao(75);
            model.setLastEventId(100L);
            model.setVersion(5L);
            model.setCoberturasResumo("Cobertura completa");
            model.setTemCoberturaTotal(true);

            assertThat(model.getId()).isEqualTo("ap-002");
            assertThat(model.getNumero()).isEqualTo("AP-2024-002");
            assertThat(model.getProduto()).isEqualTo("Seguro Vida");
            assertThat(model.getStatus()).isEqualTo(StatusApolice.SUSPENSA);
            assertThat(model.getSeguradoNome()).isEqualTo("Maria Silva");
            assertThat(model.getSeguradoCpf()).isEqualTo("98765432100");
            assertThat(model.getSeguradoEmail()).isEqualTo("maria@email.com");
            assertThat(model.getSeguradoTelefone()).isEqualTo("11988888888");
            assertThat(model.getSeguradoCidade()).isEqualTo("Rio de Janeiro");
            assertThat(model.getSeguradoEstado()).isEqualTo("RJ");
            assertThat(model.getFormaPagamento()).isEqualTo("Boleto");
            assertThat(model.getParcelas()).isEqualTo(6);
            assertThat(model.getOperadorResponsavel()).isEqualTo("Operador B");
            assertThat(model.getCanalVenda()).isEqualTo("Presencial");
            assertThat(model.getObservacoes()).isEqualTo("Observações importantes");
            assertThat(model.getDiasParaVencimento()).isEqualTo(60);
            assertThat(model.getVencimentoProximo()).isFalse();
            assertThat(model.getRenovacaoAutomatica()).isFalse();
            assertThat(model.getScoreRenovacao()).isEqualTo(75);
            assertThat(model.getLastEventId()).isEqualTo(100L);
            assertThat(model.getVersion()).isEqualTo(5L);
            assertThat(model.getCoberturasResumo()).isEqualTo("Cobertura completa");
            assertThat(model.getTemCoberturaTotal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Callbacks JPA")
    class CallbacksJPA {

        @Test
        @DisplayName("Deve executar onCreate")
        void deveExecutarOnCreate() {
            ApoliceQueryModel nova = new ApoliceQueryModel("ap-003", "AP-2024-003", "seg-003");
            nova.setVigenciaFim(LocalDate.now().plusDays(20));
            nova.onCreate();

            assertThat(nova.getCreatedAt()).isNotNull();
            assertThat(nova.getUpdatedAt()).isNotNull();
            assertThat(nova.getDiasParaVencimento()).isNotNull();
            assertThat(nova.getVencimentoProximo()).isNotNull();
        }

        @Test
        @DisplayName("Deve executar onUpdate")
        void deveExecutarOnUpdate() {
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.onUpdate();

            assertThat(model.getUpdatedAt()).isNotNull();
            assertThat(model.getDiasParaVencimento()).isEqualTo(20);
            assertThat(model.getVencimentoProximo()).isTrue();
        }

        @Test
        @DisplayName("Deve manter createdAt ao executar onCreate quando já existe")
        void deveManterCreatedAtExistente() {
            java.time.Instant dataOriginal = java.time.Instant.now().minusSeconds(3600);
            model.setCreatedAt(dataOriginal);
            model.onCreate();

            assertThat(model.getCreatedAt()).isEqualTo(dataOriginal);
        }
    }

    @Nested
    @DisplayName("Equals, HashCode e ToString")
    class EqualsHashCodeToString {

        @Test
        @DisplayName("Deve ser igual a si mesmo")
        void deveSerIgualASiMesmo() {
            assertThat(model).isEqualTo(model);
        }

        @Test
        @DisplayName("Deve ser igual a outro objeto com mesmo ID")
        void deveSerIgualComMesmoId() {
            ApoliceQueryModel outra = new ApoliceQueryModel("ap-001", "AP-2024-999", "seg-999");
            assertThat(model).isEqualTo(outra);
        }

        @Test
        @DisplayName("Não deve ser igual a objeto com ID diferente")
        void naoDeveSerIgualComIdDiferente() {
            ApoliceQueryModel outra = new ApoliceQueryModel("ap-002", "AP-2024-001", "seg-001");
            assertThat(model).isNotEqualTo(outra);
        }

        @Test
        @DisplayName("Não deve ser igual a null")
        void naoDeveSerIgualANull() {
            assertThat(model).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Não deve ser igual a objeto de outra classe")
        void naoDeveSerIgualAOutraClasse() {
            assertThat(model).isNotEqualTo("string");
        }

        @Test
        @DisplayName("Deve ter mesmo hashCode para objetos iguais")
        void deveTerMesmoHashCode() {
            ApoliceQueryModel outra = new ApoliceQueryModel("ap-001", "AP-2024-999", "seg-999");
            assertThat(model.hashCode()).isEqualTo(outra.hashCode());
        }

        @Test
        @DisplayName("Deve ter hashCode diferente para objetos diferentes")
        void deveTerHashCodeDiferente() {
            ApoliceQueryModel outra = new ApoliceQueryModel("ap-002", "AP-2024-001", "seg-001");
            assertThat(model.hashCode()).isNotEqualTo(outra.hashCode());
        }

        @Test
        @DisplayName("Deve ter toString informativo")
        void deveTerToStringInformativo() {
            String toString = model.toString();
            assertThat(toString).contains("ApoliceQueryModel");
            assertThat(toString).contains("ap-001");
            assertThat(toString).contains("AP-2024-001");
            assertThat(toString).contains("João Silva");
            assertThat(toString).containsIgnoringCase("ATIVA");
        }
    }
}
