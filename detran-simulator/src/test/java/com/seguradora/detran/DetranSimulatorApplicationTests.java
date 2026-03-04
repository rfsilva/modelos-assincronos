package com.seguradora.detran;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
@TestPropertySource(properties = {
    "spring.datasource.url=jdbc:h2:mem:testdb",
    "spring.jpa.hibernate.ddl-auto=create-drop",
    "spring.sql.init.mode=never",
    "detran.simulator.instability.enabled=false"
})
class DetranSimulatorApplicationTests {

    @Test
    void contextLoads() {
        // Teste básico para verificar se o contexto Spring carrega corretamente
        // Se chegou até aqui, o contexto foi carregado com sucesso
    }
}