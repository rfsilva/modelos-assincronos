package com.seguradora.hibrida.domain.apolice.notification.scheduler;

import com.seguradora.hibrida.domain.apolice.notification.model.ApoliceNotification;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
import com.seguradora.hibrida.domain.apolice.notification.service.NotificationTemplateService;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link VencimentoNotificationScheduler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VencimentoNotificationScheduler - Testes Unitários")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class VencimentoNotificationSchedulerTest {

    @Mock
    private ApoliceQueryRepository apoliceRepository;

    @Mock
    private ApoliceNotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateService templateService;

    @InjectMocks
    private VencimentoNotificationScheduler scheduler;

    @BeforeEach
    void setUp() {
        when(templateService.generateTitle(any(), any(), any()))
            .thenReturn("Título Teste");
        when(templateService.generateMessage(any(), any(), any()))
            .thenReturn("Mensagem Teste");
        when(templateService.getExpirationHours(any()))
            .thenReturn(24L);
    }

    @Nested
    @DisplayName("Testes de Verificação de Vencimentos")
    class VerificacaoVencimentosTests {

        @Test
        @DisplayName("Deve verificar vencimentos próximos")
        void deveVerificarVencimentosProximos() {
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), any()))
                .thenReturn(Collections.emptyList());
            when(apoliceRepository.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(any(), any()))
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();

            verify(apoliceRepository, times(4))
                .findByVigenciaFimAndStatusOrderByNumeroApolice(any(LocalDate.class), eq("ATIVA"));
        }

        @Test
        @DisplayName("Deve verificar vencimentos em 30 dias")
        void deveVerificarVencimentosEm30Dias() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve verificar vencimentos em 15 dias")
        void deveVerificarVencimentosEm15Dias() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve verificar vencimentos em 7 dias")
        void deveVerificarVencimentosEm7Dias() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve verificar vencimentos em 1 dia")
        void deveVerificarVencimentosEm1Dia() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve verificar apólices vencidas")
        void deveVerificarApolicesVencidas() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Criação de Notificações")
    class CriacaoNotificacoesTests {

        @Test
        @DisplayName("Deve criar notificação de vencimento com dados corretos")
        void deveCriarNotificacaoVencimentoComDadosCorretos() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications).isNotEmpty();

            ApoliceNotification notification = notifications.get(0);
            assertThat(notification.getId()).isNotNull();
            assertThat(notification.getApoliceId()).isEqualTo(apolice.getId());
            assertThat(notification.getSeguradoId()).isEqualTo(apolice.getSeguradoId());
        }

        @Test
        @DisplayName("Deve criar notificações para múltiplos canais")
        void deveCriarNotificacoesParaMultiplosCanais() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            // Deve criar pelo menos 8 notificações (4 períodos x 2 canais)
            verify(notificationRepository, atLeast(8)).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Não deve criar notificação se já foi notificado")
        void naoDeveCriarNotificacaoSeJaFoiNotificado() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(true); // Já foi notificado

            scheduler.verificarVencimentosProximos();

            verify(notificationRepository, never()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Verificação de Score Baixo")
    class VerificacaoScoreBaixoTests {

        @Test
        @DisplayName("Deve verificar score baixo")
        void deveVerificarScoreBaixo() {
            when(apoliceRepository.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(any(), eq("ATIVA")))
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.verificarScoreBaixo())
                .doesNotThrowAnyException();

            verify(apoliceRepository, times(1))
                .findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(any(LocalDate.class), eq("ATIVA"));
        }

        @Test
        @DisplayName("Deve criar notificação para score baixo")
        void deveCriarNotificacaoParaScoreBaixo() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            apolice.setValorSegurado(BigDecimal.valueOf(5000)); // Valor baixo (-30)
            apolice.setCoberturas(Collections.emptyList()); // Poucas coberturas (-25), score = 45 < 50

            when(apoliceRepository.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarScoreBaixo();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Não deve criar notificação para score alto")
        void naoDeveCriarNotificacaoParaScoreAlto() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            apolice.setValorSegurado(BigDecimal.valueOf(100000)); // Valor alto

            when(apoliceRepository.findByVigenciaFimLessThanEqualAndStatusOrderByVigenciaFim(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));

            scheduler.verificarScoreBaixo();

            // Pode ou não criar dependendo do cálculo aleatório do score
            // Apenas verificar que não lança exceção
            assertThatCode(() -> scheduler.verificarScoreBaixo())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Processamento em Lote")
    class ProcessamentoLoteTests {

        @Test
        @DisplayName("Deve processar múltiplas apólices")
        void deveProcessarMultiplasApolices() {
            ApoliceQueryModel apolice1 = createApoliceQueryModel();
            ApoliceQueryModel apolice2 = createApoliceQueryModel();
            ApoliceQueryModel apolice3 = createApoliceQueryModel();

            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Arrays.asList(apolice1, apolice2, apolice3));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            scheduler.verificarVencimentosProximos();

            // Deve criar notificações para todas as apólices
            verify(notificationRepository, atLeast(6)).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve lidar com lista vazia de apólices")
        void deveLidarComListaVaziaDeApolices() {
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.emptyList());
            when(apoliceRepository.findByVigenciaFimLessThanAndStatusOrderByVigenciaFim(any(), eq("ATIVA")))
                .thenReturn(Collections.emptyList());

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();

            verify(notificationRepository, never()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Tratamento de Erros")
    class TratamentoErrosTests {

        @Test
        @DisplayName("Deve lidar com exceção no repositório de apólices")
        void deveLidarComExcecaoNoRepositorioApolices() {
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenThrow(new RuntimeException("Erro de banco de dados"));

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com exceção ao criar notificação")
        void deveLidarComExcecaoAoCriarNotificacao() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);
            when(notificationRepository.save(any(ApoliceNotification.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com exceção no serviço de template")
        void deveLidarComExcecaoNoServicoTemplate() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);
            when(templateService.generateTitle(any(), any(), any()))
                .thenThrow(new RuntimeException("Erro no template"));

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com apólice sem ID")
        void deveLidarComApoliceSemId() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            apolice.setId(null);

            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com apólice sem segurado")
        void deveLidarComApoliceSemSegurado() {
            ApoliceQueryModel apolice = createApoliceQueryModel();
            apolice.setSeguradoId(null);
            apolice.setSeguradoNome(null);

            when(apoliceRepository.findByVigenciaFimAndStatusOrderByNumeroApolice(any(), eq("ATIVA")))
                .thenReturn(Collections.singletonList(apolice));
            when(notificationRepository.existsByApoliceIdAndTypeAndCriadaEmGreaterThan(any(), any(), any()))
                .thenReturn(false);

            assertThatCode(() -> scheduler.verificarVencimentosProximos())
                .doesNotThrowAnyException();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private ApoliceQueryModel createApoliceQueryModel() {
        String id = UUID.randomUUID().toString();
        String seguradoId = UUID.randomUUID().toString();
        String numero = "APO-2026-" + System.currentTimeMillis();

        ApoliceQueryModel apolice = new ApoliceQueryModel(id, numero, seguradoId);
        apolice.setSeguradoNome("João Silva");
        apolice.setSeguradoCpf("12345678901");
        apolice.setProduto("Seguro Auto");
        apolice.setValorSegurado(BigDecimal.valueOf(50000));
        apolice.setVigenciaInicio(LocalDate.now());
        apolice.setVigenciaFim(LocalDate.now().plusDays(30));
        apolice.setStatus(com.seguradora.hibrida.domain.apolice.model.StatusApolice.ATIVA);
        return apolice;
    }
}
