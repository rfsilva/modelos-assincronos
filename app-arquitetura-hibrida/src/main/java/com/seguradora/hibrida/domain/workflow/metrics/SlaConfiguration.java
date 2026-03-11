package com.seguradora.hibrida.domain.workflow.metrics;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuração de SLAs para workflows de sinistros.
 * Define prazos e alertas para diferentes tipos de sinistros.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "workflow.sla")
public class SlaConfiguration {

    /**
     * SLA para sinistro simples em horas.
     */
    private int sinistroSimplesHoras = 48;

    /**
     * SLA para sinistro complexo em dias úteis.
     */
    private int sinistroComplexoDiasUteis = 5;

    /**
     * SLA para roubo/furto em dias úteis.
     */
    private int rouboFurtoDiasUteis = 10;

    /**
     * SLA para terceiros em dias úteis.
     */
    private int terceirosDiasUteis = 15;

    /**
     * Configurações de alertas.
     */
    private Alertas alertas = new Alertas();

    /**
     * Configurações de escalação.
     */
    private Escalacao escalacao = new Escalacao();

    /**
     * SLAs customizados por tipo de sinistro.
     */
    private Map<String, Integer> customizados = new HashMap<>();

    /**
     * Obtém o SLA em horas para um tipo de sinistro.
     *
     * @param tipoSinistro tipo do sinistro
     * @return SLA em horas
     */
    public int getSlaHoras(String tipoSinistro) {
        // Verifica se há SLA customizado
        if (customizados.containsKey(tipoSinistro)) {
            return customizados.get(tipoSinistro);
        }

        // SLAs padrão
        switch (tipoSinistro.toUpperCase()) {
            case "SIMPLES":
                return sinistroSimplesHoras;
            case "COMPLEXO":
                return sinistroComplexoDiasUteis * 24;
            case "ROUBO_FURTO":
                return rouboFurtoDiasUteis * 24;
            case "TERCEIROS":
                return terceirosDiasUteis * 24;
            default:
                return sinistroComplexoDiasUteis * 24; // Default
        }
    }

    /**
     * Verifica se um tempo excede o SLA.
     *
     * @param tipoSinistro tipo do sinistro
     * @param horasDecorridas horas decorridas
     * @return true se excedeu o SLA
     */
    public boolean excedeuSla(String tipoSinistro, long horasDecorridas) {
        return horasDecorridas > getSlaHoras(tipoSinistro);
    }

    /**
     * Calcula o percentual de consumo do SLA.
     *
     * @param tipoSinistro tipo do sinistro
     * @param horasDecorridas horas decorridas
     * @return percentual (0-100+)
     */
    public double calcularPercentualSla(String tipoSinistro, long horasDecorridas) {
        int slaHoras = getSlaHoras(tipoSinistro);
        return (horasDecorridas * 100.0) / slaHoras;
    }

    /**
     * Verifica se deve emitir alerta baseado no percentual de consumo.
     *
     * @param percentualSla percentual do SLA consumido
     * @return nível de alerta ou null
     */
    public NivelAlerta getNivelAlerta(double percentualSla) {
        if (percentualSla >= 100) {
            return NivelAlerta.CRITICO;
        } else if (percentualSla >= alertas.percentual80) {
            return NivelAlerta.ALTO;
        } else if (percentualSla >= alertas.percentual50) {
            return NivelAlerta.MEDIO;
        }
        return null;
    }

    /**
     * Classe para configurações de alertas.
     */
    @Data
    public static class Alertas {
        /**
         * Percentual para alerta de nível médio (padrão: 50%).
         */
        private int percentual50 = 50;

        /**
         * Percentual para alerta de nível alto (padrão: 80%).
         */
        private int percentual80 = 80;

        /**
         * Habilitar notificações por email.
         */
        private boolean emailHabilitado = true;

        /**
         * Habilitar notificações push.
         */
        private boolean pushHabilitado = true;

        /**
         * Intervalo mínimo entre alertas em minutos.
         */
        private int intervaloMinutosEntreAlertas = 60;
    }

    /**
     * Classe para configurações de escalação.
     */
    @Data
    public static class Escalacao {
        /**
         * Habilitar escalação automática.
         */
        private boolean automatica = true;

        /**
         * Percentual do SLA para escalação automática.
         */
        private int percentualParaEscalar = 90;

        /**
         * Escalar para gestor após X horas sem movimentação.
         */
        private int horasSemMovimentacao = 24;
    }

    /**
     * Enum para níveis de alerta.
     */
    public enum NivelAlerta {
        MEDIO("Médio"),
        ALTO("Alto"),
        CRITICO("Crítico");

        private final String descricao;

        NivelAlerta(String descricao) {
            this.descricao = descricao;
        }

        public String getDescricao() {
            return descricao;
        }
    }
}
