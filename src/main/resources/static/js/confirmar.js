// Página Confirmar: a pessoa informa nome, telefone e e-mail e o agendamento é gravado.
// Endereço da página: confirmar.html?servico=1&data=2026-12-01&hora=09:00

let idServico = parametro('servico');
let data = parametro('data');
let hora = parametro('hora');

async function carregarConfirmar() {
  await iniciarPagina();

  let servico = await buscar('/api/servicos/' + idServico);
  if (servico.erros || !data || !hora) {
    window.location.href = 'agenda.html';
    return;
  }
  idServico = servico.id;

  document.getElementById('trocar-horario').href = 'agendar.html?servico=' + idServico + '&data=' + data;
  document.getElementById('resumo-servico').textContent = servico.nome;
  document.getElementById('resumo-data').textContent = dataPorExtenso(data) + ' às ' + hora;

  let form = document.getElementById('form-agendamento');

  // Se a pessoa está logada, já preenche nome e e-mail da conta
  if (usuario) {
    form.nome.value = usuario.nome;
    form.email.value = usuario.email;
  }

  form.addEventListener('submit', confirmarAgendamento);
}

async function confirmarAgendamento(evento) {
  evento.preventDefault();   // não recarrega a página: o JavaScript é que envia os dados
  let form = document.getElementById('form-agendamento');
  let caixaDeErros = document.getElementById('erros');

  // Precisa de pelo menos um telefone
  if (form.whatsapp.value.trim() === '' && form.celular.value.trim() === '') {
    mostrarErros(caixaDeErros, ['Informe o WhatsApp ou o celular.']);
    return;
  }

  let resultado = await enviar('/api/agendamentos', {
    idServico: String(idServico),
    data: data,
    hora: hora,
    nome: form.nome.value,
    whatsapp: form.whatsapp.value,
    celular: form.celular.value,
    email: form.email.value
  });

  if (resultado.ocupado) {
    // Alguém pegou o horário antes: volta para o calendário com o aviso
    window.location.href = 'agendar.html?servico=' + idServico + '&data=' + data + '&indisponivel';
    return;
  }
  if (resultado.erros) {
    mostrarErros(caixaDeErros, resultado.erros);
    return;
  }

  // Guarda o agendamento para a página de sucesso mostrar
  sessionStorage.setItem('ultimoAgendamento', JSON.stringify(resultado));
  window.location.href = 'sucesso.html';
}

carregarConfirmar();
