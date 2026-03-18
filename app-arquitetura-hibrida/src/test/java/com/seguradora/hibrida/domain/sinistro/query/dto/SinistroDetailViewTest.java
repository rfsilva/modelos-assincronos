package com.seguradora.hibrida.domain.sinistro.query.dto;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("SinistroDetailView Tests")
class SinistroDetailViewTest {

    @Test
    @DisplayName("deve criar SinistroDetailView com builder")
    void shouldCreateWithBuilder() {
        UUID id = UUID.randomUUID();
        Instant now = Instant.now();

        SinistroDetailView view = SinistroDetailView.builder()
                .id(id)
                .protocolo("SIN-2024-000001")
                .tipoSinistro("COLISAO")
                .status("ABERTO")
                .dataOcorrencia(now)
                .dataAbertura(now)
                .operadorResponsavel("João Silva")
                .descricao("Colisão frontal")
                .valorEstimado(new BigDecimal("15000.00"))
                .valorFranquia(new BigDecimal("2000.00"))
                .prioridade("NORMAL")
                .canalAbertura("WEB")
                .build();

        assertThat(view.id()).isEqualTo(id);
        assertThat(view.protocolo()).isEqualTo("SIN-2024-000001");
        assertThat(view.tipoSinistro()).isEqualTo("COLISAO");
        assertThat(view.status()).isEqualTo("ABERTO");
        assertThat(view.operadorResponsavel()).isEqualTo("João Silva");
        assertThat(view.valorEstimado()).isEqualByComparingTo("15000.00");
    }

    @Test
    @DisplayName("SeguradoInfo deve criar com builder")
    void seguradoInfoShouldCreateWithBuilder() {
        SinistroDetailView.SeguradoInfo segurado = SinistroDetailView.SeguradoInfo.builder()
                .cpf("12345678901")
                .nome("João da Silva")
                .email("joao@email.com")
                .telefone("(11) 99999-9999")
                .build();

        assertThat(segurado.cpf()).isEqualTo("12345678901");
        assertThat(segurado.nome()).isEqualTo("João da Silva");
        assertThat(segurado.email()).isEqualTo("joao@email.com");
        assertThat(segurado.telefone()).isEqualTo("(11) 99999-9999");
    }

    @Test
    @DisplayName("VeiculoInfo deve criar com builder")
    void veiculoInfoShouldCreateWithBuilder() {
        SinistroDetailView.VeiculoInfo veiculo = SinistroDetailView.VeiculoInfo.builder()
                .placa("ABC1234")
                .renavam("12345678901")
                .marca("Toyota")
                .modelo("Corolla")
                .anoFabricacao(2020)
                .anoModelo(2021)
                .cor("Branco")
                .build();

        assertThat(veiculo.placa()).isEqualTo("ABC1234");
        assertThat(veiculo.marca()).isEqualTo("Toyota");
        assertThat(veiculo.modelo()).isEqualTo("Corolla");
        assertThat(veiculo.anoFabricacao()).isEqualTo(2020);
    }

    @Test
    @DisplayName("ApoliceInfo deve criar com builder")
    void apoliceInfoShouldCreateWithBuilder() {
        SinistroDetailView.ApoliceInfo apolice = SinistroDetailView.ApoliceInfo.builder()
                .numero("AP-2024-001234")
                .vigenciaInicio(LocalDate.of(2024, 1, 1))
                .vigenciaFim(LocalDate.of(2024, 12, 31))
                .valorSegurado(new BigDecimal("50000.00"))
                .build();

        assertThat(apolice.numero()).isEqualTo("AP-2024-001234");
        assertThat(apolice.vigenciaInicio()).isEqualTo(LocalDate.of(2024, 1, 1));
        assertThat(apolice.valorSegurado()).isEqualByComparingTo("50000.00");
    }

