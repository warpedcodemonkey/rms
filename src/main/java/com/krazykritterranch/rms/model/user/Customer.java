package com.krazykritterranch.rms.model.user;


import com.krazykritterranch.rms.model.order.Order;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;


import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

public class Customer extends User {

    @ManyToMany
    @JoinTable(
            name="customer_orders",
            joinColumns = @JoinColumn(name = "customer_id"),
            inverseJoinColumns = @JoinColumn(name = "order_id")
    )
    private List<Order> orders = new ArrayList<>();


    public List<Order> getOrders() {
        return orders;
    }

    public void setOrders(List<Order> orders) {
        this.orders = orders;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Customer.class.getSimpleName() + "[", "]")
                .add("orders=" + orders)
                .toString();
    }
}
