package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.AtualizarVeiculoCommand;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.eventstore.exception.ConcurrencyException;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;

/**
 * Handler para o comando de atualização de veículo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class AtualizarVeiculoCommandHandler implements CommandHandler<AtualizarVeiculoCommand> {
    
    private final AggregateRepository<VeiculoAggregate> veiculoRepository;
    
    public AtualizarVeiculoCommandHandler(AggregateRepository<VeiculoAggregate> veiculoRepository) {
        this.veiculoRepository = veiculoRepository;
    }
    
    @Override
    @Transactional
    public CommandResult handle(AtualizarVeiculoCommand command) {
        try {
            // Carregar aggregate do veículo
            VeiculoAggregate veiculo = veiculoRepository.getById(command.getVeiculoId());
            
            // Verificar versão esperada para controle de concorrência
            if (command.getVersaoEsperada() != null && 
                !command.getVersaoEsperada().equals(veiculo.getVersion())) {
                throw new ConcurrencyException(
                    command.getVeiculoId(), 
                    command.getVersaoEsperada(), 
                    veiculo.getVersion()
                );
            }
            
            // Atualizar especificações
            veiculo.atualizarEspecificacoes(
                command.getNovaEspecificacao(),
                command.getOperadorId()
            );
            
            // Salvar alterações
            veiculoRepository.save(veiculo);
            
            // Retornar resultado de sucesso
            return CommandResult.success(command.getVeiculoId(), Map.of(
                "novaVersao", veiculo.getVersion(),
                "especificacao", command.getNovaEspecificacao()
            )).withCorrelationId(command.getCorrelationId());
            
        } catch (Exception e) {
            return CommandResult.failure(e)
                .withCorrelationId(command.getCorrelationId())
                .withMetadata("veiculoId", command.getVeiculoId());
        }
    }
    
    @Override
    public Class<AtualizarVeiculoCommand> getCommandType() {
        return AtualizarVeiculoCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 20;
    }
}