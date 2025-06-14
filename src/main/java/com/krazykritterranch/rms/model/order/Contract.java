package com.krazykritterranch.rms.model.order;

import com.krazykritterranch.rms.model.BaseVO;
import com.krazykritterranch.rms.model.common.Account;
import com.krazykritterranch.rms.model.product.Product;
import com.krazykritterranch.rms.model.user.User;
import com.krazykritterranch.rms.model.vendor.Vendor;

import jakarta.persistence.*;
import java.sql.Date;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.StringJoiner;

@Entity
@Table(name = "contracts")
@AttributeOverride(name = "id", column = @Column(name = "contract_id"))
public class Contract extends BaseVO {

    // CRITICAL: Add account relationship for multi-tenancy
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    @Column(name = "contract_number", unique = true)
    private String contractNumber;

    @Column(name = "contract_accepted")
    private Boolean contractAccepted = false;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private User customer;

    @ManyToMany
    @JoinTable(
            name = "contract_products",
            joinColumns = @JoinColumn(name = "contract_id"),
            inverseJoinColumns = @JoinColumn(name = "product_id")
    )
    private List<Product> products = new ArrayList<>();

    @Column(name = "contract_date")
    private Date contractDate;

    @Column(name = "expire_date")
    private Date expireDate;

    @Column(name = "delivery_date")
    private Date deliveryDate;

    // Add delivery location
    @Column(name = "delivery_address")
    private String deliveryAddress;

    // Add butcher facility reference (as mentioned in specifications)
    @ManyToOne
    @JoinColumn(name = "butcher_facility_id")
    private Vendor butcherFacility;

    // Add scheduled butcher date
    @Column(name = "butcher_date")
    private Date butcherDate;

    @ManyToOne
    @JoinColumn(name = "contract_author_id", nullable = false)
    private User contractAuthor;

    @Column(name = "is_active")
    private Boolean active = true;

    // Add contract value
    @Column(name = "total_value", precision = 10, scale = 2)
    private BigDecimal totalValue;

    // Add payment terms
    @Column(name = "payment_terms")
    private String paymentTerms;

    // Add notes field
    @Column(name = "notes", length = 1000)
    private String notes;

    // Add contract status
    @Column(name = "status")
    private String status = "DRAFT"; // DRAFT, SENT, ACCEPTED, COMPLETED, CANCELLED

    // Constructors
    public Contract() {
        this.contractNumber = generateContractNumber();
    }

    public Contract(Account account, User customer, User author) {
        this.account = account;
        this.customer = customer;
        this.contractAuthor = author;
        this.contractNumber = generateContractNumber();
        this.contractDate = new Date(System.currentTimeMillis());
        this.active = true;
        this.status = "DRAFT";
    }

    private String generateContractNumber() {
        return "CONTRACT-" + System.currentTimeMillis();
    }

    // Getters and Setters
    public Account getAccount() {
        return account;
    }

    public void setAccount(Account account) {
        this.account = account;
    }

    public String getContractNumber() {
        return contractNumber;
    }

    public void setContractNumber(String contractNumber) {
        this.contractNumber = contractNumber;
    }

    public Boolean getContractAccepted() {
        return contractAccepted;
    }

    public void setContractAccepted(Boolean contractAccepted) {
        this.contractAccepted = contractAccepted;
    }

    public User getCustomer() {
        return customer;
    }

    public void setCustomer(User customer) {
        this.customer = customer;
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

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date deliveryDate) {
        this.deliveryDate = deliveryDate;
    }

    public String getDeliveryAddress() {
        return deliveryAddress;
    }

    public void setDeliveryAddress(String deliveryAddress) {
        this.deliveryAddress = deliveryAddress;
    }

    public Vendor getButcherFacility() {
        return butcherFacility;
    }

    public void setButcherFacility(Vendor butcherFacility) {
        this.butcherFacility = butcherFacility;
    }

    public Date getButcherDate() {
        return butcherDate;
    }

    public void setButcherDate(Date butcherDate) {
        this.butcherDate = butcherDate;
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

    public BigDecimal getTotalValue() {
        return totalValue;
    }

    public void setTotalValue(BigDecimal totalValue) {
        this.totalValue = totalValue;
    }

    public String getPaymentTerms() {
        return paymentTerms;
    }

    public void setPaymentTerms(String paymentTerms) {
        this.paymentTerms = paymentTerms;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return new StringJoiner(", ", Contract.class.getSimpleName() + "[", "]")
                .add("id=" + getId())
                .add("account=" + (account != null ? account.getAccountNumber() : "null"))
                .add("contractNumber='" + contractNumber + "'")
                .add("status='" + status + "'")
                .add("contractAccepted=" + contractAccepted)
                .add("customer=" + (customer != null ? customer.getUsername() : "null"))
                .add("products=" + products.size())
                .add("contractDate=" + contractDate)
                .add("expireDate=" + expireDate)
                .add("deliveryDate=" + deliveryDate)
                .add("butcherFacility=" + (butcherFacility != null ? butcherFacility.getVendorName() : "null"))
                .add("butcherDate=" + butcherDate)
                .add("contractAuthor=" + (contractAuthor != null ? contractAuthor.getUsername() : "null"))
                .add("totalValue=" + totalValue)
                .add("active=" + active)
                .toString();
    }
}