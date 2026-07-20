# Banco-tokenizacao

Aplicação full-stack de transações bancárias (PIX, depósito e saque) com tokenização. Permite cadastrar e autenticar contas (senha com BCrypt), aplicar regras de negócio (saldo, conta ativa, chave PIX por CPF ou e-mail) e consultar extrato. Stack: Spring Boot + Spring Data JPA + MySQL no back-end e Angular no front-end.

## Autenticação

A autenticação usa **sessão no servidor** (Spring Security) com cookie `JSESSIONID` HttpOnly — não há token no `localStorage`. Requisições que alteram estado exigem CSRF: o front lê o cookie `XSRF-TOKEN` e reenvia no header `X-XSRF-TOKEN`. A conta de origem das transações e o extrato são sempre derivados do usuário autenticado (o cliente não informa conta de origem).

### Endpoints

| Método | Rota | Acesso |
|---|---|---|
| `GET`  | `/api/auth/csrf` | público (emite o cookie XSRF-TOKEN) |
| `POST` | `/api/contas` | público + CSRF (cadastro) |
| `POST` | `/api/auth/login` | público + CSRF |
| `GET`  | `/api/auth/me` | autenticado |
| `POST` | `/api/auth/logout` | autenticado + CSRF |
| `GET`  | `/api/contas/me` | autenticado |
| `POST` | `/api/transacoes` | autenticado + CSRF (origem = conta logada) |
| `GET`  | `/api/transacoes` | autenticado (extrato da conta logada) |

Login de conta não `ATIVA` é recusado (bloqueada → 403; credencial inválida → 401). Sessão expira em 15 minutos.

## Como rodar

### Back-end (porta 8080)

Requer MySQL ativo. A senha do banco vem da variável `DB_PASSWORD` (sem default):

```bash
cd backend
# PowerShell:  $env:DB_PASSWORD = "sua_senha"
export DB_PASSWORD=sua_senha
./mvnw spring-boot:run
```

`DB_USERNAME` (default `root`) e `DB_URL` têm defaults. Contas de teste: veja `backend/database/seed.sql` (senha `123456`).

Rodar os testes de segurança (usa H2 em memória, não precisa de MySQL):

```bash
cd backend && ./mvnw test
```

### Front-end (porta 4200)

```bash
cd frontend
npm install
npm start
```

O `ng serve` usa `proxy.conf.json` para encaminhar `/api/**` ao back-end em `localhost:8080` — assim o navegador enxerga tudo na mesma origem e os cookies de sessão/CSRF funcionam.
