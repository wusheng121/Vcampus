package client.controller;
import common.net.MessageType;

import client.net.ClientSocket;
import client.ui.LoginFrame;
import common.model.Order;
import common.model.OrderItem;
import common.model.User;
import common.net.Message;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.Objects;

public class OrderController {
    private final ClientSocket clientSocket = ClientSocket.getInstance();
    private User currentUser;

    /**
     * 设置当前用户，不允许为null
     * @param user 当前用户对象
     * @throws NullPointerException 如果user为null
     */
    public void setCurrentUser(User user) {
        this.currentUser = Objects.requireNonNull(user, "用户对象不能为null");
        System.out.println("[DEBUG] OrderController设置当前用户: " + user.getUserId());
    }

    /**
     * 创建订单
     *
     * @param currentUser
     * @param items       订单项列表
     * @return 是否创建成功
     */
    public boolean createOrder(User currentUser, List<OrderItem> items) {
        try {
            if (currentUser == null) {
                throw new IllegalStateException("用户未登录");
            }

            Message request = new Message();
            request.setType(MessageType.CREATE_ORDER);
            request.setData(items);
            request.setExtra(currentUser); // 传递当前用户

            Message response = clientSocket.sendRequest(request);
            if (!"success".equals(response.getStatus())) {
                String errorMsg = response.getMsg() != null ?
                        response.getMsg() : "创建订单失败";
                throw new RuntimeException(errorMsg);
            }
            return true;
        } catch (Exception e) {
            handleError(e);
            return false;
        }
    }

