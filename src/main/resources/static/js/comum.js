// =====================================================
// Funções usadas por todas as páginas do site.
// Cada página carrega este arquivo primeiro e depois o seu próprio .js
// =====================================================

// Preenchidas pela função iniciarPagina()
let clinica = null;   // dados da clínica (nome, endereço...)
let usuario = null;   // cliente logado, ou null se ninguém fez login


// ---------- Conversa com o servidor Java ----------

// Pede dados ao Java (GET). Exemplo: let servicos = await buscar('/api/servicos');
async function buscar(endereco) {
  let resposta = await fetch(endereco);
  return await lerJson(resposta);
}

// Envia dados para o Java (POST) no formato JSON.
async function enviar(endereco, dados) {
  let resposta = await fetch(endereco, {
    method: 'POST',
    headers: { 'Content-Type': 'application/json' },
    body: JSON.stringify(dados)
  });
  return await lerJson(resposta);
}

// Lê o JSON da resposta. Se deu erro e o Java não mandou mensagem, coloca uma genérica.
// Regra do site: quando algo dá errado, a resposta tem a lista "erros".
async function lerJson(resposta) {
  let dados = await resposta.json();
  if (!resposta.ok && !dados.erros) {
    dados.erros = ['Não foi possível concluir. Tente novamente.'];
  }
  return dados;
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
// e desenha o menu e o rodapé.
async function iniciarPagina() {
  clinica = await buscar('/api/clinica');
  let conta = await buscar('/api/conta');
  usuario = conta.cliente;

  document.title = document.title + ' | ' + clinica.nome;
  desenharMenu();
  desenharRodape();
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
