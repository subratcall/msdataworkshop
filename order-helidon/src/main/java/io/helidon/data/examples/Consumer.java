package io.helidon.data.examples;

import java.util.Properties;
import java.time.Duration;
import java.util.Arrays;

import org.oracle.okafka.clients.consumer.*;

public class Consumer {

    public static void main(String[] args) {
        Properties props = new Properties();

        /*change the bootstrap server to point to the Oracle Database*/
        props.put("oracle.service.name", "mnisopbygm56hii_orderdb_tp.atp.oraclecloud.com"); //name of the service running on the instance
        props.put("oracle.instance.name", "orderdb_tp"); //name of the Oracle Database instance
        props.put("oracle.net.tns_admin", "/Users/pparkins/Downloads/Wallet_ORDERDB/");	//eg: "/user/home" if ojdbc.properies file is in home
        props.put("bootstrap.servers", "adb.us-phoenix-1.oraclecloud.com:1522"); //ip address or host name where instance running : port where instance listener running
        props.put("group.id", "subscriber");
        props.put("enable.auto.commit", "false");
        props.put("key.deserializer", "org.oracle.okafka.common.serialization.StringDeserializer");
        props.put("value.deserializer", "org.oracle.okafka.common.serialization.StringDeserializer");
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");
        props.put("max.poll.records", 500);

        KafkaConsumer<String, String> consumer = null;

        try {
            consumer = new KafkaConsumer<String, String>(props);
            consumer.subscribe(Arrays.asList("TOPIC1"));
            ConsumerRecords<String, String> records;

            records = consumer.poll(Duration.ofMillis(1000));

            for (ConsumerRecord<String, String> record : records)
            {
                System.out.println("topic = , key = , value = \n" +
                        record.topic()+ "\t" + record.key()+ "\t" + record.value());
            }
            consumer.commitSync();
        } catch(Exception ex) {
            ex.printStackTrace();
        }  finally {
            consumer.close();
        }
    }
}