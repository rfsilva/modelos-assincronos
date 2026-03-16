package com.seguradora.hibrida.domain.veiculo.relationship.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link RelationshipAlertService}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("RelationshipAlertService - Testes Unitários")
class RelationshipAlertServiceTest {

    @InjectMocks
    private RelationshipAlertService alertService;

    @Nested
    @DisplayName("Testes de alertarVeiculoSemCobertura")
    class AlertarVeiculoSemCoberturaTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.alertarVeiculoSemCobertura(
                    "VEI-001",
                    "ABC1234",
                    "12345678909",
                    "João Silva"
                )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar diferentes veículos")
        void deveAceitarDiferentesVeiculos() {
            assertThatCode(() -> {
                alertService.alertarVeiculoSemCobertura(
                    "VEI-001", "ABC1234", "12345678909", "João Silva"
                );
                alertService.alertarVeiculoSemCobertura(
                    "VEI-002", "XYZ9876", "98765432100", "Maria Santos"
                );
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de alertarVeiculoSemCoberturaPorCancelamento")
    class AlertarVeiculoSemCoberturaPorCancelamentoTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.alertarVeiculoSemCoberturaPorCancelamento(
                    "VEI-001",
                    "ABC1234",
                    "12345678909",
                    "João Silva",
                    "APO-2024-001",
                    "Inadimplência"
                )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar diferentes motivos de cancelamento")
        void deveAceitarDiferentesMotivosCancelamento() {
            String[] motivos = {
                "Inadimplência",
                "Solicitação do cliente",
                "Fraude detectada",
                "Outros"
            };

            for (String motivo : motivos) {
                assertThatCode(() ->
                    alertService.alertarVeiculoSemCoberturaPorCancelamento(
                        "VEI-001", "ABC1234", "12345678909", "João Silva",
                        "APO-001", motivo
                    )
                ).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Testes de alertarVeiculoSemCoberturaPorVencimento")
    class AlertarVeiculoSemCoberturaPorVencimentoTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.alertarVeiculoSemCoberturaPorVencimento(
                    "VEI-001",
                    "ABC1234",
                    "12345678909",
                    "João Silva",
                    "APO-2024-001"
                )
            ).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de notificarCoberturaRestaurada")
    class NotificarCoberturaRestauradaTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.notificarCoberturaRestaurada("VEI-001", "APO-001")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar múltiplas notificações")
        void deveAceitarMultiplasNotificacoes() {
            assertThatCode(() -> {
                alertService.notificarCoberturaRestaurada("VEI-001", "APO-001");
                alertService.notificarCoberturaRestaurada("VEI-002", "APO-002");
                alertService.notificarCoberturaRestaurada("VEI-003", "APO-003");
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de alertarVencimentoProximo")
    class AlertarVencimentoProximoTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.alertarVencimentoProximo(
                    "VEI-001",
                    "ABC1234",
                    "12345678909",
                    "João Silva",
                    "APO-2024-001",
                    15
                )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar diferentes períodos de dias restantes")
        void deveAceitarDiferentesPeriodosDiasRestantes() {
            int[] diasRestantes = {1, 7, 15, 30};

            for (int dias : diasRestantes) {
                assertThatCode(() ->
                    alertService.alertarVencimentoProximo(
                        "VEI-001", "ABC1234", "12345678909", "João Silva",
                        "APO-001", dias
                    )
                ).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Testes de alertarGapCobertura")
    class AlertarGapCoberturaTests {

        @Test
        @DisplayName("Deve executar sem lançar exceção")
        void deveExecutarSemLancarExcecao() {
            assertThatCode(() ->
                alertService.alertarGapCobertura(
                    "VEI-001",
                    "ABC1234",
                    "12345678909",
                    10
                )
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve aceitar diferentes períodos de gap")
        void deveAceitarDiferentesPeriodosGap() {
            int[] diasGap = {1, 5, 7, 10, 15, 30, 60};

            for (int dias : diasGap) {
                assertThatCode(() ->
                    alertService.alertarGapCobertura(
                        "VEI-001", "ABC1234", "12345678909", dias
                    )
                ).doesNotThrowAnyException();
            }
        }
    }

    @Nested
    @DisplayName("Testes de Robustez")
    class RobustezTests {

        @Test
        @DisplayName("Deve lidar com strings vazias")
        void deveLidarComStringsVazias() {
            assertThatCode(() ->
                alertService.alertarVeiculoSemCobertura("", "", "", "")
            ).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com valores zero")
        void deveLidarComValoresZero() {
            assertThatCode(() -> {
                alertService.alertarVencimentoProximo(
                    "VEI-001", "ABC1234", "12345678909", "João Silva",
                    "APO-001", 0
                );
                alertService.alertarGapCobertura(
                    "VEI-001", "ABC1234", "12345678909", 0
                );
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve lidar com valores negativos")
        void deveLidarComValoresNegativos() {
            assertThatCode(() -> {
                alertService.alertarVencimentoProximo(
                    "VEI-001", "ABC1234", "12345678909", "João Silva",
                    "APO-001", -5
                );
                alertService.alertarGapCobertura(
                    "VEI-001", "ABC1234", "12345678909", -10
                );
            }).doesNotThrowAnyException();
        }
    }

    @Nested
    @DisplayName("Testes de Fluxo Completo")
    class FluxoCompletoTests {

        @Test
        @DisplayName("Deve processar ciclo completo de alertas")
        void deveProcessarCicloCompletoAlertas() {
            assertThatCode(() -> {
                // 1. Alerta de vencimento próximo
                alertService.alertarVencimentoProximo(
                    "VEI-001", "ABC1234", "12345678909", "João Silva",
                    "APO-001", 15
                );

                // 2. Alerta de vencimento
                alertService.alertarVeiculoSemCoberturaPorVencimento(
                    "VEI-001", "ABC1234", "12345678909", "João Silva", "APO-001"
                );

                // 3. Notificação de restauração
                alertService.notificarCoberturaRestaurada("VEI-001", "APO-002");
            }).doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Deve processar múltiplos veículos em paralelo")
        void deveProcessarMultiplosVeiculosEmParalelo() {
            assertThatCode(() -> {
                alertService.alertarVeiculoSemCobertura(
                    "VEI-001", "ABC1234", "12345678909", "João Silva"
                );
                alertService.alertarVeiculoSemCobertura(
                    "VEI-002", "XYZ9876", "98765432100", "Maria Santos"
                );
                alertService.alertarVeiculoSemCobertura(
                    "VEI-003", "DEF5678", "11122233344", "Pedro Costa"
                );
            }).doesNotThrowAnyException();
        }
    }
}
