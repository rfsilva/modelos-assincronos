package com.seguradora.hibrida.domain.sinistro.query.service;

import com.seguradora.hibrida.domain.sinistro.query.dto.DashboardView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroDetailView;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroFilter;
import com.seguradora.hibrida.domain.sinistro.query.dto.SinistroListView;
import com.seguradora.hibrida.domain.sinistro.query.model.SinistroQueryModel;
import com.seguradora.hibrida.domain.sinistro.query.repository.SinistroQueryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("SinistroQueryServiceImpl Tests")
class SinistroQueryServiceImplTest {

    @Mock
    private SinistroQueryRepository repository;

    private SinistroQueryServiceImpl service;

    private SinistroQueryModel model;

    @BeforeEach
    void setUp() {
        service = new SinistroQueryServiceImpl(repository);

        UUID id = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
        model = new SinistroQueryModel(id, "SIN-2024-001");
        model.setCpfSegurado("12345678901");
        model.setNomeSegurado("João Silva");
        model.setPlaca("ABC1234");
        model.setTipoSinistro("COLISAO");
        model.setStatus("ABERTO");
        model.setDataOcorrencia(java.time.Instant.parse("2024-06-15T10:00:00Z"));
        model.setDataAbertura(java.time.Instant.parse("2024-06-15T11:00:00Z"));
        model.setApoliceNumero("AP-2024-001");
    }

    @Test
    @DisplayName("buscarPorId deve retornar SinistroDetailView quando encontrado")
    void buscarPorIdShouldReturnDetailViewWhenFound() {
        when(repository.findById(model.getId())).thenReturn(Optional.of(model));

        Optional<SinistroDetailView> result = service.buscarPorId(model.getId());

        assertThat(result).isPresent();
        assertThat(result.get().protocolo()).isEqualTo("SIN-2024-001");
        verify(repository).findById(model.getId());
    }

    @Test
    @DisplayName("buscarPorId deve retornar empty quando não encontrado")
    void buscarPorIdShouldReturnEmptyWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        Optional<SinistroDetailView> result = service.buscarPorId(id);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("buscarPorProtocolo deve retornar SinistroDetailView quando encontrado")
    void buscarPorProtocoloShouldReturnDetailViewWhenFound() {
        when(repository.findByProtocolo("SIN-2024-001")).thenReturn(Optional.of(model));

        Optional<SinistroDetailView> result = service.buscarPorProtocolo("SIN-2024-001");

        assertThat(result).isPresent();
        assertThat(result.get().protocolo()).isEqualTo("SIN-2024-001");
        verify(repository).findByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("buscarPorProtocolo deve retornar empty quando não encontrado")
    void buscarPorProtocoloShouldReturnEmptyWhenNotFound() {
        when(repository.findByProtocolo("SIN-INEXISTENTE")).thenReturn(Optional.empty());

        Optional<SinistroDetailView> result = service.buscarPorProtocolo("SIN-INEXISTENTE");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("listar deve retornar Page de SinistroListView")
    void listarShouldReturnPage() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        Page<SinistroListView> result = service.listar(SinistroFilter.empty(), PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).protocolo()).isEqualTo("SIN-2024-001");
    }

    @Test
    @DisplayName("listar com filtro de status deve aplicar specification")
    void listarWithStatusFilterShouldApplySpec() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        SinistroFilter filter = SinistroFilter.porStatus("ABERTO");
        Page<SinistroListView> result = service.listar(filter, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }

    @Test
    @DisplayName("buscarPorCpfSegurado deve retornar Page de SinistroListView")
    void buscarPorCpfSeguradoShouldReturnPage() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SinistroListView> result = service.buscarPorCpfSegurado("12345678901", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorPlaca deve retornar Page de SinistroListView")
    void buscarPorPlacaShouldReturnPage() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SinistroListView> result = service.buscarPorPlaca("ABC1234", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    @DisplayName("buscarPorTexto deve retornar Page de SinistroListView")
    void buscarPorTextoShouldReturnPage() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SinistroListView> result = service.buscarPorTexto("João", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("buscarPorTag deve retornar Page de SinistroListView")
    void buscarPorTagShouldReturnPage() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of(model));
        when(repository.findAll(any(PageRequest.class))).thenReturn(page);

        Page<SinistroListView> result = service.buscarPorTag("URGENTE", PageRequest.of(0, 10));

        assertThat(result).isNotNull();
    }

    @Test
    @DisplayName("obterDashboard deve retornar DashboardView com totalSinistros")
    void obterDashboardShouldReturnDashboardView() {
        when(repository.count()).thenReturn(42L);

        DashboardView dashboard = service.obterDashboard();

        assertThat(dashboard).isNotNull();
        assertThat(dashboard.totalSinistros()).isEqualTo(42L);
        verify(repository).count();
    }

    @Test
    @DisplayName("listar com todos os filtros preenchidos deve construir specification completa")
    void listarWithAllFiltersBuildsFullSpec() {
        Page<SinistroQueryModel> page = new PageImpl<>(List.of());
        when(repository.findAll(any(Specification.class), any(PageRequest.class))).thenReturn(page);

        SinistroFilter filter = new SinistroFilter(
                "ABERTO", "COLISAO", "Analista01",
                java.time.Instant.parse("2024-01-01T00:00:00Z"),
                java.time.Instant.parse("2024-12-31T23:59:59Z"),
                "12345678901", "ABC1234", null, null, null, null, null
        );

        Page<SinistroListView> result = service.listar(filter, PageRequest.of(0, 10));

        assertThat(result).isNotNull();
        verify(repository).findAll(any(Specification.class), any(PageRequest.class));
    }
}
