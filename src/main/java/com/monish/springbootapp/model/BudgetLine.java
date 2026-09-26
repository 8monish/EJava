package com.monish.springbootapp.model;

import jakarta.persistence.*;

@Entity
@Table(name = "budget_lines")
public class BudgetLine {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long accountId;

    private String accountCode;

    @Column(nullable = false)
    private String accountName;

    @Column(nullable = false)
    private String accountType; // INCOME, EXPENSE

    @Column(nullable = false)
    private Double plannedAmount = 0.0;

    @Column(nullable = false)
    private Double actualAmount = 0.0;

    @Column(nullable = false)
    private Double variance = 0.0; // planned - actual or actual - planned

    @Column(nullable = false)
    private Double achievementRate = 0.0; // %

    public BudgetLine() {
    }

    public BudgetLine(Long accountId, String accountCode, String accountName, String accountType, Double plannedAmount) {
        this.accountId = accountId;
        this.accountCode = accountCode;
        this.accountName = accountName;
        this.accountType = accountType;
        this.plannedAmount = plannedAmount;
        this.actualAmount = 0.0;
        this.variance = plannedAmount;
        this.achievementRate = 0.0;
    }

    public void updateActual(Double actual) {
        this.actualAmount = Math.round(actual * 100.0) / 100.0;
        if ("INCOME".equalsIgnoreCase(this.accountType)) {
            this.variance = Math.round((this.actualAmount - this.plannedAmount) * 100.0) / 100.0;
            this.achievementRate = this.plannedAmount > 0 ? Math.round((this.actualAmount / this.plannedAmount) * 10000.0) / 100.0 : 0.0;
        } else {
            // For expenses, variance is budget remaining (planned - actual)
            this.variance = Math.round((this.plannedAmount - this.actualAmount) * 100.0) / 100.0;
            this.achievementRate = this.plannedAmount > 0 ? Math.round((this.actualAmount / this.plannedAmount) * 10000.0) / 100.0 : 0.0;
        }
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

    public String getAccountType() {
        return accountType;
    }

    public void setAccountType(String accountType) {
        this.accountType = accountType;
    }

    public Double getPlannedAmount() {
        return plannedAmount;
    }

    public void setPlannedAmount(Double plannedAmount) {
        this.plannedAmount = plannedAmount;
    }

    public Double getActualAmount() {
        return actualAmount;
    }

    public void setActualAmount(Double actualAmount) {
        this.actualAmount = actualAmount;
    }

    public Double getVariance() {
        return variance;
    }

    public void setVariance(Double variance) {
        this.variance = variance;
    }

    public Double getAchievementRate() {
        return achievementRate;
    }

    public void setAchievementRate(Double achievementRate) {
        this.achievementRate = achievementRate;
    }
}
