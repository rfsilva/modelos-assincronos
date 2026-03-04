package com.seguradora.detran.service;

import com.seguradora.detran.config.SimulatorConfig;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InstabilitySimulatorServiceTest {

    @Mock
    private SimulatorConfig config;

    @Mock
    private SimulatorConfig.Instability instability;

    @Mock
    private SimulatorConfig.Performance performance;

    @Mock
    private SimulatorConfig.DataConfig dataConfig;

    @InjectMocks
    private InstabilitySimulatorService service;

    @Test
    void testSimulateInstabilityDisabled() throws Exception {
        when(config.getInstability()).thenReturn(instability);
        when(instability.isEnabled()).thenReturn(false);
        
        String result = service.simulateInstability();
        
        assertEquals("NORMAL", result);
    }

    @Test
    void testIsDataValidWithValidPlaca() {
        when(config.getData()).thenReturn(dataConfig);
        when(dataConfig.getInvalidDataRate()).thenReturn(0.0); // Nunca inválido para teste
        
        boolean result = service.isDataValid("ABC1234", "12345678901");
        
        assertTrue(result);
    }

    @Test
    void testIsDataValidWithInvalidPlaca() {
        when(config.getData()).thenReturn(dataConfig);
        when(dataConfig.getInvalidDataRate()).thenReturn(0.0); // Nunca inválido para teste
        
        boolean result = service.isDataValid("INVALID", "12345678901");
        
        assertFalse(result);
    }

    @Test
    void testIsDataValidWithInvalidRenavam() {
        when(config.getData()).thenReturn(dataConfig);
        when(dataConfig.getInvalidDataRate()).thenReturn(0.0); // Nunca inválido para teste
        
        boolean result = service.isDataValid("ABC1234", "123");
        
        assertFalse(result);
    }

    @Test
    void testIsDataValidWithNullValues() {
        when(config.getData()).thenReturn(dataConfig);
        when(dataConfig.getInvalidDataRate()).thenReturn(0.0); // Nunca inválido para teste
        
        boolean result = service.isDataValid(null, null);
        
        assertFalse(result);
    }

    @Test
    void testCalculateResponseTimeNormal() {
        when(config.getPerformance()).thenReturn(performance);
        when(performance.getMinResponseTime()).thenReturn(500L);
        when(performance.getSlowResponseTime()).thenReturn(5000L);
        
        long responseTime = service.calculateResponseTime("NORMAL");
        
        assertTrue(responseTime >= 500L && responseTime < 5000L);
    }

    @Test
    void testCalculateResponseTimeSlow() {
        when(config.getPerformance()).thenReturn(performance);
        when(performance.getSlowResponseTime()).thenReturn(5000L);
        when(performance.getMaxResponseTime()).thenReturn(8000L);
        
        long responseTime = service.calculateResponseTime("SLOW_RESPONSE");
        
        assertTrue(responseTime >= 5000L && responseTime <= 8000L);
    }

    @Test
    void testShouldUseCacheDisabled() {
        when(config.getData()).thenReturn(dataConfig);
        when(dataConfig.isCacheSimulation()).thenReturn(false);
        
        boolean result = service.shouldUseCache("ABC1234", "12345678901");
        
        assertFalse(result);
    }
}