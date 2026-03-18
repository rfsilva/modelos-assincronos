package com.seguradora.hibrida.domain.sinistro.command;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("IniciarAnaliseCommand Tests")
class IniciarAnaliseCommandTest {

    private IniciarAnaliseCommand comando() {
        return IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001")
                .analistaId("ANALISTA-01")
                .prioridadeAnalise("ALTA")
                .prazoEstimado(5)
                .build();
    }

    @Test
    @DisplayName("deve criar comando com todos os campos via builder")
    void shouldCreateWithBuilder() {
        IniciarAnaliseCommand c = comando();
        assertThat(c.getSinistroId()).isEqualTo("SIN-001");
        assertThat(c.getAnalistaId()).isEqualTo("ANALISTA-01");
        assertThat(c.getPrioridadeAnalise()).isEqualTo("ALTA");
        assertThat(c.getPrazoEstimado()).isEqualTo(5);
    }

    @Test
    @DisplayName("getCommandType deve retornar IniciarAnaliseCommand")
    void getCommandTypeShouldReturnCorrectType() {
        assertThat(comando().getCommandType()).isEqualTo("IniciarAnaliseCommand");
    }

    @Test
    @DisplayName("isPrioridadeValida deve retornar true para ALTA, MEDIA, BAIXA")
    void isPrioridadeValidaShouldReturnTrueForValidValues() {
        assertThat(IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("A").prioridadeAnalise("ALTA").prazoEstimado(5).build()
                .isPrioridadeValida()).isTrue();
        assertThat(IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("A").prioridadeAnalise("MEDIA").prazoEstimado(5).build()
                .isPrioridadeValida()).isTrue();
        assertThat(IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("A").prioridadeAnalise("BAIXA").prazoEstimado(5).build()
                .isPrioridadeValida()).isTrue();
    }

    @Test
    @DisplayName("isPrioridadeValida deve retornar false para valor inválido")
    void isPrioridadeValidaShouldReturnFalseForInvalid() {
        IniciarAnaliseCommand c = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("ANALISTA-01")
                .prioridadeAnalise("URGENTE").prazoEstimado(5).build();
        assertThat(c.isPrioridadeValida()).isFalse();
    }

    @Test
    @DisplayName("isPrazoUrgente deve retornar true quando prazo <= 3")
    void isPrazoUrgenteShouldReturnTrueWhenThreeOrLess() {
        IniciarAnaliseCommand c = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("A").prioridadeAnalise("ALTA").prazoEstimado(3).build();
        assertThat(c.isPrazoUrgente()).isTrue();
    }

    @Test
    @DisplayName("isPrazoUrgente deve retornar false quando prazo > 3")
    void isPrazoUrgenteShouldReturnFalseWhenMoreThanThree() {
        assertThat(comando().isPrazoUrgente()).isFalse();
    }

    @Test
    @DisplayName("isPrioridadeAlta deve retornar true quando ALTA")
    void isPrioridadeAltaShouldReturnTrueWhenAlta() {
        assertThat(comando().isPrioridadeAlta()).isTrue();
    }

    @Test
    @DisplayName("isPrioridadeAlta deve retornar false quando MEDIA")
    void isPrioridadeAltaShouldReturnFalseWhenMedia() {
        IniciarAnaliseCommand c = IniciarAnaliseCommand.builder()
                .sinistroId("SIN-001").analistaId("A").prioridadeAnalise("MEDIA").prazoEstimado(5).build();
        assertThat(c.isPrioridadeAlta()).isFalse();
    }

    @Test
    @DisplayName("toString deve conter sinistroId e prioridade")
    void toStringShouldContainSinistroAndPrioridade() {
        assertThat(comando().toString()).contains("SIN-001").contains("ALTA");
    }
}
