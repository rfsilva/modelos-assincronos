package com.seguradora.hibrida.domain.apolice.service;

import com.seguradora.hibrida.domain.apolice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * Serviço responsável pelo cálculo de prêmios de apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
public class CalculadoraPremioService {
    
    private static final Logger log = LoggerFactory.getLogger(CalculadoraPremioService.class);
    
    // Fatores base para cálculo
    private static final BigDecimal FATOR_BASE_PREMIO = new BigDecimal("0.05"); // 5% do valor segurado
    private static final BigDecimal FATOR_DESCONTO_MULTIPLAS_COBERTURAS = new BigDecimal("0.10"); // 10% de desconto
    private static final BigDecimal FATOR_DESCONTO_MAXIMO = new BigDecimal("0.25"); // 25% desconto máximo
    
    // Cache de fatores de risco (em produção viria de base de dados)
    private final Map<String, BigDecimal> fatoresRiscoPorRegiao;
    private final Map<Integer, BigDecimal> fatoresRiscoPorIdade;
    
    public CalculadoraPremioService() {
        this.fatoresRiscoPorRegiao = inicializarFatoresRegiao();
        this.fatoresRiscoPorIdade = inicializarFatoresIdade();
    }
    
    /**
     * Calcula o prêmio completo de uma apólice.
     */
    public Premio calcularPremio(
            Valor valorSegurado,
            List<Cobertura> coberturas,
            FormaPagamento formaPagamento,
            LocalDate dataInicio) {
        
        log.debug("Iniciando cálculo de prêmio para valor segurado: {}", valorSegurado.getFormatado());
        
        validarParametros(valorSegurado, coberturas, formaPagamento, dataInicio);
        
        // 1. Calcular prêmio base
        Valor premioBase = calcularPremioBase(valorSegurado);
        log.debug("Prêmio base calculado: {}", premioBase.getFormatado());
        
        // 2. Aplicar fatores das coberturas
        Valor premioComCoberturas = aplicarFatoresCoberturas(premioBase, coberturas);
        log.debug("Prêmio com coberturas: {}", premioComCoberturas.getFormatado());
        
        // 3. Aplicar descontos por múltiplas coberturas
        Valor premioComDescontos = aplicarDescontoMultiplasCoberturas(premioComCoberturas, coberturas);
        log.debug("Prêmio com descontos: {}", premioComDescontos.getFormatado());
        
        // 4. Aplicar fatores de risco (simulado)
        Valor premioComRisco = aplicarFatoresRisco(premioComDescontos);
        log.debug("Prêmio com fatores de risco: {}", premioComRisco.getFormatado());
        
        // 5. Criar prêmio com forma de pagamento
        Premio premio = Premio.of(premioComRisco, formaPagamento, dataInicio);
        
        log.info("Prêmio calculado: {} (Forma: {}, Parcelas: {})", 
                premio.getValorTotal().getFormatado(), 
                formaPagamento.getDescricao(),
                premio.getNumeroParcelas());
        
        return premio;
    }
    
    /**
     * Calcula apenas o prêmio adicional para uma nova cobertura.
     */
    public Valor calcularPremioAdicionalCobertura(
            Valor valorSegurado,
            Cobertura novaCobertura,
            List<Cobertura> coberturasExistentes) {
        
        log.debug("Calculando prêmio adicional para cobertura: {}", novaCobertura.getTipo());
        
        // Calcular prêmio base da nova cobertura
        Valor premioBase = calcularPremioBase(valorSegurado);
        Valor premioCobertura = premioBase.multiplicar(novaCobertura.getTipo().getFatorPremio());
        
        // Aplicar desconto se já houver múltiplas coberturas
        if (coberturasExistentes.size() >= 2) {
            BigDecimal descontoAdicional = new BigDecimal("0.05"); // 5% adicional
            premioCobertura = premioCobertura.multiplicar(BigDecimal.ONE.subtract(descontoAdicional));
        }
        
        log.debug("Prêmio adicional calculado: {}", premioCobertura.getFormatado());
        return premioCobertura;
    }
    
    /**
     * Recalcula o prêmio após alteração no valor segurado.
     */
    public Premio recalcularPremio(
            Premio premioAtual,
            Valor novoValorSegurado,
            List<Cobertura> coberturas) {
        
        log.debug("Recalculando prêmio. Valor anterior: {}, Novo valor: {}", 
                premioAtual.getValorTotal().getFormatado(), 
                novoValorSegurado.getFormatado());
        
        // Recalcular com os mesmos parâmetros
        return calcularPremio(
                novoValorSegurado,
                coberturas,
                premioAtual.getFormaPagamento(),
                premioAtual.getDataPrimeiraParcela()
        );
    }
    
