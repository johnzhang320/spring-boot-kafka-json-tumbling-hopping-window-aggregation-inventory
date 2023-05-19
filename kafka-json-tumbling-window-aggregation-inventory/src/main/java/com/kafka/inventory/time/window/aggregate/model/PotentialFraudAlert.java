package com.kafka.inventory.time.window.aggregate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PotentialFraudAlert {
    private Long itemId;
    private Long rejectedTransactionCount;
    private String message;
}
