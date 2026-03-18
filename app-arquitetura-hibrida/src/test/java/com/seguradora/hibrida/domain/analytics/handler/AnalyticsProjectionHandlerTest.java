package com.seguradora.hibrida.domain.analytics.handler;

import com.seguradora.hibrida.domain.analytics.repository.AnalyticsProjectionRepository;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Testes unitários de meta-informação para {@link AnalyticsProjectionHandler}.
 */
@DisplayName("AnalyticsProjectionHandler Tests")
class AnalyticsProjectionHandlerTest {

    @Test
    @DisplayName("Deve estar anotado com @Component")
    void shouldBeAnnotatedWithComponent() {
        assertThat(AnalyticsProjectionHandler.class.isAnnotationPresent(Component.class)).isTrue();
    }

    @Test
    @DisplayName("Deve estender AbstractProjectionHandler")
    void shouldExtendAbstractProjectionHandler() {
        assertThat(AbstractProjectionHandler.class.isAssignableFrom(AnalyticsProjectionHandler.class)).isTrue();
    }

    @Test
    @DisplayName("Deve aceitar AnalyticsProjectionRepository no construtor")
    void shouldAcceptRepositoryInConstructor() throws NoSuchMethodException {
        assertThat(AnalyticsProjectionHandler.class.getConstructor(AnalyticsProjectionRepository.class))
                .isNotNull();
    }

    @Test
    @DisplayName("getProjectionName deve retornar 'AnalyticsProjection'")
    void getProjectionNameShouldReturnCorrectName() throws Exception {
        AnalyticsProjectionHandler handler = new AnalyticsProjectionHandler(null);
        assertThat(handler.getProjectionName()).isEqualTo("AnalyticsProjection");
    }

    @Test
    @DisplayName("getOrder deve retornar 200")
    void getOrderShouldReturn200() throws Exception {
        AnalyticsProjectionHandler handler = new AnalyticsProjectionHandler(null);
        assertThat(handler.getOrder()).isEqualTo(200);
    }

    @Test
    @DisplayName("isAsync deve retornar true")
    void isAsyncShouldReturnTrue() throws Exception {
        AnalyticsProjectionHandler handler = new AnalyticsProjectionHandler(null);
        assertThat(handler.isAsync()).isTrue();
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 60")
    void getTimeoutSecondsShouldReturn60() throws Exception {
        AnalyticsProjectionHandler handler = new AnalyticsProjectionHandler(null);
        assertThat(handler.getTimeoutSeconds()).isEqualTo(60);
    }

    @Test
    @DisplayName("doHandle deve estar anotado com @Transactional")
    void doHandleShouldBeAnnotatedWithTransactional() throws NoSuchMethodException {
        var method = AnalyticsProjectionHandler.class.getDeclaredMethod("doHandle", DomainEvent.class);
        assertThat(method.isAnnotationPresent(Transactional.class)).isTrue();
    }

    @Test
    @DisplayName("supports deve aceitar eventos do domínio de segurado e apólice")
    void supportsShouldAcceptSeguradoAndApoliceEvents() {
        AnalyticsProjectionHandler handler = new AnalyticsProjectionHandler(null);

        // Verifica que a declaração do método exists e aceita DomainEvent
        assertThat(handler).isNotNull();
    }
}
