package com.seguradora.hibrida.domain.veiculo.relationship.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link VeiculoApoliceRelacionamento}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("VeiculoApoliceRelacionamento - Testes Unitários")
class VeiculoApoliceRelacionamentoTest {

    private VeiculoApoliceRelacionamento relacionamento;

    @BeforeEach
    void setUp() {
        relacionamento = new VeiculoApoliceRelacionamento();
        relacionamento.setVeiculoId("VEI-001");
        relacionamento.setApoliceId("APO-001");
        relacionamento.setDataInicio(LocalDate.now());
        relacionamento.setStatus(StatusRelacionamento.ATIVO);
        relacionamento.setTipoRelacionamento(TipoRelacionamento.PRINCIPAL);
        relacionamento.setVeiculoPlaca("ABC1234");
        relacionamento.setVeiculoMarca("Honda");
        relacionamento.setVeiculoModelo("Civic");
        relacionamento.setVeiculoCategoria("PASSEIO");
        relacionamento.setApoliceNumero("APO-001-2024");
        relacionamento.setSeguradoCpf("12345678909");
        relacionamento.setSeguradoNome("João Silva");
        relacionamento.setTipoCobertura("COMPREENSIVA");
        relacionamento.setOperadorAssociacaoId("OP-123");
    }

    @Nested
    @DisplayName("Testes de Criação e Propriedades Básicas")
    class CriacaoPropriedadesTests {

        @Test
        @DisplayName("Deve criar relacionamento com valores padrão")
        void deveCriarRelacionamentoComValoresPadrao() {
            VeiculoApoliceRelacionamento novo = new VeiculoApoliceRelacionamento();

            assertThat(novo.getId()).isNull();
            assertThat(novo.getStatus()).isNull();
            assertThat(novo.getDataCriacao()).isNull();
        }

        @Test
        @DisplayName("Deve definir e recuperar veiculoId")
        void deveDefinirERecuperarVeiculoId() {
            assertThat(relacionamento.getVeiculoId()).isEqualTo("VEI-001");
        }

        @Test
        @DisplayName("Deve definir e recuperar apoliceId")
        void deveDefinirERecuperarApoliceId() {
            assertThat(relacionamento.getApoliceId()).isEqualTo("APO-001");
        }

        @Test
        @DisplayName("Deve definir e recuperar dataInicio")
        void deveDefinirERecuperarDataInicio() {
            LocalDate data = LocalDate.of(2024, 1, 15);
            relacionamento.setDataInicio(data);

            assertThat(relacionamento.getDataInicio()).isEqualTo(data);
        }

        @Test
        @DisplayName("Deve definir e recuperar dataFim")
        void deveDefinirERecuperarDataFim() {
            LocalDate data = LocalDate.of(2025, 1, 15);
            relacionamento.setDataFim(data);

            assertThat(relacionamento.getDataFim()).isEqualTo(data);
        }

        @Test
        @DisplayName("Deve definir e recuperar status")
        void deveDefinirERecuperarStatus() {
            relacionamento.setStatus(StatusRelacionamento.SUSPENSO);

            assertThat(relacionamento.getStatus()).isEqualTo(StatusRelacionamento.SUSPENSO);
        }

        @Test
        @DisplayName("Deve definir e recuperar tipo de relacionamento")
        void deveDefinirERecuperarTipoRelacionamento() {
            relacionamento.setTipoRelacionamento(TipoRelacionamento.ADICIONAL);

            assertThat(relacionamento.getTipoRelacionamento()).isEqualTo(TipoRelacionamento.ADICIONAL);
        }
    }

    @Nested
    @DisplayName("Testes de Dados Desnormalizados do Veículo")
    class DadosDesnormalizadosVeiculoTests {

        @Test
        @DisplayName("Deve armazenar placa do veículo")
        void deveArmazenarPlacaVeiculo() {
            assertThat(relacionamento.getVeiculoPlaca()).isEqualTo("ABC1234");
        }

        @Test
        @DisplayName("Deve armazenar marca do veículo")
        void deveArmazenarMarcaVeiculo() {
            assertThat(relacionamento.getVeiculoMarca()).isEqualTo("Honda");
        }

        @Test
        @DisplayName("Deve armazenar modelo do veículo")
        void deveArmazenarModeloVeiculo() {
            assertThat(relacionamento.getVeiculoModelo()).isEqualTo("Civic");
        }

