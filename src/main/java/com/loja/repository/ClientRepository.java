package com.loja.repository;

import com.loja.model.Client;

import java.util.List;
import java.util.Optional;

public interface ClientRepository {
    void inserir(Client c);
    Optional<Client> buscarPorId(int id);
    Optional<Client> buscarPorCpf(String cpf);
    List<Client> listarTodos();
    void atualizar(Client c);
    void desativar(int id);
    void reativar(String cpf);
    void deletar(int id);
}
