package com.monish.springbootapp.repository;

import com.monish.springbootapp.model.VendorBill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface VendorBillRepository extends JpaRepository<VendorBill, Long> {
    Optional<VendorBill> findByBillNumber(String billNumber);
    List<VendorBill> findByStatus(String status);
    List<VendorBill> findByVendorId(Long vendorId);
}
