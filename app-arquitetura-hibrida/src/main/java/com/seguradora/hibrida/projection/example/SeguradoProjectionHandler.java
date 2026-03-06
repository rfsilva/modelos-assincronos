package com.seguradora.hibrida.projection.example;

import com.seguradora.hibrida.eventstore.model.DomainEvent;
import com.seguradora.hibrida.projection.AbstractProjectionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Exemplo de Projection Handler para eventos de segurado.
 * 
 * <p>Este handler demonstra como implementar um projection handler
 * completo que processa eventos de domínio relacionados a segurados
 * e atualiza os query models correspondentes.
 * 
 * <p>Características:
 * <ul>
 *   <li>Processa eventos de criação, atualização e desativação</li>
 *   <li>Mantém dados desnormalizados para consultas otimizadas</li>
 *   <li>Implementa idempotência para reprocessamento seguro</li>
 *   <li>Suporte a rebuild completo e incremental</li>
 * </ul>
 */
@Component
public class SeguradoProjectionHandler extends AbstractProjectionHandler<DomainEvent> {
    
    private static final Logger log = LoggerFactory.getLogger(SeguradoProjectionHandler.class);
    
    // Em uma implementação real, injetaria o repository do query model
    // @Autowired
    // private SeguradoQueryRepository seguradoQueryRepository;
    
    @Override
    protected void doHandle(DomainEvent event) throws Exception {
        log.debug("Processando evento {} para projeção de segurado", event.getEventType());
        
        // Extrair dados do evento
        Map<String, Object> eventData = extractEventData(event);
        
        // Processar baseado no tipo do evento
        switch (event.getEventType()) {
            case "SeguradoCriadoEvent":
                handleSeguradoCriado(eventData);
                break;
            case "SeguradoAtualizadoEvent":
                handleSeguradoAtualizado(eventData);
                break;
            case "SeguradoDesativadoEvent":
                handleSeguradoDesativado(eventData);
                break;
            case "SeguradoReativadoEvent":
                handleSeguradoReativado(eventData);
                break;
            case "EnderecoSeguradoAtualizadoEvent":
                handleEnderecoAtualizado(eventData);
                break;
            case "ContatoSeguradoAtualizadoEvent":
                handleContatoAtualizado(eventData);
                break;
            default:
                log.debug("Tipo de evento {} não processado por este handler", event.getEventType());
        }
    }
    
