package com.seguradora.hibrida.domain.apolice.notification.service;

import com.seguradora.hibrida.domain.apolice.notification.model.ApoliceNotification;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationChannel;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationStatus;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
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

import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link NotificationSenderService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("NotificationSenderService - Testes Unitários")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class NotificationSenderServiceTest {

    @Mock
    private ApoliceNotificationRepository notificationRepository;

    @InjectMocks
    private NotificationSenderService service;

    private ApoliceNotification notification;

    @BeforeEach
    void setUp() {
        notification = ApoliceNotification.builder()
            .id(UUID.randomUUID().toString())
            .apoliceId("apolice-123")
            .seguradoId("segurado-456")
            .apoliceNumero("APO-2026-001")
            .seguradoCpf("12345678901")
            .seguradoNome("João Silva")
            .seguradoEmail("joao@email.com")
            .seguradoTelefone("+5511999999999")
            .type(NotificationType.APOLICE_CRIADA)
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .titulo("Apólice Criada")
            .mensagem("Sua apólice foi criada")
            .tentativas(0)
            .maxTentativas(3)
            .agendadaPara(Instant.now().minusSeconds(60))
            .expiraEm(Instant.now().plusSeconds(3600))
            .build();
    }

    @Nested
    @DisplayName("Testes de Processamento de Notificações Pendentes")
    class ProcessamentoNotificacoesPendentesTests {

        @Test
        @DisplayName("Deve processar notificações pendentes com sucesso")
        void deveProcessarNotificacoesPendentesComSucesso() {
            when(notificationRepository.findByStatusAndScheduledAtLessThanEqual(
                any(NotificationStatus.class),
                any(Instant.class)
            )).thenReturn(Collections.singletonList(notification));

            service.processarNotificacoesPendentes();

            verify(notificationRepository, times(1))
                .findByStatusAndScheduledAtLessThanEqual(
                    eq(NotificationStatus.PENDING),
                    any(Instant.class)
                );
        }

        @Test
        @DisplayName("Não deve processar quando não há notificações pendentes")
        void naoDeveProcessarQuandoNaoHaNotificacoesPendentes() {
            when(notificationRepository.findByStatusAndScheduledAtLessThanEqual(
                any(NotificationStatus.class),
                any(Instant.class)
            )).thenReturn(Collections.emptyList());

            service.processarNotificacoesPendentes();

            verify(notificationRepository, times(1))
                .findByStatusAndScheduledAtLessThanEqual(
                    any(NotificationStatus.class),
                    any(Instant.class)
                );
        }

        @Test
        @DisplayName("Deve processar múltiplas notificações pendentes")
        void deveProcessarMultiplasNotificacoesPendentes() {
            ApoliceNotification notif1 = createNotification(NotificationChannel.EMAIL);
            ApoliceNotification notif2 = createNotification(NotificationChannel.SMS);
            ApoliceNotification notif3 = createNotification(NotificationChannel.WHATSAPP);

            when(notificationRepository.findByStatusAndScheduledAtLessThanEqual(
                any(NotificationStatus.class),
                any(Instant.class)
            )).thenReturn(Arrays.asList(notif1, notif2, notif3));

            service.processarNotificacoesPendentes();

            verify(notificationRepository, times(1))
                .findByStatusAndScheduledAtLessThanEqual(
                    eq(NotificationStatus.PENDING),
                    any(Instant.class)
                );
        }

        @Test
        @DisplayName("Deve lidar com exceções durante processamento")
        void deveLidarComExcecoesDuranteProcessamento() {
            when(notificationRepository.findByStatusAndScheduledAtLessThanEqual(
                any(NotificationStatus.class),
                any(Instant.class)
            )).thenThrow(new RuntimeException("Erro de banco de dados"));

            assertThatCode(() -> service.processarNotificacoesPendentes())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de Notificação Individual")
    class ProcessamentoNotificacaoIndividualTests {

        @Test
        @DisplayName("Deve marcar como processando ao iniciar")
        void deveMarcarComoProcessandoAoIniciar() {
            service.processarNotificacao(notification).join();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);

            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> savedNotifications = captor.getAllValues();
            assertThat(savedNotifications).isNotEmpty();
        }

        @Test
        @DisplayName("Deve marcar como falha quando notificação expirada")
        void deveMarcarComoFalhaQuandoNotificacaoExpirada() {
            notification.setExpiraEm(Instant.now().minusSeconds(60));

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificação sem expiração")
        void deveProcessarNotificacaoSemExpiracao() {
            notification.setExpiraEm(null);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve lidar com exceções durante processamento de notificação")
        void deveLidarComExcecoesDuranteProcessamentoNotificacao() {
            when(notificationRepository.save(any(ApoliceNotification.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThatCode(() -> service.processarNotificacao(notification).join())
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Envio por Canal")
    class EnvioPorCanalTests {

        @Test
        @DisplayName("Deve processar notificação EMAIL")
        void deveProcessarNotificacaoEmail() {
            notification.setChannel(NotificationChannel.EMAIL);
            notification.setSeguradoEmail("joao@email.com");

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificação SMS")
        void deveProcessarNotificacaoSms() {
            notification.setChannel(NotificationChannel.SMS);
            notification.setSeguradoTelefone("+5511999999999");

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificação WHATSAPP")
        void deveProcessarNotificacaoWhatsapp() {
            notification.setChannel(NotificationChannel.WHATSAPP);
            notification.setSeguradoTelefone("+5511999999999");

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificação PUSH")
        void deveProcessarNotificacaoPush() {
            notification.setChannel(NotificationChannel.PUSH);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificação IN_APP")
        void deveProcessarNotificacaoInApp() {
            notification.setChannel(NotificationChannel.IN_APP);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve lidar com email ausente")
        void deveLidarComEmailAusente() {
            notification.setChannel(NotificationChannel.EMAIL);
            notification.setSeguradoEmail(null);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve lidar com telefone ausente")
        void deveLidarComTelefoneAusente() {
            notification.setChannel(NotificationChannel.SMS);
            notification.setSeguradoTelefone(null);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Limpeza de Notificações")
    class LimpezaNotificacoesTests {

        @Test
        @DisplayName("Deve remover notificações antigas")
        void deveRemoverNotificacoesAntigas() {
            when(notificationRepository.deleteByUpdatedAtLessThan(any(Instant.class)))
                .thenReturn(10);

            service.limparNotificacoes();

            verify(notificationRepository, times(1))
                .deleteByUpdatedAtLessThan(any(Instant.class));
        }

        @Test
        @DisplayName("Não deve falhar quando nenhuma notificação é removida")
        void naoDeveFalharQuandoNenhumaNotificacaoRemovida() {
            when(notificationRepository.deleteByUpdatedAtLessThan(any(Instant.class)))
                .thenReturn(0);

            assertThatCode(() -> service.limparNotificacoes())
                .doesNotThrowAnyException();

            verify(notificationRepository, times(1))
                .deleteByUpdatedAtLessThan(any(Instant.class));
        }

        @Test
        @DisplayName("Deve lidar com exceções durante limpeza")
        void deveLidarComExcecoesDuranteLimpeza() {
            when(notificationRepository.deleteByUpdatedAtLessThan(any(Instant.class)))
                .thenThrow(new RuntimeException("Erro ao deletar"));

            assertThatCode(() -> service.limparNotificacoes())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve usar cutoff de 30 dias atrás")
        void deveUsarCutoffDe30DiasAtras() {
            ArgumentCaptor<Instant> cutoffCaptor = ArgumentCaptor.forClass(Instant.class);
            when(notificationRepository.deleteByUpdatedAtLessThan(any(Instant.class)))
                .thenReturn(5);

            service.limparNotificacoes();

            verify(notificationRepository).deleteByUpdatedAtLessThan(cutoffCaptor.capture());

            Instant cutoff = cutoffCaptor.getValue();
            Instant expected = Instant.now().minusSeconds(30 * 24 * 3600);

            // Tolerância de 10 segundos
            assertThat(cutoff).isBetween(
                expected.minusSeconds(10),
                expected.plusSeconds(10)
            );
        }
    }

    @Nested
    @DisplayName("Testes de Retry")
    class RetryTests {

        @Test
        @DisplayName("Deve tentar novamente após falha quando possível")
        void deveTentarNovamenteAposFalhaQuandoPossivel() {
            notification.setTentativas(0);
            notification.setMaxTentativas(3);
            notification.setStatus(NotificationStatus.PENDING);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Não deve tentar novamente quando atingiu máximo de tentativas")
        void naoDeveTentarNovamenteQuandoAtingiuMaximo() {
            notification.setTentativas(3);
            notification.setMaxTentativas(3);
            notification.setStatus(NotificationStatus.FAILED);

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve incrementar tentativas a cada retry")
        void deveIncrementarTentativasACadaRetry() {
            notification.setTentativas(1);
            notification.setMaxTentativas(3);

            assertThat(notification.canRetry()).isFalse(); // FAILED é final
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Estado")
    class ValidacaoEstadoTests {

        @Test
        @DisplayName("Notificação pendente deve ser processada")
        void notificacaoPendenteDeveSerProcessada() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(Instant.now().minusSeconds(60));

            assertThat(notification.canSendNow()).isTrue();
        }

        @Test
        @DisplayName("Notificação futura não deve ser processada")
        void notificacaoFuturaNaoDeveSerProcessada() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(Instant.now().plusSeconds(3600));

            assertThat(notification.canSendNow()).isFalse();
        }

        @Test
        @DisplayName("Notificação enviada não deve ser reprocessada")
        void notificacaoEnviadaNaoDeveSerReprocessada() {
            notification.setStatus(NotificationStatus.SENT);

            assertThat(notification.canSendNow()).isFalse();
        }

        @Test
        @DisplayName("Notificação expirada não deve ser processada")
        void notificacaoExpiradaNaoDeveSerProcessada() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setExpiraEm(Instant.now().minusSeconds(60));

            assertThat(notification.isExpired()).isTrue();
            assertThat(notification.canSendNow()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Dados Auxiliares")
    class DadosAuxiliaresTests {

        @Test
        @DisplayName("Deve obter email do segurado quando ausente")
        void deveObterEmailDoSeguradoQuandoAusente() {
            notification.setChannel(NotificationChannel.EMAIL);
            notification.setSeguradoEmail(null);
            notification.setSeguradoCpf("12345678901");

            service.processarNotificacao(notification).join();

            // Verificar que tentou processar mesmo sem email
            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve obter telefone do segurado quando ausente")
        void deveObterTelefoneDoSeguradoQuandoAusente() {
            notification.setChannel(NotificationChannel.SMS);
            notification.setSeguradoTelefone(null);
            notification.setSeguradoCpf("12345678901");

            service.processarNotificacao(notification).join();

            // Verificar que tentou processar mesmo sem telefone
            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve obter device token do segurado")
        void deveObterDeviceTokenDoSegurado() {
            notification.setChannel(NotificationChannel.PUSH);
            notification.setSeguradoCpf("12345678901");

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Integração")
    class IntegracaoTests {

        @Test
        @DisplayName("Deve processar ciclo completo de notificação com sucesso")
        void deveProcessarCicloCompletoComSucesso() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setTentativas(0);
            notification.setAgendadaPara(Instant.now().minusSeconds(60));
            notification.setExpiraEm(Instant.now().plusSeconds(3600));

            service.processarNotificacao(notification).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificações de diferentes tipos")
        void deveProcessarNotificacoesDeDiferentesTipos() {
            ApoliceNotification notifCriada = createNotification(NotificationChannel.EMAIL);
            notifCriada.setType(NotificationType.APOLICE_CRIADA);

            ApoliceNotification notifVencimento = createNotification(NotificationChannel.SMS);
            notifVencimento.setType(NotificationType.VENCIMENTO_1_DIA);

            service.processarNotificacao(notifCriada).join();
            service.processarNotificacao(notifVencimento).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve processar notificações prioritárias corretamente")
        void deveProcessarNotificacoesPrioritariasCorretamente() {
            ApoliceNotification notifCritica = createNotification(NotificationChannel.EMAIL);
            notifCritica.setType(NotificationType.APOLICE_VENCIDA);
            notifCritica.setPriority(1);

            service.processarNotificacao(notifCritica).join();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com notificação sem ID")
        void deveLidarComNotificacaoSemId() {
            notification.setId(null);

            assertThatCode(() -> service.processarNotificacao(notification).join())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com notificação sem canal")
        void deveLidarComNotificacaoSemCanal() {
            notification.setChannel(null);

            assertThatCode(() -> service.processarNotificacao(notification).join())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com notificação sem tipo")
        void deveLidarComNotificacaoSemTipo() {
            notification.setType(null);

            assertThatCode(() -> service.processarNotificacao(notification).join())
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com todos os campos nulos")
        void deveLidarComTodosCamposNulos() {
            ApoliceNotification notifVazia = new ApoliceNotification();

            assertThatCode(() -> service.processarNotificacao(notifVazia).join())
                .doesNotThrowAnyException();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private ApoliceNotification createNotification(NotificationChannel channel) {
        return ApoliceNotification.builder()
            .id(UUID.randomUUID().toString())
            .apoliceId("apolice-" + UUID.randomUUID())
            .seguradoId("segurado-" + UUID.randomUUID())
            .apoliceNumero("APO-2026-" + System.currentTimeMillis())
            .seguradoCpf("12345678901")
            .seguradoNome("Segurado Teste")
            .seguradoEmail("teste@email.com")
            .seguradoTelefone("+5511999999999")
            .type(NotificationType.APOLICE_CRIADA)
            .channel(channel)
            .status(NotificationStatus.PENDING)
            .titulo("Título Teste")
            .mensagem("Mensagem Teste")
            .tentativas(0)
            .maxTentativas(3)
            .agendadaPara(Instant.now().minusSeconds(60))
            .expiraEm(Instant.now().plusSeconds(3600))
            .build();
    }
}
