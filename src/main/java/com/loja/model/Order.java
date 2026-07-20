package com.loja.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Entity
@Table(name = "orders")
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    // AGREGAÇÃO (Order -> Client): há uma referência, mas SEM cascade.
    // O pedido NÃO controla o ciclo de vida do cliente: apagar o pedido não apaga o cliente.
    // LAZY = só carrega o Client se alguém pedir.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "cliente_id")
    private Client cliente;

    @Column(nullable = false)
    private LocalDate data;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(nullable = false)
    private boolean finalizado;

    // COMPOSIÇÃO (Order -> OrderItem): cascade = ALL + orphanRemoval = true.
    // "O que acontece ao pedido acontece aos itens": salvou o pedido, salvou os itens;
    // apagou o pedido, apagou os itens. E se um item for removido da lista, ele vira
    // "órfão" e é DELETADO do banco. Isto é a composição, expressa em JPA.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> itens = new ArrayList<>();

    protected Order() {}

    public Order(Client cliente) {
        this.cliente = cliente;
        this.data = LocalDate.now();
        this.status = "PENDENTE";
        this.finalizado = false;
    }

    // Regra de domínio pura — continua exatamente como estava
    public void adicionarItem(Product produto, int quantidade) {
        if (finalizado) throw new IllegalStateException("Pedido finalizado não pode receber itens.");
        itens.add(new OrderItem(this, produto, quantidade, produto.getPreco()));
    }

    public void removerItem(int index) {
        if (finalizado) throw new IllegalStateException("Pedido finalizado não pode ter itens removidos.");
        if (index < 0 || index >= itens.size()) throw new IndexOutOfBoundsException("Índice inválido: " + index);
        itens.remove(index);
    }

    public void finalizarPedido() {
        this.status = "PAGO";
        this.finalizado = true;
    }

    public double calcularTotal() {
        return itens.stream().mapToDouble(OrderItem::calcularSubtotal).sum();
    }

    public int getQuantidadeItens() { return itens.size(); }

    public Integer getId() { return id; }
    public void setId(int id) { this.id = id; }

    public Client getCliente() { return cliente; }

    public LocalDate getData() { return data; }
    public void setData(LocalDate data) { this.data = data; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public boolean isFinalizado() { return finalizado; }
    public void setFinalizado(boolean finalizado) { this.finalizado = finalizado; }

    public List<OrderItem> getItens() { return Collections.unmodifiableList(itens); }
}
