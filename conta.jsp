<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Minha conta
    //   Abrir a página (GET)          -> formulário de login, ou os agendamentos de quem já entrou
    //   Enviar o login (POST)         -> email e senha
    //   Botão Sair (POST, acao=sair)  -> sai da conta
    String titulo = "Minha conta";
    String pagina = "conta";
    String email = texto(request, "email");

    if (request.getMethod().equals("POST")) {
        if (texto(request, "acao").equals("sair")) {
            session.invalidate();
            response.sendRedirect("index.jsp");
            return;
        }

        // Entrar: confere a senha digitada com a senha (criptografada) salva no banco
        String senhaSalva = primeiroValor(application, "SELECT senha FROM cliente WHERE email = ?", email.toLowerCase());
        if (senhaConfere(texto(request, "senha"), senhaSalva)) {
            guardarLogin(request, buscarCliente(application, email));
            response.sendRedirect("conta.jsp");
            return;
        }
        erros.add("E-mail ou senha incorretos.");
    }

    // Agendamentos de quem já entrou
    List<Map<String, String>> agendamentos = new ArrayList<Map<String, String>>();
    if (usuarioId != null) {
        agendamentos = consultar(application,
                "SELECT a.data, TIME_FORMAT(a.hora, '%H:%i') AS hora, s.nome_servico, p.nome_profissional "
                + "FROM agendamento a "
                + "JOIN servico s ON s.id_servico = a.id_servico "
                + "JOIN profissional p ON p.id = a.id_profissional "
                + "WHERE a.id_cliente = ? ORDER BY a.data DESC, a.hora DESC",
                usuarioId);
    }
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <h1 class="titulo">Minha conta</h1>

    <% if (usuarioId == null) { %>
      <!-- Ninguém entrou: formulário de login -->
      <form id="form-login" method="post" action="conta.jsp" class="janela formulario" novalidate>
        <header class="janela-titulo">Entrar</header>
        <div class="formulario-corpo">
          <%@ include file="/WEB-INF/includes/erros.jsp" %>

          <input type="email" name="email" placeholder="E-mail" aria-label="E-mail"
                 required maxlength="150" autocomplete="email" value="<%= html(email) %>">
          <input type="password" name="senha" placeholder="Senha" aria-label="Senha"
                 required autocomplete="current-password">

          <button type="submit" class="btn">Entrar</button>

          <p class="texto-suave">
            Ainda não tem conta? <a href="cadastro.jsp" class="link">Criar conta</a>
          </p>
        </div>
      </form>

    <% } else { %>
      <!-- Já entrou: dados e agendamentos -->
      <section class="conta">
        <div class="conta-topo">
          <div>
            <h2>Olá, <%= html(primeiroNome(usuarioNome)) %>!</h2>
            <p class="texto-suave"><%= html(usuarioEmail) %></p>
          </div>
          <form method="post" action="conta.jsp">
            <input type="hidden" name="acao" value="sair">
            <button type="submit" class="btn btn-pequeno">Sair</button>
          </form>
        </div>

        <div class="janela">
          <header class="janela-titulo">Meus agendamentos</header>
          <% if (agendamentos.isEmpty()) { %>
            <p class="vazio">
              Você ainda não tem agendamentos. <a href="procedimentos.jsp">Agendar agora</a>
            </p>
          <% } else { %>
            <table class="tabela">
              <thead>
                <tr><th>Data</th><th>Hora</th><th>Procedimento</th><th>Profissional</th></tr>
              </thead>
              <tbody>
                <% for (Map<String, String> agendamento : agendamentos) { %>
                  <tr>
                    <td><%= dataCurta(agendamento.get("data")) %></td>
                    <td><%= agendamento.get("hora") %></td>
                    <td><%= html(agendamento.get("nome_servico")) %></td>
                    <td><%= html(agendamento.get("nome_profissional")) %></td>
                  </tr>
                <% } %>
              </tbody>
            </table>
          <% } %>
        </div>
      </section>
    <% } %>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
