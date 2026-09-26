package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.ChargingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ChargingSessionRepository extends JpaRepository<ChargingSession, Long> {
    List<ChargingSession> findByPoleCode(String poleCode);
    List<ChargingSession> findByStatus(String status);
    List<ChargingSession> findByContactId(Long contactId);
}
