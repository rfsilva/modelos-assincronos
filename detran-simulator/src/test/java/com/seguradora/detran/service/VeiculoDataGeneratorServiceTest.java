package com.seguradora.detran.service;

import com.seguradora.detran.model.DetranResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class VeiculoDataGeneratorServiceTest {

    @InjectMocks
    private VeiculoDataGeneratorService service;

    @Test
    void testGenerateVeiculoDataConsistency() {
        String placa = "ABC1234";
        String renavam = "12345678901";
        
        // Gerar dados duas vezes com os mesmos parâmetros
        DetranResponse response1 = service.generateVeiculoData(placa, renavam);
        DetranResponse response2 = service.generateVeiculoData(placa, renavam);
        
        // Deve gerar dados consistentes (mesmo resultado)
        assertEquals(response1.getPlaca(), response2.getPlaca());
        assertEquals(response1.getRenavam(), response2.getRenavam());
        assertEquals(response1.getAnoFabricacao(), response2.getAnoFabricacao());
        assertEquals(response1.getAnoModelo(), response2.getAnoModelo());
        assertEquals(response1.getMarcaModelo(), response2.getMarcaModelo());
        assertEquals(response1.getCor(), response2.getCor());
    }

    @Test
    void testGenerateVeiculoDataCompleteness() {
        String placa = "XYZ9876";
        String renavam = "98765432109";
        
        DetranResponse response = service.generateVeiculoData(placa, renavam);
        
        // Verificar se todos os campos obrigatórios estão preenchidos
        assertNotNull(response.getPlaca());
        assertNotNull(response.getRenavam());
        assertNotNull(response.getAnoFabricacao());
        assertNotNull(response.getAnoModelo());
        assertNotNull(response.getMarcaModelo());
        assertNotNull(response.getCor());
        assertNotNull(response.getCombustivel());
        assertNotNull(response.getCategoria());
        assertNotNull(response.getCarroceria());
        assertNotNull(response.getEspecie());
        assertNotNull(response.getProprietario());
        assertNotNull(response.getMunicipio());
        assertNotNull(response.getSituacao());
        assertNotNull(response.getDataAquisicao());
        
        // Verificar se as listas foram inicializadas (mesmo que vazias)
        assertNotNull(response.getDebitos());
        assertNotNull(response.getDuas());
        assertNotNull(response.getInfracoes());
        assertNotNull(response.getMultas());
        assertNotNull(response.getProcessos());
        assertNotNull(response.getRecursos());
    }

    @Test
    void testGenerateVeiculoDataDifferentInputs() {
        DetranResponse response1 = service.generateVeiculoData("ABC1234", "12345678901");
        DetranResponse response2 = service.generateVeiculoData("DEF5678", "98765432109");
        
        // Dados diferentes devem gerar resultados diferentes
        assertNotEquals(response1.getPlaca(), response2.getPlaca());
        assertNotEquals(response1.getRenavam(), response2.getRenavam());
        
        // Mas a estrutura deve ser a mesma
        assertEquals(response1.getEspecie(), response2.getEspecie()); // Sempre "PASSAGEIRO"
    }

    @Test
    void testGenerateVeiculoDataValidYears() {
        DetranResponse response = service.generateVeiculoData("TST1234", "11111111111");
        
        int anoFabricacao = Integer.parseInt(response.getAnoFabricacao());
        int anoModelo = Integer.parseInt(response.getAnoModelo());
        
        // Ano de fabricação deve estar entre 2010 e 2023
        assertTrue(anoFabricacao >= 2010 && anoFabricacao <= 2023);
        
        // Ano do modelo deve ser igual ou posterior ao de fabricação
        assertTrue(anoModelo >= anoFabricacao);
        assertTrue(anoModelo <= anoFabricacao + 1);
    }
}