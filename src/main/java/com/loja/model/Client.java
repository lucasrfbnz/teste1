package com.loja.model;

import jakarta.persistence.*;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Table(name = "client")
@SQLRestriction("is_active = true")
public class Client {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 120)
    private String nome;

    @Column(nullable = false, length = 120)
    private String email;

    @Column(length = 30)
    private String telefone;

    @Column(unique = true, length = 14)
    private String cpf;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    // JPA exige construtor sem argumentos. Protegido: é para o Hibernate, não para o seu código.
    protected Client() {}

    public Client(String nome, String email, String telefone, String cpf) {
        this.nome = nome;
        this.email = email;
        this.telefone = telefone;
        this.cpf = cpf;
        this.active = true;
    }

    public Integer getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getNome() { return nome; }
    public void setNome(String nome) { this.nome = nome; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getTelefone() { return telefone; }
    public void setTelefone(String telefone) { this.telefone = telefone; }

    public String getCpf() { return cpf; }
    public void setCpf(String cpf) { this.cpf = cpf; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "Cliente{id=" + id + ", nome='" + nome + "', email='" + email
                + "', telefone='" + telefone + "', cpf='" + cpf + "'}";
    }
}
