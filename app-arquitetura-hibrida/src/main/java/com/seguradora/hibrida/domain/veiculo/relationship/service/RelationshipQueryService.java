package com.seguradora.hibrida.domain.veiculo.relationship.service;

import com.seguradora.hibrida.domain.veiculo.relationship.dto.DashboardRelacionamentosDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.HistoricoRelacionamentoDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.dto.VeiculoSemCoberturaDTO;
import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.VeiculoApoliceRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.repository.VeiculoApoliceRelacionamentoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serviço de consulta para relacionamentos Veículo-Apólice.
 * Fornece métodos otimizados para dashboard e relatórios.
 *
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class RelationshipQueryService {

    private final VeiculoApoliceRelacionamentoRepository relacionamentoRepository;

    /**
     * Retorna dashboard consolidado de relacionamentos.
     */
    @Cacheable(value = "dashboardRelacionamentos", unless = "#result == null")
    public DashboardRelacionamentosDTO getDashboard() {
        log.debug("Gerando dashboard de relacionamentos");

        List<VeiculoApoliceRelacionamento> todosRelacionamentos =
            relacionamentoRepository.findAll();

        long totalAtivos = todosRelacionamentos.stream()
            .filter(r -> StatusRelacionamento.ATIVO.equals(r.getStatus()))
            .count();

        long totalEncerrados = todosRelacionamentos.stream()
            .filter(r -> StatusRelacionamento.ENCERRADO.equals(r.getStatus()))
            .count();

        long totalCancelados = todosRelacionamentos.stream()
            .filter(r -> StatusRelacionamento.CANCELADO.equals(r.getStatus()))
            .count();

        List<String> veiculosSemCobertura = relacionamentoRepository.findVeiculosSemCobertura();

        LocalDate daquiA30Dias = LocalDate.now().plusDays(30);
        List<VeiculoApoliceRelacionamento> vencendoEmBreve =
            relacionamentoRepository.findRelacionamentosVencendoAte(daquiA30Dias);

        List<VeiculoApoliceRelacionamento> comGap =
            relacionamentoRepository.findRelacionamentosComGap();

        return DashboardRelacionamentosDTO.builder()
            .totalRelacionamentosAtivos(totalAtivos)
            .totalRelacionamentosEncerrados(totalEncerrados)
            .totalRelacionamentosCancelados(totalCancelados)
            .totalVeiculosSemCobertura(veiculosSemCobertura.size())
            .totalVencendoEm30Dias(vencendoEmBreve.size())
            .totalComGapCobertura(comGap.size())
            .dataAtualizacao(LocalDate.now())
            .build();
    }

    /**
     * Lista veículos sem cobertura ativa.
     */
    @Cacheable(value = "veiculosSemCobertura", unless = "#result.isEmpty()")
    public List<VeiculoSemCoberturaDTO> getVeiculosSemCobertura() {
        log.debug("Buscando veículos sem cobertura");

        List<String> idsVeiculosSemCobertura = relacionamentoRepository.findVeiculosSemCobertura();

        return idsVeiculosSemCobertura.stream()
            .map(veiculoId -> {
                // Buscar último relacionamento do veículo
                List<VeiculoApoliceRelacionamento> historico =
                    relacionamentoRepository.findByVeiculoIdOrderByDataInicioDesc(veiculoId);

                VeiculoApoliceRelacionamento ultimoRelacionamento =
                    historico.isEmpty() ? null : historico.get(0);

                if (ultimoRelacionamento == null) {
                    return null;
                }

                int diasSemCobertura = ultimoRelacionamento.getDataFim() != null
                    ? (int) java.time.temporal.ChronoUnit.DAYS.between(
                        ultimoRelacionamento.getDataFim(), LocalDate.now())
                    : 0;

                return VeiculoSemCoberturaDTO.builder()
                    .veiculoId(veiculoId)
                    .placa(ultimoRelacionamento.getVeiculoPlaca())
                    .seguradoCpf(ultimoRelacionamento.getSeguradoCpf())
                    .seguradoNome(ultimoRelacionamento.getSeguradoNome())
                    .ultimaApolice(ultimoRelacionamento.getApoliceNumero())
                    .dataFimUltimaCobertura(ultimoRelacionamento.getDataFim())
                    .diasSemCobertura(diasSemCobertura)
                    .build();
            })
            .filter(dto -> dto != null)
            .collect(Collectors.toList());
    }

    /**
     * Retorna histórico completo de relacionamentos de um veículo.
     */
    @Cacheable(value = "historicoRelacionamento", key = "#veiculoId")
    public List<HistoricoRelacionamentoDTO> getHistoricoVeiculo(String veiculoId) {
        log.debug("Buscando histórico de relacionamentos: veiculoId={}", veiculoId);

        List<VeiculoApoliceRelacionamento> historico =
            relacionamentoRepository.findByVeiculoIdOrderByDataInicioDesc(veiculoId);

        return historico.stream()
            .map(rel -> HistoricoRelacionamentoDTO.builder()
                .relacionamentoId(rel.getId())
                .apoliceId(rel.getApoliceId())
                .apoliceNumero(rel.getApoliceNumero())
                .dataInicio(rel.getDataInicio())
                .dataFim(rel.getDataFim())
                .status(rel.getStatus())
                .tipoRelacionamento(rel.getTipoRelacionamento())
                .tipoCobertura(rel.getTipoCobertura())
                .motivoDesassociacao(rel.getMotivoDesassociacao())
                .duracaoDias(rel.calcularDuracaoDias())
                .build())
            .collect(Collectors.toList());
    }

    /**
     * Retorna relacionamentos ativos de um veículo.
     */
    public List<VeiculoApoliceRelacionamento> getRelacionamentosAtivosVeiculo(String veiculoId) {
        return relacionamentoRepository.findByVeiculoIdAndStatus(
            veiculoId,
            StatusRelacionamento.ATIVO
        );
    }

    /**
     * Verifica se um veículo tem cobertura ativa.
     */
    public boolean temCoberturaAtiva(String veiculoId) {
        long count = relacionamentoRepository.countRelacionamentosAtivos(veiculoId);
        return count > 0;
    }

    /**
     * Verifica se veículo está coberto em uma data específica.
     */
    public boolean estaCoberto(String veiculoId, LocalDate data) {
        List<VeiculoApoliceRelacionamento> vigentes =
            relacionamentoRepository.findRelacionamentosVigentesEm(veiculoId, data);
        return !vigentes.isEmpty();
    }
}
