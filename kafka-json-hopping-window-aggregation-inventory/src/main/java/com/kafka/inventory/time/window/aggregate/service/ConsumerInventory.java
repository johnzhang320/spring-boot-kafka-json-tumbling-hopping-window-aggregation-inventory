package com.kafka.inventory.time.window.aggregate.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.kafka.inventory.time.window.aggregate.config.Constants;
import com.kafka.inventory.time.window.aggregate.model.Inventory;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.model.PotentialFraudAlert;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaListener;

@EnableKafkaStreams
@EnableKafka
@Configuration
@Slf4j
public class ConsumerInventory {
    @KafkaListener(topics = Constants.INVENTORY,groupId = Constants.CONSUMER_GROUP_ID)
    public void listenInventory(ConsumerRecord<Long, Inventory> record)  {
     //  log.info("Consumer Listened Inventory: " + record.value());
    }
    @KafkaListener(topics = Constants.REJECTED_TRANSACTIONS,groupId = Constants.CONSUMER_GROUP_ID)
    public void listenRejectedInventory(ConsumerRecord<Long, InventoryTransaction> record)  {
        log.info("Consumed Rejected InventoryTransaction:" + record.value());
    }

    @KafkaListener(topics = Constants.POTENTIAL_FRAUD_ALERT,groupId = Constants.CONSUMER_GROUP_ID)
    public void listenPotentialFraudAlert(ConsumerRecord<Long, PotentialFraudAlert> record)   {
        System.out.println("Within 20 seconds, rejected transactions>10, which could be Potential Fraud");
        System.out.println("Consumer received Potential Fraud Alert:" + record.value());
     }
}
