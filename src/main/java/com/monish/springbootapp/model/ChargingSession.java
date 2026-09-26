package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions")
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String poleCode;

    private Long poleId;

    @Column(nullable = false)
    private String driverOrFleetName;

    private Long contactId;

    private String contactName;

    @Column(nullable = false)
    private LocalDateTime startTime;

    private LocalDateTime endTime;

    private Long durationMinutes = 0L;

    @Column(nullable = false)
    private Double kwhDelivered = 0.0;

    @Column(nullable = false)
    private Double ratePerKwh = 0.35;

    @Column(nullable = false)
    private Double totalAmount = 0.0;

    @Column(nullable = false)
    private String status = "ACTIVE"; // ACTIVE, COMPLETED, INVOICED

    private Long customerInvoiceId;

    private Long journalEntryId;

    public ChargingSession() {
    }

    public ChargingSession(String poleCode, Long poleId, String driverOrFleetName, Long contactId,
                           String contactName, LocalDateTime startTime, Double ratePerKwh) {
        this.poleCode = poleCode;
        this.poleId = poleId;
        this.driverOrFleetName = driverOrFleetName;
        this.contactId = contactId;
        this.contactName = contactName;
        this.startTime = startTime;
        this.ratePerKwh = ratePerKwh;
        this.kwhDelivered = 0.0;
        this.totalAmount = 0.0;
        this.status = "ACTIVE";
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getPoleCode() {
        return poleCode;
    }

    public void setPoleCode(String poleCode) {
        this.poleCode = poleCode;
    }

    public Long getPoleId() {
        return poleId;
    }

    public void setPoleId(Long poleId) {
        this.poleId = poleId;
    }

    public String getDriverOrFleetName() {
        return driverOrFleetName;
    }

    public void setDriverOrFleetName(String driverOrFleetName) {
        this.driverOrFleetName = driverOrFleetName;
    }

    public Long getContactId() {
        return contactId;
    }

    public void setContactId(Long contactId) {
        this.contactId = contactId;
    }

    public String getContactName() {
        return contactName;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Long getDurationMinutes() {
        return durationMinutes;
    }

    public void setDurationMinutes(Long durationMinutes) {
        this.durationMinutes = durationMinutes;
    }

    public Double getKwhDelivered() {
        return kwhDelivered;
    }

    public void setKwhDelivered(Double kwhDelivered) {
        this.kwhDelivered = kwhDelivered;
    }

    public Double getRatePerKwh() {
        return ratePerKwh;
    }

    public void setRatePerKwh(Double ratePerKwh) {
        this.ratePerKwh = ratePerKwh;
    }

    public Double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(Double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Long getCustomerInvoiceId() {
        return customerInvoiceId;
    }

    public void setCustomerInvoiceId(Long customerInvoiceId) {
        this.customerInvoiceId = customerInvoiceId;
    }

    public Long getJournalEntryId() {
        return journalEntryId;
    }

    public void setJournalEntryId(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }
}
