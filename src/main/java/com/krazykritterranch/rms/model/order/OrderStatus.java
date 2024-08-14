package com.krazykritterranch.rms.model.order;

import com.krazykritterranch.rms.model.BaseVO;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "order_status_id"))
public class OrderStatus extends BaseVO {

    private String orderStatus;

    public String getOrderStatus() {
        return orderStatus;
    }

    public void setOrderStatus(String orderStatus) {
        this.orderStatus = orderStatus;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", OrderStatus.class.getSimpleName() + "[", "]")
                .add("orderStatus='" + orderStatus + "'")
                .toString();
    }
}
