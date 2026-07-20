package com.loja.dto;

import com.loja.model.Order;

import java.time.LocalDate;
import java.util.List;

public record OrderResponse(
        int id,
        int clienteId,
        LocalDate data,
        String status,
        boolean finalizado,
        List<OrderItemResponse> itens,
        double total) {

    /** Fábrica: model -> DTO de resposta. total é um campo calculado. */
    public static OrderResponse from(Order o) {
        List<OrderItemResponse> itens = o.getItens().stream()
                .map(i -> new OrderItemResponse(
                        i.getProduto().getId(),
                        i.getQuantidade(),
                        i.getPrecoUnitario(),
                        i.calcularSubtotal()))
                .toList();
        return new OrderResponse(
                o.getId(),
                o.getCliente().getId(),
                o.getData(),
                o.getStatus(),
                o.isFinalizado(),
                itens,
                o.calcularTotal());
    }
}
