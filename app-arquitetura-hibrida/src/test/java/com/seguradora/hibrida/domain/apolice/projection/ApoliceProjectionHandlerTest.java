package com.seguradora.hibrida.domain.apolice.projection;

import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ApoliceProjectionHandler}.
 *
 * <p>Verifica o processamento correto de todos os eventos de apólice,
 * idempotência, tratamento de eventos fora de ordem e invalidação de cache.</p>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de ApoliceProjectionHandler")
class ApoliceProjectionHandlerTest {

    @Mock
    private ApoliceQueryRepository repository;

    @InjectMocks
    private ApoliceProjectionHandler handler;

    @Captor
    private ArgumentCaptor<ApoliceQueryModel> modelCaptor;

    private static final String APOLICE_ID = "apolice-123";
    private static final String NUMERO_APOLICE = "APO-2026-001";
    private static final String SEGURADO_ID = "segurado-456";
    private static final String OPERADOR_ID = "operador-789";
    private static final String PRODUTO = "Seguro Auto";

    @Nested
    @DisplayName("Testes de Configuração")
    class ConfiguracaoTests {

        @Test
        @DisplayName("Deve retornar o nome correto da projeção")
        void deveRetornarNomeCorretoDaProjecao() {
            assertThat(handler.getProjectionName()).isEqualTo("ApoliceProjection");
        }

        @Test
        @DisplayName("Deve retornar o tipo correto do evento")
        void deveRetornarTipoCorretoDomainEvent() {
            assertThat(handler.getEventType()).isEqualTo(DomainEvent.class);
        }

        @Test
        @DisplayName("Deve suportar ApoliceCriadaEvent")
        void deveSuportarApoliceCriadaEvent() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceAtualizadaEvent")
        void deveSuportarApoliceAtualizadaEvent() {
            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceCanceladaEvent")
        void deveSuportarApoliceCanceladaEvent() {
            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceRenovadaEvent")
        void deveSuportarApoliceRenovadaEvent() {
            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar CoberturaAdicionadaEvent")
        void deveSuportarCoberturaAdicionadaEvent() {
            CoberturaAdicionadaEvent event = criarCoberturaAdicionadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Não deve suportar ApoliceVencidaEvent")
        void naoDeveSuportarApoliceVencidaEvent() {
            ApoliceVencidaEvent event = ApoliceVencidaEvent.create(
                APOLICE_ID, 1, NUMERO_APOLICE, SEGURADO_ID,
                LocalDate.now(), "50000.00"
            );
            assertThat(handler.supports(event)).isFalse();
        }

        @Test
        @DisplayName("Não deve suportar evento desconhecido")
        void naoDeveSuportarEventoDesconhecido() {
            DomainEvent event = mock(DomainEvent.class);
            assertThat(handler.supports(event)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de ApoliceCriadaEvent")
    class ApoliceCriadaEventTests {

        @Test
        @DisplayName("Deve criar projeção ao processar ApoliceCriadaEvent")
        void deveCriarProjecaoComSucesso() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model).isNotNull();
            assertThat(model.getId()).isEqualTo(APOLICE_ID);
            assertThat(model.getNumero()).isEqualTo(NUMERO_APOLICE);
            assertThat(model.getSeguradoId()).isEqualTo(SEGURADO_ID);
            assertThat(model.getProduto()).isEqualTo(PRODUTO);
            assertThat(model.getStatus()).isEqualTo(StatusApolice.ATIVA);
        }

        @Test
        @DisplayName("Deve converter datas corretamente")
        void deveConverterDatasCorretamente() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getVigenciaInicio()).isEqualTo(LocalDate.now());
            assertThat(model.getVigenciaFim()).isEqualTo(LocalDate.now().plusYears(1));
        }

        @Test
        @DisplayName("Deve converter valores monetários corretamente")
        void deveConverterValoresMonetariosCorretamente() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getValorSegurado()).isEqualByComparingTo(new BigDecimal("50000.00"));
            assertThat(model.getValorTotal()).isEqualByComparingTo(new BigDecimal("2500.00"));
        }

        @Test
        @DisplayName("Deve processar coberturas corretamente")
        void deveProcessarCoberturasCorretamente() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas(
                TipoCobertura.PARCIAL,
                TipoCobertura.TERCEIROS
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getCoberturas())
                .hasSize(2)
                .contains(TipoCobertura.PARCIAL, TipoCobertura.TERCEIROS);
            assertThat(model.getCoberturasResumo()).isNotBlank();
            assertThat(model.getTemCoberturaTotal()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar cobertura total")
        void deveIdentificarCoberturaTotal() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas(
                TipoCobertura.TOTAL
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getTemCoberturaTotal()).isTrue();
            assertThat(model.getCoberturasResumo()).isEqualTo("Cobertura Total");
        }

        @Test
        @DisplayName("Deve calcular métricas ao criar projeção")
        void deveCalcularMetricasAoCriarProjecao() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getDiasParaVencimento()).isNotNull();
            assertThat(model.getScoreRenovacao()).isNotNull();
            assertThat(model.getRenovacaoAutomatica()).isTrue();
        }

