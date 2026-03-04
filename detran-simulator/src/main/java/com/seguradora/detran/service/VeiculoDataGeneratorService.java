package com.seguradora.detran.service;

import com.github.javafaker.Faker;
import com.seguradora.detran.model.DetranResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

@Service
@RequiredArgsConstructor
@Slf4j
public class VeiculoDataGeneratorService {
    
    private final Faker faker = new Faker(Locale.forLanguageTag("pt-BR"));
    private final Random random = new Random();
    
    private static final String[] CORES = {
        "BRANCO", "PRATA", "PRETO", "AZUL", "VERMELHO", "CINZA", "VERDE", "AMARELO", "BEGE", "MARROM"
    };
    
    private static final String[] COMBUSTIVEIS = {
        "GASOLINA", "ETANOL", "FLEX", "DIESEL", "GNV", "ELETRICO", "HIBRIDO"
    };
    
    private static final String[] CATEGORIAS = {
        "PARTICULAR", "ALUGUEL", "APRENDIZAGEM", "OFICIAL", "DIPLOMATICO"
    };
    
    private static final String[] CARROCERIAS = {
        "SEDAN", "HATCH", "SUV", "PICKUP", "CONVERSIVEL", "COUPE", "WAGON", "VAN"
    };
    
    private static final String[] MARCAS_MODELOS = {
        "VOLKSWAGEN/GOL", "CHEVROLET/ONIX", "FIAT/UNO", "FORD/KA", "HYUNDAI/HB20",
        "TOYOTA/COROLLA", "HONDA/CIVIC", "NISSAN/MARCH", "RENAULT/SANDERO", "PEUGEOT/208"
    };
    
    /**
     * Gera dados completos do veículo baseado na placa e renavam
     */
    public DetranResponse generateVeiculoData(String placa, String renavam) {
        log.info("🚗 Gerando dados do veículo para placa: {} renavam: {}", placa, renavam);
        
        // Usar placa e renavam como seed para gerar dados consistentes
        Random seededRandom = new Random((placa + renavam).hashCode());
        
        int anoFabricacao = 2010 + seededRandom.nextInt(14); // 2010-2023
        int anoModelo = anoFabricacao + seededRandom.nextInt(2); // mesmo ano ou +1
        
        return DetranResponse.builder()
            .placa(placa)
            .renavam(renavam)
            .anoFabricacao(String.valueOf(anoFabricacao))
            .anoModelo(String.valueOf(anoModelo))
            .anoUltimoLicenciamento(String.valueOf(2023))
            .marcaModelo(MARCAS_MODELOS[seededRandom.nextInt(MARCAS_MODELOS.length)])
            .cor(CORES[seededRandom.nextInt(CORES.length)])
            .combustivel(COMBUSTIVEIS[seededRandom.nextInt(COMBUSTIVEIS.length)])
            .categoria(CATEGORIAS[seededRandom.nextInt(CATEGORIAS.length)])
            .carroceria(CARROCERIAS[seededRandom.nextInt(CARROCERIAS.length)])
            .especie("PASSAGEIRO")
            .proprietario(faker.name().fullName().toUpperCase())
            .municipio(faker.address().city().toUpperCase())
            .origem("DETRAN-" + faker.address().stateAbbr())
            .situacao(generateSituacao(seededRandom))
            .dataAquisicao(LocalDate.of(anoFabricacao, seededRandom.nextInt(12) + 1, seededRandom.nextInt(28) + 1))
            .lugares(String.valueOf(5))
            .potencia(String.valueOf(100 + seededRandom.nextInt(200)))
            .averbacaoJudicial(seededRandom.nextBoolean() ? "SIM" : "NAO")
            .gravame(seededRandom.nextDouble() < 0.3 ? "FINANCIAMENTO" : "LIVRE")
            .impedimentos(generateImpedimentos(seededRandom))
            .indicativoClonagem("NAO")
            .restricoes(generateRestricoes(seededRandom))
            .recadastradoDetran("SIM")
            .ultimoCrlv(String.valueOf(2023))
            .debitos(generateDebitos(seededRandom))
            .duas(generateDuas(seededRandom))
            .infracoes(generateInfracoes(seededRandom))
            .multas(generateMultas(seededRandom))
            .processos(generateProcessos(seededRandom))
            .recursos(generateRecursos(seededRandom))
            .build();
    }
    
    private String generateSituacao(Random random) {
        String[] situacoes = {"REGULAR", "IRREGULAR", "BLOQUEADO", "APREENDIDO"};
        double[] probabilidades = {0.7, 0.2, 0.08, 0.02}; // 70% regular, 20% irregular, etc.
        
        double rand = random.nextDouble();
        double acumulado = 0;
        
        for (int i = 0; i < situacoes.length; i++) {
            acumulado += probabilidades[i];
            if (rand <= acumulado) {
                return situacoes[i];
            }
        }
        
        return "REGULAR";
    }
    
    private String generateImpedimentos(Random random) {
        if (random.nextDouble() < 0.8) { // 80% sem impedimentos
            return "NENHUM";
        }
        
        String[] impedimentos = {
            "JUDICIAL", "ADMINISTRATIVO", "ROUBO/FURTO", "COMUNICACAO_VENDA"
        };
        
        return impedimentos[random.nextInt(impedimentos.length)];
    }
    
