package com.seguradora.hibrida.config.ratelimit;

import io.github.bucket4j.Bucket;
import io.github.bucket4j.ConsumptionProbe;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Testes unitários para {@link RateLimitInterceptor}.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("RateLimitInterceptor Tests")
class RateLimitInterceptorTest {

    @Mock
    private RateLimitConfiguration rateLimitConfiguration;

    @Mock
    private Bucket bucket;

    @Mock
    private ConsumptionProbe probe;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private RateLimitInterceptor interceptor;

    @BeforeEach
    void setUp() {
        when(request.getRemoteAddr()).thenReturn("192.168.0.1");
        when(request.getHeader("User-Agent")).thenReturn("TestAgent/1.0");
        when(rateLimitConfiguration.resolveBucket(anyString())).thenReturn(bucket);
        when(bucket.tryConsumeAndReturnRemaining(1)).thenReturn(probe);
    }

    // =========================================================================
    // Anotações e hierarquia
    // =========================================================================

    @Nested
    @DisplayName("Anotações e hierarquia")
    class AnotacoesEHierarquia {

        @Test
        @DisplayName("Deve estar anotado com @Component")
        void shouldBeAnnotatedWithComponent() {
            assertThat(RateLimitInterceptor.class.isAnnotationPresent(Component.class)).isTrue();
        }

        @Test
        @DisplayName("Deve implementar HandlerInterceptor")
        void shouldImplementHandlerInterceptor() {
            assertThat(interceptor).isInstanceOf(HandlerInterceptor.class);
        }
    }

    // =========================================================================
    // preHandle — token disponível
    // =========================================================================

    @Nested
    @DisplayName("preHandle() — dentro do limite")
    class DentroDoLimite {

        @Test
        @DisplayName("Deve retornar true quando token é consumido com sucesso")
        void shouldReturnTrueWhenTokenIsConsumed() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(true);
            when(probe.getRemainingTokens()).thenReturn(50L);

            // When
            boolean result = interceptor.preHandle(request, response, new Object());

            // Then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("Deve adicionar header X-Rate-Limit-Remaining quando consumido")
        void shouldAddRateLimitRemainingHeader() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(true);
            when(probe.getRemainingTokens()).thenReturn(42L);

            // When
            interceptor.preHandle(request, response, new Object());

            // Then
            verify(response).addHeader("X-Rate-Limit-Remaining", "42");
        }
    }

    // =========================================================================
    // preHandle — limite excedido
    // =========================================================================

    @Nested
    @DisplayName("preHandle() — limite excedido")
    class LimiteExcedido {

        @Test
        @DisplayName("Deve retornar false quando limite é excedido")
        void shouldReturnFalseWhenRateLimitExceeded() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(false);
            when(probe.getNanosToWaitForRefill()).thenReturn(30_000_000_000L); // 30s

            // When
            boolean result = interceptor.preHandle(request, response, new Object());

            // Then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("Deve adicionar header X-Rate-Limit-Retry-After-Seconds quando limite excedido")
        void shouldAddRetryAfterHeaderWhenLimitExceeded() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(false);
            when(probe.getNanosToWaitForRefill()).thenReturn(15_000_000_000L); // 15s

            // When
            interceptor.preHandle(request, response, new Object());

            // Then
            verify(response).addHeader("X-Rate-Limit-Retry-After-Seconds", "15");
        }

        @Test
        @DisplayName("Deve enviar erro 429 quando limite excedido")
        void shouldSend429WhenLimitExceeded() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(false);
            when(probe.getNanosToWaitForRefill()).thenReturn(5_000_000_000L);

            // When
            interceptor.preHandle(request, response, new Object());

            // Then
            verify(response).sendError(eq(HttpStatus.TOO_MANY_REQUESTS.value()), anyString());
        }
    }

    // =========================================================================
    // Identificação do cliente
    // =========================================================================

    @Nested
    @DisplayName("Identificação do cliente")
    class IdentificacaoDoCliente {

        @Test
        @DisplayName("Deve resolver bucket para o cliente identificado pelo IP")
        void shouldResolveBucketForClient() throws Exception {
            // Given
            when(probe.isConsumed()).thenReturn(true);
            when(probe.getRemainingTokens()).thenReturn(99L);

            // When
            interceptor.preHandle(request, response, new Object());

            // Then — resolveBucket foi chamado com uma chave não nula
            verify(rateLimitConfiguration).resolveBucket(anyString());
        }

        @Test
        @DisplayName("Deve lidar com User-Agent nulo sem lançar exceção")
        void shouldHandleNullUserAgent() throws Exception {
            // Given
            when(request.getHeader("User-Agent")).thenReturn(null);
            when(probe.isConsumed()).thenReturn(true);
            when(probe.getRemainingTokens()).thenReturn(99L);

            // When / Then — não deve lançar exceção
            boolean result = interceptor.preHandle(request, response, new Object());
            assertThat(result).isTrue();
        }
    }
}
