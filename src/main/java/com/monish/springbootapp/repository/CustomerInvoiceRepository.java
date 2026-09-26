package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.CustomerInvoice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerInvoiceRepository extends JpaRepository<CustomerInvoice, Long> {
    Optional<CustomerInvoice> findByInvoiceNumber(String invoiceNumber);
    List<CustomerInvoice> findByStatus(String status);
    List<CustomerInvoice> findByCustomerId(Long customerId);
}
