-- =====================================================
-- Script de Inicialização: Extensões Básicas
-- =====================================================
-- Autor: Principal Java Architect
-- Data: 2024-12-19
-- Descrição: Instala apenas extensões básicas necessárias
--            O Flyway será responsável pela estrutura completa
-- =====================================================

-- Criar extensões necessárias para ambos os bancos
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";
CREATE EXTENSION IF NOT EXISTS "pg_stat_statements";
CREATE EXTENSION IF NOT EXISTS "pg_trgm";
CREATE EXTENSION IF NOT EXISTS "btree_gin";

-- Log de inicialização
DO $$
BEGIN
    RAISE NOTICE 'Extensões básicas instaladas em % às %', current_database(), NOW();
    RAISE NOTICE 'Flyway será responsável pela estrutura completa do banco';
END $$;