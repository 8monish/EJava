package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "sales_order_items")
public class SalesOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long productId;
    private String productCode;

    @Column(nullable = false)
    private String productName;

    @Column(nullable = false)
    private Double quantity = 1.0;

    @Column(nullable = false)
    private Double unitPrice = 0.0;

    @Column(nullable = false)
    private Double subtotal = 0.0;

    public SalesOrderItem() {
    }

    public SalesOrderItem(Long productId, String productCode, String productName, Double quantity, Double unitPrice) {
        this.productId = productId;
        this.productCode = productCode;
        this.productName = productName;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.subtotal = Math.round(quantity * unitPrice * 100.0) / 100.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public String getProductCode() {
        return productCode;
    }

    public void setProductCode(String productCode) {
        this.productCode = productCode;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
        if (this.unitPrice != null) {
            this.subtotal = Math.round(quantity * this.unitPrice * 100.0) / 100.0;
        }
    }

    public Double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(Double unitPrice) {
        this.unitPrice = unitPrice;
        if (this.quantity != null) {
            this.subtotal = Math.round(this.quantity * unitPrice * 100.0) / 100.0;
        }
    }

    public Double getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(Double subtotal) {
        this.subtotal = subtotal;
    }
}
