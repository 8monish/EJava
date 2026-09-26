package com.monish.springbootapp.dto;

public class StopChargeRequest {
    private Long sessionId;
    private Double kwhDelivered;
    private Boolean createInvoice = true;

    public StopChargeRequest() {}

    public StopChargeRequest(Long sessionId, Double kwhDelivered, Boolean createInvoice) {
        this.sessionId = sessionId;
        this.kwhDelivered = kwhDelivered;
        this.createInvoice = createInvoice != null ? createInvoice : true;
    }

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Double getKwhDelivered() {
        return kwhDelivered;
    }

    public void setKwhDelivered(Double kwhDelivered) {
        this.kwhDelivered = kwhDelivered;
    }

    public Boolean getCreateInvoice() {
        return createInvoice;
    }

    public void setCreateInvoice(Boolean createInvoice) {
        this.createInvoice = createInvoice;
    }
}
