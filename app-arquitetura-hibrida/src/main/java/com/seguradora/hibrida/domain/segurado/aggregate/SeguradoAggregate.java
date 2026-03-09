package com.seguradora.hibrida.domain.segurado.aggregate;

import com.seguradora.hibrida.aggregate.AggregateRoot;
import com.seguradora.hibrida.aggregate.EventSourcingHandler;
import com.seguradora.hibrida.aggregate.exception.BusinessRuleViolationException;
import com.seguradora.hibrida.aggregate.validation.BusinessRule;
import com.seguradora.hibrida.domain.segurado.event.*;
import com.seguradora.hibrida.domain.segurado.model.Contato;
import com.seguradora.hibrida.domain.segurado.model.Endereco;
import com.seguradora.hibrida.domain.segurado.model.Segurado;
import com.seguradora.hibrida.domain.segurado.model.StatusSegurado;
import com.seguradora.hibrida.domain.segurado.model.TipoContato;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Aggregate Root para o domínio de Segurado.
 * 
 * <p>Responsabilidades:
 * <ul>
 *   <li>Criar novos segurados com validações completas</li>
 *   <li>Atualizar dados de segurados existentes</li>
 *   <li>Desativar e reativar segurados</li>
 *   <li>Gerenciar contatos e endereços</li>
 *   <li>Garantir invariantes de negócio</li>
 *   <li>Gerar eventos de domínio para cada operação</li>
 * </ul>
 * 
 * @author Principal Java Architect
 * @since 2.0.0
 */
@Slf4j
@Getter
public class SeguradoAggregate extends AggregateRoot {
    
    // Padrões de validação
    private static final Pattern CPF_PATTERN = Pattern.compile("\\d{11}");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    private static final Pattern TELEFONE_PATTERN = Pattern.compile("\\d{10,11}");
    
    // Estado do aggregate
    private Segurado segurado;
    private List<Contato> contatos = new ArrayList<>();
    
    /**
     * Construtor padrão para reconstrução do aggregate.
     */
    public SeguradoAggregate() {
        super();
        registerBusinessRules();
    }
    
    /**
     * Construtor para criação de novo segurado.
     */
    public SeguradoAggregate(String id, String cpf, String nome, String email, 
                            String telefone, LocalDate dataNascimento, Endereco endereco) {
        super(id);
        registerBusinessRules();
        
        // Validações antes de criar o evento
        validarCpf(cpf);
        validarNome(nome);
        validarEmail(email);
        validarTelefone(telefone);
        validarDataNascimento(dataNascimento);
        validarEndereco(endereco);
        
        // Aplicar evento de criação
        applyEvent(new SeguradoCriadoEvent(id, cpf, nome, email, telefone, dataNascimento, endereco));
    }
    
    /**
     * Atualiza os dados do segurado.
     */
    public void atualizarDados(String nome, String email, String telefone, 
                               LocalDate dataNascimento, Endereco endereco) {
        // Validar se o segurado está ativo
        if (!isAtivo()) {
            throw new BusinessRuleViolationException(
                "Não é possível atualizar dados de segurado inativo", 
                List.of("Segurado deve estar ativo para atualização")
            );
        }
        
        // Validações
        validarNome(nome);
        validarEmail(email);
        validarTelefone(telefone);
        validarDataNascimento(dataNascimento);
        validarEndereco(endereco);
        
        // Aplicar evento
        applyEvent(new SeguradoAtualizadoEvent(getId(), nome, email, telefone, dataNascimento, endereco));
    }
    
    /**
     * Atualiza apenas o endereço do segurado.
     */
    public void atualizarEndereco(Endereco endereco) {
        if (!isAtivo()) {
            throw new BusinessRuleViolationException(
                "Não é possível atualizar endereço de segurado inativo", 
                List.of("Segurado deve estar ativo para atualização")
            );
        }
        
        validarEndereco(endereco);
        // Corrigir: usar endereço anterior e novo endereço
        applyEvent(new EnderecoAtualizadoEvent(getId(), this.segurado.getEndereco(), endereco));
    }
    
    /**
     * Adiciona um contato ao segurado.
     */
    public void adicionarContato(TipoContato tipo, String valor, boolean principal) {
        if (!isAtivo()) {
            throw new BusinessRuleViolationException(
                "Não é possível adicionar contato a segurado inativo", 
                List.of("Segurado deve estar ativo para adicionar contato")
            );
        }
        
        // Validar se já existe contato do mesmo tipo e valor
        boolean jaExiste = contatos.stream()
            .anyMatch(c -> c.getTipo() == tipo && c.getValor().equals(valor));
            
        if (jaExiste) {
            throw new BusinessRuleViolationException(
                "Contato já existe", 
                List.of("Já existe um contato do tipo " + tipo + " com o valor " + valor)
            );
        }
        
        // Corrigir: usar factory method do Contato
        Contato contato = Contato.of(tipo, valor, principal);
        applyEvent(new ContatoAdicionadoEvent(getId(), tipo, valor, principal));
    }
    
