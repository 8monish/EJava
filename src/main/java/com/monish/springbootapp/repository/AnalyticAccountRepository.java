package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.AnalyticAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AnalyticAccountRepository extends JpaRepository<AnalyticAccount, Long> {
    Optional<AnalyticAccount> findByCode(String code);
    Optional<AnalyticAccount> findByName(String name);
}
