package com.seguradora.hibrida.domain.documento.model;

import com.seguradora.hibrida.domain.sinistro.model.TipoSinistro;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("TipoDocumento - Testes Unitários")
class TipoDocumentoTest {

    @Nested
    @DisplayName("Valores do Enum")
    class ValoresEnum {

        @Test
        @DisplayName("Deve conter todos os valores esperados")
        void deveConterTodosValores() {
            TipoDocumento[] valores = TipoDocumento.values();

            assertThat(valores)
                    .hasSize(10)
                    .containsExactlyInAnyOrder(
                            TipoDocumento.BOLETIM_OCORRENCIA,
                            TipoDocumento.LAUDO_PERICIAL,
                            TipoDocumento.FOTO_DANOS,
                            TipoDocumento.ORCAMENTO,
                            TipoDocumento.NOTA_FISCAL,
                            TipoDocumento.COMPROVANTE_RESIDENCIA,
                            TipoDocumento.CNH,
                            TipoDocumento.CRLV,
                            TipoDocumento.DECLARACAO_TESTEMUNHA,
                            TipoDocumento.CARTA_FRANQUIA
                    );
        }

        @Test
        @DisplayName("Deve converter string para enum corretamente")
        void deveConverterStringParaEnum() {
            assertThat(TipoDocumento.valueOf("BOLETIM_OCORRENCIA")).isEqualTo(TipoDocumento.BOLETIM_OCORRENCIA);
            assertThat(TipoDocumento.valueOf("LAUDO_PERICIAL")).isEqualTo(TipoDocumento.LAUDO_PERICIAL);
            assertThat(TipoDocumento.valueOf("CNH")).isEqualTo(TipoDocumento.CNH);
        }
    }

    @Nested
    @DisplayName("Descrições")
    class Descricoes {

