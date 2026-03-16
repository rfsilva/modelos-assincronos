package com.seguradora.hibrida.domain.veiculo.relationship.dto;

import com.seguradora.hibrida.domain.veiculo.relationship.model.StatusRelacionamento;
import com.seguradora.hibrida.domain.veiculo.relationship.model.TipoRelacionamento;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

/**
 * Testes unitários para {@link HistoricoRelacionamentoDTO}.
 *
 * @author Test Engineer
 * @since 1.0.0
 */
@DisplayName("HistoricoRelacionamentoDTO - Testes Unitários")
class HistoricoRelacionamentoDTOTest {

    @Nested
    @DisplayName("Testes de Criação com Builder")
    class CriacaoBuilderTests {

        @Test
        @DisplayName("Deve criar DTO com builder")
        void deveCriarDtoComBuilder() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .relacionamentoId("REL-001")
                .apoliceId("APO-001")
                .apoliceNumero("123456")
                .dataInicio(LocalDate.of(2024, 1, 1))
                .dataFim(LocalDate.of(2024, 12, 31))
                .status(StatusRelacionamento.ATIVO)
                .tipoRelacionamento(TipoRelacionamento.PRINCIPAL)
                .tipoCobertura("COMPREENSIVA")
                .duracaoDias(365)
                .build();

