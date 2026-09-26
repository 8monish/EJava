package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "journal_entries")
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String entryNumber;

    @Column(nullable = false)
    private String journalCode; // SALES, PURCHASE, CASH, BANK

    @Column(nullable = false)
    private LocalDate entryDate;

    private String reference;

    private String description;

    @Column(nullable = false)
    private Double totalDebit = 0.0;

    @Column(nullable = false)
    private Double totalCredit = 0.0;

    @Column(nullable = false)
    private Boolean posted = true;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "journal_entry_id")
    private List<JournalItem> items = new ArrayList<>();

    public JournalEntry() {
    }

    public JournalEntry(String entryNumber, String journalCode, LocalDate entryDate, String reference, String description) {
        this.entryNumber = entryNumber;
        this.journalCode = journalCode;
        this.entryDate = entryDate;
        this.reference = reference;
        this.description = description;
        this.posted = true;
    }

    public void addItem(JournalItem item) {
        this.items.add(item);
        recomputeTotals();
    }

    public void recomputeTotals() {
        double d = 0.0;
        double c = 0.0;
        for (JournalItem it : items) {
            d += (it.getDebit() != null ? it.getDebit() : 0.0);
            c += (it.getCredit() != null ? it.getCredit() : 0.0);
        }
        this.totalDebit = Math.round(d * 100.0) / 100.0;
        this.totalCredit = Math.round(c * 100.0) / 100.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEntryNumber() {
        return entryNumber;
    }

    public void setEntryNumber(String entryNumber) {
        this.entryNumber = entryNumber;
    }

    public String getJournalCode() {
        return journalCode;
    }

    public void setJournalCode(String journalCode) {
        this.journalCode = journalCode;
    }

    public LocalDate getEntryDate() {
        return entryDate;
    }

    public void setEntryDate(LocalDate entryDate) {
        this.entryDate = entryDate;
    }

    public String getReference() {
        return reference;
    }

    public void setReference(String reference) {
        this.reference = reference;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Double getTotalDebit() {
        return totalDebit;
    }

    public void setTotalDebit(Double totalDebit) {
        this.totalDebit = totalDebit;
    }

    public Double getTotalCredit() {
        return totalCredit;
    }

    public void setTotalCredit(Double totalCredit) {
        this.totalCredit = totalCredit;
    }

    public Boolean getPosted() {
        return posted;
    }

    public void setPosted(Boolean posted) {
        this.posted = posted;
    }

    public List<JournalItem> getItems() {
        return items;
    }

    public void setItems(List<JournalItem> items) {
        this.items = items;
        recomputeTotals();
    }
}
