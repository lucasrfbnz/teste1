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
        String sql = "INSERT INTO product (id, nome, preco, estoque) VALUES (?, ?, ?, ?)";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, p.getId());
            ps.setString(2, p.getNome());
            ps.setDouble(3, p.getPreco());
            ps.setInt(4, p.getEstoque());
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir produto", e);
        }
    }

    @Override
    public Optional<Product> buscarPorId(int id) {
        String sql = "SELECT * FROM product WHERE id = ?";
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
        String sql = "SELECT * FROM product";
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
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
        Product p = new Product(rs.getInt("id"), rs.getString("nome"), rs.getDouble("preco"));
        p.setEstoque(rs.getInt("estoque"));
        return p;
    }
}
