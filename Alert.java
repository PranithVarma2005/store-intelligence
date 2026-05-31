package com.purplle.storeintel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "alerts")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cameraId;
    private String zone;
    private String alertType;      // OVERCROWD, LINGER, ZONE_EMPTY
    private String severity;       // LOW, MEDIUM, HIGH
    private String description;
    private boolean resolved;
    private Instant timestamp;
}
