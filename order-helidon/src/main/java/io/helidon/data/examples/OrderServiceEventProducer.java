package io.helidon.data.examples;

import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.CallableStatement;
import java.sql.Connection;

import oracle.soda.OracleDocument;
import oracle.soda.OracleException;
import oracle.soda.OracleOperationBuilder;
import oracle.soda.rdbms.OracleRDBMSClient;

public class OrderServiceEventProducer {

    public String updateDataAndSendEventToTopic(
            DataSource dataSource, String messageTxt, String tableName, String action) throws Exception {
        System.out.println("sendMessage enter dataSource:"+dataSource+
                ", messageTxt:"+ messageTxt +", action:"+ action + ", tableName:"+ tableName +
                ",queueOwner:"+ OrderResource.orderQueueOwner + "queueName:"+ OrderResource.orderQueueName );
        Session session = null;
        try {
            TopicConnectionFactory topicConnectionFactory = AQjmsFactory.getTopicConnectionFactory(dataSource);
            TopicConnection topicConnection = topicConnectionFactory.createTopicConnection();
            session = topicConnection.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            Connection jdbcConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("sendMessage jdbcConnection:" + jdbcConnection);
            insertOrderViaSODA(messageTxt, action, jdbcConnection);
            Topic topic = ((AQjmsSession) session).getTopic(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            TopicPublisher publisher = ((AQjmsSession) session).createPublisher(topic);
            Message msg = session.createTextMessage();
            msg.setStringProperty("messageTxt", messageTxt);
            msg.setStringProperty("action", action);
            msg.setIntProperty("orderid", Integer.valueOf(messageTxt));
            publisher.publish(msg);
            session.commit();
            System.out.println("sendMessage committed messageTxt:" + messageTxt + " on topic:" + topic);
            session.close();
            topicConnection.close();
            return topic.toString();
        } catch (Exception e) {
            System.out.println("sendMessage failed " +
                    "(will attempt rollback if session is not null):" + e + " session:" + session);
            e.printStackTrace();
            if (session != null) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    System.out.println("sendMessage session.rollback() failed:" + e1);
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

    public String updateDataAndSendEvent(
            DataSource dataSource, String orderid, String itemid) throws Exception {
        System.out.println("sendMessage enter dataSource:"+dataSource+
                ", itemid:"+ itemid +", orderid:"+ orderid +
                ",queueOwner:"+ OrderResource.orderQueueOwner + "queueName:"+ OrderResource.orderQueueName );
        Session session = null;
        try {
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(dataSource);
            QueueConnection q_conn = q_cf.createQueueConnection();
            session = q_conn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);

            Connection jdbcConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("sendMessage jdbcConnection:" + jdbcConnection);

            insertOrderViaSODA(orderid, itemid, jdbcConnection);

            Queue queue = ((AQjmsSession) session).getQueue(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            QueueSender sender = ((AQjmsSession) session).createSender(queue);
            Message msg = session.createTextMessage();
            msg.setStringProperty("itemid", itemid);
            msg.setStringProperty("orderid", orderid);
            msg.setIntProperty("orderid", Integer.valueOf(itemid));
            sender.send(msg);
            session.commit();
            System.out.println("sendMessage committed itemid:" + itemid + " on queue:" + queue);
            session.close();
            q_conn.close();
            return queue.toString();
        } catch (Exception e) {
            System.out.println("sendMessage failed " +
                    "(will attempt rollback if session is not null):" + e + " session:" + session);
            e.printStackTrace();
            if (session != null) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    System.out.println("sendMessage session.rollback() failed:" + e1);
                    e1.printStackTrace();
                }
            }
            return null;
        }
    }

    private void insertOrderViaSODA(String orderid, String itemid, Connection jdbcConnection) throws OracleException {
        oracle.soda.OracleDatabase db = new OracleRDBMSClient().getDatabase(jdbcConnection);
        String collectionName = "orderid" + orderid;
        oracle.soda.OracleCollection collection = db.openCollection(collectionName);
        if (collection == null) collection = db.admin().createCollection(collectionName);
        String jsonString = "{ \"orderid\" : \"" + orderid + "\", \"item\" : " + itemid + " }";
        collection.insert(db.createDocumentFromString(jsonString));
//        OracleOperationBuilder key = collection.find().key(orderid);
//        OracleDocument one = key.getOne();
    }

}
