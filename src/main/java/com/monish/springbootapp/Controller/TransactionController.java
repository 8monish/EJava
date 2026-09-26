package com.monish.springbootapp.Controller;

import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import com.monish.springbootapp.service.AccountingService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class TransactionController {

    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final AccountingService accountingService;

    public TransactionController(PurchaseOrderRepository purchaseOrderRepository,
                                 VendorBillRepository vendorBillRepository,
                                 SalesOrderRepository salesOrderRepository,
                                 CustomerInvoiceRepository customerInvoiceRepository,
                                 PaymentRepository paymentRepository,
                                 AccountingService accountingService) {
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorBillRepository = vendorBillRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerInvoiceRepository = customerInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.accountingService = accountingService;
    }

    // Purchase Orders
    @GetMapping("/purchase-orders")
    public List<PurchaseOrder> getPurchaseOrders() {
        return purchaseOrderRepository.findAll();
    }

    @PostMapping("/purchase-orders")
    public ResponseEntity<PurchaseOrder> createPurchaseOrder(@RequestBody PurchaseOrder po) {
        if (po.getOrderDate() == null) {
            po.setOrderDate(LocalDate.now());
        }
        if (po.getPoNumber() == null || po.getPoNumber().isBlank()) {
            po.setPoNumber("PO-" + System.currentTimeMillis() % 100000);
        }
        po.recalculateTotal();
        po.setStatus("CONFIRMED");
        PurchaseOrder saved = purchaseOrderRepository.save(po);
        return ResponseEntity.ok(saved);
    }

    // Step 3: Convert PO into Vendor Bill
    @PostMapping("/purchase-orders/{id}/convert-to-bill")
    public ResponseEntity<VendorBill> convertPoToBill(@PathVariable Long id) {
        VendorBill bill = accountingService.convertPoToVendorBill(id);
        return ResponseEntity.ok(bill);
    }

    // Vendor Bills
    @GetMapping("/vendor-bills")
    public List<VendorBill> getVendorBills() {
        return vendorBillRepository.findAll();
    }

    // Issue payment for Vendor Bill via Bank
    @PostMapping("/vendor-bills/{id}/pay")
    public ResponseEntity<Payment> payVendorBill(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> payload) {
        Long bankAccountId = payload != null ? payload.get("bankAccountId") : null;
        Payment payment = accountingService.payVendorBill(id, bankAccountId);
        return ResponseEntity.ok(payment);
    }

    // Sales Orders
    @GetMapping("/sales-orders")
    public List<SalesOrder> getSalesOrders() {
        return salesOrderRepository.findAll();
    }

    @PostMapping("/sales-orders")
    public ResponseEntity<SalesOrder> createSalesOrder(@RequestBody SalesOrder so) {
        if (so.getOrderDate() == null) {
            so.setOrderDate(LocalDate.now());
        }
        if (so.getSoNumber() == null || so.getSoNumber().isBlank()) {
            so.setSoNumber("SO-" + System.currentTimeMillis() % 100000);
        }
        so.recalculateTotal();
        so.setStatus("CONFIRMED");
        SalesOrder saved = salesOrderRepository.save(so);
        return ResponseEntity.ok(saved);
    }

    // Convert Sales Order to Customer Invoice
    @PostMapping("/sales-orders/{id}/convert-to-invoice")
    public ResponseEntity<CustomerInvoice> convertSoToInvoice(@PathVariable Long id) {
        CustomerInvoice invoice = accountingService.convertSoToCustomerInvoice(id);
        return ResponseEntity.ok(invoice);
    }

    // Customer Invoices
    @GetMapping("/customer-invoices")
    public List<CustomerInvoice> getCustomerInvoices() {
        return customerInvoiceRepository.findAll();
    }

    // Collect Customer Payment via Bank
    @PostMapping("/customer-invoices/{id}/pay")
    public ResponseEntity<Payment> payCustomerInvoice(@PathVariable Long id, @RequestBody(required = false) Map<String, Long> payload) {
        Long bankAccountId = payload != null ? payload.get("bankAccountId") : null;
        Payment payment = accountingService.collectCustomerPayment(id, bankAccountId);
        return ResponseEntity.ok(payment);
    }

    // Payments
    @GetMapping("/payments")
    public List<Payment> getPayments() {
        return paymentRepository.findAll();
    }
}
