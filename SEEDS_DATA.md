# 🌱 Seeds - Dados de Exemplo para Testes

Este arquivo contém dados de exemplo para popular o banco de dados Airtrack e testar a API via Swagger.

## 👤 Usuários de Teste

### ADMIN
```json
{
  "username": "admin@airtrack.com",
  "password": "admin123",
  "role": "ADMIN",
  "name": "Administrador do Sistema",
  "cpf": "000.000.000-00",
  "status": true,
  "firstAccess": false
}
```

### SUPERVISOR
```json
{
  "username": "supervisor@airtrack.com",
  "password": "super123",
  "role": "SUPERVISOR",
  "name": "João Silva",
  "cpf": "111.111.111-11",
  "status": true,
  "firstAccess": false
}
```

### ENGENHEIRO
```json
{
  "username": "engenheiro@airtrack.com",
  "password": "eng123",
  "role": "ENGENHEIRO",
  "name": "Maria Santos",
  "cpf": "222.222.222-22",
  "status": true,
  "firstAccess": false
}
```

### AUDITOR
```json
{
  "username": "auditor@airtrack.com",
  "password": "aud123",
  "role": "AUDITOR",
  "name": "Carlos Oliveira",
  "cpf": "333.333.333-33",
  "status": true,
  "firstAccess": false
}
```

---

## 👥 Clientes de Exemplo

### Cliente 1 - Aviação Executiva
```json
{
  "nome": "Aviação Executiva Ltda",
  "cpf": "123.456.789-00",
  "email": "contato@aviacaoexecutiva.com.br",
  "telefone": "(11) 98765-4321",
  "endereco": "Av. Paulista, 1000 - São Paulo/SP",
  "status": true
}
```

### Cliente 2 - Táxi Aéreo Premium
```json
{
  "nome": "Táxi Aéreo Premium",
  "cpf": "987.654.321-00",
  "email": "operacoes@taxiaereopremium.com.br",
  "telefone": "(21) 99876-5432",
  "endereco": "Av. Atlântica, 500 - Rio de Janeiro/RJ",
  "status": true
}
```

### Cliente 3 - Helicópteros do Sul
```json
{
  "nome": "Helicópteros do Sul S.A.",
  "cpf": "456.789.123-00",
  "email": "manutencao@helicopterossul.com.br",
  "telefone": "(51) 98765-1234",
  "endereco": "Rua dos Aviadores, 250 - Porto Alegre/RS",
  "status": true
}
```

---

## ✈️ Motores de Exemplo

### Motor 1 - Pratt & Whitney PT6A-60A
```json
{
  "marca": "Pratt & Whitney",
  "modelo": "PT6A-60A",
  "serie_motor": "PCE-123456",
  "data_cadastro": "2023-01-15",
  "status": true,
  "horas_operacao": 850,
  "tbo": 3600,
  "cliente_cpf": "123.456.789-00",
  "cliente_nome": "Aviação Executiva Ltda"
}
```

### Motor 2 - Honeywell TPE331-10
```json
{
  "marca": "Honeywell",
  "modelo": "TPE331-10",
  "serie_motor": "HON-789012",
  "data_cadastro": "2023-03-20",
  "status": true,
  "horas_operacao": 2450,
  "tbo": 4000,
  "cliente_cpf": "987.654.321-00",
  "cliente_nome": "Táxi Aéreo Premium"
}
```

### Motor 3 - Turbomeca Arriel 2B1
```json
{
  "marca": "Turbomeca",
  "modelo": "Arriel 2B1",
  "serie_motor": "TUR-345678",
  "data_cadastro": "2024-06-10",
  "status": true,
  "horas_operacao": 150,
  "tbo": 3500,
  "cliente_cpf": "456.789.123-00",
  "cliente_nome": "Helicópteros do Sul S.A."
}
```

