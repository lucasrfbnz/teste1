package com.loja.service;

import com.loja.exception.NotFoundException;
import com.loja.model.Client;
import com.loja.model.Order;
import com.loja.model.Product;
import com.loja.repository.OrderRepository;

import java.util.List;

public class PedidoService {

    private final OrderRepository  orderRepo;
    private final ClienteService   clienteSvc;
    private final ProdutoService   produtoSvc;

    public PedidoService(OrderRepository orderRepo, ClienteService clienteSvc, ProdutoService produtoSvc) {
        this.orderRepo  = orderRepo;
        this.clienteSvc = clienteSvc;
        this.produtoSvc = produtoSvc;
    }

    public Order criarPedido(int clienteId) {
        Client client = clienteSvc.buscar(clienteId); // NotFoundException se não existir
        Order order = new Order(client);
        orderRepo.inserir(order);
        return order;
    }

    public Order adicionarItem(int pedidoId, int produtoId, int quantidade) {
        Order order = buscar(pedidoId);
        if (order.isFinalizado()) throw new IllegalStateException("Pedido finalizado não pode receber itens.");
        Product produto = produtoSvc.buscar(produtoId);
        produto.baixarEstoque(quantidade); // valida e calcula novo estoque em memória
        // O repositório JPA cuida de tudo em uma transação: novo item + estoque atualizado.
        orderRepo.adicionarItem(pedidoId, produto, quantidade);
        return buscar(pedidoId); // retorna estado fresco do banco
    }

    public Order finalizarPedido(Order order) {
        order.finalizarPedido();
        orderRepo.atualizar(order);
        return order;
    }

    public Order buscar(int id) {
        return orderRepo.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Pedido com id " + id + " não encontrado."));
    }

    public List<Order> listarPorCliente(int clienteId) {
        return orderRepo.listarPorCliente(clienteId);
    }

    public void deletar(int id) {
        buscar(id);
        orderRepo.deletar(id);
    }
}
