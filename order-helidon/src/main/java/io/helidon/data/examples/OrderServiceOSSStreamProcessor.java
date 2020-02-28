package io.helidon.data.examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.serialization.StringDeserializer;

import java.util.*;

public class OrderServiceOSSStreamProcessor implements Runnable {
    OrderResource orderResource;

    public OrderServiceOSSStreamProcessor(OrderResource orderResource) {
        this.orderResource = orderResource;
    }

    public void run() {
        System.out.println("OrderServiceOSSStreamProcessor.run");
//        String authToken = System.getenv("AUTH_TOKEN");
//        String tenancyName = System.getenv("TENANCY_NAME");
//        String username = System.getenv("STREAMING_USERNAME");
//        String streamPoolId = System.getenv("COMPARTMENT_ID");
//        String topicName = System.getenv("TOPIC_NAME");
//        String tenancyName = "ocid1.tenancy.oc1..aaaaaaaaogcg5o3giyldy7yybxphgnodvlgr4xuxv7fn5d7prjvt67qqplia";
//        String username = "ocid1.user.oc1..aaaaaaaajliqydpf6pblr2k6a4ivazjtftmk6af5seozhkel3khbzmsxngpq";
//        String streamPoolId = "ocid1.compartment.oc1..aaaaaaaagf5galz7jxth4ef6m6zxk4lsvigpv7t7kox7gpthsp2irycptmka";
        String authToken = "}uC6hEgYpJT-)]7UV7cb";
        String tenancyName = "weblogicondocker";
        String username = "paulparkinson";
        String streamPoolId = "ocid1.streampool.oc1.phx.amaaaaaagu6anria6zgrt4esdg6aibvi6zshwumji4nlb5735egsx5sfc5ea";
        String topicName = "helidon-oss-kafka";

        Properties properties = new Properties();
        properties.put("bootstrap.servers", "streaming.us-phoenix-1.oci.oraclecloud.com:9092");
        properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaExampleConsumer");
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "PLAIN");
        properties.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class.getName());
        properties.put("sasl.jaas.config",
                "org.apache.kafka.common.security.plain.PlainLoginModule required username=\""
                        + tenancyName + "/"
                        + username + "/"
                        + streamPoolId + "\" "
                        + "password=\""
                        + authToken + "\";"
        );
        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        consumer.subscribe(Collections.singletonList(topicName));
        final int giveUp = 100;   int noRecordsCount = 0;
        boolean isStopProcessing = false;
        while (!isStopProcessing) {
            final ConsumerRecords<String, String> consumerRecords =
                    consumer.poll(1000);

            if (consumerRecords.count() > 0) {

            consumerRecords.forEach(record -> {
//                System.out.printf("Consume and process food order record :(%d, %s, %d, %d)\n",
//                        record.key(), record.value(),
//                        record.partition(), record.offset());
                System.out.printf("Processing food order (todo all stream orders are given orderid 101 currently)" +
                                record.key() + ":" + record.value());
                try {
                    orderResource.insertOrderAndSendEvent("101", "4");
                } catch (Exception e) {
                    e.printStackTrace(); //todo handle
                }
            });

            consumer.commitAsync();
            } else {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
        consumer.close();
        System.out.println("DONE");

    }
}

