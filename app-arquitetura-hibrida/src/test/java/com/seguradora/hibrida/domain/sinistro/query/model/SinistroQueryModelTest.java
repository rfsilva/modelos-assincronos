package com.seguradora.hibrida.domain.sinistro.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroQueryModel Tests")
class SinistroQueryModelTest {

    @Test
    @DisplayName("construtor padrão deve inicializar createdAt e updatedAt")
    void defaultConstructorShouldInitializeTimestamps() {
        SinistroQueryModel model = new SinistroQueryModel();

        assertThat(model.getCreatedAt()).isNotNull();
        assertThat(model.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("construtor com id e protocolo deve preencher os campos")
    void constructorWithIdAndProtocoloShouldSetFields() {
        UUID id = UUID.randomUUID();
        SinistroQueryModel model = new SinistroQueryModel(id, "SIN-2024-001");

        assertThat(model.getId()).isEqualTo(id);
        assertThat(model.getProtocolo()).isEqualTo("SIN-2024-001");
        assertThat(model.getCreatedAt()).isNotNull();
    }

    @Test
    @DisplayName("isAberto deve retornar true para status ABERTO")
    void isAbertoShouldReturnTrueForABERTO() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("ABERTO");

        assertThat(model.isAberto()).isTrue();
    }

    @Test
    @DisplayName("isAberto deve retornar true para status EM_ANALISE")
    void isAbertoShouldReturnTrueForEM_ANALISE() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("EM_ANALISE");

        assertThat(model.isAberto()).isTrue();
    }

    @Test
    @DisplayName("isAberto deve retornar false para status APROVADO")
    void isAbertoShouldReturnFalseForAPROVADO() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("APROVADO");

