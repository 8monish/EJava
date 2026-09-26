package com.monish.springbootapp.service;

import com.monish.springbootapp.dto.StartChargeRequest;
import com.monish.springbootapp.dto.StopChargeRequest;
import com.monish.springbootapp.model.*;
import com.monish.springbootapp.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class ChargingService {

    private final SmartPoleUnitRepository poleRepository;
    private final ChargingSessionRepository sessionRepository;
    private final ContactRepository contactRepository;
    private final AccountRepository accountRepository;
    private final CustomerInvoiceRepository customerInvoiceRepository;
    private final AccountingService accountingService;

    public ChargingService(SmartPoleUnitRepository poleRepository,
                           ChargingSessionRepository sessionRepository,
                           ContactRepository contactRepository,
                           AccountRepository accountRepository,
                           CustomerInvoiceRepository customerInvoiceRepository,
                           AccountingService accountingService) {
        this.poleRepository = poleRepository;
        this.sessionRepository = sessionRepository;
        this.contactRepository = contactRepository;
        this.accountRepository = accountRepository;
        this.customerInvoiceRepository = customerInvoiceRepository;
        this.accountingService = accountingService;
    }

    public List<ChargingSession> getAllSessions() {
        return sessionRepository.findAll();
    }

    public List<ChargingSession> getActiveSessions() {
        return sessionRepository.findByStatus("ACTIVE");
    }

    @Transactional
    public ChargingSession startCharge(StartChargeRequest request) {
        SmartPoleUnit pole = poleRepository.findByPoleCode(request.getPoleCode())
            .orElseThrow(() -> new IllegalArgumentException("Smart Pole not found: " + request.getPoleCode()));

        if ("CHARGING".equalsIgnoreCase(pole.getChargerStatus()) && pole.getActiveSessionId() != null) {
            throw new IllegalStateException("Pole " + pole.getPoleCode() + " is already occupied by session #" + pole.getActiveSessionId());
        }

        Contact contact = null;
        if (request.getContactId() != null) {
            contact = contactRepository.findById(request.getContactId()).orElse(null);
        }
        if (contact == null) {
            // Find a commercial parking operator or default customer
            contact = contactRepository.findAll().stream()
                .filter(c -> "CUSTOMER".equalsIgnoreCase(c.getContactType()))
                .findFirst()
                .orElse(null);
        }

        String fleetName = (request.getDriverOrFleetName() != null && !request.getDriverOrFleetName().isBlank())
            ? request.getDriverOrFleetName()
            : (contact != null ? contact.getName() + " - Fleet EV" : "Municipal Commercial EV #101");

        ChargingSession session = new ChargingSession(
            pole.getPoleCode(),
            pole.getId(),
            fleetName,
            contact != null ? contact.getId() : null,
            contact != null ? contact.getName() : fleetName,
            LocalDateTime.now(),
            pole.getCurrentKwhRate()
        );

        ChargingSession savedSession = sessionRepository.save(session);

        pole.setChargerStatus("CHARGING");
        pole.setActiveSessionId(savedSession.getId());
        poleRepository.save(pole);

        return savedSession;
    }

    @Transactional
    public ChargingSession stopCharge(StopChargeRequest request) {
        ChargingSession session = sessionRepository.findById(request.getSessionId())
            .orElseThrow(() -> new IllegalArgumentException("Charging session not found: " + request.getSessionId()));

        if ("COMPLETED".equalsIgnoreCase(session.getStatus()) || "INVOICED".equalsIgnoreCase(session.getStatus())) {
            return session;
        }

        LocalDateTime now = LocalDateTime.now();
        session.setEndTime(now);

        long minutes = Duration.between(session.getStartTime(), now).toMinutes();
        if (minutes <= 0) {
            minutes = 45; // Default realistic simulation window
        }
        session.setDurationMinutes(minutes);

        double kwh;
        if (request.getKwhDelivered() != null && request.getKwhDelivered() > 0) {
            kwh = request.getKwhDelivered();
        } else {
            // Realistic simulated charging delivered (approx 15-30 kWh based on duration)
            kwh = Math.round((minutes / 60.0 * 22.0 * 0.85) * 100.0) / 100.0;
            if (kwh <= 0.0) {
                kwh = 18.5; // fallback realistic session
            }
        }
        session.setKwhDelivered(kwh);

        double total = Math.round(kwh * session.getRatePerKwh() * 100.0) / 100.0;
        session.setTotalAmount(total);
        session.setStatus("COMPLETED");

        // Update pole status
        SmartPoleUnit pole = poleRepository.findByPoleCode(session.getPoleCode()).orElse(null);
        if (pole != null) {
            pole.setChargerStatus("AVAILABLE");
            pole.setActiveSessionId(null);
            double prevKwh = pole.getTotalKwhDispensed() != null ? pole.getTotalKwhDispensed() : 0.0;
            pole.setTotalKwhDispensed(Math.round((prevKwh + kwh) * 100.0) / 100.0);
            poleRepository.save(pole);
        }

        // Auto invoice and ledger update
        if (Boolean.TRUE.equals(request.getCreateInvoice())) {
            Account arAcc = accountRepository.findByName("Accounts Receivable")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "ASSET".equals(a.getAccountType())).findFirst().orElse(null));
            Account revAcc = accountRepository.findByName("EV Charging Revenue")
                .orElseGet(() -> accountRepository.findAll().stream().filter(a -> "INCOME".equals(a.getAccountType())).findFirst().orElse(null));

            String invNum = "INV-EV-" + System.currentTimeMillis() % 100000;
            CustomerInvoice invoice = new CustomerInvoice(
                invNum,
                null,
                session.getId(),
                session.getContactId() != null ? session.getContactId() : 1L,
                session.getContactName() != null ? session.getContactName() : session.getDriverOrFleetName(),
                LocalDate.now(),
                LocalDate.now().plusDays(15),
                total,
                arAcc != null ? arAcc.getId() : 1L,
                arAcc != null ? arAcc.getName() : "Accounts Receivable",
                revAcc != null ? revAcc.getId() : 3L,
                revAcc != null ? revAcc.getName() : "EV Charging Revenue",
                "Automated invoice for EV Charging: " + kwh + " kWh at " + session.getPoleCode()
            );

            CustomerInvoice savedInvoice = customerInvoiceRepository.save(invoice);

            // Double Entry Journal Entry (Sales Journal)
            JournalEntry je = new JournalEntry(
                "JE-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                "SALES",
                LocalDate.now(),
                savedInvoice.getInvoiceNumber(),
                "EV Charging Revenue for " + session.getDriverOrFleetName() + " (" + kwh + " kWh)"
            );

            // Debit AR
            je.addItem(new JournalItem(
                invoice.getReceivableAccountId(),
                arAcc != null ? arAcc.getCode() : "1200",
                invoice.getReceivableAccountName(),
                pole != null ? pole.getAnalyticAccountId() : null,
                pole != null ? pole.getZone() : null,
                total,
                0.0,
                "Receivable - EV Charging Session #" + session.getId()
            ));

            // Credit EV Revenue
            je.addItem(new JournalItem(
                invoice.getIncomeAccountId(),
                revAcc != null ? revAcc.getCode() : "4010",
                invoice.getIncomeAccountName(),
                pole != null ? pole.getAnalyticAccountId() : null,
                pole != null ? pole.getZone() : null,
                0.0,
                total,
                "EV Charging Revenue recognized - " + session.getPoleCode()
            ));

            JournalEntry postedJe = accountingService.postJournalEntry(je);
            savedInvoice.setJournalEntryId(postedJe.getId());
            customerInvoiceRepository.save(savedInvoice);

            session.setCustomerInvoiceId(savedInvoice.getId());
            session.setJournalEntryId(postedJe.getId());
            session.setStatus("INVOICED");
        }

        return sessionRepository.save(session);
    }
}
