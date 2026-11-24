package br.com.clinica.controller;

import br.com.clinica.model.Horario;
import br.com.clinica.repository.ClinicaRepository;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Regras da agenda:
 * - Seg a sex: 9h às 18h (pausa 12h-13h). Sábado: 9h às 13h. Domingo: fechado.
 * - Um horário tem vaga enquanto houver algum profissional livre nele.
 * - Dá para agendar do mês atual até 2 meses à frente.
 */
public class AgendaService {

    private static final int MESES_A_FRENTE = 2;
    private static final String[] HORARIOS_SEMANA = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
    private static final String[] HORARIOS_SABADO = {"09:00", "10:00", "11:00", "12:00"};

    private final ClinicaRepository repo = new ClinicaRepository();

    /** Último mês em que já dá para agendar. */
    public YearMonth ultimoMes() {
        return YearMonth.now().plusMonths(MESES_A_FRENTE);
    }

    /** Dias do mês que ainda têm pelo menos um horário livre. Ex.: ["2026-12-01", "2026-12-02"] */
    public List<String> diasDisponiveis(YearMonth mes) {
        // Busca a ocupação do mês inteiro de uma vez só
        Map<String, Integer> ocupacao = repo.ocupacaoEntre(mes.atDay(1).toString(), mes.atEndOfMonth().toString());
        int profissionais = repo.contarProfissionais();

        List<String> dias = new ArrayList<>();
        for (LocalDate dia = mes.atDay(1); !dia.isAfter(mes.atEndOfMonth()); dia = dia.plusDays(1)) {
            for (Horario horario : montarHorarios(dia, ocupacao, profissionais)) {
                if (horario.isDisponivel()) {
                    dias.add(dia.toString());
                    break;   // achou um horário livre: o dia está disponível
                }
            }
        }
        return dias;
    }

    /** Todos os horários do dia, marcando quais ainda têm vaga. */
    public List<Horario> horariosDoDia(LocalDate dia) {
        Map<String, Integer> ocupacao = repo.ocupacaoEntre(dia.toString(), dia.toString());
        return montarHorarios(dia, ocupacao, repo.contarProfissionais());
    }

    /** O horário (ex.: "09:00") desse dia ainda está livre? */
    public boolean horarioDisponivel(LocalDate dia, String hora) {
        for (Horario horario : horariosDoDia(dia)) {
            if (horario.getHora().equals(hora) && horario.isDisponivel()) {
                return true;
            }
        }
        return false;
    }

    private List<Horario> montarHorarios(LocalDate dia, Map<String, Integer> ocupacao, int profissionais) {
        List<Horario> lista = new ArrayList<>();

        // Dia que já passou, depois do período liberado ou domingo: sem horários
        boolean foraDoPeriodo = dia.isBefore(LocalDate.now()) || dia.isAfter(ultimoMes().atEndOfMonth());
        if (foraDoPeriodo || dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return lista;
        }

        String[] horas = HORARIOS_SEMANA;
        if (dia.getDayOfWeek() == DayOfWeek.SATURDAY) {
            horas = HORARIOS_SABADO;
        }

        for (String hora : horas) {
            boolean jaPassou = dia.atTime(LocalTime.parse(hora)).isBefore(LocalDateTime.now());
            boolean lotado = ocupacao.getOrDefault(dia + " " + hora, 0) >= profissionais;
            lista.add(new Horario(hora, !jaPassou && !lotado));
        }
        return lista;
    }
}
