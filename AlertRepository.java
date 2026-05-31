package com.purplle.storeintel.model;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AlertRepository extends JpaRepository<Alert, Long> {
    List<Alert> findByResolvedFalseOrderByTimestampDesc();
    List<Alert> findTop20ByOrderByTimestampDesc();
    List<Alert> findByZoneOrderByTimestampDesc(String zone);
}