        @Test
        @DisplayName("Deve retornar descrições corretas")
        void deveRetornarDescricoesCorretas() {
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.getDescricao())
                    .isEqualTo("Boletim de Ocorrência");
            assertThat(TipoDocumento.LAUDO_PERICIAL.getDescricao())
                    .isEqualTo("Laudo Pericial");
            assertThat(TipoDocumento.CNH.getDescricao())
                    .isEqualTo("CNH");
            assertThat(TipoDocumento.CRLV.getDescricao())
                    .isEqualTo("CRLV");
        }
    }

    @Nested
    @DisplayName("Formatos Aceitos")
    class FormatosAceitos {

        @Test
        @DisplayName("BOLETIM_OCORRENCIA deve aceitar PDF e imagens")
        void boletimDeveAceitarPdfEImagens() {
            List<String> formatos = TipoDocumento.BOLETIM_OCORRENCIA.getFormatosAceitos();

            assertThat(formatos)
                    .containsExactlyInAnyOrder("application/pdf", "image/jpeg", "image/png");
        }

        @Test
        @DisplayName("LAUDO_PERICIAL deve aceitar apenas PDF")
        void laudoDeveAceitarApenasPdf() {
            List<String> formatos = TipoDocumento.LAUDO_PERICIAL.getFormatosAceitos();

            assertThat(formatos)
                    .containsExactly("application/pdf");
        }

        @Test
        @DisplayName("FOTO_DANOS deve aceitar formatos de imagem incluindo HEIC")
        void fotoDeveAceitarImagens() {
            List<String> formatos = TipoDocumento.FOTO_DANOS.getFormatosAceitos();

            assertThat(formatos)
                    .containsExactlyInAnyOrder("image/jpeg", "image/png", "image/heic");
        }

        @Test
        @DisplayName("NOTA_FISCAL deve aceitar PDF e XML")
        void notaFiscalDeveAceitarPdfEXml() {
            List<String> formatos = TipoDocumento.NOTA_FISCAL.getFormatosAceitos();

            assertThat(formatos)
                    .containsExactlyInAnyOrder("application/pdf", "application/xml");
        }

        @Test
        @DisplayName("ORCAMENTO deve aceitar PDF e Excel")
        void orcamentoDeveAceitarPdfEExcel() {
            List<String> formatos = TipoDocumento.ORCAMENTO.getFormatosAceitos();

            assertThat(formatos)
                    .containsExactlyInAnyOrder(
                            "application/pdf",
                            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
                    );
        }
    }

    @Nested
    @DisplayName("Validação de Formato")
    class ValidacaoFormato {

        @Test
        @DisplayName("Deve aceitar formato válido (case insensitive)")
        void deveAceitarFormatoValido() {
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.aceitaFormato("application/pdf")).isTrue();
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.aceitaFormato("APPLICATION/PDF")).isTrue();
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.aceitaFormato("image/jpeg")).isTrue();
        }

        @Test
        @DisplayName("Deve rejeitar formato inválido")
        void deveRejeitarFormatoInvalido() {
            assertThat(TipoDocumento.LAUDO_PERICIAL.aceitaFormato("image/jpeg")).isFalse();
            assertThat(TipoDocumento.CNH.aceitaFormato("application/xml")).isFalse();
        }
    }

    @Nested
    @DisplayName("Tamanho Máximo")
    class TamanhoMaximo {

        @Test
        @DisplayName("Deve retornar tamanhos máximos corretos")
        void deveRetornarTamanhosMaximosCorretos() {
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.getTamanhoMaximoMB()).isEqualTo(10);
            assertThat(TipoDocumento.LAUDO_PERICIAL.getTamanhoMaximoMB()).isEqualTo(15);
            assertThat(TipoDocumento.FOTO_DANOS.getTamanhoMaximoMB()).isEqualTo(5);
            assertThat(TipoDocumento.CNH.getTamanhoMaximoMB()).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("Requisito de Assinatura")
    class RequisitoAssinatura {

        @Test
        @DisplayName("Documentos críticos devem requerer assinatura")
        void documentosCriticosDevemRequererAssinatura() {
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.requerAssinatura()).isTrue();
            assertThat(TipoDocumento.LAUDO_PERICIAL.requerAssinatura()).isTrue();
            assertThat(TipoDocumento.NOTA_FISCAL.requerAssinatura()).isTrue();
            assertThat(TipoDocumento.DECLARACAO_TESTEMUNHA.requerAssinatura()).isTrue();
            assertThat(TipoDocumento.CARTA_FRANQUIA.requerAssinatura()).isTrue();
        }

        @Test
        @DisplayName("Documentos simples não devem requerer assinatura")
        void documentosSimplesNaoDevemRequererAssinatura() {
            assertThat(TipoDocumento.FOTO_DANOS.requerAssinatura()).isFalse();
            assertThat(TipoDocumento.ORCAMENTO.requerAssinatura()).isFalse();
            assertThat(TipoDocumento.COMPROVANTE_RESIDENCIA.requerAssinatura()).isFalse();
            assertThat(TipoDocumento.CNH.requerAssinatura()).isFalse();
            assertThat(TipoDocumento.CRLV.requerAssinatura()).isFalse();
        }
    }

    @Nested
    @DisplayName("Obrigatoriedade por Tipo de Sinistro")
    class ObrigatoriedadePorTipoSinistro {

        @Test
        @DisplayName("BOLETIM_OCORRENCIA é obrigatório para ROUBO_FURTO, VANDALISMO e INCENDIO")
        void boletimObrigatorioPara() {
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(TipoSinistro.ROUBO_FURTO)).isTrue();
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(TipoSinistro.VANDALISMO)).isTrue();
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(TipoSinistro.INCENDIO)).isTrue();
            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(TipoSinistro.COLISAO)).isFalse();
        }

        @Test
        @DisplayName("LAUDO_PERICIAL é obrigatório para COLISAO, INCENDIO e ENCHENTE")
        void laudoObrigatorioPara() {
            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(TipoSinistro.COLISAO)).isTrue();
            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(TipoSinistro.INCENDIO)).isTrue();
            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(TipoSinistro.ENCHENTE)).isTrue();
            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(TipoSinistro.ROUBO_FURTO)).isFalse();
        }

        @Test
        @DisplayName("FOTO_DANOS é obrigatório para todos os tipos de sinistro")
        void fotoDanosObrigatorioParaTodos() {
            for (TipoSinistro tipo : TipoSinistro.values()) {
                assertThat(TipoDocumento.FOTO_DANOS.isObrigatorio(tipo)).isTrue();
            }
        }

        @Test
        @DisplayName("CNH e CRLV são obrigatórios para todos os tipos de sinistro")
        void cnhCrlvObrigatoriosParaTodos() {
            for (TipoSinistro tipo : TipoSinistro.values()) {
                assertThat(TipoDocumento.CNH.isObrigatorio(tipo)).isTrue();
                assertThat(TipoDocumento.CRLV.isObrigatorio(tipo)).isTrue();
            }
        }

        @Test
        @DisplayName("CARTA_FRANQUIA é obrigatório para COLISAO e TERCEIROS")
        void cartaFranquiaObrigatorioPara() {
            assertThat(TipoDocumento.CARTA_FRANQUIA.isObrigatorio(TipoSinistro.COLISAO)).isTrue();
            assertThat(TipoDocumento.CARTA_FRANQUIA.isObrigatorio(TipoSinistro.TERCEIROS)).isTrue();
            assertThat(TipoDocumento.CARTA_FRANQUIA.isObrigatorio(TipoSinistro.ROUBO_FURTO)).isFalse();
        }
    }

    @Nested
    @DisplayName("Tipos de Sinistro Obrigatório")
    class TiposSinistroObrigatorio {

        @Test
        @DisplayName("Deve retornar set imutável de tipos de sinistro obrigatório")
        void deveRetornarSetImutavel() {
            Set<TipoSinistro> tipos = TipoDocumento.BOLETIM_OCORRENCIA.getTiposSinistroObrigatorio();

            assertThat(tipos)
                    .containsExactlyInAnyOrder(
                            TipoSinistro.ROUBO_FURTO,
                            TipoSinistro.VANDALISMO,
                            TipoSinistro.INCENDIO
                    );
        }

        @Test
        @DisplayName("Deve retornar todos os tipos para documentos universais")
        void deveRetornarTodosTiposParaDocumentosUniversais() {
            Set<TipoSinistro> tipos = TipoDocumento.FOTO_DANOS.getTiposSinistroObrigatorio();

            assertThat(tipos).hasSize(TipoSinistro.values().length);
        }
    }

    @Nested
    @DisplayName("Cenários Integrados")
    class CenariosIntegrados {

        @Test
        @DisplayName("Documento de COLISAO deve ter os documentos obrigatórios corretos")
        void documentosObrigatoriosParaColisao() {
            TipoSinistro colisao = TipoSinistro.COLISAO;

            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.FOTO_DANOS.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.ORCAMENTO.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.NOTA_FISCAL.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.CNH.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.CRLV.isObrigatorio(colisao)).isTrue();
            assertThat(TipoDocumento.CARTA_FRANQUIA.isObrigatorio(colisao)).isTrue();

            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(colisao)).isFalse();
        }

        @Test
        @DisplayName("Documento de ROUBO_FURTO deve ter os documentos obrigatórios corretos")
        void documentosObrigatoriosParaRouboFurto() {
            TipoSinistro rouboFurto = TipoSinistro.ROUBO_FURTO;

            assertThat(TipoDocumento.BOLETIM_OCORRENCIA.isObrigatorio(rouboFurto)).isTrue();
            assertThat(TipoDocumento.FOTO_DANOS.isObrigatorio(rouboFurto)).isTrue();
            assertThat(TipoDocumento.CNH.isObrigatorio(rouboFurto)).isTrue();
            assertThat(TipoDocumento.CRLV.isObrigatorio(rouboFurto)).isTrue();

            assertThat(TipoDocumento.LAUDO_PERICIAL.isObrigatorio(rouboFurto)).isFalse();
        }
    }
}
