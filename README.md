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
     time to reflect real keys fraud alert
     
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
  
  
  
  
  
## Detail information, diagrams, settings and running, code analysis, testing result analysis as below link

  [spring-boot kafka json tumbling & hopping window aggregation for inventory/](https://johnzhang320.com/spring-boot-kafka-json-tumbling-and-hopping-window-aggregation-for-inventory/)
 
  
 ## My Kafka Related Links
 
  [spring-boot-kafka-json-stateful-aggregation/](https://johnzhang320.com/spring-boot-kafka-json-stateful-aggregation)
  
  [spring-boot kafka json stream/](https://johnzhang320.com/spring-boot-kafka-json-stream)
  
  [Spring boot connects to AWS EC2 Kafka docker container/](https://johnzhang320.com/kafka-aws-ec2-kafka-docker/)
  
  [Spring-boot kafka event driven/](https://johnzhang320.com/spring-boot-kafka-event-driven)
 
  [Spring-boot kafka stream sorted unique word count/](https://johnzhang320.com/sorted-unique-word-count/)
 
  
   

