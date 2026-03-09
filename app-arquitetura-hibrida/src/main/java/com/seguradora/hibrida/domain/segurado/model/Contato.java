package com.seguradora.hibrida.domain.segurado.model;

import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.Serializable;
import java.time.Instant;
import java.util.List;

/**
 * Value Object representando um Contato do Segurado.
 * 
 * <p>Este value object encapsula as informações de contato,
 * garantindo consistência entre tipo e valor.
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Getter
@EqualsAndHashCode
public class Contato implements Serializable {
    
    private final TipoContato tipo;
    private final String valor;
    private final boolean principal;
    private final Instant dataCriacao;
    private final boolean ativo;
    
    /**
     * Construtor privado para garantir validação.
     */
    private Contato(TipoContato tipo, String valor, boolean principal, boolean ativo) {
        this.tipo = tipo;
        this.valor = valor;
        this.principal = principal;
        this.ativo = ativo;
        this.dataCriacao = Instant.now();
    }
    
    /**
     * Factory method para criar Contato válido.
     * 
     * @param tipo Tipo do contato
     * @param valor Valor do contato
     * @param principal Se é o contato principal
     * @return Contato válido
     * @throws BusinessRuleViolationException se dados forem inválidos
     */
    public static Contato of(TipoContato tipo, String valor, boolean principal) {
        if (tipo == null) {
            throw new BusinessRuleViolationException(
                "Tipo de contato é obrigatório", 
                List.of("Informe o tipo do contato")
            );
        }
        
        if (valor == null || valor.isBlank()) {
            throw new BusinessRuleViolationException(
                "Valor do contato é obrigatório", 
                List.of("Informe o valor do contato")
            );
        }
        
        // Validar consistência entre tipo e valor
        validarConsistencia(tipo, valor);
        
        return new Contato(tipo, valor.trim(), principal, true);
    }
    
    /**
     * Cria uma cópia inativa do contato.
     */
    public Contato desativar() {
        return new Contato(this.tipo, this.valor, this.principal, false);
    }
    
    /**
     * Cria uma cópia ativa do contato.
     */
    public Contato ativar() {
        return new Contato(this.tipo, this.valor, this.principal, true);
    }
    
    /**
     * Cria uma cópia marcada como principal.
     */
    public Contato marcarComoPrincipal() {
        return new Contato(this.tipo, this.valor, true, this.ativo);
    }
    
    /**
     * Cria uma cópia desmarcada como principal.
     */
    public Contato desmarcarComoPrincipal() {
        return new Contato(this.tipo, this.valor, false, this.ativo);
    }
    
    /**
     * Valida consistência entre tipo e valor do contato.
     */
    private static void validarConsistencia(TipoContato tipo, String valor) {
        switch (tipo) {
            case EMAIL:
                try {
                    Email.of(valor);
                } catch (BusinessRuleViolationException e) {
                    throw new BusinessRuleViolationException(
                        "Email inválido para contato", 
                        e.getViolations()
                    );
                }
                break;
                
            case TELEFONE:
                try {
                    Telefone telefone = Telefone.of(valor);
                    if (telefone.isCelular()) {
                        throw new BusinessRuleViolationException(
                            "Número de celular não pode ser usado como telefone fixo", 
                            List.of("Use o tipo CELULAR para números móveis")
                        );
                    }
                } catch (BusinessRuleViolationException e) {
                    throw new BusinessRuleViolationException(
                        "Telefone inválido para contato", 
                        e.getViolations()
                    );
                }
                break;
                
            case CELULAR:
            case WHATSAPP:
                try {
                    Telefone telefone = Telefone.of(valor);
                    if (!telefone.isCelular()) {
                        throw new BusinessRuleViolationException(
                            "Número de telefone fixo não pode ser usado como celular", 
                            List.of("Use o tipo TELEFONE para números fixos")
                        );
                    }
                } catch (BusinessRuleViolationException e) {
                    throw new BusinessRuleViolationException(
                        "Celular inválido para contato", 
                        e.getViolations()
                    );
                }
                break;
        }
    }
    
    /**
     * Retorna o valor formatado do contato.
     */
    public String getValorFormatado() {
        switch (tipo) {
            case EMAIL:
                return Email.of(valor).toString();
            case TELEFONE:
            case CELULAR:
            case WHATSAPP:
                return Telefone.of(valor).getFormatado();
            default:
                return valor;
        }
    }
    
    /**
     * Verifica se o contato pode receber notificações.
     */
    public boolean podeReceberNotificacoes() {
        return ativo && tipo.suportaMensagemTexto();
    }
    
    @Override
    public String toString() {
        return String.format("%s: %s%s%s", 
            tipo.getNome(), 
            getValorFormatado(),
            principal ? " (Principal)" : "",
            !ativo ? " (Inativo)" : ""
        );
    }
}