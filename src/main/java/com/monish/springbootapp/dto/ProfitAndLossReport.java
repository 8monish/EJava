package com.monish.springbootapp.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ProfitAndLossReport {
    private LocalDate startDate;
    private LocalDate endDate;
    private List<BalanceSheetReport.AccountRow> revenues = new ArrayList<>();
    private Double totalRevenue = 0.0;
    private List<BalanceSheetReport.AccountRow> expenses = new ArrayList<>();
    private Double totalExpense = 0.0;
    private Double netOperatingSurplus = 0.0;
    private Double marginPercent = 0.0;

    public ProfitAndLossReport() {}

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public List<BalanceSheetReport.AccountRow> getRevenues() { return revenues; }
    public void setRevenues(List<BalanceSheetReport.AccountRow> revenues) { this.revenues = revenues; }
    public Double getTotalRevenue() { return totalRevenue; }
    public void setTotalRevenue(Double totalRevenue) { this.totalRevenue = totalRevenue; }
    public List<BalanceSheetReport.AccountRow> getExpenses() { return expenses; }
    public void setExpenses(List<BalanceSheetReport.AccountRow> expenses) { this.expenses = expenses; }
    public Double getTotalExpense() { return totalExpense; }
    public void setTotalExpense(Double totalExpense) { this.totalExpense = totalExpense; }
    public Double getNetOperatingSurplus() { return netOperatingSurplus; }
    public void setNetOperatingSurplus(Double netOperatingSurplus) { this.netOperatingSurplus = netOperatingSurplus; }
    public Double getMarginPercent() { return marginPercent; }
    public void setMarginPercent(Double marginPercent) { this.marginPercent = marginPercent; }
}
