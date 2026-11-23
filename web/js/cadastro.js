// Página Criar conta: envia os dados para o Java criar o cadastro e já entra na conta.

async function carregarCadastro() {
  await iniciarPagina();
  let form = document.getElementById('form-cadastro');
  mascaraTelefone(form.whatsapp);
  form.addEventListener('submit', cadastrar);
}

async function cadastrar(evento) {
  evento.preventDefault();
  let form = document.getElementById('form-cadastro');
  let caixaDeErros = document.getElementById('erros');

  if (!validarCadastro(form)) {
    return;
  }

  let resultado = await enviar('api/conta/cadastro', {
    nome: form.nome.value,
    whatsapp: form.whatsapp.value,
    email: form.email.value,
    senha: form.senha.value,
    confirmacao: form.confirmacao.value
  });

  if (resultado.erros) {
    mostrarErros(caixaDeErros, resultado.erros);
    return;
  }

  window.location.href = 'conta.html';
}

// Confere os campos. Devolve true se está tudo certo.
function validarCadastro(form) {
  limparErros(form);
  let valido = true;

  if (!nomeValido(form.nome.value)) {
    erroNoCampo(form.nome, 'Informe seu nome e sobrenome.');
    valido = false;
  }
  // WhatsApp é opcional, mas se preencher tem que estar certo
  if (form.whatsapp.value.trim() !== '' && !telefoneValido(form.whatsapp.value)) {
    erroNoCampo(form.whatsapp, 'WhatsApp inválido. Exemplo: (11) 95177-2722');
    valido = false;
  }
  if (!emailValido(form.email.value)) {
    erroNoCampo(form.email, 'Informe um e-mail válido. Exemplo: nome@email.com');
    valido = false;
  }
  if (form.senha.value.length < 6) {
    erroNoCampo(form.senha, 'A senha precisa ter pelo menos 6 caracteres.');
    valido = false;
  }
  if (form.confirmacao.value !== form.senha.value) {
    erroNoCampo(form.confirmacao, 'As senhas não são iguais.');
    valido = false;
  }

  if (!valido) {
    focarPrimeiroErro(form);
  }
  return valido;
}

carregarCadastro();
