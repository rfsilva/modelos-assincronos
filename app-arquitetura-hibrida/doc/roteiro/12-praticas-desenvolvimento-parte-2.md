# 💻 PRÁTICAS DE DESENVOLVIMENTO - PARTE 2
## Git Workflow e Versionamento

### 🎯 **OBJETIVOS DESTA PARTE**
- Estabelecer workflow Git padronizado
- Implementar estratégias de branching
- Configurar conventional commits
- Definir políticas de merge e release

---

## 🌿 **ESTRATÉGIA DE BRANCHING**

### **📋 Git Flow Adaptado para Arquitetura Híbrida**

Utilizamos uma versão adaptada do Git Flow que considera as especificidades da arquitetura:

```mermaid
gitgraph
    commit id: "Initial"
    branch develop
    checkout develop
    commit id: "Setup"
    
    branch feature/US001-criar-sinistro
    checkout feature/US001-criar-sinistro
    commit id: "Command"
    commit id: "Handler"
    commit id: "Tests"
    
    checkout develop
    merge feature/US001-criar-sinistro
    
    branch feature/US002-consultar-sinistro
    checkout feature/US002-consultar-sinistro
    commit id: "Query"
    commit id: "Projection"
    
    checkout develop
    merge feature/US002-consultar-sinistro
    
    branch release/v1.0.0
    checkout release/v1.0.0
    commit id: "Prepare"
    
    checkout main
    merge release/v1.0.0
    tag: "v1.0.0"
    
    checkout develop
    merge release/v1.0.0
```

### **🏗️ Estrutura de Branches**

#### **Branches Principais:**
```bash
# Branch principal - código em produção
main/
├── Sempre estável
├── Apenas merges de release branches
├── Cada commit é uma versão
└── Protegida contra push direto

# Branch de desenvolvimento - integração contínua
develop/
├── Código em desenvolvimento
├── Base para feature branches
├── Integração de features
└── Testes automatizados
```

#### **Branches de Suporte:**
```bash
# Features - novas funcionalidades
feature/{tipo}-{numero}-{descricao}/
├── feature/US001-criar-sinistro-command
├── feature/US002-consultar-sinistro-query
├── feature/TECH-001-monitoring-setup
└── feature/BUG-001-fix-projection-lag

# Releases - preparação para produção
release/{versao}/
├── release/v1.0.0
├── release/v1.1.0
└── Apenas bug fixes e ajustes

# Hotfixes - correções urgentes em produção
hotfix/{versao}/
├── hotfix/v1.0.1
└── hotfix/v1.0.2
```

---

## 📝 **CONVENTIONAL COMMITS**

### **🎯 Padrão de Commit Messages**

Utilizamos o padrão Conventional Commits adaptado para nossa arquitetura:

#### **Formato Base:**
```
<type>[optional scope]: <description>

[optional body]

[optional footer(s)]
```

#### **Types Específicos:**
```bash
# Command Side
feat(command): adicionar CriarSinistroCommand
fix(command): corrigir validação de CPF no comando
refactor(command): extrair validações para classe separada

# Query Side  
feat(query): implementar consulta por CPF
fix(query): corrigir paginação na consulta de sinistros
perf(query): otimizar índices da projeção

# Event Sourcing
feat(event): adicionar SinistroCriadoEvent
fix(event): corrigir serialização de eventos
refactor(event): padronizar estrutura de eventos

# CQRS
feat(cqrs): implementar projection handler
fix(cqrs): corrigir lag entre command e query
perf(cqrs): otimizar processamento de projeções

# Infrastructure
feat(infra): adicionar monitoramento com Prometheus
fix(infra): corrigir configuração do banco de dados
ci(infra): adicionar pipeline de deploy

# Documentation
docs(readme): atualizar guia de instalação
docs(api): adicionar documentação dos endpoints
docs(arch): documentar padrões de Event Sourcing
```

