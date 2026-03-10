package com.seguradora.hibrida.domain.veiculo.controller;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.veiculo.command.AssociarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.command.AtualizarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.command.CriarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.command.DesassociarVeiculoCommand;
import com.seguradora.hibrida.domain.veiculo.controller.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

/**
 * Controller REST para comandos de veículos.
 * 
 * <p>Fornece endpoints para operações de escrita no domínio de veículos,
 * incluindo criação, atualização, associação e desassociação de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@RestController
@RequestMapping("/api/v1/veiculos")
@Tag(name = "Veículos - Comandos", description = "APIs para operações de escrita em veículos")
public class VeiculoCommandController {
    
    private static final Logger log = LoggerFactory.getLogger(VeiculoCommandController.class);
    
    private final CommandBus commandBus;
    
    public VeiculoCommandController(CommandBus commandBus) {
        this.commandBus = commandBus;
    }
    
    @PostMapping
    @Operation(summary = "Criar novo veículo", description = "Cria um novo veículo no sistema com validações completas")
    @ApiResponses({
        @ApiResponse(responseCode = "201", description = "Veículo criado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "409", description = "Placa, RENAVAM ou chassi já existem")
    })
    public ResponseEntity<CommandResponseDTO> criarVeiculo(
            @Valid @RequestBody CriarVeiculoRequestDTO request) {
        
        log.info("Criando novo veículo - placa: {}, marca: {}, modelo: {}", 
                request.placa(), request.marca(), request.modelo());
        
        try {
            CriarVeiculoCommand command = CriarVeiculoCommand.builder()
                .placa(request.placa())
                .renavam(request.renavam())
                .chassi(request.chassi())
                .marca(request.marca())
                .modelo(request.modelo())
                .anoFabricacao(request.anoFabricacao())
                .anoModelo(request.anoModelo())
                .cor(request.especificacao().getCor())
                .tipoCombustivel(request.especificacao().getTipoCombustivel())
                .categoria(request.especificacao().getCategoria())
                .cilindrada(request.especificacao().getCilindrada())
                .proprietarioCpfCnpj(request.proprietario().getCpfCnpj())
                .proprietarioNome(request.proprietario().getNome())
                .proprietarioTipo(request.proprietario().getTipoPessoa())
                .operadorId(request.operadorId())
                .correlationId(UUID.randomUUID())
                .userId(request.operadorId())
                .build();
            
            CommandResult result = commandBus.send(command);
            
            if (result.isSuccess()) {
                log.info("Veículo criado com sucesso - dados: {}", result.getData());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    result.getData() != null ? result.getData().toString() : null,
                    "Veículo criado com sucesso",
                    null, // version não disponível no CommandResult atual
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.status(HttpStatus.CREATED).body(response);
            } else {
                log.warn("Falha ao criar veículo - erro: {}", result.getErrorMessage());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    null,
                    "Falha ao criar veículo: " + result.getErrorMessage(),
                    null,
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar criação de veículo", e);
            
            CommandResponseDTO response = new CommandResponseDTO(
                null,
                "Erro interno ao criar veículo: " + e.getMessage(),
                null,
                java.time.Instant.now(),
                Map.of("exception", e.getClass().getSimpleName())
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PutMapping("/{id}")
    @Operation(summary = "Atualizar veículo", description = "Atualiza as especificações de um veículo existente")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Veículo atualizado com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Conflito de versão")
    })
    public ResponseEntity<CommandResponseDTO> atualizarVeiculo(
            @Parameter(description = "ID do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @Valid @RequestBody AtualizarVeiculoRequestDTO request) {
        
        log.info("Atualizando veículo - ID: {}, operador: {}", id, request.operadorId());
        
        try {
            AtualizarVeiculoCommand command = new AtualizarVeiculoCommand(
                id,
                request.especificacao(),
                request.operadorId(),
                request.versaoEsperada(),
                UUID.randomUUID(),
                request.operadorId()
            );
            
            CommandResult result = commandBus.send(command);
            
            if (result.isSuccess()) {
                log.info("Veículo atualizado com sucesso - ID: {}", id);
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Veículo atualizado com sucesso",
                    null,
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Falha ao atualizar veículo - ID: {}, erro: {}", id, result.getErrorMessage());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Falha ao atualizar veículo: " + result.getErrorMessage(),
                    null,
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar atualização de veículo - ID: {}", id, e);
            
            CommandResponseDTO response = new CommandResponseDTO(
                id,
                "Erro interno ao atualizar veículo: " + e.getMessage(),
                null,
                java.time.Instant.now(),
                Map.of("exception", e.getClass().getSimpleName())
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/{id}/associar-apolice")
    @Operation(summary = "Associar veículo à apólice", description = "Associa um veículo a uma apólice de seguro")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Associação realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Veículo já associado à apólice")
    })
    public ResponseEntity<CommandResponseDTO> associarApolice(
            @Parameter(description = "ID do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @Valid @RequestBody AssociarVeiculoRequestDTO request) {
        
        log.info("Associando veículo à apólice - veículo: {}, apólice: {}", id, request.apoliceId());
        
        try {
            AssociarVeiculoCommand command = new AssociarVeiculoCommand(
                id,
                request.apoliceId(),
                request.dataInicio(),
                request.operadorId(),
                UUID.randomUUID(),
                request.operadorId()
            );
            
            CommandResult result = commandBus.send(command);
            
            if (result.isSuccess()) {
                log.info("Veículo associado com sucesso - veículo: {}, apólice: {}", 
                        id, request.apoliceId());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Veículo associado à apólice com sucesso",
                    null,
                    result.getExecutedAt(),
                    Map.of("apoliceId", request.apoliceId())
                );
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Falha ao associar veículo - veículo: {}, erro: {}", id, result.getErrorMessage());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Falha ao associar veículo: " + result.getErrorMessage(),
                    null,
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar associação de veículo - ID: {}", id, e);
            
            CommandResponseDTO response = new CommandResponseDTO(
                id,
                "Erro interno ao associar veículo: " + e.getMessage(),
                null,
                java.time.Instant.now(),
                Map.of("exception", e.getClass().getSimpleName())
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    @PostMapping("/{id}/desassociar-apolice")
    @Operation(summary = "Desassociar veículo da apólice", description = "Remove a associação entre um veículo e uma apólice")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Desassociação realizada com sucesso"),
        @ApiResponse(responseCode = "400", description = "Dados inválidos"),
        @ApiResponse(responseCode = "404", description = "Veículo não encontrado"),
        @ApiResponse(responseCode = "409", description = "Veículo não está associado à apólice")
    })
    public ResponseEntity<CommandResponseDTO> desassociarApolice(
            @Parameter(description = "ID do veículo", example = "123e4567-e89b-12d3-a456-426614174000")
            @PathVariable String id,
            @Valid @RequestBody DesassociarVeiculoRequestDTO request) {
        
        log.info("Desassociando veículo da apólice - veículo: {}, apólice: {}", id, request.apoliceId());
        
        try {
            DesassociarVeiculoCommand command = new DesassociarVeiculoCommand(
                id,
                request.apoliceId(),
                request.dataFim(),
                request.motivo(),
                request.operadorId(),
                UUID.randomUUID(),
                request.operadorId()
            );
            
            CommandResult result = commandBus.send(command);
            
            if (result.isSuccess()) {
                log.info("Veículo desassociado com sucesso - veículo: {}, apólice: {}", 
                        id, request.apoliceId());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Veículo desassociado da apólice com sucesso",
                    null,
                    result.getExecutedAt(),
                    Map.of("apoliceId", request.apoliceId(), "motivo", request.motivo())
                );
                
                return ResponseEntity.ok(response);
            } else {
                log.warn("Falha ao desassociar veículo - veículo: {}, erro: {}", id, result.getErrorMessage());
                
                CommandResponseDTO response = new CommandResponseDTO(
                    id,
                    "Falha ao desassociar veículo: " + result.getErrorMessage(),
                    null,
                    result.getExecutedAt(),
                    result.getMetadata()
                );
                
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("Erro ao processar desassociação de veículo - ID: {}", id, e);
            
            CommandResponseDTO response = new CommandResponseDTO(
                id,
                "Erro interno ao desassociar veículo: " + e.getMessage(),
                null,
                java.time.Instant.now(),
                Map.of("exception", e.getClass().getSimpleName())
            );
            
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
        }
    }
    
    // === HEALTH CHECK ===
    
    @GetMapping("/commands/health")
    @Operation(summary = "Health check dos comandos", description = "Verifica a saúde do serviço de comandos de veículos")
    public ResponseEntity<Map<String, Object>> healthCheck() {
        log.debug("Executando health check dos comandos de veículos");
        
        try {
            Map<String, Object> health = Map.of(
                "status", "UP",
                "service", "VeiculoCommandController",
                "timestamp", java.time.Instant.now(),
                "commandBus", commandBus != null ? "AVAILABLE" : "UNAVAILABLE"
            );
            
            return ResponseEntity.ok(health);
            
        } catch (Exception e) {
            log.error("Erro no health check dos comandos de veículos", e);
            
            Map<String, Object> health = Map.of(
                "status", "DOWN",
                "service", "VeiculoCommandController",
                "timestamp", java.time.Instant.now(),
                "error", e.getMessage()
            );
            
            return ResponseEntity.status(503).body(health);
        }
    }
}