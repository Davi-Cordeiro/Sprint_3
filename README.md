# Clínica Bella Estética — Sprint 3

Site de uma clínica de estética: procedimentos, agendamento online com calendário, avaliações de clientes e
área do cliente com login.

Feito com **HTML**, **CSS**, **JavaScript**, **Java (Servlets)** e **MySQL**, rodando no **Apache Tomcat**.

---

# Como rodar (qualquer computador)

O site inteiro está em **um arquivo só: `clinica.war`**. Não precisa compilar, nem criar o banco: ele se
cria sozinho na primeira vez.

## Passo 1 — Instalar (só na primeira vez)

| Programa | Onde baixar | Atenção |
|---|---|---|
| **XAMPP** | https://www.apachefriends.org | Na instalação, deixe marcados **Apache**, **MySQL** e **Tomcat**. |
| **Java** (JDK 17 ou 21) | https://adoptium.net | No Windows, marque **"Set JAVA_HOME variable"**. Sem isso o Tomcat do XAMPP não liga. |

## Passo 2 — Ligar o MySQL e o Tomcat

Abra o **XAMPP Control Panel** e clique em **Start** no **MySQL** e no **Tomcat**.
Os dois devem ficar verdes.

## Passo 3 — Colocar o site no Tomcat

Copie o arquivo **`clinica.war`** (que está nesta pasta) para:

```
C:\xampp\tomcat\webapps\
```

Espere uns 10 segundos: o Tomcat abre o arquivo sozinho e cria a pasta `clinica`.

## Passo 4 — Abrir o site

**http://localhost:8080/clinica/**

Pronto! O banco `clinica_estetica` é criado automaticamente com os procedimentos e as avaliações de exemplo.

**Login de teste:** `mariana@exemplo.com` / senha `123456`

---

## Se não funcionar

| O que acontece | O que fazer |
|---|---|
| O Tomcat não fica verde no XAMPP | Falta o Java ou o `JAVA_HOME`. Reinstale o Java marcando **"Set JAVA_HOME variable"** e reinicie o computador. |
| A página abre, mas os procedimentos não aparecem | O MySQL está desligado (Passo 2) **ou** o seu MySQL tem senha (veja abaixo). Depois, pare e ligue o Tomcat de novo. |
| "A porta 8080 já está em uso" | Outro programa está usando a porta. Feche-o ou reinicie o computador. |
| A página não abre (erro 404) | Confira se o `clinica.war` está dentro de `C:\xampp\tomcat\webapps\` e se o endereço termina com `/clinica/`. |

**O seu MySQL tem senha?** (o do XAMPP não tem, então normalmente não precisa)
Abra `C:\xampp\tomcat\webapps\clinica\WEB-INF\banco.properties` no Bloco de Notas, coloque a senha depois de
`senha=` e reinicie o Tomcat (Stop e Start).

**Ver as mensagens do site:** no XAMPP, botão **Logs** do Tomcat. As mensagens do site começam com `[Clínica]`.

---

## Sem XAMPP (Linux / Mac / Tomcat instalado à parte)

Precisa de **Tomcat 8.5 ou 9** e **MySQL** (ou MariaDB) ligados.

1. Copie `clinica.war` para a pasta `webapps` do Tomcat.
2. Se o MySQL tiver senha, depois que a pasta `webapps/clinica` aparecer, edite
   `webapps/clinica/WEB-INF/banco.properties` e reinicie o Tomcat.
3. Abra **http://localhost:8080/clinica/**

---

# Para quem for mexer no código

Depois de alterar qualquer arquivo (Java, HTML, CSS ou JavaScript), gere o `clinica.war` de novo:

- **Windows:** dois cliques em `compilar.bat` (procura o Tomcat em `C:\xampp\tomcat`)
- **Linux / Mac:** `./compilar.sh /caminho/do/tomcat`

Para compilar é preciso o **JDK** (o `javac`). Depois, copie o novo `clinica.war` para o `webapps` do Tomcat.

## Estrutura

```
Sprint_3/
├── clinica.war                 ← O SITE PRONTO (é só este arquivo que vai para o Tomcat)
├── compilar.bat / compilar.sh  ← geram o clinica.war de novo depois de mudar o código
├── src/br/com/clinica/         ← código Java
│   ├── controller/             ← Servlets (api/...), regras da agenda, JSON e senha
│   ├── model/                  ← classes de dados (Servico, Cliente, Horario...)
│   └── repository/             ← conexão com o MySQL, SQL e criação automática do banco
└── web/                        ← conteúdo do site
    ├── *.html                  ← páginas
    ├── css/style.css           ← visual
    ├── js/                     ← JavaScript: validação dos formulários e busca dos dados
    └── WEB-INF/
        ├── banco.sql           ← criação das tabelas + dados de exemplo
        ├── banco.properties    ← usuário e senha do MySQL
        ├── lib/                ← driver do MySQL (.jar)
        └── classes/            ← Java compilado
```

## Onde mudar as coisas

| O que | Arquivo |
|---|---|
| Usuário e senha do MySQL | `web/WEB-INF/banco.properties` |
| Nome, descrição, WhatsApp, endereço e mapa da clínica | `src/br/com/clinica/controller/ClinicaServlet.java` |
| Horários de atendimento e quantos meses dá para agendar | `src/br/com/clinica/controller/AgendaService.java` |
| Tabelas, procedimentos, preços e profissionais | `web/WEB-INF/banco.sql` |
| Páginas | `web/*.html` |
| Validação dos formulários e comportamento das páginas | `web/js/` |
| Visual | `web/css/style.css` |

## Banco de dados

Tabelas: `cliente`, `profissional`, `servico`, `agendamento` e `avaliacao`.

- O banco é criado sozinho na primeira vez que o Tomcat liga o site (a partir do `WEB-INF/banco.sql`).
- **Para recriar do zero:** apague o banco (`DROP DATABASE clinica_estetica;` no phpMyAdmin) e reinicie o Tomcat.
- Também dá para importar o `web/WEB-INF/banco.sql` direto pelo phpMyAdmin.
