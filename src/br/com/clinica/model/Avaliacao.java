package br.com.clinica.model;

import br.com.clinica.controller.Json;

/** Uma avaliação para mostrar na página de avaliações. */
public class Avaliacao {

    private String nome;        // abreviado, ex.: "Mariana S."
    private String servico;
    private int nota;
    private String comentario;

    public Avaliacao(String nome, String servico, int nota, String comentario) {
        this.nome = nome;
        this.servico = servico;
        this.nota = nota;
        this.comentario = comentario;
    }

    public String getNome() { return nome; }
    public String getServico() { return servico; }
    public int getNota() { return nota; }
    public String getComentario() { return comentario; }

    public String toJson() {
        return "{\"nome\":" + Json.texto(nome)
                + ",\"servico\":" + Json.texto(servico)
                + ",\"nota\":" + nota
                + ",\"comentario\":" + Json.texto(comentario) + "}";
    }
}
