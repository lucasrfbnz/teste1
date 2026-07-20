package com.loja.controller;

import com.loja.dto.EstoqueRequest;
import com.loja.dto.ProductRequest;
import com.loja.dto.ProductResponse;
import com.loja.model.Product;
import com.loja.service.ProdutoService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class ProdutoController extends BaseController {

    private final ProdutoService service;

    public ProdutoController(ProdutoService service) {
        this.service = service;
    }

    @Override
    protected void rotear(HttpExchange ex) throws IOException {
        String metodo   = ex.getRequestMethod();
        String[] partes = ex.getRequestURI().getPath().split("/");
        Integer id      = idDoCaminho(ex);

        // Sub-recurso: POST /produtos/{id}/estoque
        if (partes.length == 4 && "estoque".equals(partes[3]) && "POST".equals(metodo)) {
            exigirId(id, "produtos");
            reporEstoque(ex, id);
            return;
        }

        switch (metodo) {
            case "POST"   -> criar(ex);
            case "GET"    -> { if (id == null) listar(ex); else buscar(ex, id); }
            case "DELETE" -> { exigirId(id, "produtos"); desativar(ex, id); }
            default       -> enviarErro(ex, 405, "Method Not Allowed",
                                "O método " + metodo + " não é suportado aqui.");
        }
    }

    // POST /produtos → 201 Created
    private void criar(HttpExchange ex) throws IOException {
        ProductRequest req = lerCorpo(ex, ProductRequest.class);
        req.validar();
        Product criado = service.cadastrar(req.toModel());
        enviarJson(ex, 201, ProductResponse.from(criado));
    }

    // GET /produtos → 200 OK
    private void listar(HttpExchange ex) throws IOException {
        List<ProductResponse> corpo = service.listarTodos().stream()
                .map(ProductResponse::from)
                .toList();
        enviarJson(ex, 200, corpo);
    }

    // GET /produtos/{id} → 200 OK | 404 Not Found
    private void buscar(HttpExchange ex, int id) throws IOException {
        enviarJson(ex, 200, ProductResponse.from(service.buscar(id)));
    }

    // POST /produtos/{id}/estoque → 200 OK
    private void reporEstoque(HttpExchange ex, int id) throws IOException {
        EstoqueRequest req = lerCorpo(ex, EstoqueRequest.class);
        Product atualizado = service.reporEstoque(id, req.quantidade());
        enviarJson(ex, 200, ProductResponse.from(atualizado));
    }

    // DELETE /produtos/{id} → 204 No Content
    private void desativar(HttpExchange ex, int id) throws IOException {
        service.desativar(id);
        enviarVazio(ex, 204);
    }
}
