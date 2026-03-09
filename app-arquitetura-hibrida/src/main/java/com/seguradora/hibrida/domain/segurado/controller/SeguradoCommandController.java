package com.seguradora.hibrida.domain.segurado.controller;

import com.seguradora.hibrida.command.CommandBus;
import com.seguradora.hibrida.command.CommandResult;
import com.seguradora.hibrida.domain.segurado.command.*;
import com.seguradora.hibrida.domain.segurado.controller.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de comando (write) em Segurado.
 * 
 * <p>Este controller expõe endpoints para operações de escrita seguindo
 * o padrão CQRS, delegando a execução para o Command Bus.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/segurados")
@RequiredArgsConstructor
public class SeguradoCommandController {
    
    private final CommandBus commandBus;
    
    /**
     * Cria um novo segurado.
     * 
     * @param request Dados do novo segurado
     * @return ResponseEntity com ID do segurado criado
     */
    @PostMapping
    public ResponseEntity<CommandResponseDTO> criarSegurado(@Valid @RequestBody CriarSeguradoRequestDTO request) {
        log.info("Requisição para criar segurado: CPF={}", request.getCpf());
        
        CriarSeguradoCommand command = CriarSeguradoCommand.builder()
                .cpf(request.getCpf())
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .dataNascimento(request.getDataNascimento())
                .endereco(request.getEndereco())
                .build();
        
        CommandResult result = commandBus.send(command);
        
        if (result.isSuccess()) {
            log.info("Segurado criado com sucesso: ID={}", result.getData());
            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(new CommandResponseDTO(true, "Segurado criado com sucesso", result.getData()));
        } else {
            log.error("Erro ao criar segurado: {}", result.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CommandResponseDTO(false, result.getErrorMessage(), null));
        }
    }
    
    /**
     * Atualiza dados de um segurado existente.
     * 
     * @param id ID do segurado
     * @param request Dados atualizados
     * @return ResponseEntity com resultado da operação
     */
    @PutMapping("/{id}")
    public ResponseEntity<CommandResponseDTO> atualizarSegurado(
            @PathVariable String id,
            @Valid @RequestBody AtualizarSeguradoRequestDTO request) {
        log.info("Requisição para atualizar segurado: ID={}", id);
        
        AtualizarSeguradoCommand command = AtualizarSeguradoCommand.builder()
                .seguradoId(id)
                .nome(request.getNome())
                .email(request.getEmail())
                .telefone(request.getTelefone())
                .dataNascimento(request.getDataNascimento())
                .endereco(request.getEndereco())
                .build();
        
        CommandResult result = commandBus.send(command);
        
        if (result.isSuccess()) {
            log.info("Segurado atualizado com sucesso: ID={}", id);
            return ResponseEntity.ok(new CommandResponseDTO(true, "Segurado atualizado com sucesso", id));
        } else {
            log.error("Erro ao atualizar segurado ID {}: {}", id, result.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CommandResponseDTO(false, result.getErrorMessage(), null));
        }
    }
    
    /**
     * Desativa um segurado.
     * 
     * @param id ID do segurado
     * @param request Motivo da desativação
     * @return ResponseEntity com resultado da operação
     */
    @PostMapping("/{id}/desativar")
    public ResponseEntity<CommandResponseDTO> desativarSegurado(
            @PathVariable String id,
            @Valid @RequestBody DesativarSeguradoRequestDTO request) {
        log.info("Requisição para desativar segurado: ID={}", id);
        
        DesativarSeguradoCommand command = DesativarSeguradoCommand.builder()
                .seguradoId(id)
                .motivo(request.getMotivo())
                .build();
        
        CommandResult result = commandBus.send(command);
        
        if (result.isSuccess()) {
            log.info("Segurado desativado com sucesso: ID={}", id);
            return ResponseEntity.ok(new CommandResponseDTO(true, "Segurado desativado com sucesso", id));
        } else {
            log.error("Erro ao desativar segurado ID {}: {}", id, result.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CommandResponseDTO(false, result.getErrorMessage(), null));
        }
    }
    
    /**
     * Reativa um segurado.
     * 
     * @param id ID do segurado
     * @param request Motivo da reativação
     * @return ResponseEntity com resultado da operação
     */
    @PostMapping("/{id}/reativar")
    public ResponseEntity<CommandResponseDTO> reativarSegurado(
            @PathVariable String id,
            @Valid @RequestBody ReativarSeguradoRequestDTO request) {
        log.info("Requisição para reativar segurado: ID={}", id);
        
        ReativarSeguradoCommand command = ReativarSeguradoCommand.builder()
                .seguradoId(id)
                .motivo(request.getMotivo())
                .build();
        
        CommandResult result = commandBus.send(command);
        
        if (result.isSuccess()) {
            log.info("Segurado reativado com sucesso: ID={}", id);
            return ResponseEntity.ok(new CommandResponseDTO(true, "Segurado reativado com sucesso", id));
        } else {
            log.error("Erro ao reativar segurado ID {}: {}", id, result.getErrorMessage());
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(new CommandResponseDTO(false, result.getErrorMessage(), null));
        }
    }
}
