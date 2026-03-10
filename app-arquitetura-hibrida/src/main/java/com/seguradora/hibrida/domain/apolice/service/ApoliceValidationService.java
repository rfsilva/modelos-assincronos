package com.seguradora.hibrida.domain.apolice.service;

import com.seguradora.hibrida.domain.apolice.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Serviço de validações específicas para apólices.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
@Service
public class ApoliceValidationService {
    
    private static final Logger log = LoggerFactory.getLogger(ApoliceValidationService.class);
    
    // Limites por produto
    private static final BigDecimal VALOR_MINIMO_AUTO = new BigDecimal("5000.00");
    private static final BigDecimal VALOR_MAXIMO_AUTO = new BigDecimal("500000.00");
    private static final BigDecimal VALOR_MINIMO_RESIDENCIAL = new BigDecimal("10000.00");
    private static final BigDecimal VALOR_MAXIMO_RESIDENCIAL = new BigDecimal("2000000.00");
    
    /**
     * Valida a vigência da apólice.
     */
    public void validarVigencia(Vigencia vigencia) {
        if (vigencia == null) {
            throw new IllegalArgumentException("Vigência não pode ser nula");
        }
        
        LocalDate hoje = LocalDate.now();
        
        // Vigência não pode começar no passado (exceto hoje)
        if (vigencia.getInicio().isBefore(hoje)) {
            throw new IllegalArgumentException("Data de início da vigência não pode ser anterior a hoje");
        }
        
        // Vigência mínima de 30 dias
        if (vigencia.getDiasVigencia() < 30) {
            throw new IllegalArgumentException("Vigência mínima é de 30 dias");
        }
        
        // Vigência máxima de 5 anos
        if (vigencia.getDiasVigencia() > 1825) { // 5 anos
            throw new IllegalArgumentException("Vigência máxima é de 5 anos");
        }
        
        // Não pode começar mais de 90 dias no futuro
        if (vigencia.getInicio().isAfter(hoje.plusDays(90))) {
            throw new IllegalArgumentException("Data de início não pode ser superior a 90 dias no futuro");
        }
        
        log.debug("Vigência validada: {} a {}", vigencia.getInicio(), vigencia.getFim());
    }
    
    /**
     * Valida o valor segurado conforme o produto.
     */
    public void validarValorSegurado(Valor valorSegurado, String produto) {
        if (valorSegurado == null) {
            throw new IllegalArgumentException("Valor segurado não pode ser nulo");
        }
        
        if (!valorSegurado.isPositivo()) {
            throw new IllegalArgumentException("Valor segurado deve ser positivo");
        }
        
        BigDecimal valor = valorSegurado.getQuantia();
        
        switch (produto.toUpperCase()) {
            case "AUTO", "AUTOMOVEL" -> {
                if (valor.compareTo(VALOR_MINIMO_AUTO) < 0) {
                    throw new IllegalArgumentException(
                        String.format("Valor mínimo para seguro auto: %s", 
                                     Valor.reais(VALOR_MINIMO_AUTO).getFormatado())
                    );
                }
                if (valor.compareTo(VALOR_MAXIMO_AUTO) > 0) {
                    throw new IllegalArgumentException(
                        String.format("Valor máximo para seguro auto: %s", 
                                     Valor.reais(VALOR_MAXIMO_AUTO).getFormatado())
                    );
                }
            }
            case "RESIDENCIAL", "CASA" -> {
                if (valor.compareTo(VALOR_MINIMO_RESIDENCIAL) < 0) {
                    throw new IllegalArgumentException(
                        String.format("Valor mínimo para seguro residencial: %s", 
                                     Valor.reais(VALOR_MINIMO_RESIDENCIAL).getFormatado())
                    );
                }
                if (valor.compareTo(VALOR_MAXIMO_RESIDENCIAL) > 0) {
                    throw new IllegalArgumentException(
                        String.format("Valor máximo para seguro residencial: %s", 
                                     Valor.reais(VALOR_MAXIMO_RESIDENCIAL).getFormatado())
                    );
                }
            }
            default -> {
                // Validação genérica
                if (valor.compareTo(new BigDecimal("1000.00")) < 0) {
                    throw new IllegalArgumentException("Valor mínimo genérico: R$ 1.000,00");
                }
                if (valor.compareTo(new BigDecimal("10000000.00")) > 0) {
                    throw new IllegalArgumentException("Valor máximo genérico: R$ 10.000.000,00");
                }
            }
        }
        
        log.debug("Valor segurado validado: {} para produto {}", valorSegurado.getFormatado(), produto);
    }
    
