<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Agenda: lista de procedimentos para agendar, botão do WhatsApp e mapa
    String titulo = "Agenda";
    String pagina = "agenda";
    List<Map<String, String>> servicos = consultar(application, "SELECT * FROM servico ORDER BY id_servico");

    // Link do WhatsApp já com a mensagem escrita, e o mapa do Google Maps
    String linkWhatsapp = "https://wa.me/" + CLINICA_WHATSAPP + "?text=" + url("Olá! Gostaria de agendar uma avaliação.");
    String linkMapa = "https://www.google.com/maps?output=embed&q=" + url(CLINICA_MAPA);
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <h1 class="titulo">Agenda</h1>

    <section class="janela formulario">
      <header class="janela-titulo">Agende sua Avaliação</header>

      <div class="formulario-corpo">
        <p class="texto-suave">Escolha o procedimento para ver os dias e horários livres:</p>

        <nav class="lista-procedimentos">
          <% for (Map<String, String> servico : servicos) { %>
            <a href="agendar.jsp?servico=<%= servico.get("id_servico") %>"><%= html(servico.get("nome_servico")) %></a>
          <% } %>
        </nav>

        <a href="<%= linkWhatsapp %>" class="btn btn-whats" target="_blank" rel="noopener">Falar direto pelo WhatsApp</a>
      </div>
    </section>

    <div class="local">
      <iframe src="<%= linkMapa %>" class="mapa" loading="lazy" referrerpolicy="no-referrer-when-downgrade"
              title="Mapa da clínica"></iframe>
      <address>
        <strong>Endereço</strong><br>
        <%= CLINICA_ENDERECO %><br>
        <%= CLINICA_CIDADE %>
      </address>
    </div>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
