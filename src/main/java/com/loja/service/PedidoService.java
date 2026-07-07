package com.loja.service;

import com.loja.model.Order;
import com.loja.model.Product;
import com.loja.repository.OrderRepository;

import java.util.List;

/**
 * Camada de serviço para Order.
 *
 * Este é o lugar certo para as validações que saímos do Order na
 * correção A.1: verificar se o cliente existe, se o produto existe,
 * etc. O Order continua sendo uma entidade pura — só PedidoService
 * conhece os repositórios.
 */
public class PedidoService {

    private final OrderRepository  orderRepo;
    private final ClienteService   clienteSvc;
    private final ProdutoService   produtoSvc;

    // injeção de dependência: recebe os serviços prontos, não cria nada
    public PedidoService(OrderRepository orderRepo, ClienteService clienteSvc, ProdutoService produtoSvc) {
        this.orderRepo  = orderRepo;
        this.clienteSvc = clienteSvc;
        this.produtoSvc = produtoSvc;
    }

    public Order criarPedido(int clienteId) {
        if (!clienteSvc.existe(clienteId)) {
            throw new IllegalArgumentException("Cliente com id " + clienteId + " não existe.");
        }
        Order order = new Order(0, clienteId); // 0 = banco vai gerar o id
        orderRepo.inserir(order);              // seta order.id via RETURN_GENERATED_KEYS
        System.out.println("Pedido #" + order.getId() + " criado para clienteId=" + clienteId);
        return order;
    }

    public void adicionarItem(Order order, int produtoId, int quantidade) {
        Product produto = produtoSvc.buscar(produtoId);
        produto.baixarEstoque(quantidade); // valida e desconta o estoque no objeto
        order.adicionarItem(produtoId, quantidade, produto.getPreco());
        // persiste o pedido E a baixa de estoque na mesma transação (bônus)
        orderRepo.atualizarComEstoque(order, produtoId, produto.getEstoque());
        System.out.println("Item adicionado: " + produto.getNome() + " x" + quantidade);
    }

    public void finalizarPedido(Order order) {
        order.finalizarPedido(); // regra de domínio aplicada pelo model
        orderRepo.atualizar(order);
        System.out.println("Pedido #" + order.getId() + " finalizado.");
    }

    public Order buscar(int id) {
        return orderRepo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Pedido com id " + id + " não encontrado."));
    }

    public List<Order> listarPorCliente(int clienteId) {
        
        return orderRepo.listarPorCliente(clienteId);
    }

    // CASCADE no banco apaga os itens automaticamente
    public void deletar(int id) {
        buscar(id); // valida que o pedido existe antes de deletar
        orderRepo.deletar(id);
        System.out.println("Pedido #" + id + " excluído.");
    }
}
