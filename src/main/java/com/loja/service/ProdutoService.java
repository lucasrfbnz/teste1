package com.loja.service;

import com.loja.exception.NotFoundException;
import com.loja.model.Product;
import com.loja.repository.ProductRepository;

import java.util.List;

public class ProdutoService {

    private final ProductRepository repo;

    public ProdutoService(ProductRepository repo) {
        this.repo = repo;
    }

    public Product cadastrar(Product produto) {
        repo.inserir(produto);
        return produto;
    }

    public Product buscar(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Produto com id " + id + " não encontrado."));
    }

    public boolean existe(int id) {
        return repo.buscarPorId(id).isPresent();
    }

    public List<Product> listarTodos() {
        return repo.listarTodos();
    }

    public void desativar(int id) {
        buscar(id);
        repo.desativar(id);
    }

    public Product reporEstoque(int id, int quantidade) {
        Product p = buscar(id);
        p.reporEstoque(quantidade);
        repo.atualizar(p);
        return p;
    }
}
