package com.seguradora.hibrida.domain.sinistro.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("AnexarDocumentoCommand Tests")
class AnexarDocumentoCommandTest {

    private AnexarDocumentoCommand comando() {
        return AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001")
                .documentoId("DOC-001")
                .tipoDocumento("BOLETIM_OCORRENCIA")
                .operadorId("OP-001")
                .observacoes("Documento digitalizado")
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        AnexarDocumentoCommand c = comando();
        assertThat(c.getSinistroId()).isEqualTo("SIN-001");
        assertThat(c.getDocumentoId()).isEqualTo("DOC-001");
        assertThat(c.getTipoDocumento()).isEqualTo("BOLETIM_OCORRENCIA");
        assertThat(c.getOperadorId()).isEqualTo("OP-001");
        assertThat(c.getObservacoes()).isEqualTo("Documento digitalizado");
    }

    @Test
    @DisplayName("getCommandType deve retornar AnexarDocumentoCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("AnexarDocumentoCommand");
    }

    @Test
    @DisplayName("isTipoDocumentoValido deve retornar true para tipo válido")
    void isTipoDocumentoValidoShouldReturnTrueForValidType() {
        assertThat(comando().isTipoDocumentoValido()).isTrue();
    }

    @Test
    @DisplayName("isTipoDocumentoValido deve retornar false para tipo inválido")
    void isTipoDocumentoValidoShouldReturnFalseForInvalidType() {
        AnexarDocumentoCommand c = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001").documentoId("DOC-001")
                .tipoDocumento("TIPO_INVALIDO").operadorId("OP-001")
                .build();
        assertThat(c.isTipoDocumentoValido()).isFalse();
    }

    @Test
    @DisplayName("isDocumentoObrigatorio deve retornar true para BOLETIM_OCORRENCIA")
    void isDocumentoObrigatorioShouldReturnTrueForBoletim() {
        assertThat(comando().isDocumentoObrigatorio()).isTrue();
    }

    @Test
    @DisplayName("isDocumentoObrigatorio deve retornar false para FOTO_VEICULO")
    void isDocumentoObrigatorioShouldReturnFalseForFoto() {
        AnexarDocumentoCommand c = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001").documentoId("DOC-001")
                .tipoDocumento("FOTO_VEICULO").operadorId("OP-001")
                .build();
        assertThat(c.isDocumentoObrigatorio()).isFalse();
    }

    @Test
    @DisplayName("hasObservacoes deve retornar true quando observacoes preenchidas")
    void hasObservacoesShouldReturnTrueWhenFilled() {
        assertThat(comando().hasObservacoes()).isTrue();
    }

    @Test
    @DisplayName("hasObservacoes deve retornar false quando observacoes nulas")
    void hasObservacoesShouldReturnFalseWhenNull() {
        AnexarDocumentoCommand c = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001").documentoId("DOC-001")
                .tipoDocumento("OUTROS").operadorId("OP-001")
                .build();
        assertThat(c.hasObservacoes()).isFalse();
    }

    @Test
    @DisplayName("getTipoDocumentoEnum deve retornar enum correspondente")
    void getTipoDocumentoEnumShouldReturnCorrectEnum() {
        assertThat(comando().getTipoDocumentoEnum())
                .isEqualTo(AnexarDocumentoCommand.TipoDocumento.BOLETIM_OCORRENCIA);
    }

    @Test
    @DisplayName("getTipoDocumentoEnum deve retornar null para tipo inválido")
    void getTipoDocumentoEnumShouldReturnNullForInvalidType() {
        AnexarDocumentoCommand c = AnexarDocumentoCommand.builder()
                .sinistroId("SIN-001").documentoId("DOC-001")
                .tipoDocumento("INVALIDO").operadorId("OP-001")
                .build();
        assertThat(c.getTipoDocumentoEnum()).isNull();
    }

    @Test
    @DisplayName("toString deve conter sinistroId e tipoDocumento")
    void toStringShouldContainSinistroAndTipo() {
        assertThat(comando().toString()).contains("SIN-001").contains("BOLETIM_OCORRENCIA");
    }
}
