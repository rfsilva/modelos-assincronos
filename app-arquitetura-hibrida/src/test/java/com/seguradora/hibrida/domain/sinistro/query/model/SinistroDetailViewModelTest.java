package com.seguradora.hibrida.domain.sinistro.query.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroDetailView (Model) Tests")
class SinistroDetailViewModelTest {

    @Test
    @DisplayName("getTimelineOrdenada deve retornar lista vazia quando timeline é null")
    void getTimelineOrdenadaReturnsEmptyWhenNull() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getTimelineOrdenada()).isEmpty();
    }

    @Test
    @DisplayName("getTimelineOrdenada deve retornar eventos em ordem decrescente de timestamp")
    void getTimelineOrdenadaReturnsSortedDesc() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        view.adicionarEvento("ABERTO", "Sinistro aberto", "sistema");
        // Adicionar pequena pausa para garantir ordem de timestamps
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        view.adicionarEvento("ANALISE", "Iniciou análise", "analista");

        List<Map<String, Object>> ordenada = view.getTimelineOrdenada();
        assertThat(ordenada).hasSize(2);
        // Mais recente primeiro
        assertThat(ordenada.get(0).get("evento")).isEqualTo("ANALISE");
        assertThat(ordenada.get(1).get("evento")).isEqualTo("ABERTO");
    }

    @Test
    @DisplayName("getDocumentosPorTipo deve retornar mapa vazio quando documentos é null")
    void getDocumentosPorTipoReturnsEmptyWhenNull() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getDocumentosPorTipo()).isEmpty();
    }

    @Test
    @DisplayName("getDocumentosPorTipo deve agrupar documentos por tipo")
    void getDocumentosPorTipoGroupsByType() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> doc1 = new HashMap<>();
        doc1.put("id", "doc-1");
        doc1.put("tipo", "FOTO_VEICULO");
        doc1.put("status", "VALIDADO");

        Map<String, Object> doc2 = new HashMap<>();
        doc2.put("id", "doc-2");
        doc2.put("tipo", "FOTO_VEICULO");
        doc2.put("status", "PENDENTE");

        Map<String, Object> doc3 = new HashMap<>();
        doc3.put("id", "doc-3");
        doc3.put("tipo", "LAUDO");
        doc3.put("status", "VALIDADO");

        view.adicionarDocumento(doc1);
        view.adicionarDocumento(doc2);
        view.adicionarDocumento(doc3);

        Map<String, List<Map<String, Object>>> porTipo = view.getDocumentosPorTipo();

        assertThat(porTipo).containsKey("FOTO_VEICULO");
        assertThat(porTipo).containsKey("LAUDO");
        assertThat(porTipo.get("FOTO_VEICULO")).hasSize(2);
        assertThat(porTipo.get("LAUDO")).hasSize(1);
    }

    @Test
    @DisplayName("getDocumentosValidados deve retornar apenas documentos com status VALIDADO")
    void getDocumentosValidadosReturnsOnlyValidated() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> validado = new HashMap<>();
        validado.put("id", "doc-1");
        validado.put("status", "VALIDADO");

        Map<String, Object> pendente = new HashMap<>();
        pendente.put("id", "doc-2");
        pendente.put("status", "PENDENTE");

        view.adicionarDocumento(validado);
        view.adicionarDocumento(pendente);

        assertThat(view.getDocumentosValidados()).hasSize(1);
        assertThat(view.getDocumentosValidados().get(0).get("id")).isEqualTo("doc-1");
    }

    @Test
    @DisplayName("getDocumentosPendentesOuRejeitados deve retornar documentos PENDENTE e REJEITADO")
    void getDocumentosPendentesOuRejeitadosReturnsBothStatuses() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> pendente = new HashMap<>();
        pendente.put("id", "doc-1");
        pendente.put("status", "PENDENTE");

        Map<String, Object> rejeitado = new HashMap<>();
        rejeitado.put("id", "doc-2");
        rejeitado.put("status", "REJEITADO");

        Map<String, Object> validado = new HashMap<>();
        validado.put("id", "doc-3");
        validado.put("status", "VALIDADO");

        view.adicionarDocumento(pendente);
        view.adicionarDocumento(rejeitado);
        view.adicionarDocumento(validado);

        assertThat(view.getDocumentosPendentesOuRejeitados()).hasSize(2);
    }

    @Test
    @DisplayName("getUltimoEvento deve retornar empty quando timeline é null")
    void getUltimoEventoReturnsEmptyWhenNull() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getUltimoEvento()).isEmpty();
    }

    @Test
    @DisplayName("getUltimoEvento deve retornar o evento com maior timestamp")
    void getUltimoEventoReturnsLatest() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        view.adicionarEvento("ABERTO", "Sinistro aberto", "sistema");
        try { Thread.sleep(5); } catch (InterruptedException ignored) {}
        view.adicionarEvento("ANALISE", "Iniciou análise", "analista");

        Optional<Map<String, Object>> ultimo = view.getUltimoEvento();

        assertThat(ultimo).isPresent();
        assertThat(ultimo.get().get("evento")).isEqualTo("ANALISE");
    }

    @Test
    @DisplayName("adicionarDocumento deve atualizar quantidadeDocumentos")
    void adicionarDocumentoShouldUpdateCount() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc-1");
        doc.put("status", "VALIDADO");

        view.adicionarDocumento(doc);

        assertThat(view.getQuantidadeDocumentos()).isEqualTo(1);
    }

    @Test
    @DisplayName("adicionarDocumento deve atualizar documentosPendentes quando há pendente")
    void adicionarDocumentoShouldSetDocumentosPendentes() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc-1");
        doc.put("status", "PENDENTE");

        view.adicionarDocumento(doc);

        assertThat(view.getDocumentosPendentes()).isTrue();
    }

    @Test
    @DisplayName("removerDocumento deve reduzir contagem e atualizar documentosPendentes")
    void removerDocumentoShouldUpdateCountAndStatus() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> doc = new HashMap<>();
        doc.put("id", "doc-1");
        doc.put("status", "PENDENTE");

        view.adicionarDocumento(doc);
        assertThat(view.getDocumentosPendentes()).isTrue();

        view.removerDocumento("doc-1");

        assertThat(view.getQuantidadeDocumentos()).isEqualTo(0);
        assertThat(view.getDocumentosPendentes()).isFalse();
    }

    @Test
    @DisplayName("podeSerAprovado deve retornar true quando condições são atendidas")
    void podeSerAprovadoTrueWhenConditionsMet() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("EM_ANALISE")
                .documentosPendentes(false)
                .consultaDetranRealizada(true)
                .build();

        assertThat(view.podeSerAprovado()).isTrue();
    }

    @Test
    @DisplayName("podeSerAprovado deve retornar false quando há documentos pendentes")
    void podeSerAprovadoFalseWhenDocsPendentes() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("EM_ANALISE")
                .documentosPendentes(true)
                .consultaDetranRealizada(true)
                .build();

        assertThat(view.podeSerAprovado()).isFalse();
    }

    @Test
    @DisplayName("podeSerAprovado deve retornar false quando consulta Detran não foi realizada")
    void podeSerAprovadoFalseWhenDetranNotRealizada() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("EM_ANALISE")
                .documentosPendentes(false)
                .consultaDetranRealizada(false)
                .build();

        assertThat(view.podeSerAprovado()).isFalse();
    }

    @Test
    @DisplayName("podeSerAprovado deve retornar false quando status não é ABERTO nem EM_ANALISE")
    void podeSerAprovadoFalseWhenWrongStatus() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("REPROVADO")
                .documentosPendentes(false)
                .consultaDetranRealizada(true)
                .build();

        assertThat(view.podeSerAprovado()).isFalse();
    }

    @Test
    @DisplayName("hasRestricoesDetran deve retornar false quando historicoDetran é null")
    void hasRestricoesDetranFalseWhenNull() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.hasRestricoesDetran()).isFalse();
    }

    @Test
    @DisplayName("hasRestricoesDetran deve retornar true quando há restrições")
    void hasRestricoesDetranTrueWhenRestrictions() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> dados = new HashMap<>();
        dados.put("restricoes", List.of("MULTA_GRAVE"));
        Map<String, Object> historico = new HashMap<>();
        historico.put("dados", dados);
        view.setHistoricoDetran(historico);

        assertThat(view.hasRestricoesDetran()).isTrue();
    }

    @Test
    @DisplayName("hasRestricoesDetran deve retornar false quando restrições é lista vazia")
    void hasRestricoesDetranFalseForEmptyList() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        Map<String, Object> dados = new HashMap<>();
        dados.put("restricoes", new ArrayList<>());
        Map<String, Object> historico = new HashMap<>();
        historico.put("dados", dados);
        view.setHistoricoDetran(historico);

        assertThat(view.hasRestricoesDetran()).isFalse();
    }

    @Test
    @DisplayName("getTempoProcessamentoDias deve retornar 0 quando tempo é null ou zero")
    void getTempoProcessamentoDiasReturnsZeroForNull() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getTempoProcessamentoDias()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("getTempoProcessamentoDias deve converter minutos para dias")
    void getTempoProcessamentoDiasConvertsMinutesToDays() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .tempoProcessamentoMinutos(1440L) // 1 dia
                .build();

        assertThat(view.getTempoProcessamentoDias()).isEqualTo(1.0);
    }

    @Test
    @DisplayName("getSeguradoEnderecoCompleto deve retornar 'Não informado' quando todos nulos")
    void getSeguradoEnderecoCompletoReturnsNaoInformado() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getSeguradoEnderecoCompleto()).isEqualTo("Não informado");
    }

    @Test
    @DisplayName("getSeguradoEnderecoCompleto deve formatar endereço completo")
    void getSeguradoEnderecoCompletoFormatsCorrectly() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .seguradoEndereco("Rua A, 123")
                .seguradoCidade("São Paulo")
                .seguradoEstado("SP")
                .build();

        assertThat(view.getSeguradoEnderecoCompleto()).contains("Rua A, 123");
        assertThat(view.getSeguradoEnderecoCompleto()).contains("São Paulo");
        assertThat(view.getSeguradoEnderecoCompleto()).contains("SP");
    }

    @Test
    @DisplayName("getOcorrenciaEnderecoCompleto deve retornar 'Não informado' quando todos nulos")
    void getOcorrenciaEnderecoCompletoReturnsNaoInformado() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .build();

        assertThat(view.getOcorrenciaEnderecoCompleto()).isEqualTo("Não informado");
    }

    @Test
    @DisplayName("getOcorrenciaEnderecoCompleto deve incluir CEP quando fornecido")
    void getOcorrenciaEnderecoCompletoIncludesCep() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .status("ABERTO")
                .enderecoOcorrencia("Av. B, 456")
                .cidadeOcorrencia("Campinas")
                .estadoOcorrencia("SP")
                .cepOcorrencia("13000000")
                .build();

        String endereco = view.getOcorrenciaEnderecoCompleto();
        assertThat(endereco).contains("Av. B, 456");
        assertThat(endereco).contains("13000000");
    }

    @Test
    @DisplayName("toString deve incluir protocolo, segurado, status e quantidade de docs")
    void toStringShouldIncludeKeyFields() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-001")
                .seguradoNome("João Silva")
                .status("ABERTO")
                .build();

        String str = view.toString();

        assertThat(str).contains("SIN-001");
        assertThat(str).contains("João Silva");
        assertThat(str).contains("ABERTO");
    }
}
