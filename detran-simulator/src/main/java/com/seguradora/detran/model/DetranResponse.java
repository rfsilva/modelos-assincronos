package com.seguradora.detran.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(
    name = "DetranResponse",
    description = "Resposta completa com dados do veículo consultado no sistema Detran"
)
public class DetranResponse {
    
    @Schema(description = "Ano de fabricação do veículo", example = "2020")
    @JsonProperty("ano_fabricacao")
    private String anoFabricacao;
    
    @Schema(description = "Ano do modelo do veículo", example = "2020")
    @JsonProperty("ano_modelo")
    private String anoModelo;
    
    @Schema(description = "Ano do último licenciamento", example = "2024")
    @JsonProperty("ano_ultimo_licenciamento")
    private String anoUltimoLicenciamento;
    
    @Schema(description = "Informações sobre averbação judicial", example = "SEM AVERBACAO")
    @JsonProperty("averbacao_judicial")
    private String averbacaoJudicial;
    
    @Schema(description = "Tipo de carroceria do veículo", example = "HATCH", allowableValues = {"HATCH", "SEDAN", "SUV", "PICKUP", "CONVERSIVEL"})
    private String carroceria;
    
    @Schema(description = "Categoria do veículo", example = "PARTICULAR", allowableValues = {"PARTICULAR", "COMERCIAL", "OFICIAL"})
    private String categoria;
    
    @Schema(description = "Tipo de combustível", example = "FLEX", allowableValues = {"GASOLINA", "ETANOL", "FLEX", "DIESEL", "GNV", "ELETRICO"})
    private String combustivel;
    
    @Schema(description = "Cor do veículo", example = "BRANCO")
    private String cor;
    
    @Schema(description = "Data de aquisição do veículo", example = "2020-03-15")
    @JsonProperty("data_aquisicao")
    private LocalDate dataAquisicao;
    
    @Schema(description = "Lista de débitos pendentes do veículo")
    private List<Debito> debitos;
    
    @Schema(description = "Lista de DUAs (Documento Único de Arrecadação)")
    private List<Dua> duas;
    
    @Schema(description = "Espécie do veículo", example = "PASSAGEIRO", allowableValues = {"PASSAGEIRO", "CARGA", "MISTO", "ESPECIAL"})
    private String especie;
    
    @Schema(description = "Informações sobre gravame/financiamento", example = "SEM GRAVAME")
    private String gravame;
    
    @Schema(description = "Impedimentos do veículo", example = "SEM IMPEDIMENTOS")
    private String impedimentos;
    
    @Schema(description = "Indicativo de clonagem", example = "NAO")
    @JsonProperty("indicativo_clonagem")
    private String indicativoClonagem;
    
    @Schema(description = "Informações sobre contrato aditivo")
    @JsonProperty("informacoes_contrato_aditivo")
    private String informacoesContratoAditivo;
    
    @Schema(description = "Lista de infrações do veículo")
    private List<Infracao> infracoes;
    
    @Schema(description = "Número de lugares do veículo", example = "5")
    private String lugares;
    
    @Schema(description = "Marca e modelo do veículo", example = "VOLKSWAGEN/GOL")
    @JsonProperty("marca_modelo")
    private String marcaModelo;
    
    @Schema(description = "Lista de multas do veículo")
    private List<Multa> multas;
    
    @Schema(description = "Município de registro do veículo", example = "SAO PAULO")
    private String municipio;
    
    @Schema(description = "Origem do veículo", example = "NACIONAL")
    private String origem;
    
    @Schema(description = "Placa do veículo", example = "ABC1234", pattern = "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$")
    private String placa;
    
    @Schema(description = "Placa anterior do veículo (se houver)")
    @JsonProperty("placa_anterior")
    private String placaAnterior;
    
    @Schema(description = "Potência do motor", example = "1.0")
    private String potencia;
    
    @Schema(description = "Lista de processos relacionados ao veículo")
    private List<Processo> processos;
    
    @Schema(description = "Nome do proprietário do veículo", example = "JOAO DA SILVA SANTOS")
    private String proprietario;
    
    @Schema(description = "Nome do proprietário anterior (se houver)")
    @JsonProperty("proprietario_anterior")
    private String proprietarioAnterior;
    
    @Schema(description = "Indicativo se foi recadastrado no Detran", example = "SIM")
    @JsonProperty("recadastrado_detran")
    private String recadastradoDetran;
    
    @Schema(description = "Lista de recursos interpostos")
    private List<Recurso> recursos;
    
    @Schema(description = "RENAVAM do veículo", example = "12345678901", pattern = "^[0-9]{11}$")
    private String renavam;
    
    @Schema(description = "Restrições do veículo", example = "SEM RESTRICOES")
    private String restricoes;
    
