// Página Avaliações: resumo das notas, lista de avaliações e formulário para avaliar.

async function carregarAvaliacoes() {
  await iniciarPagina();

  // Procedimentos para o <select>
  let servicos = await buscar('api/servicos');
  let opcoes = '<option value="">Procedimento realizado</option>';
  for (let servico of servicos) {
    opcoes += `<option value="${servico.id}">${escapar(servico.nome)}</option>`;
  }
  document.getElementById('select-servico').innerHTML = opcoes;

  // Se estiver logado, já preenche nome e e-mail
  let form = document.getElementById('form-avaliacao');
  if (usuario) {
    form.nome.value = usuario.nome;
    form.email.value = usuario.email;
  }
  form.addEventListener('submit', enviarAvaliacao);

  // Contador de caracteres do comentário
  form.comentario.addEventListener('input', atualizarContador);

  await mostrarAvaliacoes();
}

async function mostrarAvaliacoes() {
  let resposta = await buscar('api/avaliacoes');
  let media = resposta.media;            // ex.: 4.67 (ou null)
  let avaliacoes = resposta.avaliacoes;  // lista de avaliações

  if (media !== null) {
    mostrarResumo(media, avaliacoes);
  }

  document.getElementById('sem-avaliacoes').hidden = (avaliacoes.length > 0);

  let html = '';
  for (let avaliacao of avaliacoes) {
    let comentario = avaliacao.comentario;
    if (!comentario) {
      comentario = 'Avaliou sem comentário.';
    }

    html += `
      <figure class="avaliacao">
        <div class="avaliacao-topo">
          <span class="estrelas" aria-label="${avaliacao.nota} de 5 estrelas">${estrelas(avaliacao.nota)}</span>
          <span class="servico">${escapar(avaliacao.servico)}</span>
        </div>
        <blockquote>${escapar(comentario)}</blockquote>
        <figcaption>
          <span class="avatar">${escapar(avaliacao.nome.charAt(0))}</span>
          ${escapar(avaliacao.nome)}
        </figcaption>
      </figure>`;
  }
  document.getElementById('lista-avaliacoes').innerHTML = html;
}

// Quadro do topo: nota média grande + uma barra para cada nota (5 a 1)
function mostrarResumo(media, avaliacoes) {
  document.getElementById('media-numero').textContent = media.toFixed(1).replace('.', ',');
  document.getElementById('media-estrelas').textContent = estrelas(Math.round(media));

  let total = avaliacoes.length;
  if (total === 1) {
    document.getElementById('media-total').textContent = '1 avaliação';
  } else {
    document.getElementById('media-total').textContent = total + ' avaliações';
  }

  let html = '';
  for (let nota = 5; nota >= 1; nota--) {
    // Conta quantas avaliações têm essa nota
    let quantidade = 0;
    for (let avaliacao of avaliacoes) {
      if (avaliacao.nota === nota) {
        quantidade++;
      }
    }
    let porcentagem = Math.round(quantidade / total * 100);

    html += `
      <div class="barra">
        <span>${nota}★</span>
        <div class="barra-fundo"><div class="barra-cheia" style="width: ${porcentagem}%"></div></div>
        <span>${quantidade}</span>
      </div>`;
  }
  document.getElementById('barras').innerHTML = html;
  document.getElementById('resumo-notas').hidden = false;
}

// 4 -> "★★★★☆"
function estrelas(nota) {
  return '★'.repeat(nota) + '☆'.repeat(5 - nota);
}

async function enviarAvaliacao(evento) {
  evento.preventDefault();
  let form = document.getElementById('form-avaliacao');
  let caixaDeErros = document.getElementById('erros');

  if (!validarAvaliacao(form)) {
    return;
  }

  let resultado = await enviar('api/avaliacoes', {
    nome: form.nome.value,
    email: form.email.value,
    idServico: form.idServico.value,
    nota: form.nota.value,
    comentario: form.comentario.value
  });

  if (resultado.erros) {
    mostrarErros(caixaDeErros, resultado.erros);
    return;
  }

  // Deu certo: limpa o formulário e atualiza a lista
  caixaDeErros.hidden = true;
  form.idServico.value = '';
  form.comentario.value = '';
  for (let opcao of form.nota) {
    opcao.checked = false;
  }
  atualizarContador();
  document.getElementById('mensagem-sucesso').hidden = false;
  await mostrarAvaliacoes();
  window.scrollTo({ top: 0, behavior: 'smooth' });
}

// Confere os campos. Devolve true se está tudo certo.
function validarAvaliacao(form) {
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
  }
  return valido;
}

function atualizarContador() {
  let tamanho = document.getElementById('form-avaliacao').comentario.value.length;
  document.getElementById('contador').textContent = tamanho + ' / 500';
}

carregarAvaliacoes();
