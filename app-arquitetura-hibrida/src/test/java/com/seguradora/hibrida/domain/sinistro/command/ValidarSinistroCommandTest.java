package com.seguradora.hibrida.domain.sinistro.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ValidarSinistroCommand Tests")
class ValidarSinistroCommandTest {

    private Map<String, Object> dadosComplementares3() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("condicaoClimatica", "Chuva");
        dados.put("condicaoPista", "Molhada");
        dados.put("testemunhas", "João Silva");
        return dados;
    }

    private ValidarSinistroCommand comando() {
        return ValidarSinistroCommand.builder()
                .sinistroId("SIN-001")
                .dadosComplementares(dadosComplementares3())
                .documentosAnexados(List.of("DOC-001", "DOC-002"))
                .operadorId("OP-001")
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        ValidarSinistroCommand c = comando();
        assertThat(c.getSinistroId()).isEqualTo("SIN-001");
        assertThat(c.getOperadorId()).isEqualTo("OP-001");
        assertThat(c.getDadosComplementares()).hasSize(3);
        assertThat(c.getDocumentosAnexados()).hasSize(2);
    }

    @Test
    @DisplayName("getCommandType deve retornar ValidarSinistroCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("ValidarSinistroCommand");
    }

    @Test
    @DisplayName("hasDadosComplementaresSuficientes deve retornar true quando >= 3 campos")
    void hasDadosComplementaresSufficientShouldReturnTrueWhen3OrMore() {
        assertThat(comando().hasDadosComplementaresSuficientes()).isTrue();
    }

    @Test
    @DisplayName("hasDadosComplementaresSuficientes deve retornar false quando < 3 campos")
    void hasDadosComplementaresSufficientShouldReturnFalseWhenLess() {
        ValidarSinistroCommand c = ValidarSinistroCommand.builder()
                .sinistroId("SIN-001").operadorId("OP-001")
                .dadosComplementares(Map.of("k1", "v1", "k2", "v2"))
                .build();
        assertThat(c.hasDadosComplementaresSuficientes()).isFalse();
    }

    @Test
    @DisplayName("hasDocumentosAnexados deve retornar true quando há documentos")
    void hasDocumentosAnexadosShouldReturnTrueWhenPresent() {
        assertThat(comando().hasDocumentosAnexados()).isTrue();
    }

    @Test
    @DisplayName("hasDocumentosAnexados deve retornar false quando lista vazia")
    void hasDocumentosAnexadosShouldReturnFalseWhenEmpty() {
        ValidarSinistroCommand c = ValidarSinistroCommand.builder()
                .sinistroId("SIN-001").operadorId("OP-001")
                .build();
        assertThat(c.hasDocumentosAnexados()).isFalse();
    }

    @Test
    @DisplayName("getDadoComplementar deve retornar valor correto quando chave existe")
    void getDadoComplementarShouldReturnValueWhenKeyExists() {
        assertThat(comando().getDadoComplementar("condicaoClimatica")).isEqualTo("Chuva");
    }

    @Test
    @DisplayName("getDadoComplementar deve retornar null quando chave não existe")
    void getDadoComplementarShouldReturnNullWhenKeyNotFound() {
        assertThat(comando().getDadoComplementar("inexistente")).isNull();
    }

    @Test
    @DisplayName("toString deve conter sinistroId e contagem de campos")
    void toStringShouldContainSinistroAndFieldCount() {
        assertThat(comando().toString()).contains("SIN-001");
    }
}