        @Test
        @DisplayName("Deve armazenar categoria do veículo")
        void deveArmazenarCategoriaVeiculo() {
            assertThat(relacionamento.getVeiculoCategoria()).isEqualTo("PASSEIO");
        }
    }

    @Nested
    @DisplayName("Testes de Dados Desnormalizados da Apólice")
    class DadosDesnormalizadosApoliceTests {

        @Test
        @DisplayName("Deve armazenar número da apólice")
        void deveArmazenarNumeroApolice() {
            assertThat(relacionamento.getApoliceNumero()).isEqualTo("APO-001-2024");
        }

        @Test
        @DisplayName("Deve armazenar CPF do segurado")
        void deveArmazenarCpfSegurado() {
            assertThat(relacionamento.getSeguradoCpf()).isEqualTo("12345678909");
        }

        @Test
        @DisplayName("Deve armazenar nome do segurado")
        void deveArmazenarNomeSegurado() {
            assertThat(relacionamento.getSeguradoNome()).isEqualTo("João Silva");
        }

        @Test
        @DisplayName("Deve armazenar tipo de cobertura")
        void deveArmazenarTipoCobertura() {
            assertThat(relacionamento.getTipoCobertura()).isEqualTo("COMPREENSIVA");
        }
    }

    @Nested
    @DisplayName("Testes de Auditoria")
    class AuditoriaTests {

        @Test
        @DisplayName("Deve armazenar operador de associação")
        void deveArmazenarOperadorAssociacao() {
            assertThat(relacionamento.getOperadorAssociacaoId()).isEqualTo("OP-123");
        }

        @Test
        @DisplayName("Deve armazenar operador de desassociação")
        void deveArmazenarOperadorDesassociacao() {
            relacionamento.setOperadorDesassociacaoId("OP-456");

            assertThat(relacionamento.getOperadorDesassociacaoId()).isEqualTo("OP-456");
        }

        @Test
        @DisplayName("Deve armazenar data de criação")
        void deveArmazenarDataCriacao() {
            LocalDateTime agora = LocalDateTime.now();
            relacionamento.setDataCriacao(agora);

            assertThat(relacionamento.getDataCriacao()).isEqualTo(agora);
        }

        @Test
        @DisplayName("Deve armazenar data de atualização")
        void deveArmazenarDataAtualizacao() {
            LocalDateTime agora = LocalDateTime.now();
            relacionamento.setDataAtualizacao(agora);

            assertThat(relacionamento.getDataAtualizacao()).isEqualTo(agora);
        }

        @Test
        @DisplayName("Deve armazenar observações")
        void deveArmazenarObservacoes() {
            relacionamento.setObservacoes("Relacionamento criado durante migração");

            assertThat(relacionamento.getObservacoes()).isEqualTo("Relacionamento criado durante migração");
        }
    }

    @Nested
    @DisplayName("Testes de Método isAtivo")
    class IsAtivoTests {

