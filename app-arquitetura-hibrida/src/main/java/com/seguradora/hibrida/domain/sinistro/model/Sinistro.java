package com.seguradora.hibrida.domain.sinistro.model;

import lombok.Builder;
import lombok.Getter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Entidade principal que representa um Sinistro.
 *
 * <p>Agrega todas as informações relacionadas a um sinistro:
 * <ul>
 *   <li>Dados de identificação (protocolo, IDs relacionados)</li>
 *   <li>Estado atual (status, tipo)</li>
 *   <li>Ocorrência (quando, onde, como)</li>
 *   <li>Avaliação de danos</li>
 *   <li>Processamento Detran</li>
 *   <li>Valor de indenização</li>
 *   <li>Histórico e auditoria</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Getter
@Builder
public class Sinistro {

    // Identificação
    private final String id;
    private final ProtocoloSinistro protocolo;
    private final String seguradoId;
    private final String veiculoId;
    private final String apoliceId;

    // Estado
    private final TipoSinistro tipoSinistro;
    private final StatusSinistro status;

    // Ocorrência
    private final OcorrenciaSinistro ocorrencia;

    // Avaliação
    private final AvaliacaoDanos avaliacaoDanos;

    // Processamento Detran
    private final ProcessamentoDetran processamentoDetran;

    // Indenização
    private final ValorIndenizacao valorIndenizacao;

    // Prazo
    private final PrazoProcessamento prazoProcessamento;

    // Análise
    private final String analistaResponsavel;
    private final Instant dataInicioAnalise;
    private final Instant dataFimAnalise;

    // Aprovação/Reprovação
    private final String justificativa;
    private final String motivoReprovacao;
    private final String fundamentoLegal;

    // Documentos
    @Builder.Default
    private final List<String> documentosAnexados = new ArrayList<>();

    @Builder.Default
    private final Map<String, Object> dadosComplementares = new HashMap<>();

    // Auditoria
    private final String operadorCriacao;
    private final Instant dataCriacao;
    private final Instant dataUltimaAtualizacao;

    /**
     * Verifica se o sinistro está em um estado que permite análise.
     */
    public boolean podeIniciarAnalise() {
        return status == StatusSinistro.VALIDADO && analistaResponsavel == null;
    }

    /**
     * Verifica se todos os dados necessários foram coletados.
     */
    public boolean possuiDadosCompletos() {
        boolean ocorrenciaValida = ocorrencia != null && ocorrencia.isValida();
        boolean avaliacaoCompleta = avaliacaoDanos != null && avaliacaoDanos.isCompleta();

        // Se requer consulta Detran, deve estar concluída
        boolean detranOk = !tipoSinistro.requerConsultaDetran() ||
                          (processamentoDetran != null && processamentoDetran.isSucesso());

        // Se requer BO, deve possuir
        boolean boOk = !tipoSinistro.requerBoletimOcorrencia() ||
                      (ocorrencia != null && ocorrencia.possuiBoletimOcorrencia());

        return ocorrenciaValida && avaliacaoCompleta && detranOk && boOk;
    }

    /**
     * Verifica se o sinistro pode ser aprovado.
     */
    public boolean podeAprovar() {
        return (status == StatusSinistro.DADOS_COLETADOS || status == StatusSinistro.EM_ANALISE) &&
               possuiDadosCompletos() &&
               avaliacaoDanos != null &&
               avaliacaoDanos.isCompleta();
    }

    /**
     * Verifica se o sinistro pode ser reprovado.
     */
    public boolean podeReprovar() {
        return status == StatusSinistro.DADOS_COLETADOS ||
               status == StatusSinistro.EM_ANALISE;
    }

    /**
     * Verifica se está dentro do prazo de processamento.
     */
    public boolean isDentroDoPrazo() {
        if (prazoProcessamento == null) {
            return true;
        }

        return !prazoProcessamento.isVencido();
    }

    /**
     * Verifica se a consulta ao Detran foi realizada com sucesso.
     */
    public boolean isConsultaDetranSucesso() {
        return processamentoDetran != null && processamentoDetran.isSucesso();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sinistro sinistro = (Sinistro) o;
        return Objects.equals(id, sinistro.id) &&
               Objects.equals(protocolo, sinistro.protocolo);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, protocolo);
    }

    @Override
    public String toString() {
        return String.format("Sinistro{protocolo=%s, tipo=%s, status=%s}",
            protocolo,
            tipoSinistro,
            status
        );
    }
}
