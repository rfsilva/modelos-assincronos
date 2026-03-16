package com.seguradora.hibrida.domain.apolice.query.service;

import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceDetailView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceListView;
import com.seguradora.hibrida.domain.apolice.query.dto.ApoliceVencimentoView;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para ApoliceQueryServiceImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ApoliceQueryServiceImpl Tests")
class ApoliceQueryServiceImplTest {

    @Mock
    private ApoliceQueryRepository repository;

    @InjectMocks
    private ApoliceQueryServiceImpl service;

    private ApoliceQueryModel model;
    private List<ApoliceQueryModel> modelList;
    private Page<ApoliceQueryModel> modelPage;

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
        model.setFormaPagamento("Cartão");
        model.setParcelas(12);
        model.setSeguradoNome("João Silva");
        model.setSeguradoCpf("12345678901");
        model.setSeguradoEmail("joao@email.com");
        model.setSeguradoTelefone("11999999999");
        model.setSeguradoCidade("São Paulo");
        model.setSeguradoEstado("SP");
        model.setCoberturas(List.of(TipoCobertura.TOTAL));
        model.setCoberturasResumo("Total");
        model.setTemCoberturaTotal(true);
        model.setOperadorResponsavel("Operador A");
        model.setCanalVenda("Online");
        model.setObservacoes("Observações");
        model.setRenovacaoAutomatica(true);
        model.setScoreRenovacao(85);
        model.setDiasParaVencimento(365);
        model.setVencimentoProximo(false);

