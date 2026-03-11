package com.seguradora.hibrida.domain.veiculo.model;

/**
 * Enum que representa os tipos de pessoa para propriedade de veículos.
 * 
 * <p>Define se o proprietário é pessoa física ou jurídica, incluindo
 * validações específicas e regras de negócio para cada tipo.
 * 
 * @author Principal Java Architect
 * @since 1.0.0
 */
public enum TipoPessoa {
    
    /**
     * Pessoa Física.
     * 
     * <p>Proprietário individual identificado por CPF.
     * Características:
     * - Documento: CPF (11 dígitos)
     * - Uso geralmente pessoal
     * - Regras de pessoa física
     */
    FISICA("Pessoa Física", "PF", 11, "CPF"),
    
    /**
     * Pessoa Jurídica.
     * 
     * <p>Empresa ou organização identificada por CNPJ.
     * Características:
     * - Documento: CNPJ (14 dígitos)
     * - Uso comercial/empresarial
     * - Regras de pessoa jurídica
     */
    JURIDICA("Pessoa Jurídica", "PJ", 14, "CNPJ");
    
    private final String nome;
    private final String sigla;
    private final int tamanhoDocumento;
    private final String tipoDocumento;
    
    /**
     * Construtor do enum.
     * 
     * @param nome Nome completo do tipo
     * @param sigla Sigla do tipo
     * @param tamanhoDocumento Tamanho do documento em dígitos
     * @param tipoDocumento Nome do tipo de documento
     */
    TipoPessoa(String nome, String sigla, int tamanhoDocumento, String tipoDocumento) {
        this.nome = nome;
        this.sigla = sigla;
        this.tamanhoDocumento = tamanhoDocumento;
        this.tipoDocumento = tipoDocumento;
    }
    
    /**
     * Retorna o nome completo do tipo.
     * 
     * @return Nome do tipo de pessoa
     */
    public String getNome() {
        return nome;
    }
    
    /**
     * Retorna a sigla do tipo.
     * 
     * @return Sigla (PF ou PJ)
     */
    public String getSigla() {
        return sigla;
    }
    
    /**
     * Retorna o tamanho esperado do documento.
     * 
     * @return Número de dígitos do documento
     */
    public int getTamanhoDocumento() {
        return tamanhoDocumento;
    }
    
    /**
     * Retorna o nome do tipo de documento.
     * 
     * @return Nome do documento (CPF ou CNPJ)
     */
    public String getTipoDocumento() {
        return tipoDocumento;
    }
    
    /**
     * Verifica se é pessoa física.
     * 
     * @return true se é pessoa física
     */
    public boolean isPessoaFisica() {
        return this == FISICA;
    }
    
    /**
     * Verifica se é pessoa jurídica.
     * 
     * @return true se é pessoa jurídica
     */
    public boolean isPessoaJuridica() {
        return this == JURIDICA;
    }
    
    /**
     * Valida se um documento tem o formato correto para este tipo.
     * 
     * @param documento Documento a ser validado (apenas números)
     * @return true se o formato está correto
     */
    public boolean validarFormatoDocumento(String documento) {
        if (documento == null || documento.trim().isEmpty()) {
            return false;
        }
        
        // Remove caracteres não numéricos
        String apenasNumeros = documento.replaceAll("\\D", "");
        
        return apenasNumeros.length() == tamanhoDocumento;
    }
    
    /**
     * Formata um documento de acordo com o padrão do tipo.
     * 
     * @param documento Documento apenas com números
     * @return Documento formatado ou null se inválido
     */
    public String formatarDocumento(String documento) {
        if (!validarFormatoDocumento(documento)) {
            return null;
        }
        
        String apenasNumeros = documento.replaceAll("\\D", "");
        
        if (this == FISICA) {
            // Formato CPF: 000.000.000-00
            return String.format("%s.%s.%s-%s",
                apenasNumeros.substring(0, 3),
                apenasNumeros.substring(3, 6),
                apenasNumeros.substring(6, 9),
                apenasNumeros.substring(9, 11));
        } else {
            // Formato CNPJ: 00.000.000/0000-00
            return String.format("%s.%s.%s/%s-%s",
                apenasNumeros.substring(0, 2),
                apenasNumeros.substring(2, 5),
                apenasNumeros.substring(5, 8),
                apenasNumeros.substring(8, 12),
                apenasNumeros.substring(12, 14));
        }
    }
    
    /**
     * Retorna o tipo de pessoa baseado no documento.
     * 
     * @param documento Documento a ser analisado
     * @return Tipo de pessoa ou null se não identificado
     */
    public static TipoPessoa identificarPorDocumento(String documento) {
        if (documento == null) {
            return null;
        }
        
        String apenasNumeros = documento.replaceAll("\\D", "");
        
        if (apenasNumeros.length() == 11) {
            return FISICA;
        } else if (apenasNumeros.length() == 14) {
            return JURIDICA;
        }
        
        return null;
    }
    
    /**
     * Verifica se o tipo permite determinada categoria de veículo.
     * 
     * @param categoria Categoria do veículo
     * @return true se permite a categoria
     */
    public boolean permiteCategoria(CategoriaVeiculo categoria) {
        if (categoria == null) {
            return false;
        }
        
        // Pessoa física pode ter qualquer categoria
        if (this == FISICA) {
            return true;
        }
        
        // Pessoa jurídica tem restrições para motocicletas
        if (this == JURIDICA) {
            return categoria != CategoriaVeiculo.MOTOCICLETA;
        }
        
        return false;
    }
    
    /**
     * Retorna o limite de veículos recomendado por proprietário.
     * 
     * @return Número máximo recomendado de veículos
     */
    public int getLimiteVeiculosRecomendado() {
        switch (this) {
            case FISICA:
                return 5; // Pessoa física: até 5 veículos
            case JURIDICA:
                return 100; // Pessoa jurídica: até 100 veículos
            default:
                return 1;
        }
    }
    
    /**
     * Retorna representação string do tipo.
     * 
     * @return Sigla do tipo de pessoa
     */
    @Override
    public String toString() {
        return sigla;
    }
}