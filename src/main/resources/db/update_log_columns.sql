-- Script para alterar as colunas de todas as tabelas de log para LONGTEXT
-- Execute este script no seu banco de dados MySQL

-- Alterar tabela logs_ordem_servico
ALTER TABLE logs_ordem_servico MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_ordem_servico MODIFY COLUMN responseData LONGTEXT;

-- Alterar tabela logs_cliente
ALTER TABLE logs_cliente MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_cliente MODIFY COLUMN responseData LONGTEXT;

-- Alterar tabela logs_fornecedor
ALTER TABLE logs_fornecedor MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_fornecedor MODIFY COLUMN responseData LONGTEXT;

-- Alterar tabela logs_pecas
ALTER TABLE logs_pecas MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_pecas MODIFY COLUMN responseData LONGTEXT;

-- Alterar tabela logs_motor
ALTER TABLE logs_motor MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_motor MODIFY COLUMN responseData LONGTEXT;

-- Alterar tabela logs_user
ALTER TABLE logs_user MODIFY COLUMN requestData LONGTEXT;
ALTER TABLE logs_user MODIFY COLUMN responseData LONGTEXT;
