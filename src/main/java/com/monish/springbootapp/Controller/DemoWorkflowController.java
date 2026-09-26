package com.monish.springbootapp.Controller;

import com.monish.springbootapp.dto.*;
import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import com.monish.springbootapp.service.AccountingService;
import com.monish.springbootapp.service.ChargingService;
import com.monish.springbootapp.service.ReportingService;
import com.monish.springbootapp.service.SmartPoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/demo")
@CrossOrigin(origins = "*")
public class DemoWorkflowController {

    private final SmartPoleService smartPoleService;
    private final ChargingService chargingService;
    private final AccountingService accountingService;
    private final ReportingService reportingService;
    private final SmartPoleUnitRepository poleRepository;
    private final ContactRepository contactRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final ProductRepository productRepository;

    public DemoWorkflowController(SmartPoleService smartPoleService,
                                  ChargingService chargingService,
                                  AccountingService accountingService,
                                  ReportingService reportingService,
                                  SmartPoleUnitRepository poleRepository,
                                  ContactRepository contactRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  SalesOrderRepository salesOrderRepository,
                                  ProductRepository productRepository) {
        this.smartPoleService = smartPoleService;
        this.chargingService = chargingService;
        this.accountingService = accountingService;
        this.reportingService = reportingService;
        this.poleRepository = poleRepository;
        this.contactRepository = contactRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.productRepository = productRepository;
    }

    @PostMapping("/run-use-case")
    @Transactional
    public ResponseEntity<Map<String, Object>> runFullUseCase() {
        Map<String, Object> result = new HashMap<>();

        // Step 1: Create Master Data: Add smart pole unit, register fleet clients, CoA
        String demoCode = "POLE-DEMO-" + System.currentTimeMillis() % 10000;
        SmartPoleUnit demoUnit = new SmartPoleUnit(
            demoCode,
            "Innovation Hub - " + demoCode,
            "Tech Boulevard & 10th Ave",
            "Downtown Smart Pole Zone 1",
            1L,
            40.7142,
            -74.0045,
            22.0,
            0.36
        );
        SmartPoleUnit createdPole = smartPoleService.registerUnit(demoUnit);
        result.put("step1_masterData", Map.of(
            "status", "SUCCESS",
            "createdPoleCode", createdPole.getPoleCode(),
            "location", createdPole.getLocation(),
            "tariff", createdPole.getCurrentKwhRate()
        ));

        // Step 2: Process Charging Session: Start charging & stop charging session
        StartChargeRequest startReq = new StartChargeRequest(
            createdPole.getPoleCode(),
            "City Transit EV Shuttle #501",
            1L
        );
        ChargingSession activeSession = chargingService.startCharge(startReq);

        StopChargeRequest stopReq = new StopChargeRequest(
            activeSession.getId(),
            24.5, // 24.5 kWh delivered
            true // Auto generate customer invoice and double-entry ledger
        );
        ChargingSession completedSession = chargingService.stopCharge(stopReq);

        result.put("step2_chargingSession", Map.of(
            "status", "SUCCESS",
            "sessionId", completedSession.getId(),
            "kwhDelivered", completedSession.getKwhDelivered(),
            "ratePerKwh", completedSession.getRatePerKwh(),
            "totalCharged", completedSession.getTotalAmount(),
            "customerInvoiceId", completedSession.getCustomerInvoiceId(),
            "journalEntryId", completedSession.getJournalEntryId()
        ));

        // Step 3: Execute Invoicing: Issue Customer Invoice to parking operator; process Vendor Bill for LED replacements
        // 3a. Customer Invoice was generated from the charging session or Sales Order
        // 3b. Process Vendor Bill for LED replacements
        Contact vendor = contactRepository.findAll().stream()
            .filter(c -> "VENDOR".equalsIgnoreCase(c.getContactType()))
            .findFirst()
            .orElse(null);

        Product ledFix = productRepository.findByCode("GDS-LED-FIX").orElse(null);

        PurchaseOrder po = new PurchaseOrder(
            "PO-DEMO-" + System.currentTimeMillis() % 10000,
            vendor != null ? vendor.getId() : 1L,
            vendor != null ? vendor.getName() : "LumiTech Smart Optics Inc.",
            LocalDate.now(),
            "Automated replacement PO for high-efficiency LED modules"
        );
        po.addItem(new PurchaseOrderItem(
            ledFix != null ? ledFix.getId() : 1L,
            ledFix != null ? ledFix.getCode() : "GDS-LED-FIX",
            ledFix != null ? ledFix.getName() : "LED Fixture",
            10.0,
            ledFix != null ? ledFix.getUnitPrice() : 250.0
        ));
        PurchaseOrder savedPo = purchaseOrderRepository.save(po);

        // Convert PO into Vendor Bill & issue Bank Payment
        VendorBill vendorBill = accountingService.convertPoToVendorBill(savedPo.getId());
        Payment vendorPayment = accountingService.payVendorBill(vendorBill.getId(), null);

        result.put("step3_invoicing", Map.of(
            "status", "SUCCESS",
            "vendorBillNumber", vendorBill.getBillNumber(),
            "vendorBillAmount", vendorBill.getTotalAmount(),
            "vendorPaymentNumber", vendorPayment.getPaymentNumber(),
            "vendorPaymentAmount", vendorPayment.getAmount()
        ));

        // Step 4: Generate Reports: Run Smart Lighting P&L and Pole Grid Budget Reports
        ProfitAndLossReport pnl = reportingService.generateProfitAndLoss();
        BudgetPerformanceReport budgetReport = reportingService.generateBudgetReport(null);
        BalanceSheetReport balanceSheet = reportingService.generateBalanceSheet();

        result.put("step4_reports", Map.of(
            "status", "SUCCESS",
            "pnlTotalRevenue", pnl.getTotalRevenue(),
            "pnlTotalExpense", pnl.getTotalExpense(),
            "pnlNetOperatingSurplus", pnl.getNetOperatingSurplus(),
            "budgetPlannedRevenue", budgetReport.getTotalPlannedRevenue(),
            "budgetActualRevenue", budgetReport.getTotalActualRevenue(),
            "budgetPerformanceRatio", budgetReport.getPerformanceRatio() + "%",
            "balanceSheetAssets", balanceSheet.getTotalAssets(),
            "balanceSheetLiabilitiesAndEquity", balanceSheet.getTotalLiabilitiesAndEquity(),
            "balanceSheetBalanced", balanceSheet.getBalanced()
        ));

        return ResponseEntity.ok(result);
    }
}