    /**
     * Valida as coberturas conforme o produto.
     */
    public void validarCoberturas(List<Cobertura> coberturas, String produto) {
        if (coberturas == null || coberturas.isEmpty()) {
            throw new IllegalArgumentException("Deve haver pelo menos uma cobertura");
        }
        
        // Validar cada cobertura individualmente
        for (Cobertura cobertura : coberturas) {
            validarCobertura(cobertura, produto);
        }
        
        // Validar coberturas obrigatórias por produto
        validarCoberturasObrigatorias(coberturas, produto);
        
        log.debug("Coberturas validadas: {} coberturas para produto {}", coberturas.size(), produto);
    }
    
    private void validarCobertura(Cobertura cobertura, String produto) {
        if (cobertura == null) {
            throw new IllegalArgumentException("Cobertura não pode ser nula");
        }
        
        // Validar valor da cobertura
        if (!cobertura.getValorCobertura().isPositivo()) {
            throw new IllegalArgumentException("Valor da cobertura deve ser positivo");
        }
        
        // Validar franquia
        if (!cobertura.getFranquia().isPositivo()) {
            throw new IllegalArgumentException("Franquia deve ser positiva");
        }
        
        // Franquia não pode ser maior que 20% do valor da cobertura
        Valor limiteFranquia = cobertura.getValorCobertura().porcentagem(20.0);
        if (cobertura.getFranquia().ehMaiorQue(limiteFranquia)) {
            throw new IllegalArgumentException(
                String.format("Franquia não pode exceder 20%% do valor da cobertura (máximo: %s)", 
                             limiteFranquia.getFormatado())
            );
        }
        
        // Validar carência
        if (cobertura.getCarenciaDias() < 0 || cobertura.getCarenciaDias() > 365) {
            throw new IllegalArgumentException("Carência deve estar entre 0 e 365 dias");
        }
    }
    
    private void validarCoberturasObrigatorias(List<Cobertura> coberturas, String produto) {
        Set<TipoCobertura> tiposPresentes = coberturas.stream()
                .map(Cobertura::getTipo)
                .collect(Collectors.toSet());
        
        switch (produto.toUpperCase()) {
            case "AUTO", "AUTOMOVEL" -> {
                // Para auto, deve ter pelo menos TERCEIROS ou TOTAL
                if (!tiposPresentes.contains(TipoCobertura.TERCEIROS) && 
                    !tiposPresentes.contains(TipoCobertura.TOTAL)) {
                    throw new IllegalArgumentException(
                        "Seguro auto deve ter cobertura de TERCEIROS ou TOTAL"
                    );
                }
            }
            case "RESIDENCIAL", "CASA" -> {
                // Para residencial, deve ter pelo menos INCENDIO
                if (!tiposPresentes.contains(TipoCobertura.INCENDIO)) {
                    throw new IllegalArgumentException(
                        "Seguro residencial deve ter cobertura de INCÊNDIO"
                    );
                }
            }
        }
    }
    
