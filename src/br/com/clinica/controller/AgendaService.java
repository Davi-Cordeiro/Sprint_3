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
 * Regras da agenda.
 * - Seg a sex: 9h às 18h (pausa 12h-13h). Sábado: 9h às 13h. Domingo: fechado.
 * - Um horário tem vaga enquanto houver algum profissional livre nele.
 * - Dá para agendar do mês atual até 2 meses à frente.
 */
public class AgendaService {

    private static final int MESES_A_FRENTE = 2;

    private static final String[] HORARIOS_SEMANA = {"09:00", "10:00", "11:00", "13:00", "14:00", "15:00", "16:00", "17:00"};
    private static final String[] HORARIOS_SABADO = {"09:00", "10:00", "11:00", "12:00"};
    private static final String[] FECHADO = {};

    private final ClinicaRepository repo = new ClinicaRepository();

    public YearMonth primeiroMes() {
        return YearMonth.now();
    }

    public YearMonth ultimoMes() {
        return YearMonth.now().plusMonths(MESES_A_FRENTE);
    }

    /** Dias do mês que ainda têm pelo menos um horário livre. Ex.: ["2026-12-01", "2026-12-02"] */
    public List<String> diasDisponiveis(YearMonth mes) {
        LocalDate primeiroDia = mes.atDay(1);
        LocalDate ultimoDia = mes.atEndOfMonth();

        // Busca a ocupação do mês inteiro de uma vez só
        Map<String, Integer> ocupacao = repo.ocupacaoEntre(primeiroDia.toString(), ultimoDia.toString());
        int profissionais = repo.contarProfissionais();

        List<String> dias = new ArrayList<>();
        for (LocalDate dia = primeiroDia; !dia.isAfter(ultimoDia); dia = dia.plusDays(1)) {
            List<Horario> horarios = montarHorarios(dia, ocupacao, profissionais);
            for (Horario horario : horarios) {
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
        int profissionais = repo.contarProfissionais();
        return montarHorarios(dia, ocupacao, profissionais);
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
        if (!dentroDoPeriodo(dia)) {
            return lista;
        }

        for (String hora : horariosDeFuncionamento(dia)) {
            LocalDateTime inicio = dia.atTime(LocalTime.parse(hora));
            boolean jaPassou = inicio.isBefore(LocalDateTime.now());

            Integer ocupados = ocupacao.get(dia + " " + hora);
            if (ocupados == null) {
                ocupados = 0;
            }
            boolean lotado = ocupados >= profissionais;

            lista.add(new Horario(hora, !jaPassou && !lotado));
        }
        return lista;
    }

    private String[] horariosDeFuncionamento(LocalDate dia) {
        if (dia.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return FECHADO;
        }
        if (dia.getDayOfWeek() == DayOfWeek.SATURDAY) {
            return HORARIOS_SABADO;
        }
        return HORARIOS_SEMANA;
    }

    /** O dia está entre hoje e o último dia liberado para agendar? */
    private boolean dentroDoPeriodo(LocalDate dia) {
        LocalDate hoje = LocalDate.now();
        LocalDate ultimoDiaLiberado = ultimoMes().atEndOfMonth();
        return !dia.isBefore(hoje) && !dia.isAfter(ultimoDiaLiberado);
    }
}
