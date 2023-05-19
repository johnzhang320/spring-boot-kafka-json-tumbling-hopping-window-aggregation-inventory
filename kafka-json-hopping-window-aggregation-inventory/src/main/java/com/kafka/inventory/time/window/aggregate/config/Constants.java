package com.kafka.inventory.time.window.aggregate.config;

public interface Constants {

   // public static final String EXTERNAL_CONNECT_BOOSTRAP_SERVER = "localhost:29092";
   public static final String EXTERNAL_CONNECT_BOOSTRAP_SERVER = "localhost:9092";
    public static final String INVENTORY_TRANSACTIONS = "inventory-transactions";
    public static final String INVENTORY = "inventory";
    public static final String REJECTED_TRANSACTIONS = "rejected-transactions";

    public static final String INVENTORY_STORE = "inventory-store";

    public static final String POTENTIAL_FRAUD_ALERT = "potential-fraud-alert";
    public final static String CLIENT_ID_CONFIG= "json-stream-client";

    public final static String CONSUMER_GROUP_ID="consumer-inventory-group";



    public final static String APPLICATION_CONFIG_ID="kStream_json_config";


}
