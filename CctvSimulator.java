package com.purplle.storeintel.simulator;

import com.purplle.storeintel.event.DetectionEvent;
import com.purplle.storeintel.service.AnalyticsService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class CctvSimulator {

    private final AnalyticsService analyticsService;

    @Value("${app.cameras}")
    private String camerasConfig;

    @Value("${app.zones}")
    private String zonesConfig;

    private final Random random = new Random();
    private final Map<String, Long> dwellTracker = new HashMap<>();
    private final Map<String, Integer> zoneOccupancy = new HashMap<>();

    private static final int MAX_ZONE_CAPACITY = 8;
    private static final long LINGER_THRESHOLD_SECONDS = 120;

    @Scheduled(fixedDelayString = "${app.simulation.interval-ms}")
    public void simulateFrame() {
        String[] cameras = camerasConfig.split(",");
        String[] zones = zonesConfig.split(",");

        int detectionCount = 1 + random.nextInt(3);
        for (int i = 0; i < detectionCount; i++) {
            String cameraId = cameras[random.nextInt(cameras.length)];
            String zone = zones[random.nextInt(zones.length)];
            String personId = "P" + (1000 + random.nextInt(50));

            String trackKey = cameraId + ":" + personId;
            dwellTracker.merge(trackKey, (long)(2 + random.nextInt(5)), Long::sum);
            long dwellTime = dwellTracker.get(trackKey);

            int currentCount = zoneOccupancy.getOrDefault(zone, 0);
            int delta = random.nextInt(3) - 1;
            currentCount = Math.max(0, Math.min(15, currentCount + delta));
            zoneOccupancy.put(zone, currentCount);

            DetectionEvent.EventType eventType = determineEventType(currentCount, dwellTime);

            DetectionEvent event = DetectionEvent.builder()
                    .cameraId(cameraId).zone(zone).storeId("STORE_001")
                    .personId(personId)
                    .confidence(0.75 + random.nextDouble() * 0.24)
                    .personCount(currentCount)
                    .boxLeft(random.nextDouble() * 0.6).boxTop(random.nextDouble() * 0.6)
                    .boxWidth(0.1 + random.nextDouble() * 0.2).boxHeight(0.2 + random.nextDouble() * 0.3)
                    .eventType(eventType).dwellTimeSeconds(dwellTime)
                    .overcrowded(currentCount > MAX_ZONE_CAPACITY)
                    .lingering(dwellTime > LINGER_THRESHOLD_SECONDS)
                    .unattended(currentCount == 0)
                    .timestamp(Instant.now())
                    .build();

            analyticsService.processEvent(event);
        }
    }

    private DetectionEvent.EventType determineEventType(int count, long dwell) {
        if (count > MAX_ZONE_CAPACITY) return DetectionEvent.EventType.ANOMALY_OVERCROWD;
        if (dwell > LINGER_THRESHOLD_SECONDS) return DetectionEvent.EventType.ANOMALY_LINGER;
        if (count == 0) return DetectionEvent.EventType.ZONE_EMPTY;
        if (random.nextDouble() < 0.15) return DetectionEvent.EventType.PERSON_ENTERED;
        if (random.nextDouble() < 0.1) return DetectionEvent.EventType.PERSON_EXITED;
        return DetectionEvent.EventType.PERSON_DETECTED;
    }
}
