# 🚀 Quick Start - Detran Simulator

## ⚡ Início Rápido

### 1. Executar a Aplicação
```bash
cd detran-simulator
mvn spring-boot:run
```

### 2. Aguardar Inicialização
Aguarde até ver a mensagem:
```
╔══════════════════════════════════════════════════════════════╗
║                    DETRAN SIMULATOR                          ║
║  🚗 Simulador do Sistema Legado do Detran                   ║
╚══════════════════════════════════════════════════════════════╝
```

### 3. Testar Rapidamente

#### ✅ Status da Aplicação
```bash
curl "http://localhost:8080/detran-api/veiculo/status"
```

#### ✅ Consulta de Sucesso (dados pré-cadastrados)
```bash
curl "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=12345678901"
```

#### ❌ Consulta com Dados Inválidos
```bash
curl "http://localhost:8080/detran-api/veiculo?placa=INVALID&renavam=123"
```

#### 📊 Dashboard de Monitoramento
```bash
curl "http://localhost:8080/detran-api/monitoring/dashboard"
```

## 🎯 Endpoints Principais

| Endpoint | Descrição | Exemplo |
|----------|-----------|---------|
| `GET /veiculo` | Consulta principal | `?placa=ABC1234&renavam=12345678901` |
| `GET /veiculo/status` | Status do sistema | - |
| `GET /monitoring/dashboard` | Estatísticas | - |
| `GET /monitoring/consultas` | Histórico | `?page=0&size=20` |

## 🗄️ Acessar H2 Console

1. Abrir: http://localhost:8080/detran-api/h2-console
2. **JDBC URL**: `jdbc:h2:mem:detran_db`
3. **Username**: `sa`
4. **Password**: `password`

## 🧪 Dados de Teste Garantidos

```bash
# Sempre funcionam (pré-cadastrados)
ABC1234 / 12345678901 - VW Gol 2020
DEF5678 / 23456789012 - Chevrolet Onix 2019
GHI9012 / 34567890123 - Fiat Uno 2021 (Irregular)
JKL3456 / 45678901234 - Ford Ka 2018
MNO7890 / 56789012345 - Hyundai HB20 2022 (Bloqueado)
```

## 🔧 Configurar Comportamento

### Desabilitar Instabilidades (para testes determinísticos)
Editar `application.yml`:
```yaml
detran.simulator.instability.enabled: false
```

### Alterar Taxa de Falhas
```yaml
detran.simulator.instability.failure-rate: 0.30  # 30% de falhas
```

### Alterar Performance
```yaml
detran.simulator.performance.min-response-time: 100   # 100ms mínimo
detran.simulator.performance.max-response-time: 2000  # 2s máximo
```

## 🚨 Troubleshooting

### Aplicação não inicia
- ✅ Verificar Java 21+ instalado: `java -version`
- ✅ Verificar porta 8080 livre: `netstat -an | findstr 8080`
- ✅ Verificar logs de erro no console

### Sempre retorna 503
- ✅ Não acessar `/manutencao` (sempre retorna 503)
- ✅ Verificar `instability.enabled: true` no config
- ✅ Tentar múltiplas vezes (15% de falhas é normal)

### H2 Console não abre
- ✅ Verificar URL: http://localhost:8080/detran-api/h2-console
- ✅ Usar credenciais: `sa` / `password`
- ✅ JDBC URL: `jdbc:h2:mem:detran_db`

## 📈 Monitoramento

### Métricas Prometheus
http://localhost:8080/detran-api/actuator/prometheus

### Health Check
http://localhost:8080/detran-api/actuator/health

### Logs da Aplicação
Os logs mostram o comportamento em tempo real:
```
🔍 Iniciando consulta - Placa: ABC1234
🐌 Simulando resposta lenta do Detran
✅ Consulta realizada com sucesso
```

## 🎯 Próximos Passos

1. **Testar as 3 arquiteturas** propostas contra este simulador
2. **Configurar métricas** para análise de performance
3. **Simular cenários** de alta carga
4. **Validar estratégias** de retry e circuit breaker

---

**🚗 Simulador pronto para uso! Bons testes!** 🎉