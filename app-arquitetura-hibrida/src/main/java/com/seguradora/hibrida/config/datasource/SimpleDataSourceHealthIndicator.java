package com.seguradora.hibrida.config.datasource;

import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Health Indicator customizado para DataSources.
 * 
 * <p>Verifica a saúde de um DataSource específico executando
 * uma query simples e coletando métricas de conexão.
 */
public class SimpleDataSourceHealthIndicator implements HealthIndicator {
    
    private final DataSource dataSource;
    private final JdbcTemplate jdbcTemplate;
    private final String name;
    private final String testQuery;
    
    public SimpleDataSourceHealthIndicator(DataSource dataSource, String name) {
        this(dataSource, name, "SELECT 1");
    }
    
    public SimpleDataSourceHealthIndicator(DataSource dataSource, String name, String testQuery) {
        this.dataSource = dataSource;
        this.jdbcTemplate = new JdbcTemplate(dataSource);
        this.name = name;
        this.testQuery = testQuery;
    }
    
    @Override
    public Health health() {
        try {
            return checkDataSourceHealth();
        } catch (Exception e) {
            return Health.down()
                    .withDetail("datasource", name)
                    .withDetail("error", e.getMessage())
                    .withException(e)
                    .build();
        }
    }
    
    private Health checkDataSourceHealth() {
        Map<String, Object> details = new HashMap<>();
        details.put("datasource", name);
        
        try {
            // Teste de conectividade básica
            long startTime = System.currentTimeMillis();
            Integer result = jdbcTemplate.queryForObject(testQuery, Integer.class);
            long responseTime = System.currentTimeMillis() - startTime;
            
            details.put("query", testQuery);
            details.put("result", result);
            details.put("responseTime", responseTime + "ms");
            
            // Informações do pool de conexões (se disponível)
            addConnectionPoolInfo(details);
            
            // Informações da conexão
            addConnectionInfo(details);
            
            return Health.up().withDetails(details).build();
            
        } catch (DataAccessException e) {
            details.put("error", "Falha na execução da query: " + e.getMessage());
            return Health.down().withDetails(details).withException(e).build();
        }
    }
    
    private void addConnectionPoolInfo(Map<String, Object> details) {
        try {
            // Tenta obter informações do HikariCP se disponível
            if (dataSource.getClass().getName().contains("HikariDataSource")) {
                addHikariPoolInfo(details);
            }
        } catch (Exception e) {
            // Ignora erros ao obter informações do pool
            details.put("poolInfo", "Não disponível: " + e.getMessage());
        }
    }
    
    private void addHikariPoolInfo(Map<String, Object> details) {
        try {
            // Usa reflection para acessar métricas do HikariCP
            Object hikariPoolMXBean = dataSource.getClass().getMethod("getHikariPoolMXBean").invoke(dataSource);
            
            if (hikariPoolMXBean != null) {
                Map<String, Object> poolInfo = new HashMap<>();
                
                // Conexões ativas
                Integer activeConnections = (Integer) hikariPoolMXBean.getClass()
                        .getMethod("getActiveConnections").invoke(hikariPoolMXBean);
                poolInfo.put("activeConnections", activeConnections);
                
                // Conexões idle
                Integer idleConnections = (Integer) hikariPoolMXBean.getClass()
                        .getMethod("getIdleConnections").invoke(hikariPoolMXBean);
                poolInfo.put("idleConnections", idleConnections);
                
                // Total de conexões
                Integer totalConnections = (Integer) hikariPoolMXBean.getClass()
                        .getMethod("getTotalConnections").invoke(hikariPoolMXBean);
                poolInfo.put("totalConnections", totalConnections);
                
                // Threads aguardando conexão
                Integer threadsAwaitingConnection = (Integer) hikariPoolMXBean.getClass()
                        .getMethod("getThreadsAwaitingConnection").invoke(hikariPoolMXBean);
                poolInfo.put("threadsAwaitingConnection", threadsAwaitingConnection);
                
                details.put("connectionPool", poolInfo);
            }
        } catch (Exception e) {
            details.put("connectionPool", "Erro ao obter informações: " + e.getMessage());
        }
    }
    
    private void addConnectionInfo(Map<String, Object> details) {
        try (Connection connection = dataSource.getConnection()) {
            Map<String, Object> connectionInfo = new HashMap<>();
            
            connectionInfo.put("url", connection.getMetaData().getURL());
            connectionInfo.put("username", connection.getMetaData().getUserName());
            connectionInfo.put("driverName", connection.getMetaData().getDriverName());
            connectionInfo.put("driverVersion", connection.getMetaData().getDriverVersion());
            connectionInfo.put("databaseProductName", connection.getMetaData().getDatabaseProductName());
            connectionInfo.put("databaseProductVersion", connection.getMetaData().getDatabaseProductVersion());
            connectionInfo.put("autoCommit", connection.getAutoCommit());
            connectionInfo.put("readOnly", connection.isReadOnly());
            connectionInfo.put("transactionIsolation", getTransactionIsolationName(connection.getTransactionIsolation()));
            
            details.put("connection", connectionInfo);
            
        } catch (SQLException e) {
            details.put("connection", "Erro ao obter informações: " + e.getMessage());
        }
    }
    
    private String getTransactionIsolationName(int level) {
        return switch (level) {
            case Connection.TRANSACTION_NONE -> "NONE";
            case Connection.TRANSACTION_READ_UNCOMMITTED -> "READ_UNCOMMITTED";
            case Connection.TRANSACTION_READ_COMMITTED -> "READ_COMMITTED";
            case Connection.TRANSACTION_REPEATABLE_READ -> "REPEATABLE_READ";
            case Connection.TRANSACTION_SERIALIZABLE -> "SERIALIZABLE";
            default -> "UNKNOWN(" + level + ")";
        };
    }
}