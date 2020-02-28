package io.helidon.data.examples;

import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.PartitionInfo;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;

import java.util.*;

public class OrderServiceTestStreamingOrders implements Runnable{

    int numberofitemstostream;

    public OrderServiceTestStreamingOrders(int numberofitemstostream) {
        this.numberofitemstostream = numberofitemstostream;
    }

    public void run() {
        Properties properties = new Properties();
        properties.put("bootstrap.servers", "streaming.us-phoenix-1.oci.oraclecloud.com:9092");
        properties.put("security.protocol", "SASL_SSL");
        properties.put("sasl.mechanism", "PLAIN");
        properties.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
        properties.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class.getName());
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

        properties.put("retries", 5); // retries on transient errors and load balancing disconnection
        properties.put("max.request.size", 1024 * 1024); // limit request size to 1MB

        KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(properties);
        Map<String, List<PartitionInfo>> topics = consumer.listTopics();
        System.out.println("OSSKafkaProducer.produce consumer.listTopics():" + topics);
        Iterator<Map.Entry<String, List<PartitionInfo>>> iterator = topics.entrySet().iterator();
        while(iterator.hasNext()) {
            Map.Entry<String, List<PartitionInfo>> next = iterator.next();
            System.out.println("OSSKafkaProducer.produce next:" + next);
            System.out.println("OSSKafkaProducer.produce topic:" + next.getValue() + ":" + topics.get(next));
        }
        consumer.close();

        KafkaProducer producer = new KafkaProducer<>(properties);
        for (int i = 0; i < numberofitemstostream; i++) {
            ProducerRecord<String, String> record =
                    new ProducerRecord<>(topicName, UUID.randomUUID().toString(), "Test food record #" + i);

            System.out.println("OSSKafkaProducer.produce record:" + record);
            producer.send(record, (md, ex) -> {
                if( ex != null ) {
                    ex.printStackTrace();
                }
                else {
                    System.out.println(
                            "Sent msg to "
                                    + md.partition()
                                    + " with offset "
                                    + md.offset()
                                    + " at "
                                    + md.timestamp()
                    );
                }
            });
        }
        producer.flush();
        producer.close();
        System.out.println("produced 5 messages");
    }

}
