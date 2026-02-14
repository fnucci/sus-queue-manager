# SUS Queue Manager

Aplicação criada para a Fase 5 do Hackathon FIAP — "SUS Queue Manager" — para demonstrar um fluxo de gerenciamento de filas de interesse e disponibilidade do SUS com integração via filas (RabbitMQ) e notificações via WhatsApp.

Sumário
- Descrição
- Arquitetura e fluxo
- Tecnologias
- Pré-requisitos
- Como compilar e rodar (Windows)
- Como rodar com Docker / docker-compose
- Como executar testes
- Arquivos de configuração importantes
- Endpoints principais e payloads (resumo)
- Variáveis de ambiente e `.env.example` sugerido
- Troubleshooting
- Como contribuir
- Próximos passos e gaps identificados

Descrição
O SUS Queue Manager automatiza o fluxo de: receber interesses de pacientes (interesse por vaga), registrar disponibilidades (vagas), notificar pacientes por WhatsApp quando há compatibilidade e processar confirmações/respostas via webhook e filas.
A aplicação usa produtores/consumidores (RabbitMQ) para desacoplar ingestão e processamento, e possui scheduler para notificar interesses automaticamente.

Arquitetura e fluxo (resumo)
- Controllers: expõem endpoints HTTP que recebem `Interest`, `Availability` e webhooks (ex.: `WhatsAppWebhookController`, `AvailabilityController`, `InterestController`, `ResponseWebhookController`).
- Producers: publicam mensagens nas filas (ex.: `InterestProducer`, `AvailabilityProducer`, `AnswerProducer`).
- Consumers: consomem mensagens e executam lógica de negócio (ex.: `InterestConsumer`, `AvailabilityConsumer`, `AnswerConsumer`).
- Services: lógica de negócio e integração com o provider de WhatsApp (`WhatsAppNotificationService`, `AnswerService`, `WebhookResponseService`).
- Scheduler: `NotifyInterestsScheduler` varre disponibilidades e notifica interesses.
- Mensageria: RabbitMQ, configurado em `RabbitConfig` (exchanges, queues e bindings).

Tecnologias
- Java (projeto usa Spring Boot)
- Spring Boot (Starter, AMQP, Data JPA)
- RabbitMQ (mensageria)
- PostgreSQL (banco relacional)
- Maven (com wrapper `mvnw` / `mvnw.cmd`)
- Docker & Docker Compose
- Lombok, Jackson, JUnit 5, Mockito (para testes)

Pré-requisitos
- Java 17 (ou compatível com Spring Boot usado)
- Maven (pode usar o wrapper incluido)
- Docker e Docker Compose (se for rodar containers)
- Windows PowerShell (ex.: para comandos locais)

Como compilar e rodar (Windows PowerShell)
1. Compilar e empacotar (na raiz do projeto):

```powershell
.\mvnw.cmd clean package -DskipTests
```

2. Rodar com o Maven (modo desenvolvimento):

```powershell
.\mvnw.cmd spring-boot:run
```

3. Ou executar o JAR gerado:

```powershell
java -jar target\*.jar
```

A aplicação por padrão fica disponível em http://localhost:8080 (ver `application.properties`).

Como rodar com Docker / docker-compose
O repositório contém `docker-compose.yml` para orquestrar a aplicação junto com serviços necessários (Postgres e RabbitMQ).

1. Subir todos os serviços (build da imagem da app + infra):

```powershell
docker-compose up --build -d
```

2. Verificar logs (ex.: do app):

```powershell
docker-compose logs -f app
```

3. Parar e remover containers:

```powershell
docker-compose down
```

Portas usuais (conforme docker-compose):
- App: 8080
- Postgres: 5432
- RabbitMQ Management: 15672 (GUI web)

Como executar testes
- Rodar todos os testes com Maven (PowerShell):

```powershell
.\mvnw.cmd test
```

O projeto inclui testes unitários para controllers, producers/consumers, services e scheduler (ver diretório `src/test/java`).

Arquivos de configuração importantes
- `src/main/resources/application.properties` — configurações de datasource, RabbitMQ e WAPI (provider do WhatsApp) utilizadas em desenvolvimento.
- `src/main/java/br/com/fiap/hackaton/config/RabbitConfig.java` — definição de exchanges, queues e bindings.
- `Dockerfile` — imagem multi-stage para build e execução.
- `docker-compose.yml` — orquestra Postgres, RabbitMQ e a aplicação.
- `pom.xml` — dependências e plugins do Maven.

