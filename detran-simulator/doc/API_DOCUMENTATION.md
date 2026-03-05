# 📚 Documentação da API - Detran Simulator

## 🌐 Swagger/OpenAPI

O **Detran Simulator** agora inclui documentação completa da API usando **OpenAPI 3.0** (Swagger).

### 🔗 URLs da Documentação

Após iniciar o simulador (`mvn spring-boot:run`), acesse:

- **🎨 Swagger UI**: http://localhost:8080/detran-api/swagger-ui.html
- **📄 OpenAPI JSON**: http://localhost:8080/detran-api/api-docs
- **📋 OpenAPI YAML**: http://localhost:8080/detran-api/api-docs.yaml

## 📖 Funcionalidades da Documentação

### ✨ Interface Swagger UI
- **Try it out**: Teste todos os endpoints diretamente na interface
- **Exemplos completos**: Requests e responses de exemplo
- **Validação em tempo real**: Veja os formatos esperados
- **Filtros e busca**: Encontre endpoints rapidamente
- **Tempo de resposta**: Monitore performance dos testes

### 📊 Organização por Tags

#### 🚗 **Consultas de Veículos**
- `GET /veiculo` - Consultar dados do veículo
- `GET /veiculo/status` - Status do sistema
- `GET /veiculo/manutencao` - Simulação de manutenção

#### 📊 **Monitoramento**
- `GET /monitoring/dashboard` - Dashboard de estatísticas
- `GET /monitoring/consultas` - Histórico paginado
- `GET /monitoring/consultas/{placa}` - Consultas por placa
- `GET /monitoring/metrics/custom` - Métricas customizadas
- `DELETE /monitoring/consultas/cleanup` - Limpar logs

#### 🔧 **Sistema**
- Endpoints de controle e status do simulador

## 🎯 Exemplos de Uso

### 1. **Consulta Básica de Veículo**

**Request:**
```http
GET /detran-api/veiculo?placa=ABC1234&renavam=12345678901
User-Agent: Sistema-Teste/1.0
X-Forwarded-For: 192.168.1.100
```

**Response (200):**
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
  "carroceria": "HATCH",
  "especie": "PASSAGEIRO",
  "proprietario": "JOAO DA SILVA SANTOS",
  "municipio": "SAO PAULO",
  "situacao": "REGULAR",
  "data_aquisicao": "2020-03-15"
}
```

### 2. **Dashboard de Monitoramento**

**Request:**
```http
GET /detran-api/monitoring/dashboard
```

**Response (200):**
```json
{
  "total_consultas": 150,
  "total_veiculos_cadastrados": 14,
  "ultima_hora": {
    "total": 25,
    "sucessos": 21,
    "falhas": 2,
    "timeouts": 2,
    "dados_invalidos": 0
  },
  "tempo_medio_resposta_ms": 2150
}
```

### 3. **Validação de Entrada**

**Request com dados inválidos:**
```http
GET /detran-api/veiculo?placa=INVALID&renavam=123
```

**Response (400):**
```json
{
  "timestamp": "2024-03-03T19:30:00.000Z",
  "status": 400,
  "error": "Bad Request",
  "message": "Formato de placa inválido",
  "path": "/detran-api/veiculo"
}
```

## 🔄 Códigos de Status Simulados

### ✅ **200 - Success**
- Consulta realizada com sucesso
- Dados do veículo retornados
- ~85% das consultas (comportamento normal)

### ❌ **400 - Bad Request**
- Formato de placa inválido
- RENAVAM inválido
- Parâmetros obrigatórios ausentes
- Validação imediata (sem simulação)

### ⏱️ **408 - Request Timeout**
- Timeout simulado (~10% das consultas)
- Simula lentidão do sistema legado
- Tempo limite: 30 segundos

### 🔴 **503 - Service Unavailable**
- Indisponibilidade simulada (~15% das consultas)
- Simula instabilidade do sistema legado
- Endpoint `/manutencao` sempre retorna 503

### 💥 **500 - Internal Server Error**
- Erros internos não simulados
- Problemas reais do simulador
- Logs detalhados para debugging

## 📋 Modelos de Dados

### 🚗 **DetranResponse**
Modelo completo com todos os campos possíveis de um veículo:

```yaml
DetranResponse:
  type: object
  properties:
    placa:
      type: string
      pattern: "^[A-Z]{3}[0-9]{4}$|^[A-Z]{3}[0-9][A-Z][0-9]{2}$"
      example: "ABC1234"
    renavam:
      type: string
      pattern: "^[0-9]{11}$"
      example: "12345678901"
    marca_modelo:
      type: string
      example: "VOLKSWAGEN/GOL"
    situacao:
      type: string
      enum: ["REGULAR", "IRREGULAR", "BLOQUEADO", "APREENDIDO"]
      example: "REGULAR"
    # ... outros campos
