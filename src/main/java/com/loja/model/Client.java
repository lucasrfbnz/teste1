package com.loja.model;

/**
 * Entidade de domínio: representa um cliente da loja.
 *
 * AGREGAÇÃO: Order -> Client (fraca).
 * O cliente existe independentemente de qualquer pedido.
 * Order guarda apenas o clienteId (int), não este objeto inteiro.
 */
public class Client {

    private int id;
    private String nome;
    private String email;
    private String telefone;
    private String cpf;
    private boolean isActive;

    public Client(String nome, String email, String telefone, String cpf) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.isActive = true;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nome='" + nome + "', email='" + email
                + "', telefone='" + telefone + "', cpf='" + cpf + "'}";
    }
}
