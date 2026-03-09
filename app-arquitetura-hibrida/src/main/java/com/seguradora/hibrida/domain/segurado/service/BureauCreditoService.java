package com.seguradora.hibrida.domain.segurado.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

/**
 * Serviço para consulta a bureaus de crédito (Serasa/SPC).
 * 
 * <p>Implementação aprimorada para validação de CPF conforme US010:
 * <ul>
 *   <li>Validação de CPF com score</li>
 *   <li>Cache de consultas (TTL 1 hora)</li>
 *   <li>Simulação realista de restrições</li>
 *   <li>Dados adicionais do bureau</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Service
public class BureauCreditoService {
    
    private final Random random = new Random();
    
    /**
     * Valida CPF em bureaus de crédito com cache.
     * 
     * @param cpf CPF a ser validado (apenas números)
     * @return resultado da validação
     */
    @Cacheable(value = "bureau-validation", key = "#cpf")
    public BureauValidationResult validarCpf(String cpf) {
        log.info("Consultando bureaus de crédito para CPF: {}", 
                cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.***.***-$4"));
        
        // Simular delay de consulta externa
        try {
            Thread.sleep(100 + random.nextInt(200)); // 100-300ms
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return BureauValidationResult.erro("Consulta interrompida");
        }
        
        // Mock de validação - alguns CPFs com restrições conhecidas
        if (cpf.equals("11111111111") || cpf.equals("22222222222")) {
            return BureauValidationResult.falha("CPF com restrições financeiras ativas");
        }
        
        if (cpf.equals("33333333333")) {
            return BureauValidationResult.falha("CPF em lista de inadimplentes");
        }
        
        if (cpf.equals("44444444444")) {
            return BureauValidationResult.falha("Histórico de inadimplência recente");
        }
        
        if (cpf.equals("99999999999")) {
            return BureauValidationResult.erro("CPF não encontrado na base de dados");
        }
        
        // Simular 8% de CPFs com restrições aleatórias
        if (random.nextInt(100) < 8) {
            String[] motivos = {
                "Restrições financeiras ativas",
                "Histórico de inadimplência",
                "Pendências em órgãos de proteção ao crédito",
                "Score de crédito muito baixo",
                "Múltiplas consultas recentes"
            };
            return BureauValidationResult.falha(motivos[random.nextInt(motivos.length)]);
        }
        
        // Maioria dos CPFs são válidos - gerar score realista
        int score = gerarScoreRealista(cpf);
        
        BureauValidationResult resultado = BureauValidationResult.sucesso(score);
        resultado.setDadosAdicionais(gerarDadosAdicionais(cpf, score));
        
        log.info("CPF validado com sucesso - Score: {}", score);
        return resultado;
    }
    
    /**
     * Gera score de crédito realista baseado no CPF.
     */
    private int gerarScoreRealista(String cpf) {
        // Usar hash do CPF para gerar score consistente
        int hash = cpf.hashCode();
        Random cpfRandom = new Random(hash);
        
        // Distribuição realista de scores:
        // 20% - Score alto (700-1000)
        // 50% - Score médio (400-699)
        // 30% - Score baixo (100-399)
        
        int categoria = cpfRandom.nextInt(100);
        
        if (categoria < 20) {
            // Score alto
            return 700 + cpfRandom.nextInt(301); // 700-1000
        } else if (categoria < 70) {
            // Score médio
            return 400 + cpfRandom.nextInt(300); // 400-699
        } else {
            // Score baixo
            return 100 + cpfRandom.nextInt(300); // 100-399
        }
    }
    
    /**
     * Gera dados adicionais do bureau.
     */
    private Map<String, Object> gerarDadosAdicionais(String cpf, int score) {
        Map<String, Object> dados = new HashMap<>();
        Random cpfRandom = new Random(cpf.hashCode());
        
        dados.put("bureau", "SERASA_SPC");
        dados.put("score", score);
        dados.put("faixa_score", determinarFaixaScore(score));
        dados.put("consultas_recentes", cpfRandom.nextInt(5));
        dados.put("relacionamento_bancario", cpfRandom.nextBoolean());
        dados.put("tempo_relacionamento_meses", cpfRandom.nextInt(120));
        
        return dados;
    }
    
    /**
     * Determina a faixa do score.
     */
    private String determinarFaixaScore(int score) {
        if (score >= 800) return "EXCELENTE";
        if (score >= 700) return "MUITO_BOM";
        if (score >= 600) return "BOM";
        if (score >= 500) return "REGULAR";
        if (score >= 400) return "RUIM";
        return "MUITO_RUIM";
    }
    
    /**
     * Consulta score de crédito do CPF (método adicional).
     * 
     * @param cpf CPF a ser consultado
     * @return score de crédito (0-1000)
     */
    @Cacheable(value = "score-cache", key = "#cpf")
    public int consultarScore(String cpf) {
        log.info("Consultando score de crédito para CPF: {}", 
                cpf.replaceAll("(\\d{3})(\\d{3})(\\d{3})(\\d{2})", "$1.***.***-$4"));
        
        return gerarScoreRealista(cpf);
    }
}