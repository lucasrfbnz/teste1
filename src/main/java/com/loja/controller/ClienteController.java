package com.loja.controller;

import com.loja.dto.ClientRequest;
import com.loja.dto.ClientResponse;
import com.loja.model.Client;
import com.loja.service.ClienteService;
import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.util.List;

public class ClienteController extends BaseController {

    private final ClienteService service;

    public ClienteController(ClienteService service) {
        this.service = service;
    }

    @Override
    protected void rotear(HttpExchange ex) throws IOException {
        String metodo   = ex.getRequestMethod();
        String[] partes = ex.getRequestURI().getPath().split("/");

        // Sub-recurso: POST /clientes/{cpf}/reativacao — verificar antes de idDoCaminho
        // porque partes[2] pode ser um CPF (string), não um número.
        if (partes.length == 4 && "reativacao".equals(partes[3]) && "POST".equals(metodo)) {
            reativar(ex, partes[2]);
            return;
        }

        Integer id = idDoCaminho(ex);

        switch (metodo) {
            case "POST"   -> criar(ex);
            case "GET"    -> { if (id == null) listar(ex); else buscar(ex, id); }
            case "PUT"    -> { exigirId(id, "clientes"); atualizar(ex, id); }
            case "DELETE" -> { exigirId(id, "clientes"); desativar(ex, id); }
            default       -> enviarErro(ex, 405, "Method Not Allowed",
                                "O método " + metodo + " não é suportado aqui.");
        }
    }

    // POST /clientes → 201 Created
    private void criar(HttpExchange ex) throws IOException {
        ClientRequest req = lerCorpo(ex, ClientRequest.class);
        req.validar();
        Client criado = service.cadastrar(req.toModel());
        enviarJson(ex, 201, ClientResponse.from(criado));
    }

    // GET /clientes → 200 OK
    private void listar(HttpExchange ex) throws IOException {
        List<ClientResponse> corpo = service.listarTodos().stream()
                .map(ClientResponse::from)
                .toList();
        enviarJson(ex, 200, corpo);
    }

    // GET /clientes/{id} → 200 OK | 404 Not Found
    private void buscar(HttpExchange ex, int id) throws IOException {
        enviarJson(ex, 200, ClientResponse.from(service.buscar(id)));
    }

    // PUT /clientes/{id} → 200 OK
    private void atualizar(HttpExchange ex, int id) throws IOException {
        ClientRequest req = lerCorpo(ex, ClientRequest.class);
        req.validar();
        Client atualizado = service.atualizar(id, req.toModel());
        enviarJson(ex, 200, ClientResponse.from(atualizado));
    }

    // DELETE /clientes/{id} → 204 No Content (soft delete: is_active = false)
    private void desativar(HttpExchange ex, int id) throws IOException {
        service.desativar(id);
        enviarVazio(ex, 204);
    }

    // POST /clientes/{cpf}/reativacao → 200 OK
    private void reativar(HttpExchange ex, String cpf) throws IOException {
        service.reativar(cpf);
        enviarVazio(ex, 200);
    }
}
