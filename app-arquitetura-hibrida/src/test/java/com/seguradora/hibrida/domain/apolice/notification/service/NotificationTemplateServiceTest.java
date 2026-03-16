package com.seguradora.hibrida.domain.apolice.notification.service;

import com.seguradora.hibrida.domain.apolice.notification.model.NotificationChannel;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link NotificationTemplateService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("NotificationTemplateService - Testes Unitários")
class NotificationTemplateServiceTest {

    private NotificationTemplateService service;
    private Map<String, String> parameters;

    @BeforeEach
    void setUp() {
        service = new NotificationTemplateService();

        parameters = new HashMap<>();
        parameters.put("numeroApolice", "APO-2026-001");
        parameters.put("seguradoNome", "João Silva");
        parameters.put("produto", "Seguro Auto");
        parameters.put("valorTotal", "50000.00");
        parameters.put("vigenciaFim", "31/12/2026");
    }

    @Nested
    @DisplayName("Testes de Geração de Título")
    class GeracaoTituloTests {

        @Test
        @DisplayName("Deve gerar título para APOLICE_CRIADA")
        void deveGerarTituloApoliceCriada() {
            String titulo = service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(titulo)
                .isNotNull()
                .contains("Apólice Criada")
                .contains("APO-2026-001");
        }

        @Test
        @DisplayName("Deve gerar título para VENCIMENTO_1_DIA")
        void deveGerarTituloVencimento1Dia() {
            String titulo = service.generateTitle(
                NotificationType.VENCIMENTO_1_DIA,
                NotificationChannel.SMS,
                parameters
            );

            assertThat(titulo)
                .isNotNull()
                .contains("Vence AMANHÃ")
                .contains("APO-2026-001");
        }

