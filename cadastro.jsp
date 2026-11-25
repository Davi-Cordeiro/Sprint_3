<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Criar conta
    //   Abrir a página (GET)       -> formulário
    //   Enviar o formulário (POST) -> cria a conta e já entra nela
    String titulo = "Criar conta";
    String pagina = "conta";

    String nome = texto(request, "nome");
    String whatsapp = texto(request, "whatsapp");
    String email = texto(request, "email");
    String senha = texto(request, "senha");
    String confirmacao = texto(request, "confirmacao");

    if (request.getMethod().equals("POST")) {
        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (senha.length() < 6) {
            erros.add("A senha precisa ter pelo menos 6 caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            erros.add("As senhas não são iguais.");
        }
        if (erros.isEmpty()) {
            String senhaSalva = primeiroValor(application, "SELECT senha FROM cliente WHERE email = ?", email.toLowerCase());
            if (senhaSalva != null) {
                erros.add("Já existe uma conta com esse e-mail. Faça login.");
            }
        }

        if (erros.isEmpty()) {
            // Se a pessoa já agendou antes (cliente sem senha), aproveita o mesmo cadastro
            salvarCliente(application, nome, whatsapp, "", email);
            executar(application, "UPDATE cliente SET senha = ? WHERE email = ?", criptografarSenha(senha), email.toLowerCase());

            guardarLogin(request, buscarCliente(application, email));
            response.sendRedirect("conta.jsp");
            return;
        }
    }
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <a href="conta.jsp" class="voltar">← Voltar para o login</a>
    <h1 class="titulo">Criar conta</h1>

    <form id="form-cadastro" method="post" action="cadastro.jsp" class="janela formulario" novalidate>
      <header class="janela-titulo">Cadastro</header>
      <div class="formulario-corpo">
        <%@ include file="/WEB-INF/includes/erros.jsp" %>

        <input type="text"     name="nome"     placeholder="Nome completo" aria-label="Nome completo"
               required maxlength="100" autocomplete="name" value="<%= html(nome) %>">
        <input type="tel"      name="whatsapp" placeholder="WhatsApp (opcional)" aria-label="WhatsApp"
               maxlength="20" autocomplete="tel" value="<%= html(whatsapp) %>">
        <input type="email"    name="email"    placeholder="E-mail" aria-label="E-mail"
               required maxlength="150" autocomplete="email" value="<%= html(email) %>">
        <input type="password" name="senha"    placeholder="Senha (mínimo 6 caracteres)" aria-label="Senha"
               required minlength="6" maxlength="72" autocomplete="new-password">
        <input type="password" name="confirmacao" placeholder="Repita a senha" aria-label="Repita a senha"
               required minlength="6" maxlength="72" autocomplete="new-password">

        <p class="texto-suave dica">Já agendou com a gente? Use o mesmo e-mail para ver seus agendamentos.</p>

        <button type="submit" class="btn">Criar conta</button>
      </div>
    </form>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
