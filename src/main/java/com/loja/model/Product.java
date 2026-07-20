package com.loja.model;

import com.loja.exception.ValidationException;
import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "product")
@SQLRestriction("is_active = true")
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false)
    private double preco;

    @Column(nullable = false)
    private int estoque;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    protected Product() {}

    public Product(String nome, double preco) {
        this.nome = nome;
        this.preco = preco;
        this.estoque = 0;
        this.active = true;
    }

    public Integer getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public double getPreco() { return preco; }
    public void setPreco(double preco) { this.preco = preco; }

    public int getEstoque() { return estoque; }
    public void setEstoque(int estoque) { this.estoque = estoque; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    public void reporEstoque(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        this.estoque += quantidade;
    }

    public void baixarEstoque(int quantidade) {
        if (quantidade < 0) throw new IllegalArgumentException("Quantidade não pode ser negativa.");
        if (quantidade > this.estoque) throw new ValidationException(
                "Estoque insuficiente para '" + nome + "'. Atual: " + estoque + ", solicitado: " + quantidade);
        this.estoque -= quantidade;
    }

    @Override
    public String toString() {
        return "Produto{id=" + id + ", nome='" + nome + "', preco=" + preco + ", estoque=" + estoque + "}";
    }
}
