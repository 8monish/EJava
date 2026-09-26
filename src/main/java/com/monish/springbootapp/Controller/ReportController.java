package com.monish.springbootapp.Controller;

import com.monish.springbootapp.dto.BalanceSheetReport;
import com.monish.springbootapp.dto.BudgetPerformanceReport;
import com.monish.springbootapp.dto.ProfitAndLossReport;
import com.monish.springbootapp.service.ReportingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/reports")
@CrossOrigin(origins = "*")
public class ReportController {

    private final ReportingService reportingService;

    public ReportController(ReportingService reportingService) {
        this.reportingService = reportingService;
    }

    // Step 4 & Report 1: Balance Sheet
    @GetMapping("/balance-sheet")
    public ResponseEntity<BalanceSheetReport> getBalanceSheet() {
        BalanceSheetReport report = reportingService.generateBalanceSheet();
        return ResponseEntity.ok(report);
    }

    // Step 4 & Report 2: Profit & Loss Account
    @GetMapping("/profit-and-loss")
    public ResponseEntity<ProfitAndLossReport> getProfitAndLoss() {
        ProfitAndLossReport report = reportingService.generateProfitAndLoss();
        return ResponseEntity.ok(report);
    }

    // Step 4 & Report 3: Budget Performance Report
    @GetMapping("/budget-performance")
    public ResponseEntity<BudgetPerformanceReport> getBudgetPerformance(@RequestParam(required = false) Long budgetId) {
        BudgetPerformanceReport report = reportingService.generateBudgetReport(budgetId);
        return ResponseEntity.ok(report);
    }
}
