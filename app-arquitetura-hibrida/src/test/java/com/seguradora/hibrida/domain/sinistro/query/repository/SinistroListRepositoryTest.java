package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroListView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinistroListRepository Tests")
class SinistroListRepositoryTest {

    private final SinistroListRepository repository = mock(SinistroListRepository.class);

    @Test
    @DisplayName("findByProtocolo deve retornar sinistro quando encontrado")
    void findByProtocoloShouldReturnWhenFound() {
        SinistroListView view = SinistroListView.builder()
                .protocolo("SIN-2024-001")
                .seguradoNome("João Silva")
                .build();
        when(repository.findByProtocolo("SIN-2024-001")).thenReturn(Optional.of(view));

        Optional<SinistroListView> result = repository.findByProtocolo("SIN-2024-001");

        assertThat(result).isPresent();
        assertThat(result.get().getProtocolo()).isEqualTo("SIN-2024-001");
        verify(repository).findByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("findByProtocolo deve retornar empty quando não encontrado")
    void findByProtocoloShouldReturnEmptyWhenNotFound() {
        when(repository.findByProtocolo("SIN-INEXISTENTE")).thenReturn(Optional.empty());

        Optional<SinistroListView> result = repository.findByProtocolo("SIN-INEXISTENTE");

        assertThat(result).isEmpty();
        verify(repository).findByProtocolo("SIN-INEXISTENTE");
    }

    @Test
    @DisplayName("existsByProtocolo deve retornar true quando protocolo existe")
    void existsByProtocoloShouldReturnTrue() {
        when(repository.existsByProtocolo("SIN-2024-001")).thenReturn(true);

        boolean result = repository.existsByProtocolo("SIN-2024-001");

        assertThat(result).isTrue();
        verify(repository).existsByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("existsByProtocolo deve retornar false quando protocolo não existe")
    void existsByProtocoloShouldReturnFalse() {
        when(repository.existsByProtocolo("SIN-INEXISTENTE")).thenReturn(false);

        boolean result = repository.existsByProtocolo("SIN-INEXISTENTE");

        assertThat(result).isFalse();
        verify(repository).existsByProtocolo("SIN-INEXISTENTE");
    }

    @Test
    @DisplayName("findByStatusOrderByDataOcorrenciaDesc deve retornar página de sinistros")
    void findByStatusShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = mock(Page.class);
        when(repository.findByStatusOrderByDataOcorrenciaDesc("ABERTO", pageable)).thenReturn(page);

        Page<SinistroListView> result = repository.findByStatusOrderByDataOcorrenciaDesc("ABERTO", pageable);

        assertThat(result).isNotNull();
        verify(repository).findByStatusOrderByDataOcorrenciaDesc("ABERTO", pageable);
    }

    @Test
    @DisplayName("findBySeguradoCpfOrderByDataOcorrenciaDesc deve retornar página de sinistros")
    void findBySeguradoCpfShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = mock(Page.class);
        when(repository.findBySeguradoCpfOrderByDataOcorrenciaDesc("12345678901", pageable)).thenReturn(page);

        Page<SinistroListView> result = repository.findBySeguradoCpfOrderByDataOcorrenciaDesc("12345678901", pageable);

        assertThat(result).isNotNull();
        verify(repository).findBySeguradoCpfOrderByDataOcorrenciaDesc("12345678901", pageable);
    }

    @Test
    @DisplayName("countBySeguradoCpf deve retornar contagem de sinistros do segurado")
    void countBySeguradoCpfShouldReturnCount() {
        when(repository.countBySeguradoCpf("12345678901")).thenReturn(4L);

        long count = repository.countBySeguradoCpf("12345678901");

        assertThat(count).isEqualTo(4L);
        verify(repository).countBySeguradoCpf("12345678901");
    }

    @Test
    @DisplayName("findByVeiculoPlacaOrderByDataOcorrenciaDesc deve retornar página de sinistros")
    void findByVeiculoPlacaShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = mock(Page.class);
        when(repository.findByVeiculoPlacaOrderByDataOcorrenciaDesc("ABC1234", pageable)).thenReturn(page);

        Page<SinistroListView> result = repository.findByVeiculoPlacaOrderByDataOcorrenciaDesc("ABC1234", pageable);

        assertThat(result).isNotNull();
        verify(repository).findByVeiculoPlacaOrderByDataOcorrenciaDesc("ABC1234", pageable);
    }

    @Test
    @DisplayName("countByVeiculoPlaca deve retornar contagem de sinistros do veículo")
    void countByVeiculoPlacaShouldReturnCount() {
        when(repository.countByVeiculoPlaca("ABC1234")).thenReturn(2L);

        long count = repository.countByVeiculoPlaca("ABC1234");

        assertThat(count).isEqualTo(2L);
        verify(repository).countByVeiculoPlaca("ABC1234");
    }

    @Test
    @DisplayName("countByAnalistaResponsavel deve retornar contagem de sinistros do analista")
    void countByAnalistaResponsavelShouldReturnCount() {
        when(repository.countByAnalistaResponsavel("Analista01")).thenReturn(7L);

        long count = repository.countByAnalistaResponsavel("Analista01");

        assertThat(count).isEqualTo(7L);
        verify(repository).countByAnalistaResponsavel("Analista01");
    }

    @Test
    @DisplayName("countByTipo deve retornar contagem de sinistros por tipo")
    void countByTipoShouldReturnCount() {
        when(repository.countByTipo("COLISAO")).thenReturn(10L);

        long count = repository.countByTipo("COLISAO");

        assertThat(count).isEqualTo(10L);
        verify(repository).countByTipo("COLISAO");
    }

    @Test
    @DisplayName("findSinistrosUrgentes deve retornar página de sinistros urgentes")
    void findSinistrosUrgentesShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = mock(Page.class);
        when(repository.findSinistrosUrgentes(pageable)).thenReturn(page);

        Page<SinistroListView> result = repository.findSinistrosUrgentes(pageable);

        assertThat(result).isNotNull();
        verify(repository).findSinistrosUrgentes(pageable);
    }

    @Test
    @DisplayName("findSinistrosForaSla deve retornar página de sinistros fora do SLA")
    void findSinistrosForaSlaShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroListView> page = mock(Page.class);
        when(repository.findSinistrosForaSla(pageable)).thenReturn(page);

        Page<SinistroListView> result = repository.findSinistrosForaSla(pageable);

        assertThat(result).isNotNull();
        verify(repository).findSinistrosForaSla(pageable);
    }
}
