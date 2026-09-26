package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String fiscalYear;

    @Column(nullable = false)
    private Long analyticAccountId;

    @Column(nullable = false)
    private String analyticAccountName;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, CLOSED

    @Column(nullable = false)
    private Double totalPlannedRevenue = 0.0;

    @Column(nullable = false)
    private Double totalActualRevenue = 0.0;

    @Column(nullable = false)
    private Double totalPlannedExpense = 0.0;

    @Column(nullable = false)
    private Double totalActualExpense = 0.0;

    @Column(nullable = false)
    private Double netPlannedMargin = 0.0;

    @Column(nullable = false)
    private Double netActualMargin = 0.0;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "budget_id")
    private List<BudgetLine> lines = new ArrayList<>();

    public Budget() {
    }

    public Budget(String name, String fiscalYear, Long analyticAccountId, String analyticAccountName,
                  LocalDate startDate, LocalDate endDate) {
        this.name = name;
        this.fiscalYear = fiscalYear;
        this.analyticAccountId = analyticAccountId;
        this.analyticAccountName = analyticAccountName;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "ACTIVE";
    }

    public void addLine(BudgetLine line) {
        this.lines.add(line);
        recalculateTotals();
    }

    public void recalculateTotals() {
        double planRev = 0.0;
        double actRev = 0.0;
        double planExp = 0.0;
        double actExp = 0.0;

        for (BudgetLine line : lines) {
            if ("INCOME".equalsIgnoreCase(line.getAccountType())) {
                planRev += (line.getPlannedAmount() != null ? line.getPlannedAmount() : 0.0);
                actRev += (line.getActualAmount() != null ? line.getActualAmount() : 0.0);
            } else if ("EXPENSE".equalsIgnoreCase(line.getAccountType())) {
                planExp += (line.getPlannedAmount() != null ? line.getPlannedAmount() : 0.0);
                actExp += (line.getActualAmount() != null ? line.getActualAmount() : 0.0);
            }
        }

        this.totalPlannedRevenue = Math.round(planRev * 100.0) / 100.0;
        this.totalActualRevenue = Math.round(actRev * 100.0) / 100.0;
        this.totalPlannedExpense = Math.round(planExp * 100.0) / 100.0;
        this.totalActualExpense = Math.round(actExp * 100.0) / 100.0;
        this.netPlannedMargin = Math.round((this.totalPlannedRevenue - this.totalPlannedExpense) * 100.0) / 100.0;
        this.netActualMargin = Math.round((this.totalActualRevenue - this.totalActualExpense) * 100.0) / 100.0;
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

    public String getFiscalYear() {
        return fiscalYear;
    }

    public void setFiscalYear(String fiscalYear) {
        this.fiscalYear = fiscalYear;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Double getTotalPlannedRevenue() {
        return totalPlannedRevenue;
    }

    public void setTotalPlannedRevenue(Double totalPlannedRevenue) {
        this.totalPlannedRevenue = totalPlannedRevenue;
    }

    public Double getTotalActualRevenue() {
        return totalActualRevenue;
    }

    public void setTotalActualRevenue(Double totalActualRevenue) {
        this.totalActualRevenue = totalActualRevenue;
    }

    public Double getTotalPlannedExpense() {
        return totalPlannedExpense;
    }

    public void setTotalPlannedExpense(Double totalPlannedExpense) {
        this.totalPlannedExpense = totalPlannedExpense;
    }

    public Double getTotalActualExpense() {
        return totalActualExpense;
    }

    public void setTotalActualExpense(Double totalActualExpense) {
        this.totalActualExpense = totalActualExpense;
    }

    public Double getNetPlannedMargin() {
        return netPlannedMargin;
    }

    public void setNetPlannedMargin(Double netPlannedMargin) {
        this.netPlannedMargin = netPlannedMargin;
    }

    public Double getNetActualMargin() {
        return netActualMargin;
    }

    public void setNetActualMargin(Double netActualMargin) {
        this.netActualMargin = netActualMargin;
    }

    public List<BudgetLine> getLines() {
        return lines;
    }

    public void setLines(List<BudgetLine> lines) {
        this.lines = lines;
        recalculateTotals();
    }
}
