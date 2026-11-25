<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página inicial: textos da clínica + os 4 primeiros procedimentos
    String titulo = "Home";
    String pagina = "home";
    List<Map<String, String>> destaques = consultar(application, "SELECT * FROM servico ORDER BY id_servico LIMIT 4");
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <!-- Wireframe: caixa arredondada com o banner -->
    <section class="hero">
      <div class="hero-conteudo">
        <span class="hero-selo">Estética · Bem-estar · Autoestima</span>
        <h1><%= CLINICA_NOME %></h1>
        <p><%= CLINICA_DESCRICAO %></p>
        <a href="agenda.jsp" class="btn btn-destaque">Agende sua avaliação</a>
        <ul class="hero-info">
          <li><strong>Seg a Sex</strong> 9h às 18h</li>
          <li><strong>Sábado</strong> 9h às 13h</li>
          <li><%= CLINICA_ENDERECO %></li>
        </ul>
      </div>
    </section>

    <p class="chamada">Conheça nossos procedimentos</p>

    <!-- Wireframe: 4 janelas "Procedimentos" -->
    <div class="destaques">
      <% for (int i = 0; i < destaques.size(); i++) {
             Map<String, String> servico = destaques.get(i); %>
        <a href="agendar.jsp?servico=<%= servico.get("id_servico") %>" class="janela">
          <span class="janela-titulo">Procedimentos</span>
          <span class="janela-corpo">
            <span class="destaque-numero">0<%= i + 1 %></span>
            <strong class="destaque-nome"><%= html(servico.get("nome_servico")) %></strong>
            <span class="destaque-preco"><%= preco(servico.get("preco")) %></span>
            <span class="destaque-acao">Agendar →</span>
          </span>
        </a>
      <% } %>
    </div>
    <p class="ver-todos"><a href="procedimentos.jsp">Ver todos os procedimentos →</a></p>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
