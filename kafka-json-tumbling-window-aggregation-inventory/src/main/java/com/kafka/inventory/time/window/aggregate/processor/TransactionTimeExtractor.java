package com.kafka.inventory.time.window.aggregate.processor;

import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.streams.processor.TimestampExtractor;

import java.util.Optional;

public class TransactionTimeExtractor implements TimestampExtractor {

    @Override
    public long extract(ConsumerRecord<Object, Object> record, long partitionTime) {
        InventoryTransaction inventoryTransaction = (InventoryTransaction) record.value();
        return Optional.ofNullable(inventoryTransaction.getTime())
                .map(it-> it.toInstant().toEpochMilli())
                .orElse(partitionTime);
    }
}
