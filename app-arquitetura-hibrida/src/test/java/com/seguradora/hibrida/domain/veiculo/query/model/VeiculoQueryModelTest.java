package com.seguradora.hibrida.domain.veiculo.query.model;

import com.seguradora.hibrida.domain.veiculo.model.StatusVeiculo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.Year;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoQueryModel}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoQueryModel - Testes Unitários")
class VeiculoQueryModelTest {

    private VeiculoQueryModel queryModel;

    @BeforeEach
    void setUp() {
        queryModel = new VeiculoQueryModel();
        queryModel.setId("VEI-001");
        queryModel.setPlaca("ABC1234");
        queryModel.setRenavam("12345678900");
        queryModel.setChassi("1HGBH41J6MN109186");
        queryModel.setMarca("Honda");
        queryModel.setModelo("Civic");
        queryModel.setAnoFabricacao(2020);
        queryModel.setAnoModelo(2021);
        queryModel.setCor("Branco");
        queryModel.setTipoCombustivel("FLEX");
        queryModel.setCategoria("PASSEIO");
        queryModel.setCilindrada(1600);
        queryModel.setProprietarioCpf("12345678909");
        queryModel.setProprietarioNome("João Silva");
        queryModel.setProprietarioTipo("FISICA");
        queryModel.setStatus(StatusVeiculo.ATIVO);
        queryModel.setApoliceAtiva(false);
        queryModel.setVersion(1L);
    }

    @Nested
    @DisplayName("Testes de Criação e Propriedades")
    class CriacaoPropriedadesTests {

