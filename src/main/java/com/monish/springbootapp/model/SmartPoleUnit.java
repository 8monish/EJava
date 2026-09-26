package com.monish.springbootapp.model;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "smart_poles")
public class SmartPoleUnit {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String poleCode;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String location;

    @Column(nullable = false)
    private String zone;

    private Long analyticAccountId;

    private Double latitude;
    private Double longitude;

    @Column(nullable = false)
    private Integer ledBrightness = 80; // 0 to 100%

    @Column(length = 500)
    private String dimmingSchedule = "Adaptive: Dusk-22:00 (100%), 22:00-05:00 (45%), 05:00-Dawn (70%)";

    @Column(nullable = false)
    private Double ambientLightLevel = 25.0; // Lux (0 to 1000)

    @Column(nullable = false)
    private Boolean autoDimmingEnabled = true;

    @Column(nullable = false)
    private String chargerStatus = "AVAILABLE"; // AVAILABLE, CHARGING, OFFLINE, FAULT

    @Column(nullable = false)
    private Double chargerPowerKw = 22.0;

    @Column(nullable = false)
    private Double currentKwhRate = 0.35; // $ / kWh

    @Column(nullable = false)
    private String cameraStatus = "ONLINE"; // ONLINE, RECORDING, OFFLINE

    private String cameraStreamUrl = "CAM-FEED-LPR-4K";

    private Long activeSessionId;

    private Double totalKwhDispensed = 0.0;

    private LocalDate lastMaintenanceDate = LocalDate.now();

    public SmartPoleUnit() {
    }

    public SmartPoleUnit(String poleCode, String name, String location, String zone, Long analyticAccountId,
                         Double latitude, Double longitude, Double chargerPowerKw, Double currentKwhRate) {
        this.poleCode = poleCode;
        this.name = name;
        this.location = location;
        this.zone = zone;
        this.analyticAccountId = analyticAccountId;
        this.latitude = latitude;
        this.longitude = longitude;
        this.chargerPowerKw = chargerPowerKw;
        this.currentKwhRate = currentKwhRate;
        this.ledBrightness = 80;
        this.ambientLightLevel = 25.0;
        this.autoDimmingEnabled = true;
        this.chargerStatus = "AVAILABLE";
        this.cameraStatus = "ONLINE";
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLocation() {
        return location;
    }

    public void setLocation(String location) {
        this.location = location;
    }

    public String getZone() {
        return zone;
    }

    public void setZone(String zone) {
        this.zone = zone;
    }

    public Long getAnalyticAccountId() {
        return analyticAccountId;
    }

    public void setAnalyticAccountId(Long analyticAccountId) {
        this.analyticAccountId = analyticAccountId;
    }

    public Double getLatitude() {
        return latitude;
    }

    public void setLatitude(Double latitude) {
        this.latitude = latitude;
    }

    public Double getLongitude() {
        return longitude;
    }

    public void setLongitude(Double longitude) {
        this.longitude = longitude;
    }

    public Integer getLedBrightness() {
        return ledBrightness;
    }

    public void setLedBrightness(Integer ledBrightness) {
        this.ledBrightness = ledBrightness;
    }

    public String getDimmingSchedule() {
        return dimmingSchedule;
    }

    public void setDimmingSchedule(String dimmingSchedule) {
        this.dimmingSchedule = dimmingSchedule;
    }

    public Double getAmbientLightLevel() {
        return ambientLightLevel;
    }

    public void setAmbientLightLevel(Double ambientLightLevel) {
        this.ambientLightLevel = ambientLightLevel;
    }

    public Boolean getAutoDimmingEnabled() {
        return autoDimmingEnabled;
    }

    public void setAutoDimmingEnabled(Boolean autoDimmingEnabled) {
        this.autoDimmingEnabled = autoDimmingEnabled;
    }

    public String getChargerStatus() {
        return chargerStatus;
    }

    public void setChargerStatus(String chargerStatus) {
        this.chargerStatus = chargerStatus;
    }

    public Double getChargerPowerKw() {
        return chargerPowerKw;
    }

    public void setChargerPowerKw(Double chargerPowerKw) {
        this.chargerPowerKw = chargerPowerKw;
    }

    public Double getCurrentKwhRate() {
        return currentKwhRate;
    }

    public void setCurrentKwhRate(Double currentKwhRate) {
        this.currentKwhRate = currentKwhRate;
    }

    public String getCameraStatus() {
        return cameraStatus;
    }

    public void setCameraStatus(String cameraStatus) {
        this.cameraStatus = cameraStatus;
    }

    public String getCameraStreamUrl() {
        return cameraStreamUrl;
    }

    public void setCameraStreamUrl(String cameraStreamUrl) {
        this.cameraStreamUrl = cameraStreamUrl;
    }

    public Long getActiveSessionId() {
        return activeSessionId;
    }

    public void setActiveSessionId(Long activeSessionId) {
        this.activeSessionId = activeSessionId;
    }

    public Double getTotalKwhDispensed() {
        return totalKwhDispensed;
    }

    public void setTotalKwhDispensed(Double totalKwhDispensed) {
        this.totalKwhDispensed = totalKwhDispensed;
    }

    public LocalDate getLastMaintenanceDate() {
        return lastMaintenanceDate;
    }

    public void setLastMaintenanceDate(LocalDate lastMaintenanceDate) {
        this.lastMaintenanceDate = lastMaintenanceDate;
    }
}
