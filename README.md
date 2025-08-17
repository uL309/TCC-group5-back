# RabbitMQ no Projeto

O RabbitMQ é utilizado neste projeto como broker de mensagens para comunicação assíncrona entre os módulos do sistema. Ele é responsável por:
- Gerenciar a fila de eventos de domínio (ex: notificações de ordens de serviço pendentes, mudanças de status, etc).
- Permitir que notificações sejam processadas de forma desacoplada e resiliente, melhorando a escalabilidade e a robustez do backend.
- Garantir que eventos importantes do sistema sejam entregues para os consumidores (ex: serviço de notificações) mesmo que ocorram picos de uso ou falhas temporárias.

---

# Como rodar o RabbitMQ com Docker Compose

Para rodar o RabbitMQ localmente para desenvolvimento e testes, utilize o seguinte serviço no seu arquivo `docker-compose.yml` na raiz do projeto:

```yaml
version: '3.8'
services:
  rabbitmq:
    image: rabbitmq:3-management
    container_name: rabbitmq
    ports:
      - "5672:5672"   # Porta padrão do RabbitMQ
      - "15672:15672" # Porta do painel de administração web
    environment:
      RABBITMQ_DEFAULT_USER: guest
      RABBITMQ_DEFAULT_PASS: guest
```

## Passos para subir o RabbitMQ

1. Certifique-se de ter o Docker e o Docker Compose instalados.
2. Adicione o bloco acima ao seu `docker-compose.yml` (ou crie um novo arquivo se necessário).
3. No terminal, na raiz do projeto, execute:

```sh
docker-compose up -d rabbitmq
```

4. O RabbitMQ estará disponível em `localhost:5672` (aplicação) e o painel web em `localhost:15672` (usuário: guest, senha: guest).

5. Para parar o serviço:

```sh
docker-compose down rabbitmq
```

> **Observação:** O backend está preparado para ignorar falhas de conexão com o RabbitMQ, então a aplicação não irá crashar caso o serviço esteja fora do ar.
