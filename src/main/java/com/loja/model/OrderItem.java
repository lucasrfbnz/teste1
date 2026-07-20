package com.loja.model;

import jakarta.persistence.*;

// AVISO HONESTO: OrderItem perde os campos final. O Hibernate precisa de um construtor
// sem argumentos e de escrever nos campos por reflexão. Mitigamos mantendo o construtor
// sem-arg protegido e sem nenhum setter público — imutável na prática.
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id")
    private Order order;

    // AGREGAÇÃO com Product — sem cascade
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "produto_id")
    private Product produto;

    private int quantidade;

    @Column(name = "preco_unitario")
    private double precoUnitario; // congelado no momento da compra

    protected OrderItem() {}

    OrderItem(Order order, Product produto, int quantidade, double precoUnitario) {
        this.order = order;
        this.produto = produto;
        this.quantidade = quantidade;
        this.precoUnitario = precoUnitario;
    }

    public Integer getId() { return id; }
    public Order getOrder() { return order; }
    public Product getProduto() { return produto; }
    public int getQuantidade() { return quantidade; }
    public double getPrecoUnitario() { return precoUnitario; }

    public double calcularSubtotal() {
        return precoUnitario * quantidade;
    }
}
