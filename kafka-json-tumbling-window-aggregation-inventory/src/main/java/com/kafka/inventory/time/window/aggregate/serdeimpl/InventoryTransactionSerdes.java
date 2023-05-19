package com.kafka.inventory.time.window.aggregate.serdeimpl;

import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer;
import com.kafka.inventory.time.window.aggregate.serdes.JsonSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class InventoryTransactionSerdes extends Serdes.WrapperSerde<InventoryTransaction> {
    public InventoryTransactionSerdes() {
        super (new JsonSerializer<>(),new JsonDeserializer<>(InventoryTransaction.class));
    }
    public static Serde<InventoryTransaction> serdes() {
        JsonSerializer<InventoryTransaction> serializer = new JsonSerializer<>();
        JsonDeserializer<InventoryTransaction> deSerializer = new JsonDeserializer<>(InventoryTransaction.class);
        return Serdes.serdeFrom(serializer, deSerializer);
    }
}
