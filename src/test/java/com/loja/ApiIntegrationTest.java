package com.loja;

import com.loja.controller.ClienteController;
import com.loja.controller.PedidoController;
import com.loja.controller.ProdutoController;
import com.loja.repository.ClientRepositoryJPA;
import com.loja.repository.OrderRepositoryJPA;
import com.loja.repository.ProductRepositoryJPA;
import com.loja.service.ClienteService;
import com.loja.service.PedidoService;
import com.loja.service.ProdutoService;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.*;

import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ApiIntegrationTest {

    private static HttpServer server;
    private static final HttpClient http = HttpClient.newHttpClient();
    private static final String BASE = "http://localhost:8081";

    // CPF de no máximo 14 chars, único por rodada via nanoTime (coluna = length 14)
    private static final String SUFFIX = String.format("%011d", Math.abs(System.nanoTime()) % 100000000000L);
    private static final String CPF1 = "A" + SUFFIX; // teste de criação
    private static final String CPF2 = "B" + SUFFIX; // teste de duplicata
    private static final String CPF3 = "C" + SUFFIX; // fluxo completo

    @BeforeAll
    static void startServer() throws Exception {
        ClientRepositoryJPA  clientRepo  = new ClientRepositoryJPA();
        ProductRepositoryJPA productRepo = new ProductRepositoryJPA();
        OrderRepositoryJPA   orderRepo   = new OrderRepositoryJPA();

        ClienteService clienteSvc = new ClienteService(clientRepo);
        ProdutoService produtoSvc = new ProdutoService(productRepo);
        PedidoService  pedidoSvc  = new PedidoService(orderRepo, clienteSvc, produtoSvc);

        server = HttpServer.create(new InetSocketAddress(8081), 0);
        server.createContext("/clientes", new ClienteController(clienteSvc));
        server.createContext("/produtos",  new ProdutoController(produtoSvc));
        server.createContext("/pedidos",   new PedidoController(pedidoSvc));
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
        server.start();
    }

    @AfterAll
    static void stopServer() {
        if (server != null) server.stop(0);
    }

    // --- Helpers ---

    private HttpResponse<String> post(String path, String body) throws Exception {
        return http.send(
            HttpRequest.newBuilder(URI.create(BASE + path))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(body))
                .build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> get(String path) throws Exception {
        return http.send(
            HttpRequest.newBuilder(URI.create(BASE + path)).GET().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    private HttpResponse<String> delete(String path) throws Exception {
        return http.send(
            HttpRequest.newBuilder(URI.create(BASE + path)).DELETE().build(),
            HttpResponse.BodyHandlers.ofString());
    }

    // --- Testes de Cliente ---

    @Test @Order(1)
    void postClientes_retorna201() throws Exception {
        var r = post("/clientes",
            "{\"nome\":\"Teste\",\"email\":\"t@t.com\",\"telefone\":\"\",\"cpf\":\"" + CPF1 + "\"}");
        assertEquals(201, r.statusCode());
    }

    @Test @Order(2)
    void postClientes_cpfDuplicado_retorna409() throws Exception {
        post("/clientes", "{\"nome\":\"A\",\"email\":\"a@a.com\",\"telefone\":\"\",\"cpf\":\"" + CPF2 + "\"}");
        var r = post("/clientes", "{\"nome\":\"B\",\"email\":\"b@b.com\",\"telefone\":\"\",\"cpf\":\"" + CPF2 + "\"}");
        assertEquals(409, r.statusCode());
    }

    @Test @Order(3)
    void postClientes_nomeVazio_retorna400() throws Exception {
        var r = post("/clientes",
            "{\"nome\":\"\",\"email\":\"x@x.com\",\"telefone\":\"\",\"cpf\":\"INVALIDO\"}");
        assertEquals(400, r.statusCode());
    }

    @Test @Order(4)
    void getClientes_idInexistente_retorna404() throws Exception {
        assertEquals(404, get("/clientes/999999").statusCode());
    }

    // --- Testes de Produto ---

    @Test @Order(5)
    void postProdutos_retorna201() throws Exception {
        var r = post("/produtos", "{\"nome\":\"Notebook\",\"preco\":3500.00,\"estoque\":5}");
        assertEquals(201, r.statusCode());
    }

    // --- Testes de Pedido ---

    @Test @Order(6)
    void fluxoCompleto_pedido() throws Exception {
        // cria cliente
        var c = post("/clientes",
            "{\"nome\":\"Pedido Test\",\"email\":\"p@t.com\",\"telefone\":\"\",\"cpf\":\"" + CPF3 + "\"}");
        assertEquals(201, c.statusCode());
        int clienteId = Integer.parseInt(c.body().split("\"id\":")[1].split(",")[0].trim());

        // cria produto
        var p = post("/produtos", "{\"nome\":\"Item\",\"preco\":10.0,\"estoque\":5}");
        assertEquals(201, p.statusCode());
        int produtoId = Integer.parseInt(p.body().split("\"id\":")[1].split(",")[0].trim());

        // cria pedido
        var o = post("/pedidos", "{\"clienteId\":" + clienteId + "}");
        assertEquals(201, o.statusCode());
        int pedidoId = Integer.parseInt(o.body().split("\"id\":")[1].split(",")[0].trim());

        // adiciona item
        var item = post("/pedidos/" + pedidoId + "/itens",
            "{\"produtoId\":" + produtoId + ",\"quantidade\":2}");
        assertEquals(201, item.statusCode());

        // GET pedido traz itens
        assertEquals(200, get("/pedidos/" + pedidoId).statusCode());

        // finaliza pedido
        assertEquals(200, post("/pedidos/" + pedidoId + "/finalizacao", "").statusCode());

        // pedido finalizado recusa novo item
        var bloqueado = post("/pedidos/" + pedidoId + "/itens",
            "{\"produtoId\":" + produtoId + ",\"quantidade\":1}");
        assertEquals(400, bloqueado.statusCode());

        // delete pedido (cascade remove itens)
        assertEquals(204, delete("/pedidos/" + pedidoId).statusCode());
    }
}
