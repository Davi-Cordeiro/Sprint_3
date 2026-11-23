// Página Criar conta: envia os dados para o Java criar o cadastro e já entra na conta.

async function carregarCadastro() {
  await iniciarPagina();
  document.getElementById('form-cadastro').addEventListener('submit', cadastrar);
}

async function cadastrar(evento) {
  evento.preventDefault();
  let form = document.getElementById('form-cadastro');
  let caixaDeErros = document.getElementById('erros');

  // Confere antes de enviar (o Java confere de novo)
  if (form.senha.value !== form.confirmacao.value) {
    mostrarErros(caixaDeErros, ['As senhas não são iguais.']);
    return;
  }

  let resultado = await enviar('/api/conta/cadastro', {
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

carregarCadastro();
