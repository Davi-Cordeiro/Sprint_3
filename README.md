# Clínica de Estética — Sprint 3

Site de uma clínica de estética feito com **Java (Spring Boot)**, **MySQL/SQL**, **HTML**, **CSS** e **JavaScript**.

- **HTML + CSS + JavaScript** (`src/main/resources/static`): as páginas. Cada página tem um `.js` que busca os dados no Java com `fetch()`.
- **Java** (`src/main/java`): recebe os pedidos do JavaScript em `/api/...`, aplica as regras (agenda, login) e responde em JSON.
- **SQL / MySQL** (`database/banco.sql` e `ClinicaRepository.java`): onde os dados ficam guardados.

**Login de teste:** `mariana@exemplo.com` / senha `123456` (ou crie uma conta em "Minha conta").

Páginas: **Home**, **Procedimentos**, **Agenda** (mapa e lista de procedimentos), **Avaliações** e **Minha conta** (ícone de login).

**Como funciona o agendamento:** a pessoa clica em um procedimento → escolhe o dia no calendário → escolhe um horário livre →
informa nome, WhatsApp/celular e e-mail → pronto. O sistema escolhe sozinho um profissional livre naquele horário.
Atendimento: segunda a sexta 9h–18h (pausa 12h–13h), sábado 9h–13h, domingo fechado.

---

## Como rodar

Você só precisa instalar **duas coisas**: o **Java (JDK 17 ou mais novo)** e o **MySQL**.
O Maven é baixado sozinho e as tabelas do banco são criadas sozinhas na primeira vez.

### 1. Instalar o Java (JDK)

- **Windows / Mac:** baixe o **JDK 21** em https://adoptium.net e instale.
  No Windows, durante a instalação marque **"Set JAVA_HOME variable"**.
- **Linux (Ubuntu/Debian):** `sudo apt install openjdk-21-jdk`

### 2. Instalar o MySQL

- **Windows:** baixe o **MySQL Installer** em https://dev.mysql.com/downloads/installer/,
  escolha **"Server only"** e, quando pedir, crie uma **senha para o usuário root** (anote!).
- **Mac:** `brew install mysql && brew services start mysql`
- **Linux (Ubuntu/Debian):**
  ```bash
  sudo apt install mysql-server
  sudo mysql -e "ALTER USER 'root'@'localhost' IDENTIFIED WITH caching_sha2_password BY 'root';"
  ```
  (o segundo comando define a senha `root` para o usuário root)

### 3. Colocar a senha do MySQL no projeto

Abra o arquivo **`mysql.properties`** (na pasta do projeto) e coloque a senha do root do MySQL desse computador:

```properties
mysql.usuario=root
mysql.senha=SUA_SENHA_AQUI
```

### 4. Iniciar

- **Windows:** dois cliques em **`iniciar.bat`**
- **Linux / Mac:** no terminal, dentro da pasta, rode **`./iniciar.sh`**

O navegador abre sozinho em **http://localhost:8080**.
A primeira vez demora alguns minutos (baixa as dependências). Nas próximas, sobe em segundos.

**Para parar:** `Ctrl + C` no terminal (ou feche a janela).

---

## Problemas comuns

| Mensagem | Solução |
|---|---|
| `Java (JDK) não encontrado` | Instale o JDK (passo 1) e abra o terminal de novo. |
| `o MySQL não está rodando` | Instale o MySQL (passo 2) ou inicie o serviço: Windows → serviço **MySQL80**; Linux → `sudo systemctl start mysql`. |
| `NÃO FOI POSSÍVEL CONECTAR AO MYSQL` / `Access denied` | A senha no `mysql.properties` está errada (passo 3). |
| `Port 8080 was already in use` | Outra coisa está usando a porta. Linux/Mac: `PORTA=8081 ./iniciar.sh`. Windows: `set PORTA=8081` e depois `iniciar.bat`. |

---

## Banco de dados

- Na primeira vez que o site inicia, ele cria o banco **`clinica_estetica`** e roda o **`database/banco.sql`** (tabelas + dados de exemplo).
- Nas próximas vezes os dados são mantidos.
- **Para recriar o banco do zero:** apague o banco (`DROP DATABASE clinica_estetica;` no MySQL Workbench ou no terminal) e inicie o site de novo.
- Também dá para rodar o `database/banco.sql` direto no MySQL Workbench.

Tabelas: `cliente`, `profissional`, `servico`, `agendamento` e `avaliacao`, seguindo o diagrama do projeto.
Além do diagrama, foram incluídos:
- `whatsapp`, `celular` e `email` em `cliente` (pedidos na confirmação do agendamento);
- `hora` em `agendamento` (o calendário agenda por horário), com uma regra que impede o mesmo profissional de ter dois atendimentos no mesmo horário;
- `comentario` em `avaliacao` (texto exibido na página de avaliações).

---

## Onde mudar as coisas

| O que | Arquivo |
|---|---|
| Usuário/senha/porta do MySQL | `mysql.properties` |
| Nome, descrição, WhatsApp, endereço e mapa da clínica | `src/main/resources/application.yml` |
| Horários de atendimento e quantos meses dá para agendar | `src/main/java/br/com/clinica/service/AgendaService.java` |
| Procedimentos, preços e profissionais (dados iniciais) | `database/banco.sql` |
| Tabelas e dados iniciais | `database/banco.sql` |
| Páginas (HTML) | `src/main/resources/templates/` |
| Estilo (CSS) | `src/main/resources/static/css/style.css` |
| Fotos | `src/main/resources/static/img/` |
| Código Java (rotas e SQL) | `src/main/java/br/com/clinica/` |

## Estrutura

```
Sprint_3/
├── database/banco.sql            ← criação das tabelas + dados de exemplo
├── src/main/java/br/com/clinica/
│   ├── controller/               ← páginas e formulários
│   ├── repository/               ← consultas SQL ao MySQL
│   ├── model/                    ← classes de dados e formulários
│   └── config/                   ← dados da clínica e criação automática do banco
├── src/main/resources/
│   ├── templates/                ← páginas HTML
│   ├── static/css/style.css      ← CSS
│   └── application.yml           ← configurações
├── mysql.properties              ← usuário e senha do MySQL
├── mvnw / mvnw.cmd               ← baixa o Maven sozinho
├── iniciar.sh                    ← iniciar no Linux/Mac
└── iniciar.bat                   ← iniciar no Windows
```

## Abrir no IntelliJ / VS Code / Eclipse

Abra a pasta `Sprint_3` como projeto Maven e rode a classe `br.com.clinica.ClinicaApplication`.
O MySQL precisa estar rodando e o `mysql.properties` configurado.
# Sprint_3
