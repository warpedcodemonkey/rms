package com.krazykritterranch.rms.model.user;

import com.krazykritterranch.rms.model.BaseVO;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;

import java.util.StringJoiner;

@Entity
@AttributeOverride(name = "id", column = @Column(name = "role_id"))
public class Role extends BaseVO {

    private String roleName;

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Role.class.getSimpleName() + "[", "]")
                .add("roleName='" + roleName + "'")
                .toString();
    }
}
