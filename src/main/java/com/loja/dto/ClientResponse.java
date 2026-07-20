package com.loja.dto;

import com.loja.model.Client;

/** Contrato de SAÍDA: exatamente o que a API expõe. Note que isActive NÃO aparece. */
public record ClientResponse(int id, String nome, String email, String telefone, String cpf) {

    /** Fábrica: model -> DTO de resposta. */
    public static ClientResponse from(Client c) {
        return new ClientResponse(c.getId(), c.getNome(), c.getEmail(), c.getTelefone(), c.getCpf());
    }
}
