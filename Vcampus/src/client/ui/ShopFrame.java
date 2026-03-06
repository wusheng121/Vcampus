package client.ui;

import client.controller.OrderController;
import client.controller.ProductController;
import common.model.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.List;

public class ShopFrame extends JPanel {
    // UI组件
    protected JTable productTable;
    protected DefaultTableModel productTableModel;
    protected List<OrderItem> cartItems = new ArrayList<>();
    protected JLabel lblCartTotal;
    private JLabel statusBar;
    protected DefaultTableModel orderTableModel;
    protected JTable orderTable;
    protected JPanel productBrowserPanel;
    protected JPanel shoppingCartPanel;
    private JComboBox<String> categoryComboBox;
    protected JPanel mainPanel;

    private final Map<String, String> categoryNameToIdMap = new HashMap<>();

    // 控制器
    protected final ProductController productController;
    protected final OrderController orderController;
    protected final User currentUser;

    public ShopFrame(User user) {
        this.currentUser = Objects.requireNonNull(user);
        this.productController = new ProductController();
        this.orderController = new OrderController();
        productController.setCurrentUser(currentUser);
        orderController.setCurrentUser(currentUser);

        initializeUI();
        initStatusBar();
    }

    /**
     * 初始化UI
     */
    protected void initializeUI() {
        setLayout(new BorderLayout());

        // 工具栏
        add(createToolbar(), BorderLayout.NORTH);

        // 筛选面板
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.add(new JLabel("商品分类:"));

        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem("所有分类"); // 默认选项
        loadCategories(); // 加载分类数据
        categoryComboBox.addActionListener(e -> filterProductsByCategory());
        filterPanel.add(categoryComboBox);

        // 主面板
        mainPanel = createMainPanel();
        add(mainPanel, BorderLayout.CENTER);

        mainPanel.add(filterPanel, BorderLayout.NORTH);
    }

    private void loadCategories() {
        List<ProductCategory> categories = productController.getAllCategories();
        if (categories != null) {
            for (ProductCategory category : categories) {
                categoryComboBox.addItem(category.getCategoryName());
            }
        }
    }

    private void filterProductsByCategory() {
        String selected = (String) categoryComboBox.getSelectedItem();
        if ("所有分类".equals(selected)) {
            refreshProductTable(); // 显示所有商品
        } else {
            List<Product> filteredProducts = productController.getProductsByCategory(
                    getCategoryIdByName(selected)
            );
            refreshProductTable(filteredProducts);
        }
    }

    private String getCategoryIdByName(String categoryName) {
        if (categoryNameToIdMap.isEmpty()) {
            loadCategoryMap();
        }
        return categoryNameToIdMap.get(categoryName);
    }

    private void loadCategoryMap() {
        List<ProductCategory> categories = productController.getAllCategories();
        if (categories != null) {
            categoryNameToIdMap.clear();
            for (ProductCategory category : categories) {
                if (category != null && category.getCategoryName() != null) {
                    categoryNameToIdMap.put(category.getCategoryName(), category.getCategoryId());
                }
            }
        }
    }

    private void updateCategoryMap(ProductCategory category) {
        if (category != null && category.getCategoryName() != null) {
            categoryNameToIdMap.put(category.getCategoryName(), category.getCategoryId());
        }
    }

    /**
     * 创建主面板
     */
    protected JPanel createMainPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        productBrowserPanel = createProductBrowserPanel();
        panel.add(productBrowserPanel, BorderLayout.CENTER);

        JPanel bottomPanel = createBottomPanel();
        panel.add(bottomPanel, BorderLayout.SOUTH);

