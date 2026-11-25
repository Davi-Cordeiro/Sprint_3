<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Agendar: calendário do mês + horários do dia escolhido.
    //   agendar.jsp?servico=1                   -> mês atual
    //   agendar.jsp?servico=1&mes=2027-01       -> outro mês (setas do calendário)
    //   agendar.jsp?servico=1&data=2026-12-01   -> dia escolhido (mostra os horários)
    //   ...&indisponivel                        -> aviso "esse horário acabou de ser ocupado"

    Map<String, String> servico = buscarServico(application, numero(request, "servico"));
    if (servico == null) {
        response.sendRedirect("procedimentos.jsp");   // procedimento não existe: volta para a lista
        return;
    }
    String idServico = servico.get("id_servico");
    String titulo = "Agendar " + servico.get("nome_servico");
    String pagina = "agenda";
    String endereco = "agendar.jsp?servico=" + idServico;   // começo dos links desta página

    // Dia escolhido (null se ainda não escolheu)
    LocalDate data = dataDoCampo(request, "data");

    // Qual mês mostrar: o do dia escolhido, o que veio no endereço ou o mês atual
    YearMonth primeiroMes = YearMonth.now();
    YearMonth ultimoMes = ultimoMesLiberado();
    YearMonth mes = primeiroMes;
    if (data != null) {
        mes = YearMonth.from(data);
    } else {
        try {
            mes = YearMonth.parse(texto(request, "mes"));   // "2026-12"
        } catch (Exception e) {
            // sem mês ou mês inválido: fica o mês atual
        }
    }
    // Não deixa sair do período liberado
    if (mes.isBefore(primeiroMes)) {
        mes = primeiroMes;
    }
    if (mes.isAfter(ultimoMes)) {
        mes = ultimoMes;
    }

    // Busca no banco quantos atendimentos já existem em cada horário do mês
    Map<String, Integer> ocupacao = ocupacaoEntre(application, mes.atDay(1), mes.atEndOfMonth());
    int profissionais = contarProfissionais(application);

    boolean indisponivel = request.getParameter("indisponivel") != null;
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <a href="procedimentos.jsp" class="voltar">← Procedimentos</a>
    <h1 class="titulo"><%= html(servico.get("nome_servico")) %></h1>

    <% if (indisponivel) { %>
      <p class="alerta erro">Esse horário acabou de ser ocupado. Escolha outro, por favor.</p>
    <% } %>

    <div class="agendar">
      <!-- ===== Calendário ===== -->
      <section class="janela calendario">
        <header class="calendario-topo">
          <%-- Setas: desligadas no primeiro e no último mês liberado --%>
          <% if (mes.equals(primeiroMes)) { %>
            <button type="button" class="calendario-nav" aria-label="Mês anterior" disabled>‹</button>
          <% } else { %>
            <a href="<%= endereco %>&amp;mes=<%= mes.minusMonths(1) %>" class="calendario-nav" aria-label="Mês anterior">‹</a>
          <% } %>

          <strong><%= mesPorExtenso(mes) %></strong>

          <% if (mes.equals(ultimoMes)) { %>
            <button type="button" class="calendario-nav" aria-label="Próximo mês" disabled>›</button>
          <% } else { %>
            <a href="<%= endereco %>&amp;mes=<%= mes.plusMonths(1) %>" class="calendario-nav" aria-label="Próximo mês">›</a>
          <% } %>
        </header>

        <table>
          <thead>
            <tr><th>Dom</th><th>Seg</th><th>Ter</th><th>Qua</th><th>Qui</th><th>Sex</th><th>Sáb</th></tr>
          </thead>
          <tbody>
            <tr>
            <%
                // Uma linha por semana, de domingo a sábado.
                // Quadrados vazios antes do dia 1 (getValue: 1 = segunda ... 7 = domingo; "% 7" faz domingo virar 0)
                int vazios = mes.atDay(1).getDayOfWeek().getValue() % 7;
                int coluna = 0;
                for (int i = 0; i < vazios; i++) {
                    coluna++;
            %>
                <td></td>
            <%  }

                for (int dia = 1; dia <= mes.lengthOfMonth(); dia++) {
                    LocalDate esseDia = mes.atDay(dia);

                    if (diaTemVaga(esseDia, ocupacao, profissionais)) {
                        String classe = "dia livre";
                        if (esseDia.equals(data)) {
                            classe = "dia livre selecionado";
                        }
            %>
                <td><a href="<%= endereco %>&amp;data=<%= esseDia %>" class="<%= classe %>"><%= dia %></a></td>
            <%      } else { %>
                <td><span class="dia indisponivel"><%= dia %></span></td>
            <%      }

                    // Fim da semana (sábado): começa uma linha nova
                    coluna++;
                    if (coluna == 7 && dia < mes.lengthOfMonth()) {
                        coluna = 0;
            %>
            </tr><tr>
            <%      }
                }
            %>
            </tr>
          </tbody>
        </table>
      </section>

      <!-- ===== Horários do dia escolhido ===== -->
      <section class="janela horarios">
        <% if (data == null) { %>
          <header class="janela-titulo">Horários disponíveis</header>
          <p class="horarios-dica">Escolha um dia no calendário.</p>
        <% } else { %>
          <header class="janela-titulo"><%= dataPorExtenso(data.toString()) %></header>
          <div class="horarios-lista">
            <% for (String hora : horariosDoDia(data)) { %>
              <% if (horarioLivre(data, hora, ocupacao, profissionais)) { %>
                <a href="confirmar.jsp?servico=<%= idServico %>&amp;data=<%= data %>&amp;hora=<%= hora %>"
                   class="horario"><%= hora %></a>
              <% } else { %>
                <span class="horario ocupado"><%= hora %></span>
              <% } %>
            <% } %>
          </div>
        <% } %>
      </section>
    </div>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
