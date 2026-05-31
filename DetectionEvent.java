package com.purplle.storeintel.event;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * Core event schema published to Kafka for every detection.
 * Designed to be extensible for real Rekognition payloads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetectionEvent {

    // Camera and location context
    private String cameraId;
    private String zone;
    private String storeId;

    // Person tracking
    private String personId;       // Unique track ID per person
    private double confidence;     // Detection confidence 0.0–1.0
    private int personCount;       // Total persons in frame

    // Bounding box (normalised 0.0–1.0, as Rekognition returns)
    private double boxLeft;
    private double boxTop;
    private double boxWidth;
    private double boxHeight;

    // Event classification
    private EventType eventType;
    private long dwellTimeSeconds; // How long person has been in zone

    // Anomaly flags
    private boolean overcrowded;   // Zone over capacity threshold
    private boolean lingering;     // Person lingering > threshold
    private boolean unattended;    // Zone empty for too long

    @JsonFormat(shape = JsonFormat.Shape.STRING)
    private Instant timestamp;

    public enum EventType {
        PERSON_ENTERED,
        PERSON_EXITED,
        PERSON_DETECTED,
        ANOMALY_OVERCROWD,
        ANOMALY_LINGER,
        ZONE_EMPTY
    }
}
