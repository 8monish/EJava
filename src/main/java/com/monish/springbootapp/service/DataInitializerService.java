package com.monish.springbootapp.service;

import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class DataInitializerService implements CommandLineRunner {

    private final AccountRepository accountRepository;
    private final JournalRepository journalRepository;
    private final JournalEntryRepository journalEntryRepository;
    private final ContactRepository contactRepository;
    private final ProductRepository productRepository;
    private final AnalyticAccountRepository analyticAccountRepository;
    private final SmartPoleUnitRepository poleRepository;
    private final ChargingSessionRepository sessionRepository;
    private final BudgetRepository budgetRepository;
    private final PurchaseOrderRepository purchaseOrderRepository;
    private final SalesOrderRepository salesOrderRepository;
    private final AccountingService accountingService;

    public DataInitializerService(AccountRepository accountRepository,
                                  JournalRepository journalRepository,
                                  JournalEntryRepository journalEntryRepository,
                                  ContactRepository contactRepository,
                                  ProductRepository productRepository,
                                  AnalyticAccountRepository analyticAccountRepository,
                                  SmartPoleUnitRepository poleRepository,
                                  ChargingSessionRepository sessionRepository,
                                  BudgetRepository budgetRepository,
                                  PurchaseOrderRepository purchaseOrderRepository,
                                  SalesOrderRepository salesOrderRepository,
                                  AccountingService accountingService) {
        this.accountRepository = accountRepository;
        this.journalRepository = journalRepository;
        this.journalEntryRepository = journalEntryRepository;
        this.contactRepository = contactRepository;
        this.productRepository = productRepository;
        this.analyticAccountRepository = analyticAccountRepository;
        this.poleRepository = poleRepository;
        this.sessionRepository = sessionRepository;
        this.budgetRepository = budgetRepository;
        this.purchaseOrderRepository = purchaseOrderRepository;
        this.salesOrderRepository = salesOrderRepository;
        this.accountingService = accountingService;
    }

    @Override
    @Transactional
    public void run(String... args) {
        initChartOfAccounts();
        initJournals();
        initContacts();
        initProducts();
        AnalyticAccount downtownZone = initAnalyticAccounts();
        initSmartPoles(downtownZone);
        initBudget(downtownZone);
        initSampleTransactions();
    }

    private void initChartOfAccounts() {
        if (accountRepository.count() == 0) {
            // Assets
            accountRepository.save(new Account("1500", "Smart Pole Grid Infrastructure", "ASSET", 1250000.0, "Fixed municipal smart lighting and charger capital infrastructure"));
            accountRepository.save(new Account("1010", "Cash/Bank", "ASSET", 185400.0, "Municipal operating treasury bank account"));
            accountRepository.save(new Account("1200", "Accounts Receivable", "ASSET", 14250.0, "Outstanding billings from parking operators and fleet EV drivers"));

            // Liabilities
            accountRepository.save(new Account("2010", "Power Utility Creditors", "LIABILITY", 32100.0, "Accrued liabilities owed to Metropolitan Grid & Power Utility"));
            accountRepository.save(new Account("2020", "Fixture Vendor Payables", "LIABILITY", 18750.0, "Unpaid supplier invoices for LED replacements and IoT components"));

            // Income
            accountRepository.save(new Account("4010", "EV Charging Revenue", "INCOME", 48600.0, "Revenues collected from public and commercial fleet EV charging"));
            accountRepository.save(new Account("4020", "Advertising Space Rental Revenue", "INCOME", 36000.0, "Monthly rental fees for smart pole LED display panels"));

            // Expenses
            accountRepository.save(new Account("5010", "Wholesale Electricity Expenses", "EXPENSE", 28400.0, "Bulk grid electricity purchases for LED poles and charging stations"));
            accountRepository.save(new Account("5020", "Fixture Replacement Costs", "EXPENSE", 15800.0, "Maintenance, replacement LED drivers, optic lenses and labor"));
        }
    }

    private void initJournals() {
        if (journalRepository.count() == 0) {
            journalRepository.save(new Journal("SALES", "Sales Journal", "SALES"));
            journalRepository.save(new Journal("PURCHASE", "Purchase Journal", "PURCHASE"));
            journalRepository.save(new Journal("CASH", "Cash Journal", "CASH"));
            journalRepository.save(new Journal("BANK", "Bank Journal", "BANK"));
        }
    }

    private void initContacts() {
        if (contactRepository.count() == 0) {
            contactRepository.save(new Contact(
                "Metro Fleet Parking Operations Ltd.",
                "CUSTOMER",
                "Commercial Parking Operator",
                "operations@metropark.city",
                "+1 (555) 432-8811",
                "450 Commerce Expressway, Downtown Gateway"
            ));

            contactRepository.save(new Contact(
                "City Center Municipal Garages",
                "CUSTOMER",
                "Commercial Parking Operator",
                "dispatch@citycentergarages.gov",
                "+1 (555) 321-7744",
                "12 Civic Center Plaza, Sector 1"
            ));

            contactRepository.save(new Contact(
                "LumiTech Smart Optics & Fixtures Inc.",
                "VENDOR",
                "LED Module Vendor",
                "orders@lumitechoptics.com",
                "+1 (555) 882-9900",
                "88 Photonics Parkway, Tech Corridor"
            ));

            contactRepository.save(new Contact(
                "Metropolitan Grid & Power Utility",
                "VENDOR",
                "Power Utility",
                "corporate.accounts@metropowerutility.gov",
                "+1 (555) 990-1122",
                "100 Gridiron Boulevard, Power Substation 4"
            ));
        }
    }

    private void initProducts() {
        if (productRepository.count() == 0) {
            productRepository.save(new Product(
                "SRV-EV-KWH",
                "Streetlight EV Charging kWh",
                "SERVICE",
                0.35,
                "kWh",
                "Public smart light pole EV charging tariff per kilowatt-hour dispensed"
            ));

            productRepository.save(new Product(
                "SRV-POLE-AD",
                "Smart Pole Advertising Space",
                "SERVICE",
                1200.0,
                "Month",
                "Full HD digital out-of-home (DOOH) screen slot per pole per month"
            ));

            productRepository.save(new Product(
                "GDS-LED-FIX",
                "LED Fixture",
                "GOODS",
                250.0,
                "Unit",
                "High-efficiency 180lm/W IoT-dimmable retrofitted LED streetlight fixture"
            ));
        }
    }

    private AnalyticAccount initAnalyticAccounts() {
        if (analyticAccountRepository.count() == 0) {
            AnalyticAccount dt = analyticAccountRepository.save(new AnalyticAccount(
                "AN-DT-Z1",
                "Downtown Smart Pole Zone 1",
                "LIGHTING_SECTOR",
                24,
                "High-density urban commercial center with integrated EV chargers and 4K surveillance"
            ));

            analyticAccountRepository.save(new AnalyticAccount(
                "AN-MID-Z2",
                "Midtown Business Corridor Zone 2",
                "LIGHTING_SECTOR",
                18,
                "Mixed retail and financial office boulevard with automated schedule dimming"
            ));

            analyticAccountRepository.save(new AnalyticAccount(
                "AN-HAR-Z3",
                "Harbor Waterfront Zone 3",
                "LIGHTING_SECTOR",
                12,
                "Public marina walkway with high aesthetic ambient light tracking"
            ));
            return dt;
        }
        return analyticAccountRepository.findByCode("AN-DT-Z1").orElse(null);
    }

    private void initSmartPoles(AnalyticAccount downtownZone) {
        if (poleRepository.count() == 0) {
            Long zoneId = downtownZone != null ? downtownZone.getId() : 1L;

            SmartPoleUnit pole1 = new SmartPoleUnit(
                "POLE-DT-001",
                "Downtown Grand Plaza - Pole #1",
                "5th Ave & Market St",
                "Downtown Smart Pole Zone 1",
                zoneId,
                40.7128,
                -74.0060,
                22.0,
                0.35
            );
            pole1.setLedBrightness(85);
            pole1.setAmbientLightLevel(18.0);
            pole1.setCameraStatus("ONLINE");
            pole1.setCameraStreamUrl("CAM-FEED-001-4K-TRAFFIC");
            poleRepository.save(pole1);

            SmartPoleUnit pole2 = new SmartPoleUnit(
                "POLE-DT-002",
                "Civic Center Plaza - Pole #2",
                "Civic Center Way & 3rd",
                "Downtown Smart Pole Zone 1",
                zoneId,
                40.7135,
                -74.0075,
                50.0,
                0.38
            );
            pole2.setLedBrightness(90);
            pole2.setAmbientLightLevel(12.0);
            pole2.setCameraStatus("ONLINE");
            pole2.setCameraStreamUrl("CAM-FEED-002-4K-LPR");
            poleRepository.save(pole2);

            SmartPoleUnit pole3 = new SmartPoleUnit(
                "POLE-DT-003",
                "Financial District Hub - Pole #3",
                "Wall St & Broad Ave",
                "Downtown Smart Pole Zone 1",
                zoneId,
                40.7112,
                -74.0089,
                22.0,
                0.35
            );
            pole3.setLedBrightness(80);
            pole3.setAmbientLightLevel(22.0);
            pole3.setChargerStatus("CHARGING");
            pole3.setCameraStatus("RECORDING");
            pole3.setCameraStreamUrl("CAM-FEED-003-4K-SECURITY");
            SmartPoleUnit savedPole3 = poleRepository.save(pole3);

            // Create initial active session on Pole 3
            Contact metroFleet = contactRepository.findAll().stream().filter(c -> "CUSTOMER".equals(c.getContactType())).findFirst().orElse(null);
            ChargingSession activeSession = new ChargingSession(
                savedPole3.getPoleCode(),
                savedPole3.getId(),
                "Metro Fleet Delivery Van #304",
                metroFleet != null ? metroFleet.getId() : 1L,
                metroFleet != null ? metroFleet.getName() : "Metro Fleet Parking Operations",
                LocalDateTime.now().minusMinutes(35),
                savedPole3.getCurrentKwhRate()
            );
            activeSession.setKwhDelivered(12.8);
            activeSession.setDurationMinutes(35L);
            activeSession.setTotalAmount(Math.round(12.8 * 0.35 * 100.0) / 100.0);
            ChargingSession savedSession = sessionRepository.save(activeSession);

            savedPole3.setActiveSessionId(savedSession.getId());
            poleRepository.save(savedPole3);

            SmartPoleUnit pole4 = new SmartPoleUnit(
                "POLE-MID-101",
                "Midtown Avenue - Pole #101",
                "7th Ave & 42nd St",
                "Midtown Business Corridor Zone 2",
                zoneId + 1,
                40.7580,
                -73.9855,
                22.0,
                0.35
            );
            pole4.setLedBrightness(75);
            pole4.setAmbientLightLevel(30.0);
            poleRepository.save(pole4);
        }
    }

    private void initBudget(AnalyticAccount downtownZone) {
        if (budgetRepository.count() == 0) {
            Long zoneId = downtownZone != null ? downtownZone.getId() : 1L;
            String zoneName = downtownZone != null ? downtownZone.getName() : "Downtown Smart Pole Zone 1";

            Budget budget = new Budget(
                "FY2026 Downtown Smart Lighting & EV Grid Budget",
                "2026",
                zoneId,
                zoneName,
                LocalDate.of(2026, 1, 1),
                LocalDate.of(2026, 12, 31)
            );

            Account evRev = accountRepository.findByName("EV Charging Revenue").orElse(null);
            Account adRev = accountRepository.findByName("Advertising Space Rental Revenue").orElse(null);
            Account elecExp = accountRepository.findByName("Wholesale Electricity Expenses").orElse(null);
            Account fixExp = accountRepository.findByName("Fixture Replacement Costs").orElse(null);

            if (evRev != null) {
                budget.addLine(new BudgetLine(evRev.getId(), evRev.getCode(), evRev.getName(), "INCOME", 65000.0));
            }
            if (adRev != null) {
                budget.addLine(new BudgetLine(adRev.getId(), adRev.getCode(), adRev.getName(), "INCOME", 45000.0));
            }
            if (elecExp != null) {
                budget.addLine(new BudgetLine(elecExp.getId(), elecExp.getCode(), elecExp.getName(), "EXPENSE", 35000.0));
            }
            if (fixExp != null) {
                budget.addLine(new BudgetLine(fixExp.getId(), fixExp.getCode(), fixExp.getName(), "EXPENSE", 20000.0));
            }

            budgetRepository.save(budget);
        }
    }

    private void initSampleTransactions() {
        if (purchaseOrderRepository.count() == 0) {
            Contact vendor = contactRepository.findAll().stream().filter(c -> "VENDOR".equals(c.getContactType())).findFirst().orElse(null);
            Product ledFix = productRepository.findByCode("GDS-LED-FIX").orElse(null);

            if (vendor != null && ledFix != null) {
                PurchaseOrder po = new PurchaseOrder(
                    "PO-2026-001",
                    vendor.getId(),
                    vendor.getName(),
                    LocalDate.now().minusDays(5),
                    "Municipal Q3 high-efficiency replacement LED fixtures order"
                );
                po.addItem(new PurchaseOrderItem(ledFix.getId(), ledFix.getCode(), ledFix.getName(), 20.0, ledFix.getUnitPrice()));
                po.setStatus("CONFIRMED");
                purchaseOrderRepository.save(po);
            }
        }

        if (salesOrderRepository.count() == 0) {
            Contact customer = contactRepository.findAll().stream().filter(c -> "CUSTOMER".equals(c.getContactType())).findFirst().orElse(null);
            Product evKwh = productRepository.findByCode("SRV-EV-KWH").orElse(null);

            if (customer != null && evKwh != null) {
                SalesOrder so = new SalesOrder(
                    "SO-2026-001",
                    customer.getId(),
                    customer.getName(),
                    LocalDate.now().minusDays(3),
                    "Monthly fleet charging quota contract for 2,500 kWh"
                );
                so.addItem(new SalesOrderItem(evKwh.getId(), evKwh.getCode(), evKwh.getName(), 2500.0, evKwh.getUnitPrice()));
                so.setStatus("CONFIRMED");
                salesOrderRepository.save(so);
            }
        }
    }
}
