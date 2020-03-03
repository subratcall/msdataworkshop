package io.helidon.data.examples;

import oracle.jms.AQjmsDestination;
import oracle.jms.AQjmsDestinationProperty;
import oracle.jms.AQjmsFactory;
import oracle.jms.AQjmsSession;

import javax.jms.*;
import java.sql.Connection;
import java.sql.ResultSet;

public class InventoryServiceOrderEventConsumer implements Runnable {

    InventoryResource inventoryResource;

    public InventoryServiceOrderEventConsumer(InventoryResource inventoryResource) {
        this.inventoryResource = inventoryResource;
    }

    @Override
    public void run() {
        while (true) {
            listenForOrderEvents();
        }
    }

    public Object listenForOrderEventsOnTopic() {
        TopicConnection connection = null;
        javax.jms.Topic topic;
        TopicSession session = null;
        try {
            System.out.println("listenForMessages on order queue...");
            TopicConnectionFactory topicConnectionFactory = AQjmsFactory.getTopicConnectionFactory(inventoryResource.atpInventoryPdb);
//            TopicConnectionFactory topicConnectionFactory = AQjmsFactory.getTopicConnectionFactory(inventoryResource.atpOrderPdb);
            connection = topicConnectionFactory.createTopicConnection();
            session = connection.createTopicSession(true, Session.CLIENT_ACKNOWLEDGE);
            topic = ((AQjmsSession) session).getTopic("inventoryuser", "ordertopic");
//            topic = ((AQjmsSession) session).getTopic("orderuser", "orderqueue");
            TopicSubscriber topicSubscriber  =
                    ((AQjmsSession) session).createDurableSubscriber(topic, "the name used to identify this subscription"); // ((AQjmsSession) session).createTopicReceiver(topic);
            connection.start();
            System.out.println("listenForMessages before topicSubscriber topic:" + topic);
            Message msg = topicSubscriber.receive();
            System.out.println("listenForMessages message received:" + msg);
//            updateDataAndSendEventOnInventory( "inventoryitem1", "66");
            session.commit();
            session.close();
            connection.close();
            return "got message";
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (session != null) session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public Object listenForOrderEvents() {
        QueueConnection connection = null;
        javax.jms.Queue queue;
        QueueSession session = null;
        try {
            System.out.println("listenForMessages on order queue...");
            QueueConnectionFactory queueConnectionFactory = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPdb);
            connection = queueConnectionFactory.createQueueConnection();
            session = connection.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            queue = ((AQjmsSession) session).getQueue("demouser", "orderqueue");
            MessageConsumer consumer = session.createConsumer(queue);
            connection.start();
            System.out.println("listenForMessages before receive queue:" + queue);
            Message msg = consumer.receive();
            System.out.println("listenForMessages message received:" + msg);
            updateDataAndSendEventOnInventory(
                    msg.getStringProperty("itemid"), msg.getStringProperty("orderid"));
            session.commit();
            session.close();
            connection.close();
            return "got message";
        } catch (Exception e) {
            e.printStackTrace();
            try {
                if (session != null) session.rollback();
            } catch (JMSException e1) {
                e1.printStackTrace();
            }
        } finally {
            try {
                if (session != null) {
                    session.close();
                }
                if (connection != null) {
                    connection.close();
                }
            } catch (JMSException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

    public String updateDataAndSendEventOnInventory(String itemid, String orderid) {
        Session session = null;
        try {
            System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory " +
                    "itemid = [" + itemid + "], orderid = [" + orderid + "]");
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPdb);
            QueueConnection q_conn = q_cf.createQueueConnection();
            session = q_conn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
            Connection dbConnection = ((AQjmsSession) session).getDBConnection();
            System.out.println("sendMessage dbConnection:" + dbConnection);
            System.out.println("-------------->MessagingService.doIncomingOutgoing connection:" + dbConnection +
                    "Session:" + session + " check inventory for inventoryid:" + itemid);
            int inventorycount;
            String inventoryLocation = "";
            // todo this should be increment decrement, not select update...
            ResultSet resultSet = dbConnection.createStatement().executeQuery(
                    "select * from inventory  where inventoryid = '" + itemid + "'");
            if (resultSet.next()) {
                inventorycount = resultSet.getInt("inventorycount");
                inventoryLocation = resultSet.getString("inventoryLocation");
                System.out.println("MessagingService.doIncomingOutgoing inventorycount:" + inventorycount);
            } else inventorycount = 0;
            String status = inventorycount > 0 ? "inventoryexists" : "inventorydoesnotexist";
//            dbConnection.createStatement().executeUpdate(
//                    "insert into " + OrderResource.tableName + " values ('" + orderid + "', '" + inventoryid + "', " + ordercount + ", '" + status + "') ");
            System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory status:" + status);
            Queue queue = ((AQjmsSession) session).getQueue(InventoryResource.inventoryQueueOwner, InventoryResource.inventoryQueueName);
            ((AQjmsDestination) queue).start(session, true, true);
            QueueSender sender = ((AQjmsSession) session).createSender(queue);
            TextMessage msg = session.createTextMessage();
            msg.setStringProperty("orderid", orderid);
            msg.setStringProperty("action", status);
            msg.setStringProperty("inventorylocation", inventoryLocation);
            sender.send(msg);
            session.commit();
            System.out.println("sendMessage committed for inventoryid:" + itemid +
                    " orderid:" + orderid + " status:" + status + " on queue:" + queue);
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
}
