<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ include file="/WEB-INF/includes/funcoes.jsp" %>
<%
    // Página Avaliações
    //   Abrir a página (GET)       -> média das notas, lista de avaliações e formulário
    //   Enviar o formulário (POST) -> grava uma avaliação nova
    String titulo = "Avaliações";
    String pagina = "avaliacoes";

    String nome = texto(request, "nome");
    String email = texto(request, "email");
    String comentario = texto(request, "comentario");
    int idServico = numero(request, "idServico");
    int nota = numero(request, "nota");

    if (request.getMethod().equals("POST")) {
        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (buscarServico(application, idServico) == null) {
            erros.add("Escolha o procedimento que você fez.");
        }
        if (nota < 1 || nota > 5) {
            erros.add("Escolha uma nota de 1 a 5 estrelas.");
        }
        if (comentario.length() > 500) {
            erros.add("O comentário pode ter no máximo 500 caracteres.");
        }

        if (erros.isEmpty()) {
            // Se a pessoa ainda não é cliente, cadastra só com nome e e-mail
            Map<String, String> cliente = buscarCliente(application, email);
            if (cliente == null) {
                salvarCliente(application, nome, "", "", email);
                cliente = buscarCliente(application, email);
            }

            String comentarioSalvo = comentario;
            if (comentarioSalvo.isEmpty()) {
                comentarioSalvo = null;   // comentário em branco é salvo como NULL
            }
            executar(application, "INSERT INTO avaliacao (nota, comentario, id_servico, id_cliente) VALUES (?, ?, ?, ?)",
                    nota, comentarioSalvo, idServico, cliente.get("id"));

            response.sendRedirect("avaliacoes.jsp?sucesso");   // recarrega a página com a mensagem de obrigado
            return;
        }
    } else if (usuarioNome != null) {
        // Abriu a página já logado: preenche com os dados da conta
        nome = usuarioNome;
        email = usuarioEmail;
    }

    // Dados para mostrar na página
    List<Map<String, String>> avaliacoes = consultar(application,
            "SELECT c.nome_cliente, s.nome_servico, av.nota, av.comentario FROM avaliacao av "
            + "JOIN cliente c ON c.id = av.id_cliente "
            + "JOIN servico s ON s.id_servico = av.id_servico "
            + "ORDER BY av.id DESC");
    List<Map<String, String>> servicos = consultar(application, "SELECT * FROM servico ORDER BY id_servico");
    String media = primeiroValor(application, "SELECT AVG(nota) FROM avaliacao");   // null se não há avaliações

    // Quantas avaliações têm cada nota (posição 1 = nota 1 ... posição 5 = nota 5)
    int[] quantidadePorNota = new int[6];
    for (Map<String, String> avaliacao : avaliacoes) {
        quantidadePorNota[Integer.parseInt(avaliacao.get("nota"))]++;
    }
%>
<%@ include file="/WEB-INF/includes/topo.jsp" %>

  <main>
    <h1 class="titulo">Algumas avaliações sobre a clínica</h1>

    <%-- Resumo das notas: média grande + uma barra para cada nota (5 a 1) --%>
    <% if (media != null) {
           double valorMedia = Double.parseDouble(media); %>
      <section class="resumo-notas">
        <div class="resumo-media">
          <strong><%= media(valorMedia) %></strong>
          <span class="estrelas"><%= estrelas((int) Math.round(valorMedia)) %></span>
          <span class="texto-suave">
            <% if (avaliacoes.size() == 1) { %>1 avaliação<% } else { %><%= avaliacoes.size() %> avaliações<% } %>
          </span>
        </div>
        <div class="barras">
          <% for (int n = 5; n >= 1; n--) {
                 int porcentagem = Math.round(quantidadePorNota[n] * 100f / avaliacoes.size()); %>
            <div class="barra">
              <span><%= n %>★</span>
              <div class="barra-fundo"><div class="barra-cheia" style="width: <%= porcentagem %>%"></div></div>
              <span><%= quantidadePorNota[n] %></span>
            </div>
          <% } %>
        </div>
      </section>
    <% } %>

    <% if (request.getParameter("sucesso") != null) { %>
      <p class="alerta sucesso">Obrigado pela sua avaliação!</p>
    <% } %>

    <div class="lista-avaliacoes">
      <% for (Map<String, String> avaliacao : avaliacoes) {
             String autor = abreviarNome(avaliacao.get("nome_cliente"));
             String texto = avaliacao.get("comentario");
             if (texto == null || texto.isEmpty()) {
                 texto = "Avaliou sem comentário.";
             } %>
        <figure class="avaliacao">
          <div class="avaliacao-topo">
            <span class="estrelas" aria-label="<%= avaliacao.get("nota") %> de 5 estrelas"><%= estrelas(Integer.parseInt(avaliacao.get("nota"))) %></span>
            <span class="servico"><%= html(avaliacao.get("nome_servico")) %></span>
          </div>
          <blockquote><%= html(texto) %></blockquote>
          <figcaption>
            <span class="avatar"><%= html(autor.substring(0, 1)) %></span>
            <%= html(autor) %>
          </figcaption>
        </figure>
      <% } %>
    </div>
    <% if (avaliacoes.isEmpty()) { %>
      <p class="vazio">Ainda não há avaliações. Seja a primeira pessoa a avaliar!</p>
    <% } %>

    <form id="form-avaliacao" method="post" action="avaliacoes.jsp" class="janela formulario" novalidate>
      <header class="janela-titulo">Deixe sua avaliação</header>

      <div class="formulario-corpo">
        <%@ include file="/WEB-INF/includes/erros.jsp" %>

        <input type="text"  name="nome"  placeholder="Seu nome" aria-label="Seu nome" required maxlength="100"
               value="<%= html(nome) %>">
        <input type="email" name="email" placeholder="E-mail" aria-label="E-mail" required maxlength="150"
               value="<%= html(email) %>">

        <select name="idServico" aria-label="Procedimento realizado" required>
          <option value="">Procedimento realizado</option>
          <% for (Map<String, String> servico : servicos) {
                 String marcado = servico.get("id_servico").equals(String.valueOf(idServico)) ? "selected" : ""; %>
            <option value="<%= servico.get("id_servico") %>" <%= marcado %>><%= html(servico.get("nome_servico")) %></option>
          <% } %>
        </select>

        <!-- Nota de 1 a 5 estrelas (ver .estrelas-escolha no CSS) -->
        <fieldset class="notas">
          <legend>Sua nota</legend>
          <div class="estrelas-escolha">
            <% for (int n = 5; n >= 1; n--) {
                   String marcada = (n == nota) ? "checked" : ""; %>
              <input type="radio" id="nota<%= n %>" name="nota" value="<%= n %>" <%= marcada %>><label
                     for="nota<%= n %>" title="<%= n %> estrela<%= n > 1 ? "s" : "" %>">★</label>
            <% } %>
          </div>
        </fieldset>

        <textarea name="comentario" placeholder="Conte como foi sua experiência (opcional)"
                  aria-label="Comentário" maxlength="500" rows="4"><%= html(comentario) %></textarea>
        <small id="contador" class="contador"><%= comentario.length() %> / 500</small>

        <button type="submit" class="btn">Enviar avaliação</button>
      </div>
    </form>
  </main>

<%@ include file="/WEB-INF/includes/rodape.jsp" %>
