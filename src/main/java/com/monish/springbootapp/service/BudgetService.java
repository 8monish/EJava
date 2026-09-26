package com.monish.springbootapp.service;

import com.monish.springbootapp.dto.BudgetPerformanceReport;
import com.monish.springbootapp.model.Account;
import com.monish.springbootapp.model.Budget;
import com.monish.springbootapp.model.BudgetLine;
import com.monish.springbootapp.model.JournalEntry;
import com.monish.springbootapp.model.JournalItem;
import com.monish.springbootapp.repository.AccountRepository;
import com.monish.springbootapp.repository.BudgetRepository;
import com.monish.springbootapp.repository.JournalEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final AccountRepository accountRepository;

    public BudgetService(BudgetRepository budgetRepository,
                         JournalEntryRepository journalEntryRepository,
                         AccountRepository accountRepository) {
        this.budgetRepository = budgetRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.accountRepository = accountRepository;
    }

    public List<Budget> getAllBudgets() {
        List<Budget> budgets = budgetRepository.findAll();
        for (Budget b : budgets) {
            refreshBudgetActuals(b);
        }
        return budgets;
    }

    public Budget getBudgetById(Long id) {
        Budget budget = budgetRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Budget not found: " + id));
        return refreshBudgetActuals(budget);
    }

    @Transactional
    public Budget saveBudget(Budget budget) {
        return budgetRepository.save(budget);
    }

    @Transactional
    public Budget refreshBudgetActuals(Budget budget) {
        List<JournalEntry> allEntries = journalEntryRepository.findAll();

        for (BudgetLine line : budget.getLines()) {
            double actual = 0.0;
            for (JournalEntry je : allEntries) {
                for (JournalItem ji : je.getItems()) {
                    boolean accountMatches = ji.getAccountId().equals(line.getAccountId()) ||
                        (ji.getAccountName() != null && ji.getAccountName().equalsIgnoreCase(line.getAccountName()));
                    
                    boolean analyticMatches = (line.getAccountId() != null) &&
                        (ji.getAnalyticAccountId() == null || ji.getAnalyticAccountId().equals(budget.getAnalyticAccountId()));

                    if (accountMatches && analyticMatches) {
                        if ("INCOME".equalsIgnoreCase(line.getAccountType())) {
                            actual += (ji.getCredit() != null ? ji.getCredit() : 0.0);
                        } else {
                            actual += (ji.getDebit() != null ? ji.getDebit() : 0.0);
                        }
                    }
                }
            }

            // If no specific analytic entries matched, fallback to current general ledger balance of this account
            if (actual == 0.0) {
                Account acc = accountRepository.findById(line.getAccountId()).orElse(null);
                if (acc != null && acc.getBalance() != null) {
                    actual = acc.getBalance();
                }
            }

            line.updateActual(actual);
        }

        budget.recalculateTotals();
        return budgetRepository.save(budget);
    }

    public BudgetPerformanceReport getBudgetReport(Long budgetId) {
        Budget b = getBudgetById(budgetId);

        BudgetPerformanceReport report = new BudgetPerformanceReport();
        report.setBudgetId(b.getId());
        report.setBudgetName(b.getName());
        report.setFiscalYear(b.getFiscalYear());
        report.setAnalyticAccountId(b.getAnalyticAccountId());
        report.setAnalyticAccountName(b.getAnalyticAccountName());
        report.setStartDate(b.getStartDate());
        report.setEndDate(b.getEndDate());
        report.setTotalPlannedRevenue(b.getTotalPlannedRevenue());
        report.setTotalActualRevenue(b.getTotalActualRevenue());
        report.setRevenueVariance(Math.round((b.getTotalActualRevenue() - b.getTotalPlannedRevenue()) * 100.0) / 100.0);
        report.setTotalPlannedExpense(b.getTotalPlannedExpense());
        report.setTotalActualExpense(b.getTotalActualExpense());
        report.setExpenseVariance(Math.round((b.getTotalPlannedExpense() - b.getTotalActualExpense()) * 100.0) / 100.0);
        report.setNetPlannedMargin(b.getNetPlannedMargin());
        report.setNetActualMargin(b.getNetActualMargin());
        
        double perf = (b.getTotalPlannedRevenue() > 0)
            ? Math.round((b.getTotalActualRevenue() / b.getTotalPlannedRevenue()) * 10000.0) / 100.0
            : 100.0;
        report.setPerformanceRatio(perf);
        report.setLines(b.getLines());

        return report;
    }
}
