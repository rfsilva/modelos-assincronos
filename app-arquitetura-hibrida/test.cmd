@echo off
mvnw test -Dtest=VeiculoAggregateTest 2>&1 | tail -50
