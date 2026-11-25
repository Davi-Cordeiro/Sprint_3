<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Sucesso: mostra o agendamento que acabou de ser feito (o confirmar.jsp guardou na sessão)
    String titulo = "Agendamento confirmado";
    String pagina = "agenda";
    Map<String, String> agendamento = (Map<String, String>) session.getAttribute("ultimoAgendamento");
    if (agendamento == null) {
        response.sendRedirect("agenda.jsp");
        return;
    }
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <section class="janela formulario sucesso-box">
      <header class="janela-titulo">Agendamento confirmado</header>
      <div class="formulario-corpo">
        <p class="sucesso-icone" aria-hidden="true">✓</p>
        <p><strong><%= html(agendamento.get("servico")) %></strong></p>
        <p><%= dataPorExtenso(agendamento.get("data")) %> às <%= html(agendamento.get("hora")) %></p>
        <p class="texto-suave">Com <%= html(agendamento.get("profissional")) %></p>

        <a href="conta.jsp" class="btn">Ver meus agendamentos</a>
        <a href="procedimentos.jsp" class="link">Agendar outro procedimento</a>
      </div>
    </section>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
