@echo off
cd /d "C:\develop\rfsilva\modelos-assincronos\app-arquitetura-hibrida"
call mvnw.cmd -q test -Dtest=VeiculoAggregateTest 2>&1