        @Test
        @DisplayName("Deve criar modelo de query com construtor padrão")
        void deveCriarModeloQueryComConstrutorPadrao() {
            VeiculoQueryModel novo = new VeiculoQueryModel();

            assertThat(novo).isNotNull();
            assertThat(novo.getId()).isNull();
            assertThat(novo.getApoliceAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve definir e recuperar ID")
        void deveDefinirERecuperarId() {
            assertThat(queryModel.getId()).isEqualTo("VEI-001");
        }

        @Test
        @DisplayName("Deve definir e recuperar placa")
        void deveDefinirERecuperarPlaca() {
            assertThat(queryModel.getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve definir e recuperar RENAVAM")
        void deveDefinirERecuperarRenavam() {
            assertThat(queryModel.getRenavam()).isEqualTo("12345678900");
        }

        @Test
        @DisplayName("Deve definir e recuperar chassi")
        void deveDefinirERecuperarChassi() {
            assertThat(queryModel.getChassi()).isEqualTo("1HGBH41J6MN109186");
        }

        @Test
        @DisplayName("Deve definir e recuperar marca e modelo")
        void deveDefinirERecuperarMarcaModelo() {
            assertThat(queryModel.getMarca()).isEqualTo("Honda");
            assertThat(queryModel.getModelo()).isEqualTo("Civic");
        }

        @Test
        @DisplayName("Deve definir e recuperar anos")
        void deveDefinirERecuperarAnos() {
            assertThat(queryModel.getAnoFabricacao()).isEqualTo(2020);
            assertThat(queryModel.getAnoModelo()).isEqualTo(2021);
        }
    }

    @Nested
    @DisplayName("Testes de Especificações")
    class EspecificacoesTests {

        @Test
        @DisplayName("Deve definir e recuperar cor")
        void deveDefinirERecuperarCor() {
            queryModel.setCor("Preto");
            assertThat(queryModel.getCor()).isEqualTo("Preto");
        }

        @Test
        @DisplayName("Deve definir e recuperar tipo de combustível")
        void deveDefinirERecuperarTipoCombustivel() {
            queryModel.setTipoCombustivel("GASOLINA");
            assertThat(queryModel.getTipoCombustivel()).isEqualTo("GASOLINA");
        }

        @Test
        @DisplayName("Deve definir e recuperar categoria")
        void deveDefinirERecuperarCategoria() {
            queryModel.setCategoria("SUV");
            assertThat(queryModel.getCategoria()).isEqualTo("SUV");
        }

        @Test
        @DisplayName("Deve definir e recuperar cilindrada")
        void deveDefinirERecuperarCilindrada() {
            queryModel.setCilindrada(2000);
            assertThat(queryModel.getCilindrada()).isEqualTo(2000);
        }
    }

    @Nested
    @DisplayName("Testes de Proprietário")
    class ProprietarioTests {

        @Test
        @DisplayName("Deve definir e recuperar CPF do proprietário")
        void deveDefinirERecuperarCpfProprietario() {
            assertThat(queryModel.getProprietarioCpf()).isEqualTo("12345678909");
        }

        @Test
        @DisplayName("Deve definir e recuperar nome do proprietário")
        void deveDefinirERecuperarNomeProprietario() {
            assertThat(queryModel.getProprietarioNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve definir e recuperar tipo do proprietário")
        void deveDefinirERecuperarTipoProprietario() {
            assertThat(queryModel.getProprietarioTipo()).isEqualTo("FISICA");
        }

        @Test
        @DisplayName("Deve permitir CNPJ como proprietário")
        void devePermitirCnpjComoProprietario() {
            queryModel.setProprietarioCpf("11222333000181");
            queryModel.setProprietarioTipo("JURIDICA");

            assertThat(queryModel.getProprietarioCpf()).isEqualTo("11222333000181");
            assertThat(queryModel.getProprietarioTipo()).isEqualTo("JURIDICA");
        }
    }

    @Nested
    @DisplayName("Testes de Status e Relacionamentos")
    class StatusRelacionamentosTests {

        @Test
        @DisplayName("Deve definir e recuperar status")
        void deveDefinirERecuperarStatus() {
            queryModel.setStatus(StatusVeiculo.INATIVO);
            assertThat(queryModel.getStatus()).isEqualTo(StatusVeiculo.INATIVO);
        }

        @Test
        @DisplayName("Deve verificar se está ativo")
        void deveVerificarSeEstaAtivo() {
            queryModel.setStatus(StatusVeiculo.ATIVO);
            assertThat(queryModel.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se não está ativo")
        void deveVerificarSeNaoEstaAtivo() {
            queryModel.setStatus(StatusVeiculo.INATIVO);
            assertThat(queryModel.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve definir e recuperar apoliceAtiva")
        void deveDefinirERecuperarApoliceAtiva() {
            queryModel.setApoliceAtiva(true);
            assertThat(queryModel.getApoliceAtiva()).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se tem apólice ativa")
        void deveVerificarSeTemApoliceAtiva() {
            queryModel.setApoliceAtiva(true);
            assertThat(queryModel.temApoliceAtiva()).isTrue();
        }

        @Test
        @DisplayName("Deve verificar se não tem apólice ativa")
        void deveVerificarSeNaoTemApoliceAtiva() {
            queryModel.setApoliceAtiva(false);
            assertThat(queryModel.temApoliceAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve tratar null em apoliceAtiva como false")
        void deveTratarNullEmApoliceAtivaComoFalse() {
            queryModel.setApoliceAtiva(null);
            assertThat(queryModel.temApoliceAtiva()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Dados Geográficos")
    class DadosGeograficosTests {

        @Test
        @DisplayName("Deve definir e recuperar cidade")
        void deveDefinirERecuperarCidade() {
            queryModel.setCidade("São Paulo");
            assertThat(queryModel.getCidade()).isEqualTo("São Paulo");
        }

        @Test
        @DisplayName("Deve definir e recuperar estado")
        void deveDefinirERecuperarEstado() {
            queryModel.setEstado("SP");
            assertThat(queryModel.getEstado()).isEqualTo("SP");
        }

        @Test
        @DisplayName("Deve definir e recuperar região")
        void deveDefinirERecuperarRegiao() {
            queryModel.setRegiao("SUDESTE");
            assertThat(queryModel.getRegiao()).isEqualTo("SUDESTE");
        }
    }

    @Nested
    @DisplayName("Testes de Metadados")
    class MetadadosTests {

        @Test
        @DisplayName("Deve definir e recuperar versão")
        void deveDefinirERecuperarVersao() {
            queryModel.setVersion(5L);
            assertThat(queryModel.getVersion()).isEqualTo(5L);
        }

        @Test
        @DisplayName("Deve definir e recuperar lastEventId")
        void deveDefinirERecuperarLastEventId() {
            queryModel.setLastEventId(123L);
            assertThat(queryModel.getLastEventId()).isEqualTo(123L);
        }

        @Test
        @DisplayName("Deve definir e recuperar createdAt")
        void deveDefinirERecuperarCreatedAt() {
            Instant agora = Instant.now();
            queryModel.setCreatedAt(agora);
            assertThat(queryModel.getCreatedAt()).isEqualTo(agora);
        }

        @Test
        @DisplayName("Deve definir e recuperar updatedAt")
        void deveDefinirERecuperarUpdatedAt() {
            Instant agora = Instant.now();
            queryModel.setUpdatedAt(agora);
            assertThat(queryModel.getUpdatedAt()).isEqualTo(agora);
        }

        @Test
        @DisplayName("Deve atualizar timestamp ao chamar onUpdate")
        void deveAtualizarTimestampAoChamarOnUpdate() {
            Instant antes = Instant.now().minusSeconds(10);
            queryModel.setUpdatedAt(antes);

            queryModel.onUpdate();

            assertThat(queryModel.getUpdatedAt()).isAfter(antes);
        }
    }

    @Nested
    @DisplayName("Testes de Método getIdade")
    class GetIdadeTests {

        @Test
        @DisplayName("Deve calcular idade do veículo corretamente")
        void deveCalcularIdadeVeiculoCorretamente() {
            int anoAtual = Year.now().getValue();
            queryModel.setAnoFabricacao(anoAtual - 3);

            assertThat(queryModel.getIdade()).isEqualTo(3);
        }

        @Test
        @DisplayName("Deve retornar zero para veículo do ano atual")
        void deveRetornarZeroParaVeiculoAnoAtual() {
            int anoAtual = Year.now().getValue();
            queryModel.setAnoFabricacao(anoAtual);

            assertThat(queryModel.getIdade()).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular idade para veículo antigo")
        void deveCalcularIdadeParaVeiculoAntigo() {
            int anoAtual = Year.now().getValue();
            queryModel.setAnoFabricacao(anoAtual - 15);

            assertThat(queryModel.getIdade()).isEqualTo(15);
        }
    }

    @Nested
    @DisplayName("Testes de Método getDescricaoCompleta")
    class GetDescricaoCompletaTests {

        @Test
        @DisplayName("Deve retornar descrição completa do veículo")
        void deveRetornarDescricaoCompletaVeiculo() {
            String descricao = queryModel.getDescricaoCompleta();

            assertThat(descricao).isEqualTo("Honda Civic 2020/2021 - ABC1234");
        }

        @Test
        @DisplayName("Deve incluir marca, modelo, anos e placa")
        void deveIncluirMarcaModeloAnosPlaca() {
            String descricao = queryModel.getDescricaoCompleta();

            assertThat(descricao).contains("Honda");
            assertThat(descricao).contains("Civic");
            assertThat(descricao).contains("2020");
            assertThat(descricao).contains("2021");
            assertThat(descricao).contains("ABC1234");
        }

        @Test
        @DisplayName("Deve formatar corretamente para diferentes marcas")
        void deveFormatarCorretamenteParaDiferentesMarcas() {
            queryModel.setMarca("Toyota");
            queryModel.setModelo("Corolla");
            queryModel.setPlaca("XYZ9876");

            String descricao = queryModel.getDescricaoCompleta();

            assertThat(descricao).isEqualTo("Toyota Corolla 2020/2021 - XYZ9876");
        }
    }

    @Nested
    @DisplayName("Testes de equals e hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Deve ser igual a si mesmo")
        void deveSerIgualASiMesmo() {
            assertThat(queryModel).isEqualTo(queryModel);
        }

        @Test
        @DisplayName("Deve ser igual a outro com mesmo ID")
        void deveSerIgualAOutroComMesmoId() {
            VeiculoQueryModel outro = new VeiculoQueryModel();
            outro.setId("VEI-001");

            assertThat(queryModel).isEqualTo(outro);
            assertThat(queryModel.hashCode()).isEqualTo(outro.hashCode());
        }

        @Test
        @DisplayName("Não deve ser igual a outro com ID diferente")
        void naoDeveSerIgualAOutroComIdDiferente() {
            VeiculoQueryModel outro = new VeiculoQueryModel();
            outro.setId("VEI-002");

            assertThat(queryModel).isNotEqualTo(outro);
        }

        @Test
        @DisplayName("Não deve ser igual a null")
        void naoDeveSerIgualANull() {
            assertThat(queryModel).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Não deve ser igual a objeto de classe diferente")
        void naoDeveSerIgualAObjetoDeClasseDiferente() {
            assertThat(queryModel).isNotEqualTo("String");
        }

        @Test
        @DisplayName("Deve ter hashCode consistente")
        void deveTerHashCodeConsistente() {
            int hash1 = queryModel.hashCode();
            int hash2 = queryModel.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Testes de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            String resultado = queryModel.toString();

            assertThat(resultado).contains("VeiculoQueryModel");
            assertThat(resultado).contains("VEI-001");
            assertThat(resultado).contains("ABC1234");
            assertThat(resultado).contains("Honda");
            assertThat(resultado).contains("Civic");
        }

        @Test
        @DisplayName("toString deve incluir status")
        void toStringDeveIncluirStatus() {
            queryModel.setStatus(StatusVeiculo.ATIVO);

            String resultado = queryModel.toString();

            assertThat(resultado).contains("ATIVO");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar veículo ativo com apólice")
        void deveRepresentarVeiculoAtivoComApolice() {
            queryModel.setStatus(StatusVeiculo.ATIVO);
            queryModel.setApoliceAtiva(true);
            queryModel.setCidade("São Paulo");
            queryModel.setEstado("SP");
            queryModel.setRegiao("SUDESTE");

            assertThat(queryModel.isAtivo()).isTrue();
            assertThat(queryModel.temApoliceAtiva()).isTrue();
            assertThat(queryModel.getCidade()).isEqualTo("São Paulo");
            assertThat(queryModel.getEstado()).isEqualTo("SP");
            assertThat(queryModel.getRegiao()).isEqualTo("SUDESTE");
        }

        @Test
        @DisplayName("Deve representar veículo inativo sem apólice")
        void deveRepresentarVeiculoInativoSemApolice() {
            queryModel.setStatus(StatusVeiculo.INATIVO);
            queryModel.setApoliceAtiva(false);

            assertThat(queryModel.isAtivo()).isFalse();
            assertThat(queryModel.temApoliceAtiva()).isFalse();
        }

        @Test
        @DisplayName("Deve representar veículo bloqueado")
        void deveRepresentarVeiculoBloqueado() {
            queryModel.setStatus(StatusVeiculo.BLOQUEADO);

            assertThat(queryModel.isAtivo()).isFalse();
            assertThat(queryModel.getStatus()).isEqualTo(StatusVeiculo.BLOQUEADO);
        }

        @Test
        @DisplayName("Deve atualizar veículo após evento")
        void deveAtualizarVeiculoAposEvento() {
            Instant antes = Instant.now().minusSeconds(60);
            queryModel.setUpdatedAt(antes);
            queryModel.setVersion(1L);

            // Simular atualização
            queryModel.setCor("Azul");
            queryModel.setVersion(2L);
            queryModel.onUpdate();

            assertThat(queryModel.getCor()).isEqualTo("Azul");
            assertThat(queryModel.getVersion()).isEqualTo(2L);
            assertThat(queryModel.getUpdatedAt()).isAfter(antes);
        }

        @Test
        @DisplayName("Deve representar veículo novo (até 3 anos)")
        void deveRepresentarVeiculoNovo() {
            int anoAtual = Year.now().getValue();
            queryModel.setAnoFabricacao(anoAtual - 2);

            assertThat(queryModel.getIdade()).isLessThanOrEqualTo(3);
        }

        @Test
        @DisplayName("Deve representar veículo antigo (mais de 10 anos)")
        void deveRepresentarVeiculoAntigo() {
            int anoAtual = Year.now().getValue();
            queryModel.setAnoFabricacao(anoAtual - 12);

            assertThat(queryModel.getIdade()).isGreaterThan(10);
        }
    }

    @Nested
    @DisplayName("Testes de Validações de Dados")
    class ValidacoesDadosTests {

        @Test
        @DisplayName("Deve aceitar placa formato antigo")
        void deveAceitarPlacaFormatoAntigo() {
            queryModel.setPlaca("ABC1234");
            assertThat(queryModel.getPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve aceitar placa formato Mercosul")
        void deveAceitarPlacaFormatoMercosul() {
            queryModel.setPlaca("ABC1D23");
            assertThat(queryModel.getPlaca()).isEqualTo("ABC1D23");
        }

        @Test
        @DisplayName("Deve aceitar RENAVAM com 11 dígitos")
        void deveAceitarRenavamCom11Digitos() {
            queryModel.setRenavam("12345678900");
            assertThat(queryModel.getRenavam()).hasSize(11);
        }

        @Test
        @DisplayName("Deve aceitar chassi com 17 caracteres")
        void deveAceitarChassiCom17Caracteres() {
            queryModel.setChassi("1HGBH41J6MN109186");
            assertThat(queryModel.getChassi()).hasSize(17);
        }

        @Test
        @DisplayName("Deve aceitar CPF com 11 dígitos")
        void deveAceitarCpfCom11Digitos() {
            queryModel.setProprietarioCpf("12345678909");
            assertThat(queryModel.getProprietarioCpf()).hasSize(11);
        }

        @Test
        @DisplayName("Deve aceitar CNPJ com 14 dígitos")
        void deveAceitarCnpjCom14Digitos() {
            queryModel.setProprietarioCpf("11222333000181");
            assertThat(queryModel.getProprietarioCpf()).hasSize(14);
        }
    }

    @Nested
    @DisplayName("Testes de Versionamento")
    class VersionamentoTests {

        @Test
        @DisplayName("Deve iniciar com versão padrão")
        void deveIniciarComVersaoPadrao() {
            VeiculoQueryModel novo = new VeiculoQueryModel();
            assertThat(novo.getVersion()).isEqualTo(0L);
        }

        @Test
        @DisplayName("Deve incrementar versão")
        void deveIncrementarVersao() {
            queryModel.setVersion(1L);
            queryModel.setVersion(2L);
            queryModel.setVersion(3L);

            assertThat(queryModel.getVersion()).isEqualTo(3L);
        }

        @Test
        @DisplayName("Deve rastrear último evento processado")
        void deveRastrearUltimoEventoProcessado() {
            queryModel.setLastEventId(1L);
            assertThat(queryModel.getLastEventId()).isEqualTo(1L);

            queryModel.setLastEventId(2L);
            assertThat(queryModel.getLastEventId()).isEqualTo(2L);
        }
    }
}
