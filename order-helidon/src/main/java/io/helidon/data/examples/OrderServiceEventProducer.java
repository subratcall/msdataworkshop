package io.helidon.data.examples;

import oracle.jms.AQjmsConstants;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import javax.sql.DataSource;
import java.sql.Connection;

import oracle.soda.OracleException;

class OrderServiceEventProducer {


     String updateDataAndSendEvent(
            DataSource dataSource, String orderid, String itemid, String deliverylocation) throws Exception {
        System.out.println("updateDataAndSendEvent enter dataSource:" + dataSource +
                ", itemid:" + itemid + ", orderid:" + orderid +
                ",queueOwner:" + OrderResource.orderQueueOwner + "queueName:" + OrderResource.orderQueueName);
        TopicSession session = null;
        try {
            TopicConnectionFactory q_cf = AQjmsFactory.getTopicConnectionFactory(dataSource);
            TopicConnection q_conn = q_cf.createTopicConnection();
            session = q_conn.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            Connection jdbcConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("updateDataAndSendEvent jdbcConnection:" + jdbcConnection + " about to insertOrderViaSODA...");
            Order insertedOrder = insertOrderViaSODA(orderid, itemid, deliverylocation, jdbcConnection);
            System.out.println("updateDataAndSendEvent insertOrderViaSODA complete about to send order message...");
            Topic topic = ((AQjmsSession) session).getTopic(OrderResource.orderQueueOwner, OrderResource.orderQueueName);
            System.out.println("updateDataAndSendEvent topic:" + topic);
            TextMessage objmsg = session.createTextMessage();
            TopicPublisher publisher = session.createPublisher(topic);
            objmsg.setIntProperty("Id", 1);
            objmsg.setIntProperty("Priority", 2);
            String jsonString = JsonUtils.writeValueAsString(insertedOrder);
            objmsg.setText(jsonString);
            objmsg.setJMSCorrelationID("" + 1);
            objmsg.setJMSPriority(2);
            publisher.publish(topic, objmsg, DeliveryMode.PERSISTENT,2, AQjmsConstants.EXPIRATION_NEVER);
            session.commit();
            System.out.println("updateDataAndSendEvent committed JSON order in database and sent message in the same tx with payload:" + jsonString);
            return topic.toString();
        } catch (Exception e) {
            System.out.println("updateDataAndSendEvent failed with exception:" + e +
                    " (will attempt rollback if session is not null) session:" + session);
            if (session != null) {
                try {
                    session.rollback();
                } catch (JMSException e1) {
                    System.out.println("updateDataAndSendEvent session.rollback() failed:" + e1);
                } finally {
                    throw e;
                }
            }
            throw e;
        } finally {
            if (session != null) session.close();
        }
    }

    private Order insertOrderViaSODA(String orderid, String itemid, String deliverylocation,
                                    Connection jdbcConnection)
            throws OracleException {
        Order order = new Order(orderid, itemid, deliverylocation, "pending", "", "");
        new OrderDAO().create(jdbcConnection, order);
        return order;
    }

    void updateOrderViaSODA(Order order, Connection jdbcConnection)
            throws OracleException {
        new OrderDAO().update(jdbcConnection, order);
    }


    String deleteOrderViaSODA( DataSource dataSource, String orderid) throws Exception {
        new OrderDAO().delete(dataSource.getConnection(), orderid);
        return "deleteOrderViaSODA success";
    }


    String dropOrderViaSODA( DataSource dataSource) throws Exception {
        return new OrderDAO().drop(dataSource.getConnection());
    }

    Order getOrderViaSODA( DataSource dataSource, String orderid) throws Exception {
        Connection jdbcConnection = dataSource.getConnection();
        Order order = new OrderDAO().get(jdbcConnection, orderid);
        jdbcConnection.close();
        return order;
    }

}
