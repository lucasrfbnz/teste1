package com.loja.model;

/**
 * Entidade de domínio: representa um produto do catálogo.
 *
 * AGREGAÇÃO: OrderItem -> Product (fraca).
 * O produto existe independentemente de qualquer item de pedido.
 * OrderItem guarda apenas o produtoId (int) e uma cópia do preço
 * no momento da compra — não guarda este objeto inteiro.
 */
public class Product {

    private int id;
    private String nome;
    private double preco;
    private int estoque;

    public Product(int id, String nome, double preco) {
        this.id = id;
        this.nome = nome;
        this.preco = preco;
        this.estoque = 0;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public void reporEstoque(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        this.estoque += quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        if (quantidade > this.estoque) throw new IllegalStateException(
                "Estoque insuficiente para '" + nome + "'. Atual: " + estoque + ", solicitado: " + quantidade);
        this.estoque -= quantidade;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco + ", estoque=" + estoque + "}";
    }
}
