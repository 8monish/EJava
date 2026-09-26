package com.monish.springbootapp.service;

import com.monish.springbootapp.dto.BalanceSheetReport;
import com.monish.springbootapp.dto.BudgetPerformanceReport;
import com.monish.springbootapp.dto.ProfitAndLossReport;
import com.monish.springbootapp.model.Account;
import com.monish.springbootapp.model.Budget;
import com.monish.springbootapp.repository.AccountRepository;
import com.monish.springbootapp.repository.BudgetRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class ReportingService {

    private final AccountRepository accountRepository;
    private final BudgetService budgetService;
    private final BudgetRepository budgetRepository;

    public ReportingService(AccountRepository accountRepository,
                            BudgetService budgetService,
                            BudgetRepository budgetRepository) {
        this.accountRepository = accountRepository;
        this.budgetService = budgetService;
        this.budgetRepository = budgetRepository;
    }

    public BalanceSheetReport generateBalanceSheet() {
        BalanceSheetReport report = new BalanceSheetReport();
        List<Account> accounts = accountRepository.findAll();

        double totalAssets = 0.0;
        double totalLiabilities = 0.0;

        List<BalanceSheetReport.AccountRow> assetRows = new ArrayList<>();
        List<BalanceSheetReport.AccountRow> liabilityRows = new ArrayList<>();

        for (Account a : accounts) {
            double bal = a.getBalance() != null ? a.getBalance() : 0.0;
            if ("ASSET".equalsIgnoreCase(a.getAccountType())) {
                assetRows.add(new BalanceSheetReport.AccountRow(a.getCode(), a.getName(), bal));
                totalAssets += bal;
            } else if ("LIABILITY".equalsIgnoreCase(a.getAccountType())) {
                liabilityRows.add(new BalanceSheetReport.AccountRow(a.getCode(), a.getName(), bal));
                totalLiabilities += bal;
            }
        }

        totalAssets = Math.round(totalAssets * 100.0) / 100.0;
        totalLiabilities = Math.round(totalLiabilities * 100.0) / 100.0;

        // Municipal Net Equity / Retained Surplus = Total Assets - Total Liabilities
        double retainedSurplus = Math.round((totalAssets - totalLiabilities) * 100.0) / 100.0;
        double totalLiabilitiesAndEquity = Math.round((totalLiabilities + retainedSurplus) * 100.0) / 100.0;

        report.setReportDate(LocalDate.now());
        report.setAssets(assetRows);
        report.setTotalAssets(totalAssets);
        report.setLiabilities(liabilityRows);
        report.setTotalLiabilities(totalLiabilities);
        report.setRetainedSurplus(retainedSurplus);
        report.setTotalLiabilitiesAndEquity(totalLiabilitiesAndEquity);
        report.setBalanced(Math.abs(totalAssets - totalLiabilitiesAndEquity) < 0.01);

        return report;
    }

    public ProfitAndLossReport generateProfitAndLoss() {
        ProfitAndLossReport report = new ProfitAndLossReport();
        List<Account> accounts = accountRepository.findAll();

        double totalIncome = 0.0;
        double totalExpense = 0.0;

        List<BalanceSheetReport.AccountRow> incomeRows = new ArrayList<>();
        List<BalanceSheetReport.AccountRow> expenseRows = new ArrayList<>();

        for (Account a : accounts) {
            double bal = a.getBalance() != null ? a.getBalance() : 0.0;
            if ("INCOME".equalsIgnoreCase(a.getAccountType())) {
                incomeRows.add(new BalanceSheetReport.AccountRow(a.getCode(), a.getName(), bal));
                totalIncome += bal;
            } else if ("EXPENSE".equalsIgnoreCase(a.getAccountType())) {
                expenseRows.add(new BalanceSheetReport.AccountRow(a.getCode(), a.getName(), bal));
                totalExpense += bal;
            }
        }

        totalIncome = Math.round(totalIncome * 100.0) / 100.0;
        totalExpense = Math.round(totalExpense * 100.0) / 100.0;
        double netSurplus = Math.round((totalIncome - totalExpense) * 100.0) / 100.0;
        double margin = totalIncome > 0 ? Math.round((netSurplus / totalIncome) * 10000.0) / 100.0 : 0.0;

        report.setStartDate(LocalDate.now().withDayOfYear(1));
        report.setEndDate(LocalDate.now());
        report.setRevenues(incomeRows);
        report.setTotalRevenue(totalIncome);
        report.setExpenses(expenseRows);
        report.setTotalExpense(totalExpense);
        report.setNetOperatingSurplus(netSurplus);
        report.setMarginPercent(margin);

        return report;
    }

    public BudgetPerformanceReport generateBudgetReport(Long budgetId) {
        if (budgetId == null) {
            Budget defaultBudget = budgetRepository.findAll().stream().findFirst().orElse(null);
            if (defaultBudget != null) {
                return budgetService.getBudgetReport(defaultBudget.getId());
            }
            return new BudgetPerformanceReport();
        }
        return budgetService.getBudgetReport(budgetId);
    }
}
