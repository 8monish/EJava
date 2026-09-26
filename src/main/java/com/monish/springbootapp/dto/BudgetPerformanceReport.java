package com.monish.springbootapp.dto;

import com.monish.springbootapp.model.BudgetLine;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BudgetPerformanceReport {
    private Long budgetId;
    private String budgetName;
    private String fiscalYear;
    private Long analyticAccountId;
    private String analyticAccountName;
    private LocalDate startDate;
    private LocalDate endDate;
    private Double totalPlannedRevenue = 0.0;
    private Double totalActualRevenue = 0.0;
    private Double revenueVariance = 0.0;
    private Double totalPlannedExpense = 0.0;
    private Double totalActualExpense = 0.0;
    private Double expenseVariance = 0.0;
    private Double netPlannedMargin = 0.0;
    private Double netActualMargin = 0.0;
    private Double performanceRatio = 0.0; // %
    private List<BudgetLine> lines = new ArrayList<>();

    public BudgetPerformanceReport() {}

    public Long getBudgetId() { return budgetId; }
    public void setBudgetId(Long budgetId) { this.budgetId = budgetId; }
    public String getBudgetName() { return budgetName; }
    public void setBudgetName(String budgetName) { this.budgetName = budgetName; }
    public String getFiscalYear() { return fiscalYear; }
    public void setFiscalYear(String fiscalYear) { this.fiscalYear = fiscalYear; }
    public Long getAnalyticAccountId() { return analyticAccountId; }
    public void setAnalyticAccountId(Long analyticAccountId) { this.analyticAccountId = analyticAccountId; }
    public String getAnalyticAccountName() { return analyticAccountName; }
    public void setAnalyticAccountName(String analyticAccountName) { this.analyticAccountName = analyticAccountName; }
    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public Double getTotalPlannedRevenue() { return totalPlannedRevenue; }
    public void setTotalPlannedRevenue(Double totalPlannedRevenue) { this.totalPlannedRevenue = totalPlannedRevenue; }
    public Double getTotalActualRevenue() { return totalActualRevenue; }
    public void setTotalActualRevenue(Double totalActualRevenue) { this.totalActualRevenue = totalActualRevenue; }
    public Double getRevenueVariance() { return revenueVariance; }
    public void setRevenueVariance(Double revenueVariance) { this.revenueVariance = revenueVariance; }
    public Double getTotalPlannedExpense() { return totalPlannedExpense; }
    public void setTotalPlannedExpense(Double totalPlannedExpense) { this.totalPlannedExpense = totalPlannedExpense; }
    public Double getTotalActualExpense() { return totalActualExpense; }
    public void setTotalActualExpense(Double totalActualExpense) { this.totalActualExpense = totalActualExpense; }
    public Double getExpenseVariance() { return expenseVariance; }
    public void setExpenseVariance(Double expenseVariance) { this.expenseVariance = expenseVariance; }
    public Double getNetPlannedMargin() { return netPlannedMargin; }
    public void setNetPlannedMargin(Double netPlannedMargin) { this.netPlannedMargin = netPlannedMargin; }
    public Double getNetActualMargin() { return netActualMargin; }
    public void setNetActualMargin(Double netActualMargin) { this.netActualMargin = netActualMargin; }
    public Double getPerformanceRatio() { return performanceRatio; }
    public void setPerformanceRatio(Double performanceRatio) { this.performanceRatio = performanceRatio; }
    public List<BudgetLine> getLines() { return lines; }
    public void setLines(List<BudgetLine> lines) { this.lines = lines; }
}