            assertThat(dto).isNotNull();
            assertThat(dto.getRelacionamentoId()).isEqualTo("REL-001");
            assertThat(dto.getDuracaoDias()).isEqualTo(365);
        }
    }

    @Nested
    @DisplayName("Testes de Método isAtual")
    class IsAtualTests {

        @Test
        @DisplayName("Deve ser atual quando status é ATIVO")
        void deveSerAtualQuandoStatusAtivo() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .status(StatusRelacionamento.ATIVO)
                .build();

            assertThat(dto.isAtual()).isTrue();
        }

        @Test
        @DisplayName("Não deve ser atual quando status é ENCERRADO")
        void naoDeveSerAtualQuandoStatusEncerrado() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .status(StatusRelacionamento.ENCERRADO)
                .build();

            assertThat(dto.isAtual()).isFalse();
        }

        @Test
        @DisplayName("Não deve ser atual quando status é CANCELADO")
        void naoDeveSerAtualQuandoStatusCancelado() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .status(StatusRelacionamento.CANCELADO)
                .build();

            assertThat(dto.isAtual()).isFalse();
        }
    }

    @Nested
    @DisplayName("Testes de Método getDuracaoFormatada")
    class GetDuracaoFormatadaTests {

        @Test
        @DisplayName("Deve formatar dias quando menor que 30")
        void deveFormatarDiasQuandoMenorQue30() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(15)
                .build();

            assertThat(dto.getDuracaoFormatada()).isEqualTo("15 dias");
        }

        @Test
        @DisplayName("Deve formatar 1 mês")
        void deveFormatar1Mes() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(30)
                .build();

            assertThat(dto.getDuracaoFormatada()).isEqualTo("1 mês");
        }

        @Test
        @DisplayName("Deve formatar múltiplos meses")
        void deveFormatarMultiplosMeses() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(90)
                .build();

            assertThat(dto.getDuracaoFormatada()).isEqualTo("3 meses");
        }

        @Test
        @DisplayName("Deve formatar 1 ano")
        void deveFormatar1Ano() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(365)
                .build();

            assertThat(dto.getDuracaoFormatada()).isEqualTo("1 ano");
        }

        @Test
        @DisplayName("Deve formatar anos e meses")
        void deveFormatarAnosEMeses() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(425) // 1 ano e 2 meses
                .build();

            String duracao = dto.getDuracaoFormatada();
            assertThat(duracao).contains("1 ano");
            assertThat(duracao).contains("2 meses");
        }

        @Test
        @DisplayName("Deve formatar 2 anos")
        void deveFormatar2Anos() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(730)
                .build();

            assertThat(dto.getDuracaoFormatada()).isEqualTo("2 anos");
        }

        @Test
        @DisplayName("Deve formatar 2 anos e 3 meses")
        void deveFormatar2AnosE3Meses() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .duracaoDias(820) // 2 anos e 3 meses
                .build();

            String duracao = dto.getDuracaoFormatada();
            assertThat(duracao).contains("2 anos");
            assertThat(duracao).contains("3 meses");
        }
    }

    @Nested
    @DisplayName("Testes de Cenários Completos")
    class CenariosCompletosTests {

        @Test
        @DisplayName("Deve representar relacionamento ativo atual")
        void deveRepresentarRelacionamentoAtivoAtual() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .relacionamentoId("REL-001")
                .apoliceId("APO-001")
                .apoliceNumero("2024-001234")
                .dataInicio(LocalDate.now().minusDays(90))
                .dataFim(null)
                .status(StatusRelacionamento.ATIVO)
                .tipoRelacionamento(TipoRelacionamento.PRINCIPAL)
                .tipoCobertura("COMPREENSIVA")
                .duracaoDias(90)
                .build();

            assertThat(dto.isAtual()).isTrue();
            assertThat(dto.getDataFim()).isNull();
            assertThat(dto.getDuracaoFormatada()).contains("meses");
        }

        @Test
        @DisplayName("Deve representar relacionamento encerrado histórico")
        void deveRepresentarRelacionamentoEncerradoHistorico() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .relacionamentoId("REL-002")
                .apoliceId("APO-002")
                .apoliceNumero("2023-005678")
                .dataInicio(LocalDate.of(2023, 1, 1))
                .dataFim(LocalDate.of(2023, 12, 31))
                .status(StatusRelacionamento.ENCERRADO)
                .tipoRelacionamento(TipoRelacionamento.PRINCIPAL)
                .tipoCobertura("COMPREENSIVA")
                .motivoDesassociacao("Término de vigência")
                .duracaoDias(365)
                .build();

            assertThat(dto.isAtual()).isFalse();
            assertThat(dto.getDataFim()).isNotNull();
            assertThat(dto.getMotivoDesassociacao()).isNotNull();
            assertThat(dto.getDuracaoFormatada()).contains("1 ano");
        }

        @Test
        @DisplayName("Deve representar relacionamento cancelado")
        void deveRepresentarRelacionamentoCancelado() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .relacionamentoId("REL-003")
                .status(StatusRelacionamento.CANCELADO)
                .motivoDesassociacao("Venda do veículo")
                .duracaoDias(45)
                .build();

            assertThat(dto.isAtual()).isFalse();
            assertThat(dto.getStatus()).isEqualTo(StatusRelacionamento.CANCELADO);
            assertThat(dto.getMotivoDesassociacao()).contains("Venda");
            assertThat(dto.getDuracaoFormatada()).isEqualTo("1 mês"); // 45/30 = 1 mês
        }
    }

    @Nested
    @DisplayName("Testes de Tipos de Relacionamento")
    class TiposRelacionamentoTests {

        @Test
        @DisplayName("Deve representar relacionamento PRINCIPAL")
        void deveRepresentarRelacionamentoPrincipal() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .tipoRelacionamento(TipoRelacionamento.PRINCIPAL)
                .build();

            assertThat(dto.getTipoRelacionamento()).isEqualTo(TipoRelacionamento.PRINCIPAL);
        }

        @Test
        @DisplayName("Deve representar relacionamento ADICIONAL")
        void deveRepresentarRelacionamentoAdicional() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .tipoRelacionamento(TipoRelacionamento.ADICIONAL)
                .build();

            assertThat(dto.getTipoRelacionamento()).isEqualTo(TipoRelacionamento.ADICIONAL);
        }

        @Test
        @DisplayName("Deve representar relacionamento TEMPORARIO")
        void deveRepresentarRelacionamentoTemporario() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .tipoRelacionamento(TipoRelacionamento.TEMPORARIO)
                .build();

            assertThat(dto.getTipoRelacionamento()).isEqualTo(TipoRelacionamento.TEMPORARIO);
        }

        @Test
        @DisplayName("Deve representar relacionamento SUBSTITUTO")
        void deveRepresentarRelacionamentoSubstituto() {
            HistoricoRelacionamentoDTO dto = HistoricoRelacionamentoDTO.builder()
                .tipoRelacionamento(TipoRelacionamento.SUBSTITUTO)
                .build();

            assertThat(dto.getTipoRelacionamento()).isEqualTo(TipoRelacionamento.SUBSTITUTO);
        }
    }
}
