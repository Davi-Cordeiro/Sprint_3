// Página Sucesso: mostra o agendamento que acabou de ser feito.
// (O confirmar.js guardou os dados no sessionStorage, a "memória" desta aba do navegador.)

async function carregarSucesso() {
  await iniciarPagina();

  let guardado = sessionStorage.getItem('ultimoAgendamento');
  if (!guardado) {
    window.location.href = 'agenda.html';
    return;
  }
  let agendamento = JSON.parse(guardado);

  document.getElementById('sucesso-servico').textContent = agendamento.servico;
  document.getElementById('sucesso-data').textContent = dataPorExtenso(agendamento.data) + ' às ' + agendamento.hora;
  document.getElementById('sucesso-profissional').textContent = 'Com ' + agendamento.profissional;
}

carregarSucesso();
