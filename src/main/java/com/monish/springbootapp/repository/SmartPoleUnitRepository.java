package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.SmartPoleUnit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SmartPoleUnitRepository extends JpaRepository<SmartPoleUnit, Long> {
    Optional<SmartPoleUnit> findByPoleCode(String poleCode);
    boolean existsByPoleCode(String poleCode);
}