    private String generateRestricoes(Random random) {
        if (random.nextDouble() < 0.7) { // 70% sem restrições
            return "NENHUMA";
        }
        
        String[] restricoes = {
            "ALIENACAO_FIDUCIARIA", "ARRENDAMENTO", "RESERVA_DOMINIO", "PENHOR"
        };
        
        return restricoes[random.nextInt(restricoes.length)];
    }
    
    private List<DetranResponse.Debito> generateDebitos(Random random) {
        List<DetranResponse.Debito> debitos = new ArrayList<>();
        
        // 60% de chance de ter débitos
        if (random.nextDouble() < 0.6) {
            int numDebitos = 1 + random.nextInt(3); // 1 a 3 débitos
            
            for (int i = 0; i < numDebitos; i++) {
                debitos.add(DetranResponse.Debito.builder()
                    .descricao("IPVA " + (2022 + i))
                    .nominal("R$ " + (500 + random.nextInt(1000)) + ",00")
                    .atual("R$ " + (600 + random.nextInt(1200)) + ",00")
                    .corrigido("R$ " + (650 + random.nextInt(1300)) + ",00")
                    .juros("R$ " + (50 + random.nextInt(100)) + ",00")
                    .multa("R$ " + (25 + random.nextInt(50)) + ",00")
                    .desconto("R$ 0,00")
                    .vencimento(LocalDate.of(2023, 3, 31))
                    .build());
            }
        }
        
        return debitos;
    }
    
    private List<DetranResponse.Dua> generateDuas(Random random) {
        List<DetranResponse.Dua> duas = new ArrayList<>();
        
        // 40% de chance de ter DUAs
        if (random.nextDouble() < 0.4) {
            duas.add(DetranResponse.Dua.builder()
                .descricao("LICENCIAMENTO 2024")
                .nominal("R$ 150,00")
                .atual("R$ 150,00")
                .corrigido("R$ 150,00")
                .juros("R$ 0,00")
                .multa("R$ 0,00")
                .desconto("R$ 0,00")
                .url("https://detran.exemplo.com/dua/" + random.nextInt(100000))
                .vencimento(LocalDate.of(2024, 12, 31))
                .build());
        }
        
        return duas;
    }
    
    private List<DetranResponse.Infracao> generateInfracoes(Random random) {
        List<DetranResponse.Infracao> infracoes = new ArrayList<>();
        
        // 30% de chance de ter infrações
        if (random.nextDouble() < 0.3) {
            String[] tiposInfracao = {
                "EXCESSO DE VELOCIDADE", "ESTACIONAMENTO IRREGULAR", 
                "AVANCAR SINAL VERMELHO", "USO DE CELULAR"
            };
            
            String[] locais = {
                "AV PAULISTA, 1000 - SAO PAULO/SP",
                "RUA AUGUSTA, 500 - SAO PAULO/SP",
                "AV COPACABANA, 200 - RIO DE JANEIRO/RJ"
            };
            
            int numInfracoes = 1 + random.nextInt(2);
            
            for (int i = 0; i < numInfracoes; i++) {
                infracoes.add(DetranResponse.Infracao.builder()
                    .descricao(tiposInfracao[random.nextInt(tiposInfracao.length)])
                    .local(locais[random.nextInt(locais.length)])
                    .numeroAuto(String.valueOf(100000 + random.nextInt(900000)))
                    .build());
            }
        }
        
        return infracoes;
    }
    
    private List<DetranResponse.Multa> generateMultas(Random random) {
        List<DetranResponse.Multa> multas = new ArrayList<>();
        
        // 25% de chance de ter multas
        if (random.nextDouble() < 0.25) {
            multas.add(DetranResponse.Multa.builder()
                .descricao("EXCESSO DE VELOCIDADE")
                .local("AV PAULISTA, 1000 - SAO PAULO/SP")
                .numeroAuto(String.valueOf(100000 + random.nextInt(900000)))
                .boletoUrl("https://detran.exemplo.com/boleto/" + random.nextInt(100000))
                .build());
        }
        
        return multas;
    }
    
    private List<DetranResponse.Processo> generateProcessos(Random random) {
        List<DetranResponse.Processo> processos = new ArrayList<>();
        
        // 15% de chance de ter processos
        if (random.nextDouble() < 0.15) {
            processos.add(DetranResponse.Processo.builder()
                .processo(String.valueOf(2023000000L + random.nextInt(999999)))
                .motivo("TRANSFERENCIA DE PROPRIEDADE")
                .situacao("CONCLUIDO")
                .data(LocalDate.now().minusDays(random.nextInt(365)))
                .ciretran("CIRETRAN-" + (1 + random.nextInt(50)))
                .build());
        }
        
        return processos;
    }
    
    private List<DetranResponse.Recurso> generateRecursos(Random random) {
        List<DetranResponse.Recurso> recursos = new ArrayList<>();
        
        // 10% de chance de ter recursos
        if (random.nextDouble() < 0.1) {
            recursos.add(DetranResponse.Recurso.builder()
                .numeroProtocolo(String.valueOf(2023000000L + random.nextInt(999999)))
                .numeroAuto(String.valueOf(100000 + random.nextInt(900000)))
                .processo("RECURSO DE MULTA")
                .resultado("DEFERIDO")
                .detalhes("Recurso deferido por inconsistência na autuação")
                .build());
        }
        
        return recursos;
    }
}