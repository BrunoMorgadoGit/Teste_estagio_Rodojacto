# Rodojacto Frontend

Frontend Angular para consumir o backend Spring Boot/Kotlin do desafio tecnico.

O fluxo principal nao usa dados mockados: a aplicacao consome a API real do backend.

## Tecnologias

- Angular 20
- TypeScript
- Angular Router
- Reactive Forms
- HttpClient
- SCSS

## Estrutura

- `src/app/core`
  - `services`
  - `guards`
  - `interceptors`
  - `models`
- `src/app/shared`
  - `components`
- `src/app/pages`
  - `login`
  - `dashboard`
  - `organizations`
  - `collaborators`
  - `devices`

## Backend esperado

O frontend espera o backend rodando em:

`http://localhost:8080/api`

Essa URL esta configurada em:

- `src/environments/environment.ts`
- `src/environments/environment.development.ts`

Se a API mudar de porta ou host, ajuste esses arquivos.

## Como instalar

```bash
npm install
```

## Como rodar

```bash
npm start
```

Aplicacao Angular:

`http://localhost:4200`

## Usuarios de teste

Esses usuarios vem do seed aplicado no MySQL pelo Liquibase no backend:

- `manager@rodojacto.com` / `123456`
- `operator@rodojacto.com` / `123456`

## Regras de uso

- `MANAGER` pode visualizar e operar os CRUDs completos
- `OPERATOR` visualiza apenas dados da propria organizacao
- o frontend esconde acoes de escrita para `OPERATOR`
- a seguranca real continua no backend

## Principais telas

- Login
- Dashboard
- Organizacoes
- Colaboradores
- Dispositivos

## Integracoes com o backend

- `POST /auth/login`
- `GET /auth/me`
- `GET /dashboard/summary`
- CRUD de `organizations`
- CRUD de `collaborators`
- CRUD de `devices`

## Testar build

```bash
npm run build
```
