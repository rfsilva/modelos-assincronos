package com.seguradora.hibrida.domain.apolice.notification.handler;

import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.apolice.notification.model.ApoliceNotification;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationStatus;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import com.seguradora.hibrida.domain.apolice.notification.repository.ApoliceNotificationRepository;
import com.seguradora.hibrida.domain.apolice.notification.service.NotificationTemplateService;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
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
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link ApoliceNotificationEventHandler}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ApoliceNotificationEventHandler - Testes Unitários")
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ApoliceNotificationEventHandlerTest {

    @Mock
    private ApoliceNotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateService templateService;

    @InjectMocks
    private ApoliceNotificationEventHandler handler;

    private String aggregateId;
    private String seguradoId;
    private String numeroApolice;

    @BeforeEach
    void setUp() {
        aggregateId = UUID.randomUUID().toString();
        seguradoId = UUID.randomUUID().toString();
        numeroApolice = "APO-2026-001";

        // Setup mocks
        when(templateService.generateTitle(any(), any(), any()))
            .thenReturn("Título Teste");
        when(templateService.generateMessage(any(), any(), any()))
            .thenReturn("Mensagem Teste");
        when(templateService.getExpirationHours(any()))
            .thenReturn(24L);
    }

    @Nested
    @DisplayName("Testes de Configuração do Handler")
    class ConfiguracaoHandlerTests {

        @Test
        @DisplayName("Deve retornar tipo de evento correto")
        void deveRetornarTipoEventoCorreto() {
            assertThat(handler.getEventType()).isEqualTo(DomainEvent.class);
        }

        @Test
        @DisplayName("Deve ter prioridade 50")
        void deveTermPrioridade50() {
            assertThat(handler.getPriority()).isEqualTo(50);
        }

        @Test
        @DisplayName("Deve ser assíncrono")
        void deveSerAssincrono() {
            assertThat(handler.isAsync()).isTrue();
        }

        @Test
        @DisplayName("Deve ter timeout de 30 segundos")
        void deveTermTimeoutDe30Segundos() {
            assertThat(handler.getTimeoutSeconds()).isEqualTo(30);
        }
    }

    @Nested
    @DisplayName("Testes de Suporte a Eventos")
    class SuporteEventosTests {

        @Test
        @DisplayName("Deve suportar ApoliceCriadaEvent")
        void deveSuportarApoliceCriadaEvent() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceAtualizadaEvent")
        void deveSuportarApoliceAtualizadaEvent() {
            ApoliceAtualizadaEvent event = createApoliceAtualizadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceCanceladaEvent")
        void deveSuportarApoliceCanceladaEvent() {
            ApoliceCanceladaEvent event = createApoliceCanceladaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar ApoliceRenovadaEvent")
        void deveSuportarApoliceRenovadaEvent() {
            ApoliceRenovadaEvent event = createApoliceRenovadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Deve suportar CoberturaAdicionadaEvent")
        void deveSuportarCoberturaAdicionadaEvent() {
            CoberturaAdicionadaEvent event = createCoberturaAdicionadaEvent();
            assertThat(handler.supports(event)).isTrue();
        }

        @Test
        @DisplayName("Não deve suportar eventos não relacionados")
        void naoDeveSuportarEventosNaoRelacionados() {
            DomainEvent event = mock(DomainEvent.class);
            assertThat(handler.supports(event)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de ApoliceCriadaEvent")
    class ProcessamentoApoliceCriadaEventTests {

        @Test
        @DisplayName("Deve criar notificações para apólice criada")
        void deveCriarNotificacoesParaApoliceCriada() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);

            // Aguardar processamento assíncrono
            await();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve criar notificação com dados do evento")
        void deveCriarNotificacaoComDadosDoEvento() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications).isNotEmpty();

            ApoliceNotification notification = notifications.get(0);
            assertThat(notification.getApoliceId()).isEqualTo(aggregateId);
            assertThat(notification.getType()).isEqualTo(NotificationType.APOLICE_CRIADA);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("Deve criar notificações para múltiplos canais")
        void deveCriarNotificacoesParaMultiplosCanais() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);
            await();

            // Deve criar pelo menos 2 notificações (EMAIL e SMS)
            verify(notificationRepository, atLeast(2)).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve lidar com exceções durante processamento")
        void deveLidarComExcecoesDuranteProcessamento() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();
            when(notificationRepository.save(any(ApoliceNotification.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de ApoliceAtualizadaEvent")
    class ProcessamentoApoliceAtualizadaEventTests {

        @Test
        @DisplayName("Deve criar notificações para apólice atualizada")
        void deveCriarNotificacoesParaApoliceAtualizada() {
            ApoliceAtualizadaEvent event = createApoliceAtualizadaEvent();

            handler.handle(event);
            await();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve usar tipo APOLICE_ATUALIZADA")
        void deveUsarTipoApoliceAtualizada() {
            ApoliceAtualizadaEvent event = createApoliceAtualizadaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications)
                .isNotEmpty()
                .allMatch(n -> n.getType() == NotificationType.APOLICE_ATUALIZADA);
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de ApoliceCanceladaEvent")
    class ProcessamentoApoliceCanceladaEventTests {

        @Test
        @DisplayName("Deve criar notificações para apólice cancelada")
        void deveCriarNotificacoesParaApoliceCancelada() {
            ApoliceCanceladaEvent event = createApoliceCanceladaEvent();

            handler.handle(event);
            await();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve incluir motivo do cancelamento")
        void deveIncluirMotivoDoCancelamento() {
            ApoliceCanceladaEvent event = createApoliceCanceladaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications).isNotEmpty();

            ApoliceNotification notification = notifications.get(0);
            assertThat(notification.getMetadata()).containsKey("motivo");
        }

        @Test
        @DisplayName("Deve usar tipo APOLICE_CANCELADA")
        void deveUsarTipoApoliceCancelada() {
            ApoliceCanceladaEvent event = createApoliceCanceladaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications)
                .isNotEmpty()
                .allMatch(n -> n.getType() == NotificationType.APOLICE_CANCELADA);
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de ApoliceRenovadaEvent")
    class ProcessamentoApoliceRenovadaEventTests {

        @Test
        @DisplayName("Deve criar notificações para apólice renovada")
        void deveCriarNotificacoesParaApoliceRenovada() {
            ApoliceRenovadaEvent event = createApoliceRenovadaEvent();

            handler.handle(event);
            await();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve usar tipo APOLICE_RENOVADA")
        void deveUsarTipoApoliceRenovada() {
            ApoliceRenovadaEvent event = createApoliceRenovadaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications)
                .isNotEmpty()
                .allMatch(n -> n.getType() == NotificationType.APOLICE_RENOVADA);
        }
    }

    @Nested
    @DisplayName("Testes de Processamento de CoberturaAdicionadaEvent")
    class ProcessamentoCoberturaAdicionadaEventTests {

        @Test
        @DisplayName("Deve criar notificações para cobertura adicionada")
        void deveCriarNotificacoesParaCoberturaAdicionada() {
            CoberturaAdicionadaEvent event = createCoberturaAdicionadaEvent();

            handler.handle(event);
            await();

            verify(notificationRepository, atLeastOnce()).save(any(ApoliceNotification.class));
        }

        @Test
        @DisplayName("Deve usar tipo COBERTURA_ADICIONADA")
        void deveUsarTipoCoberturaAdicionada() {
            CoberturaAdicionadaEvent event = createCoberturaAdicionadaEvent();

            handler.handle(event);
            await();

            ArgumentCaptor<ApoliceNotification> captor =
                ArgumentCaptor.forClass(ApoliceNotification.class);
            verify(notificationRepository, atLeastOnce()).save(captor.capture());

            List<ApoliceNotification> notifications = captor.getAllValues();
            assertThat(notifications)
                .isNotEmpty()
                .allMatch(n -> n.getType() == NotificationType.COBERTURA_ADICIONADA);
        }
    }

    @Nested
    @DisplayName("Testes de Geração de Templates")
    class GeracaoTemplatesTests {

        @Test
        @DisplayName("Deve chamar serviço de template para gerar título")
        void deveChamarServicoTemplateParaGerarTitulo() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);
            await();

            verify(templateService, atLeastOnce())
                .generateTitle(any(), any(), any());
        }

        @Test
        @DisplayName("Deve chamar serviço de template para gerar mensagem")
        void deveChamarServicoTemplateParaGerarMensagem() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);
            await();

            verify(templateService, atLeastOnce())
                .generateMessage(any(), any(), any());
        }

        @Test
        @DisplayName("Deve chamar serviço de template para obter expiração")
        void deveChamarServicoTemplateParaObterExpiracao() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();

            handler.handle(event);
            await();

            verify(templateService, atLeastOnce())
                .getExpirationHours(any());
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com evento sem agregado ID")
        void deveLidarComEventoSemAgregadoId() {
            ApoliceCriadaEvent event = createApoliceCriadaEventWithoutId();

            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com falha no serviço de template")
        void deveLidarComFalhaNoServicoTemplate() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();
            when(templateService.generateTitle(any(), any(), any()))
                .thenThrow(new RuntimeException("Erro no template"));

            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com falha ao salvar notificação")
        void deveLidarComFalhaAoSalvarNotificacao() {
            ApoliceCriadaEvent event = createApoliceCriadaEvent();
            when(notificationRepository.save(any(ApoliceNotification.class)))
                .thenThrow(new RuntimeException("Erro ao salvar"));

            assertThatCode(() -> handler.handle(event))
                .doesNotThrowAnyException();
        }
    }

    // ========== MÉTODOS AUXILIARES ==========

    private void await() {
        try {
            Thread.sleep(200); // Aguardar processamento assíncrono
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private ApoliceCriadaEvent createApoliceCriadaEvent() {
        List<Map<String, Object>> coberturas = List.of(
            Map.of(
                "tipo", "COLISAO",
                "valorCobertura", "50000.00",
                "franquia", "2000.00",
                "carenciaDias", 0,
                "ativa", true
            )
        );

        return new ApoliceCriadaEvent(
            aggregateId,
            numeroApolice,
            seguradoId,
            "Seguro Auto",
            "2026-01-01",
            "2026-12-31",
            "50000.00",
            "MENSAL",
            coberturas,
            "1200.00",
            "operador-123"
        );
    }

    private ApoliceCriadaEvent createApoliceCriadaEventWithoutId() {
        List<Map<String, Object>> coberturas = List.of(
            Map.of(
                "tipo", "COLISAO",
                "valorCobertura", "50000.00",
                "franquia", "2000.00",
                "carenciaDias", 0,
                "ativa", true
            )
        );

        return new ApoliceCriadaEvent(
            null,
            numeroApolice,
            seguradoId,
            "Seguro Auto",
            "2026-01-01",
            "2026-12-31",
            "50000.00",
            "MENSAL",
            coberturas,
            "1200.00",
            "operador-123"
        );
    }

    private ApoliceAtualizadaEvent createApoliceAtualizadaEvent() {
        return new ApoliceAtualizadaEvent(
            aggregateId,
            1L, // version
            numeroApolice,
            seguradoId,
            Collections.singletonMap("produto", "Seguro Auto Premium"), // alteracoes
            Collections.singletonMap("produto", "Seguro Auto"), // valoresAnteriores
            Collections.singletonMap("produto", "Seguro Auto Premium"), // novosValores
            "operador-123",
            "Upgrade de produto" // motivo
        );
    }

    private ApoliceCanceladaEvent createApoliceCanceladaEvent() {
        return new ApoliceCanceladaEvent(
            aggregateId,
            1L, // version
            numeroApolice,
            seguradoId,
            "50000.00", // valorSegurado
            "Solicitação do cliente", // motivo
            java.time.LocalDate.now().toString(), // dataEfeito
            "1500.00", // valorReembolso
            "operador-123",
            "Cliente solicitou cancelamento", // observacoes
            "SOLICITACAO_SEGURADO" // tipoCancelamento
        );
    }

    private ApoliceRenovadaEvent createApoliceRenovadaEvent() {
        return new ApoliceRenovadaEvent(
            aggregateId,
            1L, // version
            numeroApolice,
            seguradoId,
            java.time.LocalDate.now().toString(), // novaVigenciaInicio
            "2027-12-31", // novaVigenciaFim
            "55000.00", // novoValorSegurado
            "2500.00", // novoPremioTotal
            Collections.emptyList(), // alteracoesCoberturas
            "ANUAL", // novaFormaPagamento
            "operador-123",
            "MANUAL", // tipoRenovacao
            "Renovação com ajuste de valor" // observacoes
        );
    }

    private CoberturaAdicionadaEvent createCoberturaAdicionadaEvent() {
        return new CoberturaAdicionadaEvent(
            aggregateId,
            1L, // version
            numeroApolice,
            seguradoId,
            "VIDROS", // tipoCobertura
            "2000.00", // valorCobertura
            "500.00", // franquia
            0, // carenciaDias
            "150.00", // valorAdicionalPremio
            java.time.LocalDate.now().toString(), // dataEfeito
            "operador-123",
            "Solicitação do cliente" // motivo
        );
    }
}
