package com.seguradora.hibrida.domain.workflow.defaults;

import com.seguradora.hibrida.domain.workflow.model.*;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * Templates de workflows padrão para diferentes tipos de sinistros.
 * Fornece workflows pré-configurados com etapas, timeouts e aprovações.
 *
 * @author Sistema de Workflow
 * @version 1.0
 */
@Component
public class WorkflowTemplates {

    /**
     * Cria workflow para sinistros simples (valor menor que R$ 5.000).
     * SLA: 48 horas
     * Etapas: Validação automática, Análise simples, Aprovação nível 1
     *
     * @return definição do workflow
     */
    public static WorkflowDefinition criarWorkflowSinistroSimples() {
        WorkflowDefinition workflow = WorkflowDefinition.builder()
                .nome("Sinistro Simples")
                .versao(1)
                .tipoSinistro("SIMPLES")
                .descricao("Workflow para sinistros de baixo valor com processamento simplificado")
                .ativo(true)
                .etapas(new ArrayList<>())
                .build();

        // Etapa 1: Validação Automática
        EtapaWorkflow etapa1 = EtapaWorkflow.builder()
                .nome("Validação Automática")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .timeoutMinutos(30)
                .descricao("Validação automática de documentos e dados")
                .build();
        etapa1.adicionarAcao("VALIDAR:documentos");
        etapa1.adicionarAcao("VALIDAR:dadosSegurado");
        etapa1.adicionarAcao("CALCULAR:valor_indenizacao");

        // Etapa 2: Análise Rápida
        EtapaWorkflow etapa2 = EtapaWorkflow.builder()
                .nome("Análise Rápida")
                .tipo(TipoEtapa.MANUAL)
                .ordem(2)
                .timeoutMinutos(120)
                .descricao("Análise rápida por analista")
                .build();
        etapa2.adicionarAcao("MANUAL:Revisar documentação");
        etapa2.adicionarAcao("MANUAL:Validar fotos do sinistro");

        // Etapa 3: Aprovação Nível 1
        EtapaWorkflow etapa3 = EtapaWorkflow.builder()
                .nome("Aprovação Analista")
                .tipo(TipoEtapa.APROVACAO)
                .ordem(3)
                .timeoutMinutos(180)
                .nivelAprovacao(NivelAprovacao.NIVEL_1_ANALISTA)
                .descricao("Aprovação por analista de sinistros")
                .build();

        // Etapa 4: Processamento Final
        EtapaWorkflow etapa4 = EtapaWorkflow.builder()
                .nome("Processamento Final")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(4)
                .timeoutMinutos(15)
                .descricao("Finalização e notificações")
                .build();
        etapa4.adicionarAcao("NOTIFICAR:segurado");
        etapa4.adicionarAcao("REGISTRAR:sinistro_aprovado");

        workflow.adicionarEtapa(etapa1);
        workflow.adicionarEtapa(etapa2);
        workflow.adicionarEtapa(etapa3);
        workflow.adicionarEtapa(etapa4);

        return workflow;
    }

