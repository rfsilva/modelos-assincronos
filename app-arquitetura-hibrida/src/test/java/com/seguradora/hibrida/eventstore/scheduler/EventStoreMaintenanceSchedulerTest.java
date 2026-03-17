package com.seguradora.hibrida.eventstore.scheduler;

import com.seguradora.hibrida.eventstore.archive.EventArchiver;
import com.seguradora.hibrida.eventstore.partition.PartitionManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link EventStoreMaintenanceScheduler}.
 */
@DisplayName("EventStoreMaintenanceScheduler Tests")
class EventStoreMaintenanceSchedulerTest {

    @Test
    @DisplayName("Deve estar anotado com @Service")
    void shouldBeAnnotatedWithService() {
        assertThat(EventStoreMaintenanceScheduler.class.isAnnotationPresent(Service.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estar anotado com @ConditionalOnProperty")
    void shouldBeAnnotatedWithConditionalOnProperty() {
        assertThat(EventStoreMaintenanceScheduler.class.isAnnotationPresent(ConditionalOnProperty.class)).isTrue();
    }

    @Test
    @DisplayName("@ConditionalOnProperty deve usar name 'eventstore.maintenance.enabled'")
    void conditionalOnPropertyShouldUseCorrectName() {
        ConditionalOnProperty prop = EventStoreMaintenanceScheduler.class.getAnnotation(ConditionalOnProperty.class);
        assertThat(prop.name()).contains("eventstore.maintenance.enabled");
    }

    @Test
    @DisplayName("Deve aceitar dependências corretas no construtor")
    void shouldAcceptCorrectDependenciesInConstructor() throws NoSuchMethodException {
        assertThat(EventStoreMaintenanceScheduler.class.getConstructor(
                PartitionManager.class, EventArchiver.class))
                .isNotNull();
    }

    @Test
    @DisplayName("dailyPartitionMaintenance deve estar anotado com @Scheduled")
    void dailyPartitionMaintenanceShouldBeAnnotatedWithScheduled() throws NoSuchMethodException {
        Method m = EventStoreMaintenanceScheduler.class.getMethod("dailyPartitionMaintenance");
        assertThat(m.isAnnotationPresent(Scheduled.class)).isTrue();
    }

    @Test
    @DisplayName("weeklyArchiving deve estar anotado com @Scheduled")
    void weeklyArchivingShouldBeAnnotatedWithScheduled() throws NoSuchMethodException {
        Method m = EventStoreMaintenanceScheduler.class.getMethod("weeklyArchiving");
        assertThat(m.isAnnotationPresent(Scheduled.class)).isTrue();
    }

    @Test
    @DisplayName("partitionHealthCheck deve estar anotado com @Scheduled")
    void partitionHealthCheckShouldBeAnnotatedWithScheduled() throws NoSuchMethodException {
        Method m = EventStoreMaintenanceScheduler.class.getMethod("partitionHealthCheck");
        assertThat(m.isAnnotationPresent(Scheduled.class)).isTrue();
    }
}
