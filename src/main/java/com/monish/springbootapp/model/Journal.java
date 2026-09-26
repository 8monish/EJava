package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "journals")
public class Journal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code; // SALES, PURCHASE, CASH, BANK

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String journalType;

    public Journal() {
    }

    public Journal(String code, String name, String journalType) {
        this.code = code;
        this.name = name;
        this.journalType = journalType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getJournalType() {
        return journalType;
    }

    public void setJournalType(String journalType) {
        this.journalType = journalType;
    }
}
