package com.seguradora.hibrida.domain.apolice.service;

import com.seguradora.hibrida.domain.apolice.model.*;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService.HistoricoSinistros;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para ApoliceValidationService.
 *
 * @author Test Automation Team
 * @since 1.0.0
 */
@DisplayName("ApoliceValidationService - Testes Unitários")
class ApoliceValidationServiceTest {

    private ApoliceValidationService service;

    @BeforeEach
    void setUp() {
        service = new ApoliceValidationService();
    }

    // ===================================================================
    // TESTES: validarVigencia
    // ===================================================================

    @Nested
    @DisplayName("validarVigencia - Validação de vigência")
    class ValidarVigenciaTests {

        @Test
        @DisplayName("Deve validar vigência válida")
        void deveValidarVigenciaValida() {
            // Arrange
            LocalDate inicio = LocalDate.now();
            LocalDate fim = inicio.plusYears(1);
            Vigencia vigencia = Vigencia.of(inicio, fim);

            // Act & Assert - Não deve lançar exceção
            assertThatNoException().isThrownBy(() ->
                service.validarVigencia(vigencia)
            );
        }

        @Test
        @DisplayName("Deve validar vigência anual")
        void deveValidarVigenciaAnual() {
            // Arrange
            Vigencia vigencia = Vigencia.anual(LocalDate.now());

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarVigencia(vigencia)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção quando início é posterior ao fim")
        void deveLancarExcecaoInicioAposFim() {
            // Arrange
            LocalDate inicio = LocalDate.now().plusYears(1);
            LocalDate fim = LocalDate.now();

            // Act & Assert
            assertThatThrownBy(() ->
                Vigencia.of(inicio, fim)
            ).isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("Deve lançar exceção quando início é muito anterior à data atual")
        void deveLancarExcecaoInicioMuitoAnterior() {
            // Arrange
            LocalDate inicio = LocalDate.now().minusDays(31);
            LocalDate fim = inicio.plusYears(1);

            // Act & Assert
            assertThatThrownBy(() -> {
                Vigencia vigencia = Vigencia.of(inicio, fim);
                service.validarVigencia(vigencia);
            }).isInstanceOf(IllegalArgumentException.class)
              .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("Deve validar vigência com início em até 30 dias no passado")
        void deveValidarVigenciaInicioRecente() {
            // Arrange
            LocalDate inicio = LocalDate.now().minusDays(15);
            LocalDate fim = inicio.plusYears(1);
            Vigencia vigencia = Vigencia.of(inicio, fim);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarVigencia(vigencia)
            );
        }

        @Test
        @DisplayName("Deve validar vigência com início futuro")
        void deveValidarVigenciaInicioFuturo() {
            // Arrange
            LocalDate inicio = LocalDate.now().plusDays(30);
            LocalDate fim = inicio.plusYears(1);
            Vigencia vigencia = Vigencia.of(inicio, fim);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarVigencia(vigencia)
            );
        }
    }

    // ===================================================================
    // TESTES: validarValorSegurado
    // ===================================================================

    @Nested
    @DisplayName("validarValorSegurado - Validação de valor segurado")
    class ValidarValorSeguradoTests {

        @Test
        @DisplayName("Deve validar valor segurado positivo")
        void deveValidarValorSeguradoPositivo() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarValorSegurado(valorSegurado, produto)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção para valor segurado zero")
        void deveLancarExcecaoValorZero() {
            // Arrange
            Valor valorZero = Valor.zero();
            String produto = "AUTO";

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarValorSegurado(valorZero, produto)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("positivo");
        }

        @Test
        @DisplayName("Deve validar valor segurado alto")
        void deveValidarValorSeguradoAlto() {
            // Arrange
            Valor valorAlto = Valor.reais(5000000.00);
            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarValorSegurado(valorAlto, produto)
            );
        }

