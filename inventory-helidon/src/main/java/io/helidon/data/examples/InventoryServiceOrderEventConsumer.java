package io.helidon.data.examples;

import oracle.jms.*;

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
            try {
                listenForOrderEvents();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void listenForOrderEvents() throws Exception {
        QueueConnectionFactory qcfact = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPDB);
        QueueConnection qconn = qcfact.createQueueConnection(inventoryResource.inventoryuser, inventoryResource.inventorypw);
        QueueSession qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
        System.out.println("PropagationSetup.setup qsess:" + qsess);
        qconn.start();
        receiveMessages(qsess);
    }

    private void receiveMessages(QueueSession qsess) throws JMSException {
        System.out.println("Receive Messages...");
        Queue queue1 = ((AQjmsSession) qsess).getQueue("inventoryuser", "orderqueue");
        AQjmsConsumer sub = (AQjmsConsumer) qsess.createConsumer(queue1);
        System.out.println("\n\nMessages for subscriber : " + sub + "  with selector: " + sub.getMessageSelector());
        boolean done = false;
        while (!done) {
            try {
                TextMessage robjmsg = (TextMessage) (sub.receiveNoWait());
                if (robjmsg != null) {
                    String rTextMsg = robjmsg.getText();
                    System.out.println("rTextMsg " + rTextMsg);
                    System.out.print(" Pri: " + robjmsg.getJMSPriority());
                    System.out.print(" Message: " + robjmsg.getIntProperty("Id"));
                    System.out.print(" " + robjmsg.getStringProperty("City"));
                    System.out.println(" " + robjmsg.getIntProperty("Priority"));
                    System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                    if (false) updateDataAndSendEventOnInventory(
                            robjmsg.getStringProperty("itemid"), robjmsg.getStringProperty("orderid"));
                } else {
                    done = true;
                }
                qsess.commit();
            } catch (Exception e) {
                System.out.println("Error in performJmsOperations: " + e);
                done = true;
            }
        }
    }

    public String updateDataAndSendEventOnInventory(String itemid, String orderid) {
        Session session = null;
        try {
            System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory " +
                    "itemid = [" + itemid + "], orderid = [" + orderid + "]");
            QueueConnectionFactory q_cf = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPDB);
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
                inventoryLocation = resultSet.getString("inventorylocation");
                System.out.println("MessagingService.doIncomingOutgoing inventorycount:" + inventorycount);
            } else inventorycount = 0;
            String status = inventorycount > 0 ? "inventoryexists" : "inventorydoesnotexist";
            System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory status:" + status);
            Queue queue = ((AQjmsSession) session).getQueue(InventoryResource.inventoryuser, InventoryResource.inventoryQueueName);
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


    private static void sendMessages(TopicSession tsess, Topic topic1) throws JMSException {
        System.out.println("Publish messages...");
        TextMessage objmsg = tsess.createTextMessage();
        TopicPublisher publisher = tsess.createPublisher(topic1);
        int i = 1;
        objmsg.setIntProperty("Id", i);
        objmsg.setStringProperty("City", "Philadelphia");
        objmsg.setIntProperty("Priority", (1 + (i % 3)));
        objmsg.setText(i + ":" + "message# " + i + ":" + 500);
        objmsg.setJMSCorrelationID("" + i);
        objmsg.setJMSPriority(1 + (i % 3));
        publisher.publish(topic1, objmsg, DeliveryMode.PERSISTENT,
                1 + (i % 3), AQjmsConstants.EXPIRATION_NEVER);
        publisher.publish(topic1, objmsg, DeliveryMode.PERSISTENT, 3, AQjmsConstants.EXPIRATION_NEVER);
        System.out.println("Commit now and sleep...");
        tsess.commit();
    }
}
