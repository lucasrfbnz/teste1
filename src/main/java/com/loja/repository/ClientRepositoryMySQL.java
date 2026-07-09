package com.loja.repository;

import com.loja.infra.Database;
import com.loja.model.Client;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ClientRepositoryMySQL implements ClientRepository {

    @Override
    public void inserir(Client c) {
        String sql = "INSERT INTO client (nome, email, telefone, cpf) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getCpf());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) c.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir cliente", e);
        }
    }

    @Override
    public Optional<Client> buscarPorId(int id) {
        String sql = "SELECT id, nome, email, telefone, cpf, is_active FROM client WHERE id = ? AND is_active = TRUE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente", e);
        }
    }

    @Override
    public Optional<Client> buscarPorCpf(String cpf) {
        String sql = "SELECT id, nome, email, telefone, cpf, is_active FROM client WHERE cpf = ? AND is_active = TRUE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar cliente por CPF", e);
        }
    }

    @Override
    public List<Client> listarTodos() {
        List<Client> lista = new ArrayList<>();
        String sql = "SELECT id, nome, email, telefone, cpf, is_active FROM client WHERE is_active = TRUE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar clientes", e);
        }
        return lista;
    }

    @Override
    public void atualizar(Client c) {
        String sql = "UPDATE client SET nome = ?, email = ?, telefone = ?, cpf = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, c.getNome());
            ps.setString(2, c.getEmail());
            ps.setString(3, c.getTelefone());
            ps.setString(4, c.getCpf());
            ps.setInt(5, c.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar cliente", e);
        }
    }

    @Override
    public void desativar(int id) {
        String sql = "UPDATE client SET is_active = FALSE WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar cliente", e);
        }
    }

    @Override
    public void reativar(String cpf) {
        String sql = "UPDATE client SET is_active = TRUE WHERE cpf = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cpf);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao reativar cliente", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM client WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar cliente", e);
        }
    }

    private Client mapear(ResultSet rs) throws SQLException {
        Client c = new Client(
                rs.getString("nome"),
                rs.getString("email"),
                rs.getString("telefone"),
                rs.getString("cpf"));
        c.setId(rs.getInt("id"));
        c.setActive(rs.getBoolean("is_active"));
        return c;
    }
}
