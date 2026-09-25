# Memes

**Projeto acadêmico de Desenvolvimento de Soluções WEB.**

Aplicação web para busca, visualização e download de memes (imagens e GIFs), com autenticação de usuários, perfis distintos (usuário e administrador) e integração com APIs externas.

> Este repositório foi desenvolvido com fins educacionais, no contexto da disciplina de **Desenvolvimento de Soluções WEB**. Não se trata de um produto comercial.

---

## Objetivo

Demonstrar, na prática, conceitos de desenvolvimento web server-side:

- Servlets e mapeamento de rotas
- Sessões e controle de acesso por perfil
- Persistência em banco de dados relacional
- Formulários HTML e feedback ao usuário
- Consumo de APIs REST externas
- Empacotamento e deploy de aplicação WAR

---

## Funcionalidades

- Cadastro e login de usuários (senha com BCrypt)
- Sessão autenticada com redirecionamento por perfil
  - **Usuário** → feed de busca de memes
  - **Admin** → painel para cadastrar memes no catálogo local
- Busca de memes (imagem e/ou GIF)
  - Catálogo local (PostgreSQL)
  - Giphy (GIFs e frames estáticos)
  - Imgflip (templates clássicos)
- Download do meme com tela de confirmação
- Exclusão de conta
- Logout e proteção de rotas com sessão

---

## Tecnologias

| Camada | Tecnologia |
|--------|------------|
| Linguagem | Java 18 |
| Web | Jakarta Servlet API 6 (Tomcat 10.1) |
| Build | Maven (`packaging: war`) |
| Banco | PostgreSQL |
| Segurança | jBCrypt |
| JSON / APIs | Gson, HttpClient |
| Front | HTML, CSS, JavaScript |

---

## Estrutura do projeto

```
src/main/
├── java/org/example/
│   ├── controller/   # Servlets (login, cadastro, home, admin, download…)
│   ├── filter/       # No-cache
│   ├── model/        # Entidades e DAOs
│   └── service/      # Busca de memes e clientes de API
├── views/            # Páginas HTML
├── WebApp/           # CSS, imagens, GIFs, index
└── resources/        # Configurações (ex.: chave Giphy)
```

---

## Pré-requisitos

- JDK 18+
- Maven 3+
- Apache Tomcat 10.1
- PostgreSQL com banco `memes`
- (Opcional) chave da API Giphy para busca de GIFs

---

## Configuração rápida

### Banco de dados

Ajuste a conexão em `src/main/java/org/example/model/Conexao.java`.

Tabela de usuários (exemplo):

```sql
CREATE TABLE usuarios (
    id SERIAL PRIMARY KEY,
    login VARCHAR(100) UNIQUE NOT NULL,
    senha VARCHAR(255) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT true
);
```

Tabela de memes (catálogo local):

```sql
CREATE TABLE memes (
    id SERIAL PRIMARY KEY,
    titulo VARCHAR(100) NOT NULL,
    tags VARCHAR(255),
    caminho VARCHAR(255) NOT NULL,
    tipo VARCHAR(10) NOT NULL,
    status BOOLEAN NOT NULL DEFAULT true
);
```

### Giphy (opcional)

```bash
cp src/main/resources/giphy.properties.example src/main/resources/giphy.properties
```

Edite o arquivo e informe `GIPHY_API_KEY`.  
O arquivo real está no `.gitignore` e **não** deve ser versionado.

### Build e deploy

```bash
mvn clean package
```

O WAR gerado fica em `target/Memes-1.0-SNAPSHOT.war`.  
Há scripts `deploy.sh` / `redeploy.sh` para publicar no Tomcat local.

Após o deploy, acesse: `http://localhost:8080/login`

---

## Rotas principais

| Rota | Descrição |
|------|-----------|
| `/login` | Autenticação |
| `/cadastro` | Criação de conta |
| `/home` | Feed / busca (usuário) |
| `/buscar` | API JSON da busca |
| `/download` | Download do meme |
| `/admin` | Painel do administrador |
| `/meme` | Cadastro de meme (admin) |
| `/logout` | Encerrar sessão |
| `/excluir` | Excluir conta |

---

## Observações acadêmicas

- O visual e o tom das telas seguem a proposta lúdica do tema “memes”.
- Integrações externas (Giphy/Imgflip) dependem de disponibilidade das APIs e, no caso do Giphy, de uma chave válida.
- Credenciais de banco e chaves de API **não** devem ser commitadas.

---

## Licença / uso

Uso estritamente acadêmico. Conteúdos obtidos via APIs de terceiros permanecem sujeitos aos termos de cada provedor.
