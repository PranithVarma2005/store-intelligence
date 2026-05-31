package com.purplle.storeintel.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.purplle.storeintel.event.DetectionEvent;
import com.purplle.storeintel.model.Alert;
import com.purplle.storeintel.model.AlertRepository;
import com.purplle.storeintel.model.Detection;
import com.purplle.storeintel.model.DetectionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalyticsService {

    private final DetectionRepository detectionRepository;
    private final AlertRepository alertRepository;
    private final SimpMessagingTemplate websocket;
    private final ObjectMapper objectMapper;

    // Called directly by simulator (no Kafka)
    public void processEvent(DetectionEvent event) {
        Detection detection = Detection.builder()
                .cameraId(event.getCameraId())
                .zone(event.getZone())
                .storeId(event.getStoreId())
                .personId(event.getPersonId())
                .confidence(event.getConfidence())
                .personCount(event.getPersonCount())
                .eventType(event.getEventType().name())
                .dwellTimeSeconds(event.getDwellTimeSeconds())
                .overcrowded(event.isOvercrowded())
                .lingering(event.isLingering())
                .timestamp(event.getTimestamp())
                .build();
        detectionRepository.save(detection);
        checkAndRaiseAlert(event);
        pushToWebSocket(event);
    }

    private void checkAndRaiseAlert(DetectionEvent event) {
        if (event.isOvercrowded()) {
            raiseAlert(event, "OVERCROWD", "HIGH",
                String.format("Zone '%s' has %d people (limit: 8)", event.getZone(), event.getPersonCount()));
        }
        if (event.isLingering()) {
            raiseAlert(event, "LINGER", "MEDIUM",
                String.format("Person %s lingering in '%s' for %ds", event.getPersonId(), event.getZone(), event.getDwellTimeSeconds()));
        }
        if (event.isUnattended() && event.getEventType() == DetectionEvent.EventType.ZONE_EMPTY) {
            raiseAlert(event, "ZONE_EMPTY", "LOW",
                String.format("Zone '%s' has been empty", event.getZone()));
        }
    }

    private void raiseAlert(DetectionEvent event, String type, String severity, String description) {
        Alert alert = Alert.builder()
                .cameraId(event.getCameraId())
                .zone(event.getZone())
                .alertType(type)
                .severity(severity)
                .description(description)
                .resolved(false)
                .timestamp(Instant.now())
                .build();
        alertRepository.save(alert);
        try {
            websocket.convertAndSend("/topic/alerts", objectMapper.writeValueAsString(alert));
        } catch (JsonProcessingException e) {
            log.error("WebSocket push failed", e);
        }
    }

    private void pushToWebSocket(DetectionEvent event) {
        try {
            websocket.convertAndSend("/topic/events", objectMapper.writeValueAsString(event));
            websocket.convertAndSend("/topic/occupancy", objectMapper.writeValueAsString(getZoneOccupancy()));
        } catch (JsonProcessingException e) {
            log.error("WebSocket push failed", e);
        }
    }

    public Map<String, Object> getDashboardSummary() {
        Instant oneHourAgo = Instant.now().minus(1, ChronoUnit.HOURS);
        List<Object[]> peakCounts = detectionRepository.findPeakCountByZoneSince(oneHourAgo);
        List<Object[]> avgDwell = detectionRepository.findAvgDwellByZoneSince(oneHourAgo);

        Map<String, Integer> peakByZone = peakCounts.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).intValue()));
        Map<String, Double> dwellByZone = avgDwell.stream()
                .collect(Collectors.toMap(r -> (String) r[0], r -> ((Number) r[1]).doubleValue()));

        return Map.of(
            "totalDetections", detectionRepository.count(),
            "activeAlerts", alertRepository.findByResolvedFalseOrderByTimestampDesc().size(),
            "peakOccupancyByZone", peakByZone,
            "avgDwellSecondsByZone", dwellByZone,
            "generatedAt", Instant.now()
        );
    }

    public Map<String, Integer> getZoneOccupancy() {
        List<Detection> recent = detectionRepository.findTop50ByOrderByTimestampDesc();
        Map<String, Integer> occupancy = new LinkedHashMap<>();
        recent.stream()
              .collect(Collectors.toMap(Detection::getZone, Detection::getPersonCount, (a, b) -> a))
              .forEach(occupancy::put);
        return occupancy;
    }

    public List<Detection> getRecentDetections() { return detectionRepository.findTop50ByOrderByTimestampDesc(); }
    public List<Alert> getActiveAlerts() { return alertRepository.findByResolvedFalseOrderByTimestampDesc(); }
    public List<Alert> getAllAlerts() { return alertRepository.findTop20ByOrderByTimestampDesc(); }

    public void resolveAlert(Long alertId) {
        alertRepository.findById(alertId).ifPresent(alert -> {
            alert.setResolved(true);
            alertRepository.save(alert);
        });
    }
}
