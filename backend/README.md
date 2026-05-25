# Projeto Estagio Jacto Backend

Backend do desafio tecnico construído com Spring Boot, Kotlin, Spring Security com JWT, JPA e MySQL.

## Tecnologias

- Java 17
- Kotlin 1.9
- Spring Boot 3
- Spring Web
- Spring Data JPA
- Spring Security
- JWT
- MySQL
- Liquibase
- JUnit 5 / MockMvc
- JaCoCo

## Estrutura

O projeto segue arquitetura MVC com separacao de responsabilidades nos pacotes:

- `config`
- `controller`
- `dto`
- `entity`
- `exception`
- `mapper`
- `repository`
- `security`
- `service`

## Requisitos para executar

- Java 17+
- MySQL 8+

## Configuracao

As configuracoes principais ficam em `src/main/resources/application.yml`.

Variaveis de ambiente suportadas:

- `DB_HOST`
- `DB_PORT`
- `DB_NAME`
- `DB_USERNAME`
- `DB_PASSWORD`
- `JWT_SECRET`
- `JWT_EXPIRATION_MS`

Valores padrao locais:

- banco: `jacto_challenge`
- usuario: `root`
- senha: `root`

## Como rodar

1. Suba um MySQL 8 local. Exemplo com Docker:

```bash
docker run --name rodojacto-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=jacto_challenge \
  -p 3306:3306 \
  -d mysql:8
```

2. Crie um banco MySQL ou deixe o Spring criar automaticamente com `createDatabaseIfNotExist=true`.
3. Ajuste as variaveis de ambiente, se necessario.
4. Execute:

```bash
./mvnw spring-boot:run
```

O Liquibase aplicara as migrations automaticamente na subida da aplicacao.

## Migrations

As migrations ficam em:

- `src/main/resources/db/changelog/db.changelog-master.yaml`
- `src/main/resources/db/changelog/changes/001-initial-schema.yaml`
- `src/main/resources/db/changelog/changes/002-seed-data.yaml`

Elas sao executadas automaticamente ao iniciar a aplicacao e sao a unica fonte do schema e do seed inicial.

## Seed inicial

O seed inicial e aplicado pelo Liquibase no banco MySQL quando as tabelas estao vazias:

- organizacoes:
  - `Rodojacto Matriz`
  - `Rodojacto Operacoes`
- usuarios:
  - `manager@rodojacto.com` / `123456`
  - `operator@rodojacto.com` / `123456`
- colaboradores e dispositivos adicionais para demonstracao

Nao existe mock, JSON local, array em memoria ou repository fake no fluxo principal do backend.

## Autenticacao

Endpoint de login:

```http
POST /api/auth/login
Content-Type: application/json
```

Payload:

```json
{
  "email": "manager@rodojacto.com",
  "password": "123456"
}
```

As demais rotas exigem `Authorization: Bearer <token>`.

## Swagger

Documentacao interativa:

- `http://localhost:8080/swagger-ui/index.html`
- `http://localhost:8080/v3/api-docs`

No Swagger, use o botao `Authorize` e informe:

```text
Bearer <token>
```

## Endpoints principais

### Publicos

- `POST /api/auth/login`
- `GET /swagger-ui/**`
- `GET /v3/api-docs/**`

### Protegidos por JWT

- `GET /api/auth/me`
- `GET /api/dashboard/summary`
- `GET/POST/PUT/DELETE /api/organizations`
- `GET/POST/PUT/DELETE /api/collaborators`
- `GET/POST/PUT/DELETE /api/devices`

### Auth

- `POST /api/auth/login`
- `GET /api/auth/me`

### Organizations

- `GET /api/organizations`
- `GET /api/organizations/{id}`
- `POST /api/organizations`
- `PUT /api/organizations/{id}`
- `DELETE /api/organizations/{id}`

### Collaborators

- `GET /api/collaborators`
- `GET /api/collaborators/{id}`
- `POST /api/collaborators`
- `PUT /api/collaborators/{id}`
- `DELETE /api/collaborators/{id}`

### Devices

- `GET /api/devices`
- `GET /api/devices/{id}`
- `POST /api/devices`
- `PUT /api/devices/{id}`
- `DELETE /api/devices/{id}`

### Dashboard

- `GET /api/dashboard/summary`

## Regras de acesso implementadas

- `MANAGER` pode visualizar todos os registros do sistema
- `MANAGER` pode criar, atualizar e remover registros
- `OPERATOR` pode visualizar apenas sua organizacao, colaboradores da propria organizacao e dispositivos da propria organizacao
- `OPERATOR` nao pode criar, atualizar ou remover registros

## Testes

Executar todos os testes:

```bash
./mvnw test
```

O profile de teste usa H2 em modo MySQL apenas para testes automatizados e aplica as mesmas migrations do Liquibase.

## Estado real do projeto

- backend principal com Spring Boot, Spring Data JPA, JWT e MySQL real
- relacionamentos reais entre `Organization`, `Collaborator` e `Device`
- schema e seed controlados por Liquibase
- frontend consumindo a API HTTP real em `http://localhost:8080/api`
- mocks restritos a testes unitarios/backend e estado local de sessao no frontend

## Cobertura

Apos rodar os testes, o relatorio JaCoCo fica em:

`target/site/jacoco/index.html`
