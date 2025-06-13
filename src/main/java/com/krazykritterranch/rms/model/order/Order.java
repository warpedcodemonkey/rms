package com.krazykritterranch.rms.model.order;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.product.Product;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;


@Entity
@Table(name = "orders")
@AttributeOverride(name = "id", column = @Column(name="order_id"))
public class Order extends BaseVO {

    private Date orderDate;
    private Date shipDate;
    private Date completeDate;
    private String shippingCarrier;
    private String trackingInformation;
    @Column(precision = 10, scale = 2)
    private BigDecimal orderTotal;

    @ManyToOne
    @JoinColumn(name = "order_status_id")
    private OrderStatus orderStatus;

    @ManyToMany
    @JoinTable(
            name = "order_products",
            joinColumns = @JoinColumn(name = "order_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList();

    public Date getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(Date orderDate) {
        this.orderDate = orderDate;
    }

    public Date getShipDate() {
        return shipDate;
    }

    public void setShipDate(Date shipDate) {
        this.shipDate = shipDate;
    }

    public Date getCompleteDate() {
        return completeDate;
    }

    public void setCompleteDate(Date completeDate) {
        this.completeDate = completeDate;
    }

    public String getShippingCarrier() {
        return shippingCarrier;
    }

    public void setShippingCarrier(String shippingCarrier) {
        this.shippingCarrier = shippingCarrier;
    }

    public String getTrackingInformation() {
        return trackingInformation;
    }

    public void setTrackingInformation(String trackingInformation) {
        this.trackingInformation = trackingInformation;
    }

    public BigDecimal getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(BigDecimal orderTotal) {
        this.orderTotal = orderTotal;
    }

    public OrderStatus getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(OrderStatus orderStatus) {
        this.orderStatus = orderStatus;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Order.class.getSimpleName() + "[", "]")
                .add("orderDate=" + orderDate)
                .add("shipDate=" + shipDate)
                .add("completeDate=" + completeDate)
                .add("shippingCarrier='" + shippingCarrier + "'")
                .add("trackingInformation='" + trackingInformation + "'")
                .add("orderTotal=" + orderTotal)
                .add("orderStatus=" + orderStatus)
                .add("products=" + products)
                .toString();
    }
}
