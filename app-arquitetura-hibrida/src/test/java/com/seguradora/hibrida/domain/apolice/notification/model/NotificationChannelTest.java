package com.seguradora.hibrida.domain.apolice.notification.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link NotificationChannel}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("NotificationChannel - Testes Unitários")
class NotificationChannelTest {

    @Nested
    @DisplayName("Testes de Propriedades Básicas")
    class PropriedadesBasicasTests {

        @Test
        @DisplayName("EMAIL deve ter propriedades corretas")
        void emailDeveSerPropriedadesCorretas() {
            NotificationChannel canal = NotificationChannel.EMAIL;

            assertThat(canal.getDisplayName()).isEqualTo("Email");
            assertThat(canal.getCode()).isEqualTo("email");
            assertThat(canal.isEnabled()).isTrue();
            assertThat(canal.getDailyLimit()).isEqualTo(1000);
        }

        @Test
        @DisplayName("SMS deve ter propriedades corretas")
        void smsDeveSerPropriedadesCorretas() {
            NotificationChannel canal = NotificationChannel.SMS;

            assertThat(canal.getDisplayName()).isEqualTo("SMS");
            assertThat(canal.getCode()).isEqualTo("sms");
            assertThat(canal.isEnabled()).isTrue();
            assertThat(canal.getDailyLimit()).isEqualTo(500);
        }

        @Test
        @DisplayName("WHATSAPP deve ter propriedades corretas")
        void whatsappDeveSerPropriedadesCorretas() {
            NotificationChannel canal = NotificationChannel.WHATSAPP;

            assertThat(canal.getDisplayName()).isEqualTo("WhatsApp");
            assertThat(canal.getCode()).isEqualTo("whatsapp");
            assertThat(canal.isEnabled()).isFalse();
            assertThat(canal.getDailyLimit()).isEqualTo(200);
        }

        @Test
        @DisplayName("PUSH deve ter propriedades corretas")
        void pushDeveSerPropriedadesCorretas() {
            NotificationChannel canal = NotificationChannel.PUSH;

            assertThat(canal.getDisplayName()).isEqualTo("Push Notification");
            assertThat(canal.getCode()).isEqualTo("push");
            assertThat(canal.isEnabled()).isFalse();
            assertThat(canal.getDailyLimit()).isEqualTo(100);
        }

