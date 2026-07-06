package com.loja.repository;

import com.loja.model.Order;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void inserir(Order order);
    Optional<Order> buscarPorId(int id);
    List<Order> listarTodos();
    List<Order> listarPorCliente(int clienteId);
    void atualizar(Order order);
    // atualiza o pedido E o estoque do produto na mesma transação (bônus)
    void atualizarComEstoque(Order order, int produtoId, int novoEstoque);
    void deletar(int id);
}
