package server.net;

import common.model.Order;
import common.model.OrderItem;
import common.model.Product;
import common.model.ProductCategory;
import common.model.User;
import common.net.Message;
import common.net.MessageType;
import common.Permissions;
import server.dao.OrderDAO;
import server.dao.OrderDAOImpl;
import server.dao.ProductDAO;
import server.dao.ProductDAOImpl;
import server.service.AuthService;
import server.service.OrderService;
import server.service.ProductService;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 商店：商品、分类与订单域
 */
public class ShopHandler implements Handler {
    private final ProductService productService;
    private final OrderService orderService = new OrderService();
    private final AuthService auth = new AuthService();

    /** RBAC 拦截：用 caller（ClientSocket 自动注入）判权限 */
    private boolean denied(Message request, Message response, String perm) {
        User c = request.getCaller();
        if (c == null || !auth.hasPermission(c.getType(), perm)) {
            response.setStatus("fail");
            response.setMsg("无权限：" + perm);
            return true;
        }
        return false;
    }

    public ShopHandler() {
        ProductDAO productDAO = new ProductDAOImpl();
        OrderDAO orderDAO = new OrderDAOImpl();
        this.productService = new ProductService(productDAO, orderDAO);
    }

    @Override
    public Message handle(Message request) {
        Message response = new Message();
        switch (request.getType()) {
            case MessageType.ADD_PRODUCT -> {
                if (denied(request, response, Permissions.PRODUCT_CREATE)) break;
                Product product = (Product) request.getData();
                User user = request.getCaller();
                if (product == null || product.getProductId() == null) {
                    response.setStatus("fail");
                    response.setMsg("商品信息不完整");
                    break;
                }
                if (productService.addProduct(product, user.getType())) {
                    response.setStatus("success");
                    response.setMsg("商品添加成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("添加商品失败，可能是商品ID已存在");
                }
            }
            case MessageType.GET_ALL_PRODUCTS -> {
                List<Product> products = productService.getAllProducts();
                response.setStatus("success");
                response.setData(products);
            }
            case MessageType.GET_AVAILABLE_PRODUCTS -> {
                List<Product> products = productService.getAvailableProducts();
                response.setStatus("success");
                response.setData(products);
            }
            case MessageType.DELETE_PRODUCT -> {
                if (denied(request, response, Permissions.PRODUCT_DELETE)) break;
                String productId = (String) request.getData();
                User user = request.getCaller();
                if (productService.deleteProduct(productId, user.getType())) {
                    response.setStatus("success");
                    response.setMsg("商品删除成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("商品删除失败，可能原因：商品不存在、有关联订单或权限不足");
                }
            }
            case MessageType.SEARCH_PRODUCTS_BY_NAME -> {
                String name = (String) request.getData();
                List<Product> products = productService.searchProductsByName(name);
                response.setStatus("success");
                response.setData(products);
            }
            case MessageType.GET_PRODUCT_BY_ID -> {
                String productId = (String) request.getData();
                Product product = productService.getProductById(productId);
                if (product != null) {
                    response.setStatus("success");
                    response.setData(product);
                } else {
                    response.setStatus("fail");
                    response.setMsg("未找到指定商品");
                }
            }
            case MessageType.UPDATE_PRODUCT -> {
                if (denied(request, response, Permissions.PRODUCT_UPDATE)) break;
                Product product = (Product) request.getData();
                User user = request.getCaller();
                if (product == null || product.getProductId() == null) {
                    response.setStatus("fail");
                    response.setMsg("商品信息不完整");
                    break;
                }
                if (productService.updateProduct(product, user.getType())) {
                    response.setStatus("success");
                    response.setMsg("商品更新成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("商品更新失败");
                }
            }
            case MessageType.GET_ALL_ORDERS -> {
                if (denied(request, response, Permissions.ORDER_READ_ALL)) break;
                List<Order> orders = orderService.getAllOrders();
                response.setStatus("success");
                response.setData(orders);
            }
            case MessageType.GET_USER_ORDERS -> {
                if (denied(request, response, Permissions.ORDER_READ_SELF)) break;
                User user = (User) request.getData();
                if (user == null) {
                    response.setStatus("fail");
                    response.setMsg("用户未登录");
                    break;
                }
                List<Order> orders = orderService.getUserOrders(user.getUserId());
                response.setStatus("success");
                response.setData(orders);
            }
            case MessageType.GET_ORDER_DETAIL -> {
                String orderId = (String) request.getData();
                Order order = orderService.getOrderDetail(orderId);
                if (order != null) {
                    response.setStatus("success");
                    response.setData(order);
                } else {
                    response.setStatus("fail");
                    response.setMsg("未找到指定订单");
                }
            }
            case MessageType.CREATE_ORDER -> {
                if (denied(request, response, Permissions.ORDER_CREATE)) break;
                List<OrderItem> items = (List<OrderItem>) request.getData();
                User user = request.getCaller();
                if (items == null || items.isEmpty()) {
                    response.setStatus("fail");
                    response.setMsg("订单商品不能为空");
                    break;
                }
                for (OrderItem item : items) {
                    if (item.getQuantity() <= 0) {
                        response.setStatus("fail");
                        response.setMsg("商品数量必须大于0");
                        break;
                    }
                    if (!productService.exists(item.getProductId())) {
                        response.setStatus("fail");
                        response.setMsg("商品不存在: " + item.getProductId());
                        break;
                    }
                }
                Map<String, Integer> insufficientItems = new HashMap<>();
                for (OrderItem item : items) {
                    Product product = productService.getProductById(item.getProductId());
                    if (product.getStock() < item.getQuantity()) {
                        insufficientItems.put(product.getName(), item.getQuantity() - product.getStock());
                    }
                }
                if (!insufficientItems.isEmpty()) {
                    StringBuilder sb = new StringBuilder("库存不足:\n");
                    insufficientItems.forEach((name, shortage) ->
                            sb.append("- ").append(name).append(" 缺少").append(shortage).append("件\n"));
                    response.setStatus("fail");
                    response.setMsg(sb.toString());
                    break;
                }
                boolean success = orderService.createOrder(user.getUserId(), items);
                if (success) {
                    response.setStatus("success");
                    response.setMsg("订单创建成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("订单创建失败");
                }
            }
            case MessageType.PAY_ORDER -> {
                if (denied(request, response, Permissions.ORDER_PAY)) break;
                String orderId = (String) request.getData();
                String[] extra = (String[]) request.getExtra();
                String userId = extra[0];
                String userType = extra[1];
                boolean success = orderService.payOrder(orderId, userId, userType);
                response.setStatus(success ? "success" : "fail");
                response.setMsg(success ? "订单支付成功" : "订单支付失败");
            }
            case MessageType.UPDATE_ORDER_STATUS -> {
                if (denied(request, response, Permissions.ORDER_UPDATE_STATUS)) break;
                String orderId = (String) request.getData();
                String[] extra = (String[]) request.getExtra();
                String status = extra[0];
                String operatorRole = extra[1];
                boolean success = orderService.updateOrderStatus(orderId, status, operatorRole);
                if (success) {
                    response.setStatus("success");
                    response.setMsg("订单状态更新成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("订单状态更新失败");
                }
            }
            case MessageType.DELETE_ORDER -> {
                if (denied(request, response, Permissions.ORDER_DELETE)) break;
                String orderId = (String) request.getData();
                User user = request.getCaller();
                boolean success = orderService.deleteOrder(orderId, user.getType());
                if (success) {
                    response.setStatus("success");
                    response.setMsg("订单删除成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("订单删除失败");
                }
            }
            case MessageType.CANCEL_ORDER -> {
                if (denied(request, response, Permissions.ORDER_CANCEL)) break;
                String orderId = (String) request.getData();
                String[] extra = (String[]) request.getExtra();
                String userId = extra[0];
                String operatorRole = extra[1];
                boolean success = orderService.cancelOrder(orderId, userId);
                if (success) {
                    response.setStatus("success");
                    response.setMsg("订单取消成功");
                } else {
                    response.setStatus("fail");
                    response.setMsg("订单取消失败");
                }
            }
            case MessageType.IS_PRODUCT_IN_ORDERS -> {
                String productId = (String) request.getData();
                boolean exists = orderService.isProductInOrders(productId);
                response.setStatus("success");
                response.setData(exists);
            }
            case MessageType.ADD_PRODUCT_CATEGORY -> {
                if (denied(request, response, Permissions.CATEGORY_CREATE)) break;
                ProductCategory category = (ProductCategory) request.getData();
                boolean success = productService.addProductCategory(category);
                response.setStatus(success ? "success" : "fail");
            }
            case MessageType.DELETE_PRODUCT_CATEGORY -> {
                if (denied(request, response, Permissions.CATEGORY_DELETE)) break;
                String categoryId = (String) request.getData();
                boolean success = productService.deleteProductCategory(categoryId);
                response.setStatus(success ? "success" : "fail");
            }
            case MessageType.GET_ALL_CATEGORIES -> {
                response.setStatus("success");
                response.setData(productService.getAllCategories());
            }
            case MessageType.GET_PRODUCTS_BY_CATEGORY -> {
                String categoryId = (String) request.getData();
                response.setStatus("success");
                response.setData(productService.getProductsByCategory(categoryId));
            }
            case MessageType.GET_CATEGORY_BY_ID -> {
                String categoryId = (String) request.getData();
                response.setStatus("success");
                response.setData(productService.getCategoryById(categoryId));
            }
            default -> {
                return null;
            }
        }
        return response;
    }
}
