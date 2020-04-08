package io.helidon.data.examples;

import oracle.jms.AQjmsConstants;
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
        System.out.println("sendMessage enter dataSource:" + dataSource +
                ", itemid:" + itemid + ", orderid:" + orderid +
                ",queueOwner:" + OrderResource.orderQueueOwner + "queueName:" + OrderResource.orderQueueName);
        TopicSession session = null;
        try {
            TopicConnectionFactory q_cf = AQjmsFactory.getTopicConnectionFactory(dataSource);
            TopicConnection q_conn = q_cf.createTopicConnection();
            session = q_conn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            Connection jdbcConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("sendMessage jdbcConnection:" + jdbcConnection);
            String jsonString = "{ \"orderid\" : \"" + orderid + "\", \"item\" : " + itemid +
                    "\", \"deliverylocation\" : " + deliverylocation + " }";
            insertOrderViaSODA(orderid, itemid, deliverylocation, jdbcConnection);
            Topic topic = ((AQjmsSession) session).getTopic(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            System.out.println("Send order messages...");
            TextMessage objmsg = session.createTextMessage();
            TopicPublisher publisher = session.createPublisher(topic);
            objmsg.setIntProperty("Id", 1);
            objmsg.setStringProperty("orderid", orderid);
            objmsg.setStringProperty("itemid", itemid);
            objmsg.setStringProperty("deliverylocation", deliverylocation);
            objmsg.setIntProperty("Priority", 2);
            objmsg.setText(jsonString);
            objmsg.setJMSCorrelationID("" + 1);
            objmsg.setJMSPriority(2);
            publisher.publish(topic, objmsg, DeliveryMode.PERSISTENT,2, AQjmsConstants.EXPIRATION_NEVER);
            session.commit();
            System.out.println("committed JSON order in database and sent message in the same tx with payload:" +   jsonString);
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

    private void insertOrderViaSODA(String orderid, String itemid, String deliverylocation,
                                    Connection jdbcConnection)
            throws OracleException {
        Order order = new Order(orderid, itemid, deliverylocation);
        new OrderDAO().create(jdbcConnection, order);
    }

}