    /**
     * Cria workflow para sinistros complexos (valor entre R$ 5.000 e R$ 50.000).
     * SLA: 5 dias úteis
     * Etapas: Validação completa, Perícia, Aprovação multinível
     *
     * @return definição do workflow
     */
    public static WorkflowDefinition criarWorkflowSinistroComplexo() {
        WorkflowDefinition workflow = WorkflowDefinition.builder()
                .nome("Sinistro Complexo")
                .versao(1)
                .tipoSinistro("COMPLEXO")
                .descricao("Workflow para sinistros de médio/alto valor com análise detalhada")
                .ativo(true)
                .etapas(new ArrayList<>())
                .build();

        // Etapa 1: Validação Completa
        EtapaWorkflow etapa1 = EtapaWorkflow.builder()
                .nome("Validação Completa")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .timeoutMinutos(60)
                .build();
        etapa1.adicionarAcao("VALIDAR:documentos");
        etapa1.adicionarAcao("VALIDAR:apolice");
        etapa1.adicionarAcao("INTEGRAR:CONSULTAR_BUREAU");

        // Etapa 2: Análise Técnica
        EtapaWorkflow etapa2 = EtapaWorkflow.builder()
                .nome("Análise Técnica")
                .tipo(TipoEtapa.MANUAL)
                .ordem(2)
                .timeoutMinutos(480)
                .build();
        etapa2.adicionarAcao("MANUAL:Análise detalhada de documentos");
        etapa2.adicionarAcao("MANUAL:Verificação de cobertura");

        // Etapa 3: Perícia
        EtapaWorkflow etapa3 = EtapaWorkflow.builder()
                .nome("Perícia Técnica")
                .tipo(TipoEtapa.INTEGRACAO)
                .ordem(3)
                .timeoutMinutos(2880)
                .build();
        etapa3.adicionarAcao("INTEGRAR:SOLICITAR_PERICIA");
        etapa3.adicionarAcao("INTEGRAR:CALCULAR_DEPRECIACAO");

        // Etapa 4: Cálculo de Indenização
        EtapaWorkflow etapa4 = EtapaWorkflow.builder()
                .nome("Cálculo de Indenização")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(4)
                .timeoutMinutos(30)
                .build();
        etapa4.adicionarAcao("CALCULAR:valor_indenizacao");
        etapa4.adicionarAcao("INTEGRAR:VERIFICAR_FRANQUIA");

        // Etapa 5: Aprovação Supervisor
        EtapaWorkflow etapa5 = EtapaWorkflow.builder()
                .nome("Aprovação Supervisor")
                .tipo(TipoEtapa.APROVACAO)
                .ordem(5)
                .timeoutMinutos(720)
                .nivelAprovacao(NivelAprovacao.NIVEL_2_SUPERVISOR)
                .build();

        // Etapa 6: Finalização
        EtapaWorkflow etapa6 = EtapaWorkflow.builder()
                .nome("Finalização")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(6)
                .timeoutMinutos(30)
                .build();
        etapa6.adicionarAcao("NOTIFICAR:segurado");
        etapa6.adicionarAcao("NOTIFICAR:peritos");
        etapa6.adicionarAcao("REGISTRAR:sinistro_finalizado");

        workflow.adicionarEtapa(etapa1);
        workflow.adicionarEtapa(etapa2);
        workflow.adicionarEtapa(etapa3);
        workflow.adicionarEtapa(etapa4);
        workflow.adicionarEtapa(etapa5);
        workflow.adicionarEtapa(etapa6);

        return workflow;
    }

    /**
     * Cria workflow específico para roubo/furto.
     * SLA: 10 dias úteis
     * Etapas: BO, Investigação, Perícia especializada
     *
     * @return definição do workflow
     */
    public static WorkflowDefinition criarWorkflowRouboFurto() {
        WorkflowDefinition workflow = WorkflowDefinition.builder()
                .nome("Roubo/Furto")
                .versao(1)
                .tipoSinistro("ROUBO_FURTO")
                .descricao("Workflow especializado para casos de roubo e furto")
                .ativo(true)
                .etapas(new ArrayList<>())
                .build();

        // Etapa 1: Validação BO
        EtapaWorkflow etapa1 = EtapaWorkflow.builder()
                .nome("Validação de Boletim de Ocorrência")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .timeoutMinutos(60)
                .build();
        etapa1.adicionarAcao("VALIDAR:boletim_ocorrencia");
        etapa1.adicionarAcao("INTEGRAR:VALIDAR_DOCUMENTOS");

        // Etapa 2: Investigação
        EtapaWorkflow etapa2 = EtapaWorkflow.builder()
                .nome("Investigação")
                .tipo(TipoEtapa.MANUAL)
                .ordem(2)
                .timeoutMinutos(4320)
                .build();
        etapa2.adicionarAcao("MANUAL:Análise de consistência");
        etapa2.adicionarAcao("MANUAL:Verificação de histórico");

        // Etapa 3: Perícia Especializada
        EtapaWorkflow etapa3 = EtapaWorkflow.builder()
                .nome("Perícia Especializada")
                .tipo(TipoEtapa.INTEGRACAO)
                .ordem(3)
                .timeoutMinutos(5760)
                .build();
        etapa3.adicionarAcao("INTEGRAR:SOLICITAR_PERICIA");

        // Etapa 4: Aprovação Gerente
        EtapaWorkflow etapa4 = EtapaWorkflow.builder()
                .nome("Aprovação Gerencial")
                .tipo(TipoEtapa.APROVACAO)
                .ordem(4)
                .timeoutMinutos(1440)
                .nivelAprovacao(NivelAprovacao.NIVEL_3_GERENTE)
                .build();

        // Etapa 5: Finalização
        EtapaWorkflow etapa5 = EtapaWorkflow.builder()
                .nome("Finalização")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(5)
                .timeoutMinutos(30)
                .build();
        etapa5.adicionarAcao("NOTIFICAR:segurado");
        etapa5.adicionarAcao("REGISTRAR:sinistro_roubo_finalizado");

        workflow.adicionarEtapa(etapa1);
        workflow.adicionarEtapa(etapa2);
        workflow.adicionarEtapa(etapa3);
        workflow.adicionarEtapa(etapa4);
        workflow.adicionarEtapa(etapa5);

        return workflow;
    }

