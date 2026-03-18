package com.seguradora.hibrida.domain.sinistro.query.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroListView (DTO record) Tests")
class SinistroListViewDtoTest {

    private static final UUID ID = UUID.fromString("123e4567-e89b-12d3-a456-426614174000");
    private static final Instant DATA_OCORRENCIA = Instant.parse("2024-06-15T10:00:00Z");
    private static final Instant DATA_ABERTURA = Instant.parse("2024-06-15T11:00:00Z");

    @Test
    @DisplayName("builder deve construir record com todos os campos preenchidos")
    void builderShouldBuildWithAllFields() {
        SinistroListView view = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-001")
                .cpfSegurado("12345678901")
                .nomeSegurado("João Silva")
                .placa("ABC1234")
                .tipoSinistro("COLISAO")
                .status("ABERTO")
                .dataOcorrencia(DATA_OCORRENCIA)
                .dataAbertura(DATA_ABERTURA)
                .operadorResponsavel("Analista01")
                .valorEstimado(new BigDecimal("15000.00"))
                .consultaDetranRealizada(true)
                .tags(List.of("URGENTE", "ALTO_VALOR"))
                .prioridade("ALTA")
                .build();

        assertThat(view.id()).isEqualTo(ID);
        assertThat(view.protocolo()).isEqualTo("SIN-2024-001");
        assertThat(view.cpfSegurado()).isEqualTo("12345678901");
        assertThat(view.nomeSegurado()).isEqualTo("João Silva");
        assertThat(view.placa()).isEqualTo("ABC1234");
        assertThat(view.tipoSinistro()).isEqualTo("COLISAO");
        assertThat(view.status()).isEqualTo("ABERTO");
        assertThat(view.dataOcorrencia()).isEqualTo(DATA_OCORRENCIA);
        assertThat(view.dataAbertura()).isEqualTo(DATA_ABERTURA);
        assertThat(view.operadorResponsavel()).isEqualTo("Analista01");
        assertThat(view.valorEstimado()).isEqualByComparingTo(new BigDecimal("15000.00"));
        assertThat(view.consultaDetranRealizada()).isTrue();
        assertThat(view.tags()).containsExactly("URGENTE", "ALTO_VALOR");
        assertThat(view.prioridade()).isEqualTo("ALTA");
    }

    @Test
    @DisplayName("builder deve construir record com campos nulos")
    void builderShouldAllowNullFields() {
        SinistroListView view = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-002")
                .status("ABERTO")
                .build();

        assertThat(view.id()).isEqualTo(ID);
        assertThat(view.protocolo()).isEqualTo("SIN-2024-002");
        assertThat(view.cpfSegurado()).isNull();
        assertThat(view.nomeSegurado()).isNull();
        assertThat(view.valorEstimado()).isNull();
        assertThat(view.tags()).isNull();
    }

    @Test
    @DisplayName("dois records com mesmos valores devem ser iguais")
    void sameValuesShouldBeEqual() {
        SinistroListView view1 = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-001")
                .status("ABERTO")
                .build();

        SinistroListView view2 = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-001")
                .status("ABERTO")
                .build();

        assertThat(view1).isEqualTo(view2);
        assertThat(view1.hashCode()).isEqualTo(view2.hashCode());
    }

    @Test
    @DisplayName("dois records com valores diferentes não devem ser iguais")
    void differentValuesShouldNotBeEqual() {
        SinistroListView view1 = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-001")
                .status("ABERTO")
                .build();

        SinistroListView view2 = SinistroListView.builder()
                .id(UUID.randomUUID())
                .protocolo("SIN-2024-002")
                .status("APROVADO")
                .build();

        assertThat(view1).isNotEqualTo(view2);
    }

    @Test
    @DisplayName("toString deve conter os valores dos campos")
    void toStringShouldContainFieldValues() {
        SinistroListView view = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-001")
                .status("ABERTO")
                .build();

        String str = view.toString();

        assertThat(str).contains("SIN-2024-001");
        assertThat(str).contains("ABERTO");
    }

    @Test
    @DisplayName("tags vazia deve ser suportada")
    void emptyTagsShouldBeSupported() {
        SinistroListView view = SinistroListView.builder()
                .id(ID)
                .protocolo("SIN-2024-003")
                .status("ABERTO")
                .tags(List.of())
                .build();

        assertThat(view.tags()).isEmpty();
    }
}
