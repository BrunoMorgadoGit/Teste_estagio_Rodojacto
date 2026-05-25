# Rodojacto - Gestão de Organizações, Colaboradores e Dispositivos

Sistema fullstack para o desafio técnico, com backend em Spring Boot/Kotlin, frontend Angular, autenticação JWT, MySQL, Liquibase, Swagger e regras de acesso por perfil.

## Funcionalidades

- Login com JWT.
- Dashboard com indicadores.
- CRUD de organizações.
- CRUD de colaboradores.
- CRUD de dispositivos.
- Controle de acesso por perfil `MANAGER` e `OPERATOR`.
- Validações de entrada nos DTOs.
- Tratamento global de erros no backend.
- Documentação Swagger/OpenAPI.
- Seed inicial aplicado via Liquibase.
- Testes automatizados no backend e no frontend.

## Tecnologias

Backend:

- Java 17.
- Kotlin 1.9.
- Spring Boot 3.5.
- Spring Security.
- JWT.
- Spring Data JPA.
- MySQL.
- Liquibase.
- Maven Wrapper.
- JUnit 5, MockMvc e JaCoCo.

Frontend:

- Angular 20.
- TypeScript.
- HTML, CSS e SCSS.
- HttpClient.
- HTTP Interceptor.
- Guards de rota.
- Reactive Forms.
- Jasmine/Karma.

## Estrutura do projeto

```text
.
├── backend/                 # Backend Spring Boot/Kotlin
├── frontend/                # Frontend Angular
├── docker-compose.yml       # MySQL local para desenvolvimento
└── README.md
```

## Pré-requisitos

- Java 17 ou superior.
- Node.js compatível com Angular 20.
- Docker com Docker Compose.
- MySQL 8, caso não use Docker.

Não é obrigatório instalar Maven globalmente, pois o backend usa Maven Wrapper (`./mvnw`). Também não é obrigatório instalar Angular CLI globalmente, pois os scripts npm usam a CLI local do projeto.

## Banco de dados

Suba o MySQL pela raiz do repositório:

```bash
docker compose up -d
```

Configuração local padrão:

- Host: `localhost`
- Porta: `3306`
- Banco: `jacto_challenge`
- Usuário: `root`
- Senha: `root`

O backend usa a URL:

```text
jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:jacto_challenge}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
```

Se a porta `3306` já estiver ocupada, altere o mapeamento no `docker-compose.yml` e execute o backend com `DB_PORT` apontando para a nova porta.

## Como rodar o backend

```bash
cd backend
./mvnw spring-boot:run
```

A API sobe por padrão em:

```text
http://localhost:8080/api
```

O Liquibase executa automaticamente na inicialização, cria o schema e aplica o seed inicial.

Variáveis de ambiente suportadas:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

## Como rodar o frontend

```bash
cd frontend
npm install
npm start
```

A aplicação Angular sobe em:

```text
http://localhost:4200
```

O frontend está configurado para consumir:

```text
http://localhost:8080/api
```

Essa URL fica em:

- `frontend/src/environments/environment.ts`
- `frontend/src/environments/environment.development.ts`

## Usuários de teste

O seed do Liquibase cria os usuários principais:

| Perfil | E-mail | Senha |
| --- | --- | --- |
| `MANAGER` | `manager@rodojacto.com` | `123456` |
| `OPERATOR` | `operator@rodojacto.com` | `123456` |

Também são criadas organizações, colaboradores e dispositivos iniciais para demonstração.

## Perfis e permissões

`MANAGER`:

- Visualiza todos os registros do sistema.
- Pode criar, atualizar e remover registros.
- Recebe totais globais no dashboard.

`OPERATOR`:

- Visualiza apenas a própria organização.
- Visualiza apenas colaboradores e dispositivos da própria organização.
- Não possui ações de escrita no frontend.
- Tem a segurança validada também no backend.

## Swagger

Swagger UI:

```text
http://localhost:8080/swagger-ui/index.html
```

OpenAPI JSON:

```text
http://localhost:8080/v3/api-docs
```

Para autenticar no Swagger:

1. Faça login em `POST /api/auth/login`.
2. Copie o token retornado.
3. Clique em `Authorize`.
4. Informe o JWT no esquema `bearerAuth`; as chamadas serão enviadas com `Authorization: Bearer <token>`.

Exemplo de login:

```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"manager@rodojacto.com","password":"123456"}'
```

## Principais endpoints

Autenticação:

- `POST /api/auth/login`
- `GET /api/auth/me`

Dashboard:

- `GET /api/dashboard/summary`

Organizações:

- `GET /api/organizations`
- `GET /api/organizations/{id}`
- `POST /api/organizations`
- `PUT /api/organizations/{id}`
- `DELETE /api/organizations/{id}`

Colaboradores:

- `GET /api/collaborators`
- `GET /api/collaborators/{id}`
- `POST /api/collaborators`
- `PUT /api/collaborators/{id}`
- `DELETE /api/collaborators/{id}`

Dispositivos:

- `GET /api/devices`
- `GET /api/devices/{id}`
- `POST /api/devices`
- `PUT /api/devices/{id}`
- `DELETE /api/devices/{id}`

Com exceção de `POST /api/auth/login`, os endpoints da API exigem token JWT.

## Liquibase e seed

As migrations ficam em:

- `backend/src/main/resources/db/changelog/db.changelog-master.yaml`
- `backend/src/main/resources/db/changelog/changes/001-initial-schema.yaml`
- `backend/src/main/resources/db/changelog/changes/002-seed-data.yaml`

Tabelas criadas:

- `organizations`
- `collaborators`
- `devices`
- tabelas internas do Liquibase (`databasechangelog` e `databasechangeloglock`)

O schema não é criado por `ddl-auto`; o backend usa `spring.jpa.hibernate.ddl-auto=validate`. As tabelas e dados iniciais são controlados pelo Liquibase. Os dados criados pela API são persistidos no MySQL.

## Testes

Backend:

```bash
cd backend
./mvnw test
```

E2E/API backend:

```bash
cd backend
./mvnw -Dtest=ApiE2ETest test
```

Frontend:

```bash
cd frontend
npm test -- --watch=false --browsers=ChromeHeadless
```

Não há Cypress ou Playwright configurado neste projeto. O E2E existente é de API no backend, usando Spring Boot/MockMvc.

## Observações importantes

- O fluxo principal do backend usa MySQL real, Spring Data JPA e repositories reais.
- H2 aparece somente no escopo de testes automatizados do backend.
- Senhas são armazenadas com BCrypt.
- Senhas não são retornadas nas responses dos colaboradores.
- Swagger e login são públicos; as demais rotas são protegidas por JWT.
- O frontend oculta ações de escrita para `OPERATOR`, mas a autorização principal fica no backend.

## Entrega

Para entregar o teste técnico:

1. Publique o repositório em um link público.
2. Informe no corpo da entrega que a solução usa Spring Boot/Kotlin, Angular, MySQL, Liquibase e JWT.
3. Informe os usuários de teste:
   - `manager@rodojacto.com` / `123456`
   - `operator@rodojacto.com` / `123456`
4. Informe os comandos principais:
   - `docker compose up -d`
   - `cd backend && ./mvnw spring-boot:run`
   - `cd frontend && npm install && npm start`

Preencher manualmente antes da entrega:

- Link público do repositório.
- Qualquer observação específica do ambiente usado na avaliação, caso a porta `3306`, `8080` ou `4200` precise ser alterada.
