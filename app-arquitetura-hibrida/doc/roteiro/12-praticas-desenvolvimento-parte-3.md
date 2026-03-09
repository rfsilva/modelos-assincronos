# 💻 PRÁTICAS DE DESENVOLVIMENTO - PARTE 3
## Code Review e Qualidade

### 🎯 **OBJETIVOS DESTA PARTE**
- Estabelecer processo de code review eficiente
- Implementar gates de qualidade automatizados
- Definir métricas de qualidade de código
- Configurar ferramentas de análise estática

---

## 👥 **PROCESSO DE CODE REVIEW**

### **📋 Filosofia de Code Review**

O code review na arquitetura híbrida deve focar em:

#### **Aspectos Técnicos:**
- ✅ **Arquitetura**: Aderência aos padrões CQRS/Event Sourcing
- ✅ **Performance**: Otimizações específicas para event store e projeções
- ✅ **Segurança**: Validações e sanitização de dados
- ✅ **Testabilidade**: Cobertura e qualidade dos testes

#### **Aspectos de Negócio:**
- ✅ **Domain Logic**: Implementação correta das regras de negócio
- ✅ **Event Design**: Estrutura e versionamento de eventos
- ✅ **API Design**: Consistência e usabilidade das APIs
- ✅ **Error Handling**: Tratamento adequado de erros

### **🔍 Checklist de Code Review**

#### **Template de PR Review:**
```markdown
## 🔍 Code Review Checklist

### 📐 Arquitetura
- [ ] Separação clara entre Command e Query side
- [ ] Eventos bem estruturados e versionados
- [ ] Agregados respeitam invariantes de negócio
- [ ] Projeções são idempotentes
- [ ] Não há vazamento de conceitos entre camadas

### 🎯 Lógica de Negócio
- [ ] Regras de negócio implementadas corretamente
- [ ] Validações adequadas nos comandos
- [ ] Event handlers processam eventos corretamente
- [ ] Tratamento de casos edge

### 🧪 Testes
- [ ] Cobertura de testes adequada (>80%)
- [ ] Testes unitários para lógica de domínio
- [ ] Testes de integração para fluxos completos
- [ ] Testes de contrato para APIs

### 🚀 Performance
- [ ] Consultas otimizadas (sem N+1)
- [ ] Índices adequados nas projeções
- [ ] Paginação implementada corretamente
- [ ] Sem vazamentos de memória

### 🔒 Segurança
- [ ] Validação de entrada adequada
- [ ] Sanitização de dados
- [ ] Autorização implementada
- [ ] Logs não expõem dados sensíveis

### 📝 Documentação
- [ ] JavaDoc atualizado
- [ ] README atualizado se necessário
- [ ] Changelog atualizado
- [ ] API documentation atualizada
```

### **🎯 Níveis de Review**

#### **Level 1 - Automated Review:**
```yaml
# .github/workflows/automated-review.yml
name: Automated Code Review

on:
  pull_request:
    branches: [develop, main]

jobs:
  automated-review:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Run Tests
        run: mvn test
      
      - name: Code Quality Check
        run: |
          mvn checkstyle:check
          mvn pmd:check
          mvn spotbugs:check
      
      - name: Security Scan
        run: mvn org.owasp:dependency-check-maven:check
      
      - name: Coverage Report
        run: mvn jacoco:report
      
      - name: SonarQube Analysis
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: mvn sonar:sonar
```

#### **Level 2 - Peer Review:**
```markdown
## 👨‍💻 Peer Review Guidelines

### Para o Autor:
1. **Self-review primeiro**: Revise seu próprio código antes de criar PR
2. **Contexto claro**: Descreva o que foi feito e por quê
3. **Testes incluídos**: Sempre inclua testes para novas funcionalidades
4. **Commits limpos**: Use commits atômicos e mensagens claras
5. **Documentação**: Atualize documentação relevante

### Para o Reviewer:
1. **Seja construtivo**: Foque em melhorias, não críticas pessoais
2. **Explique o "porquê"**: Não apenas aponte problemas, explique
3. **Considere alternativas**: Sugira soluções quando possível
4. **Teste localmente**: Para mudanças complexas, teste o código
5. **Aprove rapidamente**: Não deixe PRs esperando desnecessariamente
```

#### **Level 3 - Architecture Review:**
```markdown
## 🏗️ Architecture Review (Senior/Architect)

### Quando Necessário:
- Mudanças na estrutura de eventos
- Novos agregados ou bounded contexts
- Alterações em padrões arquiteturais
- Performance crítica
- Mudanças de API públicas

### Aspectos Avaliados:
- Aderência aos princípios DDD
- Consistência com padrões estabelecidos
- Impacto em outros bounded contexts
- Estratégia de migração para breaking changes
- Documentação arquitetural
```

