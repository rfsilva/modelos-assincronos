package com.seguradora.hibrida.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Propriedades de configuração para o DataSource de leitura (Query Side).
 * 
 * <p>Configurações otimizadas para operações de leitura:
 * <ul>
 *   <li>Pool maior para suportar mais consultas simultâneas</li>
 *   <li>Timeouts menores para responsividade</li>
 *   <li>Configurações de cache otimizadas</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.datasource.read")
@Validated
public class ReadDataSourceProperties {
    
    /**
     * URL de conexão com o banco de dados de leitura.
     */
    @NotBlank(message = "URL do datasource de leitura é obrigatória")
    private String url = "jdbc:postgresql://localhost:5436/sinistros_projections";
    
    /**
     * Nome de usuário para conexão.
     */
    @NotBlank(message = "Username é obrigatório")
    private String username = "postgres";
    
    /**
     * Senha para conexão.
     */
    @NotBlank(message = "Password é obrigatório")
    private String password = "postgres";
    
    /**
     * Driver JDBC a ser utilizado.
     */
    private String driverClassName = "org.postgresql.Driver";
    
    /**
     * Configurações do pool de conexões Hikari.
     */
    private HikariConfig hikari = new HikariConfig();
    
    /**
     * Configurações específicas do Flyway.
     */
    private FlywayConfig flyway = new FlywayConfig();
    
    /**
     * Configurações de JPA/Hibernate.
     */
    private JpaConfig jpa = new JpaConfig();
    
    /**
     * Configurações de fallback.
     */
    private FallbackConfig fallback = new FallbackConfig();
    
    // Getters e Setters
    public String getUrl() {
        return url;
    }
    
    public void setUrl(String url) {
        this.url = url;
    }
    
    public String getUsername() {
        return username;
    }
    
    public void setUsername(String username) {
        this.username = username;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getDriverClassName() {
        return driverClassName;
    }
    
    public void setDriverClassName(String driverClassName) {
        this.driverClassName = driverClassName;
    }
    
    public HikariConfig getHikari() {
        return hikari;
    }
    
    public void setHikari(HikariConfig hikari) {
        this.hikari = hikari;
    }
    
    public FlywayConfig getFlyway() {
        return flyway;
    }
    
    public void setFlyway(FlywayConfig flyway) {
        this.flyway = flyway;
    }
    
    public JpaConfig getJpa() {
        return jpa;
    }
    
    public void setJpa(JpaConfig jpa) {
        this.jpa = jpa;
    }
    
    public FallbackConfig getFallback() {
        return fallback;
    }
    
    public void setFallback(FallbackConfig fallback) {
        this.fallback = fallback;
    }
    
    /**
     * Configurações do pool Hikari otimizadas para leitura.
     */
    public static class HikariConfig {
        
        /**
         * Tamanho máximo do pool de conexões.
         */
        @Min(value = 10, message = "Pool mínimo deve ser 10")
        @Max(value = 100, message = "Pool máximo deve ser 100")
        private int maximumPoolSize = 50;
        
        /**
         * Tamanho mínimo do pool de conexões.
         */
        @Min(value = 5, message = "Pool mínimo deve ser 5")
        private int minimumIdle = 10;
        
        /**
         * Timeout para obter conexão (ms).
         */
        @Min(value = 5000, message = "Timeout mínimo deve ser 5s")
        private long connectionTimeout = 20000;
        
        /**
         * Timeout de idle para conexões (ms).
         */
        private long idleTimeout = 300000; // 5 minutos
        
        /**
         * Tempo máximo de vida de uma conexão (ms).
         */
        private long maxLifetime = 900000; // 15 minutos
        
        /**
         * Timeout para validação de conexão (ms).
         */
        private long validationTimeout = 3000;
        
        /**
         * Query para teste de conexão.
         */
        private String connectionTestQuery = "SELECT 1";
        
        /**
         * Nome do pool para identificação.
         */
        private String poolName = "ReadPool";
        
        /**
         * Se deve usar conexões read-only.
         */
        private boolean readOnly = true;
        
        // Getters e Setters
        public int getMaximumPoolSize() {
            return maximumPoolSize;
        }
        
        public void setMaximumPoolSize(int maximumPoolSize) {
            this.maximumPoolSize = maximumPoolSize;
        }
        
        public int getMinimumIdle() {
            return minimumIdle;
        }
        
        public void setMinimumIdle(int minimumIdle) {
            this.minimumIdle = minimumIdle;
        }
        
        public long getConnectionTimeout() {
            return connectionTimeout;
        }
        
        public void setConnectionTimeout(long connectionTimeout) {
            this.connectionTimeout = connectionTimeout;
        }
        
        public long getIdleTimeout() {
            return idleTimeout;
        }
        
        public void setIdleTimeout(long idleTimeout) {
            this.idleTimeout = idleTimeout;
        }
        
        public long getMaxLifetime() {
            return maxLifetime;
        }
        
        public void setMaxLifetime(long maxLifetime) {
            this.maxLifetime = maxLifetime;
        }
        
        public long getValidationTimeout() {
            return validationTimeout;
        }
        
        public void setValidationTimeout(long validationTimeout) {
            this.validationTimeout = validationTimeout;
        }
        
        public String getConnectionTestQuery() {
            return connectionTestQuery;
        }
        
        public void setConnectionTestQuery(String connectionTestQuery) {
            this.connectionTestQuery = connectionTestQuery;
        }
        
        public String getPoolName() {
            return poolName;
        }
        
        public void setPoolName(String poolName) {
            this.poolName = poolName;
        }
        
        public boolean isReadOnly() {
            return readOnly;
        }
        
        public void setReadOnly(boolean readOnly) {
            this.readOnly = readOnly;
        }
    }
    
    /**
     * Configurações do Flyway para migração.
     */
    public static class FlywayConfig {
        
        /**
         * Se o Flyway está habilitado.
         */
        private boolean enabled = true;
        
        /**
         * Localizações dos scripts de migração.
         */
        private String[] locations = {"classpath:db/migration-projections"};
        
        /**
         * Schema base para migração.
         */
        private String defaultSchema = "projections";
        
        /**
         * Tabela de histórico do Flyway.
         */
        private String table = "flyway_schema_history_projections";
        
        /**
         * Se deve validar migrações na inicialização.
         */
        private boolean validateOnMigrate = true;
        
        // Getters e Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String[] getLocations() {
            return locations;
        }
        
        public void setLocations(String[] locations) {
            this.locations = locations;
        }
        
        public String getDefaultSchema() {
            return defaultSchema;
        }
        
        public void setDefaultSchema(String defaultSchema) {
            this.defaultSchema = defaultSchema;
        }
        
        public String getTable() {
            return table;
        }
        
        public void setTable(String table) {
            this.table = table;
        }
        
        public boolean isValidateOnMigrate() {
            return validateOnMigrate;
        }
        
        public void setValidateOnMigrate(boolean validateOnMigrate) {
            this.validateOnMigrate = validateOnMigrate;
        }
    }
    
