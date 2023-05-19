package com.kafka.inventory.time.window.aggregate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

import java.math.BigDecimal;
import java.util.Date;

@Data

@AllArgsConstructor
@Builder
@ToString
public class Inventory {

    private Long itemId;
    private String itemName;
    private Long quantity;
    private BigDecimal price = BigDecimal.ZERO;

    @JsonFormat(shape = JsonFormat.Shape.STRING,
            pattern = "dd-MM-yyyy hh:mm:ss")
    private Date lastUpdate;
    private InventoryTransaction latestTransaction;

    public Inventory() {
        this.itemId=0l;
        this.itemName="";
        this.quantity=0L;
        this.price = BigDecimal.ZERO;
        this.latestTransaction=null;

    }
    public Inventory processTransaction(InventoryTransaction inventoryTransaction) {

         setInventory(inventoryTransaction);

        if (inventoryTransaction.transactionRequest==InventoryTransaction.TransactionRequestState.SHIPPING) {
            // shipping item from inventory
            if (this.quantity < inventoryTransaction.getQuantity()) {
                this.latestTransaction.setState(InventoryTransaction.InventoryTransactionState.REJECTED);
            } else {
                this.latestTransaction.setState(InventoryTransaction.InventoryTransactionState.APPROVED);
                this.quantity-= inventoryTransaction.getQuantity()==null ? 0:inventoryTransaction.getQuantity();
            }
        } else if (inventoryTransaction.transactionRequest==InventoryTransaction.TransactionRequestState.ADD){
            // add item to inventory
            this.quantity+=inventoryTransaction.getQuantity()==null ? 0:inventoryTransaction.getQuantity();
            this.latestTransaction.setState(InventoryTransaction.InventoryTransactionState.APPROVED);
        }
        return this;
    }
    public void setInventory(InventoryTransaction transaction) {
        this.itemId= transaction.getItemId();
        this.itemName = transaction.getItemName();
        this.price = transaction.getPrice();
        this.lastUpdate=transaction.getTime();
        this.latestTransaction = transaction;
    }
}
