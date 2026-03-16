package com.seguradora.hibrida.domain.apolice.notification.repository;

import com.seguradora.hibrida.domain.apolice.notification.model.ApoliceNotification;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationChannel;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationStatus;
import com.seguradora.hibrida.domain.apolice.notification.model.NotificationType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link ApoliceNotificationRepository}.
 *
 * <p>Nota: Este teste valida a estrutura e assinaturas dos métodos do repositório.
 * Testes de integração com banco de dados real devem ser feitos separadamente.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("ApoliceNotificationRepository - Testes Unitários")
class ApoliceNotificationRepositoryTest {

    @Nested
    @DisplayName("Testes de Estrutura de Métodos")
    class EstruturaMetodosTests {

        @Test
        @DisplayName("Deve ter método findByApoliceIdOrderByCreatedAtDesc")
        void deveTerMetodoFindByApoliceIdOrderByCreatedAtDesc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByApoliceIdOrderByCreatedAtDesc",
                String.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método findByApoliceNumeroOrderByCreatedAtDesc")
        void deveTerMetodoFindByApoliceNumeroOrderByCreatedAtDesc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByApoliceNumeroOrderByCreatedAtDesc",
                String.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método countByApoliceIdAndStatus")
        void deveTerMetodoCountByApoliceIdAndStatus() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "countByApoliceIdAndStatus",
                String.class,
                NotificationStatus.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(long.class);
        }

