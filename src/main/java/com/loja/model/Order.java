package com.loja.model;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Entidade de domínio: representa um pedido.
 *
 * AGREGAÇÃO com Client: guarda apenas clienteId (int).
 * COMPOSIÇÃO com OrderItem: cria e controla o ciclo de vida dos itens.
 *
 * Regra de ouro: o model protege apenas regras que dependem SÓ DELE
 * (ex: pedido finalizado não aceita mais itens). Validações que
 * dependem de outras entidades (cliente existe? produto existe?)
 * ficam na camada de serviço (PedidoService).
 */
public class Order {

    private int id;
    private int clienteId;         // AGREGAÇÃO: só o id, não o objeto Client
    private LocalDate data;
    private List<OrderItem> itens; // COMPOSIÇÃO: Order cria e owns os itens
    private String status;
    private boolean finalizado;

    public Order(int id, int clienteId) {
        this.id = id;
        this.clienteId = clienteId;
        this.data = LocalDate.now();
        this.itens = new ArrayList<>();
        this.status = "PENDENTE";
        this.finalizado = false;
    }

    // Regra de domínio pura: depende só do estado interno do próprio Order
    public void adicionarItem(int produtoId, int quantidade, double precoUnitario) {
        if (finalizado) throw new IllegalStateException("Pedido finalizado não pode receber itens.");
        int novoId = itens.size() + 1;
        itens.add(new OrderItem(novoId, produtoId, quantidade, precoUnitario));
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
        double total = 0;
        for (OrderItem item : itens) total += item.calcularSubtotal();
        return total;
    }

    public int getQuantidadeItens() { return itens.size(); }

    /**
     * Usado pelo OrderRepository para reconstruir um Order carregado
     * do banco — injeta itens diretamente sem checar finalizado,
     * pois o estado já vem persistido corretamente.
     */
    public void carregarItem(OrderItem item) {
        itens.add(item);
    }

    public void exibirResumo(Client cliente, Product... produtos) {
        System.out.println("===== Resumo do Pedido #" + id + " =====");
        System.out.println("Data: " + data);
        System.out.println("Status: " + status + (finalizado ? " (finalizado)" : " (em aberto)"));
        System.out.println("Cliente: " + (cliente != null ? cliente.getNome() : "id=" + clienteId));
        System.out.println("Itens:");
        for (OrderItem item : itens) {
            Product prod = null;
            for (Product p : produtos) if (p.getId() == item.getProdutoId()) { prod = p; break; }
            String nome = prod != null ? prod.getNome() : "Produto#" + item.getProdutoId();
            System.out.println("  - " + nome + " | qtd: " + item.getQuantidade()
                    + " | preço unit.: R$ " + item.getPrecoUnitario()
                    + " | subtotal: R$ " + item.calcularSubtotal());
        }
        System.out.println("TOTAL: R$ " + calcularTotal());
        System.out.println("==========================================");
    }

    // Getters
    public int getId() { return id; }
    public int getClienteId() { return clienteId; }
    public LocalDate getData() { return data; }
    public String getStatus() { return status; }
    public boolean isFinalizado() { return finalizado; }
    public List<OrderItem> getItens() { return Collections.unmodifiableList(itens); }

    // Setters usados pelo repository ao reconstruir do banco
    public void setData(LocalDate data) { this.data = data; }
    public void setStatus(String status) { this.status = status; }
    public void setFinalizado(boolean finalizado) { this.finalizado = finalizado; }
}
