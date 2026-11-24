package br.com.clinica.controller;

import br.com.clinica.model.Cliente;
import br.com.clinica.model.Servico;
import br.com.clinica.repository.ClinicaRepository;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Endereço api/agendamentos (POST): grava um agendamento.
 * Recebe: idServico, data, hora, nome, whatsapp, celular e email.
 */
@WebServlet("/api/agendamentos")
public class AgendamentoServlet extends HttpServlet {

    private static final String OCUPADO = "{\"ocupado\":true,"
            + "\"erros\":[\"Esse horário acabou de ser ocupado. Escolha outro, por favor.\"]}";

    private final ClinicaRepository repo = new ClinicaRepository();
    private final AgendaService agenda = new AgendaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");   // para ler acentos corretamente
        String nome = Json.parametro(request, "nome");
        String whatsapp = Json.parametro(request, "whatsapp");
        String celular = Json.parametro(request, "celular");
        String email = Json.parametro(request, "email");
        String hora = Json.parametro(request, "hora");
        Servico servico = repo.buscarServico(Json.numero(Json.parametro(request, "idServico")));

        // Confere os dados (o JavaScript já conferiu, mas o servidor confere de novo)
        List<String> erros = new ArrayList<>();
        if (nome.isEmpty()) {
            erros.add("Informe seu nome.");
        }
        if (whatsapp.isEmpty() && celular.isEmpty()) {
            erros.add("Informe o WhatsApp ou o celular.");
        }
        if (!email.contains("@")) {
            erros.add("Informe um e-mail válido.");
        }
        if (servico == null) {
            erros.add("Procedimento inválido.");
        }
        LocalDate data = null;
        try {
            data = LocalDate.parse(Json.parametro(request, "data"));   // "2026-12-01"
        } catch (Exception e) {
            erros.add("Data inválida.");
        }
        if (!erros.isEmpty()) {
            Json.enviar(response, Json.erros(erros));
            return;
        }

        // O horário ainda está livre? (o "ocupado" avisa o JavaScript para voltar ao calendário)
        if (!agenda.horarioDisponivel(data, hora)) {
            Json.enviar(response, OCUPADO);
            return;
        }

        repo.salvarCliente(nome, whatsapp, celular, email);
        Cliente cliente = repo.buscarClientePorEmail(email);

        String profissional = repo.registrarAgendamento(cliente.getId(), servico.getId(), data.toString(), hora);
        if (profissional == null) {
            Json.enviar(response, OCUPADO);
            return;
        }

        Json.enviar(response, "{\"servico\":" + Json.texto(servico.getNome())
                + ",\"data\":" + Json.texto(data.toString())
                + ",\"hora\":" + Json.texto(hora)
                + ",\"profissional\":" + Json.texto(profissional) + "}");
    }
}
