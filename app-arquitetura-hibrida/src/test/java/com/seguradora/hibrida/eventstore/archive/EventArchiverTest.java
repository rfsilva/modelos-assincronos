package com.seguradora.hibrida.eventstore.archive;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link EventArchiver}.
 *
 * <p>Testes de comportamento completos requerem banco de dados.
 * Esta classe verifica a estrutura e anotações do componente.
 */
@DisplayName("EventArchiver Tests")
class EventArchiverTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(EventArchiver.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar dependências corretas no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(EventArchiver.class.getConstructor(
                JdbcTemplate.class,
                ObjectMapper.class,
                ArchiveStorageService.class,
                EventArchiveProperties.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método archivePartition")
    void shouldDeclareArchivePartitionMethod() throws NoSuchMethodException {
        assertThat(EventArchiver.class.getMethod("archivePartition", String.class))
                .isNotNull();
    }

    @Test
    @DisplayName("archivePartition deve retornar ArchiveResult")
    void archivePartitionShouldReturnArchiveResult() throws NoSuchMethodException {
        var method = EventArchiver.class.getMethod("archivePartition", String.class);
        assertThat(method.getReturnType()).isEqualTo(ArchiveResult.class);
    }

    @Test
    @DisplayName("Deve declarar método executeAutoArchiving")
    void shouldDeclareExecuteAutoArchivingMethod() throws NoSuchMethodException {
        assertThat(EventArchiver.class.getMethod("executeAutoArchiving"))
                .isNotNull();
    }

    @Test
    @DisplayName("executeAutoArchiving deve retornar ArchiveSummary")
    void executeAutoArchivingShouldReturnArchiveSummary() throws NoSuchMethodException {
        var method = EventArchiver.class.getMethod("executeAutoArchiving");
        assertThat(method.getReturnType()).isEqualTo(ArchiveSummary.class);
    }

    @Test
    @DisplayName("Deve declarar método getArchiveStatistics")
    void shouldDeclareGetArchiveStatisticsMethod() throws NoSuchMethodException {
        assertThat(EventArchiver.class.getMethod("getArchiveStatistics"))
                .isNotNull();
    }

    @Test
    @DisplayName("getArchiveStatistics deve retornar ArchiveStatistics")
    void getArchiveStatisticsShouldReturnArchiveStatistics() throws NoSuchMethodException {
        var method = EventArchiver.class.getMethod("getArchiveStatistics");
        assertThat(method.getReturnType()).isEqualTo(ArchiveStatistics.class);
    }
}
