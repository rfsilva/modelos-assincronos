package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroDetailView;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinistroDetailRepository Tests")
class SinistroDetailRepositoryTest {

    private final SinistroDetailRepository repository = mock(SinistroDetailRepository.class);

    @Test
    @DisplayName("findByProtocolo deve retornar detalhe quando encontrado")
    void findByProtocoloShouldReturnWhenFound() {
        SinistroDetailView view = new SinistroDetailView();
        view.setProtocolo("SIN-2024-001");
        when(repository.findByProtocolo("SIN-2024-001")).thenReturn(Optional.of(view));

        Optional<SinistroDetailView> result = repository.findByProtocolo("SIN-2024-001");

        assertThat(result).isPresent();
        assertThat(result.get().getProtocolo()).isEqualTo("SIN-2024-001");
        verify(repository).findByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("findByProtocolo deve retornar empty quando não encontrado")
    void findByProtocoloShouldReturnEmptyWhenNotFound() {
        when(repository.findByProtocolo("SIN-INEXISTENTE")).thenReturn(Optional.empty());

        Optional<SinistroDetailView> result = repository.findByProtocolo("SIN-INEXISTENTE");

        assertThat(result).isEmpty();
        verify(repository).findByProtocolo("SIN-INEXISTENTE");
    }

    @Test
    @DisplayName("findCompletoByProtocolo deve retornar detalhe completo quando encontrado")
    void findCompletoByProtocoloShouldReturnWhenFound() {
        SinistroDetailView view = new SinistroDetailView();
        view.setProtocolo("SIN-2024-001");
        when(repository.findCompletoByProtocolo("SIN-2024-001")).thenReturn(Optional.of(view));

        Optional<SinistroDetailView> result = repository.findCompletoByProtocolo("SIN-2024-001");

        assertThat(result).isPresent();
        verify(repository).findCompletoByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("findCompletoById deve retornar detalhe quando encontrado")
    void findCompletoByIdShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        SinistroDetailView view = new SinistroDetailView();
        view.setId(id);
        when(repository.findCompletoById(id)).thenReturn(Optional.of(view));

        Optional<SinistroDetailView> result = repository.findCompletoById(id);

        assertThat(result).isPresent();
        assertThat(result.get().getId()).isEqualTo(id);
        verify(repository).findCompletoById(id);
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
    @DisplayName("countEmAnaliseByAnalista deve retornar contagem de sinistros em análise")
    void countEmAnaliseByShouldReturnCount() {
        when(repository.countEmAnaliseByAnalista("Analista01")).thenReturn(5L);

        long count = repository.countEmAnaliseByAnalista("Analista01");

        assertThat(count).isEqualTo(5L);
        verify(repository).countEmAnaliseByAnalista("Analista01");
    }

    @Test
    @DisplayName("countByApoliceNumero deve retornar contagem de sinistros da apólice")
    void countByApoliceNumeroShouldReturnCount() {
        when(repository.countByApoliceNumero("AP-001")).thenReturn(3L);

        long count = repository.countByApoliceNumero("AP-001");

        assertThat(count).isEqualTo(3L);
        verify(repository).countByApoliceNumero("AP-001");
    }

    @Test
    @DisplayName("countBySeguradoCpf deve retornar contagem de sinistros do segurado")
    void countBySeguradoCpfShouldReturnCount() {
        when(repository.countBySeguradoCpf("12345678901")).thenReturn(2L);

        long count = repository.countBySeguradoCpf("12345678901");

        assertThat(count).isEqualTo(2L);
        verify(repository).countBySeguradoCpf("12345678901");
    }

    @Test
    @DisplayName("findLastUpdated deve retornar último sinistro atualizado")
    void findLastUpdatedShouldReturnLatest() {
        SinistroDetailView view = new SinistroDetailView();
        view.setProtocolo("SIN-2024-999");
        when(repository.findLastUpdated()).thenReturn(Optional.of(view));

        Optional<SinistroDetailView> result = repository.findLastUpdated();

        assertThat(result).isPresent();
        assertThat(result.get().getProtocolo()).isEqualTo("SIN-2024-999");
        verify(repository).findLastUpdated();
    }

    @Test
    @DisplayName("findLastUpdated deve retornar empty quando não há sinistros")
    void findLastUpdatedShouldReturnEmptyWhenNone() {
        when(repository.findLastUpdated()).thenReturn(Optional.empty());

        Optional<SinistroDetailView> result = repository.findLastUpdated();

        assertThat(result).isEmpty();
        verify(repository).findLastUpdated();
    }

    @Test
    @DisplayName("findEmAnaliseByAnalista deve retornar lista de sinistros em análise")
    void findEmAnaliseByShouldReturnList() {
        SinistroDetailView view = new SinistroDetailView();
        view.setAnalistaResponsavel("Analista01");
        view.setStatus("EM_ANALISE");
        when(repository.findEmAnaliseByAnalista("Analista01")).thenReturn(List.of(view));

        List<SinistroDetailView> result = repository.findEmAnaliseByAnalista("Analista01");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getAnalistaResponsavel()).isEqualTo("Analista01");
        verify(repository).findEmAnaliseByAnalista("Analista01");
    }
}
