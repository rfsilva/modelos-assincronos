# 🚗 Detran Simulator

Simulador do sistema legado do Detran para testes de integração do sistema de gestão de sinistros.

## 📋 Características Simuladas

### 🔴 Instabilidades
- **15% de falhas** (indisponibilidade do sistema)
- **10% de timeouts** (requisições que excedem 30s)
- **25% de respostas lentas** (>5s de resposta)

### ⚡ Performance
- **Tempo mínimo**: 500ms
- **Tempo máximo**: 8s
- **Respostas lentas**: >5s
- **Timeout**: 30s

### 📊 Dados
- **10% de dados inválidos** (placa/renavam incorretos)
- **90% de dados válidos** com resposta completa
- **Cache simulado** (30% de chance de hit)
- **Dados consistentes** baseados em placa+renavam

## 🚀 Como Executar

### Pré-requisitos
- Java 21+
- Maven 3.8+

### Executar Aplicação
```bash
cd detran-simulator
mvn spring-boot:run
```

### Acessar Aplicação
- **🎨 Swagger UI**: http://localhost:8080/detran-api/swagger-ui.html
- **📄 OpenAPI Docs**: http://localhost:8080/detran-api/api-docs
- **🌐 API Principal**: http://localhost:8080/detran-api
- **🗄️ H2 Console**: http://localhost:8080/detran-api/h2-console
- **📊 Actuator**: http://localhost:8080/detran-api/actuator
- **📈 Métricas**: http://localhost:8080/detran-api/actuator/prometheus

## 📚 Documentação da API

### 🎨 Swagger UI Interativo
Acesse **http://localhost:8080/detran-api/swagger-ui.html** para:
- **🧪 Testar endpoints** diretamente no browser
- **📖 Ver documentação** completa com exemplos
- **🔍 Explorar modelos** de dados
- **⏱️ Monitorar tempos** de resposta
- **🎯 Entender comportamentos** simulados

### 📄 Especificação OpenAPI
- **JSON**: http://localhost:8080/detran-api/api-docs
- **YAML**: http://localhost:8080/detran-api/api-docs.yaml

### 📮 Collections Postman
Na pasta `postman/` você encontra:
- **Collection completa** com todos os cenários
- **Environment configurado** com variáveis
- **Scripts avançados** para análise
- **Documentação detalhada** de uso

## 📡 Endpoints Principais

### 🔍 Consulta de Veículo
```http
GET /detran-api/veiculo?placa={placa}&renavam={renavam}
```

**Exemplo de Sucesso:**
```bash
curl "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=12345678901"
```

**Resposta (200 OK):**
```json
{
  "placa": "ABC1234",
  "renavam": "12345678901",
  "ano_fabricacao": "2020",
  "ano_modelo": "2020",
  "marca_modelo": "VOLKSWAGEN/GOL",
  "cor": "BRANCO",
  "combustivel": "FLEX",
  "categoria": "PARTICULAR",
  "proprietario": "JOAO DA SILVA SANTOS",
  "situacao": "REGULAR",
  "debitos": [...],
  "multas": [...],
  "infracoes": [...]
}
```

**Possíveis Respostas de Erro:**
- `400 Bad Request` - Dados inválidos
- `408 Request Timeout` - Timeout simulado
- `503 Service Unavailable` - Indisponibilidade simulada

### 📊 Status do Sistema
```http
GET /detran-api/veiculo/status
```

### 🔧 Manutenção (sempre retorna 503)
```http
GET /detran-api/veiculo/manutencao
```

## 📈 Monitoramento

### Dashboard de Estatísticas
```http
GET /detran-api/monitoring/dashboard
```

**Resposta:**
```json
{
  "total_consultas": 1250,
  "total_veiculos_cadastrados": 14,
  "ultima_hora": {
    "total": 45,
    "sucessos": 32,
    "falhas": 7,
    "timeouts": 4,
    "dados_invalidos": 2
  },
  "tempo_medio_resposta_ms": 2150
}
```

### Histórico de Consultas
```http
GET /detran-api/monitoring/consultas?page=0&size=20
```

### Consultas por Placa
```http
GET /detran-api/monitoring/consultas/{placa}
```

### Métricas Customizadas
```http
GET /detran-api/monitoring/metrics/custom
```

## 🗄️ Banco de Dados H2

### Acesso ao Console
1. Acesse: http://localhost:8080/detran-api/h2-console
2. **JDBC URL**: `jdbc:h2:mem:detran_db`
3. **Username**: `sa`
4. **Password**: `password`

### Tabelas Principais
- `veiculos` - Dados dos veículos cadastrados
- `consulta_logs` - Log de todas as consultas realizadas

## 🧪 Dados de Teste

### Veículos Pré-cadastrados
```
ABC1234 / 12345678901 - VW Gol 2020 (Regular)
DEF5678 / 23456789012 - Chevrolet Onix 2019 (Regular)  
GHI9012 / 34567890123 - Fiat Uno 2021 (Irregular)
JKL3456 / 45678901234 - Ford Ka 2018 (Regular)
MNO7890 / 56789012345 - Hyundai HB20 2022 (Bloqueado)
```

