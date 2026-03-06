package server.dao;

import common.model.Order;
import common.model.OrderItem;

import java.util.List;

public interface OrderDAO {
    List<Order> findOrdersByUserId(String userId);
    List<Order> findAllOrders();
    Order findOrderById(String orderId);
    boolean createOrder(Order order);
    boolean updateOrderStatus(String orderId, String status);
    boolean addOrderItem(OrderItem item);
    List<OrderItem> findOrderItemsByOrderId(String orderId);
    boolean isProductInOrders(String productId);
    int countOrdersByProduct(String productId);
    boolean deleteOrder(String orderId);
    boolean deleteOrderItems(String orderId);
}
