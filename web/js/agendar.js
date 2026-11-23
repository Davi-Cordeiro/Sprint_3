// Página Agendar: calendário do mês + horários livres do dia escolhido.
// Endereço da página: agendar.html?servico=1   (pode vir também &data=2026-12-01)

let idServico = parametro('servico');
let dataEscolhida = parametro('data');   // ex.: "2026-12-01" (ou null)
let mesNaTela = null;                    // ex.: "2026-12"

// Só aceita data no formato AAAA-MM-DD (alguém pode digitar qualquer coisa no endereço)
if (dataEscolhida && !/^\d{4}-\d{2}-\d{2}$/.test(dataEscolhida)) {
  dataEscolhida = null;
}

async function carregarAgendar() {
  await iniciarPagina();

  let servico = await buscar('api/servicos?id=' + encodeURIComponent(idServico));
  if (servico.erros) {
    // Procedimento não existe: volta para a lista
    window.location.href = 'procedimentos.html';
    return;
  }
  idServico = servico.id;   // usa o número que veio do Java, e não o texto do endereço
  document.getElementById('nome-servico').textContent = servico.nome;
  document.title = 'Agendar ' + servico.nome;

  // Veio da tela de confirmação porque o horário foi ocupado
  if (parametro('indisponivel') !== null) {
    document.getElementById('aviso-ocupado').hidden = false;
  }

  document.getElementById('mes-anterior').addEventListener('click', voltarMes);
  document.getElementById('mes-seguinte').addEventListener('click', avancarMes);

  if (dataEscolhida) {
    await mostrarMes(dataEscolhida.substring(0, 7));   // "2026-12-01" -> "2026-12"
    await mostrarHorarios(dataEscolhida);
  } else {
    await mostrarMes(null);   // null = mês atual
  }
}


// ==================== Calendário ====================

// Pede ao Java os dias livres do mês e desenha o calendário.
async function mostrarMes(mes) {
  let endereco = 'api/agenda/dias';
  if (mes) {
    endereco += '?mes=' + mes;
  }
  let resposta = await buscar(endereco);
  // resposta = { mes: "2026-12", primeiroMes: "2026-11", ultimoMes: "2027-01", diasDisponiveis: [...] }

  mesNaTela = resposta.mes;
  desenharCalendario(resposta.mes, resposta.diasDisponiveis);

  // Desliga as setas no primeiro e no último mês liberado
  document.getElementById('mes-anterior').disabled = (resposta.mes === resposta.primeiroMes);
  document.getElementById('mes-seguinte').disabled = (resposta.mes === resposta.ultimoMes);
}

function voltarMes() {
  mostrarMes(somarMeses(mesNaTela, -1));
}

function avancarMes() {
  mostrarMes(somarMeses(mesNaTela, 1));
}

// somarMeses("2026-12", 1) -> "2027-01"
function somarMeses(mes, quantidade) {
  let partes = mes.split('-');
  let data = new Date(Number(partes[0]), Number(partes[1]) - 1 + quantidade, 1);
  let novoMes = data.getMonth() + 1;
  if (novoMes < 10) {
    novoMes = '0' + novoMes;
  }
  return data.getFullYear() + '-' + novoMes;
}

// Monta a tabela do mês: uma linha por semana, de domingo a sábado.
function desenharCalendario(mes, diasDisponiveis) {
  let partes = mes.split('-');
  let ano = Number(partes[0]);
  let numeroMes = Number(partes[1]);

  let primeiroDia = new Date(ano, numeroMes - 1, 1);
  let totalDeDias = new Date(ano, numeroMes, 0).getDate();   // dia 0 do mês seguinte = último dia deste mês

  // Nome do mês no topo: "Dezembro de 2026"
  let nomeMes = primeiroDia.toLocaleDateString('pt-BR', { month: 'long', year: 'numeric' });
  document.getElementById('nome-mes').textContent = primeiraMaiuscula(nomeMes);

  let html = '<tr>';
  let coluna = 0;

  // Quadrados vazios antes do dia 1 (getDay: 0 = domingo, 1 = segunda...)
  for (let i = 0; i < primeiroDia.getDay(); i++) {
    html += '<td></td>';
    coluna++;
  }

  for (let dia = 1; dia <= totalDeDias; dia++) {
    let diaComZero = dia;
    if (dia < 10) {
      diaComZero = '0' + dia;
    }
    let data = mes + '-' + diaComZero;   // "2026-12-01"

    if (diasDisponiveis.includes(data)) {
      let classe = 'dia livre';
      if (data === dataEscolhida) {
        classe += ' selecionado';
      }
      html += `<td><button type="button" class="${classe}" data-data="${data}">${dia}</button></td>`;
    } else {
      html += `<td><span class="dia indisponivel">${dia}</span></td>`;
    }

    // Fim da semana (sábado): começa uma linha nova
    coluna++;
    if (coluna === 7 && dia < totalDeDias) {
      html += '</tr><tr>';
      coluna = 0;
    }
  }
  html += '</tr>';
  document.getElementById('dias').innerHTML = html;

  // Clique nos dias livres
  let botoes = document.querySelectorAll('.dia.livre');
  for (let botao of botoes) {
    botao.addEventListener('click', function () {
      escolherDia(botao.dataset.data);
    });
  }
}

function escolherDia(data) {
  dataEscolhida = data;

  // Pinta só o dia escolhido
  let botoes = document.querySelectorAll('.dia.livre');
  for (let botao of botoes) {
    if (botao.dataset.data === data) {
      botao.classList.add('selecionado');
    } else {
      botao.classList.remove('selecionado');
    }
  }

  // Guarda a data no endereço da página (assim o "Trocar horário" volta para este dia)
  history.replaceState(null, '', 'agendar.html?servico=' + idServico + '&data=' + data);

  mostrarHorarios(data);
}


// ==================== Horários ====================

async function mostrarHorarios(data) {
  let horarios = await buscar('api/agenda/horarios?data=' + data);
  // horarios = [ { hora: "09:00", disponivel: true }, ... ]

  document.getElementById('titulo-horarios').textContent = dataPorExtenso(data);
  document.getElementById('dica-horarios').hidden = true;

  let html = '';
  for (let horario of horarios) {
    if (horario.disponivel) {
      let link = 'confirmar.html?servico=' + idServico + '&data=' + data + '&hora=' + horario.hora;
      html += `<a href="${link}" class="horario">${horario.hora}</a>`;
    } else {
      html += `<span class="horario ocupado">${horario.hora}</span>`;
    }
  }

  let lista = document.getElementById('lista-horarios');
  lista.innerHTML = html;
  lista.hidden = false;
}

carregarAgendar();
