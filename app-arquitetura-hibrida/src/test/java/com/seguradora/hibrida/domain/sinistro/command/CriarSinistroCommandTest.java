package com.seguradora.hibrida.domain.sinistro.command;

import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("CriarSinistroCommand Tests")
class CriarSinistroCommandTest {

    private CriarSinistroCommand comando() {
        return CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001")
                .veiculoId("VEI-001")
                .apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.COLISAO)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000, São Paulo")
                .descricao("Colisão traseira em via de alto fluxo com danos significativos")
                .operadorId("OP-001")
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        CriarSinistroCommand c = comando();
        assertThat(c.getProtocolo()).isEqualTo("SIN-2024-000001");
        assertThat(c.getSeguradoId()).isEqualTo("SEG-001");
        assertThat(c.getVeiculoId()).isEqualTo("VEI-001");
        assertThat(c.getApoliceId()).isEqualTo("APO-001");
        assertThat(c.getTipoSinistro()).isEqualTo(TipoSinistro.COLISAO);
        assertThat(c.getOperadorId()).isEqualTo("OP-001");
    }

    @Test
    @DisplayName("commandId e timestamp devem ser preenchidos automaticamente")
    void commandIdAndTimestampShouldBeAutoFilled() {
        CriarSinistroCommand c = comando();
        assertThat(c.getCommandId()).isNotNull();
        assertThat(c.getTimestamp()).isNotNull();
    }

    @Test
    @DisplayName("getCommandType deve retornar CriarSinistroCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("CriarSinistroCommand");
    }

    @Test
    @DisplayName("isDataOcorrenciaValida deve retornar true para data passada")
    void isDataOcorrenciaValidaShouldReturnTrueForPastDate() {
        assertThat(comando().isDataOcorrenciaValida()).isTrue();
    }

    @Test
    @DisplayName("isDataOcorrenciaValida deve retornar false para data futura")
    void isDataOcorrenciaValidaShouldReturnFalseForFutureDate() {
        CriarSinistroCommand c = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001")
                .veiculoId("VEI-001")
                .apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.COLISAO)
                .dataOcorrencia(LocalDateTime.now().plusDays(1))
                .localOcorrencia("Av. Paulista, 1000, São Paulo")
                .descricao("Colisão traseira em via de alto fluxo com danos significativos")
                .operadorId("OP-001")
                .build();
        assertThat(c.isDataOcorrenciaValida()).isFalse();
    }

    @Test
    @DisplayName("isDataOcorrenciaValida deve retornar false para data nula")
    void isDataOcorrenciaValidaShouldReturnFalseForNull() {
        CriarSinistroCommand c = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .tipoSinistro(TipoSinistro.COLISAO)
                .build();
        assertThat(c.isDataOcorrenciaValida()).isFalse();
    }

    @Test
    @DisplayName("isBoletimObrigatorioValido deve retornar true quando BO preenchido para tipo que exige")
    void isBoletimObrigatorioValidoShouldReturnTrueWhenBoFilled() {
        CriarSinistroCommand c = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001").veiculoId("VEI-001").apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.ROUBO_FURTO)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000, São Paulo")
                .descricao("Roubo do veículo na via pública com violência")
                .operadorId("OP-001")
                .boletimOcorrencia("BO-2024-001")
                .build();
        assertThat(c.isBoletimObrigatorioValido()).isTrue();
    }

    @Test
    @DisplayName("isBoletimObrigatorioValido deve retornar false quando BO ausente para tipo que exige")
    void isBoletimObrigatorioValidoShouldReturnFalseWhenBoMissing() {
        CriarSinistroCommand c = CriarSinistroCommand.builder()
                .protocolo("SIN-2024-000001")
                .seguradoId("SEG-001").veiculoId("VEI-001").apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.ROUBO_FURTO)
                .dataOcorrencia(LocalDateTime.now().minusDays(1))
                .localOcorrencia("Av. Paulista, 1000, São Paulo")
                .descricao("Roubo do veículo na via pública com violência")
                .operadorId("OP-001")
                .build();
        assertThat(c.isBoletimObrigatorioValido()).isFalse();
    }

    @Test
    @DisplayName("isBoletimObrigatorioValido deve retornar true para tipo que não exige BO")
    void isBoletimObrigatorioValidoShouldReturnTrueForTypeWithoutBo() {
        // COLISAO não requer BO
        assertThat(comando().isBoletimObrigatorioValido()).isTrue();
    }

    @Test
    @DisplayName("toString deve conter protocolo e tipoSinistro")
    void toStringShouldContainProtocoloAndTipo() {
        String str = comando().toString();
        assertThat(str).contains("SIN-2024-000001").contains("COLISAO");
    }
}
