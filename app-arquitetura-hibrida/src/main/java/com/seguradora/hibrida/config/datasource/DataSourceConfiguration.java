package com.seguradora.hibrida.config.datasource;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.orm.jpa.JpaTransactionManager;
import org.springframework.orm.jpa.LocalContainerEntityManagerFactoryBean;
import org.springframework.orm.jpa.vendor.HibernateJpaVendorAdapter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * Configuração de múltiplos DataSources para CQRS.
 * 
 * <p>Configura dois datasources separados:
 * <ul>
 *   <li><strong>Write DataSource</strong>: Para operações de escrita (Command Side)</li>
 *   <li><strong>Read DataSource</strong>: Para operações de leitura (Query Side)</li>
 * </ul>
 * 
 * <p>Cada datasource tem suas próprias configurações otimizadas:
 * <ul>
 *   <li>Pool de conexões dimensionado adequadamente</li>
 *   <li>Timeouts específicos para cada tipo de operação</li>
 *   <li>Configurações JPA otimizadas</li>
 *   <li>Health checks independentes</li>
 * </ul>
 */
@Configuration
@EnableTransactionManagement
@EnableConfigurationProperties({WriteDataSourceProperties.class, ReadDataSourceProperties.class})
public class DataSourceConfiguration {
    
    private final WriteDataSourceProperties writeProperties;
    private final ReadDataSourceProperties readProperties;
    
    public DataSourceConfiguration(WriteDataSourceProperties writeProperties,
                                 ReadDataSourceProperties readProperties) {
        this.writeProperties = writeProperties;
        this.readProperties = readProperties;
    }
    
    /**
     * DataSource para operações de escrita (Command Side).
     * 
     * <p>Configurado como @Primary para ser usado por padrão
     * quando não especificado explicitamente.
     */
    @Bean("writeDataSource")
    @Primary
    public DataSource writeDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configurações básicas
        config.setJdbcUrl(writeProperties.getUrl());
        config.setUsername(writeProperties.getUsername());
        config.setPassword(writeProperties.getPassword());
        config.setDriverClassName(writeProperties.getDriverClassName());
        
        // Configurações do pool
        WriteDataSourceProperties.HikariConfig hikariConfig = writeProperties.getHikari();
        config.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
        config.setMinimumIdle(hikariConfig.getMinimumIdle());
        config.setConnectionTimeout(hikariConfig.getConnectionTimeout());
        config.setIdleTimeout(hikariConfig.getIdleTimeout());
        config.setMaxLifetime(hikariConfig.getMaxLifetime());
        config.setValidationTimeout(hikariConfig.getValidationTimeout());
        config.setConnectionTestQuery(hikariConfig.getConnectionTestQuery());
        config.setPoolName(hikariConfig.getPoolName());
        
        // Configurações específicas para escrita
        config.setAutoCommit(false); // Controle manual de transações
        config.setReadOnly(false);
        
        // Configurações de performance
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("rewriteBatchedStatements", "true");
        
