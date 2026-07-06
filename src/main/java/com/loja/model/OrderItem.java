package com.loja.model;

/**
 * Parte da COMPOSIÇÃO: só existe dentro de um Order.
 * Imutável após criado (todos os atributos são final, sem setters).
 *
 * AGREGAÇÃO com Product: guarda apenas produtoId (int) + cópia do
 * preço no momento da compra, não o objeto Product inteiro.
 */
public class OrderItem {

    private final int id;
    private final int produtoId;
    private final int quantidade;
    private final double precoUnitario; // congelado no momento da compra

    public OrderItem(int id, int produtoId, int quantidade, double precoUnitario) {
        this.id = id;
        this.produtoId = produtoId;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public int getId() { return id; }
    public int getProdutoId() { return produtoId; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }

    public double calcularSubtotal() {
        return precoUnitario * quantidade;
    }

    @Override
    public String toString() {
        return "OrderItem{id=" + id + ", produtoId=" + produtoId
                + ", quantidade=" + quantidade + ", precoUnitario=" + precoUnitario
                + ", subtotal=" + calcularSubtotal() + "}";
    }
}
