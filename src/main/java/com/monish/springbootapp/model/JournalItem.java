package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "journal_items")
public class JournalItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    private String accountCode;

    @Column(nullable = false)
    private String accountName;

    private Long analyticAccountId;

    private String analyticAccountName;

    @Column(nullable = false)
    private Double debit = 0.0;

    @Column(nullable = false)
    private Double credit = 0.0;

    private String label;

    public JournalItem() {
    }

    public JournalItem(Long accountId, String accountCode, String accountName, Long analyticAccountId,
                       String analyticAccountName, Double debit, Double credit, String label) {
        this.accountId = accountId;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.analyticAccountId = analyticAccountId;
        this.analyticAccountName = analyticAccountName;
        this.debit = debit != null ? debit : 0.0;
        this.credit = credit != null ? credit : 0.0;
        this.label = label;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAccountId() {
        return accountId;
    }

    public void setAccountId(Long accountId) {
        this.accountId = accountId;
    }

    public String getAccountCode() {
        return accountCode;
    }

    public void setAccountCode(String accountCode) {
        this.accountCode = accountCode;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public Long getAnalyticAccountId() {
        return analyticAccountId;
    }

    public void setAnalyticAccountId(Long analyticAccountId) {
        this.analyticAccountId = analyticAccountId;
    }

    public String getAnalyticAccountName() {
        return analyticAccountName;
    }

    public void setAnalyticAccountName(String analyticAccountName) {
        this.analyticAccountName = analyticAccountName;
    }

    public Double getDebit() {
        return debit;
    }

    public void setDebit(Double debit) {
        this.debit = debit;
    }

    public Double getCredit() {
        return credit;
    }

    public void setCredit(Double credit) {
        this.credit = credit;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }
}
