package com.kafka.inventory.time.window.aggregate.service;

import com.kafka.inventory.time.window.aggregate.config.Constants;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SendInventoryTransaction {
    private final KafkaTemplate<Long, InventoryTransaction> kafkaTemplate;
    public void SendInventoryTransaction(InventoryTransaction inventoryTransaction) {
        kafkaTemplate.send(Constants.INVENTORY_TRANSACTIONS,inventoryTransaction.getItemId(),inventoryTransaction);
    }

}
