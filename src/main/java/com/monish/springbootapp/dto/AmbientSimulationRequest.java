package com.monish.springbootapp.dto;

public class AmbientSimulationRequest {
    private Double ambientLightLux;
    private String poleCode;

    public AmbientSimulationRequest() {}

    public AmbientSimulationRequest(Double ambientLightLux, String poleCode) {
        this.ambientLightLux = ambientLightLux;
        this.poleCode = poleCode;
    }

    public Double getAmbientLightLux() {
        return ambientLightLux;
    }

    public void setAmbientLightLux(Double ambientLightLux) {
        this.ambientLightLux = ambientLightLux;
    }

    public String getPoleCode() {
        return poleCode;
    }

    public void setPoleCode(String poleCode) {
        this.poleCode = poleCode;
    }
}
