package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "sales_orders")
public class SalesOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String soNumber;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, CONFIRMED, INVOICED, CANCELLED

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    private Long customerInvoiceId;

    private String notes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "sales_order_id")
    private List<SalesOrderItem> items = new ArrayList<>();

    public SalesOrder() {
    }

    public SalesOrder(String soNumber, Long customerId, String customerName, LocalDate orderDate, String notes) {
        this.soNumber = soNumber;
        this.customerId = customerId;
        this.customerName = customerName;
        this.orderDate = orderDate;
        this.notes = notes;
        this.status = "DRAFT";
    }

    public void addItem(SalesOrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        double sum = 0.0;
        for (SalesOrderItem item : items) {
            sum += (item.getSubtotal() != null ? item.getSubtotal() : 0.0);
        }
        this.totalAmount = Math.round(sum * 100.0) / 100.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getSoNumber() {
        return soNumber;
    }

    public void setSoNumber(String soNumber) {
        this.soNumber = soNumber;
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public LocalDate getOrderDate() {
        return orderDate;
    }

    public void setOrderDate(LocalDate orderDate) {
        this.orderDate = orderDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getCustomerInvoiceId() {
        return customerInvoiceId;
    }

    public void setCustomerInvoiceId(Long customerInvoiceId) {
        this.customerInvoiceId = customerInvoiceId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<SalesOrderItem> getItems() {
        return items;
    }

    public void setItems(List<SalesOrderItem> items) {
        this.items = items;
        recalculateTotal();
    }
}
