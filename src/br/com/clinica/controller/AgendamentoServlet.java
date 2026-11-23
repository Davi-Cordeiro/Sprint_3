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

    private final ClinicaRepository repo = new ClinicaRepository();
    private final AgendaService agenda = new AgendaService();

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");   // para ler acentos corretamente
        String nome = parametro(request, "nome");
        String whatsapp = parametro(request, "whatsapp");
        String celular = parametro(request, "celular");
        String email = parametro(request, "email");
        String hora = parametro(request, "hora");

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

        Servico servico = null;
        try {
            servico = repo.buscarServico(Integer.parseInt(parametro(request, "idServico")));
        } catch (NumberFormatException e) {
            // idServico não é um número: servico continua null
        }
        if (servico == null) {
            erros.add("Procedimento inválido.");
        }

        LocalDate data = null;
        try {
            data = LocalDate.parse(parametro(request, "data"));   // "2026-12-01"
        } catch (Exception e) {
            erros.add("Data inválida.");
        }

        if (!erros.isEmpty()) {
            Json.enviar(response, Json.erros(erros));
            return;
        }

        // O horário ainda está livre?
        if (!agenda.horarioDisponivel(data, hora)) {
            enviarHorarioOcupado(response);
            return;
        }

        repo.salvarCliente(nome, whatsapp, celular, email);
        Cliente cliente = repo.buscarClientePorEmail(email);

        String profissional = repo.registrarAgendamento(cliente.getId(), servico.getId(), data.toString(), hora);
        if (profissional == null) {
            enviarHorarioOcupado(response);
            return;
        }

        String json = "{\"servico\":" + Json.texto(servico.getNome())
                + ",\"data\":" + Json.texto(data.toString())
                + ",\"hora\":" + Json.texto(hora)
                + ",\"profissional\":" + Json.texto(profissional) + "}";
        Json.enviar(response, json);
    }

    /** O "ocupado" avisa o JavaScript para voltar ao calendário. */
    private void enviarHorarioOcupado(HttpServletResponse response) throws IOException {
        Json.enviar(response, "{\"ocupado\":true,"
                + "\"erros\":[\"Esse horário acabou de ser ocupado. Escolha outro, por favor.\"]}");
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
