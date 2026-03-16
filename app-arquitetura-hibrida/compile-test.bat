@echo off
cd /d "C:\develop\rfsilva\modelos-assincronos\app-arquitetura-hibrida"
call mvnw.cmd compiler:testCompile 2>&1 | findstr /i "error"
