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
    /**
     * 原子扣减库存：仅当当前库存 >= quantity 时才扣减。
     * @return 成功扣减返回 true；库存不足或商品不存在返回 false（无副作用）
     */
    boolean decrementStock(String productId, int quantity);
    List<ProductCategory> findAllCategories();
    ProductCategory getCategoryById(String categoryId);
    List<Product> findProductsByCategory(String categoryId);
    boolean addCategory(ProductCategory category);
    boolean deleteCategory(String categoryId);
    boolean categoryExists(String categoryId);
    boolean isCategoryInUse(String categoryId);

}