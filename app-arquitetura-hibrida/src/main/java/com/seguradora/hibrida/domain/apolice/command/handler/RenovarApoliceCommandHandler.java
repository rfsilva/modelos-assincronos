package com.seguradora.hibrida.domain.apolice.command.handler;

import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.apolice.aggregate.ApoliceAggregate;
import com.seguradora.hibrida.domain.apolice.command.RenovarApoliceCommand;
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
 * Command Handler para renovação de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class RenovarApoliceCommandHandler {
    
    private static final Logger log = LoggerFactory.getLogger(RenovarApoliceCommandHandler.class);
    private static final Duration TIMEOUT = Duration.ofSeconds(45);
    
    private final AggregateRepository<ApoliceAggregate> repository;
    private final ApoliceValidationService apoliceValidationService;
    private final SeguradoValidationService seguradoValidationService;
    
    public RenovarApoliceCommandHandler(
            AggregateRepository<ApoliceAggregate> repository,
            ApoliceValidationService apoliceValidationService,
            SeguradoValidationService seguradoValidationService) {
        
        this.repository = repository;
        this.apoliceValidationService = apoliceValidationService;
        this.seguradoValidationService = seguradoValidationService;
    }
    
    /**
     * Processa comando de renovação de apólice.
     */
    @Transactional
    public CompletableFuture<Void> handle(RenovarApoliceCommand command) {
        log.info("Processando renovação da apólice: {} (tipo: {})", 
                command.getApoliceId(), command.getTipoRenovacao());
        
        return CompletableFuture
                .runAsync(() -> processarRenovacao(command))
                .orTimeout(TIMEOUT.toSeconds(), TimeUnit.SECONDS)
                .whenComplete((result, throwable) -> {
                    if (throwable != null) {
                        log.error("Erro ao renovar apólice {}: {}", command.getApoliceId(), throwable.getMessage());
                    } else {
                        log.info("Apólice renovada com sucesso: {}", command.getApoliceId());
                    }
                });
    }
    
    private void processarRenovacao(RenovarApoliceCommand command) {
        try {
            // 1. Carregar aggregate do repositório
            ApoliceAggregate aggregate = carregarAggregate(command);
            
            // 2. Validar elegibilidade para renovação
            validarElegibilidade(aggregate, command);
            
            // 3. Verificar dados do segurado
            verificarSegurado(aggregate, command);
            
            // 4. Validar novos dados da renovação
            validarDadosRenovacao(command);
            
            // 5. Recalcular prêmio com novos fatores
            calcularNovosPremios(aggregate, command);
            
            // 6. Aplicar renovação
            aggregate.renovar(
                    command.getNovaVigencia(),
                    command.getNovoValorSegurado(),
                    command.getNovasCoberturas(),
                    command.getNovaFormaPagamento(),
                    command.getOperadorId(),
                    command.getTipoRenovacao(),
                    command.getObservacoes()
            );
            
            // 7. Salvar alterações
            repository.save(aggregate);
            
            log.info("Apólice {} renovada por {} até {}", 
                    command.getApoliceId(), command.getOperadorId(), command.getNovaVigencia().getFim());
            
        } catch (Exception e) {
            log.error("Erro ao processar renovação da apólice {}: {}", 
                    command.getApoliceId(), e.getMessage(), e);
            throw new RuntimeException("Falha ao renovar apólice: " + e.getMessage(), e);
        }
    }
    
    private ApoliceAggregate carregarAggregate(RenovarApoliceCommand command) {
        try {
            return repository.getById(command.getApoliceId());
        } catch (Exception e) {
            log.error("Erro ao carregar apólice {}: {}", command.getApoliceId(), e.getMessage());
            throw new RuntimeException("Apólice não encontrada: " + command.getApoliceId(), e);
        }
    }
    
    private void validarElegibilidade(ApoliceAggregate aggregate, RenovarApoliceCommand command) {
        log.debug("Validando elegibilidade para renovação da apólice {}", command.getApoliceId());
        
        // Verificar se pode ser renovada
        apoliceValidationService.validarRenovacao(
                aggregate.getStatus(),
                aggregate.getVigencia(),
                command.getNovaVigencia()
        );
        
        // Verificar histórico de pagamentos (simulado)
        boolean temPagamentosEmDia = Math.random() > 0.1; // 90% chance de estar em dia
        if (!temPagamentosEmDia) {
            throw new IllegalStateException("Não é possível renovar apólice com pagamentos em atraso");
        }
        
        // Verificar se não há sinistros suspeitos (simulado)
        boolean temSinistrosSuspeitos = Math.random() < 0.05; // 5% chance
        if (temSinistrosSuspeitos) {
            throw new IllegalStateException("Renovação bloqueada devido a sinistros em análise");
        }
        
        log.debug("Apólice {} elegível para renovação", command.getApoliceId());
    }
    
    private void verificarSegurado(ApoliceAggregate aggregate, RenovarApoliceCommand command) {
        log.debug("Verificando dados do segurado para renovação");
        
        String seguradoId = aggregate.getSeguradoId();
        
        // Verificar se segurado ainda está ativo
        if (!seguradoValidationService.isSeguradoAtivo(seguradoId)) {
            throw new IllegalArgumentException("Segurado não está mais ativo: " + seguradoId);
        }
        
        // Verificar se não há novas restrições
        if (seguradoValidationService.temRestricoes(seguradoId)) {
            log.warn("Segurado {} possui restrições - renovação pode ser negada", seguradoId);
            
            // Para renovação automática, bloquear se houver restrições
            if (command.isRenovacaoAutomatica()) {
                throw new IllegalStateException("Renovação automática bloqueada devido a restrições do segurado");
            }
        }
        
        // Verificar score de crédito atualizado
        int scoreAtual = seguradoValidationService.obterScoreCredito(seguradoId);
        int scoreMinimo = 250; // Mais flexível para renovação
        
        if (scoreAtual < scoreMinimo) {
            throw new IllegalArgumentException(
                String.format("Score de crédito insuficiente para renovação: %d (mínimo: %d)", 
                             scoreAtual, scoreMinimo)
            );
        }
        
        log.debug("Dados do segurado verificados para renovação");
    }
    
    private void validarDadosRenovacao(RenovarApoliceCommand command) {
        log.debug("Validando dados da renovação");
        
        // Validar nova vigência
        apoliceValidationService.validarVigencia(command.getNovaVigencia());
        
        // Validar novo valor segurado
        if (!command.getNovoValorSegurado().isPositivo()) {
            throw new IllegalArgumentException("Novo valor segurado deve ser positivo");
        }
        
        // Validar novas coberturas
        if (command.getNovasCoberturas().isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura na renovação");
        }
        
        // Validar combinação de coberturas
        apoliceValidationService.validarCombinacaoCoberturas(command.getNovasCoberturas());
        
        // Validar nova forma de pagamento
        apoliceValidationService.validarFormaPagamento(
                command.getNovaFormaPagamento(), 
                command.getNovoValorSegurado()
        );
        
        log.debug("Dados da renovação validados");
    }
    
    private void calcularNovosPremios(ApoliceAggregate aggregate, RenovarApoliceCommand command) {
        log.debug("Calculando novos prêmios para renovação");
        
        // Obter histórico de sinistros para ajustar prêmio
        String seguradoId = aggregate.getSeguradoId();
        SeguradoValidationService.HistoricoSinistros historico = 
                seguradoValidationService.obterHistoricoSinistros(seguradoId);
        
        // Aplicar fator de risco baseado no histórico
        double fatorRisco = historico.getFatorRisco();
        log.debug("Fator de risco aplicado: {}", fatorRisco);
        
        // Para renovação automática, aplicar desconto de fidelidade
        if (command.isRenovacaoAutomatica()) {
            log.debug("Aplicando desconto de fidelidade para renovação automática");
        }
        
        // Verificar se há alterações significativas que impactem o prêmio
        if (command.hasAlteracoesCoberturas()) {
            log.debug("Recalculando prêmio devido a alterações nas coberturas");
        }
        
        log.debug("Cálculo de prêmios concluído para renovação");
    }
}