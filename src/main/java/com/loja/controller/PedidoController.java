package com.loja.controller;

import com.loja.dto.ItemRequest;
import com.loja.dto.OrderResponse;
import com.loja.dto.PedidoRequest;
import com.loja.exception.ValidationException;
import com.loja.model.Order;
import com.loja.service.PedidoService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class PedidoController extends BaseController {

    private final PedidoService service;

    public PedidoController(PedidoService service) {
        this.service = service;
    }

    @Override
    protected void rotear(HttpExchange ex) throws IOException {
        String metodo   = ex.getRequestMethod();
        String[] partes = ex.getRequestURI().getPath().split("/");
        Integer id      = idDoCaminho(ex);
        String query    = ex.getRequestURI().getQuery();

        // Sub-recursos: /pedidos/{id}/itens e /pedidos/{id}/finalizacao
        // split("/") de "/pedidos/7/itens" → ["", "pedidos", "7", "itens"] → length 4
        if (partes.length == 4 && "POST".equals(metodo)) {
            exigirId(id, "pedidos");
            if ("itens".equals(partes[3]))       { adicionarItem(ex, id); return; }
            if ("finalizacao".equals(partes[3])) { finalizar(ex, id);     return; }
        }

        switch (metodo) {
            case "POST"   -> criar(ex);
            case "GET"    -> {
                // Query string: /pedidos?clienteId=1 → lê com getQuery() e faz split em "="
                if (query != null && query.startsWith("clienteId=")) {
                    listarPorCliente(ex, query);
                } else {
                    exigirId(id, "pedidos");
                    buscar(ex, id);
                }
            }
            case "DELETE" -> { exigirId(id, "pedidos"); deletar(ex, id); }
            default       -> enviarErro(ex, 405, "Method Not Allowed",
                                "O método " + metodo + " não é suportado aqui.");
        }
    }

    // POST /pedidos → 201 Created (corpo: { "clienteId": 1 })
    private void criar(HttpExchange ex) throws IOException {
        PedidoRequest req = lerCorpo(ex, PedidoRequest.class);
        Order order = service.criarPedido(req.clienteId());
        enviarJson(ex, 201, OrderResponse.from(order));
    }

    // GET /pedidos/{id} → 200 OK
    private void buscar(HttpExchange ex, int id) throws IOException {
        enviarJson(ex, 200, OrderResponse.from(service.buscar(id)));
    }

    // GET /pedidos?clienteId=1 → 200 OK
    private void listarPorCliente(HttpExchange ex, String query) throws IOException {
        try {
            int clienteId = Integer.parseInt(query.split("=")[1]);
            List<OrderResponse> corpo = service.listarPorCliente(clienteId).stream()
                    .map(OrderResponse::from)
                    .toList();
            enviarJson(ex, 200, corpo);
        } catch (NumberFormatException e) {
            throw new ValidationException("clienteId deve ser um número inteiro.");
        }
    }

    // POST /pedidos/{id}/itens → 201 Created
    private void adicionarItem(HttpExchange ex, int id) throws IOException {
        ItemRequest req = lerCorpo(ex, ItemRequest.class);
        Order order = service.adicionarItem(id, req.produtoId(), req.quantidade());
        enviarJson(ex, 201, OrderResponse.from(order));
    }

    // POST /pedidos/{id}/finalizacao → 200 OK
    private void finalizar(HttpExchange ex, int id) throws IOException {
        Order order = service.buscar(id);
        Order finalizado = service.finalizarPedido(order);
        enviarJson(ex, 200, OrderResponse.from(finalizado));
    }

    // DELETE /pedidos/{id} → 204 No Content (itens vão junto via cascade)
    private void deletar(HttpExchange ex, int id) throws IOException {
        service.deletar(id);
        enviarVazio(ex, 204);
    }
}
