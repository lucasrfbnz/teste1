package com.loja.main;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Main {

    private static final Logger log = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) throws Exception {
        // 1. Monta as camadas de baixo para cima (injeção de dependência manual)
        ClientRepositoryJPA  clientRepo  = new ClientRepositoryJPA();
        ProductRepositoryJPA productRepo = new ProductRepositoryJPA();
        OrderRepositoryJPA   orderRepo   = new OrderRepositoryJPA();

        ClienteService clienteSvc = new ClienteService(clientRepo);
        ProdutoService produtoSvc = new ProdutoService(productRepo);
        PedidoService  pedidoSvc  = new PedidoService(orderRepo, clienteSvc, produtoSvc);

        // 2. Sobe o servidor e registra as controllers nos caminhos
        HttpServer server = HttpServer.create(new InetSocketAddress(8080), 0);
        server.createContext("/clientes", new ClienteController(clienteSvc));
        server.createContext("/produtos",  new ProdutoController(produtoSvc));
        server.createContext("/pedidos",   new PedidoController(pedidoSvc));

        // 3. Java 21: uma thread virtual por requisição — leves e baratas
        server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());

        server.start();
        log.info("API no ar em http://localhost:8080");
    }
}
