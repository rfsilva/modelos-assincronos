# 💻 PRÁTICAS DE DESENVOLVIMENTO - PARTE 4
## CI/CD e Automação

### 🎯 **OBJETIVOS DESTA PARTE**
- Implementar pipeline CI/CD completo
- Configurar deploy automatizado
- Estabelecer estratégias de rollback
- Implementar blue-green deployment

---

## 🚀 **PIPELINE CI/CD COMPLETO**

### **📋 Visão Geral do Pipeline**

```mermaid
graph LR
    A[Push/PR] --> B[Build & Test]
    B --> C[Quality Gates]
    C --> D[Security Scan]
    D --> E[Package]
    E --> F[Deploy Dev]
    F --> G[Integration Tests]
    G --> H[Deploy Staging]
    H --> I[E2E Tests]
    I --> J[Deploy Production]
    J --> K[Health Check]
    K --> L[Rollback?]
```

### **🔧 GitHub Actions Workflow**

#### **.github/workflows/ci-cd.yml:**
```yaml
name: CI/CD Pipeline

on:
  push:
    branches: [main, develop]
  pull_request:
    branches: [main, develop]

env:
  REGISTRY: ghcr.io
  IMAGE_NAME: ${{ github.repository }}

jobs:
  # ===== BUILD & TEST =====
  build-and-test:
    runs-on: ubuntu-latest
    timeout-minutes: 20
    
    services:
      postgres:
        image: postgres:13
        env:
          POSTGRES_PASSWORD: postgres
        options: >-
          --health-cmd pg_isready
          --health-interval 10s
          --health-timeout 5s
          --health-retries 5
        ports:
          - 5432:5432
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run unit tests
        run: mvn test -B
      
      - name: Run integration tests
        run: mvn verify -B -P integration-tests
        env:
          SPRING_DATASOURCE_URL: jdbc:postgresql://localhost:5432/postgres
          SPRING_DATASOURCE_USERNAME: postgres
          SPRING_DATASOURCE_PASSWORD: postgres
      
      - name: Generate test report
        uses: dorny/test-reporter@v1
        if: success() || failure()
        with:
          name: Maven Tests
          path: target/surefire-reports/*.xml
          reporter: java-junit
      
      - name: Upload coverage to Codecov
        uses: codecov/codecov-action@v3
        with:
          file: ./target/site/jacoco/jacoco.xml

  # ===== QUALITY GATES =====
  quality-gates:
    runs-on: ubuntu-latest
    needs: build-and-test
    timeout-minutes: 15
    
    steps:
      - uses: actions/checkout@v4
        with:
          fetch-depth: 0
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
      
      - name: Code quality analysis
        run: |
          mvn compile -B
          mvn checkstyle:check pmd:check spotbugs:check
      
      - name: SonarCloud Scan
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: mvn sonar:sonar -B
      
      - name: Quality Gate check
        uses: sonarqube-quality-gate-action@master
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}

  # ===== SECURITY SCAN =====
  security-scan:
    runs-on: ubuntu-latest
    needs: build-and-test
    timeout-minutes: 10
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: OWASP Dependency Check
        run: mvn org.owasp:dependency-check-maven:check -B
      
      - name: Upload OWASP report
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: owasp-report
          path: target/dependency-check-report.html
      
      - name: Trivy vulnerability scanner
        uses: aquasecurity/trivy-action@master
        with:
          scan-type: 'fs'
          scan-ref: '.'
          format: 'sarif'
          output: 'trivy-results.sarif'
      
      - name: Upload Trivy scan results
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: 'trivy-results.sarif'

  # ===== BUILD & PACKAGE =====
  build-image:
    runs-on: ubuntu-latest
    needs: [quality-gates, security-scan]
    if: github.event_name == 'push'
    timeout-minutes: 15
    
    outputs:
      image: ${{ steps.image.outputs.image }}
      digest: ${{ steps.build.outputs.digest }}
    
    steps:
      - name: Checkout code
        uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Build application
        run: mvn package -B -DskipTests
      
      - name: Set up Docker Buildx
        uses: docker/setup-buildx-action@v3
      
      - name: Log in to Container Registry
        uses: docker/login-action@v3
        with:
          registry: ${{ env.REGISTRY }}
          username: ${{ github.actor }}
          password: ${{ secrets.GITHUB_TOKEN }}
      
      - name: Extract metadata
        id: meta
        uses: docker/metadata-action@v5
        with:
          images: ${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}
          tags: |
            type=ref,event=branch
            type=ref,event=pr
            type=sha,prefix={{branch}}-
            type=raw,value=latest,enable={{is_default_branch}}
      
      - name: Build and push Docker image
        id: build
        uses: docker/build-push-action@v5
        with:
          context: .
          push: true
          tags: ${{ steps.meta.outputs.tags }}
          labels: ${{ steps.meta.outputs.labels }}
          cache-from: type=gha
          cache-to: type=gha,mode=max
      
      - name: Output image
        id: image
        run: |
          echo "image=${{ env.REGISTRY }}/${{ env.IMAGE_NAME }}:${{ github.sha }}" >> $GITHUB_OUTPUT

  # ===== DEPLOY DEVELOPMENT =====
  deploy-dev:
    runs-on: ubuntu-latest
    needs: build-image
    if: github.ref == 'refs/heads/develop'
    environment: development
    timeout-minutes: 10
    
    steps:
      - name: Deploy to Development
        uses: ./.github/actions/deploy
        with:
          environment: development
          image: ${{ needs.build-image.outputs.image }}
          kubeconfig: ${{ secrets.KUBECONFIG_DEV }}
      
      - name: Run smoke tests
        run: |
          sleep 30  # Wait for deployment
          curl -f http://dev.arquitetura-hibrida.com/actuator/health

  # ===== INTEGRATION TESTS =====
  integration-tests-dev:
    runs-on: ubuntu-latest
    needs: deploy-dev
    timeout-minutes: 20
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run integration tests against dev
        run: mvn test -B -P integration-tests-dev
        env:
          TEST_BASE_URL: http://dev.arquitetura-hibrida.com

  # ===== DEPLOY STAGING =====
  deploy-staging:
    runs-on: ubuntu-latest
    needs: [build-image, integration-tests-dev]
    if: github.ref == 'refs/heads/main'
    environment: staging
    timeout-minutes: 10
    
    steps:
      - name: Deploy to Staging
        uses: ./.github/actions/deploy
        with:
          environment: staging
          image: ${{ needs.build-image.outputs.image }}
          kubeconfig: ${{ secrets.KUBECONFIG_STAGING }}

  # ===== E2E TESTS =====
  e2e-tests:
    runs-on: ubuntu-latest
    needs: deploy-staging
    timeout-minutes: 30
    
    steps:
      - uses: actions/checkout@v4
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
          cache: maven
      
      - name: Run E2E tests
        run: mvn test -B -P e2e-tests
        env:
          TEST_BASE_URL: http://staging.arquitetura-hibrida.com
      
      - name: Upload E2E test results
        uses: actions/upload-artifact@v3
        if: always()
        with:
          name: e2e-test-results
          path: target/e2e-reports/

  # ===== DEPLOY PRODUCTION =====
  deploy-production:
    runs-on: ubuntu-latest
    needs: [build-image, e2e-tests]
    if: github.ref == 'refs/heads/main'
    environment: production
    timeout-minutes: 15
    
    steps:
      - name: Deploy to Production
        uses: ./.github/actions/deploy
        with:
          environment: production
          image: ${{ needs.build-image.outputs.image }}
          kubeconfig: ${{ secrets.KUBECONFIG_PROD }}
          strategy: blue-green
      
      - name: Health check
        run: |
          sleep 60  # Wait for deployment
          curl -f https://api.arquitetura-hibrida.com/actuator/health
      
      - name: Notify deployment
        uses: 8398a7/action-slack@v3
        with:
          status: ${{ job.status }}
          channel: '#deployments'
          webhook_url: ${{ secrets.SLACK_WEBHOOK }}
```