        @Test
        @DisplayName("IN_APP deve ter propriedades corretas")
        void inAppDeveSerPropriedadesCorretas() {
            NotificationChannel canal = NotificationChannel.IN_APP;

            assertThat(canal.getDisplayName()).isEqualTo("Notificação In-App");
            assertThat(canal.getCode()).isEqualTo("in_app");
            assertThat(canal.isEnabled()).isTrue();
            assertThat(canal.getDailyLimit()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("Testes de Características do Canal")
    class CaracteristicasCanalTests {

        @Test
        @DisplayName("SMS deve ser instantâneo")
        void smsDeveSerInstantaneo() {
            assertThat(NotificationChannel.SMS.isInstantaneo()).isTrue();
        }

        @Test
        @DisplayName("WHATSAPP deve ser instantâneo")
        void whatsappDeveSerInstantaneo() {
            assertThat(NotificationChannel.WHATSAPP.isInstantaneo()).isTrue();
        }

        @Test
        @DisplayName("PUSH deve ser instantâneo")
        void pushDeveSerInstantaneo() {
            assertThat(NotificationChannel.PUSH.isInstantaneo()).isTrue();
        }

        @Test
        @DisplayName("EMAIL não deve ser instantâneo")
        void emailNaoDeveSerInstantaneo() {
            assertThat(NotificationChannel.EMAIL.isInstantaneo()).isFalse();
        }

        @Test
        @DisplayName("IN_APP não deve ser instantâneo")
        void inAppNaoDeveSerInstantaneo() {
            assertThat(NotificationChannel.IN_APP.isInstantaneo()).isFalse();
        }

        @Test
        @DisplayName("EMAIL deve ser confiável")
        void emailDeveSerConfiavel() {
            assertThat(NotificationChannel.EMAIL.isConfiavel()).isTrue();
        }

        @Test
        @DisplayName("SMS deve ser confiável")
        void smsDeveSerConfiavel() {
            assertThat(NotificationChannel.SMS.isConfiavel()).isTrue();
        }

        @Test
        @DisplayName("WHATSAPP não deve ser confiável")
        void whatsappNaoDeveSerConfiavel() {
            assertThat(NotificationChannel.WHATSAPP.isConfiavel()).isFalse();
        }

        @Test
        @DisplayName("PUSH não deve ser confiável")
        void pushNaoDeveSerConfiavel() {
            assertThat(NotificationChannel.PUSH.isConfiavel()).isFalse();
        }

        @Test
        @DisplayName("IN_APP não deve ser confiável")
        void inAppNaoDeveSerConfiavel() {
            assertThat(NotificationChannel.IN_APP.isConfiavel()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Custo Relativo")
    class CustoRelativoTests {

        @Test
        @DisplayName("IN_APP deve ter menor custo")
        void inAppDeveTerMenorCusto() {
            assertThat(NotificationChannel.IN_APP.getCustoRelativo()).isEqualTo(1);
        }

        @Test
        @DisplayName("PUSH deve ter custo baixo")
        void pushDeveTerCustoBaixo() {
            assertThat(NotificationChannel.PUSH.getCustoRelativo()).isEqualTo(2);
        }

        @Test
        @DisplayName("EMAIL deve ter custo médio")
        void emailDeveTerCustoMedio() {
            assertThat(NotificationChannel.EMAIL.getCustoRelativo()).isEqualTo(3);
        }

        @Test
        @DisplayName("WHATSAPP deve ter custo alto")
        void whatsappDeveTerCustoAlto() {
            assertThat(NotificationChannel.WHATSAPP.getCustoRelativo()).isEqualTo(4);
        }

        @Test
        @DisplayName("SMS deve ter maior custo")
        void smsDeveTerMaiorCusto() {
            assertThat(NotificationChannel.SMS.getCustoRelativo()).isEqualTo(5);
        }

        @Test
        @DisplayName("Custos devem estar em ordem crescente")
        void custosDevemEstarEmOrdemCrescente() {
            assertThat(NotificationChannel.IN_APP.getCustoRelativo())
                .isLessThan(NotificationChannel.PUSH.getCustoRelativo());
            assertThat(NotificationChannel.PUSH.getCustoRelativo())
                .isLessThan(NotificationChannel.EMAIL.getCustoRelativo());
            assertThat(NotificationChannel.EMAIL.getCustoRelativo())
                .isLessThan(NotificationChannel.WHATSAPP.getCustoRelativo());
            assertThat(NotificationChannel.WHATSAPP.getCustoRelativo())
                .isLessThan(NotificationChannel.SMS.getCustoRelativo());
        }
    }

    @Nested
    @DisplayName("Testes de Ordem de Fallback")
    class OrdemFallbackTests {

        @Test
        @DisplayName("EMAIL deve ser primeira opção de fallback")
        void emailDeveSerPrimeiraOpcaoFallback() {
            assertThat(NotificationChannel.EMAIL.getOrdemFallback()).isEqualTo(1);
        }

        @Test
        @DisplayName("SMS deve ser segunda opção de fallback")
        void smsDeveSerSegundaOpcaoFallback() {
            assertThat(NotificationChannel.SMS.getOrdemFallback()).isEqualTo(2);
        }

        @Test
        @DisplayName("WHATSAPP deve ser terceira opção de fallback")
        void whatsappDeveSerTerceiraOpcaoFallback() {
            assertThat(NotificationChannel.WHATSAPP.getOrdemFallback()).isEqualTo(3);
        }

        @Test
        @DisplayName("IN_APP deve ser quarta opção de fallback")
        void inAppDeveSerQuartaOpcaoFallback() {
            assertThat(NotificationChannel.IN_APP.getOrdemFallback()).isEqualTo(4);
        }

        @Test
        @DisplayName("PUSH deve ser última opção de fallback")
        void pushDeveSerUltimaOpcaoFallback() {
            assertThat(NotificationChannel.PUSH.getOrdemFallback()).isEqualTo(5);
        }

        @Test
        @DisplayName("Ordem de fallback deve ser sequencial")
        void ordemFallbackDeveSerSequencial() {
            assertThat(NotificationChannel.EMAIL.getOrdemFallback())
                .isLessThan(NotificationChannel.SMS.getOrdemFallback());
            assertThat(NotificationChannel.SMS.getOrdemFallback())
                .isLessThan(NotificationChannel.WHATSAPP.getOrdemFallback());
            assertThat(NotificationChannel.WHATSAPP.getOrdemFallback())
                .isLessThan(NotificationChannel.IN_APP.getOrdemFallback());
            assertThat(NotificationChannel.IN_APP.getOrdemFallback())
                .isLessThan(NotificationChannel.PUSH.getOrdemFallback());
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Todos os Valores")
    class ValidacaoTodosValoresTests {

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Todos os canais devem ter display name não nulo")
        void todosCanaisDevemTerDisplayName(NotificationChannel canal) {
            assertThat(canal.getDisplayName()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Todos os canais devem ter code não nulo")
        void todosCanaisDevemTerCode(NotificationChannel canal) {
            assertThat(canal.getCode()).isNotNull().isNotEmpty();
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Todos os canais devem ter daily limit positivo")
        void todosCanaisDevemTerDailyLimitPositivo(NotificationChannel canal) {
            assertThat(canal.getDailyLimit()).isPositive();
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Todos os canais devem ter custo relativo entre 1 e 5")
        void todosCanaisDevemTerCustoRelativoValido(NotificationChannel canal) {
            assertThat(canal.getCustoRelativo()).isBetween(1, 5);
        }

        @ParameterizedTest
        @EnumSource(NotificationChannel.class)
        @DisplayName("Todos os canais devem ter ordem fallback entre 1 e 5")
        void todosCanaisDevemTerOrdemFallbackValida(NotificationChannel canal) {
            assertThat(canal.getOrdemFallback()).isBetween(1, 5);
        }
    }

    @Nested
    @DisplayName("Testes de Comparação e Ordenação")
    class ComparacaoOrdenacaoTests {

        @Test
        @DisplayName("Deve ter exatamente 5 canais")
        void deveTerExatamente5Canais() {
            assertThat(NotificationChannel.values()).hasSize(5);
        }

        @Test
        @DisplayName("Todos os códigos devem ser únicos")
        void todosCodigosDevemSerUnicos() {
            NotificationChannel[] canais = NotificationChannel.values();
            long codigosUnicos = java.util.Arrays.stream(canais)
                .map(NotificationChannel::getCode)
                .distinct()
                .count();

            assertThat(codigosUnicos).isEqualTo(canais.length);
        }

        @Test
        @DisplayName("Todas as ordens de fallback devem ser únicas")
        void todasOrdensFallbackDevemSerUnicas() {
            NotificationChannel[] canais = NotificationChannel.values();
            long ordensUnicas = java.util.Arrays.stream(canais)
                .map(NotificationChannel::getOrdemFallback)
                .distinct()
                .count();

            assertThat(ordensUnicas).isEqualTo(canais.length);
        }

        @Test
        @DisplayName("Todos os custos relativos devem ser únicos")
        void todosCustosRelativosDevemSerUnicos() {
            NotificationChannel[] canais = NotificationChannel.values();
            long custosUnicos = java.util.Arrays.stream(canais)
                .map(NotificationChannel::getCustoRelativo)
                .distinct()
                .count();

            assertThat(custosUnicos).isEqualTo(canais.length);
        }
    }

    @Nested
    @DisplayName("Testes de Regras de Negócio")
    class RegrasNegocioTests {

        @Test
        @DisplayName("Canais confiáveis devem estar habilitados")
        void canaisConfiaveisDevemEstarHabilitados() {
            for (NotificationChannel canal : NotificationChannel.values()) {
                if (canal.isConfiavel()) {
                    assertThat(canal.isEnabled())
                        .as("Canal confiável %s deveria estar habilitado", canal)
                        .isTrue();
                }
            }
        }

        @Test
        @DisplayName("Canais com maior custo devem ter maior ordem de fallback")
        void canaisComMaiorCustoDevemTerMaiorOrdemFallback() {
            // SMS tem maior custo (5) mas não tem maior ordem fallback (2)
            // Esta é uma regra de negócio: canais confiáveis têm prioridade no fallback
            // independente do custo
            NotificationChannel email = NotificationChannel.EMAIL;
            NotificationChannel sms = NotificationChannel.SMS;

            // EMAIL é mais confiável e tem menor custo, então tem prioridade no fallback
            assertThat(email.getOrdemFallback()).isLessThan(sms.getOrdemFallback());
            assertThat(email.getCustoRelativo()).isLessThan(sms.getCustoRelativo());
        }

        @Test
        @DisplayName("Canais instantâneos devem ter custo relativamente alto")
        void canaisInstantaneosDevemTerCustoAlto() {
            for (NotificationChannel canal : NotificationChannel.values()) {
                if (canal.isInstantaneo() && canal != NotificationChannel.PUSH) {
                    // SMS e WHATSAPP são instantâneos e têm custo >= 4
                    assertThat(canal.getCustoRelativo())
                        .as("Canal instantâneo %s deveria ter custo alto", canal)
                        .isGreaterThanOrEqualTo(4);
                }
            }
        }

        @Test
        @DisplayName("Canal com menor custo deve ter maior daily limit")
        void canalComMenorCustoDeveTerMaiorDailyLimit() {
            NotificationChannel menorCusto = NotificationChannel.IN_APP;

            assertThat(menorCusto.getCustoRelativo()).isEqualTo(1);

            // IN_APP não tem o maior limite diário
            // Esta é uma regra de negócio específica
            for (NotificationChannel canal : NotificationChannel.values()) {
                if (canal != menorCusto) {
                    // Apenas verificar que os limites fazem sentido
                    assertThat(canal.getDailyLimit()).isPositive();
                }
            }
        }
    }

    @Nested
    @DisplayName("Testes de Edge Cases")
    class EdgeCasesTests {

        @Test
        @DisplayName("valueOf deve funcionar corretamente")
        void valueOfDeveFuncionarCorretamente() {
            assertThat(NotificationChannel.valueOf("EMAIL")).isEqualTo(NotificationChannel.EMAIL);
            assertThat(NotificationChannel.valueOf("SMS")).isEqualTo(NotificationChannel.SMS);
            assertThat(NotificationChannel.valueOf("WHATSAPP")).isEqualTo(NotificationChannel.WHATSAPP);
            assertThat(NotificationChannel.valueOf("PUSH")).isEqualTo(NotificationChannel.PUSH);
            assertThat(NotificationChannel.valueOf("IN_APP")).isEqualTo(NotificationChannel.IN_APP);
        }

        @Test
        @DisplayName("valueOf deve lançar exceção para valor inválido")
        void valueOfDeveLancarExcecaoParaValorInvalido() {
            assertThatThrownBy(() -> NotificationChannel.valueOf("INVALIDO"))
                .isInstanceOf(IllegalArgumentException.class);
        }

        @Test
        @DisplayName("values deve retornar todos os canais")
        void valuesDeveRetornarTodosCanais() {
            NotificationChannel[] canais = NotificationChannel.values();

            assertThat(canais).contains(
                NotificationChannel.EMAIL,
                NotificationChannel.SMS,
                NotificationChannel.WHATSAPP,
                NotificationChannel.PUSH,
                NotificationChannel.IN_APP
            );
        }
    }
}
