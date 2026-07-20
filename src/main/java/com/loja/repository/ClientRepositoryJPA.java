package com.loja.repository;

import com.loja.exception.ConflictException;
import com.loja.infra.JpaUtil;
import com.loja.model.Client;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

public class ClientRepositoryJPA implements ClientRepository {

    @Override
    public void inserir(Client c) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.persist(c); // gera o INSERT e preenche o id gerado
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            if (causedByDuplicate(e)) throw new ConflictException("CPF já cadastrado.");
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Client> buscarPorId(int id) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            Client c = em.find(Client.class, id);
            return (c != null && c.isActive()) ? Optional.of(c) : Optional.empty();
        } finally {
            em.close();
        }
    }

    @Override
    public Optional<Client> buscarPorCpf(String cpf) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // @SQLRestriction já filtra is_active = true automaticamente.
            return em.createQuery(
                    "SELECT c FROM Client c WHERE c.cpf = :cpf",
                    Client.class)
                    .setParameter("cpf", cpf)
                    .getResultStream().findFirst();
        } finally {
            em.close();
        }
    }

    @Override
    public List<Client> listarTodos() {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            // @SQLRestriction garante que só clientes ativos aparecem.
            return em.createQuery("SELECT c FROM Client c", Client.class)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    @Override
    public void atualizar(Client c) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            em.merge(c);
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
            Client c = em.find(Client.class, id);
            // DIRTY CHECKING: nenhum UPDATE escrito à mão.
            // O JPA detecta a mudança e gera o UPDATE no commit.
            if (c != null) c.setActive(false);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    @Override
    public void reativar(String cpf) {
        EntityManager em = JpaUtil.getEntityManager();
        try {
            em.getTransaction().begin();
            // @SQLRestriction bloqueia JPQL de encontrar clientes inativos.
            // Native query ignora o filtro e enxerga a linha com is_active = false.
            @SuppressWarnings("unchecked")
            List<Client> found = em.createNativeQuery(
                    "SELECT * FROM client WHERE cpf = :cpf AND is_active = false", Client.class)
                    .setParameter("cpf", cpf)
                    .getResultList();
            if (!found.isEmpty()) found.get(0).setActive(true); // dirty checking gera UPDATE
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
            Client c = em.find(Client.class, id);
            if (c != null) em.remove(c);
            em.getTransaction().commit();
        } catch (RuntimeException e) {
            if (em.getTransaction().isActive()) em.getTransaction().rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    private boolean causedByDuplicate(Throwable e) {
        while (e != null) {
            if (e instanceof java.sql.SQLIntegrityConstraintViolationException sqle
                    && sqle.getErrorCode() == 1062) return true;
            e = e.getCause();
        }
        return false;
    }
}
