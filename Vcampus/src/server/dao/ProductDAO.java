package server.dao;

import common.model.Product;
import common.model.ProductCategory;

import java.util.List;

public interface ProductDAO {
    List<Product> findAllProducts();
    List<Product> findAvailableProducts();
    List<Product> findProductsByName(String name);
    Product findProductById(String productId);
    boolean addProduct(Product product);
    boolean updateProduct(Product product);
    boolean updateProductStock(String productId, int quantity);
    boolean deleteProduct(String productId);
    boolean isProductInOrders(String productId);
    boolean updateStock(String productId, int delta);
    List<ProductCategory> findAllCategories();
    List<Product> findProductsByCategory(String categoryId);
    boolean addCategory(ProductCategory category);
    boolean deleteCategory(String categoryId);
    boolean categoryExists(String categoryId);
    boolean isCategoryInUse(String categoryId);

}