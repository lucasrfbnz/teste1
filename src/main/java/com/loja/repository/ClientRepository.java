package com.loja.repository;

import com.loja.model.Client;

import java.util.List;
import java.util.Optional;

// Interface: define o contrato. A implementação concreta pode ser MySQL, memória, etc.
public interface ClientRepository {
    void inserir(Client c);
    Optional<Client> buscarPorId(int id);
    List<Client> listarTodos();
    void atualizar(Client c);
    void deletar(int id);
}
