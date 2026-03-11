package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.*;

/**
 * Define as políticas de aprovação e hierarquia de aprovadores.
 * Gerencia a delegação e validação de permissões por nível.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AprovadorPolicy {

    // Mock de banco de aprovadores - em produção viria de banco de dados
    private static final Map<NivelAprovacao, List<String>> APROVADORES_POR_NIVEL = new HashMap<>();

    static {
        APROVADORES_POR_NIVEL.put(NivelAprovacao.NIVEL_1_ANALISTA,
                Arrays.asList("ANALISTA_001", "ANALISTA_002", "ANALISTA_003"));

        APROVADORES_POR_NIVEL.put(NivelAprovacao.NIVEL_2_SUPERVISOR,
                Arrays.asList("SUPERVISOR_001", "SUPERVISOR_002"));

        APROVADORES_POR_NIVEL.put(NivelAprovacao.NIVEL_3_GERENTE,
                Arrays.asList("GERENTE_001"));

        APROVADORES_POR_NIVEL.put(NivelAprovacao.NIVEL_4_DIRETOR,
                Arrays.asList("DIRETOR_001"));
    }

    /**
     * Obtém a lista de aprovadores para um determinado nível.
     *
     * @param nivel nível de aprovação
     * @param sinistroId ID do sinistro (para regras específicas)
     * @return lista de IDs de aprovadores
     */
    public List<String> obterAprovadores(NivelAprovacao nivel, String sinistroId) {
        log.debug("Obtendo aprovadores para nível {} - Sinistro: {}", nivel, sinistroId);

        List<String> aprovadores = APROVADORES_POR_NIVEL.get(nivel);

        if (aprovadores == null || aprovadores.isEmpty()) {
            log.warn("Nenhum aprovador encontrado para nível {}", nivel);
            return Collections.emptyList();
        }

        // Em produção, aqui poderiam ser aplicadas regras de:
        // - Distribuição de carga
        // - Especialização por tipo de sinistro
        // - Disponibilidade do aprovador
        // - Região geográfica

        return new ArrayList<>(aprovadores);
    }

    /**
     * Valida se um aprovador tem permissão para aprovar determinado valor.
     *
     * @param aprovadorId ID do aprovador
     * @param nivel nível de aprovação
     * @param valor valor a ser aprovado
     * @throws IllegalArgumentException se não tiver permissão
     */
    public void validarPermissao(String aprovadorId, NivelAprovacao nivel, BigDecimal valor) {
        log.debug("Validando permissão do aprovador {} para nível {} - Valor: {}",
                aprovadorId, nivel, valor);

        // Verifica se o aprovador pertence ao nível
        List<String> aprovadoresNivel = APROVADORES_POR_NIVEL.get(nivel);

        if (aprovadoresNivel == null || !aprovadoresNivel.contains(aprovadorId)) {
            throw new IllegalArgumentException(
                    "Aprovador " + aprovadorId + " não tem permissão para nível " + nivel);
        }

        // Verifica se o nível pode aprovar o valor
        if (!nivel.podeAprovar(valor)) {
            throw new IllegalArgumentException(
                    "Nível " + nivel + " não pode aprovar valor de " + valor);
        }

        log.debug("Permissão validada com sucesso");
    }

    /**
     * Obtém o próximo nível de aprovação.
     *
     * @param nivelAtual nível atual
     * @return próximo nível ou null se já é o máximo
     */
    public NivelAprovacao obterProximoNivel(NivelAprovacao nivelAtual) {
        return nivelAtual.getProximo();
    }

    /**
     * Determina o nível de aprovação necessário baseado no valor.
     *
     * @param valor valor da indenização
     * @return nível de aprovação adequado
     */
    public NivelAprovacao determinarNivelPorValor(BigDecimal valor) {
        return NivelAprovacao.determinarNivel(valor);
    }

    /**
     * Verifica se um aprovador existe e está ativo.
     *
     * @param aprovadorId ID do aprovador
     * @return true se existe e está ativo
     */
    public boolean isAprovadorValido(String aprovadorId) {
        return APROVADORES_POR_NIVEL.values().stream()
                .anyMatch(lista -> lista.contains(aprovadorId));
    }

    /**
     * Obtém o nível de um aprovador.
     *
     * @param aprovadorId ID do aprovador
     * @return nível do aprovador ou null se não encontrado
     */
    public NivelAprovacao obterNivelAprovador(String aprovadorId) {
        for (Map.Entry<NivelAprovacao, List<String>> entry : APROVADORES_POR_NIVEL.entrySet()) {
            if (entry.getValue().contains(aprovadorId)) {
                return entry.getKey();
            }
        }
        return null;
    }

    /**
     * Lista todos os aprovadores disponíveis.
     *
     * @return lista de todos os aprovadores
     */
    public List<String> listarTodosAprovadores() {
        List<String> todos = new ArrayList<>();
        APROVADORES_POR_NIVEL.values().forEach(todos::addAll);
        return todos;
    }

    /**
     * Verifica se há aprovadores disponíveis para um nível.
     *
     * @param nivel nível de aprovação
     * @return true se há aprovadores disponíveis
     */
    public boolean hasAprovadoresDisponiveis(NivelAprovacao nivel) {
        List<String> aprovadores = APROVADORES_POR_NIVEL.get(nivel);
        return aprovadores != null && !aprovadores.isEmpty();
    }
}