    @Schema(description = "Situação atual do veículo", example = "REGULAR", allowableValues = {"REGULAR", "IRREGULAR", "BLOQUEADO", "APREENDIDO"})
    private String situacao;
    
    @Schema(description = "Tipo do veículo", example = "AUTOMOVEL")
    private String tipo;
    
    @Schema(description = "Último CRLV emitido")
    @JsonProperty("ultimo_crlv")
    private String ultimoCrlv;
    
    // Classes internas para estruturas complexas
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Debito", description = "Informações sobre débito do veículo")
    public static class Debito {
        @Schema(description = "Valor atual do débito", example = "150.00")
        private String atual;
        
        @Schema(description = "Valor corrigido do débito", example = "165.50")
        private String corrigido;
        
        @Schema(description = "Valor do desconto aplicável", example = "15.00")
        private String desconto;
        
        @Schema(description = "Descrição do débito", example = "IPVA 2024")
        private String descricao;
        
        @Schema(description = "Valor dos juros", example = "10.50")
        private String juros;
        
        @Schema(description = "Valor da multa", example = "5.00")
        private String multa;
        
        @Schema(description = "Valor nominal original", example = "150.00")
        private String nominal;
        
        @Schema(description = "Data de vencimento do débito", example = "2024-03-31")
        private LocalDate vencimento;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Dua", description = "Documento Único de Arrecadação")
    public static class Dua {
        @Schema(description = "Valor atual da DUA", example = "75.00")
        private String atual;
        
        @Schema(description = "Valor corrigido da DUA", example = "82.50")
        private String corrigido;
        
        @Schema(description = "Valor do desconto", example = "7.50")
        private String desconto;
        
        @Schema(description = "Descrição da DUA", example = "Taxa de Licenciamento")
        private String descricao;
        
        @Schema(description = "Valor dos juros", example = "5.25")
        private String juros;
        
        @Schema(description = "Valor da multa", example = "2.25")
        private String multa;
        
        @Schema(description = "Valor nominal", example = "75.00")
        private String nominal;
        
        @Schema(description = "URL para pagamento da DUA", example = "https://detran.sp.gov.br/dua/123456")
        private String url;
        
        @Schema(description = "Data de vencimento", example = "2024-04-30")
        private LocalDate vencimento;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Infracao", description = "Informações sobre infração de trânsito")
    public static class Infracao {
        @Schema(description = "Descrição da infração", example = "Excesso de velocidade")
        private String descricao;
        
        @Schema(description = "Local da infração", example = "Av. Paulista, 1000 - São Paulo/SP")
        private String local;
        
        @Schema(description = "Número do auto de infração", example = "SP123456789")
        @JsonProperty("numero_auto")
        private String numeroAuto;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Multa", description = "Informações sobre multa de trânsito")
    public static class Multa {
        @Schema(description = "URL do boleto para pagamento", example = "https://detran.sp.gov.br/boleto/123456")
        @JsonProperty("boleto_url")
        private String boletoUrl;
        
        @Schema(description = "Descrição da multa", example = "Estacionar em local proibido")
        private String descricao;
        
        @Schema(description = "Local da multa", example = "Rua Augusta, 500 - São Paulo/SP")
        private String local;
        
        @Schema(description = "Número do auto de infração", example = "SP987654321")
        @JsonProperty("numero_auto")
        private String numeroAuto;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Processo", description = "Processo administrativo relacionado ao veículo")
    public static class Processo {
        @Schema(description = "CIRETRAN responsável", example = "CIRETRAN São Paulo")
        private String ciretran;
        
        @Schema(description = "Data do processo", example = "2024-01-15")
        private LocalDate data;
        
        @Schema(description = "Motivo do processo", example = "Transferência de propriedade")
        private String motivo;
        
        @Schema(description = "Número do processo", example = "2024.001.123456")
        private String processo;
        
        @Schema(description = "Situação do processo", example = "EM ANDAMENTO", allowableValues = {"EM ANDAMENTO", "CONCLUIDO", "CANCELADO"})
        private String situacao;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(name = "Recurso", description = "Recurso interposto contra infração")
    public static class Recurso {
        @Schema(description = "Detalhes do recurso", example = "Recurso contra multa por excesso de velocidade")
        private String detalhes;
        
        @Schema(description = "Número do auto de infração", example = "SP123456789")
        @JsonProperty("numero_auto")
        private String numeroAuto;
        
        @Schema(description = "Número do protocolo do recurso", example = "REC2024001234")
        @JsonProperty("numero_protocolo")
        private String numeroProtocolo;
        
        @Schema(description = "Número do processo", example = "2024.002.654321")
        private String processo;
        
        @Schema(description = "Resultado do recurso", example = "DEFERIDO", allowableValues = {"DEFERIDO", "INDEFERIDO", "EM ANALISE"})
        private String resultado;
    }
}