    /**
     * Remove um contato do segurado.
     */
    public void removerContato(TipoContato tipo, String valor) {
        if (!isAtivo()) {
            throw new BusinessRuleViolationException(
                "Não é possível remover contato de segurado inativo", 
                List.of("Segurado deve estar ativo para remover contato")
            );
        }
        
        // Verificar se o contato existe
        boolean existe = contatos.stream()
            .anyMatch(c -> c.getTipo() == tipo && c.getValor().equals(valor));
            
        if (!existe) {
            throw new BusinessRuleViolationException(
                "Contato não encontrado", 
                List.of("Não foi encontrado contato do tipo " + tipo + " com o valor " + valor)
            );
        }
        
        applyEvent(new ContatoRemovidoEvent(getId(), tipo, valor));
    }
    
    /**
     * Desativa o segurado.
     */
    public void desativar(String motivo) {
        if (!isAtivo()) {
            throw new BusinessRuleViolationException(
                "Segurado já está inativo", 
                List.of("Status atual: " + getStatus())
            );
        }
        
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessRuleViolationException(
                "Motivo da desativação é obrigatório", 
                List.of("Informe o motivo da desativação")
            );
        }
        
        applyEvent(new SeguradoDesativadoEvent(getId(), motivo));
    }
    
    /**
     * Reativa o segurado.
     */
    public void reativar(String motivo) {
        if (isAtivo()) {
            throw new BusinessRuleViolationException(
                "Segurado já está ativo", 
                List.of("Status atual: " + getStatus())
            );
        }
        
        if (motivo == null || motivo.isBlank()) {
            throw new BusinessRuleViolationException(
                "Motivo da reativação é obrigatório", 
                List.of("Informe o motivo da reativação")
            );
        }
        
        applyEvent(new SeguradoReativadoEvent(getId(), motivo));
    }
    
    // ==================== GETTERS ADICIONAIS ====================
    
    public boolean isAtivo() {
        return segurado != null && segurado.isAtivo();
    }
    
    public StatusSegurado getStatus() {
        return segurado != null ? segurado.getStatus() : null;
    }
    
    public String getEmail() {
        return segurado != null ? segurado.getEmail() : null;
    }
    
    public String getCpf() {
        return segurado != null ? segurado.getCpf() : null;
    }
    
    public String getNome() {
        return segurado != null ? segurado.getNome() : null;
    }
    
    // ==================== EVENT SOURCING HANDLERS ====================
    
    @EventSourcingHandler
    protected void on(SeguradoCriadoEvent event) {
        this.segurado = new Segurado(
            event.getCpf(),
            event.getNome(),
            event.getEmail(),
            event.getTelefone(),
            event.getDataNascimento(),
            event.getEndereco()
        );
        log.debug("Segurado criado: CPF={}, Nome={}", event.getCpf(), event.getNome());
    }
    
    @EventSourcingHandler
    protected void on(SeguradoAtualizadoEvent event) {
        this.segurado.setNome(event.getNome());
        this.segurado.setEmail(event.getEmail());
        this.segurado.setTelefone(event.getTelefone());
        this.segurado.setDataNascimento(event.getDataNascimento());
        this.segurado.setEndereco(event.getEndereco());
        log.debug("Segurado atualizado: ID={}", getId());
    }
    
    @EventSourcingHandler
    protected void on(EnderecoAtualizadoEvent event) {
        // Corrigir: usar o novo endereço
        this.segurado.setEndereco(event.getNovoEndereco());
        log.debug("Endereço atualizado: ID={}", getId());
    }
    
    @EventSourcingHandler
    protected void on(ContatoAdicionadoEvent event) {
        // Corrigir: criar contato a partir dos dados do evento
        Contato contato = Contato.of(event.getTipo(), event.getValor(), event.isPrincipal());
        this.contatos.add(contato);
        log.debug("Contato adicionado: ID={}, Tipo={}", getId(), event.getTipo());
    }
    
    @EventSourcingHandler
    protected void on(ContatoRemovidoEvent event) {
        this.contatos.removeIf(c -> c.getTipo() == event.getTipo() && c.getValor().equals(event.getValor()));
        log.debug("Contato removido: ID={}, Tipo={}", getId(), event.getTipo());
    }
    
    @EventSourcingHandler
    protected void on(SeguradoDesativadoEvent event) {
        this.segurado.setStatus(StatusSegurado.INATIVO);
        log.debug("Segurado desativado: ID={}, Motivo={}", getId(), event.getMotivo());
    }
    
    @EventSourcingHandler
    protected void on(SeguradoReativadoEvent event) {
        this.segurado.setStatus(StatusSegurado.ATIVO);
        log.debug("Segurado reativado: ID={}, Motivo={}", getId(), event.getMotivo());
    }
    
    // ==================== VALIDAÇÕES ====================
    
    private void validarCpf(String cpf) {
        if (cpf == null || !CPF_PATTERN.matcher(cpf).matches()) {
            throw new BusinessRuleViolationException(
                "CPF inválido", 
                List.of("CPF deve conter exatamente 11 dígitos numéricos")
            );
        }
        
        // Validação de CPF com dígitos verificadores
        if (!isCpfValido(cpf)) {
            throw new BusinessRuleViolationException(
                "CPF inválido", 
                List.of("CPF não passou na validação de dígitos verificadores")
            );
        }
    }
    
    private void validarNome(String nome) {
        if (nome == null || nome.isBlank()) {
            throw new BusinessRuleViolationException(
                "Nome é obrigatório", 
                List.of("Nome não pode ser vazio")
            );
        }
        
        if (nome.length() < 3 || nome.length() > 100) {
            throw new BusinessRuleViolationException(
                "Nome inválido", 
                List.of("Nome deve ter entre 3 e 100 caracteres")
            );
        }
    }
    
    private void validarEmail(String email) {
        if (email == null || !EMAIL_PATTERN.matcher(email).matches()) {
            throw new BusinessRuleViolationException(
                "Email inválido", 
                List.of("Email deve estar em formato válido")
            );
        }
    }
    
    private void validarTelefone(String telefone) {
        if (telefone == null || !TELEFONE_PATTERN.matcher(telefone).matches()) {
            throw new BusinessRuleViolationException(
                "Telefone inválido", 
                List.of("Telefone deve conter 10 ou 11 dígitos numéricos")
            );
        }
    }
    
    private void validarDataNascimento(LocalDate dataNascimento) {
        if (dataNascimento == null) {
            throw new BusinessRuleViolationException(
                "Data de nascimento é obrigatória", 
                List.of("Informe a data de nascimento")
            );
        }
        
        if (dataNascimento.isAfter(LocalDate.now())) {
            throw new BusinessRuleViolationException(
                "Data de nascimento inválida", 
                List.of("Data de nascimento não pode ser futura")
            );
        }
        
        // Validar idade mínima (18 anos)
        if (LocalDate.now().minusYears(18).isBefore(dataNascimento)) {
            throw new BusinessRuleViolationException(
                "Segurado deve ser maior de idade", 
                List.of("Idade mínima para ser segurado é 18 anos")
            );
        }
    }
    
    private void validarEndereco(Endereco endereco) {
        if (endereco == null) {
            throw new BusinessRuleViolationException(
                "Endereço é obrigatório", 
                List.of("Informe o endereço completo")
            );
        }
        
        if (endereco.getCep() == null || endereco.getCep().length() != 8) {
            throw new BusinessRuleViolationException(
                "CEP inválido", 
                List.of("CEP deve conter 8 dígitos")
            );
        }
    }
    
    /**
     * Validação de CPF com dígitos verificadores.
     */
    private boolean isCpfValido(String cpf) {
        // Rejeitar CPFs com todos os dígitos iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        try {
            // Calcular primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int primeiroDigito = 11 - (soma % 11);
            if (primeiroDigito >= 10) primeiroDigito = 0;
            
            // Calcular segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            int segundoDigito = 11 - (soma % 11);
            if (segundoDigito >= 10) segundoDigito = 0;
            
            // Validar dígitos
            return Character.getNumericValue(cpf.charAt(9)) == primeiroDigito &&
                   Character.getNumericValue(cpf.charAt(10)) == segundoDigito;
                   
        } catch (Exception e) {
            return false;
        }
    }
    
    // ==================== BUSINESS RULES ====================
    
    private void registerBusinessRules() {
        // Regra: Segurado deve ter dados básicos válidos
        registerBusinessRule(new BusinessRule() {
            @Override
            public boolean isValid(AggregateRoot aggregate) {
                if (segurado == null) return true; // Ainda não foi criado
                return segurado.getCpf() != null && 
                       segurado.getNome() != null && 
                       segurado.getEmail() != null;
            }
            
            @Override
            public String getErrorMessage() {
                return "Segurado deve ter CPF, nome e email válidos";
            }
        });
    }
    
    // ==================== SNAPSHOT SUPPORT ====================
    
    @Override
    public Object createSnapshot() {
        return this.segurado;
    }
    
    @Override
    protected void restoreFromSnapshot(Object snapshotData) {
        if (snapshotData instanceof Segurado) {
            this.segurado = (Segurado) snapshotData;
        }
    }
    
    @Override
    protected void clearState() {
        this.segurado = null;
        this.contatos.clear();
    }
}