    /**
     * Calcula desconto para renovação baseado no histórico.
     */
    public BigDecimal calcularDescontoRenovacao(
            int anosRelacionamento,
            boolean temSinistros,
            boolean pagamentoEmDia) {
        
        BigDecimal desconto = BigDecimal.ZERO;
        
        // Desconto por tempo de relacionamento
        if (anosRelacionamento >= 5) {
            desconto = desconto.add(new BigDecimal("0.15")); // 15%
        } else if (anosRelacionamento >= 3) {
            desconto = desconto.add(new BigDecimal("0.10")); // 10%
        } else if (anosRelacionamento >= 1) {
            desconto = desconto.add(new BigDecimal("0.05")); // 5%
        }
        
        // Desconto por não ter sinistros
        if (!temSinistros) {
            desconto = desconto.add(new BigDecimal("0.10")); // 10%
        }
        
        // Desconto por pagamento em dia
        if (pagamentoEmDia) {
            desconto = desconto.add(new BigDecimal("0.05")); // 5%
        }
        
        // Limitar desconto máximo
        if (desconto.compareTo(FATOR_DESCONTO_MAXIMO) > 0) {
            desconto = FATOR_DESCONTO_MAXIMO;
        }
        
        log.debug("Desconto renovação calculado: {}% (Anos: {}, Sem sinistros: {}, Em dia: {})", 
                desconto.multiply(new BigDecimal("100")), anosRelacionamento, !temSinistros, pagamentoEmDia);
        
        return desconto;
    }
    
    // Métodos privados
    
    private void validarParametros(Valor valorSegurado, List<Cobertura> coberturas, FormaPagamento formaPagamento, LocalDate dataInicio) {
        if (valorSegurado == null || !valorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Valor segurado deve ser positivo");
        }
        
        if (coberturas == null || coberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula");
        }
        
        if (dataInicio == null) {
            throw new IllegalArgumentException("Data de início não pode ser nula");
        }
    }
    
    private Valor calcularPremioBase(Valor valorSegurado) {
        return valorSegurado.multiplicar(FATOR_BASE_PREMIO);
    }
    
    private Valor aplicarFatoresCoberturas(Valor premioBase, List<Cobertura> coberturas) {
        BigDecimal fatorTotal = BigDecimal.ZERO;
        
        for (Cobertura cobertura : coberturas) {
            if (cobertura.isAtiva()) {
                fatorTotal = fatorTotal.add(cobertura.getTipo().getFatorPremio());
            }
        }
        
        return premioBase.multiplicar(fatorTotal);
    }
    
    private Valor aplicarDescontoMultiplasCoberturas(Valor premio, List<Cobertura> coberturas) {
        long coberturasAtivas = coberturas.stream()
                .mapToLong(c -> c.isAtiva() ? 1 : 0)
                .sum();
        
        if (coberturasAtivas >= 3) {
            // Desconto de 10% para 3 ou mais coberturas
            BigDecimal desconto = BigDecimal.ONE.subtract(FATOR_DESCONTO_MULTIPLAS_COBERTURAS);
            return premio.multiplicar(desconto);
        }
        
        return premio;
    }
    
    private Valor aplicarFatoresRisco(Valor premio) {
        // Simulação de fatores de risco
        // Em produção, estes dados viriam de análise de perfil do segurado
        
        BigDecimal fatorRisco = BigDecimal.ONE;
        
        // Fator por região (simulado - São Paulo)
        fatorRisco = fatorRisco.multiply(fatoresRiscoPorRegiao.getOrDefault("SP", BigDecimal.ONE));
        
        // Fator por idade do segurado (simulado - 35 anos)
        fatorRisco = fatorRisco.multiply(fatoresRiscoPorIdade.getOrDefault(35, BigDecimal.ONE));
        
        return premio.multiplicar(fatorRisco);
    }
    
    private Map<String, BigDecimal> inicializarFatoresRegiao() {
        Map<String, BigDecimal> fatores = new HashMap<>();
        
        // Fatores de risco por estado (simulados)
        fatores.put("SP", new BigDecimal("1.20")); // São Paulo - maior risco
        fatores.put("RJ", new BigDecimal("1.15")); // Rio de Janeiro
        fatores.put("MG", new BigDecimal("1.05")); // Minas Gerais
        fatores.put("RS", new BigDecimal("1.00")); // Rio Grande do Sul - risco padrão
        fatores.put("SC", new BigDecimal("0.95")); // Santa Catarina - menor risco
        fatores.put("PR", new BigDecimal("1.00")); // Paraná
        
        return fatores;
    }
    
