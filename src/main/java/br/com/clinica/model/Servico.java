package br.com.clinica.model;

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
}
