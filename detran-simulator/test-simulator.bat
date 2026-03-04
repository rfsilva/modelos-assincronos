@echo off
echo ========================================
echo    TESTANDO DETRAN SIMULATOR
echo ========================================
echo.

echo 1. Testando endpoint de status...
curl -s "http://localhost:8080/detran-api/veiculo/status"
echo.
echo.

echo 2. Testando consulta com dados validos (pre-cadastrados)...
curl -s "http://localhost:8080/detran-api/veiculo?placa=ABC1234&renavam=12345678901"
echo.
echo.

echo 3. Testando consulta com dados invalidos...
curl -s "http://localhost:8080/detran-api/veiculo?placa=INVALID&renavam=123"
echo.
echo.

echo 4. Testando dashboard de monitoramento...
curl -s "http://localhost:8080/detran-api/monitoring/dashboard"
echo.
echo.

echo 5. Testando multiplas consultas (simulando instabilidades)...
for /L %%i in (1,1,5) do (
    echo Consulta %%i:
    curl -w "Status: %%{http_code} Time: %%{time_total}s" -s "http://localhost:8080/detran-api/veiculo?placa=TST%%i234&renavam=1234567890%%i"
    echo.
)

echo.
echo ========================================
echo    TESTES CONCLUIDOS
echo ========================================