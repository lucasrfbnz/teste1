package com.loja.repository;

import com.loja.model.Order;
import com.loja.model.Product;

import java.util.List;
import java.util.Optional;

public interface OrderRepository {
    void inserir(Order order);
    Optional<Order> buscarPorId(int id);
    List<Order> listarTodos();
    List<Order> listarPorCliente(int clienteId);
    void atualizar(Order order);
    // Adiciona o item e atualiza o estoque do produto na mesma transação JPA.
    void adicionarItem(int orderId, Product produto, int quantidade);
    void deletar(int id);
}
