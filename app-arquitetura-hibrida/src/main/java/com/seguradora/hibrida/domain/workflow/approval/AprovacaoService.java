package com.seguradora.hibrida.domain.workflow.approval;

import com.seguradora.hibrida.domain.workflow.model.NivelAprovacao;
import com.seguradora.hibrida.domain.workflow.repository.AprovacaoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Serviço de gerenciamento de aprovações de sinistros.
 * Coordena o fluxo de aprovação com múltiplos níveis hierárquicos.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class AprovacaoService {

    private final AprovacaoRepository aprovacaoRepository;
    private final AprovadorPolicy aprovadorPolicy;
    private final AprovacaoNotificationService notificationService;

    /**
     * Solicita aprovação para um sinistro.
     *
     * @param workflowInstanceId ID da instância do workflow
     * @param sinistroId ID do sinistro
     * @param nivel nível de aprovação necessário
     * @param valor valor da indenização
     * @return aprovação criada
     */
    @Transactional
    public Aprovacao solicitarAprovacao(String workflowInstanceId, String sinistroId,
                                       NivelAprovacao nivel, BigDecimal valor) {
        log.info("Solicitando aprovação nível {} para sinistro {} - Valor: {}", nivel, sinistroId, valor);

        // Verifica se já existe aprovação pendente
        List<Aprovacao> aprovacoesPendentes = aprovacaoRepository
                .findBySinistroIdAndStatusIn(sinistroId,
                        List.of(Aprovacao.StatusAprovacao.PENDENTE, Aprovacao.StatusAprovacao.EM_ANALISE));

        if (!aprovacoesPendentes.isEmpty()) {
            log.warn("Já existe aprovação pendente para sinistro {}", sinistroId);
            return aprovacoesPendentes.get(0);
        }

        // Obtém aprovadores para o nível
        List<String> aprovadores = aprovadorPolicy.obterAprovadores(nivel, sinistroId);

        if (aprovadores.isEmpty()) {
            throw new IllegalStateException("Nenhum aprovador encontrado para nível: " + nivel);
        }

        // Calcula prazo limite (72 horas para decisão)
        LocalDateTime dataLimite = LocalDateTime.now().plusHours(72);

        // Cria a aprovação
        Aprovacao aprovacao = Aprovacao.builder()
                .workflowInstanceId(workflowInstanceId)
                .sinistroId(sinistroId)
                .nivel(nivel)
                .aprovadores(aprovadores)
                .valorSinistro(valor)
                .dataLimite(dataLimite)
                .status(Aprovacao.StatusAprovacao.PENDENTE)
                .build();

        aprovacao = aprovacaoRepository.save(aprovacao);

        // Notifica aprovadores
        notificationService.notificarAprovadores(aprovacao);

        log.info("Aprovação {} criada com sucesso - {} aprovadores notificados",
                aprovacao.getId(), aprovadores.size());

        return aprovacao;
    }

    /**
     * Aprova uma solicitação.
     *
     * @param aprovacaoId ID da aprovação
     * @param aprovadorId ID do aprovador
     * @param aprovadorNome nome do aprovador
     * @param justificativa justificativa da aprovação
     */
    @Transactional
    public void aprovar(String aprovacaoId, String aprovadorId, String aprovadorNome, String justificativa) {
        log.info("Aprovando solicitação {} por {}", aprovacaoId, aprovadorNome);

        Aprovacao aprovacao = aprovacaoRepository.findById(aprovacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Aprovação não encontrada: " + aprovacaoId));

        // Valida permissão
        aprovadorPolicy.validarPermissao(aprovadorId, aprovacao.getNivel(), aprovacao.getValorSinistro());

        // Aprova
        aprovacao.aprovar(aprovadorId, aprovadorNome, justificativa);
        aprovacaoRepository.save(aprovacao);

        // Notifica sobre a decisão
        notificationService.notificarDecisao(aprovacao, Aprovacao.DecisaoAprovacao.APROVADO);

        log.info("Aprovação {} concedida por {}", aprovacaoId, aprovadorNome);
    }

    /**
     * Rejeita uma solicitação.
     *
     * @param aprovacaoId ID da aprovação
     * @param aprovadorId ID do aprovador
     * @param aprovadorNome nome do aprovador
     * @param motivo motivo da rejeição
     */
    @Transactional
    public void rejeitar(String aprovacaoId, String aprovadorId, String aprovadorNome, String motivo) {
        log.info("Rejeitando solicitação {} por {}", aprovacaoId, aprovadorNome);

        Aprovacao aprovacao = aprovacaoRepository.findById(aprovacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Aprovação não encontrada: " + aprovacaoId));

        // Valida permissão
        aprovadorPolicy.validarPermissao(aprovadorId, aprovacao.getNivel(), aprovacao.getValorSinistro());

        // Rejeita
        aprovacao.rejeitar(aprovadorId, aprovadorNome, motivo);
        aprovacaoRepository.save(aprovacao);

        // Notifica sobre a decisão
        notificationService.notificarDecisao(aprovacao, Aprovacao.DecisaoAprovacao.REJEITADO);

        log.info("Aprovação {} rejeitada por {}: {}", aprovacaoId, aprovadorNome, motivo);
    }

    /**
     * Delega aprovação para outros aprovadores.
     *
     * @param aprovacaoId ID da aprovação
     * @param novosAprovadores lista de novos aprovadores
     */
    @Transactional
    public void delegar(String aprovacaoId, List<String> novosAprovadores) {
        log.info("Delegando aprovação {} para {} aprovadores", aprovacaoId, novosAprovadores.size());

        Aprovacao aprovacao = aprovacaoRepository.findById(aprovacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Aprovação não encontrada: " + aprovacaoId));

        aprovacao.delegar(novosAprovadores);
        aprovacaoRepository.save(aprovacao);

        // Notifica novos aprovadores
        notificationService.notificarAprovadores(aprovacao);

        log.info("Aprovação {} delegada com sucesso", aprovacaoId);
    }

    /**
     * Verifica e processa aprovações com timeout.
     */
    @Transactional
    public void verificarTimeout() {
        log.debug("Verificando aprovações com timeout");

        List<Aprovacao> aprovacoesPendentes = aprovacaoRepository
                .findByStatusIn(List.of(Aprovacao.StatusAprovacao.PENDENTE, Aprovacao.StatusAprovacao.EM_ANALISE));

        int timeoutsProcessados = 0;

        for (Aprovacao aprovacao : aprovacoesPendentes) {
            if (aprovacao.excedeuPrazo()) {
                log.warn("Aprovação {} excedeu o prazo limite", aprovacao.getId());

                aprovacao.expirar();
                aprovacaoRepository.save(aprovacao);

                // Escala automaticamente para próximo nível
                notificationService.escalarPorTimeout(aprovacao);

                timeoutsProcessados++;
            } else if (aprovacao.getDataLimite() != null) {
                // Verifica se está próximo do prazo (menos de 12 horas)
                long horasRestantes = java.time.Duration.between(
                        LocalDateTime.now(), aprovacao.getDataLimite()).toHours();

                if (horasRestantes <= 12 && horasRestantes > 0) {
                    notificationService.enviarLembretes(List.of(aprovacao));
                }
            }
        }

        if (timeoutsProcessados > 0) {
            log.info("Processados {} timeouts de aprovação", timeoutsProcessados);
        }
    }

    /**
     * Escala aprovação para próximo nível.
     *
     * @param aprovacaoId ID da aprovação
     * @param motivo motivo da escalação
     */
    @Transactional
    public void escalar(String aprovacaoId, String motivo) {
        log.info("Escalando aprovação {} - Motivo: {}", aprovacaoId, motivo);

        Aprovacao aprovacao = aprovacaoRepository.findById(aprovacaoId)
                .orElseThrow(() -> new IllegalArgumentException("Aprovação não encontrada: " + aprovacaoId));

        NivelAprovacao proximoNivel = aprovadorPolicy.obterProximoNivel(aprovacao.getNivel());

        if (proximoNivel == null) {
            throw new IllegalStateException("Já está no nível máximo de aprovação");
        }

        List<String> novosAprovadores = aprovadorPolicy.obterAprovadores(proximoNivel, aprovacao.getSinistroId());

        aprovacao.escalar(novosAprovadores);
        aprovacao.adicionarObservacao("Escalado para " + proximoNivel.name() + ": " + motivo);

        aprovacaoRepository.save(aprovacao);

        // Notifica novos aprovadores
        notificationService.notificarAprovadores(aprovacao);

        log.info("Aprovação {} escalada para nível {}", aprovacaoId, proximoNivel);
    }

    /**
     * Busca aprovação por sinistro.
     *
     * @param sinistroId ID do sinistro
     * @return lista de aprovações
     */
    @Transactional(readOnly = true)
    public List<Aprovacao> buscarPorSinistro(String sinistroId) {
        return aprovacaoRepository.findBySinistroId(sinistroId);
    }

    /**
     * Busca aprovações pendentes de um aprovador.
     *
     * @param aprovadorId ID do aprovador
     * @return lista de aprovações pendentes
     */
    @Transactional(readOnly = true)
    public List<Aprovacao> buscarPendentesPorAprovador(String aprovadorId) {
        return aprovacaoRepository.findByAprovadoresContainingAndStatusIn(
                aprovadorId,
                List.of(Aprovacao.StatusAprovacao.PENDENTE, Aprovacao.StatusAprovacao.EM_ANALISE)
        );
    }
}
