package com.loja.service;

import com.loja.model.Client;
import com.loja.repository.ClientRepository;

import java.util.List;

public class ClienteService {

    private final ClientRepository repo;

    // injeção de dependência: quem cria o service decide qual repo usar
    public ClienteService(ClientRepository repo) {
        this.repo = repo;
    }

    public void cadastrar(Client cliente) {
        if (repo.buscarPorId(cliente.getId()).isPresent()) {
            throw new IllegalArgumentException("Cliente com id " + cliente.getId() + " já existe.");
        }
        repo.inserir(cliente);
        System.out.println("Cliente cadastrado: " + cliente.getNome());
    }

    public Client buscar(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new IllegalArgumentException("Cliente com id " + id + " não encontrado."));
    }

    public boolean existe(int id) {
        return repo.buscarPorId(id).isPresent();
    }

    public List<Client> listarTodos() {
        return repo.listarTodos();
    }
}