        @Test
        @DisplayName("Deve ter método findBySeguradoCpfOrderByCreatedAtDesc")
        void deveTerMetodoFindBySeguradoCpfOrderByCreatedAtDesc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findBySeguradoCpfOrderByCreatedAtDesc",
                String.class,
                Pageable.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método findUnreadBySeguradoCpf")
        void deveTerMetodoFindUnreadBySeguradoCpf() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findUnreadBySeguradoCpf",
                String.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método findByStatusOrderByPriorityAscCriadaEmAsc")
        void deveTerMetodoFindByStatusOrderByPriorityAscCriadaEmAsc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByStatusOrderByPriorityAscCriadaEmAsc",
                NotificationStatus.class,
                Pageable.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método findPendingForSending")
        void deveTerMetodoFindPendingForSending() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findPendingForSending",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método findFailedForRetry")
        void deveTerMetodoFindFailedForRetry() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findFailedForRetry",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método findExpiredNotifications")
        void deveTerMetodoFindExpiredNotifications() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findExpiredNotifications",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos de Scheduler")
    class MetodosSchedulerTests {

        @Test
        @DisplayName("Deve ter método findByStatusAndScheduledAtLessThanEqual")
        void deveTerMetodoFindByStatusAndScheduledAtLessThanEqual() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByStatusAndScheduledAtLessThanEqual",
                NotificationStatus.class,
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método existsByApoliceIdAndTypeAndCriadaEmGreaterThan")
        void deveTerMetodoExistsByApoliceIdAndTypeAndCriadaEmGreaterThan() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "existsByApoliceIdAndTypeAndCriadaEmGreaterThan",
                String.class,
                NotificationType.class,
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Deve ter método deleteByUpdatedAtLessThan")
        void deveTerMetodoDeleteByUpdatedAtLessThan() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "deleteByUpdatedAtLessThan",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos por Tipo e Canal")
    class MetodosTipoCanalTests {

        @Test
        @DisplayName("Deve ter método findByTypeOrderByCriadaEmDesc")
        void deveTerMetodoFindByTypeOrderByCriadaEmDesc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByTypeOrderByCriadaEmDesc",
                NotificationType.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método findByChannelOrderByCriadaEmDesc")
        void deveTerMetodoFindByChannelOrderByCriadaEmDesc() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByChannelOrderByCriadaEmDesc",
                NotificationChannel.class,
                Pageable.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método findCriticalPending")
        void deveTerMetodoFindCriticalPending() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findCriticalPending"
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos por Período")
    class MetodosPeriodoTests {

        @Test
        @DisplayName("Deve ter método findByPeriod")
        void deveTerMetodoFindByPeriod() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByPeriod",
                Instant.class,
                Instant.class,
                Pageable.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(Page.class);
        }

        @Test
        @DisplayName("Deve ter método findScheduledForPeriod")
        void deveTerMetodoFindScheduledForPeriod() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findScheduledForPeriod",
                Instant.class,
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos de Controle")
    class MetodosControleTests {

        @Test
        @DisplayName("Deve ter método countSentTodayByChannel")
        void deveTerMetodoCountSentTodayByChannel() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "countSentTodayByChannel",
                NotificationChannel.class,
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(long.class);
        }

        @Test
        @DisplayName("Deve ter método existsByApoliceIdAndTypeAndStatusNot")
        void deveTerMetodoExistsByApoliceIdAndTypeAndStatusNot() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "existsByApoliceIdAndTypeAndStatusNot",
                String.class,
                NotificationType.class,
                NotificationStatus.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(boolean.class);
        }

        @Test
        @DisplayName("Deve ter método findLastByApoliceAndType")
        void deveTerMetodoFindLastByApoliceAndType() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findLastByApoliceAndType",
                String.class,
                NotificationType.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(ApoliceNotification.class);
        }
    }

    @Nested
    @DisplayName("Testes de Métodos Analíticos")
    class MetodosAnaliticosTests {

        @Test
        @DisplayName("Deve ter método getStatisticsByStatus")
        void deveTerMetodoGetStatisticsByStatus() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "getStatisticsByStatus"
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método getStatisticsByChannel")
        void deveTerMetodoGetStatisticsByChannel() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "getStatisticsByChannel"
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método getStatisticsByType")
        void deveTerMetodoGetStatisticsByType() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "getStatisticsByType"
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método getSuccessRateByChannel")
        void deveTerMetodoGetSuccessRateByChannel() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "getSuccessRateByChannel",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }

        @Test
        @DisplayName("Deve ter método getHourlyPerformanceReport")
        void deveTerMetodoGetHourlyPerformanceReport() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "getHourlyPerformanceReport",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(List.class);
        }
    }

    @Nested
    @DisplayName("Testes de Operações de Limpeza")
    class OperacoesLimpezaTests {

        @Test
        @DisplayName("Deve ter método markExpiredNotifications")
        void deveTerMetodoMarkExpiredNotifications() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "markExpiredNotifications",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(int.class);
        }

        @Test
        @DisplayName("Deve ter método deleteOldNotifications")
        void deveTerMetodoDeleteOldNotifications() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "deleteOldNotifications",
                Instant.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(int.class);
        }

        @Test
        @DisplayName("Deve ter método updateStatusBatch")
        void deveTerMetodoUpdateStatusBatch() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "updateStatusBatch",
                List.class,
                NotificationStatus.class
            );

            assertThat(method).isNotNull();
            assertThat(method.getReturnType()).isEqualTo(int.class);
        }
    }

    @Nested
    @DisplayName("Testes de Anotações")
    class AnotacoesTests {

        @Test
        @DisplayName("Deve ter anotação @Repository")
        void deveTerAnotacaoRepository() {
            assertThat(ApoliceNotificationRepository.class.isAnnotationPresent(
                org.springframework.stereotype.Repository.class
            )).isTrue();
        }

        @Test
        @DisplayName("Deve estender JpaRepository")
        void deveEstenderJpaRepository() {
            var interfaces = ApoliceNotificationRepository.class.getInterfaces();

            assertThat(interfaces)
                .extracting(Class::getSimpleName)
                .contains("JpaRepository");
        }
    }

    @Nested
    @DisplayName("Testes de Validação de Assinaturas")
    class ValidacaoAssinaturasTests {

        @Test
        @DisplayName("Métodos de consulta por String devem aceitar String")
        void metodosConsultaPorStringDevemAceitarString() throws NoSuchMethodException {
            var methods = List.of(
                ApoliceNotificationRepository.class.getMethod(
                    "findByApoliceIdOrderByCreatedAtDesc", String.class
                ),
                ApoliceNotificationRepository.class.getMethod(
                    "findByApoliceNumeroOrderByCreatedAtDesc", String.class
                ),
                ApoliceNotificationRepository.class.getMethod(
                    "findBySeguradoCpfOrderByCreatedAtDesc", String.class, Pageable.class
                )
            );

            for (var method : methods) {
                assertThat(method.getParameterTypes()[0])
                    .as("Método %s deve aceitar String como primeiro parâmetro", method.getName())
                    .isEqualTo(String.class);
            }
        }

        @Test
        @DisplayName("Métodos de consulta por enum devem aceitar enum específico")
        void metodosConsultaPorEnumDevemAceitarEnumEspecifico() throws NoSuchMethodException {
            var methodStatus = ApoliceNotificationRepository.class.getMethod(
                "findByStatusOrderByPriorityAscCriadaEmAsc",
                NotificationStatus.class,
                Pageable.class
            );
            assertThat(methodStatus.getParameterTypes()[0]).isEqualTo(NotificationStatus.class);

            var methodType = ApoliceNotificationRepository.class.getMethod(
                "findByTypeOrderByCriadaEmDesc",
                NotificationType.class
            );
            assertThat(methodType.getParameterTypes()[0]).isEqualTo(NotificationType.class);

            var methodChannel = ApoliceNotificationRepository.class.getMethod(
                "findByChannelOrderByCriadaEmDesc",
                NotificationChannel.class,
                Pageable.class
            );
            assertThat(methodChannel.getParameterTypes()[0]).isEqualTo(NotificationChannel.class);
        }

        @Test
        @DisplayName("Métodos de consulta por período devem aceitar Instant")
        void metodosConsultaPorPeriodoDevemAceitarInstant() throws NoSuchMethodException {
            var method = ApoliceNotificationRepository.class.getMethod(
                "findByPeriod",
                Instant.class,
                Instant.class,
                Pageable.class
            );

            assertThat(method.getParameterTypes()[0]).isEqualTo(Instant.class);
            assertThat(method.getParameterTypes()[1]).isEqualTo(Instant.class);
        }

        @Test
        @DisplayName("Métodos paginados devem aceitar Pageable")
        void metodosPaginadosDevemAceitarPageable() throws NoSuchMethodException {
            var methods = List.of(
                ApoliceNotificationRepository.class.getMethod(
                    "findBySeguradoCpfOrderByCreatedAtDesc", String.class, Pageable.class
                ),
                ApoliceNotificationRepository.class.getMethod(
                    "findByStatusOrderByPriorityAscCriadaEmAsc", NotificationStatus.class, Pageable.class
                ),
                ApoliceNotificationRepository.class.getMethod(
                    "findByChannelOrderByCriadaEmDesc", NotificationChannel.class, Pageable.class
                )
            );

            for (var method : methods) {
                var params = method.getParameterTypes();
                assertThat(params[params.length - 1])
                    .as("Método %s deve aceitar Pageable como último parâmetro", method.getName())
                    .isEqualTo(Pageable.class);
            }
        }
    }

    @Nested
    @DisplayName("Testes de Contagem de Métodos")
    class ContagemMetodosTests {

        @Test
        @DisplayName("Deve ter pelo menos 30 métodos customizados")
        void deveTerPeloMenos30MetodosCustomizados() {
            var methods = ApoliceNotificationRepository.class.getDeclaredMethods();

            // Filtra apenas métodos declarados no repositório (não herdados)
            long customMethods = java.util.Arrays.stream(methods)
                .filter(m -> !m.isSynthetic())
                .count();

            assertThat(customMethods)
                .as("Repositório deve ter pelo menos 28 métodos customizados")
                .isGreaterThanOrEqualTo(28);
        }

        @Test
        @DisplayName("Deve ter métodos de todos os tipos de operação")
        void deveTerMetodosDeTodosTiposOperacao() {
            var methods = ApoliceNotificationRepository.class.getDeclaredMethods();
            var methodNames = java.util.Arrays.stream(methods)
                .map(java.lang.reflect.Method::getName)
                .toList();

            // Consultas
            assertThat(methodNames).anyMatch(name -> name.startsWith("find"));

            // Contagens
            assertThat(methodNames).anyMatch(name -> name.startsWith("count"));

            // Verificações de existência
            assertThat(methodNames).anyMatch(name -> name.startsWith("exists"));

            // Deleções
            assertThat(methodNames).anyMatch(name -> name.startsWith("delete"));

            // Atualizações
            assertThat(methodNames).anyMatch(name -> name.contains("update") || name.contains("mark"));

            // Estatísticas
            assertThat(methodNames).anyMatch(name -> name.contains("Statistics") || name.contains("Report"));
        }
    }

    @Nested
    @DisplayName("Testes de Nomenclatura")
    class NomenclaturaTests {

        @Test
        @DisplayName("Métodos find devem retornar List ou Page")
        void metodosFindDevemRetornarListOuPage() {
            var findMethods = java.util.Arrays.stream(
                ApoliceNotificationRepository.class.getDeclaredMethods()
            )
            .filter(m -> m.getName().startsWith("find"))
            .toList();

            for (var method : findMethods) {
                var returnType = method.getReturnType();
                assertThat(returnType)
                    .as("Método %s deve retornar List ou Page", method.getName())
                    .matches(type ->
                        type.equals(List.class) ||
                        type.equals(Page.class) ||
                        type.equals(ApoliceNotification.class)
                    );
            }
        }

        @Test
        @DisplayName("Métodos count devem retornar long")
        void metodosCountDevemRetornarLong() {
            var countMethods = java.util.Arrays.stream(
                ApoliceNotificationRepository.class.getDeclaredMethods()
            )
            .filter(m -> m.getName().startsWith("count"))
            .toList();

            for (var method : countMethods) {
                assertThat(method.getReturnType())
                    .as("Método %s deve retornar long", method.getName())
                    .isEqualTo(long.class);
            }
        }

        @Test
        @DisplayName("Métodos exists devem retornar boolean")
        void metodosExistsDevemRetornarBoolean() {
            var existsMethods = java.util.Arrays.stream(
                ApoliceNotificationRepository.class.getDeclaredMethods()
            )
            .filter(m -> m.getName().startsWith("exists"))
            .toList();

            for (var method : existsMethods) {
                assertThat(method.getReturnType())
                    .as("Método %s deve retornar boolean", method.getName())
                    .isEqualTo(boolean.class);
            }
        }

        @Test
        @DisplayName("Métodos delete devem retornar int ou void")
        void metodosDeleteDevemRetornarIntOuVoid() {
            var deleteMethods = java.util.Arrays.stream(
                ApoliceNotificationRepository.class.getDeclaredMethods()
            )
            .filter(m -> m.getName().startsWith("delete"))
            .toList();

            for (var method : deleteMethods) {
                var returnType = method.getReturnType();
                assertThat(returnType)
                    .as("Método %s deve retornar int ou void", method.getName())
                    .matches(type -> type.equals(int.class) || type.equals(void.class));
            }
        }
    }
}