        return panel;
    }

    /**
     * 创建商品浏览面板
     */
    protected JPanel createProductBrowserPanel() {
        JPanel panel = new JPanel(new BorderLayout());

        productTableModel = new DefaultTableModel(
                new String[]{"商品ID", "商品名称", "价格", "库存", "状态"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };

        productTable = new JTable(productTableModel);
        JScrollPane scrollPane = new JScrollPane(productTable);

        refreshProductTable();

        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }

    protected void refreshProductTable() {
        productTableModel.setRowCount(0);
        List<Product> products = productController.getAvailableProducts();
        if (products != null) {
            for (Product product : products) {
                productTableModel.addRow(new Object[]{
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getStatus()
                });
            }
        }
    }

    protected void refreshProductTable(List<Product> products) {
        productTableModel.setRowCount(0);
        if (products != null) {
            for (Product product : products) {
                String categoryName = "未分类";
                if (product.getCategoryId() != null) {
                    ProductCategory category = productController.getCategoryById(product.getCategoryId());
                    if (category != null) categoryName = category.getCategoryName();
                }
                productTableModel.addRow(new Object[]{
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getStatus(),
                        categoryName
                });
            }
        }
    }

    /**
     * 创建底部面板（购物车信息和按钮）
     */
    protected JPanel createBottomPanel() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));

        // 左侧按钮
        JButton btnAddToCart = new JButton("加入购物车");
        btnAddToCart.addActionListener(e -> addToCart());
        btnAddToCart.setBackground(new Color(76,175,80));
        btnAddToCart.setForeground(Color.WHITE);
        btnAddToCart.setFont(new Font("微软雅黑", Font.BOLD, 14));
        btnAddToCart.setPreferredSize(new Dimension(120,40));
        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.add(btnAddToCart);

        // 右侧购物车信息
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        lblCartTotal = new JLabel("购物车: 0件商品");
        lblCartTotal.setFont(new Font("微软雅黑", Font.PLAIN, 14));

        JButton btnViewCart = new JButton("查看购物车");
        btnViewCart.addActionListener(e -> viewCart());
        btnViewCart.setBackground(new Color(33,150,243));
        btnViewCart.setForeground(Color.WHITE);

        rightPanel.add(lblCartTotal);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(btnViewCart);

        panel.add(leftPanel, BorderLayout.WEST);
        panel.add(rightPanel, BorderLayout.EAST);

        return panel;
    }

    protected JToolBar createToolbar() {
        JToolBar toolBar = new JToolBar();
        toolBar.setFloatable(false);

        JButton btnOrders = new JButton("我的订单");
        btnOrders.addActionListener(e -> showMyOrders());
        toolBar.add(btnOrders);

        JButton btnRefresh = new JButton("刷新");
        btnRefresh.addActionListener(e -> loadProducts());
        toolBar.add(btnRefresh);

        return toolBar;
    }

    private void loadProducts() {
        try {
            List<Product> products = productController.getAvailableProducts();
            refreshProductTable(products);
            setStatus("商品加载完成");
        } catch (Exception e) {
            setStatus("加载商品失败: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "加载商品失败: " + e.getMessage(),
                    "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void addToCart() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要购买的商品");
            return;
        }

        String productId = (String) productTableModel.getValueAt(selectedRow, 0);
        String productName = (String) productTableModel.getValueAt(selectedRow, 1);
        BigDecimal price = (BigDecimal) productTableModel.getValueAt(selectedRow, 2);
        Integer stock = (Integer) productTableModel.getValueAt(selectedRow, 3);

        String quantityStr = JOptionPane.showInputDialog(this, "请输入购买数量:", "购买 " + productName, JOptionPane.QUESTION_MESSAGE);
        if (quantityStr == null || quantityStr.trim().isEmpty()) return;

        try {
            int quantity = Integer.parseInt(quantityStr);
            if (quantity <= 0) { JOptionPane.showMessageDialog(this, "购买数量必须大于0"); return; }
            if (quantity > stock) { JOptionPane.showMessageDialog(this, "库存不足，当前库存: " + stock); return; }

            OrderItem item = new OrderItem();
            item.setProductId(productId);
            item.setQuantity(quantity);
            item.setPrice(price);

            cartItems.add(item);
            updateCartSummary();

            JOptionPane.showMessageDialog(this, "已添加到购物车: " + productName + " × " + quantity);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this, "请输入有效的数量");
        }
    }

    private void updateCartSummary() {
        int totalItems = cartItems.stream().mapToInt(OrderItem::getQuantity).sum();
        lblCartTotal.setText("购物车: " + totalItems + "件商品");
    }

    private void viewCart() {
        if (cartItems.isEmpty()) {
            JOptionPane.showMessageDialog(this, "购物车为空");
            return;
        }

        JFrame cartFrame = new JFrame("购物车");
        cartFrame.setSize(500,400);
        cartFrame.setLocationRelativeTo(this);

        String[] columns = {"商品","数量","单价","小计"};
        DefaultTableModel cartModel = new DefaultTableModel(columns,0);
        JTable cartTable = new JTable(cartModel);

        BigDecimal totalAmount = BigDecimal.ZERO;
        for (OrderItem item : cartItems) {
            BigDecimal subtotal = item.getPrice().multiply(BigDecimal.valueOf(item.getQuantity()));
            cartModel.addRow(new Object[]{item.getProductId(), item.getQuantity(), "¥" + item.getPrice(), "¥" + subtotal});
            totalAmount = totalAmount.add(subtotal);
        }

        JPanel totalPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        totalPanel.add(new JLabel("总计: ¥" + totalAmount));

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JButton btnCheckout = new JButton("结算");
        JButton btnClear = new JButton("清空购物车");
        buttonPanel.add(btnCheckout);
        buttonPanel.add(btnClear);

        cartFrame.setLayout(new BorderLayout());
        cartFrame.add(new JScrollPane(cartTable), BorderLayout.CENTER);
        cartFrame.add(totalPanel, BorderLayout.NORTH);
        cartFrame.add(buttonPanel, BorderLayout.SOUTH);

        btnCheckout.addActionListener(e -> checkout(cartFrame));
        btnClear.addActionListener(e -> {
            cartItems.clear();
            updateCartSummary();
            cartFrame.dispose();
            JOptionPane.showMessageDialog(this, "购物车已清空");
        });

        cartFrame.setVisible(true);
    }

    private void checkout(JFrame cartFrame) {
        if (cartItems.isEmpty()) { JOptionPane.showMessageDialog(this, "购物车为空"); return; }
        try {
            boolean success = orderController.createOrder(currentUser, cartItems);
            if (success) {
                JOptionPane.showMessageDialog(cartFrame, "订单创建成功！");
                cartItems.clear();
                updateCartSummary();
                cartFrame.dispose();
            } else {
                JOptionPane.showMessageDialog(cartFrame, "订单创建失败，请重试");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(cartFrame, "结算时出错: " + e.getMessage(), "错误", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void initStatusBar() {
        statusBar = new JLabel("就绪");
        statusBar.setBorder(BorderFactory.createEtchedBorder());
        add(statusBar, BorderLayout.SOUTH);
    }

    public JLabel getStatusBar() { return statusBar; }
    public void setStatus(String message) { statusBar.setText(message); }

    public User getCurrentUser() { return currentUser; }

    private String formatDate(Date date) {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(date);
    }

    private void showMyOrders() {

        if (orderTableModel == null) {
            String[] columns = {"订单号", "总金额", "状态", "创建时间"};
            orderTableModel = new DefaultTableModel(columns, 0) {
                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };
            orderTable = new JTable(orderTableModel);
        }
        JFrame orderFrame = new JFrame("我的订单 - " + currentUser.getUserId());
        orderFrame.setSize(800, 500);
        orderFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        orderFrame.setLocationRelativeTo(null);

        // 主面板
        JPanel mainPanel = new JPanel(new BorderLayout());

        // 1. 添加操作按钮面板
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // 2. 创建订单表格
        String[] columns = {"订单号", "总金额", "状态", "创建时间"};
        orderTableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        orderTable = new JTable(orderTableModel);
        orderTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // 设置表格样式
        orderTable.setBackground(new Color(240, 248, 255)); // 浅蓝色
        orderTable.setGridColor(Color.LIGHT_GRAY);

        JScrollPane scrollPane = new JScrollPane(orderTable);

        // 3. 加载订单数据
        loadOrderData();

        // 刷新按钮
        JButton refreshButton = new JButton("刷新");
        refreshButton.addActionListener(e -> refreshOrderTable());
        buttonPanel.add(refreshButton);

        // 支付订单按钮（新增）
        JButton payButton = new JButton("支付订单");
        payButton.addActionListener(e -> paySelectedOrder());
        buttonPanel.add(payButton);

        // 取消订单按钮
        JButton cancelButton = new JButton("取消订单");
        cancelButton.addActionListener(e -> cancelSelectedOrder());
        buttonPanel.add(cancelButton);

        // 布局
        mainPanel.add(buttonPanel, BorderLayout.NORTH);
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        orderFrame.add(mainPanel);
        orderFrame.setVisible(true);
    }

    private void cancelSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要取消的订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        String status = (String) orderTableModel.getValueAt(selectedRow, 2);
        String amount = (String) orderTableModel.getValueAt(selectedRow, 1);

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
                refreshOrderTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "取消订单失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void paySelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this,
                    "请先选择要支付的订单", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String orderId = (String) orderTableModel.getValueAt(selectedRow, 0);
        String status = (String) orderTableModel.getValueAt(selectedRow, 2);
        String amount = (String) orderTableModel.getValueAt(selectedRow, 1);

        // 检查订单状态是否允许支付
        if (!"待支付".equals(status)) {
            JOptionPane.showMessageDialog(this,
                    "只有'待支付'状态的订单可以支付\n当前状态: " + status,
                    "无法支付",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 确认对话框
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要支付订单吗？\n订单号: " + orderId + "\n金额: " + amount,
                "确认支付",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = orderController.payOrder(orderId);
            if (success) {
                JOptionPane.showMessageDialog(this,
                        "订单支付成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshOrderTable();
            } else {
                JOptionPane.showMessageDialog(this,
                        "支付订单失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void refreshOrderTable() {
        loadOrderData();
    }

    private void loadOrderData() {
        orderTableModel.setRowCount(0); // 清空表格

        List<Order> orders = orderController.getUserOrders();
        if (orders != null) {
            for (Order order : orders) {
                orderTableModel.addRow(new Object[]{
                        order.getOrderId(),
                        "￥" + order.getTotalAmount().setScale(2, RoundingMode.HALF_UP),
                        order.getStatus(),
                        formatDate(order.getCreatedAt())
                });
            }
        }
    }

    protected JPanel getProductBrowserPanel() {
        if (productBrowserPanel == null) {
            productBrowserPanel = createProductBrowserPanel();
        }
        return productBrowserPanel;
    }
    protected JPanel getBottomPanel() {return createBottomPanel();}

        // ... [其余 showMyOrders()、cancelSelectedOrder()、paySelectedOrder() 方法可直接保持原逻辑，嵌入到 JPanel 环境中调用即可] ...
}
