# spring-boot kafka json tumbling & hopping window aggregation for inventory
## Key Points
  1. Implement tumbling and hopping time window to capture specific rejected count based on inventory transaction aggregation 
  2. Comparing two time windows not only by the analysis but also by real tansaction event stream and simulate real conditions 
  3. Inventory transaction include state of "ADD" item to inventory and take item from inventory call "SHIPPING","APPROVE","REJECT"
  4. Aggregation transforms inventory transaction into inventory
  5. Supported by Generic Json Serializer and Json Deserializer and Object Serdes, topology and spring boot use same objects without
     other transform intermediate class such as Avro.
  6. Through many real data tests, we found some important point: when we change the object's key to that never be used prviously,
     Tumbling windows first time still report last time key rejected count, second time it may miss or partial miss reject counts as 
     fraud even they meet fraud condition, However using exactly same transform with different window type, Hopping window first time 
     report fraud exactly if they meet fraud condition.
  7. Again, it is supprised that we found change keys of event objects, first time call POST URL, tumbling window responses previous 
     fraud key's count, second time call same URL, tumbling window missed or partial missed the fraud count. But hopping window first 
     time to reflect real keys fraud aler
     
## Tumbling and Hopping Window Analysis
 
### Tumbling Window

   We already know tumbling window size are fixed and each time go forward fixed size of window length, it detects all events, here 
   we need tumbling window  capture all rejected transaction events if the count >=10 within 20 seconds, therefore if  say rejected 
   events 17 times but across two windows,  first event might start at middle of tumbling window, 8 seconds for example,  even the 
   count =17 and the count <20,  the event count meets fraud alert condition, tumbling window would not capture the count and would 
   not report fraud alert



### Hopping Window

  We know hopping window has the window size and advanced window size, window size is fixed and each time the window forward 
  advanced size. Advance Size is always smaller than window size.  From sampling theory, the sampling density is much more than 
  tumbling window that forward window size.
  For same count of rejected transaction started at about middle (8 seconds) , it always was captured by hopping window
  
  <img src="images/two-type-of-time-windows.png" width="80%" height="80%">

## Data Flow Chart
  
  <img src="images/work-flow-chart.png" width="80%" height="80%">
 
  
## System configuration and Settings 
## Project Structure
  We create one project container modules, one module contains hopping window kstream processor and another module is tumbling window
  processor. each modules are mostly same except the kstream processors. 
  Under main project directory (spring-boot-kafka-json-tumbling-hopping-window-aggregation-inventory), we have docker-compose.yml
  and restart.sh, run restart.sh to start docker-container 
  
  <img src="images/project-structure-two-modules.png" width="35%" height="35%">
  

  
  
## docker-compose.yml

   We need to enphase two points of docker-compose.yml, 
   1. We set KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092, and expose 9092, in our code we alse use thus external same host
      as bootstrap-server because we find spring boot @Kafkalistener of consumer, not care boostrap-server configure, only point 
      localhost:9092, when I use localhost:29092, it issued "127.0.0.1:9092 Node -1, broker connection refused" error message
   2. We need set kafka network explicity name, here is "kafka_same_host_net", otherwise docker compose take current directory
      spring-boot-kafka-json-tumbling-hopping-window-aggregation-inventory-default as network name, when we start similiar docker-
      compose.yml in different diectory name, it complained spring-boot-kafka-json-tumbling-hopping-window-aggregation-inventory-
      default not found , refuse start 
   
            version: '3'
            services:
              zookeeper:
                image: confluentinc/cp-zookeeper:6.0.0
                hostname: zookeeper
                container_name: zookeeper
                ports:
                  - "32181:32181"
                environment:
                  ZOOKEEPER_CLIENT_PORT: 32181
                  ZOOKEEPER_TICK_TIME: 2000
                networks:
                  - kafka_network
              kafka:
                image: confluentinc/cp-enterprise-kafka:6.0.0
                hostname: kafka
                container_name: kafka
                depends_on:
                  - zookeeper
                ports:
                  - "29092:29092"
                  - "9092:9092"
                environment:
                  KAFKA_BROKER_ID: 1
                  KAFKA_ZOOKEEPER_CONNECT: 'zookeeper:32181'
                  KAFKA_LISTENER_SECURITY_PROTOCOL_MAP: PLAINTEXT:PLAINTEXT,PLAINTEXT_HOST:PLAINTEXT
                  KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092,PLAINTEXT_HOST://localhost:29092
                  KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
                  KAFKA_GROUP_INITIAL_REBALANCE_DELAY_MS: 0
                  KAFKA_TRANSACTION_STATE_LOG_MIN_ISR: 1
                  KAFKA_TRANSACTION_STATE_LOG_REPLICATION_FACTOR: 1
                networks:
                  - kafka_network

            networks:
              kafka_network:
                name: kafka_same_host_net
  
## Data Modeling
   
