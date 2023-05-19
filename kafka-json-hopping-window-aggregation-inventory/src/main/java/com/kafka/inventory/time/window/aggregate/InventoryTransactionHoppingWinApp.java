package com.kafka.inventory.time.window.aggregate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class InventoryTransactionHoppingWinApp {

    public static void main(String[] args) {
        SpringApplication.run(InventoryTransactionHoppingWinApp.class);
    }
}