    /**
     * Cria workflow para sinistros com terceiros envolvidos.
     * SLA: 15 dias úteis
     * Etapas: Notificação terceiros, Mediação, Acordo
     *
     * @return definição do workflow
     */
    public static WorkflowDefinition criarWorkflowTerceiros() {
        WorkflowDefinition workflow = WorkflowDefinition.builder()
                .nome("Sinistro com Terceiros")
                .versao(1)
                .tipoSinistro("TERCEIROS")
                .descricao("Workflow para sinistros envolvendo responsabilidade de terceiros")
                .ativo(true)
                .etapas(new ArrayList<>())
                .build();

        // Etapa 1: Validação Inicial
        EtapaWorkflow etapa1 = EtapaWorkflow.builder()
                .nome("Validação Inicial")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(1)
                .timeoutMinutos(60)
                .build();
        etapa1.adicionarAcao("VALIDAR:documentos");
        etapa1.adicionarAcao("VALIDAR:dados_terceiros");

        // Etapa 2: Notificação Terceiros
        EtapaWorkflow etapa2 = EtapaWorkflow.builder()
                .nome("Notificação de Terceiros")
                .tipo(TipoEtapa.INTEGRACAO)
                .ordem(2)
                .timeoutMinutos(1440)
                .build();
        etapa2.adicionarAcao("NOTIFICAR:terceiro_envolvido");
        etapa2.adicionarAcao("INTEGRAR:CONSULTAR_BUREAU");

        // Etapa 3: Análise de Responsabilidade
        EtapaWorkflow etapa3 = EtapaWorkflow.builder()
                .nome("Análise de Responsabilidade")
                .tipo(TipoEtapa.MANUAL)
                .ordem(3)
                .timeoutMinutos(2880)
                .build();
        etapa3.adicionarAcao("MANUAL:Determinar responsabilidade");
        etapa3.adicionarAcao("MANUAL:Avaliar documentação");

        // Etapa 4: Negociação
        EtapaWorkflow etapa4 = EtapaWorkflow.builder()
                .nome("Negociação e Acordo")
                .tipo(TipoEtapa.MANUAL)
                .ordem(4)
                .timeoutMinutos(7200)
                .build();
        etapa4.adicionarAcao("MANUAL:Negociar acordo");

        // Etapa 5: Aprovação Supervisor
        EtapaWorkflow etapa5 = EtapaWorkflow.builder()
                .nome("Aprovação de Acordo")
                .tipo(TipoEtapa.APROVACAO)
                .ordem(5)
                .timeoutMinutos(1440)
                .nivelAprovacao(NivelAprovacao.NIVEL_2_SUPERVISOR)
                .build();

        // Etapa 6: Finalização
        EtapaWorkflow etapa6 = EtapaWorkflow.builder()
                .nome("Finalização")
                .tipo(TipoEtapa.AUTOMATICA)
                .ordem(6)
                .timeoutMinutos(30)
                .build();
        etapa6.adicionarAcao("NOTIFICAR:segurado");
        etapa6.adicionarAcao("NOTIFICAR:terceiro");
        etapa6.adicionarAcao("REGISTRAR:acordo_finalizado");

        workflow.adicionarEtapa(etapa1);
        workflow.adicionarEtapa(etapa2);
        workflow.adicionarEtapa(etapa3);
        workflow.adicionarEtapa(etapa4);
        workflow.adicionarEtapa(etapa5);
        workflow.adicionarEtapa(etapa6);

        return workflow;
    }

    /**
     * Lista todos os templates disponíveis.
     *
     * @return lista de workflows padrão
     */
    public static List<WorkflowDefinition> listarTodos() {
        List<WorkflowDefinition> templates = new ArrayList<>();
        templates.add(criarWorkflowSinistroSimples());
        templates.add(criarWorkflowSinistroComplexo());
        templates.add(criarWorkflowRouboFurto());
        templates.add(criarWorkflowTerceiros());
        return templates;
    }
}
