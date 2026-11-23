package br.com.clinica.controller;

import br.com.clinica.model.Servico;
import br.com.clinica.repository.ClinicaRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Endereço api/servicos
 *   api/servicos       -> lista todos os procedimentos
 *   api/servicos?id=3  -> só o procedimento 3
 */
@WebServlet("/api/servicos")
public class ServicosServlet extends HttpServlet {

    private final ClinicaRepository repo = new ClinicaRepository();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String id = request.getParameter("id");

        // Um procedimento só
        if (id != null) {
            Servico servico = null;
            try {
                servico = repo.buscarServico(Integer.parseInt(id));
            } catch (NumberFormatException e) {
                // id não é número: servico continua null
            }
            if (servico == null) {
                Json.enviar(response, Json.erro("Procedimento não encontrado."));
            } else {
                Json.enviar(response, servico.toJson());
            }
            return;
        }

        // Todos os procedimentos
        List<String> itens = new ArrayList<>();
        for (Servico servico : repo.listarServicos()) {
            itens.add(servico.toJson());
        }
        Json.enviar(response, Json.lista(itens));
    }
}
