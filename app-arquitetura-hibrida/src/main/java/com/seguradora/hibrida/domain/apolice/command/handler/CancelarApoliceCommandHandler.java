package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.CancelarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Command Handler para cancelamento de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class CancelarApoliceCommandHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CancelarApoliceCommandHandler.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(30);
    
    private final AggregateRepository<ApoliceAggregate> repository;
    private final ApoliceValidationService validationService;
    
    public CancelarApoliceCommandHandler(
            AggregateRepository<ApoliceAggregate> repository,
            ApoliceValidationService validationService) {
        
        this.repository = repository;
        this.validationService = validationService;
    }
    
    /**
     * Processa comando de cancelamento de apólice.
     */
    @Transactional
    public CompletableFuture<Void> handle(CancelarApoliceCommand command) {
        log.info("Processando cancelamento da apólice: {} (tipo: {})", 
                command.getApoliceId(), command.getTipoCancelamento());
        
        return CompletableFuture
                .runAsync(() -> processarCancelamento(command))
                .orTimeout(TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Erro ao cancelar apólice {}: {}", command.getApoliceId(), throwable.getMessage());
                    } else {
                        log.info("Apólice cancelada com sucesso: {}", command.getApoliceId());
                    }
                });
    }
    
    private void processarCancelamento(CancelarApoliceCommand command) {
        try {
            // 1. Carregar aggregate do repositório
            ApoliceAggregate aggregate = carregarAggregate(command);
            
            // 2. Validar se pode ser cancelada
            validarCancelamento(aggregate, command);
            
            // 3. Verificar sinistros em aberto (simulado)
            verificarSinistrosAbertos(command);
            
            // 4. Validar permissões do operador
            validarPermissoesOperador(command);
            
            // 5. Aplicar cancelamento
            aggregate.cancelar(
                    command.getMotivo(),
                    command.getDataEfeito(),
                    command.getOperadorId(),
                    command.getObservacoes(),
                    command.getTipoCancelamento()
            );
            
            // 6. Salvar alterações
            repository.save(aggregate);
            
            log.info("Apólice {} cancelada por {} - Motivo: {}", 
                    command.getApoliceId(), command.getOperadorId(), command.getMotivo());
            
        } catch (Exception e) {
            log.error("Erro ao processar cancelamento da apólice {}: {}", 
                    command.getApoliceId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao cancelar apólice: " + e.getMessage(), e);
        }
    }
    
    private ApoliceAggregate carregarAggregate(CancelarApoliceCommand command) {
        try {
            return repository.getById(command.getApoliceId());
        } catch (Exception e) {
            log.error("Erro ao carregar apólice {}: {}", command.getApoliceId(), e.getMessage());
            throw new RuntimeException("Apólice não encontrada: " + command.getApoliceId(), e);
        }
    }
    
    private void validarCancelamento(ApoliceAggregate aggregate, CancelarApoliceCommand command) {
        log.debug("Validando cancelamento da apólice {}", command.getApoliceId());
        
        // Verificar status, vigência e data de efeito
        validationService.validarCancelamento(
                aggregate.getStatus(), 
                aggregate.getVigencia(), 
                command.getDataEfeito()
        );
        
        // Validações específicas por tipo de cancelamento
        switch (command.getTipoCancelamento()) {
            case SOLICITACAO_SEGURADO -> {
                // Verificar se não há carência para cancelamento
                if (aggregate.getVigencia().getDiasDecorridos() < 30) {
                    log.warn("Cancelamento solicitado antes de 30 dias - pode haver taxa");
                }
            }
            case INADIMPLENCIA -> {
                // Verificar se realmente há inadimplência (simulado)
                log.debug("Validando inadimplência para apólice {}", command.getApoliceId());
            }
            case FRAUDE -> {
                // Cancelamento por fraude deve ser imediato
                if (!command.getDataEfeito().equals(java.time.LocalDate.now())) {
                    throw new IllegalArgumentException("Cancelamento por fraude deve ser imediato");
                }
            }
            case PERDA_TOTAL -> {
                // Verificar se há sinistro de perda total registrado
                log.debug("Validando perda total para apólice {}", command.getApoliceId());
            }
        }
        
        log.debug("Cancelamento validado para apólice {}", command.getApoliceId());
    }
    
    private void verificarSinistrosAbertos(CancelarApoliceCommand command) {
        log.debug("Verificando sinistros em aberto para apólice {}", command.getApoliceId());

        // TODO: Integrar com sistema de sinistros para verificar pendências
        // Por enquanto, assume que não há sinistros em aberto
        // Em produção, esta validação seria feita via serviço externo:
        // boolean temSinistrosAbertos = sinistroService.verificarSinistrosAbertos(command.getApoliceId());
        //
        // if (temSinistrosAbertos) {
        //     switch (command.getTipoCancelamento()) {
        //         case SOLICITACAO_SEGURADO, VENDA_VEICULO -> {
        //             throw new IllegalStateException(
        //                 "Não é possível cancelar apólice com sinistros em aberto"
        //             );
        //         }
        //         case FRAUDE, INADIMPLENCIA -> {
        //             log.warn("Cancelando apólice {} com sinistros em aberto devido a {}",
        //                     command.getApoliceId(), command.getTipoCancelamento());
        //         }
        //     }
        // }

        log.debug("Verificação de sinistros concluída para apólice {}", command.getApoliceId());
        
        log.debug("Verificação de sinistros concluída para apólice {}", command.getApoliceId());
    }
    
    private void validarPermissoesOperador(CancelarApoliceCommand command) {
        log.debug("Validando permissões do operador {} para cancelamento", command.getOperadorId());
        
        // Simulação - em produção seria consulta ao sistema de permissões
        boolean temPermissao = true; // Assumindo que tem permissão
        
        // Alguns tipos de cancelamento exigem permissões especiais
        switch (command.getTipoCancelamento()) {
            case FRAUDE -> {
                // Apenas supervisores podem cancelar por fraude
                boolean isSupervisor = command.getOperadorId().startsWith("SUP_");
                if (!isSupervisor) {
                    throw new IllegalArgumentException(
                        "Apenas supervisores podem cancelar apólices por fraude"
                    );
                }
            }
            case DECISAO_SEGURADORA -> {
                // Apenas gerentes podem cancelar por decisão da seguradora
                boolean isGerente = command.getOperadorId().startsWith("GER_");
                if (!isGerente) {
                    throw new IllegalArgumentException(
                        "Apenas gerentes podem cancelar por decisão da seguradora"
                    );
                }
            }
        }
        
        if (!temPermissao) {
            throw new IllegalArgumentException(
                "Operador não tem permissão para cancelar apólices: " + command.getOperadorId()
            );
        }
        
        log.debug("Permissões validadas para operador {}", command.getOperadorId());
    }
}