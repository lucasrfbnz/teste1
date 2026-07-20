package com.loja.repository;

import com.loja.infra.JpaUtil;
import com.loja.model.Order;
import com.loja.model.Product;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class OrderRepositoryJPA implements OrderRepository {

    @Override
    public void inserir(Order order) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(order);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Order> buscarPorId(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JOIN FETCH inicializa a coleção lazy antes de fechar o EntityManager,
            // evitando LazyInitializationException ao acessar os itens fora da sessão.
            return em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.itens i " +
                    "LEFT JOIN FETCH i.produto " +
                    "WHERE o.id = :id", Order.class)
                    .setParameter("id", id)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // JOIN FETCH em dois níveis evita N+1: 1 query para pedidos + itens + produtos.
            return em.createQuery(
                    "SELECT DISTINCT o FROM Order o LEFT JOIN FETCH o.itens i LEFT JOIN FETCH i.produto",
                    Order.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Order> listarPorCliente(int clienteId) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            return em.createQuery(
                    "SELECT DISTINCT o FROM Order o " +
                    "LEFT JOIN FETCH o.itens i " +
                    "LEFT JOIN FETCH i.produto " +
                    "WHERE o.cliente.id = :clienteId", Order.class)
                    .setParameter("clienteId", clienteId)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void atualizar(Order order) {
        // JPQL update cirúrgico: atualiza só status/finalizado sem tocar nos itens.
        // Evita problema de entidade detached com a coleção de itens.
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.createQuery(
                    "UPDATE Order o SET o.status = :status, o.finalizado = :finalizado WHERE o.id = :id")
                    .setParameter("status", order.getStatus())
                    .setParameter("finalizado", order.isFinalizado())
                    .setParameter("id", order.getId())
                    .executeUpdate();
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void adicionarItem(int orderId, Product produto, int quantidade) {
        // Tudo em uma transação: dirty checking cuida do estoque do produto,
        // cascade cuida do novo OrderItem. O service não precisa coordenar duas chamadas.
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order managedOrder   = em.find(Order.class, orderId);
            Product managedProd  = em.find(Product.class, produto.getId());
            managedProd.setEstoque(produto.getEstoque()); // replica o baixarEstoque já feito no service
            managedOrder.adicionarItem(managedProd, quantidade);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void deletar(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Order o = em.find(Order.class, id);
            if (o != null) em.remove(o); // cascade deleta os itens
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