---

## 🐳 **CONTAINERIZAÇÃO**

### **📦 Multi-stage Dockerfile**

#### **Dockerfile:**
```dockerfile
# ===== BUILD STAGE =====
FROM maven:3.9-openjdk-17-slim AS builder

WORKDIR /app

# Copy dependency files first for better caching
COPY pom.xml .
COPY .mvn .mvn
COPY mvnw .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build application
RUN mvn package -B -DskipTests && \
    java -Djarmode=layertools -jar target/*.jar extract

# ===== RUNTIME STAGE =====
FROM openjdk:17-jre-slim

# Install monitoring and debugging tools
RUN apt-get update && \
    apt-get install -y curl jq && \
    rm -rf /var/lib/apt/lists/*

# Create non-root user
RUN groupadd -r appuser && useradd -r -g appuser appuser

WORKDIR /app

# Copy application layers for better caching
COPY --from=builder app/dependencies/ ./
COPY --from=builder app/spring-boot-loader/ ./
COPY --from=builder app/snapshot-dependencies/ ./
COPY --from=builder app/application/ ./

# Create logs directory
RUN mkdir -p /app/logs && chown -R appuser:appuser /app

# Switch to non-root user
USER appuser

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD curl -f http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM optimization for containers
ENV JAVA_OPTS="-XX:+UseContainerSupport \
               -XX:MaxRAMPercentage=75.0 \
               -XX:+UseG1GC \
               -XX:+UseStringDeduplication \
               -XX:+PrintGCDetails \
               -XX:+PrintGCTimeStamps \
               -Xloggc:/app/logs/gc.log"

# Application configuration
ENV SPRING_PROFILES_ACTIVE=docker

# Start application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS org.springframework.boot.loader.JarLauncher"]
```

