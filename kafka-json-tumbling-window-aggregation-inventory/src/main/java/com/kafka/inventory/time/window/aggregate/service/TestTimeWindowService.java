package com.kafka.inventory.time.window.aggregate.service;

import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
@Service
public class TestTimeWindowService {

    public List<InventoryTransaction> testTumblingWindowFraud() {
        int times = 28; //(int) (100+Math.random()*10);   // 28 times rejected Transactions
        BigDecimal price = new BigDecimal(239.99);
        price = price.setScale(2, BigDecimal.ROUND_HALF_EVEN);
        List<InventoryTransaction> list = new ArrayList<>();
        for (int i = 0; i<times;i++) {
            InventoryTransaction inventoryTransaction= InventoryTransaction.builder()
                    .itemId(10020L)
                    .count(i+1)
                    .price(price)
                    .transactionRequest(InventoryTransaction.TransactionRequestState.SHIPPING)
                    .quantity((long) (2000+Math.random()*1000))
                    .itemName("iWatch")
                    .time(new Date())
                    .build();
            list.add( inventoryTransaction);
        }
        return list;
    }
}
