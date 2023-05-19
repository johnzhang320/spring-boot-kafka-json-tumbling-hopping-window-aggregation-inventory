package com.kafka.inventory.time.window.aggregate.service;

import com.kafka.inventory.time.window.aggregate.dto.InventoryTransactionDto;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
public class InventoryTransactionService {
    public InventoryTransaction toInventoryTransaction(InventoryTransactionDto inventoryTransactionDto) {
        InventoryTransaction.TransactionRequestState requestState;
        if (inventoryTransactionDto.requestState.equalsIgnoreCase("ADD")) {
            requestState = InventoryTransaction.TransactionRequestState.ADD;
        } else if (inventoryTransactionDto.requestState.equalsIgnoreCase("SHIPPING")) {
            requestState = InventoryTransaction.TransactionRequestState.SHIPPING;
        } else {
            throw new RuntimeException("must specify request state \"Add\" or \"SHOPPING\"");
        }
        if (inventoryTransactionDto.getPrice() == null) {
            throw new RuntimeException("Price is required!");
        }
        if (inventoryTransactionDto.getQuantity() == null) {
            throw new RuntimeException("Quantity is required!");
        }
        // keep two decimals only
        BigDecimal price = new BigDecimal(inventoryTransactionDto.getPrice());
        price = price.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        InventoryTransaction InventoryTransaction = com.kafka.inventory.time.window.aggregate.model.InventoryTransaction.builder()
                .itemId(inventoryTransactionDto.getItemId())
                .transactionRequest(requestState)
                .price(price)
                .quantity(Long.valueOf(inventoryTransactionDto.getQuantity()))
                .itemName(inventoryTransactionDto.getItemName())
                .time(new Date())
                .build();
        return InventoryTransaction;
    }

}
