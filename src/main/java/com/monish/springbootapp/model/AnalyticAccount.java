package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "analytic_accounts")
public class AnalyticAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String code;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String sectorType = "LIGHTING_SECTOR";

    private Integer poleCount = 0;

    private String description;

    public AnalyticAccount() {
    }

    public AnalyticAccount(String code, String name, String sectorType, Integer poleCount, String description) {
        this.code = code;
        this.name = name;
        this.sectorType = sectorType;
        this.poleCount = poleCount;
        this.description = description;
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

    public String getSectorType() {
        return sectorType;
    }

    public void setSectorType(String sectorType) {
        this.sectorType = sectorType;
    }

    public Integer getPoleCount() {
        return poleCount;
    }

    public void setPoleCount(Integer poleCount) {
        this.poleCount = poleCount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
