package com.seguradora.hibrida.domain.sinistro.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Sinistro Tests")
class SinistroTest {

    private LocalOcorrencia localCompleto() {
        return LocalOcorrencia.builder()
                .logradouro("Rua das Flores").numero("1").cidade("SP").estado("SP").build();
    }

    private OcorrenciaSinistro ocorrenciaValida() {
        return OcorrenciaSinistro.builder()
                .dataOcorrencia(Instant.now().minusSeconds(3600))
                .localOcorrencia(localCompleto())
                .descricao("Colisão na esquina com danos no para-choque dianteiro")
                .boletimOcorrencia("BO-2024-001")
                .build();
    }

    private AvaliacaoDanos avaliacaoCompleta() {
        return AvaliacaoDanos.builder()
                .tipoDano(TipoDano.TERCEIROS)
                .valorEstimado(new BigDecimal("3000.00"))
                .build();
    }

    private ProcessamentoDetran detranSucesso() {
        Map<String, Object> dados = new HashMap<>();
        dados.put("possui_restricao", false);
        dados.put("possui_debito", false);
        return ProcessamentoDetran.builder()
                .status(DetranConsultaStatus.CONCLUIDA)
                .dadosRetornados(dados)
                .tentativas(1)
                .dataInicio(Instant.now().minusSeconds(5))
                .dataFim(Instant.now())
                .placa("ABC1234")
                .build();
    }

    private Sinistro sinistroValidado() {
        return Sinistro.builder()
                .id("SIN-001")
                .protocolo(ProtocoloSinistro.of("2024-000001"))
                .seguradoId("SEG-001")
                .veiculoId("VEI-001")
                .apoliceId("APO-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.VALIDADO)
                .ocorrencia(ocorrenciaValida())
                .avaliacaoDanos(avaliacaoCompleta())
                .dataCriacao(Instant.now())
                .build();
    }

    @Test
    @DisplayName("podeIniciarAnalise deve retornar true quando VALIDADO e sem analista")
    void podeIniciarAnaliseShouldReturnTrueWhenValidadoNoAnalyst() {
        assertThat(sinistroValidado().podeIniciarAnalise()).isTrue();
    }

    @Test
    @DisplayName("podeIniciarAnalise deve retornar false quando status não é VALIDADO")
    void podeIniciarAnaliseShouldReturnFalseWhenNotValidado() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-002")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.NOVO)
                .build();
        assertThat(sinistro.podeIniciarAnalise()).isFalse();
    }

