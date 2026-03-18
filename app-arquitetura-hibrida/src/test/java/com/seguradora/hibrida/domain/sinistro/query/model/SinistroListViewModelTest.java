package com.seguradora.hibrida.domain.sinistro.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroListView (Model) Tests")
class SinistroListViewModelTest {

    @Test
    @DisplayName("getDataOcorrenciaFormatada deve retornar string vazia quando data é nula")
    void getDataOcorrenciaFormatadaReturnsEmptyWhenNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getDataOcorrenciaFormatada()).isEmpty();
    }

    @Test
    @DisplayName("getDataOcorrenciaFormatada deve retornar data formatada em dd/MM/yyyy HH:mm")
    void getDataOcorrenciaFormatadaReturnsFormattedDate() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .dataOcorrencia(Instant.parse("2024-06-15T14:30:00Z"))
                .build();

        assertThat(view.getDataOcorrenciaFormatada()).matches("\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}");
    }

    @Test
    @DisplayName("getDataAberturaFormatada deve retornar string vazia quando data é nula")
    void getDataAberturaFormatadaReturnsEmptyWhenNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getDataAberturaFormatada()).isEmpty();
    }

    @Test
    @DisplayName("getValorEstimadoFormatado deve retornar R$ 0,00 quando valor é nulo")
    void getValorEstimadoFormatadoReturnsZeroWhenNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getValorEstimadoFormatado()).isEqualTo("R$ 0,00");
    }

    @Test
    @DisplayName("getValorEstimadoFormatado deve retornar valor formatado")
    void getValorEstimadoFormatadoReturnsFormatted() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .valorEstimado(new BigDecimal("15000.00"))
                .build();

        assertThat(view.getValorEstimadoFormatado()).contains("15");
        assertThat(view.getValorEstimadoFormatado()).startsWith("R$");
    }

    @Test
    @DisplayName("getLocalizacaoCompleta deve retornar 'Não informado' quando ambos são nulos")
    void getLocalizacaoCompletaReturnsNaoInformadoWhenBothNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getLocalizacaoCompleta()).isEqualTo("Não informado");
    }

    @Test
    @DisplayName("getLocalizacaoCompleta deve retornar Cidade/UF quando ambos preenchidos")
    void getLocalizacaoCompletaReturnsCidadeUF() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .cidadeOcorrencia("São Paulo")
                .estadoOcorrencia("SP")
                .build();

        assertThat(view.getLocalizacaoCompleta()).isEqualTo("São Paulo/SP");
    }

    @Test
    @DisplayName("getLocalizacaoCompleta deve retornar só cidade quando UF é nulo")
    void getLocalizacaoCompletaReturnsCidadeOnly() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .cidadeOcorrencia("São Paulo")
                .build();

        assertThat(view.getLocalizacaoCompleta()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("getLocalizacaoCompleta deve retornar só estado quando cidade é nula")
    void getLocalizacaoCompletaReturnsEstadoOnly() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .estadoOcorrencia("SP")
                .build();

        assertThat(view.getLocalizacaoCompleta()).isEqualTo("SP");
    }

    @Test
    @DisplayName("getSeguradoCpfFormatado deve retornar null quando CPF é nulo")
    void getSeguradoCpfFormatadoReturnsNullWhenNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getSeguradoCpfFormatado()).isNull();
    }

    @Test
    @DisplayName("getSeguradoCpfFormatado deve formatar CPF com 11 dígitos")
    void getSeguradoCpfFormatadoFormats11Digits() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .seguradoCpf("12345678901")
                .build();

        assertThat(view.getSeguradoCpfFormatado()).isEqualTo("123.456.789-01");
    }

    @Test
    @DisplayName("getSeguradoCpfFormatado deve retornar CPF sem formatação se não tiver 11 dígitos")
    void getSeguradoCpfFormatadoReturnsRawForShortCpf() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .seguradoCpf("123")
                .build();

        assertThat(view.getSeguradoCpfFormatado()).isEqualTo("123");
    }

    @Test
    @DisplayName("getVeiculoPlacaFormatada deve retornar null quando placa é nula")
    void getVeiculoPlacaFormatadaReturnsNullWhenNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getVeiculoPlacaFormatada()).isNull();
    }

    @Test
    @DisplayName("getVeiculoPlacaFormatada deve formatar placa Mercosul (7 chars)")
    void getVeiculoPlacaFormatadaFormatsMercosul() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .veiculoPlaca("ABC1D23")
                .build();

        // Formato Mercosul: ABC + 1 + D23 = "ABC1D23" mantido
        String formatted = view.getVeiculoPlacaFormatada();
        assertThat(formatted).hasSize(7);
    }

    @Test
    @DisplayName("getVeiculoPlacaFormatada deve formatar placa antiga (8 chars) com hífen")
    void getVeiculoPlacaFormatadaFormatsOldFormat() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .veiculoPlaca("ABC12345")
                .build();

        // Formato antigo: ABC-12345
        assertThat(view.getVeiculoPlacaFormatada()).isEqualTo("ABC-12345");
    }

    @Test
    @DisplayName("getStatusBadgeColor deve retornar 'info' para status ABERTO")
    void getStatusBadgeColorReturnsInfoForABERTO() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getStatusBadgeColor()).isEqualTo("info");
    }

    @Test
    @DisplayName("getStatusBadgeColor deve retornar 'success' para status APROVADO")
    void getStatusBadgeColorReturnsSuccessForAPROVADO() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("APROVADO")
                .build();

        assertThat(view.getStatusBadgeColor()).isEqualTo("success");
    }

    @Test
    @DisplayName("getStatusBadgeColor deve retornar 'danger' para status REPROVADO")
    void getStatusBadgeColorReturnsDangerForREPROVADO() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("REPROVADO")
                .build();

        assertThat(view.getStatusBadgeColor()).isEqualTo("danger");
    }

    @Test
    @DisplayName("getStatusBadgeColor deve retornar 'secondary' para status null")
    void getStatusBadgeColorReturnsSecondaryForNull() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .build();

        assertThat(view.getStatusBadgeColor()).isEqualTo("secondary");
    }

    @Test
    @DisplayName("getPrioridadeBadgeColor deve retornar cor correta para cada prioridade")
    void getPrioridadeBadgeColorReturnsCorrectColor() {
        assertThat(SinistroListView.builder().protocolo("X").prioridade("BAIXA").build().getPrioridadeBadgeColor()).isEqualTo("success");
        assertThat(SinistroListView.builder().protocolo("X").prioridade("NORMAL").build().getPrioridadeBadgeColor()).isEqualTo("info");
        assertThat(SinistroListView.builder().protocolo("X").prioridade("ALTA").build().getPrioridadeBadgeColor()).isEqualTo("warning");
        assertThat(SinistroListView.builder().protocolo("X").prioridade("URGENTE").build().getPrioridadeBadgeColor()).isEqualTo("danger");
    }

    @Test
    @DisplayName("isAberto deve retornar true para status ABERTO e EM_ANALISE")
    void isAbertoReturnsTrueForOpenStatuses() {
        assertThat(SinistroListView.builder().protocolo("X").status("ABERTO").build().isAberto()).isTrue();
        assertThat(SinistroListView.builder().protocolo("X").status("EM_ANALISE").build().isAberto()).isTrue();
        assertThat(SinistroListView.builder().protocolo("X").status("APROVADO").build().isAberto()).isFalse();
    }

    @Test
    @DisplayName("isPrecisaAtencao deve retornar true para prioridade URGENTE")
    void isPrecisaAtencaoTrueForUrgente() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .prioridade("URGENTE")
                .dentroSla(true)
                .documentosPendentes(false)
                .build();

        assertThat(view.isPrecisaAtencao()).isTrue();
    }

    @Test
    @DisplayName("isPrecisaAtencao deve retornar true quando fora do SLA")
    void isPrecisaAtencaoTrueWhenOutsideSla() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .prioridade("NORMAL")
                .dentroSla(false)
                .documentosPendentes(false)
                .build();

        assertThat(view.isPrecisaAtencao()).isTrue();
    }

    @Test
    @DisplayName("isPrecisaAtencao deve retornar true quando tem documentos pendentes")
    void isPrecisaAtencaoTrueWhenDocumentosPendentes() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .prioridade("NORMAL")
                .dentroSla(true)
                .documentosPendentes(true)
                .build();

        assertThat(view.isPrecisaAtencao()).isTrue();
    }

    @Test
    @DisplayName("getTipoIcone deve retornar ícone correto para cada tipo")
    void getTipoIconeReturnsCorrectIcon() {
        assertThat(SinistroListView.builder().protocolo("X").tipo("COLISAO").build().getTipoIcone()).isEqualTo("fa-car-crash");
        assertThat(SinistroListView.builder().protocolo("X").tipo("ROUBO").build().getTipoIcone()).isEqualTo("fa-user-secret");
        assertThat(SinistroListView.builder().protocolo("X").tipo("FURTO").build().getTipoIcone()).isEqualTo("fa-user-secret");
        assertThat(SinistroListView.builder().protocolo("X").tipo("INCENDIO").build().getTipoIcone()).isEqualTo("fa-fire");
        assertThat(SinistroListView.builder().protocolo("X").tipo("TERCEIROS").build().getTipoIcone()).isEqualTo("fa-users");
        assertThat(SinistroListView.builder().protocolo("X").tipo("OUTRO").build().getTipoIcone()).isEqualTo("fa-exclamation-triangle");
        assertThat(SinistroListView.builder().protocolo("X").build().getTipoIcone()).isEqualTo("fa-file");
    }

    @Test
    @DisplayName("toString deve incluir protocolo, segurado, status e tipo")
    void toStringShouldIncludeMainFields() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-001")
                .seguradoNome("João Silva")
                .status("ABERTO")
                .tipo("COLISAO")
                .build();

        String str = view.toString();

        assertThat(str).contains("SIN-001");
        assertThat(str).contains("João Silva");
        assertThat(str).contains("ABERTO");
        assertThat(str).contains("COLISAO");
    }
}
