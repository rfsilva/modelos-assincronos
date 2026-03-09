package com.seguradora.hibrida.domain.segurado.controller;

import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoQueryModel;
import com.seguradora.hibrida.domain.segurado.query.model.SeguradoListView;
import com.seguradora.hibrida.domain.segurado.query.service.SeguradoQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller REST para operações de consulta (read) em Segurado.
 * 
 * <p>Este controller expõe endpoints para operações de leitura seguindo
 * o padrão CQRS, com cache Redis para otimização de performance.</p>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@RestController
@RequestMapping("/api/v1/segurados")
@RequiredArgsConstructor
public class SeguradoQueryController {
    
    private final SeguradoQueryService queryService;
    
    /**
     * Busca segurado por ID.
     * 
     * @param id ID do segurado
     * @return ResponseEntity com o segurado encontrado
     */
    @GetMapping("/{id}")
    public ResponseEntity<SeguradoQueryModel> buscarPorId(@PathVariable String id) {
        log.info("Requisição para buscar segurado por ID: {}", id);
        
        return queryService.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca segurado por CPF.
     * 
     * @param cpf CPF do segurado
     * @return ResponseEntity com o segurado encontrado
     */
    @GetMapping("/cpf/{cpf}")
    public ResponseEntity<SeguradoQueryModel> buscarPorCpf(@PathVariable String cpf) {
        log.info("Requisição para buscar segurado por CPF: {}", cpf);
        
        return queryService.findByCpf(cpf)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Busca segurado por email.
     * 
     * @param email Email do segurado
     * @return ResponseEntity com o segurado encontrado
     */
    @GetMapping("/email/{email}")
    public ResponseEntity<SeguradoQueryModel> buscarPorEmail(@PathVariable String email) {
        log.info("Requisição para buscar segurado por email: {}", email);
        
        return queryService.findByEmail(email)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
    
    /**
     * Lista todos os segurados com paginação.
     * 
     * @param pageable Configuração de paginação
     * @return ResponseEntity com página de segurados
     */
    @GetMapping
    public ResponseEntity<Page<SeguradoListView>> listarTodos(
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Requisição para listar todos os segurados - Página: {}", pageable.getPageNumber());
        
        Page<SeguradoListView> page = queryService.findAll(pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * Lista segurados por status.
     * 
     * @param status Status do segurado
     * @param pageable Configuração de paginação
     * @return ResponseEntity com página de segurados
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<Page<SeguradoListView>> listarPorStatus(
            @PathVariable StatusSegurado status,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Requisição para listar segurados por status: {} - Página: {}", status, pageable.getPageNumber());
        
        Page<SeguradoListView> page = queryService.findByStatus(status, pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * Busca segurados por nome (busca parcial).
     * 
     * @param nome Nome ou parte do nome
     * @param pageable Configuração de paginação
     * @return ResponseEntity com página de segurados
     */
    @GetMapping("/buscar/nome")
    public ResponseEntity<Page<SeguradoListView>> buscarPorNome(
            @RequestParam String nome,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Requisição para buscar segurados por nome: {} - Página: {}", nome, pageable.getPageNumber());
        
        Page<SeguradoListView> page = queryService.findByNome(nome, pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * Busca segurados por cidade.
     * 
     * @param cidade Cidade do segurado
     * @param pageable Configuração de paginação
     * @return ResponseEntity com página de segurados
     */
    @GetMapping("/cidade/{cidade}")
    public ResponseEntity<Page<SeguradoListView>> buscarPorCidade(
            @PathVariable String cidade,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Requisição para buscar segurados por cidade: {} - Página: {}", cidade, pageable.getPageNumber());
        
        Page<SeguradoListView> page = queryService.findByCidade(cidade, pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * Busca segurados por estado.
     * 
     * @param estado Sigla do estado (UF)
     * @param pageable Configuração de paginação
     * @return ResponseEntity com página de segurados
     */
    @GetMapping("/estado/{estado}")
    public ResponseEntity<Page<SeguradoListView>> buscarPorEstado(
            @PathVariable String estado,
            @PageableDefault(size = 20, sort = "nome", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("Requisição para buscar segurados por estado: {} - Página: {}", estado, pageable.getPageNumber());
        
        Page<SeguradoListView> page = queryService.findByEstado(estado, pageable);
        return ResponseEntity.ok(page);
    }
    
    /**
     * Verifica se existe segurado com o CPF informado.
     * 
     * @param cpf CPF do segurado
     * @return ResponseEntity com resultado da verificação
     */
    @GetMapping("/existe/cpf/{cpf}")
    public ResponseEntity<Boolean> existePorCpf(@PathVariable String cpf) {
        log.info("Requisição para verificar existência de segurado com CPF: {}", cpf);
        
        boolean existe = queryService.existsByCpf(cpf);
        return ResponseEntity.ok(existe);
    }
    
    /**
     * Verifica se existe segurado com o email informado.
     * 
     * @param email Email do segurado
     * @return ResponseEntity com resultado da verificação
     */
    @GetMapping("/existe/email/{email}")
    public ResponseEntity<Boolean> existePorEmail(@PathVariable String email) {
        log.info("Requisição para verificar existência de segurado com email: {}", email);
        
        boolean existe = queryService.existsByEmail(email);
        return ResponseEntity.ok(existe);
    }
    
    /**
     * Conta segurados por status.
     * 
     * @param status Status do segurado
     * @return ResponseEntity com a contagem
     */
    @GetMapping("/contar/status/{status}")
    public ResponseEntity<Long> contarPorStatus(@PathVariable StatusSegurado status) {
        log.info("Requisição para contar segurados por status: {}", status);
        
        long count = queryService.countByStatus(status);
        return ResponseEntity.ok(count);
    }
}