package br.com.clinica.model;

import br.com.clinica.controller.Json;

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

    /** Ex.: {"hora":"09:00","disponivel":true} */
    public String toJson() {
        return "{\"hora\":" + Json.texto(hora) + ",\"disponivel\":" + disponivel + "}";
    }
}
