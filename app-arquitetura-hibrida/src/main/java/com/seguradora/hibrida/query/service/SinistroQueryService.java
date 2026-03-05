package com.seguradora.hibrida.query.service;

import com.seguradora.hibrida.query.dto.DashboardView;
import com.seguradora.hibrida.query.dto.SinistroDetailView;
import com.seguradora.hibrida.query.dto.SinistroFilter;
import com.seguradora.hibrida.query.dto.SinistroListView;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;
import java.util.UUID;

/**
 * Interface para serviço de consultas de sinistros no Query Side do CQRS.
 */
public interface SinistroQueryService {
    
    /**
     * Busca sinistro por ID.
     */
    Optional<SinistroDetailView> buscarPorId(UUID id);
    
    /**
     * Busca sinistro por protocolo.
     */
    Optional<SinistroDetailView> buscarPorProtocolo(String protocolo);
    
    /**
     * Lista sinistros com filtros e paginação.
     */
    Page<SinistroListView> listar(SinistroFilter filter, Pageable pageable);
    
    /**
     * Busca sinistros por CPF do segurado.
     */
    Page<SinistroListView> buscarPorCpfSegurado(String cpf, Pageable pageable);
    
    /**
     * Busca sinistros por placa do veículo.
     */
    Page<SinistroListView> buscarPorPlaca(String placa, Pageable pageable);
    
    /**
     * Busca textual em sinistros.
     */
    Page<SinistroListView> buscarPorTexto(String termo, Pageable pageable);
    
    /**
     * Busca sinistros por tag.
     */
    Page<SinistroListView> buscarPorTag(String tag, Pageable pageable);
    
    /**
     * Obtém dados para dashboard.
     */
    DashboardView obterDashboard();
}