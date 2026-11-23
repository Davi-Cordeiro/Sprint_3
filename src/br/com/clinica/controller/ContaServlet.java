package br.com.clinica.controller;

import br.com.clinica.model.Agendamento;
import br.com.clinica.model.Cliente;
import br.com.clinica.repository.ClinicaRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Login dos clientes. Quem fez login fica guardado na sessão com o nome "usuario".
 *   GET  api/conta           -> quem está logado e seus agendamentos
 *   POST api/conta/entrar    -> email e senha
 *   POST api/conta/cadastro  -> nome, whatsapp, email, senha e confirmacao
 *   POST api/conta/sair
 */
@WebServlet({"/api/conta", "/api/conta/entrar", "/api/conta/cadastro", "/api/conta/sair"})
public class ContaServlet extends HttpServlet {

    private final ClinicaRepository repo = new ClinicaRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Cliente usuario = (Cliente) request.getSession().getAttribute("usuario");
        if (usuario == null) {
            Json.enviar(response, "{\"cliente\":null}");
            return;
        }

        List<String> itens = new ArrayList<>();
        for (Agendamento agendamento : repo.listarAgendamentosDoCliente(usuario.getId())) {
            itens.add(agendamento.toJson());
        }
        Json.enviar(response, "{\"cliente\":" + usuario.toJson() + ",\"agendamentos\":" + Json.lista(itens) + "}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");   // para ler acentos corretamente
        String caminho = request.getServletPath();

        if (caminho.equals("/api/conta/entrar")) {
            entrar(request, response);
        } else if (caminho.equals("/api/conta/cadastro")) {
            cadastrar(request, response);
        } else if (caminho.equals("/api/conta/sair")) {
            request.getSession().invalidate();
            Json.enviar(response, "{\"mensagem\":\"Você saiu da conta.\"}");
        }
    }

    private void entrar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String email = parametro(request, "email");
        String senha = parametro(request, "senha");

        String senhaSalva = repo.buscarSenha(email);
        if (!Senha.confere(senha, senhaSalva)) {
            Json.enviar(response, Json.erro("E-mail ou senha incorretos."));
            return;
        }

        Cliente cliente = repo.buscarClientePorEmail(email);
        guardarNaSessao(request, cliente);
        Json.enviar(response, "{\"cliente\":" + cliente.toJson() + "}");
    }

    private void cadastrar(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String nome = parametro(request, "nome");
        String whatsapp = parametro(request, "whatsapp");
        String email = parametro(request, "email");
        String senha = parametro(request, "senha");
        String confirmacao = parametro(request, "confirmacao");

        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        List<String> erros = new ArrayList<>();
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (senha.length() < 6) {
            erros.add("A senha precisa ter pelo menos 6 caracteres.");
        }
        if (!senha.equals(confirmacao)) {
            erros.add("As senhas não são iguais.");
        }
        if (erros.isEmpty() && repo.buscarSenha(email) != null) {
            erros.add("Já existe uma conta com esse e-mail. Faça login.");
        }
        if (!erros.isEmpty()) {
            Json.enviar(response, Json.erros(erros));
            return;
        }

        // Se a pessoa já agendou antes (cliente sem senha), aproveita o mesmo cadastro
        repo.salvarCliente(nome, whatsapp, null, email);
        repo.definirSenha(email, Senha.criptografar(senha));

        Cliente cliente = repo.buscarClientePorEmail(email);
        guardarNaSessao(request, cliente);
        Json.enviar(response, "{\"cliente\":" + cliente.toJson() + "}");
    }

    private void guardarNaSessao(HttpServletRequest request, Cliente cliente) {
        HttpSession sessao = request.getSession();
        request.changeSessionId();   // troca o código da sessão ao entrar (proteção contra roubo de sessão)
        sessao.setAttribute("usuario", cliente);
    }

    /** Valor de um campo do formulário, sem espaços nas pontas ("" se não veio). */
    private String parametro(HttpServletRequest request, String nome) {
        String valor = request.getParameter(nome);
        if (valor == null) {
            return "";
        }
        return valor.trim();
    }
}
