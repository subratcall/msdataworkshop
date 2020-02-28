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

