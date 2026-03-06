package server.dao;

import common.model.Product;
import common.model.ProductCategory;
import util.DBUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static util.DBUtil.getConnection;

public class ProductDAOImpl implements ProductDAO {

    @Override
    public List<Product> findAllProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, price, stock, status FROM product ORDER BY created_at DESC";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findAvailableProducts() {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, price, stock, status FROM product WHERE status = '上架' AND stock > 0";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                products.add(extractProduct(rs));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return products;
    }

    @Override
    public List<Product> findProductsByName(String name) {
        List<Product> products = new ArrayList<>();
        String sql = "SELECT product_id, name, price, stock, status FROM product WHERE name LIKE ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, "%" + name + "%");

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    products.add(extractProduct(rs));
                }
            }
            return products;

        } catch (SQLException e) {
            System.err.println("按名称查找商品错误: " + e.getMessage());
            return Collections.emptyList();
        }
    }

    @Override
    public Product findProductById(String productId) {
        String sql = "SELECT product_id, name, price, stock, status FROM product WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return extractProduct(rs);
                }
            }
            return null;

        } catch (SQLException e) {
            System.err.println("按ID查找商品错误: " + e.getMessage());
            return null;
        }
    }



    @Override
    public boolean addProduct(Product product) {
        String sql = "INSERT INTO product (product_id, name, price, stock, status) VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            setProductParams(ps, product);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean updateProduct(Product product) {
        String sql = "UPDATE product SET name=?, price=?, stock=?, status=? WHERE product_id=?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            // 记录更新前的值
            Product oldProduct = findProductById(product.getProductId());
            if (oldProduct != null) {
                System.out.println("[DAO] 更新前数据: " + oldProduct);
            }

            // 设置参数
            ps.setString(1, product.getName());
            ps.setBigDecimal(2, product.getPrice());
            ps.setInt(3, product.getStock());
            ps.setString(4, product.getStatus());
            ps.setString(5, product.getProductId());

            // 执行更新
            int rows = ps.executeUpdate();
            System.out.println("[DAO] 影响行数: " + rows);

            // 验证更新
            if (rows == 0) {
                System.out.println("[DAO] 可能原因: 商品ID不存在或数据未变化");
                System.out.println("[DAO] 尝试更新的ID: " + product.getProductId());

                // 检查ID是否存在
                boolean exists = findProductById(product.getProductId()) != null;
                System.out.println("[DAO] 商品存在状态: " + exists);
            }

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] SQL错误: " + e.getMessage());
            System.err.println("[DAO] 执行的SQL: " + sql);
            System.err.println("[DAO] 参数: " +
                    product.getName() + ", " +
                    product.getPrice() + ", " +
                    product.getStock() + ", " +
                    product.getStatus() + ", " +
                    product.getProductId());
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean updateProductStock(String productId, int quantity) {
        String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, quantity);
            ps.setString(2, productId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public boolean deleteProduct(String productId) {
        String sql = "DELETE FROM product WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            int rows = ps.executeUpdate();
            System.out.println("[DAO] 删除影响行数: " + rows);

            return rows > 0;

        } catch (SQLException e) {
            System.err.println("[DAO] 删除商品SQL错误: " + e.getMessage());
            System.err.println("[DAO] 执行的SQL: " + sql);
            e.printStackTrace();
            return false;
        }
    }

    private Product extractProduct(ResultSet rs) throws SQLException {
        Product product = new Product();
        product.setProductId(rs.getString("product_id"));
        product.setName(rs.getString("name"));
        product.setPrice(rs.getBigDecimal("price"));
        product.setStock(rs.getInt("stock"));
        product.setStatus(rs.getString("status"));
        return product;
    }

    private void setProductParams(PreparedStatement ps, Product product) throws SQLException {
        ps.setString(1, product.getProductId());
        ps.setString(2, product.getName());
        ps.setBigDecimal(3, product.getPrice());
        ps.setInt(4, product.getStock());
        ps.setString(5, product.getStatus());
    }


    public boolean isProductInOrders(String productId) {
        String sql = "SELECT COUNT(*) FROM order_item WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, productId);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
            return false;

        } catch (SQLException e) {
            System.err.println("[DAO] 检查商品订单关联错误: " + e.getMessage());
            e.printStackTrace();
            return true; // 出错时保守处理，认为有关联
        }
    }

    @Override
    public boolean updateStock(String productId, int delta) {
        String sql = "UPDATE product SET stock = stock + ? WHERE product_id = ?";

        try (Connection conn = getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, delta);
            ps.setString(2, productId);

            int rows = ps.executeUpdate();
            return rows > 0;

        } catch (SQLException e) {
            System.err.println("更新库存失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public List<ProductCategory> findAllCategories() {
        String sql = "SELECT category_id, category_name, description FROM product_categories";
        List<ProductCategory> categories = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                ProductCategory category = new ProductCategory();
                category.setCategoryId(rs.getString("category_id"));
                category.setCategoryName(rs.getString("category_name"));
                category.setDescription(rs.getString("description"));
                categories.add(category);
            }
        } catch (SQLException e) {
            System.err.println("查询所有分类失败: " + e.getMessage());
            e.printStackTrace();
        }

        return categories;
    }

    @Override
    public List<Product> findProductsByCategory(String categoryId) {
        String sql = "SELECT p.product_id, p.name, p.price, p.stock, p.status, p.category_id, " +
                "c.category_name FROM product p " +
                "LEFT JOIN product_categories c ON p.category_id = c.category_id " +
                "WHERE p.category_id = ?";
        List<Product> products = new ArrayList<>();

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Product product = new Product();
                    product.setProductId(rs.getString("product_id"));
                    product.setName(rs.getString("name"));
                    product.setPrice(rs.getBigDecimal("price"));
                    product.setStock(rs.getInt("stock"));
                    product.setStatus(rs.getString("status"));
                    product.setCategoryId(rs.getString("category_id"));

                    // 设置分类信息
                    if (rs.getString("category_name") != null) {
                        ProductCategory category = new ProductCategory();
                        category.setCategoryId(rs.getString("category_id"));
                        category.setCategoryName(rs.getString("category_name"));
                        product.setCategory(category);
                    }

                    products.add(product);
                }
            }
        } catch (SQLException e) {
            System.err.println("按分类查询商品失败: " + e.getMessage());
            e.printStackTrace();
        }

        return products;
    }



    @Override
    public boolean addCategory(ProductCategory category) {
        String sql = "INSERT INTO product_categories (category_id, category_name, description) " +
                "VALUES (?, ?, ?)";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, category.getCategoryId());
            ps.setString(2, category.getCategoryName());
            ps.setString(3, category.getDescription());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("添加分类失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean deleteCategory(String categoryId) {
        String sql = "DELETE FROM product_categories WHERE category_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("删除分类失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean categoryExists(String categoryId) {
        String sql = "SELECT COUNT(*) FROM product_categories WHERE category_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查分类存在失败: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean isCategoryInUse(String categoryId) {
        String sql = "SELECT COUNT(*) FROM product WHERE category_id = ?";

        try (Connection conn = DBUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, categoryId);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() && rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            System.err.println("检查分类使用情况失败: " + e.getMessage());
            return true; // 出错时保守处理，认为在使用中
        }
    }



}