package com.purplle.storeintel.controller;

import com.purplle.storeintel.model.Alert;
import com.purplle.storeintel.model.Detection;
import com.purplle.storeintel.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST API — Store Intelligence System
 *
 * GET  /api/dashboard          → summary stats
 * GET  /api/zones/occupancy    → current person count per zone
 * GET  /api/detections         → recent detections (last 50)
 * GET  /api/alerts             → active unresolved alerts
 * GET  /api/alerts/all         → last 20 alerts
 * POST /api/alerts/{id}/resolve → resolve an alert
 * GET  /api/health             → health check
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "Store Intelligence System"));
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(analyticsService.getDashboardSummary());
    }

    @GetMapping("/zones/occupancy")
    public ResponseEntity<Map<String, Integer>> zoneOccupancy() {
        return ResponseEntity.ok(analyticsService.getZoneOccupancy());
    }

    @GetMapping("/detections")
    public ResponseEntity<List<Detection>> recentDetections() {
        return ResponseEntity.ok(analyticsService.getRecentDetections());
    }

    @GetMapping("/alerts")
    public ResponseEntity<List<Alert>> activeAlerts() {
        return ResponseEntity.ok(analyticsService.getActiveAlerts());
    }

    @GetMapping("/alerts/all")
    public ResponseEntity<List<Alert>> allAlerts() {
        return ResponseEntity.ok(analyticsService.getAllAlerts());
    }

    @PostMapping("/alerts/{id}/resolve")
    public ResponseEntity<Map<String, String>> resolveAlert(@PathVariable Long id) {
        analyticsService.resolveAlert(id);
        return ResponseEntity.ok(Map.of("status", "resolved", "alertId", id.toString()));
    }
}
