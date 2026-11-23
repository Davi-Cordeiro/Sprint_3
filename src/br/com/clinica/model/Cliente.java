package br.com.clinica.model;

import br.com.clinica.controller.Json;

import java.io.Serializable;

/** Um cliente (tabela cliente). Também é quem fica guardado na sessão depois do login. */
public class Cliente implements Serializable {

    private int id;
    private String nome;
    private String email;

    public Cliente(int id, String nome, String email) {
        this.id = id;
        this.nome = nome;
        this.email = email;
    }

    public int getId() { return id; }
    public String getNome() { return nome; }
    public String getEmail() { return email; }

    public String toJson() {
        return "{\"id\":" + id
                + ",\"nome\":" + Json.texto(nome)
                + ",\"email\":" + Json.texto(email) + "}";
    }
}
