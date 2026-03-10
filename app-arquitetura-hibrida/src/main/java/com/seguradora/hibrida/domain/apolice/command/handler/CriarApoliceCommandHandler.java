package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.CriarApoliceCommand;
import com.seguradora.hibrida.domain.apolice.model.NumeroApolice;
import com.seguradora.hibrida.domain.apolice.service.ApoliceValidationService;
import com.seguradora.hibrida.domain.apolice.service.SeguradoValidationService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

/**
 * Command Handler para criação de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class CriarApoliceCommandHandler {
    
    private static final Logger log = LoggerFactory.getLogger(CriarApoliceCommandHandler.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(45);
    
    private final AggregateRepository<ApoliceAggregate> repository;
    private final ApoliceValidationService apoliceValidationService;
    private final SeguradoValidationService seguradoValidationService;
    
    public CriarApoliceCommandHandler(
            AggregateRepository<ApoliceAggregate> repository,
            ApoliceValidationService apoliceValidationService,
            SeguradoValidationService seguradoValidationService) {
        
        this.repository = repository;
        this.apoliceValidationService = apoliceValidationService;
        this.seguradoValidationService = seguradoValidationService;
    }
    
    /**
     * Processa comando de criação de apólice.
     */
    @Transactional
    public CompletableFuture<String> handle(CriarApoliceCommand command) {
        log.info("Processando criação de apólice: {}", command.getApoliceId());
        
        return CompletableFuture
                .supplyAsync(() -> processarCriacao(command))
                .orTimeout(TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Erro ao criar apólice {}: {}", command.getApoliceId(), throwable.getMessage());
                    } else {
                        log.info("Apólice criada com sucesso: {}", result);
                    }
                });
    }
    
    private String processarCriacao(CriarApoliceCommand command) {
        try {
            // 1. Validar segurado
            validarSegurado(command);
            
            // 2. Validar dados da apólice
            validarDadosApolice(command);
            
            // 3. Gerar número da apólice
            NumeroApolice numeroApolice = gerarNumeroApolice();
            
            // 4. Criar aggregate
            ApoliceAggregate aggregate = new ApoliceAggregate(
                    command.getApoliceId(),
                    numeroApolice,
                    command.getSeguradoId(),
                    command.getProduto(),
                    command.getVigencia(),
                    command.getValorSegurado(),
                    command.getFormaPagamento(),
                    command.getCoberturas(),
                    command.getOperadorId()
            );
            
            // 5. Salvar no repositório
            repository.save(aggregate);
            
            log.info("Apólice {} criada com número {}", command.getApoliceId(), numeroApolice.getNumero());
            return command.getApoliceId();
            
        } catch (Exception e) {
            log.error("Erro ao processar criação da apólice {}: {}", command.getApoliceId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao criar apólice: " + e.getMessage(), e);
        }
    }
    
    private void validarSegurado(CriarApoliceCommand command) {
        log.debug("Validando segurado: {}", command.getSeguradoId());
        
        // Verificar se segurado está ativo
        if (!seguradoValidationService.isSeguradoAtivo(command.getSeguradoId())) {
            throw new IllegalArgumentException("Segurado não está ativo: " + command.getSeguradoId());
        }
        
        // Verificar limite de apólices por segurado
        int limiteApolices = 5; // Configurável
        long apolicesAtivas = seguradoValidationService.contarApolicesAtivas(command.getSeguradoId());
        
        if (apolicesAtivas >= limiteApolices) {
            throw new IllegalArgumentException(
                String.format("Segurado %s já possui %d apólices ativas (limite: %d)", 
                             command.getSeguradoId(), apolicesAtivas, limiteApolices)
            );
        }
        
        // Verificar score de crédito (simulado)
        int scoreMinimo = 300;
        int scoreSegurado = seguradoValidationService.obterScoreCredito(command.getSeguradoId());
        
        if (scoreSegurado < scoreMinimo) {
            throw new IllegalArgumentException(
                String.format("Score de crédito insuficiente: %d (mínimo: %d)", 
                             scoreSegurado, scoreMinimo)
            );
        }
        
        log.debug("Segurado validado com sucesso: {}", command.getSeguradoId());
    }
    
    private void validarDadosApolice(CriarApoliceCommand command) {
        log.debug("Validando dados da apólice");
        
        // Validar vigência
        apoliceValidationService.validarVigencia(command.getVigencia());
        
        // Validar valor segurado
        apoliceValidationService.validarValorSegurado(command.getValorSegurado(), command.getProduto());
        
        // Validar coberturas
        apoliceValidationService.validarCoberturas(command.getCoberturas(), command.getProduto());
        
        // Validar combinação de coberturas
        apoliceValidationService.validarCombinacaoCoberturas(command.getCoberturas());
        
        // Validar forma de pagamento
        apoliceValidationService.validarFormaPagamento(command.getFormaPagamento(), command.getValorSegurado());
        
        log.debug("Dados da apólice validados com sucesso");
    }
    
    private NumeroApolice gerarNumeroApolice() {
        // Gerar número sequencial baseado no ano atual
        // Formato: AP-YYYY-NNNNNN
        int ano = java.time.Year.now().getValue();
        long sequencial = System.currentTimeMillis() % 1000000; // Simplificado para demo
        
        String numero = String.format("AP-%d-%06d", ano, sequencial);
        return NumeroApolice.of(numero);
    }
}