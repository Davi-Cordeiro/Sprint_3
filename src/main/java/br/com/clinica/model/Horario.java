package br.com.clinica.model;

/** Um horário do dia ("09:00") e se ainda tem vaga. */
public class Horario {

    private String hora;
    private boolean disponivel;

    public Horario(String hora, boolean disponivel) {
        this.hora = hora;
        this.disponivel = disponivel;
    }

    public String getHora() { return hora; }
    public boolean isDisponivel() { return disponivel; }
}