    /**
     * Processa evento de criação de segurado.
     */
    private void handleSeguradoCriado(Map<String, Object> eventData) {
        log.debug("Criando nova projeção de segurado");
        
        try {
            // Em uma implementação real, criaria um novo SeguradoQueryModel
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            String cpf = (String) eventData.get("cpf");
            String nome = (String) eventData.get("nome");
            String email = (String) eventData.get("email");
            String telefone = (String) eventData.get("telefone");
            
            // Validações básicas
            if (seguradoId == null || cpf == null || nome == null) {
                throw new IllegalArgumentException("Dados obrigatórios não fornecidos para criação do segurado");
            }
            
            // SeguradoQueryModel segurado = new SeguradoQueryModel();
            // segurado.setId(seguradoId);
            // segurado.setCpf(cpf);
            // segurado.setNome(nome);
            // segurado.setEmail(email);
            // segurado.setTelefone(telefone);
            // segurado.setStatus("ATIVO");
            // segurado.setDataCriacao(Instant.now());
            // segurado.setDataAtualizacao(Instant.now());
            
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Projeção de segurado criada: {} - {}", cpf, nome);
            
        } catch (Exception e) {
            log.error("Erro ao criar projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Processa evento de atualização de segurado.
     */
    private void handleSeguradoAtualizado(Map<String, Object> eventData) {
        log.debug("Atualizando projeção de segurado");
        
        try {
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            
            if (seguradoId == null) {
                throw new IllegalArgumentException("ID do segurado não fornecido para atualização");
            }
            
            // Em uma implementação real, buscaria e atualizaria o SeguradoQueryModel
            // SeguradoQueryModel segurado = seguradoQueryRepository.findById(seguradoId)
            //     .orElseThrow(() -> new ProjectionException("Segurado não encontrado: " + seguradoId, getProjectionName()));
            
            // Atualizar campos modificados
            // if (eventData.containsKey("nome")) {
            //     segurado.setNome((String) eventData.get("nome"));
            // }
            // if (eventData.containsKey("email")) {
            //     segurado.setEmail((String) eventData.get("email"));
            // }
            // if (eventData.containsKey("telefone")) {
            //     segurado.setTelefone((String) eventData.get("telefone"));
            // }
            
            // segurado.setDataAtualizacao(Instant.now());
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Projeção de segurado atualizada: {}", seguradoId);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Processa evento de desativação de segurado.
     */
    private void handleSeguradoDesativado(Map<String, Object> eventData) {
        log.debug("Desativando projeção de segurado");
        
        try {
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            String motivo = (String) eventData.get("motivo");
            
            if (seguradoId == null) {
                throw new IllegalArgumentException("ID do segurado não fornecido para desativação");
            }
            
            // Em uma implementação real, atualizaria o status
            // SeguradoQueryModel segurado = seguradoQueryRepository.findById(seguradoId)
            //     .orElseThrow(() -> new ProjectionException("Segurado não encontrado: " + seguradoId, getProjectionName()));
            
            // segurado.setStatus("INATIVO");
            // segurado.setMotivoInativacao(motivo);
            // segurado.setDataInativacao(Instant.now());
            // segurado.setDataAtualizacao(Instant.now());
            
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Projeção de segurado desativada: {} - Motivo: {}", seguradoId, motivo);
            
        } catch (Exception e) {
            log.error("Erro ao desativar projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Processa evento de reativação de segurado.
     */
    private void handleSeguradoReativado(Map<String, Object> eventData) {
        log.debug("Reativando projeção de segurado");
        
        try {
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            
            if (seguradoId == null) {
                throw new IllegalArgumentException("ID do segurado não fornecido para reativação");
            }
            
            // Em uma implementação real, atualizaria o status
            // SeguradoQueryModel segurado = seguradoQueryRepository.findById(seguradoId)
            //     .orElseThrow(() -> new ProjectionException("Segurado não encontrado: " + seguradoId, getProjectionName()));
            
            // segurado.setStatus("ATIVO");
            // segurado.setMotivoInativacao(null);
            // segurado.setDataInativacao(null);
            // segurado.setDataReativacao(Instant.now());
            // segurado.setDataAtualizacao(Instant.now());
            
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Projeção de segurado reativada: {}", seguradoId);
            
        } catch (Exception e) {
            log.error("Erro ao reativar projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Processa evento de atualização de endereço.
     */
    private void handleEnderecoAtualizado(Map<String, Object> eventData) {
        log.debug("Atualizando endereço na projeção de segurado");
        
        try {
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            
            if (seguradoId == null) {
                throw new IllegalArgumentException("ID do segurado não fornecido para atualização de endereço");
            }
            
            // Em uma implementação real, atualizaria os dados de endereço
            // SeguradoQueryModel segurado = seguradoQueryRepository.findById(seguradoId)
            //     .orElseThrow(() -> new ProjectionException("Segurado não encontrado: " + seguradoId, getProjectionName()));
            
            // segurado.setCep((String) eventData.get("cep"));
            // segurado.setLogradouro((String) eventData.get("logradouro"));
            // segurado.setNumero((String) eventData.get("numero"));
            // segurado.setComplemento((String) eventData.get("complemento"));
            // segurado.setBairro((String) eventData.get("bairro"));
            // segurado.setCidade((String) eventData.get("cidade"));
            // segurado.setUf((String) eventData.get("uf"));
            // segurado.setDataAtualizacao(Instant.now());
            
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Endereço atualizado na projeção de segurado: {}", seguradoId);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar endereço na projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Processa evento de atualização de contato.
     */
    private void handleContatoAtualizado(Map<String, Object> eventData) {
        log.debug("Atualizando contato na projeção de segurado");
        
        try {
            UUID seguradoId = (UUID) eventData.get("seguradoId");
            
            if (seguradoId == null) {
                throw new IllegalArgumentException("ID do segurado não fornecido para atualização de contato");
            }
            
            // Em uma implementação real, atualizaria os dados de contato
            // SeguradoQueryModel segurado = seguradoQueryRepository.findById(seguradoId)
            //     .orElseThrow(() -> new ProjectionException("Segurado não encontrado: " + seguradoId, getProjectionName()));
            
            // if (eventData.containsKey("email")) {
            //     segurado.setEmail((String) eventData.get("email"));
            // }
            // if (eventData.containsKey("telefone")) {
            //     segurado.setTelefone((String) eventData.get("telefone"));
            // }
            // if (eventData.containsKey("celular")) {
            //     segurado.setCelular((String) eventData.get("celular"));
            // }
            
            // segurado.setDataAtualizacao(Instant.now());
            // seguradoQueryRepository.save(segurado);
            
            log.debug("Contato atualizado na projeção de segurado: {}", seguradoId);
            
        } catch (Exception e) {
            log.error("Erro ao atualizar contato na projeção de segurado: {}", e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Extrai dados do evento de domínio.
     */
    private Map<String, Object> extractEventData(DomainEvent event) {
        // Implementação simplificada - em produção usaria deserialização adequada
        return event.getMetadata() != null ? event.getMetadata() : Map.of();
    }
    
    @Override
    public boolean supports(DomainEvent event) {
        // Suporta apenas eventos relacionados a segurados
        String eventType = event.getEventType();
        return eventType.startsWith("Segurado") || 
               eventType.contains("SeguradoEvent") ||
               eventType.startsWith("Endereco") && eventType.contains("Segurado") ||
               eventType.startsWith("Contato") && eventType.contains("Segurado");
    }
    
    @Override
    public int getOrder() {
        return 5; // Prioridade alta para projeção principal de segurados
    }
    
    @Override
    public boolean isAsync() {
        return true; // Processamento assíncrono para melhor performance
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 20; // Timeout adequado para operações de segurado
    }
    
    @Override
    public boolean isRetryable() {
        return true; // Permite retry em caso de falha
    }
    
    @Override
    public int getMaxRetries() {
        return 3; // Máximo 3 tentativas
    }
    
    @Override
    protected void recordSuccess(DomainEvent event, long processingTimeMs) {
        super.recordSuccess(event, processingTimeMs);
        
        // Métricas específicas do handler de segurado
        if (processingTimeMs > 1000) { // Log se demorar mais que 1 segundo
            log.warn("Processamento lento do evento {} para segurado: {}ms", 
                    event.getEventType(), processingTimeMs);
        }
    }
    
    @Override
    protected void recordError(DomainEvent event, long processingTimeMs, Exception error) {
        super.recordError(event, processingTimeMs, error);
        
        // Log específico para erros de segurado
        log.error("Erro no processamento do evento {} para segurado: {} ({}ms)", 
                 event.getEventType(), error.getMessage(), processingTimeMs);
    }
}