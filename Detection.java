package com.purplle.storeintel.model;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Entity
@Table(name = "detections")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Detection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cameraId;
    private String zone;
    private String storeId;
    private String personId;
    private double confidence;
    private int personCount;
    private String eventType;
    private long dwellTimeSeconds;
    private boolean overcrowded;
    private boolean lingering;
    private Instant timestamp;
}
