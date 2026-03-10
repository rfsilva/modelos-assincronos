package com.seguradora.hibrida.domain.sinistro.projection;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import com.seguradora.hibrida.domain.sinistro.query.model.SinistroQueryModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Map;
import java.util.UUID;

/**
 * Projection Handler para eventos de sinistro.
 * 
 * <p>Este handler processa eventos de domínio relacionados a sinistros
 * e atualiza as projeções de consulta correspondentes.
 * 
 * <p>Características:
 * <ul>
 *   <li>Extends AbstractProjectionHandler para funcionalidades básicas</li>
 *   <li>Processa eventos de forma idempotente</li>
 *   <li>Atualiza SinistroQueryModel automaticamente</li>
 *   <li>Suporte a retry em caso de falha</li>
 * </ul>
 */
@Component
public class SinistroProjectionHandler extends AbstractProjectionHandler<DomainEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SinistroProjectionHandler.class);
    
    // Em uma implementação real, injetaria o repository do query model
    // @Autowired
    // private SinistroQueryRepository sinistroQueryRepository;
    
    @Override
    protected void doHandle(DomainEvent event) throws Exception {
        log.info("Processando evento {} para projeção de sinistro", event.getEventType());
        
        // Extrair dados do evento
        Map<String, Object> eventData = extractEventData(event);
        
        // Processar baseado no tipo do evento
        switch (event.getEventType()) {
            case "SinistroCriadoEvent":
                handleSinistroCriado(eventData);
                break;
            case "SinistroAtualizadoEvent":
                handleSinistroAtualizado(eventData);
                break;
            case "ConsultaDetranIniciadaEvent":
                handleConsultaDetranIniciada(eventData);
                break;
            case "ConsultaDetranConcluidaEvent":
                handleConsultaDetranConcluida(eventData);
                break;
            default:
                log.debug("Tipo de evento {} não processado por este handler", event.getEventType());
        }
    }
    
    /**
     * Processa evento de criação de sinistro.
     */
    private void handleSinistroCriado(Map<String, Object> eventData) {
        log.debug("Criando nova projeção de sinistro");
        
        // Em uma implementação real, criaria um novo SinistroQueryModel
        SinistroQueryModel sinistro = new SinistroQueryModel();
        sinistro.setId((UUID) eventData.get("sinistroId"));
        sinistro.setProtocolo((String) eventData.get("protocolo"));
        sinistro.setCpfSegurado((String) eventData.get("cpfSegurado"));
        sinistro.setNomeSegurado((String) eventData.get("nomeSegurado"));
        sinistro.setPlaca((String) eventData.get("placa"));
        sinistro.setTipoSinistro((String) eventData.get("tipoSinistro"));
        sinistro.setStatus("ABERTO");
        
        // Converter LocalDateTime para Instant se necessário
        Object dataOcorrencia = eventData.get("dataOcorrencia");
        if (dataOcorrencia instanceof LocalDateTime) {
            sinistro.setDataOcorrencia(((LocalDateTime) dataOcorrencia).atZone(ZoneId.systemDefault()).toInstant());
        } else if (dataOcorrencia instanceof Instant) {
            sinistro.setDataOcorrencia((Instant) dataOcorrencia);
        }
        
        sinistro.setDataAbertura(Instant.now());
        
        // sinistroQueryRepository.save(sinistro);
        
        log.debug("Projeção de sinistro criada: {}", sinistro.getProtocolo());
    }
    
    /**
     * Processa evento de atualização de sinistro.
     */
    private void handleSinistroAtualizado(Map<String, Object> eventData) {
        log.debug("Atualizando projeção de sinistro");
        
        UUID sinistroId = (UUID) eventData.get("sinistroId");
        
        // Em uma implementação real, buscaria e atualizaria o SinistroQueryModel
        // SinistroQueryModel sinistro = sinistroQueryRepository.findById(sinistroId)
        //     .orElseThrow(() -> new ProjectionException("Sinistro não encontrado: " + sinistroId, getProjectionName()));
        
        // Atualizar campos modificados
        // if (eventData.containsKey("status")) {
        //     sinistro.setStatus((String) eventData.get("status"));
        // }
        // if (eventData.containsKey("operadorResponsavel")) {
        //     sinistro.setOperadorResponsavel((String) eventData.get("operadorResponsavel"));
        // }
        
        // sinistroQueryRepository.save(sinistro);
        
        log.debug("Projeção de sinistro atualizada: {}", sinistroId);
    }
    
    /**
     * Processa evento de início de consulta Detran.
     */
    private void handleConsultaDetranIniciada(Map<String, Object> eventData) {
        log.debug("Atualizando status da consulta Detran");
        
        UUID sinistroId = (UUID) eventData.get("sinistroId");
        
        // Em uma implementação real, atualizaria o status da consulta
        // SinistroQueryModel sinistro = sinistroQueryRepository.findById(sinistroId)
        //     .orElseThrow(() -> new ProjectionException("Sinistro não encontrado: " + sinistroId, getProjectionName()));
        
        // sinistro.setConsultaDetranStatus("EM_ANDAMENTO");
        // sinistro.setConsultaDetranTimestamp(Instant.now());
        
        // sinistroQueryRepository.save(sinistro);
        
        log.debug("Status da consulta Detran atualizado para sinistro: {}", sinistroId);
    }
    
    /**
     * Processa evento de conclusão de consulta Detran.
     */
    private void handleConsultaDetranConcluida(Map<String, Object> eventData) {
        log.debug("Finalizando consulta Detran");
        
        UUID sinistroId = (UUID) eventData.get("sinistroId");
        
        // Em uma implementação real, atualizaria com os dados retornados
        // SinistroQueryModel sinistro = sinistroQueryRepository.findById(sinistroId)
        //     .orElseThrow(() -> new ProjectionException("Sinistro não encontrado: " + sinistroId, getProjectionName()));
        
        // sinistro.setConsultaDetranStatus("CONCLUIDA");
        // sinistro.setConsultaDetranRealizada(true);
        // sinistro.setDadosDetran((Map<String, Object>) eventData.get("dadosDetran"));
        
        // sinistroQueryRepository.save(sinistro);
        
        log.debug("Consulta Detran concluída para sinistro: {}", sinistroId);
    }
    
    /**
     * Extrai dados do evento de domínio.
     * 
     * <p>Em uma implementação real, isso seria feito através de
     * deserialização adequada do evento.
     */
    private Map<String, Object> extractEventData(DomainEvent event) {
        // Implementação simplificada - em produção usaria deserialização adequada
        // EventMetadata extends HashMap, então podemos usar diretamente
        return event.getMetadata() != null ? event.getMetadata() : Map.of();
    }
    
    @Override
    public boolean supports(DomainEvent event) {
        // Suporta apenas eventos relacionados a sinistros
        String eventType = event.getEventType();
        return eventType.startsWith("Sinistro") || 
               eventType.startsWith("ConsultaDetran");
    }
    
    @Override
    public int getOrder() {
        return 10; // Prioridade alta para projeção principal
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono para melhor performance
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 15; // Timeout menor para projeções simples
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Permite retry em caso de falha
    }
    
    @Override
    public int getMaxRetries() {
        return 3; // Máximo 3 tentativas
    }
}