package client.ui;

import client.controller.OrderController;
import common.model.Order;
import common.model.Product;
import common.model.ProductCategory;
import common.model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class AdminShopFrame extends ShopFrame {
    private final OrderController orderController;
    private JTabbedPane tabbedPane;

    public AdminShopFrame(User adminUser) {
        super(adminUser);
        this.orderController = new OrderController();
        this.orderController.setCurrentUser(adminUser);

        setupAdminUI();
    }

    private void setupAdminUI() {
        setLayout(new BorderLayout());
        tabbedPane = new JTabbedPane();

        // 1. 商品购买页（带分类筛选）
        tabbedPane.addTab("商品购买", createPurchasePanelWithCategoryFilter());

        // 2. 商品管理页
        tabbedPane.addTab("商品管理", new ProductManagementPanel(getCurrentUser(), orderController));

        // 3. 订单管理页
        tabbedPane.addTab("订单管理", new OrderManagementPanel(getCurrentUser()));

        // 4. 我的订单页
        tabbedPane.addTab("我的订单", createMyOrdersPanel());

        add(tabbedPane, BorderLayout.CENTER);
        add(getStatusBar(), BorderLayout.SOUTH);
    }

    /**
     * 商品购买面板（带分类筛选工具栏）
     */
    private JPanel createPurchasePanelWithCategoryFilter() {
        JPanel panel = new JPanel(new BorderLayout());

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("商品分类:"));

        JComboBox<String> categoryCombo = new JComboBox<>();
        categoryCombo.addItem("所有分类");

        List<ProductCategory> categories = productController.getAllCategories();
        if (categories != null) {
            for (ProductCategory category : categories) {
                categoryCombo.addItem(category.getCategoryName());
            }
        }

        categoryCombo.addActionListener(e -> {
            String selected = (String) categoryCombo.getSelectedItem();
            if ("所有分类".equals(selected)) {
                refreshProductTable();
            } else {
                String categoryId = getCategoryIdByName(selected);
                if (categoryId != null) {
                    List<Product> filteredProducts = productController.getProductsByCategory(categoryId);
                    refreshProductTable(filteredProducts);
                } else {
                    JOptionPane.showMessageDialog(this, "找不到选中的分类", "错误", JOptionPane.ERROR_MESSAGE);
                }
            }
        });

        filterPanel.add(categoryCombo);
        panel.add(filterPanel, BorderLayout.NORTH);

        panel.add(super.getProductBrowserPanel(), BorderLayout.CENTER);
        panel.add(super.getBottomPanel(), BorderLayout.SOUTH);

        return panel;
    }

    private String getCategoryIdByName(String name) {
        List<ProductCategory> categories = productController.getAllCategories();
        if (categories != null) {
            for (ProductCategory category : categories) {
                if (category.getCategoryName().equals(name)) {
                    return category.getCategoryId();
                }
            }
        }
        return null;
    }

    /**
     * 我的订单面板
     */
    private JPanel createMyOrdersPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        // 订单表格
        String[] columns = {"订单号", "总金额", "状态", "创建时间"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        JTable orderTable = new JTable(model);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        orderTable.setRowHeight(30);

        // 加载订单数据
        loadMyOrders(model);

        // 添加表格到滚动面板
        JScrollPane scrollPane = new JScrollPane(orderTable);
        panel.add(scrollPane, BorderLayout.CENTER);

        // 操作按钮面板 - 修改为使用BoxLayout实现居中
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new BoxLayout(buttonPanel, BoxLayout.X_AXIS));
        buttonPanel.add(Box.createHorizontalGlue()); // 左侧弹性空间

        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> loadMyOrders(model));
        buttonPanel.add(refreshButton);

        // 添加按钮间间距
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // 支付按钮
        JButton payButton = new JButton("支付");
        payButton.setEnabled(false);
        payButton.addActionListener(e -> paySelectedOrder(orderTable, model));
        buttonPanel.add(payButton);

        // 添加按钮间间距
        buttonPanel.add(Box.createRigidArea(new Dimension(10, 0)));

        // 取消按钮
        JButton cancelButton = new JButton("取消");
        cancelButton.setEnabled(false);
        cancelButton.addActionListener(e -> cancelSelectedOrder(orderTable, model));
        buttonPanel.add(cancelButton);

        buttonPanel.add(Box.createHorizontalGlue()); // 右侧弹性空间

        // 表格选择监听器
        orderTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                int selectedRow = orderTable.getSelectedRow();
                if (selectedRow >= 0) {
                    String status = (String) orderTable.getValueAt(selectedRow, 2);
                    payButton.setEnabled("待支付".equals(status));
                    cancelButton.setEnabled(canCancelOrder(status));
                } else {
                    payButton.setEnabled(false);
                    cancelButton.setEnabled(false);
                }
            }
        });

        // 添加底部边距
        JPanel southPanel = new JPanel(new BorderLayout());
        southPanel.add(buttonPanel, BorderLayout.CENTER);
        southPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0)); // 上下边距

        panel.add(southPanel, BorderLayout.SOUTH);

        return panel;
    }

    private void loadMyOrders(DefaultTableModel model) {
        model.setRowCount(0);
        List<Order> orders = orderController.getUserOrders();
        if (orders != null) {
            for (Order order : orders) {
                model.addRow(new Object[]{
                        order.getOrderId(),
                        "¥" + order.getTotalAmount(),
                        order.getStatus(),
                        formatDate(order.getCreatedAt())
                });
            }
        }
    }

    private void paySelectedOrder(JTable orderTable, DefaultTableModel model) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要支付订单 " + orderId + " 吗？",
                    "确认支付",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = orderController.payOrder(orderId);

                if (success) {
                    JOptionPane.showMessageDialog(this,
                            "订单支付成功！",
                            "支付成功",
                            JOptionPane.INFORMATION_MESSAGE);
                    loadMyOrders(model); // 刷新订单列表
                } else {
                    JOptionPane.showMessageDialog(this,
                            "订单支付失败，请稍后重试！",
                            "支付失败",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        }
    }

    private void cancelSelectedOrder(JTable orderTable, DefaultTableModel model) {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            String orderId = (String) orderTable.getValueAt(selectedRow, 0);
            String currentStatus = (String) orderTable.getValueAt(selectedRow, 2);

            // 先检查订单状态
            if (!canCancelOrder(currentStatus)) {
                JOptionPane.showMessageDialog(this,
                        "当前订单状态不允许取消",
                        "取消失败",
                        JOptionPane.WARNING_MESSAGE);
                return;
            }

            int confirm = JOptionPane.showConfirmDialog(this,
                    "确定要取消订单 " + orderId + " 吗？",
                    "确认取消",
                    JOptionPane.YES_NO_OPTION);

            if (confirm == JOptionPane.YES_OPTION) {
                new SwingWorker<Boolean, Void>() {
                    @Override
                    protected Boolean doInBackground() throws Exception {
                        return orderController.cancelOrder(orderId);
                    }

                    @Override
                    protected void done() {
                        try {
                            boolean success = get(); // 获取doInBackground的结果
                            if (success) {
                                JOptionPane.showMessageDialog(AdminShopFrame.this,
                                        "订单取消成功！",
                                        "取消成功",
                                        JOptionPane.INFORMATION_MESSAGE);
                                loadMyOrders(model); // 刷新订单列表
                            }
                        } catch (Exception e) {
                            // 错误信息已由OrderController处理
                            loadMyOrders(model); // 刷新订单列表获取最新状态
                        }
                    }
                }.execute();
            }
        }
    }

    private boolean canCancelOrder(String status) {
        return "待支付".equals(status) || "已支付".equals(status) || "待发货".equals(status);
    }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    /**
     * 创建简化版购买面板（不显示右侧购物车，可选备用）
     */
    private JPanel createSimplifiedPurchasePanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.add(super.getProductBrowserPanel(), BorderLayout.CENTER);
        panel.add(super.getBottomPanel(), BorderLayout.SOUTH);
        return panel;
    }
}