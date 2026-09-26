package com.monish.springbootapp.Controller;

import com.monish.springbootapp.dto.AmbientSimulationRequest;
import com.monish.springbootapp.dto.DimmingUpdateRequest;
import com.monish.springbootapp.dto.StartChargeRequest;
import com.monish.springbootapp.dto.StopChargeRequest;
import com.monish.springbootapp.model.ChargingSession;
import com.monish.springbootapp.model.SmartPoleUnit;
import com.monish.springbootapp.service.ChargingService;
import com.monish.springbootapp.service.SmartPoleService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/smartpoles")
@CrossOrigin(origins = "*")
public class SmartPoleController {

    private final SmartPoleService smartPoleService;
    private final ChargingService chargingService;

    public SmartPoleController(SmartPoleService smartPoleService, ChargingService chargingService) {
        this.smartPoleService = smartPoleService;
        this.chargingService = chargingService;
    }

    @GetMapping("/units")
    public List<SmartPoleUnit> getAllUnits() {
        return smartPoleService.getAllUnits();
    }

    @GetMapping("/units/{id}")
    public SmartPoleUnit getUnitById(@PathVariable Long id) {
        return smartPoleService.getUnitById(id);
    }

    // Step 1: Add smart pole unit via POST /api/smartpoles/units
    @PostMapping("/units")
    public ResponseEntity<SmartPoleUnit> createUnit(@RequestBody SmartPoleUnit unit) {
        SmartPoleUnit created = smartPoleService.registerUnit(unit);
        return ResponseEntity.ok(created);
    }

    @PutMapping("/units/{poleCode}/dimming")
    public ResponseEntity<SmartPoleUnit> updateDimming(@PathVariable String poleCode, @RequestBody DimmingUpdateRequest req) {
        SmartPoleUnit updated = smartPoleService.updateDimming(poleCode, req);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/units/{poleCode}/tariff")
    public ResponseEntity<SmartPoleUnit> updateTariff(@PathVariable String poleCode, @RequestBody Map<String, Double> payload) {
        Double tariff = payload.get("currentKwhRate");
        if (tariff == null) {
            tariff = payload.get("tariff");
        }
        SmartPoleUnit updated = smartPoleService.updateTariff(poleCode, tariff != null ? tariff : 0.35);
        return ResponseEntity.ok(updated);
    }

    @PutMapping("/units/{poleCode}/camera")
    public ResponseEntity<SmartPoleUnit> updateCamera(@PathVariable String poleCode, @RequestBody Map<String, String> payload) {
        String status = payload.get("cameraStatus");
        SmartPoleUnit updated = smartPoleService.updateCameraStatus(poleCode, status != null ? status : "ONLINE");
        return ResponseEntity.ok(updated);
    }

    // Step 2: Start charging session via POST /api/smartpoles/charge/start
    @PostMapping("/charge/start")
    public ResponseEntity<ChargingSession> startCharge(@RequestBody StartChargeRequest req) {
        ChargingSession session = chargingService.startCharge(req);
        return ResponseEntity.ok(session);
    }

    // Step 2: End session and compute total kWh charges
    @PostMapping("/charge/stop")
    public ResponseEntity<ChargingSession> stopCharge(@RequestBody StopChargeRequest req) {
        ChargingSession session = chargingService.stopCharge(req);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/charge/sessions")
    public List<ChargingSession> getAllSessions() {
        return chargingService.getAllSessions();
    }

    @GetMapping("/charge/sessions/active")
    public List<ChargingSession> getActiveSessions() {
        return chargingService.getActiveSessions();
    }

    // System: Tracks ambient light levels and auto-adjusts light output
    @PostMapping("/simulate/ambient")
    public ResponseEntity<List<SmartPoleUnit>> simulateAmbient(@RequestBody AmbientSimulationRequest req) {
        List<SmartPoleUnit> updated = smartPoleService.autoAdjustByAmbientLux(
            req.getAmbientLightLux() != null ? req.getAmbientLightLux() : 20.0,
            req.getPoleCode()
        );
        return ResponseEntity.ok(updated);
    }
}
