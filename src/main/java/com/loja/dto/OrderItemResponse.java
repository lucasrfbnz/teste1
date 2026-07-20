package com.loja.dto;

public record OrderItemResponse(int produtoId, int quantidade, double precoUnitario, double subtotal) {}
