// Página Procedimentos: um card para cada procedimento da clínica.

async function carregarProcedimentos() {
  await iniciarPagina();

  let servicos = await buscar('api/servicos');

  // A área .card-midia mostra a inicial do procedimento (ex.: "L" de Limpeza de pele).
  // Para usar fotos: crie a pasta static/img, coloque servico-1.jpg (2, 3...) nela e troque a
  // <span class="card-inicial"> por <img src="img/servico-${servico.id}.jpg" alt="">
  let html = '';
  for (let servico of servicos) {
    html += `
      <article class="janela card">
        <a href="agendar.html?servico=${servico.id}" class="card-link">
          <header class="janela-titulo">${escapar(servico.nome)}</header>
          <div class="card-midia">
            <span class="card-inicial">${escapar(servico.nome.charAt(0))}</span>
            <span class="card-preco">${formatarPreco(servico.preco)}</span>
          </div>
        </a>

        <details>
          <summary>Mais informações</summary>
          <p>${escapar(servico.descricao)}</p>
        </details>

        <a href="agendar.html?servico=${servico.id}" class="btn btn-pequeno card-agendar">Agendar</a>
      </article>`;
  }
  document.getElementById('cards').innerHTML = html;
}

carregarProcedimentos();