### Placas Mercosul
```
BRA2E19 / 11234567890 - VW T-Cross 2022
BRB3F20 / 21234567890 - Chevrolet Tracker 2023
```

### Casos de Teste Especiais
```
TST0001 / 99999999999 - Veículo Apreendido
TST0002 / 88888888888 - Veículo Antigo Irregular
```

## ⚙️ Configurações

### Arquivo `application.yml`
```yaml
detran:
  simulator:
    instability:
      enabled: true
      failure-rate: 0.15      # 15% falhas
      timeout-rate: 0.10      # 10% timeouts
      slow-response-rate: 0.25 # 25% respostas lentas
    
    performance:
      min-response-time: 500   # 500ms mínimo
      max-response-time: 8000  # 8s máximo
      slow-response-time: 5000 # 5s para "lento"
    
    data:
      invalid-data-rate: 0.10  # 10% dados inválidos
      cache-simulation: true

# Configurações do Swagger
springdoc:
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    tryItOutEnabled: true
    filter: true
    displayRequestDuration: true
```

## 🔧 Personalização

### Alterar Taxa de Falhas
Edite `application.yml` e reinicie a aplicação:
```yaml
detran.simulator.instability.failure-rate: 0.30  # 30% de falhas
```

### Desabilitar Instabilidades
```yaml
detran.simulator.instability.enabled: false
```

### Alterar Performance
```yaml
detran.simulator.performance.min-response-time: 1000  # 1s mínimo
detran.simulator.performance.max-response-time: 15000 # 15s máximo
```

### Desabilitar Swagger (Produção)
```yaml
springdoc.swagger-ui.enabled: false
springdoc.api-docs.enabled: false
```

## 📊 Métricas Prometheus

O simulador expõe métricas customizadas para monitoramento:

```
# Taxa de sucesso das consultas
detran_success_rate

# Total de requisições na última hora
detran_total_requests_last_hour

# Requisições bem-sucedidas na última hora
detran_successful_requests_last_hour

# Requisições com falha na última hora
detran_failed_requests_last_hour

# Requisições com timeout na última hora
detran_timeout_requests_last_hour
```

## 🧪 Cenários de Teste

### 1. Teste Interativo (Swagger UI)
1. Acesse: http://localhost:8080/detran-api/swagger-ui.html
2. Expanda **"🚗 Consultas de Veículos"**
3. Clique em **"🔍 Consultar dados do veículo"**
4. Clique em **"Try it out"**
5. Preencha: `placa=ABC1234` e `renavam=12345678901`
6. Clique em **"Execute"**

### 2. Teste de Sucesso (cURL)
```bash
# Usar dados pré-cadastrados
curl "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=12345678901"
```

### 3. Teste de Dados Inválidos
```bash
# Placa inválida
curl "http://localhost:8080/detran-api/veiculo?placa=INVALID&renavam=12345678901"

# RENAVAM inválido
curl "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=123"
```

### 4. Teste de Instabilidades
```bash
# Fazer múltiplas requisições para simular diferentes comportamentos
for i in {1..20}; do
  curl -w "Status: %{http_code} Time: %{time_total}s\n" \
       "http://localhost:8080/detran-api/veiculo?placa=XYZ${i}234&renavam=1234567890${i}"
done
```

### 5. Teste com Postman
1. Importe os arquivos da pasta `postman/`
2. Selecione o environment "Detran Simulator Environment - Completo"
3. Execute a collection completa ou requests individuais
4. Monitore resultados no dashboard

## 🐛 Troubleshooting

### Aplicação não inicia
- Verificar se Java 21+ está instalado
- Verificar se porta 8080 está disponível
- Verificar logs de inicialização

### Swagger UI não abre
- Verificar se aplicação está rodando
- Acessar: http://localhost:8080/detran-api/swagger-ui.html
- Verificar se `springdoc.swagger-ui.enabled: true`

### H2 Console não abre
- Verificar se aplicação está rodando
- Acessar: http://localhost:8080/detran-api/h2-console
- Usar credenciais: sa/password

### Sempre retorna erro 503
- Verificar se não está acessando `/manutencao`
- Verificar configuração `instability.enabled`
- Verificar logs da aplicação

## 📝 Logs

Os logs mostram o comportamento simulado:
```
🔍 Iniciando consulta - Placa: ABC1234 Renavam: 12345678901
🐌 Simulando resposta lenta do Detran
✅ Consulta realizada com sucesso - Placa: ABC1234 Comportamento: SLOW_RESPONSE
```

Símbolos nos logs:
- 🔍 Consulta iniciada
- ✅ Sucesso
- ❌ Dados inválidos  
- 🔴 Indisponibilidade
- ⏱️ Timeout
- 🐌 Resposta lenta
- 💾 Cache utilizado

## 🎯 Próximos Passos

1. **🚀 Inicie o simulador**: `mvn spring-boot:run`
2. **🎨 Explore o Swagger**: http://localhost:8080/detran-api/swagger-ui.html
3. **📮 Teste com Postman**: Importe as collections da pasta `postman/`
4. **📊 Monitore métricas**: Use o dashboard de monitoramento
5. **🔧 Integre com sua aplicação**: Use a especificação OpenAPI

---

**🎉 Simulador completo com documentação interativa e ferramentas de teste prontas para uso!**