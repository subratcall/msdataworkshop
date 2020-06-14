package io.helidon.data.examples;

import oracle.jms.*;

import javax.jms.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class InventoryServiceOrderEventConsumer implements Runnable {

    InventoryResource inventoryResource;

    public InventoryServiceOrderEventConsumer(InventoryResource inventoryResource) {
        this.inventoryResource = inventoryResource;
    }

    @Override
    public void run() {
        System.out.println("Receive Messages...");
            try {
                listenForOrderEvents();
            } catch (Exception e) {
                e.printStackTrace();
            }
    }

    public void listenForOrderEvents() throws Exception {
        QueueConnectionFactory qcfact = AQjmsFactory.getQueueConnectionFactory(inventoryResource.atpInventoryPDB);
        QueueConnection qconn = qcfact.createQueueConnection(inventoryResource.inventoryuser, inventoryResource.inventorypw);
        QueueSession qsess = qconn.createQueueSession(true, Session.CLIENT_ACKNOWLEDGE);
        qconn.start();
        receiveMessages(qsess);
    }

    private void receiveMessages(QueueSession qsess) throws JMSException {
        Queue queue = ((AQjmsSession) qsess).getQueue(inventoryResource.inventoryuser, inventoryResource.orderQueueName);
        AQjmsConsumer sub = (AQjmsConsumer) qsess.createConsumer(queue);
        boolean done = false;
        while (!done) {
            try {
                TextMessage orderMessage = (TextMessage) (sub.receiveNoWait());
                if (orderMessage != null) {
                    String txt = orderMessage.getText();
                    System.out.println("txt " + txt);
                    System.out.print("JMSPriority: " + orderMessage.getJMSPriority());
                    System.out.println("Priority: " + orderMessage.getIntProperty("Priority"));
                    System.out.print(" Message: " + orderMessage.getIntProperty("Id"));
                    Order order = JsonUtils.read(txt, Order.class);
                    System.out.print(" orderid:" + order.getOrderid());
                    System.out.print(" itemid:" + order.getItemid());
                    System.out.println("((AQjmsSession) qsess).getDBConnection(): " + ((AQjmsSession) qsess).getDBConnection());
                    updateDataAndSendEventOnInventory((AQjmsSession) qsess, order.getOrderid(), order.getItemid());
                } else {
                  //  done = true;
                }
                Thread.sleep(500);
            } catch (Exception e) {
                System.out.println("Error in performJmsOperations: " + e);
                qsess.rollback();
                done = true;
            }
        }
    }

    private void updateDataAndSendEventOnInventory(AQjmsSession session, String orderid, String itemid) throws Exception {
        String inventorylocation = InventoryResource.isDirectSupplierQuickTest ?
                (InventoryResource.inventorycount > 0 ?"Philadelphia": "noinventoryforitem") : evaluateInventory(session, itemid);
        Inventory inventory = new Inventory(orderid, itemid, inventorylocation,"lettuce"); //static suggestiveSale - represents an additional service/event
        String jsonString = JsonUtils.writeValueAsString(inventory);
        Topic inventoryTopic =  session.getTopic(InventoryResource.inventoryuser, InventoryResource.inventoryQueueName);
        System.out.println("send inventory status message... jsonString:" + jsonString + " inventoryTopic:" + inventoryTopic) ;
        TextMessage objmsg = session.createTextMessage();
        TopicPublisher publisher = session.createPublisher(inventoryTopic);
        objmsg.setIntProperty("Id", 1);
        objmsg.setIntProperty("Priority", 2);
        objmsg.setText(jsonString);
        objmsg.setJMSCorrelationID("" + 2);
        objmsg.setJMSPriority(2);
        publisher.publish(inventoryTopic, objmsg, DeliveryMode.PERSISTENT,2, AQjmsConstants.EXPIRATION_NEVER);
        session.commit();
        System.out.println("message sent");
    }

    //returns location if exists and "inventorydoesnotexist" otherwise
    private String evaluateInventory(AQjmsSession session, String itemid) throws JMSException, SQLException {
        Connection dbConnection = session.getDBConnection();
        System.out.println("-------------->evaluateInventory connection:" + dbConnection +
                "Session:" + session + " check inventory for inventoryid:" + itemid);
        int inventorycount;
        String inventoryLocation = "";
        ResultSet resultSet = dbConnection.createStatement().executeQuery(
                "select * from inventory  where inventoryid = '" + itemid + "'");
        if (resultSet.next()) {
            inventorycount = resultSet.getInt("inventorycount");
            inventoryLocation = resultSet.getString("inventorylocation");
            System.out.println("MessagingService.doIncomingOutgoing inventorycount:" + inventorycount);
        } else inventorycount = 0;
        String status = inventorycount > 0 ? "inventoryexists" : "inventorydoesnotexist";
        inventoryLocation = inventorycount > 0 ? inventoryLocation : "inventorydoesnotexist";
        System.out.println("InventoryServiceOrderEventConsumer.updateDataAndSendEventOnInventory status:" + status);
        return inventoryLocation;
    }


}
