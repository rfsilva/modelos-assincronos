package com.seguradora.hibrida.eventstore.replay.exception;

import com.seguradora.hibrida.eventstore.replay.ReplayConfiguration;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.UUID;

@DisplayName("ReplayConfigurationException Tests")
class ReplayConfigurationExceptionTest {

    @Test
    @DisplayName("Deve criar com mensagem e configuração")
    void shouldCreateWithMessageAndConfiguration() {
        String message = "Configuration error";
        ReplayConfiguration config = mock(ReplayConfiguration.class);
        UUID replayId = UUID.randomUUID();
        when(config.getReplayId()).thenReturn(replayId);
        when(config.getName()).thenReturn("Test Replay");

        ReplayConfigurationException exception = new ReplayConfigurationException(message, config);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getConfiguration()).isEqualTo(config);
        assertThat(exception.getReplayId()).isEqualTo(replayId);
    }

    @Test
    @DisplayName("Deve criar com mensagem, configuração e causa")
    void shouldCreateWithMessageConfigurationAndCause() {
        String message = "Configuration error";
        ReplayConfiguration config = mock(ReplayConfiguration.class);
        UUID replayId = UUID.randomUUID();
        when(config.getReplayId()).thenReturn(replayId);
        when(config.getName()).thenReturn("Test Replay");
        Throwable cause = new RuntimeException("Cause");

        ReplayConfigurationException exception = new ReplayConfigurationException(message, config, cause);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getConfiguration()).isEqualTo(config);
        assertThat(exception.getCause()).isEqualTo(cause);
        assertThat(exception.getReplayId()).isEqualTo(replayId);
    }

    @Test
    @DisplayName("Deve lidar com configuração nula")
    void shouldHandleNullConfiguration() {
        String message = "Configuration error";

        ReplayConfigurationException exception = new ReplayConfigurationException(message, null);

        assertThat(exception.getMessage()).contains(message);
        assertThat(exception.getConfiguration()).isNull();
    }

    @Test
    @DisplayName("Deve ser uma ReplayException")
    void shouldBeAReplayException() {
        ReplayConfiguration config = mock(ReplayConfiguration.class);
        ReplayConfigurationException exception = new ReplayConfigurationException("Test", config);
        assertThat(exception).isInstanceOf(ReplayException.class);
    }
}
