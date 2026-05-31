package com.purplle.storeintel.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface DetectionRepository extends JpaRepository<Detection, Long> {

    List<Detection> findByZoneOrderByTimestampDesc(String zone);

    List<Detection> findByCameraIdOrderByTimestampDesc(String cameraId);

    @Query("SELECT d.zone, MAX(d.personCount) FROM Detection d WHERE d.timestamp > :since GROUP BY d.zone")
    List<Object[]> findPeakCountByZoneSince(Instant since);

    @Query("SELECT d.zone, AVG(d.dwellTimeSeconds) FROM Detection d WHERE d.timestamp > :since GROUP BY d.zone")
    List<Object[]> findAvgDwellByZoneSince(Instant since);

    @Query("SELECT COUNT(d) FROM Detection d WHERE d.zone = :zone AND d.timestamp > :since")
    long countFootfallByZone(String zone, Instant since);

    List<Detection> findTop50ByOrderByTimestampDesc();
}
