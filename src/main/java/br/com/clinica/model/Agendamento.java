package br.com.clinica.model;

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

    public String getData() { return data; }
    public String getHora() { return hora; }
    public String getServico() { return servico; }
    public String getProfissional() { return profissional; }
}
