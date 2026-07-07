package com.loja.repository;

import com.loja.infra.Database;
import com.loja.model.Product;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProductRepositoryMySQL implements ProductRepository {

    @Override
    public void inserir(Product p) {
        String sql = "INSERT INTO product (nome, preco, estoque) VALUES (?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, p.getNome());
            ps.setDouble(2, p.getPreco());
            ps.setInt(3, p.getEstoque());
            ps.executeUpdate();
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) p.setId(rs.getInt(1));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        String sql = "SELECT id, nome, preco, estoque, is_active FROM product WHERE id = ? AND is_active = TRUE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return Optional.of(mapear(rs));
                return Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar produto", e);
        }
    }

    @Override
    public List<Product> listarTodos() {
        List<Product> lista = new ArrayList<>();
        String sql = "SELECT id, nome, preco, estoque, is_active FROM product WHERE is_active = TRUE";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lista.add(mapear(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar produtos", e);
        }
        return lista;
    }

    @Override
    public void atualizar(Product p) {
        String sql = "UPDATE product SET nome = ?, preco = ?, estoque = ? WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, p.getNome());
            ps.setDouble(2, p.getPreco());
            ps.setInt(3, p.getEstoque());
            ps.setInt(4, p.getId());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar produto", e);
        }
    }

    @Override
    public void desativar(int id) {
        String sql = "UPDATE product SET is_active = FALSE WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao desativar produto", e);
        }
    }

    @Override
    public void deletar(int id) {
        String sql = "DELETE FROM product WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar produto", e);
        }
    }

    private Product mapear(ResultSet rs) throws SQLException {
        Product p = new Product(rs.getString("nome"), rs.getDouble("preco"));
        p.setId(rs.getInt("id"));
        p.setEstoque(rs.getInt("estoque"));
        p.setActive(rs.getBoolean("is_active"));
        return p;
    }
}
