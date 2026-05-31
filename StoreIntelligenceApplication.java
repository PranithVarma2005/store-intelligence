package com.purplle.storeintel;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class StoreIntelligenceApplication {
    public static void main(String[] args) {
        SpringApplication.run(StoreIntelligenceApplication.class, args);
    }
}
