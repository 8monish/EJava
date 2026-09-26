package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "purchase_orders")
public class PurchaseOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poNumber;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false)
    private LocalDate orderDate;

    @Column(nullable = false)
    private String status = "DRAFT"; // DRAFT, CONFIRMED, BILLED, CANCELLED

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    private Long vendorBillId;

    private String notes;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "purchase_order_id")
    private List<PurchaseOrderItem> items = new ArrayList<>();

    public PurchaseOrder() {
    }

    public PurchaseOrder(String poNumber, Long vendorId, String vendorName, LocalDate orderDate, String notes) {
        this.poNumber = poNumber;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.orderDate = orderDate;
        this.notes = notes;
        this.status = "DRAFT";
    }

    public void addItem(PurchaseOrderItem item) {
        this.items.add(item);
        recalculateTotal();
    }

    public void recalculateTotal() {
        double sum = 0.0;
        for (PurchaseOrderItem item : items) {
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

    public String getPoNumber() {
        return poNumber;
    }

    public void setPoNumber(String poNumber) {
        this.poNumber = poNumber;
    }

    public Long getVendorId() {
        return vendorId;
    }

    public void setVendorId(Long vendorId) {
        this.vendorId = vendorId;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
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

    public Long getVendorBillId() {
        return vendorBillId;
    }

    public void setVendorBillId(Long vendorBillId) {
        this.vendorBillId = vendorBillId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    public List<PurchaseOrderItem> getItems() {
        return items;
    }

    public void setItems(List<PurchaseOrderItem> items) {
        this.items = items;
        recalculateTotal();
    }
}
