package com.loja.main;

import com.loja.model.Client;
import com.loja.model.Order;
import com.loja.model.Product;
import com.loja.repository.ClientRepositoryMySQL;
import com.loja.repository.OrderRepositoryMySQL;
import com.loja.repository.ProductRepositoryMySQL;
import com.loja.service.ClienteService;
import com.loja.service.PedidoService;
import com.loja.service.ProdutoService;

import java.util.List;
import java.util.Scanner;

/**
 * Ponto de entrada da aplicação — menu interativo no console.
 * Usa os Services (nunca acessa Repositories diretamente).
 * Aqui fazemos o "wiring" manual: criamos os repos e injetamos nos services.
 */
public class Main {

    private static final Scanner sc = new Scanner(System.in);

    // 1. criar as implementações MySQL dos repositórios
    private static final ClientRepositoryMySQL  clientRepo  = new ClientRepositoryMySQL();
    private static final ProductRepositoryMySQL productRepo = new ProductRepositoryMySQL();
    private static final OrderRepositoryMySQL   orderRepo   = new OrderRepositoryMySQL();

    // 2. injetar os repositórios nos services
    private static final ClienteService clienteSvc = new ClienteService(clientRepo);
    private static final ProdutoService produtoSvc = new ProdutoService(productRepo);
    private static final PedidoService  pedidoSvc  = new PedidoService(orderRepo, clienteSvc, produtoSvc);

    public static void main(String[] args) {
        System.out.println("===== LOJA — MENU =====");
        int opcao;
        do {
            menu();
            opcao = lerInt("Escolha: ");
            try {
                switch (opcao) {
                    case 1 -> cadastrarCliente();
                    case 2 -> listarClientes();
                    case 3 -> cadastrarProduto();
                    case 4 -> criarPedido();
                    case 5 -> listarPedidosPorCliente();
                    case 6 -> excluirPedido();
                    case 0 -> System.out.println("Encerrando...");
                    default -> System.out.println("Opção inválida.");
                }
            } catch (IllegalArgumentException | IllegalStateException e) {
                System.out.println("Erro: " + e.getMessage());
            } catch (RuntimeException e) {
                System.out.println("Erro de banco de dados: " + e.getMessage());
            }
            System.out.println();
        } while (opcao != 0);
    }

    private static void menu() {
        System.out.println("-----------------------");
        System.out.println("1. Cadastrar cliente");
        System.out.println("2. Listar clientes");
        System.out.println("3. Cadastrar produto");
        System.out.println("4. Criar pedido");
        System.out.println("5. Listar pedidos de um cliente");
        System.out.println("6. Excluir pedido");
        System.out.println("0. Sair");
        System.out.println("-----------------------");
    }

    // ---- handlers ----

    private static void cadastrarCliente() {
        int    id       = lerInt("Id: ");
        String nome     = lerString("Nome: ");
        String email    = lerString("Email: ");
        String telefone = lerString("Telefone: ");
        clienteSvc.cadastrar(new Client(id, nome, email, telefone));
    }

    private static void listarClientes() {
        List<Client> lista = clienteSvc.listarTodos();
        if (lista.isEmpty()) { System.out.println("Nenhum cliente cadastrado."); return; }
        lista.forEach(System.out::println);
    }

    private static void cadastrarProduto() {
        int    id     = lerInt("Id: ");
        String nome   = lerString("Nome: ");
        double preco  = lerDouble("Preço: ");
        int estoque   = lerInt("Estoque inicial: ");
        Product p = new Product(id, nome, preco);
        p.setEstoque(estoque);
        produtoSvc.cadastrar(p);
    }

    // Criar pedido já inclui o loop de adição de itens
    private static void criarPedido() {
        int pedidoId  = lerInt("Id do pedido: ");
        int clienteId = lerInt("Id do cliente: ");
        Order order = pedidoSvc.criarPedido(pedidoId, clienteId);

        System.out.println("--- Adicionar itens (0 para terminar) ---");
        while (true) {
            int produtoId = lerInt("Id do produto (0 para parar): ");
            if (produtoId == 0) break;
            int quantidade = lerInt("Quantidade: ");
            pedidoSvc.adicionarItem(order, produtoId, quantidade);
        }
        System.out.println("Pedido #" + pedidoId + " criado com " + order.getQuantidadeItens() + " item(s).");
    }

    private static void listarPedidosPorCliente() {
        int clienteId = lerInt("Id do cliente: ");
        List<Order> pedidos = pedidoSvc.listarPorCliente(clienteId);
        if (pedidos.isEmpty()) { System.out.println("Nenhum pedido encontrado."); return; }
        for (Order o : pedidos) {
            System.out.println("Pedido #" + o.getId()
                    + " | status: "  + o.getStatus()
                    + " | itens: "   + o.getQuantidadeItens()
                    + " | total: R$" + String.format("%.2f", o.calcularTotal()));
        }
    }

    private static void excluirPedido() {
        int pedidoId = lerInt("Id do pedido: ");
        pedidoSvc.deletar(pedidoId);
    }

    // ---- utilitários de leitura ----

    private static int lerInt(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextInt()) { sc.next(); System.out.print(prompt); }
        int val = sc.nextInt(); sc.nextLine();
        return val;
    }

    private static double lerDouble(String prompt) {
        System.out.print(prompt);
        while (!sc.hasNextDouble()) { sc.next(); System.out.print(prompt); }
        double val = sc.nextDouble(); sc.nextLine();
        return val;
    }

    private static String lerString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }
}
