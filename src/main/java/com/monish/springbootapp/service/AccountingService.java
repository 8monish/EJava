package com.monish.springbootapp.service;

import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class AccountingService {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final VendorBillRepository vendorBillRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final PaymentRepository paymentRepository;
    private final ContactRepository contactRepository;

    public AccountingService(AccountRepository accountRepository,
                             JournalRepository journalRepository,
                             JournalEntryRepository journalEntryRepository,
                             PurchaseOrderRepository purchaseOrderRepository,
                             VendorBillRepository vendorBillRepository,
                             SalesOrderRepository salesOrderRepository,
                             CustomerInvoiceRepository customerInvoiceRepository,
                             PaymentRepository paymentRepository,
                             ContactRepository contactRepository) {
        this.accountRepository = accountRepository;
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.vendorBillRepository = vendorBillRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.customerInvoiceRepository = customerInvoiceRepository;
        this.paymentRepository = paymentRepository;
        this.contactRepository = contactRepository;
    }

    @Transactional
    public JournalEntry postJournalEntry(JournalEntry entry) {
        entry.recomputeTotals();
        // Check debit == credit
        if (Math.abs(entry.getTotalDebit() - entry.getTotalCredit()) > 0.01) {
            throw new IllegalArgumentException("Journal Entry must balance! Debits: " 
                + entry.getTotalDebit() + ", Credits: " + entry.getTotalCredit());
        }

        JournalEntry saved = journalEntryRepository.save(entry);

        // Update account balances
        for (JournalItem item : saved.getItems()) {
            Account account = accountRepository.findById(item.getAccountId())
                .orElse(null);
            if (account != null) {
                double debit = item.getDebit() != null ? item.getDebit() : 0.0;
                double credit = item.getCredit() != null ? item.getCredit() : 0.0;

                double current = account.getBalance() != null ? account.getBalance() : 0.0;
                if ("ASSET".equalsIgnoreCase(account.getAccountType()) || "EXPENSE".equalsIgnoreCase(account.getAccountType())) {
                    account.setBalance(Math.round((current + debit - credit) * 100.0) / 100.0);
                } else { // LIABILITY, INCOME
                    account.setBalance(Math.round((current + credit - debit) * 100.0) / 100.0);
                }
                accountRepository.save(account);
            }
        }
        return saved;
    }

    @Transactional
    public VendorBill convertPoToVendorBill(Long poId) {
        PurchaseOrder po = purchaseOrderRepository.findById(poId)
            .orElseThrow(() -> new IllegalArgumentException("Purchase Order not found: " + poId));

        if ("BILLED".equalsIgnoreCase(po.getStatus())) {
            throw new IllegalStateException("PO " + po.getPoNumber() + " is already billed.");
        }

        Account expenseAcc = accountRepository.findByName("Fixture Replacement Costs")
            .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "EXPENSE".equals(a.getAccountType())).findFirst().orElse(null));
        Account payableAcc = accountRepository.findByName("Fixture Vendor Payables")
            .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "LIABILITY".equals(a.getAccountType())).findFirst().orElse(null));

        String billNum = "BILL-" + System.currentTimeMillis() % 100000;
        VendorBill bill = new VendorBill(
            billNum,
            po.getId(),
            po.getVendorId(),
            po.getVendorName(),
            LocalDate.now(),
            LocalDate.now().plusDays(30),
            po.getTotalAmount(),
            expenseAcc != null ? expenseAcc.getId() : 1L,
            expenseAcc != null ? expenseAcc.getName() : "Fixture Replacement Costs",
            payableAcc != null ? payableAcc.getId() : 2L,
            payableAcc != null ? payableAcc.getName() : "Fixture Vendor Payables",
            "Bill for Purchase Order " + po.getPoNumber() + " - LED replacements"
        );

        VendorBill savedBill = vendorBillRepository.save(bill);

        // Generate Purchase Journal Entry (Debit Expense, Credit Accounts Payable)
        JournalEntry je = new JournalEntry(
            "JE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "PURCHASE",
            LocalDate.now(),
            savedBill.getBillNumber(),
            "Vendor Bill " + savedBill.getBillNumber() + " - " + po.getVendorName()
        );

        je.addItem(new JournalItem(
            bill.getExpenseAccountId(),
            expenseAcc != null ? expenseAcc.getCode() : "5020",
            bill.getExpenseAccountName(),
            null,
            null,
            po.getTotalAmount(),
            0.0,
            "LED Fixture Expense"
        ));

        je.addItem(new JournalItem(
            bill.getPayableAccountId(),
            payableAcc != null ? payableAcc.getCode() : "2020",
            bill.getPayableAccountName(),
            null,
            null,
            0.0,
            po.getTotalAmount(),
            "Payable to " + po.getVendorName()
        ));

        JournalEntry savedJe = postJournalEntry(je);
        savedBill.setJournalEntryId(savedJe.getId());
        vendorBillRepository.save(savedBill);

        po.setStatus("BILLED");
        po.setVendorBillId(savedBill.getId());
        purchaseOrderRepository.save(po);

        return savedBill;
    }

    @Transactional
    public Payment payVendorBill(Long billId, Long bankAccountId) {
        VendorBill bill = vendorBillRepository.findById(billId)
            .orElseThrow(() -> new IllegalArgumentException("Vendor Bill not found: " + billId));

        if ("PAID".equalsIgnoreCase(bill.getStatus())) {
            throw new IllegalStateException("Bill " + bill.getBillNumber() + " is already paid.");
        }

        Account bankAcc = bankAccountId != null ? accountRepository.findById(bankAccountId).orElse(null) : null;
        if (bankAcc == null) {
            bankAcc = accountRepository.findByName("Cash/Bank")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "ASSET".equals(a.getAccountType())).findFirst().orElse(null));
        }

        double amountToPay = bill.getTotalAmount() - bill.getAmountPaid();
        String payNum = "PAY-OUT-" + System.currentTimeMillis() % 100000;
        Payment payment = new Payment(
            payNum,
            "PAYMENT",
            bill.getVendorId(),
            bill.getVendorName(),
            LocalDate.now(),
            amountToPay,
            bankAcc != null ? bankAcc.getId() : 1L,
            bankAcc != null ? bankAcc.getName() : "Cash/Bank",
            "VENDOR_BILL",
            bill.getId(),
            "Payment for bill " + bill.getBillNumber()
        );

        // Journal Entry for Outbound Payment: Debit Payables, Credit Bank
        JournalEntry je = new JournalEntry(
            "JE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "BANK",
            LocalDate.now(),
            payNum,
            "Bank payment for Bill " + bill.getBillNumber()
        );

        je.addItem(new JournalItem(
            bill.getPayableAccountId(),
            "2020",
            bill.getPayableAccountName(),
            null,
            null,
            amountToPay,
            0.0,
            "Settlement of Vendor Payable"
        ));

        je.addItem(new JournalItem(
            bankAcc != null ? bankAcc.getId() : 1L,
            bankAcc != null ? bankAcc.getCode() : "1010",
            bankAcc != null ? bankAcc.getName() : "Cash/Bank",
            null,
            null,
            0.0,
            amountToPay,
            "Disbursement via Bank"
        ));

        JournalEntry savedJe = postJournalEntry(je);
        payment.setJournalEntryId(savedJe.getId());
        Payment savedPayment = paymentRepository.save(payment);

        bill.setAmountPaid(bill.getTotalAmount());
        bill.setStatus("PAID");
        vendorBillRepository.save(bill);

        return savedPayment;
    }

    @Transactional
    public CustomerInvoice convertSoToCustomerInvoice(Long soId) {
        SalesOrder so = salesOrderRepository.findById(soId)
            .orElseThrow(() -> new IllegalArgumentException("Sales Order not found: " + soId));

        if ("INVOICED".equalsIgnoreCase(so.getStatus())) {
            throw new IllegalStateException("Sales Order " + so.getSoNumber() + " is already invoiced.");
        }

        Account arAcc = accountRepository.findByName("Accounts Receivable")
            .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "ASSET".equals(a.getAccountType())).findFirst().orElse(null));
        Account revAcc = accountRepository.findByName("EV Charging Revenue")
            .orElseGet(() -> accountRepository.findByName("Advertising Space Rental Revenue")
            .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "INCOME".equals(a.getAccountType())).findFirst().orElse(null)));

        String invNum = "INV-" + System.currentTimeMillis() % 100000;
        CustomerInvoice inv = new CustomerInvoice(
            invNum,
            so.getId(),
            null,
            so.getCustomerId(),
            so.getCustomerName(),
            LocalDate.now(),
            LocalDate.now().plusDays(15),
            so.getTotalAmount(),
            arAcc != null ? arAcc.getId() : 1L,
            arAcc != null ? arAcc.getName() : "Accounts Receivable",
            revAcc != null ? revAcc.getId() : 3L,
            revAcc != null ? revAcc.getName() : "EV Charging Revenue",
            "Customer Invoice for Sales Order " + so.getSoNumber()
        );

        CustomerInvoice savedInv = customerInvoiceRepository.save(inv);

        // Sales Journal Entry: Debit Accounts Receivable, Credit Revenue
        JournalEntry je = new JournalEntry(
            "JE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "SALES",
            LocalDate.now(),
            savedInv.getInvoiceNumber(),
            "Sales invoice " + savedInv.getInvoiceNumber() + " for " + so.getCustomerName()
        );

        je.addItem(new JournalItem(
            inv.getReceivableAccountId(),
            arAcc != null ? arAcc.getCode() : "1200",
            inv.getReceivableAccountName(),
            null,
            null,
            so.getTotalAmount(),
            0.0,
            "Receivable from " + so.getCustomerName()
        ));

        je.addItem(new JournalItem(
            inv.getIncomeAccountId(),
            revAcc != null ? revAcc.getCode() : "4010",
            inv.getIncomeAccountName(),
            null,
            null,
            0.0,
            so.getTotalAmount(),
            "Smart Grid Revenue recognition"
        ));

        JournalEntry savedJe = postJournalEntry(je);
        savedInv.setJournalEntryId(savedJe.getId());
        customerInvoiceRepository.save(savedInv);

        so.setStatus("INVOICED");
        so.setCustomerInvoiceId(savedInv.getId());
        salesOrderRepository.save(so);

        return savedInv;
    }

    @Transactional
    public Payment collectCustomerPayment(Long invoiceId, Long bankAccountId) {
        CustomerInvoice inv = customerInvoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Customer Invoice not found: " + invoiceId));

        if ("PAID".equalsIgnoreCase(inv.getStatus())) {
            throw new IllegalStateException("Invoice " + inv.getInvoiceNumber() + " is already paid.");
        }

        Account bankAcc = bankAccountId != null ? accountRepository.findById(bankAccountId).orElse(null) : null;
        if (bankAcc == null) {
            bankAcc = accountRepository.findByName("Cash/Bank")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "ASSET".equals(a.getAccountType())).findFirst().orElse(null));
        }

        double amountToCollect = inv.getTotalAmount() - inv.getAmountPaid();
        String payNum = "RCPT-" + System.currentTimeMillis() % 100000;
        Payment payment = new Payment(
            payNum,
            "RECEIPT",
            inv.getCustomerId(),
            inv.getCustomerName(),
            LocalDate.now(),
            amountToCollect,
            bankAcc != null ? bankAcc.getId() : 1L,
            bankAcc != null ? bankAcc.getName() : "Cash/Bank",
            "CUSTOMER_INVOICE",
            inv.getId(),
            "Receipt for invoice " + inv.getInvoiceNumber()
        );

        // Bank Journal Entry: Debit Bank, Credit Accounts Receivable
        JournalEntry je = new JournalEntry(
            "JE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
            "BANK",
            LocalDate.now(),
            payNum,
            "Customer receipt for Invoice " + inv.getInvoiceNumber()
        );

        je.addItem(new JournalItem(
            bankAcc != null ? bankAcc.getId() : 1L,
            bankAcc != null ? bankAcc.getCode() : "1010",
            bankAcc != null ? bankAcc.getName() : "Cash/Bank",
            null,
            null,
            amountToCollect,
            0.0,
            "Bank deposit receipt"
        ));

        je.addItem(new JournalItem(
            inv.getReceivableAccountId(),
            "1200",
            inv.getReceivableAccountName(),
            null,
            null,
            0.0,
            amountToCollect,
            "Receivable clearance for " + inv.getCustomerName()
        ));

        JournalEntry savedJe = postJournalEntry(je);
        payment.setJournalEntryId(savedJe.getId());
        Payment savedPayment = paymentRepository.save(payment);

        inv.setAmountPaid(inv.getTotalAmount());
        inv.setStatus("PAID");
        customerInvoiceRepository.save(inv);

        return savedPayment;
    }
}
