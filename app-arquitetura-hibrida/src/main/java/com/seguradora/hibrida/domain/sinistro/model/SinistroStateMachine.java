package com.seguradora.hibrida.domain.sinistro.model;

import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Máquina de estados para gerenciar transições de status do sinistro.
 *
 * <p>Implementa o padrão State Machine para garantir que apenas
 * transições válidas sejam executadas, mantendo a integridade do agregado.
 *
 * <p>Funcionalidades:
 * <ul>
 *   <li>Validação de transições permitidas</li>
 *   <li>Mapeamento completo de estados e transições</li>
 *   <li>Ações automáticas em transições específicas</li>
 *   <li>Logs de auditoria para rastreabilidade</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Slf4j
public class SinistroStateMachine {

    private static final Map<StatusSinistro, Set<StatusSinistro>> TRANSITIONS = new HashMap<>();

    static {
        // Estado NOVO
        TRANSITIONS.put(StatusSinistro.NOVO, Set.of(
            StatusSinistro.VALIDADO,
            StatusSinistro.ARQUIVADO
        ));

        // Estado VALIDADO
        TRANSITIONS.put(StatusSinistro.VALIDADO, Set.of(
            StatusSinistro.EM_ANALISE,
            StatusSinistro.ARQUIVADO
        ));

        // Estado EM_ANALISE
        TRANSITIONS.put(StatusSinistro.EM_ANALISE, Set.of(
            StatusSinistro.AGUARDANDO_DETRAN,
            StatusSinistro.DADOS_COLETADOS,
            StatusSinistro.APROVADO,
            StatusSinistro.REPROVADO,
            StatusSinistro.ARQUIVADO
        ));

        // Estado AGUARDANDO_DETRAN
        TRANSITIONS.put(StatusSinistro.AGUARDANDO_DETRAN, Set.of(
            StatusSinistro.DADOS_COLETADOS,
            StatusSinistro.EM_ANALISE, // Retry em caso de falha
            StatusSinistro.ARQUIVADO
        ));

        // Estado DADOS_COLETADOS
        TRANSITIONS.put(StatusSinistro.DADOS_COLETADOS, Set.of(
            StatusSinistro.APROVADO,
            StatusSinistro.REPROVADO,
            StatusSinistro.ARQUIVADO
        ));

        // Estado APROVADO
        TRANSITIONS.put(StatusSinistro.APROVADO, Set.of(
            StatusSinistro.PAGO,
            StatusSinistro.ARQUIVADO
        ));

        // Estado REPROVADO
        TRANSITIONS.put(StatusSinistro.REPROVADO, Set.of(
            StatusSinistro.ARQUIVADO
        ));

        // Estado PAGO
        TRANSITIONS.put(StatusSinistro.PAGO, Set.of(
            StatusSinistro.ARQUIVADO
        ));

        // Estado ARQUIVADO - final, sem transições
        TRANSITIONS.put(StatusSinistro.ARQUIVADO, Set.of());
    }

    /**
     * Verifica se a transição de status é válida.
     *
     * @param statusAtual Status atual do sinistro
     * @param novoStatus Novo status desejado
     * @return true se a transição é válida
     */
    public static boolean podeTransicionar(StatusSinistro statusAtual, StatusSinistro novoStatus) {
        if (statusAtual == null || novoStatus == null) {
            log.warn("Status atual ou novo status é nulo");
            return false;
        }

        Set<StatusSinistro> transicoesPermitidas = TRANSITIONS.get(statusAtual);

        if (transicoesPermitidas == null) {
            log.warn("Nenhuma transição configurada para status: {}", statusAtual);
            return false;
        }

        boolean permitido = transicoesPermitidas.contains(novoStatus);

        if (!permitido) {
            log.warn("Transição inválida: {} -> {}", statusAtual, novoStatus);
        }

        return permitido;
    }

    /**
     * Obtém todas as transições possíveis a partir do status atual.
     *
     * @param statusAtual Status atual do sinistro
     * @return Conjunto de status possíveis
     */
    public static Set<StatusSinistro> getTransicoesPossiveis(StatusSinistro statusAtual) {
        if (statusAtual == null) {
            return Set.of();
        }

        return TRANSITIONS.getOrDefault(statusAtual, Set.of());
    }

    /**
     * Verifica se o status é um estado final.
     *
     * @param status Status a verificar
     * @return true se é estado final
     */
    public static boolean isEstadoFinal(StatusSinistro status) {
        if (status == null) {
            return false;
        }

        Set<StatusSinistro> transicoes = TRANSITIONS.get(status);
        return transicoes == null || transicoes.isEmpty();
    }

    /**
     * Obtém ações automáticas que devem ser executadas na transição.
     *
     * @param statusAnterior Status anterior
     * @param novoStatus Novo status
     * @return Conjunto de ações a executar
     */
    public static Set<String> getAcoesAutomaticas(StatusSinistro statusAnterior, StatusSinistro novoStatus) {
        // EM_ANALISE -> AGUARDANDO_DETRAN: Dispara consulta ao Detran
        if (statusAnterior == StatusSinistro.EM_ANALISE &&
            novoStatus == StatusSinistro.AGUARDANDO_DETRAN) {
            return Set.of("DISPARAR_CONSULTA_DETRAN");
        }

        // DADOS_COLETADOS -> APROVADO: Calcula indenização
        if (statusAnterior == StatusSinistro.DADOS_COLETADOS &&
            novoStatus == StatusSinistro.APROVADO) {
            return Set.of("CALCULAR_INDENIZACAO", "VALIDAR_ALADA");
        }

        // APROVADO -> PAGO: Processa pagamento
        if (statusAnterior == StatusSinistro.APROVADO &&
            novoStatus == StatusSinistro.PAGO) {
            return Set.of("PROCESSAR_PAGAMENTO", "ENVIAR_NOTIFICACAO");
        }

        // Qualquer status -> ARQUIVADO: Finaliza processamento
        if (novoStatus == StatusSinistro.ARQUIVADO) {
            return Set.of("FINALIZAR_PROCESSAMENTO", "ARQUIVAR_DOCUMENTOS");
        }

        return Set.of();
    }

    /**
     * Valida se a transição requer dados adicionais.
     *
     * @param novoStatus Novo status desejado
     * @return true se requer dados adicionais
     */
    public static boolean requerDadosAdicionais(StatusSinistro novoStatus) {
        return novoStatus == StatusSinistro.APROVADO ||
               novoStatus == StatusSinistro.REPROVADO;
    }
}
