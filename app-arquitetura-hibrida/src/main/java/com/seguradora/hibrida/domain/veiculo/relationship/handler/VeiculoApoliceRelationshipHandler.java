package com.seguradora.hibrida.domain.veiculo.relationship.handler;

import com.seguradora.hibrida.domain.apolice.event.ApoliceCanceladaEvent;
import com.seguradora.hibrida.domain.apolice.event.ApoliceVencidaEvent;
import com.seguradora.hibrida.domain.veiculo.event.VeiculoAssociadoEvent;
import com.seguradora.hibrida.domain.veiculo.event.VeiculoDesassociadoEvent;
import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.TipoRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Handler dedicado para processar eventos de relacionamento Veículo-Apólice.
 * Responsável por:
 * - Criar/atualizar relacionamentos
 * - Detectar veículos sem cobertura
 * - Disparar alertas automáticos
 * - Manter auditoria completa
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class VeiculoApoliceRelationshipHandler {

    private final VeiculoApoliceRelacionamentoRepository relacionamentoRepository;
    private final RelationshipAlertService alertService;

    /**
     * Processa evento de associação de veículo a apólice.
     */
    @EventListener
    @Async
    @Transactional
    public void handle(VeiculoAssociadoEvent event) {
        log.info("Processando associação: veículo={}, apólice={}",
            event.getAggregateId(), event.getApoliceId());

        try {
            // Verificar se já existe relacionamento ativo
            boolean jaExiste = relacionamentoRepository.existsRelacionamentoAtivo(
                event.getAggregateId(),
                event.getApoliceId()
            );

            if (jaExiste) {
                log.warn("Relacionamento ativo já existe: veículo={}, apólice={}",
                    event.getAggregateId(), event.getApoliceId());
                return;
            }

            // Criar novo relacionamento
            VeiculoApoliceRelacionamento relacionamento = new VeiculoApoliceRelacionamento();
            relacionamento.setVeiculoId(event.getAggregateId());
            relacionamento.setApoliceId(event.getApoliceId());
            relacionamento.setDataInicio(event.getDataInicio());
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setTipoRelacionamento(TipoRelacionamento.PRINCIPAL);
            relacionamento.setOperadorAssociacaoId(event.getOperadorId());

            // Buscar dados desnormalizados do veículo e apólice
            // (em produção, buscar de VeiculoQueryModel e ApoliceQueryModel)
            relacionamento.setVeiculoPlaca("Pendente"); // TODO: buscar dados reais
            relacionamento.setApoliceNumero("Pendente"); // TODO: buscar dados reais

            relacionamentoRepository.save(relacionamento);

            log.info("Relacionamento criado com sucesso: id={}", relacionamento.getId());

            // Verificar se havia gap de cobertura e alertar que foi resolvido
            alertService.notificarCoberturaRestaurada(
                event.getAggregateId(),
                event.getApoliceId()
            );

        } catch (Exception e) {
            log.error("Erro ao processar associação de veículo: veículo={}, apólice={}",
                event.getAggregateId(), event.getApoliceId(), e);
            throw new RuntimeException("Falha ao processar associação de veículo", e);
        }
    }

    /**
     * Processa evento de desassociação de veículo de apólice.
     */
    @EventListener
    @Async
    @Transactional
    public void handle(VeiculoDesassociadoEvent event) {
        log.info("Processando desassociação: veículo={}, apólice={}, motivo={}",
            event.getAggregateId(), event.getApoliceId(), event.getMotivo());

        try {
            // Buscar relacionamento ativo
            var relacionamentoOpt = relacionamentoRepository.findRelacionamentoAtivo(
                event.getAggregateId(),
                event.getApoliceId()
            );

            if (relacionamentoOpt.isEmpty()) {
                log.warn("Relacionamento ativo não encontrado: veículo={}, apólice={}",
                    event.getAggregateId(), event.getApoliceId());
                return;
            }

            VeiculoApoliceRelacionamento relacionamento = relacionamentoOpt.get();
            relacionamento.setDataFim(event.getDataFim());
            relacionamento.setStatus(StatusRelacionamento.ENCERRADO);
            relacionamento.setMotivoDesassociacao(event.getMotivo());
            relacionamento.setOperadorDesassociacaoId(event.getOperadorId());

            relacionamentoRepository.save(relacionamento);

            log.info("Relacionamento encerrado com sucesso: id={}", relacionamento.getId());

            // Verificar se o veículo ficou sem cobertura
            long coberturaAtivas = relacionamentoRepository.countRelacionamentosAtivos(
                event.getAggregateId()
            );

            if (coberturaAtivas == 0) {
                log.warn("Veículo ficou sem cobertura: veículoId={}", event.getAggregateId());
                alertService.alertarVeiculoSemCobertura(
                    event.getAggregateId(),
                    relacionamento.getVeiculoPlaca(),
                    relacionamento.getSeguradoCpf(),
                    relacionamento.getSeguradoNome()
                );
            }

        } catch (Exception e) {
            log.error("Erro ao processar desassociação de veículo: veículo={}, apólice={}",
                event.getAggregateId(), event.getApoliceId(), e);
            throw new RuntimeException("Falha ao processar desassociação de veículo", e);
        }
    }

    /**
     * Processa evento de cancelamento de apólice.
     * Desassocia automaticamente todos os veículos da apólice cancelada.
     */
    @EventListener
    @Async
    @Transactional
    public void handle(ApoliceCanceladaEvent event) {
        log.info("Processando cancelamento de apólice: apoliceId={}, motivo={}",
            event.getAggregateId(), event.getMotivo());

        try {
            // Buscar todos os relacionamentos ativos da apólice
            List<VeiculoApoliceRelacionamento> relacionamentos =
                relacionamentoRepository.findByApoliceIdAndStatus(
                    event.getAggregateId(),
                    StatusRelacionamento.ATIVO
                );

            if (relacionamentos.isEmpty()) {
                log.info("Nenhum veículo associado à apólice cancelada: apoliceId={}",
                    event.getAggregateId());
                return;
            }

            // Encerrar todos os relacionamentos
            LocalDate dataEfeito = LocalDate.parse(event.getDataEfeito());

            for (VeiculoApoliceRelacionamento rel : relacionamentos) {
                rel.setDataFim(dataEfeito);
                rel.setStatus(StatusRelacionamento.CANCELADO);
                rel.setMotivoDesassociacao("Apólice cancelada: " + event.getMotivo());
                rel.setOperadorDesassociacaoId(event.getOperadorId());
                relacionamentoRepository.save(rel);

                log.info("Relacionamento cancelado: veículoId={}, relacionamentoId={}",
                    rel.getVeiculoId(), rel.getId());

                // Alertar que o veículo ficou sem cobertura
                alertService.alertarVeiculoSemCoberturaPorCancelamento(
                    rel.getVeiculoId(),
                    rel.getVeiculoPlaca(),
                    rel.getSeguradoCpf(),
                    rel.getSeguradoNome(),
                    event.getNumeroApolice(),
                    event.getMotivo()
                );
            }

            log.info("Processados {} relacionamentos da apólice cancelada", relacionamentos.size());

        } catch (Exception e) {
            log.error("Erro ao processar cancelamento de apólice: apoliceId={}",
                event.getAggregateId(), e);
            throw new RuntimeException("Falha ao processar cancelamento de apólice", e);
        }
    }

    /**
     * Processa evento de apólice vencida.
     * Similar ao cancelamento, mas com status diferente.
     */
    @EventListener
    @Async
    @Transactional
    public void handle(ApoliceVencidaEvent event) {
        log.info("Processando vencimento de apólice: apoliceId={}", event.getAggregateId());

        try {
            List<VeiculoApoliceRelacionamento> relacionamentos =
                relacionamentoRepository.findByApoliceIdAndStatus(
                    event.getAggregateId(),
                    StatusRelacionamento.ATIVO
                );

            for (VeiculoApoliceRelacionamento rel : relacionamentos) {
                rel.setDataFim(LocalDate.now());
                rel.setStatus(StatusRelacionamento.ENCERRADO);
                rel.setMotivoDesassociacao("Apólice vencida");
                relacionamentoRepository.save(rel);

                // Alertar sobre vencimento
                alertService.alertarVeiculoSemCoberturaPorVencimento(
                    rel.getVeiculoId(),
                    rel.getVeiculoPlaca(),
                    rel.getSeguradoCpf(),
                    rel.getSeguradoNome(),
                    event.getNumeroApolice()
                );
            }

            log.info("Processados {} relacionamentos da apólice vencida", relacionamentos.size());

        } catch (Exception e) {
            log.error("Erro ao processar vencimento de apólice: apoliceId={}",
                event.getAggregateId(), e);
        }
    }
}