### **🔧 Docker Compose para Desenvolvimento**

#### **docker-compose.dev.yml:**
```yaml
version: '3.8'

services:
  app:
    build:
      context: .
      dockerfile: Dockerfile
      target: builder
    ports:
      - "8080:8080"
      - "5005:5005"  # Debug port
    environment:
      - SPRING_PROFILES_ACTIVE=dev
      - JAVA_OPTS=-agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
      - SPRING_DATASOURCE_WRITE_URL=jdbc:postgresql://eventstore-db:5432/eventstore
      - SPRING_DATASOURCE_READ_URL=jdbc:postgresql://projections-db:5432/projections
    depends_on:
      - eventstore-db
      - projections-db
      - redis
    volumes:
      - ./logs:/app/logs
      - ./target:/app/target
    networks:
      - app-network

  eventstore-db:
    image: postgres:13
    environment:
      POSTGRES_DB: eventstore
      POSTGRES_USER: eventstore
      POSTGRES_PASSWORD: eventstore123
    ports:
      - "5432:5432"
    volumes:
      - eventstore_data:/var/lib/postgresql/data
      - ./docker/init-eventstore-db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - app-network

  projections-db:
    image: postgres:13
    environment:
      POSTGRES_DB: projections
      POSTGRES_USER: projections
      POSTGRES_PASSWORD: projections123
    ports:
      - "5433:5432"
    volumes:
      - projections_data:/var/lib/postgresql/data
      - ./docker/init-projections-db.sql:/docker-entrypoint-initdb.d/init.sql
    networks:
      - app-network

  redis:
    image: redis:6-alpine
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    networks:
      - app-network

volumes:
  eventstore_data:
  projections_data:
  redis_data:

networks:
  app-network:
    driver: bridge
```

---

## ☸️ **KUBERNETES DEPLOYMENT**

### **📋 Kubernetes Manifests**

#### **k8s/namespace.yaml:**
```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: arquitetura-hibrida
  labels:
    name: arquitetura-hibrida
    environment: production
```

