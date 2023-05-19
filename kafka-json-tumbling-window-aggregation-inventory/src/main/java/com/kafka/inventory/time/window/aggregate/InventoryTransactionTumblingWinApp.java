package com.kafka.inventory.time.window.aggregate;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@SpringBootApplication
@EnableKafkaStreams
public class InventoryTransactionTumblingWinApp {

    public static void main(String[] args) {
        SpringApplication.run(InventoryTransactionTumblingWinApp.class);

    }
}
