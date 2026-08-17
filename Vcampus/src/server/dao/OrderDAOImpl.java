package server.dao;

import common.model.Order;
import common.model.OrderItem;
import common.model.Product;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class OrderDAOImpl implements OrderDAO {
    @Override
    public boolean createOrder(Order order) {
        String sql = "INSERT INTO orders (order_id, user_id, total_amount, status) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, order.getOrderId());
            ps.setString(2, order.getUserId());
            ps.setBigDecimal(3, order.getTotalAmount());
            ps.setString(4, order.getStatus());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("创建订单失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean addOrderItem(OrderItem item) {
        String sql = "INSERT INTO order_items (order_id, product_id, quantity, price) VALUES (?, ?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, item.getOrderId());
            ps.setString(2, item.getProductId());
            ps.setInt(3, item.getQuantity());
            ps.setBigDecimal(4, item.getPrice());

            int affectedRows = ps.executeUpdate();
            if (affectedRows == 0) {

                return false;
            }
            return true;
        } catch (SQLException e) {
            return false;
        }
    }


    @Override
    public Order findOrderById(String orderId) {
        String sql = "SELECT order_id, user_id, total_amount, status, created_at FROM orders WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getString("order_id"));
                    order.setUserId(rs.getString("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    order.setItems(findOrderItemsByOrderId(orderId));
                    return order;
                }
            }
            return null;
        } catch (SQLException e) {
            System.err.println("查询订单失败: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<Order> findOrdersByUserId(String userId) {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, status, created_at FROM orders WHERE user_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, userId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Order order = new Order();
                    order.setOrderId(rs.getString("order_id"));
                    order.setUserId(rs.getString("user_id"));
                    order.setTotalAmount(rs.getBigDecimal("total_amount"));
                    order.setStatus(rs.getString("status"));
                    order.setCreatedAt(rs.getTimestamp("created_at"));
                    orders.add(order);
                }
            }
            return orders;
        } catch (SQLException e) {
            System.err.println("查询用户订单失败: " + e.getMessage());
            return orders;
        }
    }

    @Override
    public List<Order> findAllOrders() {
        List<Order> orders = new ArrayList<>();
        String sql = "SELECT order_id, user_id, total_amount, status, created_at FROM orders ORDER BY created_at DESC";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                Order order = new Order();
                order.setOrderId(rs.getString("order_id"));
                order.setUserId(rs.getString("user_id"));
                order.setTotalAmount(rs.getBigDecimal("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setCreatedAt(rs.getTimestamp("created_at"));
                orders.add(order);
            }
            return orders;
        } catch (SQLException e) {
            System.err.println("查询所有订单失败: " + e.getMessage());
            return orders;
        }
    }

    @Override
    public List<OrderItem> findOrderItemsByOrderId(String orderId) {
        List<OrderItem> items = new ArrayList<>();
        String sql = "SELECT oi.order_id, oi.product_id, oi.quantity, oi.price, p.name as product_name " +
                "FROM order_items oi JOIN product p ON oi.product_id = p.product_id " +
                "WHERE oi.order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    OrderItem item = new OrderItem();
                    item.setOrderId(rs.getString("order_id"));
                    item.setProductId(rs.getString("product_id"));
                    item.setQuantity(rs.getInt("quantity"));
                    item.setPrice(rs.getBigDecimal("price"));

                    Product product = new Product();
                    product.setProductId(rs.getString("product_id"));
                    product.setName(rs.getString("product_name"));
                    item.setProduct(product);

                    items.add(item);
                }
            }
            return items;
        } catch (SQLException e) {
            System.err.println("查询订单项失败: " + e.getMessage());
            return items;
        }
    }

    @Override
    public boolean updateOrderStatus(String orderId, String status) {
        String sql = "UPDATE orders SET status = ? WHERE order_id = ?";

        DBUtil dataSource;
        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, orderId);

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("更新订单状态失败", e);
        }
    }


    @Override
    public boolean isProductInOrders(String productId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查商品订单关联失败: " + e.getMessage());
            return true; // 出错时保守处理
        }
    }

    @Override
    public int countOrdersByProduct(String productId) {
        String sql = "SELECT COUNT(*) FROM order_items WHERE product_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
            return 0;

        } catch (SQLException e) {
            System.err.println("统计商品订单数量失败: " + e.getMessage());
            return 0; // 出错时返回0，保守处理
        }
    }

    @Override
    public boolean deleteOrder(String orderId) {
        String sql = "DELETE FROM orders WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("删除订单失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteOrderItems(String orderId) {
        String sql = "DELETE FROM order_items WHERE order_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, orderId);
            return ps.executeUpdate() > 0;

        } catch (SQLException e) {
            System.err.println("删除订单项失败: " + e.getMessage());
            return false;
        }
    }
}