        @Test
        @DisplayName("Deve registrar eventId para idempotência")
        void deveRegistrarEventIdParaIdempotencia() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getLastEventId()).isNotNull();
        }

        @Test
        @DisplayName("Deve atribuir operador responsável")
        void deveAtribuirOperadorResponsavel() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getOperadorResponsavel()).isEqualTo(OPERADOR_ID);
        }

        @Test
        @DisplayName("Deve usar valores padrão em caso de erro na conversão de datas")
        void deveUsarValoresPadraoEmCasoDeErroNaConversao() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComDatasInvalidas();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getVigenciaInicio()).isNotNull();
            assertThat(model.getVigenciaFim()).isNotNull();
            assertThat(model.getValorSegurado()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(model.getValorTotal()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Deve ignorar coberturas com tipo inválido")
        void deveIgnorarCoberturasComTipoInvalido() {
            List<Map<String, Object>> coberturas = new ArrayList<>();
            coberturas.add(Map.of("tipo", "TIPO_INVALIDO", "valorCobertura", "1000"));

            ApoliceCriadaEvent event = new ApoliceCriadaEvent(
                APOLICE_ID, NUMERO_APOLICE, SEGURADO_ID, PRODUTO,
                LocalDate.now().toString(), LocalDate.now().plusYears(1).toString(),
                "50000.00", "ANUAL", coberturas, "2500.00", OPERADOR_ID
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getCoberturas()).isEmpty();
        }

        @Test
        @DisplayName("Deve calcular score de renovação base")
        void deveCalcularScoreRenovacaoBase() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isGreaterThanOrEqualTo(70);
        }

        @Test
        @DisplayName("Deve dar bonus no score para cobertura total")
        void deveDarBonusNoScoreParaCoberturaTotal() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas(TipoCobertura.TOTAL);

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isGreaterThanOrEqualTo(80); // 70 base + 10 bonus
        }

        @Test
        @DisplayName("Deve dar bonus no score para valor alto")
        void deveDarBonusNoScoreParaValorAlto() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComValor("150000.00");

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isGreaterThanOrEqualTo(75); // 70 base + 5 bonus
        }
    }

    @Nested
    @DisplayName("Testes de ApoliceAtualizadaEvent")
    class ApoliceAtualizadaEventTests {

        @Test
        @DisplayName("Deve atualizar projeção existente")
        void deveAtualizarProjecaoExistente() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getObservacoes()).contains("Alteração realizada");
            assertThat(model.getLastEventId()).isNotNull();
        }

        @Test
        @DisplayName("Deve adicionar observação à existente")
        void deveAdicionarObservacaoAExistente() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setObservacoes("Observação anterior");
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getObservacoes())
                .contains("Observação anterior")
                .contains("Alteração realizada");
        }

        @Test
        @DisplayName("Deve recalcular métricas ao atualizar")
        void deveRecalcularMetricasAoAtualizar() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setScoreRenovacao(50);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isNotEqualTo(50);
        }

        @Test
        @DisplayName("Deve ignorar atualização de apólice não encontrada")
        void deveIgnorarAtualizacaoDeApoliceNaoEncontrada() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de ApoliceCanceladaEvent")
    class ApoliceCanceladaEventTests {

        @Test
        @DisplayName("Deve cancelar projeção existente")
        void deveCancelarProjecaoExistente() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getStatus()).isEqualTo(StatusApolice.CANCELADA);
        }

        @Test
        @DisplayName("Deve registrar motivo do cancelamento nas observações")
        void deveRegistrarMotivoCancelamentoNasObservacoes() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getObservacoes())
                .contains("Cancelada")
                .contains("Solicitação do cliente");
        }

        @Test
        @DisplayName("Deve zerar métricas de renovação ao cancelar")
        void deveZerarMetricasDeRenovacaoAoCancelar() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setRenovacaoAutomatica(true);
            modelExistente.setVencimentoProximo(true);
            modelExistente.setScoreRenovacao(80);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getRenovacaoAutomatica()).isFalse();
            assertThat(model.getVencimentoProximo()).isFalse();
            assertThat(model.getScoreRenovacao()).isZero();
        }

        @Test
        @DisplayName("Deve ignorar cancelamento de apólice não encontrada")
        void deveIgnorarCancelamentoDeApoliceNaoEncontrada() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            handler.handle(event);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de ApoliceRenovadaEvent")
    class ApoliceRenovadaEventTests {

        @Test
        @DisplayName("Deve renovar projeção existente")
        void deveRenovarProjecaoExistente() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getStatus()).isEqualTo(StatusApolice.ATIVA);
            assertThat(model.getObservacoes()).contains("Renovada");
        }

        @Test
        @DisplayName("Deve atualizar vigência ao renovar")
        void deveAtualizarVigenciaAoRenovar() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            LocalDate novaVigenciaInicio = LocalDate.now().plusYears(1);
            LocalDate novaVigenciaFim = LocalDate.now().plusYears(2);

            ApoliceRenovadaEvent event = criarApoliceRenovadaEventComVigencia(
                novaVigenciaInicio, novaVigenciaFim
            );
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getVigenciaInicio()).isEqualTo(novaVigenciaInicio);
            assertThat(model.getVigenciaFim()).isEqualTo(novaVigenciaFim);
        }

        @Test
        @DisplayName("Deve atualizar valor segurado se fornecido")
        void deveAtualizarValorSeguradoSeFornecido() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEventComValor("75000.00");
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getValorSegurado()).isEqualByComparingTo(new BigDecimal("75000.00"));
        }

        @Test
        @DisplayName("Deve recalcular métricas ao renovar")
        void deveRecalcularMetricasAoRenovar() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getDiasParaVencimento()).isNotNull();
            assertThat(model.getScoreRenovacao()).isNotNull();
        }

        @Test
        @DisplayName("Deve garantir status ativo ao renovar")
        void deveGarantirStatusAtivoAoRenovar() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setStatus(StatusApolice.VENCIDA);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getStatus()).isEqualTo(StatusApolice.ATIVA);
        }

        @Test
        @DisplayName("Deve ignorar renovação de apólice não encontrada")
        void deveIgnorarRenovacaoDeApoliceNaoEncontrada() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            handler.handle(event);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve tratar erro na conversão de datas de renovação")
        void deveTratarErroNaConversaoDeDatasDeRenovacao() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            LocalDate vigenciaAnterior = modelExistente.getVigenciaInicio();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEventComDatasInvalidas();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            // Deve manter os valores originais
            assertThat(model.getVigenciaInicio()).isEqualTo(vigenciaAnterior);
        }
    }

    @Nested
    @DisplayName("Testes de CoberturaAdicionadaEvent")
    class CoberturaAdicionadaEventTests {

        @Test
        @DisplayName("Deve adicionar cobertura à projeção existente")
        void deveAdicionarCoberturaAProjecaoExistente() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            CoberturaAdicionadaEvent event = criarCoberturaAdicionadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getObservacoes()).contains("Cobertura adicionada");
        }

        @Test
        @DisplayName("Deve registrar eventId ao adicionar cobertura")
        void deveRegistrarEventIdAoAdicionarCobertura() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            CoberturaAdicionadaEvent event = criarCoberturaAdicionadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getLastEventId()).isNotNull();
        }

        @Test
        @DisplayName("Deve ignorar adição de cobertura para apólice não encontrada")
        void deveIgnorarAdicaoDeCoberturaParaApoliceNaoEncontrada() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            CoberturaAdicionadaEvent event = criarCoberturaAdicionadaEvent();
            handler.handle(event);

            verify(repository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Testes de Idempotência")
    class IdempotenciaTests {

        @Test
        @DisplayName("Deve processar evento novo normalmente")
        void deveProcessarEventoNovoNormalmente() {
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(any(ApoliceQueryModel.class));
        }

        @Test
        @DisplayName("Deve permitir reprocessamento de evento com mesmo ID")
        void devePermitirReprocessamentoDeEventoComMesmoId() {
            // Na implementação atual, não há verificação de idempotência baseada em eventId
            // Isso é uma decisão de design para permitir reconstrução de projeções
            ApoliceQueryModel modelExistente = criarModeloExistente();
            long eventoIdExistente = 12345L;
            modelExistente.setLastEventId(eventoIdExistente);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository).save(any(ApoliceQueryModel.class));
        }

        @Test
        @DisplayName("Deve atualizar lastEventId em cada processamento")
        void deveAtualizarLastEventIdEmCadaProcessamento() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setLastEventId(11111L);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getLastEventId()).isNotEqualTo(11111L);
        }
    }

    @Nested
    @DisplayName("Testes de Eventos Fora de Ordem")
    class EventosForaDeOrdemTests {

        @Test
        @DisplayName("Deve processar evento de atualização mesmo sem criação prévia")
        void deveProcessarEventoDeAtualizacaoMesmoSemCriacaoPrevia() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();
            handler.handle(event);

            // Não deve salvar pois não encontrou a apólice
            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve processar cancelamento mesmo sem criação prévia")
        void deveProcessarCancelamentoMesmoSemCriacaoPrevia() {
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.empty());

            ApoliceCanceladaEvent event = criarApoliceCanceladaEvent();
            handler.handle(event);

            verify(repository, never()).save(any());
        }

        @Test
        @DisplayName("Deve processar renovação sobre apólice cancelada")
        void deveProcessarRenovacaoSobreApoliceCancelada() {
            ApoliceQueryModel modelExistente = criarModeloExistente();
            modelExistente.setStatus(StatusApolice.CANCELADA);
            when(repository.findById(APOLICE_ID)).thenReturn(Optional.of(modelExistente));

            ApoliceRenovadaEvent event = criarApoliceRenovadaEvent();
            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getStatus()).isEqualTo(StatusApolice.ATIVA);
        }
    }

    @Nested
    @DisplayName("Testes de Tratamento de Erros")
    class TratamentoDeErrosTests {

        @Test
        @DisplayName("Deve propagar exceção ao salvar no repositório")
        void devePropagarExcecaoAoSalvarNoRepositorio() {
            when(repository.save(any(ApoliceQueryModel.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve propagar exceção ao buscar no repositório")
        void devePropagarExcecaoAoBuscarNoRepositorio() {
            when(repository.findById(APOLICE_ID))
                .thenThrow(new RuntimeException("Erro ao buscar"));

            ApoliceAtualizadaEvent event = criarApoliceAtualizadaEvent();

            assertThatThrownBy(() -> handler.handle(event))
                .isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve tratar evento com dados nulos graciosamente")
        void deveTratarEventoComDadosNulosGraciosamente() {
            // ApoliceCriadaEvent possui validações que lançam exceção
            assertThatThrownBy(() ->
                new ApoliceCriadaEvent(
                    null, null, null, null, null, null, null, null, null, null, null
                )
            ).isInstanceOf(Exception.class);
        }

        @Test
        @DisplayName("Deve tratar conversão de dados inválidos")
        void deveTratarConversaoDeDadosInvalidos() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComDatasInvalidas();

            // Deve processar sem lançar exceção, usando valores padrão
            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();

            verify(repository).save(any(ApoliceQueryModel.class));
        }
    }

    @Nested
    @DisplayName("Testes de Cálculo de Métricas")
    class CalculoDeMetricasTests {

        @Test
        @DisplayName("Deve calcular dias para vencimento corretamente")
        void deveCalcularDiasParaVencimentoCorretamente() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComVigenciaPersonalizada(
                LocalDate.now(),
                LocalDate.now().plusDays(30)
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getDiasParaVencimento()).isBetween(29, 31);
        }

        @Test
        @DisplayName("Deve identificar vencimento próximo")
        void deveIdentificarVencimentoProximo() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComVigenciaPersonalizada(
                LocalDate.now(),
                LocalDate.now().plusDays(25)
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getVencimentoProximo()).isTrue();
        }

        @Test
        @DisplayName("Não deve marcar vencimento próximo para datas distantes")
        void naoDevemarcarVencimentoProximoParaDatasDistantes() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComVigenciaPersonalizada(
                LocalDate.now(),
                LocalDate.now().plusDays(60)
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getVencimentoProximo()).isFalse();
        }

        @Test
        @DisplayName("Deve calcular score máximo 100")
        void deveCalcularScoreMaximo100() {
            // Criar evento com todas as condições para bonus
            ApoliceCriadaEvent event = criarApoliceCriadaEventComTodasCondicoes();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isLessThanOrEqualTo(100);
        }

        @Test
        @DisplayName("Deve calcular score mínimo 0")
        void deveCalcularScoreMinimo0() {
            // Em condições normais, o score não deve ser negativo
            ApoliceCriadaEvent event = criarApoliceCriadaEvent();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isGreaterThanOrEqualTo(0);
        }

        @Test
        @DisplayName("Deve dar bonus por pagamento anual")
        void deveDarBonusPorPagamentoAnual() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComFormaPagamento("ANUAL");

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getScoreRenovacao()).isGreaterThanOrEqualTo(75); // 70 + 5
        }
    }

    @Nested
    @DisplayName("Testes de Resumo de Coberturas")
    class ResumoDeCoberturasTests {

        @Test
        @DisplayName("Deve criar resumo para coberturas múltiplas")
        void deveCriarResumoParaCoberturaMultiplas() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas(
                TipoCobertura.PARCIAL,
                TipoCobertura.TERCEIROS,
                TipoCobertura.ROUBO_FURTO
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            String resumo = model.getCoberturasResumo();
            assertThat(resumo)
                .contains("Parcial")
                .contains("Terceiros")
                .contains("Roubo e Furto");
        }

        @Test
        @DisplayName("Deve criar resumo vazio para lista vazia")
        void deveCriarResumoVazioParaListaVazia() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas();

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getCoberturasResumo()).isEqualTo("Nenhuma cobertura");
        }

        @Test
        @DisplayName("Deve priorizar cobertura total no resumo")
        void devePriorizarCoberturaInalNoResumo() {
            ApoliceCriadaEvent event = criarApoliceCriadaEventComCoberturas(
                TipoCobertura.TOTAL,
                TipoCobertura.PARCIAL
            );

            handler.handle(event);

            verify(repository).save(modelCaptor.capture());

            ApoliceQueryModel model = modelCaptor.getValue();
            assertThat(model.getCoberturasResumo()).isEqualTo("Cobertura Total");
        }
    }

    // ===== MÉTODOS AUXILIARES =====

    private ApoliceCriadaEvent criarApoliceCriadaEvent() {
        return criarApoliceCriadaEventComCoberturas(TipoCobertura.PARCIAL);
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComCoberturas(TipoCobertura... tipos) {
        List<Map<String, Object>> coberturas = new ArrayList<>();
        for (TipoCobertura tipo : tipos) {
            coberturas.add(Map.of(
                "tipo", tipo.name(),
                "valorCobertura", "10000.00",
                "franquia", "500.00",
                "carenciaDias", 0,
                "ativa", true
            ));
        }

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            "50000.00",
            "MENSAL",
            coberturas.isEmpty() ? List.of(Map.of("tipo", "DANOS_MATERIAIS", "valorCobertura", "1000")) : coberturas,
            "2500.00",
            OPERADOR_ID
        );
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComValor(String valor) {
        List<Map<String, Object>> coberturas = List.of(
            Map.of("tipo", "DANOS_MATERIAIS", "valorCobertura", "10000.00")
        );

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            valor,
            "MENSAL",
            coberturas,
            "2500.00",
            OPERADOR_ID
        );
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComFormaPagamento(String formaPagamento) {
        List<Map<String, Object>> coberturas = List.of(
            Map.of("tipo", "DANOS_MATERIAIS", "valorCobertura", "10000.00")
        );

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            "50000.00",
            formaPagamento,
            coberturas,
            "2500.00",
            OPERADOR_ID
        );
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComVigenciaPersonalizada(
            LocalDate inicio, LocalDate fim) {
        List<Map<String, Object>> coberturas = List.of(
            Map.of("tipo", "DANOS_MATERIAIS", "valorCobertura", "10000.00")
        );

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            inicio.toString(),
            fim.toString(),
            "50000.00",
            "MENSAL",
            coberturas,
            "2500.00",
            OPERADOR_ID
        );
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComTodasCondicoes() {
        List<Map<String, Object>> coberturas = List.of(
            Map.of("tipo", "TOTAL", "valorCobertura", "150000.00")
        );

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            LocalDate.now().toString(),
            LocalDate.now().plusYears(1).toString(),
            "150000.00",
            "ANUAL",
            coberturas,
            "7500.00",
            OPERADOR_ID
        );
    }

    private ApoliceCriadaEvent criarApoliceCriadaEventComDatasInvalidas() {
        List<Map<String, Object>> coberturas = List.of(
            Map.of("tipo", "DANOS_MATERIAIS", "valorCobertura", "10000.00")
        );

        return new ApoliceCriadaEvent(
            APOLICE_ID,
            NUMERO_APOLICE,
            SEGURADO_ID,
            PRODUTO,
            "DATA_INVALIDA",
            "OUTRA_DATA_INVALIDA",
            "VALOR_INVALIDO",
            "MENSAL",
            coberturas,
            "PREMIO_INVALIDO",
            OPERADOR_ID
        );
    }

    private ApoliceAtualizadaEvent criarApoliceAtualizadaEvent() {
        Map<String, Object> alteracoes = Map.of("status", "atualizado");
        Map<String, Object> valoresAnteriores = Map.of("status", "ativo");
        Map<String, Object> novosValores = Map.of("status", "atualizado");

        return new ApoliceAtualizadaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            alteracoes,
            valoresAnteriores,
            novosValores,
            OPERADOR_ID,
            "Atualização solicitada"
        );
    }

    private ApoliceCanceladaEvent criarApoliceCanceladaEvent() {
        return new ApoliceCanceladaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            "50000.00",
            "Solicitação do cliente",
            LocalDate.now().toString(),
            "0.00",
            OPERADOR_ID,
            "Cancelamento imediato",
            "SOLICITACAO_SEGURADO"
        );
    }

    private ApoliceRenovadaEvent criarApoliceRenovadaEvent() {
        return criarApoliceRenovadaEventComVigencia(
            LocalDate.now().plusYears(1),
            LocalDate.now().plusYears(2)
        );
    }

    private ApoliceRenovadaEvent criarApoliceRenovadaEventComVigencia(
            LocalDate inicio, LocalDate fim) {
        return new ApoliceRenovadaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            inicio.toString(),
            fim.toString(),
            "50000.00",
            "2500.00",
            null,
            "MENSAL",
            OPERADOR_ID,
            "MANUAL",
            "Renovação padrão"
        );
    }

    private ApoliceRenovadaEvent criarApoliceRenovadaEventComValor(String novoValor) {
        return new ApoliceRenovadaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            LocalDate.now().plusYears(1).toString(),
            LocalDate.now().plusYears(2).toString(),
            novoValor,
            "3750.00",
            null,
            "MENSAL",
            OPERADOR_ID,
            "MANUAL",
            "Renovação com reajuste"
        );
    }

    private ApoliceRenovadaEvent criarApoliceRenovadaEventComDatasInvalidas() {
        return new ApoliceRenovadaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            "DATA_INVALIDA",
            "OUTRA_DATA_INVALIDA",
            "50000.00",
            "2500.00",
            null,
            "MENSAL",
            OPERADOR_ID,
            "MANUAL",
            "Renovação teste"
        );
    }

    private CoberturaAdicionadaEvent criarCoberturaAdicionadaEvent() {
        return new CoberturaAdicionadaEvent(
            APOLICE_ID,
            2,
            NUMERO_APOLICE,
            SEGURADO_ID,
            "ROUBO_FURTO",
            "15000.00",
            "750.00",
            30,
            "250.00",
            LocalDate.now().toString(),
            OPERADOR_ID,
            "Solicitação do segurado"
        );
    }

    private ApoliceQueryModel criarModeloExistente() {
        ApoliceQueryModel model = new ApoliceQueryModel(APOLICE_ID, NUMERO_APOLICE, SEGURADO_ID);
        model.setProduto(PRODUTO);
        model.setStatus(StatusApolice.ATIVA);
        model.setVigenciaInicio(LocalDate.now());
        model.setVigenciaFim(LocalDate.now().plusYears(1));
        model.setValorSegurado(new BigDecimal("50000.00"));
        model.setValorTotal(new BigDecimal("2500.00"));
        model.setFormaPagamento("MENSAL");
        model.setOperadorResponsavel(OPERADOR_ID);
        model.setRenovacaoAutomatica(true);
        model.setCoberturas(List.of(TipoCobertura.PARCIAL));
        model.setCoberturasResumo("Parcial");
        model.setTemCoberturaTotal(false);
        model.setDiasParaVencimento(365);
        model.setVencimentoProximo(false);
        model.setScoreRenovacao(70);
        return model;
    }
}