### Motor 4 - Rolls-Royce M250-C30
```json
{
  "marca": "Rolls-Royce",
  "modelo": "M250-C30",
  "serie_motor": "RR-901234",
  "data_cadastro": "2022-11-05",
  "status": true,
  "horas_operacao": 3200,
  "tbo": 3600,
  "cliente_cpf": "456.789.123-00",
  "cliente_nome": "Helicópteros do Sul S.A."
}
```

---

## 🏢 Fornecedores de Exemplo

### Fornecedor 1 - Parts Supply Aviation
```json
{
  "nome": "Parts Supply Aviation",
  "cnpj": "12.345.678/0001-90",
  "email": "vendas@partssupply.com.br",
  "telefone": "(11) 3456-7890",
  "endereco": "Rua das Peças, 100 - São Paulo/SP",
  "status": true
}
```

### Fornecedor 2 - Aero Componentes Brasil
```json
{
  "nome": "Aero Componentes Brasil",
  "cnpj": "98.765.432/0001-10",
  "email": "comercial@aerocomponentes.com.br",
  "telefone": "(21) 2345-6789",
  "endereco": "Av. Industrial, 500 - Rio de Janeiro/RJ",
  "status": true
}
```

### Fornecedor 3 - Helico Parts Ltda
```json
{
  "nome": "Helico Parts Ltda",
  "cnpj": "45.678.901/0001-23",
  "email": "suporte@helicoparts.com.br",
  "telefone": "(51) 3210-9876",
  "endereco": "Rua Aeroporto, 300 - Porto Alegre/RS",
  "status": true
}
```

---

## 🔧 Peças de Exemplo

### Peça 1 - Filtro de Óleo
```json
{
  "nome": "Filtro de Óleo PT6A",
  "codigo": "FLT-PT6A-001",
  "descricao": "Filtro de óleo para motor Pratt & Whitney PT6A",
  "quantidade_estoque": 25,
  "preco_unitario": 450.00,
  "fornecedor_id": 1,
  "status": true
}
```

### Peça 2 - Vela de Ignição
```json
{
  "nome": "Vela de Ignição TPE331",
  "codigo": "IGN-TPE-002",
  "descricao": "Vela de ignição premium para Honeywell TPE331",
  "quantidade_estoque": 50,
  "preco_unitario": 280.00,
  "fornecedor_id": 2,
  "status": true
}
```

### Peça 3 - Rolamento Principal
```json
{
  "nome": "Rolamento Principal Arriel",
  "codigo": "BRG-ARR-003",
  "descricao": "Rolamento principal para Turbomeca Arriel 2B1",
  "quantidade_estoque": 10,
  "preco_unitario": 1250.00,
  "fornecedor_id": 3,
  "status": true
}
```

---

## 📝 Ordens de Serviço de Exemplo

### OS 1 - Revisão 500h Motor PCE-123456
```json
{
  "motor_id": 1,
  "data_abertura": "2025-10-01",
  "data_prevista": "2025-10-15",
  "status": "EM_ANDAMENTO",
  "descricao": "Revisão programada de 500 horas de operação",
  "observacoes": "Cliente solicitou inspeção completa do sistema de combustível",
  "tecnico_responsavel": "João Silva"
}
```

**Linhas da OS 1:**
```json
[
  {
    "descricao": "Troca de filtro de óleo",
    "quantidade": 1,
    "peca_id": 1,
    "valor_unitario": 450.00,
    "tempo_estimado": 2.0
  },
  {
    "descricao": "Inspeção visual do compressor",
    "quantidade": 1,
    "valor_unitario": 0.00,
    "tempo_estimado": 4.0
  },
  {
    "descricao": "Teste funcional pós-manutenção",
    "quantidade": 1,
    "valor_unitario": 0.00,
    "tempo_estimado": 3.0
  }
]
```