---

## 🔧 **FERRAMENTAS DE QUALIDADE**

### **📊 SonarQube Configuration**

#### **sonar-project.properties:**
```properties
# Configuração do projeto
sonar.projectKey=arquitetura-hibrida
sonar.projectName=Arquitetura Híbrida - Sinistros
sonar.projectVersion=1.0

# Configurações de análise
sonar.sources=src/main/java
sonar.tests=src/test/java
sonar.java.binaries=target/classes
sonar.java.test.binaries=target/test-classes

# Cobertura de código
sonar.coverage.jacoco.xmlReportPaths=target/site/jacoco/jacoco.xml
sonar.junit.reportPaths=target/surefire-reports

# Exclusões
sonar.exclusions=**/*Configuration.java,**/*Properties.java,**/dto/**,**/config/**
sonar.test.exclusions=**/*Test.java,**/*IT.java

# Quality Gates customizados
sonar.qualitygate.wait=true

# Configurações específicas para arquitetura
sonar.issue.ignore.multicriteria=e1,e2,e3

# Ignorar complexidade em agregados (lógica de domínio complexa é esperada)
sonar.issue.ignore.multicriteria.e1.ruleKey=java:S3776
sonar.issue.ignore.multicriteria.e1.resourceKey=**/aggregate/**/*.java

# Ignorar "too many parameters" em construtores de eventos
sonar.issue.ignore.multicriteria.e2.ruleKey=java:S107
sonar.issue.ignore.multicriteria.e2.resourceKey=**/*Event.java

# Ignorar serialVersionUID em DTOs
sonar.issue.ignore.multicriteria.e3.ruleKey=java:S2057
sonar.issue.ignore.multicriteria.e3.resourceKey=**/dto/**/*.java
```

#### **Quality Gates Customizados:**
```json
{
  "name": "Arquitetura Híbrida Quality Gate",
  "conditions": [
    {
      "metric": "coverage",
      "op": "LT",
      "error": "80.0"
    },
    {
      "metric": "duplicated_lines_density",
      "op": "GT", 
      "error": "3.0"
    },
    {
      "metric": "maintainability_rating",
      "op": "GT",
      "error": "1"
    },
    {
      "metric": "reliability_rating",
      "op": "GT",
      "error": "1"
    },
    {
      "metric": "security_rating",
      "op": "GT",
      "error": "1"
    },
    {
      "metric": "sqale_rating",
      "op": "GT",
      "error": "1"
    }
  ]
}
```

### **🔍 Análise de Dependências**

#### **OWASP Dependency Check:**
```xml
<!-- pom.xml -->
<plugin>
    <groupId>org.owasp</groupId>
    <artifactId>dependency-check-maven</artifactId>
    <version>8.4.0</version>
    <configuration>
        <failBuildOnCVSS>7</failBuildOnCVSS>
        <suppressionFiles>
            <suppressionFile>owasp-suppressions.xml</suppressionFile>
        </suppressionFiles>
        <formats>
            <format>HTML</format>
            <format>JSON</format>
        </formats>
    </configuration>
    <executions>
        <execution>
            <goals>
                <goal>check</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

#### **owasp-suppressions.xml:**
```xml
<?xml version="1.0" encoding="UTF-8"?>
<suppressions xmlns="https://jeremylong.github.io/DependencyCheck/dependency-suppression.1.3.xsd">
    
    <!-- Suprimir falsos positivos conhecidos -->
    <suppress>
        <notes>Spring Boot starter - falso positivo</notes>
        <packageUrl regex="true">^pkg:maven/org\.springframework\.boot/spring-boot-starter.*$</packageUrl>
        <cve>CVE-2016-1000027</cve>
    </suppress>
    
    <!-- Suprimir vulnerabilidades em dependências de teste -->
    <suppress>
        <notes>Dependência apenas de teste</notes>
        <packageUrl regex="true">^pkg:maven/org\.testcontainers/.*$</packageUrl>
        <cve>CVE-2021-29425</cve>
    </suppress>
    
</suppressions>
```

---

## 📊 **MÉTRICAS DE QUALIDADE**

### **🎯 Definição de Métricas**

#### **Métricas de Código:**
```yaml
code_metrics:
  coverage:
    target: 85%
    minimum: 80%
    critical_paths: 95%  # Agregados e Command Handlers
    
  complexity:
    cyclomatic_max: 10
    cognitive_max: 15
    
  maintainability:
    technical_debt_ratio: < 5%
    code_smells: < 50 per 1000 lines
    
  duplication:
    max_percentage: 3%
    max_blocks: 10
    
  size:
    max_method_lines: 50
    max_class_lines: 500
    max_parameters: 7
