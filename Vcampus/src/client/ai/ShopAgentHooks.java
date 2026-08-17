package client.ai;

import client.controller.OrderController;
import client.controller.ProductController;
import common.model.Order;
import common.model.OrderItem;
import common.model.Product;
import common.model.User;

import java.util.List;

/**
 * 商店购物助理 Hook。
 * 数据集：在售商品（available_products）+ 本人订单（my_orders）。
 */
public class ShopAgentHooks implements AgentHooks {
    private final User user;
    private final ProductController productController = new ProductController();
    private final OrderController orderController = new OrderController();

    public ShopAgentHooks(User user) {
        this.user = user;
        this.orderController.setCurrentUser(user);
    }

    @Override
    public String systemPrompt() {
        return AgentPrompts.shopSystemPrompt();
    }

    @Override
    public String buildDataset() {
        StringBuilder sb = new StringBuilder();
        sb.append("<<DATASET>>\n{\n");

        // 在售商品
        sb.append("  \"available_products\": [\n");
        List<Product> products = productController.getAvailableProducts();
        if (products != null) {
            for (int i = 0; i < products.size(); i++) {
                Product p = products.get(i);
                sb.append("    {\"product_id\":\"").append(safe(p.getProductId()))
                  .append("\",\"name\":\"").append(safe(p.getName()))
                  .append("\",\"price\":").append(p.getPrice())
                  .append(",\"stock\":").append(p.getStock())
                  .append(",\"status\":\"").append(safe(p.getStatus())).append("\"}");
                if (i < products.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("  ],\n");

        // 我的订单
        sb.append("  \"my_orders\": [\n");
        List<Order> orders = orderController.getUserOrders();
        if (orders != null) {
            for (int i = 0; i < orders.size(); i++) {
                Order o = orders.get(i);
                sb.append("    {\"order_id\":\"").append(safe(o.getOrderId()))
                  .append("\",\"status\":\"").append(safe(o.getStatus()))
                  .append("\",\"total_amount\":").append(o.getTotalAmount())
                  .append(",\"items\":[");
                List<OrderItem> items = o.getItems();
                if (items != null) {
                    for (int j = 0; j < items.size(); j++) {
                        OrderItem it = items.get(j);
                        sb.append("{\"product_id\":\"").append(safe(it.getProductId()))
                          .append("\",\"name\":\"").append(it.getProduct() != null ? safe(it.getProduct().getName()) : "")
                          .append("\",\"quantity\":").append(it.getQuantity())
                          .append(",\"price\":").append(it.getPrice()).append("}");
                        if (j < items.size() - 1) sb.append(",");
                    }
                }
                sb.append("]}");
                if (i < orders.size() - 1) sb.append(",");
                sb.append("\n");
            }
        }
        sb.append("  ]\n}\n");
        sb.append("</DATASET>\n");
        return sb.toString();
    }

    @Override
    public String helpText() {
        return """
                我是你的商店购物助理

                可用指令：
                • /help —— 查看帮助
                • /clear —— 清空对话与上下文

                我能做什么：
                • 查询在售商品、价格、库存
                • 查看你的订单与状态、订单明细
                • 不替你下单/支付/取消，仅提供信息与建议
                """;
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"").replace("\n", " ").replace("\r", " ");
    }
}
