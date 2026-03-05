package com.seguradora.hibrida.config.datasource;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuração JPA para o DataSource de leitura (Query Side).
 * 
 * <p>Configura os repositories que devem usar o datasource de leitura:
 * <ul>
 *   <li>Query model repositories</li>
 *   <li>Projection repositories</li>
 *   <li>Read-only repositories</li>
 * </ul>
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "com.seguradora.hibrida.query.repository"
    },
    entityManagerFactoryRef = "readEntityManagerFactory",
    transactionManagerRef = "readTransactionManager"
)
public class ReadJpaConfiguration {
    
    // Esta classe serve apenas para configurar os repositories
    // A configuração real está em DataSourceConfiguration
}