package common.model;

import java.io.Serializable;
import java.math.BigDecimal;

public class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private String productId;
    private String name;
    private BigDecimal price;
    private Integer stock;
    private String status;
    private String categoryId;
    private ProductCategory category;

    public Product() {}

    public Product(String productId, String name, BigDecimal price, Integer stock, String status) {
        this.productId = productId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.status = status;
    }

    // Getter和Setter
    public String getProductId() { return productId; }
    public void setProductId(String productId) { this.productId = productId; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }
    public Integer getStock() { return stock; }
    public void setStock(Integer stock) { this.stock = stock; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return name + " - ¥" + price + " (库存: " + stock + ")";
    }

    public String getCategoryId() {
        return categoryId;
    }

    /**
     * 设置分类ID
     */
    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    /**
     * 获取分类对象
     */
    public ProductCategory getCategory() {
        return category;
    }

    /**
     * 设置分类对象
     */
    public void setCategory(ProductCategory category) {
        this.category = category;
        if (category != null) {
            this.categoryId = category.getCategoryId();
        }
    }

}