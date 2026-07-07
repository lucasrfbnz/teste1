package com.loja.service;

import com.loja.model.Product;
import com.loja.repository.ProductRepository;

import java.util.List;

public class ProdutoService {

    private final ProductRepository repo;

    public ProdutoService(ProductRepository repo) {
        this.repo = repo;
    }

    public void cadastrar(Product produto) {
        repo.inserir(produto);
        System.out.println("Produto cadastrado: " + produto.getNome() + " (id=" + produto.getId() + ")");
    }

    public Product buscar(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Produto com id " + id + " não encontrado."));
    }

    public boolean existe(int id) {
        return repo.buscarPorId(id).isPresent();
    }

    public List<Product> listarTodos() {
        return repo.listarTodos();
    }

    public void desativar(int id) {
        buscar(id); // valida que o produto existe e está ativo
        repo.desativar(id);
        System.out.println("Produto #" + id + " desativado.");
    }

    public void reporEstoque(int id, int quantidade) {
        Product p = buscar(id);
        p.reporEstoque(quantidade);
        repo.atualizar(p);
        System.out.println("Estoque de '" + p.getNome() + "' atualizado para " + p.getEstoque());
    }
}
