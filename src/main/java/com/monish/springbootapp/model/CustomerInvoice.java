package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "customer_invoices")
public class CustomerInvoice {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String invoiceNumber;

    private Long salesOrderId;

    private Long chargingSessionId;

    @Column(nullable = false)
    private Long customerId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private LocalDate invoiceDate;

    private LocalDate dueDate;

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    @Column(nullable = false)
    private Double amountPaid = 0.0;

    @Column(nullable = false)
    private String status = "POSTED"; // DRAFT, POSTED, PAID

    private Long receivableAccountId;
    private String receivableAccountName;

    private Long incomeAccountId;
    private String incomeAccountName;

    private Long journalEntryId;

    private String description;

    public CustomerInvoice() {
    }

    public CustomerInvoice(String invoiceNumber, Long salesOrderId, Long chargingSessionId,
                           Long customerId, String customerName, LocalDate invoiceDate, LocalDate dueDate,
                           Double totalAmount, Long receivableAccountId, String receivableAccountName,
                           Long incomeAccountId, String incomeAccountName, String description) {
        this.invoiceNumber = invoiceNumber;
        this.salesOrderId = salesOrderId;
        this.chargingSessionId = chargingSessionId;
        this.customerId = customerId;
        this.customerName = customerName;
        this.invoiceDate = invoiceDate;
        this.dueDate = dueDate;
        this.totalAmount = totalAmount;
        this.amountPaid = 0.0;
        this.status = "POSTED";
        this.receivableAccountId = receivableAccountId;
        this.receivableAccountName = receivableAccountName;
        this.incomeAccountId = incomeAccountId;
        this.incomeAccountName = incomeAccountName;
        this.description = description;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public Long getSalesOrderId() {
        return salesOrderId;
    }

    public void setSalesOrderId(Long salesOrderId) {
        this.salesOrderId = salesOrderId;
    }

    public Long getChargingSessionId() {
        return chargingSessionId;
    }

    public void setChargingSessionId(Long chargingSessionId) {
        this.chargingSessionId = chargingSessionId;
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

    public LocalDate getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(LocalDate invoiceDate) {
        this.invoiceDate = invoiceDate;
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

    public Long getReceivableAccountId() {
        return receivableAccountId;
    }

    public void setReceivableAccountId(Long receivableAccountId) {
        this.receivableAccountId = receivableAccountId;
    }

    public String getReceivableAccountName() {
        return receivableAccountName;
    }

    public void setReceivableAccountName(String receivableAccountName) {
        this.receivableAccountName = receivableAccountName;
    }

    public Long getIncomeAccountId() {
        return incomeAccountId;
    }

    public void setIncomeAccountId(Long incomeAccountId) {
        this.incomeAccountId = incomeAccountId;
    }

    public String getIncomeAccountName() {
        return incomeAccountName;
    }

    public void setIncomeAccountName(String incomeAccountName) {
        this.incomeAccountName = incomeAccountName;
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
