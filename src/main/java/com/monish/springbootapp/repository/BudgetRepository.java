package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.Budget;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByAnalyticAccountId(Long analyticAccountId);
    List<Budget> findByFiscalYear(String fiscalYear);
}