        assertThat(model.isAberto()).isFalse();
    }

    @Test
    @DisplayName("isFechado deve retornar true para status FECHADO")
    void isFechadoShouldReturnTrueForFECHADO() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("FECHADO");

        assertThat(model.isFechado()).isTrue();
    }

    @Test
    @DisplayName("isFechado deve retornar true para status CANCELADO")
    void isFechadoShouldReturnTrueForCANCELADO() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("CANCELADO");

        assertThat(model.isFechado()).isTrue();
    }

    @Test
    @DisplayName("isFechado deve retornar false para status ABERTO")
    void isFechadoShouldReturnFalseForABERTO() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setStatus("ABERTO");

        assertThat(model.isFechado()).isFalse();
    }

    @Test
    @DisplayName("isConsultaDetranSucesso deve retornar true quando realizada e status SUCCESS")
    void isConsultaDetranSucessoShouldReturnTrue() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setConsultaDetranRealizada(true);
        model.setConsultaDetranStatus("SUCCESS");

        assertThat(model.isConsultaDetranSucesso()).isTrue();
    }

    @Test
    @DisplayName("isConsultaDetranSucesso deve retornar false quando não realizada")
    void isConsultaDetranSucessoShouldReturnFalseWhenNotRealizada() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setConsultaDetranRealizada(false);
        model.setConsultaDetranStatus("SUCCESS");

        assertThat(model.isConsultaDetranSucesso()).isFalse();
    }

    @Test
    @DisplayName("isConsultaDetranSucesso deve retornar false quando status é ERROR")
    void isConsultaDetranSucessoShouldReturnFalseForError() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setConsultaDetranRealizada(true);
        model.setConsultaDetranStatus("ERROR");

        assertThat(model.isConsultaDetranSucesso()).isFalse();
    }

    @Test
    @DisplayName("isApoliceVigenteNaOcorrencia deve retornar true quando data dentro da vigência")
    void isApoliceVigenteNaOcorrenciaShouldReturnTrue() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setApoliceVigenciaInicio(LocalDate.of(2024, 1, 1));
        model.setApoliceVigenciaFim(LocalDate.of(2024, 12, 31));
        model.setDataOcorrencia(java.time.Instant.parse("2024-06-15T10:00:00Z"));

        assertThat(model.isApoliceVigenteNaOcorrencia()).isTrue();
    }

    @Test
    @DisplayName("isApoliceVigenteNaOcorrencia deve retornar false quando campos nulos")
    void isApoliceVigenteNaOcorrenciaShouldReturnFalseForNullFields() {
        SinistroQueryModel model = new SinistroQueryModel();

        assertThat(model.isApoliceVigenteNaOcorrencia()).isFalse();
    }

    @Test
    @DisplayName("isApoliceVigenteNaOcorrencia deve retornar false quando data anterior à vigência")
    void isApoliceVigenteNaOcorrenciaShouldReturnFalseBeforeVigencia() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setApoliceVigenciaInicio(LocalDate.of(2024, 6, 1));
        model.setApoliceVigenciaFim(LocalDate.of(2024, 12, 31));
        model.setDataOcorrencia(java.time.Instant.parse("2024-01-15T10:00:00Z"));

        assertThat(model.isApoliceVigenteNaOcorrencia()).isFalse();
    }

    @Test
    @DisplayName("getDadoDetran deve retornar valor do tipo correto")
    void getDadoDetranShouldReturnTypedValue() {
        SinistroQueryModel model = new SinistroQueryModel();
        Map<String, Object> dados = new HashMap<>();
        dados.put("situacao", "REGULAR");
        dados.put("multas", 3);
        model.setDadosDetran(dados);

        assertThat(model.getDadoDetran("situacao", String.class)).isEqualTo("REGULAR");
        assertThat(model.getDadoDetran("multas", Integer.class)).isEqualTo(3);
    }

    @Test
    @DisplayName("getDadoDetran deve retornar null quando chave não existe")
    void getDadoDetranShouldReturnNullForMissingKey() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.setDadosDetran(new HashMap<>());

        assertThat(model.getDadoDetran("inexistente", String.class)).isNull();
    }

    @Test
    @DisplayName("getDadoDetran deve retornar null quando dadosDetran é nulo")
    void getDadoDetranShouldReturnNullWhenDadosDetranNull() {
        SinistroQueryModel model = new SinistroQueryModel();

        assertThat(model.getDadoDetran("qualquer", String.class)).isNull();
    }

    @Test
    @DisplayName("getDadoDetran deve retornar null quando tipo não confere")
    void getDadoDetranShouldReturnNullForWrongType() {
        SinistroQueryModel model = new SinistroQueryModel();
        Map<String, Object> dados = new HashMap<>();
        dados.put("valor", "texto");
        model.setDadosDetran(dados);

        assertThat(model.getDadoDetran("valor", Integer.class)).isNull();
    }

    @Test
    @DisplayName("adicionarTag deve adicionar nova tag")
    void adicionarTagShouldAddNewTag() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.adicionarTag("URGENTE");

        assertThat(model.possuiTag("URGENTE")).isTrue();
        assertThat(model.getTags()).containsExactly("URGENTE");
    }

    @Test
    @DisplayName("adicionarTag não deve duplicar tag existente")
    void adicionarTagShouldNotDuplicateExistingTag() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.adicionarTag("URGENTE");
        model.adicionarTag("URGENTE");

        assertThat(model.getTags()).hasSize(1);
    }

    @Test
    @DisplayName("removerTag deve remover tag existente")
    void removerTagShouldRemoveExistingTag() {
        SinistroQueryModel model = new SinistroQueryModel();
        model.adicionarTag("URGENTE");
        model.adicionarTag("ALTO_VALOR");
        model.removerTag("URGENTE");

        assertThat(model.possuiTag("URGENTE")).isFalse();
        assertThat(model.possuiTag("ALTO_VALOR")).isTrue();
    }

    @Test
    @DisplayName("removerTag não deve lançar exceção quando tags é null")
    void removerTagShouldNotThrowWhenTagsNull() {
        SinistroQueryModel model = new SinistroQueryModel();

        org.assertj.core.api.Assertions.assertThatNoException()
                .isThrownBy(() -> model.removerTag("URGENTE"));
    }

    @Test
    @DisplayName("possuiTag deve retornar false quando tags é null")
    void possuiTagShouldReturnFalseWhenTagsNull() {
        SinistroQueryModel model = new SinistroQueryModel();

        assertThat(model.possuiTag("URGENTE")).isFalse();
    }

    @Test
    @DisplayName("toString deve incluir campos de identificação")
    void toStringShouldIncludeIdentificationFields() {
        SinistroQueryModel model = new SinistroQueryModel(UUID.randomUUID(), "SIN-001");
        model.setCpfSegurado("12345678901");
        model.setStatus("ABERTO");

        String str = model.toString();

        assertThat(str).contains("SIN-001");
        assertThat(str).contains("12345678901");
        assertThat(str).contains("ABERTO");
    }
}
