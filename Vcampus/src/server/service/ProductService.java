package server.service;

import common.model.Product;
import common.model.ProductCategory;
import server.dao.OrderDAO;
import server.dao.ProductDAO;
import server.dao.ProductDAOImpl;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;

public class ProductService {
    private ProductDAO productDAO = new ProductDAOImpl();
    private final OrderDAO orderDAO;

    public ProductService(ProductDAO productDAO, OrderDAO orderDAO) {
        this.productDAO = productDAO;
        this.orderDAO = orderDAO;
    }

    public List<Product> getAllProducts() {
        return productDAO.findAllProducts();
    }

    public List<Product> getAvailableProducts() {
        return productDAO.findAvailableProducts();
    }

    public Product getProductById(String productId) {
        if (productId == null || productId.trim().isEmpty()) {
            return null;
        }
        return productDAO.findProductById(productId.trim());
    }

    public List<Product> searchProductsByName(String name) {
        if (name == null || name.trim().isEmpty()) {
            return Collections.emptyList();
        }
        return productDAO.findProductsByName(name.trim());
    }

    public boolean addProduct(Product product, String operatorRole) {
        if (!validateProduct(product)) {
            return false;
        }
        if (!"admin".equals(operatorRole)) {
            return false;
        }
        return productDAO.addProduct(product);
    }

    public boolean updateProduct(Product product, String operatorRole) {
        if (!validateProduct(product)) {
            return false;
        }
        // 增强日志记录
        System.out.println("[SERVER] 开始更新商品 - ID: " + product.getProductId());
        System.out.println("[SERVER] 操作者角色: " + operatorRole);

        // 1. 权限验证
        if (!"admin".equalsIgnoreCase(operatorRole)) {
            System.out.println("[SERVER] 权限验证失败: 需要admin权限");
            return false;
        }

        // 2. 数据验证
        if (product == null || product.getProductId() == null) {
            System.out.println("[SERVER] 商品数据不完整");
            return false;
        }

        // 3. 检查商品是否存在
        Product existing = productDAO.findProductById(product.getProductId());
        if (existing == null) {
            System.out.println("[SERVER] 商品不存在, ID: " + product.getProductId());
            return false;
        }

        // 4. 执行更新
        try {
            boolean success = productDAO.updateProduct(product);
            System.out.println("[SERVER] 更新结果: " + (success ? "成功" : "失败"));

            if (!success) {
                System.out.println("[SERVER] 可能原因: 数据未变化或ID不存在");
            }
            return success;

        } catch (Exception e) {
            System.err.println("[SERVER] 更新异常: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean validateProduct(Product product) {
        if (product == null) return false;
        if (product.getProductId() == null || product.getProductId().trim().isEmpty()) return false;
        if (product.getName() == null || product.getName().trim().isEmpty()) return false;
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) return false;
        if (product.getStock() == null || product.getStock() < 0) return false;
        if (product.getStatus() == null ||
                (!"上架".equals(product.getStatus()) && !"下架".equals(product.getStatus()))) {
            return false;
        }
        return true;
    }

    public boolean restockProduct(String productId, int quantity, String operatorRole) {
        if (!"admin".equals(operatorRole)) {
            return false;
        }
        return productDAO.updateProductStock(productId, quantity);
    }

    public boolean deleteProduct(String productId, String operatorRole) {
        System.out.println("[SERVER] 删除商品请求 - ID: " + productId + ", 操作者: " + operatorRole);

        try {
            // 1. 检查商品是否存在
            Product product = productDAO.findProductById(productId);
            if (product == null) {
                System.out.println("[SERVER] 商品不存在");
                return false;
            }


            // 3. 执行删除
            boolean success = productDAO.deleteProduct(productId);
            System.out.println("[SERVER] 删除结果: " + success);
            return success;

        } catch (Exception e) {
            System.err.println("[SERVER] 删除异常: " + e.getMessage());
            return false;
        }
    }

    public boolean exists(String productId) {
        try {
            Product product = productDAO.findProductById(productId);
            return product != null;
        } catch (Exception e) {
            System.err.println("检查商品存在性失败: " + e.getMessage());
            return false;
        }
    }

    public boolean checkStock(String productId, int quantity) {
        Product product = productDAO.findProductById(productId);
        return product != null && product.getStock() >= quantity;
    }

    public List<ProductCategory> getAllCategories() {
        return productDAO.findAllCategories();
    }

    public ProductCategory getCategoryById(String categoryId) {
        return productDAO.getCategoryById(categoryId);
    }

    public List<Product> getProductsByCategory(String categoryId) {
        return productDAO.findProductsByCategory(categoryId);
    }



    public boolean addProductCategory(ProductCategory category) {
        // 验证分类数据
        if (category == null || category.getCategoryId() == null ||
                category.getCategoryName() == null) {
            return false;
        }

        // 检查是否已存在
        if (productDAO.categoryExists(category.getCategoryId())) {
            return false;
        }

        return productDAO.addCategory(category);
    }

    public boolean deleteProductCategory(String categoryId) {
        // 检查分类是否在使用中
        if (productDAO.isCategoryInUse(categoryId)) {
            return false;
        }

        return productDAO.deleteCategory(categoryId);
    }




}