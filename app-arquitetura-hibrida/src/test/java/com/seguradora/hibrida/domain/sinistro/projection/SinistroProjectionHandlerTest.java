package com.seguradora.hibrida.domain.sinistro.projection;

import com.seguradora.hibrida.domain.sinistro.event.SinistroCriadoEvent;
import com.seguradora.hibrida.domain.sinistro.event.SinistroEmAnaliseEvent;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@DisplayName("SinistroProjectionHandler Tests")
class SinistroProjectionHandlerTest {

    private SinistroProjectionHandler handler;

    @BeforeEach
    void setUp() {
        handler = new SinistroProjectionHandler();
    }

    @Test
    @DisplayName("getOrder deve retornar 10")
    void getOrderShouldReturn10() {
        assertThat(handler.getOrder()).isEqualTo(10);
    }

    @Test
    @DisplayName("isAsync deve retornar true")
    void isAsyncShouldReturnTrue() {
        assertThat(handler.isAsync()).isTrue();
    }

    @Test
    @DisplayName("getTimeoutSeconds deve retornar 15")
    void getTimeoutSecondsShouldReturn15() {
        assertThat(handler.getTimeoutSeconds()).isEqualTo(15);
    }

    @Test
    @DisplayName("isRetryable deve retornar true")
    void isRetryableShouldReturnTrue() {
        assertThat(handler.isRetryable()).isTrue();
    }

    @Test
    @DisplayName("getMaxRetries deve retornar 3")
    void getMaxRetriesShouldReturn3() {
        assertThat(handler.getMaxRetries()).isEqualTo(3);
    }

    @Test
    @DisplayName("supports deve retornar true para eventos SinistroCriado")
    void supportsShouldReturnTrueForSinistroEvents() {
        DomainEvent event = mock(DomainEvent.class);
        when(event.getEventType()).thenReturn("SinistroCriadoEvent");

        assertThat(handler.supports(event)).isTrue();
    }

    @Test
    @DisplayName("supports deve retornar true para eventos ConsultaDetran")
    void supportsShouldReturnTrueForConsultaDetranEvents() {
        DomainEvent event = mock(DomainEvent.class);
        when(event.getEventType()).thenReturn("ConsultaDetranIniciadaEvent");

        assertThat(handler.supports(event)).isTrue();
    }

    @Test
    @DisplayName("supports deve retornar false para outros eventos")
    void supportsShouldReturnFalseForOtherEvents() {
        DomainEvent event = mock(DomainEvent.class);
        when(event.getEventType()).thenReturn("ApoliceRenovadaEvent");

        assertThat(handler.supports(event)).isFalse();
    }

    @Test
    @DisplayName("supports deve retornar true para SinistroAtualizadoEvent")
    void supportsShouldReturnTrueForSinistroAtualizadoEvent() {
        DomainEvent event = mock(DomainEvent.class);
        when(event.getEventType()).thenReturn("SinistroAtualizadoEvent");

        assertThat(handler.supports(event)).isTrue();
    }
}
