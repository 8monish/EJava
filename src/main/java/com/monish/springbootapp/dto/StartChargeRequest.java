package com.monish.springbootapp.dto;

public class StartChargeRequest {
    private String poleCode;
    private String driverOrFleetName;
    private Long contactId;

    public StartChargeRequest() {}

    public StartChargeRequest(String poleCode, String driverOrFleetName, Long contactId) {
        this.poleCode = poleCode;
        this.driverOrFleetName = driverOrFleetName;
        this.contactId = contactId;
    }

    public String getPoleCode() {
        return poleCode;
    }

    public void setPoleCode(String poleCode) {
        this.poleCode = poleCode;
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
}
