package br.com.clinica.controller;

import br.com.clinica.model.Horario;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

/**
 * Endereços da agenda:
 *   api/agenda/dias?mes=2026-12             -> dias do mês que têm vaga
 *   api/agenda/horarios?data=2026-12-01     -> horários do dia (livres e ocupados)
 */
@WebServlet({"/api/agenda/dias", "/api/agenda/horarios"})
public class AgendaServlet extends HttpServlet {

    private final AgendaService agenda = new AgendaService();

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        if (request.getServletPath().equals("/api/agenda/dias")) {
            enviarDias(request, response);
        } else {
            enviarHorarios(request, response);
        }
    }

    private void enviarDias(HttpServletRequest request, HttpServletResponse response) throws IOException {
        YearMonth mes = agenda.primeiroMes();
        try {
            mes = YearMonth.parse(request.getParameter("mes"));   // "2026-12"
        } catch (Exception e) {
            // sem mês ou mês inválido: fica o mês atual
        }

        // Não deixa sair do período liberado
        if (mes.isBefore(agenda.primeiroMes())) {
            mes = agenda.primeiroMes();
        }
        if (mes.isAfter(agenda.ultimoMes())) {
            mes = agenda.ultimoMes();
        }

        List<String> dias = new ArrayList<>();
        for (String dia : agenda.diasDisponiveis(mes)) {
            dias.add(Json.texto(dia));
        }

        String json = "{\"mes\":" + Json.texto(mes.toString())
                + ",\"primeiroMes\":" + Json.texto(agenda.primeiroMes().toString())
                + ",\"ultimoMes\":" + Json.texto(agenda.ultimoMes().toString())
                + ",\"diasDisponiveis\":" + Json.lista(dias) + "}";
        Json.enviar(response, json);
    }

    private void enviarHorarios(HttpServletRequest request, HttpServletResponse response) throws IOException {
        LocalDate data;
        try {
            data = LocalDate.parse(request.getParameter("data"));   // "2026-12-01"
        } catch (Exception e) {
            Json.enviar(response, Json.erro("Data inválida."));
            return;
        }

        List<String> itens = new ArrayList<>();
        for (Horario horario : agenda.horariosDoDia(data)) {
            itens.add(horario.toJson());
        }
        Json.enviar(response, Json.lista(itens));
    }
}
