# Clínica Bella Estética — Sprint 3

Site de uma clínica de estética: procedimentos, agendamento online com calendário, avaliações de clientes e
área do cliente com login.

Feito com **JSP**, **HTML**, **CSS**, **JavaScript** e **MariaDB/MySQL** (o banco do XAMPP),
rodando no **Apache Tomcat 9**.

---

# Como rodar (qualquer computador)

Esta pasta **é o site**: basta colocá-la dentro do `webapps` do Tomcat. Não precisa compilar nada e o banco
se cria sozinho na primeira vez.

## Passo 1 — Instalar (só na primeira vez)

| Programa | Onde baixar | Atenção |
|---|---|---|
| **XAMPP** | https://www.apachefriends.org | Na instalação, deixe marcados **Apache**, **MySQL**, **Tomcat** e **phpMyAdmin**. |
| **Java** (JDK 17 ou 21) | https://adoptium.net | No Windows, marque **"Set JAVA_HOME variable"**. Sem isso o Tomcat do XAMPP não liga. |

## Passo 2 — Ligar os programas

Abra o **XAMPP Control Panel** e clique em **Start** nesta ordem:

1. **MySQL** (o banco — precisa ligar antes do Tomcat)
2. **Apache** (só para abrir o phpMyAdmin)
3. **Tomcat** (roda o site)

Os três devem ficar verdes. (No XAMPP o MariaDB aparece com o nome "MySQL".)

## Passo 3 — Colocar o site no Tomcat

Copie **esta pasta inteira** para dentro de `C:\xampp\tomcat\webapps\` e renomeie para **`clinica`**:

```
C:\xampp\tomcat\webapps\clinica\
    ├── index.jsp, procedimentos.jsp, ...
    ├── css\  js\
    └── WEB-INF\
```

O nome da pasta é o endereço do site (`/clinica/`). Confira se o `WEB-INF` está **direto** dentro de
`clinica` (e não em `clinica\Sprint_3\WEB-INF`).

## Passo 4 — Abrir

| O quê | Endereço |
|---|---|
| **Site** | **http://localhost:8080/clinica/** |
| **phpMyAdmin** (ver as tabelas) | http://localhost/phpmyadmin → banco `clinica_estetica` |

Pronto! O banco `clinica_estetica` é criado automaticamente com os procedimentos e as avaliações de exemplo.

**Login de teste:** `mariana@exemplo.com` / senha `123456`

---

## Se não funcionar

| O que acontece | O que fazer |
|---|---|
| O Tomcat não fica verde no XAMPP | Falta o Java ou o `JAVA_HOME`. Reinstale o Java marcando **"Set JAVA_HOME variable"** e reinicie o computador. |
| Erro 500 ao abrir as páginas | Leia a mensagem na tela. Se falar do banco: o MySQL está desligado **ou** o seu MySQL tem senha (veja abaixo). |
| "A porta 8080 já está em uso" | Outro programa está usando a porta. Feche-o ou reinicie o computador. |
| A página não abre (erro 404) | Confira se a pasta se chama `clinica`, se está direto em `C:\xampp\tomcat\webapps\` e se o endereço termina com `/clinica/`. |
| Erro com "jakarta" nos logs | O Tomcat é 10 ou mais novo. Use o **Tomcat 9** (ou o do XAMPP). |

**O seu MySQL tem senha?** (o do XAMPP não tem, então normalmente não precisa)
Abra `C:\xampp\tomcat\webapps\clinica\WEB-INF\banco.properties` no Bloco de Notas, coloque a senha depois de
`senha=` e reinicie o Tomcat (Stop e Start).

**Ver as mensagens do site:** no XAMPP, botão **Logs** do Tomcat. As mensagens do site começam com `[Clínica]`.

---

# Como o site funciona (só JSP)

Cada página é um arquivo **`.jsp`**: HTML com pedaços de Java dentro. O **começo** de cada página busca os
dados no banco (e, nas páginas com formulário, confere e grava o que foi enviado). O **resto** monta o HTML.

```
Navegador ──pede procedimentos.jsp──► procedimentos.jsp ──SELECT──► Banco (MySQL)
                                             │
