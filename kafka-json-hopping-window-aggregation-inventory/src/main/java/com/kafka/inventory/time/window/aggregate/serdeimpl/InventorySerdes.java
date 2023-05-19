package com.kafka.inventory.time.window.aggregate.serdeimpl;

import com.kafka.inventory.time.window.aggregate.model.Inventory;
import com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer;
import com.kafka.inventory.time.window.aggregate.serdes.JsonSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class InventorySerdes extends Serdes.WrapperSerde<Inventory> {
    public InventorySerdes() {
        super (new JsonSerializer<>(),new JsonDeserializer<>(Inventory.class));
    }
    public static Serde<Inventory> serdes() {
        JsonSerializer<Inventory> serializer = new JsonSerializer<>();
        JsonDeserializer<Inventory> deSerializer = new JsonDeserializer<>(Inventory.class);
        return Serdes.serdeFrom(serializer, deSerializer);
    }
}
