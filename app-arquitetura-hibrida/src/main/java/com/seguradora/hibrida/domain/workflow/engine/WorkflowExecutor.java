package com.seguradora.hibrida.domain.workflow.engine;

import com.seguradora.hibrida.domain.workflow.execution.WorkflowContext;
import com.seguradora.hibrida.domain.workflow.execution.WorkflowResult;
import com.seguradora.hibrida.domain.workflow.model.EtapaWorkflow;
import com.seguradora.hibrida.domain.workflow.model.TipoEtapa;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Componente responsável pela execução efetiva das etapas de workflow.
 * Processa ações automáticas, valida condições e integra com sistemas externos.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class WorkflowExecutor {

    /**
     * Executa uma etapa de workflow.
     *
     * @param etapa etapa a ser executada
     * @param contexto contexto de execução
     * @return resultado da execução
     */
    public WorkflowResult executeEtapa(EtapaWorkflow etapa, WorkflowContext contexto) {
        log.info("Executando etapa: {} ({})", etapa.getNome(), etapa.getTipo());

        try {
            // Valida condições da etapa
            if (!validateCondicoes(etapa.getCondicoes(), contexto)) {
                return WorkflowResult.failure("Condições da etapa não satisfeitas");
            }

            // Executa baseado no tipo
            switch (etapa.getTipo()) {
                case AUTOMATICA:
                    return executeEtapaAutomatica(etapa, contexto);

                case MANUAL:
                    return executeEtapaManual(etapa, contexto);

                case APROVACAO:
                    return executeEtapaAprovacao(etapa, contexto);

                case INTEGRACAO:
                    return executeEtapaIntegracao(etapa, contexto);

                default:
                    return WorkflowResult.failure("Tipo de etapa não suportado: " + etapa.getTipo());
            }

        } catch (Exception e) {
            log.error("Erro ao executar etapa {}: {}", etapa.getNome(), e.getMessage(), e);
            return WorkflowResult.failure("Erro na execução: " + e.getMessage());
        }
    }

    /**
     * Executa uma etapa automática.
     */
    private WorkflowResult executeEtapaAutomatica(EtapaWorkflow etapa, WorkflowContext contexto) {
        log.debug("Executando etapa automática: {}", etapa.getNome());

        List<String> acoes = etapa.getAcoesAutomaticas();

        if (acoes.isEmpty()) {
            return WorkflowResult.success("Etapa automática sem ações específicas");
        }

        // Executa cada ação
        for (String acao : acoes) {
            WorkflowResult resultado = executeAcaoAutomatica(acao, contexto);
            if (resultado.isFalha()) {
                return resultado;
            }

            // Mescla dados do resultado no contexto
            if (resultado.getDados() != null) {
                resultado.getDados().forEach(contexto::set);
            }
        }

        return WorkflowResult.success("Etapa automática concluída com sucesso");
    }

    /**
     * Executa uma etapa manual.
     */
    private WorkflowResult executeEtapaManual(EtapaWorkflow etapa, WorkflowContext contexto) {
        log.debug("Etapa manual aguarda intervenção: {}", etapa.getNome());

        // Etapas manuais não são executadas automaticamente
        // Retorna sucesso indicando que está aguardando ação manual
        return WorkflowResult.success("Aguardando intervenção manual")
                .comDado("aguardandoAcaoManual", true)
                .comDado("acoesRequeridas", etapa.getAcoesManuais());
    }

    /**
     * Executa uma etapa de aprovação.
     */
    private WorkflowResult executeEtapaAprovacao(EtapaWorkflow etapa, WorkflowContext contexto) {
        log.debug("Etapa de aprovação: {} - Nível: {}", etapa.getNome(), etapa.getNivelAprovacao());

        // Verifica se já existe aprovação no contexto
        Object aprovacaoStatus = contexto.get("aprovacao_status");

        if (aprovacaoStatus != null) {
            if ("APROVADO".equals(aprovacaoStatus)) {
                return WorkflowResult.success("Aprovação concedida");
            } else if ("REJEITADO".equals(aprovacaoStatus)) {
                return WorkflowResult.failureSemRetry("Aprovação rejeitada");
            }
        }

        // Aprovação ainda não foi processada
        return WorkflowResult.success("Aguardando aprovação")
                .comDado("aguardandoAprovacao", true)
                .comDado("nivelAprovacao", etapa.getNivelAprovacao().name())
                .comDado("limiteAlcada", etapa.getNivelAprovacao().getLimite());
    }

    /**
     * Executa uma etapa de integração.
     */
    private WorkflowResult executeEtapaIntegracao(EtapaWorkflow etapa, WorkflowContext contexto) {
        log.debug("Executando etapa de integração: {}", etapa.getNome());

        // Identifica o tipo de integração baseado nas ações
        List<String> acoes = etapa.getAcoesAutomaticas();

        for (String acao : acoes) {
            if (acao.startsWith("INTEGRAR:")) {
                String tipoIntegracao = acao.substring(9);
                WorkflowResult resultado = executarIntegracao(tipoIntegracao, contexto);

                if (resultado.isFalha()) {
                    return resultado;
                }

                // Mescla dados da integração no contexto
                if (resultado.getDados() != null) {
                    resultado.getDados().forEach(contexto::set);
                }
            }
        }

        return WorkflowResult.success("Integrações concluídas com sucesso");
    }

    /**
     * Executa uma ação automática específica.
     *
     * @param acao descrição da ação
     * @param contexto contexto de execução
     * @return resultado da execução
     */
    public WorkflowResult executeAcaoAutomatica(String acao, WorkflowContext contexto) {
        log.debug("Executando ação: {}", acao);

        try {
            // Parse da ação
            String[] partes = acao.split(":");

            if (partes.length < 2) {
                return WorkflowResult.failure("Formato de ação inválido: " + acao);
            }

            String tipoAcao = partes[0].trim();
            String parametros = partes[1].trim();

            switch (tipoAcao) {
                case "CALCULAR":
                    return executarCalculo(parametros, contexto);

                case "VALIDAR":
                    return executarValidacao(parametros, contexto);

                case "NOTIFICAR":
                    return executarNotificacao(parametros, contexto);

                case "REGISTRAR":
                    return executarRegistro(parametros, contexto);

                case "SET":
                    return executarSetVariavel(parametros, contexto);

                default:
                    log.warn("Tipo de ação não reconhecido: {}", tipoAcao);
                    return WorkflowResult.success("Ação ignorada: " + tipoAcao);
            }

        } catch (Exception e) {
            log.error("Erro ao executar ação {}: {}", acao, e.getMessage(), e);
            return WorkflowResult.failure("Erro ao executar ação: " + e.getMessage());
        }
    }

    /**
     * Valida condições da etapa contra o contexto.
     *
     * @param condicoes mapa de condições
     * @param contexto contexto de execução
     * @return true se todas as condições são satisfeitas
     */
    public boolean validateCondicoes(Map<String, String> condicoes, WorkflowContext contexto) {
        if (condicoes == null || condicoes.isEmpty()) {
            return true;
        }

        for (Map.Entry<String, String> condicao : condicoes.entrySet()) {
            String expressao = condicao.getKey() + " " + condicao.getValue();

            if (!contexto.avaliarCondicao(expressao)) {
                log.debug("Condição não satisfeita: {}", expressao);
                return false;
            }
        }

        return true;
    }

    // ===== Métodos auxiliares de execução =====

    private WorkflowResult executarCalculo(String formula, WorkflowContext contexto) {
        log.debug("Executando cálculo: {}", formula);

        // Implementação simplificada de cálculos comuns
        if (formula.equals("valor_indenizacao")) {
            BigDecimal valor = contexto.getValorIndenizacao();
            if (valor != null) {
                return WorkflowResult.success("Valor calculado")
                        .comDado("valor_calculado", valor);
            }
        }

        return WorkflowResult.success("Cálculo executado");
    }

    private WorkflowResult executarValidacao(String campo, WorkflowContext contexto) {
        log.debug("Executando validação: {}", campo);

        Object valor = contexto.get(campo);

        if (valor == null) {
            return WorkflowResult.failure("Campo obrigatório não informado: " + campo);
        }

        return WorkflowResult.success("Validação bem-sucedida");
    }

    private WorkflowResult executarNotificacao(String destinatario, WorkflowContext contexto) {
        log.info("Enviando notificação para: {}", destinatario);

        // Mock de envio de notificação
        // Em produção, integraria com sistema de notificações real

        return WorkflowResult.success("Notificação enviada")
                .comDado("notificacao_enviada", true)
                .comDado("destinatario", destinatario);
    }

    private WorkflowResult executarRegistro(String evento, WorkflowContext contexto) {
        log.info("Registrando evento: {}", evento);

        // Mock de registro de evento
        // Em produção, integraria com sistema de auditoria

        return WorkflowResult.success("Evento registrado")
                .comDado("evento_registrado", evento);
    }

    private WorkflowResult executarSetVariavel(String expressao, WorkflowContext contexto) {
        String[] partes = expressao.split("=");

        if (partes.length != 2) {
            return WorkflowResult.failure("Formato inválido para SET: " + expressao);
        }

        String variavel = partes[0].trim();
        String valor = partes[1].trim();

        contexto.set(variavel, valor);

        return WorkflowResult.success("Variável definida: " + variavel);
    }

    private WorkflowResult executarIntegracao(String tipoIntegracao, WorkflowContext contexto) {
        log.info("Executando integração: {}", tipoIntegracao);

        // Mock de integrações externas
        // Em produção, cada tipo teria implementação específica

        switch (tipoIntegracao) {
            case "VALIDAR_DOCUMENTOS":
                return WorkflowResult.success("Documentos validados")
                        .comDado("documentos_validos", true);

            case "CONSULTAR_BUREAU":
                return WorkflowResult.success("Consulta ao bureau realizada")
                        .comDado("score_credito", 750)
                        .comDado("restricoes", false);

            case "VERIFICAR_FRANQUIA":
                return WorkflowResult.success("Franquia verificada")
                        .comDado("franquia_aplicavel", true);

            case "CALCULAR_DEPRECIACAO":
                return WorkflowResult.success("Depreciação calculada")
                        .comDado("percentual_depreciacao", 10);

            default:
                log.warn("Tipo de integração não implementado: {}", tipoIntegracao);
                return WorkflowResult.success("Integração mock: " + tipoIntegracao);
        }
    }
}
