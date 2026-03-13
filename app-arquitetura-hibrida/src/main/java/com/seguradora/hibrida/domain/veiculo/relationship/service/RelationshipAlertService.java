package com.seguradora.hibrida.domain.veiculo.relationship.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Serviço responsável por disparar alertas relacionados a coberturas de veículos.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class RelationshipAlertService {

    // TODO: Injetar NotificationService quando disponível
    // private final NotificationService notificationService;

    /**
     * Alerta quando um veículo fica sem cobertura ativa.
     */
    public void alertarVeiculoSemCobertura(
            String veiculoId,
            String placa,
            String seguradoCpf,
            String seguradoNome) {

        log.warn("ALERTA: Veículo sem cobertura - veiculoId={}, placa={}, segurado={}",
            veiculoId, placa, seguradoNome);

        // TODO: Enviar notificação ao segurado via email/SMS
        String mensagem = String.format(
            "ATENÇÃO: Seu veículo (placa %s) ficou sem cobertura ativa. " +
            "Por favor, entre em contato para regularizar sua apólice.",
            placa
        );

        log.info("Notificação gerada: {}", mensagem);

        // TODO: Criar registro de alerta no banco
        // alertRepository.save(new Alert(veiculoId, TipoAlerta.VEICULO_SEM_COBERTURA, mensagem));
    }

    /**
     * Alerta quando um veículo fica sem cobertura por cancelamento de apólice.
     */
    public void alertarVeiculoSemCoberturaPorCancelamento(
            String veiculoId,
            String placa,
            String seguradoCpf,
            String seguradoNome,
            String numeroApolice,
            String motivoCancelamento) {

        log.warn("ALERTA: Veículo sem cobertura por cancelamento - veiculoId={}, placa={}, apólice={}, motivo={}",
            veiculoId, placa, numeroApolice, motivoCancelamento);

        String mensagem = String.format(
            "ATENÇÃO: Seu veículo (placa %s) ficou sem cobertura devido ao cancelamento da apólice %s. " +
            "Motivo: %s. Entre em contato urgentemente para regularizar.",
            placa, numeroApolice, motivoCancelamento
        );

        log.info("Notificação urgente gerada: {}", mensagem);

        // TODO: Enviar notificação prioritária
        // notificationService.sendPriority(seguradoCpf, mensagem);
    }

    /**
     * Alerta quando um veículo fica sem cobertura por vencimento de apólice.
     */
    public void alertarVeiculoSemCoberturaPorVencimento(
            String veiculoId,
            String placa,
            String seguradoCpf,
            String seguradoNome,
            String numeroApolice) {

        log.warn("ALERTA: Veículo sem cobertura por vencimento - veiculoId={}, placa={}, apólice={}",
            veiculoId, placa, numeroApolice);

        String mensagem = String.format(
            "ATENÇÃO: Seu veículo (placa %s) ficou sem cobertura pois a apólice %s venceu. " +
            "Renove sua apólice para manter a proteção do seu veículo.",
            placa, numeroApolice
        );

        log.info("Notificação de vencimento gerada: {}", mensagem);

        // TODO: Enviar notificação de renovação
        // notificationService.sendRenewalReminder(seguradoCpf, numeroApolice, mensagem);
    }

    /**
     * Notifica quando a cobertura de um veículo é restaurada.
     */
    public void notificarCoberturaRestaurada(String veiculoId, String apoliceId) {
        log.info("Cobertura restaurada: veiculoId={}, apoliceId={}", veiculoId, apoliceId);

        // TODO: Enviar notificação positiva ao segurado
        // notificationService.sendSuccess(...);
    }

    /**
     * Alerta sobre relacionamentos que vencem em breve.
     */
    public void alertarVencimentoProximo(
            String veiculoId,
            String placa,
            String seguradoCpf,
            String seguradoNome,
            String numeroApolice,
            int diasRestantes) {

        log.warn("ALERTA: Cobertura vence em {} dias - veiculoId={}, placa={}, apólice={}",
            diasRestantes, veiculoId, placa, numeroApolice);

        String mensagem = String.format(
            "AVISO: A cobertura do seu veículo (placa %s) vence em %d dias. " +
            "Renove sua apólice %s para manter a proteção.",
            placa, diasRestantes, numeroApolice
        );

        log.info("Alerta de vencimento próximo: {}", mensagem);

        // TODO: Enviar notificação preventiva
        // notificationService.sendReminder(seguradoCpf, mensagem, diasRestantes);
    }

    /**
     * Alerta sobre gaps de cobertura detectados.
     */
    public void alertarGapCobertura(
            String veiculoId,
            String placa,
            String seguradoCpf,
            int diasSemCobertura) {

        log.warn("ALERTA: Gap de cobertura detectado - veiculoId={}, placa={}, dias={}",
            veiculoId, placa, diasSemCobertura);

        String mensagem = String.format(
            "ATENÇÃO: Detectamos que seu veículo (placa %s) está há %d dias sem cobertura. " +
            "Regularize sua situação para evitar problemas em caso de sinistro.",
            placa, diasSemCobertura
        );

        log.info("Alerta de gap: {}", mensagem);

        // TODO: Escalar para corretor/gerente se gap > 7 dias
    }
}