    /**
     * Configurações JPA específicas para leitura.
     */
    public static class JpaConfig {
        
        /**
         * Se deve mostrar SQL no log.
         */
        private boolean showSql = false;
        
        /**
         * Se deve formatar SQL no log.
         */
        private boolean formatSql = false;
        
        /**
         * Estratégia de DDL.
         */
        private String ddlAuto = "validate";
        
        /**
         * Dialeto do Hibernate.
         */
        private String dialect = "org.hibernate.dialect.PostgreSQLDialect";
        
        /**
         * Tamanho do batch para consultas.
         */
        private int fetchSize = 100;
        
        /**
         * Se deve usar cache de segundo nível.
         */
        private boolean useSecondLevelCache = true;
        
        /**
         * Se deve usar cache de queries.
         */
        private boolean useQueryCache = true;
        
        // Getters e Setters
        public boolean isShowSql() {
            return showSql;
        }
        
        public void setShowSql(boolean showSql) {
            this.showSql = showSql;
        }
        
        public boolean isFormatSql() {
            return formatSql;
        }
        
        public void setFormatSql(boolean formatSql) {
            this.formatSql = formatSql;
        }
        
        public String getDdlAuto() {
            return ddlAuto;
        }
        
        public void setDdlAuto(String ddlAuto) {
            this.ddlAuto = ddlAuto;
        }
        
        public String getDialect() {
            return dialect;
        }
        
        public void setDialect(String dialect) {
            this.dialect = dialect;
        }
        
        public int getFetchSize() {
            return fetchSize;
        }
        
        public void setFetchSize(int fetchSize) {
            this.fetchSize = fetchSize;
        }
        
        public boolean isUseSecondLevelCache() {
            return useSecondLevelCache;
        }
        
        public void setUseSecondLevelCache(boolean useSecondLevelCache) {
            this.useSecondLevelCache = useSecondLevelCache;
        }
        
        public boolean isUseQueryCache() {
            return useQueryCache;
        }
        
        public void setUseQueryCache(boolean useQueryCache) {
            this.useQueryCache = useQueryCache;
        }
    }
    
    /**
     * Configurações de fallback para o datasource de leitura.
     */
    public static class FallbackConfig {
        
        /**
         * Se o fallback está habilitado.
         */
        private boolean enabled = true;
        
        /**
         * URL de fallback (pode ser o datasource de escrita).
         */
        private String fallbackUrl;
        
        /**
         * Timeout para detectar falha (ms).
         */
        private long failureDetectionTimeout = 5000;
        
        /**
         * Intervalo para tentar reconectar (ms).
         */
        private long retryInterval = 30000;
        
        /**
         * Número máximo de tentativas de reconexão.
         */
        private int maxRetries = 3;
        
        // Getters e Setters
        public boolean isEnabled() {
            return enabled;
        }
        
        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }
        
        public String getFallbackUrl() {
            return fallbackUrl;
        }
        
        public void setFallbackUrl(String fallbackUrl) {
            this.fallbackUrl = fallbackUrl;
        }
        
        public long getFailureDetectionTimeout() {
            return failureDetectionTimeout;
        }
        
        public void setFailureDetectionTimeout(long failureDetectionTimeout) {
            this.failureDetectionTimeout = failureDetectionTimeout;
        }
        
        public long getRetryInterval() {
            return retryInterval;
        }
        
        public void setRetryInterval(long retryInterval) {
            this.retryInterval = retryInterval;
        }
        
        public int getMaxRetries() {
            return maxRetries;
        }
        
        public void setMaxRetries(int maxRetries) {
            this.maxRetries = maxRetries;
        }
    }
}