        @Test
        @DisplayName("Deve retornar true quando status é ATIVO")
        void deveRetornarTrueQuandoStatusAtivo() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.isAtivo()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando status é SUSPENSO")
        void deveRetornarFalseQuandoStatusSuspenso() {
            relacionamento.setStatus(StatusRelacionamento.SUSPENSO);

            assertThat(relacionamento.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando status é ENCERRADO")
        void deveRetornarFalseQuandoStatusEncerrado() {
            relacionamento.setStatus(StatusRelacionamento.ENCERRADO);

            assertThat(relacionamento.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando status é CANCELADO")
        void deveRetornarFalseQuandoStatusCancelado() {
            relacionamento.setStatus(StatusRelacionamento.CANCELADO);

            assertThat(relacionamento.isAtivo()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando status é null")
        void deveRetornarFalseQuandoStatusNull() {
            relacionamento.setStatus(null);

            assertThat(relacionamento.isAtivo()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método isEncerrado")
    class IsEncerradoTests {

        @Test
        @DisplayName("Deve retornar true quando status é ENCERRADO")
        void deveRetornarTrueQuandoStatusEncerrado() {
            relacionamento.setStatus(StatusRelacionamento.ENCERRADO);

            assertThat(relacionamento.isEncerrado()).isTrue();
        }

        @Test
        @DisplayName("Deve retornar false quando status é ATIVO")
        void deveRetornarFalseQuandoStatusAtivo() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.isEncerrado()).isFalse();
        }

        @Test
        @DisplayName("Deve retornar false quando status é null")
        void deveRetornarFalseQuandoStatusNull() {
            relacionamento.setStatus(null);

            assertThat(relacionamento.isEncerrado()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método calcularDuracaoDias")
    class CalcularDuracaoDiasTests {

        @Test
        @DisplayName("Deve calcular duração até data fim quando informada")
        void deveCalcularDuracaoAteDataFimQuandoInformada() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 1, 31));

            long duracao = relacionamento.calcularDuracaoDias();

            assertThat(duracao).isEqualTo(30);
        }

        @Test
        @DisplayName("Deve calcular duração até hoje quando data fim não informada")
        void deveCalcularDuracaoAteHojeQuandoDataFimNaoInformada() {
            relacionamento.setDataInicio(LocalDate.now().minusDays(10));
            relacionamento.setDataFim(null);

            long duracao = relacionamento.calcularDuracaoDias();

            assertThat(duracao).isEqualTo(10);
        }

        @Test
        @DisplayName("Deve retornar zero quando dataInicio é hoje e dataFim é null")
        void deveRetornarZeroQuandoDataInicioHojeEDataFimNull() {
            relacionamento.setDataInicio(LocalDate.now());
            relacionamento.setDataFim(null);

            long duracao = relacionamento.calcularDuracaoDias();

            assertThat(duracao).isEqualTo(0);
        }

        @Test
        @DisplayName("Deve calcular duração de um ano")
        void deveCalcularDuracaoDeUmAno() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2025, 1, 1));

            long duracao = relacionamento.calcularDuracaoDias();

            assertThat(duracao).isEqualTo(366); // 2024 é bissexto
        }
    }

    @Nested
    @DisplayName("Testes de Método estaVigenteEm")
    class EstaVigenteEmTests {

        @Test
        @DisplayName("Deve estar vigente na data de início")
        void deveEstarVigenteNaDataInicio() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2024, 1, 1))).isTrue();
        }

