package client.ui;

import client.controller.OrderController;
import client.controller.ProductController;
import common.model.Product;
import common.model.ProductCategory;
import common.model.User;
import util.EmptyState;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductManagementPanel extends JPanel {
    private final User currentUser;
    private final ProductController productController;
    private final OrderController orderController;
    private JTable productTable;
    private JScrollPane tableScrollPane;
    private DefaultTableModel tableModel;
    private Map<String, String> categoryNameToIdMap = new HashMap<>();

    public ProductManagementPanel(User user, OrderController orderController) {
        this.currentUser = user;
        this.orderController = orderController;
        this.productController = new ProductController();
        this.productController.setCurrentUser(user);

        initializeUI();
        loadInitialData();
    }

    private void initializeUI() {
        setLayout(new BorderLayout());

        // 1. 顶部面板 - 搜索功能
        JPanel searchPanel = createSearchPanel();
        add(searchPanel, BorderLayout.NORTH);

        // 2. 中间面板 - 表格
        JScrollPane tableScrollPane = createProductTable();
        add(tableScrollPane, BorderLayout.CENTER);

        // 3. 底部面板 - 操作按钮
        JPanel actionPanel = createActionPanel();
        add(actionPanel, BorderLayout.SOUTH);
    }

    private JPanel createSearchPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBorder(BorderFactory.createTitledBorder("商品搜索"));

        JTextField txtSearchId = new JTextField(12);
        JButton btnSearchId = new JButton("按ID查找");
        btnSearchId.addActionListener(e -> searchById(txtSearchId.getText()));

        JTextField txtSearchName = new JTextField(15);
        JButton btnSearchName = new JButton("按名称查找");
        btnSearchName.addActionListener(e -> searchByName(txtSearchName.getText()));

        JButton btnReset = new JButton("显示全部");
        btnReset.addActionListener(e -> refreshTable());

        panel.add(new JLabel("商品ID:"));
        panel.add(txtSearchId);
        panel.add(btnSearchId);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(new JLabel("商品名称:"));
        panel.add(txtSearchName);
        panel.add(btnSearchName);
        panel.add(Box.createHorizontalStrut(20));
        panel.add(btnReset);

        return panel;
    }

    private JScrollPane createProductTable() {
        String[] columns = {"商品ID", "商品名称", "价格", "库存", "状态", "分类"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        productTable = new JTable(tableModel);
        tableScrollPane = new JScrollPane(productTable);
        return tableScrollPane;
    }

    private JPanel createActionPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBorder(BorderFactory.createEtchedBorder());

        JButton btnAdd = new JButton("添加商品");
        btnAdd.addActionListener(e -> showAddProductDialog());

        JButton btnEdit = new JButton("编辑商品");
        btnEdit.addActionListener(e -> showEditProductDialog());

        JButton btnDelete = new JButton("删除商品");
        btnDelete.addActionListener(e -> deleteProduct());

        JButton btnRefresh = new JButton("刷新");
        btnRefresh.addActionListener(e -> refreshTable());

        panel.add(btnAdd);
        panel.add(btnEdit);
        panel.add(btnDelete);
        panel.add(btnRefresh);

        return panel;
    }

    private void loadInitialData() {
        refreshTable();
        loadCategories();
    }

    private void loadCategories() {
        categoryNameToIdMap.clear();
        List<ProductCategory> categories = productController.getAllCategories();
        if (categories != null) {
            for (ProductCategory category : categories) {
                if (category != null && category.getCategoryName() != null) {
                    categoryNameToIdMap.put(category.getCategoryName(), category.getCategoryId());
                }
            }
        }
    }

    private void refreshTable() {
        tableModel.setRowCount(0);
        List<Product> products = productController.getAllProducts();
        if (products != null) {
            for (Product product : products) {
                String categoryName = getCategoryNameById(product.getCategoryId());
                tableModel.addRow(new Object[]{
                        product.getProductId(),
                        product.getName(),
                        product.getPrice(),
                        product.getStock(),
                        product.getStatus(),
                        categoryName
                });
            }
        }
        EmptyState.updateEmptyState(tableScrollPane, productTable, "暂无商品");
    }

    private String getCategoryNameById(String categoryId) {
        if (categoryId == null) return "未分类";
        for (Map.Entry<String, String> entry : categoryNameToIdMap.entrySet()) {
            if (entry.getValue().equals(categoryId)) {
                return entry.getKey();
            }
        }
        return "未分类";
    }

    private void showAddProductDialog() {
        ProductDialog dialog = new ProductDialog(null, "添加商品");
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Product newProduct = dialog.getProduct();
            boolean success = productController.addProduct(newProduct);
            if (success) {
                JOptionPane.showMessageDialog(this, "商品添加成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "商品添加失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void showEditProductDialog() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要编辑的商品", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        Product product = productController.getProductById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "商品不存在或已被删除", "错误", JOptionPane.ERROR_MESSAGE);
            refreshTable();
            return;
        }

        ProductDialog dialog = new ProductDialog(product, "编辑商品");
        dialog.setVisible(true);
        if (dialog.isConfirmed()) {
            Product updatedProduct = dialog.getProduct();
            boolean success = productController.updateProduct(updatedProduct);
            if (success) {
                JOptionPane.showMessageDialog(this, "商品更新成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "商品更新失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void deleteProduct() {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "请选择要删除的商品", "提示", JOptionPane.WARNING_MESSAGE);
            return;
        }

        String productId = (String) tableModel.getValueAt(selectedRow, 0);
        int confirm = JOptionPane.showConfirmDialog(this,
                "确定要删除这个商品吗？", "确认删除", JOptionPane.YES_NO_OPTION);

        if (confirm == JOptionPane.YES_OPTION) {
            boolean success = productController.deleteProduct(productId);
            if (success) {
                JOptionPane.showMessageDialog(this, "商品删除成功", "成功", JOptionPane.INFORMATION_MESSAGE);
                refreshTable();
            } else {
                JOptionPane.showMessageDialog(this, "商品删除失败", "错误", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void searchById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入商品ID");
            return;
        }

        Product product = productController.getProductById(productId);
        if (product == null) {
            JOptionPane.showMessageDialog(this, "未找到指定商品");
            return;
        }

        tableModel.setRowCount(0);
        String categoryName = getCategoryNameById(product.getCategoryId());
        tableModel.addRow(new Object[]{
                product.getProductId(),
                product.getName(),
                product.getPrice(),
                product.getStock(),
                product.getStatus(),
                categoryName
        });
    }

    private void searchByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this, "请输入商品名称");
            return;
        }

        List<Product> products = productController.searchProductsByName(name);
        if (products.isEmpty()) {
            JOptionPane.showMessageDialog(this, "未找到匹配商品");
            return;
        }

        tableModel.setRowCount(0);
        for (Product product : products) {
            String categoryName = getCategoryNameById(product.getCategoryId());
            tableModel.addRow(new Object[]{
                    product.getProductId(),
                    product.getName(),
                    product.getPrice(),
                    product.getStock(),
                    product.getStatus(),
                    categoryName
            });
        }
    }

    private class ProductDialog extends JDialog {
        private Product product;
        private boolean confirmed = false;
        private JTextField txtId;
        private JTextField txtName;
        private JComboBox<String> cmbCategory;
        private JTextField txtPrice;
        private JTextField txtStock;
        private JComboBox<String> cmbStatus;

        public ProductDialog(Product product, String title) {
            this.product = product != null ? product : new Product();
            setTitle(title);
            setModal(true);
            setSize(400, 300);
            setLocationRelativeTo(ProductManagementPanel.this);
            setLayout(new BorderLayout());

            initForm();
            initButtons();
        }

        private void initForm() {
            JPanel formPanel = new JPanel(new GridLayout(6, 2, 5, 5));

            // 商品ID
            formPanel.add(new JLabel("商品ID:"));
            txtId = new JTextField(product.getProductId());
            if (product.getProductId() != null) {
                txtId.setEditable(false);
            }
            formPanel.add(txtId);

            // 商品名称
            formPanel.add(new JLabel("商品名称:"));
            txtName = new JTextField(product.getName());
            formPanel.add(txtName);

            // 分类
            formPanel.add(new JLabel("分类:"));
            cmbCategory = new JComboBox<>();
            cmbCategory.addItem("未分类");
            for (String categoryName : categoryNameToIdMap.keySet()) {
                cmbCategory.addItem(categoryName);
            }
            if (product.getCategoryId() != null) {
                String currentCategoryName = getCategoryNameById(product.getCategoryId());
                cmbCategory.setSelectedItem(currentCategoryName);
            }
            formPanel.add(cmbCategory);

            // 价格
            formPanel.add(new JLabel("价格:"));
            txtPrice = new JTextField(product.getPrice() != null ? product.getPrice().toString() : "");
            formPanel.add(txtPrice);

            // 库存
            formPanel.add(new JLabel("库存:"));
            txtStock = new JTextField(product.getStock() != null ? product.getStock().toString() : "");
            formPanel.add(txtStock);

            // 状态
            formPanel.add(new JLabel("状态:"));
            cmbStatus = new JComboBox<>(new String[]{"上架", "下架", "缺货"});
            if (product.getStatus() != null) {
                cmbStatus.setSelectedItem(product.getStatus());
            }
            formPanel.add(cmbStatus);

            add(formPanel, BorderLayout.CENTER);
        }

        private void initButtons() {
            JPanel buttonPanel = new JPanel();
            JButton btnSubmit = new JButton("确定");
            JButton btnCancel = new JButton("取消");

            btnSubmit.addActionListener(e -> {
                if (validateInput()) {
                    confirmed = true;
                    updateProduct();
                    dispose();
                }
            });

            btnCancel.addActionListener(e -> dispose());

            buttonPanel.add(btnSubmit);
            buttonPanel.add(btnCancel);
            add(buttonPanel, BorderLayout.SOUTH);
        }

        private boolean validateInput() {
            try {
                if (txtId.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "商品ID不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                if (txtName.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(this, "商品名称不能为空", "错误", JOptionPane.ERROR_MESSAGE);
                    return false;
                }
                new BigDecimal(txtPrice.getText().trim());
                Integer.parseInt(txtStock.getText().trim());
                return true;
            } catch (NumberFormatException e) {
                JOptionPane.showMessageDialog(this, "请输入有效的价格和库存", "错误", JOptionPane.ERROR_MESSAGE);
                return false;
            }
        }

        private void updateProduct() {
            product.setProductId(txtId.getText().trim());
            product.setName(txtName.getText().trim());
            product.setPrice(new BigDecimal(txtPrice.getText().trim()));
            product.setStock(Integer.parseInt(txtStock.getText().trim()));
            product.setStatus((String) cmbStatus.getSelectedItem());

            // 设置分类ID
            String selectedCategory = (String) cmbCategory.getSelectedItem();
            if ("未分类".equals(selectedCategory)) {
                product.setCategoryId(null);
            } else {
                product.setCategoryId(categoryNameToIdMap.get(selectedCategory));
            }
        }

        public boolean isConfirmed() {
            return confirmed;
        }

        public Product getProduct() {
            return product;
        }
    }
}