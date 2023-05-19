package com.kafka.inventory.time.window.aggregate.processor;

import com.kafka.inventory.time.window.aggregate.config.Constants;
import com.kafka.inventory.time.window.aggregate.model.Inventory;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.serdeimpl.InventorySerdes;
import com.kafka.inventory.time.window.aggregate.serdeimpl.InventoryTransactionSerdes;

import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsBuilder;

import org.apache.kafka.streams.kstream.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;

@EnableKafkaStreams
@EnableKafka
@Configuration
@Slf4j
public class StatefulAggregationProcessor {


   //@Bean
    public KStream<Long, Inventory> kStream(StreamsBuilder streamsBuilder) {
   // public static Topology buildTopology() {
      //  Serde<BankTransaction> bankTransactionSerdes = new JsonSerde<>(BankTransaction.class);
      //  Serde<BankBalance> bankBalanceSerde = new JsonSerde<>(BankBalance.class);
       // StreamsBuilder streamsBuilder = new StreamsBuilder();
        /**
         * Consumed.with(Serdes.Long(), bankTransactionSerdes) consume balance Id abd BankTransaction instance
         * groupByKey group by all balances which has same group Id
         * .aggregate(initial, aggregator, Materialized.with, ) three parameters
         * first one : initializer , create instance of BankBalance by 'new BankBalance()' simplify BankBalance:: new
         * second one: aggregator function (key,value, aggregate) ->{}, key is balance Id, value is bank transaction, aggregate bank balance
         * aggregate here is reference of BankBalance, aggregate.process(value) is BankBalance.process(BankTransaction)
         * third one : Materialized.with(Serdes.Long(), bankTransactionSerdes) tells kafka stored data into disk
         *  .toStream(); convert KTable<Long,BankBalance> to kstream
         */
        KStream<Long, Inventory> bankBalancesStream = streamsBuilder.stream(Constants.INVENTORY_TRANSACTIONS,
                Consumed.with(Serdes.Long(), InventoryTransactionSerdes.serdes()))
                .groupByKey()
                .aggregate(Inventory::new,
                        (key, value, aggregate) -> aggregate.processTransaction(value),
                        Materialized.with(Serdes.Long(), InventorySerdes.serdes()))
                .toStream();


        bankBalancesStream.peek((key,value)->log.info("bankBalancesStream key=" +key+"+,value="+value))
                .to(Constants.INVENTORY, Produced.with(Serdes.Long(), InventorySerdes.serdes()));

        bankBalancesStream
                .mapValues((readOnlyKey, value) -> value.getLatestTransaction())
                .filter((key, value) -> value.state == InventoryTransaction.InventoryTransactionState.REJECTED)
                .to(Constants.REJECTED_TRANSACTIONS, Produced.with(Serdes.Long(), InventoryTransactionSerdes.serdes()));

      //  return streamsBuilder.build();
        return bankBalancesStream;
    }
}
