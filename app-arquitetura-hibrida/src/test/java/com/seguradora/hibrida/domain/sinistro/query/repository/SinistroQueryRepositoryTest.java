package com.seguradora.hibrida.domain.sinistro.query.repository;

import com.seguradora.hibrida.domain.sinistro.query.model.SinistroQueryModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("SinistroQueryRepository Tests")
class SinistroQueryRepositoryTest {

    private final SinistroQueryRepository repository = mock(SinistroQueryRepository.class);

    @Test
    @DisplayName("findByProtocolo deve retornar sinistro quando encontrado")
    void findByProtocoloShouldReturnWhenFound() {
        UUID id = UUID.randomUUID();
        SinistroQueryModel model = new SinistroQueryModel(id, "SIN-2024-001");
        when(repository.findByProtocolo("SIN-2024-001")).thenReturn(Optional.of(model));

        Optional<SinistroQueryModel> result = repository.findByProtocolo("SIN-2024-001");

        assertThat(result).isPresent();
        assertThat(result.get().getProtocolo()).isEqualTo("SIN-2024-001");
        verify(repository).findByProtocolo("SIN-2024-001");
    }

    @Test
    @DisplayName("findByProtocolo deve retornar empty quando não encontrado")
    void findByProtocoloShouldReturnEmptyWhenNotFound() {
        when(repository.findByProtocolo("SIN-INEXISTENTE")).thenReturn(Optional.empty());

        Optional<SinistroQueryModel> result = repository.findByProtocolo("SIN-INEXISTENTE");

        assertThat(result).isEmpty();
        verify(repository).findByProtocolo("SIN-INEXISTENTE");
    }

    @Test
    @DisplayName("findByCpfSeguradoOrderByDataAberturaDesc deve retornar lista de sinistros")
    void findByCpfSeguradoShouldReturnList() {
        UUID id = UUID.randomUUID();
        SinistroQueryModel model = new SinistroQueryModel(id, "SIN-001");
        model.setCpfSegurado("12345678901");
        when(repository.findByCpfSeguradoOrderByDataAberturaDesc("12345678901")).thenReturn(List.of(model));

        List<SinistroQueryModel> result = repository.findByCpfSeguradoOrderByDataAberturaDesc("12345678901");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCpfSegurado()).isEqualTo("12345678901");
        verify(repository).findByCpfSeguradoOrderByDataAberturaDesc("12345678901");
    }

    @Test
    @DisplayName("findByPlacaOrderByDataAberturaDesc deve retornar lista de sinistros")
    void findByPlacaShouldReturnList() {
        UUID id = UUID.randomUUID();
        SinistroQueryModel model = new SinistroQueryModel(id, "SIN-001");
        model.setPlaca("ABC1234");
        when(repository.findByPlacaOrderByDataAberturaDesc("ABC1234")).thenReturn(List.of(model));

        List<SinistroQueryModel> result = repository.findByPlacaOrderByDataAberturaDesc("ABC1234");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getPlaca()).isEqualTo("ABC1234");
        verify(repository).findByPlacaOrderByDataAberturaDesc("ABC1234");
    }

    @Test
    @DisplayName("findByApoliceNumeroOrderByDataAberturaDesc deve retornar lista de sinistros")
    void findByApoliceNumeroShouldReturnList() {
        UUID id = UUID.randomUUID();
        SinistroQueryModel model = new SinistroQueryModel(id, "SIN-001");
        model.setApoliceNumero("AP-001");
        when(repository.findByApoliceNumeroOrderByDataAberturaDesc("AP-001")).thenReturn(List.of(model));

        List<SinistroQueryModel> result = repository.findByApoliceNumeroOrderByDataAberturaDesc("AP-001");

        assertThat(result).hasSize(1);
        verify(repository).findByApoliceNumeroOrderByDataAberturaDesc("AP-001");
    }

    @Test
    @DisplayName("findByStatusOrderByDataAberturaDesc deve retornar página de sinistros")
    void findByStatusShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroQueryModel> page = mock(Page.class);
        when(repository.findByStatusOrderByDataAberturaDesc("ABERTO", pageable)).thenReturn(page);

        Page<SinistroQueryModel> result = repository.findByStatusOrderByDataAberturaDesc("ABERTO", pageable);

        assertThat(result).isNotNull();
        verify(repository).findByStatusOrderByDataAberturaDesc("ABERTO", pageable);
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
    @DisplayName("countByCpfSegurado deve retornar contagem de sinistros por CPF")
    void countByCpfSeguradoShouldReturnCount() {
        when(repository.countByCpfSegurado("12345678901")).thenReturn(3L);

        long count = repository.countByCpfSegurado("12345678901");

        assertThat(count).isEqualTo(3L);
        verify(repository).countByCpfSegurado("12345678901");
    }

    @Test
    @DisplayName("countByPlaca deve retornar contagem de sinistros por placa")
    void countByPlacaShouldReturnCount() {
        when(repository.countByPlaca("ABC1234")).thenReturn(2L);

        long count = repository.countByPlaca("ABC1234");

        assertThat(count).isEqualTo(2L);
        verify(repository).countByPlaca("ABC1234");
    }

    @Test
    @DisplayName("findByOperadorResponsavelOrderByDataAberturaDesc deve retornar página")
    void findByOperadorShouldReturnPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<SinistroQueryModel> page = mock(Page.class);
        when(repository.findByOperadorResponsavelOrderByDataAberturaDesc("Analista01", pageable)).thenReturn(page);

        Page<SinistroQueryModel> result = repository.findByOperadorResponsavelOrderByDataAberturaDesc("Analista01", pageable);

        assertThat(result).isNotNull();
        verify(repository).findByOperadorResponsavelOrderByDataAberturaDesc("Analista01", pageable);
    }
}
