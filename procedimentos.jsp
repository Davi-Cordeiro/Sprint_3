<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Procedimentos: um card para cada procedimento
    String titulo = "Procedimentos";
    String pagina = "procedimentos";
    List<Map<String, String>> servicos = consultar(application, "SELECT * FROM servico ORDER BY id_servico");
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <h1 class="titulo">Procedimentos</h1>
    <p class="subtitulo">Clique em um procedimento para escolher o dia e o horário.</p>

    <%-- A área .card-midia mostra a inicial do procedimento (ex.: "L" de Limpeza de pele).
         Para usar fotos: crie a pasta img, coloque servico-1.jpg (2, 3...) nela e troque a
         <span class="card-inicial"> por <img src="img/servico-<%= servico.get("id_servico") %>.jpg" alt=""> --%>
    <div class="cards">
      <% for (Map<String, String> servico : servicos) { %>
        <article class="janela card">
          <a href="agendar.jsp?servico=<%= servico.get("id_servico") %>" class="card-link">
            <header class="janela-titulo"><%= html(servico.get("nome_servico")) %></header>
            <div class="card-midia">
              <span class="card-inicial"><%= html(servico.get("nome_servico").substring(0, 1)) %></span>
              <span class="card-preco"><%= preco(servico.get("preco")) %></span>
            </div>
          </a>

          <details>
            <summary>Mais informações</summary>
            <p><%= html(servico.get("descricao")) %></p>
          </details>

          <a href="agendar.jsp?servico=<%= servico.get("id_servico") %>" class="btn btn-pequeno card-agendar">Agendar</a>
        </article>
      <% } %>
    </div>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