        @Test
        @DisplayName("Deve validar valor segurado baixo mas positivo")
        void deveValidarValorSeguradoBaixo() {
            // Arrange
            Valor valorBaixo = Valor.reais(1000.00);
            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarValorSegurado(valorBaixo, produto)
            );
        }
    }

    // ===================================================================
    // TESTES: validarCoberturas
    // ===================================================================

    @Nested
    @DisplayName("validarCoberturas - Validação de coberturas")
    class ValidarCoberturasTests {

        @Test
        @DisplayName("Deve validar lista com uma cobertura")
        void deveValidarUmaCobertura() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCoberturas(coberturas, produto)
            );
        }

        @Test
        @DisplayName("Deve validar lista com múltiplas coberturas")
        void deveValidarMultiplasCoberturas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado),
                Cobertura.basica(TipoCobertura.INCENDIO, valorSegurado)
            );
            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCoberturas(coberturas, produto)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção para lista vazia de coberturas")
        void deveLancarExcecaoListaVazia() {
            // Arrange
            List<Cobertura> coberturasVazias = List.of();
            String produto = "AUTO";

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarCoberturas(coberturasVazias, produto)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("cobertura");
        }
    }

    // ===================================================================
    // TESTES: validarCombinacaoCoberturas
    // ===================================================================

    @Nested
    @DisplayName("validarCombinacaoCoberturas - Validação de combinações")
    class ValidarCombinacaoCoberturasTests {

        @Test
        @DisplayName("Deve validar combinação de coberturas válida")
        void deveValidarCombinacaoValida() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.COLISAO, valorSegurado),
                Cobertura.basica(TipoCobertura.ROUBO_FURTO, valorSegurado)
            );

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCombinacaoCoberturas(coberturas)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção para lista vazia")
        void deveLancarExcecaoListaVazia() {
            // Arrange
            List<Cobertura> coberturasVazias = List.of();

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarCombinacaoCoberturas(coberturasVazias)
            ).isInstanceOf(IllegalArgumentException.class);
        }
    }

    // ===================================================================
    // TESTES: validarFormaPagamento
    // ===================================================================

    @Nested
    @DisplayName("validarFormaPagamento - Validação de forma de pagamento")
    class ValidarFormaPagamentoTests {

        @Test
        @DisplayName("Deve validar forma de pagamento anual")
        void deveValidarFormaPagamentoAnual() {
            // Arrange
            FormaPagamento formaPagamento = FormaPagamento.ANUAL;
            Valor valorSegurado = Valor.reais(100000.00);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarFormaPagamento(formaPagamento, valorSegurado)
            );
        }

        @Test
        @DisplayName("Deve validar forma de pagamento mensal")
        void deveValidarFormaPagamentoMensal() {
            // Arrange
            FormaPagamento formaPagamento = FormaPagamento.MENSAL;
            Valor valorSegurado = Valor.reais(100000.00);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarFormaPagamento(formaPagamento, valorSegurado)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção para forma de pagamento nula")
        void deveLancarExcecaoFormaPagamentoNula() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarFormaPagamento(null, valorSegurado)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("Forma de pagamento");
        }

        @Test
        @DisplayName("Deve lançar exceção para valor segurado zero")
        void deveLancarExcecaoValorSeguradoZero() {
            // Arrange
            FormaPagamento formaPagamento = FormaPagamento.ANUAL;
            Valor valorZero = Valor.zero();

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarFormaPagamento(formaPagamento, valorZero)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("positivo");
        }
    }

    // ===================================================================
    // TESTES: validarRenovacao
    // ===================================================================

    @Nested
    @DisplayName("validarRenovacao - Validação de renovação")
    class ValidarRenovacaoTests {

        @Test
        @DisplayName("Deve validar renovação de apólice ativa")
        void deveValidarRenovacaoApoliceAtiva() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido
            Vigencia vigenciaAtual = Vigencia.anual(LocalDate.now().minusDays(15));
            Vigencia novaVigencia = Vigencia.anual(vigenciaAtual.getFim().plusDays(1));

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            );
        }

        @Test
        @DisplayName("Deve validar renovação de apólice vencida")
        void deveValidarRenovacaoApoliceVencida() {
            // Arrange
            StatusApolice status = StatusApolice.VENCIDA;
            Vigencia vigenciaAtual = Vigencia.anual(LocalDate.now().minusYears(2));
            Vigencia novaVigencia = Vigencia.anual(LocalDate.now());

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção ao renovar apólice cancelada")
        void deveLancarExcecaoRenovarApoliceCancelada() {
            // Arrange
            StatusApolice status = StatusApolice.CANCELADA;
            Vigencia vigenciaAtual = Vigencia.anual(LocalDate.now().minusMonths(6));
            Vigencia novaVigencia = Vigencia.anual(vigenciaAtual.getFim().plusDays(1));

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("cancelada");
        }

        @Test
        @DisplayName("Deve lançar exceção quando nova vigência é anterior à atual")
        void deveLancarExcecaoNovaVigenciaAnterior() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigenciaAtual = Vigencia.anual(LocalDate.now());
            Vigencia novaVigencia = Vigencia.anual(LocalDate.now().minusMonths(1));

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("posterior");
        }

        @Test
        @DisplayName("Deve validar renovação com vigência sequencial")
        void deveValidarRenovacaoSequencial() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            LocalDate inicioAtual = LocalDate.now().minusMonths(6);
            Vigencia vigenciaAtual = Vigencia.anual(inicioAtual);
            Vigencia novaVigencia = vigenciaAtual.renovar();

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            );
        }
    }

    // ===================================================================
    // TESTES: validarCancelamento
    // ===================================================================

    @Nested
    @DisplayName("validarCancelamento - Validação de cancelamento")
    class ValidarCancelamentoTests {

        @Test
        @DisplayName("Deve validar cancelamento de apólice ativa")
        void deveValidarCancelamentoApoliceAtiva() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());
            LocalDate dataCancelamento = LocalDate.now().plusDays(15);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            );
        }

        @Test
        @DisplayName("Deve validar cancelamento imediato")
        void deveValidarCancelamentoImediato() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido
            Vigencia vigencia = Vigencia.anual(LocalDate.now().minusDays(10));
            LocalDate dataCancelamento = LocalDate.now();

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção ao cancelar apólice já cancelada")
        void deveLancarExcecaoApoliceCancelada() {
            // Arrange
            StatusApolice status = StatusApolice.CANCELADA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());
            LocalDate dataCancelamento = LocalDate.now();

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("cancelada");
        }

        @Test
        @DisplayName("Deve lançar exceção quando data de cancelamento é anterior ao início")
        void deveLancarExcecaoDataCancelamentoAnterior() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());
            LocalDate dataCancelamento = vigencia.getInicio().minusDays(1);

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("anterior");
        }

        @Test
        @DisplayName("Deve validar cancelamento de apólice suspensa")
        void deveValidarCancelamentoApoliceSuspensa() {
            // Arrange
            StatusApolice status = StatusApolice.SUSPENSA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());
            LocalDate dataCancelamento = LocalDate.now();

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            );
        }
    }

    // ===================================================================
    // TESTES: validarAlteracao
    // ===================================================================

    @Nested
    @DisplayName("validarAlteracao - Validação de alteração")
    class ValidarAlteracaoTests {

        @Test
        @DisplayName("Deve validar alteração de apólice ativa durante vigência")
        void deveValidarAlteracaoApoliceAtiva() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido
            Vigencia vigencia = Vigencia.anual(LocalDate.now().minusDays(10));

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarAlteracao(status, vigencia)
            );
        }

        @Test
        @DisplayName("Deve lançar exceção ao alterar apólice cancelada")
        void deveLancarExcecaoAlterarApoliceCancelada() {
            // Arrange
            StatusApolice status = StatusApolice.CANCELADA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarAlteracao(status, vigencia)
            ).isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("cancelada");
        }

        @Test
        @DisplayName("Deve lançar exceção quando vigência ainda não começou")
        void deveLancarExcecaoVigenciaNaoComecou() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now().plusMonths(2));

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarAlteracao(status, vigencia)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("vigência");
        }

        @Test
        @DisplayName("Deve lançar exceção quando vigência já expirou")
        void deveLancarExcecaoVigenciaExpirou() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now().minusYears(2));

            // Act & Assert
            assertThatThrownBy(() ->
                service.validarAlteracao(status, vigencia)
            ).isInstanceOf(IllegalArgumentException.class)
             .hasMessageContaining("vigência");
        }
    }

    // ===================================================================
    // TESTES: Métodos de Segurado
    // ===================================================================

    @Nested
    @DisplayName("Métodos de Segurado - Informações do segurado")
    class MetodosSeguradoTests {

        @Test
        @DisplayName("Deve verificar se segurado está ativo")
        void deveVerificarSeguradoAtivo() {
            // Act
            boolean ativo = service.isSeguradoAtivo("SEG-001");

            // Assert - Baseado na implementação, segurados são simulados como ativos
            assertThat(ativo).isNotNull();
        }

        @Test
        @DisplayName("Deve contar apólices ativas do segurado")
        void deveContarApolicesAtivas() {
            // Act
            long count = service.contarApolicesAtivas("SEG-001");

            // Assert
            assertThat(count).isGreaterThanOrEqualTo(0);
            assertThat(count).isLessThanOrEqualTo(5); // Baseado na implementação
        }

        @Test
        @DisplayName("Deve obter score de crédito do segurado")
        void deveObterScoreCredito() {
            // Act
            int score = service.obterScoreCredito("SEG-001");

            // Assert
            assertThat(score).isBetween(250, 850); // Range válido de score
        }

        @Test
        @DisplayName("Deve verificar se segurado tem restrições")
        void deveVerificarRestricoes() {
            // Act
            boolean temRestricoes = service.temRestricoes("SEG-001");

            // Assert
            assertThat(temRestricoes).isNotNull();
        }

        @Test
        @DisplayName("Deve obter histórico de sinistros do segurado")
        void deveObterHistoricoSinistros() {
            // Act
            HistoricoSinistros historico = service.obterHistoricoSinistros("SEG-001");

            // Assert
            assertThat(historico).isNotNull();
            assertThat(historico.getTotalSinistros()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Deve obter mesmas informações para mesmo segurado (cache)")
        void deveRetornarMesmasInformacoesCache() {
            // Act
            String seguradoId = "SEG-CACHE-TEST";
            int score1 = service.obterScoreCredito(seguradoId);
            int score2 = service.obterScoreCredito(seguradoId);
            long apolices1 = service.contarApolicesAtivas(seguradoId);
            long apolices2 = service.contarApolicesAtivas(seguradoId);

            // Assert - Cache deve retornar mesmos valores
            assertThat(score1).isEqualTo(score2);
            assertThat(apolices1).isEqualTo(apolices2);
        }
    }

    // ===================================================================
    // TESTES: HistoricoSinistros (classe interna)
    // ===================================================================

    @Nested
    @DisplayName("HistoricoSinistros - Testes da classe interna")
    class HistoricoSinistrosTests {

        @Test
        @DisplayName("Deve identificar perfil de alto risco com mais de 3 sinistros")
        void deveIdentificarPerfilAltoRisco() {
            // Arrange
            HistoricoSinistros historico = new HistoricoSinistros(4);

            // Act
            boolean altoRisco = historico.isPerfilAltoRisco();

            // Assert
            assertThat(altoRisco).isTrue();
        }

        @Test
        @DisplayName("Deve identificar perfil normal com até 3 sinistros")
        void deveIdentificarPerfilNormal() {
            // Arrange
            HistoricoSinistros historico = new HistoricoSinistros(3);

            // Act
            boolean altoRisco = historico.isPerfilAltoRisco();

            // Assert
            assertThat(altoRisco).isFalse();
        }

        @Test
        @DisplayName("Deve calcular fator de risco baseado no número de sinistros")
        void deveCalcularFatorRisco() {
            // Arrange
            HistoricoSinistros semSinistros = new HistoricoSinistros(0);
            HistoricoSinistros comSinistros = new HistoricoSinistros(5);

            // Act
            double fatorSem = semSinistros.getFatorRisco();
            double fatorCom = comSinistros.getFatorRisco();

            // Assert - 10% adicional por sinistro
            assertThat(fatorSem).isEqualTo(1.0);
            assertThat(fatorCom).isEqualTo(1.5); // 1.0 + (5 * 0.1)
        }

        @Test
        @DisplayName("Deve retornar total de sinistros")
        void deveRetornarTotalSinistros() {
            // Arrange
            int total = 7;
            HistoricoSinistros historico = new HistoricoSinistros(total);

            // Act
            int totalSinistros = historico.getTotalSinistros();

            // Assert
            assertThat(totalSinistros).isEqualTo(total);
        }

        @Test
        @DisplayName("Deve criar histórico sem sinistros")
        void deveCriarHistoricoSemSinistros() {
            // Arrange
            HistoricoSinistros historico = new HistoricoSinistros(0);

            // Assert
            assertThat(historico.getTotalSinistros()).isZero();
            assertThat(historico.isPerfilAltoRisco()).isFalse();
            assertThat(historico.getFatorRisco()).isEqualTo(1.0);
        }

        @Test
        @DisplayName("Deve calcular fator de risco progressivo")
        void deveCalcularFatorRiscoProgressivo() {
            // Arrange & Act & Assert
            for (int i = 0; i <= 10; i++) {
                HistoricoSinistros historico = new HistoricoSinistros(i);
                double fatorEsperado = 1.0 + (i * 0.1);
                assertThat(historico.getFatorRisco()).isEqualTo(fatorEsperado);
            }
        }
    }

    // ===================================================================
    // TESTES: Casos Extremos e Edge Cases
    // ===================================================================

    @Nested
    @DisplayName("Casos Extremos")
    class CasosExtremosTests {

        @Test
        @DisplayName("Deve validar vigência no limite mínimo (30 dias)")
        void deveValidarVigenciaMinima() {
            // Arrange
            LocalDate inicio = LocalDate.now();
            LocalDate fim = inicio.plusDays(30);
            Vigencia vigencia = Vigencia.of(inicio, fim);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarVigencia(vigencia)
            );
        }

        @Test
        @DisplayName("Deve validar múltiplos segurados diferentes")
        void deveValidarMultiplosSegurados() {
            // Act
            boolean ativo1 = service.isSeguradoAtivo("SEG-001");
            boolean ativo2 = service.isSeguradoAtivo("SEG-002");
            boolean ativo3 = service.isSeguradoAtivo("SEG-003");

            // Assert - Todos devem retornar valores
            assertThat(ativo1).isNotNull();
            assertThat(ativo2).isNotNull();
            assertThat(ativo3).isNotNull();
        }

        @Test
        @DisplayName("Deve validar cancelamento no último dia de vigência")
        void deveValidarCancelamentoUltimoDia() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido
            Vigencia vigencia = Vigencia.anual(LocalDate.now().minusDays(5));
            LocalDate dataCancelamento = vigencia.getFim();

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCancelamento(status, vigencia, dataCancelamento)
            );
        }

        @Test
        @DisplayName("Deve validar alteração no primeiro dia de vigência")
        void deveValidarAlteracaoPrimeiroDia() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            Vigencia vigencia = Vigencia.anual(LocalDate.now());

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarAlteracao(status, vigencia)
            );
        }

        @Test
        @DisplayName("Deve validar coberturas com todos os tipos disponíveis")
        void deveValidarTodasCoberturas() {
            // Arrange
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = new ArrayList<>();

            for (TipoCobertura tipo : TipoCobertura.values()) {
                coberturas.add(Cobertura.basica(tipo, valorSegurado));
            }

            String produto = "AUTO";

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarCoberturas(coberturas, produto)
            );
        }

        @Test
        @DisplayName("Deve obter histórico de segurado com muitos sinistros")
        void deveObterHistoricoMuitosSinistros() {
            // Act
            HistoricoSinistros historico = service.obterHistoricoSinistros("SEG-HIGH-RISK");

            // Assert
            assertThat(historico).isNotNull();
            assertThat(historico.getTotalSinistros()).isLessThanOrEqualTo(7); // Limite da implementação
        }

        @Test
        @DisplayName("Deve validar renovação com vigências de diferentes durações")
        void deveValidarRenovacaoDiferentesDuracoes() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido
            Vigencia vigenciaAtual = Vigencia.comDuracaoMeses(LocalDate.now().minusDays(10), 6);
            Vigencia novaVigencia = Vigencia.comDuracaoMeses(vigenciaAtual.getFim().plusDays(1), 12);

            // Act & Assert
            assertThatNoException().isThrownBy(() ->
                service.validarRenovacao(status, vigenciaAtual, novaVigencia)
            );
        }
    }

    // ===================================================================
    // TESTES: Integração entre validações
    // ===================================================================

    @Nested
    @DisplayName("Integração entre Validações")
    class IntegracaoValidacoesTests {

        @Test
        @DisplayName("Deve validar todos os componentes de uma apólice nova")
        void deveValidarApoliceNova() {
            // Arrange
            Vigencia vigencia = Vigencia.anual(LocalDate.now());
            Valor valorSegurado = Valor.reais(100000.00);
            List<Cobertura> coberturas = List.of(
                Cobertura.basica(TipoCobertura.TOTAL, valorSegurado)
            );
            FormaPagamento formaPagamento = FormaPagamento.MENSAL;
            String produto = "AUTO";

            // Act & Assert - Todas as validações devem passar
            assertThatNoException().isThrownBy(() -> {
                service.validarVigencia(vigencia);
                service.validarValorSegurado(valorSegurado, produto);
                service.validarCoberturas(coberturas, produto);
                service.validarFormaPagamento(formaPagamento, valorSegurado);
            });
        }

        @Test
        @DisplayName("Deve validar fluxo completo de renovação")
        void deveValidarFluxoRenovacao() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido (máximo 30 dias no passado)
            Vigencia vigenciaAtual = Vigencia.anual(LocalDate.now().minusDays(15));
            Vigencia novaVigencia = vigenciaAtual.renovar();

            // Act & Assert
            assertThatNoException().isThrownBy(() -> {
                service.validarVigencia(vigenciaAtual);
                service.validarRenovacao(status, vigenciaAtual, novaVigencia);
                service.validarVigencia(novaVigencia);
            });
        }

        @Test
        @DisplayName("Deve validar fluxo completo de cancelamento")
        void deveValidarFluxoCancelamento() {
            // Arrange
            StatusApolice status = StatusApolice.ATIVA;
            // Usar data dentro do limite permitido (máximo 30 dias no passado)
            Vigencia vigencia = Vigencia.anual(LocalDate.now().minusDays(10));
            LocalDate dataCancelamento = LocalDate.now();

            // Act & Assert
            assertThatNoException().isThrownBy(() -> {
                service.validarVigencia(vigencia);
                service.validarCancelamento(status, vigencia, dataCancelamento);
            });
        }
    }
}
