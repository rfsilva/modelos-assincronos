#!/bin/bash

echo "========================================"
echo "    TESTANDO DETRAN SIMULATOR"
echo "========================================"
echo

echo "1. Testando endpoint de status..."
curl -s "http://localhost:8080/detran-api/veiculo/status" | jq .
echo
echo

echo "2. Testando consulta com dados válidos (pré-cadastrados)..."
curl -s "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=12345678901" | jq .
echo
echo

echo "3. Testando consulta com dados inválidos..."
curl -s "http://localhost:8080/detran-api/veiculo?placa=INVALID&renavam=123"
echo
echo

echo "4. Testando dashboard de monitoramento..."
curl -s "http://localhost:8080/detran-api/monitoring/dashboard" | jq .
echo
echo

echo "5. Testando múltiplas consultas (simulando instabilidades)..."
for i in {1..5}; do
    echo "Consulta $i:"
    curl -w "Status: %{http_code} Time: %{time_total}s\n" -s "http://localhost:8080/detran-api/veiculo?placa=TST${i}234&renavam=1234567890${i}"
    echo
done

echo
echo "========================================"
echo "    TESTES CONCLUÍDOS"
echo "========================================"