package br.com.clinica.model;

import br.com.clinica.controller.Json;

/** Um agendamento para mostrar na tela "Minha conta". */
public class Agendamento {

    private String data;          // "2026-12-01"
    private String hora;          // "09:00"
    private String servico;       // nome do procedimento
    private String profissional;  // nome do profissional

    public Agendamento(String data, String hora, String servico, String profissional) {
        this.data = data;
        this.hora = hora;
        this.servico = servico;
        this.profissional = profissional;
    }

    public String toJson() {
        return "{\"data\":" + Json.texto(data) + ",\"hora\":" + Json.texto(hora)
                + ",\"servico\":" + Json.texto(servico) + ",\"profissional\":" + Json.texto(profissional) + "}";
    }
}
