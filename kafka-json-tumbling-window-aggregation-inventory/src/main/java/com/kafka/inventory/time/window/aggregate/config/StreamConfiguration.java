package com.kafka.inventory.time.window.aggregate.config;

import com.kafka.inventory.time.window.aggregate.model.Inventory;
import com.kafka.inventory.time.window.aggregate.model.InventoryTransaction;
import com.kafka.inventory.time.window.aggregate.model.PotentialFraudAlert;

import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;


import org.apache.kafka.common.serialization.LongDeserializer;
import org.apache.kafka.common.serialization.LongSerializer;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.StreamsConfig;

import org.apache.kafka.streams.processor.WallclockTimestampExtractor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.annotation.EnableKafkaStreams;
import org.springframework.kafka.annotation.KafkaStreamsDefaultConfiguration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.config.KafkaStreamsConfiguration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.*;
import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
@EnableKafkaStreams
@EnableKafka
public class StreamConfiguration {

    @Bean(name="boostrapServerAdmin")
    public KafkaAdmin admin() {
        Map<String, Object> configs = new HashMap<>();
        configs.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        return new KafkaAdmin(configs);
    }
    @Bean(name=Constants.INVENTORY)
    @DependsOn("boostrapServerAdmin")
    public NewTopic newTopic1() {
        return TopicBuilder.name(Constants.INVENTORY)
                .partitions(1)
                .replicas(1)
                .build();
    }

    @Bean(name=Constants.INVENTORY_TRANSACTIONS)
    @DependsOn(Constants.INVENTORY)
    public NewTopic newTopic2() {
        return TopicBuilder.name(Constants.INVENTORY_TRANSACTIONS)
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean(name=Constants.REJECTED_TRANSACTIONS)
    @DependsOn(Constants.INVENTORY_TRANSACTIONS)
    public NewTopic newTopic3() {
        return TopicBuilder.name(Constants.REJECTED_TRANSACTIONS )
                .partitions(1)
                .replicas(1)
                .build();
    }
    @Bean(name=Constants.POTENTIAL_FRAUD_ALERT)
    @DependsOn(Constants.REJECTED_TRANSACTIONS)
    public NewTopic newTopic4() {
        return TopicBuilder.name(Constants.POTENTIAL_FRAUD_ALERT )
                .partitions(1)
                .replicas(1)
                .build();
    }

    @DependsOn(Constants.POTENTIAL_FRAUD_ALERT)
    @Bean (name=KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
    public KafkaStreamsConfiguration kafkaStreamsConfiguration() {
        Map<String,Object> properties = new HashMap<>();
        properties.put(StreamsConfig.APPLICATION_ID_CONFIG, Constants.APPLICATION_CONFIG_ID);
        properties.put(StreamsConfig.CLIENT_ID_CONFIG, Constants.CLIENT_ID_CONFIG);
        properties.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        properties.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.Long().getClass());
        properties.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        properties.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, "0");
        properties.put(StreamsConfig.COMMIT_INTERVAL_MS_CONFIG, 10 * 1000);
        // For illustrative purposes we disable record caches.
        properties.put(StreamsConfig.CACHE_MAX_BYTES_BUFFERING_CONFIG, 0);
        properties.put(StreamsConfig.DEFAULT_TIMESTAMP_EXTRACTOR_CLASS_CONFIG, WallclockTimestampExtractor.class.getName());
        return new KafkaStreamsConfiguration(properties);
    }

    @Bean
    @DependsOn("consumerFactoryId1")
    public ConcurrentKafkaListenerContainerFactory<Long, Inventory> kafkaListenerContainerFactoryInventory (final ConsumerFactory<Long, Inventory> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<Long, Inventory> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactoryInventory());
        return factory;
    }

    @Bean(name="consumerFactoryId1")
    public ConsumerFactory<Long, Inventory> consumerFactoryInventory() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_GROUP_ID);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // trust model package, critical step
        config.put(JsonDeserializer.TRUSTED_PACKAGES,"com.kafka.inventory.time.window.aggregate.model");
        return new DefaultKafkaConsumerFactory<Long, Inventory>(config, new LongDeserializer(),
                new com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer<Inventory>(Inventory.class));
    }


    @Bean
    @DependsOn("consumerFactoryId2")
    public ConcurrentKafkaListenerContainerFactory<Long, InventoryTransaction> kafkaListenerContainerFactoryTranscation (final ConsumerFactory<Long, InventoryTransaction> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<Long, InventoryTransaction> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactoryTransaction());
        return factory;
    }
    @Bean(name="consumerFactoryId2")
    public ConsumerFactory<Long, InventoryTransaction> consumerFactoryTransaction() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_GROUP_ID);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // trust model package, critical step
        config.put(JsonDeserializer.TRUSTED_PACKAGES,"com.kafka.inventory.time.window.aggregate.model");
        return new DefaultKafkaConsumerFactory<Long, InventoryTransaction>(config, new LongDeserializer(),
                new com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer<InventoryTransaction>(InventoryTransaction.class));
    }

    @Bean
    @DependsOn("consumerFactoryId3")
    public ConcurrentKafkaListenerContainerFactory<Long, PotentialFraudAlert> kafkaListenerContainerFactoryAlert (final ConsumerFactory<Long, PotentialFraudAlert> consumerFactory) {
        final ConcurrentKafkaListenerContainerFactory<Long, PotentialFraudAlert> factory = new ConcurrentKafkaListenerContainerFactory();
        factory.setConsumerFactory(consumerFactoryAlert());
        return factory;
    }
    @Bean(name="consumerFactoryId3")
    public ConsumerFactory<Long, PotentialFraudAlert> consumerFactoryAlert() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ConsumerConfig.GROUP_ID_CONFIG, Constants.CONSUMER_GROUP_ID);
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, LongDeserializer.class);
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, JsonDeserializer.class);
        // trust model package, critical step
        config.put(JsonDeserializer.TRUSTED_PACKAGES,"com.kafka.inventory.time.window.aggregate.model");
        return new DefaultKafkaConsumerFactory<Long, PotentialFraudAlert>(config, new LongDeserializer(),
                new com.kafka.inventory.time.window.aggregate.serdes.JsonDeserializer<PotentialFraudAlert>(PotentialFraudAlert.class));
    }


    @Bean
    public ProducerFactory<Long, InventoryTransaction> producerFactory() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<Long, InventoryTransaction>(config);
    }
    @Bean
    public KafkaTemplate<Long, InventoryTransaction> kafkaTemplate(final ProducerFactory producerFactory) {
        return new KafkaTemplate<>(producerFactory());
    }


    @Bean
    public ProducerFactory<Long, Inventory> producerFactoryBalance() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<Long, Inventory>(config);
    }
    @Bean
   // @DependsOn("producerFactoryId2")
    public KafkaTemplate<Long, Inventory> kafkaTemplateBalance(final ProducerFactory producerFactoryBalance) {
        return new KafkaTemplate<>(producerFactoryBalance());
    }

    @Bean
    public ProducerFactory<Long, PotentialFraudAlert> producerFactoryAlert() {
        final Map<String ,Object> config = new HashMap<>();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, Constants.EXTERNAL_CONNECT_BOOSTRAP_SERVER);
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<Long, PotentialFraudAlert>(config);
    }
    @Bean
   // @DependsOn("producerFactoryId3")
    public KafkaTemplate<Long, PotentialFraudAlert> kafkaTemplateAlert(final ProducerFactory producerFactoryAlert) {
        return new KafkaTemplate<>(producerFactoryAlert());
    }

}
