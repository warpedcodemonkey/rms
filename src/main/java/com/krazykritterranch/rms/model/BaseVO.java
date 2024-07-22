package com.krazykritterranch.rms.model;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import java.util.StringJoiner;

@MappedSuperclass
public class BaseVO {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", BaseVO.class.getSimpleName() + "[", "]")
                .add("id=" + id)
                .toString();
    }
}
