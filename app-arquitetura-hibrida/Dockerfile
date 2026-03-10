# =====================================================
# Dockerfile Multi-stage para Aplicação Spring Boot
# =====================================================
# Autor: Principal Java Architect
# Data: 2024-12-19
# Descrição: Build otimizado com multi-stage para produção
# =====================================================

# Stage 1: Build da aplicação
FROM openjdk:21-jdk-slim as builder

# Instalar Maven
RUN apt-get update && apt-get install -y maven && rm -rf /var/lib/apt/lists/*

# Definir diretório de trabalho
WORKDIR /app

# Copiar arquivos de configuração do Maven
COPY pom.xml .
COPY src ./src

# Build da aplicação (pular testes para build mais rápido)
RUN mvn clean package -DskipTests

# Stage 2: Runtime da aplicação
FROM openjdk:21-jre-slim

# Instalar curl para health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Criar usuário não-root para segurança
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Definir diretório de trabalho
WORKDIR /app

# Criar diretórios necessários
RUN mkdir -p /app/logs /app/data /app/data/archives && \
    chown -R appuser:appuser /app

# Copiar JAR da aplicação do stage anterior
COPY --from=builder /app/target/*.jar app.jar

# Alterar proprietário do JAR
RUN chown appuser:appuser app.jar

# Mudar para usuário não-root
USER appuser

# Expor porta da aplicação
EXPOSE 8083

# Configurar JVM para container
ENV JAVA_OPTS="-Xmx1g -Xms512m -XX:+UseG1GC -XX:+UseStringDeduplication -XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8083/api/v1/actuator/health || exit 1

# Comando de inicialização
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]