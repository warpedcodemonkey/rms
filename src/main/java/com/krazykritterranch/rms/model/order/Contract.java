package com.krazykritterranch.rms.model.order;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.product.Product;
import com.krazykritterranch.rms.model.user.User;

import jakarta.persistence.*;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@AttributeOverride(name="id", column = @Column(name = "order_id"))
public class Contract extends BaseVO {

    private Boolean contractAccepted;

    @ManyToOne
    @JoinColumn(name = "customer_id")
    private User customer;

    @ManyToMany
    @JoinTable(
            name = "contract_products", // Name of the junction table
            joinColumns = @JoinColumn(name = "contract_id"), // The foreign key column for Customer
            inverseJoinColumns = @JoinColumn(name = "product_id") // The foreign key column for Email
    )
    private List<Product> products = new ArrayList<>();

    private Date contractDate;
    private Date expireDate;
    private Date deliverDate;

    @ManyToOne
    @JoinColumn(name = "contract_author_id")
    private User contractAuthor;

    private Boolean active;

    public Boolean getContractAccepted() {
        return contractAccepted;
    }

    public void setContractAccepted(Boolean contractAccepted) {
        this.contractAccepted = contractAccepted;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User user) {
        this.customer = user;
    }

    public List<Product> getProducts() {
        return products;
    }

    public void setProducts(List<Product> products) {
        this.products = products;
    }

    public Date getContractDate() {
        return contractDate;
    }

    public void setContractDate(Date contractDate) {
        this.contractDate = contractDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public Date getDeliverDate() {
        return deliverDate;
    }

    public void setDeliverDate(Date deliverDate) {
        this.deliverDate = deliverDate;
    }

    public User getContractAuthor() {
        return contractAuthor;
    }

    public void setContractAuthor(User contractAuthor) {
        this.contractAuthor = contractAuthor;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Contract.class.getSimpleName() + "[", "]")
                .add("contractAccepted=" + contractAccepted)
                .add("customer=" + customer)
                .add("products=" + products)
                .add("contractDate=" + contractDate)
                .add("expireDate=" + expireDate)
                .add("deliverDate=" + deliverDate)
                .add("contractAuthor=" + contractAuthor)
                .add("active=" + active)
                .toString();
    }
}
