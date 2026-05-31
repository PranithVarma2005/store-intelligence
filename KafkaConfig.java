package com.purplle.storeintel.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic detectionsTopic() {
        return TopicBuilder.name("store-detections")
                .partitions(4)   // One partition per camera group
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic alertsTopic() {
        return TopicBuilder.name("store-alerts")
                .partitions(1)
                .replicas(1)
                .build();
    }
}