    private Map<Integer, BigDecimal> inicializarFatoresIdade() {
        Map<Integer, BigDecimal> fatores = new HashMap<>();
        
        // Fatores de risco por faixa etária (simulados)
        for (int idade = 18; idade <= 25; idade++) {
            fatores.put(idade, new BigDecimal("1.30")); // Jovens - maior risco
        }
        
        for (int idade = 26; idade <= 35; idade++) {
            fatores.put(idade, new BigDecimal("1.10")); // Adultos jovens
        }
        
        for (int idade = 36; idade <= 50; idade++) {
            fatores.put(idade, new BigDecimal("1.00")); // Adultos - risco padrão
        }
        
        for (int idade = 51; idade <= 65; idade++) {
            fatores.put(idade, new BigDecimal("1.05")); // Adultos maduros
        }
        
        for (int idade = 66; idade <= 80; idade++) {
            fatores.put(idade, new BigDecimal("1.20")); // Idosos - maior risco
        }
        
        return fatores;
    }
    
    /**
     * Calcula o IOF (Imposto sobre Operações Financeiras) do prêmio.
     */
    public Valor calcularIOF(Valor premioTotal) {
        // IOF para seguros: 7,38%
        BigDecimal aliquotaIOF = new BigDecimal("0.0738");
        return premioTotal.multiplicar(aliquotaIOF);
    }
    
    /**
     * Calcula o valor total com impostos.
     */
    public Valor calcularValorTotalComImpostos(Valor premioTotal) {
        Valor iof = calcularIOF(premioTotal);
        return premioTotal.somar(iof);
    }
    
    /**
     * Simula cálculo de prêmio com base em dados externos (FIPE, etc).
     */
    public Valor calcularPremioComDadosExternos(
            Valor valorSegurado,
            String marca,
            String modelo,
            int anoFabricacao,
            String cep) {
        
        log.debug("Calculando prêmio com dados externos: {} {} {} - CEP: {}", 
                marca, modelo, anoFabricacao, cep);
        
        Valor premioBase = calcularPremioBase(valorSegurado);
        
        // Fator por marca/modelo (simulado)
        BigDecimal fatorVeiculo = calcularFatorVeiculo(marca, modelo);
        
        // Fator por idade do veículo
        BigDecimal fatorIdade = calcularFatorIdadeVeiculo(anoFabricacao);
        
        // Fator por localização (CEP)
        BigDecimal fatorLocalizacao = calcularFatorLocalizacao(cep);
        
        BigDecimal fatorTotal = fatorVeiculo
                .multiply(fatorIdade)
                .multiply(fatorLocalizacao);
        
        return premioBase.multiplicar(fatorTotal);
    }
    
    private BigDecimal calcularFatorVeiculo(String marca, String modelo) {
        // Simulação - em produção consultaria tabela FIPE e estatísticas de roubo
        Map<String, BigDecimal> fatoresMarca = Map.of(
                "TOYOTA", new BigDecimal("0.95"),
                "HONDA", new BigDecimal("1.00"),
                "VOLKSWAGEN", new BigDecimal("1.05"),
                "FORD", new BigDecimal("1.10"),
                "CHEVROLET", new BigDecimal("1.15")
        );
        
        return fatoresMarca.getOrDefault(marca.toUpperCase(), BigDecimal.ONE);
    }
    
    private BigDecimal calcularFatorIdadeVeiculo(int anoFabricacao) {
        int idade = LocalDate.now().getYear() - anoFabricacao;
        
        if (idade <= 2) return new BigDecimal("0.90"); // Veículo novo
        if (idade <= 5) return new BigDecimal("1.00"); // Veículo seminovo
        if (idade <= 10) return new BigDecimal("1.10"); // Veículo usado
        return new BigDecimal("1.25"); // Veículo antigo
    }
    
    private BigDecimal calcularFatorLocalizacao(String cep) {
        // Simulação baseada nos primeiros dígitos do CEP
        if (cep == null || cep.length() < 2) {
            return BigDecimal.ONE;
        }
        
        String prefixo = cep.substring(0, 2);
        
        // Fatores por região (simulados)
        Map<String, BigDecimal> fatoresCep = Map.of(
                "01", new BigDecimal("1.30"), // São Paulo - Centro
                "02", new BigDecimal("1.25"), // São Paulo - Zona Norte
                "03", new BigDecimal("1.20"), // São Paulo - Zona Leste
                "04", new BigDecimal("1.15"), // São Paulo - Zona Sul
                "05", new BigDecimal("1.10"), // São Paulo - Zona Oeste
                "20", new BigDecimal("1.25"), // Rio de Janeiro
                "30", new BigDecimal("1.05")  // Belo Horizonte
        );
        
        return fatoresCep.getOrDefault(prefixo, BigDecimal.ONE);
    }
}