```

#### **Métricas de Arquitetura:**
```yaml
architecture_metrics:
  coupling:
    afferent_coupling: < 10  # Ca
    efferent_coupling: < 10  # Ce
    instability: 0.3 - 0.7   # I = Ce / (Ca + Ce)
    
  cohesion:
    lcom4: < 2  # Lack of Cohesion of Methods
    
  abstraction:
    abstractness: 0.2 - 0.8  # A = Abstract Classes / Total Classes
    
  distance_from_main_sequence:
    max_distance: 0.3  # D = |A + I - 1|
```

### **📈 Dashboard de Qualidade**

#### **Grafana Dashboard - Code Quality:**
```json
{
  "dashboard": {
    "title": "Code Quality Metrics",
    "panels": [
      {
        "title": "Test Coverage Trend",
        "type": "graph",
        "targets": [
          {
            "expr": "sonarqube_coverage_percentage",
            "legendFormat": "Coverage %"
          }
        ],
        "yAxes": [
          {"min": 0, "max": 100, "unit": "percent"}
        ],
        "thresholds": [
          {"value": 80, "colorMode": "critical", "op": "lt"}
        ]
      },
      {
        "title": "Technical Debt",
        "type": "stat",
        "targets": [
          {
            "expr": "sonarqube_technical_debt_minutes",
            "legendFormat": "Debt (minutes)"
          }
        ],
        "fieldConfig": {
          "unit": "minutes",
          "thresholds": {
            "steps": [
              {"color": "green", "value": 0},
              {"color": "yellow", "value": 480},
              {"color": "red", "value": 960}
            ]
          }
        }
      },
      {
        "title": "Code Smells by Type",
        "type": "piechart",
        "targets": [
          {
            "expr": "sonarqube_code_smells_by_type",
            "legendFormat": "{{type}}"
          }
        ]
      }
    ]
  }
}
```

---

## 🚦 **QUALITY GATES**

### **📋 Pipeline de Quality Gates**

#### **Stage 1 - Fast Feedback:**
```yaml
# .github/workflows/quality-gate-fast.yml
name: Quality Gate - Fast Feedback

on:
  pull_request:
    branches: [develop]

jobs:
  fast-quality-check:
    runs-on: ubuntu-latest
    timeout-minutes: 10
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache Maven dependencies
        uses: actions/cache@v3
        with:
          path: ~/.m2
          key: ${{ runner.os }}-m2-${{ hashFiles('**/pom.xml') }}
      
      - name: Compile
        run: mvn compile -q
      
      - name: Unit Tests
        run: mvn test -q
      
      - name: Checkstyle
        run: mvn checkstyle:check -q
      
      - name: SpotBugs
        run: mvn spotbugs:check -q
```

#### **Stage 2 - Comprehensive Analysis:**
```yaml
# .github/workflows/quality-gate-comprehensive.yml
name: Quality Gate - Comprehensive

on:
  pull_request:
    branches: [main, develop]
  push:
    branches: [main, develop]

jobs:
  comprehensive-quality-check:
    runs-on: ubuntu-latest
    timeout-minutes: 30
    
    steps:
      - uses: actions/checkout@v3
        with:
          fetch-depth: 0  # Shallow clones should be disabled for better analysis
      
      - name: Setup Java
        uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      
      - name: Cache SonarCloud packages
        uses: actions/cache@v3
        with:
          path: ~/.sonar/cache
          key: ${{ runner.os }}-sonar
      
      - name: Full Test Suite
        run: mvn clean verify
      
      - name: Integration Tests
        run: mvn failsafe:integration-test failsafe:verify
      
      - name: Security Scan
        run: mvn org.owasp:dependency-check-maven:check
      
      - name: SonarCloud Analysis
        env:
          GITHUB_TOKEN: ${{ secrets.GITHUB_TOKEN }}
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
        run: mvn sonar:sonar
      
      - name: Quality Gate Check
        uses: sonarqube-quality-gate-action@master
        timeout-minutes: 5
        env:
          SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
```

### **🎯 Custom Quality Rules**

#### **ArchUnit Rules para Arquitetura:**
```java
@AnalyzeClasses(packages = "com.seguradora.hibrida")
public class ArchitectureTest {
    
    @ArchTest
    static final ArchRule commands_should_be_in_command_package =
        classes().that().haveSimpleNameEndingWith("Command")
            .should().resideInAPackage("..command..")
            .because("Commands devem estar no pacote command");
    
    @ArchTest
    static final ArchRule command_handlers_should_implement_interface =
        classes().that().haveSimpleNameEndingWith("CommandHandler")
            .should().implement(CommandHandler.class)
            .because("Command Handlers devem implementar CommandHandler interface");
    
