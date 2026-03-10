# Collections Postman - Arquitetura Híbrida

Este diretório contém as collections do Postman para testar todos os endpoints da aplicação de Arquitetura Híbrida (CQRS + Event Sourcing).

## 📁 Estrutura dos Arquivos

### Environment
- **`environment.json`** - Variáveis de ambiente compartilhadas entre todas as collections

### Collections por Domínio

#### 👤 Segurado
- **`segurado-commands.json`** - Endpoints de comandos (write) para o domínio Segurado
- **`segurado-queries.json`** - Endpoints de consultas (read) para o domínio Segurado

#### 🚗 Sinistro
- **`sinistro-queries.json`** - Endpoints de consultas (read) para o domínio Sinistro

#### 🏗️ Infraestrutura CQRS
- **`command-bus.json`** - Monitoramento do Command Bus
- **`projections.json`** - Gerenciamento e monitoramento de projeções
- **`event-store.json`** - Monitoramento do Event Store
- **`cqrs-monitoring.json`** - Monitoramento geral do CQRS

#### 🔍 Sistema
- **`health-check.json`** - Health checks e monitoramento geral do sistema

## 🚀 Como Usar

### 1. Importar no Postman

1. Abra o Postman
2. Clique em **Import**
3. Selecione **Folder** e escolha a pasta `postman`
4. Todas as collections e o environment serão importados automaticamente

### 2. Configurar Environment

1. No Postman, selecione o environment **"Arquitetura Híbrida - Environment"**
2. Verifique se a variável `baseUrl` está configurada corretamente:
   - **Desenvolvimento local**: `http://localhost:8080`
   - **Outros ambientes**: Ajuste conforme necessário

### 3. Executar Collections

#### Ordem Recomendada para Testes

1. **Health Check** - Verificar se o sistema está funcionando
2. **Segurado Commands** - Criar dados de teste
3. **Segurado Queries** - Verificar se os dados foram criados
4. **Command Bus** - Monitorar o funcionamento dos comandos
5. **Projections** - Verificar o estado das projeções
6. **Event Store** - Monitorar os eventos gerados
7. **CQRS Monitoring** - Visão geral do sistema

#### Executar Collection Completa

1. Clique com o botão direito na collection
2. Selecione **Run collection**
3. Configure os parâmetros desejados
4. Clique em **Run**

#### Executar Request Individual

1. Abra a collection desejada
2. Selecione o request
3. Clique em **Send**

## 🔧 Variáveis de Environment

| Variável | Valor Padrão | Descrição |
|----------|--------------|-----------|
| `baseUrl` | `http://localhost:8080` | URL base da aplicação |
| `apiVersion` | `v1` | Versão da API |
| `contentType` | `application/json` | Content-Type padrão |
| `seguradoId` | *(vazio)* | ID do segurado (preenchido automaticamente) |
| `veiculoId` | *(vazio)* | ID do veículo (preenchido automaticamente) |
| `sinistroId` | *(vazio)* | ID do sinistro (preenchido automaticamente) |
| `apoliceId` | *(vazio)* | ID da apólice (preenchido automaticamente) |
| `cpfTeste` | `12345678901` | CPF para testes |
| `emailTeste` | `teste@seguradora.com` | Email para testes |
| `placaTeste` | `ABC1234` | Placa para testes |
| `protocoloTeste` | `SIN-2024-001234` | Protocolo de sinistro para testes |

## 📋 Testes Automatizados

Cada collection inclui testes automatizados que verificam:

- **Status codes** corretos
- **Estrutura das respostas** JSON
- **Campos obrigatórios** presentes
- **Tipos de dados** corretos
- **Valores esperados** em campos específicos

### Visualizar Resultados dos Testes

Após executar uma collection:
1. Vá para a aba **Test Results**
2. Veja o resumo de testes passados/falhados
3. Clique em cada teste para ver detalhes

## 🔄 Fluxo de Dados Automático

As collections estão configuradas para:

1. **Capturar IDs** automaticamente após criação de recursos
2. **Reutilizar IDs** em requests subsequentes
3. **Validar dados** criados através de consultas
4. **Limpar variáveis** quando necessário

### Exemplo de Fluxo Automático

1. **Criar Segurado** → Captura `seguradoId`
2. **Buscar por ID** → Usa `seguradoId` capturado
3. **Atualizar Segurado** → Usa `seguradoId` capturado
4. **Verificar Atualização** → Confirma mudanças

## 🐛 Troubleshooting

### Problemas Comuns

#### 1. Erro de Conexão
- Verifique se a aplicação está rodando
- Confirme a URL no environment
- Teste o health check primeiro

#### 2. Testes Falhando
- Verifique se os dados de teste são válidos
- Confirme se as variáveis estão sendo capturadas
- Execute as collections na ordem recomendada

#### 3. IDs Não Capturados
- Verifique os scripts de **Test** nos requests
- Confirme se a resposta tem a estrutura esperada
- Execute requests individuais para debug

### Logs e Debug

Para debug detalhado:
1. Abra o **Console** do Postman (View → Show Postman Console)
2. Execute os requests
3. Veja logs detalhados de requests/responses

## 📚 Documentação Adicional

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **Actuator**: `http://localhost:8080/actuator`
- **Health Check**: `http://localhost:8080/actuator/health`

## 🤝 Contribuindo

Para adicionar novos endpoints:

1. Identifique o controller apropriado
2. Adicione o request na collection correspondente
3. Inclua testes automatizados
4. Atualize as variáveis de environment se necessário
5. Documente no README

## 📝 Notas Importantes

- **Dados de Teste**: Use sempre dados fictícios
- **Environment**: Não commite dados sensíveis no environment
- **Ordem**: Respeite a ordem de execução para evitar dependências quebradas
- **Cleanup**: Algumas collections podem precisar de limpeza manual dos dados

---

**Versão**: 1.0.0  
**Última Atualização**: 2024-12-19  
**Compatível com**: Postman 10.x+