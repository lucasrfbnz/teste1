package com.loja.repository;

import com.loja.infra.Database;
import com.loja.model.Order;
import com.loja.model.OrderItem;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class OrderRepositoryMySQL implements OrderRepository {

    @Override
    public void inserir(Order order) {
        String sqlOrder = "INSERT INTO orders (id, cliente_id, data, status, finalizado) VALUES (?, ?, ?, ?, ?)";
        String sqlItem  = "INSERT INTO order_item (id, order_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
                    ps.setInt(1, order.getId());
                    ps.setInt(2, order.getClienteId());
                    ps.setDate(3, Date.valueOf(order.getData()));
                    ps.setString(4, order.getStatus());
                    ps.setBoolean(5, order.isFinalizado());
                    ps.executeUpdate();
                }
                inserirItens(conn, order, sqlItem);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao inserir pedido", e);
        }
    }

    @Override
    public void atualizar(Order order) {
        String sqlOrder = "UPDATE orders SET status = ?, finalizado = ? WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                atualizarOrder(conn, order, sqlOrder);
                reinseriritens(conn, order);
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pedido", e);
        }
    }

    // Bônus — camada de estoque: atualiza pedido E estoque numa única transação
    @Override
    public void atualizarComEstoque(Order order, int produtoId, int novoEstoque) {
        String sqlOrder   = "UPDATE orders SET status = ?, finalizado = ? WHERE id = ?";
        String sqlEstoque = "UPDATE product SET estoque = ? WHERE id = ?";
        try (Connection conn = Database.getConnection()) {
            conn.setAutoCommit(false);
            try {
                atualizarOrder(conn, order, sqlOrder);
                reinseriritens(conn, order);
                // baixa de estoque na mesma transação: se falhar, tudo volta
                try (PreparedStatement ps = conn.prepareStatement(sqlEstoque)) {
                    ps.setInt(1, novoEstoque);
                    ps.setInt(2, produtoId);
                    ps.executeUpdate();
                }
                conn.commit();
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao atualizar pedido com estoque", e);
        }
    }

    @Override
    public Optional<Order> buscarPorId(int id) {
        String sqlOrder = "SELECT * FROM orders WHERE id = ?";
        String sqlItens = "SELECT * FROM order_item WHERE order_id = ?";
        try (Connection conn = Database.getConnection()) {
            Order order = null;
            try (PreparedStatement ps = conn.prepareStatement(sqlOrder)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) order = mapearOrder(rs);
                }
            }
            if (order == null) return Optional.empty();
            try (PreparedStatement ps = conn.prepareStatement(sqlItens)) {
                ps.setInt(1, id);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) order.carregarItem(mapearItem(rs));
                }
            }
            return Optional.of(order);
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao buscar pedido", e);
        }
    }

    @Override
    public List<Order> listarTodos() {
        List<Order> lista = new ArrayList<>();
        try (Connection conn = Database.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery("SELECT * FROM orders")) {
            while (rs.next()) lista.add(mapearOrder(rs));
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos", e);
        }
        return lista;
    }

    @Override
    public List<Order> listarPorCliente(int clienteId) {
        List<Order> lista = new ArrayList<>();
        String sqlOrders = "SELECT * FROM orders WHERE cliente_id = ?";
        String sqlItens  = "SELECT * FROM order_item WHERE order_id = ?";
        try (Connection conn = Database.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(sqlOrders)) {
                ps.setInt(1, clienteId);
                try (ResultSet rs = ps.executeQuery()) {
                    while (rs.next()) lista.add(mapearOrder(rs));
                }
            }
            try (PreparedStatement ps = conn.prepareStatement(sqlItens)) {
                for (Order order : lista) {
                    ps.setInt(1, order.getId());
                    try (ResultSet rs = ps.executeQuery()) {
                        while (rs.next()) order.carregarItem(mapearItem(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao listar pedidos por cliente", e);
        }
        return lista;
    }

    @Override
    public void deletar(int id) {
        // CASCADE no banco apaga os order_item automaticamente
        String sql = "DELETE FROM orders WHERE id = ?";
        try (Connection conn = Database.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Erro ao deletar pedido", e);
        }
    }

    // ---- helpers privados ----

    private void atualizarOrder(Connection conn, Order order, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, order.getStatus());
            ps.setBoolean(2, order.isFinalizado());
            ps.setInt(3, order.getId());
            ps.executeUpdate();
        }
    }

    private void reinseriritens(Connection conn, Order order) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM order_item WHERE order_id = ?")) {
            ps.setInt(1, order.getId());
            ps.executeUpdate();
        }
        inserirItens(conn, order, "INSERT INTO order_item (id, order_id, produto_id, quantidade, preco_unitario) VALUES (?, ?, ?, ?, ?)");
    }

    private void inserirItens(Connection conn, Order order, String sql) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (OrderItem item : order.getItens()) {
                ps.setInt(1, item.getId());
                ps.setInt(2, order.getId());
                ps.setInt(3, item.getProdutoId());
                ps.setInt(4, item.getQuantidade());
                ps.setDouble(5, item.getPrecoUnitario());
                ps.addBatch();
            }
            ps.executeBatch();
        }
    }

    private Order mapearOrder(ResultSet rs) throws SQLException {
        Order o = new Order(rs.getInt("id"), rs.getInt("cliente_id"));
        o.setData(rs.getDate("data").toLocalDate());
        o.setStatus(rs.getString("status"));
        o.setFinalizado(rs.getBoolean("finalizado"));
        return o;
    }

    private OrderItem mapearItem(ResultSet rs) throws SQLException {
        return new OrderItem(
                rs.getInt("id"),
                rs.getInt("produto_id"),
                rs.getInt("quantidade"),
                rs.getDouble("preco_unitario"));
    }
}
