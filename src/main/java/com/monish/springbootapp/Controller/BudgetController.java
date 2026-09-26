package com.monish.springbootapp.Controller;

import com.monish.springbootapp.dto.BudgetPerformanceReport;
import com.monish.springbootapp.model.Budget;
import com.monish.springbootapp.service.BudgetService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
@CrossOrigin(origins = "*")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @GetMapping
    public List<Budget> getAllBudgets() {
        return budgetService.getAllBudgets();
    }

    @GetMapping("/{id}")
    public Budget getBudgetById(@PathVariable Long id) {
        return budgetService.getBudgetById(id);
    }

    @PostMapping
    public ResponseEntity<Budget> createBudget(@RequestBody Budget budget) {
        Budget saved = budgetService.saveBudget(budget);
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/{id}/report")
    public ResponseEntity<BudgetPerformanceReport> getBudgetReport(@PathVariable Long id) {
        BudgetPerformanceReport report = budgetService.getBudgetReport(id);
        return ResponseEntity.ok(report);
    }
}
