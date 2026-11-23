// Página Home: textos da clínica + os 4 primeiros procedimentos em destaque.

async function carregarHome() {
  await iniciarPagina();

  document.getElementById('nome-clinica').textContent = clinica.nome;
  document.getElementById('descricao-clinica').textContent = clinica.descricao;
  document.getElementById('endereco-clinica').textContent = clinica.endereco;

  let servicos = await buscar('/api/servicos');

  let html = '';
  for (let i = 0; i < 4 && i < servicos.length; i++) {
    let servico = servicos[i];
    let numero = '0' + (i + 1);   // 01, 02, 03, 04

    html += `
      <a href="agendar.html?servico=${servico.id}" class="janela">
        <span class="janela-titulo">Procedimentos</span>
        <span class="janela-corpo">
          <span class="destaque-numero">${numero}</span>
          <strong class="destaque-nome">${escapar(servico.nome)}</strong>
          <span class="destaque-preco">${formatarPreco(servico.preco)}</span>
          <span class="destaque-acao">Agendar →</span>
        </span>
      </a>`;
  }
  document.getElementById('destaques').innerHTML = html;
}

carregarHome();
