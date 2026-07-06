package com.loja.repository;

import com.loja.model.Product;

import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    void inserir(Product p);
    Optional<Product> buscarPorId(int id);
    List<Product> listarTodos();
    void atualizar(Product p);
    void deletar(int id);
}
