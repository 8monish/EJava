package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {
    Optional<Account> findByCode(String code);
    Optional<Account> findByName(String name);
    List<Account> findByAccountType(String accountType);
}
