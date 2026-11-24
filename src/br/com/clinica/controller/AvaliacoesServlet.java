package br.com.clinica.controller;

import br.com.clinica.model.Avaliacao;
import br.com.clinica.model.Cliente;
import br.com.clinica.repository.ClinicaRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Endereço api/avaliacoes
 *   GET  -> média das notas e lista de avaliações
 *   POST -> grava uma avaliação nova (nome, email, idServico, nota, comentario)
 */
@WebServlet("/api/avaliacoes")
public class AvaliacoesServlet extends HttpServlet {

    private final ClinicaRepository repo = new ClinicaRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        List<String> itens = new ArrayList<>();
        for (Avaliacao avaliacao : repo.listarAvaliacoes()) {
            itens.add(avaliacao.toJson());
        }
        Double media = repo.mediaDasNotas();   // null quando ainda não há avaliações
        Json.enviar(response, "{\"media\":" + media + ",\"avaliacoes\":" + Json.lista(itens) + "}");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");   // para ler acentos corretamente
        String nome = Json.parametro(request, "nome");
        String email = Json.parametro(request, "email");
        String comentario = Json.parametro(request, "comentario");
        int idServico = Json.numero(Json.parametro(request, "idServico"));
        int nota = Json.numero(Json.parametro(request, "nota"));

        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        List<String> erros = new ArrayList<>();
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (repo.buscarServico(idServico) == null) {
            erros.add("Escolha o procedimento que você fez.");
        }
        if (nota < 1 || nota > 5) {
            erros.add("Escolha uma nota de 1 a 5 estrelas.");
        }
        if (comentario.length() > 500) {
            erros.add("O comentário pode ter no máximo 500 caracteres.");
        }
        if (!erros.isEmpty()) {
            Json.enviar(response, Json.erros(erros));
            return;
        }

        // Se a pessoa ainda não é cliente, cadastra só com nome e e-mail
        Cliente cliente = repo.buscarClientePorEmail(email);
        if (cliente == null) {
            repo.salvarCliente(nome, null, null, email);
            cliente = repo.buscarClientePorEmail(email);
        }

        repo.registrarAvaliacao(cliente.getId(), idServico, nota, comentario);
        Json.enviar(response, "{\"mensagem\":\"Avaliação registrada.\"}");
    }
}
