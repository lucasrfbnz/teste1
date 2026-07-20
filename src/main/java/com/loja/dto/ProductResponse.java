package com.loja.dto;

import com.loja.model.Product;

public record ProductResponse(int id, String nome, double preco, int estoque) {

    public static ProductResponse from(Product p) {
        return new ProductResponse(p.getId(), p.getNome(), p.getPreco(), p.getEstoque());
    }
}
