// =====================================================
// JavaScript do site: confere os formulários ANTES de enviar,
// para a pessoa ver o erro na hora. (O Java confere de novo quando os dados chegam.)
//
// Este arquivo é carregado no fim de todas as páginas (rodape.jsp).
// Cada parte só funciona se o formulário dela existir na página.
// =====================================================


// ---------- Mensagens de erro nos campos ----------

// Tira as mensagens de erro da última tentativa.
function limparErros(form) {
  let mensagens = form.querySelectorAll('.erro-campo');
  for (let mensagem of mensagens) {
    mensagem.remove();
  }
  let campos = form.querySelectorAll('.invalido');
  for (let campo of campos) {
    campo.classList.remove('invalido');
  }
}

// Mostra uma mensagem de erro embaixo do campo e pinta a borda de vermelho.
function erroNoCampo(campo, mensagem) {
  campo.classList.add('invalido');
  let aviso = document.createElement('small');
  aviso.className = 'erro-campo';
  aviso.textContent = mensagem;
  campo.insertAdjacentElement('afterend', aviso);
}

// Leva a tela até o primeiro campo com erro.
function focarPrimeiroErro(form) {
  let primeiro = form.querySelector('.invalido');
  if (primeiro) {
    primeiro.focus();
  }
}


// ---------- Regras dos campos ----------

// Nome com pelo menos duas palavras. Ex.: "Maria Silva"
function nomeValido(nome) {
  let partes = nome.trim().split(' ');
  return partes.length >= 2 && partes[0].length >= 2;
}

// Formato algo@algo.algo
function emailValido(email) {
  return /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email.trim());
}

// Telefone com DDD: 10 ou 11 números. Ex.: (11) 95177-2722
function telefoneValido(telefone) {
  let numeros = telefone.replace(/\D/g, '');   // \D = tudo que não é número
  return numeros.length === 10 || numeros.length === 11;
}

// Máscara: enquanto a pessoa digita, o campo vai ficando (11) 95177-2722
function mascaraTelefone(campo) {
  campo.addEventListener('input', function () {
    let n = campo.value.replace(/\D/g, '').substring(0, 11);
    let formatado = n;
    if (n.length > 2) {
      formatado = '(' + n.substring(0, 2) + ') ' + n.substring(2);
    }
    if (n.length > 6) {
      let meio = n.length === 11 ? 7 : 6;   // celular tem 9 dígitos, fixo tem 8
      formatado = '(' + n.substring(0, 2) + ') ' + n.substring(2, meio) + '-' + n.substring(meio);
    }
    campo.value = formatado;
  });
}


// ---------- Página Confirmar (agendamento) ----------

let formAgendamento = document.getElementById('form-agendamento');

if (formAgendamento) {
  mascaraTelefone(formAgendamento.whatsapp);
  mascaraTelefone(formAgendamento.celular);

  formAgendamento.addEventListener('submit', function (evento) {
    let form = formAgendamento;
    limparErros(form);
    let valido = true;

    if (!nomeValido(form.nome.value)) {
      erroNoCampo(form.nome, 'Informe seu nome e sobrenome.');
      valido = false;
    }

    let whatsapp = form.whatsapp.value.trim();
    let celular = form.celular.value.trim();
    if (whatsapp === '' && celular === '') {
      erroNoCampo(form.celular, 'Informe o WhatsApp ou o celular.');
      valido = false;
    }
    if (whatsapp !== '' && !telefoneValido(whatsapp)) {
      erroNoCampo(form.whatsapp, 'WhatsApp inválido. Exemplo: (11) 95177-2722');
      valido = false;
    }
    if (celular !== '' && !telefoneValido(celular)) {
      erroNoCampo(form.celular, 'Celular inválido. Exemplo: (11) 95177-2722');
      valido = false;
    }

    if (!emailValido(form.email.value)) {
      erroNoCampo(form.email, 'Informe um e-mail válido. Exemplo: nome@email.com');
      valido = false;
    }

    if (!valido) {
      focarPrimeiroErro(form);
      evento.preventDefault();   // tem erro: não envia
    }
  });
}


// ---------- Página Avaliações ----------

let formAvaliacao = document.getElementById('form-avaliacao');

if (formAvaliacao) {
  // Contador de caracteres do comentário
  formAvaliacao.comentario.addEventListener('input', function () {
    let tamanho = formAvaliacao.comentario.value.length;
    document.getElementById('contador').textContent = tamanho + ' / 500';
  });

  formAvaliacao.addEventListener('submit', function (evento) {
    let form = formAvaliacao;
    limparErros(form);
    let valido = true;

    if (!nomeValido(form.nome.value)) {
      erroNoCampo(form.nome, 'Informe seu nome e sobrenome.');
      valido = false;
    }
    if (!emailValido(form.email.value)) {
      erroNoCampo(form.email, 'Informe um e-mail válido. Exemplo: nome@email.com');
      valido = false;
    }
    if (form.idServico.value === '') {
      erroNoCampo(form.idServico, 'Escolha o procedimento que você fez.');
      valido = false;
    }
    if (form.nota.value === '') {
      erroNoCampo(document.querySelector('.estrelas-escolha'), 'Clique nas estrelas para dar sua nota.');
      valido = false;
    }
    if (form.comentario.value.length > 500) {
      erroNoCampo(form.comentario, 'O comentário pode ter no máximo 500 caracteres.');
      valido = false;
    }

    if (!valido) {
      focarPrimeiroErro(form);
      evento.preventDefault();   // tem erro: não envia
    }
  });
}


// ---------- Página Criar conta ----------

let formCadastro = document.getElementById('form-cadastro');

if (formCadastro) {
  mascaraTelefone(formCadastro.whatsapp);

  formCadastro.addEventListener('submit', function (evento) {
    let form = formCadastro;
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
      evento.preventDefault();   // tem erro: não envia
    }
  });
}


// ---------- Página Minha conta (login) ----------

let formLogin = document.getElementById('form-login');

if (formLogin) {
  formLogin.addEventListener('submit', function (evento) {
    let form = formLogin;
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
      evento.preventDefault();   // tem erro: não envia
    }
  });
}