### OS 2 - Manutenção Corretiva Motor HON-789012
```json
{
  "motor_id": 2,
  "data_abertura": "2025-10-10",
  "data_prevista": "2025-10-25",
  "status": "PENDENTE",
  "descricao": "Substituição de velas de ignição e teste de performance",
  "observacoes": "Cliente reportou queda de potência",
  "tecnico_responsavel": "Maria Santos"
}
```

**Linhas da OS 2:**
```json
[
  {
    "descricao": "Substituição de 4 velas de ignição",
    "quantidade": 4,
    "peca_id": 2,
    "valor_unitario": 280.00,
    "tempo_estimado": 3.0
  },
  {
    "descricao": "Teste de performance em dinamômetro",
    "quantidade": 1,
    "valor_unitario": 0.00,
    "tempo_estimado": 5.0
  }
]
```

### OS 3 - Overhaul Programado Motor TUR-345678
```json
{
  "motor_id": 3,
  "data_abertura": "2025-11-01",
  "data_prevista": "2025-12-15",
  "status": "PENDENTE",
  "descricao": "Overhaul completo programado - 3500 horas",
  "observacoes": "Overhaul de fábrica. Necessário kit completo",
  "tecnico_responsavel": "João Silva"
}
```

---

## 🔔 Notificações de Exemplo

### Notificação 1 - TBO Próximo
```json
{
  "type": "MOTOR_TBO_EXPIRED",
  "title": "TBO Próximo - Motor PCE-123456",
  "message": "O motor PCE-123456 atingiu 85% do TBO (850/3600 horas). Programe a revisão.",
  "status": "ACTIVE",
  "motor_id": 1
}
```

### Notificação 2 - OS Pendente
```json
{
  "type": "OS_PENDING",
  "title": "Ordem de Serviço #2 Pendente",
  "message": "OS #2 aguardando início há 5 dias. Motor: HON-789012",
  "status": "ACTIVE",
  "ordem_servico_id": 2
}
```

---

## 🗄️ Script SQL para Popular Banco

```sql
-- Inserir usuários (senhas são hasheadas no código)
-- Use o endpoint /register para criar os usuários

-- Inserir clientes
INSERT INTO cliente (nome, cpf, email, telefone, endereco, status) VALUES
('Aviação Executiva Ltda', '123.456.789-00', 'contato@aviacaoexecutiva.com.br', '(11) 98765-4321', 'Av. Paulista, 1000 - São Paulo/SP', true),
('Táxi Aéreo Premium', '987.654.321-00', 'operacoes@taxiaereopremium.com.br', '(21) 99876-5432', 'Av. Atlântica, 500 - Rio de Janeiro/RJ', true),
('Helicópteros do Sul S.A.', '456.789.123-00', 'manutencao@helicopterossul.com.br', '(51) 98765-1234', 'Rua dos Aviadores, 250 - Porto Alegre/RS', true);

-- Inserir motores (ajustar cliente ID conforme necessário)
INSERT INTO motor (marca, modelo, serie_motor, data_cadastro, status, horas_operacao, cliente) VALUES
('Pratt & Whitney', 'PT6A-60A', 'PCE-123456', '2023-01-15', true, 850, 1),
('Honeywell', 'TPE331-10', 'HON-789012', '2023-03-20', true, 2450, 2),
('Turbomeca', 'Arriel 2B1', 'TUR-345678', '2024-06-10', true, 150, 3),
('Rolls-Royce', 'M250-C30', 'RR-901234', '2022-11-05', true, 3200, 3);
```

---

## 📌 Notas Importantes

1. **Senhas**: Todas as senhas de exemplo devem ser alteradas em produção
2. **CPF/CNPJ**: Valores fictícios para testes
3. **IDs**: Ajuste os IDs de referência conforme seu banco de dados
4. **Datas**: Atualize as datas conforme necessário para seus testes
5. **TBO**: Values são exemplos, consulte fabricante para valores reais

---

**Use estes dados para testar a API no Swagger! 🚀**