    /**
     * 获取当前用户的订单列表
     * @return 订单列表，如果出错返回null
     */
    public List<Order> getUserOrders() {
        try {
            validateCurrentUser(); // 验证当前用户

            System.out.println("[DEBUG] 获取用户订单 - 当前用户: " + currentUser.getUserId());

            Message request = new Message();
            request.setType(MessageType.GET_USER_ORDERS);
            request.setData(currentUser); // 传递整个用户对象

            Message response = clientSocket.sendRequest(request);
            System.out.println("[DEBUG] 收到订单响应: " + response.getStatus());

            if ("success".equals(response.getStatus())) {
                return (List<Order>) response.getData();
            } else {
                throw new RuntimeException(response.getMsg() != null ?
                        response.getMsg() : "获取订单失败");
            }
        } catch (Exception e) {
            System.err.println("获取用户订单失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "获取订单失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * 获取所有订单（管理员权限）
     * @return 所有订单列表，如果出错或权限不足返回null
     */
    public List<Order> getAllOrders() {
        try {
            validateCurrentUser(); // 验证当前用户

            if (!"admin".equals(currentUser.getType())) {
                throw new IllegalStateException("权限不足，需要管理员权限");
            }

            Message request = new Message();
            request.setType(MessageType.GET_ALL_ORDERS);
            request.setData(currentUser);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<Order>) response.getData();
            }
            throw new RuntimeException(response.getMsg() != null ?
                    response.getMsg() : "获取所有订单失败");
        } catch (Exception e) {
            System.err.println("获取所有订单失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "获取所有订单失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * 获取订单详情
     * @param orderId 订单ID
     * @return 订单对象，如果出错返回null
     */
    public Order getOrderDetail(String orderId) {
        try {
            Objects.requireNonNull(orderId, "订单ID不能为null");

            Message request = new Message();
            request.setType(MessageType.GET_ORDER_DETAIL);
            request.setData(orderId);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Order) response.getData();
            }
            throw new RuntimeException(response.getMsg() != null ?
                    response.getMsg() : "获取订单详情失败");
        } catch (Exception e) {
            System.err.println("获取订单详情失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "获取订单详情失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return null;
        }
    }

    /**
     * 更新订单状态
     * @param orderId 订单ID
     * @param status 新状态
     * @return 是否更新成功
     */
    public boolean updateOrderStatus(String orderId, String status) {
        try {
            validateCurrentUser(); // 验证当前用户
            Objects.requireNonNull(orderId, "订单ID不能为null");
            Objects.requireNonNull(status, "状态不能为null");

            Message request = new Message();
            request.setType(MessageType.UPDATE_ORDER_STATUS);
            request.setData(orderId);
            request.setExtra(new String[]{status, currentUser.getType()});

            Message response = clientSocket.sendRequest(request);
            if (!"success".equals(response.getStatus())) {
                throw new RuntimeException(response.getMsg() != null ?
                        response.getMsg() : "更新订单状态失败");
            }
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "更新订单状态失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 检查商品是否存在于任何订单中
     * @param productId 商品ID
     * @return 如果商品存在于任何订单中则返回true，否则返回false
     */
    public boolean isProductInOrders(String productId) {
        try {
            Objects.requireNonNull(productId, "商品ID不能为null");
            validateCurrentUser(); // 确保用户已登录

            Message request = new Message();
            request.setType(MessageType.IS_PRODUCT_IN_ORDERS);
            request.setData(productId);
            request.setExtra(currentUser); // 传递当前用户信息

            Message response = clientSocket.sendRequest(request);

            if ("success".equals(response.getStatus())) {
                return (Boolean) response.getData();
            } else {
                throw new RuntimeException(response.getMsg() != null ?
                        response.getMsg() : "检查商品订单关联失败");
            }
        } catch (Exception e) {
            System.err.println("检查商品订单关联失败: " + e.getMessage());
            return true; // 出错时保守处理，认为有关联
        }
    }

    /**
     * 删除订单
     * @param orderId 订单ID
     * @return 是否删除成功
     */
    public boolean deleteOrder(String orderId) {
        try {
            validateCurrentUser(); // 验证当前用户
            Objects.requireNonNull(orderId, "订单ID不能为null");

            Message request = new Message();
            request.setType(MessageType.DELETE_ORDER);
            request.setData(orderId);
            request.setExtra(currentUser); // 传递当前用户信息

            Message response = clientSocket.sendRequest(request);

            if (!"success".equals(response.getStatus())) {
                throw new RuntimeException(response.getMsg() != null ?
                        response.getMsg() : "删除订单失败");
            }
            return true;
        } catch (Exception e) {
            System.err.println("删除订单失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "删除订单失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 验证当前用户是否有效
     * @throws IllegalStateException 如果当前用户为null
     */
    private void validateCurrentUser() {
        if (currentUser == null) {
            System.err.println("[ERROR] 当前用户未设置");
            throw new IllegalStateException("用户未登录，请先登录");
        }
    }

    private void validateOrderRequest(List<OrderItem> items) {
        if (currentUser == null) {
            throw new IllegalStateException("用户未登录");
        }
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单商品不能为空");
        }
    }

    /**
     * 处理错误
     * @param e 异常对象
     */
    private void handleError(Exception e) {
        String errorMsg = "操作失败: " + e.getMessage();
        System.err.println(errorMsg);
        JOptionPane.showMessageDialog(null, errorMsg, "错误", JOptionPane.ERROR_MESSAGE);

        // 自动处理未登录情况
        if (e instanceof IllegalStateException && e.getMessage().contains("未登录")) {
            redirectToLogin();
        }
    }

    public boolean cancelOrder(String orderId) {
        try {
            // 1. 准备请求
            Message request = new Message();
            request.setType(MessageType.CANCEL_ORDER);
            request.setData(orderId);
            request.setExtra(new String[]{
                    currentUser.getUserId(),
                    currentUser.getType()
            });

            // 2. 发送请求
            Message response = clientSocket.sendRequest(request);

            // 3. 处理响应
            if (!"success".equals(response.getStatus())) {
                throw new RuntimeException(response.getMsg());
            }
            return true;
        } catch (Exception e) {
            handleCancelError(e);
            return false;
        }
    }

    private void handleCancelError(Exception e) {
        String errorMsg = "取消订单失败: " + e.getMessage();
        System.err.println(errorMsg);

        if (e.getMessage().contains("无权")) {
            JOptionPane.showMessageDialog(null,
                    errorMsg, "权限不足", JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(null,
                    errorMsg, "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    public boolean payOrder(String orderId) {
        try {
            if (currentUser == null) {
                throw new IllegalStateException("用户未登录");
            }

            Message request = new Message();
            request.setType(MessageType.PAY_ORDER);
            request.setData(orderId);
            request.setExtra(new String[]{
                    currentUser.getUserId(),
                    currentUser.getType()
            });

            Message response = clientSocket.sendRequest(request);

            if (!"success".equals(response.getStatus())) {
                String errorMsg = response.getMsg() != null ?
                        response.getMsg() : "支付订单失败";
                throw new RuntimeException(errorMsg);
            }
            return true;
        } catch (Exception e) {
            JOptionPane.showMessageDialog(null,
                    "支付订单失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 重定向到登录界面
     */
    private void redirectToLogin() {
        EventQueue.invokeLater(() -> {
            new LoginFrame().setVisible(true);
        });
    }
}