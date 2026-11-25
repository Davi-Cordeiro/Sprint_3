<%--
  Início de todas as páginas: <head> e menu lateral.
  Antes de incluir este arquivo, a página cria as variáveis:
    titulo -> texto da aba do navegador (ex.: "Procedimentos")
    pagina -> item do menu que fica destacado ("home", "procedimentos", "agenda", "avaliacoes" ou "conta")
--%>
<%@ page pageEncoding="UTF-8" %>
<%
    String textoLogin = "Entrar";
    if (usuarioNome != null) {
        textoLogin = "Olá, " + primeiroNome(usuarioNome);
    }
%>
<!DOCTYPE html>
<html lang="pt-BR">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <link rel="icon" href="data:,">
  <title><%= html(titulo) %> | <%= CLINICA_NOME %></title>
  <link rel="stylesheet" href="https://fonts.googleapis.com/css2?family=Playfair+Display:wght@600;700&display=swap">
  <link rel="stylesheet" href="css/style.css">
</head>
<body>
  <aside class="sidebar">
    <a href="conta.jsp" class="login <%= pagina.equals("conta") ? "ativo" : "" %>" title="Login da conta">
      <svg viewBox="0 0 64 64" aria-hidden="true">
        <circle cx="32" cy="22" r="12" />
        <path d="M10 60c0-12 10-20 22-20s22 8 22 20" />
        <path d="M19 22c0-9 6-15 13-15s13 6 13 15" />
      </svg>
      <span class="sr-only">Login da conta</span>
    </a>
    <span class="login-nome"><%= html(textoLogin) %></span>

    <nav class="menu">
      <a href="index.jsp" class="<%= pagina.equals("home") ? "ativo" : "" %>">Home</a>
      <a href="procedimentos.jsp" class="<%= pagina.equals("procedimentos") ? "ativo" : "" %>">Procedimentos</a>
      <a href="agenda.jsp" class="<%= pagina.equals("agenda") ? "ativo" : "" %>">Agenda</a>
      <a href="avaliacoes.jsp" class="<%= pagina.equals("avaliacoes") ? "ativo" : "" %>">Avaliações</a>
    </nav>
  </aside>
