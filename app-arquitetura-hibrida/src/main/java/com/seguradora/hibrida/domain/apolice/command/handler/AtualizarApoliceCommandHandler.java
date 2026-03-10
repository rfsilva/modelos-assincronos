package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.AtualizarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Command Handler para atualização de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class AtualizarApoliceCommandHandler {
    
    private static final Logger log = LoggerFactory.getLogger(AtualizarApoliceCommandHandler.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    private final AggregateRepository<ApoliceAggregate> repository;
    private final ApoliceValidationService validationService;
    
    public AtualizarApoliceCommandHandler(
            AggregateRepository<ApoliceAggregate> repository,
            ApoliceValidationService validationService) {
        
        this.repository = repository;
        this.validationService = validationService;
    }
    
    /**
     * Processa comando de atualização de apólice.
     */
    @Transactional
    public CompletableFuture<Void> handle(AtualizarApoliceCommand command) {
        log.info("Processando atualização da apólice: {}", command.getApoliceId());
        
        return CompletableFuture
                .runAsync(() -> processarAtualizacao(command))
                .orTimeout(TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Erro ao atualizar apólice {}: {}", command.getApoliceId(), throwable.getMessage());
                    } else {
                        log.info("Apólice atualizada com sucesso: {}", command.getApoliceId());
                    }
                });
    }
    
    private void processarAtualizacao(AtualizarApoliceCommand command) {
        try {
            // 1. Carregar aggregate do repositório
            ApoliceAggregate aggregate = carregarAggregate(command);
            
            // 2. Validar se pode ser alterada
            validarAlteracao(aggregate, command);
            
            // 3. Validar novos dados
            validarNovosDados(command);
            
            // 4. Aplicar alterações
            aggregate.atualizarDados(
                    command.getNovoValorSegurado(),
                    command.getNovasCoberturas(),
                    command.getOperadorId(),
                    command.getMotivo()
            );
            
            // 5. Salvar alterações
            repository.save(aggregate);
            
            log.info("Apólice {} atualizada por operador {}", 
                    command.getApoliceId(), command.getOperadorId());
            
        } catch (ConcurrencyException e) {
            log.warn("Conflito de concorrência ao atualizar apólice {}: {}", 
                    command.getApoliceId(), e.getMessage());
            throw new RuntimeException("Conflito de versão. Recarregue os dados e tente novamente.", e);
            
        } catch (Exception e) {
            log.error("Erro ao processar atualização da apólice {}: {}", 
                    command.getApoliceId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao atualizar apólice: " + e.getMessage(), e);
        }
    }
    
    private ApoliceAggregate carregarAggregate(AtualizarApoliceCommand command) {
        try {
            ApoliceAggregate aggregate = repository.getById(command.getApoliceId());
            
            // Verificar controle de versão se especificado
            if (command.hasVersionControl()) {
                Long versaoAtual = aggregate.getVersion();
                Long versaoEsperada = command.getVersaoEsperada();
                
                if (!versaoAtual.equals(versaoEsperada)) {
                    throw new ConcurrencyException(
                        command.getApoliceId(), 
                        versaoEsperada, 
                        versaoAtual
                    );
                }
            }
            
            return aggregate;
            
        } catch (Exception e) {
            log.error("Erro ao carregar apólice {}: {}", command.getApoliceId(), e.getMessage());
            throw new RuntimeException("Apólice não encontrada: " + command.getApoliceId(), e);
        }
    }
    
    private void validarAlteracao(ApoliceAggregate aggregate, AtualizarApoliceCommand command) {
        log.debug("Validando se apólice {} pode ser alterada", command.getApoliceId());
        
        // Verificar status e vigência
        validationService.validarAlteracao(aggregate.getStatus(), aggregate.getVigencia());
        
        // Verificar se há alterações significativas
        boolean temAlteracoes = !aggregate.getValorSegurado().ehIgualA(command.getNovoValorSegurado()) ||
                               !coberturasIguais(aggregate.getCoberturas(), command.getNovasCoberturas());
        
        if (!temAlteracoes) {
            throw new IllegalArgumentException("Nenhuma alteração detectada");
        }
        
        log.debug("Apólice {} pode ser alterada", command.getApoliceId());
    }
    
    private void validarNovosDados(AtualizarApoliceCommand command) {
        log.debug("Validando novos dados da apólice {}", command.getApoliceId());
        
        // Validar novo valor segurado
        if (!command.getNovoValorSegurado().isPositivo()) {
            throw new IllegalArgumentException("Novo valor segurado deve ser positivo");
        }
        
        // Validar novas coberturas
        if (command.getNovasCoberturas().isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        // Validar combinação de coberturas
        validationService.validarCombinacaoCoberturas(command.getNovasCoberturas());
        
        log.debug("Novos dados validados para apólice {}", command.getApoliceId());
    }
    
    private boolean coberturasIguais(java.util.List<com.seguradora.hibrida.domain.apolice.model.Cobertura> coberturas1, 
                                   java.util.List<com.seguradora.hibrida.domain.apolice.model.Cobertura> coberturas2) {
        if (coberturas1.size() != coberturas2.size()) {
            return false;
        }
        
        return coberturas1.containsAll(coberturas2) && coberturas2.containsAll(coberturas1);
    }
}