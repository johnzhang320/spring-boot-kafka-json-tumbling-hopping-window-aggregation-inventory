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
    .....

### Hopping Window

   We know hopping window has the window size and advanced window size, window size is fixed and each time the window forward a
   .....

## Detail information, diagrams, system settinf and running, code analysis, testing data and result analysis as below link

  [spring-boot kafka json tumbling & hopping window aggregation for inventory/](https://johnzhang320.com/spring-boot-kafka-json-tumbling-and-hopping-window-aggregation-for-inventory/)
 
  
 ## My Kafka Related Links
 
  [spring-boot-kafka-json-stateful-aggregation/](https://johnzhang320.com/spring-boot-kafka-json-stateful-aggregation)
  
  [spring-boot kafka json stream/](https://johnzhang320.com/spring-boot-kafka-json-stream)
  
  [Spring boot connects to AWS EC2 Kafka docker container/](https://johnzhang320.com/kafka-aws-ec2-kafka-docker/)
  
  [Spring-boot kafka event driven/](https://johnzhang320.com/spring-boot-kafka-event-driven)
 
  [Spring-boot kafka stream sorted unique word count/](https://johnzhang320.com/sorted-unique-word-count/)
 
  
   

