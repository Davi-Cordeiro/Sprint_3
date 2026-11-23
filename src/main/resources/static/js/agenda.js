// Página Agenda: lista de procedimentos para agendar, botão do WhatsApp e mapa.

async function carregarAgenda() {
  await iniciarPagina();

  let servicos = await buscar('/api/servicos');
  let html = '';
  for (let servico of servicos) {
    html += `<a href="agendar.html?servico=${servico.id}">${escapar(servico.nome)}</a>`;
  }
  document.getElementById('lista-procedimentos').innerHTML = html;

  // Link do WhatsApp já com a mensagem escrita
  let mensagem = 'Olá! Gostaria de agendar uma avaliação.';
  document.getElementById('link-whatsapp').href =
    'https://wa.me/' + clinica.whatsapp + '?text=' + encodeURIComponent(mensagem);

  // Mapa do Google Maps
  document.getElementById('mapa').src =
    'https://www.google.com/maps?output=embed&q=' + encodeURIComponent(clinica.mapaBusca);

  document.getElementById('endereco').textContent = clinica.endereco;
  document.getElementById('cidade').textContent = clinica.cidade;
}

carregarAgenda();
