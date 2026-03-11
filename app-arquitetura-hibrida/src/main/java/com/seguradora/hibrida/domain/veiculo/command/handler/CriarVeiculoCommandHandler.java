package com.seguradora.hibrida.domain.veiculo.command.handler;

import com.seguradora.hibrida.command.CommandHandler;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.aggregate.VeiculoAggregate;
import com.seguradora.hibrida.domain.veiculo.command.CriarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.model.*;
import com.seguradora.hibrida.aggregate.repository.AggregateRepository;
import com.seguradora.hibrida.domain.veiculo.service.VeiculoValidationService;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

/**
 * Handler para o comando de criação de veículo.
 * 
 * <p>Responsável por:
 * <ul>
 *   <li>Validar dados do comando</li>
 *   <li>Verificar unicidade de placa, RENAVAM e chassi</li>
 *   <li>Criar o aggregate VeiculoAggregate</li>
 *   <li>Persistir no repositório</li>
 *   <li>Retornar resultado com ID do veículo criado</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Component
public class CriarVeiculoCommandHandler implements CommandHandler<CriarVeiculoCommand> {
    
    private final AggregateRepository<VeiculoAggregate> veiculoRepository;
    private final VeiculoValidationService validationService;
    
    public CriarVeiculoCommandHandler(AggregateRepository<VeiculoAggregate> veiculoRepository,
                                     VeiculoValidationService validationService) {
        this.veiculoRepository = veiculoRepository;
        this.validationService = validationService;
    }
    
    @Override
    @Transactional
    public CommandResult handle(CriarVeiculoCommand command) {
        try {
            // Gerar ID único para o veículo
            String veiculoId = UUID.randomUUID().toString();
            
            // Validar unicidade
            validationService.validarUnicidade(command.getPlaca(), command.getRenavam(), command.getChassi());
            
            // Criar objetos de valor usando métodos estáticos
            Especificacao especificacao = Especificacao.of(
                command.getCor(),
                command.getTipoCombustivel(),
                command.getCategoria(),
                command.getCilindrada()
            );
            
            Proprietario proprietario = Proprietario.of(
                command.getProprietarioCpfCnpj(),
                command.getProprietarioNome(),
                command.getProprietarioTipo()
            );
            
            // Criar aggregate
            VeiculoAggregate veiculo = VeiculoAggregate.criarVeiculo(
                veiculoId,
                command.getPlaca(),
                command.getRenavam(),
                command.getChassi(),
                command.getMarca(),
                command.getModelo(),
                command.getAnoFabricacao(),
                command.getAnoModelo(),
                especificacao,
                proprietario,
                command.getOperadorId()
            );
            
            // Salvar no repositório
            veiculoRepository.save(veiculo);
            
            // Retornar resultado de sucesso
            return CommandResult.success(veiculoId, Map.of(
                "placa", command.getPlaca(),
                "marca", command.getMarca(),
                "modelo", command.getModelo(),
                "version", 1L
            )).withCorrelationId(command.getCorrelationId());
            
        } catch (Exception e) {
            return CommandResult.failure(e)
                .withCorrelationId(command.getCorrelationId())
                .withMetadata("command", command.getClass().getSimpleName());
        }
    }
    
    @Override
    public Class<CriarVeiculoCommand> getCommandType() {
        return CriarVeiculoCommand.class;
    }
    
    @Override
    public int getTimeoutSeconds() {
        return 30;
    }
}