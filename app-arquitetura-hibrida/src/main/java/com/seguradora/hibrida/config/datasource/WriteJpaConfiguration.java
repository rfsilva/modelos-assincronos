package com.seguradora.hibrida.config.datasource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/**
 * Configuração JPA para o DataSource de escrita (Command Side).
 * 
 * <p>Configura os repositories que devem usar o datasource de escrita:
 * <ul>
 *   <li>Event Store repositories</li>
 *   <li>Snapshot repositories</li>
 *   <li>Projection tracking repositories</li>
 * </ul>
 */
@Configuration
@EnableTransactionManagement
@EnableJpaRepositories(
    basePackages = {
        "com.seguradora.hibrida.eventstore.repository",
        "com.seguradora.hibrida.snapshot.repository",
        "com.seguradora.hibrida.projection.tracking"
    },
    entityManagerFactoryRef = "writeEntityManagerFactory",
    transactionManagerRef = "writeTransactionManager"
)
public class WriteJpaConfiguration {
    
    // Esta classe serve apenas para configurar os repositories
    // A configuração real está em DataSourceConfiguration
}