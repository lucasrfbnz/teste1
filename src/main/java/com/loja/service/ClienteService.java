package com.loja.service;

import com.loja.exception.ConflictException;
import com.loja.exception.NotFoundException;
import com.loja.model.Client;
import com.loja.repository.ClientRepository;

import java.util.List;

public class ClienteService {

    private final ClientRepository repo;

    public ClienteService(ClientRepository repo) {
        this.repo = repo;
    }

    public Client cadastrar(Client cliente) {
        if (repo.buscarPorCpf(cliente.getCpf()).isPresent()) {
            throw new ConflictException("Já existe cliente ativo com o CPF " + cliente.getCpf());
        }
        repo.inserir(cliente);
        return cliente;
    }

    public Client buscar(int id) {
        return repo.buscarPorId(id)
                .orElseThrow(() -> new NotFoundException("Cliente com id " + id + " não encontrado."));
    }

    public Client buscarPorCpf(String cpf) {
        return repo.buscarPorCpf(cpf)
                .orElseThrow(() -> new NotFoundException("Cliente com CPF " + cpf + " não encontrado."));
    }

    public boolean existe(int id) {
        return repo.buscarPorId(id).isPresent();
    }

    public List<Client> listarTodos() {
        return repo.listarTodos();
    }

    public Client atualizar(int id, Client dadosNovos) {
        Client c = buscar(id);
        c.setNome(dadosNovos.getNome());
        c.setEmail(dadosNovos.getEmail());
        c.setTelefone(dadosNovos.getTelefone());
        c.setCpf(dadosNovos.getCpf());
        repo.atualizar(c);
        return c;
    }

    public void desativar(int id) {
        buscar(id);
        repo.desativar(id);
    }

    public void reativar(String cpf) {
        if (repo.buscarPorCpf(cpf).isPresent()) {
            throw new ConflictException("Cliente com CPF " + cpf + " já está ativo.");
        }
        repo.reativar(cpf);
    }
}