#### **k8s/configmap.yaml:**
```yaml
apiVersion: v1
kind: ConfigMap
metadata:
  name: app-config
  namespace: arquitetura-hibrida
data:
  application.yml: |
    spring:
      profiles:
        active: kubernetes
      datasource:
        write:
          url: jdbc:postgresql://eventstore-db:5432/eventstore
          username: ${DB_WRITE_USERNAME}
          password: ${DB_WRITE_PASSWORD}
        read:
          url: jdbc:postgresql://projections-db:5432/projections
          username: ${DB_READ_USERNAME}
          password: ${DB_READ_PASSWORD}
      redis:
        host: redis-service
        port: 6379
    
    management:
      endpoints:
        web:
          exposure:
            include: "*"
      endpoint:
        health:
          show-details: always
    
    logging:
      level:
        com.seguradora.hibrida: INFO
      pattern:
        console: "%d{yyyy-MM-dd HH:mm:ss} [%thread] %-5level [%X{traceId:-},%X{spanId:-}] %logger{36} - %msg%n"
```

#### **k8s/secret.yaml:**
```yaml
apiVersion: v1
kind: Secret
metadata:
  name: app-secrets
  namespace: arquitetura-hibrida
type: Opaque
data:
  db-write-username: ZXZlbnRzdG9yZQ==  # eventstore (base64)
  db-write-password: ZXZlbnRzdG9yZTEyMw==  # eventstore123 (base64)
  db-read-username: cHJvamVjdGlvbnM=  # projections (base64)
  db-read-password: cHJvamVjdGlvbnMxMjM=  # projections123 (base64)
```

#### **k8s/deployment.yaml:**
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: app-deployment
  namespace: arquitetura-hibrida
  labels:
    app: arquitetura-hibrida
    version: v1
spec:
  replicas: 3
  strategy:
    type: RollingUpdate
    rollingUpdate:
      maxSurge: 1
      maxUnavailable: 0
  selector:
    matchLabels:
      app: arquitetura-hibrida
  template:
    metadata:
      labels:
        app: arquitetura-hibrida
        version: v1
      annotations:
        prometheus.io/scrape: "true"
        prometheus.io/port: "8080"
        prometheus.io/path: "/actuator/prometheus"
    spec:
      containers:
      - name: app
        image: ghcr.io/seguradora/arquitetura-hibrida:latest
        ports:
        - containerPort: 8080
          name: http
        env:
        - name: DB_WRITE_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-write-username
        - name: DB_WRITE_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-write-password
        - name: DB_READ_USERNAME
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-read-username
        - name: DB_READ_PASSWORD
          valueFrom:
            secretKeyRef:
              name: app-secrets
              key: db-read-password
        - name: JAVA_OPTS
          value: "-XX:+UseContainerSupport -XX:MaxRAMPercentage=75.0"
        
        volumeMounts:
        - name: config-volume
          mountPath: /app/config
        - name: logs-volume
          mountPath: /app/logs
        
        resources:
          requests:
            memory: "512Mi"
            cpu: "250m"
          limits:
            memory: "1Gi"
            cpu: "500m"
        
        livenessProbe:
          httpGet:
            path: /actuator/health/liveness
            port: 8080
          initialDelaySeconds: 60
          periodSeconds: 30
          timeoutSeconds: 10
          failureThreshold: 3
        
        readinessProbe:
          httpGet:
            path: /actuator/health/readiness
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 3
        
        startupProbe:
          httpGet:
            path: /actuator/health
            port: 8080
          initialDelaySeconds: 30
          periodSeconds: 10
          timeoutSeconds: 5
          failureThreshold: 30
      
      volumes:
      - name: config-volume
        configMap:
          name: app-config
      - name: logs-volume
        emptyDir: {}
      
      securityContext:
        runAsNonRoot: true
        runAsUser: 1000
        fsGroup: 1000
```

#### **k8s/service.yaml:**
```yaml
apiVersion: v1
kind: Service
metadata:
  name: app-service
  namespace: arquitetura-hibrida
  labels:
    app: arquitetura-hibrida
spec:
  selector:
    app: arquitetura-hibrida
  ports:
  - name: http
    port: 80
    targetPort: 8080
    protocol: TCP
  type: ClusterIP
