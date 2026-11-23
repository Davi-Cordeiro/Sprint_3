package br.com.clinica.model;

import br.com.clinica.controller.Json;

/** Um procedimento da clínica (tabela servico). */
public class Servico {

    private int id;
    private String nome;
    private double preco;
    private String descricao;

    public Servico(int id, String nome, double preco, String descricao) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.descricao = descricao;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public double getPreco() { return preco; }
    public String getDescricao() { return descricao; }

    /** Ex.: {"id":1,"nome":"Limpeza de pele","preco":150.0,"descricao":"..."} */
    public String toJson() {
        return "{\"id\":" + id
                + ",\"nome\":" + Json.texto(nome)
                + ",\"preco\":" + preco
                + ",\"descricao\":" + Json.texto(descricao) + "}";
    }
}
