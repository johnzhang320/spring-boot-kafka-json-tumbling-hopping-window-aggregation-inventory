package com.kafka.inventory.time.window.aggregate.dto;

import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.text.DecimalFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryTransactionDto {
    private Long itemId;
    private String itemName;
    private Long quantity;
    private Double price;
    public String requestState;

}