    @ArchTest
    static final ArchRule aggregates_should_extend_aggregate_root =
        classes().that().haveSimpleNameEndingWith("Aggregate")
            .should().beAssignableTo(AggregateRoot.class)
            .because("Agregados devem estender AggregateRoot");
    
    @ArchTest
    static final ArchRule events_should_be_immutable =
        classes().that().haveSimpleNameEndingWith("Event")
            .should().haveOnlyFinalFields()
            .because("Eventos devem ser imutáveis");
    
    @ArchTest
    static final ArchRule no_cycles_in_packages =
        slices().matching("com.seguradora.hibrida.(*)..")
            .should().beFreeOfCycles()
            .because("Não deve haver dependências cíclicas entre pacotes");
    
    @ArchTest
    static final ArchRule command_side_should_not_depend_on_query_side =
        noClasses().that().resideInAPackage("..command..")
            .should().dependOnClassesThat().resideInAPackage("..query..")
            .because("Command side não deve depender do Query side");
    
    @ArchTest
    static final ArchRule controllers_should_only_use_command_bus_and_query_service =
        classes().that().resideInAPackage("..controller..")
            .should().onlyDependOnClassesThat()
            .resideInAnyPackage("..command..", "..query..", "java..", "org.springframework..", "..dto..")
            .because("Controllers devem usar apenas Command Bus e Query Services");
}
```

---

## 📝 **TEMPLATES E AUTOMAÇÃO**

### **🤖 PR Template**

#### **.github/pull_request_template.md:**
```markdown
## 📋 Descrição
<!-- Descreva brevemente as mudanças implementadas -->

## 🎯 Tipo de Mudança
- [ ] 🐛 Bug fix (correção que resolve um problema)
- [ ] ✨ Nova feature (funcionalidade que adiciona valor)
- [ ] 💥 Breaking change (mudança que quebra compatibilidade)
- [ ] 📚 Documentação (apenas mudanças na documentação)
- [ ] 🎨 Refatoração (mudança que não adiciona feature nem corrige bug)
- [ ] ⚡ Performance (mudança que melhora performance)
- [ ] 🧪 Testes (adição ou correção de testes)

## 🔄 Mudanças Implementadas
<!-- Liste as principais mudanças -->
- [ ] 
- [ ] 
- [ ] 

## 🧪 Como Testar
<!-- Instruções para testar as mudanças -->
```bash
# Comandos para testar
```

## 📊 Impacto
<!-- Descreva o impacto das mudanças -->
- **Performance**: <!-- Impacto na performance -->
- **Segurança**: <!-- Impacto na segurança -->
- **Compatibilidade**: <!-- Impacto na compatibilidade -->

## 📋 Checklist
- [ ] Código segue os padrões estabelecidos
- [ ] Self-review realizado
- [ ] Testes adicionados/atualizados
- [ ] Documentação atualizada
- [ ] Quality gates passando
- [ ] Sem breaking changes (ou documentado)

## 🔗 Issues Relacionadas
<!-- Links para issues, user stories, etc. -->
- Closes #
- Related to #
```

### **🔧 Automated Code Formatting**

#### **pre-commit configuration:**
```yaml
# .pre-commit-config.yaml
repos:
  - repo: https://github.com/pre-commit/pre-commit-hooks
    rev: v4.4.0
    hooks:
      - id: trailing-whitespace
      - id: end-of-file-fixer
      - id: check-yaml
      - id: check-added-large-files
      
  - repo: local
    hooks:
      - id: maven-test
        name: Maven Test
        entry: mvn test -q
        language: system
        pass_filenames: false
        
      - id: checkstyle
        name: Checkstyle
        entry: mvn checkstyle:check -q
        language: system
        pass_filenames: false
        
      - id: spotbugs
        name: SpotBugs
        entry: mvn spotbugs:check -q
        language: system
        pass_filenames: false
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Google Code Review Guidelines](https://google.github.io/eng-practices/review/)
- [SonarQube Quality Gates](https://docs.sonarqube.org/latest/user-guide/quality-gates/)
- [ArchUnit User Guide](https://www.archunit.org/userguide/html/000_Index.html)
- [OWASP Dependency Check](https://owasp.org/www-project-dependency-check/)

### **📖 Próximas Partes:**
- **Parte 4**: CI/CD e Automação
- **Parte 5**: Documentação e Knowledge Sharing

---

**📝 Parte 3 de 5 - Code Review e Qualidade**  
**⏱️ Tempo estimado**: 60 minutos  
**🎯 Próximo**: [Parte 4 - CI/CD e Automação](./12-praticas-desenvolvimento-parte-4.md)