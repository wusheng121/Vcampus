package client.controller;

import client.net.ClientSocket;
import common.model.Product;
import common.model.ProductCategory;
import common.model.User;
import common.net.Message;
import server.dao.OrderDAO;
import server.dao.OrderDAOImpl;
import server.dao.ProductDAO;
import server.dao.ProductDAOImpl;
import server.service.ProductService;

import javax.swing.*;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class ProductController {
    private final ClientSocket clientSocket;
    private User currentUser;
    private final ProductService productService;

    public ProductController() {
        this(new ClientSocket(), createProductService());
    }

    public ProductController(ClientSocket clientSocket, ProductService productService) {
        this.clientSocket = clientSocket;
        this.productService = productService;
    }

    // 创建 ProductService 的辅助方法
    private static ProductService createProductService() {
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();
        return new ProductService(productDAO, orderDAO);
    }
    // 设置当前用户
    public void setCurrentUser(User user) {
        this.currentUser = Objects.requireNonNull(user, "用户不能为null");
        System.out.println("[DEBUG] 设置当前用户: " + currentUser.getUserId() + ", 类型: " + currentUser.getType());
    }

    public List<Product> getAllProducts() {
        try {
            Message request = new Message();
            request.setType("getAllProducts");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                List<Product> products = (List<Product>) response.getData();
                return products != null ? products : Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("获取商品列表失败: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public ProductCategory getCategoryById(String categoryId) {
        try {
            Message request = new Message();
            request.setType("getCategoryById");
            request.setData(categoryId);

            Message response = clientSocket.sendRequest(request);

            if ("success".equals(response.getStatus())) {
                return (ProductCategory) response.getData();
            }
        } catch (Exception e) {
            System.err.println("获取分类信息失败: " + e.getMessage());
        }
        return null;
    }

    public List<Product> getAvailableProducts() {
        try {
            Message request = new Message();
            request.setType("getAvailableProducts");

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                List<Product> products = (List<Product>) response.getData();
                return products != null ? products : Collections.emptyList();
            }
        } catch (Exception e) {
            System.err.println("获取可用商品失败: " + e.getMessage());
        }
        return Collections.emptyList();
    }

    public boolean addProduct(Product product) {
        try {
            // 验证用户
            if (currentUser == null) {
                throw new IllegalStateException("用户未登录");
            }
            if (!"admin".equalsIgnoreCase(currentUser.getType())) {
                throw new IllegalStateException("权限不足，需要管理员权限");
            }

            // 验证商品
            if (product == null || product.getProductId() == null) {
                throw new IllegalArgumentException("商品信息不完整");
            }

            // 准备请求
            Message request = new Message();
            request.setType("addProduct");
            request.setData(product);
            request.setExtra(currentUser);

            System.out.println("[DEBUG] 发送添加商品请求 - 用户: " + currentUser.getUserId());

            // 发送请求
            Message response = clientSocket.sendRequest(request);

            // 处理响应
            if (!"success".equals(response.getStatus())) {
                throw new RuntimeException(response.getMsg() != null ?
                        response.getMsg() : "添加商品失败");
            }
            return true;

        } catch (Exception e) {
            System.err.println("添加商品错误: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "添加商品失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public List<Product> searchProductsByName(String name) {
        try {
            Message request = new Message();
            request.setType("searchProductsByName");
            request.setData(name);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (List<Product>) response.getData();
            }
            return Collections.emptyList();
        } catch (Exception e) {
            System.err.println("按名称搜索商品错误: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    public Product getProductById(String productId) {
        try {
            Message request = new Message();
            request.setType("getProductById");
            request.setData(productId);

            Message response = clientSocket.sendRequest(request);
            if ("success".equals(response.getStatus())) {
                return (Product) response.getData();
            }
            return null;
        } catch (Exception e) {
            System.err.println("按ID获取商品错误: " + e.getMessage());
            return null;
        }
    }


    public boolean updateProduct(Product product) {
        try {
            // 验证用户
            if (currentUser == null) {
                throw new IllegalStateException("用户未登录，请重新登录");
            }
            if (!"admin".equalsIgnoreCase(currentUser.getType())) {
                throw new IllegalStateException("权限不足，需要管理员权限");
            }

            // 验证商品
            if (product == null) {
                throw new IllegalArgumentException("商品信息不能为空");
            }
            if (product.getProductId() == null || product.getProductId().trim().isEmpty()) {
                throw new IllegalArgumentException("商品ID不能为空");
            }

            System.out.println("[CLIENT] 准备更新商品 - ID: " + product.getProductId());
            System.out.println("[CLIENT] 新数据: " + product);

            // 准备请求
            Message request = new Message();
            request.setType("updateProduct");
            request.setData(product);
            request.setExtra(currentUser);

            // 发送请求
            Message response = clientSocket.sendRequest(request);

            // 处理响应
            if (!"success".equals(response.getStatus())) {
                String errorMsg = "更新失败: " +
                        (response.getMsg() != null ? response.getMsg() : "未知原因");

                // 根据不同的失败原因提供建议
                if (errorMsg.contains("不存在")) {
                    errorMsg += " (请刷新商品列表确认商品是否存在)";
                } else if (errorMsg.contains("权限")) {
                    errorMsg += " (请确认使用管理员账号登录)";
                }

                throw new RuntimeException(errorMsg);
            }
            return true;

        } catch (Exception e) {
            System.err.println("[CLIENT] 更新商品错误: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    e.getMessage(),
                    "更新失败",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    public boolean deleteProduct(String productId) {
        try {
            // 调试日志
            logDeleteAttempt(productId);

            // 准备请求
            Message request = buildDeleteRequest(productId);

            // 发送请求并处理响应
            return processDeleteResponse(clientSocket.sendRequest(request));

        } catch (Exception e) {
            handleDeleteError(e);
            return false;
        }
    }

// === 辅助方法 ===

    private boolean validateDeleteRequest(String productId) {
        // 1. 验证用户登录状态
        if (currentUser == null) {
            showErrorDialog("操作失败", "用户会话已过期，请重新登录");
            return false;
        }

        // 2. 验证管理员权限
        if (!"admin".equalsIgnoreCase(currentUser.getType())) {
            showErrorDialog("权限不足", "需要管理员权限才能删除商品");
            return false;
        }

        // 3. 验证商品ID格式
        if (productId == null || productId.trim().isEmpty()) {
            showErrorDialog("参数错误", "商品ID不能为空");
            return false;
        }

        return true;
    }

    private void logDeleteAttempt(String productId) {
        System.out.printf("[DELETE] 用户 %s 尝试删除商品 %s%n",
                currentUser.getUserId(), productId);
    }

    private Message buildDeleteRequest(String productId) {
        Message request = new Message();
        request.setType("deleteProduct");
        request.setData(productId);
        request.setExtra(currentUser);
        return request;
    }

    private boolean processDeleteResponse(Message response) {
        if ("success".equals(response.getStatus())) {
            System.out.println("[DELETE] 商品删除成功");
            showSuccessDialog("操作成功", "商品已成功删除");
            return true;
        }

        // 处理特定错误消息
        String errorMsg = response.getMsg() != null ?
                response.getMsg() : "未知错误导致删除失败";

        throw new RuntimeException(errorMsg);
    }

    private void handleDeleteError(Exception e) {
        System.err.println("[DELETE ERROR] " + e.getMessage());

        String title = "删除失败";
        String message = e.getMessage();

        // 特定错误处理
        if (e instanceof IllegalStateException) {
            if (e.getMessage().contains("权限")) {
                title = "权限不足";
            } else if (e.getMessage().contains("关联订单")) {
                title = "业务约束";
                message += "\n\n请先删除相关订单记录";
            }
        } else if (e instanceof IllegalArgumentException) {
            title = "参数错误";
        }

        showErrorDialog(title, message);
    }

    private void showErrorDialog(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE);
    }

    private void showSuccessDialog(String title, String message) {
        JOptionPane.showMessageDialog(null,
                message,
                title,
                JOptionPane.INFORMATION_MESSAGE);
    }


    public List<ProductCategory> getAllCategories() {
        return productService.getAllCategories();
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productService.getProductsByCategory(categoryId);
    }

    public boolean addProductCategory(ProductCategory category) {
        try {
            Message request = new Message();
            request.setType("addProductCategory");
            request.setData(category);
            request.setExtra(currentUser); // 传递当前用户信息

            Message response = clientSocket.sendRequest(request);

            if ("success".equals(response.getStatus())) {
                return true;
            } else {
                String errorMsg = response.getMsg() != null ?
                        response.getMsg() : "添加分类失败";
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            System.err.println("添加商品分类失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "添加分类失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }

    /**
     * 删除商品分类
     * @param categoryId 要删除的分类ID
     * @return 是否删除成功
     */
    public boolean deleteProductCategory(String categoryId) {
        try {
            Message request = new Message();
            request.setType("deleteProductCategory");
            request.setData(categoryId);
            request.setExtra(currentUser); // 传递当前用户信息

            Message response = clientSocket.sendRequest(request);

            if ("success".equals(response.getStatus())) {
                return true;
            } else {
                String errorMsg = response.getMsg() != null ?
                        response.getMsg() : "删除分类失败";
                throw new RuntimeException(errorMsg);
            }
        } catch (Exception e) {
            System.err.println("删除商品分类失败: " + e.getMessage());
            JOptionPane.showMessageDialog(null,
                    "删除分类失败: " + e.getMessage(),
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return false;
        }
    }


}