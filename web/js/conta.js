// Página Minha conta: mostra o formulário de login ou, se já estiver logado, os agendamentos.

async function carregarConta() {
  let conta = await iniciarPagina();

  document.getElementById('form-login').addEventListener('submit', entrar);
  document.getElementById('botao-sair').addEventListener('click', sair);

  if (usuario) {
    mostrarAgendamentos(conta.agendamentos);
  } else {
    document.getElementById('form-login').hidden = false;
  }
}

function mostrarAgendamentos(agendamentos) {
  document.getElementById('area-cliente').hidden = false;
  document.getElementById('ola').textContent = 'Olá, ' + primeiroNome(usuario.nome) + '!';
  document.getElementById('email-cliente').textContent = usuario.email;

  if (agendamentos.length === 0) {
    document.getElementById('sem-agendamentos').hidden = false;
    return;
  }

  let html = '';
  for (let agendamento of agendamentos) {
    html += `
      <tr>
        <td>${formatarData(agendamento.data)}</td>
        <td>${agendamento.hora}</td>
        <td>${escapar(agendamento.servico)}</td>
        <td>${escapar(agendamento.profissional)}</td>
      </tr>`;
  }
  document.getElementById('agendamentos').innerHTML = html;
  document.getElementById('tabela-agendamentos').hidden = false;
}

async function entrar(evento) {
  evento.preventDefault();
  let form = document.getElementById('form-login');

  // Confere os campos
  limparErros(form);
  let valido = true;
  if (!emailValido(form.email.value)) {
    erroNoCampo(form.email, 'Informe um e-mail válido.');
    valido = false;
  }
  if (form.senha.value === '') {
    erroNoCampo(form.senha, 'Informe sua senha.');
    valido = false;
  }
  if (!valido) {
    focarPrimeiroErro(form);
    return;
  }

  let resultado = await enviar('api/conta/entrar', {
    email: form.email.value,
    senha: form.senha.value
  });

  if (resultado.erros) {
    form.senha.value = '';
    mostrarErros(document.getElementById('erros'), resultado.erros);
    return;
  }

  // Recarrega a página para o menu também mostrar "Olá, Nome"
  window.location.reload();
}

async function sair() {
  await enviar('api/conta/sair', {});
  window.location.href = 'index.html';
}

carregarConta();
