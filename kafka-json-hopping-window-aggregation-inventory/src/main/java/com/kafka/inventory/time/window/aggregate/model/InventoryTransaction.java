package com.kafka.inventory.time.window.aggregate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class InventoryTransaction {

    private Long itemId;
    private String itemName;
    private Long quantity;
    private BigDecimal price = BigDecimal.ZERO;
    private Integer count;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
                pattern = "dd-MM-yyyy hh:mm:ss")
    public Date time;
    @Builder.Default
    public InventoryTransactionState state = InventoryTransactionState.CREATED;

    public TransactionRequestState transactionRequest;

    public static enum InventoryTransactionState {
        CREATED, APPROVED, REJECTED
    }
    public static enum TransactionRequestState {
        ADD,SHIPPING
    }
}
