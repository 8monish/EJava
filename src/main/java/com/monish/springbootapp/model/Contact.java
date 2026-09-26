package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String contactType; // CUSTOMER, VENDOR

    @Column(nullable = false)
    private String roleCategory; // Commercial Parking Operator, LED Module Vendor, Power Utility, Fleet Operator

    private String email;
    private String phone;
    private String address;

    @Column(nullable = false)
    private Double accountBalance = 0.0;

    public Contact() {
    }

    public Contact(String name, String contactType, String roleCategory, String email, String phone, String address) {
        this.name = name;
        this.contactType = contactType;
        this.roleCategory = roleCategory;
        this.email = email;
        this.phone = phone;
        this.address = address;
        this.accountBalance = 0.0;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContactType() {
        return contactType;
    }

    public void setContactType(String contactType) {
        this.contactType = contactType;
    }

    public String getRoleCategory() {
        return roleCategory;
    }

    public void setRoleCategory(String roleCategory) {
        this.roleCategory = roleCategory;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Double getAccountBalance() {
        return accountBalance;
    }

    public void setAccountBalance(Double accountBalance) {
        this.accountBalance = accountBalance;
    }
}
