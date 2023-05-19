package com.kafka.inventory.time.window.aggregate.controller;

import com.kafka.inventory.time.window.aggregate.dto.InventoryTransactionDto;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.service.InventoryTransactionService;
import com.kafka.inventory.time.window.aggregate.service.SendInventoryTransaction;
import com.kafka.inventory.time.window.aggregate.service.TestTimeWindowService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("/inventory")
@RequiredArgsConstructor
public class TranscationController {

    private final SendInventoryTransaction sendInventoryTransaction;
    private final InventoryTransactionService inventoryTransactionService;
    private final TestTimeWindowService testTimeWindowService;
    /*
       GET
       http://localhost:8097/inventory/dataProducer
     */

    /*
           POST
           http://localhost:8097/inventory/transaction
           {
               "itemId":100,
               "itemName":"iWatch",
               "price":250.0
               "quantity":1200,
               "requestState":"ADD"
            }

     */
    @PostMapping("/transaction")
    public InventoryTransaction InventoryTransaction(@RequestBody InventoryTransactionDto inventoryTransactionDto) {
        InventoryTransaction inventoryTransaction = inventoryTransactionService.toInventoryTransaction(inventoryTransactionDto);
        sendInventoryTransaction.SendInventoryTransaction(inventoryTransaction);
        return inventoryTransaction;
    }

    /*
         POST
         http://localhost:8097/inventory/transactions
        [

           {
               "itemId":100,
               "itemName":"iWatch",
               "quantity":1000,
               "price":249.4,
               "requestState":"ADD"
            },
           {
              "itemId":100,
               "itemName":"iWatch",
               "price":240.0,
               "quantity":200,
               "requestState":"SHIPPING"
            },
            {
               "itemId":100,
               "itemName":"iWatch",
               "price":240.0,
               "quantity":100,
               "requestState":"SHIPPING"
           },
             {
               "itemId":100,
               "itemName":"iWatch",
               "price":240.0,
               "quantity":5200,
               "requestState":"SHIPPING"
             },
              {
               "itemId":150,
               "itemName":"iPhone14",
               "price":1249.99,
               "quantity":100,
               "requestState":"ADD"
             },
               {
               "itemId":150,
               "itemName":"iPhone14",
               "price":1249.99,
               "quantity":102,
               "requestState":"SHIPPING"
             }
         ]

     */
    @PostMapping("/transactions")
    public List<InventoryTransactionDto> InventoryTransaction(@RequestBody List<InventoryTransactionDto> inventoryTransactionDtos) {
        inventoryTransactionDtos.forEach(inventoryTransactionDto -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
            }
            InventoryTransaction inventoryTransaction = inventoryTransactionService.toInventoryTransaction(inventoryTransactionDto);
            sendInventoryTransaction.SendInventoryTransaction(inventoryTransaction);
        });
        return inventoryTransactionDtos;
    }

    @GetMapping("/potentialFraud")
    public List<InventoryTransaction> potentialFraud() {
        List<InventoryTransaction> inventoryTransactions = testTimeWindowService.testTumblingWindowFraud();
        inventoryTransactions.forEach(inventoryTransaction -> {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {}

            sendInventoryTransaction.SendInventoryTransaction(inventoryTransaction);
          });
        return inventoryTransactions;
    }

}