Endpoints principais (resumo com rotas)
Com base nas controllers encontradas no projeto:
- POST /availability
  - Controller: `AvailabilityController`
  - Uso: registra uma disponibilidade e publica mensagem na fila (AvailabilityProducer).
  - Payload: ver DTOs em `src/main/java/br/com/fiap/hackaton/dto/request`.

- POST /interests
  - Controller: `InterestController`
  - Uso: registra um interesse de paciente e publica mensagem na fila (InterestProducer).
  - Payload: ver DTOs em `src/main/java/br/com/fiap/hackaton/dto/request`.

- POST /webhook/whatsapp
  - Controller: `WhatsAppWebhookController`
  - Uso: endpoint para receber callbacks do provider WhatsApp. Converte o body em `AnswerRequest` (telefone + resposta: sim/não) e publica na fila (AnswerProducer).
  - Observação: o controller extrai `chat.id` (telefone) e `msgContent.conversation` para determinar aceitação.

- POST /webhook/response
  - Controller: `ResponseWebhookController`
  - Uso: endpoint de webhook para simular/receber respostas externas (ver Controller para detalhes).

Para payloads exatos, consulte as classes DTO em `src/main/java/br/com/fiap/hackaton/dto/request`.

Variáveis de ambiente e `.env.example` sugerido
Recomenda-se externalizar credenciais em variáveis de ambiente. Variáveis detectadas no projeto / docker-compose:

- SPRING_DATASOURCE_URL (ex.: jdbc:postgresql://postgres:5432/postech_fase5)
- SPRING_DATASOURCE_USERNAME
- SPRING_DATASOURCE_PASSWORD
- SPRING_RABBITMQ_HOST
- SPRING_RABBITMQ_PORT
- SPRING_RABBITMQ_USERNAME
- SPRING_RABBITMQ_PASSWORD
- wapi.url
- wapi.instanceId
- wapi.token

Exemplo `.env.example` (coloque na raiz do projeto, não commite segredos reais):

```
SPRING_DATASOURCE_URL=jdbc:postgresql://postgres:5432/postech_fase5
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=admin
SPRING_RABBITMQ_HOST=rabbitmq
SPRING_RABBITMQ_PORT=5672
SPRING_RABBITMQ_USERNAME=guest
SPRING_RABBITMQ_PASSWORD=guest
WAPI_URL=http://wapi.example.com
WAPI_INSTANCE_ID=0000
WAPI_TOKEN=your-token-here
```

Troubleshooting (dicas rápidas)
- RabbitMQ: se o app não conectar à fila, verifique se o container `rabbitmq` está rodando e se as credenciais/host batem com `application.properties` ou variáveis de ambiente.
- PostgreSQL: se erros de conexão ocorrerem, confirme `SPRING_DATASOURCE_URL` e credenciais e se o container Postgres está saudável.
- Token WAPI no `application.properties`: mover para variável de ambiente em produção para não vazar segredos.
- Erros de binding/queues: verifique `RabbitConfig` e se as exchanges/queues foram corretamente criadas no broker.

Como contribuir
- Faça um fork do repositório e crie branches nomeadas como `feature/*` ou `fix/*`.
- Execute `mvnw.cmd test` e garanta que os testes passem antes de abrir PR.
- Documente novas variáveis no README e adicione `.env.example` quando apropriado.

Próximos passos sugeridos
- Externalizar segredos e tokens para variáveis de ambiente ou um secret manager (ex.: Vault).
- Adicionar exemplos de payload JSON completos para cada endpoint no README.
- Implementar testes de integração com Docker (Testcontainers ou pipeline CI) para validar o fluxo completo com RabbitMQ e Postgres.
- Melhorar observabilidade: health checks, metrics e logs estruturados.

Gaps e observações finais
- Algumas informações de produção/infra (tokens, URLs externas, políticas de retry, DLQ) não podem ser inferidas automaticamente e devem ser documentadas manualmente.
- Há um token (WAPI) presente em `application.properties` do projeto; ele deve ser tratado como segredo.

Arquivos-chave para referência rápida
- Controllers: `src/main/java/br/com/fiap/hackaton/controller/*`
- Producers: `src/main/java/br/com/fiap/hackaton/producer/*`
- Consumers: `src/main/java/br/com/fiap/hackaton/consumer/*`
- Services: `src/main/java/br/com/fiap/hackaton/service/*`
- Scheduler: `src/main/java/br/com/fiap/hackaton/scheduler/*`
- RabbitMQ config: `src/main/java/br/com/fiap/hackaton/config/RabbitConfig.java`
- Config: `src/main/resources/application.properties`
- Docker: `Dockerfile`, `docker-compose.yml`
- Build: `pom.xml`