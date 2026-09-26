package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "vendor_bills")
public class VendorBill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String billNumber;

    private Long purchaseOrderId;

    @Column(nullable = false)
    private Long vendorId;

    @Column(nullable = false)
    private String vendorName;

    @Column(nullable = false)
    private LocalDate billDate;

    private LocalDate dueDate;

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    @Column(nullable = false)
    private Double amountPaid = 0.0;

    @Column(nullable = false)
    private String status = "POSTED"; // DRAFT, POSTED, PAID

    private Long expenseAccountId;
    private String expenseAccountName;

    private Long payableAccountId;
    private String payableAccountName;

    private Long journalEntryId;

    private String description;

    public VendorBill() {
    }

    public VendorBill(String billNumber, Long purchaseOrderId, Long vendorId, String vendorName,
                      LocalDate billDate, LocalDate dueDate, Double totalAmount,
                      Long expenseAccountId, String expenseAccountName,
                      Long payableAccountId, String payableAccountName, String description) {
        this.billNumber = billNumber;
        this.purchaseOrderId = purchaseOrderId;
        this.vendorId = vendorId;
        this.vendorName = vendorName;
        this.billDate = billDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.amountPaid = 0.0;
        this.status = "POSTED";
        this.expenseAccountId = expenseAccountId;
        this.expenseAccountName = expenseAccountName;
        this.payableAccountId = payableAccountId;
        this.payableAccountName = payableAccountName;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getBillNumber() {
        return billNumber;
    }

    public void setBillNumber(String billNumber) {
        this.billNumber = billNumber;
    }

    public Long getPurchaseOrderId() {
        return purchaseOrderId;
    }

    public void setPurchaseOrderId(Long purchaseOrderId) {
        this.purchaseOrderId = purchaseOrderId;
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

    public LocalDate getBillDate() {
        return billDate;
    }

    public void setBillDate(LocalDate billDate) {
        this.billDate = billDate;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Double getAmountPaid() {
        return amountPaid;
    }

    public void setAmountPaid(Double amountPaid) {
        this.amountPaid = amountPaid;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getExpenseAccountId() {
        return expenseAccountId;
    }

    public void setExpenseAccountId(Long expenseAccountId) {
        this.expenseAccountId = expenseAccountId;
    }

    public String getExpenseAccountName() {
        return expenseAccountName;
    }

    public void setExpenseAccountName(String expenseAccountName) {
        this.expenseAccountName = expenseAccountName;
    }

    public Long getPayableAccountId() {
        return payableAccountId;
    }

    public void setPayableAccountId(Long payableAccountId) {
        this.payableAccountId = payableAccountId;
    }

    public String getPayableAccountName() {
        return payableAccountName;
    }

    public void setPayableAccountName(String payableAccountName) {
        this.payableAccountName = payableAccountName;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
