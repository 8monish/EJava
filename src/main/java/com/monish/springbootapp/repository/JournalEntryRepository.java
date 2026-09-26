package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface JournalEntryRepository extends JpaRepository<JournalEntry, Long> {
    Optional<JournalEntry> findByEntryNumber(String entryNumber);
    List<JournalEntry> findByJournalCode(String journalCode);
    List<JournalEntry> findByEntryDateBetween(LocalDate start, LocalDate end);
    List<JournalEntry> findAllByOrderByEntryDateDescIdDesc();
}
