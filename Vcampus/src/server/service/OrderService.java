package server.service;

import common.model.Order;
import common.model.OrderItem;
import common.model.Product;
import server.dao.OrderDAO;
import server.dao.OrderDAOImpl;
import server.dao.ProductDAO;
import server.dao.ProductDAOImpl;
import util.DBUtil;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;


public class OrderService {
    private final OrderDAO orderDAO = new OrderDAOImpl();
    private final ProductDAO productDAO = new ProductDAOImpl();
    private final ProductService productService = new ProductService(productDAO,orderDAO);

    public boolean createOrder(String userId, List<OrderItem> items) {
        try {
            // 1. 验证数据
            if (userId == null || userId.trim().isEmpty()) {
                throw new IllegalArgumentException("用户ID不能为空");
            }

            if (items == null || items.isEmpty()) {
                throw new IllegalArgumentException("订单商品不能为空");
            }

            // 2. 检查库存
            Map<String, Integer> insufficientItems = new HashMap<>();
            for (OrderItem item : items) {
                Product product = productDAO.findProductById(item.getProductId());
                if (product == null) {
                    throw new IllegalArgumentException("商品不存在: " + item.getProductId());
                }
                if (product.getStock() < item.getQuantity()) {
                    insufficientItems.put(product.getName(),
                            item.getQuantity() - product.getStock());
                }
            }



            // 3. 创建订单
            String orderId = generateOrderId();
            Order order = new Order(orderId, userId, calculateTotal(items), "待支付");

            if (!orderDAO.createOrder(order)) {
                throw new RuntimeException("创建订单记录失败");
            }



            // 4. 添加订单项并扣减库存
            for (OrderItem item : items) {
                item.setOrderId(orderId);

                if (!productDAO.updateStock(item.getProductId(), -item.getQuantity())) {
                    throw new RuntimeException("更新库存失败: " + item.getProductId());
                }
            }

            return true;
        } catch (Exception e) {
            // 记录详细错误日志
            System.err.println("订单创建失败: " + e.getMessage());
            e.printStackTrace();
            throw e; // 传播异常以便客户端获取
        }
    }

    /**
     * 计算订单总金额
     * @param items 订单项列表
     * @return 订单总金额
     */
    private BigDecimal calculateTotal(List<OrderItem> items) {
        BigDecimal total = BigDecimal.ZERO;

        for (OrderItem item : items) {
            // 获取商品价格
            Product product = productDAO.findProductById(item.getProductId());
            if (product == null) {
                throw new IllegalArgumentException("商品不存在: " + item.getProductId());
            }

            // 计算单项总价：单价 × 数量
            BigDecimal itemTotal = product.getPrice().multiply(
                    BigDecimal.valueOf(item.getQuantity()));

            total = total.add(itemTotal);
        }

        return total;
    }

    private String generateOrderId() {
        // 获取当前日期
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
        String dateStr = dateFormat.format(new Date());

        // 生成4位随机数
        Random random = new Random();
        int randomNum = random.nextInt(9000) + 1000; // 1000-9999

        return "ORD" + dateStr + randomNum;
    }

    public List<Order> getUserOrders(String userId) {
        return orderDAO.findOrdersByUserId(userId);
    }

    public List<Order> getAllOrders() {
        return orderDAO.findAllOrders();
    }

    public Order getOrderDetail(String orderId) {
        Order order = orderDAO.findOrderById(orderId);
        if (order != null) {
            order.setItems(orderDAO.findOrderItemsByOrderId(orderId));
        }
        return order;
    }



    public boolean updateOrderStatus(String orderId, String status, String operatorRole) {
        Order order = orderDAO.findOrderById(orderId);
        if (order == null) return false;

        if (!isValidStatusTransition(order.getStatus(), status, operatorRole)) {
            return false;
        }

        return orderDAO.updateOrderStatus(orderId, status);
    }

    // OrderService.java
    public boolean cancelOrder(String orderId, String userId) {
        // 1. 验证订单属于当前用户
        Order order = orderDAO.findOrderById(orderId);
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("无权取消他人订单");
        }

        // 2. 验证订单状态
        if (!"待支付".equals(order.getStatus())) {
            throw new IllegalStateException("只有待支付订单可以取消");
        }

