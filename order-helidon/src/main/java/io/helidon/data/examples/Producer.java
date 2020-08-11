package io.helidon.data.examples;

import java.util.Properties;
import org.oracle.okafka.clients.producer.*;

public class Producer {


    /**
     Aug 10, 2020 1:11:23 PM org.oracle.okafka.common.utils.AppInfoParser <clinit>
     WARNING: Error while loading kafka-version.properties :inStream parameter is null
     Aug 10, 2020 1:11:23 PM org.oracle.okafka.common.utils.AppInfoParser$AppInfo <init>
     INFO: Kafka version : unknown
     Aug 10, 2020 1:11:23 PM org.oracle.okafka.common.utils.AppInfoParser$AppInfo <init>
     INFO: Kafka commitId : unknown
     Producing messages 10
     Aug 10, 2020 1:11:26 PM org.oracle.okafka.clients.producer.internals.SenderThread run
     SEVERE: [Producer clientId=producer-1] Uncaught error in kafka producer I/O thread:
     java.lang.NullPointerException
     at org.oracle.okafka.clients.NetworkClient.initiateConnect(NetworkClient.java:439)
     at org.oracle.okafka.clients.NetworkClient.access$700(NetworkClient.java:60)
     at org.oracle.okafka.clients.NetworkClient$DefaultMetadataUpdater.maybeUpdate(NetworkClient.java:565)
     at org.oracle.okafka.clients.NetworkClient$DefaultMetadataUpdater.maybeUpdate(NetworkClient.java:509)
     at org.oracle.okafka.clients.NetworkClient.maybeUpdateMetadata(NetworkClient.java:375)
     at org.oracle.okafka.clients.producer.internals.SenderThread.run(SenderThread.java:170)
     at org.oracle.okafka.clients.producer.internals.SenderThread.run(SenderThread.java:131)
     at java.base/java.lang.Thread.run(Thread.java:834)
     */
    public static void main(String[] args) {

        KafkaProducer<String,String> prod = null;
        int msgCnt =10;
        Properties props = new Properties();

        props.put("inStream", "false");
        /* change the properties to point to the Oracle Database */
        props.put("oracle.service.name", "mnisopbygm56hii_orderdb_tp.atp.oraclecloud.com"); //name of the service running on the instance
        props.put("oracle.instance.name", "ORDERDB"); //name of the Oracle Database instance
        props.put("oracle.net.tns_admin", "/Users/pparkins/Downloads/Wallet_ORDERDB/");	//eg: "/user/home" if ojdbc.properies file is in home
        props.put("bootstrap.servers", "adb.us-phoenix-1.oraclecloud.com:1522"); //ip address or host name where instance running : port where instance listener running
        props.put("linger.ms", 1000);

//        security.protocol = "SSL"
        props.put("security.protocol", "SSL"); //name of the Oracle Database instance
//        oracle.net.tns_admin = "location of tnsnames.ora file"  (for parsing JDBC connection string)
//        tns.alias = "alias of connection string in tnsnames.ora"
        props.put("tns.alias", "orderdb_tp"); //name of the Oracle Database instance

        props.put("key.serializer", "org.oracle.okafka.common.serialization.StringSerializer");
        props.put("value.serializer", "org.oracle.okafka.common.serialization.StringSerializer");
//        props.put("key.serializer", "org.apache.kafka.common.serialization.StringSerializer");
//        props.put("value.serializer", "org.apache.kafka.common.serialization.StringSerializer");

        prod=new KafkaProducer<String, String>(props);
        try {
            System.out.println("Producing messages " + msgCnt);
            for(int j=0;j < msgCnt; j++) {
                prod.send(new ProducerRecord<String, String>("TOPIC1" , "Key","This is new message"+j));
            }

            System.out.println("Messages sent " );

        } catch(Exception ex) {
            ex.printStackTrace();
        } finally {
            prod.close();
        }
    }
}