```

#### **k8s/ingress.yaml:**
```yaml
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: app-ingress
  namespace: arquitetura-hibrida
  annotations:
    kubernetes.io/ingress.class: nginx
    cert-manager.io/cluster-issuer: letsencrypt-prod
    nginx.ingress.kubernetes.io/rate-limit: "100"
    nginx.ingress.kubernetes.io/rate-limit-window: "1m"
spec:
  tls:
  - hosts:
    - api.arquitetura-hibrida.com
    secretName: app-tls
  rules:
  - host: api.arquitetura-hibrida.com
    http:
      paths:
      - path: /
        pathType: Prefix
        backend:
          service:
            name: app-service
            port:
              number: 80
```

---

## 🔄 **ESTRATÉGIAS DE DEPLOYMENT**

### **🔵🟢 Blue-Green Deployment**

#### **.github/actions/deploy/action.yml:**
```yaml
name: 'Deploy Application'
description: 'Deploy application with blue-green strategy'

inputs:
  environment:
    description: 'Target environment'
    required: true
  image:
    description: 'Docker image to deploy'
    required: true
  kubeconfig:
    description: 'Kubernetes config'
    required: true
  strategy:
    description: 'Deployment strategy'
    required: false
    default: 'rolling'

runs:
  using: 'composite'
  steps:
    - name: Setup kubectl
      uses: azure/setup-kubectl@v3
      with:
        version: 'v1.28.0'
    
    - name: Setup kubeconfig
      shell: bash
      run: |
        mkdir -p ~/.kube
        echo "${{ inputs.kubeconfig }}" | base64 -d > ~/.kube/config
        chmod 600 ~/.kube/config
    
    - name: Blue-Green Deployment
      if: inputs.strategy == 'blue-green'
      shell: bash
      run: |
        # Get current active color
        CURRENT_COLOR=$(kubectl get service app-service -o jsonpath='{.spec.selector.color}' || echo "blue")
        NEW_COLOR=$([ "$CURRENT_COLOR" = "blue" ] && echo "green" || echo "blue")
        
        echo "Current color: $CURRENT_COLOR"
        echo "Deploying to: $NEW_COLOR"
        
        # Update deployment with new color
        kubectl patch deployment app-deployment -p '{"spec":{"template":{"metadata":{"labels":{"color":"'$NEW_COLOR'"}}}}}'
        kubectl patch deployment app-deployment -p '{"spec":{"selector":{"matchLabels":{"color":"'$NEW_COLOR'"}}}}'
        kubectl set image deployment/app-deployment app=${{ inputs.image }}
        
        # Wait for rollout
        kubectl rollout status deployment/app-deployment --timeout=300s
        
        # Health check on new deployment
        kubectl wait --for=condition=ready pod -l color=$NEW_COLOR --timeout=300s
        
        # Switch traffic to new color
        kubectl patch service app-service -p '{"spec":{"selector":{"color":"'$NEW_COLOR'"}}}'
        
        echo "Deployment completed. Traffic switched to $NEW_COLOR"
    
    - name: Rolling Update
      if: inputs.strategy != 'blue-green'
      shell: bash
      run: |
        kubectl set image deployment/app-deployment app=${{ inputs.image }}
        kubectl rollout status deployment/app-deployment --timeout=300s
    
    - name: Verify deployment
      shell: bash
      run: |
        # Wait for pods to be ready
        kubectl wait --for=condition=ready pod -l app=arquitetura-hibrida --timeout=300s
        
        # Health check
        kubectl get pods -l app=arquitetura-hibrida
        
        # Test endpoint
        SERVICE_IP=$(kubectl get service app-service -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
        if [ -n "$SERVICE_IP" ]; then
          curl -f http://$SERVICE_IP/actuator/health
        fi
```

### **⏪ Rollback Strategy**

#### **rollback.sh:**
```bash
#!/bin/bash

set -e

NAMESPACE="arquitetura-hibrida"
DEPLOYMENT="app-deployment"

echo "🔄 Starting rollback process..."

# Get rollout history
echo "📋 Rollout history:"
kubectl rollout history deployment/$DEPLOYMENT -n $NAMESPACE

# Get previous revision
PREVIOUS_REVISION=$(kubectl rollout history deployment/$DEPLOYMENT -n $NAMESPACE --revision=0 | tail -2 | head -1 | awk '{print $1}')

echo "⏪ Rolling back to revision: $PREVIOUS_REVISION"

# Perform rollback
kubectl rollout undo deployment/$DEPLOYMENT -n $NAMESPACE --to-revision=$PREVIOUS_REVISION

# Wait for rollback to complete
echo "⏳ Waiting for rollback to complete..."
kubectl rollout status deployment/$DEPLOYMENT -n $NAMESPACE --timeout=300s

# Verify health
echo "🏥 Verifying application health..."
kubectl wait --for=condition=ready pod -l app=arquitetura-hibrida -n $NAMESPACE --timeout=300s

# Test endpoint
SERVICE_IP=$(kubectl get service app-service -n $NAMESPACE -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
if [ -n "$SERVICE_IP" ]; then
    if curl -f http://$SERVICE_IP/actuator/health; then
        echo "✅ Rollback completed successfully!"
    else
        echo "❌ Health check failed after rollback!"
        exit 1
    fi
else
    echo "⚠️  Could not get service IP for health check"
fi

echo "🎉 Rollback process completed!"
```

---

## 📊 **MONITORAMENTO DE DEPLOYMENT**

### **📈 Deployment Metrics**

#### **deployment-dashboard.json:**
```json
{
  "dashboard": {
    "title": "Deployment Monitoring",
    "panels": [
      {
        "title": "Deployment Status",
        "type": "stat",
        "targets": [
          {
            "expr": "kube_deployment_status_replicas_available{deployment=\"app-deployment\"}",
            "legendFormat": "Available Replicas"
          }
        ]
      },
      {
        "title": "Pod Restart Rate",
        "type": "graph",
        "targets": [
          {
            "expr": "rate(kube_pod_container_status_restarts_total{pod=~\"app-deployment-.*\"}[5m])",
            "legendFormat": "{{pod}}"
          }
        ]
      },
      {
        "title": "Deployment Events",
        "type": "logs",
        "targets": [
          {
            "expr": "{namespace=\"arquitetura-hibrida\"} |= \"deployment\""
          }
        ]
      }
    ]
  }
}
```

### **🚨 Deployment Alerts**

#### **deployment-alerts.yml:**
```yaml
groups:
  - name: deployment-alerts
    rules:
      - alert: DeploymentReplicasMismatch
        expr: kube_deployment_spec_replicas != kube_deployment_status_replicas_available
        for: 5m
        labels:
          severity: warning
        annotations:
          summary: "Deployment replicas mismatch"
          description: "Deployment {{ $labels.deployment }} has {{ $value }} available replicas, expected {{ $labels.spec_replicas }}"
      
      - alert: PodCrashLooping
        expr: rate(kube_pod_container_status_restarts_total[15m]) > 0
        for: 5m
        labels:
          severity: critical
        annotations:
          summary: "Pod is crash looping"
          description: "Pod {{ $labels.pod }} is restarting frequently"
      
      - alert: DeploymentFailed
        expr: kube_deployment_status_condition{condition="Progressing",status="false"} == 1
        for: 10m
        labels:
          severity: critical
        annotations:
          summary: "Deployment failed"
          description: "Deployment {{ $labels.deployment }} has failed to progress"
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [GitHub Actions Documentation](https://docs.github.com/en/actions)
- [Kubernetes Best Practices](https://kubernetes.io/docs/concepts/configuration/overview/)
- [Docker Multi-stage Builds](https://docs.docker.com/develop/dev-best-practices/)
- [Blue-Green Deployment](https://martinfowler.com/bliki/BlueGreenDeployment.html)

### **📖 Próxima Parte:**
- **Parte 5**: Documentação e Knowledge Sharing

---

**📝 Parte 4 de 5 - CI/CD e Automação**  
**⏱️ Tempo estimado**: 75 minutos  
**🎯 Próximo**: [Parte 5 - Documentação e Knowledge Sharing](./12-praticas-desenvolvimento-parte-5.md)