        // 3. 更新状态
        return orderDAO.updateOrderStatus(orderId, "已取消");
    }

    private void validateCancelPermission(Order order, String userId, String operatorRole) {
        // 管理员可以取消任何订单
        if ("admin".equals(operatorRole)) {
            return;
        }

        // 普通用户只能取消自己的订单
        if (!order.getUserId().equals(userId)) {
            throw new SecurityException("无权取消他人订单");
        }
    }

    private void restoreStock(String orderId) {
        List<OrderItem> items = orderDAO.findOrderItemsByOrderId(orderId);
        for (OrderItem item : items) {
            if (!productDAO.updateStock(item.getProductId(), item.getQuantity())) {
                throw new RuntimeException("恢复库存失败: " + item.getProductId());
            }
        }
    }

    private boolean isValidStatusTransition(String currentStatus, String newStatus, String userRole) {
        Map<String, Set<String>> allowedTransitions = new HashMap<>();

        // 管理员允许的状态转换
        Set<String> adminTransitions = new HashSet<>(Arrays.asList(
                "待支付", "已支付", "已取消", "已完成"
        ));

        // 普通用户允许的状态转换
        Set<String> userTransitions = new HashSet<>(Arrays.asList(
                "待支付", "已取消"
        ));

        allowedTransitions.put("admin", adminTransitions);
        allowedTransitions.put("user", userTransitions);

        Set<String> allowed = "admin".equals(userRole) ?
                allowedTransitions.get("admin") : allowedTransitions.get("user");

        return allowed.contains(newStatus) &&
                !"已完成".equals(currentStatus) &&
                !"已取消".equals(currentStatus);
    }

    public boolean isProductInOrders(String productId) {
        return orderDAO.isProductInOrders(productId);
    }

    public boolean payOrder(String orderId, String userId, String userType) {
        try {
            // 1. 获取订单
            Order order = orderDAO.findOrderById(orderId);
            if (order == null) {
                throw new IllegalArgumentException("订单不存在: " + orderId);
            }

            // 2. 验证权限
            if (!order.getUserId().equals(userId) && !"admin".equals(userType)) {
                throw new SecurityException("无权支付他人订单");
            }

            // 3. 检查状态
            if (!"待支付".equals(order.getStatus())) {
                throw new IllegalStateException("只有待支付订单可以支付");
            }

            // 4. 检查库存
            checkStockBeforePayment(orderId);

            // 5. 更新状态
            return orderDAO.updateOrderStatus(orderId, "已支付");
        } catch (Exception e) {
            throw new RuntimeException("支付订单失败: " + e.getMessage(), e);
        }
    }

    private void checkStockBeforePayment(String orderId) {
        List<OrderItem> items = orderDAO.findOrderItemsByOrderId(orderId);
        for (OrderItem item : items) {
            Product product = productDAO.findProductById(item.getProductId());
            if (product.getStock() < item.getQuantity()) {
                throw new IllegalStateException(
                        "商品[" + product.getName() + "]库存不足，当前库存: " +
                                product.getStock() + "，需要: " + item.getQuantity());
            }
        }
    }


    /**
     * 删除订单
     * @param orderId 订单ID
     * @param operatorRole 操作者角色
     * @return 是否删除成功
     */
    public boolean deleteOrder(String orderId, String operatorRole) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            conn.setAutoCommit(false);

            // 1. 验证权限
            if (!"admin".equals(operatorRole)) {
                System.out.println("[SERVER] 权限不足，需要管理员权限");
                return false;
            }

            // 2. 获取订单详情
            Order order = orderDAO.findOrderById(orderId);
            if (order == null) {
                System.out.println("[SERVER] 订单不存在: " + orderId);
                return false;
            }

            // 3. 检查订单状态
            if (!"待支付".equals(order.getStatus())) {
                System.out.println("[SERVER] 只能删除待支付订单，当前状态: " + order.getStatus());
                return false;
            }

            // 4. 恢复库存
            for (OrderItem item : order.getItems()) {
                productDAO.updateProductStock(item.getProductId(), item.getQuantity());
            }

            // 5. 删除订单项
            orderDAO.deleteOrderItems(orderId);

            // 6. 删除订单
            boolean success = orderDAO.deleteOrder(orderId);

            if (success) {
                conn.commit();
                System.out.println("[SERVER] 订单删除成功: " + orderId);
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (Exception e) {
            if (conn != null) {
                try { conn.rollback(); } catch (SQLException ex) {}
            }
            System.err.println("[SERVER] 删除订单异常: " + e.getMessage());
            return false;
        } finally {
            DBUtil.close(conn);
        }
    }


}