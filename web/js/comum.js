// =====================================================
// Funções usadas por todas as páginas do site.
// Cada página carrega este arquivo primeiro e depois o seu próprio .js
// =====================================================

// Preenchidas pela função iniciarPagina()
let clinica = null;   // dados da clínica (nome, endereço...)
let usuario = null;   // cliente logado, ou null se ninguém fez login


// ---------- Conversa com o servidor Java ----------

// Pede dados ao Java (GET). Exemplo: let servicos = await buscar('api/servicos');
async function buscar(endereco) {
  let resposta = await fetch(endereco);
  return await lerJson(resposta);
}

// Envia dados para o Java (POST), do mesmo jeito que um formulário HTML envia.
// No Java, cada valor é lido com request.getParameter("nome").
async function enviar(endereco, dados) {
  let resposta = await fetch(endereco, {
    method: 'POST',
    body: new URLSearchParams(dados)
  });
  return await lerJson(resposta);
}

// Lê o JSON que o Java respondeu.
// Regra do site: quando algo dá errado, a resposta tem a lista "erros".
async function lerJson(resposta) {
  try {
    return await resposta.json();
  } catch (e) {
    // O servidor não respondeu em JSON (ex.: erro no banco de dados)
    return { erros: ['Não foi possível concluir. Tente novamente.'] };
  }
}


// ---------- Validação dos formulários ----------
// Cada página confere os campos antes de enviar. Se algum estiver errado,
// a mensagem aparece embaixo do campo e o formulário não é enviado.

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


// ---------- Ajudantes para montar a página ----------

// Evita que um texto digitado por alguém (ex.: um comentário) vire código HTML na página.
function escapar(texto) {
  let div = document.createElement('div');
  div.textContent = texto;
  return div.innerHTML;
}

// Mostra a lista de erros dentro de um <ul class="alerta erro">.
function mostrarErros(elemento, erros) {
  let html = '';
  for (let erro of erros) {
    html += '<li>' + escapar(erro) + '</li>';
  }
  elemento.innerHTML = html;
  elemento.hidden = false;
}

// Lê um valor do endereço da página. Ex.: em agendar.html?servico=3, parametro('servico') dá "3"
function parametro(nome) {
  let parametros = new URLSearchParams(window.location.search);
  return parametros.get(nome);
}


// ---------- Formatação ----------

// 150 -> "R$ 150,00"
function formatarPreco(valor) {
  return valor.toLocaleString('pt-BR', { style: 'currency', currency: 'BRL' });
}

// "2026-12-01" -> Date. (Separa ano, mês e dia para não errar o dia por causa do fuso horário.)
function lerData(texto) {
  let partes = texto.split('-');
  let ano = Number(partes[0]);
  let mes = Number(partes[1]);
  let dia = Number(partes[2]);
  return new Date(ano, mes - 1, dia);   // no JavaScript, janeiro é o mês 0
}

// "2026-12-01" -> "01/12/2026"
function formatarData(texto) {
  return lerData(texto).toLocaleDateString('pt-BR');
}

// "2026-12-01" -> "Terça-feira, 1 de dezembro"
function dataPorExtenso(texto) {
  let extenso = lerData(texto).toLocaleDateString('pt-BR', { weekday: 'long', day: 'numeric', month: 'long' });
  return primeiraMaiuscula(extenso);
}

function primeiraMaiuscula(texto) {
  return texto.charAt(0).toUpperCase() + texto.slice(1);
}

// "Mariana Souza" -> "Mariana"
function primeiroNome(nome) {
  return nome.split(' ')[0];
}


// ---------- Menu lateral e rodapé (iguais em todas as páginas) ----------

// Toda página chama esta função primeiro: busca os dados da clínica e do login
// e desenha o menu e o rodapé. Devolve os dados da conta (cliente e agendamentos).
async function iniciarPagina() {
  clinica = await buscar('api/clinica');
  let conta = await buscar('api/conta');
  usuario = conta.cliente;

  document.title = document.title + ' | ' + clinica.nome;
  desenharMenu();
  desenharRodape();
  return conta;
}

function desenharMenu() {
  // A página atual vem do <body data-pagina="...">
  let paginaAtual = document.body.dataset.pagina;

  let textoLogin = 'Entrar';
  if (usuario) {
    textoLogin = 'Olá, ' + primeiroNome(usuario.nome);
  }

  document.getElementById('menu').innerHTML = `
    <a href="conta.html" class="login ${classeAtivo('conta', paginaAtual)}" title="Login da conta">
      <svg viewBox="0 0 64 64" aria-hidden="true">
        <circle cx="32" cy="22" r="12" />
        <path d="M10 60c0-12 10-20 22-20s22 8 22 20" />
        <path d="M19 22c0-9 6-15 13-15s13 6 13 15" />
      </svg>
      <span class="sr-only">Login da conta</span>
    </a>
    <span class="login-nome">${escapar(textoLogin)}</span>

    <nav class="menu">
      <a href="index.html" class="${classeAtivo('home', paginaAtual)}">Home</a>
      <a href="procedimentos.html" class="${classeAtivo('procedimentos', paginaAtual)}">Procedimentos</a>
      <a href="agenda.html" class="${classeAtivo('agenda', paginaAtual)}">Agenda</a>
      <a href="avaliacoes.html" class="${classeAtivo('avaliacoes', paginaAtual)}">Avaliações</a>
    </nav>`;
}

// Destaca no menu a página em que a pessoa está
function classeAtivo(pagina, paginaAtual) {
  if (pagina === paginaAtual) {
    return 'ativo';
  }
  return '';
}

function desenharRodape() {
  document.getElementById('rodape').innerHTML = `
    <span>© 2026 ${escapar(clinica.nome)}</span>
    <span>${escapar(clinica.endereco)} · ${escapar(clinica.cidade)}</span>`;
}
