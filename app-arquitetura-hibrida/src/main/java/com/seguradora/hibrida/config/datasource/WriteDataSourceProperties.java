package com.seguradora.hibrida.config.datasource;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

/**
 * Propriedades de configuração para o DataSource de escrita (Command Side).
 * 
 * <p>Configurações otimizadas para operações de escrita:
 * <ul>
 *   <li>Pool menor mas com conexões duradouras</li>
 *   <li>Timeouts maiores para transações complexas</li>
 *   <li>Configurações de isolamento para consistência</li>
 * </ul>
 */
@ConfigurationProperties(prefix = "app.datasource.write")
@Validated
public class WriteDataSourceProperties {
    
    /**
     * URL de conexão com o banco de dados de escrita.
     */
    @NotBlank(message = "URL do datasource de escrita é obrigatória")
    private String url = "jdbc:postgresql://localhost:5435/sinistros_eventstore";
    
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
    
    /**
     * Configurações do pool Hikari otimizadas para escrita.
     */
    public static class HikariConfig {
        
        /**
         * Tamanho máximo do pool de conexões.
         */
        @Min(value = 5, message = "Pool mínimo deve ser 5")
        @Max(value = 50, message = "Pool máximo deve ser 50")
        private int maximumPoolSize = 20;
        
        /**
         * Tamanho mínimo do pool de conexões.
         */
        @Min(value = 2, message = "Pool mínimo deve ser 2")
        private int minimumIdle = 5;
        
        /**
         * Timeout para obter conexão (ms).
         */
        @Min(value = 10000, message = "Timeout mínimo deve ser 10s")
        private long connectionTimeout = 30000;
        
        /**
         * Timeout de idle para conexões (ms).
         */
        private long idleTimeout = 600000; // 10 minutos
        
        /**
         * Tempo máximo de vida de uma conexão (ms).
         */
        private long maxLifetime = 1800000; // 30 minutos
        
        /**
         * Timeout para validação de conexão (ms).
         */
        private long validationTimeout = 5000;
        
        /**
         * Query para teste de conexão.
         */
        private String connectionTestQuery = "SELECT 1";
        
        /**
         * Nome do pool para identificação.
         */
        private String poolName = "WritePool";
        
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
        private String[] locations = {"classpath:db/migration"};
        
        /**
         * Schema base para migração.
         */
        private String defaultSchema = "eventstore";
        
        /**
         * Tabela de histórico do Flyway.
         */
        private String table = "flyway_schema_history";
        
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
     * Configurações JPA específicas para escrita.
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
         * Tamanho do batch para inserções.
         */
        private int batchSize = 50;
        
        /**
         * Se deve usar batch ordenado.
         */
        private boolean orderInserts = true;
        
        /**
         * Se deve usar batch para updates.
         */
        private boolean orderUpdates = true;
        
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
        
        public int getBatchSize() {
            return batchSize;
        }
        
        public void setBatchSize(int batchSize) {
            this.batchSize = batchSize;
        }
        
        public boolean isOrderInserts() {
            return orderInserts;
        }
        
        public void setOrderInserts(boolean orderInserts) {
            this.orderInserts = orderInserts;
        }
        
        public boolean isOrderUpdates() {
            return orderUpdates;
        }
        
        public void setOrderUpdates(boolean orderUpdates) {
            this.orderUpdates = orderUpdates;
        }
    }
}