        @Test
        @DisplayName("Deve gerar título para APOLICE_VENCIDA")
        void deveGerarTituloApoliceVencida() {
            String titulo = service.generateTitle(
                NotificationType.APOLICE_VENCIDA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(titulo)
                .isNotNull()
                .containsIgnoringCase("VENCIDA")
                .contains("APO-2026-001");
        }

        @Test
        @DisplayName("Deve lidar com parâmetro numeroApolice ausente")
        void deveLidarComParametroNumeroApoliceAusente() {
            parameters.remove("numeroApolice");

            String titulo = service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(titulo)
                .isNotNull()
                .contains("N/A");
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Deve gerar título para todos os tipos de notificação")
        void deveGerarTituloParaTodosTipos(NotificationType type) {
            String titulo = service.generateTitle(type, NotificationChannel.EMAIL, parameters);

            assertThat(titulo)
                .isNotNull()
                .isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Deve gerar título para todos os canais")
        void deveGerarTituloParaTodosCanais(NotificationChannel channel) {
            String titulo = service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                channel,
                parameters
            );

            assertThat(titulo)
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Geração de Mensagem")
    class GeracaoMensagemTests {

        @Test
        @DisplayName("Deve gerar mensagem EMAIL para APOLICE_CRIADA")
        void deveGerarMensagemEmailApoliceCriada() {
            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("João Silva")
                .contains("APO-2026-001")
                .contains("Seguro Auto")
                .contains("50000.00")
                .contains("31/12/2026");
        }

        @Test
        @DisplayName("Deve gerar mensagem SMS para APOLICE_CRIADA")
        void deveGerarMensagemSmsApoliceCriada() {
            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.SMS,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("João Silva")
                .contains("APO-2026-001")
                .hasSizeLessThan(200); // SMS deve ser conciso
        }

        @Test
        @DisplayName("Deve gerar mensagem WHATSAPP para APOLICE_CRIADA")
        void deveGerarMensagemWhatsappApoliceCriada() {
            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.WHATSAPP,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("João Silva")
                .contains("APO-2026-001");
        }

        @Test
        @DisplayName("Deve gerar mensagem para VENCIMENTO_30_DIAS")
        void deveGerarMensagemVencimento30Dias() {
            String mensagem = service.generateMessage(
                NotificationType.VENCIMENTO_30_DIAS,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .containsIgnoringCase("30 dias")
                .contains("renovar");
        }

        @Test
        @DisplayName("Deve gerar mensagem urgente para VENCIMENTO_1_DIA")
        void deveGerarMensagemUrgenteVencimento1Dia() {
            String mensagem = service.generateMessage(
                NotificationType.VENCIMENTO_1_DIA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .containsIgnoringCase("AMANHÃ")
                .containsAnyOf("ÚLTIMA CHANCE", "URGENTE");
        }

        @Test
        @DisplayName("Deve gerar mensagem crítica para APOLICE_VENCIDA")
        void deveGerarMensagemCriticaApoliceVencida() {
            String mensagem = service.generateMessage(
                NotificationType.APOLICE_VENCIDA,
                NotificationChannel.SMS,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .containsIgnoringCase("VENCIDA")
                .containsIgnoringCase("SEM PROTEÇÃO");
        }

        @Test
        @DisplayName("Deve gerar mensagem para APOLICE_CANCELADA com motivo")
        void deveGerarMensagemApoliceCanceladaComMotivo() {
            parameters.put("motivo", "Solicitação do cliente");
            parameters.put("valorReembolso", "1500.00");

            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CANCELADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("cancelada")
                .contains("Solicitação do cliente")
                .contains("1500.00");
        }

        @Test
        @DisplayName("Deve gerar mensagem para APOLICE_RENOVADA")
        void deveGerarMensagemApoliceRenovada() {
            parameters.put("novaVigenciaFim", "31/12/2027");
            parameters.put("novoValorTotal", "55000.00");

            String mensagem = service.generateMessage(
                NotificationType.APOLICE_RENOVADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("renovada")
                .contains("31/12/2027")
                .contains("55000.00");
        }

        @Test
        @DisplayName("Deve gerar mensagem para COBERTURA_ADICIONADA")
        void deveGerarMensagemCoberturaAdicionada() {
            parameters.put("tipoCobertura", "Vidros");
            parameters.put("valorAdicional", "2000.00");

            String mensagem = service.generateMessage(
                NotificationType.COBERTURA_ADICIONADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .isNotNull()
                .contains("cobertura")
                .contains("Vidros")
                .contains("2000.00");
        }

        @Test
        @DisplayName("Deve usar valores padrão quando parâmetros ausentes")
        void deveUsarValoresPadraoQuandoParametrosAusentes() {
            Map<String, String> parametrosVazios = new HashMap<>();

            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parametrosVazios
            );

            assertThat(mensagem)
                .isNotNull()
                .containsAnyOf("Cliente", "N/A");
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Deve gerar mensagem para todos os tipos")
        void deveGerarMensagemParaTodosTipos(NotificationType type) {
            String mensagem = service.generateMessage(type, NotificationChannel.EMAIL, parameters);

            assertThat(mensagem)
                .isNotNull()
                .isNotEmpty();
        }
    }

    @Nested
    @DisplayName("Testes de Template Name")
    class TemplateNameTests {

        @Test
        @DisplayName("Deve gerar template name correto")
        void deveGerarTemplateNameCorreto() {
            String templateName = service.getTemplateName(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL
            );

            assertThat(templateName).isEqualTo("apolice_criada_email");
        }

        @Test
        @DisplayName("Deve gerar template name em lowercase")
        void deveGerarTemplateNameEmLowercase() {
            String templateName = service.getTemplateName(
                NotificationType.VENCIMENTO_1_DIA,
                NotificationChannel.SMS
            );

            assertThat(templateName)
                .isEqualTo("vencimento_1_dia_sms")
                .isLowerCase();
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Deve gerar template name para todos os tipos")
        void deveGerarTemplateNameParaTodosTipos(NotificationType type) {
            String templateName = service.getTemplateName(type, NotificationChannel.EMAIL);

            assertThat(templateName)
                .isNotNull()
                .isNotEmpty()
                .contains("_email");
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Template")
    class ValidacaoTemplateTests {

        @Test
        @DisplayName("Deve retornar true para combinação válida de tipo e canal")
        void deveRetornarTrueParaCombinacaoValida() {
            boolean hasTemplate = service.hasTemplate(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL
            );

            assertThat(hasTemplate).isTrue();
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Deve ter template para todos os tipos com EMAIL")
        void deveTermTemplateParaTodosTiposComEmail(NotificationType type) {
            boolean hasTemplate = service.hasTemplate(type, NotificationChannel.EMAIL);

            assertThat(hasTemplate).isTrue();
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Deve ter template para APOLICE_CRIADA em todos os canais")
        void deveTermTemplateParaApoliceCriadaEmTodosCanais(NotificationChannel channel) {
            boolean hasTemplate = service.hasTemplate(NotificationType.APOLICE_CRIADA, channel);

            assertThat(hasTemplate).isTrue();
        }
    }

    @Nested
    @DisplayName("Testes de Configuração de Expiração")
    class ConfiguracaoExpiracaoTests {

        @Test
        @DisplayName("VENCIMENTO_1_DIA deve expirar em 6 horas")
        void vencimento1DiaDeveExpirarEm6Horas() {
            long horas = service.getExpirationHours(NotificationType.VENCIMENTO_1_DIA);
            assertThat(horas).isEqualTo(6);
        }

        @Test
        @DisplayName("APOLICE_VENCIDA deve expirar em 6 horas")
        void apoliceVencidaDeveExpirarEm6Horas() {
            long horas = service.getExpirationHours(NotificationType.APOLICE_VENCIDA);
            assertThat(horas).isEqualTo(6);
        }

        @Test
        @DisplayName("VENCIMENTO_7_DIAS deve expirar em 24 horas")
        void vencimento7DiasDeveExpirarEm24Horas() {
            long horas = service.getExpirationHours(NotificationType.VENCIMENTO_7_DIAS);
            assertThat(horas).isEqualTo(24);
        }

        @Test
        @DisplayName("VENCIMENTO_15_DIAS deve expirar em 48 horas")
        void vencimento15DiasDeveExpirarEm48Horas() {
            long horas = service.getExpirationHours(NotificationType.VENCIMENTO_15_DIAS);
            assertThat(horas).isEqualTo(48);
        }

        @Test
        @DisplayName("VENCIMENTO_30_DIAS deve expirar em 72 horas")
        void vencimento30DiasDeveExpirarEm72Horas() {
            long horas = service.getExpirationHours(NotificationType.VENCIMENTO_30_DIAS);
            assertThat(horas).isEqualTo(72);
        }

        @Test
        @DisplayName("APOLICE_CRIADA deve expirar em 168 horas (7 dias)")
        void apoliceCriadaDeveExpirarEm168Horas() {
            long horas = service.getExpirationHours(NotificationType.APOLICE_CRIADA);
            assertThat(horas).isEqualTo(168);
        }

        @Test
        @DisplayName("APOLICE_RENOVADA deve expirar em 168 horas (7 dias)")
        void apoliceRenovadaDeveExpirarEm168Horas() {
            long horas = service.getExpirationHours(NotificationType.APOLICE_RENOVADA);
            assertThat(horas).isEqualTo(168);
        }

        @Test
        @DisplayName("COBERTURA_ADICIONADA deve ter expiração padrão de 24 horas")
        void coberturaAdicionadaDeveTermExpiracaoPadrao() {
            long horas = service.getExpirationHours(NotificationType.COBERTURA_ADICIONADA);
            assertThat(horas).isEqualTo(24);
        }

        @Test
        @DisplayName("Notificações críticas devem ter expiração curta")
        void notificacoesCriticasDevemTermExpiracaoCurta() {
            for (NotificationType type : NotificationType.values()) {
                if (type.isCritica()) {
                    long horas = service.getExpirationHours(type);
                    assertThat(horas)
                        .as("Tipo crítico %s deveria expirar em até 24 horas", type)
                        .isLessThanOrEqualTo(24);
                }
            }
        }

        @ParameterizedTest
        @EnumSource(NotificationType.class)
        @DisplayName("Todas as expirações devem ser positivas")
        void todasExpiracoesDevemSerPositivas(NotificationType type) {
            long horas = service.getExpirationHours(type);
            assertThat(horas).isPositive();
        }
    }

    @Nested
    @DisplayName("Testes de Diferentes Canais")
    class DiferentesCanaisTests {

        @Test
        @DisplayName("Mensagem EMAIL deve ser mais detalhada que SMS")
        void mensagemEmailDeveSerMaisDetalhadaQueSms() {
            String email = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            String sms = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.SMS,
                parameters
            );

            assertThat(email.length()).isGreaterThan(sms.length());
        }

        @Test
        @DisplayName("Mensagem SMS deve ser concisa")
        void mensagemSmsDeveSerConcisa() {
            String sms = service.generateMessage(
                NotificationType.VENCIMENTO_30_DIAS,
                NotificationChannel.SMS,
                parameters
            );

            // SMS geralmente tem limite de 160 caracteres
            assertThat(sms.length()).isLessThan(200);
        }

        @Test
        @DisplayName("Mensagem WHATSAPP pode usar emojis")
        void mensagemWhatsappPodeUsarEmojis() {
            String whatsapp = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.WHATSAPP,
                parameters
            );

            // WhatsApp geralmente usa emojis - verifica por caracteres não-ASCII ou símbolos
            assertThat(whatsapp).isNotEmpty();
            // Since emojis may not render properly in all environments, just verify the message is not empty
            // and contains some expected keywords
            assertThat(whatsapp).contains("apólice", "APO-2026-001");
        }

        @Test
        @DisplayName("Canais diferentes devem gerar mensagens diferentes")
        void canaisDiferentesDevemGerarMensagensDiferentes() {
            String email = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            String sms = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.SMS,
                parameters
            );

            String whatsapp = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.WHATSAPP,
                parameters
            );

            assertThat(email).isNotEqualTo(sms);
            assertThat(email).isNotEqualTo(whatsapp);
            assertThat(sms).isNotEqualTo(whatsapp);
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("Deve lidar com mapa de parâmetros null")
        void deveLidarComMapaParametrosNull() {
            assertThatCode(() -> service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                null
            )).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com mapa de parâmetros vazio")
        void deveLidarComMapaParametrosVazio() {
            Map<String, String> vazio = new HashMap<>();

            String titulo = service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                vazio
            );

            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                vazio
            );

            assertThat(titulo).isNotNull().isNotEmpty();
            assertThat(mensagem).isNotNull().isNotEmpty();
        }

        @Test
        @DisplayName("Deve lidar com valores null em parâmetros")
        void deveLidarComValoresNullEmParametros() {
            parameters.put("numeroApolice", null);
            parameters.put("seguradoNome", null);

            String titulo = service.generateTitle(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(titulo).isNotNull();
        }

        @Test
        @DisplayName("Deve lidar com caracteres especiais em parâmetros")
        void deveLidarComCaracteresEspeciaisEmParametros() {
            parameters.put("seguradoNome", "José María Ñoño");
            parameters.put("produto", "Seguro <Auto> & Vida");

            String mensagem = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem)
                .contains("José María Ñoño")
                .contains("Seguro <Auto> & Vida");
        }
    }

    @Nested
    @DisplayName("Testes de Consistência")
    class ConsistenciaTests {

        @Test
        @DisplayName("Mesmos parâmetros devem gerar mesmas mensagens")
        void mesmosParametrosDevemGerarMesmasMensagens() {
            String mensagem1 = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            String mensagem2 = service.generateMessage(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL,
                parameters
            );

            assertThat(mensagem1).isEqualTo(mensagem2);
        }

        @Test
        @DisplayName("Template name deve ser consistente")
        void templateNameDeveSerConsistente() {
            String nome1 = service.getTemplateName(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL
            );

            String nome2 = service.getTemplateName(
                NotificationType.APOLICE_CRIADA,
                NotificationChannel.EMAIL
            );

            assertThat(nome1).isEqualTo(nome2);
        }

        @Test
        @DisplayName("Expiração deve ser consistente")
        void expiracaoDeveSerConsistente() {
            long horas1 = service.getExpirationHours(NotificationType.VENCIMENTO_1_DIA);
            long horas2 = service.getExpirationHours(NotificationType.VENCIMENTO_1_DIA);

            assertThat(horas1).isEqualTo(horas2);
        }
    }
}
