package com.loja.service;

import com.loja.model.Client;
import com.loja.repository.ClientRepository;

import java.util.List;

public class ClienteService {

    private final ClientRepository repo;

    public ClienteService(ClientRepository repo) {
        this.repo = repo;
    }

    public void cadastrar(Client cliente) {
        repo.inserir(cliente);
        System.out.println("Cliente cadastrado: " + cliente.getNome() + " (id=" + cliente.getId() + ")");
    }

    public Client buscar(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente com id " + id + " não encontrado."));
    }

    public Client buscarPorCpf(String cpf) {
        return repo.buscarPorCpf(cpf)
                .orElseThrow(() -> new IllegalArgumentException("Cliente com CPF " + cpf + " não encontrado."));
    }

    public boolean existe(int id) {
        return repo.buscarPorId(id).isPresent();
    }

    public List<Client> listarTodos() {
        return repo.listarTodos();
    }

    public void desativar(int id) {
        buscar(id); // valida que o cliente existe e está ativo
        repo.desativar(id);
        System.out.println("Cliente #" + id + " desativado.");
    }

    public void reativar(String cpf) {
        if (repo.buscarPorCpf(cpf).isPresent()) {
            throw new IllegalArgumentException("Cliente com CPF " + cpf + " já está ativo.");
        }
        repo.reativar(cpf);
        System.out.println("Cliente com CPF " + cpf + " reativado.");
    }
}
