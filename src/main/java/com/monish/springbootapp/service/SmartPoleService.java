package com.monish.springbootapp.service;

import com.monish.springbootapp.dto.DimmingUpdateRequest;
import com.monish.springbootapp.model.SmartPoleUnit;
import com.monish.springbootapp.repository.SmartPoleUnitRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SmartPoleService {

    private final SmartPoleUnitRepository poleRepository;

    public SmartPoleService(SmartPoleUnitRepository poleRepository) {
        this.poleRepository = poleRepository;
    }

    public List<SmartPoleUnit> getAllUnits() {
        return poleRepository.findAll();
    }

    public SmartPoleUnit getUnitById(Long id) {
        return poleRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Smart Pole not found with id: " + id));
    }

    public SmartPoleUnit getUnitByCode(String poleCode) {
        return poleRepository.findByPoleCode(poleCode)
            .orElseThrow(() -> new IllegalArgumentException("Smart Pole not found with code: " + poleCode));
    }

    @Transactional
    public SmartPoleUnit registerUnit(SmartPoleUnit unit) {
        if (poleRepository.existsByPoleCode(unit.getPoleCode())) {
            throw new IllegalArgumentException("Smart Pole with code '" + unit.getPoleCode() + "' already exists!");
        }
        if (unit.getLedBrightness() == null) {
            unit.setLedBrightness(80);
        }
        if (unit.getAmbientLightLevel() == null) {
            unit.setAmbientLightLevel(25.0);
        }
        if (unit.getChargerStatus() == null) {
            unit.setChargerStatus("AVAILABLE");
        }
        if (unit.getCameraStatus() == null) {
            unit.setCameraStatus("ONLINE");
        }
        return poleRepository.save(unit);
    }

    @Transactional
    public SmartPoleUnit updateDimming(String poleCode, DimmingUpdateRequest request) {
        SmartPoleUnit pole = getUnitByCode(poleCode);
        if (request.getBrightness() != null) {
            pole.setLedBrightness(Math.max(0, Math.min(100, request.getBrightness())));
        }
        if (request.getDimmingSchedule() != null && !request.getDimmingSchedule().isBlank()) {
            pole.setDimmingSchedule(request.getDimmingSchedule());
        }
        if (request.getAutoDimmingEnabled() != null) {
            pole.setAutoDimmingEnabled(request.getAutoDimmingEnabled());
        }
        return poleRepository.save(pole);
    }

    @Transactional
    public List<SmartPoleUnit> autoAdjustByAmbientLux(Double lux, String targetPoleCode) {
        List<SmartPoleUnit> poles;
        if (targetPoleCode != null && !targetPoleCode.isBlank()) {
            poles = List.of(getUnitByCode(targetPoleCode));
        } else {
            poles = poleRepository.findAll();
        }

        for (SmartPoleUnit pole : poles) {
            pole.setAmbientLightLevel(lux);
            if (Boolean.TRUE.equals(pole.getAutoDimmingEnabled())) {
                int targetBrightness;
                if (lux >= 600) {
                    targetBrightness = 0; // Full daylight - off
                } else if (lux >= 300) {
                    targetBrightness = 20; // Late afternoon / overcast
                } else if (lux >= 100) {
                    targetBrightness = 50; // Dusk / twilight
                } else if (lux >= 30) {
                    targetBrightness = 80; // Standard night illumination
                } else {
                    targetBrightness = 100; // Deep darkness - maximum illumination
                }
                pole.setLedBrightness(targetBrightness);
            }
            poleRepository.save(pole);
        }
        return poles;
    }

    @Transactional
    public SmartPoleUnit updateTariff(String poleCode, Double tariff) {
        SmartPoleUnit pole = getUnitByCode(poleCode);
        pole.setCurrentKwhRate(tariff);
        return poleRepository.save(pole);
    }

    @Transactional
    public SmartPoleUnit updateCameraStatus(String poleCode, String status) {
        SmartPoleUnit pole = getUnitByCode(poleCode);
        pole.setCameraStatus(status);
        return poleRepository.save(pole);
    }
}
