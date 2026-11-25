<%-- Lista de erros do formulário (a página adiciona com erros.add("...")). Não mostra nada se não houver erros. --%>
<%@ page pageEncoding="UTF-8" %>
<% if (!erros.isEmpty()) { %>
  <ul class="alerta erro">
    <% for (String erro : erros) { %>
      <li><%= html(erro) %></li>
    <% } %>
  </ul>
<% } %>