    /**
     * Valida combinações de coberturas.
     */
    public void validarCombinacaoCoberturas(List<Cobertura> coberturas) {
        Set<TipoCobertura> tipos = coberturas.stream()
                .map(Cobertura::getTipo)
                .collect(Collectors.toSet());
        
        // TOTAL não pode ser combinada com coberturas específicas
        if (tipos.contains(TipoCobertura.TOTAL)) {
            Set<TipoCobertura> incompativeis = Set.of(
                TipoCobertura.TERCEIROS,
                TipoCobertura.ROUBO_FURTO,
                TipoCobertura.COLISAO,
                TipoCobertura.INCENDIO
            );
            
            for (TipoCobertura incompativel : incompativeis) {
                if (tipos.contains(incompativel)) {
                    throw new IllegalArgumentException(
                        String.format("Cobertura TOTAL não pode ser combinada com %s", incompativel)
                    );
                }
            }
        }
        
        // Verificar duplicatas
        if (tipos.size() != coberturas.size()) {
            throw new IllegalArgumentException("Não pode haver coberturas duplicadas");
        }
        
        log.debug("Combinação de coberturas validada: {}", tipos);
    }
    
    /**
     * Valida a forma de pagamento conforme o valor.
     */
    public void validarFormaPagamento(FormaPagamento formaPagamento, Valor valorSegurado) {
        if (formaPagamento == null) {
            throw new IllegalArgumentException("Forma de pagamento não pode ser nula");
        }
        
        // Para valores muito baixos, não permitir parcelamento
        if (valorSegurado.ehMenorQue(Valor.reais(new BigDecimal("2000.00"))) && 
            formaPagamento.isParcelado()) {
            throw new IllegalArgumentException(
                "Valores abaixo de R$ 2.000,00 devem ser pagos à vista"
            );
        }
        
        // Para valores muito altos, exigir pelo menos semestral
        if (valorSegurado.ehMaiorQue(Valor.reais(new BigDecimal("100000.00"))) && 
            formaPagamento == FormaPagamento.MENSAL) {
            throw new IllegalArgumentException(
                "Valores acima de R$ 100.000,00 não podem ser pagos mensalmente"
            );
        }
        
        log.debug("Forma de pagamento validada: {} para valor {}", 
                 formaPagamento, valorSegurado.getFormatado());
    }
    
    /**
     * Valida se a apólice pode ser alterada.
     */
    public void validarAlteracao(StatusApolice status, Vigencia vigencia) {
        if (status != StatusApolice.ATIVA) {
            throw new IllegalStateException("Apenas apólices ativas podem ser alteradas");
        }
        
        if (vigencia != null && !vigencia.estaVigenteHoje()) {
            throw new IllegalStateException("Apólice não está vigente");
        }
    }
    
    /**
     * Valida se a apólice pode ser cancelada.
     */
    public void validarCancelamento(StatusApolice status, Vigencia vigencia, LocalDate dataEfeito) {
        if (!status.podeSerCancelada()) {
            throw new IllegalStateException("Apólice não pode ser cancelada no status atual: " + status);
        }
        
        if (dataEfeito.isBefore(LocalDate.now())) {
            throw new IllegalArgumentException("Data de efeito não pode ser anterior a hoje");
        }
        
        if (vigencia != null && dataEfeito.isAfter(vigencia.getFim())) {
            throw new IllegalArgumentException("Data de efeito não pode ser posterior ao fim da vigência");
        }
    }
    
    /**
     * Valida se a apólice pode ser renovada.
     */
    public void validarRenovacao(StatusApolice status, Vigencia vigenciaAtual, Vigencia novaVigencia) {
        if (!status.podeSerRenovada()) {
            throw new IllegalStateException("Apólice não pode ser renovada no status atual: " + status);
        }
        
        if (vigenciaAtual != null && novaVigencia.getInicio().isBefore(vigenciaAtual.getFim().plusDays(1))) {
            throw new IllegalArgumentException("Nova vigência deve começar após o fim da vigência atual");
        }
        
        // Não permitir renovação muito antecipada (mais de 60 dias antes do vencimento)
        if (vigenciaAtual != null && LocalDate.now().isBefore(vigenciaAtual.getFim().minusDays(60))) {
            throw new IllegalArgumentException("Renovação só pode ser feita até 60 dias antes do vencimento");
        }
    }
}