```

### 📊 **ConsultaLog**
Modelo para logs de consultas:

```yaml
ConsultaLog:
  type: object
  properties:
    id:
      type: integer
      example: 1
    placa:
      type: string
      example: "ABC1234"
    status:
      type: string
      enum: ["SUCESSO", "DADOS_INVALIDOS", "TIMEOUT_SIMULADO", "INDISPONIBILIDADE_SIMULADA", "ERRO_INTERNO"]
    responseTimeMs:
      type: integer
      example: 1250
    consultaTimestamp:
      type: string
      format: date-time
      example: "2024-03-03T19:30:00"
```

## 🎨 Personalizando a Documentação

### Configurações no `application.yml`

```yaml
springdoc:
  api-docs:
    path: /api-docs
    enabled: true
  swagger-ui:
    path: /swagger-ui.html
    enabled: true
    operationsSorter: method
    tagsSorter: alpha
    tryItOutEnabled: true
    filter: true
    displayRequestDuration: true
    displayOperationId: true
    defaultModelsExpandDepth: 2
    defaultModelExpandDepth: 2
    docExpansion: none
```

### Customizações Disponíveis

- **Ordenação**: Por método HTTP ou alfabética
- **Filtros**: Busca por endpoints
- **Expansão**: Controle de abertura de seções
- **Try it out**: Habilitar/desabilitar testes
- **Duração**: Mostrar tempo de resposta
- **Modelos**: Profundidade de expansão

## 🔧 Integração com Ferramentas

### 📊 **Postman**
1. Acesse: http://localhost:8080/detran-api/api-docs
2. Copie o JSON
3. No Postman: **Import** → **Raw text** → Cole o JSON
4. Collection será criada automaticamente

### 🐙 **Insomnia**
1. Acesse: http://localhost:8080/detran-api/api-docs
2. **Import/Export** → **Import Data** → **From URL**
3. Cole a URL do OpenAPI JSON

### 🔄 **Geração de Clientes**
Use ferramentas como **OpenAPI Generator** para gerar clientes:

```bash
# Gerar cliente Java
openapi-generator-cli generate \
  -i http://localhost:8080/detran-api/api-docs \
  -g java \
  -o ./detran-client-java

# Gerar cliente Python
openapi-generator-cli generate \
  -i http://localhost:8080/detran-api/api-docs \
  -g python \
  -o ./detran-client-python
```

## 🎯 Casos de Uso da Documentação

### 👨‍💻 **Para Desenvolvedores**
- **Explorar a API** sem precisar ler código
- **Testar endpoints** diretamente no browser
- **Validar requests** antes de implementar
- **Entender modelos** de dados complexos

### 🧪 **Para Testes**
- **Gerar casos de teste** baseados nos exemplos
- **Validar contratos** de API
- **Automatizar testes** usando especificação
- **Documentar cenários** de teste

### 📚 **Para Documentação**
- **Manter docs atualizadas** automaticamente
- **Compartilhar especificação** com equipes
- **Integrar com wikis** e portais
- **Gerar documentação** em outros formatos

### 🔄 **Para Integração**
- **Entender comportamentos** simulados
- **Implementar circuit breakers** adequados
- **Configurar timeouts** corretos
- **Tratar erros** apropriadamente

## 🚀 Próximos Passos

1. **Inicie o simulador**: `mvn spring-boot:run`
2. **Acesse o Swagger**: http://localhost:8080/detran-api/swagger-ui.html
3. **Explore os endpoints** usando "Try it out"
4. **Teste diferentes cenários** de instabilidade
5. **Monitore resultados** no dashboard
6. **Integre com suas aplicações** usando a especificação

---

**🎉 Agora você tem documentação completa e interativa da API do Detran Simulator!**