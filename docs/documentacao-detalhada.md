# Documentação detalhada

Documentação completa do projeto **Memes**.

> **Projeto acadêmico de Desenvolvimento de Soluções WEB.**  
> Desenvolvido com fins educacionais. Não é um produto comercial.

---

## 1. Objetivo

Demonstrar, na prática, conceitos de desenvolvimento web server-side:

- Servlets e mapeamento de rotas
- Sessões e controle de acesso por perfil
- Persistência em banco de dados relacional
- Formulários HTML e feedback ao usuário
- Consumo de APIs REST externas
- Empacotamento e deploy de aplicação WAR

---

## 2. Funcionalidades

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

## 3. Tecnologias

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

## 4. Estrutura do projeto

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

docs/
└── documentacao-detalhada.md
```

---

## 5. Pré-requisitos

- JDK 18+
- Maven 3+
- Apache Tomcat 10.1
- PostgreSQL com banco `memes`
- (Opcional) chave da API Giphy para busca de GIFs

---

## 6. Configuração

### 6.1 Banco de dados

Ajuste a conexão em `src/main/java/org/example/model/Conexao.java`.

Tabela de usuários:

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

### 6.2 Giphy (opcional)

```bash
cp src/main/resources/giphy.properties.example src/main/resources/giphy.properties
```

Edite o arquivo e informe `GIPHY_API_KEY`.  
O arquivo real está no `.gitignore` e **não** deve ser versionado.

Alternativas à chave:

- Variável de ambiente: `export GIPHY_API_KEY=sua_chave`
- Propriedade JVM: `-DGIPHY_API_KEY=sua_chave`

### 6.3 Build e deploy

```bash
mvn clean package
```

O WAR gerado fica em `target/Memes-1.0-SNAPSHOT.war`.

Scripts auxiliares na raiz:

- `deploy.sh` — deploy com backup e rollback
- `redeploy.sh` — para Tomcat, build limpo e sobe de novo

Após o deploy, acesse: `http://localhost:8080/login`

---

## 7. Rotas principais

| Rota | Método | Descrição |
|------|--------|-----------|
| `/` | GET | Redireciona para login |
| `/login` | GET/POST | Autenticação |
| `/cadastro` | GET/POST | Criação de conta |
| `/home` | GET | Feed / busca (usuário) |
| `/buscar` | GET | API JSON da busca (`q`, `tipo`) |
| `/download` | GET | Download do meme |
| `/admin` | GET | Painel do administrador |
| `/meme` | POST | Cadastro de meme (admin) |
| `/logout` | GET | Encerrar sessão |
| `/excluir` | GET/POST | Excluir conta |

### Fluxo resumido

```
Cadastro → Login → Home (usuário) ou Admin
                ↓
         Buscar meme → Clicar → Download → download-ok
                ↓
         Logout / Excluir conta
```

---

## 8. Camadas (visão técnica)

| Pacote | Responsabilidade |
|--------|------------------|
| `controller` | Entrada HTTP, sessão, redirects/forwards |
| `model` | Entidades (`Usuario`, `Meme`), DAOs e `Conexao` |
| `service` | Orquestração da busca (`MemeBuscaService`) e clientes Giphy/Imgflip |
| `filter` | Headers anti-cache em recursos e rotas |
| `views` | Páginas HTML servidas no WAR |

---

## 9. Telas

| Arquivo | Função |
|---------|--------|
| `login.html` | Entrada no sistema |
| `cadastro.html` | Nova conta |
| `home.html` | Feed, busca e download |
| `admin.html` | Cadastro de memes no catálogo |
| `download-ok.html` | Confirmação de download |
| `exclusao.html` | Confirmação de exclusão de conta |

---

## 10. Observações acadêmicas

- O visual e o tom das telas seguem a proposta lúdica do tema “memes”.
- Integrações externas (Giphy/Imgflip) dependem da disponibilidade das APIs e, no caso do Giphy, de uma chave válida.
- Credenciais de banco e chaves de API **não** devem ser commitadas.
- Arquivos sensíveis do IntelliJ (Data Sources) estão no `.gitignore`.

---

## 11. Licença / uso

Uso estritamente acadêmico. Conteúdos obtidos via APIs de terceiros permanecem sujeitos aos termos de cada provedor.
