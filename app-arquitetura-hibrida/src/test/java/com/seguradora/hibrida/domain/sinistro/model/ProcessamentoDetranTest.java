package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ProcessamentoDetran Tests")
class ProcessamentoDetranTest {

    private ProcessamentoDetran processamentoConcluido() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("possui_restricao", false);
        dados.put("possui_debito", false);
        Instant inicio = Instant.now().minusSeconds(10);
        return ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .dadosRetornados(dados)
                .tentativas(1)
                .dataInicio(inicio)
                .dataFim(Instant.now())
                .placa("ABC1234")
                .renavam("123456789")
                .build();
    }

    @Test
    @DisplayName("isSucesso deve retornar true quando CONCLUIDA com dados")
    void isSucessoShouldReturnTrueWhenConcluida() {
        assertThat(processamentoConcluido().isSucesso()).isTrue();
    }

    @Test
    @DisplayName("isSucesso deve retornar false quando status não é CONCLUIDA")
    void isSucessoShouldReturnFalseWhenNotConcluida() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.FALHADA)
                .tentativas(1)
                .build();
        assertThat(p.isSucesso()).isFalse();
    }

    @Test
    @DisplayName("isSucesso deve retornar false quando dados vazio")
    void isSucessoShouldReturnFalseWhenEmptyData() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .tentativas(1)
                .build();
        assertThat(p.isSucesso()).isFalse();
    }

    @Test
    @DisplayName("deveRetry deve retornar true quando FALHADA e tentativas < 3")
    void deveRetryShouldReturnTrueWhenFailedUnder3Attempts() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.FALHADA)
                .tentativas(2)
                .build();
        assertThat(p.deveRetry()).isTrue();
    }

    @Test
    @DisplayName("deveRetry deve retornar false quando tentativas >= 3")
    void deveRetryShouldReturnFalseWhenAtMaxAttempts() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.FALHADA)
                .tentativas(3)
                .build();
        assertThat(p.deveRetry()).isFalse();
    }

    @Test
    @DisplayName("deveRetry deve retornar false quando status CONCLUIDA")
    void deveRetryShouldReturnFalseWhenConcluida() {
        assertThat(processamentoConcluido().deveRetry()).isFalse();
    }

    @Test
    @DisplayName("deveRetry deve retornar false quando status nulo")
    void deveRetryShouldReturnFalseWhenStatusNull() {
        ProcessamentoDetran p = ProcessamentoDetran.builder().build();
        assertThat(p.deveRetry()).isFalse();
    }

    @Test
    @DisplayName("getTempoProcessamentoMs deve retornar diferença entre datas")
    void getTempoProcessamentoMsShouldReturnDifference() {
        Instant inicio = Instant.ofEpochMilli(1000L);
        Instant fim = Instant.ofEpochMilli(5000L);
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .dataInicio(inicio)
                .dataFim(fim)
                .tentativas(1)
                .build();
        assertThat(p.getTempoProcessamentoMs()).isEqualTo(4000L);
    }

    @Test
    @DisplayName("getTempoProcessamentoMs deve retornar 0 quando datas nulas")
    void getTempoProcessamentoMsShouldReturn0WhenNull() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.PENDENTE)
                .tentativas(0)
                .build();
        assertThat(p.getTempoProcessamentoMs()).isEqualTo(0L);
    }

    @Test
    @DisplayName("excedeuTimeout deve retornar true quando status TIMEOUT")
    void excedeuTimeoutShouldReturnTrueWhenStatusTimeout() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.TIMEOUT)
                .tentativas(1)
                .build();
        assertThat(p.excedeuTimeout()).isTrue();
    }

    @Test
    @DisplayName("excedeuTimeout deve retornar true quando tempo > 30s")
    void excedeuTimeoutShouldReturnTrueWhenOver30Seconds() {
        Instant inicio = Instant.now().minusSeconds(40);
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.EM_ANDAMENTO)
                .dataInicio(inicio)
                .dataFim(Instant.now())
                .tentativas(1)
                .build();
        assertThat(p.excedeuTimeout()).isTrue();
    }

    @Test
    @DisplayName("getDado deve retornar valor correto quando chave existe e tipo bate")
    void getDadoShouldReturnValueWhenKeyExistsAndTypeMatches() {
        ProcessamentoDetran p = processamentoConcluido();
        assertThat(p.getDado("possui_restricao", Boolean.class)).isEqualTo(false);
    }

    @Test
    @DisplayName("getDado deve retornar null quando chave não existe")
    void getDadoShouldReturnNullWhenKeyNotFound() {
        ProcessamentoDetran p = processamentoConcluido();
        assertThat(p.getDado("inexistente", Boolean.class)).isNull();
    }

    @Test
    @DisplayName("possuiRestricao deve retornar false quando não há restrição")
    void possuiRestricaoShouldReturnFalseWhenNoRestriction() {
        assertThat(processamentoConcluido().possuiRestricao()).isFalse();
    }

    @Test
    @DisplayName("possuiRestricao deve retornar true quando há restrição")
    void possuiRestricaoShouldReturnTrueWhenRestricted() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("possui_restricao", true);
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .dadosRetornados(dados)
                .tentativas(1)
                .build();
        assertThat(p.possuiRestricao()).isTrue();
    }

    @Test
    @DisplayName("isVeiculoRegular deve retornar true quando sem restrição e sem débito")
    void isVeiculoRegularShouldReturnTrueWhenClean() {
        assertThat(processamentoConcluido().isVeiculoRegular()).isTrue();
    }

    @Test
    @DisplayName("isVeiculoRegular deve retornar false quando consulta não concluída")
    void isVeiculoRegularShouldReturnFalseWhenNotSucceeded() {
        ProcessamentoDetran p = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.PENDENTE)
                .tentativas(0)
                .build();
        assertThat(p.isVeiculoRegular()).isFalse();
    }

    @Test
    @DisplayName("equals deve comparar campos relevantes")
    void equalsShouldCompareRelevantFields() {
        Instant inicio = Instant.now();
        ProcessamentoDetran p1 = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .tentativas(1)
                .placa("ABC1234")
                .renavam("123456789")
                .dataInicio(inicio)
                .build();
        ProcessamentoDetran p2 = ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .tentativas(1)
                .placa("ABC1234")
                .renavam("123456789")
                .dataInicio(inicio)
                .build();
        assertThat(p1).isEqualTo(p2);
    }

    @Test
    @DisplayName("toString deve conter placa e status")
    void toStringShouldContainPlacaAndStatus() {
        assertThat(processamentoConcluido().toString())
                .contains("ABC1234");
    }
}