        return new HikariDataSource(config);
    }
    
    /**
     * DataSource para operações de leitura (Query Side).
     */
    @Bean("readDataSource")
    public DataSource readDataSource() {
        HikariConfig config = new HikariConfig();
        
        // Configurações básicas
        config.setJdbcUrl(readProperties.getUrl());
        config.setUsername(readProperties.getUsername());
        config.setPassword(readProperties.getPassword());
        config.setDriverClassName(readProperties.getDriverClassName());
        
        // Configurações do pool
        ReadDataSourceProperties.HikariConfig hikariConfig = readProperties.getHikari();
        config.setMaximumPoolSize(hikariConfig.getMaximumPoolSize());
        config.setMinimumIdle(hikariConfig.getMinimumIdle());
        config.setConnectionTimeout(hikariConfig.getConnectionTimeout());
        config.setIdleTimeout(hikariConfig.getIdleTimeout());
        config.setMaxLifetime(hikariConfig.getMaxLifetime());
        config.setValidationTimeout(hikariConfig.getValidationTimeout());
        config.setConnectionTestQuery(hikariConfig.getConnectionTestQuery());
        config.setPoolName(hikariConfig.getPoolName());
        
        // Configurações específicas para leitura
        config.setAutoCommit(true); // Auto-commit para consultas
        config.setReadOnly(hikariConfig.isReadOnly());
        
        // Configurações de performance para leitura
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "500");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "4096");
        config.addDataSourceProperty("useServerPrepStmts", "true");
        config.addDataSourceProperty("defaultRowFetchSize", "100");
        
        return new HikariDataSource(config);
    }
    
    /**
     * EntityManagerFactory para operações de escrita.
     */
    @Bean("writeEntityManagerFactory")
    @Primary
    public LocalContainerEntityManagerFactoryBean writeEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(writeDataSource());
        em.setPackagesToScan(
            "com.seguradora.hibrida.eventstore.entity",
            "com.seguradora.hibrida.snapshot.entity",
            "com.seguradora.hibrida.projection.tracking"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        WriteDataSourceProperties.JpaConfig jpaConfig = writeProperties.getJpa();
        
        properties.setProperty("hibernate.dialect", jpaConfig.getDialect());
        properties.setProperty("hibernate.hbm2ddl.auto", jpaConfig.getDdlAuto());
        properties.setProperty("hibernate.show_sql", String.valueOf(jpaConfig.isShowSql()));
        properties.setProperty("hibernate.format_sql", String.valueOf(jpaConfig.isFormatSql()));
        
        // Configurações de performance para escrita
        properties.setProperty("hibernate.jdbc.batch_size", String.valueOf(jpaConfig.getBatchSize()));
        properties.setProperty("hibernate.order_inserts", String.valueOf(jpaConfig.isOrderInserts()));
        properties.setProperty("hibernate.order_updates", String.valueOf(jpaConfig.isOrderUpdates()));
        properties.setProperty("hibernate.jdbc.batch_versioned_data", "true");
        properties.setProperty("hibernate.connection.provider_disables_autocommit", "true");
        
        em.setJpaProperties(properties);
        
        return em;
    }
    
    /**
     * EntityManagerFactory para operações de leitura.
     */
    @Bean("readEntityManagerFactory")
    public LocalContainerEntityManagerFactoryBean readEntityManagerFactory() {
        LocalContainerEntityManagerFactoryBean em = new LocalContainerEntityManagerFactoryBean();
        em.setDataSource(readDataSource());
        em.setPackagesToScan(
            "com.seguradora.hibrida.query.model"
        );
        
        HibernateJpaVendorAdapter vendorAdapter = new HibernateJpaVendorAdapter();
        em.setJpaVendorAdapter(vendorAdapter);
        
        Properties properties = new Properties();
        ReadDataSourceProperties.JpaConfig jpaConfig = readProperties.getJpa();
        
        properties.setProperty("hibernate.dialect", jpaConfig.getDialect());
        properties.setProperty("hibernate.hbm2ddl.auto", jpaConfig.getDdlAuto());
        properties.setProperty("hibernate.show_sql", String.valueOf(jpaConfig.isShowSql()));
        properties.setProperty("hibernate.format_sql", String.valueOf(jpaConfig.isFormatSql()));
        
        // Configurações de performance para leitura
        properties.setProperty("hibernate.jdbc.fetch_size", String.valueOf(jpaConfig.getFetchSize()));
        properties.setProperty("hibernate.cache.use_second_level_cache", String.valueOf(jpaConfig.isUseSecondLevelCache()));
        properties.setProperty("hibernate.cache.use_query_cache", String.valueOf(jpaConfig.isUseQueryCache()));
        properties.setProperty("hibernate.connection.autocommit", "true");
        
        em.setJpaProperties(properties);
        
        return em;
    }
    
    /**
     * Transaction Manager para operações de escrita.
     */
    @Bean("writeTransactionManager")
    @Primary
    public PlatformTransactionManager writeTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(writeEntityManagerFactory().getObject());
        transactionManager.setDataSource(writeDataSource());
        return transactionManager;
    }
    
    /**
     * Transaction Manager para operações de leitura.
     */
    @Bean("readTransactionManager")
    public PlatformTransactionManager readTransactionManager() {
        JpaTransactionManager transactionManager = new JpaTransactionManager();
        transactionManager.setEntityManagerFactory(readEntityManagerFactory().getObject());
        transactionManager.setDataSource(readDataSource());
        return transactionManager;
    }
    
    /**
     * Health Indicator para o DataSource de escrita.
     */
    @Bean("writeDataSourceHealthIndicator")
    public HealthIndicator writeDataSourceHealthIndicator() {
        return new SimpleDataSourceHealthIndicator(writeDataSource(), "WriteDataSource");
    }
    
    /**
     * Health Indicator para o DataSource de leitura.
     */
    @Bean("readDataSourceHealthIndicator")
    public HealthIndicator readDataSourceHealthIndicator() {
        return new SimpleDataSourceHealthIndicator(readDataSource(), "ReadDataSource");
    }
}