package com.seguradora.hibrida.domain.apolice.notification.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ApoliceNotification}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ApoliceNotification - Testes Unitários")
class ApoliceNotificationTest {

    private static final String ID = "notif-123";
    private static final String APOLICE_ID = "apolice-456";
    private static final String SEGURADO_ID = "seg-789";
    private static final String APOLICE_NUMERO = "APO-2026-001";
    private static final String SEGURADO_CPF = "12345678901";
    private static final String SEGURADO_NOME = "João Silva";
    private static final String SEGURADO_EMAIL = "joao@email.com";
    private static final String SEGURADO_TELEFONE = "+5511999999999";

    private ApoliceNotification notification;

    @BeforeEach
    void setUp() {
        notification = ApoliceNotification.builder()
            .id(ID)
            .apoliceId(APOLICE_ID)
            .seguradoId(SEGURADO_ID)
            .apoliceNumero(APOLICE_NUMERO)
            .seguradoCpf(SEGURADO_CPF)
            .seguradoNome(SEGURADO_NOME)
            .seguradoEmail(SEGURADO_EMAIL)
            .seguradoTelefone(SEGURADO_TELEFONE)
            .type(NotificationType.APOLICE_CRIADA)
            .channel(NotificationChannel.EMAIL)
            .status(NotificationStatus.PENDING)
            .titulo("Apólice Criada")
            .mensagem("Sua apólice foi criada com sucesso")
            .priority(3)
            .tentativas(0)
            .maxTentativas(3)
            .metadata(new HashMap<>())
            .build();
    }

    @Nested
    @DisplayName("Testes de Construção")
    class ConstrucaoTests {