        @Test
        @DisplayName("Deve estar vigente na data fim")
        void deveEstarVigenteNaDataFim() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2024, 12, 31))).isTrue();
        }

        @Test
        @DisplayName("Deve estar vigente entre data início e fim")
        void deveEstarVigenteEntreDataInicioEFim() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2024, 6, 15))).isTrue();
        }

        @Test
        @DisplayName("Não deve estar vigente antes da data início")
        void naoDeveEstarVigenteAntesDataInicio() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2023, 12, 31))).isFalse();
        }

        @Test
        @DisplayName("Não deve estar vigente depois da data fim")
        void naoDeveEstarVigenteDepoisDataFim() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2025, 1, 1))).isFalse();
        }

        @Test
        @DisplayName("Deve estar vigente quando dataFim é null e status é ATIVO")
        void deveEstarVigenteQuandoDataFimNullEStatusAtivo() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(null);
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.now())).isTrue();
        }

        @Test
        @DisplayName("Não deve estar vigente quando status não é ATIVO")
        void naoDeveEstarVigenteQuandoStatusNaoAtivo() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));
            relacionamento.setStatus(StatusRelacionamento.SUSPENSO);

            assertThat(relacionamento.estaVigenteEm(LocalDate.of(2024, 6, 15))).isFalse();
        }

        @Test
        @DisplayName("Não deve estar vigente quando data é null")
        void naoDeveEstarVigenteQuandoDataNull() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.estaVigenteEm(null)).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método temGapCobertura")
    class TemGapCoberturaTests {

        @Test
        @DisplayName("Deve ter gap quando status ATIVO mas dataFim no passado")
        void deveTerGapQuandoStatusAtivoMasDataFimNoPassado() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setDataFim(LocalDate.now().minusDays(10));

            assertThat(relacionamento.temGapCobertura()).isTrue();
        }

        @Test
        @DisplayName("Não deve ter gap quando status ATIVO e dataFim no futuro")
        void naoDeveTerGapQuandoStatusAtivoEDataFimNoFuturo() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setDataFim(LocalDate.now().plusDays(10));

            assertThat(relacionamento.temGapCobertura()).isFalse();
        }

        @Test
        @DisplayName("Não deve ter gap quando status ATIVO e dataFim é hoje")
        void naoDeveTerGapQuandoStatusAtivoEDataFimHoje() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setDataFim(LocalDate.now());

            assertThat(relacionamento.temGapCobertura()).isFalse();
        }

        @Test
        @DisplayName("Não deve ter gap quando status ATIVO e dataFim é null")
        void naoDeveTerGapQuandoStatusAtivoEDataFimNull() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setDataFim(null);

            assertThat(relacionamento.temGapCobertura()).isFalse();
        }

        @Test
        @DisplayName("Não deve ter gap quando status não é ATIVO")
        void naoDeveTerGapQuandoStatusNaoAtivo() {
            relacionamento.setStatus(StatusRelacionamento.ENCERRADO);
            relacionamento.setDataFim(LocalDate.now().minusDays(10));

            assertThat(relacionamento.temGapCobertura()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Callbacks JPA")
    class CallbacksJpaTests {

        @Test
        @DisplayName("onCreate deve definir dataCriacao")
        void onCreateDeveDefinirDataCriacao() {
            VeiculoApoliceRelacionamento novo = new VeiculoApoliceRelacionamento();
            novo.setVeiculoId("VEI-001");
            novo.setApoliceId("APO-001");
            novo.setDataInicio(LocalDate.now());

            // Simular @PrePersist
            novo.onCreate();

            assertThat(novo.getDataCriacao()).isNotNull();
            assertThat(novo.getDataCriacao()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("onCreate deve definir status ATIVO quando null")
        void onCreateDeveDefinirStatusAtivoQuandoNull() {
            VeiculoApoliceRelacionamento novo = new VeiculoApoliceRelacionamento();

            // Simular @PrePersist
            novo.onCreate();

            assertThat(novo.getStatus()).isEqualTo(StatusRelacionamento.ATIVO);
        }

        @Test
        @DisplayName("onCreate não deve sobrescrever status existente")
        void onCreateNaoDeveSobrescreverStatusExistente() {
            VeiculoApoliceRelacionamento novo = new VeiculoApoliceRelacionamento();
            novo.setStatus(StatusRelacionamento.SUSPENSO);

            // Simular @PrePersist
            novo.onCreate();

            assertThat(novo.getStatus()).isEqualTo(StatusRelacionamento.SUSPENSO);
        }

        @Test
        @DisplayName("onUpdate deve definir dataAtualizacao")
        void onUpdateDeveDefinirDataAtualizacao() {
            // Simular @PreUpdate
            relacionamento.onUpdate();

            assertThat(relacionamento.getDataAtualizacao()).isNotNull();
            assertThat(relacionamento.getDataAtualizacao()).isBeforeOrEqualTo(LocalDateTime.now());
        }
    }

    @Nested
    @DisplayName("Testes de equals e hashCode")
    class EqualsHashCodeTests {

        @Test
        @DisplayName("Deve ser igual a si mesmo")
        void deveSerIgualASiMesmo() {
            assertThat(relacionamento).isEqualTo(relacionamento);
        }

        @Test
        @DisplayName("Deve ser igual a outro com mesmo ID")
        void deveSerIgualAOutroComMesmoId() {
            relacionamento.setId("REL-001");

            VeiculoApoliceRelacionamento outro = new VeiculoApoliceRelacionamento();
            outro.setId("REL-001");

            assertThat(relacionamento).isEqualTo(outro);
            assertThat(relacionamento.hashCode()).isEqualTo(outro.hashCode());
        }

        @Test
        @DisplayName("Não deve ser igual a outro com ID diferente")
        void naoDeveSerIgualAOutroComIdDiferente() {
            relacionamento.setId("REL-001");

            VeiculoApoliceRelacionamento outro = new VeiculoApoliceRelacionamento();
            outro.setId("REL-002");

            assertThat(relacionamento).isNotEqualTo(outro);
        }

        @Test
        @DisplayName("Não deve ser igual a null")
        void naoDeveSerIgualANull() {
            assertThat(relacionamento).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Não deve ser igual a objeto de classe diferente")
        void naoDeveSerIgualAObjetoDeClasseDiferente() {
            assertThat(relacionamento).isNotEqualTo("String");
        }

        @Test
        @DisplayName("Deve ter hashCode consistente")
        void deveTerHashCodeConsistente() {
            relacionamento.setId("REL-001");

            int hash1 = relacionamento.hashCode();
            int hash2 = relacionamento.hashCode();

            assertThat(hash1).isEqualTo(hash2);
        }
    }

    @Nested
    @DisplayName("Testes de toString")
    class ToStringTests {

        @Test
        @DisplayName("toString deve conter informações principais")
        void toStringDeveConterInformacoesPrincipais() {
            relacionamento.setId("REL-001");
            relacionamento.setVeiculoPlaca("ABC1234");
            relacionamento.setApoliceNumero("APO-001");

            String resultado = relacionamento.toString();

            assertThat(resultado).contains("VeiculoApoliceRelacionamento");
            assertThat(resultado).contains("REL-001");
            assertThat(resultado).contains("ABC1234");
            assertThat(resultado).contains("APO-001");
        }

        @Test
        @DisplayName("toString deve incluir status")
        void toStringDeveIncluirStatus() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            String resultado = relacionamento.toString();

            assertThat(resultado).contains("ATIVO");
        }

        @Test
        @DisplayName("toString deve incluir datas")
        void toStringDeveIncluirDatas() {
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 12, 31));

            String resultado = relacionamento.toString();

            assertThat(resultado).contains("2024-01-01");
            assertThat(resultado).contains("2024-12-31");
        }
    }

    @Nested
    @DisplayName("Testes de Desassociação")
    class DesassociacaoTests {

        @Test
        @DisplayName("Deve armazenar motivo de desassociação")
        void deveArmazenarMotivoDesassociacao() {
            relacionamento.setMotivoDesassociacao("Venda do veículo");

            assertThat(relacionamento.getMotivoDesassociacao()).isEqualTo("Venda do veículo");
        }

        @Test
        @DisplayName("Deve permitir motivo de desassociação longo")
        void devePermitirMotivoDesassociacaoLongo() {
            String motivoLongo = "Desassociação solicitada pelo cliente devido à venda do veículo " +
                "para terceiro. Novo proprietário irá contratar apólice própria.";
            relacionamento.setMotivoDesassociacao(motivoLongo);

            assertThat(relacionamento.getMotivoDesassociacao()).isEqualTo(motivoLongo);
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar relacionamento completo ATIVO")
        void deveRepresentarRelacionamentoCompletoAtivo() {
            relacionamento.setId("REL-001");
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(null);
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setTipoRelacionamento(TipoRelacionamento.PRINCIPAL);

            assertThat(relacionamento.isAtivo()).isTrue();
            assertThat(relacionamento.isEncerrado()).isFalse();
            assertThat(relacionamento.estaVigenteEm(LocalDate.now())).isTrue();
            assertThat(relacionamento.temGapCobertura()).isFalse();
        }

        @Test
        @DisplayName("Deve representar relacionamento ENCERRADO")
        void deveRepresentarRelacionamentoEncerrado() {
            relacionamento.setId("REL-002");
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.of(2024, 6, 30));
            relacionamento.setStatus(StatusRelacionamento.ENCERRADO);
            relacionamento.setMotivoDesassociacao("Término de vigência");
            relacionamento.setOperadorDesassociacaoId("OP-456");

            assertThat(relacionamento.isAtivo()).isFalse();
            assertThat(relacionamento.isEncerrado()).isTrue();
            assertThat(relacionamento.estaVigenteEm(LocalDate.now())).isFalse();
            assertThat(relacionamento.getMotivoDesassociacao()).isNotEmpty();
        }

        @Test
        @DisplayName("Deve representar relacionamento TEMPORARIO")
        void deveRepresentarRelacionamentoTemporario() {
            relacionamento.setTipoRelacionamento(TipoRelacionamento.TEMPORARIO);
            relacionamento.setDataInicio(LocalDate.now().minusDays(15));
            relacionamento.setDataFim(LocalDate.now().plusDays(15));
            relacionamento.setStatus(StatusRelacionamento.ATIVO);

            assertThat(relacionamento.getTipoRelacionamento().isTemporario()).isTrue();
            assertThat(relacionamento.isAtivo()).isTrue();
            assertThat(relacionamento.estaVigenteEm(LocalDate.now())).isTrue();
        }

        @Test
        @DisplayName("Deve detectar gap de cobertura")
        void deveDetectarGapCobertura() {
            relacionamento.setStatus(StatusRelacionamento.ATIVO);
            relacionamento.setDataInicio(LocalDate.of(2024, 1, 1));
            relacionamento.setDataFim(LocalDate.now().minusDays(30));

            assertThat(relacionamento.isAtivo()).isTrue();
            assertThat(relacionamento.temGapCobertura()).isTrue();
            assertThat(relacionamento.estaVigenteEm(LocalDate.now())).isFalse();
        }
    }
}
