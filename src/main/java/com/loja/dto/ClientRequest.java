package com.loja.dto;

import com.loja.exception.ValidationException;
import com.loja.model.Client;

/** Contrato de ENTRADA: o que o cliente da API pode enviar. Sem id, sem isActive. */
public record ClientRequest(String nome, String email, String telefone, String cpf) {

    /** Validação de formato/preenchimento — o que dá para checar sem consultar o banco. */
    public void validar() {
        if (nome == null || nome.isBlank())    throw new ValidationException("O campo 'nome' é obrigatório.");
        if (email == null || !email.contains("@")) throw new ValidationException("E-mail inválido.");
        if (cpf == null || cpf.isBlank())      throw new ValidationException("O campo 'cpf' é obrigatório.");
    }

    /** Converte o DTO de entrada no model do domínio. */
    public Client toModel() {
        return new Client(nome, email, telefone, cpf);
    }
}