Navegador ◄────────── HTML pronto ───────────┘  (monta o HTML com <% for ... %>)
```

Não tem nada para compilar: o **Tomcat compila os `.jsp` sozinho** na primeira vez que cada página é aberta.

| Página | O que faz |
|---|---|
| `index.jsp` | Página inicial: banner da clínica e 4 procedimentos em destaque |
| `procedimentos.jsp` | Um card para cada procedimento |
| `agenda.jsp` | Lista de procedimentos, WhatsApp e mapa |
| `agendar.jsp?servico=1` | Calendário do mês e horários do dia escolhido |
| `confirmar.jsp` | Formulário (nome, telefone, e-mail) e grava o agendamento |
| `sucesso.jsp` | Agendamento confirmado |
| `avaliacoes.jsp` | Média das notas, avaliações e formulário para avaliar |
| `cadastro.jsp` | Criar conta |
| `conta.jsp` | Login, meus agendamentos e sair |

- Os formulários enviam os dados (POST) para a **própria página**. Se tiver erro, a página aparece de novo com a
  lista de erros; se der certo, vai para a próxima página.
- Tudo o que se repete fica em `WEB-INF/includes/` e entra nas páginas com `<%@ include file="..." %>`:
  - `funcoes.jsp` — dados da clínica, conexão com o banco, regras da agenda, formatação e senha
  - `topo.jsp` e `rodape.jsp` — menu e rodapé
  - `erros.jsp` — lista de erros dos formulários
- O JavaScript (`js/script.js`) só confere os campos dos formulários antes de enviar, faz a máscara do telefone
  e conta os caracteres do comentário. O JSP confere tudo de novo quando os dados chegam.
- Na primeira vez que alguém abre o site, o `funcoes.jsp` cria o banco sozinho (rodando o `WEB-INF/banco.sql`).

---

# Para quem for mexer no código

Mudou qualquer arquivo (`.jsp`, CSS ou JavaScript)? É só salvar e atualizar a página no navegador (F5).

## Estrutura

```
clinica/                     ← esta pasta (vai inteira para o webapps do Tomcat)
├── index.jsp                ← páginas do site
├── procedimentos.jsp, agenda.jsp, agendar.jsp, confirmar.jsp,
├── sucesso.jsp, avaliacoes.jsp, cadastro.jsp, conta.jsp
├── css/style.css            ← visual
├── js/script.js             ← confere os formulários antes de enviar
└── WEB-INF/                 ← parte privada (o navegador não acessa)
    ├── includes/            ← partes usadas por todas as páginas
    │   ├── funcoes.jsp      ← banco de dados, agenda, formatação, senha e dados da clínica
    │   ├── topo.jsp         ← <head> e menu
    │   ├── rodape.jsp       ← rodapé
    │   └── erros.jsp        ← lista de erros dos formulários
    ├── lib/                 ← driver do MariaDB (.jar) — liga o Java ao banco, não apague
    ├── banco.sql            ← criação das tabelas + dados de exemplo
    └── banco.properties     ← usuário e senha do banco
```

## Onde mudar as coisas

| O que | Arquivo |
|---|---|
| Usuário e senha do banco | `WEB-INF/banco.properties` |
| Nome, descrição, WhatsApp, endereço e mapa da clínica | `WEB-INF/includes/funcoes.jsp` (parte 1) |
| Horários de atendimento e quantos meses dá para agendar | `WEB-INF/includes/funcoes.jsp` (parte 3) |
| Tabelas, procedimentos, preços e profissionais | `WEB-INF/banco.sql` |
| Páginas | os `.jsp` da pasta principal |
| Menu e rodapé | `WEB-INF/includes/topo.jsp` e `WEB-INF/includes/rodape.jsp` |
| Validação dos formulários na tela | `js/script.js` |
| Visual | `css/style.css` |

## Banco de dados

O site usa o **MariaDB que vem no XAMPP** (no painel ele aparece como "MySQL"), com usuário `root` e sem senha.

Tabelas: `cliente`, `profissional`, `servico`, `agendamento` e `avaliacao`.

- **Para ver os dados:** no XAMPP, dê Start também no **Apache** e abra **http://localhost/phpmyadmin** → banco `clinica_estetica`.
- O banco é criado sozinho na primeira vez que alguém abre o site (a partir do `WEB-INF/banco.sql`).
- **Para recriar do zero:** no phpMyAdmin, **Importar** → escolha o `WEB-INF/banco.sql` → **Executar**
  (apaga tudo e volta aos dados de exemplo).
