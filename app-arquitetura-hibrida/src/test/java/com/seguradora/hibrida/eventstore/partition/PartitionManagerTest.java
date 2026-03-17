package com.seguradora.hibrida.eventstore.partition;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link PartitionManager}.
 *
 * <p>Testes de comportamento completos requerem banco de dados.
 * Esta classe verifica a estrutura e anotações do componente.
 */
@DisplayName("PartitionManager Tests")
class PartitionManagerTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(PartitionManager.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar JdbcTemplate no construtor")
    void shouldAcceptJdbcTemplateInConstructor() throws NoSuchMethodException {
        assertThat(PartitionManager.class.getConstructor(JdbcTemplate.class)).isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método createMonthlyPartition")
    void shouldDeclareCreateMonthlyPartitionMethod() throws NoSuchMethodException {
        assertThat(PartitionManager.class.getMethod("createMonthlyPartition", String.class, LocalDate.class))
                .isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método maintainPartitions")
    void shouldDeclareMaintainPartitionsMethod() throws NoSuchMethodException {
        assertThat(PartitionManager.class.getMethod("maintainPartitions")).isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método arePartitionsHealthy")
    void shouldDeclareArePartitionsHealthyMethod() throws NoSuchMethodException {
        assertThat(PartitionManager.class.getMethod("arePartitionsHealthy")).isNotNull();
    }

    @Test
    @DisplayName("Deve declarar método calculatePartitionName")
    void shouldDeclareCalculatePartitionNameMethod() throws NoSuchMethodException {
        assertThat(PartitionManager.class.getMethod("calculatePartitionName", LocalDate.class))
                .isNotNull();
    }

    @Test
    @DisplayName("calculatePartitionName deve retornar String")
    void calculatePartitionNameShouldReturnString() throws NoSuchMethodException {
        var method = PartitionManager.class.getMethod("calculatePartitionName", LocalDate.class);
        assertThat(method.getReturnType()).isEqualTo(String.class);
    }
}
