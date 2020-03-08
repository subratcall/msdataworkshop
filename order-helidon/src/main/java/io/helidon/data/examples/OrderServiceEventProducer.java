package io.helidon.data.examples;

import oracle.jms.AQjmsAgent;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;

import oracle.soda.OracleException;
import oracle.soda.rdbms.OracleRDBMSClient;

public class OrderServiceEventProducer {


    public String updateDataAndSendEvent(
            DataSource dataSource, String orderid, String itemid, String deliverylocation) throws Exception {
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

            insertOrderViaSODA(orderid, itemid, deliverylocation, jdbcConnection);

            Queue queue = ((AQjmsSession) session).getQueue(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            QueueSender sender = ((AQjmsSession) session).createSender(queue);
            Message msg = session.createTextMessage();
            msg.setStringProperty("itemid", itemid);
            msg.setStringProperty("orderid", orderid);
            msg.setStringProperty("deliverylocation", deliverylocation);
            msg.setIntProperty("orderid", Integer.valueOf(itemid));



            AQjmsAgent agt = new AQjmsAgent("", destinationqueuename + "@" + linkName);


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

    public String updateDataAndSendEvent0(
            DataSource dataSource, String orderid, String itemid, String deliverylocation) throws Exception {
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

            insertOrderViaSODA(orderid, itemid, deliverylocation, jdbcConnection);

            Queue queue = ((AQjmsSession) session).getQueue(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            QueueSender sender = ((AQjmsSession) session).createSender(queue);
            Message msg = session.createTextMessage();
            msg.setStringProperty("itemid", itemid);
            msg.setStringProperty("orderid", orderid);
            msg.setStringProperty("deliverylocation", deliverylocation);
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

    private void insertOrderViaSODA(String orderid, String itemid, String deliverylocation, Connection jdbcConnection)
            throws OracleException {
        oracle.soda.OracleDatabase db = new OracleRDBMSClient().getDatabase(jdbcConnection);
        String collectionName = "orderid" + orderid;
        oracle.soda.OracleCollection collection = db.openCollection(collectionName);
        if (collection == null) collection = db.admin().createCollection(collectionName);
        String jsonString = "{ \"orderid\" : \"" + orderid + "\", \"item\" : " + itemid +
                "\", \"deliverylocation\" : " + deliverylocation + " }";
        collection.insert(db.createDocumentFromString(jsonString));
//        OracleOperationBuilder key = collection.find().key(orderid);
//        OracleDocument one = key.getOne();
    }

}