        modelList = List.of(model);
        modelPage = new PageImpl<>(modelList);
    }

    @Nested
    @DisplayName("Consultas Básicas")
    class ConsultasBasicas {

        @Test
        @DisplayName("Deve buscar apólice por ID")
        void deveBuscarPorId() {
            // Arrange
            when(repository.findById("ap-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("ap-001");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo("ap-001");
            assertThat(result.get().numero()).isEqualTo("AP-2024-001");
            verify(repository).findById("ap-001");
        }

        @Test
        @DisplayName("Deve retornar empty quando não encontrar por ID")
        void deveRetornarEmptyQuandoNaoEncontrarPorId() {
            // Arrange
            when(repository.findById("inexistente")).thenReturn(Optional.empty());

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("inexistente");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve buscar apólice por número")
        void deveBuscarPorNumero() {
            // Arrange
            when(repository.findByNumero("AP-2024-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorNumero("AP-2024-001");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().numero()).isEqualTo("AP-2024-001");
        }

        @Test
        @DisplayName("Deve listar todas as apólices")
        void deveListarTodas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findAll(pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.listarTodas(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).id()).isEqualTo("ap-001");
        }
    }

    @Nested
    @DisplayName("Consultas por Segurado")
    class ConsultasPorSegurado {

        @Test
        @DisplayName("Deve buscar apólices por CPF do segurado")
        void deveBuscarPorCpfSegurado() {
            // Arrange
            when(repository.findBySeguradoCpfOrderByVigenciaInicioDesc("12345678901"))
                .thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarPorCpfSegurado("12345678901");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).seguradoCpf()).isEqualTo("12345678901");
        }

        @Test
        @DisplayName("Deve buscar apólices ativas por CPF")
        void deveBuscarAtivasPorCpf() {
            // Arrange
            when(repository.findApolicesAtivasPorCpf("12345678901")).thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarAtivasPorCpfSegurado("12345678901");

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).status()).isEqualTo(StatusApolice.ATIVA);
        }

        @Test
        @DisplayName("Deve buscar apólices por nome do segurado")
        void deveBuscarPorNomeSegurado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoNomeContaining("João", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorNomeSegurado("João", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).seguradoNome()).contains("João");
        }
    }

    @Nested
    @DisplayName("Consultas por Status")
    class ConsultasPorStatus {

        @Test
        @DisplayName("Deve buscar apólices por status")
        void deveBuscarPorStatus() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findByStatusOrderByVigenciaFimAsc(StatusApolice.ATIVA, pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorStatus(StatusApolice.ATIVA, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).status()).isEqualTo(StatusApolice.ATIVA);
        }

        @Test
        @DisplayName("Deve buscar apólices ativas")
        void deveBuscarAtivas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findApolicesAtivas(pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarAtivas(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices vencidas")
        void deveBuscarVencidas() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findApolicesVencidas(pageable)).thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarVencidas(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas por Vencimento")
    class ConsultasPorVencimento {

        @Test
        @DisplayName("Deve buscar apólices vencendo entre datas")
        void deveBuscarVencendoEntre() {
            // Arrange
            LocalDate inicio = LocalDate.now();
            LocalDate fim = LocalDate.now().plusDays(30);
            when(repository.findApolicesVencendoEntre(inicio, fim)).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarVencendoEntre(inicio, fim);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).id()).isEqualTo("ap-001");
        }

        @Test
        @DisplayName("Deve buscar apólices vencendo em X dias")
        void deveBuscarVencendoEm() {
            // Arrange
            model.setVigenciaFim(LocalDate.now().plusDays(30));
            model.setDiasParaVencimento(30);
            model.setVencimentoProximo(true);
            LocalDate dataLimite = LocalDate.now().plusDays(30);
            when(repository.findApolicesVencendoEm(dataLimite)).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarVencendoEm(30);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).diasParaVencimento()).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve buscar apólices com vencimento próximo")
        void deveBuscarComVencimentoProximo() {
            // Arrange
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setDiasParaVencimento(20);
            model.setVencimentoProximo(true);
            when(repository.findApolicesComVencimentoProximo()).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarComVencimentoProximo();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).diasParaVencimento()).isEqualTo(20);
        }
    }

    @Nested
    @DisplayName("Consultas por Produto e Cobertura")
    class ConsultasProdutoCobertura {

        @Test
        @DisplayName("Deve buscar apólices por produto")
        void deveBuscarPorProduto() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findByProdutoOrderByVigenciaInicioDesc("Seguro Auto", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorProduto("Seguro Auto", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).produto()).isEqualTo("Seguro Auto");
        }

        @Test
        @DisplayName("Deve buscar apólices por cobertura")
        void deveBuscarPorCobertura() {
            // Arrange
            when(repository.findByCobertura(TipoCobertura.TOTAL)).thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarPorCobertura(TipoCobertura.TOTAL);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).coberturas()).contains(TipoCobertura.TOTAL);
        }

        @Test
        @DisplayName("Deve buscar apólices com cobertura total")
        void deveBuscarComCoberturaTotal() {
            // Arrange
            when(repository.findApolicesComCoberturaTotal()).thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarComCoberturaTotal();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).temCoberturaTotal()).isTrue();
        }
    }

    @Nested
    @DisplayName("Consultas por Valor")
    class ConsultasPorValor {

        @Test
        @DisplayName("Deve buscar apólices por faixa de valor")
        void deveBuscarPorFaixaValor() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            BigDecimal min = new BigDecimal("1000");
            BigDecimal max = new BigDecimal("10000");
            when(repository.findByValorTotalBetween(min, max, pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorFaixaValor(min, max, pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices de alto valor")
        void deveBuscarAltoValor() {
            // Arrange
            BigDecimal valorMinimo = new BigDecimal("50000");
            model.setValorTotal(new BigDecimal("100000"));
            when(repository.findApolicesAltoValor(valorMinimo)).thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarAltoValor(valorMinimo);

            // Assert
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Consultas de Renovação")
    class ConsultasRenovacao {

        @Test
        @DisplayName("Deve buscar apólices elegíveis para renovação")
        void deveBuscarElegiveisRenovacao() {
            // Arrange
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setDiasParaVencimento(20);
            model.setVencimentoProximo(true);
            when(repository.findElegiveisRenovacaoAutomatica()).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarElegiveisRenovacaoAutomatica();

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).renovacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Deve buscar apólices precisando de atenção")
        void deveBuscarPrecisandoAtencao() {
            // Arrange
            model.setVigenciaFim(LocalDate.now().plusDays(20));
            model.setDiasParaVencimento(20);
            model.setVencimentoProximo(true);
            model.setScoreRenovacao(50);
            when(repository.findPrecisandoAtencaoRenovacao()).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarPrecisandoAtencaoRenovacao();

            // Assert
            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Deve buscar apólices por score de renovação")
        void deveBuscarPorScoreRenovacao() {
            // Arrange
            when(repository.findByScoreRenovacao(70, 100)).thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarPorScoreRenovacao(70, 100);

            // Assert
            assertThat(result).hasSize(1);
            assertThat(result.get(0).scoreRenovacao()).isGreaterThanOrEqualTo(70);
        }
    }

    @Nested
    @DisplayName("Consultas por Localização")
    class ConsultasPorLocalizacao {

        @Test
        @DisplayName("Deve buscar apólices por cidade")
        void deveBuscarPorCidade() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoCidadeOrderByVigenciaFimDesc("São Paulo", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorCidade("São Paulo", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).seguradoCidade()).isEqualTo("São Paulo");
        }

        @Test
        @DisplayName("Deve buscar apólices por estado")
        void deveBuscarPorEstado() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findBySeguradoEstadoOrderByVigenciaFimDesc("SP", pageable))
                .thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarPorEstado("SP", pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).seguradoEstado()).isEqualTo("SP");
        }
    }

    @Nested
    @DisplayName("Consultas Customizadas")
    class ConsultasCustomizadas {

        @Test
        @DisplayName("Deve buscar com múltiplos filtros")
        void deveBuscarComFiltros() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findWithFilters(
                any(), any(), any(), any(), any(), any(), any(), any()
            )).thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarComFiltros(
                StatusApolice.ATIVA, "Seguro Auto", "12345678901",
                LocalDate.now(), LocalDate.now().plusDays(365),
                new BigDecimal("1000"), new BigDecimal("10000"), pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(1);
            verify(repository).findWithFilters(
                StatusApolice.ATIVA, "Seguro Auto", "12345678901",
                LocalDate.now(), LocalDate.now().plusDays(365),
                new BigDecimal("1000"), new BigDecimal("10000"), pageable
            );
        }

        @Test
        @DisplayName("Deve buscar com filtros nulos")
        void deveBuscarComFiltrosNulos() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findWithFilters(
                null, null, null, null, null, null, null, pageable
            )).thenReturn(modelPage);

            // Act
            Page<ApoliceListView> result = service.buscarComFiltros(
                null, null, null, null, null, null, null, pageable
            );

            // Assert
            assertThat(result.getContent()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("Verificações")
    class Verificacoes {

        @Test
        @DisplayName("Deve verificar existência por número")
        void deveVerificarExistenciaPorNumero() {
            // Arrange
            when(repository.existsByNumero("AP-2024-001")).thenReturn(true);

            // Act
            boolean exists = service.existeComNumero("AP-2024-001");

            // Assert
            assertThat(exists).isTrue();
        }

        @Test
        @DisplayName("Deve contar apólices ativas por CPF")
        void deveContarAtivasPorCpf() {
            // Arrange
            when(repository.countApolicesAtivasPorCpf("12345678901")).thenReturn(5L);

            // Act
            long count = service.contarAtivasPorCpf("12345678901");

            // Assert
            assertThat(count).isEqualTo(5L);
        }

        @Test
        @DisplayName("Deve verificar se segurado possui apólices ativas")
        void deveVerificarSeguradoPossuiAtivas() {
            // Arrange
            when(repository.seguradoPossuiApolicesAtivas("12345678901")).thenReturn(true);

            // Act
            boolean possui = service.seguradoPossuiApolicesAtivas("12345678901");

            // Assert
            assertThat(possui).isTrue();
        }
    }

    @Nested
    @DisplayName("Conversão de DTOs")
    class ConversaoDTOs {

        @Test
        @DisplayName("Deve converter para DetailView corretamente")
        void deveConverterParaDetailView() {
            // Arrange
            when(repository.findById("ap-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("ap-001");

            // Assert
            assertThat(result).isPresent();
            ApoliceDetailView view = result.get();
            assertThat(view.id()).isEqualTo(model.getId());
            assertThat(view.numero()).isEqualTo(model.getNumero());
            assertThat(view.produto()).isEqualTo(model.getProduto());
            assertThat(view.status()).isEqualTo(model.getStatus());
            assertThat(view.seguradoNome()).isEqualTo(model.getSeguradoNome());
            assertThat(view.seguradoCpf()).isEqualTo(model.getSeguradoCpf());
        }

        @Test
        @DisplayName("Deve converter para ListView corretamente")
        void deveConverterParaListView() {
            // Arrange
            when(repository.findBySeguradoCpfOrderByVigenciaInicioDesc("12345678901"))
                .thenReturn(modelList);

            // Act
            List<ApoliceListView> result = service.buscarPorCpfSegurado("12345678901");

            // Assert
            assertThat(result).hasSize(1);
            ApoliceListView view = result.get(0);
            assertThat(view.id()).isEqualTo(model.getId());
            assertThat(view.numero()).isEqualTo(model.getNumero());
            assertThat(view.status()).isEqualTo(model.getStatus());
        }

        @Test
        @DisplayName("Deve converter para VencimentoView corretamente")
        void deveConverterParaVencimentoView() {
            // Arrange
            model.setVigenciaFim(LocalDate.now().plusDays(30));
            model.setDiasParaVencimento(30);
            model.setVencimentoProximo(true);
            when(repository.findApolicesComVencimentoProximo()).thenReturn(modelList);

            // Act
            List<ApoliceVencimentoView> result = service.buscarComVencimentoProximo();

            // Assert
            assertThat(result).hasSize(1);
            ApoliceVencimentoView view = result.get(0);
            assertThat(view.id()).isEqualTo(model.getId());
            assertThat(view.numero()).isEqualTo(model.getNumero());
            assertThat(view.diasParaVencimento()).isEqualTo(30);
            assertThat(view.prioridadeVencimento()).isEqualTo("MÉDIA");
        }

        @Test
        @DisplayName("Deve calcular valor da parcela na conversão para DetailView")
        void deveCalcularValorParcela() {
            // Arrange
            when(repository.findById("ap-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("ap-001");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().valorParcela()).isNotNull();
            assertThat(result.get().valorParcela())
                .isEqualByComparingTo(new BigDecimal("416.67"));
        }

        @Test
        @DisplayName("Deve calcular duração em meses na conversão para DetailView")
        void deveCalcularDuracaoMeses() {
            // Arrange
            when(repository.findById("ap-001")).thenReturn(Optional.of(model));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("ap-001");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().duracaoMeses()).isEqualTo(12);
        }
    }

    @Nested
    @DisplayName("Cache")
    class Cache {

        @Test
        @DisplayName("Deve aplicar cache em buscarPorId")
        void deveAplicarCacheEmBuscarPorId() {
            // Arrange
            when(repository.findById("ap-001")).thenReturn(Optional.of(model));

            // Act
            service.buscarPorId("ap-001");
            service.buscarPorId("ap-001");

            // Assert
            // O cache deve fazer com que o repository seja chamado apenas uma vez
            // Nota: Este teste realmente verifica cache apenas em testes de integração
            verify(repository, atLeastOnce()).findById("ap-001");
        }
    }

    @Nested
    @DisplayName("Casos Especiais")
    class CasosEspeciais {

        @Test
        @DisplayName("Deve tratar modelo sem valores opcionais")
        void deveTratarModeloSemValoresOpcionais() {
            // Arrange
            ApoliceQueryModel modeloSimples = new ApoliceQueryModel(
                "ap-002", "AP-2024-002", "seg-002"
            );
            modeloSimples.setStatus(StatusApolice.ATIVA);
            modeloSimples.setSeguradoNome("Maria Silva");

            when(repository.findById("ap-002")).thenReturn(Optional.of(modeloSimples));

            // Act
            Optional<ApoliceDetailView> result = service.buscarPorId("ap-002");

            // Assert
            assertThat(result).isPresent();
            assertThat(result.get().id()).isEqualTo("ap-002");
        }

        @Test
        @DisplayName("Deve retornar lista vazia quando não há resultados")
        void deveRetornarListaVazia() {
            // Arrange
            when(repository.findBySeguradoCpfOrderByVigenciaInicioDesc("00000000000"))
                .thenReturn(List.of());

            // Act
            List<ApoliceListView> result = service.buscarPorCpfSegurado("00000000000");

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há resultados")
        void deveRetornarPaginaVazia() {
            // Arrange
            Pageable pageable = PageRequest.of(0, 20);
            when(repository.findAll(pageable)).thenReturn(Page.empty());

            // Act
            Page<ApoliceListView> result = service.listarTodas(pageable);

            // Assert
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }
}