    @Test
    @DisplayName("podeIniciarAnalise deve retornar false quando analista já atribuído")
    void podeIniciarAnaliseShouldReturnFalseWhenAnalystAssigned() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001").protocolo(ProtocoloSinistro.of("2024-000001"))
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.VALIDADO)
                .ocorrencia(ocorrenciaValida()).avaliacaoDanos(avaliacaoCompleta())
                .analistaResponsavel("ANALISTA-01")
                .dataCriacao(Instant.now())
                .build();
        assertThat(sinistro.podeIniciarAnalise()).isFalse();
    }

    @Test
    @DisplayName("possuiDadosCompletos deve retornar true quando todos os dados completos (sem Detran)")
    void possuiDadosCompletosShouldReturnTrueWhenComplete() {
        // TERCEIROS não requer Detran nem BO
        assertThat(sinistroValidado().possuiDadosCompletos()).isTrue();
    }

    @Test
    @DisplayName("possuiDadosCompletos deve retornar false quando ocorrência inválida")
    void possuiDadosCompletosShouldReturnFalseWhenInvalidOcorrencia() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.VALIDADO)
                .ocorrencia(OcorrenciaSinistro.builder().build())
                .avaliacaoDanos(avaliacaoCompleta())
                .build();
        assertThat(sinistro.possuiDadosCompletos()).isFalse();
    }

    @Test
    @DisplayName("possuiDadosCompletos deve retornar false quando avaliação incompleta")
    void possuiDadosCompletosShouldReturnFalseWhenIncompleteAvaliacao() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.VALIDADO)
                .ocorrencia(ocorrenciaValida())
                .avaliacaoDanos(AvaliacaoDanos.builder().tipoDano(TipoDano.TERCEIROS).build())
                .build();
        assertThat(sinistro.possuiDadosCompletos()).isFalse();
    }

    @Test
    @DisplayName("possuiDadosCompletos deve retornar false quando Detran necessário e não concluído")
    void possuiDadosCompletosShouldReturnFalseWhenDetranRequiredButNotDone() {
        // COLISAO requer Detran
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-003")
                .tipoSinistro(TipoSinistro.COLISAO)
                .status(StatusSinistro.EM_ANALISE)
                .ocorrencia(ocorrenciaValida())
                .avaliacaoDanos(AvaliacaoDanos.builder()
                        .tipoDano(TipoDano.PARCIAL)
                        .valorEstimado(new BigDecimal("5000.00"))
                        .laudoPericial("LAUDO")
                        .build())
                .build();
        assertThat(sinistro.possuiDadosCompletos()).isFalse();
    }

    @Test
    @DisplayName("podeAprovar deve retornar true quando dados completos e status EM_ANALISE")
    void podeAprovarShouldReturnTrueWhenComplete() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001").protocolo(ProtocoloSinistro.of("2024-000001"))
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.EM_ANALISE)
                .ocorrencia(ocorrenciaValida()).avaliacaoDanos(avaliacaoCompleta())
                .dataCriacao(Instant.now())
                .build();
        assertThat(sinistro.podeAprovar()).isTrue();
    }

    @Test
    @DisplayName("podeAprovar deve retornar false quando status NOVO")
    void podeAprovarShouldReturnFalseWhenNovo() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.NOVO)
                .ocorrencia(ocorrenciaValida()).avaliacaoDanos(avaliacaoCompleta())
                .build();
        assertThat(sinistro.podeAprovar()).isFalse();
    }

    @Test
    @DisplayName("podeReprovar deve retornar true quando status EM_ANALISE ou DADOS_COLETADOS")
    void podeReprovarShouldReturnTrueForCorrectStatuses() {
        Sinistro emAnalise = Sinistro.builder()
                .id("SIN-001").tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.EM_ANALISE).build();
        Sinistro dadosColetados = Sinistro.builder()
                .id("SIN-001").tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.DADOS_COLETADOS).build();
        assertThat(emAnalise.podeReprovar()).isTrue();
        assertThat(dadosColetados.podeReprovar()).isTrue();
    }

    @Test
    @DisplayName("podeReprovar deve retornar false quando status NOVO")
    void podeReprovarShouldReturnFalseWhenNovo() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001").tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.NOVO).build();
        assertThat(sinistro.podeReprovar()).isFalse();
    }

    @Test
    @DisplayName("isDentroDoPrazo deve retornar true quando prazo nulo")
    void isDentroDoPrazoShouldReturnTrueWhenPrazoNull() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-004")
                .tipoSinistro(TipoSinistro.TERCEIROS)
                .status(StatusSinistro.NOVO)
                .build();
        assertThat(sinistro.isDentroDoPrazo()).isTrue();
    }

    @Test
    @DisplayName("isDentroDoPrazo deve retornar true quando prazo válido")
    void isDentroDoPrazoShouldReturnTrueWhenValid() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001").tipoSinistro(TipoSinistro.TERCEIROS).status(StatusSinistro.VALIDADO)
                .prazoProcessamento(PrazoProcessamento.criar(TipoSinistro.COLISAO))
                .build();
        assertThat(sinistro.isDentroDoPrazo()).isTrue();
    }

    @Test
    @DisplayName("isConsultaDetranSucesso deve retornar false quando sem processamento")
    void isConsultaDetranSucessoShouldReturnFalseWhenNull() {
        assertThat(sinistroValidado().isConsultaDetranSucesso()).isFalse();
    }

    @Test
    @DisplayName("isConsultaDetranSucesso deve retornar true quando consulta concluída")
    void isConsultaDetranSucessoShouldReturnTrueWhenSucceeded() {
        Sinistro sinistro = Sinistro.builder()
                .id("SIN-001").tipoSinistro(TipoSinistro.COLISAO).status(StatusSinistro.VALIDADO)
                .processamentoDetran(detranSucesso())
                .build();
        assertThat(sinistro.isConsultaDetranSucesso()).isTrue();
    }

    @Test
    @DisplayName("equals deve comparar id e protocolo")
    void equalsShouldCompareIdAndProtocolo() {
        Sinistro s1 = sinistroValidado();
        Sinistro s2 = sinistroValidado();
        assertThat(s1).isEqualTo(s2);
    }

    @Test
    @DisplayName("toString deve conter protocolo, tipo e status")
    void toStringShouldContainProtocoloTypeAndStatus() {
        String str = sinistroValidado().toString();
        assertThat(str).contains("2024-000001");
    }
}
