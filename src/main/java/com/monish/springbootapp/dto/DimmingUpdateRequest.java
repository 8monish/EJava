package com.monish.springbootapp.dto;

public class DimmingUpdateRequest {
    private Integer brightness;
    private String dimmingSchedule;
    private Boolean autoDimmingEnabled;

    public DimmingUpdateRequest() {}

    public DimmingUpdateRequest(Integer brightness, String dimmingSchedule, Boolean autoDimmingEnabled) {
        this.brightness = brightness;
        this.dimmingSchedule = dimmingSchedule;
        this.autoDimmingEnabled = autoDimmingEnabled;
    }

    public Integer getBrightness() {
        return brightness;
    }

    public void setBrightness(Integer brightness) {
        this.brightness = brightness;
    }

    public String getDimmingSchedule() {
        return dimmingSchedule;
    }

    public void setDimmingSchedule(String dimmingSchedule) {
        this.dimmingSchedule = dimmingSchedule;
    }

    public Boolean getAutoDimmingEnabled() {
        return autoDimmingEnabled;
    }

    public void setAutoDimmingEnabled(Boolean autoDimmingEnabled) {
        this.autoDimmingEnabled = autoDimmingEnabled;
    }
}
