package com.kafka.inventory.time.window.aggregate.processor;

import com.kafka.inventory.time.window.aggregate.config.Constants;
import com.kafka.inventory.time.window.aggregate.model.Inventory;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.model.PotentialFraudAlert;
import com.kafka.inventory.time.window.aggregate.serdeimpl.InventorySerdes;
import com.kafka.inventory.time.window.aggregate.serdeimpl.InventoryTransactionSerdes;
import com.kafka.inventory.time.window.aggregate.serdeimpl.PotentialFraudAlertSerdes;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;


import java.time.Duration;

import static org.apache.kafka.streams.kstream.Suppressed.BufferConfig.unbounded;
import static org.apache.kafka.streams.kstream.Suppressed.untilWindowCloses;

@EnableKafkaStreams
@EnableKafka
@Configuration
@Slf4j
public class InventoryHoppingWindowProcessor {

    @Bean
    @DependsOn(KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KStream<Long, Inventory> kStream(StreamsBuilder streamsBuilder) {
        /*
         * Consumed.with(Serdes.Long(), inventoryTransactionSerdes) consume  InventoryTransaction instance
         * groupByKey group by all balances which has same group Id
         * .aggregate(initial, aggregator, Materialized.with, ) three parameters
         * first one : initializer , create instance of Inventory by 'new Inventory()' simplify Inventory:: new
         * second one: aggregator function (key,value, aggregate) ->{}, key is balance Id, value is inventory transaction, aggregate inventory balance
         * aggregate here is reference of Inventory, aggregate.process(value) is Inventory.process(InventoryTransaction)
         * third one : Materialized.with(Serdes.Long(), inventoryTransactionSerdes) tells kafka stored data into disk
         *  .toStream(); convert KTable<Long,Inventory> to kstream
         *  kafka know how to extract timestamp by withTimestampExtractor , we implement timestamp TransactionTimeExtractor()
         */

        KStream<Long, InventoryTransaction> inventoryTransactionKStream = streamsBuilder.stream(Constants.INVENTORY_TRANSACTIONS,
                Consumed.with(Serdes.Long(), InventoryTransactionSerdes.serdes())
                        .withTimestampExtractor(new TransactionTimeExtractor()));


        KStream<Long, Inventory> inventoryKStream =
                inventoryTransactionKStream.groupByKey()
                        .aggregate(()->new Inventory(),
                                (key, value, aggregate) -> {
                                    aggregate.processTransaction(value);
                                    return aggregate;
                                },
                                Materialized.with(Serdes.Long(), InventorySerdes.serdes())
                        )
                        .toStream();
        //  .peek(((key,value)->log.info("Peek Processed Transaction Inventory for key={},  value={}",key,value.toString())));


        inventoryKStream.to(Constants.INVENTORY, Produced.with(Serdes.Long(), InventorySerdes.serdes()));

        KStream<Long, InventoryTransaction> rejectedTransactionStream = inventoryKStream
                .mapValues((readOnlyKey,value)->value.getLatestTransaction())
                .filter((kay,value)->value.state== InventoryTransaction.InventoryTransactionState.REJECTED);


        rejectedTransactionStream
                //  .peek(((key,value)->log.info("Peek rejected transaction for key={},  value={}",key,value)))
                .to(Constants.REJECTED_TRANSACTIONS, Produced.with(Serdes.Long(), InventoryTransactionSerdes.serdes()));

         /*
          groupByKey-- group event by balance Id
          TimeWindows.of(Duration.ofSeconds(20L))-- Tumbling Window fixed time is 20 seconds
          grace -- grace period for time window, ensure that event are delayed because of network heartbeat issue
                   we have time window  take account of that, we set the duration of 2 seconds
          count() --count event that is counting number of events
          suppress -- we do not want intermediate events, we only want events when the window close,
                      when the window close, we only take one event for all window close, suppress event until
                      window closes
                      I use two peeks to compare all event count include intermediate events and not include
                      pay attend the peek with "Rejected Inventory Transactions >=10"
         */
        Duration hoppingWindowSize = Duration.ofSeconds(20L);
        Duration advanceWindowSize = Duration.ofSeconds(2L);
        rejectedTransactionStream
                .groupByKey()
                .windowedBy(TimeWindows.of(hoppingWindowSize).advanceBy(advanceWindowSize).grace(Duration.ofSeconds(0)))
                .count()
                .suppress(untilWindowCloses(unbounded()))
                .toStream()
                .map((key,value)-> KeyValue.pair(key.key(),value))
                 .filter((key,value)->value>=10)
                .peek(((key,value)->log.info("Peek Within 20 seconds and rejected times>=10 ,Hopping Window captured rejected inventory transactions as itemId {},  Count {}",key,value)))
                .mapValues((key,value)->new PotentialFraudAlert(key,value,String.format("Hopping Window captured Potential Fraud Alerts as itemId %s Count %d",key,value)))
                .to(Constants.POTENTIAL_FRAUD_ALERT,Produced.with(Serdes.Long(), PotentialFraudAlertSerdes.serdes()));

        return inventoryKStream;
    }
}
