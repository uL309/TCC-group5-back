# 📚 Documentação da API Airtrack - Swagger

## 🚀 Acesso Rápido

Após iniciar a aplicação, acesse a documentação interativa do Swagger:

### URLs de Acesso:

- **Swagger UI (Interface Interativa):** http://localhost:8080/swagger-ui.html
- **API Docs (JSON):** http://localhost:8080/api-docs
- **API Docs (YAML):** http://localhost:8080/api-docs.yaml

---

## 🔐 Como Usar a Autenticação JWT

### Passo 1: Fazer Login

1. Na interface do Swagger, vá até a seção **"Autenticação"**
2. Expanda o endpoint `POST /login`
3. Clique em **"Try it out"**
4. Use um dos exemplos de credenciais:

**Exemplo - Admin:**
```json
{
  "username": "admin@airtrack.com",
  "password": "admin123"
}
```

**Exemplo - Supervisor:**
```json
{
  "username": "supervisor@airtrack.com",
  "password": "super123"
}
```

**Exemplo - Engenheiro:**
```json
{
  "username": "engenheiro@airtrack.com",
  "password": "eng123"
}
```

5. Clique em **"Execute"**
6. Copie o **token JWT** retornado na resposta

### Passo 2: Autorizar no Swagger

1. Na parte superior da página do Swagger, clique no botão **"Authorize"** 🔓
2. No campo de autenticação, digite:
   ```
   Bearer {seu-token-jwt-aqui}
   ```
   **Exemplo:**
   ```
   Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
   ```
3. Clique em **"Authorize"**
4. Clique em **"Close"**

✅ Agora você pode testar todos os endpoints protegidos!

---

## 📋 Estrutura da API

### 🏷️ Categorias de Endpoints

#### 1. **Autenticação** 🔐
- Login e geração de token JWT
- Registro de novos usuários
- Reset de senha
- Primeiro acesso

#### 2. **Motor** ✈️
- Cadastro de motores de aeronaves
- Atualização de horas de operação
- Consulta por ID ou lista completa
- Exclusão lógica
- Controle de TBO (Time Between Overhaul)

**Exemplo de Motor:**
```json
{
  "marca": "Pratt & Whitney",
  "modelo": "PT6A-60A",
  "serie_motor": "PCE-123456",
  "data_cadastro": "2025-01-15",
  "status": true,
  "horas_operacao": 850,
  "tbo": 3600,
  "cliente_cpf": "123.456.789-00",
  "cliente_nome": "Aviação Executiva Ltda"
}
```

#### 3. **Ordem de Serviço** 📝
- Criação de ordens de manutenção
- Adicionar linhas de serviço
- Atualização de status (PENDENTE → EM_ANDAMENTO → CONCLUIDA)
- Upload de arquivos técnicos
- Geração de PDF da ordem

**Status de Ordem:**
- `PENDENTE` - Aguardando início
- `EM_ANDAMENTO` - Em execução
- `CONCLUIDA` - Finalizada
- `CANCELADA` - Cancelada

#### 4. **Documentos** 📄
- Upload de Manuais (MOM/MCQ)
- Download de documentos
- Versionamento automático
- Armazenamento no Azure Blob Storage

**Tipos de Documentos:**
- **MOM** - Manual da Organização de Manutenção
- **MCQ** - Manual de Controle da Qualidade

#### 5. **Cliente** 👤
- Cadastro de proprietários de aeronaves
- Vinculação com motores
- Histórico de manutenções

#### 6. **Fornecedor** 🏢
- Cadastro de fornecedores de peças
- Gestão de contatos
- Controle de serviços

#### 7. **Peças** 🔧
- Inventário de peças
- Movimentação de estoque
- Controle de lote e validade

#### 8. **Relatórios** 📊
- Relatórios técnicos
- Relatórios operacionais
- Exportação em PDF

#### 9. **Logs de Auditoria** 🔍
- Rastreamento de todas as operações
- Filtros por usuário, módulo e período
- Logs por módulo (Cliente, Motor, Ordem, etc.)

#### 10. **Notificações** 🔔
- Alertas de TBO próximo
- Notificações de ordens pendentes
- Status do motor

---

## 🎯 Níveis de Acesso

| Papel | Permissões |
|-------|-----------|
| **ADMIN** | Acesso total ao sistema |
| **SUPERVISOR** | Gerenciamento de ordens e equipe |
| **ENGENHEIRO** | Operações técnicas e relatórios |
| **AUDITOR** | Somente leitura para auditoria |

---

## 💡 Dicas de Uso

### 1. Teste Direto no Swagger
- Use o botão **"Try it out"** em cada endpoint
- Edite os exemplos fornecidos
- Veja a resposta em tempo real

### 2. Exemplos Pré-configurados
Todos os endpoints possuem exemplos baseados em dados reais do sistema:
- Motores Pratt & Whitney
- Clientes existentes
- Ordens de serviço reais

### 3. Validação de Dados
O Swagger mostra:
- ✅ Campos obrigatórios
- 📝 Descrições detalhadas
- 🔢 Tipos de dados esperados
- 📋 Exemplos de valores válidos

### 4. Códigos de Resposta HTTP
- `200` - Sucesso
- `201` - Criado com sucesso
- `400` - Erro de validação
- `401` - Não autorizado (token inválido)
- `403` - Acesso negado (permissão insuficiente)
- `404` - Recurso não encontrado
- `500` - Erro interno do servidor

---

## 🔄 Fluxo Completo de Teste

### Exemplo: Criar uma Ordem de Serviço

1. **Fazer Login**
   ```
   POST /login
   ```

2. **Autorizar no Swagger** com o token retornado

3. **Criar uma Ordem**
   ```
   POST /ordem/cord
   ```
   ```json
   {
     "motor_id": 1,
     "data_abertura": "2025-10-19",
     "status": "PENDENTE",
     "descricao": "Revisão programada 500h"
   }
   ```

4. **Adicionar Linha de Serviço**
   ```
   POST /ordem/linhaordem
   ```

5. **Atualizar Status**
   ```
   PUT /ordem/cord/{id}
   ```

6. **Gerar PDF**
   ```
   GET /ordem/pdf/{id}
   ```

---

## 🛠️ Tecnologias

- **Framework:** Spring Boot 3.4.4
- **Documentação:** SpringDoc OpenAPI 3.0 (Swagger)
- **Segurança:** JWT (JSON Web Token)
- **Banco de Dados:** MySQL 8
- **Armazenamento:** Azure Blob Storage
- **Java:** JDK 21

---

## 📞 Suporte

Para dúvidas ou problemas:
- **Email:** contato@airtrack.com
- **Documentação Completa:** http://localhost:8080/swagger-ui.html

---

## 🎨 Recursos do Swagger

### Filtros e Ordenação
- Filtrar endpoints por tag
- Ordenar por método HTTP
- Buscar endpoints específicos

### Execução de Requisições
- Testar requisições diretamente no navegador
- Ver corpo da requisição e resposta
- Copiar comandos cURL

### Schemas Detalhados
- Visualizar estrutura completa dos objetos
- Ver tipos de dados e validações
- Exemplos integrados

---

**✨ Desenvolvido pela Equipe Airtrack**