#### **Exemplos Práticos:**
```bash
# Feature completa
feat(command): implementar criação de sinistro

Adiciona comando e handler para criação de sinistros:
- CriarSinistroCommand com validações
- CriarSinistroCommandHandler com lógica de negócio
- Testes unitários e de integração
- Documentação da API

Closes #US001

# Bug fix
fix(projection): corrigir idempotência no SinistroProjectionHandler

O handler estava processando eventos duplicados devido à
falta de verificação do eventId. Adicionada validação
para garantir idempotência.

Fixes #BUG-123

# Breaking change
feat(event)!: alterar estrutura do SinistroCriadoEvent

BREAKING CHANGE: O campo 'valor' foi renomeado para 'valorEstimado'
para melhor clareza. Migrations necessárias para dados existentes.

Closes #TECH-456
```

### **🔧 Configuração de Git Hooks**

#### **pre-commit hook:**
```bash
#!/bin/sh
# .git/hooks/pre-commit

echo "🔍 Executando verificações pré-commit..."

# Verificar formato do commit message
commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "❌ Formato de commit inválido!"
    echo "Use: type(scope): description"
    echo "Exemplo: feat(command): adicionar CriarSinistroCommand"
    exit 1
fi

# Executar testes unitários
echo "🧪 Executando testes unitários..."
mvn test -q
if [ $? -ne 0 ]; then
    echo "❌ Testes falharam!"
    exit 1
fi

# Verificar qualidade do código
echo "🔍 Verificando qualidade do código..."
mvn checkstyle:check -q
if [ $? -ne 0 ]; then
    echo "❌ Problemas de estilo encontrados!"
    exit 1
fi

echo "✅ Todas as verificações passaram!"
```

#### **commit-msg hook:**
```bash
#!/bin/sh
# .git/hooks/commit-msg

commit_regex='^(feat|fix|docs|style|refactor|perf|test|chore|ci|build)(\(.+\))?: .{1,50}'

if ! grep -qE "$commit_regex" "$1"; then
    echo "❌ Formato de commit inválido!"
    echo ""
    echo "Formato correto:"
    echo "  type(scope): description"
    echo ""
    echo "Types válidos:"
    echo "  feat     - nova funcionalidade"
    echo "  fix      - correção de bug"
    echo "  docs     - documentação"
    echo "  style    - formatação"
    echo "  refactor - refatoração"
    echo "  perf     - melhoria de performance"
    echo "  test     - testes"
    echo "  chore    - tarefas de manutenção"
    echo "  ci       - integração contínua"
    echo "  build    - sistema de build"
    echo ""
    echo "Scopes sugeridos:"
    echo "  command, query, event, cqrs, infra, api, test"
    echo ""
    echo "Exemplo:"
    echo "  feat(command): adicionar CriarSinistroCommand"
    exit 1
fi
```

---

## 🔄 **WORKFLOW DE DESENVOLVIMENTO**

### **📋 Processo de Feature Development**

#### **1. Criação de Feature Branch:**
```bash
# Atualizar develop
git checkout develop
git pull origin develop

# Criar feature branch
git checkout -b feature/US001-criar-sinistro-command

# Configurar upstream
git push -u origin feature/US001-criar-sinistro-command
```

#### **2. Desenvolvimento Iterativo:**
```bash
# Commits frequentes e atômicos
git add .
git commit -m "feat(command): adicionar estrutura básica do CriarSinistroCommand"

git add .
git commit -m "feat(command): implementar validações no CriarSinistroCommand"

git add .
git commit -m "feat(command): adicionar CriarSinistroCommandHandler"

git add .
git commit -m "test(command): adicionar testes para CriarSinistroCommand"

# Push regular
git push origin feature/US001-criar-sinistro-command
```