### InventoryTransaction class
    Two states need us pay attention. First state is TranscationRequestState, If add item to inventory call ADD and take away and 
    ship to customer, call SHIPPING, second InventoryTransactionState, when create a transaction, state is CREATE, when aggregator
    call "processTransaction" , if trnsaction is SHIPPING request state and inventory quantity is not enough, transaction will be 
    "REJECT" state
   
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

    
### Inventory class
   
  In inventory method processTransaction(), it sets current transaction to lastTransaction, and then check if transaction 
  request state is SHIPPING and the quantity > inventory balance quantity then set the inventoryTransactionState as "REJECT"
  if the quantity <= inventory quantity, then inventory quantity minus transaction quantity. if request state is ADD, add anyway
  
  
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
          
### Potential Fraud Alert Class
        @Data
        @NoArgsConstructor
        @AllArgsConstructor
        public class PotentialFraudAlert {
            private Long itemId;
            private Long rejectedTransactionCount;
            private String message;
        }


## Detail Topology of Aggregation & Time Window 

  This part is core of this project. Basic logic is that consumed the transaction event stream, submitted the transaction to
  inventory, filter the rejected transactions and sink to rejected topic, applied time window, within 20 seconds, if rejected 
  transactions count>=10, create potential fraud alert objects and sink to fraud alert topic
  
  Following is detail topology flow
  
  <img src="images/topology-for-kstream-processor.png" width="90%" height="90%">
    
### Topology code as following 
  
### Inventory Hopping window kstream process  

            @EnableKafkaStreams
            @EnableKafka
            @Configuration
            @Slf4j
            public class InventoryHoppingWindowProcessor {

                @Bean
                @DependsOn(KafkaStreamsDefaultConfiguration.DEFAULT_STREAMS_CONFIG_BEAN_NAME)
                public KStream<Long, Inventory> kStream(StreamsBuilder streamsBuilder) {


                    KStream<Long, InventoryTransaction> inventoryTransactionKStream = 
                    streamsBuilder.stream(Constants.INVENTORY_TRANSACTIONS,
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
    
                    inventoryKStream.to(Constants.INVENTORY, Produced.with(Serdes.Long(), InventorySerdes.serdes()));

                    KStream<Long, InventoryTransaction> rejectedTransactionStream = inventoryKStream
                            .mapValues((readOnlyKey,value)->value.getLatestTransaction())
                            .filter((kay,value)->value.state== InventoryTransaction.InventoryTransactionState.REJECTED);


                    rejectedTransactionStream
                            .to(Constants.REJECTED_TRANSACTIONS, Produced.with(Serdes.Long(), InventoryTransactionSerdes.serdes()));

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
                            .peek(((key,value)->log.info("Peek Within 20 seconds and rejected times>=10 ,Hopping Window captured 
                            rejected inventory transactions as itemId {},  Count {}",key,value)))
                            .mapValues((key,value)->new PotentialFraudAlert(key,value,String.format("Hopping Window captured Potential 
                            Fraud Alerts as itemId %s Count %d",key,value)))
                            .to(Constants.POTENTIAL_FRAUD_ALERT,Produced.with(Serdes.Long(), PotentialFraudAlertSerdes.serdes()));
                    return inventoryKStream;
                }
            }
  
### Inventory Tumbling window kstream processor

   
   Ignore same parts as hopping window, only show time window code section as following 
   
   
   ...........
   
                 Duration tumblingWindowSize = Duration.ofSeconds(20L);
                rejectedTransactionStream
                        .groupByKey()
                        // tumbling window setting 20 second and grace means consider latency of system or network
                        .windowedBy(TimeWindows.of(tumblingWindowSize).grace(Duration.ofSeconds(0)))
                        .count()
                        .suppress(untilWindowCloses(unbounded()))
                        .toStream()
                        .map((key,value)-> KeyValue.pair(key.key(),value))
                        .filter((key,value)->value>=10)
                        .peek(((key,value)->log.info("Within 20 seconds and rejected times>=10 ,Tumbling Window captured rejected 
                        inventory transactions as itemId {},  Count {}",key,value)))
                        .mapValues((key,value)->new PotentialFraudAlert(key,value,String.format("Within 20 seconds and rejected 
                        times>=10, Tumbling Window captured rejected inventory transactions as itemId %s Count %d",key,value)))
                        .to(Constants.POTENTIAL_FRAUD_ALERT,Produced.with(Serdes.Long(), PotentialFraudAlertSerdes.serdes()));

              return inventoryKStream;
          }
## Detail information as below link

  [spring-boot kafka json tumbling & hopping window aggregation for inventory/](https://johnzhang320.com/spring-boot-kafka-json-tumbling-and-hopping-window-aggregation-for-inventory/)
 
  
 ## My Kafka Related Links
 
  [spring-boot-kafka-json-stateful-aggregation/](https://johnzhang320.com/spring-boot-kafka-json-stateful-aggregation)
  
  [spring-boot kafka json stream/](https://johnzhang320.com/spring-boot-kafka-json-stream)
  
  [Spring boot connects to AWS EC2 Kafka docker container/](https://johnzhang320.com/kafka-aws-ec2-kafka-docker/)
  
  [Spring-boot kafka event driven/](https://johnzhang320.com/spring-boot-kafka-event-driven)
 
  [Spring-boot kafka stream sorted unique word count/](https://johnzhang320.com/sorted-unique-word-count/)
 
  
   

