# Doari backend microservices

Backend distribuido para a plataforma simples de doacoes para ONGs.

## Servicos

- `usuarios-service`: cadastro de doadores e ONGs, login, logout logico e perfil.
- `doacoes-service`: doacoes, pedidos de ONGs, listagens e compatibilidade.
- `notificacao-service`: notificacoes simples geradas quando uma doacao combina com um pedido.

## Executar com Docker Compose

```bash
docker compose up --build
```

Portas:

- Usuarios: `http://localhost:8081`
- Doacoes: `http://localhost:8082`
- Notificacoes: `http://localhost:8083`
- MySQL: `localhost:3306`

O Compose cria tres schemas MySQL:

- `usuarios_db`
- `doacoes_db`
- `notificacoes_db`

O arquivo `docker/mysql/init.sql` tambem cria as tabelas iniciais:

- `usuarios_db.usuario`
- `doacoes_db.doacao`
- `doacoes_db.pedido`
- `notificacoes_db.notificacao`

## Contrato REST principal

Usuarios:

- `POST /usuarios/cadastro`
- `POST /auth/login`
- `POST /auth/logout`
- `POST /auth/validar-token`
- `GET /usuarios/{id}/perfil`

O login retorna um JWT:

```json
{
  "usuarioId": 1,
  "nome": "Maria",
  "email": "maria@email.com",
  "tipo": "DOADOR",
  "tokenType": "Bearer",
  "accessToken": "eyJ...",
  "expiresIn": 86400
}
```

O segredo do JWT pode ser configurado com `JWT_SECRET`.

As rotas do `doacoes-service` exigem o cabecalho:

```http
Authorization: Bearer <accessToken>
```

O `doacoes-service` valida o JWT com o mesmo `JWT_SECRET` do `usuarios-service`.
Em operacoes de doador, o `usuarioId` vem do token. Em operacoes de ONG, o
`ongId` vem do token. Por isso, o cliente nao deve enviar esses IDs no corpo.

Doacoes:

- `POST /doacoes`
- `GET /doacoes`
- `GET /doacoes/usuario/{usuarioId}`
- `PUT /doacoes/{id}`
- `DELETE /doacoes/{id}`

Pedidos:

- `POST /pedidos`
- `GET /pedidos`
- `GET /pedidos/ong/{ongId}`

Compatibilidade:

- `GET /compatibilidades`

Exemplo de cadastro de doacao:

```json
{
  "item": "Arroz",
  "quantidade": "5 kg",
  "descricao": "Pacotes fechados"
}
```

Exemplo de cadastro de pedido:

```json
{
  "item": "Arroz",
  "quantidade": "10 kg",
  "descricao": "Pedido para cesta basica"
}
```

Notificacoes:

- `POST /notificacoes`
- `GET /notificacoes?destinatarioTipo=ONG&destinatarioId=1`
- `POST /notificacoes/{id}/lida`
- `GET /notificacoes/stream?token=<accessToken>&destinatarioTipo=ONG&destinatarioId=1`

`POST /notificacoes` e uma rota interna. Ela exige:

```http
X-Internal-Token: <NOTIFICACAO_INTERNAL_TOKEN>
```

O `doacoes-service` envia esse header quando encontra compatibilidade. O frontend
nao deve chamar essa rota diretamente.

As rotas `GET /notificacoes`, `POST /notificacoes/{id}/lida` e
`GET /notificacoes/stream` validam o JWT do usuario. O destinatario informado
precisa bater com o `id` e o `tipo` presentes no token.

Exemplo de SSE no frontend:

```js
const source = new EventSource(
  "http://localhost:8083/notificacoes/stream"
    + "?token=" + encodeURIComponent(accessToken)
    + "&destinatarioTipo=ONG"
    + "&destinatarioId=1"
);

source.addEventListener("notificacao", (event) => {
  const notificacao = JSON.parse(event.data);
  console.log("Nova notificacao:", notificacao);
});
```