#### **3. Sincronização com Develop:**
```bash
# Rebase regular para manter histórico limpo
git checkout develop
git pull origin develop
git checkout feature/US001-criar-sinistro-command
git rebase develop

# Resolver conflitos se necessário
git add .
git rebase --continue

# Force push após rebase
git push --force-with-lease origin feature/US001-criar-sinistro-command
```

#### **4. Pull Request:**
```markdown
## 🎯 Descrição
Implementa comando e handler para criação de sinistros conforme US001.

## 🔄 Mudanças
- [x] CriarSinistroCommand com validações
- [x] CriarSinistroCommandHandler com lógica de negócio  
- [x] Testes unitários (cobertura > 90%)
- [x] Testes de integração
- [x] Documentação da API

## 🧪 Como Testar
```bash
# Executar testes
mvn test -Dtest=CriarSinistroCommandTest

# Testar via API
curl -X POST http://localhost:8080/api/sinistros \
  -H "Content-Type: application/json" \
  -d '{"protocolo":"SIN-2024-001","cpfSegurado":"12345678901",...}'
```

## 📋 Checklist
- [x] Código segue padrões estabelecidos
- [x] Testes passando (unit + integration)
- [x] Documentação atualizada
- [x] Sem breaking changes
- [x] Performance verificada

## 🔗 Links
- Closes #US001
- Related to #ARCH-001
```

---

## 🏷️ **ESTRATÉGIA DE VERSIONAMENTO**

### **📊 Semantic Versioning**

Utilizamos SemVer adaptado para arquitetura de microsserviços:

#### **Formato: MAJOR.MINOR.PATCH**
```
v1.2.3
│ │ │
│ │ └── PATCH: Bug fixes, hotfixes
│ └──── MINOR: Novas features, melhorias
└────── MAJOR: Breaking changes, mudanças de arquitetura
```

#### **Exemplos de Versionamento:**
```bash
# v1.0.0 - Release inicial
- Implementação básica de CQRS
- Commands: CriarSinistro, AtualizarSinistro
- Queries: BuscarSinistro, ListarSinistros
- Event Store funcional
- Projeções básicas

# v1.1.0 - Novas features
- feat(command): adicionar CancelarSinistroCommand
- feat(query): implementar busca por CPF
- feat(monitoring): adicionar métricas de negócio
- perf(projection): otimizar processamento

# v1.1.1 - Bug fixes
- fix(projection): corrigir idempotência
- fix(command): validação de CPF
- fix(query): paginação incorreta

# v2.0.0 - Breaking changes
- BREAKING: alterar estrutura de eventos
- BREAKING: nova API de consultas
- feat: implementar snapshots
- feat: adicionar multi-tenancy
```

### **🔖 Tagging Strategy**

#### **Criação de Tags:**
```bash
# Tag de release
git checkout main
git tag -a v1.0.0 -m "Release v1.0.0

Features:
- Implementação completa de CQRS
- Commands e Queries básicos
- Event Store com PostgreSQL
- Monitoramento com Prometheus

Breaking Changes:
- Nenhuma (primeira release)

Migration Guide:
- Primeira instalação, seguir README.md"

git push origin v1.0.0

# Tag de pre-release
git tag -a v1.1.0-rc.1 -m "Release Candidate v1.1.0-rc.1"
git push origin v1.1.0-rc.1

# Tag de hotfix
git tag -a v1.0.1 -m "Hotfix v1.0.1 - Correção crítica na projeção"
git push origin v1.0.1
```

#### **Changelog Automático:**
```bash
# Gerar changelog baseado em commits
git log v1.0.0..HEAD --pretty=format:"- %s" --grep="feat\|fix\|BREAKING"

# Exemplo de saída:
- feat(command): adicionar CancelarSinistroCommand
- fix(projection): corrigir processamento de eventos
- feat(query): implementar busca avançada
- BREAKING: alterar estrutura do SinistroCriadoEvent
```

---

## 🔒 **POLÍTICAS DE BRANCH PROTECTION**

