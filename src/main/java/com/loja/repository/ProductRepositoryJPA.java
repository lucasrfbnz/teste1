package com.loja.repository;

import com.loja.infra.JpaUtil;
import com.loja.model.Product;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ProductRepositoryJPA implements ProductRepository {

    @Override
    public void inserir(Product p) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(p);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Product p = em.find(Product.class, id);
            return (p != null && p.isActive()) ? Optional.of(p) : Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Product> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // @SQLRestriction garante que só produtos ativos aparecem.
            return em.createQuery("SELECT p FROM Product p", Product.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void atualizar(Product p) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(p);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void desativar(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            Product p = em.find(Product.class, id);
            if (p != null) p.setActive(false);
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
            Product p = em.find(Product.class, id);
            if (p != null) em.remove(p);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }
}
