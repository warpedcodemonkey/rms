package com.krazykritterranch.rms.model.vendor;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;

import jakarta.persistence.*;
import java.util.StringJoiner;

@Entity
@Table(name = "vendor_types",
        uniqueConstraints = @UniqueConstraint(columnNames = {"type_name", "account_id"}))
@AttributeOverride(name = "id", column = @Column(name = "vendor_type_id"))
public class VendorType extends BaseVO {

    @Column(name = "type_name", nullable = false)
    private String typeName;

    @Column(name = "description")
    private String description;

    // Multi-tenant: Each account can have their own vendor types
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Track if this is a system default (for new accounts)
    @Column(name = "is_system_default")
    private Boolean isSystemDefault = false;

    // Display order for UI
    @Column(name = "display_order")
    private Integer displayOrder = 0;

    // Constructors
    public VendorType() {}

    public VendorType(String typeName, String description, Account account) {
        this.typeName = typeName;
        this.description = description;
        this.account = account;
        this.isActive = true;
        this.isSystemDefault = false;
    }

    // Getters and Setters
    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public Boolean getIsActive() {
        return isActive;
    }

    public void setIsActive(Boolean isActive) {
        this.isActive = isActive;
    }

    public Boolean getIsSystemDefault() {
        return isSystemDefault;
    }

    public void setIsSystemDefault(Boolean isSystemDefault) {
        this.isSystemDefault = isSystemDefault;
    }

    public Integer getDisplayOrder() {
        return displayOrder;
    }

    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", VendorType.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("typeName='" + typeName + "'")
                .add("description='" + description + "'")
                .add("account=" + (account != null ? account.getAccountNumber() : "null"))
                .add("isActive=" + isActive)
                .add("isSystemDefault=" + isSystemDefault)
                .add("displayOrder=" + displayOrder)
                .toString();
    }
}