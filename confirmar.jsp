<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Confirmar: confirmar.jsp?servico=1&data=2026-12-01&hora=09:00
    //   Abrir a página (GET)    -> mostra o resumo e o formulário
    //   Enviar o formulário (POST) -> confere os dados e grava o agendamento

    Map<String, String> servico = buscarServico(application, numero(request, "servico"));
    LocalDate data = dataDoCampo(request, "data");
    String hora = texto(request, "hora");
    if (servico == null || data == null || hora.isEmpty()) {
        response.sendRedirect("agenda.jsp");   // faltou alguma informação no endereço
        return;
    }
    String idServico = servico.get("id_servico");
    String titulo = "Confirmar agendamento";
    String pagina = "agenda";

    String nome = texto(request, "nome");
    String whatsapp = texto(request, "whatsapp");
    String celular = texto(request, "celular");
    String email = texto(request, "email");

    if (request.getMethod().equals("POST")) {
        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (whatsapp.isEmpty() && celular.isEmpty()) {
            erros.add("Informe o WhatsApp ou o celular.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }

        if (erros.isEmpty()) {
            // Endereço para voltar ao calendário com o aviso, se o horário já tiver sido ocupado
            String ocupado = "agendar.jsp?servico=" + idServico + "&data=" + data + "&indisponivel";

            // 1. O horário existe nesse dia e ainda tem vaga?
            Map<String, Integer> ocupacao = ocupacaoEntre(application, data, data);
            int profissionais = contarProfissionais(application);
            boolean livre = false;
            for (String horaDoDia : horariosDoDia(data)) {
                if (horaDoDia.equals(hora) && horarioLivre(data, hora, ocupacao, profissionais)) {
                    livre = true;
                }
            }
            if (!livre) {
                response.sendRedirect(ocupado);
                return;
            }

            // 2. Cadastra o cliente (ou atualiza, se o e-mail já existe)
            salvarCliente(application, nome, whatsapp, celular, email);
            Map<String, String> cliente = buscarCliente(application, email);

            // 3. Procura o primeiro profissional que não tem atendimento nesse dia e hora
            List<Map<String, String>> livres = consultar(application,
                    "SELECT * FROM profissional p WHERE NOT EXISTS (SELECT 1 FROM agendamento a "
                    + "WHERE a.id_profissional = p.id AND a.data = ? AND a.hora = ?) ORDER BY id LIMIT 1",
                    data.toString(), hora);
            if (livres.isEmpty()) {
                response.sendRedirect(ocupado);
                return;
            }
            Map<String, String> profissional = livres.get(0);

            // 4. Grava o agendamento
            try {
                executar(application, "INSERT INTO agendamento (id_cliente, id_profissional, id_servico, data, hora) "
                        + "VALUES (?, ?, ?, ?, ?)", cliente.get("id"), profissional.get("id"), idServico, data.toString(), hora);
            } catch (SQLIntegrityConstraintViolationException e) {
                response.sendRedirect(ocupado);   // outra pessoa agendou o mesmo horário no mesmo instante
                return;
            }

            // 5. Guarda o agendamento na sessão para a página de sucesso mostrar
            Map<String, String> agendamento = new HashMap<String, String>();
            agendamento.put("servico", servico.get("nome_servico"));
            agendamento.put("data", data.toString());
            agendamento.put("hora", hora);
            agendamento.put("profissional", profissional.get("nome_profissional"));
            session.setAttribute("ultimoAgendamento", agendamento);
            response.sendRedirect("sucesso.jsp");
            return;
        }
    } else if (usuarioNome != null) {
        // Abriu a página já logado: preenche com os dados da conta
        nome = usuarioNome;
        email = usuarioEmail;
    }
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <a href="agendar.jsp?servico=<%= idServico %>&amp;data=<%= data %>" class="voltar">← Trocar horário</a>
    <h1 class="titulo">Confirmar agendamento</h1>

    <form id="form-agendamento" method="post" action="confirmar.jsp" class="janela formulario" novalidate>
      <header class="janela-titulo">Agende sua Avaliação</header>

      <div class="formulario-corpo">
        <div class="resumo">
          <strong><%= html(servico.get("nome_servico")) %></strong>
          <span><%= dataPorExtenso(data.toString()) %> às <%= html(hora) %></span>
        </div>

        <%@ include file="/WEB-INF/includes/erros.jsp" %>

        <%-- Procedimento, data e hora vão junto com o formulário, escondidos --%>
        <input type="hidden" name="servico" value="<%= idServico %>">
        <input type="hidden" name="data" value="<%= data %>">
        <input type="hidden" name="hora" value="<%= html(hora) %>">

        <input type="text"  name="nome"     placeholder="Nome completo" aria-label="Nome completo" required maxlength="100"
               value="<%= html(nome) %>">
        <input type="tel"   name="whatsapp" placeholder="WhatsApp" aria-label="WhatsApp" maxlength="20"
               value="<%= html(whatsapp) %>">
        <input type="tel"   name="celular"  placeholder="Celular" aria-label="Celular" maxlength="20"
               value="<%= html(celular) %>">
        <input type="email" name="email"    placeholder="E-mail" aria-label="E-mail" required maxlength="150"
               value="<%= html(email) %>">

        <button type="submit" class="btn">Confirmar agendamento</button>
      </div>
    </form>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
