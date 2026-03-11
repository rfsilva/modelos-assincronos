package com.seguradora.hibrida.domain.sinistro.projection;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.seguradora.hibrida.domain.sinistro.event.*;
import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDashboardView;
import com.seguradora.hibrida.domain.sinistro.query.repository.SinistroDashboardRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.axonframework.eventhandling.EventHandler;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.WeekFields;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Projection handler para atualizar dashboards de sinistros.
 *
 * <p>Responsável por processar eventos de sinistros e atualizar
 * as métricas agregadas do dashboard de forma incremental.
 *
 * <p>Características:
 * <ul>
 *   <li>Atualização incremental de métricas</li>
 *   <li>Cache L1 com TTL de 2 minutos (Caffeine)</li>
 *   <li>Recálculo otimizado de agregações</li>
 *   <li>Suporte a múltiplos tipos de período (DIA, SEMANA, MES)</li>
 *   <li>Processamento assíncrono via Axon Framework</li>
 * </ul>
 *
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class SinistroDashboardProjection {

    private final SinistroDashboardRepository dashboardRepository;

    /**
     * Cache L1 para dashboards com TTL de 2 minutos.
     * Chave: periodo+tipoPeriodo, Valor: SinistroDashboardView
     */
    private final Cache<String, SinistroDashboardView> dashboardCache = Caffeine.newBuilder()
            .expireAfterWrite(2, TimeUnit.MINUTES)
            .maximumSize(500)
            .recordStats()
            .build();

    /**
     * Cache de controle de processamento de eventos duplicados.
     */
    private final Map<String, Long> processedEvents = new ConcurrentHashMap<>();

    // === EVENT HANDLERS ===

    /**
     * Processa evento de sinistro criado.
     */
    @EventHandler
    @Transactional
    public void on(SinistroCriadoEvent event) {
        log.debug("Processando SinistroCriadoEvent para aggregate: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            log.debug("Evento já processado, ignorando: {}", event.getAggregateId());
            return;
        }

        try {
            LocalDate dataOcorrencia = extractDataOcorrencia(event);

            // Atualizar dashboard diário
            atualizarDashboard(dataOcorrencia, "DIA", dashboard -> {
                dashboard.setTotalSinistros(dashboard.getTotalSinistros() + 1);
                dashboard.setSinistrosAbertos(dashboard.getSinistrosAbertos() + 1);
                dashboard.incrementarStatus("ABERTO");
                dashboard.incrementarTipo(event.getTipoSinistro());

                // Extrair região do evento (se disponível)
                String regiao = extractRegiao(event);
                if (regiao != null) {
                    dashboard.incrementarRegiao(regiao);
                }
            });

            // Atualizar dashboard semanal
            atualizarDashboard(dataOcorrencia, "SEMANA", dashboard -> {
                dashboard.setTotalSinistros(dashboard.getTotalSinistros() + 1);
                dashboard.setSinistrosAbertos(dashboard.getSinistrosAbertos() + 1);
            });

            // Atualizar dashboard mensal
            atualizarDashboard(dataOcorrencia, "MES", dashboard -> {
                dashboard.setTotalSinistros(dashboard.getTotalSinistros() + 1);
                dashboard.setSinistrosAbertos(dashboard.getSinistrosAbertos() + 1);
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());
            log.debug("Dashboard atualizado para sinistro criado");

        } catch (Exception e) {
            log.error("Erro ao processar SinistroCriadoEvent", e);
            throw e;
        }
    }

    /**
     * Processa evento de sinistro validado.
     */
    @EventHandler
    @Transactional
    public void on(SinistroValidadoEvent event) {
        log.debug("Processando SinistroValidadoEvent: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            return;
        }

        try {
            LocalDate dataEvento = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

            atualizarDashboard(dataEvento, "DIA", dashboard -> {
                // Atualizar contadores de status
                dashboard.incrementarStatus("VALIDADO");
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());

        } catch (Exception e) {
            log.error("Erro ao processar SinistroValidadoEvent", e);
            throw e;
        }
    }

    /**
     * Processa evento de sinistro em análise.
     */
    @EventHandler
    @Transactional
    public void on(SinistroEmAnaliseEvent event) {
        log.debug("Processando SinistroEmAnaliseEvent: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            return;
        }

        try {
            LocalDate dataEvento = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

            atualizarDashboard(dataEvento, "DIA", dashboard -> {
                // Decrementar abertos, incrementar em análise
                if (dashboard.getSinistrosAbertos() > 0) {
                    dashboard.setSinistrosAbertos(dashboard.getSinistrosAbertos() - 1);
                }
                dashboard.setSinistrosEmAnalise(dashboard.getSinistrosEmAnalise() + 1);
                dashboard.incrementarStatus("EM_ANALISE");
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());

        } catch (Exception e) {
            log.error("Erro ao processar SinistroEmAnaliseEvent", e);
            throw e;
        }
    }

    /**
     * Processa evento de sinistro aprovado.
     */
    @EventHandler
    @Transactional
    public void on(SinistroAprovadoEvent event) {
        log.debug("Processando SinistroAprovadoEvent: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            return;
        }

        try {
            LocalDate dataEvento = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
            // valorIndenizacao está como String no evento, converter para BigDecimal
            BigDecimal valorAprovado = BigDecimal.ZERO;
            try {
                valorAprovado = new BigDecimal(event.getValorIndenizacao());
            } catch (Exception ex) {
                log.warn("Erro ao converter valor de indenização: {}", event.getValorIndenizacao());
            }

            final BigDecimal valorFinal = valorAprovado;
            atualizarDashboard(dataEvento, "DIA", dashboard -> {
                // Decrementar em análise, incrementar aprovados
                if (dashboard.getSinistrosEmAnalise() > 0) {
                    dashboard.setSinistrosEmAnalise(dashboard.getSinistrosEmAnalise() - 1);
                }
                dashboard.setSinistrosAprovados(dashboard.getSinistrosAprovados() + 1);
                dashboard.incrementarStatus("APROVADO");

                // Atualizar valores financeiros
                dashboard.setValorTotal(dashboard.getValorTotal().add(valorFinal));

                // Atualizar valor máximo/mínimo
                if (valorFinal.compareTo(dashboard.getValorMaximo()) > 0) {
                    dashboard.setValorMaximo(valorFinal);
                }
                if (dashboard.getValorMinimo().equals(BigDecimal.ZERO) ||
                    valorFinal.compareTo(dashboard.getValorMinimo()) < 0) {
                    dashboard.setValorMinimo(valorFinal);
                }

                // Tempo de processamento - calcular baseado no timestamp do evento
                // Em produção, buscar data de abertura do sinistro e calcular diferença
            });

            // Atualizar dashboards semanais e mensais
            atualizarDashboard(dataEvento, "SEMANA", dashboard -> {
                dashboard.setSinistrosAprovados(dashboard.getSinistrosAprovados() + 1);
                dashboard.setValorTotal(dashboard.getValorTotal().add(valorFinal));
            });

            atualizarDashboard(dataEvento, "MES", dashboard -> {
                dashboard.setSinistrosAprovados(dashboard.getSinistrosAprovados() + 1);
                dashboard.setValorTotal(dashboard.getValorTotal().add(valorFinal));
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());

        } catch (Exception e) {
            log.error("Erro ao processar SinistroAprovadoEvent", e);
            throw e;
        }
    }

    /**
     * Processa evento de sinistro reprovado.
     */
    @EventHandler
    @Transactional
    public void on(SinistroReprovadoEvent event) {
        log.debug("Processando SinistroReprovadoEvent: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            return;
        }

        try {
            LocalDate dataEvento = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

            atualizarDashboard(dataEvento, "DIA", dashboard -> {
                // Decrementar em análise, incrementar reprovados
                if (dashboard.getSinistrosEmAnalise() > 0) {
                    dashboard.setSinistrosEmAnalise(dashboard.getSinistrosEmAnalise() - 1);
                }
                dashboard.setSinistrosReprovados(dashboard.getSinistrosReprovados() + 1);
                dashboard.incrementarStatus("REPROVADO");
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());

        } catch (Exception e) {
            log.error("Erro ao processar SinistroReprovadoEvent", e);
            throw e;
        }
    }

    /**
     * Processa evento de consulta Detran iniciada.
     */
    @EventHandler
    @Transactional
    public void on(ConsultaDetranIniciadaEvent event) {
        log.debug("Processando ConsultaDetranIniciadaEvent: {}", event.getAggregateId());
        // Não atualiza métricas de dashboard, apenas registra
        markEventAsProcessed(event.getAggregateId(), event.getVersion());
    }

    /**
     * Processa evento de consulta Detran concluída.
     */
    @EventHandler
    @Transactional
    public void on(ConsultaDetranConcluidaEvent event) {
        log.debug("Processando ConsultaDetranConcluidaEvent: {}", event.getAggregateId());
        // Não atualiza métricas de dashboard diretamente
        markEventAsProcessed(event.getAggregateId(), event.getVersion());
    }

    /**
     * Processa evento de consulta Detran falhada.
     */
    @EventHandler
    @Transactional
    public void on(ConsultaDetranFalhadaEvent event) {
        log.debug("Processando ConsultaDetranFalhadaEvent: {}", event.getAggregateId());
        // Pode gerar alerta se necessário
        markEventAsProcessed(event.getAggregateId(), event.getVersion());
    }

    /**
     * Processa evento de documento anexado.
     */
    @EventHandler
    @Transactional
    public void on(DocumentoAnexadoEvent event) {
        log.debug("Processando DocumentoAnexadoEvent: {}", event.getAggregateId());
        markEventAsProcessed(event.getAggregateId(), event.getVersion());
    }

    /**
     * Processa evento de documento validado.
     */
    @EventHandler
    @Transactional
    public void on(DocumentoValidadoEvent event) {
        log.debug("Processando DocumentoValidadoEvent: {}", event.getAggregateId());
        markEventAsProcessed(event.getAggregateId(), event.getVersion());
    }

    /**
     * Processa evento de documento rejeitado.
     */
    @EventHandler
    @Transactional
    public void on(DocumentoRejeitadoEvent event) {
        log.debug("Processando DocumentoRejeitadoEvent: {}", event.getAggregateId());

        if (isEventProcessed(event.getAggregateId(), event.getVersion())) {
            return;
        }

        try {
            LocalDate dataEvento = Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();

            atualizarDashboard(dataEvento, "DIA", dashboard -> {
                // Incrementar contador de documentos pendentes
                dashboard.setSinistrosDocPendente(dashboard.getSinistrosDocPendente() + 1);
            });

            markEventAsProcessed(event.getAggregateId(), event.getVersion());

        } catch (Exception e) {
            log.error("Erro ao processar DocumentoRejeitadoEvent", e);
            throw e;
        }
    }

    // === MÉTODOS AUXILIARES ===

    /**
     * Atualiza ou cria dashboard para um período específico.
     */
    private void atualizarDashboard(LocalDate dataReferencia, String tipoPeriodo,
                                   java.util.function.Consumer<SinistroDashboardView> updater) {
        String periodo = formatarPeriodo(dataReferencia, tipoPeriodo);
        String cacheKey = periodo + ":" + tipoPeriodo;

        // Buscar do cache ou banco
        SinistroDashboardView dashboard = dashboardCache.get(cacheKey, key -> {
            return dashboardRepository.findByPeriodoAndTipoPeriodo(periodo, tipoPeriodo)
                    .orElseGet(() -> criarNovoDashboard(periodo, tipoPeriodo, dataReferencia));
        });

        // Aplicar atualizações
        updater.accept(dashboard);

        // Persistir
        dashboard = dashboardRepository.save(dashboard);

        // Atualizar cache
        dashboardCache.put(cacheKey, dashboard);

        log.trace("Dashboard atualizado: {} - {}", periodo, tipoPeriodo);
    }

    /**
     * Cria novo dashboard para um período.
     */
    private SinistroDashboardView criarNovoDashboard(String periodo, String tipoPeriodo,
                                                    LocalDate dataReferencia) {
        return SinistroDashboardView.builder()
                .periodo(periodo)
                .tipoPeriodo(tipoPeriodo)
                .dataReferencia(dataReferencia)
                .totalSinistros(0)
                .sinistrosAbertos(0)
                .sinistrosEmAnalise(0)
                .sinistrosAprovados(0)
                .sinistrosReprovados(0)
                .sinistrosCancelados(0)
                .valorTotal(BigDecimal.ZERO)
                .valorMedio(BigDecimal.ZERO)
                .valorMaximo(BigDecimal.ZERO)
                .valorMinimo(BigDecimal.ZERO)
                .tempoMedioProcessamento(0L)
                .taxaAprovacao(BigDecimal.ZERO)
                .taxaReprovacao(BigDecimal.ZERO)
                .sinistrosDentroSla(0)
                .sinistrosForaSla(0)
                .sinistrosUrgentes(0)
                .sinistrosDocPendente(0)
                .build();
    }

    /**
     * Formata período de acordo com o tipo.
     */
    private String formatarPeriodo(LocalDate data, String tipoPeriodo) {
        return switch (tipoPeriodo) {
            case "DIA" -> data.format(DateTimeFormatter.ISO_LOCAL_DATE);
            case "SEMANA" -> {
                WeekFields weekFields = WeekFields.of(Locale.getDefault());
                int weekNumber = data.get(weekFields.weekOfWeekBasedYear());
                yield String.format("%d-W%02d", data.getYear(), weekNumber);
            }
            case "MES" -> data.format(DateTimeFormatter.ofPattern("yyyy-MM"));
            default -> data.format(DateTimeFormatter.ISO_LOCAL_DATE);
        };
    }

    /**
     * Atualiza tempo médio de processamento de forma incremental.
     */
    private void atualizarTempoMedioProcessamento(SinistroDashboardView dashboard, Long novoTempo) {
        int totalProcessados = dashboard.getSinistrosAprovados() + dashboard.getSinistrosReprovados();

        if (totalProcessados <= 1) {
            dashboard.setTempoMedioProcessamento(novoTempo);
        } else {
            // Média móvel incremental
            long tempoAtual = dashboard.getTempoMedioProcessamento() != null ?
                    dashboard.getTempoMedioProcessamento() : 0L;
            long novoTempoMedio = ((tempoAtual * (totalProcessados - 1)) + novoTempo) / totalProcessados;
            dashboard.setTempoMedioProcessamento(novoTempoMedio);
        }
    }

    /**
     * Extrai data de ocorrência do evento.
     */
    private LocalDate extractDataOcorrencia(SinistroCriadoEvent event) {
        // Assumindo que o evento possui timestamp de ocorrência
        // Em produção, usar o campo específico do evento
        return Instant.now().atZone(ZoneId.systemDefault()).toLocalDate();
    }

    /**
     * Extrai região do evento (estado da ocorrência).
     */
    private String extractRegiao(SinistroCriadoEvent event) {
        // Em produção, extrair do evento ou buscar de outra fonte
        // Por enquanto, retorna null
        return null;
    }

    /**
     * Verifica se o evento já foi processado (idempotência).
     */
    private boolean isEventProcessed(String aggregateId, Long sequenceNumber) {
        String key = aggregateId + ":" + sequenceNumber;
        return processedEvents.containsKey(key);
    }

    /**
     * Marca evento como processado.
     */
    private void markEventAsProcessed(String aggregateId, Long sequenceNumber) {
        String key = aggregateId + ":" + sequenceNumber;
        processedEvents.put(key, System.currentTimeMillis());

        // Limpar entradas antigas (acima de 1 hora)
        long umHoraAtras = System.currentTimeMillis() - (60 * 60 * 1000);
        processedEvents.entrySet().removeIf(entry -> entry.getValue() < umHoraAtras);
    }

    /**
     * Retorna estatísticas do cache (útil para monitoring).
     */
    public com.github.benmanes.caffeine.cache.stats.CacheStats getCacheStats() {
        return dashboardCache.stats();
    }

    /**
     * Limpa o cache (útil para testes ou reset).
     */
    public void clearCache() {
        dashboardCache.invalidateAll();
        log.info("Cache de dashboards limpo");
    }
}
