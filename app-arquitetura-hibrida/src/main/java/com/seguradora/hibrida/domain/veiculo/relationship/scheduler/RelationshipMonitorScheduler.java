package com.seguradora.hibrida.domain.veiculo.relationship.scheduler;

import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
import com.seguradora.hibrida.domain.veiculo.relationship.service.RelationshipAlertService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;

/**
 * Scheduler para monitorar relacionamentos Veículo-Apólice e disparar alertas automáticos.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class RelationshipMonitorScheduler {

    private final VeiculoApoliceRelacionamentoRepository relacionamentoRepository;
    private final RelationshipAlertService alertService;

    /**
     * Monitora relacionamentos que vencem nos próximos 30 dias.
     * Executa diariamente às 08:00.
     */
    @Scheduled(cron = "0 0 8 * * *")
    public void monitorarVencimentos() {
        log.info("Iniciando monitoramento de vencimentos de relacionamentos");

        try {
            LocalDate hoje = LocalDate.now();

            // Alertar relacionamentos vencendo em 30 dias
            LocalDate daquiA30Dias = hoje.plusDays(30);
            List<VeiculoApoliceRelacionamento> vencendoEm30 =
                relacionamentoRepository.findRelacionamentosVencendoAte(daquiA30Dias);

            for (VeiculoApoliceRelacionamento rel : vencendoEm30) {
                int diasRestantes = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    hoje, rel.getDataFim());

                // Alertar apenas nos marcos importantes: 30, 15, 7 dias
                if (diasRestantes == 30 || diasRestantes == 15 || diasRestantes == 7) {
                    alertService.alertarVencimentoProximo(
                        rel.getVeiculoId(),
                        rel.getVeiculoPlaca(),
                        rel.getSeguradoCpf(),
                        rel.getSeguradoNome(),
                        rel.getApoliceNumero(),
                        diasRestantes
                    );
                }
            }

            log.info("Monitoramento de vencimentos concluído: {} relacionamentos analisados",
                vencendoEm30.size());

        } catch (Exception e) {
            log.error("Erro ao monitorar vencimentos de relacionamentos", e);
        }
    }

    /**
     * Detecta e alerta sobre gaps de cobertura.
     * Executa diariamente às 09:00.
     */
    @Scheduled(cron = "0 0 9 * * *")
    public void detectarGapsCobertura() {
        log.info("Iniciando detecção de gaps de cobertura");

        try {
            List<VeiculoApoliceRelacionamento> comGap =
                relacionamentoRepository.findRelacionamentosComGap();

            for (VeiculoApoliceRelacionamento rel : comGap) {
                int diasSemCobertura = (int) java.time.temporal.ChronoUnit.DAYS.between(
                    rel.getDataFim(), LocalDate.now());

                alertService.alertarGapCobertura(
                    rel.getVeiculoId(),
                    rel.getVeiculoPlaca(),
                    rel.getSeguradoCpf(),
                    diasSemCobertura
                );
            }

            log.info("Detecção de gaps concluída: {} gaps encontrados", comGap.size());

        } catch (Exception e) {
            log.error("Erro ao detectar gaps de cobertura", e);
        }
    }

    /**
     * Monitora veículos sem cobertura ativa.
     * Executa a cada 6 horas.
     */
    @Scheduled(fixedRate = 21600000) // 6 horas em millisegundos
    public void monitorarVeiculosSemCobertura() {
        log.info("Iniciando monitoramento de veículos sem cobertura");

        try {
            List<String> veiculosSemCobertura =
                relacionamentoRepository.findVeiculosSemCobertura();

            log.info("Encontrados {} veículos sem cobertura ativa", veiculosSemCobertura.size());

            if (!veiculosSemCobertura.isEmpty()) {
                log.warn("ATENÇÃO: {} veículos estão sem cobertura ativa!",
                    veiculosSemCobertura.size());

                // TODO: Gerar relatório consolidado para gestores
                // TODO: Escalar casos críticos (>30 dias sem cobertura)
            }

        } catch (Exception e) {
            log.error("Erro ao monitorar veículos sem cobertura", e);
        }
    }
}
