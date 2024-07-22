package com.krazykritterranch.rms.model.product;

import com.krazykritterranch.rms.model.BaseVO;

import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import java.math.BigDecimal;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name="product_id"))
public class Product extends BaseVO {
    private String productName;
    private String productDescription;
    @Column(precision = 10, scale = 2)
    private BigDecimal price;
    private String productImage;

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getProductDescription() {
        return productDescription;
    }

    public void setProductDescription(String productDescription) {
        this.productDescription = productDescription;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public String getProductImage() {
        return productImage;
    }

    public void setProductImage(String productImage) {
        this.productImage = productImage;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Product.class.getSimpleName() + "[", "]")
                .add("productName='" + productName + "'")
                .add("productDescription='" + productDescription + "'")
                .add("price=" + price)
                .add("productImage='" + productImage + "'")
                .toString();
    }
}
