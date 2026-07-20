package com.loja.dto;

import com.loja.exception.ValidationException;
import com.loja.model.Product;

public record ProductRequest(String nome, double preco, int estoque) {

    public void validar() {
        if (nome == null || nome.isBlank()) throw new ValidationException("O campo 'nome' é obrigatório.");
        if (preco <= 0)   throw new ValidationException("Preço deve ser positivo.");
        if (estoque < 0)  throw new ValidationException("Estoque não pode ser negativo.");
    }

    public Product toModel() {
        Product p = new Product(nome, preco);
        p.setEstoque(estoque);
        return p;
    }
}
