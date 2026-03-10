package com.seguradora.hibrida.domain.apolice.projection;

import com.seguradora.hibrida.domain.apolice.event.*;
import com.seguradora.hibrida.domain.apolice.model.StatusApolice;
import com.seguradora.hibrida.domain.apolice.model.TipoCobertura;
import com.seguradora.hibrida.domain.apolice.query.model.ApoliceQueryModel;
import com.seguradora.hibrida.domain.apolice.query.repository.ApoliceQueryRepository;
import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Projection Handler para eventos de apólices.
 * 
 * <p>Processa eventos de apólices para manter
 * as projeções de consulta atualizadas com dados desnormalizados.</p>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class ApoliceProjectionHandler extends AbstractProjectionHandler<DomainEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceProjectionHandler.class);
    
    private final ApoliceQueryRepository repository;
    
    public ApoliceProjectionHandler(ApoliceQueryRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public String getProjectionName() {
        return "ApoliceProjection";
    }
    
    @Override
    public Class<DomainEvent> getEventType() {
        return DomainEvent.class;
    }
    
    @Override
    public boolean supports(DomainEvent event) {
        return event instanceof ApoliceCriadaEvent ||
               event instanceof ApoliceAtualizadaEvent ||
               event instanceof ApoliceCanceladaEvent ||
               event instanceof ApoliceRenovadaEvent ||
               event instanceof CoberturaAdicionadaEvent;
    }
    
    @Override
    @Transactional
    protected void doHandle(DomainEvent event) {
        log.debug("Processando evento {} para projeção de apólice", event.getClass().getSimpleName());
        
        try {
            switch (event) {
                case ApoliceCriadaEvent e -> handleApoliceCriada(e);
                case ApoliceAtualizadaEvent e -> handleApoliceAtualizada(e);
                case ApoliceCanceladaEvent e -> handleApoliceCancelada(e);
                case ApoliceRenovadaEvent e -> handleApoliceRenovada(e);
                case CoberturaAdicionadaEvent e -> handleCoberturaAdicionada(e);
                default -> log.warn("Tipo de evento não suportado: {}", event.getClass().getSimpleName());
            }
            
        } catch (Exception ex) {
            log.error("Erro ao processar evento {} para apólice: {}", 
                     event.getClass().getSimpleName(), ex.getMessage(), ex);
            throw ex;
        }
    }
    
    /**
     * Processa evento de criação de apólice.
     */
    private void handleApoliceCriada(ApoliceCriadaEvent event) {
        log.info("Criando projeção para apólice: {}", event.getNumeroApolice());
        
        ApoliceQueryModel model = new ApoliceQueryModel(
            event.getAggregateId(),
            event.getNumeroApolice(),
            event.getSeguradoId()
        );
        
        // Dados básicos da apólice
        model.setProduto(event.getProduto());
        model.setStatus(StatusApolice.ATIVA);
        
        // Converter strings para tipos apropriados
        try {
            model.setVigenciaInicio(LocalDate.parse(event.getVigenciaInicio()));
            model.setVigenciaFim(LocalDate.parse(event.getVigenciaFim()));
            model.setValorSegurado(new BigDecimal(event.getValorSegurado()));
            model.setValorTotal(new BigDecimal(event.getPremioTotal()));
            model.setFormaPagamento(event.getFormaPagamento());
            
            // Processar coberturas
            List<TipoCobertura> tipos = new ArrayList<>();
            if (event.getCoberturas() != null) {
                for (Map<String, Object> coberturaData : event.getCoberturas()) {
                    String tipoStr = (String) coberturaData.get("tipo");
                    if (tipoStr != null) {
                        try {
                            TipoCobertura tipo = TipoCobertura.valueOf(tipoStr);
                            tipos.add(tipo);
                        } catch (IllegalArgumentException ex) {
                            log.warn("Tipo de cobertura inválido: {}", tipoStr);
                        }
                    }
                }
            }
            model.setCoberturas(tipos);
            model.setCoberturasResumo(criarResumoCobertura(tipos));
            model.setTemCoberturaTotal(tipos.contains(TipoCobertura.TOTAL));
            
        } catch (Exception ex) {
            log.error("Erro ao converter dados do evento: {}", ex.getMessage());
            // Usar valores padrão em caso de erro
            model.setVigenciaInicio(LocalDate.now());
            model.setVigenciaFim(LocalDate.now().plusYears(1));
            model.setValorSegurado(BigDecimal.ZERO);
            model.setValorTotal(BigDecimal.ZERO);
        }
        
        // Dados de controle
        model.setOperadorResponsavel(event.getOperadorId());
        model.setRenovacaoAutomatica(true); // Padrão
        
        // Métricas
        calcularMetricas(model);
        
        // Auditoria
        model.setLastEventId(event.getEventId() != null ? (long) event.getEventId().hashCode() : null);
        
        repository.save(model);
        
        log.info("Projeção criada para apólice: {}", event.getNumeroApolice());
    }
    
    /**
     * Processa evento de atualização de apólice.
     */
    private void handleApoliceAtualizada(ApoliceAtualizadaEvent event) {
        log.info("Atualizando projeção para apólice: {}", event.getAggregateId());
        
        Optional<ApoliceQueryModel> optModel = repository.findById(event.getAggregateId());
        if (optModel.isEmpty()) {
            log.warn("Apólice não encontrada para atualização: {}", event.getAggregateId());
            return;
        }
        
        ApoliceQueryModel model = optModel.get();
        
        // Atualizar observações
        String obs = model.getObservacoes();
        obs = (obs != null ? obs + "; " : "") + "Alteração realizada";
        model.setObservacoes(obs);
        
        // Recalcular métricas
        calcularMetricas(model);
        
        // Auditoria
        model.setLastEventId(event.getEventId() != null ? (long) event.getEventId().hashCode() : null);
        
        repository.save(model);
        
        log.info("Projeção atualizada para apólice: {}", event.getAggregateId());
    }
    
    /**
     * Processa evento de cancelamento de apólice.
     */
    private void handleApoliceCancelada(ApoliceCanceladaEvent event) {
        log.info("Cancelando projeção para apólice: {}", event.getAggregateId());
        
        Optional<ApoliceQueryModel> optModel = repository.findById(event.getAggregateId());
        if (optModel.isEmpty()) {
            log.warn("Apólice não encontrada para cancelamento: {}", event.getAggregateId());
            return;
        }
        
        ApoliceQueryModel model = optModel.get();
        
        // Atualizar status
        model.setStatus(StatusApolice.CANCELADA);
        
        // Adicionar informações do cancelamento
        String obs = model.getObservacoes();
        obs = (obs != null ? obs + "; " : "") + 
              String.format("Cancelada - Motivo: %s", event.getMotivo());
        model.setObservacoes(obs);
        
        // Zerar métricas de renovação
        model.setRenovacaoAutomatica(false);
        model.setVencimentoProximo(false);
        model.setScoreRenovacao(0);
        
        // Auditoria
        model.setLastEventId(event.getEventId() != null ? (long) event.getEventId().hashCode() : null);
        
        repository.save(model);
        
        log.info("Projeção cancelada para apólice: {}", event.getAggregateId());
    }
    
    /**
     * Processa evento de renovação de apólice.
     */
    private void handleApoliceRenovada(ApoliceRenovadaEvent event) {
        log.info("Renovando projeção para apólice: {}", event.getAggregateId());
        
        Optional<ApoliceQueryModel> optModel = repository.findById(event.getAggregateId());
        if (optModel.isEmpty()) {
            log.warn("Apólice não encontrada para renovação: {}", event.getAggregateId());
            return;
        }
        
        ApoliceQueryModel model = optModel.get();
        
        // Converter e atualizar vigência
        try {
            model.setVigenciaInicio(LocalDate.parse(event.getNovaVigenciaInicio()));
            model.setVigenciaFim(LocalDate.parse(event.getNovaVigenciaFim()));
            
            if (event.getNovoValorSegurado() != null) {
                model.setValorSegurado(new BigDecimal(event.getNovoValorSegurado()));
            }
        } catch (Exception ex) {
            log.error("Erro ao converter datas de renovação: {}", ex.getMessage());
        }
        
        // Garantir status ativo
        model.setStatus(StatusApolice.ATIVA);
        
        // Adicionar informação da renovação
        String obs = model.getObservacoes();
        obs = (obs != null ? obs + "; " : "") + "Renovada";
        model.setObservacoes(obs);
        
        // Recalcular métricas
        calcularMetricas(model);
        
        // Auditoria
        model.setLastEventId(event.getEventId() != null ? (long) event.getEventId().hashCode() : null);
        
        repository.save(model);
        
        log.info("Projeção renovada para apólice: {}", event.getAggregateId());
    }
    
    /**
     * Processa evento de adição de cobertura.
     */
    private void handleCoberturaAdicionada(CoberturaAdicionadaEvent event) {
        log.info("Adicionando cobertura à projeção da apólice: {}", event.getAggregateId());
        
        Optional<ApoliceQueryModel> optModel = repository.findById(event.getAggregateId());
        if (optModel.isEmpty()) {
            log.warn("Apólice não encontrada para adição de cobertura: {}", event.getAggregateId());
            return;
        }
        
        ApoliceQueryModel model = optModel.get();
        
        // Adicionar observação
        String obs = model.getObservacoes();
        obs = (obs != null ? obs + "; " : "") + "Cobertura adicionada";
        model.setObservacoes(obs);
        
        // Auditoria
        model.setLastEventId(event.getEventId() != null ? (long) event.getEventId().hashCode() : null);
        
        repository.save(model);
        
        log.info("Cobertura adicionada à projeção da apólice: {}", event.getAggregateId());
    }
    
    /**
     * Cria resumo textual das coberturas.
     */
    private String criarResumoCobertura(List<TipoCobertura> coberturas) {
        if (coberturas == null || coberturas.isEmpty()) {
            return "Nenhuma cobertura";
        }
        
        if (coberturas.contains(TipoCobertura.TOTAL)) {
            return "Cobertura Total";
        }
        
        return coberturas.stream()
            .map(TipoCobertura::getDescricao)
            .reduce((a, b) -> a + ", " + b)
            .orElse("Coberturas diversas");
    }
    
    /**
     * Calcula métricas da apólice.
     */
    private void calcularMetricas(ApoliceQueryModel model) {
        if (model.getVigenciaFim() != null) {
            // Calcular dias para vencimento
            long dias = ChronoUnit.DAYS.between(LocalDate.now(), model.getVigenciaFim());
            model.setDiasParaVencimento((int) dias);
            
            // Verificar vencimento próximo (30 dias)
            model.setVencimentoProximo(dias <= 30 && dias >= 0);
            
            // Calcular score de renovação (simulado)
            int score = calcularScoreRenovacao(model);
            model.setScoreRenovacao(score);
        }
    }
    
    /**
     * Calcula score de renovação baseado em critérios simulados.
     */
    private int calcularScoreRenovacao(ApoliceQueryModel model) {
        int score = 70; // Base
        
        // Bonus por cobertura total
        if (Boolean.TRUE.equals(model.getTemCoberturaTotal())) {
            score += 10;
        }
        
        // Bonus por valor alto
        if (model.getValorSegurado() != null && 
            model.getValorSegurado().compareTo(BigDecimal.valueOf(100000)) > 0) {
            score += 5;
        }
        
        // Bonus por pagamento anual
        if ("ANUAL".equals(model.getFormaPagamento())) {
            score += 5;
        }
        
        // Penalidade por vencimento muito próximo sem ação
        if (model.getDiasParaVencimento() != null && model.getDiasParaVencimento() <= 7) {
            score -= 10;
        }
        
        return Math.max(0, Math.min(100, score));
    }
}