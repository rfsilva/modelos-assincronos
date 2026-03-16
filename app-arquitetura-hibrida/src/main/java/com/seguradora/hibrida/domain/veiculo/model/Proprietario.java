package com.seguradora.hibrida.domain.veiculo.model;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object que representa o proprietário de um veículo.
 * 
 * <p>Encapsula informações do proprietário incluindo tipo de pessoa (física/jurídica),
 * documento (CPF/CNPJ) e nome, com validações específicas para cada tipo.
 * 
 * <p>Este Value Object garante que os dados do proprietário sejam consistentes
 * e válidos de acordo com as regras brasileiras de documentação.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public final class Proprietario implements Serializable {
    
    private static final long serialVersionUID = 1L;
    
    private final String cpfCnpj;
    private final String nome;
    private final TipoPessoa tipoPessoa;
    
    /**
     * Construtor privado.
     * 
     * @param cpfCnpj Documento do proprietário
     * @param nome Nome do proprietário
     * @param tipoPessoa Tipo de pessoa
     */
    private Proprietario(String cpfCnpj, String nome, TipoPessoa tipoPessoa) {
        this.cpfCnpj = cpfCnpj;
        this.nome = nome;
        this.tipoPessoa = tipoPessoa;
    }
    
    /**
     * Cria uma instância de Proprietario.
     * 
     * @param cpfCnpj CPF ou CNPJ do proprietário
     * @param nome Nome do proprietário
     * @param tipoPessoa Tipo de pessoa (física ou jurídica)
     * @return Instância de Proprietario
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public static Proprietario of(String cpfCnpj, String nome, TipoPessoa tipoPessoa) {
        TipoPessoa tipoValidado = validarTipoPessoa(tipoPessoa);
        String documentoValidado = validarCpfCnpj(cpfCnpj, tipoValidado);
        String nomeValidado = validarNome(nome);

        return new Proprietario(documentoValidado, nomeValidado, tipoValidado);
    }
    
    /**
     * Cria uma instância detectando automaticamente o tipo de pessoa.
     * 
     * @param cpfCnpj CPF ou CNPJ do proprietário
     * @param nome Nome do proprietário
     * @return Instância de Proprietario
     * @throws IllegalArgumentException se algum dado for inválido
     */
    public static Proprietario of(String cpfCnpj, String nome) {
        TipoPessoa tipo = TipoPessoa.identificarPorDocumento(cpfCnpj);
        if (tipo == null) {
            throw new IllegalArgumentException("Não foi possível identificar o tipo de pessoa pelo documento: " + cpfCnpj);
        }
        
        return of(cpfCnpj, nome, tipo);
    }
    
    /**
     * Retorna o CPF ou CNPJ sem formatação.
     * 
     * @return Documento sem formatação
     */
    public String getCpfCnpj() {
        return cpfCnpj;
    }
    
    /**
     * Retorna o CPF ou CNPJ formatado.
     * 
     * @return Documento formatado
     */
    public String getCpfCnpjFormatado() {
        return tipoPessoa.formatarDocumento(cpfCnpj);
    }
    
    /**
     * Retorna o nome do proprietário.
     * 
     * @return Nome do proprietário
     */
    public String getNome() {
        return nome;
    }
    
    /**
     * Retorna o tipo de pessoa.
     * 
     * @return Tipo de pessoa
     */
    public TipoPessoa getTipoPessoa() {
        return tipoPessoa;
    }
    
    /**
     * Verifica se é pessoa física.
     * 
     * @return true se é pessoa física
     */
    public boolean isPessoaFisica() {
        return tipoPessoa.isPessoaFisica();
    }
    
    /**
     * Verifica se é pessoa jurídica.
     * 
     * @return true se é pessoa jurídica
     */
    public boolean isPessoaJuridica() {
        return tipoPessoa.isPessoaJuridica();
    }
    
    /**
     * Retorna o tipo de documento (CPF ou CNPJ).
     * 
     * @return Tipo de documento
     */
    public String getTipoDocumento() {
        return tipoPessoa.getTipoDocumento();
    }
    
    /**
     * Verifica se o proprietário pode possuir determinada categoria de veículo.
     * 
     * @param categoria Categoria do veículo
     * @return true se pode possuir a categoria
     */
    public boolean podePosituirCategoria(CategoriaVeiculo categoria) {
        return tipoPessoa.permiteCategoria(categoria);
    }
    
    /**
     * Retorna o limite recomendado de veículos para este proprietário.
     * 
     * @return Número máximo recomendado de veículos
     */
    public int getLimiteVeiculosRecomendado() {
        return tipoPessoa.getLimiteVeiculosRecomendado();
    }
    
    /**
     * Retorna uma representação resumida do proprietário.
     * 
     * @return Resumo do proprietário
     */
    public String getResumo() {
        return String.format("%s (%s: %s)", 
            nome, 
            getTipoDocumento(), 
            getCpfCnpjFormatado());
    }
    
    /**
     * Retorna apenas as iniciais do nome para privacidade.
     * 
     * @return Iniciais do nome
     */
    public String getIniciais() {
        if (nome == null || nome.trim().isEmpty()) {
            return "";
        }
        
        String[] partes = nome.trim().split("\\s+");
        StringBuilder iniciais = new StringBuilder();
        
        for (String parte : partes) {
            if (!parte.isEmpty()) {
                iniciais.append(parte.charAt(0)).append(".");
            }
        }
        
        return iniciais.toString();
    }
    
    /**
     * Retorna o nome mascarado para privacidade.
     * 
     * @return Nome com máscara
     */
    public String getNomeMascarado() {
        if (nome == null || nome.length() <= 3) {
            return nome;
        }
        
        String[] partes = nome.split("\\s+");
        if (partes.length == 1) {
            // Nome único: mostra primeira e última letra
            return partes[0].charAt(0) + "***" + partes[0].charAt(partes[0].length() - 1);
        } else {
            // Múltiplas partes: mostra primeiro nome completo e iniciais dos demais
            StringBuilder resultado = new StringBuilder(partes[0]);
            for (int i = 1; i < partes.length; i++) {
                resultado.append(" ").append(partes[i].charAt(0)).append(".");
            }
            return resultado.toString();
        }
    }
    
    /**
     * Valida o CPF ou CNPJ.
     * 
     * @param cpfCnpj Documento a ser validado
     * @param tipoPessoa Tipo de pessoa
     * @return Documento validado (apenas números)
     * @throws IllegalArgumentException se o documento for inválido
     */
    private static String validarCpfCnpj(String cpfCnpj, TipoPessoa tipoPessoa) {
        if (cpfCnpj == null || cpfCnpj.trim().isEmpty()) {
            throw new IllegalArgumentException("CPF/CNPJ não pode ser nulo ou vazio");
        }
        
        String apenasNumeros = cpfCnpj.replaceAll("\\D", "");
        
        if (!tipoPessoa.validarFormatoDocumento(apenasNumeros)) {
            throw new IllegalArgumentException(
                String.format("Formato de %s inválido: %s", 
                    tipoPessoa.getTipoDocumento(), cpfCnpj));
        }
        
        // Validações específicas
        if (tipoPessoa.isPessoaFisica()) {
            if (!validarCPF(apenasNumeros)) {
                throw new IllegalArgumentException("CPF inválido: " + cpfCnpj);
            }
        } else {
            if (!validarCNPJ(apenasNumeros)) {
                throw new IllegalArgumentException("CNPJ inválido: " + cpfCnpj);
            }
        }
        
        return apenasNumeros;
    }
    
    /**
     * Valida o nome do proprietário.
     * 
     * @param nome Nome a ser validado
     * @return Nome validado
     * @throws IllegalArgumentException se o nome for inválido
     */
    private static String validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new IllegalArgumentException("Nome não pode ser nulo ou vazio");
        }

        String nomeLimpo = nome.trim().replaceAll("\\s+", " ");

        if (nomeLimpo.length() < 2) {
            throw new IllegalArgumentException("Nome deve ter pelo menos 2 caracteres");
        }

        if (nomeLimpo.length() > 100) {
            throw new IllegalArgumentException("Nome não pode ter mais de 100 caracteres");
        }

        // Validar caracteres permitidos (letras, espaços, acentos, hífen, apóstrofo)
        if (!nomeLimpo.matches("^[a-zA-ZÀ-ÿ\\s'\\-\\.]+$")) {
            throw new IllegalArgumentException("Nome contém caracteres inválidos: " + nome);
        }

        return nomeLimpo;
    }
    
    /**
     * Valida o tipo de pessoa.
     * 
     * @param tipoPessoa Tipo a ser validado
     * @return Tipo validado
     * @throws IllegalArgumentException se o tipo for inválido
     */
    private static TipoPessoa validarTipoPessoa(TipoPessoa tipoPessoa) {
        if (tipoPessoa == null) {
            throw new IllegalArgumentException("Tipo de pessoa não pode ser nulo");
        }
        return tipoPessoa;
    }
    
    /**
     * Valida um CPF usando o algoritmo oficial.
     * 
     * @param cpf CPF apenas com números
     * @return true se o CPF é válido
     */
    private static boolean validarCPF(String cpf) {
        if (cpf == null || cpf.length() != 11) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais
        if (cpf.matches("(\\d)\\1{10}")) {
            return false;
        }
        
        try {
            // Calcula primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 9; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (10 - i);
            }
            int resto = soma % 11;
            int dv1 = (resto < 2) ? 0 : (11 - resto);
            
            // Calcula segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 10; i++) {
                soma += Character.getNumericValue(cpf.charAt(i)) * (11 - i);
            }
            resto = soma % 11;
            int dv2 = (resto < 2) ? 0 : (11 - resto);
            
            // Verifica se os dígitos calculados conferem
            return dv1 == Character.getNumericValue(cpf.charAt(9)) &&
                   dv2 == Character.getNumericValue(cpf.charAt(10));
                   
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Valida um CNPJ usando o algoritmo oficial.
     * 
     * @param cnpj CNPJ apenas com números
     * @return true se o CNPJ é válido
     */
    private static boolean validarCNPJ(String cnpj) {
        if (cnpj == null || cnpj.length() != 14) {
            return false;
        }
        
        // Verifica se todos os dígitos são iguais
        if (cnpj.matches("(\\d)\\1{13}")) {
            return false;
        }
        
        try {
            // Pesos para cálculo dos dígitos verificadores
            int[] peso1 = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            int[] peso2 = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
            
            // Calcula primeiro dígito verificador
            int soma = 0;
            for (int i = 0; i < 12; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso1[i];
            }
            int resto = soma % 11;
            int dv1 = (resto < 2) ? 0 : (11 - resto);
            
            // Calcula segundo dígito verificador
            soma = 0;
            for (int i = 0; i < 13; i++) {
                soma += Character.getNumericValue(cnpj.charAt(i)) * peso2[i];
            }
            resto = soma % 11;
            int dv2 = (resto < 2) ? 0 : (11 - resto);
            
            // Verifica se os dígitos calculados conferem
            return dv1 == Character.getNumericValue(cnpj.charAt(12)) &&
                   dv2 == Character.getNumericValue(cnpj.charAt(13));
                   
        } catch (NumberFormatException e) {
            return false;
        }
    }
    
    /**
     * Gera um proprietário de exemplo para testes.
     *
     * @return Proprietário de exemplo
     */
    public static Proprietario exemplo() {
        return of("11144477735", "João da Silva", TipoPessoa.FISICA);
    }

    /**
     * Gera um proprietário pessoa jurídica de exemplo para testes.
     *
     * @return Proprietário PJ de exemplo
     */
    public static Proprietario exemploEmpresa() {
        return of("11222333000181", "Empresa Exemplo LTDA", TipoPessoa.JURIDICA);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        
        Proprietario that = (Proprietario) obj;
        return Objects.equals(cpfCnpj, that.cpfCnpj) &&
               tipoPessoa == that.tipoPessoa;
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(cpfCnpj, tipoPessoa);
    }
    
    @Override
    public String toString() {
        return getResumo();
    }
}