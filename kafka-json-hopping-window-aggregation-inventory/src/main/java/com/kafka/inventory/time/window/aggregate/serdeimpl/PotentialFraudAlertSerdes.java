package com.kafka.inventory.time.window.aggregate.serdeimpl;

import com.kafka.inventory.time.window.aggregate.model.PotentialFraudAlert;
import com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer;
import com.kafka.inventory.time.window.aggregate.serdes.JsonSerializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;

public class PotentialFraudAlertSerdes extends Serdes.WrapperSerde<PotentialFraudAlert> {
    public PotentialFraudAlertSerdes() {
        super (new JsonSerializer<>(),new JsonDeserializer<>(PotentialFraudAlert.class));
    }
    public static Serde<PotentialFraudAlert> serdes() {
        JsonSerializer<PotentialFraudAlert> serializer = new JsonSerializer<>();
        JsonDeserializer<PotentialFraudAlert> deSerializer = new JsonDeserializer<>(PotentialFraudAlert.class);
        return Serdes.serdeFrom(serializer, deSerializer);
    }
}
