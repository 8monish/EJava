package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "payments")
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String paymentNumber;

    @Column(nullable = false)
    private String paymentType; // RECEIPT (Inbound from customer), PAYMENT (Outbound to vendor)

    @Column(nullable = false)
    private Long contactId;

    @Column(nullable = false)
    private String contactName;

    @Column(nullable = false)
    private LocalDate paymentDate;

    @Column(nullable = false)
    private Double amount;

    private Long bankAccountId;
    private String bankAccountName;

    private String targetDocType; // CUSTOMER_INVOICE, VENDOR_BILL
    private Long targetDocId;

    private Long journalEntryId;

    private String notes;

    public Payment() {
    }

    public Payment(String paymentNumber, String paymentType, Long contactId, String contactName,
                   LocalDate paymentDate, Double amount, Long bankAccountId, String bankAccountName,
                   String targetDocType, Long targetDocId, String notes) {
        this.paymentNumber = paymentNumber;
        this.paymentType = paymentType;
        this.contactId = contactId;
        this.contactName = contactName;
        this.paymentDate = paymentDate;
        this.amount = amount;
        this.bankAccountId = bankAccountId;
        this.bankAccountName = bankAccountName;
        this.targetDocType = targetDocType;
        this.targetDocId = targetDocId;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPaymentNumber() {
        return paymentNumber;
    }

    public void setPaymentNumber(String paymentNumber) {
        this.paymentNumber = paymentNumber;
    }

    public String getPaymentType() {
        return paymentType;
    }

    public void setPaymentType(String paymentType) {
        this.paymentType = paymentType;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public LocalDate getPaymentDate() {
        return paymentDate;
    }

    public void setPaymentDate(LocalDate paymentDate) {
        this.paymentDate = paymentDate;
    }

    public Double getAmount() {
        return amount;
    }

    public void setAmount(Double amount) {
        this.amount = amount;
    }

    public Long getBankAccountId() {
        return bankAccountId;
    }

    public void setBankAccountId(Long bankAccountId) {
        this.bankAccountId = bankAccountId;
    }

    public String getBankAccountName() {
        return bankAccountName;
    }

    public void setBankAccountName(String bankAccountName) {
        this.bankAccountName = bankAccountName;
    }

    public String getTargetDocType() {
        return targetDocType;
    }

    public void setTargetDocType(String targetDocType) {
        this.targetDocType = targetDocType;
    }

    public Long getTargetDocId() {
        return targetDocId;
    }

    public void setTargetDocId(Long targetDocId) {
        this.targetDocId = targetDocId;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
