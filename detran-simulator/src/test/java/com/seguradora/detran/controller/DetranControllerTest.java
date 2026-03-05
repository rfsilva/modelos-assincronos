package com.seguradora.detran.controller;

import com.seguradora.detran.service.DetranSimulatorService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DetranController.class)
@ActiveProfiles("test")
class DetranControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DetranSimulatorService detranSimulatorService;

    @Test
    void testStatusEndpoint() throws Exception {
        mockMvc.perform(get("/veiculo/status"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ONLINE"))
                .andExpect(jsonPath("$.sistema").value("DETRAN Simulator"));
    }

    @Test
    void testManutencaoEndpoint() throws Exception {
        mockMvc.perform(get("/veiculo/manutencao"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.erro").value("SISTEMA_EM_MANUTENCAO"));
    }

    @Test
    void testConsultaVeiculoSemParametros() throws Exception {
        mockMvc.perform(get("/veiculo"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConsultaVeiculoComPlacaInvalida() throws Exception {
        mockMvc.perform(get("/veiculo")
                .param("placa", "INVALID")
                .param("renavam", "12345678901"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testConsultaVeiculoComRenavamInvalido() throws Exception {
        mockMvc.perform(get("/veiculo")
                .param("placa", "ABC1234")
                .param("renavam", "123"))
                .andExpect(status().isBadRequest());
    }
}