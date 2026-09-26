package com.monish.springbootapp.dto;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class BalanceSheetReport {
    private LocalDate reportDate = LocalDate.now();
    private List<AccountRow> assets = new ArrayList<>();
    private Double totalAssets = 0.0;
    private List<AccountRow> liabilities = new ArrayList<>();
    private Double totalLiabilities = 0.0;
    private Double retainedSurplus = 0.0;
    private Double totalLiabilitiesAndEquity = 0.0;
    private Boolean balanced = true;

    public static class AccountRow {
        private String code;
        private String name;
        private Double balance;

        public AccountRow() {}
        public AccountRow(String code, String name, Double balance) {
            this.code = code;
            this.name = name;
            this.balance = balance;
        }
        public String getCode() { return code; }
        public void setCode(String code) { this.code = code; }
        public String getName() { return name; }
        public void setName(String name) { this.name = name; }
        public Double getBalance() { return balance; }
        public void setBalance(Double balance) { this.balance = balance; }
    }

    public BalanceSheetReport() {}

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
    public List<AccountRow> getAssets() { return assets; }
    public void setAssets(List<AccountRow> assets) { this.assets = assets; }
    public Double getTotalAssets() { return totalAssets; }
    public void setTotalAssets(Double totalAssets) { this.totalAssets = totalAssets; }
    public List<AccountRow> getLiabilities() { return liabilities; }
    public void setLiabilities(List<AccountRow> liabilities) { this.liabilities = liabilities; }
    public Double getTotalLiabilities() { return totalLiabilities; }
    public void setTotalLiabilities(Double totalLiabilities) { this.totalLiabilities = totalLiabilities; }
    public Double getRetainedSurplus() { return retainedSurplus; }
    public void setRetainedSurplus(Double retainedSurplus) { this.retainedSurplus = retainedSurplus; }
    public Double getTotalLiabilitiesAndEquity() { return totalLiabilitiesAndEquity; }
    public void setTotalLiabilitiesAndEquity(Double totalLiabilitiesAndEquity) { this.totalLiabilitiesAndEquity = totalLiabilitiesAndEquity; }
    public Boolean getBalanced() { return balanced; }
    public void setBalanced(Boolean balanced) { this.balanced = balanced; }
}
