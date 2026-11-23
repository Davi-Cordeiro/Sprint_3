package br.com.clinica.controller;

import br.com.clinica.model.Cliente;
import br.com.clinica.model.Horario;
import br.com.clinica.model.Servico;
import br.com.clinica.repository.ClinicaRepository;
import br.com.clinica.service.AgendaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Agenda: dias livres do mês, horários livres do dia e gravação do agendamento. */
@RestController
public class AgendaController {

    private final ClinicaRepository repo;
    private final AgendaService agenda;

    public AgendaController(ClinicaRepository repo, AgendaService agenda) {
        this.repo = repo;
        this.agenda = agenda;
    }

    /**
     * Dias com vaga no mês. Exemplo: /api/agenda/dias?mes=2026-12
     * Também diz o primeiro e o último mês liberados (para as setas do calendário).
     */
    @GetMapping("/api/agenda/dias")
    public Map<String, Object> dias(@RequestParam(required = false) String mes) {
        YearMonth mesEscolhido = agenda.primeiroMes();
        if (mes != null) {
            mesEscolhido = YearMonth.parse(mes);
        }
        // Não deixa sair do período liberado
        if (mesEscolhido.isBefore(agenda.primeiroMes())) {
            mesEscolhido = agenda.primeiroMes();
        }
        if (mesEscolhido.isAfter(agenda.ultimoMes())) {
            mesEscolhido = agenda.ultimoMes();
        }

        Map<String, Object> resposta = new HashMap<>();
        resposta.put("mes", mesEscolhido.toString());
        resposta.put("primeiroMes", agenda.primeiroMes().toString());
        resposta.put("ultimoMes", agenda.ultimoMes().toString());
        resposta.put("diasDisponiveis", agenda.diasDisponiveis(mesEscolhido));
        return resposta;
    }

    /** Horários do dia, livres e ocupados. Exemplo: /api/agenda/horarios?data=2026-12-01 */
    @GetMapping("/api/agenda/horarios")
    public List<Horario> horarios(@RequestParam String data) {
        return agenda.horariosDoDia(LocalDate.parse(data));
    }

    /**
     * Grava o agendamento. Recebe do JavaScript:
     * idServico, data, hora, nome, whatsapp, celular e email.
     */
    @PostMapping("/api/agendamentos")
    public ResponseEntity<Object> agendar(@RequestBody Map<String, String> dados) {
        String nome = dados.getOrDefault("nome", "").trim();
        String whatsapp = dados.getOrDefault("whatsapp", "").trim();
        String celular = dados.getOrDefault("celular", "").trim();
        String email = dados.getOrDefault("email", "").trim();
        String hora = dados.getOrDefault("hora", "");

        // Confere os dados
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
            servico = repo.buscarServico(Integer.parseInt(dados.get("idServico")));
        } catch (NumberFormatException e) {
            // continua null
        }
        if (servico == null) {
            erros.add("Procedimento inválido.");
        }

        LocalDate data = null;
        try {
            data = LocalDate.parse(dados.get("data"));
        } catch (DateTimeParseException | NullPointerException e) {
            erros.add("Data inválida.");
        }

        if (!erros.isEmpty()) {
            return ResponseEntity.status(400).body(Map.of("erros", erros));
        }

        // O horário ainda está livre?
        if (!agenda.horarioDisponivel(data, hora)) {
            return horarioOcupado();
        }

        repo.salvarCliente(nome, whatsapp, celular, email);
        Cliente cliente = repo.buscarClientePorEmail(email);

        String profissional = repo.registrarAgendamento(cliente.getId(), servico.getId(), data.toString(), hora);
        if (profissional == null) {
            return horarioOcupado();
        }

        Map<String, String> resposta = new HashMap<>();
        resposta.put("servico", servico.getNome());
        resposta.put("data", data.toString());
        resposta.put("hora", hora);
        resposta.put("profissional", profissional);
        return ResponseEntity.ok(resposta);
    }

    private ResponseEntity<Object> horarioOcupado() {
        Map<String, Object> resposta = new HashMap<>();
        resposta.put("erros", List.of("Esse horário acabou de ser ocupado. Escolha outro, por favor."));
        resposta.put("ocupado", true);
        return ResponseEntity.status(409).body(resposta);
    }
}
