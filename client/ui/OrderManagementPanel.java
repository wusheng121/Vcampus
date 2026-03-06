package client.ui;

import client.controller.OrderController;
import common.model.Order;
import common.model.OrderItem;
import common.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class OrderManagementPanel extends JPanel {
    private JToolBar toolBar;
    private final OrderController orderController;
    private final User currentUser;
    private JTable orderTable;
    private DefaultTableModel tableModel;

    public OrderManagementPanel(User user) {
        this.currentUser = user;
        this.orderController = new OrderController();
        this.orderController.setCurrentUser(user);

        setLayout(new BorderLayout());

        initializeUI();
        loadOrders();
    }

    private void initializeUI() {

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 工具栏
        JPanel toolPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton btnRefresh = new JButton("刷新");
        JButton btnViewDetail = new JButton("查看详情");
        JButton btnUpdateStatus = new JButton("更新状态");

        btnRefresh.addActionListener(e -> loadOrders());
        btnViewDetail.addActionListener(e -> viewOrderDetail());
        btnUpdateStatus.addActionListener(e -> updateOrderStatus());

        toolPanel.add(btnRefresh);
        toolPanel.add(btnViewDetail);
        toolPanel.add(btnUpdateStatus);

        JButton btnDeleteOrder = new JButton("删除订单");
        btnDeleteOrder.addActionListener(e -> deleteOrder());
        toolPanel.add(btnDeleteOrder); // 添加到工具栏


        // 订单表格
        String[] columns = {"订单号", "用户ID", "总金额", "状态", "创建时间"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        orderTable = new JTable(tableModel);
        JScrollPane scrollPane = new JScrollPane(orderTable);

        mainPanel.add(toolPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        add(mainPanel);
    }

    private void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = "admin".equals(currentUser.getType()) ?
                orderController.getAllOrders() : orderController.getUserOrders();

        if (orders != null) {
            for (Order order : orders) {
                tableModel.addRow(new Object[]{
                        order.getOrderId(),
                        order.getUserId(),
                        "¥" + order.getTotalAmount(),
                        order.getStatus(),
                        order.getCreatedAt()
                });
            }
        }
    }

    private void cancelSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要取消的订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String status = (String) tableModel.getValueAt(selectedRow, 2);
        String amount = (String) tableModel.getValueAt(selectedRow, 1);

        // 检查订单状态是否允许取消
        if (!"待支付".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "只有'待支付'状态的订单可以取消\n当前状态: " + status,
                    "无法取消",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要取消订单吗？\n订单号: " + orderId + "\n金额: " + amount,
                "确认取消",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderController.cancelOrder(orderId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "订单取消成功", "成功", JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this,
                        "取消订单失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void viewOrderDetail() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要查看的订单");
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        Order order = orderController.getOrderDetail(orderId);

        if (order != null && order.getItems() != null) {
            StringBuilder detail = new StringBuilder();
            detail.append("订单号: ").append(order.getOrderId()).append("\n");
            detail.append("用户ID: ").append(order.getUserId()).append("\n");
            detail.append("总金额: ¥").append(order.getTotalAmount()).append("\n");
            detail.append("状态: ").append(order.getStatus()).append("\n");
            detail.append("创建时间: ").append(order.getCreatedAt()).append("\n\n");
            detail.append("商品明细:\n");

            for (OrderItem item : order.getItems()) {
                detail.append("- ").append(item.getProduct().getName())
                        .append(" × ").append(item.getQuantity())
                        .append(" = ¥").append(item.getSubtotal()).append("\n");
            }

            JTextArea textArea = new JTextArea(detail.toString());
            textArea.setEditable(false);
            JScrollPane scrollPane = new JScrollPane(textArea);
            scrollPane.setPreferredSize(new Dimension(500, 300));

            JOptionPane.showMessageDialog(this, scrollPane, "订单详情", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "获取订单详情失败");
        }
    }

    private void updateOrderStatus() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要更新的订单");
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String currentStatus = (String) tableModel.getValueAt(selectedRow, 3);

        JComboBox<String> cmbStatus = new JComboBox<>(new String[]{"待支付", "已支付", "已取消", "已完成"});
        cmbStatus.setSelectedItem(currentStatus);

        Object[] message = {
                "订单号: " + orderId,
                "新状态:", cmbStatus
        };

        int option = JOptionPane.showConfirmDialog(this, message, "更新订单状态", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String newStatus = (String) cmbStatus.getSelectedItem();
            if (orderController.updateOrderStatus(orderId, newStatus)) {
                JOptionPane.showMessageDialog(this, "订单状态更新成功");
                loadOrders();
            }
        }
    }

    private void deleteOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要删除的订单",
                    "提示",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) tableModel.getValueAt(selectedRow, 0);
        String orderStatus = (String) tableModel.getValueAt(selectedRow, 3);

        // 检查订单状态
        if (!"待支付".equals(orderStatus)) {
            JOptionPane.showMessageDialog(this,
                    "只能删除状态为'待支付'的订单",
                    "错误",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除订单 " + orderId + " 吗？此操作不可撤销！",
                "确认删除",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            if (orderController.deleteOrder(orderId)) {
                JOptionPane.showMessageDialog(this,
                        "订单删除成功",
                        "成功",
                        JOptionPane.INFORMATION_MESSAGE);
                loadOrders(); // 刷新订单列表
            } else {
                JOptionPane.showMessageDialog(this,
                        "订单删除失败",
                        "错误",
                        JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}