        @Test
        @DisplayName("Deve criar notificação com builder completo")
        void deveCriarNotificacaoComBuilder() {
            assertThat(notification).isNotNull();
            assertThat(notification.getId()).isEqualTo(ID);
            assertThat(notification.getApoliceId()).isEqualTo(APOLICE_ID);
            assertThat(notification.getSeguradoId()).isEqualTo(SEGURADO_ID);
            assertThat(notification.getApoliceNumero()).isEqualTo(APOLICE_NUMERO);
            assertThat(notification.getSeguradoCpf()).isEqualTo(SEGURADO_CPF);
            assertThat(notification.getSeguradoNome()).isEqualTo(SEGURADO_NOME);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("Deve criar notificação com construtor customizado")
        void deveCriarComConstrutorCustomizado() {
            ApoliceNotification notif = new ApoliceNotification(
                ID, APOLICE_ID, APOLICE_NUMERO, SEGURADO_CPF, SEGURADO_NOME
            );

            assertThat(notif.getId()).isEqualTo(ID);
            assertThat(notif.getApoliceId()).isEqualTo(APOLICE_ID);
            assertThat(notif.getApoliceNumero()).isEqualTo(APOLICE_NUMERO);
            assertThat(notif.getSeguradoCpf()).isEqualTo(SEGURADO_CPF);
            assertThat(notif.getSeguradoNome()).isEqualTo(SEGURADO_NOME);
            assertThat(notif.getStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(notif.getTentativas()).isZero();
            assertThat(notif.getMaxTentativas()).isEqualTo(3);
            assertThat(notif.getMetadata()).isNotNull().isEmpty();
        }

        @Test
        @DisplayName("Deve lançar exceção quando ID é nulo no construtor")
        void deveLancarExcecaoQuandoIdNulo() {
            assertThatThrownBy(() -> new ApoliceNotification(
                null, APOLICE_ID, APOLICE_NUMERO, SEGURADO_CPF, SEGURADO_NOME
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("ID não pode ser nulo");
        }

        @Test
        @DisplayName("Deve lançar exceção quando apólice ID é nulo no construtor")
        void deveLancarExcecaoQuandoApoliceIdNulo() {
            assertThatThrownBy(() -> new ApoliceNotification(
                ID, null, APOLICE_NUMERO, SEGURADO_CPF, SEGURADO_NOME
            ))
            .isInstanceOf(NullPointerException.class)
            .hasMessageContaining("ID da apólice não pode ser nulo");
        }

        @Test
        @DisplayName("Deve ter valores padrão corretos")
        void deveTeValoresPadrao() {
            ApoliceNotification notif = ApoliceNotification.builder()
                .id(ID)
                .apoliceId(APOLICE_ID)
                .seguradoId(SEGURADO_ID)
                .apoliceNumero(APOLICE_NUMERO)
                .seguradoCpf(SEGURADO_CPF)
                .seguradoNome(SEGURADO_NOME)
                .titulo("Teste")
                .mensagem("Mensagem teste")
                .build();

            // Call onCreate to initialize priority based on type
            notif.onCreate();

            assertThat(notif.getType()).isEqualTo(NotificationType.APOLICE_CRIADA);
            assertThat(notif.getChannel()).isEqualTo(NotificationChannel.EMAIL);
            assertThat(notif.getStatus()).isEqualTo(NotificationStatus.PENDING);
            assertThat(notif.getPriority()).isEqualTo(NotificationType.APOLICE_CRIADA.getPrioridade());
            assertThat(notif.getTentativas()).isZero();
            assertThat(notif.getMaxTentativas()).isEqualTo(3);
            assertThat(notif.getMetadata()).isNotNull().isEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Estado")
    class ValidacaoEstadoTests {

        @Test
        @DisplayName("Deve identificar notificação expirada")
        void deveIdentificarNotificacaoExpirada() {
            notification.setExpiraEm(Instant.now().minusSeconds(3600));
            assertThat(notification.isExpired()).isTrue();
        }

        @Test
        @DisplayName("Não deve identificar como expirada quando não tem expiração")
        void naoDeveIdentificarComoExpiradaSemExpiracao() {
            notification.setExpiraEm(null);
            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Não deve identificar como expirada quando ainda válida")
        void naoDeveIdentificarComoExpiradaQuandoValida() {
            notification.setExpiraEm(Instant.now().plusSeconds(3600));
            assertThat(notification.isExpired()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir envio quando pendente e não agendada")
        void devePermitirEnvioQuandoPendenteENaoAgendada() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(null);
            notification.setExpiraEm(Instant.now().plusSeconds(3600));

            assertThat(notification.canSendNow()).isTrue();
        }

        @Test
        @DisplayName("Deve permitir envio quando pendente e hora chegou")
        void devePermitirEnvioQuandoHoraChegou() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(Instant.now().minusSeconds(60));
            notification.setExpiraEm(Instant.now().plusSeconds(3600));

            assertThat(notification.canSendNow()).isTrue();
        }

        @Test
        @DisplayName("Não deve permitir envio quando agendada para futuro")
        void naoDevePermitirEnvioQuandoAgendadaFuturo() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(Instant.now().plusSeconds(3600));

            assertThat(notification.canSendNow()).isFalse();
        }

        @Test
        @DisplayName("Não deve permitir envio quando expirada")
        void naoDevePermitirEnvioQuandoExpirada() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setExpiraEm(Instant.now().minusSeconds(60));

            assertThat(notification.canSendNow()).isFalse();
        }

        @Test
        @DisplayName("Não deve permitir envio quando não está pendente")
        void naoDevePermitirEnvioQuandoNaoPendente() {
            notification.setStatus(NotificationStatus.SENT);

            assertThat(notification.canSendNow()).isFalse();
        }

        @Test
        @DisplayName("Deve permitir retry quando falhou e tem tentativas")
        void devePermitirRetryQuandoFalhouComTentativas() {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setTentativas(1);
            notification.setMaxTentativas(3);
            notification.setExpiraEm(Instant.now().plusSeconds(3600));

            assertThat(notification.canRetry()).isTrue();
        }

        @Test
        @DisplayName("Não deve permitir retry quando atingiu máximo de tentativas")
        void naoDevePermitirRetryQuandoAtingiuMaximo() {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setTentativas(3);
            notification.setMaxTentativas(3);

            assertThat(notification.canRetry()).isFalse();
        }

        @Test
        @DisplayName("Não deve permitir retry quando expirada")
        void naoDevePermitirRetryQuandoExpirada() {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setTentativas(1);
            notification.setMaxTentativas(3);
            notification.setExpiraEm(Instant.now().minusSeconds(60));

            assertThat(notification.canRetry()).isFalse();
        }

        @Test
        @DisplayName("Deve identificar notificação crítica")
        void deveIdentificarNotificacaoCritica() {
            notification.setType(NotificationType.VENCIMENTO_1_DIA);
            assertThat(notification.isCritical()).isTrue();

            notification.setType(NotificationType.APOLICE_VENCIDA);
            assertThat(notification.isCritical()).isTrue();

            notification.setType(NotificationType.APOLICE_CANCELADA);
            assertThat(notification.isCritical()).isTrue();
        }

        @Test
        @DisplayName("Não deve identificar notificação não crítica como crítica")
        void naoDeveIdentificarNaoCriticaComoCritica() {
            notification.setType(NotificationType.APOLICE_CRIADA);
            assertThat(notification.isCritical()).isFalse();

            notification.setType(NotificationType.COBERTURA_ADICIONADA);
            assertThat(notification.isCritical()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Operações de Estado")
    class OperacoesEstadoTests {

        @Test
        @DisplayName("Deve marcar como enviada corretamente")
        void deveMarcarComoEnviada() {
            String externalId = "ext-123";
            Instant antes = Instant.now();

            notification.markAsSent(externalId);

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getEnviadaEm()).isNotNull()
                .isAfterOrEqualTo(antes);
            assertThat(notification.getExternalId()).isEqualTo(externalId);
            assertThat(notification.getUltimoErro()).isNull();
        }

        @Test
        @DisplayName("Deve marcar como falha e incrementar tentativas")
        void deveMarcarComoFalhaEIncrementarTentativas() {
            notification.setTentativas(0);
            notification.setMaxTentativas(3);

            notification.markAsFailed("Erro de conexão");

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(notification.getUltimoErro()).isEqualTo("Erro de conexão");
            assertThat(notification.getTentativas()).isEqualTo(1);
        }

        @Test
        @DisplayName("Deve marcar como expirada quando atinge máximo de tentativas")
        void deveMarcarComoExpiradaQuandoAtingeMaximo() {
            notification.setTentativas(2);
            notification.setMaxTentativas(3);

            notification.markAsFailed("Erro final");

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.EXPIRED);
            assertThat(notification.getTentativas()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve cancelar notificação com motivo")
        void deveCancelarNotificacao() {
            notification.cancel("Cancelado pelo usuário");

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.CANCELLED);
            assertThat(notification.getUltimoErro()).isEqualTo("Cancelado pelo usuário");
        }

        @Test
        @DisplayName("Deve agendar para envio futuro")
        void deveAgendarParaEnvioFuturo() {
            Instant futuro = Instant.now().plusSeconds(3600);

            notification.scheduleFor(futuro);

            assertThat(notification.getAgendadaPara()).isEqualTo(futuro);
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.PENDING);
        }

        @Test
        @DisplayName("Deve calcular próximo retry com backoff exponencial")
        void deveCalcularProximoRetryComBackoff() {
            notification.setStatus(NotificationStatus.FAILED);
            notification.setMaxTentativas(3);
            notification.setExpiraEm(Instant.now().plusSeconds(7200));

            // Primeira tentativa: 5 minutos
            notification.setTentativas(0);
            Instant retry1 = notification.calculateNextRetry();
            assertThat(retry1).isNotNull();

            // Segunda tentativa: 15 minutos
            notification.setTentativas(1);
            Instant retry2 = notification.calculateNextRetry();
            assertThat(retry2).isNotNull();

            // Terceira tentativa: 45 minutos
            notification.setTentativas(2);
            Instant retry3 = notification.calculateNextRetry();
            assertThat(retry3).isNotNull();
        }

        @Test
        @DisplayName("Não deve calcular retry quando não pode reprocessar")
        void naoDeveCalcularRetryQuandoNaoPodeReprocessar() {
            notification.setStatus(NotificationStatus.SENT);

            assertThat(notification.calculateNextRetry()).isNull();
        }
    }

    @Nested
    @DisplayName("Testes de Parâmetros e Metadata")
    class ParametrosMetadataTests {

        @Test
        @DisplayName("Deve adicionar parâmetro")
        void deveAdicionarParametro() {
            notification.addParameter("key1", "value1");

            assertThat(notification.getMetadata()).containsEntry("key1", "value1");
        }

        @Test
        @DisplayName("Deve obter parâmetro existente")
        void deveObterParametroExistente() {
            notification.addParameter("key1", "value1");

            assertThat(notification.getParameter("key1")).isEqualTo("value1");
        }

        @Test
        @DisplayName("Deve retornar null para parâmetro inexistente")
        void deveRetornarNullParaParametroInexistente() {
            assertThat(notification.getParameter("inexistente")).isNull();
        }

        @Test
        @DisplayName("Deve inicializar metadata se for null ao adicionar parâmetro")
        void deveInicializarMetadataSeNull() {
            notification.setMetadata(null);

            notification.addParameter("key1", "value1");

            assertThat(notification.getMetadata()).isNotNull()
                .containsEntry("key1", "value1");
        }

        @Test
        @DisplayName("Deve adicionar múltiplos parâmetros")
        void deveAdicionarMultiplosParametros() {
            notification.addParameter("key1", "value1");
            notification.addParameter("key2", "value2");
            notification.addParameter("key3", "value3");

            assertThat(notification.getMetadata())
                .hasSize(3)
                .containsEntry("key1", "value1")
                .containsEntry("key2", "value2")
                .containsEntry("key3", "value3");
        }
    }

    @Nested
    @DisplayName("Testes de Callbacks JPA")
    class CallbacksJpaTests {

        @Test
        @DisplayName("Deve inicializar dados no onCreate")
        void deveInicializarDadosNoOnCreate() {
            ApoliceNotification notif = ApoliceNotification.builder()
                .id(ID)
                .apoliceId(APOLICE_ID)
                .seguradoId(SEGURADO_ID)
                .apoliceNumero(APOLICE_NUMERO)
                .seguradoCpf(SEGURADO_CPF)
                .seguradoNome(SEGURADO_NOME)
                .titulo("Teste")
                .mensagem("Mensagem")
                .type(NotificationType.VENCIMENTO_1_DIA)
                .build();

            notif.onCreate();

            assertThat(notif.getCriadaEm()).isNotNull();
            assertThat(notif.getPriority()).isEqualTo(NotificationType.VENCIMENTO_1_DIA.getPrioridade());
            assertThat(notif.getMetadata()).isNotNull();
        }

        @Test
        @DisplayName("Não deve sobrescrever criadaEm se já existir")
        void naoDeveSobrescreverCriadaEmSeExistir() {
            Instant criadaOriginal = Instant.now().minusSeconds(3600);
            notification.setCriadaEm(criadaOriginal);

            notification.onCreate();

            assertThat(notification.getCriadaEm()).isEqualTo(criadaOriginal);
        }

        @Test
        @DisplayName("Deve atualizar atualizadaEm no onUpdate")
        void deveAtualizarAtualizadaEmNoOnUpdate() {
            Instant antes = Instant.now();

            notification.onUpdate();

            assertThat(notification.getAtualizadaEm()).isNotNull()
                .isAfterOrEqualTo(antes);
        }
    }

    @Nested
    @DisplayName("Testes de Equals, HashCode e ToString")
    class EqualsHashCodeToStringTests {

        @Test
        @DisplayName("Deve ser igual quando mesmo ID")
        void deveSerIgualQuandoMesmoId() {
            ApoliceNotification notif1 = ApoliceNotification.builder()
                .id(ID)
                .apoliceId(APOLICE_ID)
                .seguradoId(SEGURADO_ID)
                .apoliceNumero(APOLICE_NUMERO)
                .seguradoCpf(SEGURADO_CPF)
                .seguradoNome(SEGURADO_NOME)
                .titulo("Teste")
                .mensagem("Mensagem")
                .build();

            ApoliceNotification notif2 = ApoliceNotification.builder()
                .id(ID)
                .apoliceId("outro-id")
                .seguradoId("outro-segurado")
                .apoliceNumero("OUTRO-NUM")
                .seguradoCpf("98765432100")
                .seguradoNome("Maria")
                .titulo("Outro")
                .mensagem("Outra mensagem")
                .build();

            assertThat(notif1).isEqualTo(notif2);
            assertThat(notif1.hashCode()).isEqualTo(notif2.hashCode());
        }

        @Test
        @DisplayName("Não deve ser igual quando IDs diferentes")
        void naoDeveSerIgualQuandoIdsDiferentes() {
            ApoliceNotification notif1 = ApoliceNotification.builder()
                .id("id-1")
                .apoliceId(APOLICE_ID)
                .seguradoId(SEGURADO_ID)
                .apoliceNumero(APOLICE_NUMERO)
                .seguradoCpf(SEGURADO_CPF)
                .seguradoNome(SEGURADO_NOME)
                .titulo("Teste")
                .mensagem("Mensagem")
                .build();

            ApoliceNotification notif2 = ApoliceNotification.builder()
                .id("id-2")
                .apoliceId(APOLICE_ID)
                .seguradoId(SEGURADO_ID)
                .apoliceNumero(APOLICE_NUMERO)
                .seguradoCpf(SEGURADO_CPF)
                .seguradoNome(SEGURADO_NOME)
                .titulo("Teste")
                .mensagem("Mensagem")
                .build();

            assertThat(notif1).isNotEqualTo(notif2);
        }

        @Test
        @DisplayName("Deve ser igual a si mesmo")
        void deveSerIgualASiMesmo() {
            assertThat(notification).isEqualTo(notification);
        }

        @Test
        @DisplayName("Não deve ser igual a null")
        void naoDeveSerIgualANull() {
            assertThat(notification).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Não deve ser igual a objeto de outra classe")
        void naoDeveSerIgualAObjetoDeOutraClasse() {
            assertThat(notification).isNotEqualTo("string");
        }

        @Test
        @DisplayName("ToString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            String toString = notification.toString();

            assertThat(toString)
                .contains("ApoliceNotification")
                .contains(ID)
                .contains(APOLICE_NUMERO)
                .contains("APOLICE_CRIADA")
                .contains("EMAIL")
                .matches(".*(?i)(pending|pendente).*"); // Match either English or Portuguese
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Complexos")
    class CenariosComplexosTests {

        @Test
        @DisplayName("Deve processar ciclo completo de envio com sucesso")
        void deveProcessarCicloCompletoComSucesso() {
            // 1. Criar pendente
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(Instant.now().minusSeconds(60));

            assertThat(notification.canSendNow()).isTrue();

            // 2. Processar
            notification.setStatus(NotificationStatus.PROCESSING);

            // 3. Marcar como enviada
            notification.markAsSent("ext-123");

            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.SENT);
            assertThat(notification.getEnviadaEm()).isNotNull();
            assertThat(notification.canSendNow()).isFalse();
            assertThat(notification.canRetry()).isFalse();
        }

        @Test
        @DisplayName("Deve processar ciclo com falhas e retry")
        void deveProcessarCicloComFalhasERetry() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setTentativas(0);
            notification.setMaxTentativas(3);
            notification.setExpiraEm(Instant.now().plusSeconds(7200));

            // Primeira falha
            notification.markAsFailed("Erro 1");
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.FAILED);
            assertThat(notification.getTentativas()).isEqualTo(1);
            assertThat(notification.canRetry()).isTrue();

            // Reagendar
            Instant retry1 = notification.calculateNextRetry();
            notification.setStatus(NotificationStatus.PENDING);
            notification.setAgendadaPara(retry1);

            // Segunda falha
            notification.markAsFailed("Erro 2");
            assertThat(notification.getTentativas()).isEqualTo(2);
            assertThat(notification.canRetry()).isTrue();

            // Terceira falha (última)
            notification.markAsFailed("Erro 3");
            assertThat(notification.getStatus()).isEqualTo(NotificationStatus.EXPIRED);
            assertThat(notification.getTentativas()).isEqualTo(3);
            assertThat(notification.canRetry()).isFalse();
        }

        @Test
        @DisplayName("Deve lidar com expiração durante processamento")
        void deveLidarComExpiracaoDuranteProcessamento() {
            notification.setStatus(NotificationStatus.PENDING);
            notification.setExpiraEm(Instant.now().plusSeconds(5));
            notification.setAgendadaPara(Instant.now());

            assertThat(notification.canSendNow()).isTrue();

            // Simular passagem do tempo
            try {
                Thread.sleep(6000); // 6 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }

            assertThat(notification.isExpired()).isTrue();
            assertThat(notification.canSendNow()).isFalse();
            assertThat(notification.canRetry()).isFalse();
        }

        @Test
        @DisplayName("Deve manter metadata ao longo do ciclo de vida")
        void deveManterMetadataAoLongoDoCiclo() {
            Map<String, String> params = new HashMap<>();
            params.put("numeroApolice", APOLICE_NUMERO);
            params.put("seguradoNome", SEGURADO_NOME);
            params.put("valorTotal", "50000.00");

            notification.setMetadata(params);

            // Processar
            notification.setStatus(NotificationStatus.PROCESSING);
            assertThat(notification.getMetadata()).isEqualTo(params);

            // Falhar
            notification.markAsFailed("Erro temporário");
            assertThat(notification.getMetadata()).isEqualTo(params);

            // Enviar com sucesso
            notification.setStatus(NotificationStatus.PENDING);
            notification.markAsSent("ext-123");
            assertThat(notification.getMetadata()).isEqualTo(params);
        }
    }
}