    @Test
    @DisplayName("ConsultaDetranInfo deve criar com builder")
    void consultaDetranInfoShouldCreateWithBuilder() {
        Instant ts = Instant.now();

        SinistroDetailView.ConsultaDetranInfo detran = SinistroDetailView.ConsultaDetranInfo.builder()
                .realizada(true)
                .timestamp(ts)
                .status("SUCCESS")
                .dados(Map.of("restricao", "false"))
                .build();

        assertThat(detran.realizada()).isTrue();
        assertThat(detran.status()).isEqualTo("SUCCESS");
        assertThat(detran.dados()).containsKey("restricao");
    }

    @Test
    @DisplayName("LocalizacaoInfo deve criar com builder")
    void localizacaoInfoShouldCreateWithBuilder() {
        SinistroDetailView.LocalizacaoInfo localizacao = SinistroDetailView.LocalizacaoInfo.builder()
                .cep("01234567")
                .endereco("Rua das Flores, 100")
                .cidade("São Paulo")
                .estado("SP")
                .build();

        assertThat(localizacao.cep()).isEqualTo("01234567");
        assertThat(localizacao.cidade()).isEqualTo("São Paulo");
        assertThat(localizacao.estado()).isEqualTo("SP");
    }

    @Test
    @DisplayName("deve criar SinistroDetailView com nested records")
    void shouldCreateWithNestedRecords() {
        UUID id = UUID.randomUUID();

        SinistroDetailView.SeguradoInfo segurado = SinistroDetailView.SeguradoInfo.builder()
                .cpf("12345678901")
                .nome("Maria Souza")
                .build();

        SinistroDetailView.VeiculoInfo veiculo = SinistroDetailView.VeiculoInfo.builder()
                .placa("XYZ9876")
                .marca("Honda")
                .build();

        SinistroDetailView view = SinistroDetailView.builder()
                .id(id)
                .protocolo("SIN-2024-000002")
                .segurado(segurado)
                .veiculo(veiculo)
                .tipoSinistro("ROUBO_FURTO")
                .status("ABERTO")
                .tags(List.of("URGENTE", "ROUBO"))
                .build();

        assertThat(view.segurado()).isNotNull();
        assertThat(view.segurado().cpf()).isEqualTo("12345678901");
        assertThat(view.veiculo()).isNotNull();
        assertThat(view.veiculo().placa()).isEqualTo("XYZ9876");
        assertThat(view.tags()).containsExactly("URGENTE", "ROUBO");
    }

    @Test
    @DisplayName("deve ser igual quando campos são iguais")
    void shouldBeEqualWhenFieldsAreEqual() {
        UUID id = UUID.randomUUID();

        SinistroDetailView view1 = SinistroDetailView.builder()
                .id(id)
                .protocolo("SIN-2024-000001")
                .status("ABERTO")
                .build();

        SinistroDetailView view2 = SinistroDetailView.builder()
                .id(id)
                .protocolo("SIN-2024-000001")
                .status("ABERTO")
                .build();

        assertThat(view1).isEqualTo(view2);
    }

    @Test
    @DisplayName("toString deve conter informações relevantes")
    void toStringShouldContainRelevantInfo() {
        SinistroDetailView view = SinistroDetailView.builder()
                .protocolo("SIN-2024-000001")
                .status("ABERTO")
                .build();

        String str = view.toString();

        assertThat(str).contains("SIN-2024-000001");
        assertThat(str).contains("ABERTO");
    }

    @Test
    @DisplayName("campos de data devem aceitar instants")
    void dateFieldsShouldAcceptInstants() {
        Instant now = Instant.now();

        SinistroDetailView view = SinistroDetailView.builder()
                .dataOcorrencia(now)
                .dataAbertura(now)
                .dataFechamento(now)
                .createdAt(now)
                .updatedAt(now)
                .build();

        assertThat(view.dataOcorrencia()).isEqualTo(now);
        assertThat(view.dataAbertura()).isEqualTo(now);
        assertThat(view.dataFechamento()).isEqualTo(now);
        assertThat(view.createdAt()).isEqualTo(now);
        assertThat(view.updatedAt()).isEqualTo(now);
    }
}