### **📋 Configuração do GitHub/GitLab**

#### **Branch Protection Rules:**
```yaml
# .github/branch-protection.yml
protection_rules:
  main:
    required_status_checks:
      strict: true
      contexts:
        - "ci/tests"
        - "ci/quality-gate"
        - "ci/security-scan"
    enforce_admins: true
    required_pull_request_reviews:
      required_approving_review_count: 2
      dismiss_stale_reviews: true
      require_code_owner_reviews: true
    restrictions:
      users: []
      teams: ["architecture-team", "senior-developers"]
    
  develop:
    required_status_checks:
      strict: true
      contexts:
        - "ci/tests"
        - "ci/quality-gate"
    required_pull_request_reviews:
      required_approving_review_count: 1
      dismiss_stale_reviews: true
```

#### **CODEOWNERS:**
```bash
# .github/CODEOWNERS

# Global owners
* @architecture-team

# Command side
/src/main/java/com/seguradora/hibrida/command/ @senior-developers @architecture-team

# Query side  
/src/main/java/com/seguradora/hibrida/query/ @senior-developers

# Event sourcing core
/src/main/java/com/seguradora/hibrida/eventstore/ @architecture-team
/src/main/java/com/seguradora/hibrida/aggregate/ @architecture-team

# Infrastructure
/src/main/java/com/seguradora/hibrida/config/ @devops-team @architecture-team
/docker-compose*.yml @devops-team
/.github/ @devops-team

# Documentation
/doc/ @architecture-team @tech-writers
README.md @architecture-team
```

---

## 🚀 **RELEASE PROCESS**

### **📋 Processo de Release**

#### **1. Preparação da Release:**
```bash
# Criar release branch
git checkout develop
git pull origin develop
git checkout -b release/v1.1.0

# Atualizar versão
mvn versions:set -DnewVersion=1.1.0
git add pom.xml
git commit -m "chore(release): bump version to 1.1.0"

# Executar testes completos
mvn clean verify
```

#### **2. Finalização da Release:**
```bash
# Merge para main
git checkout main
git pull origin main
git merge --no-ff release/v1.1.0
git tag -a v1.1.0 -m "Release v1.1.0"

# Merge de volta para develop
git checkout develop
git merge --no-ff release/v1.1.0

# Push tudo
git push origin main
git push origin develop
git push origin v1.1.0

# Limpar release branch
git branch -d release/v1.1.0
git push origin --delete release/v1.1.0
```

#### **3. Deploy Automático:**
```yaml
# .github/workflows/release.yml
name: Release Deploy

on:
  push:
    tags:
      - 'v*'

jobs:
  deploy:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Deploy to Production
        run: |
          echo "Deploying version ${{ github.ref_name }}"
          # Deploy logic here
          
      - name: Create GitHub Release
        uses: actions/create-release@v1
        with:
          tag_name: ${{ github.ref }}
          release_name: Release ${{ github.ref }}
          body: |
            ## Changes in this Release
            ${{ steps.changelog.outputs.changelog }}
          draft: false
          prerelease: false
```

---

## 📚 **RECURSOS DE REFERÊNCIA**

### **🔗 Links Úteis:**
- [Git Flow](https://nvie.com/posts/a-successful-git-branching-model/)
- [Conventional Commits](https://www.conventionalcommits.org/)
- [Semantic Versioning](https://semver.org/)
- [GitHub Flow](https://guides.github.com/introduction/flow/)

### **📖 Próximas Partes:**
- **Parte 3**: Code Review e Qualidade
- **Parte 4**: CI/CD e Automação
- **Parte 5**: Documentação e Knowledge Sharing

---

**📝 Parte 2 de 5 - Git Workflow e Versionamento**  
**⏱️ Tempo estimado**: 50 minutos  
**🎯 Próximo**: [Parte 3 - Code Review e Qualidade](./12-praticas-desenvolvimento-parte-3.md)